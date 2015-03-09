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
import static org.testng.Assert.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.test.TestConstants;

public class HouseholdTest {
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
	public void testHouseholdInitializationWithNull() {
		try {
			new DepositingHousehold( null );
		} catch (final IllegalArgumentException e) {
			assertEquals( e.getMessage(), "depositHolder == null" );
		}
	}
	
	@Test
	public void testHouseholdInitialMoney() {
		final DepositingHousehold hh = new DepositingHousehold( bank );
		
		assertEquals( hh.getDepositValue(), 0.0, TestConstants.DELTA );
	}
	
	@Test
	public void testHouseholdInitialMoneyAssigned() {
		final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
		
		assertEquals( hh.getDepositValue(), 100.0, TestConstants.DELTA );
	}
	
	@Test
	public void testHouseholdCreditWithEnoughMoney() {
		try {
			final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
			final double ret = hh.credit( 50.0 );
			
			assertEquals( hh.getDepositValue(), 50.0, TestConstants.DELTA );
			assertEquals( ret, 50.0, TestConstants.DELTA );
		} catch (final InsufficientFundsException e) {
			fail( e.getMessage(), e );
		}
	}
	
	@Test
	public void testHouseholdCreditWithNotEnoughMoneyFailsAndLeavesMoneyReservesUntouched() {
		final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
		
		try {
			hh.credit( 200.0 );
		} catch (final InsufficientFundsException e) {
			assertEquals( hh.getDepositValue(), 100.0, TestConstants.DELTA );
		}
	}
	
	@Test
	public void testHouseholdDebit() {
		final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
		hh.debit( 100 );
		assertEquals( hh.getDepositValue(), 200.0, TestConstants.DELTA );
	}
}
