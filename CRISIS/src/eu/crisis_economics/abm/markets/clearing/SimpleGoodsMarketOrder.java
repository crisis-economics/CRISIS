/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import eu.crisis_economics.abm.contracts.GoodHolder;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.abm.household.MacroHousehold;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;

final public class SimpleGoodsMarketOrder {
    public enum Side {
        BUY, SELL
    }
    
    private Party party;
    private SimpleGoodsInstrument instrument;
    private Side side;
    
    private double
        size,
        price,
        openSize;
    
    public SimpleGoodsMarketOrder(
        Party party,
        SimpleGoodsInstrument instrument, 
        double orderSize, 
        double orderUnitPrice)
        throws OrderException {
        this.party = party;
        this.instrument = instrument;
        this.price = orderUnitPrice;
        
        if (orderSize > 0) {
            this.side = Side.SELL;
            this.size = orderSize;
        } else {
            this.side = Side.BUY;
            this.size = -orderSize;
        }
        
        this.openSize = this.size;
    }
    
    protected void execute(double volume, double price) {
        this.openSize -= volume;
    }

    protected void disallocatePartyAsset() {
        if (side == Side.BUY) {
            ((GoodsBuyer) this.getParty()).disallocateCash(getOpenSize() * this.getPrice());
        } else {
            SimpleGoodsInstrument inst = this.getInstrument();
            ((GoodsSeller) this.getParty()).getGoodsRepository().changeAllocatedQuantityBy(
               inst.getGoodsType(), -getOpenSize());
        }
    }
    
    public Side getSide() {
        return side;
    }
    
    public double getOpenSize() {
        return openSize;
    }

    public void updateOpenSize(double volume) {
        this.openSize -= volume;
    }
    
    /** Add an additional amount to the tradeable volume, and reset the price per unit trade. */
    public void addAdditionalQuantityAndUpdatePrice(
       double positiveAdditionalQuantity, double updatedPrice) {
       if(positiveAdditionalQuantity <= 0.)
          throw new IllegalArgumentException(
             "GoodsMarketOrder.addAdditionalQuantityAndUpdatePrice: additional quantity " + 
             "is non-positive (value " + positiveAdditionalQuantity + ").");
       this.size += positiveAdditionalQuantity;
       this.openSize += positiveAdditionalQuantity;
       resetPrice(updatedPrice);
    }
    
    /** Reset the quantity of this order. */
    public void resetQuantity(double newPositiveQuantity) {
       if(newPositiveQuantity < 0.)
          throw new IllegalArgumentException(
             "GoodsMarketOrder.resetQuantity: new market order quantity (value " + 
             newPositiveQuantity + ") is negative.");
       size = newPositiveQuantity;
       openSize = newPositiveQuantity;
    }
    
    /** Reset the price of this order. */
    public void resetPrice(double newPrice) {
       if(newPrice < 0.)
          throw new IllegalArgumentException(
             "GoodsMarketOrder.resetPrice: new price (value " + newPrice + ") is negative.");
       price = newPrice;
    }
    
    /** Get the total size (allocated and unallocated units) of this order. */
    public double getSize() {
        return size;
    }

    /** Get the unit price of this order. */
    public double getPrice() {
        return price;
    }

    private SimpleGoodsInstrument getInstrument() {
        return instrument;
    }
    
    /** 
      * Get the goods type of this order.
      */
    public String getGoodsType() {
       return instrument.getGoodsType();
    }

    public Party getParty() {
        return party;
    }

    public boolean isRegisteredSeller(Party party) {
        if (null == party) {
            throw new IllegalArgumentException("party == null");
        }
        
        if (party instanceof GoodHolder)
            return true;
        else
            return false;
    }

    public boolean isRegisteredBuyer(Party party) {
        if (null == party) {
            throw new IllegalArgumentException("party == null");
        }
        
        if (party instanceof GoodHolder)
            return true;
        else
            return false;
    }

    public void cancel() {
        disallocatePartyAsset();
        this.openSize = 0.;
        if (party instanceof MacroFirm)
            ((MacroFirm) party).removeOrder(this);
        else if (party instanceof MacroHousehold)
            ((MacroHousehold) party).removeOrder(this);
        
        instrument.discontinueOrderTracking(this);
    }

}
