/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.test;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

/**
 * @author olaf
 *
 */
public class SchedulingOrderTest {
	private SimState simState;

	@BeforeMethod
	public void setUp() {
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation( System.currentTimeMillis() );
		simState.start();
	}
	
	@AfterMethod
	public void tearDown() {
		simState.finish();
	}
	
	@Test
	public final void testStrategyFirmTiming() {
		Assert.assertTrue(
		   NamedEventOrderings.FIRM_DECIDE_PRODUCTION.getUnitIntervalTime() < 
		      NamedEventOrderings.DEPOSIT_PAYMENT.getUnitIntervalTime());
		Assert.assertTrue(
		   NamedEventOrderings.DEPOSIT_PAYMENT.getUnitIntervalTime() > 
		      NamedEventOrderings.COMMERCIAL_MARKET_BIDDING.getUnitIntervalTime());
	}
}

