/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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
package eu.crisis_economics.abm.bank;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;

/**
  * This is a bank stub implementation which overrides {@link #credit} and {@link #debit}
  * methods in such a way that this {@link Bank} has a virtually unlimited cash reserve.
  * The cash supply of this {@link Bank} {@link Agent} is taken to be exogenous and unlimited.
  * <br><br>
  * 
  * When used in conjunction with the {@link FixedLoanSupplyClearingStrategy}, this
  * {@link Bank} provides a fixed customizable loan supply.
  * 
  * @author lovric
  */
public final class BankStub extends CommercialBank {
   
   @Inject
   public BankStub(
      final ClearingBankStrategyFactory strategyFactory,
   @Named("BANK_CLEARING_MARKET_RESPONSES")
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final ClearingHouse clearingHouse
      ) {
      super(Double.MAX_VALUE, strategyFactory, marketParticipationFactory, clearingHouse);
   }
   
   /**
     * Withdraw any positive amount of cash. This method is equivalent to
     * {@code return(Math.max(amountToWithdraw, 0))}.
     */
   @Override
   public final double credit(double amountToWithdraw) {
      return Math.max(amountToWithdraw, 0.);
   }
   
   /**
     * Debit any amount of cash. This method does nothing, and cannot fail,
     * in this implementation.
     */
   @Override
   public final void debit(double amountToDeposit) {
      // No action.
   }
   
   /**
     * Returns {@code Double.MAX_VALUE}.
     */
   @Override
   public double getCashReserveValue() {
      return Double.MAX_VALUE;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Bank Stub, assets not including exogenous cash: " + getTotalAssets() + ".";
   }
}
