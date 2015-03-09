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
package eu.crisis_economics.abm.events;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;

/**
  * An {@link Event} which signals that an {@link Employer} has paid wages to an {@link Employee}.
  * 
  * @author phillips
  */
public final class WagePaymentEvent implements Event {
   
   final Employer
      employer;
   final Employee
      employee;
   final double
      cashFlow;
   
   /**
     * Create a {@link WagePaymentEvent} object.
     * 
     * @param employer <br>
     *        The {@link Employer} who paid wages. This argument must be non-<code>null</code>.
     * @param employee <br>
     *        The {@link Employee} who received wages. This argument must be non-<code>null</code>.
     * @param cashFlow <br>
     *        The wage sum paid. This argument may not be negative or zero.
     */
   public WagePaymentEvent(   // Immutable
      final Employer employer,
      final Employee employee,
      final double cashFlow
      ) {
      this.employer = Preconditions.checkNotNull(employer);
      this.employee = Preconditions.checkNotNull(employee);
      Preconditions.checkArgument(
         cashFlow > 0., getClass().getSimpleName() + ": wage payment sum is negative or zero.");
      this.cashFlow = cashFlow;
   }
   
   public Employer getEmployer() {
      return employer;
   }
   
   public Employee getEmployee() {
      return employee;
   }
   
   public double getCashFlow() {
      return cashFlow;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Wage Payment Event, employer:" 
            + employer.getClass().getSimpleName() + employer.hashCode()
            + ", employee:"
            + employee.getClass().getSimpleName() + employee.hashCode() 
            + ", cash flow: " + cashFlow + ".";
   }
}
