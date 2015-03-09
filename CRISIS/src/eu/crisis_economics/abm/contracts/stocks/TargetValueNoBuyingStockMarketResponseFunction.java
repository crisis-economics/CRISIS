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
package eu.crisis_economics.abm.contracts.stocks;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.AbstractResponseFunction;

/**
  * An implementation of the {@link AbstractResponseFunction}.<br><br>
  * 
  * This implementation describes a stock market response function in which a stock 
  * market participant posts a target investment value. The participant will offer 
  * to sell any number of shares that prove to be necessary to achieve this target value, 
  * however the participant will never (no matter at what price) offer to buy shares.
  * 
  * @author phillips
  */
public final class TargetValueNoBuyingStockMarketResponseFunction
   extends AbstractResponseFunction {
   
   private double
      desiredInvestmentValue,
      numberOfSharesOwned;
   
   /**
     * Create a {@link TargetValueNoBuyingStockMarketResponseFunction} object with
     * custom parameters.
     * 
     * @param numberOfSharesOwned
     *        The number of shares currently owned by the calling clearing market participant
     * @param desiredInvestmentValue
     *        The desired investment value in stocks.
     */
   public TargetValueNoBuyingStockMarketResponseFunction(   // Immutable
      final double numberOfSharesOwned,
      final double desiredInvestmentValue
      ) {
      super(0., Double.MAX_VALUE);
      if(numberOfSharesOwned < 0. ||
         desiredInvestmentValue < 0.)
         throw new IllegalArgumentException(
            "TargetValueNoBuyingStockMarketResponseFunction: the caller must own a positive "
            + "number of shares in the market, and have a positive target investment value, "
            + "in order to paticipate in a clearing market with this response function. " 
            + "(number of shares owned: " + numberOfSharesOwned + ", desired investment value: "
            + desiredInvestmentValue + ").");
      this.numberOfSharesOwned = numberOfSharesOwned;
      this.desiredInvestmentValue = desiredInvestmentValue;
   }
   
   @Override
   public double[] getValue(final int[] queries, TradeOpportunity[] opportunities) {
      final int query = queries[0];
      if(query > 0 || opportunities.length != 1)
         throw new UnsupportedOperationException(
            "TargetValueNoBuyingStockMarketResponseFunction.getValue: this market response " +
            "function can be used only for clearing networks with one unique stock type."
            );
      final double
         pricePerShare = opportunities[0].getRate(),
         valueOfExistingShares = pricePerShare * numberOfSharesOwned,
         responseToExistingPosition = Math.min(0., desiredInvestmentValue - valueOfExistingShares);
      return new double[] { responseToExistingPosition };
   }
   
   /**
     * @return The desired value of the stock investment.
     */
   public double getDesiredInvestmentValue() {
      return desiredInvestmentValue;
   }
   
   /**
     * @return The number of shares owned by the market participant.
     */
   public double getNumberOfSharesOwned() {
      return numberOfSharesOwned;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         " TargetValueNoBuyingStockMarketResponseFunction, desired investment value: " +
            + desiredInvestmentValue + ", number of shares owned: " + numberOfSharesOwned + ".";
   }
}
