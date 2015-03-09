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

import java.util.Map.Entry;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.DummyStockHolder;
import eu.crisis_economics.abm.algorithms.statistics.DiscreteTimeSeries;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.simulation.EmptySimulation;

public class MertonDistanceToDefaultTest {
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing MertonDistanceToDefaultTest..");
   }
   
   @Test(enabled = false) // TODO: enable following bankrupty merge.
   /**
     * Test the naive Merton 'probability of default' (PoD) measurement.
     * This test creates the following:
     *   (a) a clean simulation state,
     *   (b) one stock releasing bank (B), with 100 units of cash
     *       reserve, and one fixed contract liability of value 1.0,
     *   (c) one dummy stock holder (H) with exogenous cash holdings,
     *   (d) Equity and PoD tracking statistics for agent B.
     * The simulation runs for 10 complete cycles. In each cycle, the
     * stock releaser B pays 1.0 units of cash in dividends to H. During
     * each simulation timestep, the rating agency computes the naive 
     * Merton-PoD timeseries for agent B. At the end of the simulation,
     * this PoD timeseries is compared to predicted values.
     * 
     * Mathematica-style code to compute the naive-Merton-PoD for this
     * test:
     * 
     * (* Mean value of an ordered timeseries. *)
     * GetMeanEquity[x_] := (x[[1]] + x[[Length[x]]]) / 2
     * 
     * (* Std-Deviation of an ordered timeseries. *)
     * GetStdDevEquity[x_] := 
     *    Module[{},
     *       If[Length[x] <= 1, 0,
     *          Sqrt[Abs[Integrate[(f - GetMeanEquity[x])^2, {f, x[[1]], 
     *            x[[Length[x]]]}]/(x[[Length[x]]] - x[[1]])]]]
     *       ]
     * 
     * (* Cumulative Normal Distribution. *)
     * CumulativeNormalDistribution[x_] := CDF[NormalDistribution[0, 1]][x]
     * 
     * (* naive-Merton-Default-Probability algoritm: *)
     * GetNaiveDD[eq_, dps_, debt_, sprice_, timeframe_] :=
     * Module[{n, i, equitySample, D, r, equity, sigmaE, sigmaD, sigmaV, dD , pD, pDSeriesResult},
     *    n = Length[dps] - 1;
     *    pDSeriesResult = {};
     *    For[i = 1, i <= n, ++i,
     *      equitySample = eq[[2 ;; i]];
     *      equity = eq[[i + 1]];
     *      D = debt[[i]];
     *      r = dps[[i]] / sprice[[i]];
     *      sigmaE = GetStdDevEquity[equitySample];
     *      sigmaD = 5/100 + 25/100*sigmaE;
     *      sigmaV = equity/(equity + D)*sigmaE + D/(equity + D)*sigmaD;
     *      dD = ( Log[(equity + D)/D] + (r - 1/2*sigmaV^2)*timeframe)/(
     *      sigmaV * Sqrt[timeframe]);
     *      pD = CumulativeNormalDistribution[-dD];
     *      AppendTo[pDSeriesResult, pD];
     *      ];
     *   Return[N[pDSeriesResult, 30]]
     *   ]
     *   
     * (* Inputs: *)
     * EquitySeries = Range[100, 90, -1]
     * DividendPerShareSeries = Table[1, {i, 1, 11}]
     * StockPriceTimeSeries = Table[2, {i, 1, 11}]
     * DebtFaceValueTimeSeries = Table[1, {i, 1  , 11}]
     */
   public void testMertonDistanceToDefaultCalculation() {
      /**
        * Predicted naive Merton 'probability of default' measurements.
        */
      final double[] expectedNaiveMertonDefaultProbabilities =
         new double[] {
            0.,
            0.,                                                 // End of first cycle
            0.,                                                 // End of second cycle
            5.39371238975361344748724776234e-7,
            0.00333136880333204353793806031,
            0.07064027274127580526829527479,
            0.27506747856868464661531402407,
            0.53644300907473974609952450934,
            0.75018332485414252313134791013,
            0.88351084795184691900323456718
            };
      
      SimState simState = new EmptySimulation(0L);
      simState.start();
      DummyStockHolder stockHolder = new DummyStockHolder();
      StockReleasingBank bank = new StockReleasingBank(100.0);
      RatingAgency.Instance.addTrackingMeasurement(
         "Equity", new AgentEquityMeasurement());
      RatingAgency.Instance.addTrackingMeasurement(
         "Naive Merton Distance to Default",
             new NaiveMertonDistanceToDefaultAlgorithm(5.0));
      RatingAgency.Instance.trackAgent(bank);
      bank.setDividendPerShare(1.0);
      FixedValueContract exogenousLiability = new FixedValueContract();
      bank.addLiability(exogenousLiability);
      exogenousLiability.setValue(1.0);
      try {
         bank.releaseShares(1.0, 0.0, stockHolder);
      } catch (final InsufficientFundsException neverFails) {
         Assert.fail();
      }
      UniqueStockExchange.Instance.setStockPrice(bank.getUniqueName(), 2.0);
      int lastCycleIndex = 0;
      while(simState.schedule.getTime() < 10.) {
         while(simState.schedule.getTime() < lastCycleIndex + 1)
            simState.schedule.step(simState);
         ++lastCycleIndex;
      }
      final DiscreteTimeSeries
         mertonDefaultProbabilityTimeSeries = 
            RatingAgency.Instance.getTimeSeries("Naive Merton Distance to Default", bank);
      /*
       * Compare 'probability of default' measurements to predicted values.
       */
      {
      int counter = 0;
      for(final Entry<Double, Double> record : mertonDefaultProbabilityTimeSeries.entrySet()) {
         System.out.printf(
            "time: %16.10g value: %16.10g\n", record.getKey(), record.getValue());
         final double
            gained = record.getValue(),
            expected = expectedNaiveMertonDefaultProbabilities[counter++];
         if(expected == 0.) Assert.assertTrue(gained == 0.);
         else Assert.assertEquals(expected, gained, 1.e-8);
      }
      }
      simState.finish();
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("MertonDistanceToDefaultTest tests pass.");
      RatingAgency.Instance.flush();
   }
   
   /*
    * Manual entry point.
    */
   public static void main(String[] args) {
      MertonDistanceToDefaultTest test = new MertonDistanceToDefaultTest();
      test.setUp();
      test.testMertonDistanceToDefaultCalculation();
      test.tearDown();
   }
}
