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
package eu.crisis_economics.abm.firm;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.HomogeneousRationingAlgorithm;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.ManualGoodsClassifier;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.model.parameters.FunctionParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/**
  * Lightweight unit tests for the class {@link FromFileTimeseriesParameter}.
  * 
  * @author phillips
  */
public class TestExogenousFirm {
   
   private Simulation
      state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "...");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   private void advanceTimeByOneCycle() {
      final double timeNow = state.schedule.getTime();
      while(state.schedule.getTime() < timeNow + 1)
         state.schedule.step(state);
   }
   
   void advanceUntilJustAfterTime(final SimulatedEventOrder order) {
      final SimulatedEventOrder haltPoint = CustomSimulationCycleOrdering.create(order, 1);
      class DummyEvent { 
         boolean hasFired = false;
         public DummyEvent() {
            if(Simulation.getTime() - Simulation.getFloorTime() > order.getUnitIntervalTime())
               Simulation.onceCustom(this, "emptyEvent", haltPoint, ScheduleIntervals.create(1));
            else
               Simulation.onceCustom(this, "emptyEvent", haltPoint, ScheduleIntervals.create(0));
         }
         @SuppressWarnings("unused")
         public void emptyEvent() { hasFired = true; }
      }
      DummyEvent event = new DummyEvent();
      while(true) {
         state.schedule.step(state);
         if(event.hasFired) return;          // This is a false positive FindBugs mark point.
         else continue;
      }
   }
   
   /**
     * Create a {@link ModelParameter} from a {@link UnivariateFunction}.
     * 
     * @param function
     *        The {@link UnivariateFunction} used to generate the {@link ModelParameter}.
     */
   private TimeseriesParameter<Double>
      createParameterFromUnivariateFunction(
         final UnivariateFunction function) {
      return new TimeseriesParameter<Double>(
         new FunctionParameter(function, "")
         );
   }
   
   /**
     * Test whether an instance of {@link ExogenousFirm} behaves as expected. This
     * test operates as follows:<br><br>
     * 
     * {@code (a)}
     *   One {@link StockTradingBank} is created, to serve as a {@link StockHolder}
     *   and {@link DepositHolder} for an {@link ExogenousFirm};<br>
     * {@code (b)}
     *   One {@link ExogenousFirm} is created, as well as one goods market and one
     *   labour market;<br>
     * {@code (c)}
     *   {@link UnivariateFunction}{@code s} are created for every decision on which
     *   the {@link ExogenousFirm} depends;<br>
     * {@code (d)}
     *   One {@link ExogenousGoodsBuyer} and one {@link ExogenousEmployee} are 
     *   created. Both are connected to the relevant markets;<br>
     * {@code (e)}
     *   The {@link ExogenousGoodsBuyer} is credited and debited several times, 
     *   and made subject to cash injections. It is asserted that the financial
     *   state of the {@link DepositHolder} is unchanged;<br>
     * {@code (f)}
     *   Several simulation cycles elapse. In each cycle, it is asserted that the
     *   balance sheet of the {@link ExogenousFirm} is as expected and that the
     *   {@link ExogenousFirm} has interacted with the markets as expected.
     */
   @Test
   public void testExogenousFirmBehaviour() {
      final StockTradingBank
         bank = new StockTradingBank(1.0);                  // £1 to pay for initial shares
      
      final Map<String, Pair<Double, Double>> goodsTypes =  // One durable good,
         new HashMap<String, Pair<Double, Double>>();       //  one non-durable good.
      goodsTypes.put("type 1", Pair.create(1.0, 1.0));      // 100% time/use decay
      goodsTypes.put("type 2", Pair.create(0.1, 0.2));      // 10% time decay, 20% use decay.
      
      final SimpleGoodsMarket goodsMarket =
         new SimpleGoodsMarket(
            new ManualGoodsClassifier(goodsTypes),
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())
            );
      goodsMarket.addInstrument("type 1");
      goodsMarket.addInstrument("type 2");
      
      final SimpleLabourMarket labourMarket =
         new SimpleLabourMarket(
            new ForagerMatchingAlgorithm(
               new HomogeneousRationingAlgorithm()));
      
      final Random
         dice = new Random(1L);
      
      final class TestFunction implements UnivariateFunction {
         private final double
            grad;
         private TestFunction(Random dice) {
            grad = dice.nextDouble() + 1.;
         }
         @Override
         public double value(final double t) {
            return grad * (Math.floor(t) + 1.);
         }
      }
      
      final TimeseriesParameter<Double>
         deposits = createParameterFromUnivariateFunction(new TestFunction(dice)),
         commercialLoanDemand = createParameterFromUnivariateFunction(new TestFunction(dice)),
         desiredLabour = createParameterFromUnivariateFunction(new TestFunction(dice)),
         wageBidPrice = createParameterFromUnivariateFunction(new TestFunction(dice)),
         productionYield = createParameterFromUnivariateFunction(new TestFunction(dice)),
         sellingPrice = createParameterFromUnivariateFunction(new TestFunction(dice)),
         dividendPayment = createParameterFromUnivariateFunction(new TestFunction(dice)),
         maximumLendingRate = createParameterFromUnivariateFunction(new TestFunction(dice));
      
      final ExogenousFirm
         firm = new ExogenousFirm(
            bank,
            bank, 
            1.,                                                      // 1.0 shares emitted
            1.,                                                      // £1.0 per share
            "type 1",
            labourMarket,
            goodsMarket,
            deposits,
            commercialLoanDemand,
            desiredLabour,
            wageBidPrice,
            productionYield,
            sellingPrice,
            dividendPayment,
            maximumLendingRate,
            new CreditDemandFunction.RiskNeutralCreditDemandFunction(),
            new InstanceIDAgentNameFactory()
            );
      
      // Check whether debits, credits and cash flow injections are exogenous:
      
      {
      final double
         startingBankCashReserve = bank.getCashReserveValue(),
         startingFirmEquity = firm.getEquity();
      
      firm.debit(Double.MAX_VALUE);
      firm.debit(-1.);
      
      try {
         firm.credit(Double.MAX_VALUE / 2.);
         firm.credit(-1.);
      } catch (InsufficientFundsException e) {
         Assert.fail();
      }
      
      firm.cashFlowInjection(Double.MAX_VALUE / 4.);
      firm.cashFlowInjection(-1.);
      
      Assert.assertEquals(bank.getCashReserveValue(), startingBankCashReserve, 1.e-12);
      Assert.assertEquals(firm.getEquity(), startingFirmEquity, 1.e-12);
      }
      
      // Check whether cash allocation has memory:
      
      firm.allocateCash(100.);
      Assert.assertEquals(firm.getAllocatedCash(), 100., 1.e-12);
      firm.allocateCash(50.);
      Assert.assertEquals(firm.getAllocatedCash(), 150., 1.e-12);
      firm.disallocateCash(151.);
      Assert.assertEquals(firm.getAllocatedCash(), 0., 1.e-12);
      
      final ExogenousEmployee
         employee = new ExogenousEmployee(labourMarket);
      final ExogenousGoodsBuyer
         goodsBuyer = new ExogenousGoodsBuyer(goodsMarket);
      
      advanceTimeByOneCycle();
      
      double
         productionYieldLastValue = 0.;
      
      for(int i = 0; i< 10; ++i) {
         employee.setLabourSupply(100., 1.);
         goodsBuyer.setGoodsDemand("type 1", 100.);
         
         advanceUntilJustAfterTime(NamedEventOrderings.GOODS_INPUT_MARKET_MATCHING);
         
         final double
            depositsValueProvided = deposits.get(),
            desiredLabourValueProvided = desiredLabour.get(),
            wageBidPriceValueProvided = wageBidPrice.get(),
            productionYieldValueProvided = productionYield.get(),
            sellingPriceValueProvided = sellingPrice.get(),
            dividendPaymentValueProvided = dividendPayment.get();
         
         if(i != 0)                                                        // No trade in the first
            Assert.assertEquals(                                           //  simulation cycle, as
               goodsMarket.getInstrument("type 1")                         //  goods are yet to
                  .getWorstAskPriceAmongSellers(),                         //  appear in inventory.
               sellingPriceValueProvided,
               1.e-12
               );
         Assert.assertEquals(
            goodsMarket.getInstrument("type 1").getTotalSupply(), productionYieldLastValue, 1.e-12);
         
         
         advanceUntilJustAfterTime(NamedEventOrderings.AFTER_ALL);
         
         Assert.assertEquals(firm.getDepositValue(), depositsValueProvided, 1.e-12);
         Assert.assertEquals(firm.getAllocatedCash(), 0., 1.e-12);
         
         Assert.assertEquals(
            labourMarket.getLastLabourTotalDemand(), desiredLabourValueProvided, 1.e-12);
         Assert.assertEquals(
            firm.getCurrentDividend(), dividendPaymentValueProvided, 1.e-12);
         Assert.assertEquals(
            labourMarket.getInstrument(1).getDemandWeightedBidWage(),
            wageBidPriceValueProvided, 1.e-12);
         
         productionYieldLastValue = productionYieldValueProvided;
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
      state.finish();
   }
   
   /*
    * Manual entry point.
    */
   static public void main(final String[] args) {
      try {
         final TestExogenousFirm
            test = new TestExogenousFirm();
         test.setUp();
         test.testExogenousFirmBehaviour();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
