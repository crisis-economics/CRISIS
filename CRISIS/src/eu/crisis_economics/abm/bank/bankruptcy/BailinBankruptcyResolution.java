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
package eu.crisis_economics.abm.bank.bankruptcy;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.BankBankruptcyStockInstrumentErasureEvent;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.intermediary.Intermediary;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.Pair;

/**
  * An implementation of a bailin resolution bankruptcy handler for
  * {@link StockReleasingBank} {@link Agent}{@code s}.
  * 
  * @author lovric
  * @author phillips
  * @author de-caux
  */
public final class BailinBankruptcyResolution
   extends SimpleAbstactAgentOperation<Boolean> implements BankBankruptyHandler {
   
   private AgentOperation<Double>
      equityModification;
   private SingleBeneficiaryIntermediaryFactory
      intermediaryFactory;
   
   /**
     * Create a {@link BailinBankruptcyResolution} object with a custom
     * CAR target.
     * 
     * @param equityModification
     *        An algorithm which accepts the state of the bankrupt
     *        {@link Agent} and returns an equity change that is required
     *        in order to complete the bankruptcy resolution process.
     */
   @Inject
   public BailinBankruptcyResolution(
   @Named("BANK_BAILIN_RESOLUTION_EQUITY_ADJUSTMENT")
      final AgentOperation<Double> equityModification,
      final SingleBeneficiaryIntermediaryFactory intermediaryFactory
      ) {
      Preconditions.checkNotNull(equityModification, intermediaryFactory);
      this.equityModification = equityModification;
      this.intermediaryFactory = intermediaryFactory;
   }
   
   /**
     * Perform a bailin procedure for this {@link StockReleasingBank}. The
     * bailin will be processed as follows:<br>
     * <br>
     * (a) terminate all stock accounts in this {@link StockReleaser},
     *     including, potentially, shares owned by this {@link StockReleasingBank} in
     *     itself;<br>
     * (b) compute the amount required to restore the bank balance sheet to the
     *     specified CAR level;<br>
     * (c) write down loan liabilities, in equal proportions, until either
     *     (i.) no further loan debt can be written down, or (ii.) the sum in (b)
     *     has been completely deducted from loan debt;<br>
     * (d) if necessary, continue to write down deposit account liabilities, in 
     *     equal proportions, until either (i.) no further deposit debt can be written
     *     down, or (ii.) the sum in (b) has been completely deducted from deposit 
     *     account debts;<br>
     * (e) issue new shares to all creditors who have suffered losses. The number
     *     of shares received by each creditor depends on the size of the loss
     *     suffered during steps (c) and (d).<br><br>
     * 
     * If this {@link StockReleasingBank} has emitted no shares at the time 
     * this method is called, then 1.0 units of new shares, in total, will
     * be created for creditors who have suffered losses.<br><br>
     * 
     * If this {@link StockReleasingBank} cannot satisfy the CAR level computed
     * in (b) by writing down loan loan and deposit account liabilities, then
     * this {@link Bank} will be scheduled for liquidation.
     * 
     * The {@link Bank} must be an instance of a {@link StockReleasingBank}. Otherwise,
     * this algorithm performs no action and returns {@code false}.
     */
   @Override
   public Boolean operateOn(Agent agent) {
      if(!(agent instanceof StockReleasingBank))
         return false;
      final StockReleasingBank bank = (StockReleasingBank) agent;
      
      double currentNumberOfShares =
         UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(bank.getUniqueName());
      
      System.out.printf(
         "--------------------------------------------\n"
       + "* Bank bailin commencing:\n"
       + "  Bank equity:              %16.10g\n"
       + "  Bank assets (total):      %16.10g\n"
       + "  Bank liabilities (total): %16.10g\n"
       + "  Current number of shares: %16.10g\n",
         bank.getEquity(),
         bank.getTotalAssets(),
         bank.getTotalLiabilities(),
         currentNumberOfShares
         );
      
      if (currentNumberOfShares == 0.) {                                   // No shares
         System.out.flush();
         System.err.println(
            "Bank.bailin: no shares have been emitted in this releaser. 1.0 units of new "
          + "shares will be created. ");
         System.err.flush();
         currentNumberOfShares = 1.0;
      }
      
      /*
       * This agent may own stocks in itself. This action may further 
       * reduce the agent equity:
       */
      UniqueStockExchange.Instance.getOwnershipTracker(bank.getUniqueName())
         .terminateAllExistingShareAccountsWithoutCompensation();
      
      final double bailinSum =
         NumberUtil.incrementBySmallestAmount(equityModification.operateOn(bank));
      
      if(bailinSum <= 0.) {
         System.err.println(
           "Bank.bailin: bailin resolution amount is negative. No action is" + 
           " to be taken.");
         System.err.flush();
         System.out.printf("--------------------------------------------\n");
         return false;
      }
      
      System.out.printf(
         "  Bailin sum:               %16.10g\n",
         bailinSum
         );
      
      // Calculate total liabilities (loans and deposits only)
      double
         totalDepositLiabilities = 0,
         totalLoanLiabilities = 0;
      for (final Contract contract : bank.getLiabilities()) {
         if (contract instanceof DepositAccount) {
            if(contract.getValue() <= 0.) continue;
            totalDepositLiabilities += contract.getValue();
         }
         if (contract instanceof Loan)
            totalLoanLiabilities += contract.getValue();
      }
      
      // Check whether there is enough liability to cover the bail-in amount
      final double
         totalLiabilities = totalDepositLiabilities + totalLoanLiabilities;
      if (bailinSum > totalLiabilities) {
         System.out.printf(
             "* Insufficient liabilities to process bailin.\n"
           + "  Bailin resolution failed.\n"
           + "--------------------------------------------\n");
         return true;
      }
      
      /*
       * Determine the proportions of loans and deposits to write down.
       * If loans are sufficient for bail-in, deposits will remain 
       * untouched.
       */
      final double
         proportionLoans = Math.min(bailinSum / totalLoanLiabilities, 1.0),
         proportionDeposits = Math.max(bailinSum - totalLoanLiabilities, 0.0) 
            / totalDepositLiabilities; 
      
      // Remember affected creditors and their amounts written down:
      final List<Pair<UniquelyIdentifiable, Double>>
         affectedCreditors = new ArrayList<Pair<UniquelyIdentifiable, Double>>();
      
      // Write down proportionLoans of Loans:
      for (final Contract contract : bank.getLiabilities()) {
         if (contract instanceof Loan) {
            final double
               convertedValue = contract.getValue() * proportionLoans,
               newValue = contract.getValue() * (1 - proportionLoans);
            
            // Record affected creditors (loan owners) and their amounts to be
            // written down:
            affectedCreditors.add(
               new Pair<UniquelyIdentifiable, Double>(
                  ((Loan) contract).getLender(), convertedValue));
            
            contract.setValue(newValue);
         }
      }
      
      // Write down proportionDeposits of deposits (if loans were not enough):
      if (proportionDeposits > 0.0)
         for (final Contract contract : bank.getLiabilities()) {
            if (contract instanceof DepositAccount) {
               if(contract.getValue() <= 0.) continue;
               
               final double
                  convertedValue = contract.getValue() * proportionDeposits,
                  newValue = contract.getValue() * (1 - proportionDeposits);
               
               // Record affected depositors and their amounts to be written down:
               affectedCreditors.add(new Pair<UniquelyIdentifiable, Double>(
                  ((DepositAccount) contract).getDepositor(), convertedValue));
               
               contract.setValue(newValue);
           }
        }
     if (bank.getEquity() < 0.) {
        System.out.printf(
            "* Bailin process has not restored positive equity.\n"
          + "  Bailin process failed.\n"
          + "--------------------------------------------\n");
        return true;
     }
     
     final double
        newPricePerShare = bank.getEquity() / currentNumberOfShares;
     
     UniqueStockExchange.Instance.setStockPrice(bank.getUniqueName(), newPricePerShare);
     
     /*
      * Issue new shares to agents who have suffered losses:
      */
     for(Pair<UniquelyIdentifiable, Double> pair : affectedCreditors) {
        final double
           numberOfSharesGenerated = (pair.getSecond() / bailinSum) * currentNumberOfShares;
        if (  pair.getFirst() instanceof StockHolder && 
            !(pair.getFirst() instanceof Household) &&
            !(pair.getFirst() instanceof CentralBank) ) { 
           UniqueStockExchange.Instance.generateFreeSharesFor(
              bank.getUniqueName(), (StockHolder) pair.getFirst(), numberOfSharesGenerated);
           System.out.printf(
             "* %16.10g shares sent to %s\n",
             numberOfSharesGenerated,
             pair.getFirst().getUniqueName()
             );
        } else {                                   // If not StockHolder, use an intermediary.
           Intermediary intermediary = intermediaryFactory.createFor(
              (SettlementParty) pair.getFirst());
           UniqueStockExchange.Instance.generateFreeSharesFor(
              bank.getUniqueName(), intermediary, numberOfSharesGenerated);
           System.out.printf(
              "* %16.10g shares sent to %s, on behalf of %s\n",
              numberOfSharesGenerated,
              intermediary.getUniqueName(),
              pair.getFirst().getUniqueName()
              );
        }
     }
     
     System.out.printf(
        "* Bank bailin recieved.\n"
      + "  Bank equity:              %16.10g\n"
      + "  Bank assets (total):      %16.10g\n"
      + "  Bank liabilities (total): %16.10g\n"
      + "  New price per share:      %16.10g\n"
      + "  New number of shares:     %16.10g\n"
      + "* Bailin complete.\n"
      + "--------------------------------------------\n",
        bank.getEquity(),
        bank.getTotalAssets(),
        bank.getTotalLiabilities(),
        newPricePerShare,
        UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(bank)
        );
     
     if(bank.getEquity() <= 0.) {
        System.err.println(
           "StockReleasingBank.bailin: equity (value " + bank.getEquity() + ") after bailout "
         + "is negative. This behaviour is not expected in the current implementation."
           );
        System.err.flush();
     }
     
     Simulation.events().post(new BankBankruptcyStockInstrumentErasureEvent(bank.getUniqueName()));
     
     return true;
   }
}
