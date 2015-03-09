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
package eu.crisis_economics.abm.contracts.settlements;

import java.util.Set;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Skeletal implementation of a two party settlement.
  * @author phillips
  */
abstract class AbstractTwoPartySettlement implements Settlement {
   private final SettlementParty
      firstParty,
      secondParty;
   
   private final SettlementPartyNotifier
      partyNotifier;
    
   private Set<SettlementListener>
      listeners;
   
   /**
     * @param firstParty
     *        The first settlement party (the source of funds, for regular directed transfers).
     * @param secondParty
     *        The second settlement party (the receiver of funds, for regular directed transfers).
     * @param partyNotifier
     *        A notification object. This object performs a custom notification whenever
     *        the paying settlement party is credited, and subsequently when the receiving
     *        settlement party is debited.
     */
   AbstractTwoPartySettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty,
      final SettlementPartyNotifier partyNotifier
      ) {
      StateVerifier.checkNotNull(firstParty, secondParty, partyNotifier);
      this.firstParty = firstParty;
      this.secondParty = secondParty;
      this.partyNotifier = partyNotifier;
      this.listeners = SettlementListeners.getInstance();
   }
   
   /**
     * @param firstParty
     *        The first settlement party (the source of funds, for regular directed transfers).
     * @param secondParty
     *        The second settlement party (the receiver of funds, for regular directed transfers).
     * This constructor generates a settlement object with no credit/debit 
     * party notifications.
     */
   AbstractTwoPartySettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty
      ) {
      this(firstParty, secondParty, new SimpleSettlementPartyNotifier());
   }
   
   /**
     * Transfers funds from the first settlement party to the second settlement
     * party. The quantity positiveCashAmount should be non-negative. Otherwise, 
     * an error is logged and no further action is taken.
     *  
     * @param amt amount of funds to transfer
     * @throws InverseTransferException negative amt
     * @throws InsufficientFundsException insufficient funds in A
     */
   @Override
   public final void transfer(final double positiveCashAmount)
      throws InsufficientFundsException {
      if(positiveCashAmount < 0.)
         System.err.println(
            "TwoPartySettlement.transfer: cannot transfer a negative amount (value "
          + positiveCashAmount + "). No action was taken.");
      double amountRecieved = executeDirectedTransfer(firstParty, secondParty, positiveCashAmount);
      notifyListenersOfTransfer(amountRecieved);
   }
    
   /**
     * Transfers funds between a pair of settlement parties. If amountToTransfer
     * is positive, an attempt is made to transfer funds from the first
     * settlement party to the second settlement party. Otherwise, an attempt
     * is made to transfer (-amountToTransfer) from the second settlement 
     * party to the first settlement party.
     * 
     * Should the transaction fail, InsufficientFundsException is raised.
     */
   @Override
   public final void bidirectionalTransfer(final double amountToTransfer)
      throws InsufficientFundsException {
      if(amountToTransfer == 0.) return;
      double amountRecieved;
      if (amountToTransfer > 0.)
         amountRecieved = executeDirectedTransfer(firstParty, secondParty, amountToTransfer);
      else
         amountRecieved = executeDirectedTransfer(secondParty, firstParty, -amountToTransfer);
      notifyListenersOfTransfer(amountToTransfer >= 0 ? amountRecieved : -amountRecieved);
   }
   
   /**
     * Attempt to execute a directed transfer of funds. The argument 
     * amountToTransfer should be non-negative. Otherwise, the behaviour
     * of this method is undefined.
     * 
     * @return The amount of funds recieved by the target.
     */
   protected abstract double executeDirectedTransfer(
      final SettlementParty source,
      final SettlementParty target,
      final double amountToTransfer
      ) throws InsufficientFundsException;
   
   @Override
   public final void addListener(final SettlementListener listener) {
      this.listeners.add(listener);
   }
   
   @Override
   public final void removeListener(final SettlementListener listener) {
      this.listeners.remove(listener);
   }
   
   protected void notifyListenersOfTransfer(final double amount) {
      for (final SettlementListener listener : listeners)
         listener.transferDone(new SettlementEvent(this, amount));
   }
   
   /**
     * Get the first settlement party.
     */
   @Override
   public SettlementParty getFirstParty() {
      return firstParty;
   }
   
   /**
     * Get the second settlement party.
     */
   @Override
   public SettlementParty getSecondParty() {
      return secondParty;
   }
   
   @Override
   public void setListeners(final Set<SettlementListener> listeners) {
      this.listeners = listeners;
   }
   
   /**
     * Notify the paying settlement party of a fund withdrawal.
     */
   protected final void postCredit(double amountCredited) {
      partyNotifier.postCredit(amountCredited);
   }
   
   /**
     * Notify the receiving settlement party of a debit.
     */
   protected final void postDebit(double amountDebited) {
      partyNotifier.postDebit(amountDebited);
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Two party settlement, first party: " + getFirstParty().getUniqueName() 
           + ", second party: " + getSecondParty().getUniqueName() + ".";
   }
}