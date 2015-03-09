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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MapUtil {
   /**
     * Cast a {@link Map}{@code <A, B>} to a {@link Map}{@code <K, V>}. It is 
     * assumed that both casts (K) A and (V) B are legal. If the argument is
     * {@code null}, this method returns {@code null}.
     */
   @SuppressWarnings("unchecked")
   public static <K, V, A, B> Map<K, V> castMap(Map<A, B> input) {
      if(input == null) return null;
      final Map<K, V>
         result = new HashMap<K, V>();
      for(final Entry<A, B> record : input.entrySet())
         result.put((K) record.getKey(), (V) record.getValue());
      return result;
   }
}
