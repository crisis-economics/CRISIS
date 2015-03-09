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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.FinancialSystemHasCollapsedException;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of a liquidation bankruptcy handler for
  * {@link Bank} {@link Agent}{@code s}.
  * 
  * @author lovric
  * @author phillips
  * @author de-caux
  */
public final class LiquidateBankruptcyResolution
   extends SimpleAbstactAgentOperation<Boolean> implements BankBankruptyHandler {
   
   public LiquidateBankruptcyResolution() { } // Stateless
   
   /**
     * Perform a liquidation process for the specified {@link Bank}. This 
     * resolution method cannot fail, and always returns {@code true}.
     */
   @Override
   public Boolean operateOn(Agent agent) {
      if(!(agent instanceof Bank))
         return false;
      final Bank bank = (Bank) agent;
      
      if (bank.getCentralBank() != null && bank.getBadBank() != null) {
         // first resolve secured loans
         bank.unwindSecuredLoans();
         // remaining assets are sold to the BadBank
         
            int nAssets = bank.getAssets().size();
            for (int i=nAssets-1;i>=0;i--){
               Contract contract = bank.getAssets().get(i);
               if (contract instanceof StockAccount){
                  ((StockAccount) contract).transferSharesTo(bank.getBadBank());
               }
               if (contract instanceof Loan){
                  ((Loan) contract).transferToNewLender(bank.getBadBank());
               }
            }
         final double assetValue = Math.max(bank.getCashReserveValue(), 0.);
         try {
            bank.credit(bank.getCashReserveValue());
         } catch (InsufficientFundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         // collected cash is distributed pro rata to debtors
         double totalDeposits = 0;
         double totalLoans = 0;
         for (final Contract contract : bank.getLiabilities()){
            if (contract instanceof DepositAccount){
               totalDeposits += contract.getValue();
            }
            if (contract instanceof Loan){
               totalLoans += contract.getValue();
         }     
         }     
         
         // agents open up deposit at new bank, if possible
               
         List<Bank>
            banks = Simulation.getRunningModel().getRegularBanks(),
            banksInBusiness = new ArrayList<Bank>();
         for(int i=0;i<banks.size();i++) {
            if (!banks.get(i).equals(agent) && !((Bank) banks.get(i)).isBankrupt()){
               banksInBusiness.add(banks.get(i));
            }
         }
         if (banksInBusiness.size() <= 1){
            throw new FinancialSystemHasCollapsedException(
               "Bank.liquidate: all banks are bankrupt.");
         }
         int nDeposits = bank.getLiabilitiesDeposits().size();
         for(int i=nDeposits-1;i>=0;i--){
            DepositAccount deposit = bank.getLiabilitiesDeposits().get(i);
            int j = Simulation.getSimState().random.nextInt(banksInBusiness.size());
            deposit.setValue(assetValue * (deposit.getValue() / (totalDeposits + totalLoans)) );
            deposit.setDepositHolder((Bank) banksInBusiness.get(j));
         }
         
         // loans are paid off in cash as far as possible
         
         int nLoans = bank.getLiabilitiesLoans().size();
         for(int i=nLoans-1;i>=0;i--){
            Loan loan = bank.getLiabilitiesLoans().get(i);
            ((Bank) loan.getLender()).debit(assetValue * loan.getValue() / 
                (totalDeposits + totalLoans));
            loan.terminateContract();
         }
         
         // flag bank as bankrupt and remove it from markets
         if(bank instanceof ClearingBank) {
            ClearingBank clearingBank = (ClearingBank) bank;
            clearingBank.getClearingHouse().removeParticipant(clearingBank);
         }
         
         bank.setBankrupt();
         for (final Market market : bank.getMarketsOfType(CommercialLoanMarket.class)) {
            bank.removeMarket(market);
         }
         for (final Market market : bank.getMarketsOfType(DepositMarket.class)) {
            bank.removeMarket(market);
         }
         for (final Market market : bank.getMarketsOfType(StockMarket.class)) {
            bank.removeMarket(market);
         }
         for (final Market market : bank.getMarketsOfType(InterbankLoanMarket.class)) {
            bank.removeMarket(market);
         }
      }
      
      return true;
   }
}
