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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.aitia.crisis.aspectj.Agent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.utilities.Pair;

/**
  * An implementation of the {@link FirmBankruptcyHandler} interface.<br><br>
  * 
  * This implementation assumes that the bankrupt {@link Firm} is a {@link MacroFirm}
  * {@link Agent}. This limitation was inherited from an earlier edition of the codebase 
  * and should be relaxed as soon as possible.<br><br>
  * 
  * This algorithm processes a {@link Firm} bankruptcy event in three ordered steps as follows:
  * 
  * <ul>
  *   <li> A {@link CashCompensationAlgorithm} object is asked to compute the maximum 
  *        amount of {@link Firm} liquidity, <code>C</code>, that can be used to pay
  *        existing creditor debts. In this implementation it is expected that creditor
  *        debts consist of unpaid commercial loan instalments only. Up to <code>C</code>
  *        units of {@link Firm} liquidity are paid to creditors in proportion according
  *        to the outstanding debt to each creditor.
  *   <li> Should <code>C</code> be insufficient to repay the outstanding creditor debt,
  *        this bankruptcy resolution process resorts to a stock redistribution. A
  *        {@link StockRedistributionAlgorithm} algorithm is triggered. If this algorithm does
  *        not raise an instance of {@link NoStockRedistributionSolutionException} then
  *        the bankruptcy resolution is taken to be complete and no further action is
  *        taken.
  *   <li> If stock redistribution is insufficient to compensate creditors for the outstanding
  *        {@link Firm} debt, then this bankruptcy resolution algorithm liquidates the 
  *        firm by erasing its existing stock instrument and cancelling all outstanding
  *        commercial {@link Loan}{@code s}. {@link Firm} shares are created anew with a
  *        stock price equal to the value of the total {@link Firm} assets, and all shares
  *        are distributed to the {@link Firm}{@code 's} creditors in proportion to the debt
  *        owed to each creditor.
  * </ul>
  * 
  * @author phillips
  */
public final class AssetThresholdFirmBankruptcyHandler implements FirmBankruptcyHandler {
   
   private final CashCompensationAlgorithm
      cashCompensationAlgorithm;
   private final StockRedistributionAlgorithm
      stockRedistributionAlgorithm;
   
   /**
     * Create an {@link AssetThresholdFirmBankruptcyHandler} object with custom
     * parameters. <br><br>
     * 
     * See also {@link AssetThresholdFirmBankruptcyHandler}.
     * 
     * @param cashCompensationAlgorithm (<code>C</code>) <br>
     *        A {@link CashCompensationAlgorithm} object to which this bankruptcy
     *        resolution process delegates. <code>C</code> is called during the 
     *        first step of this bankruptcy resolution process. <code>C</code> will
     *        compute and return, by any means, a cash sum that the {@link Firm} {@link Agent}
     *        is willing to pay to creditors to settle at least part of the {@link Firm}{@code 's}
     *        outstanding debt. If the cash sum specified by <code>C</code> is inadequate
     *        to settle the {@link Firm}{@code 's} outstanding creditor debt, then this
     *        algorithm resorts to a stock redistribution to be executed by the next argument.
     *        argument.
     * @param stockRedistributionAlgorithm (<code>D</code>) <br>
     *        A {@link StockRedistributionAlgorithm} object to which this bankruptcy
     *        resolution process delegates. <code>D</code> is called during the 
     *        second step of this bankruptcy resolution process. <code>D</code> will
     *        attempt to redistribute existing stocks and shares emitted by the {@link Firm}
     *        {@link Agent} in an effort to satisfy creditor debt (potentially at the 
     *        expense of existing non-creditor stockholders). Should this process still
     *        not satisfy the creditor debt, then this algorithm resorts to liquidating
     *        and regenerating the {@link Firm}.
     */
   @Inject
   public AssetThresholdFirmBankruptcyHandler(
   @Named("CASH_COMPENSATION_ALGORITHM")
      final CashCompensationAlgorithm cashCompensationAlgorithm,
   @Named("STOCK_REDISTRIBUTION_ALGORITHM")
      final StockRedistributionAlgorithm stockRedistributionAlgorithm
      ) {
      this.cashCompensationAlgorithm =
         Preconditions.checkNotNull(cashCompensationAlgorithm);
      this.stockRedistributionAlgorithm =
         Preconditions.checkNotNull(stockRedistributionAlgorithm);
   }
   
   /*
    * TODO: This bankruptcy resolution process should not be limited to the {@link MacroFirm}
    *       type. This limitation was inherited from an earlier version of the codebase and
    *       should be rectified at the soonest opportunity.
    */
    public void initializeBankruptcyProcedure(Firm agent) {
       if(!(agent instanceof MacroFirm)) {
          System.err.println(
             getClass().getSimpleName() + ": this bankruptcy handler applies "
          + "to firm agents of type " + MacroFirm.class.getSimpleName() + " only. "
          + "No action was taken.\n");
          System.err.flush();
          return;
       }
       final MacroFirm
          firm = (MacroFirm) agent;
       
       System.out.printf("%s: a firm [%s] is bankrupt.\n",
          firm.getClass().getSimpleName(), firm.getUniqueName());
       
       firm.cancelGoodsOrders();                                     // Exit all markets (labour
       firm.cancelLabourOrders();                                    //  and goods)
       
       /**
         * Compute the total outstanding loan liability including interest.
         * 
         * This bankruptcy resolution procedure will, in any eventuality, terminate
         * all payable outstanding loan liabilities. These loan liabilities can be 
         * terminated without compensation immediately:
         */
       final Map<String, CreditorDebts>
          creditorDebts = new HashMap<String, CreditorDebts>();
       double
          outstandingLoanLiability = 0.0;
       for(final Loan loan : new ArrayList<Loan>(firm.getLiabilitiesLoans())) {
          final Lender
             creditor = loan.getLender();
          final String
             creditorName = creditor.getUniqueName();
          if(!creditorDebts.containsKey(creditorName))
             creditorDebts.put(creditorName, new CreditorDebts(creditor));
          creditorDebts.get(creditorName).incrementDebt(loan.getFaceValue());
          outstandingLoanLiability += loan.getFaceValue();
          loan.terminateContract();
       }
       if(outstandingLoanLiability == 0.) {
          System.out.printf(
             "%s: bankruptcy resolution complete. The result of the bankruptcy resolution process "
           + "was a direct payment from existing firm liquidity.\n",
             firm.getClass().getSimpleName()
             );
          return;                                                    // Nothing to do
       }
       
       /**
         * Check whether the firm can now afford do pay its creditors.
         */
       {
          final double
             fundsAvailable = cashCompensationAlgorithm.computePayment(firm),
             creditorRationing = Math.min(1.0, fundsAvailable / outstandingLoanLiability);
          for(final CreditorDebts debt : creditorDebts.values()) {
             final Settlement settlement = 
                SettlementFactory.createDirectSettlement(firm, debt.getLender());
             try {
                final double
                   amountToPay = creditorRationing * debt.getDebt();
                settlement.transfer(amountToPay);
                debt.incrementDebt(-amountToPay);
                continue;
             } catch (final InsufficientFundsException neverThrows) {
                final String err =
                      getClass().getSimpleName() + ": an insufficient funds exception was raised "
                   + "when paying a creditor from outstanding firm cash reserves during insolvency."
                   + "resolution. This behaviour is not expected in the current implementation "
                   + "and is indicative of an error. Details follow: " + neverThrows + ".\n";
                System.err.println(err);
                System.err.flush();
                throw new IllegalStateException(err);
             }
          }
       }
       
       outstandingLoanLiability = 0.0;
       for(final CreditorDebts debt : creditorDebts.values())
          outstandingLoanLiability += debt.getDebt();
       
       /**
         * Check whether the firm is now debtless
         */
       if(outstandingLoanLiability == 0.0)
          return;                                                    // Success
       
       try {                                                         // Try to redistribute stock
          stockRedistributionAlgorithm.processStockRedistribution(firm, creditorDebts);
          System.out.printf(
             "%s: bankruptcy resolution complete. The result of the bankruptcy resolution process "
           + "was a stock redistribution not involving firm liquidation.\n",
             firm.getClass().getSimpleName()
             );
          return;                                                    // Success
       }
       catch(final NoStockRedistributionSolutionException noSolution) { }
       
       /**
         * The debt could not be resolved in cash, and the debt could also not be
         * resolved via a stock redistribution. The firm must now be liquidated and 
         * regenerated as a new firm with a new stock price.
         */
       
       double
          numberOfNewSharesToCreate = firm.getNumberOfEmittedShares();
       
       {
          final Collection<StockAccount>
             stockAccounts = firm.getStockAccounts();
          for(StockAccount account : stockAccounts.toArray(new StockAccount[0]))
             account.terminateAccountWithoutCompensatingStockHolder();
          stockAccounts.clear();
       }
       
       {  // Compute the number of new shares to issue per creditor
          final Map<String, Double>
             numberOfSharesPerStockHolder = new HashMap<String, Double>();
          double
             counter = 0.0;
          for(final Entry<String, CreditorDebts> record : creditorDebts.entrySet()) {
             final double
                debt = record.getValue().getDebt();
             counter += debt;
             numberOfSharesPerStockHolder.put(record.getKey(), debt);
          }
          for(final Entry<String, CreditorDebts> record : creditorDebts.entrySet())
             numberOfSharesPerStockHolder.put(record.getKey(),
                numberOfSharesPerStockHolder.get(record.getKey()) *
                   numberOfNewSharesToCreate / counter);
          
          assert (firm.getTotalLiabilities() == 0.0);
          
          double 
             newFirmTotalEquity = firm.getTotalAssets(),
             newFirmPricePerShare = newFirmTotalEquity / numberOfNewSharesToCreate;
          
          // Set new stock price
          UniqueStockExchange.Instance.setStockPrice(firm.getUniqueName(), newFirmPricePerShare);
          
          final List<Pair<StockHolder, Double>>
             newStocksToCreate = new ArrayList<Pair<StockHolder,Double>>();
          for(final Entry<String, Double> record : numberOfSharesPerStockHolder.entrySet()) {
             final Lender
                lender = creditorDebts.get(record.getKey()).getLender();
             newStocksToCreate.add(
                Pair.create((StockHolder) lender, record.getValue()));
          }
          UniqueStockExchange.Instance.eraseAndReplaceStockWithoutCompensation(
             firm.getUniqueName(),
             newStocksToCreate,
             newFirmPricePerShare
             );
       }
       
       System.out.printf(
          "%s: bankruptcy resolution complete. The result of the bankruptcy resolution process "
        + "was firm liquidation.\n", firm.getClass().getSimpleName()
          );
   }
}