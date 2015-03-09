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

import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import eu.crisis_economics.utilities.Pair;

public final class MeanOfTimeSeriesStatistic implements DiscreteTimeSeriesStatistic {
   
   public MeanOfTimeSeriesStatistic() { } // Stateless
   
   @Override
   /**
     * Measure the mean of a timeseries, assuming linear 
     * interpolation of the samples.
     */
   public double measureStatistic(final DiscreteTimeSeries timeSeries) {
      if(timeSeries.size() == 0)
         return 0.;
      Preconditions.checkNotNull(timeSeries);
      final int length = timeSeries.size();
      Pair<Double, Double>
         last = Pair.create(
            timeSeries.firstKey(), timeSeries.get(timeSeries.firstKey()));
      if(length == 1) return last.getSecond();
      Pair<Double, Double> next = last;
      double
         runningMean = 0.;
      int counter = 0;
      for(final Entry<Double, Double> record : timeSeries.entrySet()) {
         last = next;
         next = Pair.create(record.getKey(), record.getValue());
         if(counter++ == 0) continue;
         double
            xL = last.getFirst(),
            xH = next.getFirst(),
            yL = last.getSecond(),
            yH = next.getSecond();
         runningMean += (yH + yL) * (xH - xL);
      }
      return runningMean * .5 / timeSeries.getInterval();
   }
}
