/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ResourceDistributionAlgorithm;
import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
public final class UpfrontInjectionLoanDistributionAlgorithm implements
   ResourceDistributionAlgorithm<Borrower, Lender> {
   
   public UpfrontInjectionLoanDistributionAlgorithm() { } // Stateless
   
   private static final boolean VERBOSE_MODE = false;
   
   /**
     * Loan distribution algorithm based on an upfront Lender
     * cash injections prior to contract creation. If the 
     * cash injection does not succeed, or is insufficient to
     * meet the requirements of the Lender, then loan supply
     * distributed by the lender is reduced by a fixed fraction
     * for all Borrowers.
     * @see ResourceDistributionAlgorithm.distributeResources
     */
   @Override
   public Pair<Double, Double> distributeResources(
      final Map<String, Borrower> marketConsumers,
      final Map<String, Lender> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      ) {
      if(VERBOSE_MODE) {
         System.out.printf("Upfront injection loan formation. Planned exchanges:\n");
         for(Entry<Pair<String, String>, Pair<Double, Double>> record :
             desiredResourceExchanges.entrySet()) {
            final String
               to = record.getKey().getFirst(),
               from = record.getKey().getSecond();
            final double
               volume = record.getValue().getFirst(),
               rate = record.getValue().getSecond();
            System.out.printf(
               "To %s from %s: volume: %16.10g at rate %16.10g\n",
               to, from, volume, rate
               );
         }
         System.out.printf("- end of planned exchanges.\n");
      }
      final Map<String, Double>
         lenderCashAmountToRaise = new HashMap<String, Double>(),
         lenderSupplyRationFactors = new HashMap<String, Double>();
      for(Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         final String
            lenderID = record.getKey().getSecond();
         if(!lenderCashAmountToRaise.containsKey(lenderID))
            lenderCashAmountToRaise.put(lenderID, 0.);
         lenderCashAmountToRaise.put(
            lenderID, lenderCashAmountToRaise.get(lenderID) + record.getValue().getFirst());
      }
      for(Entry<String, Double> record : lenderCashAmountToRaise.entrySet()) {
         final Lender
            lender = marketSuppliers.get(record.getKey());
         final double
            amountToRaise = record.getValue() - lender.getUnallocatedCash();
         if(amountToRaise <= 0. || record.getValue() == 0.)
            lenderSupplyRationFactors.put(record.getKey(), 1.);
         else {
            lender.cashFlowInjection(record.getValue());
            double
               newLenderLiquidity = lender.getUnallocatedCash(),
               lenderSupplyRationFactor = 
                  (newLenderLiquidity / record.getValue()) * (1. - 1.e-6);
            lenderSupplyRationFactor = NumberUtil.clamp(0., lenderSupplyRationFactor, 1.);
            lenderSupplyRationFactors.put(record.getKey(), lenderSupplyRationFactor);
         }
      }
      double
         effectiveLoan = 0.,
         tradeWeightedInterestRate = 0.;
      for(Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         final double
            supplierRationFactor = lenderSupplyRationFactors.get(record.getKey().getSecond()),
            loanPrincipalToExchange = record.getValue().getFirst() * supplierRationFactor,
            interestRate = record.getValue().getSecond();
         final Borrower borrower =
            marketConsumers.get(record.getKey().getFirst());
         final Lender lender =
            marketSuppliers.get(record.getKey().getSecond());
         try {
            if(VERBOSE_MODE)
               System.out.printf(
                  "creating loan between %s and %s. volume: %16.10g, principal: %16.10g\n",
                  borrower.getUniqueName(),
                  lender.getUniqueName(),
                  loanPrincipalToExchange,
                  interestRate
                  );
            LoanFactory.createFixedRateMortgage(
                borrower, lender, loanPrincipalToExchange, interestRate, 1);
            effectiveLoan += loanPrincipalToExchange;
            tradeWeightedInterestRate += loanPrincipalToExchange * interestRate;
         } catch (LenderInsufficientFundsException fundingException) {
            if(VERBOSE_MODE)
               System.out.printf("* loan creation failed.\n");
         }
      }
      if(VERBOSE_MODE)
         System.out.printf("total loans formed: value: %16.10g\n", effectiveLoan);
      return new Pair<Double, Double>(
         effectiveLoan, effectiveLoan == 0. ? 0 : tradeWeightedInterestRate / effectiveLoan);
   }
}
