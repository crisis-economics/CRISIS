/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Olaf Bochmann
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

/**
 * An infrastructure to transfer funds from one party to another.
 * 
 * @author phillips
 * @author olaf
 */
public interface Settlement {
   /**
     * Transfers funds from the first settlement party to the second settlement
     * party. The quantity positiveCashAmount should be non-negative. Otherwise, 
     * an error is logged and no further action is taken.
     *  
     * @param amt amount of funds to transfer
     * @throws InverseTransferException negative amt
     * @throws InsufficientFundsException insufficient funds in A
     */
   public void transfer(final double positiveCashAmount)
      throws InsufficientFundsException;
    
   /**
     * Transfers funds between a pair of settlement parties. If amountToTransfer
     * is positive, an attempt is made to transfer funds from the first
     * settlement party to the second settlement party. Otherwise, an attempt
     * is made to transfer (-amountToTransfer) from the second settlement 
     * party to the first settlement party.
     * 
     * Should the transaction fail, InsufficientFundsException is raised.
     */
   public void bidirectionalTransfer(final double amountToTransfer)
      throws InsufficientFundsException;
   
   public void addListener(final SettlementListener listener);
   
   public void setListeners(final Set<SettlementListener> listeners);
   
   public void removeListener(final SettlementListener listener);
   
   /**
     * Get the primary settlement party (source of funds during regular transfer)
     */
   public SettlementParty getFirstParty();
   
   /**
     * Get the secondary settlement party (recipient of funds during regular transfer)
     */
   public SettlementParty getSecondParty();
}