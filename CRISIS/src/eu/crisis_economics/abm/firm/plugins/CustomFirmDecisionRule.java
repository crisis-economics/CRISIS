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
import eu.crisis_economics.abm.model.parameters.ModelParameter;

/**
  * An implementation of the {@link FirmDecisionRule} class. This rule delegates
  * the {@link Firm} decision rule to an instance of a {@link Parameter}, <code>D</code>. 
  * The result of {@link Parameter#get()} is taken to be the decision made by 
  * the {@link Firm}.<br><br>
  * 
  * <code>D</code> is called exactly once each time a decision is requested, and 
  * once before the first call to {@link #computeNext(FirmState)}.
  * 
  * @author phillips
  */
public final class CustomFirmDecisionRule extends FirmDecisionRule {
   
   private ModelParameter<Double>
      decisionGenerator;
   private double
      currentDecision;
   
   /**
     * Create a {@link CustomFirmDecisionRule} object with custom parameters.<br><br>
     * 
     * See also {@link CustomFirmDecisionRule}.
     * 
     * @param decisionValue (<code>D</code>)<br>
     *        A {@link Parameter} to be used to generate decisions for this {@link Firm}.
     */
   @Inject
   public CustomFirmDecisionRule(
      @Named("CUSTOM_FIRM_DECISION_RULE_VALUE")
      final ModelParameter<Double> decisionGenerator
      ) {
      super(decisionGenerator.get());
      this.decisionGenerator = Preconditions.checkNotNull(decisionGenerator);
      this.currentDecision = super.getLastValue();
   }
   
   @Override
   public double computeNext(final FirmState currentState) {
      final double
         result = currentDecision;
      this.currentDecision = decisionGenerator.get();
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Custom Firm Decision Rule, generator: " + decisionGenerator + ", current value: "
           + currentDecision + ".";
   }
}
