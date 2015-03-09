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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ai.aitia.crisis.aspectj.Agent;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.HomogeneousRationingAlgorithm;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.firm.bankruptcy.DefaultFirmBankruptcyHandler;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.firm.plugins.LabourOnlyProductionFunction;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.ManualGoodsClassifier;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.model.parameters.ParameterUtils;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.FirmProductionFunctionFactory;
import eu.crisis_economics.utilities.Pair;

/**
  * An extended unit test for {@link MacroFirm} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public class MacroFirmTest {
   
   private Simulation state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "..");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   /**
     * Advance the simulation clock until just after the next occurence of
     * the stated event.
     * 
     * @param eventOrder <br>
     *        The event to wait for in the simulation cycle order.
     */
   void advanceUntilJustAfterTime(final SimulatedEventOrder order) {
      final SimulatedEventOrder haltPoint = CustomSimulationCycleOrdering.create(order, 100);
      class DummyEvent { 
         boolean hasFired = false;
         public DummyEvent() {
            Simulation.onceCustom(this, "emptyEvent", haltPoint, ScheduleIntervals.create(0));
         }
         @SuppressWarnings("unused")
         public void emptyEvent() { hasFired = true; }
      }
      DummyEvent event = new DummyEvent();
      while(true) {
         state.schedule.step(state);
         if(event.hasFired) return;
         else continue;
      }
   }
   
   void advanceUntilJustAfterTime(final double target) {
      double
         startTime = state.schedule.getTime();
      if(startTime > target)
         throw new IllegalArgumentException();
      while(state.schedule.getTime() <= target)
         state.schedule.step(state);
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
   }
   
   /**
     * Create a {@link FirmDecisionRule} from a custom timeseries X. The resulting
     * {@link FirmDecisionRule} will return the value X(n) at any time t in the 
     * interval [n, n+1).
     */
   private FirmDecisionRule createFirmDecisionRuleFromTimeseries(
      final double[] timeseries,
      final boolean incrementBefore
      ) {
      return new FirmDecisionRule(timeseries[0]) {
         int index = 0;
         @Override
         public double computeNext(final FirmState currentState) {
            final double
               result = timeseries[incrementBefore ? ++index : index++];
            super.recordNewValue(result);
            return result;
         }
      };
   }
   
   /**
     * Create a {@link TargetProductionAlgorithm} from a custom timeseries X. The
     * resulting {@link TargetProductionAlgorithm} will return the value X(n) at 
     * any time t in the interval [n, n+1).
     */
   private FirmDecisionRule createFirmTargetProductionAlgorithm(final double[] timeseries) {
      return new FirmDecisionRule(timeseries[0]) {
         int index = 0;
         @Override
         public double computeNext(final FirmState currentState) {
            final double
               result = timeseries[++index];
            super.recordNewValue(result);
            return result;
         }
      };
   }
   
   /**
     * This relatively complex test involves the creation of:<br>
     * 
     * <ul>
     *    <li> one {@link MacroFirm} agent (<code>F</code>) with an initial cash
     *         endowment, initial inventory, and public stock emission value;
     *    <li> one {@link Bank}, collecting all deposits and owning all 
     *         {@link Firm} stocks;
     *    <li> one (macro-scale) {@link Employee} with exogenous deposit
     *         accounts and a customizable {@link Labour} supply;
     *    <li> one goods buyer, with exogenous cash reserves;
     * </ul>
     * 
     * The test configures <code>F</code> in such a way that all decisions made
     * by <code>F</code> are predetermined. For several simulation cycles, 
     * <code>F</code> will engage in production activities, be issued with
     * commercial loans, and will pay dividends to a single stockholder
     * {@link Bank}. The demand for goods and services (namely, consumption) is 
     * regulated in such a way that the performance of <code>F</code> on the 
     * goods and labour markets is also predicted.<br><br>
     * 
     * <b>Predicting the results of the test.</b><br><br>
     * 
     * The last line of this unit test asserts that the value of the {@link Firm}
     * {@link DepositAccount} is as expected. When this assertion is made, the
     * simulation has evolved through several nontrivial business cycles.<br><br>
     * 
     * The expected value of the {@link Firm} {@link DepositAccount} can be obtained
     * by running an accompanying standalone python script in the same directory
     * as this java codefile.<br><br>
     * 
     * The script, named {@code ./compute-macrofirm-test-expected-results.py}, simulates
     * the outcome of this simulation in python and prints the resulting final {@link Firm}
     * {@link DepositAccount} value to the console. Further information can be found in
     * the header of this python file if required. The python file accepts no arguments
     * can be run simply by executing:<br><br>
     * 
     * <code><center>
     *   python compute-macrofirm-test-expected-results.py
     * </center></code><br>
     * 
     * in the same directory as this java codefile.
     */
   @Test
   public void testExtendedMacroFirmBahaviour() {
      
      final StockTradingBank bank = 
         new StockTradingBank(100.);                        // Shared deposit holder
      
      final Map<String, Pair<Double, Double>> goodsTypes =  // One durable good,
         new HashMap<String, Pair<Double, Double>>();       //  one non-durable good.
      goodsTypes.put("1", Pair.create(1.0, 1.0));           // 100% time/use decay
      
      final SimpleGoodsMarket goodsMarket =
         new SimpleGoodsMarket(
            new ManualGoodsClassifier(goodsTypes),
            new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())
            );
      goodsMarket.addInstrument("1");
      
      final SimpleLabourMarket labourMarket =
         new SimpleLabourMarket(
            new ForagerMatchingAlgorithm(
               new HomogeneousRationingAlgorithm()));
      
      final ExogenousEmployee
         employee = new ExogenousEmployee(labourMarket);
      
      final ExogenousGoodsBuyer
         buyer = new ExogenousGoodsBuyer(goodsMarket);
      
      /*
       * Predetermined decision outcomes:
       */
      final double[]
         consumptionDemand     = new double[] { 0.5, 0.55, 0.6, 0.55, 0.5 },
         firmtargetProductions = new double[] { 1.0, 2.0,  3.0, 2.0,  1.0 },
         goodsPrices           = new double[] { 5.0, 6.0,  7.0, 6.0,  4.0 },
         wageBidPrices         = new double[] { 1.0, 0.9,  0.2, 0.8,  1.1 },
         liquidityTargets      = new double[] { 1.0, 0.8,  0.6, 0.7,  0.8 };
      
      final MacroFirm firm = new MacroFirm(
         bank,                                              
         0.49,                                              // £0.49 starting cash
         bank,                                              
         1.0,                                               // 1 initial shares to emit,
         0.01,                                              //  at £0.01/unit
         "1",                                               
         1.0,                                               // 1.0 units of starting inventory
         goodsMarket,
         labourMarket,
         createFirmTargetProductionAlgorithm(firmtargetProductions),
         createFirmDecisionRuleFromTimeseries(goodsPrices, true),
         createFirmDecisionRuleFromTimeseries(wageBidPrices, false),
         new FirmProductionFunctionFactory() {
            @Override
            public FirmProductionFunction create(final String goodsType) {
               return new LabourOnlyProductionFunction(ParameterUtils.asTimeseries(1.0), 1.0);
            }
         },
         new CreditDemandFunction.CustomRiskToleranceCreditDemandFunction(0., 1.0, 1.0),
         createFirmDecisionRuleFromTimeseries(liquidityTargets, false),
         new DefaultFirmBankruptcyHandler(),
         new InstanceIDAgentNameFactory()
         );
      goodsMarket.getInstrument("1").registerSeller(firm);
      
      System.out.printf("Starting extended MacroFirm test..\n");
      
      advanceToTimeZero();
      advanceUntilJustAfterTime(                            // wait for enqueued goods to appear
         NamedEventOrderings.BEFORE_ALL);
      
      double
         observedFirmCash = 0.0;
      
      for(int i = 0; i< 5; ++i) {
         employee.setLabourSupply(20.0, wageBidPrices[i]);
         
         advanceUntilJustAfterTime(NamedEventOrderings.FIRM_SHARE_PAYMENTS);
         
         observedFirmCash = firm.getDepositValue();
         
         try {
            final double
               loanAmount = Math.max(0.0,
                  firmtargetProductions[i] * wageBidPrices[i] - firm.getDepositValue());
            bank.debit(loanAmount);
            LoanFactory.createFixedRateMortgage(firm, bank, loanAmount, 0.1, 1);
         } catch (LenderInsufficientFundsException neverThrows) {
            Assert.fail();
         }
         
         buyer.setGoodsDemand("1", consumptionDemand[i]);
         
         if(i < 4)
            advanceUntilJustAfterTime(i+1);
      }
      
      /*
       * Many cycles have now elapsed. At this stage, asserting the value of the firm
       * deposit account versus a predicted value is an acceptable reassurance:
       */
      Assert.assertEquals(observedFirmCash, 0.7);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         MacroFirmTest test = new MacroFirmTest();
         test.setUp();
         test.testExtendedMacroFirmBahaviour();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
