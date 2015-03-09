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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class ClearingLoanMarketsTest {
   private SimState state;
   private double timeToSample;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing ClearingLoanMarketsTest..");
      state = new EmptySimulation(0L);
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.AFTER_ALL, 2).getUnitIntervalTime();
      state.start();
   }
   
   private void resetSimulation() {
      state.finish();
      state = new EmptySimulation(0L);
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
   /**
     * This unit test generates random loan market clearing sessions,
     * processes all desired trades, and inspects the resulting state
     * of the economy. 
     * 
     * The test sequence is as follows:
     *    (a) a unique deposit-holding bank is created;
     *    (b) a random number of loan-issuing banks are created;
     *    (c) cash injection behaviour is disabled for all agents
     *        created in step (b); 
     *    (d) random initial cash endowments are distributed to 
     *        agents in (b);
     *    (e) each loan issuer is given a random target loan 
     *        portfolio size;
     *    (f) firms are created, each with random loan demands;
     *    (g) a clearing loan market is established, and processed;
     *    (h) it is asserted that: i. the liability position of
     *        the firms has been modified, and ii. that no cash has
     *        been created in the system.
     * 
     * Steps (a-g) are then repeated 200 times, afresh, for independent
     * randomly generated configurations generated configurations.
     */
   public void testSeveralRandomClearingMarketSessions() {
      int numTests = 200;
      for(int i = 0; i< numTests; ++i) {
         testRandomClearingMarketSession(i);
         resetSimulation();
      }
   }
   
   private void testRandomClearingMarketSession(int seed) {
      advanceToTimeZero();
      
      final Random
         dice = new Random(seed);
      final int
         numberOfLoanIssuers = dice.nextInt(50) + 2,
         numberOfLoanConsumers = dice.nextInt(50) + 2;
      final double
         priceScaleForTransactions = 1.e10;
      final double[]
         desiredLoanIssuerInvestments = new double[numberOfLoanIssuers],
         desiredLoanConsumerInvestments = new double[numberOfLoanConsumers],
         initialLoanIssuerCash = new double[numberOfLoanIssuers],
         initialLoanConsumerCash = new double[numberOfLoanConsumers];
      
      for(int i = 0; i< numberOfLoanIssuers; ++i) {
         desiredLoanIssuerInvestments[i] = dice.nextDouble() * priceScaleForTransactions + .1;
         initialLoanIssuerCash[i] = dice.nextDouble() * priceScaleForTransactions + .1;
      }
      
      for(int i = 0; i< numberOfLoanConsumers; ++i) {
         desiredLoanConsumerInvestments[i] = dice.nextDouble() * priceScaleForTransactions + .1;
         initialLoanConsumerCash[i] = dice.nextDouble() * priceScaleForTransactions + .1;
      }
      
      class CustomBank extends Bank implements ClearingMarketParticipant {       // Loan Issuers
         private final double
            targetLoanInvestment;
         public CustomBank(
            double initialCash, double targetLoanInvestment) {
            super(initialCash);
            this.targetLoanInvestment = targetLoanInvestment;
         }
         @Override
         public MarketResponseFunction getMarketResponseFunction(
            final ClearingMarketInformation marketInformation
            ) {
            return new PartitionedResponseFunction(
               new ExpIOCPartitionFunction(),
               new PolynomialSupplyInnerResponse(0.5, 1.0, targetLoanInvestment)
               );
         }
         
         @Override
         public void cashFlowInjection(double amount) { }                       // No CFIs.
         
         @Override
         public double credit(double amount) throws InsufficientFundsException {
            if(amount <= 0.) return 0.;
            if(amount > getCashReserveValue())
               throw new InsufficientFundsException();
            decreaseCashReserves(amount);
            return amount;
         }
      }
      
      final Bank depositHolder = new CustomBank(100., 0.);
      
      class CustomFirm extends Firm implements ClearingMarketParticipant {
         private final double
            targetLoanInvestment;
         public CustomFirm(
            double initialCash,
            double targetLoanInvestment,
            DepositHolder depositHolder
            ) {
            super(depositHolder, new InstanceIDAgentNameFactory());
            this.targetLoanInvestment = targetLoanInvestment;
            this.debit(initialCash);
         }
         @Override
         public MarketResponseFunction getMarketResponseFunction(
            final ClearingMarketInformation marketName
            ) {
            return new PartitionedResponseFunction(                      // Commercial Loans
               new InverseExpIOCPartitionFunction(),
               new PolynomialDemandInnerResponse(1.0, 1.0, targetLoanInvestment)
               );
         }
      }
      
      ClearingHouse clearingHouse = new ClearingHouse();
      clearingHouse.addMarket(
         new ClearingSimpleCommercialLoanMarket("Commercial Loans", clearingHouse, true));
      
      final CustomBank[] loanIssuers = new CustomBank[numberOfLoanIssuers];
      for(int i = 0; i< numberOfLoanIssuers; ++i) {
         loanIssuers[i] = new CustomBank(initialLoanIssuerCash[i], desiredLoanIssuerInvestments[i]);
         clearingHouse.addLender(loanIssuers[i]);
      }
      
      final CustomFirm[] loanConsumers = new CustomFirm[numberOfLoanConsumers];
      Map<String, Borrower> borrowers = new HashMap<String, Borrower>();
      for(int i = 0; i< numberOfLoanConsumers; ++i) {
         loanConsumers[i] = new CustomFirm(
            initialLoanConsumerCash[i], desiredLoanConsumerInvestments[i], depositHolder);
         borrowers.put(loanConsumers[i].getUniqueName(), loanConsumers[i]);
         clearingHouse.addBorrower(loanConsumers[i]);
      }
      
      for(Firm firm : loanConsumers)
         Assert.assertTrue(firm.getTotalLiabilities() == 0.);
      
      
      // Record the total initial cash in this system:
      
      double initialCashInSystem = 0.;
      for(CustomBank bank : loanIssuers)
         initialCashInSystem += bank.getCashReserveValue();
      initialCashInSystem += depositHolder.getCashReserveValue();
      
      advanceUntilSampleTime();
      advanceUntilSampleTime(); // first clearing market session has been processed.
      
      // Test no cash has been created during the clearing process:
      
      double finalCashInSystem = 0.;
      for(CustomBank bank : loanIssuers)
         finalCashInSystem += bank.getCashReserveValue();
      finalCashInSystem += depositHolder.getCashReserveValue();
      
      // Assert new loans have been created:
      
      double totalLoanLiabilities = 0.;
      for(Firm firm : loanConsumers)
         totalLoanLiabilities += firm.getTotalLiabilities();
      Assert.assertTrue(totalLoanLiabilities > 0.);
      
      Assert.assertEquals(
         initialCashInSystem, finalCashInSystem,
         Math.max(initialCashInSystem, finalCashInSystem) * 1.e-10
         );
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("ClearingLoanMarketsTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         ClearingLoanMarketsTest test = new ClearingLoanMarketsTest();
         test.setUp();
         test.testSeveralRandomClearingMarketSessions();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
