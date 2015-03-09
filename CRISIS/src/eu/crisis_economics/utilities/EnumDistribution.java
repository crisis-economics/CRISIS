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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;

import eu.crisis_economics.abm.simulation.ScheduleIntervals;

/**
  * @author phillips
  */
public final class EnumDistribution<T extends Enum<T>> {
   private T[] values;
   private EnumMap<T, Double> probabilities;
   private IntegerDistribution dice;
   
   public static <T extends Enum<T>> EnumDistribution<T> // Immutable
      create(Class<T> token, String sourceFile) throws IOException {
      if(token == null)
         throw new NullArgumentException();
      if(!token.isEnum())
         throw new IllegalArgumentException(
            "EnumDistribution: " + token.getSimpleName() + " is not an enum.");
      if(token.getEnumConstants().length == 0)
         throw new IllegalArgumentException(
            "EnumDistribution: " + token.getSimpleName() + " is an empty enum.");
      EnumDistribution<T> result = new EnumDistribution<T>();
      result.values = token.getEnumConstants();
      result.probabilities = new EnumMap<T, Double>(token);
      Map<String, T> converter = new HashMap<String, T>();
      final int numberOfValues = result.values.length;
      int[] valueIndices = new int[numberOfValues];
      double[] valueProbabilities = new double[numberOfValues];
      BitSet valueIsComitted = new BitSet(numberOfValues);
      {
      int counter = 0;
      for(T value : result.values) {
         valueIndices[counter] = counter++;
         result.probabilities.put(value, 0.);
         converter.put(value.name(), value);
      }
      }
      BufferedReader reader =
         new BufferedReader(new FileReader(sourceFile));
      try {
         String newLine;
         while((newLine = reader.readLine()) != null) {
            if(newLine.isEmpty()) continue;
            StringTokenizer tokenizer = new StringTokenizer(newLine);
            final String
               name = tokenizer.nextToken(" ,:\t"),
               pStr = tokenizer.nextToken(" ,:\t");
            if(tokenizer.hasMoreTokens())
               throw new ParseException(
                  "EnumDistribution: " + newLine + " is not a valid entry in " + 
                  sourceFile + ".", 0);
            final double
               p = Double.parseDouble(pStr);
            if(p < 0. || p > 1.)
               throw new IOException(
                  pStr + " is not a valid probability for the value " + name);
            result.probabilities.put(converter.get(name), p);
            final int ordinal = converter.get(name).ordinal();
            if(valueIsComitted.get(ordinal))
               throw new ParseException(
                  "The value " + name + " appears twice in " + sourceFile, 0);
            valueProbabilities[converter.get(name).ordinal()] = p;
            valueIsComitted.set(ordinal, true);
         }
         { // Check sum of probabilities
            double sum = 0.;
            for(double p : valueProbabilities)
               sum += p;
            if(Math.abs(sum - 1.) > 1e2 * Math.ulp(1.))
               throw new IllegalStateException(
                  "EnumDistribution: parser has succeeded, but the resulting " + 
                  "probaility sum (value " + sum + ") is not equal to 1.");
         }
      }
      catch(Exception e) {
          throw new IOException(e.getMessage());
      }
      finally {
         reader.close();
      }
      result.dice = new EnumeratedIntegerDistribution(
         valueIndices, valueProbabilities);
      return result;
   }
   
   /**
     * Sample from the enum distribution.
     */
   public T sample() {
      return values[dice.sample()];
   }
   
   /**
     * Get the probability of an enum value.
     */
   public double getProbability(T value) {
      return probabilities.get(value);
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and as such should not be regarded as fixed.
    */
   @Override
   public String toString() {
      String result = "EnumDistribution, probabilities: ";
      for(Entry<T, Double> record : probabilities.entrySet())
         result += record.getKey().name() + " " + record.getValue() + "\n";
      return result;
   }
   
   static public void main(String[] args) {
      try {
         EnumDistribution<ScheduleIntervals> dice = 
            EnumDistribution.create(ScheduleIntervals.class, "./test.dat");
         EnumMap<ScheduleIntervals, Integer> tallies = 
            new EnumMap<ScheduleIntervals, Integer>(ScheduleIntervals.class);
         for(ScheduleIntervals value : ScheduleIntervals.values())
            tallies.put(value, 0);
         final int numSamples = 10000000;
         for(int i = 0; i< numSamples; ++i) {
            ScheduleIntervals value = dice.sample();
            tallies.put(value, tallies.get(value) + 1);
         }
         for(Entry<ScheduleIntervals, Integer> record : tallies.entrySet()) {
            ScheduleIntervals value = record.getKey();
            int tally = tallies.get(value);
            System.out.printf("%20s %16.10g\n", value.name(), tally / (double)numSamples);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
