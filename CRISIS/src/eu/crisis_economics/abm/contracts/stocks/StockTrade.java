/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
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
package eu.crisis_economics.abm.contracts.stocks;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.contracts.TwoPartyContract;

/**
 * @author olaf
 * 
 */
public final class StockTrade extends TwoPartyContract {
   
   private final String instrumentName;
   private final double
      pricePerShare,
      volume;
   
   public StockTrade(
      final String instrumentName,
      final double pricePerShare,
      final double volume,
      final StockHolder buyer,
      final StockHolder seller
      ) {
      super(buyer, seller);
      if(instrumentName == null)
         throw new NullArgumentException();
      this.instrumentName = instrumentName;
      this.pricePerShare = pricePerShare;
      this.volume = volume;
   }
   
   public final String getInstrumentName() {
      return instrumentName;
   }
   
   public final double getPricePerShare() {
      return pricePerShare;
   }
   
   public final double getVolume() {
      return volume;
   }
   
   public final StockHolder getBuyer() {
      return (StockHolder)super.getFirstSettlementParty();
   }
   
   public final StockHolder getSeller() {
      return (StockHolder)super.getSecondSettlementParty();
   }
   
   @Override
   public double getValue() {
      return 0.;
   }
   
   @Override
   public void setValue(double newValue) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public double getFaceValue() {
      return 0;
   }
   
   @Override
   public void setFaceValue(double value) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public double getInterestRate() {
      return 0;
   }
   
   @Override
   public void setInterestRate(double interestRate) {
      throw new UnsupportedOperationException();
   }
}
