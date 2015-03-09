/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.fund;

import java.util.List;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Order;

/**
  * Simple implementation of the Depositor interface. This implementation
  * deposits its cash in an exogenous source not known to the simulation.
  * The underlying exogenous deposit holder does not respond to any 
  * stimuli, cannot register assets or liabilities, and cannot raise any 
  * exception, including when incrementing its cash reserves.
  */
public final class ExogenousDepositor implements Depositor {
   
   private final DepositAccount depositAccount;
   private final Party party;
   private final HasAssets hasAssets;
   
   public <T extends HasAssets & Party> ExogenousDepositor(
      final T parent,
      final double initialCash
      ) {
      this(parent, parent, initialCash);
   }
   
   public ExogenousDepositor(
      final HasAssets assets,
      final Party party,
      final double initialCash
      ) {
      Preconditions.checkNotNull(assets);
      Preconditions.checkNotNull(party);
      Preconditions.checkArgument(initialCash >= 0.);
      this.hasAssets = assets;
      this.party = party;
      ExogenousDepositHolder exogenousDepositHolder = new ExogenousDepositHolder();
      this.depositAccount = new DepositAccount(exogenousDepositHolder, this, initialCash, 0.);
   }
   
   /** Exogenous deposit holder with no response to stimuli.
     * 
     * @author phillips
     */
   private final class ExogenousDepositHolder implements DepositHolder {
      @Override
      public void addLiability(Contract liability) { }
      
      @Override
      public boolean removeLiability(Contract liability) { return false; }
      
      @Override
      public List<Contract> getLiabilities() { return null; }
      
      @Override
      public boolean isBankrupt() { return false; }
      
      @Override
      public void handleBankruptcy() { }
      
      @Override
      public void decreaseCashReserves(double amount) throws LiquidityException { }
      
      @Override
      public void increaseCashReserves(double amount) { }
      
      @Override
      public void cashFlowInjection(double amount) { }
   }
   
   public DepositAccount getSettlementAccount() {
      return depositAccount;
   }
   
   @Override
   public double credit(final double positiveCashAmount) 
      throws InsufficientFundsException {
      depositAccount.withdraw(positiveCashAmount);
      return positiveCashAmount;
   }
   
   @Override
   public void debit(final double positiveCashAmount) {
      depositAccount.deposit(positiveCashAmount);
   }
   
   @Override
   public void cashFlowInjection(final double amount) { }
   
   @Override
   public void addOrder(Order order) {
      party.addOrder(order);
   }
   
   @Override
   public boolean removeOrder(Order order) {
      return party.removeOrder(order);
   }
   
   @Override
   @Deprecated
   public void updateState(Order order) {
      party.updateState(order);
   }
   
   @Override
   public String getUniqueName() {
      return party.getUniqueName();
   }
   
   @Override
   public void addAsset(final Contract asset) {
      hasAssets.addAsset(asset);
   }
   
   @Override
   public boolean removeAsset(final Contract asset) {
      return (hasAssets.removeAsset(asset));
   }
   
   @Override
   public List<Contract> getAssets() {
      return (hasAssets.getAssets());
   }
   
   @Override
   public double getDepositValue() {
      return depositAccount.getValue();
   }
}
