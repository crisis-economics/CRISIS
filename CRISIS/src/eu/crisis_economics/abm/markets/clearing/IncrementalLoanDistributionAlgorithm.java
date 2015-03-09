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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import org.apache.commons.collections15.keyvalue.MultiKey;

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.FixedRateMortgage;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ResourceDistributionAlgorithm;
import eu.crisis_economics.utilities.Pair;

/**
  * @author bochmann 
  * @author phillips (legacy compat.)
  */
public final class IncrementalLoanDistributionAlgorithm
   implements ResourceDistributionAlgorithm<Borrower, Lender> {
   
   public IncrementalLoanDistributionAlgorithm() { }   // Stateless
   
   /** 
     * Establish a new {@link Loan} contract. At present, commercial loans
     * are implemented as {@link FixedRateMortgage} contracts.
     */
   private static Loan setupLoanContract(
      Borrower borrower,
      Lender lender,
      final double loanSize,
      final double interestRate
      ) {
      try {
         return LoanFactory.createFixedRateMortgage(
            borrower, lender, loanSize, interestRate, 1);
      } catch (LenderInsufficientFundsException fundingException) {
         fundingException.printStackTrace();
      }
      return null;
   }
   
   /** 
     * Extend a the principal of an existing {@link Loan} {@link Contract}.
     * The argument to this method should be strictly positive, or else
     * no action is taken.
     */
   static void extendExistingLoan(
      Loan existingLoan,
      final double postitiveAmount
      ) {
      if(postitiveAmount <= 0.) return;
      try {
         existingLoan.extendLoan(postitiveAmount);
      } catch(InsufficientFundsException fundingException) {
         fundingException.printStackTrace();
      }
   }
   
   @Override
   public Pair<Double, Double> distributeResources(
      final Map<String, Borrower> marketConsumers,
      final Map<String, Lender> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      ) {
      Map<Pair<String, String>, Double> interestRates = 
         new HashMap<Pair<String, String>, Double>();
      Map<String, Double> 
         loanDemands = new HashMap<String, Double>(),
         loanSupplies = new HashMap<String, Double>();
      for(Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         interestRates.put(record.getKey(), record.getValue().getSecond());
         final String
            borrowerID = record.getKey().getFirst(),
            lenderID = record.getKey().getSecond();
         final double
            desiredTrade = record.getValue().getFirst();
         if(!loanDemands.containsKey(borrowerID))
            loanDemands.put(borrowerID, 0.);
         if(!loanSupplies.containsKey(lenderID))
            loanSupplies.put(lenderID, 0.);
         loanDemands.put(borrowerID, loanDemands.get(borrowerID) + desiredTrade);
         loanSupplies.put(lenderID, loanSupplies.get(lenderID) + desiredTrade);
      }
      return this.distributeLoans(
         marketConsumers, marketSuppliers, loanDemands, loanSupplies, interestRates);
   }
   
   private Pair<Double, Double> distributeLoans(
      final Map<String, Borrower> borrowers,
      final Map<String, Lender> lenders,
      final Map<String, Double> loanDemands,
      final Map<String, Double> loanSupplies,
      final Map<Pair<String, String>, Double> interestRates
      ) {
      
      // collect amount demanded by each firm at the current interest rate
      Map<String,Double> updatedLoanDemand = new HashMap<String,Double>();
      for(String key : loanDemands.keySet())
         updatedLoanDemand.put(key, 0.);
      
      Map<String,Double> updatedLoanSupply = new HashMap<String, Double>();
      Map<String, Double> loanSuppliesCopy = new HashMap<String, Double>();
      for(String key : loanSupplies.keySet()) {
         updatedLoanSupply.put(key, 0.);
         loanSuppliesCopy.put(key, loanSupplies.get(key));
      }
   
   /** 
     * Aggregation for loans. Loans between the same participant pair (the
     * same lender-borrower pair) will be combined, by extension of the loan
     * principal, until the cash aggreation threshold LOAN_AGGREGATION_THRESHOLD
     * is reached. At this point, a new loan contract will instead be created.
     */
   class LoanAggregator {
      private static final double LOAN_AGGREGATION_THRESHOLD = 5.e7;
      private Map<MultiKey<String>, Stack<Loan>> newLoanContracts = 
         new HashMap<MultiKey<String>, Stack<Loan>>();
      // Establish a new effective loan, either (a) by creating a loan contract, or
      // (b) by extending the principal sum of an existing loan.
      void establishNew(
         String borrowerID,
         String lenderID,
         double principalValue
         ) {
         MultiKey<String> contractKey = new MultiKey<String>(borrowerID, lenderID);
         Stack<Loan> existingLoans = newLoanContracts.get(contractKey);
         if(existingLoans == null) {
            existingLoans = new Stack<Loan>();
            newLoanContracts.put(contractKey, existingLoans);
            existingLoans.push(createNewLoan(borrowerID, lenderID, principalValue));
         }
         else if (existingLoans.peek().getLoanPrincipalValue() < LOAN_AGGREGATION_THRESHOLD) {
            extendExistingLoan(existingLoans.peek(), principalValue);
         } 
         else
            existingLoans.push(createNewLoan(borrowerID, lenderID, principalValue));
      }
      
      // Create a new loan contract.
      private Loan createNewLoan(
         String borrowerID,
         String lenderID,
         double principalValue
         ) {
         return setupLoanContract(
            borrowers.get(borrowerID),
            lenders.get(lenderID),
            principalValue,
            interestRates.get(new Pair<String, String>(borrowerID, lenderID))
            );
      }
   }
   LoanAggregator loanAggregator = new LoanAggregator();
   
   final Iterator<Entry<String, Double>> firmIter = loanDemands.entrySet().iterator();
   while (firmIter.hasNext()) {
       Entry<String, Double> firm = firmIter.next();
       final Iterator<Entry<String, Double>> bankIter = loanSuppliesCopy.entrySet().iterator();
       while (bankIter.hasNext()) { 
           Entry<String, Double> bank = bankIter.next();
           // leverage circle in case lender == borrower's depositor - try same bank again
           while (updatedLoanDemand.get(firm.getKey())<firm.getValue()&&updatedLoanSupply.get(bank.getKey())<bank.getValue()) { 
               double reserves = ((Bank) lenders.get(bank.getKey())).getCashReserveValue();
               if (reserves>0.0) {
                   double increment  = Math.min(reserves,Math.min(firm.getValue()-updatedLoanDemand.get(firm.getKey()),bank.getValue()-updatedLoanSupply.get(bank.getKey())));
                   updatedLoanDemand.put(firm.getKey(),updatedLoanDemand.get(firm.getKey())+increment);
                   updatedLoanSupply.put(bank.getKey(),updatedLoanSupply.get(bank.getKey())+increment);
                   loanSupplies.put(bank.getKey(), loanSupplies.get(bank.getKey()) - increment); //legacy
                   loanAggregator.establishNew(firm.getKey(), bank.getKey(), increment);
               } else {
                   // leverage circle in case lender != borrower's depositor - try all (other) banks
                   final Iterator<Entry<String, Double>> otherBankIter = loanSuppliesCopy.entrySet().iterator();
                   while (otherBankIter.hasNext()) { 
                       Entry<String, Double> otherBank = otherBankIter.next();
                       while (updatedLoanDemand.get(firm.getKey())<firm.getValue()&&updatedLoanSupply.get(otherBank.getKey())<otherBank.getValue()) { 
                           reserves = ((Bank) lenders.get(otherBank.getKey())).getCashReserveValue();
                           if (reserves>0.0) {
                               double increment  = Math.min(reserves,Math.min(firm.getValue()-updatedLoanDemand.get(firm.getKey()),otherBank.getValue()-updatedLoanSupply.get(otherBank.getKey())));
                               updatedLoanDemand.put(firm.getKey(),updatedLoanDemand.get(firm.getKey())+increment);
                               updatedLoanSupply.put(otherBank.getKey(),updatedLoanSupply.get(otherBank.getKey())+increment);
                               loanSupplies.put(otherBank.getKey(), loanSupplies.get(otherBank.getKey()) - increment); //legacy
                               loanAggregator.establishNew(firm.getKey(), otherBank.getKey(), increment);
                           } else break;
                       }
                       if (updatedLoanDemand.get(firm.getKey())>=firm.getValue()) 
                           break; // otherBank loop shortcut
                   }
                   // in case cash is not flowing back to any bank, bank has to handle cash flow exception
                   double increment = firm.getValue()-updatedLoanDemand.get(firm.getKey());
                   if (increment > 0) {
                       updatedLoanDemand.put(firm.getKey(),updatedLoanDemand.get(firm.getKey())+increment);
                       updatedLoanSupply.put(bank.getKey(),updatedLoanSupply.get(bank.getKey())+increment);
                       loanSupplies.put(bank.getKey(), loanSupplies.get(bank.getKey()) - increment); //legacy
                       loanAggregator.establishNew(firm.getKey(), bank.getKey(), increment);
                   }
                   break; // no point of trying same bank again
               }
           }
           if (updatedLoanDemand.get(firm.getKey())>=firm.getValue()) 
               break; // bank loop shortcut
         }
      }
      
      double
         effectiveLoan = 0.,
         tradeWeightedInterestRate = 0.;
      int numberOfLoansCreated = 0;
      for(Stack<Loan> loans : loanAggregator.newLoanContracts.values()) {
         for(Loan loan : loans) {
            effectiveLoan += loan.getLoanPrincipalValue();
            tradeWeightedInterestRate += loan.getLoanPrincipalValue() * loan.getInterestRate();
         }
         numberOfLoansCreated += loans.size();
      }
      System.out.println("Number of loans created: " + numberOfLoansCreated + ".");
      loanAggregator.newLoanContracts.clear();
      return new Pair<Double, Double>(effectiveLoan, tradeWeightedInterestRate/effectiveLoan);
   }
}
