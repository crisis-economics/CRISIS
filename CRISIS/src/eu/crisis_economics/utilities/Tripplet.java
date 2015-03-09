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

import com.google.common.base.Preconditions;

/**
  * A lightweight, mutable, generic tripplet class with a zip function.
  * @author phillips
  */
public final class Tripplet<T1, T2, T3> {
   private T1 first;
   private T2 second;
   private T3 third;
   
   public Tripplet(
      final T1 first, 
      final T2 second,
      final T3 third
      ) {
      this.first = first;
      this.second = second;
      this.third = third;
   }
   
   static public <T1, T2, T3> Tripplet<T1, T2, T3> create(T1 first, T2 second, T3 third) {
      return new Tripplet<T1, T2, T3>(first, second, third);
   }
   
   static public <T1, T2, T3> Tripplet<T1, T2, T3> create(Tripplet<T1, T2, T3> other) {
      return new Tripplet<T1, T2, T3>(other.first, other.second, other.third);
   }
   
   public T1 getFirst() {
      return first;
   }
   
   public T2 getSecond() {
      return second;
   }
   
   public T3 getThird() {
      return third;
   }
   
   public void setFirst(final T1 value) {
      this.first = value;
   }
   
   public void setSecond(final T2 value) {
      this.second = value;
   }
   
   public void setThird(final T3 value) {
      this.third = value;
   }
   
   /**
     * Form a zipped array of tripplets from three arrays of singletons.
     * All arguments must be:
     *    (a) non-null and of the same length; or
     *    (b) all null, in which case null is returned.
     */
   public static <T1, T2, T3> List<Tripplet<T1, T2, T3>> zip(
      final List<T1> first,
      final List<T2> second,
      final List<T3> third
      ) {
      if(first == null && second == null && third == null)
         return null;
      Preconditions.checkNotNull(first);
      Preconditions.checkNotNull(second);
      Preconditions.checkNotNull(third);
      Preconditions.checkArgument(second.size() == third.size() && second.size() == first.size());
      final List<Tripplet<T1, T2, T3>>
         result = new ArrayList<Tripplet<T1, T2, T3>>();
      for(int i = 0; i< first.size(); ++i)
         result.add(Tripplet.create(first.get(i), second.get(i), third.get(i)));
      return result;
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((first == null) ? 0 : first.hashCode());
      result = prime * result + ((second == null) ? 0 : second.hashCode());
      result = prime * result + ((third == null) ? 0 : third.hashCode());
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
      Tripplet<?, ?, ?> other = (Tripplet<?, ?, ?>) obj;
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
      if (third == null) {
         if (other.third != null)
            return false;
      } else if (!third.equals(other.third))
         return false;
      return true;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Tripplet: first: [" + first.getClass() + "] = " + first + ", "
            + " second: [" + second.getClass() + "] = " + second + ", "
            + " third: [" + third.getClass() + "] = " + third + ".";
   }
}