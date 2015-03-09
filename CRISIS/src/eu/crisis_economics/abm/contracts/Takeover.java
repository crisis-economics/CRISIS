/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.contracts;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;

/**
 * A contract to arrange a takeover after bankruptcy. 
 * @author bochmann
 */
public final class Takeover extends TwoPartyContract {
   
   private final Settlement settlement;
   
   /**
     * An entity taken over by a bank. 
     * @param liquidatedEntity 
     * The entity subject to a takeover. This agent must, in addition, implement the
     * SettlementParty interface.
     * @param takeoverEntity The entity performing a takeover.
     */
   public Takeover(
      final Agent liquidatedEntity,
      final Bank takeoverEntity
      ) {
      super(takeoverEntity, (SettlementParty)liquidatedEntity);
      this.settlement = SettlementFactory.createDirectSettlement(
         (SettlementParty)liquidatedEntity, takeoverEntity);
      
      // terminate liability loan contracts
      for (final Loan loan : liquidatedEntity.getLiabilitiesLoans()) {
         loan.terminateContract();
      }
      // transfer liability deposits to new bank
      for (final DepositAccount deposit :
         liquidatedEntity.getLiabilitiesDeposits()) {
         deposit.setDepositHolder(takeoverEntity);
      }
      // TODO
      for (final Contract contract : liquidatedEntity.getLiabilities()) {
         System.out.println("Can't terminate liability " + contract.toString());
      }
      
      // transfer asset loan contracts
      for (final Loan loan : liquidatedEntity.getAssetLoans()) {
         loan.transferToNewLender(takeoverEntity);
      }
      // transfer asset deposit contracts (cash of firms and households)
      for (final DepositAccount deposit : liquidatedEntity.getAssetDeposits()) {
         try {
            this.settlement.transfer(deposit.getFaceValue());
         } catch (InsufficientFundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         deposit.terminate();
      }
      // transfer asset stock account contracts
      for (final StockAccount stockAccount : liquidatedEntity.getAssetStockAccounts()) {
         final StockHolder newStockHolder = (StockHolder)takeoverEntity;
         stockAccount.transferSharesTo(newStockHolder);
      }
      // transfer cash reserves of failed bank
      // TODO this may also contain other kind of contracts that need different
      // treatment!
      for (final Contract contract : liquidatedEntity.getAssets()) {
         try {
            this.settlement.transfer(contract.getFaceValue());
         } catch (InsufficientFundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      System.out.println("Agent " + liquidatedEntity.toString() 
         + " is taken over by bank " + takeoverEntity.toString());
   }
   
   public final Bank getTakeoverEntity() {
      return (Bank) super.getFirstSettlementParty();
   }
   
   public final Agent getLiquidatedEntity() {
      return (Agent) super.getSecondSettlementParty();
   }

   @Override
   public double getValue() {
      return 0.;
   }

   @Override
   public void setValue(double newValue) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double getFaceValue() {
      return 0;
   }

   @Override
   public void setFaceValue(double value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double getInterestRate() {
      return 0;
   }

   @Override
   public void setInterestRate(double interestRate) {
      throw new UnsupportedOperationException();
   }
}
