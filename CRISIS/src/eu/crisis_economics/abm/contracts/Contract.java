/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.contracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sim.engine.SimState;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author bochmann
  * @author phillips
  */
public abstract class Contract {
    
    private static long contractsCreated;
    private final String contractUID;
    private static List<Contract> allActiveContracts;
    
    static {
       allActiveContracts = new ArrayList<Contract>();
    }
    
    private final double
       contractCreationTime,
       contractExpiryTime;
    
    protected Contract(
       final double expirationTime
       ) {
       this.contractCreationTime = Simulation.getTime();
       this.contractUID = getClass().getName() + contractsCreated++;
       this.contractExpiryTime = expirationTime;
       if(contractExpiryTime < contractCreationTime)
          throw new IllegalArgumentException(
             "Contract: creation time: " + contractCreationTime + " is later than " + 
             "expiration time: " + expirationTime + ".");
       
       storeInstance();
   }
   
   /**
     * Global logging for Contract objects. Ultimately all Contract objects are
     * created via the above constructor. If the Simulation indicates that 
     * Contracts should be recorded, then a singleton ArrayList<Contract>
     * is populated with all new instances. This method executes the logging.
     */
   private void storeInstance() {
      final SimState simState = Simulation.getSimState();
      if (simState != null) {
         if (simState instanceof Simulation) {                      // TODO
            final Simulation simulation = (Simulation) simState;
            if (simulation.isRecordingContracts())
               allActiveContracts.add(this);
         }
      }
   }
   
   /** Get an unmodifiable list of Contracts. This list contains all
     * Contract objects ever created by the running simulation. */
   public static List<Contract> getContracts() {
      return Collections.unmodifiableList(allActiveContracts);
   }
   
   /** Get a unique (String) ID corresponding to this contract. */
   public final String getUniqueId() { 
      return contractUID;
   }
   
   public abstract double getValue();
   
   public abstract void setValue(double newValue);
   
   protected synchronized final void incrementValue(double valueIncrement) {
      setValue(getValue() + valueIncrement);
   }
   
   public abstract double getFaceValue();
   
   public abstract void setFaceValue(double value);
   
   protected synchronized final void incrementFaceValue(double valueIncrement) {
      setFaceValue(getFaceValue() + valueIncrement);
   }
   
   /** Get the (simulation) time at which this contact was created. */
   public final double getCreationTime() {
      return contractCreationTime;
   }
   
   /** Get the (simulation) time at which this contract will expire. */
   public final double getExpirationTime() {
      return contractExpiryTime;
   }
   
   public abstract double getInterestRate();
   
   public abstract void setInterestRate(double interestRate);
   
   @Override
   public synchronized String toString() {
      return
         String.format(
            "Contract, value: %16.10g, face value: %16.10g, interest rate: %16.10g, " +
            "creation time: %16.10g, expiration time: %16.10g.",
            getValue(),
            getFaceValue(),
            getInterestRate(),
            getCreationTime(),
            getExpirationTime()
            );
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((contractUID == null) ? 0 : contractUID.hashCode());
      return result;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Contract other = (Contract) obj;
      if (contractUID == null) {
         if (other.contractUID != null)
            return false;
      } else if (!contractUID.equals(other.contractUID))
         return false;
      return true;
   }

   static public void resetInstanceCounterAndClearLogs() {
      contractsCreated = 0;
      allActiveContracts.clear();
   }
}