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
package eu.crisis_economics.abm.fund;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.GraduallySellSharesMarketResponseFunctionFactory;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.factories.AbstractClearingMarketParticipantFactory;

/**
  * A trivial implementation of the {@link Fund} interface.<br><br>
  * 
  * This implementation provides the minimum non-exogenous behaviours that 
  * are required of a functional, trivial {@link Fund} {@link Agent}. This 
  * implementation will not participate in any markets and will, except in 
  * the case of external intervention, hold its assets entirely in cash. This 
  * {@link Fund} will do virtually nothing whatsoever unless it is first 
  * instructed to take action by an external {@link Agent}.
  * 
  * @author phillips
  */
public final class TrivialFund extends AbstractFund {
   @Inject
   public TrivialFund(
   @Assisted
      final DepositHolder depositHolder,
      final ClearingHouse clearingHouse
      ) {
      super(depositHolder,
         new AbstractClearingMarketParticipantFactory() {
         @Override
         protected void setupStockMarketResponses() {
            for(final ClearingStockMarket market :
               clearingHouse.getMarketsOfType(ClearingStockMarket.class))
               responses().put(
                  market.getMarketName(),
                  (new GraduallySellSharesMarketResponseFunctionFactory(market))
                  );
         }
         @Override
         protected void setupLoanMarketResponses() { }
      });
   }
   
   @Override
   public <T> T accept(AgentOperation<T> operation) {
      return operation.operateOn((Fund) this);
   }
   
   @Override
   public void preClearingProcessing() {
      // No action.
   }
   
   @Override
   public void setStocksToInvestIn(
      final CollectionProvider<ClearingStockMarket> markets) {
      // No action.
   }
   
   @Override
   public void setLoansToInvestIn(
      final CollectionProvider<ClearingLoanMarket> markets) {
      // No action.
   }
}
