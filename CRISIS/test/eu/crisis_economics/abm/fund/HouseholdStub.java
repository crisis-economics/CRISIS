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
package eu.crisis_economics.abm.fund;


import java.util.Random;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;

public class HouseholdStub extends Agent implements Depositor {
	
	public HouseholdStub(Fund fund, Bank myBank, double openingBalance) {
		bankAccount		= new MDepositor(this, myBank, openingBalance);
		myFund			= fund;
		rand = new Random();
		rand.setSeed(1234);
	}
	
	public void doSomeStuff() throws InsufficientFundsException {
		FundTest.printBalanceSheet("MutualFund", myFund);
//		printBalanceSheet("Household", iHouseholdAgent);
		
		System.out.println("Investing 100...");
		
		myFund.invest(this,100);

		FundTest.printBalanceSheet("MutualFund", myFund);
		FundTest.printBalanceSheet("Household", this);
			
		System.out.println("Withdrawing 50...");

		myFund.requestWithdrawal(this, 50);
		
		FundTest.printBalanceSheet("MutualFund", myFund);
		FundTest.printBalanceSheet("Household", this);

	}
	
	
	public double investmentRandomAmount() throws InsufficientFundsException {
		double desiredInvestment = getEquity()*rand.nextDouble();
		if(desiredInvestment >= myFund.getBalance(this)) {
			myFund.invest(this, desiredInvestment-myFund.getBalance(this));
		} else {
			myFund.requestWithdrawal(this, myFund.getBalance(this)-desiredInvestment);			
		}
		return(desiredInvestment);
	}
	
	
	public Fund myFund;	
	public MDepositor bankAccount;
	
	@Override
	public double credit(double amt) throws InsufficientFundsException {
		return(bankAccount.credit(amt));
	}

	@Override
	public void debit(double amt) {
		bankAccount.debit(amt);
	}

	@Override
	public void cashFlowInjection(double amt) {
		bankAccount.cashFlowInjection(amt);
	}
	
	Random rand;

   @Override
   public double getDepositValue() {
      return bankAccount.getBalance();
   }
}
