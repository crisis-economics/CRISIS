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
package eu.crisis_economics.abm.model.parameters;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A counting implementation of the {@link Parameter}{@code <Long>
  * interface. This implementation provides a single {@link Long} model
  * parameter whose value increments by {@code 1} every for every {@code Nth}
  * query, where {@code N} is fixed positive number.<br><br>
  * 
  * Concretely: this {@link Parameter}{@code <Long>} generates
  * {@link Long} sequences of the following types:<br><br>
  * 
  * <center><code>
  * 0, 1, 2, 3, 4, 5...
  * </code></center><br>
  * 
  * and
  * 
  * <center><code>
  * 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3...
  * </code></center><br>
  * 
  * where the length of the repeating block is customizable.<br>
  * 
  * @author phillips
  */
public class LongCounter extends AbstractParameter<Long> {
   
   private final long
      sizeOfBlocks;
   private long
      evaluationCounter;
   
   /**
     * Create a {@link LongCounter} with custom parameters.<br><br>
     * 
     * See also {@link LongCounter} and {@link Parameter}.
     * 
     * @param name
     *        The {@link String} name of this parameter. It is acceptable for
     *        this argument to be the empty {@link String}.
     * @param sizeOfBlocks
     *        A positive Long specifying the block width for repetitions during
     *        counting. If this argument is {@code 1}, the resulting counting
     *        sequence is {@code 0, 1, 2, 3 ...}. If this argument is {@code 2}, 
     *        the resulting counting sequence is {@code 0, 0, 1, 1, 2, 2...}.
     *        This argument must be nonzero.
     */
   @Inject
   public LongCounter(
   @Named("LONG_COUNTER_MODEL_PARAMETER_NAME")
      final String name,
   @Named("LONG_COUNTER_MODEL_PARAMETER_BLOCK_SIZE")
      final long sizeOfBlocks,
   @Named("LONG_COUNTER_MODEL_PARAMETER_INITIAL_VALUE")
      final long initialValue
      ) {
      super(name);
      this.sizeOfBlocks = sizeOfBlocks;
      this.evaluationCounter = initialValue;
   }
   
   @Override
   public Long get() {
      return evaluationCounter++ / sizeOfBlocks;
   }
   
   @Override
   public Class<?> getParameterType() {
      return Long.class;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Simple Model Parameter, name:" + super.getName() + ".";
   }
}
