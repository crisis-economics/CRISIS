/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

import cern.jet.random.AbstractDistribution;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.StockTrader;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.markets.nonclearing.DepositInstrument.AccountType;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

public class RandomStrategicBankTest {
    
    private final class PartyImplementation 
        implements Party, Depositor, Borrower, Lender, StockHolder, StockTrader {
            
            private final List<Order> orders = new ArrayList<Order>();
            private final List<Contract> assets = new ArrayList<Contract>();
            private final List<Contract> liabilities = new ArrayList<Contract>();
            private final String uniqueID;
            
            private double
                cash = 0.,
                allocatedCash = 0.;
            
            PartyImplementation(double cash) {
                this.cash = cash;
                this.uniqueID = UUID.randomUUID().toString();
            }
            
            @Override
            public void updateState(Order order) {
                // Intentionally left blank
            }
            
            @Override
            public boolean removeOrder(Order order) {
                return orders.remove(order);
            }
            
            @Override
            public void addOrder(Order order) {
                orders.add(order);
            }
            
            @Override
            public double credit(double amt)
                throws InsufficientFundsException {
                if (cash < amt)
                    throw new InsufficientFundsException();
                cash -= amt;    
                return amt;
            }
            
            @Override
            public void debit(double amt) {
                cash += amt;
            }
            
            @Override
            public void addAsset(Contract asset) {
                assets.add(asset);
            }
            
            @Override
            public boolean removeAsset(Contract asset) {
                return assets.remove(asset);
            }
            
            @Override
            public double getNumberOfSharesOwnedIn(String instrumentName) {
                StockAccount account = getStockAccount(instrumentName);
                if (account == null)
                    return 0;
                return account.getQuantity();
            }
            
            @Override
            public boolean hasShareIn(String instrumentName) {
                return getNumberOfSharesOwnedIn(instrumentName) > 0;
            }
            
            @Override
            public StockAccount getStockAccount(String stockName) {
                for (Contract contract : assets) {
                    if (contract instanceof StockAccount) {
                        StockAccount stockAccount = (StockAccount) contract;
                        if (stockAccount.getInstrumentName().equals(stockName)) {
                            return stockAccount;
                        }
                    }
                }
                return null;
            }
            
            @Override
            public void addLiability(Contract liability) {
                liabilities.add(liability);
            }
            
            @Override
            public boolean removeLiability(Contract liability) {
                return liabilities.remove(liability);
            }
            
            @Override
            public List<Contract> getLiabilities() {
                return liabilities;
            }
            
            @Override
            public List<Contract> getAssets() {
                return assets;
            }
            
            @Override
            public Set<StockMarket> getStockMarkets() { return null; }
            
            @Override
            public void handleBankruptcy() {
                Simulation.getSimState().finish();
            }
            
            @Override
            public boolean isBankrupt() { return false; }
    
            @Override
            public void allocateShares(String tickerSymbol, double volume)
                throws AllocationException {
                if(this.getStockAccount(tickerSymbol).getQuantity()
                   - this.getStockAccount(tickerSymbol).allocatedShares < volume)
                {
                    double unallocatedShares = 
                        this.getStockAccount(tickerSymbol).getQuantity() - 
                        this.getStockAccount(tickerSymbol).allocatedShares; 
                    throw new AllocationException(
                        "RandomStrategicBankTest: unallocated shares ( " +
                        unallocatedShares + " ) < volume ( " + volume + " )" +
                        " at step " + Simulation.getSimState().schedule.getSteps());
                } 
                else this.getStockAccount(tickerSymbol).allocatedShares += volume;
            }
            
            @Override
            public void disallocateShares(String tickerSymbol, double volume) {
                StockAccount stockAccount = this.getStockAccount(tickerSymbol);
                if (stockAccount != null) { // If null then probably all sold
                    if (stockAccount.allocatedShares < volume) {
                        throw new IllegalArgumentException(
                           "RandomStrategicBankTest: allocated shares ( "
                           + stockAccount.allocatedShares + " ) < volume ( "
                           + volume + " )");
                    } else
                        this.getStockAccount(tickerSymbol).allocatedShares -= volume;
                }
            }
            
            @Override
            public double allocateCash(double amount) {
               double allocatable = Math.max(cash - allocatedCash, 0.);
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
            public double calculateAllocatedCashFromActiveOrders() {
                double cash = 0.0;
                int i = 0;
                for (Order order : orders) {
                    if (order instanceof StockOrder) {
                        if (((StockOrder) order).getSide() == Order.Side.BUY) {
                            cash += ((StockOrder) order).getOpenSize()
                                    * ((StockOrder) order).getPrice();
                            i++;
                        }
                        if (order instanceof LoanOrder) {
                            if (((LoanOrder) order).getSide() == Order.Side.SELL) {
                                cash += ((LoanOrder) order).getOpenSize();
                                i++;
                            }
                        }
                    }
                }
                if (cash != this.allocatedCash) {
                    throw new RuntimeException(
                        "RandomStrategicBankTest: stockflow inconsistency: allocated cash ( "
                        + this.allocatedCash
                        + " ), differs from acual allocation ( " + cash
                        + " ) in " + i + " active orders.");
                }
                return cash;
            }
            
            @Override
            public void cashFlowInjection(double amt) { }
            
            @Override
            public void registerNewLoanTransfer(
               double principalCashValue, Lender lender, double interestRate) { }
            
            @Override
            public void registerNewLoanRepaymentInstallment(
               double installmentCashValue, Lender lender, double interestRate) { }
            
            @Override
            public double getUnallocatedCash() {
               return Math.max(cash - getAllocatedCash(), 0.);
            }
            
            @Override
            public String getUniqueName() {
               return uniqueID;
            }
            
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
            public double getDepositValue() {
               return cash;
            }
    }
    
    private final class MyRandomStrategicBank
        extends RandomStrategyBank implements StockTrader {
        
        public MyRandomStrategicBank(
            double initialCash, 
            AbstractDistribution orderSizeDistribution,
            AbstractDistribution orderPriceDistribution,
            AbstractDistribution stockInstrumentDistribution) {
            super(initialCash,
                  orderSizeDistribution,
                  orderPriceDistribution,
                  stockInstrumentDistribution
                  );
        }
        
        @Override
        public List<Contract> getAssets() {
            return assets;
        }
        
        @Override
        public List<Contract> getLiabilities() {
            return liabilities;
        }
    }
    
    private DepositMarket depositMarket;
    private CommercialLoanMarket commercialLoanMarket;
    private StockMarket stockMarket;
    
    @BeforeMethod
    public void setUp() {
        System.out.println("Test: " + this.getClass().getSimpleName() + " begins.");
        final Simulation sim = new EmptySimulation(0L);
        sim.start();
        depositMarket = new DepositMarket();
        commercialLoanMarket = new CommercialLoanMarket();
        stockMarket = new StockMarket();
    }

    @AfterMethod
    public void tearDown() {
        Simulation.getSimState().finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " ends.");
    }

    @Test(enabled = false)
    public void ordersAdded() {
        MyRandomStrategicBank bank = new MyRandomStrategicBank(
            100000.0,
            new AbstractDistribution() {
                private static final long serialVersionUID = 1L;
                @Override
                public double nextDouble() {
                    return 1.0;
                }
            },
            new AbstractDistribution() {
                private static final long serialVersionUID = 1L;
                @Override
                public double nextDouble() {
                    return 1.0;
                }
            },
            new AbstractDistribution() {
                private static final long serialVersionUID = 1L;
                @Override
                public double nextDouble() {
                    return 1.0;
                }
            });
        
        bank.addMarket(depositMarket);
        bank.addMarket(commercialLoanMarket);
        bank.addMarket(stockMarket);
        
        StockReleaser releaser = mock(StockReleaser.class);
        when(releaser.getUniqueName()).thenReturn("Stock Releaser");
        UniqueStockExchange.Instance.addStock(releaser, 1.0);
        
        PartyImplementation partyImplementation = 
           new PartyImplementation(1000.0);
        StockAccount.create(
            partyImplementation,
            releaser, 
            1
            );
        
        try {
            depositMarket.addOrder(partyImplementation, AccountType.DEBIT,
                    1000.0, 1.0);
            commercialLoanMarket.addOrder(partyImplementation, 1, -1.0, 1.0);
            stockMarket.addOrder(partyImplementation, releaser.getUniqueName(), 1.0, 1.0);
        } catch (final OrderException e) {
            e.printStackTrace();
            Simulation.getSimState().finish();
        } catch (AllocationException e1) {
            e1.printStackTrace();
            Simulation.getSimState().finish();
        }
        
        while(Simulation.getTime() < 1.0)
           Simulation.getSimState().schedule.step(Simulation.getSimState());
        
        for (Contract contract : bank.getAssets()) {
            if (contract instanceof StockAccount) {
                StockAccount stockAccount = (StockAccount) contract;
                Assert.assertEquals(stockAccount.getInstrumentName(), releaser.getUniqueName());
                Assert.assertEquals(stockAccount.getQuantity(), 1.0);
                Assert.assertEquals(stockAccount.getPricePerShare(), 1.0);
                continue;
            }
            
            if (contract instanceof DepositAccount) {
                final DepositAccount depositAccount = (DepositAccount) contract;
                Assert.assertEquals(depositAccount.getDepositHolder(), bank);
                Assert.assertEquals(depositAccount.getDepositor(),
                        partyImplementation);
                Assert.assertEquals(depositAccount.getInterestRate(), 1.0);
                Assert.assertEquals(depositAccount.getValue(), 1000.0);
                continue;
            }
            
            if (contract instanceof Loan) {
                final Loan loan = (Loan) contract;
                Assert.assertEquals(loan.getBorrower(), partyImplementation);
                Assert.assertEquals(loan.getLender(), bank);
                Assert.assertEquals(loan.getInterestRate(), 1.0);
                Assert.assertEquals(loan.getLoanPrincipalValue(), 1.0);
                continue;
            }
            
            Assert.assertEquals(contract.getValue(), 100998.0);
        }
        
        for (Contract contract : bank.getLiabilities()) {
            if (contract instanceof Loan) {
                final Loan loan = (Loan) contract;
                Assert.assertEquals(loan.getBorrower(), bank);
                Assert.assertEquals(loan.getLender(), partyImplementation);
                Assert.assertEquals(loan.getInterestRate(), 1.0);
                Assert.assertEquals(loan.getLoanPrincipalValue(), 1.0);
            }
        }
        
        for (Contract contract : partyImplementation.getAssets()) {
            if (contract instanceof DepositAccount) {
                final DepositAccount depositAccount = (DepositAccount) contract;
                Assert.assertEquals(depositAccount.getDepositHolder(), bank);
                Assert.assertEquals(depositAccount.getDepositor(),
                        partyImplementation);
                Assert.assertEquals(depositAccount.getInterestRate(), 1.0);
                Assert.assertEquals(depositAccount.getValue(), 1000.0);
                continue;
            }
            
            Loan loan = (Loan) contract;
            Assert.assertEquals(loan.getLender(), partyImplementation);
            Assert.assertEquals(loan.getBorrower(), bank);
            Assert.assertEquals(loan.getInterestRate(), 1.0);
            Assert.assertEquals(loan.getLoanPrincipalValue(), 1.0);
        }
        
        for (Contract contract : partyImplementation.getLiabilities()) {
            final Loan loan = (Loan) contract;
            Assert.assertEquals(loan.getLender(), bank);
            Assert.assertEquals(loan.getBorrower(), partyImplementation);
            Assert.assertEquals(loan.getInterestRate(), 1.0);
            Assert.assertEquals(loan.getLoanPrincipalValue(), 1.0);
        }
    }
    
    /**
      * Manual Entry Point
      */
    static public void main(String[] args) {
       try {
          final RandomStrategicBankTest
             test = new RandomStrategicBankTest();
          test.setUp();
          test.ordersAdded();
          test.tearDown();
       }
       catch(final Exception e) {
          Assert.fail();
       }
    }
}
