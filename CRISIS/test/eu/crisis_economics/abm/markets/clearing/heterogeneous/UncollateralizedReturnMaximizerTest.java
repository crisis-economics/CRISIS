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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction.TradeOpportunity;
import eu.crisis_economics.abm.strategy.clearing.DominoUncollateralizedReturnMaximizer;
import eu.crisis_economics.abm.strategy.clearing.LCQPUncollateralizedReturnMaximizer;
import eu.crisis_economics.abm.strategy.clearing.UncollateralizedReturnMaximizerMRFAdapater;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Unit tests for investment portfolio profit maximization algorithms.<br><br>
  * 
  * Portfolio profit maximization algorithms accept as input a set of asset return rates
  * and liability interest rates with market impact (Ie. return rates depending on the 
  * investment size) and return, as output, a collection of asset investments and liability
  * debts which maximize the expected profit over the entire investment portfolio.<br><br>
  * 
  * It is possible to use more than one algorithm to determine these optimal investments.
  * This unit test explores both the relative performance of different portfolio maximization
  * algorithms and the results that these algorithms yield.
  * 
  * @author phillips
  */
public final class UncollateralizedReturnMaximizerTest {
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing UncollateralizedReturnMaximizer..");
   }
   
   /** 
    * The following is a Mathematica-style code snippet to perform the 
    * optimization and output the expected test results.
    * 
      
      A[a_] := (-10^-3)*a + (1/20)
      rL[l_] :=  (+10^-3)*l + (1/10)
      
      \[Lambda] = 3
      Eq = 1/10
      
      MeritFunction[a_, l_] := rA[a] * a - rL[l] * l
      
      NMaximize[{
       rA[a] * a - rL[l] * l,
       a >= 0,
       l >= 0,
       a <= \[Lambda]*Eq,
       a - l == 0
       },
      {a, l},
      WorkingPrecision -> 40
      ]
    
    * 
    */
   @Test
   public void testEqualAssetLiabilityReturnRates() {
      System.out.println("Testing UncollateralizedReturnMaximizer (equal return rates)..");
      { // Equal nonzero return rates
         final List<Pair<Double, Double>>
            knownAssetRates = new ArrayList<Pair<Double,Double>>(),
            knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
         knownAssetRates.add(new Pair<Double, Double>(-1.e-3, 0.1));
         knownLiabilityRates.add(new Pair<Double, Double>(1.e-3, 0.1));
         
         final double[] expectedResult = { 0., 0. };
         
         LCQPUncollateralizedReturnMaximizer optimizer = 
            new LCQPUncollateralizedReturnMaximizer();
         
         final List<Double> result = optimizer.performOptimization(
            knownAssetRates,
            knownLiabilityRates,
            0.,         // cash
            0.1 * 3.0   // capital constraint
            );
         
         System.out.printf(
            "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
            "expected: asset investment: %16.10g liabilities: %16.10g\n",
            result.get(0), result.get(1),
            expectedResult[0], expectedResult[1]
            );
         
         Assert.assertTrue(Math.abs(result.get(0) - expectedResult[0]) < 1.e-3);
         Assert.assertTrue(Math.abs(result.get(1) - expectedResult[1]) < 1.e-3);
      }
      
      { // Unequal return rates, liabilities dominate.
         final List<Pair<Double, Double>>
            knownAssetRates = new ArrayList<Pair<Double,Double>>(),
            knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
         knownAssetRates.add(new Pair<Double, Double>(-1.e-3, 0.05));
         knownLiabilityRates.add(new Pair<Double, Double>(1.e-3, 0.1));
         
         final double[] expectedResult = { 0., 0. };
         
         LCQPUncollateralizedReturnMaximizer optimizer = 
            new LCQPUncollateralizedReturnMaximizer();
         
         final List<Double> result = optimizer.performOptimization(
            knownAssetRates,
            knownLiabilityRates,
            0.,         // cash
            0.1 * 3.0   // capital constraint
            );
         
         System.out.printf(
            "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
            "expected: asset investment: %16.10g liabilities: %16.10g\n",
            result.get(0), result.get(1),
            expectedResult[0], expectedResult[1]
            );
         
         Assert.assertTrue(Math.abs(result.get(0) - expectedResult[0]) < 1.e-5);
         Assert.assertTrue(Math.abs(result.get(1) - expectedResult[1]) < 1.e-5);
      }
      
      System.out.println("Pass.");
   }
   
   /**
     * Test the LCQP portfolio profit maximization algorithm using
     * unequal asset and liability interest rates. In this test, the 
     * asset return rate is 20% and the liability return rate is 10%.
     * The market impact in both cases is -/+1.e-3 per unit investment.
     * The expected optimal investment (.3 units of cash) has been
     * computed manually.
     */
   @Test
   public void testUnequalAssetLiabilityReturnRatesPair() {
      System.out.println("Testing UncollateralizedReturnMaximizer (unequal return rate pair)..");
      
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(new Pair<Double, Double>(-1.e-3, 0.2));
      knownLiabilityRates.add(new Pair<Double, Double>(1.e-3, 0.1));
      
      final double[] expectedResult = { 0.3, 0.3 };
      
      LCQPUncollateralizedReturnMaximizer optimizer = 
         new LCQPUncollateralizedReturnMaximizer();
      
      final List<Double> result = optimizer.performOptimization(
         knownAssetRates,
         knownLiabilityRates,
         0.,
         0.1 * 3.0
         );
      
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         result.get(0), result.get(1),
         expectedResult[0], expectedResult[1]
         );
      
      Assert.assertTrue(Math.abs(result.get(0) - expectedResult[0]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(1) - expectedResult[1]) < 1.e-5);
      
      System.out.println("Pass.");
   }
   
   /** 
     * The following is a Mathematica-style code snippet to perform the 
     * optimization and output the expected test results.
     *
       
       rA[a_, r_, dr_] := dr*a + r
       rL[l_, r_, dr_] :=  dr*l + r
       
       \[Lambda] = 3
       Eq = 1/10
       
       NMaximize[{
         + rA[a1, 99/100, - 99/100] * a1 
         + rA[a2, 105/100, -105/100] * a2
         - rL[l1, 90/100, 90/100] * l1 
         - rL[l2, 95/100, 95/100] * l2,
        a1 >= 0,
        a2 >= 0,
        l1 >= 0,
        l2 >= 0,
        a1 + a2 <= \[Lambda]*Eq,
        a1 + a2 - l1 - l2 == 0
        },
       {a1, a2, l1, l2},
       WorkingPrecision -> 40
       ]
       
     *
     */
   @Test
   public void testTwoAssetsAndTwoLiabilities() {
      System.out.println(
         "Testing UncollateralizedReturnMaximizer (two assets, two liabilities)..");
      
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(new Pair<Double, Double>(-99./100., 99./100.));
      knownAssetRates.add(new Pair<Double, Double>(-105./100., 105./100.));
      knownLiabilityRates.add(new Pair<Double, Double>(90./100., 90./100.));
      knownLiabilityRates.add(new Pair<Double, Double>(95./100, 95./100));
      
      final double[] expectedResult = { 
         0.0103994110068,
         0.0383765875207,
         0.0385606478925,
         0.0102153506350
         };
      
      LCQPUncollateralizedReturnMaximizer optimizer = 
         new LCQPUncollateralizedReturnMaximizer();
      
      final List<Double> result = optimizer.performOptimization(
         knownAssetRates,
         knownLiabilityRates,
         0.,
         0.1 * 3.0
         );
      
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         result.get(0), result.get(2),
         expectedResult[0], expectedResult[0]
         );
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         result.get(1), result.get(3),
         expectedResult[1], expectedResult[3]
         );
      
      Assert.assertTrue(Math.abs(result.get(0) - expectedResult[0]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(1) - expectedResult[1]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(2) - expectedResult[2]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(3) - expectedResult[3]) < 1.e-5);
      
      System.out.println("Pass.");
   }
   
   /**
     * Test the LCQP algorithm with a portfolio consisting of:<br><br>
     * 
     *  (a) two assets, with different return rates and different market impacts;<br>
     *  (b) one liability, with nonzero interest rate and market impact.<br>
     * 
     * The expected optimum has been computed manually.
     */
   @Test
   public void testPolynomialTimeSolverSpecialCase() {
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(Pair.create(-1./7., 1./3.));
      knownAssetRates.add(Pair.create(-1./9., 1./5.));
      knownLiabilityRates.add(Pair.create(1./13.,  1./11.));
      
      DominoUncollateralizedReturnMaximizer optimizer = 
         new DominoUncollateralizedReturnMaximizer();
      
      final List<Double> PolySolution =
         optimizer.performOptimization(
            knownAssetRates, knownLiabilityRates,
            0., 11.);
      
      final double
         expectedA0 = 2513./4785.,
         expectedA1 = 24./319.,
         expectedL0 = 2873./4785.;
      
      System.out.printf(
         "poly solution: a[0]: %16.10g a[1]:%16.10g l:%16.10g\n" +
         "expected:      a[0]: %16.10g a[1]:%16.10g l:%16.10g\n" +
         "diff.:         a[0]: %16.10g a[1]:%16.10g l:%16.10g\n",
         PolySolution.get(0), PolySolution.get(1), PolySolution.get(2),
         expectedA0, expectedA1, expectedL0,
         PolySolution.get(0) - expectedA0,
         PolySolution.get(1) - expectedA1,
         PolySolution.get(2) - expectedL0
         );
      
      Assert.assertEquals(expectedA0, PolySolution.get(0), 1.e-10);
      Assert.assertEquals(expectedA1, PolySolution.get(1), 1.e-10);
      Assert.assertEquals(expectedL0, PolySolution.get(2), 1.e-10);
   }
   
   /**
     * Test an alternative portfolio maximization algorithm with polynomial
     * time performance and complexity. This unit test accepts a seed, and,
     * using this seed, generates a random portfolio maximization problem of
     * dimension between 2 and 50. The initial cash of the portfolio investor
     * is selected randomly, as is the capital constraint. The resulting 
     * solution is compared to the result of the LCQP profit maximization
     * algorithm.
     */
   public void testPolynomialTimeSolver(long seed) {
      
      Random dice = new Random(seed);
      
      final class ActualReturn {
         double get(Pair<Double, Double> rates, double investment) {
            return (rates.getFirst() * investment + rates.getSecond()) * investment;
         }
      }
      
      final ActualReturn actualReturn = new ActualReturn();
      
      final double
         scale = Math.pow(10., dice.nextDouble() * 10.);
      
      final int
         numAssetTypes = dice.nextInt(18) + 1,
         numLiabilityTypes = dice.nextInt(18) + 1;
      
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      for(int i = 0; i< numAssetTypes; ++i) {
         knownAssetRates.add(
            Pair.create(-dice.nextDouble() / scale, dice.nextDouble()));
      }
      for(int j = 0; j< numLiabilityTypes; ++j) {
         knownLiabilityRates.add(
            Pair.create(dice.nextDouble() / scale, dice.nextDouble()));
      }
      
      System.out.printf(
         "problem statement: \n");
      for(int i = 0; i< knownAssetRates.size(); ++i) {
         System.out.printf(
            "asset     %2d: alpha: %16.10g const.:%16.10g\n",
            i, knownAssetRates.get(i).getFirst(), knownAssetRates.get(i).getSecond()
            );
      }
      for(int j = 0; j< knownLiabilityRates.size(); ++j) {
         System.out.printf(
            "liability %2d: alpha: %16.10g const.:%16.10g\n",
            j, knownLiabilityRates.get(j).getFirst(), knownLiabilityRates.get(j).getSecond()
            );
      }
      
      final double
         capitalConstraint = scale * dice.nextDouble() * 20.,
         cashToSpend = scale * dice.nextDouble() * .1;
      
      final long startTime = System.nanoTime();
      
      System.out.printf("Computing LCQP solution..\n");
      
      final List<Double> LCQPSolution =
         (new LCQPUncollateralizedReturnMaximizer()).performOptimization(
            knownAssetRates, knownLiabilityRates, cashToSpend, capitalConstraint);
      
      final long LCQPEndTime = System.nanoTime();
      
      System.out.printf("Computing Poly solution..\n");
      
      final List<Double> PolySolution =
         (new DominoUncollateralizedReturnMaximizer()).
            performOptimization(
               knownAssetRates, knownLiabilityRates, cashToSpend, capitalConstraint);
      
      final long PolyEndTime = System.nanoTime();
      
      final double
         lcqpTimeTaken = (double)(LCQPEndTime - startTime),
         polyTimeTaken = (double)(PolyEndTime - LCQPEndTime);
      System.out.printf(
         "Nanotime taken:\n"
       + "LCQP: %16.10g\n"
       + "Poly: %16.10g (%16.10g%%)\n",
         lcqpTimeTaken,
         polyTimeTaken,
         1.e2 * polyTimeTaken / lcqpTimeTaken
         );
      
      double
         lcqpReturn = 0.,
         polyReturn = 0.;
      for(int i = 0; i< knownAssetRates.size(); ++i) {
         lcqpReturn += actualReturn.get(
            knownAssetRates.get(i), LCQPSolution.get(i));
         polyReturn += actualReturn.get(
            knownAssetRates.get(i), PolySolution.get(i));
      }
      for(int j = 0; j< knownLiabilityRates.size(); ++j) {
         lcqpReturn -= actualReturn.get(
            knownLiabilityRates.get(j), LCQPSolution.get(j + numAssetTypes));
         polyReturn -= actualReturn.get(
            knownLiabilityRates.get(j), PolySolution.get(j + numAssetTypes));
      }
      System.out.printf(
         "LCQP return: %16.10g Poly return: %16.10g\n",
         lcqpReturn, polyReturn
         );
      
      boolean success = true;
      
      double
         capitalSumLCQP = 0.,
         capitalSumPoly = 0.,
         equityLCQP = 0.,
         equityPoly = 0.;
      
      for(int i = 0; i< numAssetTypes; ++i) {
         double
            ratio = PolySolution.get(i) / LCQPSolution.get(i);
         System.out.printf(
            "asset:     inst.: lcqp: %16.10g poly: %16.10g ratio: %16.10g\n",
            LCQPSolution.get(i), PolySolution.get(i), ratio);
         capitalSumLCQP += LCQPSolution.get(i);
         capitalSumPoly += PolySolution.get(i);
         
         success = success && (
            (PolySolution.get(i) == 0. && Math.abs(LCQPSolution.get(i)) < 1.e-3) ||
            Math.abs(ratio - 1.) / 1. < 1.e-4 ||
            lcqpReturn < polyReturn
            );
      }
      equityLCQP = capitalSumLCQP;
      equityPoly = capitalSumPoly;
      for(int j = 0; j< numLiabilityTypes; ++j) {
         int
            index = j + numAssetTypes;
         double
            ratio = PolySolution.get(index) / LCQPSolution.get(index);
         System.out.printf(
            "liability: inst.: lcqp: %16.10g poly: %16.10g ratio: %16.10g\n",
            LCQPSolution.get(index), PolySolution.get(index), ratio
            );
         equityLCQP -= LCQPSolution.get(index);
         equityPoly -= PolySolution.get(index);
         
         success = success && (
            (PolySolution.get(index) <= 1.e-10 && Math.abs(LCQPSolution.get(index)) < 1.e-3) ||
            Math.abs(ratio - 1.) / 1. < 1.e-4 ||
            lcqpReturn < polyReturn
            );
      }
      
      System.out.printf(
         "LCQP result: equity change: %16.10g cc.:%16.10g\n" +
         "Poly result: equity change: %16.10g cc.:%16.10g\n",
         equityLCQP, capitalSumLCQP,
         equityPoly, capitalSumPoly
         );
      
      Assert.assertTrue(success);
   }
   
   /**
     * Test the LCQP portfolio profit maximization algorithm using two
     * distinct asset types and two distinct liability types. In this 
     * example, one asset provides zero returns and both liabilities have
     * interest rate zero. The optimal investment solution is expected to
     * be distributed equally between both liability types, entirely 
     * avoiding asset type 2 (which is profitless). 
     */
   public void testTwoAssetsAndTwoLiabilities2() {
      System.out.println(
         "Testing UncollateralizedReturnMaximizer (2) (two assets, two liabilities)..");
      
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(new Pair<Double, Double>(-1.e-3, 2.0));
      knownAssetRates.add(new Pair<Double, Double>(-1.e-3, 0.0));
      knownLiabilityRates.add(new Pair<Double, Double>(1.e-3, 0.0));
      knownLiabilityRates.add(new Pair<Double, Double>(1.e-3, 0.0));
      
      final double[] expectedResult = { 
         1.0,
         0.0,
         0.5,
         0.5
         };
      
      final List<Double> result =
         (new LCQPUncollateralizedReturnMaximizer()).performOptimization(
            knownAssetRates,
            knownLiabilityRates,
            0.,
            1.0
            );
      
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         result.get(0), result.get(2),
         expectedResult[0], expectedResult[2]
         );
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         result.get(1), result.get(3),
         expectedResult[1], expectedResult[3]
         );
      
      Assert.assertTrue(Math.abs(result.get(0) - expectedResult[0]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(1) - expectedResult[1]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(2) - expectedResult[2]) < 1.e-5);
      Assert.assertTrue(Math.abs(result.get(3) - expectedResult[3]) < 1.e-5);
      
      System.out.println("Pass.");
   }
   
   /**
     * A unit test for the LCQP optimization function when including a 
     * cash reserve (with zero return rate and zero risk). This unit 
     * test creates three differentiated assets (commercial loans with
     * distinct interest rates and market impact) and one liability
     * (a bond resource with zero market impact). The starting cash
     * reserve of the caller is taken to be 4.0 units.<br><br>
     * 
     * The following Mathematica-like code snippet reproduces the 
     * results of this test:<br>
     * 
     * rC1[x_] := (1/10) / 3 - (10^-2)*x<br>
     * rC2[x_] := (11/100) / 3 - (2*10^-2)*x<br>
     * rC3[x_] := (12/100) / 3 - (4 * 10^-2) * x<br>
     * rB[x_] := (1/100)<br>
     * 
     * Optimization[rC1_, rC2_, rC3_, rB_, \[Lambda]_, Eq_, IC_] := Module[{},<br>
     *  N[NMaximize[{<br>
     *      rC1[C1] * C1 + rC2[C2] * C2 + rC3[C3] * C3 - rB[B] * B,<br>
     *      C1 >= 0,<br>
     *      C2 >= 0,<br>
     *      C3 >= 0,<br>
     *      B >= 0,<br>
     *      Cash >= -IC,<br>
     *      C1 + C2 + C3 + (Cash + IC) <= \[Lambda]*Eq,<br>
     *      C1 + C2 + C3 + Cash - B == 0<br>
     *      },<br>
     *     {C1, C2, C3, B, Cash},<br>
     *     WorkingPrecision -> 100<br>
     *     ], 20][[2]]v
     *  ]<br>
     */
   @Test
   public void testLCQPWithCashReserve() {
      System.out.println(
            "Testing UncollateralizedReturnMaximizer (3) (testLCWPWithCashReserve)..");
         
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(new Pair<Double, Double>(-1.e-2, (1./10.) / 3.));
      knownAssetRates.add(new Pair<Double, Double>(-2.e-2, (11./100.) / 3.));
      knownAssetRates.add(new Pair<Double, Double>(-4.e-2, (12./100.) / 3.));
      knownLiabilityRates.add(new Pair<Double, Double>(0., 1./100));
      
      final double
         equity = 1.0,
         lambda = 5.0,
         initialCash = 4.0;
      
      final double[] expectedResult = { 
         1.666666666666666666,
         0.916666666666666666,
         0.500000000000000000,
         0.0
         };
      
      final List<Double> result = 
         (new LCQPUncollateralizedReturnMaximizer()).performOptimization(
            knownAssetRates,
            knownLiabilityRates,
            initialCash,
            equity * lambda
            );
      
      final List<Double> resultUsingPolySolver = 
         (new DominoUncollateralizedReturnMaximizer()).performOptimization(
            knownAssetRates,
            knownLiabilityRates,
            initialCash,
            equity * lambda
            );
      
      Assert.assertTrue(result.size() == expectedResult.length);
      
      for(int i = 0; i< expectedResult.length; ++i) {
         System.out.printf(
            "gained:   %16.10g expected: %16.10g\n",
            result.get(i), expectedResult[i]
            );
         Assert.assertTrue(Math.abs(result.get(i) - expectedResult[i]) < 1.e-5);
         Assert.assertTrue(Math.abs(resultUsingPolySolver.get(i) - expectedResult[i]) < 1.e-12);
      }
      
      System.out.println("testLCWPWithCashReserve Pass.");
   }
   
   /**
     * Test the LCQP portfolio profit maximization algorithm against a
     * known failure case. The LCQP algorithm uses adaptive precision and
     * accuracy goals in an attempt to compute the optimal investment 
     * even for numerically difficult problems. The expected optimal
     * solution for this problem has been computed manually.
     */
   @Test
   public void testLCQPWithKnownFailureCase() {
      System.out.println(
            "Testing UncollateralizedReturnMaximizer (4) (testLCWPWithKnownFailureCase)..");
         
      final List<Pair<Double, Double>>
         knownAssetRates = new ArrayList<Pair<Double,Double>>(),
         knownLiabilityRates = new ArrayList<Pair<Double,Double>>();
      knownAssetRates.add(new Pair<Double, Double>(-1.0E-10, 0.06993087461386047));
      knownAssetRates.add(new Pair<Double, Double>(-1.0E-10, 0.23508528896142042));
      knownAssetRates.add(new Pair<Double, Double>(-1.0E-10, 1.0E-6));
      knownLiabilityRates.add(new Pair<Double, Double>(0., 1.0E-6));
      
      final double
         equity = 8.257849119535892E8,
         lambda = 1.0,
         initialCash = 8.175270628340533E8;
      
      try {
         final List<Double> result =
            (new LCQPUncollateralizedReturnMaximizer()).performOptimization(
               knownAssetRates,
               knownLiabilityRates,
               initialCash,
               equity * lambda
               );
         StateVerifier.checkNotNull(result);
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      System.out.println("testLCWPWithKnownFailureCase Pass.");
   }
   
   @Test
   /**
     * A unit test for the LCQP Market Response Function (MRF) wrapper.
     * The LCQP MRF adapter wraps calls to the portfolio optimisation
     * algorithm (the LCQP) in a structure utilised by the Mixed Clearing
     * Network. It is expected that calls to the adapter should return
     * the same results as direct calls to the LCQP.
     */
   public void testMarketResponseFunctionAdapter() {
      System.out.println(
         "Testing UncollateralizedReturnMaximizer (two assets, two liabilities, MRF adapter)..");
      
      final double
         equity = 0.1,
         lambda = 3.0;
      
      final Set<String>
         assetTypes = new HashSet<String>(),
         liabilityTypes = new HashSet<String>();
      assetTypes.add("Asset Type 1");
      assetTypes.add("Asset Type 2");
      liabilityTypes.add("Liability Type 1");
      liabilityTypes.add("Liability Type 2");
      
      final Map<String, Pair<Double, Double>>
         assetReturnRates = new HashMap<String, Pair<Double, Double>>(),
         liabilityReturnRates = new HashMap<String, Pair<Double, Double>>();
      assetReturnRates.put("Asset Type 1", Pair.create(1.0, -99./100.));
      assetReturnRates.put("Asset Type 2", Pair.create(1.0, -105./100.));
      liabilityReturnRates.put("Liability Type 1", Pair.create(1.0, 90./100.));
      liabilityReturnRates.put("Liability Type 2", Pair.create(1.0, 95./100));
      
      final double[] expectedResult = { 
         0.0103994110068,
         0.0383765875207,
         0.0385606478925,
         0.0102153506350
         };
      
      MarketResponseFunction marketResponseFunction = 
         UncollateralizedReturnMaximizerMRFAdapater.create(
             equity,
             lambda,
             0.,
             assetTypes,
             liabilityTypes,
             assetReturnRates,
             liabilityReturnRates
             );
      
      final double[] result =
         marketResponseFunction.getValue(
            new int[] {0, 1, 2, 3}, 
            new TradeOpportunity[] {
               TradeOpportunity.create(
                  99./100., new ClearingInstrument("Mock Market", "Asset Type 1"), ""),
               TradeOpportunity.create(
                  105./100., new ClearingInstrument("Mock Market", "Asset Type 2"), ""),
               TradeOpportunity.create(
                  90./100., new ClearingInstrument("Mock Market", "Liability Type 1"), ""),
               TradeOpportunity.create(
                  95./100., new ClearingInstrument("Mock Market", "Liability Type 2"), "")
               }
            );
      
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         -result[0], result[2],
         expectedResult[0], expectedResult[2]
         );
      System.out.printf(
         "gained:   asset investment: %16.10g liabilities: %16.10g\n" + 
         "expected: asset investment: %16.10g liabilities: %16.10g\n",
         -result[1], result[3],
         expectedResult[1], expectedResult[3]
         );
      
      Assert.assertTrue(Math.abs(-result[0] - expectedResult[0]) < 1.e-5);
      Assert.assertTrue(Math.abs(-result[1] - expectedResult[1]) < 1.e-5);
      Assert.assertTrue(Math.abs(result[2] - expectedResult[2]) < 1.e-5);
      Assert.assertTrue(Math.abs(result[3] - expectedResult[3]) < 1.e-5);
      
      System.out.println("Pass.");
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("UncollateralizedReturnMaximizer (MRF adapter) tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   public static void main(final String[] args) {
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testEqualAssetLiabilityReturnRates();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testUnequalAssetLiabilityReturnRatesPair();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testTwoAssetsAndTwoLiabilities();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testTwoAssetsAndTwoLiabilities2();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testMarketResponseFunctionAdapter();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testLCQPWithCashReserve();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         test.testPolynomialTimeSolverSpecialCase();
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
      
      try {
         final UncollateralizedReturnMaximizerTest test =
            new UncollateralizedReturnMaximizerTest();
         test.setUp();
         final int
            stressTestLength = 1000;
         for(int i = 0; i< stressTestLength; ++i) {
            System.out.printf("Comparing solvers (test %d/%d):\n", i + 1, stressTestLength);
            test.testPolynomialTimeSolver((long) i);
         }
         test.tearDown();
      }
      catch(final Exception failureCase) {
         Assert.fail();
      }
   }
}
