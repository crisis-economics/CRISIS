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
package eu.crisis_economics.utilities;

import java.util.ArrayList;
import java.util.List;

/**
  * @author phillips
  */
public class Pair<T1, T2> {
   private T1 first;
   private T2 second;
   
   public Pair(
      final T1 first, 
      final T2 second
      ) {
      this.first = first;
      this.second = second;
   }
   
   static public <T1, T2> Pair<T1, T2> create(T1 first, T2 second) {
      return new Pair<T1, T2>(first, second);
   }
   
   static public <T1, T2> Pair<T1, T2> create(Pair<T1, T2> other) {
      return new Pair<T1, T2>(other.first, other.second);
   }
   
   public T1 getFirst() {
      return first;
   }
   
   public T2 getSecond() {
      return second;
   }
   
   public void setFirst(final T1 value) {
      this.first = value;
   }
   
   public void setSecond(final T2 value) {
      this.second = value;
   }
   
   /**
     * Form a zipped array of pairs from two arrays of singletons.
     * Arguments List<T1> first and List<T2> second must have the
     * same length.
     */
   public static <T1, T2> List<Pair<T1, T2>> zip(
      final List<T1> first,
      final List<T2> second
      ) {
      if(first.size() != second.size())
         throw new IllegalArgumentException(
            "Pair.zip: argument lists must have the same length.");
      final List<Pair<T1, T2>> result = new ArrayList<Pair<T1, T2>>();
      for(int i = 0; i< first.size(); ++i)
         result.add(Pair.create(first.get(i), second.get(i)));
      return result;
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((first == null) ? 0 : first.hashCode());
      result = prime * result + ((second == null) ? 0 : second.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      @SuppressWarnings("unchecked")
      Pair<T1, T2> other = (Pair<T1, T2>) obj;
      if (first == null) {
         if (other.first != null)
            return false;
      } else if (!first.equals(other.first))
         return false;
      if (second == null) {
         if (other.second != null)
            return false;
      } else if (!second.equals(other.second))
         return false;
      return true;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return
         "Pair, first: [" + first.getClass() + "] " + first + ", " +
         "second: [" + second.getClass() + "]" + second + ".";
   }
}