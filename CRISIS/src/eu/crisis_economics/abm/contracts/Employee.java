/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Alessandro Gobbi
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
package eu.crisis_economics.abm.contracts;

import eu.crisis_economics.abm.markets.LabourMarketParty;

/**
 * An employee maintains a state of employment. In case of employment, the employee holds an labor contract. 
 * The employee can hold only one labor contract at a time. 
 * @author bochmann
 *
 */
public interface Employee extends LabourMarketParty {

	/**
	 * Set the state of the employee to {@code employed}. The state returns to {@code unemployed} as soon as the 
	 * given labor contract expires or is canceled. 
	 * @param labour {@code Labor} contract
	 */
	void startContract(Labour labour) throws DoubleEmploymentException;
	
	/** Signal that a labour contract is being closed. */
	void endContract(Labour labour);
	
	/**
	  * Get the total number of labour units currently salaried for this employee.
	  */
	double getLabourAmountEmployed();
	
	/**
	  * Get the maximum number of labour units this employee can supply in the present 
	  * business cycle.
	  */
	double getMaximumLabourSupply();
	
	/**
	  * Get the total number of labour units currently unemployed for this employee.
	  */
	double getLabourAmountUnemployed();

	void allocateLabour(double amount) throws AllocationException;

	void disallocateLabour(double amount) throws IllegalArgumentException;
	/** the firms who issue an order to buy labour, should disallocate labour
	 * this will be useful one different labour market will be in place
	 */

	/**
	  * Notify the employee of outgoing labour (employment)
	  * @param labourEmployed
	  *        The amount of labour employed
	  * @param wagePerUnitLabour
	  *        The wage per unit labour received
	  */
	void notifyOutgoingEmployment(double labourEmployed, double wagePerUnitLabour);
	
	/**
	  * Notify the employee of incoming wages or incoming benefits. This
	  * method should be called after the wage of benefit sum has been
	  * transferred to the employee.
	  * @param totalPayment
	  *        The total wage or benefit sum received.
	  */
	void notifyIncomingWageOrBenefit(double totalPayment);
}
