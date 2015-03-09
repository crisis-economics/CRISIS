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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.BankBankruptcyStockInstrumentErasureEvent;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of a bailout resolution bankruptcy handler for
  * {@link StockReleasingBank} {@link Agent}{@code s}.
  * 
  * @author lovric
  * @author phillips
  * @author de-caux
  */
public final class BailoutBankruptcyResolution
   extends SimpleAbstactAgentOperation<Boolean> implements BankBankruptyHandler {
   
   private AgentOperation<Double>
      equityModification;
   private Government
      government;
   
   /**
     * Create a {@link BailoutBankruptcyResolution} object with a custom
     * CAR target.
     * 
     * @param equityModification
     *        An algorithm which accepts the state of the bankrupt
     *        {@link Agent} and returns an equity change that is required
     *        in order to complete the bankruptcy resolution process.
     * @param government
     *        The {@link Government} {@link Agent} that is to be responsible
     *        for conducting the bailin operation.
     */
   @Inject
   public BailoutBankruptcyResolution(
   @Named("BANK_BAILOUT_RESOLUTION_EQUITY_ADJUSTMENT")
      final AgentOperation<Double> equityModification,
      final Government government
      ) {
      this.equityModification = equityModification;
      this.government = Preconditions.checkNotNull(government);
   }
   
   /**
     * Perform a bailout procedure for this {@link StockReleasingBank}. The
     * bailout will be processed as follows:<br>
     * <br>
     * (a) terminate all stock accounts in this this {@link StockReleaser},
     *     including, potentially, shares owned by this {@link StockReleasingBank} in
     *     itself;<br>
     * (b) compute the amount required to restore the bank balance sheet to the
     *     specified CAR level;<br>
     * (c) request this sum from the {@link Government}, in exchange for shares;<br>
     * (d) if the {@link Government} is unable, or declines, to pay the sum
     *     bailout sum, then this resolution policy fails.<br><br>
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
       + "* Bank bailout commencing:\n"
       + "  Bank equity:              %16.10g\n"
       + "  Bank assets (total):      %16.10g\n"
       + "  Bank liabilities (total): %16.10g\n"
       + "  Current number of shares: %16.10g\n"
       + "  Government cash reserve:  %16.10g\n",
         bank.getEquity(),
         bank.getTotalAssets(),
         bank.getTotalLiabilities(),
         currentNumberOfShares,
         government.getCashReserveValue()
         );
      
      /*
       * This agent may own stocks in itself. This action may further 
       * reduce the agent equity:
       */
      UniqueStockExchange.Instance.getOwnershipTracker(bank.getUniqueName())
         .terminateAllExistingShareAccountsWithoutCompensation();
      
      
      
      final double bailoutSum = equityModification.operateOn(bank);
      
      System.out.printf(
         "  Bailout sum:              %16.10g\n",
         bailoutSum
         );
      
      // Attempt the bailout:
      try {
         bank.debit(government.bailout(bailoutSum));
      } catch (InsufficientFundsException e) {
         System.out.printf(
            "* Government has denied bailout.\n"
          + "  Bailout resolution failed.\n"
          + "--------------------------------------------\n"
            );
         return false;
      }
      
      if (currentNumberOfShares == 0.) { // No shares
         System.out.flush();
         System.err.println(
            "Bank.bailout: no shares have been emitted in this releaser. 1.0 units of new "
          + "shares will be created. ");
         System.err.flush();
         currentNumberOfShares = 1.0;
      }
     
     // Set new stock price
     final double
        newPricePerShare = bank.getEquity() / currentNumberOfShares;
     
     System.out.printf(
        "* Bank bailout recieved.\n"
      + "  Bank equity:              %16.10g\n"
      + "  Bank assets (total):      %16.10g\n"
      + "  Bank liabilities (total): %16.10g\n"
      + "  Government cash reserve:  %16.10g\n"
      + "  New price per share:      %16.10g\n",
        bank.getEquity(),
        bank.getTotalAssets(),
        bank.getTotalLiabilities(),
        government.getCashReserveValue(),
        newPricePerShare
        );
     
     UniqueStockExchange.Instance.setStockPrice(bank.getUniqueName(), newPricePerShare);
     
     // Generate new stocks for the government as a compensation
     UniqueStockExchange.Instance.generateFreeSharesFor(
        bank.getUniqueName(),
        government,
        currentNumberOfShares
        );
     
     System.out.printf(
        "* Shares issued to Govt.:   %16.10g.\n"
      + "--------------------------------------------\n",
        UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(bank)
        );
     
     if(bank.getEquity() <= 0.) {
        System.err.println(
           "StockReleasingBank.bailout: equity (value " + bank.getEquity() + ") after bailout "
         + "is negative. This behaviour is not expected in the current implementation."
           );
        System.err.flush();
     }
     
     Simulation.events().post(new BankBankruptcyStockInstrumentErasureEvent(bank.getUniqueName()));
     
     return true;
   }
   
}
