/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.markets.nonclearing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.markets.StockTrader;
import eu.crisis_economics.abm.markets.nonclearing.Instrument;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.test.TestConstants;

public class StockMarketTest {
    public static class TestStockHolder
        extends Agent implements StockHolder, StockTrader {
        
		private final List<Order> orders = new ArrayList<Order>();
		
		private double
		   savings,
		   allocatedCash;
		
		public double getSavings() {
			return savings;
		}
		
		@Override
		public void addOrder(final Order order) {
			orders.add( order );
		}
		
		@Override
		public boolean removeOrder(final Order order) {
			return orders.remove( order );
		}
		
		@Override
		public void updateState(final Order order) {
			// Empty by intention
		}
		
		@Override
		public double credit(final double amt)
				throws InsufficientFundsException {
			// Note we have infinite money, credit() is always successful
			// For production/example usage, this must be updated
			return amt;
		}
		
		@Override
		public void debit(final double amt) {
			savings += amt;
		}
		
		@Override
		public StockAccount getStockAccount(final String instrumentName) {
			if ( null == instrumentName ) {
				throw new AssertionError( "null == instrumentName" );
			}
			
			for (final Contract act : assets) {
				if ( act instanceof StockAccount ) {
					final StockAccount stockAccount = (StockAccount) act;
					
					if ( instrumentName.equals( stockAccount.getInstrumentName() ) ) {
						return stockAccount;
					}
				}
			}
			
			return null;
		}
		
		@Override
		public boolean hasShareIn(final String instrumentName) {
			for (final Contract act : assets) {
				if ( act instanceof StockAccount ) {
					final StockAccount stockAccount = (StockAccount) act;
					
					if ( instrumentName.equals( stockAccount.getInstrumentName() ) ) {
						return true;
					}
				}
			}
			
			return false;
		}
		
		@Override
		public double getNumberOfSharesOwnedIn(final String instrumentName) {
			if ( hasShareIn( instrumentName ) )
				return getStockAccount( instrumentName ).getQuantity();
			else
			    return 0;
		}
		
		@Override
		public String toString() {
			return "TestStockHolder";
		}
		
		@Override
		public void addAsset(final Contract asset) {
			assets.add( asset );
		}
		
		@Override
		public boolean removeAsset(final Contract asset) {
			return assets.remove( asset );
		}
		
		@Override
		public List<Contract> getAssets() {
			return Collections.unmodifiableList( assets );
		}

		@Override
		public Set<StockMarket> getStockMarkets() {
			// TODO Auto-generated method stub
			return null;
		}

      @Override
      public double allocateCash(double amount) {
         double allocatable = Math.max(savings - allocatedCash, 0.);
         amount = Math.min(allocatable, amount);
         allocatedCash += amount;
         return amount;
      }
      
      @Override
      public double disallocateCash(double amount) {
         amount = Math.min(allocatedCash, amount);
         allocatedCash -= amount;
         return amount;
      }
      
      @Override
      public double getAllocatedCash() {
         return allocatedCash;
      }
      
      @Override
      public void allocateShares(String tickerSymbol, double volume)
            throws AllocationException {
         if (this.getStockAccount(tickerSymbol).getQuantity()
               - this.getStockAccount(tickerSymbol).allocatedShares < volume) {
            throw new AllocationException();
         } else
            this.getStockAccount(tickerSymbol).allocatedShares += volume;
      }

		@Override
		public void disallocateShares(String tickerSymbol, double volume) {
			StockAccount stockAccount=this.getStockAccount(tickerSymbol);
			if ( stockAccount.allocatedShares < volume) {
				volume = 0;
				throw new IllegalArgumentException();
			} else this.getStockAccount(tickerSymbol).allocatedShares -= volume;		
		}

		@Override
		public double calculateAllocatedCashFromActiveOrders() {
			return 0.;
		}

		@Override
		public void cashFlowInjection(double amt) { }

      @Override
      public Map<String, StockAccount> getStockAccounts() {
         return null;
      }

      @Override
      public void registerIncomingDividendPayment(
         final double dividendPayment,
         final double pricePerShare,
         final double numberOfSharesHeld
         ) {
         // No action.
      }
      
      @Override
      public <T> T accept(final AgentOperation<T> operation) {
         return operation.operateOn(this);
      }

      @Override
      public double getUnallocatedCash() {
         return Math.max(savings - getAllocatedCash(), 0.);
      }
	}
	
	public static class TestStockReleaser
			implements StockReleaser {
		
		private final List<Contract> assets = new ArrayList<Contract>();
		private final String uniqueName;
		
		public TestStockReleaser() {
			Simulation.once(this, "step", NamedEventOrderings.FIRM_SHARE_PAYMENTS);
			this.uniqueName = java.util.UUID.randomUUID().toString();
		}
		
		@SuppressWarnings("unused")
        private void step() {
            for (final Contract contract : assets) {
                if (contract instanceof StockAccount) {
                    final StockAccount stockAccount = (StockAccount) contract;
                    try {
                        final double dividend = stockAccount.getReleaser().computeDividendFor( stockAccount );
                        stockAccount.makeDividentPayment(dividend);
                    } catch (final InsufficientFundsException e) {
                       throw new RuntimeException( e );
                    }
                }
           }
           Simulation.once(this, "step", NamedEventOrderings.FIRM_SHARE_PAYMENTS);
		}
		
		public List<Contract> getAssets() {
			return Collections.unmodifiableList( assets );
		}
		
		@Override
		public double credit(final double amt)
				throws InsufficientFundsException {
			return amt; // Indicates infinite money
		}
		
		@Override
		public void debit(final double amt) {
			throw new AssertionError( "Should not be called." );
		}
		
		@Override
		public double computeDividendFor(final StockAccount stockAccount) {
			return 1.0;
		}
		
		@Override
		public String toString() {
			return "TestStockReleaser";
		}
		
		@Override
		public void addStockAccount(final StockAccount stockAccount) {
			assets.add( stockAccount );
		}
		
		@Override
		public boolean removeStockAccount(final StockAccount stockAccount) {
			return assets.remove( stockAccount );
		}

		@Override
		public void releaseShares(final double numberOfShares, final double pricePerShare,
				final StockHolder shareHolder) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDividendPerShare(final double dividendPerShare) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getMarketValue() {
			// TODO Auto-generated method stub
			return ((StockAccount)assets.get(0)).getPricePerShare();
		}

		@Override
		public void cashFlowInjection(double amt) {
			// TODO Auto-generated method stub
			
		}
		
      @Override
      public void registerOutgoingDividendPayment(
         double dividendPayment,
         double pricePerShare,
         double numberOfShars
         ) { }
      
      @Override
      public String getUniqueName() {
         return uniqueName;
      }

      @Override
      public double getDividendPerShare() {
         return 0;
      }
	}
	
	// ======================================================================================================================
	
	private SimState simState;
	private StockMarket stockMarket;
	
	@BeforeMethod
	public void setUp() {
		simState = new EmptySimulation(0L);
		simState.start();
		
		stockMarket = new StockMarket();
	}
	
	@AfterMethod
	public void tearDown() {
		simState.finish();
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	@Test
	public void testGenerateTickerSymbol() {
		final String tickerSymbol = StockInstrument.generateTickerSymbol( "WAAAAAAGH!" );
		
		assertEquals( tickerSymbol, "Stock_WAAAAAAGH!" );
	}
	
	@Test
	public void testParseNameFromTickerSymbol() {
		final String pureInstrumentName = StockInstrument.parseNameFromTicker( "Stock_WAAAAAAGH!" );
		
		assertEquals( pureInstrumentName, "WAAAAAAGH!" );
	}
	
	@Test
	public void testParseNameFromTickerSymbolForInvalidInput() {
		try {
			StockInstrument.parseNameFromTicker( "GRUNGA!" );
		} catch (final Exception e) {
			assertEquals( e.getMessage(), "Malformed ticker symbol: GRUNGA!" );
		}
	}
	
	@Test
	public void testStockInstrumentValidatesParam() {
		try {
			stockMarket.getInstrument( null );
		} catch (final IllegalArgumentException e) {
			assertEquals( e.getMessage(), "null == instrumentName" );
		}
	}
	
	@Test
	public void testStockInstrumentHandlesNonExistentInstrument() {
		final String symbol = StockInstrument.generateTickerSymbol( "GhostReconGame" );
		
		assertTrue( ! stockMarket.hasInstrument( symbol ) );
		stockMarket.addInstrument( symbol );
		assertTrue( stockMarket.hasInstrument( symbol ) );
		
		if ( stockMarket.getInstrument( symbol ) instanceof StockInstrument ) {
			final StockInstrument stockInstrument = (StockInstrument) stockMarket.getInstrument( symbol );
			assertEquals( stockInstrument.getInstrumentName(), "GhostReconGame" );
		} else {
			fail();
		}
	}
	
	@Test
	public void testStockInstrumentAddsNonExistentInstrumentWithProperName() {
		final String symbol = StockInstrument.generateTickerSymbol( "GhostReconGame" );
		stockMarket.addInstrument( symbol );
		
		final StockInstrument stockInstrument = (StockInstrument) stockMarket.getInstrument( symbol );
		assertEquals( stockInstrument.getInstrumentName(), "GhostReconGame" );
	}
	
	@Test
	public void testGetStockInstrumentValidatesParam() {
		try {
			stockMarket.getStockInstrument( null );
		} catch (final IllegalArgumentException e) {
			assertEquals( e.getMessage(), "instrumentName == null" );
		}
	}
	
	@Test
	public void testSameInstrumentAddedTwiceDoesNotHaveAnyEffect() {
		final String symbol = StockInstrument.generateTickerSymbol( "GhostReconGame" );
		stockMarket.addInstrument( symbol );
		
		final Instrument orig = stockMarket.getInstrument( symbol );
		stockMarket.addInstrument( symbol );
		
		// This == is intended here: we expect the original instrument
		assertTrue( orig == stockMarket.getInstrument( symbol ) );
	}
	
	@Test
	public final void testInitialStockAccountSetup() {
		final TestStockReleaser firm = new TestStockReleaser();
		final TestStockHolder stockOwner = new TestStockHolder();
		
		final double quantity = 1.;
		final double pricePerShare = 10;
		UniqueStockExchange.Instance.addStock(firm, pricePerShare);
		final String stockName = firm.getUniqueName();
		
		final StockAccount stockAccount = StockAccount.create(stockOwner, firm, quantity);
		
		assertEquals( stockAccount.getHolder(), stockOwner );
		assertEquals( stockOwner.getNumberOfSharesOwnedIn(stockName), 1.0, TestConstants.DELTA );
		assertEquals( stockOwner.getStockAccount(stockName).getPricePerShare(), 10, TestConstants.DELTA );
	}
	
	@Test
	public final void testBuyerHasNotEnoughMoney() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			// TODO Mocking would be useful here
			final TestStockHolder buyer = new TestStockHolder() {
				@Override
				public double credit(final double amt)
						throws InsufficientFundsException {
					throw new InsufficientFundsException( "No moneyz!" );
				}
			};
			final TestStockReleaser firm = new TestStockReleaser();
			
			final String instrument = firm.getUniqueName();
			final double pricePerShare = 100;
			UniqueStockExchange.Instance.addStock(firm, pricePerShare);
            
			StockAccount.create(seller, firm, 1);
			
			try {
				stockMarket.addOrder( buyer, instrument, - 1.0, pricePerShare );
			} catch (AllocationException e) {
				assertEquals(buyer.allocatedCash, 0.0);
			}
			try {
				stockMarket.addOrder( seller, instrument, 1.0, pricePerShare );
			} catch (AllocationException e) {
				e.printStackTrace();
				fail();
			}
		} catch (final OrderException e) {
			fail();
		} catch (final RuntimeException e) {
			fail("make sure to block cash at buyer!");
		}
	}
	
	@Test(enabled = false)
	public final void testErrorIfStockDividendsAreNegative() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			// TODO Mocking would be useful here
			final TestStockReleaser firm = new TestStockReleaser() {
				@Override
				public double computeDividendFor(final StockAccount stockAccount) {
					return - 1.0;
				}
			};
			final double pricePerShare = 1.0;
			UniqueStockExchange.Instance.addStock(firm, pricePerShare);
			
			StockAccount.create(seller, firm, 1);
			// This should throw an exception: not enough money to pay for dividends.
            while(Simulation.getCycleIndex() < 1 && Simulation.hasFurtherScheduledEvents()) {
               System.out.printf("time: %10.5g cycle: %d\n",
                  Simulation.getTime(), Simulation.getCycleIndex());
               simState.schedule.step(simState);
            }
			fail();
		} catch (final Exception e) {
			final IllegalStateException cause = (IllegalStateException) e.getCause();
			System.out.println(cause);
		}
	}
	
	@Test
	public final void testErrorIfNoMoneyToPayDividends() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			// TODO Mocking would be useful here
			final TestStockReleaser firm = new TestStockReleaser() {
				@Override
				public double credit(final double amt)
						throws InsufficientFundsException {
					throw new InsufficientFundsException(
					    "Money, money, money... it's da rich man's world..." );
				}
			};
			final double stockPrice = 1.0;
			UniqueStockExchange.Instance.addStock(firm, stockPrice);
			
			StockAccount.create(seller, firm, 1);
			while(Simulation.getCycleIndex() < 1 && Simulation.hasFurtherScheduledEvents()) {
	           System.out.printf("time: %10.5g cycle: %d\n",
	              Simulation.getTime(), Simulation.getCycleIndex());
			   simState.schedule.step(simState); // This should throw an exception: Not enough money to pay for dividends
			}
			fail();
		} catch (final RuntimeException e) {
			final InsufficientFundsException cause = (InsufficientFundsException) e.getCause();
			assertEquals( cause.getMessage(), "Money, money, money... it's da rich man's world..." );
		}
	}
	
	// TODO Should be refactored to a separate test class, it has nothing to do with the StockMarket
	// 		It would require extracting the abstract agent implementations, which is under development at the moment
	@Test
	public final void testSellingWorksOnAccount() {
		final TestStockHolder holder = new TestStockHolder();
		final TestStockReleaser releaser = new TestStockReleaser();
		UniqueStockExchange.Instance.addStock(releaser, 10.);
		final StockAccount account = StockAccount.create(holder, releaser, 10.);
		
		assertEquals( account.getPricePerShare(), 10.0, TestConstants.DELTA );
		
		try {
           UniqueStockExchange.Instance.generateSharesFor(
              releaser.getUniqueName(), holder, 10., 0.);
        } catch (InsufficientFundsException e) {
           Assert.fail();
        }
		
		assertEquals( account.getQuantity(), 20., 1.e-12);
		assertEquals(
		   account.getValue(),
		   account.getPricePerShare() * account.getQuantity(),
		   TestConstants.DELTA);
	}
	
	@Test
	public final void testTryingToSellMoreSharesThanOwned() {
		try {
			final TestStockHolder holder = new TestStockHolder();
			final TestStockReleaser releaser = new TestStockReleaser();
			UniqueStockExchange.Instance.addStock(releaser, 1.0);
            final StockAccount account = StockAccount.create(holder, releaser, 10.);
			
			account.sellSharesTo(20., holder);
		} catch (final RuntimeException e) {
			return;
		} catch (InsufficientFundsException e) {
            Assert.fail();
        }
	}
	
	@Test
	public final void testDividendTransfer() {
		final TestStockReleaser firm = new TestStockReleaser();
		final TestStockHolder stockOwner = new TestStockHolder();
		final double pricePerShare = 10;
        UniqueStockExchange.Instance.addStock(firm, pricePerShare);
		
		final double quantity = 1;
		
		assertTrue( firm.getAssets().isEmpty() );
		assertEquals( stockOwner.getSavings(), 0.0, TestConstants.DELTA );
		
		StockAccount.create(stockOwner, firm, quantity);
		
		while(Simulation.getCycleIndex() < 1) {
		   System.out.printf("time: %10.5g cycle: %d\n",
		      Simulation.getTime(), Simulation.getCycleIndex());
		   simState.schedule.step( simState );
		}
		
		assertEquals( firm.getAssets().get( 0 ).getValue(), 10.0, TestConstants.DELTA );
		assertEquals( stockOwner.getSavings(), 1.0, TestConstants.DELTA );
	}

	@Test
	public final void testDividendTransferAfterSale() throws OrderException {
		final TestStockReleaser firm = new TestStockReleaser();
		final TestStockHolder stockOwner = new TestStockHolder();
		final double pricePerShare = 10.;
        UniqueStockExchange.Instance.addStock(firm, pricePerShare);
		
		final double quantity = 1;
		final String stockName = firm.getUniqueName();
		
		assertTrue( firm.getAssets().isEmpty() );
		assertEquals( stockOwner.getSavings(), 0.0, TestConstants.DELTA );
		
		StockAccount.create(stockOwner, firm, quantity);
		
        while(Simulation.getCycleIndex() < 1) {
           System.out.printf("time: %10.5g cycle: %d\n",
              Simulation.getTime(), Simulation.getCycleIndex());
           simState.schedule.step( simState );
        }
		
		assertEquals( firm.getAssets().get( 0 ).getValue(), 10.0, TestConstants.DELTA );
		assertEquals( stockOwner.getSavings(), 1.0, TestConstants.DELTA );
		
		final TestStockHolder stockOwner2 = new TestStockHolder();
		stockOwner2.savings = 10;
		final StockMarket market = new StockMarket();
		
		try {
			market.addBuyOrder(stockOwner2, stockName, quantity, pricePerShare);
		} catch (AllocationException e) {
			fail();
		}
		try {
			market.addSellOrder(stockOwner, stockName, quantity, pricePerShare);
		} catch (AllocationException e) {
			fail();
		}

		simState.schedule.step( simState );

		assertEquals( stockOwner.getSavings(), 11.0, TestConstants.DELTA );
		assertEquals( stockOwner2.getSavings(), 11.0, TestConstants.DELTA );

	}

	@Test
	public final void testTryingToSellMoreThanTheOwnedStocks() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			final TestStockReleaser firm = new TestStockReleaser();
			final double pricePerShare = 10;
	        UniqueStockExchange.Instance.addStock(firm, pricePerShare);
			final String instrument = firm.getUniqueName();
			
			StockAccount.create(seller, firm, 2);
			
			try {
				stockMarket.addOrder( seller, instrument, 3.0, pricePerShare );
			} catch (AllocationException e) {
				fail();
			}
		} catch (final OrderException e) {
			return;
		}
		catch(RuntimeException e) {
		   Assert.fail();
		}
	}
	
	@Test
	public final void testSellingHalfOfTheOwnedStocks() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			final TestStockHolder buyer = new TestStockHolder();
			buyer.savings = 10.;
			final TestStockReleaser firm = new TestStockReleaser();
			
			final String instrument = firm.getUniqueName();
			final double pricePerShare = 10.;
			UniqueStockExchange.Instance.addStock(firm, pricePerShare);
			
			StockAccount.create(seller, firm, 2.);
			
			try {
				stockMarket.addOrder( buyer, instrument, - 1.0, pricePerShare );
				assertEquals( buyer.allocatedCash, pricePerShare );
			} catch (AllocationException e) {
				fail();
			}
			try {
				stockMarket.addOrder( seller, instrument, 1.0, pricePerShare );
			} catch (AllocationException e) {
				fail();
			}
			
			simState.schedule.step( simState );
			
			assertEquals( buyer.getNumberOfSharesOwnedIn( instrument ), 1.0, 1.e-12);
			assertEquals( seller.getNumberOfSharesOwnedIn( instrument ), 1.0, 1.e-12);
		} catch (final OrderException e) {
			fail( "Unexpected error", e );
		}
	}
	
	@Test
	public final void testSellingAllStocks() {
		try {
			final TestStockHolder seller = new TestStockHolder();
			final TestStockHolder buyer = new TestStockHolder();
			buyer.savings = 10;
			final TestStockReleaser firm = new TestStockReleaser();
			
			final String stockName = firm.getUniqueName();
			final double quantity = 1.;
			final double pricePerShare = 10;
			UniqueStockExchange.Instance.addStock(firm, pricePerShare);
			
			StockAccount.create(seller, firm, quantity);
			
			assertTrue( seller.hasShareIn( stockName ) );
			assertEquals( seller.getNumberOfSharesOwnedIn( stockName ), quantity );
			
			// Sell them all
			try {
				stockMarket.addOrder( buyer, stockName, - quantity, pricePerShare );
			} catch (AllocationException e) {
				fail();
			}
			try {
				stockMarket.addOrder( seller, stockName, quantity, pricePerShare );
			} catch (AllocationException e) {
				fail();
			}
			
			simState.schedule.step( simState );
			
			assertTrue( ! seller.hasShareIn( stockName ) );
			assertTrue( buyer.hasShareIn( stockName ) );
			assertEquals( buyer.getNumberOfSharesOwnedIn( stockName ), quantity );
		} catch (final OrderException e) {
			fail( "Unexpected error", e );
		}
	}
	
    private static void testMethod(String methodName) {
       StockMarketTest test = new StockMarketTest();
       test.setUp();
       System.out.println(
          "Test: " + test.getClass().getSimpleName() + "." + methodName + " begins.");
       try {
          test.getClass().getDeclaredMethod(methodName).invoke(test);
       } catch (Exception e) {
          Assert.fail("StockMarketTest: method " + methodName + " does not exist.");
       }
       test.tearDown();
       System.out.println(
          "Test: " + test.getClass().getSimpleName() + "." + methodName + " ends.");
    }
    
    static public void main(String[] args) {
       testMethod("testBuyerHasNotEnoughMoney");
       testMethod("testDividendTransfer");
       testMethod("testDividendTransferAfterSale");
       testMethod("testErrorIfNoMoneyToPayDividends");
       testMethod("testErrorIfStockDividendsAreNegative");
       testMethod("testGenerateTickerSymbol");
       testMethod("testGetStockInstrumentValidatesParam");
       testMethod("testInitialStockAccountSetup");
       testMethod("testParseNameFromTickerSymbol");
       testMethod("testParseNameFromTickerSymbolForInvalidInput");
       testMethod("testSameInstrumentAddedTwiceDoesNotHaveAnyEffect");
       testMethod("testSellingAllStocks");
       testMethod("testSellingHalfOfTheOwnedStocks");
       testMethod("testSellingWorksOnAccount");
       testMethod("testStockInstrumentAddsNonExistentInstrumentWithProperName");
       testMethod("testStockInstrumentHandlesNonExistentInstrument");
       testMethod("testStockInstrumentValidatesParam");
       testMethod("testTryingToSellMoreSharesThanOwned");
       testMethod("testTryingToSellMoreThanTheOwnedStocks");
    }
}
