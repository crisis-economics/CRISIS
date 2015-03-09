/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.household;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import cern.jet.random.Uniform;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.test.TestConstants;

public class RandomHouseholdTest {
	private SimState simState;
	private Bank bank;
	
	@BeforeMethod
	public void setUp() {
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation( System.currentTimeMillis() );
		simState.start();
		bank = new Bank( 100 );
	}
	
	@AfterMethod
	public void tearDown() {
		simState.finish();
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	@Test
	public void testFrequencyAndDepositSuccess() {
		final RandomHousehold rh = new RandomHousehold( bank, new Uniform( 0.0, 1.0, 0 ), 1, 9, 1 );
		
		rh.step( simState );
		
		assertTrue( 1.0 <= rh.getDepositValue() && rh.getDepositValue() <= 10.0 );
	}
	
	@Test
	public void testFrequencyAndWithdrawalSuccess() {
		final RandomHousehold rh = new RandomHousehold( bank, new Uniform( 0.0, 1.0, 0 ), 1, 9, - 10 );
		rh.debit( 20 );
		
		rh.step( simState );
		
		assertTrue( 10.0 <= rh.getDepositValue() && rh.getDepositValue() <= 20.0 );
	}
	
	@Test
	public void testFrequencyAndWithdrawalFailureEmpties() {
		final RandomHousehold rh = new RandomHousehold( bank, new Uniform( 0.0, 1.0, 0 ), 1, 9, - 10 );
		
		rh.step( simState );
		
		assertEquals( rh.getDepositValue(), 0.0, TestConstants.DELTA );
	}
	
	@Test
	public void testFrequencyFailure() {
		final RandomHousehold rh = new RandomHousehold( bank, new Uniform( 0.0, 1.0, 0 ), 0, 9, 1 );
		
		rh.step( simState );
		
		assertEquals( rh.getDepositValue(), 0.0, TestConstants.DELTA );
	}
}
