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
package eu.crisis_economics.abm.algorithms.statistics;

import java.util.Map.Entry;

import com.google.common.base.Preconditions;

public final class VarianceOfTimeSeriesStatistic implements DiscreteTimeSeriesStatistic {
   
   public VarianceOfTimeSeriesStatistic() { } // Stateless
   
   @Override
   /**
     * Measure the variance of a timeseries, assuming linear 
     * interpolation of the samples.
     */
   public double measureStatistic(final DiscreteTimeSeries timeSeries) {
      Preconditions.checkNotNull(timeSeries);
      final int length = timeSeries.size();
      if(length == 1) return 0.;
      Entry<Double, Double>
         last = timeSeries.firstEntry(),
         next = last;
      double
         runningVariance = 0.;
      final double
         m = (new MeanOfTimeSeriesStatistic()).measureStatistic(timeSeries),
         m2 = m * m;
      int counter = 0;
      for(Entry<Double, Double> record : timeSeries.entrySet()) {
         last = next;
         next = record;
         if(counter++ == 0) continue;
         double
            yL = last.getValue(),
            yH = next.getValue(),
            xL = last.getKey(),
            xH = next.getKey();
         if(yL > yH) { double t = yH; yH = yL; yL = t; }
         runningVariance += 
            (m2 - m * yL + yL * yL / 3. - m * yH + (yL * yH) / 3. + yH * yH / 3.) * (xH - xL);
      }
      return runningVariance / timeSeries.getInterval();
   }
}
