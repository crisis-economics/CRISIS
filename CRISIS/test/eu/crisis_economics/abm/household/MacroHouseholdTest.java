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
package eu.crisis_economics.abm.household;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.HomogeneousRationingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.NoSmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.HomogeneousPortfolioWeighting;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.fund.CustomFundInvestmentDecisionRule;
import eu.crisis_economics.abm.fund.MutualFund;
import eu.crisis_economics.abm.fund.SimpleThresholdFundInvestmentDecisionRule;
import eu.crisis_economics.abm.household.plugins.ConsumptionPropensityBudgetAlgorithm;
import eu.crisis_economics.abm.household.plugins.FixedWageExpectationAlgorithm;
import eu.crisis_economics.abm.household.plugins.HomogeneousConsumptionAlgorithm;
import eu.crisis_economics.abm.household.plugins.TrivialHouseholdDecisionRule;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.model.parameters.ParameterUtils;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

public class MacroHouseholdTest {
   private Simulation state;
   private double timeToSample;
   private ClearingHouse clearingHouse;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing MacroHouseholdTest..");
      state = new EmptySimulation(0L);
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.AFTER_ALL, 2).getUnitIntervalTime();
      state.start();
      clearingHouse = new ClearingHouse();
   }
   
   private void advanceUntilJustAfterTime(final double target) {
      double
         startTime = state.schedule.getTime(),
         startFloorTime = Math.floor(startTime),
         endTime = startFloorTime + target;
      if(endTime <= startTime)
         endTime += 1.;
      while(state.schedule.getTime() <= endTime)
         state.schedule.step(state);
   }
   
   private void advanceUntilSampleTime() {
      advanceUntilJustAfterTime(timeToSample);
   }
   
   private void advanceUnitlJustAfterBeforeAll() {
      advanceUntilJustAfterTime(NamedEventOrderings.BEFORE_ALL.getUnitIntervalTime());
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
   }
   
   /**
     * Test whether labour wage payments are:
     *    (a) paid at the expected level;
     *    (b) paid with the expected frequency.
     */
   @Test
   public void testWagesArePaidOnceOnly() {
      advanceToTimeZero();
      
      final StockTradingBank bank = new StockTradingBank(100.);
      final MutualFund fund = new MutualFund(
         bank,
         new HomogeneousPortfolioWeighting(),
         new FundamentalistStockReturnExpectationFunction(),
         clearingHouse,
         new NoSmoothingAlgorithm()
         );
      fund.debit(10.);
      final SimpleGoodsMarket market =
         new SimpleGoodsMarket(
            new DurableGoodsRepository.SimpleGoodsClassifier(),
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())
            );
      market.addInstrument("0");                            // Initial goods price: £1 per unit
      
      final MacroHousehold household = new MacroHousehold(
         bank,
         100.,                                              // £100 starting cash
         ParameterUtils.asMultiSampleParameter(1.0),        // 1.0 units of labour to supply
         market,
         new SimpleLabourMarket(
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())),
         new TrivialHouseholdDecisionRule(),
         new TrivialHouseholdDecisionRule(),
                                                            // Withdraw from funds when over £150,
                                                            // Invest 10% of unallocated cash,
                                                            // when available.
         new HomogeneousConsumptionAlgorithm()
         );
      household.setFund(fund);
      household.setFundInvestmentTargetDecisionRule(
         new CustomFundInvestmentDecisionRule(ParameterUtils.asMultiSampleParameter(0.)));
      
      Employer employer = new ExogenousEmployer();
      
      advanceUntilSampleTime();                             // Time T = 0 becomes time T = 1.
      
      advanceUnitlJustAfterBeforeAll();
      
      try {
         Labour.create(employer, household, 0.5, 1, 10);    // 0.5 units of labour at £10 per unit.
      } catch (final DoubleEmploymentException e) {
         Assert.fail();
      }
      
      Assert.assertEquals(household.getEquity(), 105., 1.e-12);
      Assert.assertEquals(household.getTotalAssets(), 105., 1.e-12);
      Assert.assertEquals(household.getAllocatedCash(), 0., 0.);
      Assert.assertEquals(household.getCash(), 105., 0.);
      Assert.assertEquals(household.getTotalLiabilities(), 0., 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(household.getEquity(), 105., 1.e-12);
      Assert.assertEquals(household.getTotalAssets(), 105., 1.e-12);
      Assert.assertEquals(household.getAllocatedCash(), 0., 0.);
      Assert.assertEquals(household.getCash(), 105., 0.);
      Assert.assertEquals(household.getTotalLiabilities(), 0., 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(household.getCash(), 105., 0.);
      Assert.assertEquals(household.getTotalLiabilities(), 0., 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(household.getCash(), 105., 0.);
      Assert.assertEquals(household.getTotalLiabilities(), 0., 0.);
   }
   
   /**
     * This relatively complex test involves the creation of:<br>
     * 
     * <ul>
     *    <li> one Macro Household agent (H) with an initial cash and 
     *         labour endowment;
     *    <li> one fund, disconnected from all clearing markets;
     *    <li> one bank, collecting all deposits;
     *    <li> one employer, with exogenous cash reserves;
     *    <li> one goods seller, with exogenous cash reserves;
     * </ul>
     * 
     * The test configures H to use a 90% consumption propensity
     * budget allocation algorithm, fixed wage ask price (per
     * unit labour) and a simple (threshold-based) fund investment
     * decision rule.<br><br>
     * 
     * For several simulation cycles, the goods seller is endowed 
     * with goods and services, and the exogenous employer requests
     * labour contracts from H. As a result of these controlled market
     * activities, H receives income and spends cash on goods, services,
     * and fund investments in a controlled way.<br><br>
     * 
     * <b>Predicting the results of the test.</b><br><br>
     * 
     * The results of this test are asserted in the last 5 lines of
     * this method. These assertions are as follows:<br><br>
     * 
     * <code>
     *  Assert.assertEquals(household.getEquity(),            55.0, 1.e-12);<br>
     *  Assert.assertEquals(household.getTotalAssets(),       55.0, 1.e-12);<br>
     *  Assert.assertEquals(household.getCash(),              10.0355, 1.e-12);<br>
     *  Assert.assertEquals(fund.getBalance(household),       44.9645, 1.e-12);
     * </code><br><br>
     * 
     * These values can be computed independently using the {@code python}
     * scipt {@code ./compute-macrohousehold-test-expected-results.py}
     * located in the same directory as this unit test. Usage instructions
     * for this {@code python} codefile can be found commented in its header.
     * The {@code python} script writes the following data to the 
     * console:<br><br>
     * 
     * <code>
     * results:<br>
     * household equity: 55.0<br>
     * household total assets: 55.0<br>
     * household cash: 10.0355<br>
     * fund balance: 44.9645
     * </code>
     */
   @Test
   public void testExtendedMacroHouseholdGoodsAndLabourAndFinancialBahaviour() {
      advanceToTimeZero();
      
      final StockTradingBank bank = new StockTradingBank(100.);
      final MutualFund fund = new MutualFund(
         bank,
         new HomogeneousPortfolioWeighting(),
         new FundamentalistStockReturnExpectationFunction(),
         clearingHouse,
         new NoSmoothingAlgorithm()
         );
      fund.debit(10.);
      final SimpleGoodsMarket market =
         new SimpleGoodsMarket(
            new DurableGoodsRepository.SimpleGoodsClassifier(),
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())
            );
      market.addInstrument("0");
      
      final MacroHousehold household = new MacroHousehold(
         bank,
         100.,                                              // £100 starting cash
         ParameterUtils.asMultiSampleParameter(1.0),        // 1.0 units of labour to supply
         market,
         new SimpleLabourMarket(
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())),
         new FixedWageExpectationAlgorithm(1.5e7),
         new ConsumptionPropensityBudgetAlgorithm(0.9),
         new HomogeneousConsumptionAlgorithm()
         );
      household.setFund(fund);
      household.addGoodToConsider("0");
      household.setFundInvestmentTargetDecisionRule(
         new SimpleThresholdFundInvestmentDecisionRule(150., 1.0, 0.1)
                                                            // Withdraw from funds when over £150,
                                                            // Invest 10% of unallocated cash,
                                                            // when available.
         );
      
      advanceUntilSampleTime();                             // Time T = 0 becomes time T = 1.
      
      // Initial fund investment
      household.debit(10.);
      try {
         fund.invest(household, 10.);                       // £10 initial fund contribution
      } catch (InsufficientFundsException e) {
         Assert.fail();
      }
      
      Employer employer = new ExogenousEmployer();
      
      advanceUntilJustAfterTime(NamedEventOrderings.BEFORE_ALL.getUnitIntervalTime() + 1.e-10);
      
      try {
         Labour.create(employer, household, 0.5, 1, 10);    // 0.5 units of labour at £10 per unit.
      } catch (final DoubleEmploymentException e) {
         Assert.fail();
      }
      
      ExogenousGoodsSeller producer = new ExogenousGoodsSeller(market);
      producer.setGoodsSellingPrice(1.0);                   // Initial goods price: £1 per unit
      market.getGoodsInstrument("0").registerSeller(producer);
                                                            // Ready next cycle.
      
      
      advanceUntilSampleTime();                             // AFTER_ALL + delta, at time 1.0
      
      producer.getGoodsRepository().push("0", 50.);         // Add 50 units of non-durable goods.
      
      advanceUntilJustAfterTime(NamedEventOrderings.BEFORE_ALL.getUnitIntervalTime() + 1.e-10);
      
      try {
         Labour.create(employer, household, 0.5, 1, 10);    // 0.5 units of labour at £10 per unit.
      } catch (final DoubleEmploymentException e) {
         Assert.fail();
      }
      
      advanceUntilSampleTime();
      
      producer.getGoodsRepository().pushWithDelay("0", 50.);  // Add 50 units of non-durable goods.
      
      advanceUnitlJustAfterBeforeAll();
      
      advanceUntilJustAfterTime(NamedEventOrderings.LABOUR_MARKET_MATCHING.getUnitIntervalTime());
      
      try {
         Labour.create(employer, household, 0.5, 1, 40);    // 0.5 units of labour at £40 per unit.
      } catch (final DoubleEmploymentException e) {
         Assert.fail();
      }
      
      advanceUntilSampleTime();
      
      producer.getGoodsRepository().pushWithDelay("0", 15.);  // Add 15 units of non-durable goods.
      
      advanceUnitlJustAfterBeforeAll();
      
      advanceUntilJustAfterTime(NamedEventOrderings.LABOUR_MARKET_MATCHING.getUnitIntervalTime());
      
      try {
         Labour.create(employer, household, 0.5, 1, 40);    // 0.5 units of labour at £40 per unit.
      } catch (final DoubleEmploymentException e) {
         Assert.fail();
      }
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(household.getAllocatedCash(),     0., 0.);
      Assert.assertEquals(household.getTotalLiabilities(),  0., 0.);
      
      Assert.assertEquals(household.getEquity(),            55.0, 1.e-12);
      Assert.assertEquals(household.getTotalAssets(),       55.0, 1.e-12);
      Assert.assertEquals(household.getCash(),              10.0355, 1.e-12);
      Assert.assertEquals(fund.getBalance(household),       44.9645, 1.e-12);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("MacroHouseholdTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         MacroHouseholdTest test = new MacroHouseholdTest();
         test.setUp();
         test.testWagesArePaidOnceOnly();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      try {
         MacroHouseholdTest test = new MacroHouseholdTest();
         test.setUp();
         test.testExtendedMacroHouseholdGoodsAndLabourAndFinancialBahaviour();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
