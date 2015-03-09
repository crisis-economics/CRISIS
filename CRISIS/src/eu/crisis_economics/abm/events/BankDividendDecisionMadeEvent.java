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
package eu.crisis_economics.abm.events;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.loans.Loan;

/**
  * An {@link Event} which signals that a {@link Bank} {@link Agent} has
  * decided its next dividend payment. This {@link Event} contains immutable
  * information about:
  * 
  * <ul>
  *   <li> The size of the dividend payment to be made.
  *   <li> The leverage state the {@link Bank} would be in immediately after the
  *        payment of this dividend.
  * </ul>
  * 
  * Note that this object only specifies the dividend decided by the {@link Bank}
  * {@link Agent}. It may be the case, due for instance to cash reserve of equity
  * constraints, that the {@link Bank} is not able to pay its intended dividend
  * when this payment becomes due.<br><br>
  * 
  * @author phillips
  */
public final class BankDividendDecisionMadeEvent implements Event {
   final String
      bankName;
   final double
      dividendPlanned,
      leverageAfterDividend,
      effectiveRiskyAssetsValue,
      effectiveEquity,
      assetsInLoans,
      assetsInStocks,
      cashReserveAfterDividend;
   
   /**
     * Create an immutable {@link BankDividendDecisionMadeEvent} {@link Event}.
     * 
     * @param bankName
     *        The unique name of the {@link Bank} that has made a dividend decision.
     * @param dividendPlanned
     *        The size of the planned dividend
     * @param leverageAfterDividend
     *        The leverage state of the {@link Bank} at the instant the dividend
     *        decision was made after taking into account the expected dividend
     *        payment.
     * @param assetsInLoans
     *        The assets held by the {@link Bank} in {@link Loan}{@code s} at the 
     *        moment this event was posted.
     * @param assetsInStocks
     *        The assets held by the {@link Bank} in stocks at the moment this event 
     *        was posted.
     * @param effectiveCashReserve
     *        The cash reserve held by the {@link Bank} after pending dividends have
     *        been subtracted.
     */
   public BankDividendDecisionMadeEvent(    // Immutable
      final String bankName,
      final double dividendPlanned,
      final double leverageAfterDividend,
      final double effectiveRiskyAssetsValue,
      final double effectiveEquity,
      final double assetsInLoans,
      final double assetsInStocks,
      final double effectiveCashReserve
      ) {
      this.bankName = Preconditions.checkNotNull(bankName);
      this.dividendPlanned = dividendPlanned;
      this.leverageAfterDividend = leverageAfterDividend;
      this.effectiveRiskyAssetsValue = effectiveRiskyAssetsValue;
      this.effectiveEquity = effectiveEquity;
      this.assetsInLoans = assetsInLoans;
      this.assetsInStocks = assetsInStocks;
      this.cashReserveAfterDividend = effectiveCashReserve;
   }
   
   public String getBankName() {
      return bankName;
   }
   
   public double getDividendPlanned() {
      return dividendPlanned;
   }
   
   public double getLeverageAfterDividend() {
      return leverageAfterDividend;
   }
   
   public double getEffectiveRiskyAssetsValue() {
      return effectiveRiskyAssetsValue;
   }
   
   public double getEffectiveEquity() {
      return effectiveEquity;
   }
   
   public double getAssetsInLoans() {
      return assetsInLoans;
   }
   
   public double getAssetsInStocks() {
      return assetsInStocks;
   }
   
   public double getCashReserveAfterDividend() {
      return cashReserveAfterDividend;
   }
}
