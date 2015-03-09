/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.matching.*;
import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.utilities.EmpiricalDistribution;

/**
  * A simple instrument for {@link Labour} contracts of fixed maturity.
  * 
  * @author phillips
  */
public final class SimpleLabourInstrument {
   
   private final int 
      labourContractMaturity;
   
   private Map<String, SimpleLabourMarketOrder>
      bidOrders,
      askOrders;
   
   private EmpiricalDistribution
      employedLabourVolume,
      demandWeightedMeanBidPrice,
      supplyWeightedMeanAskPrice,
      totalLabourSupply,
      totalLabourDemand;
   
   private ArrayList<Double> 
      tradingPricesSeries;
   private ArrayList<Integer>
      tradingVolumeSeries;
   
   MatchingAlgorithm                                  // Employee-Employer Matching Strategy
      matchingAlgorithm;
   
   /**
     * Create a {@link SimpleLabourInstrument} with a custom contract maturity and
     * a custom employer/employee {@link MatchingAlgorithm}.
     * 
     * @param labourContractMaturity
     *        The duration (number of simulation cycles) for labour contracts.
     *        This argument should be strictly positive.
     * @param matchingAlgorithm
     *        The matching algorithm to be used.
     */
   public SimpleLabourInstrument(
      final int labourContractMaturity,
      final MatchingAlgorithm matchingAlgorithm
      ) {
      Preconditions.checkNotNull(matchingAlgorithm);
      Preconditions.checkArgument(labourContractMaturity > 0);
      this.bidOrders = new LinkedHashMap<String, SimpleLabourMarketOrder>();
      this.askOrders = new LinkedHashMap<String, SimpleLabourMarketOrder>();
      this.employedLabourVolume = new EmpiricalDistribution(50);
      this.demandWeightedMeanBidPrice = new EmpiricalDistribution(50);
      this.supplyWeightedMeanAskPrice = new EmpiricalDistribution(50);
      this.totalLabourSupply = new EmpiricalDistribution(50);
      this.totalLabourDemand = new EmpiricalDistribution(50);
      this.tradingPricesSeries = new ArrayList<Double>();
      this.tradingVolumeSeries = new ArrayList<Integer>();
      this.labourContractMaturity = labourContractMaturity;
      this.matchingAlgorithm = matchingAlgorithm;
   }
   
   /** 
     * Match {@link Employer}{@code s} to {@link Employee}{@code s}.
     */
   public void matchOrders() {
      // Call a matching algorithm
      final List<SimpleNode> 
         sellers = new ArrayList<SimpleNode>(), 
         buyers = new ArrayList<SimpleNode>();
      double 
         totalLabourSupplyNow = 0.,
         totalLabourDemandNow = 0.;
      for (SimpleLabourMarketOrder order : bidOrders.values()) {
         totalLabourDemandNow += order.getOpenSize();
         buyers.add(new SimpleNode(
            order.getPrice(), order.getOpenSize(), order));
      }
      for (SimpleLabourMarketOrder order : askOrders.values()) {
         totalLabourSupplyNow += order.getOpenSize();
         sellers.add(new SimpleNode(
            order.getPrice(), order.getOpenSize(), order));
      }
      totalLabourSupply.add(totalLabourSupplyNow);
      totalLabourDemand.add(totalLabourDemandNow);
      
      Matching match;
      try {
         match = matchingAlgorithm.matchNodes(sellers, buyers);
      } catch (final InvalidAlgorithmParameterException e) {
         final String errMsg = 
            "SimpleLabourMarketMatchingAlgorithm.matchParties: labour contracts matching" + 
            " algorithm raised a fatal algorithm parameter exception. Labour market " + 
            "processing cannot proceed. This behaviour is not expected and is indicative " +
            "of a simulation error. Cause details follow: " + 
            e.getMessage();
         System.err.println(errMsg);
         System.err.flush();
         // Abandon
         match = new Matching.Builder().build();
      }
      
      // Commit labour contracts
      double
         employedLabourVolumeNow = 0,
         demandWeightedMeanBidPriceNow = 0.,
         supplyWeightedMeanAskPriceNow = 0.;
      for (Matching.OneToOneMatch trade : match) {
         SimpleLabourMarketOrder
            sellerOrder = 
               (SimpleLabourMarketOrder)trade.leftNode.getObjectReference(),
            buyOrder = 
               (SimpleLabourMarketOrder)trade.rightNode.getObjectReference();
            
            setupContract(
               (Employer) buyOrder.getParty(),
               (Employee) sellerOrder.getParty(),
               trade.matchAmount,
               buyOrder.getPrice(),
               sellerOrder.getPrice(),
               trade.matchCost
               );
            
            employedLabourVolumeNow += trade.matchAmount;
            demandWeightedMeanBidPriceNow +=
               trade.matchAmount * buyOrder.getPrice();
            supplyWeightedMeanAskPriceNow +=
               trade.matchAmount * sellerOrder.getPrice();
            
            sellerOrder.decrementOpenSize(trade.matchAmount);
            buyOrder.decrementOpenSize(trade.matchAmount);
      }
      
      if(totalLabourSupplyNow > 0.)
         supplyWeightedMeanAskPriceNow /= employedLabourVolumeNow;
      else
         supplyWeightedMeanAskPriceNow = 0.;
      
      if(totalLabourDemandNow > 0.)
         demandWeightedMeanBidPriceNow /= employedLabourVolumeNow;
      else
         demandWeightedMeanBidPriceNow = 0.;
      
      employedLabourVolume.add(employedLabourVolumeNow);
      demandWeightedMeanBidPrice.add(demandWeightedMeanBidPriceNow);
      supplyWeightedMeanAskPrice.add(supplyWeightedMeanAskPriceNow);
   }
   
   /**
     * Try to create a {@link Labour} contract between an {@link Employer} 
     * and an {@link Employee}. If the {@link Labour} contract cannot be
     * formed (for instance, but not limited to, insufficient {@link Employer}
     * funds) then the contract creation attempt is abandonned and no further
     * action is taken.
     * 
     * @param employer
     *        The {@link Employer}
     * @param employee
     *        The {@link Employee}
     * @param quantity
     *        The amount (number of units) of labour to purchase
     * @param wageAsk
     *        The ask price per unit labour to employ
     * @param wageBid
     *        The bid price per unit labour to employ
     * @param effectiveWage
     *        The price per unit for the executed labour contract
     */
   private void setupContract(
      Employer employer, 
      Employee employee,
      final double quantity, 
      final double wageAsk,
      final double wageBid,
      final double effectiveWage
      ) {
      try {
         employer.disallocateCash(wageAsk * quantity);
         Labour.create(employer, employee, quantity, labourContractMaturity, effectiveWage);
      } catch (final DoubleEmploymentException e) {
         // Abandon
      }
   }
   
   /** 
     * Insert a new market order.
     */
   public void insertOrder(SimpleLabourMarketOrder order) {
      if (order.getSide() == SimpleLabourMarketOrder.Side.BUY)
         insertBuyOrder(order);
      else
         insertSellOrder(order);
   }
   
   // Insert a new buy order.
   private void insertBuyOrder(SimpleLabourMarketOrder order) {
      String partyIdentifier = order.getParty().getUniqueName();
      if(bidOrders.containsKey(partyIdentifier)) {
         throw new IllegalStateException(
            "LabourInstrument.insertBuyOrder: a party has submitted two distinct buy orders" + 
            "for the same instrument.");
      }
      else 
      {
    	  bidOrders.put(partyIdentifier, order);
      }
   }
   
   // Insert a new sell order.
   private void insertSellOrder(SimpleLabourMarketOrder order) {
      String partyIdentifier = order.getParty().getUniqueName();
      if(askOrders.containsKey(partyIdentifier)) {
         throw new IllegalStateException(
            "LabourInstrument.insertBuyOrder: a party has submitted two distinct sell orders" + 
            "for the same instrument.");
      }
      else 
      {
    	  askOrders.put(partyIdentifier, order);
      }
   }
   
   /** Cancel all orders known to this instrument. */
   public void cancelOrders() {
      List<SimpleLabourMarketOrder> ordersCopy = 
         new ArrayList<SimpleLabourMarketOrder>(askOrders.values());
      for(SimpleLabourMarketOrder order : ordersCopy)
    	  order.cancel();
      ordersCopy = new ArrayList<SimpleLabourMarketOrder>(bidOrders.values());
      for(SimpleLabourMarketOrder order : ordersCopy)
    	  order.cancel();
   }
   
   /** Remove a labour market order from this instrument. */
   public void discontinueOrderTracking(SimpleLabourMarketOrder order) {
      String partyIdentifier = order.getParty().getUniqueName();
      Map<String, SimpleLabourMarketOrder> orderList;
      if(order.getSide() == SimpleLabourMarketOrder.Side.BUY) orderList = bidOrders;
      else orderList = askOrders;
      SimpleLabourMarketOrder knownOrder = orderList.get(partyIdentifier);
      if(knownOrder == null) return;
      else if(knownOrder != order)
         throw new IllegalArgumentException(
            "LabourInstrument.discontinueOrderTracking: the order already assigned to this" + 
            " party is different from the order to remove.");
      orderList.remove(partyIdentifier);
   }
   
   /** Get an unmodifiable list of historical trading prices. */
   public List<Double> getTradingPrices() {
      return Collections.unmodifiableList(tradingPricesSeries);
   }
   
   /** Get an unmodifiable list of historical traded volumes. */
   public List<Integer> getTradingVolumeSeries() {
      return Collections.unmodifiableList(tradingVolumeSeries);
   }
   
   /** Get the maturity of labour contracts belonging to this instrument. */
   public int getMaturity() {
      return labourContractMaturity;
   }
    
    /** Get the last total employment known to this instrument. */
    public double getLastTotalEmployedLabour() {
        if(employedLabourVolume.size() == 0)
            return 0.;
        return employedLabourVolume.getLastAdded();
    }
   
   /** Get the last total labour supply known to this instrument. */
   public double getLastLabourTotalSupply() {
      if (totalLabourSupply.size() == 0) return 0.;
      return totalLabourSupply.getLastAdded();
   }
   
   /** Get the last total labour demand known to this instrument. */
   public double getLastLabourTotalDemand() {
      if (totalLabourDemand.size() == 0) return 0.;
      return totalLabourDemand.getLastAdded();
   }
    
   /** Get the last total labour unemployment known to this instrument. */
   public double getLastTotalUnemployment() {
      return (getLastLabourTotalSupply() - getLastTotalEmployedLabour());
   }
   
   /** Get the last trade-weighted bid wage known to this instrument. */
   public double getDemandWeightedBidWage() {
      if (totalLabourDemand.size() == 0.) return 0.;
      return demandWeightedMeanBidPrice.getLastAdded();
   }
   
   public double getSupplyWeightedAskWage() {
      if (totalLabourDemand.size() == 0.) return 0.;
      return supplyWeightedMeanAskPrice.getLastAdded();
   }
}
