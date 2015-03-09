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
package eu.crisis_economics.abm.ratings;

import java.util.Map.Entry;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.algorithms.statistics.DiscreteTimeSeries;
import eu.crisis_economics.abm.algorithms.statistics.StdDevOfTimeSeriesStatistic;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/**
  * Compute the naive Merton 'Distance to Default' measure for an agent.
  * When applied to an agent, this measurement assumes the following:
  *   (a) that the agent is a stock releaser whose stocks are
  *       traded on the exchange, and
  *   (b) that the rating agency processing this algorithm also
  *       tracks the equity of the agent in a measurement called
  *       "Equity".
  * @author phillips
  */
public final class NaiveMertonDistanceToDefaultAlgorithm
   extends SimpleAbstactAgentOperation<Double> implements AgentTrackingMeasurement {
   
   private double forecastHorizon;
   
   public NaiveMertonDistanceToDefaultAlgorithm(final double forecastHorizon) {
      Preconditions.checkArgument(forecastHorizon > 0.);
      this.forecastHorizon = forecastHorizon;
   }
   
   /**
     * Compute the (Naive) Merton Distance-to-Default measure for an agent
     * in a specified forecast timeframe T.
     * 
     * @param debtFaceValue (F)
     *        The face value of the agent debt.
     * @param equityVolatility
     *        The volatility of agent equity.
     * @param equity (E)
     *        The current agent equity.
     * @param expectedAssetReturn
     *        The asset return of the agent during the last forecast window.
     * @param forecastHorizon (T)
     *        The period over which to forecast agent default.
     * @return
     *        A Pair<Double, Double> in the format:
     *        Pair<Naive Merton Distance-to-Default, Naive Merton
     *        Probability-of-Default>, in the period of the forecast timeframe.
     * 
     * It is not permissible for: both the debt face value (F) and equity (E)
     * aguments to be simultaneously zero; for the debt face value (F) to be
     * negative; or for the forecase horizon (T) to be zero or negative. If the 
     * debt face value is zero and equity is nonzero, then the distance to 
     * default is taken to be +Infinity.
     */
   static Pair<Double, Double> compute(
      final double debtFaceValue,
      final double equityVolatility,
      final double equity,
      final double expectedAssetReturn,
      final double forecastHorizon
      ) {
      Preconditions.checkArgument(equity != 0. || debtFaceValue > 0.);
      Preconditions.checkArgument(forecastHorizon > 0.);
      Preconditions.checkArgument(debtFaceValue >= 0.);
      final double
         debtVolatility = .05 + .25 * equityVolatility,
         overallValueVolatility =
            equityVolatility * equity / (equity + debtFaceValue) +
            debtVolatility * debtFaceValue / (equity + debtFaceValue);
      double
         distanceToDefault = Math.log((equity + debtFaceValue)/debtFaceValue) +
            (expectedAssetReturn - .5 * overallValueVolatility * overallValueVolatility) * 
               forecastHorizon;
      distanceToDefault /= Math.sqrt(forecastHorizon) * overallValueVolatility;
      NormalDistribution normalDist = new NormalDistribution();
      final double
         defaultProbability = normalDist.cumulativeProbability(-distanceToDefault);
      return Pair.create(distanceToDefault, defaultProbability);
   }
   
   @Override
   public Double operateOn(final Agent agent) {
      double
         debtFaceValue = 0.,
         equity = agent.getEquity(),
         equityVolatility = getEquityVolatility(agent),
         expectedAssetReturn = getExpectedAssetReturn(agent),
         forecastHorizon = this.forecastHorizon;
      for(final Contract contract : agent.getLiabilities())
         debtFaceValue += contract.getFaceValue();
      final double
         pDef = compute(
            debtFaceValue, equityVolatility, equity,
            expectedAssetReturn, forecastHorizon).getSecond();
      return pDef;
   }
   
   /**
     * Form a fundamentalist estimate of asset returns for the specified
     * agent. The asset return of the agent is estimated using fundamentalist
     * stock market returns in the last year (simulation time). For this 
     * reason, the agent must be stock releaser whose stocks are traded on
     * the stock exchange.
     * 
     * If stock market price per share/dividend per share information is not
     * available, or if this method is unable to estimate the stock market
     * return for the specified agent, the value 1.0 is returned.
     */
   private double getExpectedAssetReturn(final Agent agent) {
      if(!UniqueStockExchange.Instance.hasStock(agent.getUniqueName()))
         throw new IllegalArgumentException(
            "NaiveMertonDistanceToDefaultAlgorithm: agent must be a stock releaser, however the" 
          + "agent is not listed on the stock exhange.");
      final double
         timeOfInterest =
            Math.max(Simulation.getTime() - forecastHorizon, 0.);
      final DiscreteTimeSeries
         pricePerShareTimeSeries = 
            RatingAgency.Instance.getTimeSeries("Price Per Share", agent),
         dividendPerShareTimeSeries = 
            RatingAgency.Instance.getTimeSeries("Dividend Per Share", agent);
      if(pricePerShareTimeSeries.isEmpty() ||
         dividendPerShareTimeSeries.isEmpty())
         return 1.0;                                        // No information about asset returns.
      final Entry<Double, Double>
         ppsRecord = pricePerShareTimeSeries.ceilingEntry(timeOfInterest),
         dpsRecord = dividendPerShareTimeSeries.ceilingEntry(timeOfInterest);
      if(ppsRecord.getValue() == 0.)
         return 1.0;                                        // Stock market crashed.
      return dpsRecord.getValue() / ppsRecord.getValue();   // Fundamentalist return estimate.
   }
   
   /**
     * Get the standard deviation of agent equity in the last simulated
     * year. If records do not reach as far back as one year, all available
     * records are used.
     * @return
     */
   private double getEquityVolatility(final Agent agent) {
      final DiscreteTimeSeries series = RatingAgency.Instance.getTimeSeries("Equity", agent);
      if(series == null) return 0.;                         // No historical equity data.
      final double latestTimeOfInterest = 
         Math.max(Simulation.getTime() - forecastHorizon, 0.);
      DiscreteTimeSeries subseries = series.tailMap(latestTimeOfInterest, true);
      return (new StdDevOfTimeSeriesStatistic()).measureStatistic(subseries);
   }
   
   /**
     * Get the forecast horizon for this distance to default measurement.
     */
   public double getForecastHorizon() {
      return forecastHorizon;
   }
   
   /**
     * Set the forecast horizon for this distance to default measurement.
     * The forecast horizon must be strictly positive.
     */
   public void setForecastHorizon(double value) {
      Preconditions.checkArgument(value > 0.);
      this.forecastHorizon = value;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "NaiveMertonDistanceToDefaultAlgorithm, forecast horizon: "
            + forecastHorizon + ".";
   }
}
