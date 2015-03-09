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
package eu.crisis_economics.abm.ratings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;

import sim.engine.SimState;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.utilities.Pair;

/**
  * Tests for eu.crisis_economics.abm.ratings.
  * @author phillips
  */
public final class RatingsAgencyTest {
   
   /**
     * This test consists of:
     *   (a) one trivial household with random exogenous income/
     *       expenses,
     *   (b) one trivial bank, holding all household deposits.
     * 
     * For 10 simulation cycles the household (a) receives a random
     * income/incurs a random expense. The equity of the household
     * and the bank are recorded by the test. The rating agency is
     * instructed to record the same equity measurement.
     * 
     * When 10 cycles have elapsed, the rating agency is asked for
     * a copy of these equity measurements as a timeseries. It is
     * expected that this data should agree with the measurements 
     * made by the test.
     */
   public void testPredictedEquityTimeSeries() {
      SimState simState = new EmptySimulation(0L);
      simState.start();
      Bank bank = new Bank(101.);
      Household household = new DepositingHousehold(bank);
      household.debit(100.);
      RatingAgency.Instance.addTrackingMeasurement("Equity", new AgentEquityMeasurement());
      RatingAgency.Instance.trackAgent((Agent)household);
      RatingAgency.Instance.trackAgent((Agent)bank);
      Random dice = new Random();
      List<Double>
         predictedHouseholdEquityTimeSeries = new ArrayList<Double>(),
         preductedBankEquityTimeSeries = new ArrayList<Double>();
      int lastCycleIndex = 0;
      while(simState.schedule.getTime() < 10.) {
         while(simState.schedule.getTime() < lastCycleIndex + 1)
            simState.schedule.step(simState);
         predictedHouseholdEquityTimeSeries.add(household.getEquity());
         preductedBankEquityTimeSeries.add(bank.getEquity());
         final double
            amountToTransfer = dice.nextDouble() - .5;
         Settlement settlement = SettlementFactory.createDirectSettlement(household, bank);
         try {
            settlement.bidirectionalTransfer(amountToTransfer);
         } catch (final InsufficientFundsException e) {
            Assert.fail();
         }
         ++lastCycleIndex;
      }
      List<Pair<Double, Double>>
         actualHouseholdEquityTimeSeries = 
            RatingAgency.Instance.getTimeSeries("Equity", (Agent)household).asList(),
         actualBankEquityTimeSeries =
            RatingAgency.Instance.getTimeSeries("Equity", (Agent)bank).asList();
      Assert.assertEquals(
         predictedHouseholdEquityTimeSeries.size(), actualHouseholdEquityTimeSeries.size());
      Assert.assertEquals(
         preductedBankEquityTimeSeries.size(), actualBankEquityTimeSeries.size());
      for(int i = 0; i< predictedHouseholdEquityTimeSeries.size(); ++i) {
         double
            expected = preductedBankEquityTimeSeries.get(i),
            gained = actualBankEquityTimeSeries.get(i).getSecond();
         Assert.assertEquals(expected, gained, 1.e-10);
         expected = predictedHouseholdEquityTimeSeries.get(i);
         gained = actualHouseholdEquityTimeSeries.get(i).getSecond();
         Assert.assertEquals(expected, gained, 1.e-10);
      }
   }
   
   static public void main(final String[] args) {
      RatingsAgencyTest test = new RatingsAgencyTest();
      test.testPredictedEquityTimeSeries();
   }
}
