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
package eu.crisis_economics.abm.firm.bankruptcy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockOwnershipTracker;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.intermediary.Intermediary;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.utilities.NumberUtil;

public class DebtProportionStockRedistributionAlgorithm implements StockRedistributionAlgorithm {
   
   private static final long serialVersionUID = 5964749855176720391L;
   
   private final SingleBeneficiaryIntermediaryFactory
      intermediaryFactory;
   
   @Inject
   public DebtProportionStockRedistributionAlgorithm(
      final SingleBeneficiaryIntermediaryFactory intermediaryFactory
      ) {
      this.intermediaryFactory = Preconditions.checkNotNull(intermediaryFactory);
   }
   
   @Override
   public void processStockRedistribution(
      final StockReleasingFirm firm,
      final Map<String, CreditorDebts> creditorDebts
      ) throws NoStockRedistributionSolutionException 
   {
      final double
         stockTotalValue =
            UniqueStockExchange.Instance.getStockPrice(firm) *
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(firm);
      final String
         stockName = firm.getUniqueName();
      double
         totalDebt = 0.0,
         stockValueHeldByCreditors = 0.0;
      final StockOwnershipTracker
         tracker = UniqueStockExchange.Instance.getOwnershipTracker(stockName);
      for(final Entry<String, CreditorDebts> record : creditorDebts.entrySet()) {
         totalDebt += record.getValue().getDebt();
         final String
            creditorName = record.getKey();
         if(tracker.doesOwnShares(creditorName))
            stockValueHeldByCreditors += tracker.getValueOfSharesOwned(creditorName);
      }
      if(totalDebt == 0.0)
         return;
      final List<StockHolder>
         nonCreditorStockHolders = new ArrayList<StockHolder>();
      final double
         stockValueNotHeldByCreditors = Math.max(0.0, stockTotalValue - stockValueHeldByCreditors);
      if(stockValueNotHeldByCreditors < totalDebt)
         throw new NoStockRedistributionSolutionException();
      for(final StockHolder holder : tracker) {
         if(!creditorDebts.containsKey(holder.getUniqueName()))
            nonCreditorStockHolders.add(holder);
      }
      double
         rationing =
            NumberUtil.clamp(0.0, totalDebt / stockValueNotHeldByCreditors, 1.0) /
               creditorDebts.size();
      for(final StockHolder holder : nonCreditorStockHolders) {
         final double
            sharesOwned = holder.getStockAccount(stockName).getQuantity(),
            sharesToRedistribute = sharesOwned * rationing;
         for(final CreditorDebts debts : creditorDebts.values()) {
            final StockAccount
               stocksToTransfer = holder.getStockAccount(stockName);
            if(debts.getLender() instanceof StockHolder) {
               stocksToTransfer.transferSharesTo(
                  sharesToRedistribute, (StockHolder) debts.getLender());
            }
            else {
               final Intermediary
                  intermediary = intermediaryFactory.createFor(debts.getLender());
               stocksToTransfer.transferSharesTo(
                  sharesToRedistribute, intermediary);
            }
         }
      }
   }
}
