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

import java.util.List;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Simple implementation of the Depositor interface.
  */
final class MDepositor implements Depositor {

   public <T extends Depositor & Party> MDepositor(
      final T parent, final DepositHolder bank, final double initialCash) {
      this(parent, parent, bank, initialCash);
   }
   
   public <T extends Depositor & Party> MDepositor(
      final T parent, final DepositAccount settlementAccount) {
      this(parent, parent, settlementAccount);
   }
   
   public MDepositor(
      final Depositor owner, final Party party,
      final DepositHolder bank, final double initialCash
      ) {
      this.iHasAssets = owner;
      initCommon(owner, party, new DepositAccount(bank, owner, initialCash, 0.));
   }
   
   public MDepositor(HasAssets assets, Party party, DepositAccount settlementAccount) {
      this.iHasAssets = assets;
      initCommon(assets, party, settlementAccount);
   }
	
	private void initCommon(
	   final HasAssets assets, final Party party, final DepositAccount settlementAccount) {
	   StateVerifier.checkNotNull(assets, party, settlementAccount);
       iParty = party;
       depositAccount = settlementAccount;
	}

	DepositAccount depositAccount;

	public double getBalance() {
		return(depositAccount.getValue());
	}
	
	public DepositAccount getSettlementAccount() {
		return(depositAccount);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Settlement party redirection
	///////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public double credit(double positiveCashAmount) throws InsufficientFundsException {
		depositAccount.withdraw(positiveCashAmount);
		return(positiveCashAmount);
	}
	
	@Override
	public void debit(double positiveCashAmount) {
		depositAccount.deposit(positiveCashAmount);
	}
	
	@Override
	public void cashFlowInjection(double positiveCashAmount) {
       if (positiveCashAmount < 0.)
          throw new IllegalArgumentException(
             "Depositor.cashFlowInjection: cash flow injection amount is negative.");
       System.err.println(
          "Depositor.cashFlowInjection: cash flow injection is not implemented for this type.");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Party interface
	/////////////////////////////////////////////////////////////////////////////////////////////////

	Party iParty;
	
	@Override
	public void addOrder(Order order) {
		iParty.addOrder(order);
	}

	@Override
	public boolean removeOrder(Order order) {
		return(iParty.removeOrder(order));
	}

	@Override
	@Deprecated
	public void updateState(Order order) {
		iParty.updateState(order);
	}

	@Override
	public String getUniqueName() {
		return(iParty.getUniqueName());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// HasAssets interface
	////////////////////////////////////////////////////////////////////////////////////////////////

	HasAssets iHasAssets;
	
	@Override
	public void addAsset(Contract asset) {
		iHasAssets.addAsset(asset);
	}

	@Override
	public boolean removeAsset(Contract asset) {
		return(iHasAssets.removeAsset(asset));
	}
   
   @Override
   public List<Contract> getAssets() {
      return(iHasAssets.getAssets());
   }
   
   @Override
   public double getDepositValue() {
      return depositAccount.getValue();
   }
   
	/**
      * Returns a brief description of this object. The exact details of the
      * string are subject to change, and should not be regarded as fixed.
	  */
    @Override
    public String toString() {
       return "MDepositor, deposit account value: " + depositAccount.getValue() + ".";
    }
}
