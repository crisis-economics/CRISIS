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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.algorithms.optimization.WeightedCobbDouglasAlgorithm;
import eu.crisis_economics.abm.firm.plugins.CobbDouglasProductionFunction;
import eu.crisis_economics.abm.model.parameters.ParameterUtils;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Unit tests for the {@link IOConstrainedCobbDouglasProductionFunction}.
  * 
  * @author phillips
  */
public final class TestIOCobbDouglasProductionFunction {
   
   /*
    * The following test parameters have no special or physical meanings.
    */
   final double
      labourUnitCost = 1.0,
      labourBias = 0.7,
      totalFactorProductivity = 5.0;
   Map<String, Double>
      technologicalWeights,
      economicUnitCosts,
      goodsToUse;
   final double
      labourToUse = 3.5;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getCanonicalName() + "...");
      
      technologicalWeights = new LinkedHashMap<String, Double>();
      technologicalWeights.put("type 1", 0.1);
      technologicalWeights.put("type 2", 0.2);
      technologicalWeights.put("type 3", 0.3);
      technologicalWeights.put("type 4", 0.4);
      
      economicUnitCosts = new LinkedHashMap<String, Double>();
      economicUnitCosts.put("type 1", 0.3);
      economicUnitCosts.put("type 2", 0.4);
      economicUnitCosts.put("type 3", 0.3);
      economicUnitCosts.put("type 4", 0.4);
      
      goodsToUse = new LinkedHashMap<String, Double>();
      goodsToUse.put("type 1", 1.1);
      goodsToUse.put("type 2", 1.2);
      goodsToUse.put("type 3", 1.3);
      goodsToUse.put("type 4", 1.3);
      
      Simulation simulation = new EmptySimulation(1L);
      simulation.start();
   }
   
   /**
     * Test whether {@link WeightedCobbDouglasAlgorithm} yields the expected
     * (a) production, and (b) total economic cost for a set of manual stimuli.
     * <br><br>
     * 
     * The expected results for this unit test could be reproduced with the following
     * snippet of Mathematica-like code:<br><br>
     * 
       (* Compute both the yield and cost of production according to 
         the weighted Cobb-Douglas production function. *) <br>
       CobbDouglas[Z_, Alpha_, weights_,  Labour_, Goods_, labourPrice_, goodsPrices_] :=
          Module[{yield, totalEconomicCost, numGoods},
          (* Compute yield *)
          yield = (Labour^Alpha);
          numGoods = Length[Goods];
          For[i = 1, i <= numGoods, i = i + 1,
             yield *= (Goods[[i]]^(weights[[i]] * (1 - Alpha)));
          ];
          yield = (Z^Alpha) * yield;
          totalEconomicCost = labourPrice * Labour;
          (* Compute total economic cost *)
          
          For[i = 1, i <= numGoods, i = i + 1,
             totalEconomicCost += goodsPrices[[i]] * Goods[[i]];
          ];
          Return[{yield, totalEconomicCost}];
          ]
      
       labourUnitCost = 1;
       alpha = 7/10;
       tfpFactor = 5;
       weights = { 1/10, 2/10, 3/10, 4/10 };
       economicCosts = { 3/10, 4/10, 3/10, 4/10 };
       labourToUse = 35/10;
       goodsToUse = { 11/10, 12/10, 13/10, 13/10 };
       
       (* Compute yield and cost *)
       CobbDouglas[tfpFactor, alpha, weights, labourToUse,
          goodsToUse, labourUnitCost, economicCosts]
     * 
     */
   @Test
   public void testCobbDouglasIONetworkOptimizationAlgorithm() {
      WeightedCobbDouglasAlgorithm optimizer = new WeightedCobbDouglasAlgorithm(
         labourUnitCost,
         technologicalWeights,
         economicUnitCosts,
         totalFactorProductivity,
         labourBias
         );
      
      final double
         observedYield = optimizer.evaluateProductionFor(labourToUse, goodsToUse),
         observedTotalEconomicCost = optimizer.evaluateTotalCostFor(labourToUse, goodsToUse);
      
      /*
       * Expected results.
       */
      final double
         expectedYield =
            Math.pow(3.,  (3./50.)) *
            Math.pow(5.,  (2./5.)) *
            Math.pow(7.,  (7./10.)) *
            Math.pow(11., (3./100.)) *
            Math.pow(13., (21./100.)) /
            Math.pow(2.,  (22./25.)),
         expectedEconomicCost = 261./50.;
      
      System.out.printf("yield: observed:               %16.10g expected: %16.10g\n", 
         observedYield, expectedYield);
      System.out.printf("total economic cost: observed: %16.10g expected: %16.10g\n", 
         observedTotalEconomicCost, expectedEconomicCost);
      
      Assert.assertEquals(observedYield, expectedYield, 1.e-14);
      Assert.assertEquals(observedTotalEconomicCost, expectedEconomicCost, 1.e-14);
   }
   
   /**
     * This method extends {@link #testCobbDouglasIONetworkOptimizationAlgorithm()}
     * by processing goods and labour demand for a set of stimuli. The desired
     * yield and resource prices/economic costs are set manually, at which point
     * an instance of {@link CobbDouglasProductionFunction} computes the desired 
     * volumes for labour and goods.<br><br>
     * 
     * The expected results for this unit test could be reproduced with the following
     * snippet of Mathematica-like code (in conjunction with the snippet appearing
     * in the documentation for {@link #testCobbDouglasIONetworkOptimizationAlgorithm}):
     * <br><br>
     * 
       N[NMinimize[{ <br>
          CobbDouglas[tfpFactor, alpha, weights, L, {g1, g2, g3, g4}, <br>
            labourUnitCost, economicCosts][[2]], <br>
          CobbDouglas[tfpFactor, alpha, weights, L, {g1, g2, g3, g4}, <br>
             labourUnitCost, economicCosts][[1]] == 10, <br>
          L >= 0, g1 >= 0, g2 >= 0, g3 >= 0, g4 >= 0 <br>
          }, <br>
         {L, g1, g2, g3, g4}, <br>
         WorkingPrecision -> 300, <br>
         MaxIterations -> 1000, <br>
         PrecisionGoal -> 40, <br>
         AccuracyGoal -> 40 <br>
         ], 50] <br>
     *
     */
   @Test
   public void testCobbDouglasIONetworkProductionFunction() {
      final int
         numGoods = technologicalWeights.size();
      final double
         productionTarget = 10.;
//      List<Double>
//         normalizedWeights = new ArrayList<Double>(technologicalWeights);
//      double norm = 0.;
//      for(int i = 0; i< normalizedWeights.size(); ++i)
//         norm += Math.pow(normalizedWeights.get(i), 2.0);
//      norm = Math.sqrt(norm);
//      for(int i = 0; i< normalizedWeights.size(); ++i)
//         normalizedWeights.set(i, normalizedWeights.get(i) / norm)
      
      CobbDouglasProductionFunction productionFunction =
         new CobbDouglasProductionFunction(
            ParameterUtils.asTimeseries(totalFactorProductivity),
            labourBias,
            technologicalWeights
            );
      
      OptimalProduction optimium = 
         productionFunction.computeInputsDemand(
            productionTarget, labourUnitCost, economicUnitCosts);
      
      System.out.printf(
         "optimum (observed):\n" +
         "desired units of labour: %16.10g\n" +
         "total economic cost:     %16.10g\n",
         optimium.getDesiredUnitsOfLabour(),
         optimium.getTotalEconomicCost()
         );
      for(final String type : technologicalWeights.keySet())
         System.out.printf(
            "demand for type %s:      %16.10g\n",
            type, optimium.getDemandAmounts().get(type)
         );
      
      /*
       * Expected results
       */
      final double
         expectedTotalEconomicCost = 6.432672333260720738,
         expectedDesiredUnitsOfLabour = 4.502870633282504516;
      final List<Double>
         expectedGoodsDemands = Collections.unmodifiableList(
            Arrays.asList(
               0.643267233326072073837,
               0.964900849989108110756,
               1.929801699978216221512,
               1.929801699978216221512
               ));
      
      System.out.printf(
         "optimum (expected):\n" +
         "desired units of labour: %16.10g\n" +
         "total economic cost:     %16.10g\n",
         expectedDesiredUnitsOfLabour,
         expectedTotalEconomicCost
         );
      for(int i = 0; i< numGoods; ++i)
         System.out.printf(
            "demand for type %2d:      %16.10g\n",
            i + 1, expectedGoodsDemands.get(i)
         );
      
      Assert.assertEquals(
         expectedDesiredUnitsOfLabour, optimium.getDesiredUnitsOfLabour(), 1.e-12);
      Assert.assertEquals(
         expectedTotalEconomicCost, optimium.getTotalEconomicCost(), 1.e-12);
      {
      int i = 0;
      for(final Entry<String, Double> record : economicUnitCosts.entrySet())
         Assert.assertEquals(
            optimium.getDemandAmounts().get(record.getKey()),
            expectedGoodsDemands.get(i++),
            1.e-12
            );
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
         TestIOCobbDouglasProductionFunction test = new TestIOCobbDouglasProductionFunction();
         test.setUp();
         test.testCobbDouglasIONetworkOptimizationAlgorithm();
         test.tearDown();
      } catch (final Exception unexpectedException) {
         System.err.println(
            "Unexpected exception raised. TestIOCobbDouglasProductionFunction tests failed.");
         System.err.flush();
      }
      
      try {
         TestIOCobbDouglasProductionFunction test = new TestIOCobbDouglasProductionFunction();
         test.setUp();
         test.testCobbDouglasIONetworkProductionFunction();
         test.tearDown();
      } catch (final Exception unexpectedException) {
         System.err.println(
            "Unexpected exception raised. TestIOCobbDouglasProductionFunction tests failed.");
         System.err.flush();
         Assert.fail();
      }
   }
}
