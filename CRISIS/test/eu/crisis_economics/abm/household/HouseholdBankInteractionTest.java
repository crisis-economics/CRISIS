/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.household;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.test.TestConstants;

public class HouseholdBankInteractionTest {
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
		System.out.println("Test: " + this.getClass().getSimpleName() + "complete.");
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	@Test
	public void testHouseholdRegisteredProperlyAtBank() {
		final Bank bank = new Bank( 100 );
		final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
		
		// Bank's cache and Deposits of the households 
		assertEquals( bank.getLiabilities().size(), 1 );
		assertEquals( bank.getAssets().size(), 1 );
		
		final DepositAccount account = (DepositAccount) bank.getLiabilities().get( 0 );
		assertEquals( account.getDepositor(), hh );
		assertEquals( account.getDepositHolder(), bank );
	}
	
	@Test
	public void testHouseholdCreditShowsAtBank() {
		try {
			final Bank bank = new Bank( 100 );
			final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
			
			hh.credit( 50 );
			
			final DepositAccount account = (DepositAccount) bank.getLiabilities().get( 0 );
			assertEquals( account.getValue(), 50.0, TestConstants.DELTA );
			
		} catch (final InsufficientFundsException e) {
			fail( e.getMessage(), e );
		}
	}
	
	@Test
	public void testHouseholdDebitShowsAtBank() {
		final Bank bank = new Bank( 100 );
		final DepositingHousehold hh = new DepositingHousehold( bank, 100.0 );
		
		hh.debit( 50 );
		
		final DepositAccount account = (DepositAccount) bank.getLiabilities().get( 0 );
		assertEquals( account.getValue(), 150.0, TestConstants.DELTA );
	}
	
    public static void main(String[] args) {
       HouseholdBankInteractionTest test = new HouseholdBankInteractionTest();
       test.setUp();
       test.testHouseholdCreditShowsAtBank();
       test.testHouseholdDebitShowsAtBank();
       test.testHouseholdRegisteredProperlyAtBank();
       test.tearDown();
    }
}
