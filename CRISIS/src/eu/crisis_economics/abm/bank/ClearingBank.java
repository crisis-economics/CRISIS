/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Aurélien Vermeir
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
package eu.crisis_economics.abm.bank;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

public abstract class ClearingBank extends StrategyBank
   implements ClearingMarketParticipant {
   
   private ClearingHouse
      clearingHouse;
   private ClearingMarketParticipant
      clearingMarketParticipant;
   
   public ClearingBank(
      final double initialBankCash,
      final ClearingBankStrategyFactory strategyFactory,
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final ClearingHouse clearingHouse
      ) {
      super(initialBankCash, InjectionFactoryUtils.asBankStrategyFactory(strategyFactory));
      
      Preconditions.checkNotNull(clearingHouse, marketParticipationFactory);
      this.clearingHouse = clearingHouse;
      this.clearingMarketParticipant = marketParticipationFactory.create(this);
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(
         this, "computePorfolioSizeForClearingMarkets",
         NamedEventOrderings.BANK_CONSIDER_PORTFOLIO
         );
      Simulation.repeat(
         this, "computePorfolioDistributionForClearingMarkets",
         NamedEventOrderings.PRE_CLEARING_MARKET_MATCHING
         );
      Simulation.repeat(
         this, "decideAndSetDividendPerSharePayment",
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.BANK_SHARE_PAYMENTS, 1)
         );
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private final void computePorfolioSizeForClearingMarkets() {
      getStrategy().decidePorfolioSize();
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private final void computePorfolioDistributionForClearingMarkets() {
      getStrategy().decidePortfolioDistribution();
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private final void decideAndSetDividendPerSharePayment() {
      getStrategy().decideDividendPayment();
   }
   
   @Override
   public final MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation market) {
      if(isBankrupt()) return null;
      return clearingMarketParticipant.getMarketResponseFunction(market);
   }
   
   /**
     * Get the {@link ClearingHouse} for this {@link ClearingBank}.
     */
   public final ClearingHouse getClearingHouse() {
      return clearingHouse;
   }
   
   /**
     * Get the {@link ClearingBankStrategy} for this bank. This method is
     * equivalent to (ClearingBankStrategy) getStrategy().
     */
   @Override
   public ClearingBankStrategy getStrategy() {
      return (ClearingBankStrategy) super.getStrategy();
   }
}
