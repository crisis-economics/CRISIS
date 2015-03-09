/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 *
 * CRISIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CRISIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.crisis_economics.abm.markets.clearing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.security.InvalidAlgorithmParameterException;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.matching.Matching;
import eu.crisis_economics.abm.algorithms.matching.MatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.SimpleNode;
import eu.crisis_economics.abm.contracts.settlements.GoodsForCashTransaction;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarketOrder.Side;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A simple instrument for trading (durable) goods.<br><br>
  * 
  * This instrument processes bid and ask orders from {@link GoodsBuyer} and {@link GoodsSeller}
  * participants, respectively. {@link GoodsSeller} participants must be registered as sellers 
  * with this instrument before they will be permitted to submit ask orders. Registration is
  * performed by calling {@link #registerSeller(GoodsSeller)}. <br><br>
  * 
  * This instrument provides basic on-demand historical trade data. In particular, 
  * {@link #getTradingPrices()} returns an ordered list of historical trade-weighted
  * selling prices (per unit goods) and {@link #getTradingVolumes()} provides an ordered
  * list of effective trade volumes observed for each processing of this instrument.
  * The {@code zeroth} record in these {@link List}{@code s} is the oldest measurement.
  * <br><br>
  * 
  * This instrument cannot provide trade-weighted price data at any time before the 
  * instrument has been processed for the first time, because no such trades are
  * known until the first instrument processing session. For this reason, any call to 
  * {@link #getTradingPrices()} <i>before</i> the first call to {@link #matchOrders()} will 
  * return a {@link List} of size {@code 1} containing the average selling price over all
  * sellers who were registered with this instrument at time {@code T = 0}, 
  * {@link NamedEventOrderings#BEFORE_ALL}. This average is simply the sum of such selling 
  * ask prices divided by the number of {@link GoodsSeller}{@code s} registered with this
  * instrument at that time. If no {@link GoodsSeller}{@code s} are registered with this 
  * instrument at that time, the selling price is taken to be zero.
  * 
  * @author phillips
  */
public final class SimpleGoodsInstrument {
   
   private static final int
      TRADE_MEMORY_LENGTH_NUMBER_OF_SESSIONS = 20;
   
   private String
      goodsType;
   
   private Map<String, GoodsSeller>
      registeredSellers;
   
   private Map<String, SimpleGoodsMarketOrder>
      bidOrders,
      askOrders;
   
   private Deque<Double>
      historicalTradingPricesSeries,
      historicalTradingVolumeSeries;
   
   private Map<String, Double>
      lastMarketShareBySeller;
   
   private double
      lastAggregateDemand,
      aggreateDemandNow;
   
   private MatchingAlgorithm
      matchingAlgorithm;
   
   /**
     * Create a {@link SimpleGoodsInstrument} object. See also {@link SimpleGoodsInstrument}.
     * 
     * @param goodsType
     *        The type of goods represented by this instrument
     * @param matchingAlgorithm
     *        The {@link MatchingAlgorithm} used to determine trades between
     *        {@link GoodsBuyer}{@code s} and {@link GoodsSeller}{@code s}.
     */
   public SimpleGoodsInstrument(
      String goodsType,
      final MatchingAlgorithm matchingAlgorithm
      ) {
      Preconditions.checkArgument(!goodsType.isEmpty());
      this.goodsType = Preconditions.checkNotNull(goodsType);
      this.registeredSellers = new LinkedHashMap<String, GoodsSeller>();
      this.bidOrders = new LinkedHashMap<String, SimpleGoodsMarketOrder>();
      this.askOrders = new LinkedHashMap<String, SimpleGoodsMarketOrder>();
      this.historicalTradingPricesSeries = new ArrayDeque<Double>();
      this.historicalTradingVolumeSeries = new ArrayDeque<Double>();
      this.matchingAlgorithm = matchingAlgorithm;
      this.lastMarketShareBySeller = new HashMap<String, Double>();
      
      historicalTradingPricesSeries.add(0.);
      historicalTradingVolumeSeries.add(0.);
      
      Simulation.once(
         this, "addInitialHistoricalTradeData", NamedEventOrderings.BEFORE_ALL);
      
      this.resetMemories();
      Simulation.repeat(this, "resetMemories", NamedEventOrderings.AFTER_ALL);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void addInitialHistoricalTradeData() {
      if(registeredSellers.size() != 0) {
         double
            meanSellingPrice = 0.;
         for(final GoodsSeller seller : registeredSellers.values())
            meanSellingPrice += seller.getGoodsSellingPrice();
         meanSellingPrice /= registeredSellers.size();
         historicalTradingPricesSeries.add(meanSellingPrice);
      }
      else
         historicalTradingPricesSeries.add(0.);
      historicalTradingVolumeSeries.add(0.);
   }
   
   /**
     * Register a {@link GoodsSeller} for this instrument.
     */
   public void registerSeller(final GoodsSeller seller) {
      registeredSellers.put(seller.getUniqueName(), seller);
   }
   
   /**
     * Unregister a {@link GoodsSeller} for this instrument. This method undoes a 
     * previous call to {@link #registerSeller(GoodsSeller)}.
     */
   public boolean unregisterSeller(final GoodsSeller seller) {
      return (registeredSellers.remove(seller.getUniqueName()) != null);
   }
   
   /**
     * NOTE
     * 
     * Typically this instrument is processed twice per simulation cycle
     * (once for input/production goods and again, subsequently, for domestic
     * consumption). Total demand includes every bid order in both sessions.
     */
   private void resetMemories() {
      lastAggregateDemand = aggreateDemandNow;
      aggreateDemandNow = 0.;
      lastMarketShareBySeller.clear();
   }
   
   public void matchOrders() {
      List<SimpleNode> 
         sellers = new ArrayList<SimpleNode>(), 
         buyers = new ArrayList<SimpleNode>();
      for(final SimpleGoodsMarketOrder order : bidOrders.values()) {
         buyers.add(new SimpleNode(order.getPrice(), order.getOpenSize(), order));
         aggreateDemandNow += order.getOpenSize();
      }
      for(final SimpleGoodsMarketOrder order : askOrders.values())
         sellers.add(new SimpleNode(order.getPrice(), order.getOpenSize(), order));
      Matching
         match = null;
      try {
         match = matchingAlgorithm.matchNodes(buyers, sellers);
      } catch (final InvalidAlgorithmParameterException e) { }                  // Failed. Abandon.
      
      double
         meanTradeUnitPrice = 0,
         totalTradeVolume = 0;
      
      // Commit
      if(match != null)
      for(Matching.OneToOneMatch trade : match) {
         final SimpleGoodsMarketOrder
            seller = (SimpleGoodsMarketOrder) trade.rightNode.getObjectReference(),
            buyer = (SimpleGoodsMarketOrder) trade.leftNode.getObjectReference();
            
         if(trade.matchAmount < 1.e-9) continue; // Weak Epsilon Test
         
         setupContract(
            (GoodsBuyer)buyer.getParty(), (GoodsSeller)seller.getParty(),
            trade.matchAmount, trade.matchCost
            );
         
         seller.execute(trade.matchAmount, trade.matchCost);
         buyer.execute(trade.matchAmount, trade.matchCost);
         
         {
         final String
            sellerName = seller.getParty().getUniqueName();
         
         if(lastMarketShareBySeller.containsKey(sellerName))
            lastMarketShareBySeller.put(sellerName,
               lastMarketShareBySeller.get(sellerName) + trade.matchAmount);
         else 
            lastMarketShareBySeller.put(sellerName, trade.matchAmount);
         }
         
         meanTradeUnitPrice += trade.matchAmount * trade.matchCost;
         totalTradeVolume += trade.matchAmount;
      }
      
      meanTradeUnitPrice = (totalTradeVolume == 0. ? 0. : meanTradeUnitPrice / totalTradeVolume);
      historicalTradingPricesSeries.add(meanTradeUnitPrice);
      historicalTradingVolumeSeries.add(totalTradeVolume);
      
      if(historicalTradingPricesSeries.size() > TRADE_MEMORY_LENGTH_NUMBER_OF_SESSIONS) {
         historicalTradingPricesSeries.pop();
         historicalTradingVolumeSeries.pop();
      }
   }
      
   private void setupContract(
      GoodsBuyer buyer,
      GoodsSeller seller,
      double tradeQuantiy,
      double auctionPrice
      ) {
      GoodsForCashTransaction.process(auctionPrice, tradeQuantiy, getGoodsType(), seller, buyer);
   }
   
   /**
     * Insert a new {@link SimpleGoodsMarketOrder}.
     */
   public void insertOrder(SimpleGoodsMarketOrder order){
      if (order.getSide() == SimpleGoodsMarketOrder.Side.BUY)
         insertBuyOrder(order);
      else
         insertSellOrder(order);
   }
   
   private void insertBuyOrder(SimpleGoodsMarketOrder order) {
      String partyIdentifier = order.getParty().getUniqueName();
      if(bidOrders.containsKey(partyIdentifier)) {
         throw new IllegalStateException(
            "GoodsInstrument.insertBuyOrder: a party has submitted two distinct buy orders" + 
            "for the same instrument.");
      }
      else bidOrders.put(partyIdentifier, order);
   }
    
   private void insertSellOrder(SimpleGoodsMarketOrder order) {
      final String
         partyIdentifier = order.getParty().getUniqueName();
      if(!registeredSellers.containsKey(partyIdentifier))
         throw new IllegalArgumentException(
            "GoodsInstrument: goods seller " + partyIdentifier + " has not been registered "
          + "with this instrument and cannot therefor submit a sell order."
            );
      if(askOrders.containsKey(partyIdentifier))
         throw new IllegalStateException(
            "GoodsInstrument.insertBuyOrder: a party has submitted two distinct sell orders" + 
            "for the same instrument.");
      askOrders.put(partyIdentifier, order);
   }
   
   /** 
     * Cancel all orders known to this instrument.
     */
   public void cancelOrders() {
      List<SimpleGoodsMarketOrder> ordersCopy = 
         new ArrayList<SimpleGoodsMarketOrder>(askOrders.values());
      for(SimpleGoodsMarketOrder order : ordersCopy) order.cancel();
      ordersCopy = new ArrayList<SimpleGoodsMarketOrder>(bidOrders.values());
      for(SimpleGoodsMarketOrder order : ordersCopy) order.cancel();
   }
   
   /** 
     * Remove a goods market order from this instrument.
     */
   public void discontinueOrderTracking(SimpleGoodsMarketOrder order) {
      String partyIdentifier = order.getParty().getUniqueName();
      Map<String, SimpleGoodsMarketOrder> orderList;
      if(order.getSide() == Side.BUY) orderList = bidOrders;
      else orderList = askOrders;
      SimpleGoodsMarketOrder knownOrder = orderList.get(partyIdentifier);
      if(knownOrder == null) return;
      else if(knownOrder != order)
         throw new IllegalArgumentException(
            "GoodsInstrument.discontinueOrderTracking: the order already assigned to this" + 
            " party is different from the order to remove.");
      orderList.remove(partyIdentifier);
   }
   
   /**
     * Get the name of the goods type processed by this instrument.
     */
   public String getGoodsType() { return goodsType; }
   
   /** 
     * Get a {@link Party} market order, if such an order exists. Otherwise, this
     * method returns {@code null}.
     */
   public SimpleGoodsMarketOrder getOrder(final Party party, final Side orderSide) {
      if(orderSide == Side.BUY)
         return getBidOrder(party);
      else
         return getAskOrder(party);
   }
   
   /** 
     * Get the bid order corresponding to a {@link Party}, if such a bid
     * order currently exists. Otherwise, return {@code null}.
     */
   private SimpleGoodsMarketOrder getBidOrder(final Party party) {
      return bidOrders.get(party.getUniqueName());
   }
   
   /** 
     * Get the ask order corresponding to a {@link Party}, if such an ask
     * order currently exists. Otherwise, return {@code null}.
     */
   private SimpleGoodsMarketOrder getAskOrder(final Party party) {
      return askOrders.get(party.getUniqueName());
   }
   
   /**
     * Get the worst ask price (per unit) known to this instrument.<br><br>
     * 
     * If ask orders currently exist, this method returns the worst ask price
     * per unit among all such ask orders. If no ask order exists, this method
     * returns the worst ask price (per unit goods) according to the method 
     * {@link GoodsSeller#getGoodsSellingPrice()} over all {@link GoodsSeller}{@code s} 
     * <i>currently</i> registered with this instrument. If no {@link GoodsSeller}{@code s}
     * have been registered with this instrument, this method returns {@code 0}.
     */
   public double getWorstAskPriceAmongSellers() {
      double result = 0.;
      if(askOrders.isEmpty())
         for(final GoodsSeller seller : registeredSellers.values())
            result = Math.max(seller.getGoodsSellingPrice(), result);
      else
         for(final SimpleGoodsMarketOrder order : askOrders.values())
            result = Math.max(result, order.getPrice());
      return result;
   }
   
   /** 
     * Get a list of historical trading prices for this instrument. The result is a 
     * list of average trade prices per unit sold for this instrument during recent 
     * market processing sessions. The average price during each such session is 
     * weighted by the size of the trades. Modifying the return value will not 
     * affect this object. The first element of the list is the oldest such record.
     */
   public List<Double> getTradingPrices() {
      return new ArrayList<Double>(historicalTradingPricesSeries);
   }
   
   /** 
     * Get a list of historical trading volumes for this instrument. The result is a 
     * list of effective trade volumes for this instrument during recent 
     * market processing sessions. The average price during each such session is 
     * weighted by the size of the trades. Modifying the return value will not 
     * affect this object. The first element of the list is the oldest such record.
     */
   public List<Double> getTradingVolumes() { 
      return new ArrayList<Double>(historicalTradingVolumeSeries); 
   }
   
   /** 
     * Get the last aggregate market demand for this instrument. The returned 
     * value is the sum of all ask order volumes over <i>every</i> call to 
     * {@link #matchOrders()} in the last simulation cycle.
     */
   public double getLastAggregateDemand() { 
      return lastAggregateDemand;
   }
   
   /**
     * Count the total supply (sum of ask order quantities) <i>at this time</i>. This
     * method will return {@code 0} if no ask orders are currently known to
     * this instrument.
     */
   public double getTotalSupply() {
      double result = 0.;
      for(final SimpleGoodsMarketOrder order : askOrders.values())
         result += order.getOpenSize();
      return result;
   }
   
   /**
     * Get the number of goods sellers currently registered with this instrument.
     */
   public int getNumberOfRegisteredSellers() {
      return registeredSellers.size();
   }
   
   /**
     * Get the last market share for the specified {@link GoodsSeller} (name).
     * This method returns a number in the range <code>[0, 1]</code> inclusive.
     * If the return value is {@code 0}, then either no trades were executed
     * in the last market session or the seller was not involved in any such
     * trades. If the return value is {@code 1}, then the seller was involved
     * in every attempted trade execution for this instrument in the last
     * market session. Similarly, values less than {@code 1} and greater than 
     * {@code 0} represent the fraction of attempted trade executions in which
     * the specified seller was involved in the last market processing session.
     * <br><br>
     * 
     * The fraction returned represents the fraction of trade volumes, not the fraction
     * of the number of attempted contract executions. Thus the order size of
     * contracts executed in the last market processing session matters.
     */
   public double getLastMarketShareFor(final String sellerName) {
      double
         sumOfAttemptedExecutedTrades = 0.;
      for(final Entry<String, Double> record : lastMarketShareBySeller.entrySet())
         sumOfAttemptedExecutedTrades += record.getValue();
      if(sumOfAttemptedExecutedTrades == 0.)
         return 0.;
      else
         return lastMarketShareBySeller.get(sellerName) / sumOfAttemptedExecutedTrades;
   }
}
