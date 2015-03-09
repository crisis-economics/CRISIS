/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tamás Máhr
 *
 */
public class ArrayUtil {

	public interface Function {
		public double apply(double value);
	}
	
   /**
     * Convert a boxed Double array to a primitive array.
     */
   public static double[] toPrimitive(final Double[] boxedArray) {
      if (boxedArray == null)
         return null;
      else if (boxedArray.length == 0)
         return new double[0];
      else {
         final double[] result = new double[boxedArray.length];
         for (int i = 0; i < boxedArray.length; ++i)
            result[i] = boxedArray[i].doubleValue();
         return result;
      }
   }
   
   public static double[] toPrimitive(final List<Double> list) {
      return toPrimitive(list.toArray(new Double[0]));
   }
   
   /**
     * Convert a list of type List<E> to List<T> if the cast is 
     * acceptable for all elements of the input list. This 
     * operation yields an unmodifiable list copy of the input.
     */
   @SuppressWarnings("unchecked")
   public static <T, E> List<T> castList(final List<E> original) {
      List<T> result = new ArrayList<T>();
      for(E record : original) result.add((T)record);
      return Collections.unmodifiableList(result);
   }
   
   /**
     * Convert a map to a list of pairs.
     */
   public static <T1, T2> List<Pair<T1, T2>> convertMapToPairList(final Map<T1, T2> map) {
      List<Pair<T1, T2>> result = new ArrayList<Pair<T1, T2>>();
      for(Entry<T1, T2> record : map.entrySet())
         result.add(Pair.create(record.getKey(), record.getValue()));
      return result;
   }
   
   /**
     * Generate a double[] of the specified length for which every record has value 1.0.
     */
   public static double[] ones(final int arrayLength) {
      double[] result = new double[arrayLength];
      Arrays.fill(result, 1.0);
      return result;
   }
   
   /**
     * Sum the contents of a {@link Double} {@link Collection}.
     * 
     * @param data
     *        The {@link Collection} to sum.
     */
   public double sumOf(final Collection<Double> data) {
      final Iterator<Double> p = data.iterator();
      double result = p.next();
      while(p.hasNext())
         result += p.next();
      return result;
   }

	/**
	 * Transforms the given array by applying the given function at each index of the array. Note that the value returned by the function is stored in
	 * the same array, that is this method modifies its argument.
	 * 
	 * @param array the array to transform
	 * @param function the function to apply
	 */
	public static void map(final double[] array, final Function function){
		for (int i = 0; i < array.length; i++) {
			array[i] = function.apply(array[i]);
		}
	}
	
	public static double dotProduct(final double[] anArray, final double[] anotherArray){
		double product = 0;
		
		for (int i = 0; i < anotherArray.length; i++) {
			product += anArray[i] * anotherArray[i];
		}
		
		return product;
	}
}
