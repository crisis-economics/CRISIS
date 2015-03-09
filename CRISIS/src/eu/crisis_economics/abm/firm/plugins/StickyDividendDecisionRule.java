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
package eu.crisis_economics.abm.firm.plugins;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.NumberUtil;

/**
  * An implementation of a smoothed {@link Firm} liquidity target decision rule.
  * This decision rule accepts a composite liquidity target decision rule ({@code R})
  * and, using this target, computes a smoothed liquidity target {@code L'} using an
  * exponentially smoothed dividend envelope.<br><br>
  * 
  * Concretely:
  * 
  * <ul>
  *   <li> A liquidity target {@code L} is computed from {@code R}
  *   <li> The maximum affordable dividend payment {@code dmax} (based on unallocated cash,
  *        due loan repayments and {@code L}) is computed for the {@link Firm}
  *   <li> The next maximum <i>effective</i> dividend payment {@code e} (the envelope) for the
  *        {@link Firm} is computed using an exponential smoothing over the last upper bound
  *        {@code e'} and the current maximum affordable dividend payment {@code dmax}:
  *        <br><br>
  *        <center><code>
  *           e = dmax * a + e * (1 - a)
  *        </code></center><br>
  *   <li> {@code e} is not allowed to increase further than {@code (1 + m)e'}, where
  *        {@code m} is a customizable upper bound in dividend growth;
  *   <li> The actual dividend {@code d} is set to be {@code min(dmax, e)} and the final
  *        liquidity target {@code L'} is computed accordingly.
  * </ul>
  * 
  * This smoothing decision rule therefor accepts a liquidity target {@code L} from another
  * liquidity decision rule, and will attempt to introduce stickiness into the dividend 
  * payment {@code d} so long as the resulting liquidity target {@code L'} does not fall
  * below {@code L}.
  * 
  * @author phillips
  */
public final class StickyDividendDecisionRule extends FirmDecisionRule {
   
   private final FirmDecisionRule
      decisionRule;
   
   public final static double
      DEFAULT_ENABLE_TIME = 100.,
      DEFAULT_MAX_ENVELOPE_CHANGE_UPPER = 1.02,
      DEFAULT_MAX_ENVELOPE_CHANGE_LOWER = .98,
      DEFAULT_ADAPTATION_RATE = .4,
      DEFAULT_MINIMUM_ENVELOPE = 1.;
   
   private final double
      enableTime,
      maxEnvelopeChangeUpper,
      maxEnvelopeChangeLower,
      adaptationRate,
      minimumEnvelope;
   
   @Inject
   public StickyDividendDecisionRule(
   @Named("STICKY_DIVIDEND_DECISION_RULE_COMPOSITE")
      final FirmDecisionRule decisionRule,
   @Named("STICKY_DIVIDEND_DECISION_RULE_ENABLE_TIME")
      final double enableTime,
   @Named("STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_UPPER")
      final double maxEnvelopeChangeUpper,
   @Named("STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_LOWER")
      final double maxEnvelopeChangeLower,
   @Named("STICKY_DIVIDEND_DECISION_ADAPTATION_RATE")
      final double adaptationRate,
   @Named("STICKY_DIVIDEND_DECISION_MINIMUM_ENVELOPE")
      final double minimumEnvelope
      ) {
      super(Preconditions.checkNotNull(decisionRule).getLastValue());
      Preconditions.checkArgument(adaptationRate >= 0.);
      Preconditions.checkArgument(enableTime >= 0.);
      Preconditions.checkArgument(maxEnvelopeChangeUpper >= 0.);
      Preconditions.checkArgument(maxEnvelopeChangeLower >= 0.);
      Preconditions.checkArgument(minimumEnvelope >= 0.);
      this.decisionRule = decisionRule;
      this.enableTime = enableTime;
      this.maxEnvelopeChangeUpper = maxEnvelopeChangeUpper;
      this.maxEnvelopeChangeLower = maxEnvelopeChangeLower;
      this.adaptationRate = adaptationRate;
      this.minimumEnvelope = minimumEnvelope;
   }
   
   /**
     * Create a {@link StickyDividendDecisionRule} with default parameters.
     * See also {@link #StickyDividendDecisionRule(FirmDecisionRule, double, double,
     * double, double)}
     */
   public StickyDividendDecisionRule(
      final FirmDecisionRule decisionRule) {
      this(
         decisionRule,
         DEFAULT_ENABLE_TIME,
         DEFAULT_MAX_ENVELOPE_CHANGE_UPPER,
         DEFAULT_MAX_ENVELOPE_CHANGE_LOWER,
         DEFAULT_ADAPTATION_RATE,
         DEFAULT_MINIMUM_ENVELOPE
         );
   }
   
   private double
      dividendMemory = -1.,
      upperEnvelope = -1.;
   
   @Override
   public double computeNext(final FirmState state) {
      decisionRule.computeNext(state);
      final double
         time = Simulation.getTime(),
         absoluteLiquidityTarget = decisionRule.getLastValue(),
         pendingLoans = state.getLoanLiability(),
         unallocatedCash = state.getUnallocatedCash(),
         maxDividend = Math.max(0., unallocatedCash - pendingLoans - absoluteLiquidityTarget);
      double
         smoothedDividend;
      if(dividendMemory == -1 || time < enableTime) {
         upperEnvelope = maxDividend;
         smoothedDividend = maxDividend;
      } else {
         upperEnvelope = NumberUtil.clamp(
            upperEnvelope * maxEnvelopeChangeLower,
            (upperEnvelope * (1. - adaptationRate) + maxDividend * adaptationRate),
            Math.max(upperEnvelope * maxEnvelopeChangeUpper, minimumEnvelope)
            );
         smoothedDividend = Math.min(maxDividend, upperEnvelope);
      }
      final double
         smoothedLiquidityTarget =
            Math.max(absoluteLiquidityTarget, unallocatedCash - smoothedDividend - pendingLoans);
      dividendMemory = smoothedDividend;
      super.recordNewValue(smoothedLiquidityTarget);
      return smoothedLiquidityTarget;
   }
   
   public double getEnableTime() {
      return enableTime;
   }
   
   public double getMaxEnvelopeChangeUpper() {
      return maxEnvelopeChangeUpper;
   }
   
   public double getMaxEnvelopeChangeLower() {
      return maxEnvelopeChangeLower;
   }
   
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Sticky Dividend Decision Rule, enable time: " + enableTime
            + ", adaptation rate: " + adaptationRate + ", envelope change rate"
            + " upper: " + maxEnvelopeChangeUpper + ", envelope change rate lower: "
            + maxEnvelopeChangeLower + ".";
   }
}
