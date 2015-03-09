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
package eu.crisis_economics.abm.household.plugins;

import java.lang.annotation.Retention;

import com.google.common.base.Preconditions;
import com.google.inject.BindingAnnotation;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A {@link Household}{@link HouseholdDecisionRule} that applies noise to the results of
  * an existing {@link HouseholdDecisionRule}. This algorithm operates as follows:<br><br>
  * (a) Noise, N, is drawn from a Gaussian distribution with mean zero and 
  *     custom variance;<br>
  * (b) N is clamped in the range [min, max] for customizable values min, max;<br>
  * (c) Another {@link HouseholdDecisionRule} is called, and returns value D;<br>
  * (d) This {@link HouseholdDecisionRule} returns D * (1 + N).
  * @author phillips
  */
public final class NoisyHouseholdDecisionRule extends HouseholdDecisionRule {
   @BindingAnnotation @Retention(RUNTIME)
   public @interface NoisyHouseholdDecisionRuleArg {}
   
   private final HouseholdDecisionRule
     rule;
   private final double
     noiseAggressiveness,
     noiseLowerBound,
     noiseUpperBound;
   
   @AssistedInject
   /**
     * Create a {@link NoisyHouseholdDecisionRule}.
     * @param rule
     *        The {@link HouseholdDecisionRule} to which noise is added.
     * @param noiseAggressiveness
     *        The variance of the Gaussian distribution from which to draw
     *        noise.
     * @param noiseLowerBound
     *        A lower bound on the value of the noise to apply.
     * @param noiseUpperBound
     *        An upper bound on the value of the noise to apply.
     */
   public NoisyHouseholdDecisionRule(
   @Assisted
      final HouseholdDecisionRule rule,
   @Named("HOUSEHOLD_DECISION_RULE_NOISE_AGGRESSIVENESS")
      final double noiseAggressiveness,
   @Named("HOUSEHOLD_DECISION_RULE_NOISE_LOWER_BOUND")
      final double noiseLowerBound,
   @Named("HOUSEHOLD_DECISION_RULE_NOISE_UPPER_BOUND")
      final double noiseUpperBound
      ) {
      StateVerifier.checkNotNull(rule);
      Preconditions.checkArgument(noiseAggressiveness >= 0.);
      Preconditions.checkArgument(noiseLowerBound <= noiseUpperBound);
      this.rule = rule;
      this.noiseAggressiveness = noiseAggressiveness;
      this.noiseLowerBound = noiseLowerBound;
      this.noiseUpperBound = noiseUpperBound;
   }
   
   @Override
   public double computeNext(final HouseholdState currentState) {
      final double
         baseValue = rule.computeNext(currentState),
         noise = Simulation.getRunningModel().random.nextGaussian() * noiseAggressiveness,
         clampedNoise = NumberUtil.clamp(noiseLowerBound, noise, noiseUpperBound),
         result = baseValue * (1. + clampedNoise);
      super.recordNewValue(result);
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Noisy Household Decision Rule, applies to:" + rule
            + ", noise aggressiveness:" + noiseAggressiveness + ".";
   }
}
