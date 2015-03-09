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
package eu.crisis_economics.abm.algorithms.statistics;

import com.google.common.base.Preconditions;

public final class StdDevOfTimeSeriesStatistic implements DiscreteTimeSeriesStatistic {
   
   public StdDevOfTimeSeriesStatistic() { } // Stateless
   
   @Override
   /**
     * Measure the standard deviation of a timeseries, assuming
     * linear interpolation of the samples.
     */
   public double measureStatistic(final DiscreteTimeSeries timeSeries) {
      return Math.sqrt((new VarianceOfTimeSeriesStatistic()).measureStatistic(timeSeries));
   }
   
   /**
     * Compute the standard deviation of a timeseries (static method).
     * @param timeSeries
     *        The timeseries for which to compute the statistic.
     */
   static double computeFor(final DiscreteTimeSeries timeSeries) {
      Preconditions.checkNotNull(timeSeries);
      StdDevOfTimeSeriesStatistic statistic = new StdDevOfTimeSeriesStatistic();
      return statistic.measureStatistic(timeSeries);
   }
}
