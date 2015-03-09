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
import java.util.List;

import com.google.inject.Inject;

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
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.utilities.ArrayUtil;
import eu.crisis_economics.utilities.Pair;

public class DefaultFirmBankruptcyHandler
   implements FirmBankruptcyHandler {
   
   StockMarket stockMarket;
   @Inject
   private ClearingHouse clearingHouse;
   
   @Inject
   public DefaultFirmBankruptcyHandler() { }
   
   /*
    * TODO: This bankruptcy resolution process should not be limited to the {@link MacroFirm}
    *       type. This limitation was inherited from an earlier version of the codebase and
    *       should be rectified at the soonest opportunity.
    */
    public void initializeBankruptcyProcedure(Firm agent) {
        if(!(agent instanceof MacroFirm)) {
           System.err.println(getClass().getSimpleName() + ": this bankruptcy handler applies "
              + "to firm agents of type " + MacroFirm.class.getSimpleName() + " only.");
           System.err.flush();
           return;
        }
        final MacroFirm
           firm = (MacroFirm) agent;
        
        System.out.printf("A firm [%s] is bankrupt.\n", firm.getUniqueName());
        
        // Exit all markets (labour and goods)
        firm.cancelGoodsOrders();
        firm.cancelLabourOrders();
        
        final double
           cashAvailable = firm.getUnallocatedCash();
        
        // Compute total loan liabilities
        double sumOfLiabilities = 0;
        for(final Loan loan : firm.getLiabilitiesLoans())
            sumOfLiabilities += loan.getValue();
        
        // Check whether the firm can now afford do pay its creditors
        if(cashAvailable >= sumOfLiabilities) {
           final List<Loan>
              loanLiabilities = firm.getLiabilitiesLoans();
           for(final Loan loan : loanLiabilities) {
              final Settlement
                 settlement = SettlementFactory.createDirectSettlement(
                    loan.getBorrower(), loan.getLender());
              try {
                 settlement.transfer(loan.getValue());
                 loan.terminateContract();
              } catch (final InsufficientFundsException neverThrows) {
                 break;    // Abandon
              }
           }
        }
        
        if(firm.getLiabilitiesLoans().isEmpty() && firm.getEquity() >= 0.)
           return;         // Resolved
        
        final List<Double>
           proportionOfLiabilities = new ArrayList<Double>();
        final List<Lender>
           lenders = new ArrayList<Lender>();
        
        // Terminate loans
        for(final Loan loan : firm.getLiabilitiesLoans()) {
            final double
               value = loan.getValue();
            final Lender
               owner = loan.getLender();
            
            proportionOfLiabilities.add(value / sumOfLiabilities);
            lenders.add(owner);
            
            loan.terminateContract();
        }
        
        // Get total assets
        double totalFirmAssetValue = firm.getTotalAssets();
        
        String stockSymbol = "Stock_" +firm.getUniqueName();
        StockInstrument firmSharesStockInstrument = null;
        
        if(this.stockMarket != null) {
            // Terminate orders in the firm stock instrument.
            firmSharesStockInstrument = 
                (StockInstrument)stockMarket.getInstrument(stockSymbol);
            if (firmSharesStockInstrument != null)
                firmSharesStockInstrument.cancelAllOrders();
            // Replace the stock instrument
            stockMarket.addNewInstrument(stockSymbol);
        } 
        else if (this.clearingHouse != null) {
            // TODO not sure anything needs to be done in clearing house
        }
        
        // Get stock accounts and terminate them.
        Collection<StockAccount> stockAccounts = 
            firm.getStockAccounts();
        for(StockAccount account : stockAccounts.toArray(new StockAccount[0]))
            account.terminateAccountWithoutCompensatingStockHolder();
        stockAccounts.clear();
        
        // firm.setNumberOfEmittedShares(firm.getNumberOfEmittedShares());
        double totalNumberOfNewShares = firm.getNumberOfEmittedShares();
        
        ArrayList<Double> numberOfSharesPerStockHolder = 
            new ArrayList<Double>();
        double totalSharesAllocated = 0.;
        for(int i = 0; i< proportionOfLiabilities.size(); ++i) {
            double numberOfShares = 
                proportionOfLiabilities.get(i) * totalNumberOfNewShares;
            totalSharesAllocated += numberOfShares;
            numberOfSharesPerStockHolder.add(numberOfShares);
        }
        
        double 
            newFirmTotalEquity = totalFirmAssetValue,
            newFirmPricePerShare = newFirmTotalEquity / totalSharesAllocated;
        
        // set new price in market
        UniqueStockExchange.Instance.setStockPrice(firm.getUniqueName(), newFirmPricePerShare);
        
        {
        final List<StockHolder> stockHolders = ArrayUtil.castList(lenders);
        UniqueStockExchange.Instance.eraseAndReplaceStockWithoutCompensation(
           firm.getUniqueName(),
           Pair.zip(stockHolders, numberOfSharesPerStockHolder),
           newFirmPricePerShare
           );
        }
        
        System.out.println("Bankruptcy resolution complete.");
   }
   
   public void setStockMarket(StockMarket stockMarket) {
      this.stockMarket = stockMarket;
   }
   
   public void setClearingHouse(ClearingHouse clearingHouse) {
      this.clearingHouse = clearingHouse;
   }

}