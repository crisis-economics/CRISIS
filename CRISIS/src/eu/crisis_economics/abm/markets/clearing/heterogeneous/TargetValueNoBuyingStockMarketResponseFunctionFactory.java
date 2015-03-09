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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.RaisingAgentOperation;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.TargetValueNoBuyingStockMarketResponseFunction;
import eu.crisis_economics.abm.fund.PortfolioFund;

/**
  * An implementation of the {@link ClearingMarketResponseFactory} interface.
  * This implementation creates a stock instrument {@link MarketResponseFunction}
  * for a {@link StockHolder} {@link Agent}. The resulting response function
  * specifies the <u><i>target value</i></u> of shares the {@link StockHolder} 
  * should hold following market clearing for the stock instrument and forbits the
  * {@link StockHolder} from offering to buy any shares.
  * 
  * @author phillips
  */
public final class TargetValueNoBuyingStockMarketResponseFunctionFactory
   extends RaisingAgentOperation<MarketResponseFunction> {
   
   private final ClearingStockMarket
      market;
   
   /**
     * Create a {@link TargetValueNoBuyingStockMarketResponseFunctionFactory}
     * with custom parameters. This factory creates a {@link MarketResponseFunction}
     * for a {@link ClearingStockMarket}. The market response is such that the 
     * participant will attempt to secure a predetermined investment value in 
     * stocks following market clearing, as long as this investment target does
     * not result in the participant offering to buy shares.
     * 
     * @param market
     *        The {@link ClearingMarket} for which a {@link MarketResponseFunction}
     *        should be generated. This argument must be an instance of a 
     *        {@link ClearingStockMarket}, or else {@link IllegalArgumentException}
     *        is raised.
     */
   @Inject
   public TargetValueNoBuyingStockMarketResponseFunctionFactory(
   @Assisted
      final ClearingMarket market
      ) {
      Preconditions.checkNotNull(market);
      Preconditions.checkArgument(market instanceof ClearingStockMarket);
      this.market = (ClearingStockMarket) market;
   }
   
   public MarketResponseFunction operateOn(final PortfolioFund participant) {
      final double
         targetInvestment = Math.max(
            participant.getDesiredStockInvestmentIn(market.getStockReleaserName()), 0.),
         numberOfSharesOwned = participant.getNumberOfSharesOwnedIn(market.getStockReleaserName());
      System.out.printf("Fund: ");
      return create(targetInvestment, numberOfSharesOwned);
   }
      
   public MarketResponseFunction operateOn(final CommercialBank participant) {
      final double 
         targetInvestment = Math.max(
            participant.getStrategy().getDesiredStockInvestmentIn(market), 0.),
         numberOfSharesOwned = participant.getNumberOfSharesOwnedIn(
            market.getStockReleaserName());
      System.out.printf("Bank: ");
      return create(targetInvestment, numberOfSharesOwned);
   }
   
   /**
     * Create a {@link MarketResponseFunction} for a {@link StockHolder}
     * {@link ClearingMarketParticipant} who wishes to bid for a target
     * value of stocks over the clearing markets.
     * 
     * @param targetInvestment
     *        The desired investment in stocks.
     * @param numberOfSharesOwned
     *        The number of shares already owned by the participant.
     */
   private MarketResponseFunction create(
      final double targetInvestment,
      final double numberOfSharesOwned
      ) {
      System.out.printf(
         "desired stock investment:        %16.10g, owned shares: %16.10g\n",
         targetInvestment, numberOfSharesOwned);
      
      if(numberOfSharesOwned == 0. && targetInvestment == 0.)
         return null;
      return new TargetValueNoBuyingStockMarketResponseFunction(
         numberOfSharesOwned,
         targetInvestment
         );
   }
   
   public ClearingMarket getMarket() {
      return market;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Target Value Stock Market Response Function Factory," +
             "stock instrument: " + market.getMarketName() + ".";
   }
}
