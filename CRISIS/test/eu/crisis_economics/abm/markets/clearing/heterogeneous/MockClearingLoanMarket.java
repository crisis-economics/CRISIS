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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.testng.Assert;

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.fund.MutualFund;
import eu.crisis_economics.abm.fund.PortfolioFund;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.AbstractClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingInstrument;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;

/*******************************
 * Sets up a FixedValueContract to simulate share-holding assets
 * @author daniel
 *
 */
public class MockClearingLoanMarket extends AbstractClearingMarket implements ClearingLoanMarket {
   public MockClearingLoanMarket(
      final MutualFund fund,
      final Bank bank,
      final String marketName,
      final String instrumentName,
      final double interestRateOnLoans,
      final ClearingHouse clearingHouse
      ) {
      super(marketName, clearingHouse);
      this.myFund = fund;
      this.myBank = bank;
      this.marketName = marketName;
      this.instrumentName = instrumentName;
      this.interestRateOnLoans = interestRateOnLoans;
      this.clearingHouse = clearingHouse;
      this.instrument = new ClearingInstrument(getMarketName(), instrumentName);
      this.rand = new Random(7569);
   }
   
   public void expireLoans() throws InsufficientFundsException {
      // randomly expire loans
      List<Loan> loans = myFund.getAssetLoans();
      
      for(Loan loanContract : loans) {
         double interestRate;
         if (rand.nextDouble() > 0.5) {
            interestRate = loanContract.getInterestRate();
            if(interestRate != interestRateOnLoans)
               continue;
            myFund.debit(loanContract.getValue());
            myBank.credit(loanContract.getValue());
            loanContract.terminateContract();
         }
      }
   }
   
   @Override
   public void process() {
      // ---- process loans
      final double newCashInvestmentValue =
         myFund.getDesiredLoanInvestmentIn(marketName);
      for (final Loan loanContract : myFund.getAssetLoans()) {
         if(loanContract.getInterestRate() == interestRateOnLoans) {
            try {
               loanContract.extendLoan(newCashInvestmentValue - loanContract.getValue());
            } catch (LenderInsufficientFundsException e) {
               Assert.fail();
            }
         } else {
            Assert.assertEquals(
               loanContract.getValue(),
               newCashInvestmentValue,
               1e-6
               );
         }
      }
   }
   
   PortfolioFund myFund;
   Bank myBank;
   String
      marketName,
      instrumentName;
   final double interestRateOnLoans;
   Random rand;
   ClearingHouse clearingHouse;
   ClearingInstrument instrument;
   
   @Override
   public Set<ClearingInstrument> getInstruments() {
      final Set<ClearingInstrument>
         result = new HashSet<ClearingInstrument>();
      result.add(instrument);
      return result;
   }
   
   @Override
   public ClearingMarketInformation getSummaryInformation() {
      final ClearingMarketInformation
         result = new ClearingMarketInformation(this);
      result.add(instrument, interestRateOnLoans, 1.0);
      return result;
   }
}
