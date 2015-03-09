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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

import eu.crisis_economics.utilities.Pair;

/**
  * A (composite) typedef for NavigableMap<Double, Double>.
  * 
  * @author phillips
  */
public final class DiscreteTimeSeries implements NavigableMap<Double, Double> {
   
   private NavigableMap<Double, Double>
      treeMap;
   
   /**
     * Create an empty {@link DiscreteTimeSeries} object.
     */
   public DiscreteTimeSeries() {
      this.treeMap = new TreeMap<Double, Double>();
   }
   
   /**
     * Create a {@link DiscreteTimeSeries} object by copying records
     * from an existing collection. The argument is unchanged by this
     * operation.
     */
   public DiscreteTimeSeries(final Collection<? extends Pair<Double, Double>> data) {
      this();
      for(final Pair<Double, Double> record : data)
         treeMap.put(record.getFirst(), record.getSecond());
   }
   
   /**
     * Create a {@link DiscreteTimeSeries} object by copying records
     * from an existing {@link NavigableMap}. The argument is unchanged by this
     * operation, and no reference to the argument is kept.
     */
   public DiscreteTimeSeries(final NavigableMap<Double, Double> composite) {
      Preconditions.checkNotNull(composite);
      this.treeMap = new TreeMap<Double, Double>(composite);
   }
   
   /**
     * Create a {@link DiscreteTimeSeries} from an existing {@link NavigableMap}
     * object. The argument will be retained as a reference. Changes to the 
     * argument will be reflected in the resulting {@link DiscreteTimeSeries}.
     */
   public static DiscreteTimeSeries crateFromReference(
      final NavigableMap<Double, Double> composite) {
      final DiscreteTimeSeries result = new DiscreteTimeSeries();
      result.treeMap = composite;
      return result;
   }
   
   /**
     * Return a deep copy of this object. No references are kept to
     * the resulting object.
     */
   public DiscreteTimeSeries deepCopy() {
      final DiscreteTimeSeries copy = new DiscreteTimeSeries(treeMap);
      return copy;
   }
   
   public java.util.Map.Entry<Double, Double> ceilingEntry(Double key) {
      return treeMap.ceilingEntry(key);
   }
   
   public Double ceilingKey(Double key) {
      return treeMap.ceilingKey(key);
   }
   
   public void clear() {
      treeMap.clear();
   }
   
   public Comparator<? super Double> comparator() {
      return treeMap.comparator();
   }
   
   public boolean containsKey(Object key) {
      return treeMap.containsKey(key);
   }
   
   public boolean containsValue(Object value) {
      return treeMap.containsValue(value);
   }
   
   public NavigableSet<Double> descendingKeySet() {
      return treeMap.descendingKeySet();
   }
   
   public NavigableMap<Double, Double> descendingMap() {
      return treeMap.descendingMap();
   }
   
   public Set<java.util.Map.Entry<Double, Double>> entrySet() {
      return treeMap.entrySet();
   }
   
   public boolean equals(Object o) {
      return treeMap.equals(o);
   }
   
   public java.util.Map.Entry<Double, Double> firstEntry() {
      return treeMap.firstEntry();
   }
   
   public Double firstKey() {
      return treeMap.firstKey();
   }
   
   public java.util.Map.Entry<Double, Double> floorEntry(Double key) {
      return treeMap.floorEntry(key);
   }
   
   public Double floorKey(Double key) {
      return treeMap.floorKey(key);
   }
   
   public Double get(Object key) {
      return treeMap.get(key);
   }
   
   public int hashCode() {
      return treeMap.hashCode();
   }
   
   public DiscreteTimeSeries headMap(Double toKey, boolean inclusive) {
      return crateFromReference(treeMap.headMap(toKey, inclusive));
   }
   
   public DiscreteTimeSeries headMap(Double toKey) {
      return crateFromReference(headMap(toKey, false));
   }
   
   public java.util.Map.Entry<Double, Double> higherEntry(Double key) {
      return treeMap.higherEntry(key);
   }
   
   public Double higherKey(Double key) {
      return treeMap.higherKey(key);
   }
   
   public boolean isEmpty() {
      return treeMap.isEmpty();
   }
   
   public Set<Double> keySet() {
      return treeMap.keySet();
   }
   
   public java.util.Map.Entry<Double, Double> lastEntry() {
      return treeMap.lastEntry();
   }
   
   public Double lastKey() {
      return treeMap.lastKey();
   }
   
   public java.util.Map.Entry<Double, Double> lowerEntry(Double key) {
      return treeMap.lowerEntry(key);
   }
   
   public Double lowerKey(Double key) {
      return treeMap.lowerKey(key);
   }
   
   public NavigableSet<Double> navigableKeySet() {
      return treeMap.navigableKeySet();
   }
   
   public java.util.Map.Entry<Double, Double> pollFirstEntry() {
      return treeMap.pollFirstEntry();
   }
   
   public java.util.Map.Entry<Double, Double> pollLastEntry() {
      return treeMap.pollLastEntry();
   }
   
   public Double put(Double key, Double value) {
      return treeMap.put(key, value);
   }
   
   public void put(final Pair<Double, Double> record) {
      treeMap.put(record.getFirst(), record.getSecond());
   }
   
   public void putAll(Map<? extends Double, ? extends Double> m) {
      treeMap.putAll(m);
   }
   
   public Double remove(Object key) {
      return treeMap.remove(key);
   }
   
   public int size() {
      return treeMap.size();
   }
   
   public DiscreteTimeSeries subMap(
      final Double fromKey,
      final boolean fromInclusive,
      final Double toKey,
      final boolean toInclusive
      ) {
      return crateFromReference(treeMap.subMap(fromKey, fromInclusive, toKey, toInclusive));
   }
   
   public DiscreteTimeSeries subMap(Double fromKey, Double toKey) {
      return crateFromReference(subMap(fromKey, true, toKey, false));
   }
   
   public DiscreteTimeSeries tailMap(Double fromKey, boolean inclusive) {
      return crateFromReference(treeMap.tailMap(fromKey, inclusive));
   }
   
   public DiscreteTimeSeries tailMap(Double fromKey) {
      return crateFromReference(tailMap(fromKey, true));
   }
   
   public Collection<Double> values() {
      return treeMap.values();
   }
   
   /**
     * Get the time interval between the records with oldest and 
     * newest timestamps. If the timeseries is empty or contains 
     * one element, this method returns zero.
     */
   public double getInterval() {
      if(treeMap.isEmpty() || treeMap.size() == 1) return 0.;
      return treeMap.lastKey() - treeMap.firstKey();
   }
   
   /**
     * Returns a {@link List} view of the data known to this object. The
     * result is a deep copy, and no references to the return value are
     * kept by this object.
     */
   public List<Pair<Double, Double>> asList() {
      List<Pair<Double, Double>> result = new ArrayList<Pair<Double, Double>>();
      for(final Entry<Double, Double> record : entrySet())
         result.add(Pair.create(record.getKey(), record.getValue()));
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   public String toString() {
      return treeMap.toString();
   }
}
