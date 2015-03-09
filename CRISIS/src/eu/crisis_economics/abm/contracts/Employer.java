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
 * An employer maintains a payroll - a list of labor contracts with employees. 
 * @author bochmann
 *
 */
public interface Employer extends LabourMarketParty, CashAllocating {

	/**
	 * Add a labor contract to the employers payroll.
	 * @param labour {@code Labor} contract
	 */
	void addLabour(Labour labour);

	/**
	 * Remove a labor contract from the employers payroll.
	 * @param labour {@code Labor} contract
	 * @return {@code true} if a contract was removed as a result of this call
	 */
	boolean removeLabour(Labour labour);
	
	/**
	 * @return number of labor contracts on the payroll
	 */
	double getLabourForce();

	void disallocateLabour(double size);

    void registerIncomingEmployment(double labourEmployed, double unitLabourWage);
}
