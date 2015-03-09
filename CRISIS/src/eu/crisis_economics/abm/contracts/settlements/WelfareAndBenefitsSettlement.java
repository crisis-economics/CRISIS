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
package eu.crisis_economics.abm.contracts.settlements;

import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.government.Government;

/**
  * Simple welfare and benefits settlement with no deductions,
  * auxiliaries or taxes.
  * @author phillips
  */
final class WelfareAndBenefitsSettlement extends SimpleTwoPartySettlement {
   WelfareAndBenefitsSettlement(
      final Government government,
      final Employee employee
      ) {
      super(government, employee, new SettlementPartyNotifier() {
         @Override
         public void postDebit(final double amountDebited) {
            // Notify the employee of incoming welfare/benefits.
            employee.notifyIncomingWageOrBenefit(amountDebited);
         }
         @Override
         public void postCredit(final double amountCredited) {
            // No Action
         }
      });
   }
}
