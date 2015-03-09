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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

/**
  * @author phillips
  */
public final class MixedClearingNetworkResult {
   private final Object
      demandSideObject, 
      supplySideObject;
   private final String
      demandSideID,
      supplySideID;
   
   private final double
      clearingRate,
      demandVolume,
      supplyVolume;
   
   MixedClearingNetworkResult( // Immutable
      final Object demandNodeObj,
      final String demandNodeID,
      final Object supplyNodeObj,
      final String supplyNodeID,
      final double clearingRate,
      final double demandVolume,
      final double supplyVolume
      ) {
      this.demandSideObject = demandNodeObj;
      this.supplySideObject = supplyNodeObj;
      this.demandSideID = demandNodeID;
      this.supplySideID = supplyNodeID;
      this.clearingRate = clearingRate;
      this.demandVolume = demandVolume;
      this.supplyVolume = -supplyVolume;
   }

   public Object getDemandSideObject() {
      return demandSideObject;
   }

   public Object getSupplySideObject() {
      return supplySideObject;
   }

   public String getDemandSideID() {
      return demandSideID;
   }

   public String getSupplySideID() {
      return supplySideID;
   }

   public double getClearingRate() {
      return clearingRate;
   }

   public double getDemandVolume() {
      return demandVolume;
   }

   public double getSupplyVolume() {
      return supplyVolume;
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(clearingRate);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result
            + ((demandSideID == null) ? 0 : demandSideID.hashCode());
      temp = Double.doubleToLongBits(demandVolume);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result
            + ((supplySideID == null) ? 0 : supplySideID.hashCode());
      temp = Double.doubleToLongBits(supplyVolume);
      result = prime * result + (int) (temp ^ (temp >>> 32));
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
      MixedClearingNetworkResult other = (MixedClearingNetworkResult) obj;
      if (Double.doubleToLongBits(clearingRate) != Double
            .doubleToLongBits(other.clearingRate))
         return false;
      if (demandSideID == null) {
         if (other.demandSideID != null)
            return false;
      } else if (!demandSideID.equals(other.demandSideID))
         return false;
      if (Double.doubleToLongBits(demandVolume) != Double
            .doubleToLongBits(other.demandVolume))
         return false;
      if (supplySideID == null) {
         if (other.supplySideID != null)
            return false;
      } else if (!supplySideID.equals(other.supplySideID))
         return false;
      if (Double.doubleToLongBits(supplyVolume) != Double
            .doubleToLongBits(other.supplyVolume))
         return false;
      return true;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "HeterogeneousClearingResult, demandSideID:" + demandSideID
            + ", supplySideID:" + supplySideID + ", clearingRate:"
            + clearingRate + ", demandVolume:" + demandVolume
            + ", supplyVolume=" + supplyVolume + ".";
   }
}
