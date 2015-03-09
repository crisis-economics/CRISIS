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
package eu.crisis_economics.abm.government;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.TargetValueStockMarketResponseFunction;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.intermediary.Intermediary;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediary;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

public class IntermediaryTest {
   private Simulation state;
   private double timeToSample;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "..");
      state = new EmptySimulation(0L);
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.AFTER_ALL, 2).getUnitIntervalTime();
      state.start();
   }
   
   private void advanceUntilSampleTime() {
      double
         startTime = state.schedule.getTime(),
         startFloorTime = Math.floor(startTime),
         endTime = startFloorTime + timeToSample;
      if(endTime <= startTime)
         endTime += 1.;
      while(state.schedule.getTime() <= endTime)
         state.schedule.step(state);
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
   }
   
   @Test
   public void testIntermediaryPassesCashToBeneficiary() {
      ClearingHouse clearingHouse = new ClearingHouse();
      Bank bank = new Bank(100.);
      Intermediary intermediary = new SingleBeneficiaryIntermediary(bank, clearingHouse);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      intermediary.debit(100.);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 200., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 200., 1.e-10);
      intermediary.debit(0.);       // No action
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 200., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 200., 1.e-10);
      intermediary.debit(-100.);    // No action
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 200., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 200., 1.e-10);
   }
   
   @Test
   public void testIntermediaryDoesNotSupportCashWithdrawal() {
      ClearingHouse clearingHouse = new ClearingHouse();
      Bank bank = new Bank(100.);
      Intermediary intermediary = new SingleBeneficiaryIntermediary(bank, clearingHouse);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      boolean raisedIFE = false;
      try {
         intermediary.credit(100.);
      }
      catch(final InsufficientFundsException e) {
         raisedIFE = true;
      }
      Assert.assertTrue(raisedIFE);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 100., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 100., 1.e-10);
      try {
         intermediary.credit(0.);   // No action
      }
      catch(final InsufficientFundsException e) { }
      Assert.assertTrue(raisedIFE);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 100., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 100., 1.e-10);
      try {
         intermediary.credit(-100.);   // No action
      }
      catch(final InsufficientFundsException e) { }
      Assert.assertTrue(raisedIFE);
      Assert.assertTrue(intermediary.getEquity() == 0.);
      Assert.assertTrue(intermediary.getTotalAssets() == 0.);
      Assert.assertEquals(bank.getCashReserveValue(), 100., 1.e-10);
      Assert.assertEquals(bank.getEquity(), 100., 1.e-10);
   }
   
   @Test
   public void testIntermediarySellsSharesAndPassesIncomeToBeneficiary() {
      ClearingHouse clearingHouse = new ClearingHouse();
      
      advanceToTimeZero();
      
      final StockReleasingBank bank = new StockReleasingBank(100.);
      Bank beneficiary = new Bank(0.);
      
      clearingHouse.addMarket(new ClearingStockMarket(bank, clearingHouse));
      
      Intermediary intermediary = new SingleBeneficiaryIntermediary(beneficiary, clearingHouse);
      clearingHouse.addStockMarketParticipant(intermediary);
      
      UniqueStockExchange.Instance.setStockPrice(bank.getUniqueName(), 2.0); // £2.0 per bank share.
      StockAccount.create(intermediary, bank, 1.0);                          // 1.0 shares issued.
      
      Assert.assertEquals(beneficiary.getTotalAssets(), 0., 0.);
      Assert.assertEquals(beneficiary.getEquity(), 0., 0.);
      Assert.assertEquals(intermediary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(intermediary.getEquity(), 2.0, 0.);
      Assert.assertEquals(bank.getTotalAssets(), 100., 0.);
      Assert.assertEquals(bank.getEquity(), 100., 0.);
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-10);
      
      bank.setDividendPerShare(1.0);  // £1.0 per share dividend to pay.
      
      Assert.assertEquals(beneficiary.getTotalAssets(), 0., 0.);
      Assert.assertEquals(beneficiary.getEquity(), 0., 0.);
      Assert.assertEquals(intermediary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(intermediary.getEquity(), 2.0, 0.);
      Assert.assertEquals(bank.getTotalAssets(), 100., 0.);
      Assert.assertEquals(bank.getEquity(), 100., 0.);
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-10);
      
      advanceUntilSampleTime();
      
      /*
       * At this point no market clearing is scheduled, so the 
       * intermediary has no opportunity to sell its shares. For
       * a number of steps, dividends payments are expected to
       * be made to the intermediary, who is, in turn, expected
       * to pass these dividends on immediately to the beneficiary.
       */
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(beneficiary.getCashReserveValue(), 1.0);
      Assert.assertEquals(beneficiary.getTotalAssets(), 1.0, 0.);
      Assert.assertEquals(beneficiary.getEquity(), 1.0, 0.);
      Assert.assertEquals(intermediary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(intermediary.getEquity(), 2.0, 0.);
      Assert.assertEquals(bank.getTotalAssets(), 99., 0.);
      Assert.assertEquals(bank.getEquity(), 99., 0.);
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-10);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(beneficiary.getCashReserveValue(), 2.0);
      Assert.assertEquals(beneficiary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(beneficiary.getEquity(), 2.0, 0.);
      Assert.assertEquals(intermediary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(intermediary.getEquity(), 2.0, 0.);
      Assert.assertEquals(bank.getTotalAssets(), 98., 0.);
      Assert.assertEquals(bank.getEquity(), 98., 0.);
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-10);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(beneficiary.getCashReserveValue(), 3.0);
      Assert.assertEquals(beneficiary.getTotalAssets(), 3.0, 0.);
      Assert.assertEquals(beneficiary.getEquity(), 3.0, 0.);
      Assert.assertEquals(intermediary.getTotalAssets(), 2.0, 0.);
      Assert.assertEquals(intermediary.getEquity(), 2.0, 0.);
      Assert.assertEquals(bank.getTotalAssets(), 97., 0.);
      Assert.assertEquals(bank.getEquity(), 97., 0.);
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-10);
      
      /*
       * Create a clearing house and add the intermediary to the 
       * registered stockholders.
       */
      
      // Time T = 4.0. The first clearing session is scheduled for cycle 5.0.
      class CustomBank extends StockTradingBank implements ClearingMarketParticipant {
         public CustomBank(double initialCash) {
            super(initialCash);
         }
         
         @Override
         public MarketResponseFunction getMarketResponseFunction(
            final ClearingMarketInformation marketName
            ) {
            return new TargetValueStockMarketResponseFunction(               // Stock Market
               super.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0);   // £1 investment
         }
      }
      
      CustomBank competitor = new CustomBank(10.0);
      clearingHouse.addStockMarketParticipant(competitor);
      
      advanceUntilSampleTime();     // T = 5.0
      
      /*
       * Run market clearing several times, as the Intermediary will
       * sell its shares only gradually.
       */
      
      for(int i = 0; i< 100; ++i)
         advanceUntilSampleTime();
      
      Assert.assertEquals(intermediary.getNumberOfSharesOwnedIn(bank.getUniqueName()), 0.0, 1.e-5);
      Assert.assertEquals(competitor.getNumberOfSharesOwnedIn(bank.getUniqueName()), 1.0, 1.e-5);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("IntermediaryTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         IntermediaryTest test = new IntermediaryTest();
         test.setUp();
         test.testIntermediaryPassesCashToBeneficiary();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      try {
         IntermediaryTest test = new IntermediaryTest();
         test.setUp();
         test.testIntermediaryDoesNotSupportCashWithdrawal();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      try {
         IntermediaryTest test = new IntermediaryTest();
         test.setUp();
         test.testIntermediarySellsSharesAndPassesIncomeToBeneficiary();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
