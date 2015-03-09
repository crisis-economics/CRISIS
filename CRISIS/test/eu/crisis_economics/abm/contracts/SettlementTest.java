/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Peter Klimek
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

import org.junit.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementAdjacencyMatrix;
import eu.crisis_economics.abm.contracts.settlements.SettlementAdjacencyMatrixXGMML;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.settlements.Settlements;

/**
 * @author  olaf
 */
public class SettlementTest {
	
	private Settlement settlement;
	private Settlements settlements;
	private MySettlementParty partyA;	
	private MySettlementParty partyB;
	private SettlementAdjacencyMatrix adjacencyMatrix;
	private SettlementAdjacencyMatrixXGMML xgmmlGenerator;
	
	private class MySettlementParty implements SettlementParty{
		
	    private String uniqueID;
	    
		public MySettlementParty(final double funds) {
			super();
			this.funds = funds;
			this.uniqueID = java.util.UUID.randomUUID().toString();
		}

		public double funds;

		@Override
		public double credit(final double amt) throws InsufficientFundsException {
			try {
				if (funds < amt) {
					throw new InsufficientFundsException("Insufficient Funds!");
				} 
				funds -= amt;
				return (amt);
			}
			finally {}
		}

		@Override
		public void debit(final double amt) {
			funds += amt;			
		}

		@Override
		public void cashFlowInjection(double amt) {
			this.debit(50);
		}

      @Override
      public String getUniqueName() {
         return uniqueID;
      }
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		partyA = new MySettlementParty(100.0);
		partyB = new MySettlementParty(100.0);
		settlements = new Settlements();
		settlement = settlements.create(partyA, partyB);
		
		System.out.println("Test: " + this.getClass().getSimpleName());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#transfer()}.
	 * Test for a regular transfer.   
	 */
	@Test
	public final void testTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.transfer(50.0);
			AssertJUnit.assertEquals( 50.0, partyA.funds );
			AssertJUnit.assertEquals( 150.0, partyB.funds );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
			e.printStackTrace();
		} 
	}
	
	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#transfer()}.
	 * Listener Test for a regular transfer. Transfers should add up.
	 */
	@Test
	public final void testTransferListener() {
		adjacencyMatrix = new SettlementAdjacencyMatrix();
		settlement.addListener(adjacencyMatrix);
		xgmmlGenerator = new SettlementAdjacencyMatrixXGMML("FlowOfCash");
		settlement.addListener(xgmmlGenerator);
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyB) );
		try {
			settlement.transfer(50.0);	// transfer 50 a -> b
			AssertJUnit.assertEquals( 50.0, partyA.funds );
			AssertJUnit.assertEquals( 150.0, partyB.funds );
			AssertJUnit.assertEquals( 50.0, adjacencyMatrix.getValue(partyA, partyB) );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
			e.printStackTrace();
		} 
		try {
			settlement.transfer(30.0);	// transfer 30 a -> b
			AssertJUnit.assertEquals( 20.0, partyA.funds );
			AssertJUnit.assertEquals( 180.0, partyB.funds );
			AssertJUnit.assertEquals( 80.0, adjacencyMatrix.getValue(partyA, partyB) );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
			e.printStackTrace();
		} 
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyA) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyA) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyB) );
		try {
			xgmmlGenerator.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#transfer()}.
	 * Overdrafts should throw InsufficientFundsException after cashFlowInjection() was not sucessful. 
	 */
	@Test
	public final void testOverdraftTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.transfer(200.0);
			AssertJUnit.fail();
		} catch (final InsufficientFundsException e) {
			// do something about the overdraft! cashFlowInjection is only 50
			AssertJUnit.assertEquals( 150.0, partyA.funds );
			AssertJUnit.assertEquals( 100.0, partyB.funds );
		} 
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#transfer()}.
	 * Listener overdraft Test should throw InsufficientFundsException after cashFlowInjection() was not sucessful. 
	 * Transfer flow should not add up.
	 */
	@Test
	public final void testOverdraftTransferListener() {
		adjacencyMatrix = new SettlementAdjacencyMatrix();
		settlement.addListener(adjacencyMatrix);
		xgmmlGenerator = new SettlementAdjacencyMatrixXGMML("FlowOfCash");
		settlement.addListener(xgmmlGenerator);
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyB) );
		try {
			settlement.transfer(90.0);
			AssertJUnit.assertEquals( 10.0, partyA.funds );
			AssertJUnit.assertEquals( 190.0, partyB.funds );
			AssertJUnit.assertEquals( 90.0, adjacencyMatrix.getValue(partyA, partyB) );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
			e.printStackTrace();
		} 
		try {
			settlement.transfer(110.0);
			AssertJUnit.fail();
		} catch (final InsufficientFundsException e) {
			// do something about the overdraft! cashFlowInjection is only 50
			AssertJUnit.assertEquals( 60.0, partyA.funds );
			AssertJUnit.assertEquals( 190.0, partyB.funds );
			AssertJUnit.assertEquals( 90.0, adjacencyMatrix.getValue(partyA, partyB) );
		} 
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyA) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyA) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyB) );
		try {
			xgmmlGenerator.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * Test for a regular transfer.
	 */
	@Test
	public final void testBidirectionalTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.bidirectionalTransfer(50.0);
			AssertJUnit.assertEquals( 50.0, partyA.funds );
			AssertJUnit.assertEquals( 150.0, partyB.funds );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * Test transfer with negative argument. (incl. Listener)
	 */
	@Test
	public final void testInverseBidirectionalTransfer() {
		adjacencyMatrix = new SettlementAdjacencyMatrix();
		settlement.addListener(adjacencyMatrix);
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyA) );
		try {
			settlement.bidirectionalTransfer(-50.0);
			AssertJUnit.assertEquals( 150.0, partyA.funds );
			AssertJUnit.assertEquals( 50.0, partyB.funds );
			AssertJUnit.assertEquals( 50.0, adjacencyMatrix.getValue(partyB, partyA) );
		} catch (final InsufficientFundsException e) {
			e.printStackTrace();
			AssertJUnit.fail();
		}
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyB) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyA, partyA) );
		AssertJUnit.assertEquals( 0.0, adjacencyMatrix.getValue(partyB, partyB) );
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * Regular transfer with successful cashFlowInjection().
	 */
	@Test
	public final void testcashFlowInjectionOverdraftBidirectionalTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.bidirectionalTransfer(150.0);
			AssertJUnit.assertEquals( 0.0, partyA.funds );
			AssertJUnit.assertEquals( 250.0, partyB.funds );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
		}
	}
	
	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * regular transfer with unsecessful cashFlowInjection().
	 */
	@Test
	public final void testOverdraftBidirectionalTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.bidirectionalTransfer(200.0);
			AssertJUnit.fail();
		} catch (final InsufficientFundsException e) {
			// do something about the overdraft! cashFlowInjection is only 50 !
			AssertJUnit.assertEquals( 150.0, partyA.funds );
			AssertJUnit.assertEquals( 100.0, partyB.funds );
		}
	}
	
	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * Negative argument transfer with successful cashFlowInjection().
	 */
	@Test
	public final void testcashFlowInjectionOverdraftInverseBidirectionalTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.bidirectionalTransfer(-150.0);
			AssertJUnit.assertEquals( 250.0, partyA.funds );
			AssertJUnit.assertEquals( 0.0, partyB.funds );
		} catch (final InsufficientFundsException e) {
			AssertJUnit.fail();
		} 
	}

	/**
	 * Test method for {@link eu.crisis_economics.abm.contracts.settlements.Settlement#bidirectionalTransfer()}.
	 * Negative argument transfer with unsuccessful cashFlowInjection().
	 */
	@Test
	public final void testOverdraftInverseBidirectionalTransfer() {
		AssertJUnit.assertEquals( 100.0, partyA.funds );
		AssertJUnit.assertEquals( 100.0, partyB.funds );
		try {
			settlement.bidirectionalTransfer(-200.0);
			AssertJUnit.fail();
		} catch (final InsufficientFundsException e) {
			// do something about the overdraft! cashFlowInjection is only 50 !
			AssertJUnit.assertEquals( 100.0, partyA.funds );
			AssertJUnit.assertEquals( 150.0, partyB.funds );

		} 
	}
	
   static public void main(String[] args) {
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testTransferListener();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testOverdraftTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testOverdraftTransferListener();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testInverseBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testcashFlowInjectionOverdraftBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testOverdraftBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testcashFlowInjectionOverdraftInverseBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      try {
         SettlementTest test = new SettlementTest();
         test.setUp();
         test.testOverdraftInverseBidirectionalTransfer();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      return;
   }
}
