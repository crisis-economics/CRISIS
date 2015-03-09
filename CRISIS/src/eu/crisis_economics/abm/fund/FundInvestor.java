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
package eu.crisis_economics.abm.fund;

import eu.crisis_economics.abm.contracts.CashAllocating;
import eu.crisis_economics.abm.contracts.Depositor;

/**
  * A minimal interface for a {@link Fund} investor.
  * 
  * @author phillips
  */
public interface FundInvestor extends Depositor, CashAllocating {
   /**
     * Set the investment decision rule for this {@link Agent}. A positive return 
     * value {@code X} is interpreted as an investment of size {@code X}. A negative
     * return value is interpreted as a {@link Fund} withdrawal request of size
     * {@code X}.
     * 
     * @param targetInvestmentValue
     *        The decision rule to govern {@link Fund} investments.
     */
   public void setFundInvestmentTargetDecisionRule(
      final FundInvestmentDecisionRule targetInvestmentValue
      );
   
   /**
     * Set the {@link Fund} {@link Agent} to which this {@link FundInvestor} is
     * linked. All investments will be with the stated {@link Fund}. It is 
     * acceptable for the argument to be {@code null}. In the event that the
     * argument is {@code null}, no {@link Fund} investments will take place.
     * 
     * @param fund
     */
   public void setFund(Fund fund);
   
   /**
     * Get the {@link Fund} {@link Agent} to which this {@link FundInvestor} is
     * linked. This method may return {@code null}.
     */
   public Fund getFund();
}
