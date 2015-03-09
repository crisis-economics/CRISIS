/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.bank.central.CentralBank;

/** 
 * This class implements BookEngine interface and is used directly by an Instrument class. 
 * Orders are only matched at the top of the order book, which is triggered by insertion of a new order. 
 * This implementation guarantees insertion in O(log(n)) into both ask and bid sides of the book, using binary search index discovery with minor modifications. 
 * Insertion into the book is based on the price-time priority algorithm.
 * 
 * @author  olaf
 */
public class BookEngine implements BookEngineInterface {

    private static final long serialVersionUID = 8949492175637349392L;
    
    private final String tickerSymbol;
    private final List<Order>  bidLimitOrders;
    private final List<Order>  askLimitOrders;
    private final List<Order>  filledOrders;
    private final List<Order>  partiallyFilledOrders;
    /**
     * When BookEngine object is created, it gets access to all Instrument books
     * @param bidLimitOrders
     * @param askLimitOrders
     * @param filledOrders
     * @param partiallyFilledOrders
     * @param updatedOrders
     * @param tickerSymbol
     */
    public BookEngine(final List<Order>  bidLimitOrders, final List<Order>  askLimitOrders, final List<Order>  filledOrders, final List<Order>  partiallyFilledOrders, final BlockingQueue<Order> updatedOrders, final String tickerSymbol) {

        this.tickerSymbol = tickerSymbol;
        
        this.bidLimitOrders = bidLimitOrders;
        this.askLimitOrders = askLimitOrders;
        this.filledOrders   = filledOrders;
        this.partiallyFilledOrders = partiallyFilledOrders;
    }
    
//  public void addBuyOrder(Order order){
//      order.getInstrument().updateBidVolume(order.getOpenSize());
//      int i = findIndex(order);   
//      
//      bidLimitOrders.add(i, order);
//  }
    
//  public void addSellOrder(Order order){
//      order.getInstrument().updateAskVolume(order.getOpenSize());
//      int i = findIndex(order);
//      
//      askLimitOrders.add(i, order);
//  }

//  /**
//   * finds an index where to insert this order. BinarySearch is used, but the order must never be found, 
//   * as every new order is unique - hence, we only look for a correct index
//   * @param order
//   * @return
//   */
//  private int findIndex(Order order){
//      int i = 0;
//      if (order.getSide() == Order.Side.BUY) {
//          //this returns (-insertion point -1), where insertion point is the needed index
//          i = Collections.binarySearch(bidLimitOrders, order, new PriceTimePriorityComparator());
//      } else {
//          i = Collections.binarySearch(askLimitOrders, order, new PriceTimePriorityComparator());         
//      }
//      return -(i+1);  
//  }

    /**
     * @return the tickerSymbol
     */
    public String getTickerSymbol() {
        return tickerSymbol;
    }

    @Override
    public void processNewOrder( final Order o) {
        final Instrument instrument = o.getInstrument();
        
        if(o.getSide() == Order.Side.BUY){
            
            //get the market current price for the market order
            if(o.getType() == Order.Type.MARKET){
                if(instrument.isSynchronous()) {
                    // market orders have no meaning in synchronous mode
                    o.cancel();
                    o.setStatus(Order.Status.REJECTED);
                    //updatedOrders.add(o); TODO memory leakage
                    return;
                }
                final List<Order> allowedOrders = filterOrders(o, askLimitOrders);
                if(allowedOrders.size() > 0) {
                    o.setPrice(allowedOrders.get(0).getPrice()); // TODO this price in meaningless in sync mode
                } else{
                        o.cancel();
                        o.setStatus(Order.Status.REJECTED);
                        //updatedOrders.add(o); TODO memory leakage
                        return;
                }
            }
            
            //Update instrument's immediate statistics
            instrument.updateBidVWAP(o.getSize(), o.getPrice());
            instrument.updateBidHigh(o.getPrice());
            instrument.updateBidLow(o.getPrice());
            
            //Try to match immediately, there is no immediate matching in synchronous mode
            if (instrument.isAsynchronous()) {
                try {
                    matchBuyOrder(o);
                } catch (final InstrumentException e) {
                    e.printStackTrace();
                }
            } 
            
            if(o.isFilled()) {
                addToFilledOrders(o);
                o.setStatus(Order.Status.FILLED);
            } else {
                insertBuyOrder(o);
            }
        }
        else{
            //get the market current price for the market order
            if(o.getType() == Order.Type.MARKET){
                final List<Order> allowedOrders = filterOrders(o, bidLimitOrders);
                if(allowedOrders.size() > 0) {
                    o.setPrice(allowedOrders.get(0).getPrice()); //TODO this price in meaningless in sync mode
                } else{
                        o.cancel();
                        o.setStatus(Order.Status.REJECTED);
                        //updatedOrders.add(o); TODO memory leakage
                        return;
                }
            }
            
            //Update instrument's immediate statistics
            instrument.updateAskVWAP(o.getSize(), o.getPrice());
            instrument.updateAskHigh(o.getPrice());
            instrument.updateAskLow(o.getPrice());
            
            //Try to match immediately, there is no immediate matching in synchronous mode
            if (instrument.isAsynchronous()) {
                try {
                    matchSellOrder(o);
                } catch (final InstrumentException e) {
                    e.printStackTrace();
                }
            } 
            
            if(o.isFilled()) {
                addToFilledOrders(o);
                o.setStatus(Order.Status.FILLED);
            } else {
                insertSellOrder(o);
            }
        }
            
        //Remove filled orders from partially filled orders, and add them to filled orders
        cleanUpPartiallyFilledOrders();
    }

    private List<Order> filterOrders(final Order evaluatedOrder, final List<Order> allOrders) {
        final ArrayList<Order> ret = new ArrayList<Order>();
        
        for (final Order act : allOrders) {
            if ( evaluatedOrder.accepts( act ) && act.accepts( evaluatedOrder ) ) {
                ret.add( act );
            }
        }
        
        return ret;
    }

    /**
     * clean up partiallyFilledOrders by eliminating already filled orders, and moving them into filledOrders
     */
    private void cleanUpPartiallyFilledOrders() {
        final Iterator<Order> iter = partiallyFilledOrders.iterator();
        while(iter.hasNext()){
            final Order o = iter.next();
            if(o.isFilled()){
                iter.remove();
                addToFilledOrders(o);
            }
        }
    }
    
    private void matchSellOrder(final Order o) throws InstrumentException {
        if(bidLimitOrders.size() > 0){
            final Instrument instrument = o.getInstrument();
            final Iterator<Order> iter = bidLimitOrders.iterator();
            
            while(iter.hasNext()){
                final Order curOrder = iter.next();
                final double price = curOrder.getPrice();
                
                if(price >= o.getPrice() && o.getOpenSize() > 0 && o.accepts(curOrder) && curOrder.accepts(o)){
                    
                    //calculate matched quantity
                    double quantity;
                    if(curOrder.getOpenSize() > o.getOpenSize()) {
                        quantity = o.getOpenSize();
                    } else {
                        quantity = curOrder.getOpenSize();
                    }
                
                    //update order states and set instrument's last price
                    curOrder.execute(quantity, price);
                    o.execute(quantity, price);
                    
                    //setup contract
                    instrument.setupContract(curOrder, o, quantity, price);
                
                    addToPartiallyFilledOrders(o); // TODO it could be even filled
                    
                    
                    if(curOrder.isFilled()){
                        iter.remove();
                        addToFilledOrders(curOrder);
                        curOrder.setStatus(Order.Status.FILLED);
                    }
                    else{
                        addToPartiallyFilledOrders(curOrder);
                        curOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                    }
                    
                    instrument.updateLastPrice(price);
                    instrument.updateBidVolume(-quantity);
                    instrument.updateSellVolume(quantity);
                    instrument.updateAveragePrice(quantity, price);
                    instrument.updateAverageSellPrice(quantity, price);
                     
                }
                //need to break to avoid going through the whole book, as it is ordered
//              else{
//                  break;
//              }
            }
        }
    }

    private void addToPartiallyFilledOrders(final Order o) {
        if(o.isFilled()){
            if(partiallyFilledOrders.contains(o)){
                partiallyFilledOrders.remove(o);
                addToFilledOrders(o);
                o.setStatus(Order.Status.FILLED);
            }
        }
        else{
            if(!partiallyFilledOrders.contains(o)){
                partiallyFilledOrders.add(o);
                o.setStatus(Order.Status.PARTIALLY_FILLED);
            }
        }
    }

    private void addToFilledOrders(final Order o) {
        if(!filledOrders.contains(o)) {
            //filledOrders.add(o); TODO do we need that? when do we remove them?
        }
    }

    /**
     * this is to be called by a scheduler to match active orders in synchronized matching mode
     * TODO some deadlock?
     * @throws InstrumentException 
     */
    @Override
    public void synchronizedOrderMatch() throws InstrumentException {
        while ((bidLimitOrders.size() > 0 && askLimitOrders.size() > 0) // using shortcut
                && (bidLimitOrders.get(0).getPrice() >= askLimitOrders.get(0).getPrice())) {
            // we have crossing of prices
            if (bidLimitOrders.get(0).getEntryTime() > askLimitOrders.get(0).getEntryTime()) {
                // matched price is best-ask-price
                matchBuyOrder(bidLimitOrders.get(0));
            } else if (askLimitOrders.get(0).getEntryTime() > bidLimitOrders.get(0).getEntryTime()) {
                // matched price is best-bid-price
                matchSellOrder(askLimitOrders.get(0));
            } else {
                // both orders have the same time stamp
                if (Simulation.getSimState().random.nextBoolean()) {
                    matchBuyOrder(bidLimitOrders.get(0));
                } else {
                    matchSellOrder(askLimitOrders.get(0));
                }
            }
        }
    }
    

    /**
     * matches orders in synchronous mode. 
     * @throws InstrumentException
     * @deprecated use {@link #synchronizedOrderMatch()} instead, as soon as it works.
     */
    @Deprecated
    @Override
    public synchronized void matchOrders() throws InstrumentException {
        if(bidLimitOrders.size() > 0){
            final Iterator<Order> iterBid = bidLimitOrders.iterator();
            while(iterBid.hasNext()){
                final Order curBidOrder = iterBid.next();
                final Instrument instrument = curBidOrder.getInstrument();
                
                if(askLimitOrders.size()>0){
                    final Iterator<Order> iterAsk = askLimitOrders.iterator();
                    while(iterAsk.hasNext()){
                        final Order curAskOrder = iterAsk.next();
                        if(curAskOrder.getType() == Order.Type.MARKET && curAskOrder.getOpenSize() > 0)
                         {
                            curAskOrder.setPrice(curBidOrder.getPrice()); // set price for market sell order
                        }
                        final double priceAsk = curAskOrder.getPrice();
                        if(curBidOrder.getType() == Order.Type.MARKET && curBidOrder.getOpenSize() > 0)
                         {
                            curBidOrder.setPrice(curAskOrder.getPrice()); // set price for market buy order
                        //double priceBid = curBidOrder.getPrice();
                        }

                        //If the current price of sell order is less than the price of current buy order, then it is a match
                        if(priceAsk <= curBidOrder.getPrice() && curBidOrder.getOpenSize() > 0 && curAskOrder.accepts( curBidOrder ) && curBidOrder.accepts( curAskOrder )){

                            // determine the quantity matched
                            double quantity;
                            if(curAskOrder.getOpenSize() > curBidOrder.getOpenSize()) {
                                quantity = curBidOrder.getOpenSize();
                            } else {
                                quantity = curAskOrder.getOpenSize();
                            }
                            
                            //update order states and set instrument's last price
                            curAskOrder.execute(quantity, priceAsk);
                            curBidOrder.execute(quantity, priceAsk);
                            
                            //setup contract
                            instrument.setupContract(curBidOrder,curAskOrder, quantity, priceAsk);
                            

                            if(curAskOrder.isFilled()){
                                iterAsk.remove();
                                addToFilledOrders(curAskOrder);
                                curAskOrder.setStatus(Order.Status.FILLED);
                            }else{
                                addToPartiallyFilledOrders(curAskOrder);
                                curAskOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                            }
                            if(curBidOrder.isFilled()){
                                iterBid.remove();
                                addToFilledOrders(curBidOrder);
                                curBidOrder.setStatus(Order.Status.FILLED);
                            }else{
                                addToPartiallyFilledOrders(curBidOrder);
                                curBidOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                            }
                            
                            cleanUpPartiallyFilledOrders();

                            instrument.updateLastPrice(priceAsk);
                            instrument.updateAskVolume(-quantity);
                            instrument.updateBuyVolume(quantity);
                            instrument.updateAveragePrice(quantity, priceAsk);
                            instrument.updateAverageBuyPrice(quantity, priceAsk);

                        }
                        //need to break to avoid going through the whole book
//                      else{
//                          break;
//                      }   
                    }
                }
                //need to break to avoid going through the whole book
                //break;
            }
        }
    }
    
    /**
     * matches orders in synchronous mode. 
     * @throws InstrumentException
     * @deprecated use {@link #synchronizedOrderMatch()} instead, as soon as it works.
     */
    @Deprecated
    @Override
    public synchronized void matchOrdersBestBid() throws InstrumentException {
        if(askLimitOrders.size() > 0){
            final Iterator<Order> iterAsk = askLimitOrders.iterator();
            while(iterAsk.hasNext()){
                final Order curAskOrder = iterAsk.next();
                final Instrument instrument = curAskOrder.getInstrument();
                
                if(bidLimitOrders.size()>0){
                    final Iterator<Order> iterBid = bidLimitOrders.iterator();
                    while(iterBid.hasNext()){
                        final Order curBidOrder = iterBid.next();
                        if(curBidOrder.getType() == Order.Type.MARKET && curBidOrder.getOpenSize() > 0)
                         {
                            curBidOrder.setPrice(curAskOrder.getPrice()); // set price for market buy order
                        }
                        final double priceBid = curBidOrder.getPrice();
                        if(curAskOrder.getType() == Order.Type.MARKET && curAskOrder.getOpenSize() > 0)
                         {
                            curAskOrder.setPrice(curBidOrder.getPrice()); // set price for market sell order
                        //double priceBid = curBidOrder.getPrice();
                        }

                        //If the current price of sell order is less than the price of current buy order, then it is a match
                        if(priceBid > curAskOrder.getPrice() && curAskOrder.getOpenSize() > 0 && curAskOrder.accepts( curBidOrder ) && curBidOrder.accepts( curAskOrder )){

                            // determine the quantity matched
                            double quantity;
                            if(curBidOrder.getOpenSize() >= curAskOrder.getOpenSize()) {
                                quantity = curAskOrder.getOpenSize();
                            } else {
                                quantity = curBidOrder.getOpenSize();
                            }
                            
                            //update order states and set instrument's last price
                            curAskOrder.execute(quantity, priceBid);
                            curBidOrder.execute(quantity, priceBid);
                            
                            //setup contract
                            instrument.setupContract(curBidOrder,curAskOrder, quantity, priceBid);
                            
                            if(curBidOrder.isFilled()){
                                iterBid.remove();
                                addToFilledOrders(curBidOrder);
                                curBidOrder.setStatus(Order.Status.FILLED);
                            }else{
                                addToPartiallyFilledOrders(curBidOrder);
                                curBidOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                            }
                            if(curAskOrder.isFilled()){
                                iterAsk.remove();
                                addToFilledOrders(curAskOrder);
                                curAskOrder.setStatus(Order.Status.FILLED);
                            }else{
                                addToPartiallyFilledOrders(curAskOrder);
                                curAskOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                            }
                            
                            
                            cleanUpPartiallyFilledOrders();

                            instrument.updateLastPrice(priceBid);
                            instrument.updateAskVolume(-quantity);
                            instrument.updateBuyVolume(quantity);
                            instrument.updateAveragePrice(quantity, priceBid);
                            instrument.updateAverageBuyPrice(quantity, priceBid);

                        }
                        //need to break to avoid going through the whole book
//                      else{
//                          break;
//                      }   
                    }
                }
                //need to break to avoid going through the whole book
                //break;
            }
        }
    }
    
    /**
     * Match all Orders using the interbank LOB. (for sync mode)
     * Goes through all lenders (ask), starting from the best (lowest)
     * For each lender, goes through all borrowers (bid) starting from the highest
     * If the engine runs out of lenders or borrowers, and a central bank is in the LOB,
     * lenders deposit with the CB, and borrowers borrow from the CB.
     * 
     * @return void
     * @throws InstrumentException 
     */
    @Deprecated
    @Override
    public synchronized void matchOrdersInterbank() throws InstrumentException {
        if(askLimitOrders.size() > 0){
            final Iterator<Order> iterAsk = askLimitOrders.iterator();
            while(iterAsk.hasNext()){
                final Order curAskOrder = iterAsk.next();
                final Instrument instrument = curAskOrder.getInstrument();
                
                if(bidLimitOrders.size()>0){
                    final Iterator<Order> iterBid = bidLimitOrders.iterator();
                    while(iterBid.hasNext()){
                        final Order curBidOrder = iterBid.next();
                        if(curBidOrder.getType() == Order.Type.MARKET && curBidOrder.getOpenSize() > 0)
                         {
                            curBidOrder.setPrice(curAskOrder.getPrice()); // set price for market buy order
                        }
                        final double priceBid = curBidOrder.getPrice();
                        if(curAskOrder.getType() == Order.Type.MARKET && curAskOrder.getOpenSize() > 0)
                        {
                            curAskOrder.setPrice(curBidOrder.getPrice()); // set price for market sell order
                        //double priceBid = curBidOrder.getPrice();
                        }

                        if ((curAskOrder.getParty() instanceof CentralBank) && (curBidOrder.getParty() instanceof CentralBank)) {
                            //do nothing, central bank does not lend to itself
                        } else if (curAskOrder.getParty() instanceof CentralBank) {
                            //CB is the lender, commercial bank is borrower - in this case, use lender's price
                            //If the current price of sell order is less than the price of current buy order, then it is a match
                            if(/*priceBid > curAskOrder.getPrice() && */curAskOrder.getOpenSize() > 0 && curAskOrder.accepts( curBidOrder ) && curBidOrder.accepts( curAskOrder )){

                                // determine the quantity matched
                                double quantity;
                                if(curBidOrder.getOpenSize() >= curAskOrder.getOpenSize()) {
                                    quantity = curAskOrder.getOpenSize();
                                } else {
                                    quantity = curBidOrder.getOpenSize();
                                }
                                
                                //update order states and set instrument's last price
                                curAskOrder.execute(quantity, curAskOrder.getPrice());
                                curBidOrder.execute(quantity, curAskOrder.getPrice());
                                
                                //setup contract
                                instrument.setupContract(curBidOrder,curAskOrder, quantity, curAskOrder.getPrice());
                                
                                if(curBidOrder.isFilled()){
                                    iterBid.remove();
                                    addToFilledOrders(curBidOrder);
                                    curBidOrder.setStatus(Order.Status.FILLED);
                                }else{
                                    addToPartiallyFilledOrders(curBidOrder);
                                    curBidOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                                }
                                if(curAskOrder.isFilled()){
                                    iterAsk.remove();
                                    addToFilledOrders(curAskOrder);
                                    curAskOrder.setStatus(Order.Status.FILLED);
                                }else{
                                    addToPartiallyFilledOrders(curAskOrder);
                                    curAskOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                                }
                                
                                
                                cleanUpPartiallyFilledOrders();

                                instrument.updateLastPrice(curAskOrder.getPrice());
                                instrument.updateAskVolume(-quantity);
                                instrument.updateBuyVolume(quantity);
                                instrument.updateAveragePrice(quantity, curAskOrder.getPrice());
                                instrument.updateAverageBuyPrice(quantity, curAskOrder.getPrice());

                            }
                        //} else if (curBidOrder.getParty() instanceof CentralBank) {
                            //commercial bank deposits with CB ('lends' to CB) - same as 2 commercial banks scenario
                            
                        } else {
                            //two commercial banks
                            //If the current price of sell order is less than the price of current buy order, then it is a match
                            if(/*priceBid > curAskOrder.getPrice() && */curAskOrder.getOpenSize() > 0 && curAskOrder.accepts( curBidOrder ) && curBidOrder.accepts( curAskOrder )){

                                // determine the quantity matched
                                double quantity;
                                if(curBidOrder.getOpenSize() >= curAskOrder.getOpenSize()) {
                                    quantity = curAskOrder.getOpenSize();
                                } else {
                                    quantity = curBidOrder.getOpenSize();
                                }
                                
                                //update order states and set instrument's last price
                                curAskOrder.execute(quantity, priceBid);
                                curBidOrder.execute(quantity, priceBid);
                                
                                //setup contract
                                instrument.setupContract(curBidOrder,curAskOrder, quantity, priceBid);
                                
                                if(curBidOrder.isFilled()){
                                    iterBid.remove();
                                    addToFilledOrders(curBidOrder);
                                    curBidOrder.setStatus(Order.Status.FILLED);
                                }else{
                                    addToPartiallyFilledOrders(curBidOrder);
                                    curBidOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                                }
                                if(curAskOrder.isFilled()){
                                    iterAsk.remove();
                                    addToFilledOrders(curAskOrder);
                                    curAskOrder.setStatus(Order.Status.FILLED);
                                }else{
                                    addToPartiallyFilledOrders(curAskOrder);
                                    curAskOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                                }
                                
                                
                                cleanUpPartiallyFilledOrders();

                                instrument.updateLastPrice(priceBid);
                                instrument.updateAskVolume(-quantity);
                                instrument.updateBuyVolume(quantity);
                                instrument.updateAveragePrice(quantity, priceBid);
                                instrument.updateAverageBuyPrice(quantity, priceBid);

                            }
                        }
                    }
                }
            }
        }
    }

    private synchronized void matchBuyOrder(final Order o) throws InstrumentException {
        if(askLimitOrders.size()>0){
            final Instrument instrument = o.getInstrument();
            final Iterator<Order> iter = askLimitOrders.iterator();
            
            while(iter.hasNext()){
                final Order curOrder = iter.next(); // sell order
                final double price = curOrder.getPrice();
                if(o.getType() == Order.Type.MARKET && o.getOpenSize() > 0)
                 {
                    o.setPrice(price); // set price for market buy order
                }
                
                //If the current price of sell order is less
                //than the price of buy order, then it is a match
                if(price <= o.getPrice() && o.getOpenSize() > 0 && o.accepts( curOrder ) && curOrder.accepts( o )){
                    double quantity;
                    //figure out the quantity matched
                    if(curOrder.getOpenSize() > o.getOpenSize()) {
                        quantity = o.getOpenSize();
                    } else {
                        quantity = curOrder.getOpenSize();
                    }
                    
                    // TODO consider calling instrument.setupContract() first
                    // and order.execute afterwards! The problem is that the
                    // order.execute() method notifies the Party via the update
                    // method, and executed in this order, the Party won't see
                    // any change, which happens in the setupContract method.
                    
                    //update order states and set instrument's last price
                    curOrder.execute(quantity, price);
                    o.execute(quantity, price);
                    
                    //setup contract
                    instrument.setupContract(o,curOrder, quantity, price);
                    
                    if(curOrder.isFilled()){
                        iter.remove();
                        addToFilledOrders(curOrder);
                        curOrder.setStatus(Order.Status.FILLED);
                    }else{
                        addToPartiallyFilledOrders(curOrder);
                        curOrder.setStatus(Order.Status.PARTIALLY_FILLED);
                    }
                    addToPartiallyFilledOrders(o);
                    
                    instrument.updateLastPrice(price);
                    instrument.updateAskVolume(-quantity);
                    instrument.updateBuyVolume(quantity);
                    instrument.updateAveragePrice(quantity, price);
                    instrument.updateAverageBuyPrice(quantity, price);
                    
                } 
                //need to break to avoid going through the whole book as it is ordered
//              else{
//                  break;
//              }   
            }
        }
    }

    @Override
    public Order processCancelOrder(final Order o) {
//      Order order;
        boolean removed = false;
        
        if(o.getSide() == Order.Side.BUY){
            removed = bidLimitOrders.remove(o);
//          final Iterator<Order> iter = bidLimitOrders.iterator();
//          while(iter.hasNext()){
//              order = iter.next();
//              if(order.equals(o)){
//                  iter.remove();
//                  order.cancel();
//                  order.setStatus(Order.Status.CANCELLED);
//                  updatedOrders.add(order);
//                  return order;
//              }
//          }
//          return null;
        }
        
        if(o.getSide() == Order.Side.SELL){
            removed = askLimitOrders.remove(o);
//          final Iterator<Order> iter = askLimitOrders.iterator();
//          while(iter.hasNext()){
//              order = iter.next();
//              if(order.equals(o)){
//                  iter.remove();
//                  order.cancel();
//                  order.setStatus(Order.Status.CANCELLED);
//                  updatedOrders.add(order);
//                  return order;
//              }
//          }
//          return null;
        }
        
        if (removed){
            o.cancel();
            o.setStatus(Order.Status.CANCELLED);
            //updatedOrders.add(o);TODO memory leakage
            return o;
        }
        
        return null;
    }

    @Override
    public void insertBuyOrder(final Order o) { //guaranteed to perform insertion in O(log(n))
        o.getInstrument().updateBidVolume(o.getOpenSize());
        bidLimitOrders.add(Math.abs(Collections.binarySearch(bidLimitOrders, o, new PriceTimePriorityComparator())+1), o);
    }

    @Override
    public void insertSellOrder(final Order o) {
        o.getInstrument().updateAskVolume(o.getOpenSize());
        askLimitOrders.add(Math.abs(Collections.binarySearch(askLimitOrders, o, new PriceTimePriorityComparator())+1), o);
    }

}
