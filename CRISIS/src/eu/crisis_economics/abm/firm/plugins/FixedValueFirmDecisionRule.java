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

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link FirmDecisionRule} class. This {@link FirmDecisionRule}
  * returns a constant decision outcome forever. The value of this decision outcome is
  * customizable.
  * 
  * @author phillips
  */
public final class FixedValueFirmDecisionRule extends FirmDecisionRule {
   
   private double
      decisionOutcome;
   
   /**
     * Create a {@link FixedValueFirmDecisionRule} object with custom parameters.<br><br>
     * 
     * See also {@link FixedValueFirmDecisionRule}.
     * 
     * @param decisionOutcome (<code>D</code>)<br>
     *        The outcome of this {@link FirmDecisionRule}. This value is returned
     *        by {@link #getLastValue()} forevermore.
     */
   @Inject
   public FixedValueFirmDecisionRule(
      @Named("FIXED_VALUE_FIRM_DECISION_RULE_VALUE")
      final double decisionOutcome
      ) {
      super(decisionOutcome);
      this.decisionOutcome = decisionOutcome;
   }
   
   @Override
   public double computeNext(final FirmState currentState) {
      return decisionOutcome;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Fixed Value Firm Decision Rule, fixed outcome: " + decisionOutcome + ".";
   }
}
