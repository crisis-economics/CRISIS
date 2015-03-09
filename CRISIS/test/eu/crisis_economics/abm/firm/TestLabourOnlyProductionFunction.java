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
package eu.crisis_economics.abm.firm;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.firm.plugins.LabourOnlyProductionFunction;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction.Yield;
import eu.crisis_economics.abm.model.parameters.ParameterUtils;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Unit tests for the {@link LabourOnlyProductionFunction} {@link FirmProductionFunction}
  * algorithm.
  * 
  * @author phillips
  */
public final class TestLabourOnlyProductionFunction {
   
   /*
    * The following test parameters have no special or physical meanings.
    */
   final double
      labourUnitCost = 2.0,
      labourProductivityExponent = 0.7,
      totalFactorProductivity = 5.0;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getCanonicalName() + "...");
      Simulation simulation = new EmptySimulation(1L);
      simulation.start();
   }
   
   /**
     * Test whether the {@link LabourOnlyProductionFunction} {@link FirmProductionFunction}
     * yields the expected production amount, computes the cost of production correctly, 
     * and solves the labour input problem correctly.<br><br>
     * 
     * The expected results for this unit test can be reproduced with the following
     * snippet of Mathematica-like code:<br><br>
       
       <code>
         LabourOnlyProduction[Z_, Q_, LabourInput_, WagePerUnitLabour_] :=
          Module[{yield, totalEconomicCost, numGoods},
           (*Compute yield*)
           yield = Z *(LabourInput^Q);
           totalEconomicCost = WagePerUnitLabour*LabourInput;
           Return[{yield, totalEconomicCost}];
           ]
         
         Z = 5;
         Q = 7/10;
         WagePerUnitLabour = 2;
         
         LabourOnlyProduction[Z, Q, 2, WagePerUnitLabour]
         
         NSolve[
          LabourOnlyProduction[Z, Q, x, WagePerUnitLabour][[1]] == 
           5 2^(7/10), x]
        </code>
        <br><br>
       
     * This unit test is split into two parts. In Part 1, the yield (goods amount
     * produced) is tested against expected values. In Part 2, it is asserted the
     * the {@link LabourOnlyProductionFunction} solves the labour input problem
     * correctly.
     */
   @Test
   public void testLabourOnlyProductionOptimizationAlgorithm() {
      final LabourOnlyProductionFunction
         productionFunction = new LabourOnlyProductionFunction(
            ParameterUtils.asTimeseries(totalFactorProductivity),
            labourProductivityExponent
            );
      
      { // Part 1
      final Yield
         yield = productionFunction.produce(
            2., new HashMap<String, Double>(), labourUnitCost, new HashMap<String, Double>());
      
      final double
         expectedYield = 5. * Math.pow(2., 7./10.),
         expectedCost = 4.;
      Assert.assertEquals(expectedYield, yield.getGoodsYield(), 1.e-12);
      Assert.assertEquals(expectedCost, yield.getCashProductionCost(), 1.e-12);
      }
      
      { // Part 2
      OptimalProduction
         solution = productionFunction.computeInputsDemand(
            5. * Math.pow(2., 7./10.), labourUnitCost, new HashMap<String, Double>());
      
      final double
         expectedLabourInput = 2.,
         expectedCost = 4.;
      Assert.assertEquals(expectedLabourInput, solution.getDesiredUnitsOfLabour(), 1.e-12);
      Assert.assertEquals(expectedCost, solution.getTotalEconomicCost(), 1.e-12);
      
      Map<String, Double>
         goodsInputDemands = solution.getDemandAmounts();
      Assert.assertTrue(goodsInputDemands.isEmpty());
      }
   }
   
   @AfterMethod
   public void tearDown() {
      Simulation.getSimState().finish();
      System.out.println(getClass().getSimpleName() + " tests pass.");
   }
   
   /*
    * Manual entry point
    */
   static public void main(final String args[]) {
      try {
         TestLabourOnlyProductionFunction test = new TestLabourOnlyProductionFunction();
         test.setUp();
         test.testLabourOnlyProductionOptimizationAlgorithm();
         test.tearDown();
      } catch (final Exception unexpectedException) {
         System.err.println(
            "Unexpected exception raised. TestIOCobbDouglasProductionFunction tests failed.");
         System.err.flush();
         Assert.fail();
      }
   }
}
