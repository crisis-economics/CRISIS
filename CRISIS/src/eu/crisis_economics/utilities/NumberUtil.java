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
package eu.crisis_economics.utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public final class NumberUtil {
   
   /**
     * Increment a double by the smallest possible amount. This 
     * method converts the input number to a long integer and 
     * increments this long integer by one. The result is cast
     * from a long integer back to a double and returned.
     * @param value
     * @return
     */
   static public double incrementBySmallestAmount(final double value) {
      if(value >= 0.)
         return Double.longBitsToDouble(Double.doubleToLongBits(value) + 1);
      else
         return Double.longBitsToDouble(Double.doubleToLongBits(value) - 1);
   }
   
   /**
     * Decrement a double by the smallest possible amount. This 
     * method converts the input number to a long integer and 
     * decrements this long integer by one. The result is cast
     * from a long integer back to a double and returned.
     * @param value
     * @return
     */
   static public double decrementBySmallestAmount(final double value) {
      if(value >= 0.)
         return Double.longBitsToDouble(Double.doubleToLongBits(value) - 1);
      else
         return Double.longBitsToDouble(Double.doubleToLongBits(value) + 1);
   }
   
   /**
     * Clamp a {@link Double} inbetween a lower bound and an upper bound.
     * The behaviour of this method is equivalent to:<br><br>
     * 
     * return Math.min(Math.max(value, min), max).
     * 
     * @param min
     *        The lower bound above which to clamp. The value of this
     *        argument should be less than {@code max}.
     * @param max
     *        The upper bound above which to clamp. The value of this
     *        argument should be greater than {@code min}.
     * @param value
     *        The value to clamp.
     */
   static public double clamp(final double min, final double value, final double max) {
      return Math.min(Math.max(value, min), max);
   }
   
   /**
     * Compute the minimum of a list.
     * @param args
     *        A list of doubles, in which to find the minimum.
     */
   static public double min(Double... args) {
      double result = args[0];
      for(int i = 1; i< args.length; ++i)
         result = Math.min(result, args[i]);
      return result;
   }
   
   /**
    * Compute the maximum of a list.
    * @param args
    *        A list of doubles, in which to find the minimum.
    */
   static public double max(Double... args) {
      double result = args[0];
      for(int i = 1; i< args.length; ++i)
         result = Math.max(result, args[i]);
      return result;
   }
   
   /**
     * Compute the minimum of a map, returning a Pair containing
     * the key of the minimum and the value of the minimum. If 
     * the argument is an empty map, null is returned.
     */
   static public <T> Pair<T, Double> min(final Map<T, Double> map) {
      if(map.isEmpty()) return null;
      final Entry<T, Double> result = Collections.min(
         map.entrySet(), new Comparator<Entry<T, Double>>() {
            public int compare(
               final Entry<T, Double> first,
               final Entry<T, Double> second
               ) {
               return first.getValue().compareTo(second.getValue());
            }
         });
      return Pair.create(result.getKey(), result.getValue());
   }
   
   /**
    * Compute the maximum of a map, returning a Pair containing
    * the key of the maximum and the value of the maximum. If 
    * the argument is an empty map, null is returned.
    */
   static public <T> Pair<T, Double> max(final Map<T, Double> map) {
      if(map.isEmpty()) return null;
      final Entry<T, Double> result = Collections.max(
         map.entrySet(), new Comparator<Entry<T, Double>>() {
            public int compare(
               final Entry<T, Double> first,
               final Entry<T, Double> second
               ) {
               return first.getValue().compareTo(second.getValue());
            }
         });
      return Pair.create(result.getKey(), result.getValue());
   }
   
   /**
     * Perform a max/min operation on a signed double to ensure that
     * its modulus is not greater than the second argument.
     * 
     * @param value
     *        The value on which to operate.
     * @param maxAmplitude
     *        The maximum acceptable mod(value). This argument 
     *        should be non-negative.
     */
   static public double ampClamp(final double value, final double maxAmplitude) {
      if(value >= 0)
         return Math.min(value, maxAmplitude);
      else
         return Math.max(value, -maxAmplitude);
   }
}
