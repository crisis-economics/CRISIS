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
package eu.crisis_economics.abm.contracts;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.contracts.settlements.SettlementAdjacencyMatrix;
import eu.crisis_economics.abm.contracts.settlements.SettlementAdjacencyMatrixXGMML;
import eu.crisis_economics.abm.contracts.settlements.SettlementListeners;


public class SettlementListenersTest {
	
	private SettlementListeners sone = null, stwo = null;

	@BeforeMethod
	public void setUp() throws Exception {
		System.out.println("Test: " + this.getClass().getSimpleName());
		SettlementListeners.getInstance().clear();	// other tests use the same loader and may interfere 
		sone = SettlementListeners.getInstance();
		stwo = SettlementListeners.getInstance();
	}

	@AfterMethod
	public void tearDown() {
		SettlementListeners.getInstance().clear();
	}
	
	@Test
	public void getInstance() {
		sone.add(new SettlementAdjacencyMatrix());
		stwo.add(new SettlementAdjacencyMatrixXGMML("FlowOfCash"));
		AssertJUnit.assertEquals( 2, sone.size() );
		AssertJUnit.assertEquals( 2, stwo.size() );
		AssertJUnit.assertEquals( sone, stwo );
		SettlementListeners.getInstance().remove(sone.iterator().next());
		SettlementListeners.getInstance().remove(sone.iterator().next());
		AssertJUnit.assertEquals( 0, sone.size() );
		AssertJUnit.assertEquals( 0, stwo.size() );
	}
}
