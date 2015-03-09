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

import eu.crisis_economics.abm.algorithms.optimization.NeumannLeontieffAlgorithm;
import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.firm.plugins.NeumannLeontieffProductionFunction;

/**
  * Unit tests for the {@link NeumannLeontieffProductionFunction}.
  * 
  * @author phillips
  */
public final class TestLeontieffProductionFunction {
   
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
   }
   
   /**
     * Test whether the {@link NeumannLeontieffAlgorithm} yields the expected
     * (a) production, and (b) total economic cost for a set of manual stimuli.<br><br>
     * 
     * The expected results for this unit test could be reproduced with the following
     * snippet of Mathematica-like code:<br><br>
     * 
       (* Compute both the yield and cost of production according to 
          the Neumann-Leontieff production function. *) <br>
       Leontieff[Z_, Alpha_, weights_,  Labour_, Goods_, labourPrice_, goodsPrices_] := <br>
          Module[{yield, totalEconomicCost, numGoods}, <br>
          (* Compute yield *) <br>
          yield = Alpha * Labour; <br>
          numGoods = Length[Goods]; <br>
          For[i = 1, i <= numGoods, i = i + 1, <br>
             yield = Min[yield, (1 - Alpha) * weights[[i]] * Goods[[i]]]; <br>
          ]; <br>
          yield = Z * yield; <br>
          totalEconomicCost = labourPrice * Labour; <br>
          (* Compute total economic cost *) <br>
          For[i = 1, i <= numGoods, i = i + 1, <br>
             totalEconomicCost += goodsPrices[[i]] * Goods[[i]]; <br>
          ]; <br>
          Return[{yield, totalEconomicCost}]; <br>
       ] <br>
       
       labourUnitCost = 1;
       alpha = 7/10;
       tfpFactor = 5;
       weights = { 1/10, 2/10, 3/10, 4/10 };
       economicCosts = { 3/10, 4/10, 3/10, 4/10 };
       labourToUse = 35/10;
       goodsToUse = { 11/10, 12/10, 13/10, 13/10 };
       
       (* Compute yield and cost *)
       Leontieff[tfpFactor, alpha, weights, labourToUse, goodsToUse, labourUnitCost, economicCosts]
     *
     */
   @Test
   public void testLeontieffIONetworkOptimizationAlgorithm() {
      /*
       * Expected results.
       */
      final double
         expectedYield = 33./200.,
         expectedEconomicCost = 261./50.;
      
      NeumannLeontieffAlgorithm optimizer = new NeumannLeontieffAlgorithm(
         labourUnitCost,
         labourBias,
         totalFactorProductivity,
         technologicalWeights,
         economicUnitCosts
         );
      
      final double
         observedYield = optimizer.evaluateProductionFor(labourToUse, goodsToUse),
         observedTotalEconomicCost = optimizer.evaluateTotalCostFor(labourToUse, goodsToUse);
      
      System.out.printf("yield: observed:               %16.10g expected: %16.10g\n", 
         observedYield, expectedYield);
      System.out.printf("total economic cost: observed: %16.10g expected: %16.10g\n", 
         observedTotalEconomicCost, expectedEconomicCost);
      
      Assert.assertEquals(observedYield, expectedYield, 1.e-14);
      Assert.assertEquals(observedTotalEconomicCost, expectedEconomicCost, 1.e-14);
   }
   
   /**
     * This method extends {@link #testLeontieffIONetworkOptimizationAlgorithm}
     * by processing goods and labour demand for a set of stimuli. The desired
     * yield and resource prices/economic costs are set manually, at which point
     * an instance of {@link NeumannLeontieffProductionFunction} computes the desired 
     * volumes for labour and goods.<br><br>
     * 
     * The expected results for this unit test could be reproduced with the following
     * snippet of Mathematica-like code (in conjunction with the snippet appearing
     * in the documentation for {@link #testLeontieffIONetworkOptimizationAlgorithm}):
     * <br><br>
     * 
       N[Minimize[{ <br>
          Leontieff[tfpFactor, alpha, weights, L, {g1, g2, g3, g4}, <br>
          labourUnitCost, economicCosts][[2]], <br>
          Leontieff[tfpFactor, alpha, weights, L, {g1, g2, g3, g4}, <br>
          labourUnitCost, economicCosts][[1]] == 10 <br>
          }, <br>
          {L, g1, g2, g3, g4} <br>
          ], 20] <br>
       
     * 
     */
   @Test
   public void testLeonteiffIONetworkProductionFunction() {
      final int
         numGoods = technologicalWeights.size();
      final double
         productionTarget = 10.;
      
      NeumannLeontieffProductionFunction productionFunction =
         new NeumannLeontieffProductionFunction(
            labourBias, totalFactorProductivity, technologicalWeights);
      
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
         expectedTotalEconomicCost = 49.523809523809523810,
         expectedDesiredUnitsOfLabour = 2.8571428571428571429;
      final List<Double>
         expectedGoodsDemands = Collections.unmodifiableList(
            Arrays.asList(
               66.666666666666666667,
               33.333333333333333333,
               22.222222222222222222,
               16.666666666666666667
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
      System.out.println(getClass().getSimpleName() + " tests pass.");
   }
   
   /*
    * Manual entry point
    */
   static public void main(final String args[]) {
      try {
         TestLeontieffProductionFunction test = new TestLeontieffProductionFunction();
         test.setUp();
         test.testLeontieffIONetworkOptimizationAlgorithm();
         test.tearDown();
      } catch (final Exception unexpectedException) {
         System.err.println(
            "Unexpected exception raised. TestLeontieffProductionFunction tests failed.");
         System.err.flush();
      }
      
      try {
         TestLeontieffProductionFunction test = new TestLeontieffProductionFunction();
         test.setUp();
         test.testLeonteiffIONetworkProductionFunction();
         test.tearDown();
      } catch (final Exception unexpectedException) {
         System.err.println(
            "Unexpected exception raised. TestLeontieffProductionFunction tests failed.");
         System.err.flush();
         Assert.fail();
      }
   }
}
