/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
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
package eu.crisis_economics.abm.bankruptcy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.engine.Steppable;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.firm.bankruptcy.FirmBankruptcyHandler;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.BankStrategyFactory;
import eu.crisis_economics.utilities.ArrayUtil;
import eu.crisis_economics.utilities.Pair;

/**
 * @author  jakob e o frate ro cazz
 */
public class BankruptcyHandlerTest {
	public static final double E = 0.0001; 
	private Simulation simState;
	
	private StockMarket stockMarket;
	private MyBankruptcyHandler bankruptcyHandlerToTest;
	private MyBank bank0; 
	private MyBank bank1;
	private MyBank bank2;
	
	private class MyBankruptcyHandler implements FirmBankruptcyHandler {
		
     public void initializeBankruptcyProcedure(Firm agent){
        if(!(agent instanceof MyFirm)) {
           System.err.println(getClass().getSimpleName() + ": this bankruptcy handler applies "
              + "to firm agents of type " + MyFirm.class.getSimpleName() + " only.");
           System.err.flush();
           return;
        }
        final MyFirm
           firm = (MyFirm) agent;
        
			//get liabilities and their value
	    	List<Loan> liabilities = firm.getLiabilitiesLoans();
	    	
	    	double sumLiabilities = 0;
	    	for (int i=0; i<liabilities.size();i++){
	    		sumLiabilities += liabilities.get(i).getValue();
	    	}
	    	
	    	ArrayList<Double> shares = new ArrayList<Double>();
	    	ArrayList<Lender> lenders = new ArrayList<Lender>();
	    	
	    	for (int i=0; i<liabilities.size();i++){
	    		if (liabilities.get(i) instanceof Loan){
	    		double value = liabilities.get(i).getValue();
	    		Lender owner = liabilities.get(i).getLender();
	    		
	    		shares.add(value/sumLiabilities);
	    		lenders.add(owner);
	    		
	    		liabilities.get(i).terminateContract();
	    		
	    		}
	    	}
	    	
	    	//get asset value
	    	double assetValue = firm.getTotalAssets();
	    	
	    	//eliminate orders in the instrument trading the stocks of this firm
	    	String stockname = firm.getUniqueName();
	    	StockInstrument instrument = (StockInstrument) stockMarket.getInstrument(stockname);
	    	if (instrument != null){
	    		instrument.cancelAllOrders();
	    	}
	    	
	    	
	    	//get stock accounts and terminate them. The info about the share holders is in stockReleasing firm
	    	Collection<StockAccount> stockAccounts = firm.getStockAccounts();
	    	
	    	//I need to iterate over the stockAccounts to terminate them 
	    	for (StockAccount stockAccount : stockAccounts) {
	    		stockAccount.terminateAccountWithoutCompensatingStockHolder();
	        }
	    	
	    	//put to zero number of emittedShare!	
	    	// firm.setNumberOfEmittedShares(0);
	    	
	    	
	    	// I am going to use the new realeseFirm method defined in MacroFirm
	        // compute the value of the lambda*assets. Decide the total number of 
	    	// share. Give to each lender share*number_of_share
	    	// double total_number_of_new_shares = 100;
	    	double totalNumberOfSharesToEmit = firm.getNumberOfEmittedShares();
	    	
	    	//the release firm method has as inputs: the lender/new stock holder, the price_per_share and the number_of_share to the specific lender

            List<Double> sharesEmittedToStockHolders = new ArrayList<Double>();
            double sumOfShares = 0.;
            for(int i = 0; i < shares.size(); ++i) {
                double newShares = shares.get(i) * totalNumberOfSharesToEmit;
                sumOfShares += newShares;
                sharesEmittedToStockHolders.add(newShares);
            }
            
            double 
                newFirmTotalEquity = assetValue,
                newFirmShareUnitPrice = newFirmTotalEquity / sumOfShares;
            
            {
            final List<StockHolder> stockHolders = ArrayUtil.castList(lenders);
            UniqueStockExchange.Instance.eraseAndReplaceStockWithoutCompensation(
               firm.getUniqueName(),
               Pair.zip(stockHolders, sharesEmittedToStockHolders),
               newFirmShareUnitPrice
               );
            }
		}
		
		
	}
	
	private class MyFirm extends StockReleasingFirm implements Steppable, Borrower, Party{
		private static final long serialVersionUID = 1L;
		private MyBankruptcyHandler bankruptcyHandlerToTest;

		public MyFirm(DepositHolder depositHolder, double initialDeposit,
				StockHolder stockHolder, double numberOfShares, double emission_price) {
			super(depositHolder, initialDeposit, stockHolder, numberOfShares,  emission_price,
			   new InstanceIDAgentNameFactory());
		}
		
		public void setBankruptcyHandler(MyBankruptcyHandler bankruptcyHandler){
			this.bankruptcyHandlerToTest = bankruptcyHandler;
		}

		@Override
		public void addOrder(Order order) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean removeOrder(Order order) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void updateState(Order order) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void step(SimState state) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void handleBankruptcy(){
			bankruptcyHandlerToTest.initializeBankruptcyProcedure(this);
			
		}

      @Override
      public void registerOutgoingDividendPayment(
         double dividendPayment,
         double pricePerShare, 
         double numberOfSharesHeldByInvestor
         ) { }
	}
   
   private class MyBank extends StrategyBank implements Lender, DepositHolder, StockHolder {
      public MyBank(
         double initialCash) {
         super(initialCash, new BankStrategyFactory() {
            @Override
            public BankStrategy create(final StrategyBank bank) {
               return new EmptyBankStrategy(bank);
            }
         });
		}
	}
	
	@BeforeMethod
	public void setUp(){
		
		System.out.println("Test: " + this.getClass().getSimpleName());
		
		simState = new EmptySimulation(123L);
		simState.start();
		bankruptcyHandlerToTest = new MyBankruptcyHandler();
		stockMarket = new StockMarket();
		
		bank0 = new MyBank(1000);
        bank1 = new MyBank(1000);
		bank2 = new MyBank(1000);
		MyFirm firm = new MyFirm(bank0,100,bank0,100, 1.0);
		firm.setBankruptcyHandler(bankruptcyHandlerToTest);
		
		bank0.getEquity();
		System.out.println(bank0.getEquity());
		System.out.println(bank1.getEquity());
		System.out.println(bank2.getEquity());
		
		
		
		
		try {
		    LoanFactory.createFixedRateMortgage(firm, bank1,100,0.03,10);
		} catch (LenderInsufficientFundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
		    LoanFactory.createFixedRateMortgage(firm, bank2,100,0.03,10);
		} catch (LenderInsufficientFundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//==================================================
		//firm starts with 100 of cash (asset)
		// two loans of 100 rise the cash to 300, the liabilities to 200, and equity stays to 100
		
		// I withdraw money from the firm to force bankruptcy
		
		try {
			firm.credit(200);
		} catch (InsufficientFundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(firm.getEquity());
		
		firm.handleBankruptcy();
		
		System.out.println(firm.getMarketValue());
	}	
	@AfterMethod
	public void tearDown(){
		simState.finish();
	}
	
	@Test
	public void verifyAsynchronousMarket(){
		System.out.println("in test");
		//original stockholder should have a capital loss of 100
		//lenders should have a loss of 50, since 100 of loans is transformed in 50 of equity
		// the overal economic loss is equal to 200, which are exactly the money 
		//"wasted" by the firm (money gone out of the system).
		AssertJUnit.assertEquals(900,bank0.getEquity(), E);
		AssertJUnit.assertEquals(950,bank1.getEquity(), E);
		AssertJUnit.assertEquals(950,bank2.getEquity(), E);
		
		//bank0 has zero stocks
		AssertJUnit.assertEquals(0, bank0.getAssetStockAccounts().size());
		
		//bank1 has a stock with 50.getFaceValue()
		AssertJUnit.assertEquals(1, bank1.getAssetStockAccounts().size());
		AssertJUnit.assertEquals(50, bank1.getAssetStockAccounts().get(0).getFaceValue(), E);
		//bak2 has a stock with 50.getFaceValue()
		AssertJUnit.assertEquals(1, bank2.getAssetStockAccounts().size());
		AssertJUnit.assertEquals(50, bank2.getAssetStockAccounts().get(0).getFaceValue(), E);
		
	    do{  System.out.println(Simulation.getSimState().schedule.getSteps() + " TIME"); 
	    	if (Simulation.getSimState().schedule.getSteps() == 0) {
	    	}
	    		    	
	 	   if (!simState.schedule.step(simState)) {
        		//System.out.println(((Labour) firm1.getLabourList().get(1)).getWage());        		
        		break;
        	}
        	
        //cancel all orders at the end of each period
	 	
	 	
	    } while(Simulation.getSimState().schedule.getSteps() < 1);
	   
	}
	
	public static void main(String[] args) {
		BankruptcyHandlerTest test = new BankruptcyHandlerTest();
		test.setUp();
		test.verifyAsynchronousMarket();
		test.tearDown();
		System.out.println("BankruptcyHandler tests pass.");
	}
}
