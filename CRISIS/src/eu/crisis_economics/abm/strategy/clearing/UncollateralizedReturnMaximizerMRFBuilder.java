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
package eu.crisis_economics.abm.strategy.clearing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.utilities.Pair;

/**
  * A helper class for the construction of valid UncollateralizedReturnMaximizerMRF
  * objects. This builder type accumulates information about the MRF to be built.
  * If the information collected is incompatible with a valid MRF, this object
  * will not yield an MRF to use.
  * @author phillips
  */
public final class UncollateralizedReturnMaximizerMRFBuilder {
   
   private double
      equity,
      lambda,
      existingCashToSpend;
   private Set<String>
      assets,
      liabilities;
   private Map<String, Pair<Double, Double>>
      assetRisks,
      liabilityRisks;
   private Map<String, Double>
      initialAssetHoldings,
      initialLiabilityHoldings;
   
   public UncollateralizedReturnMaximizerMRFBuilder() { // Builder
      this.equity = Double.NaN;
      this.lambda = Double.NaN;
      this.existingCashToSpend = Double.NaN;
      this.assets = new HashSet<String>();
      this.liabilities = new HashSet<String>();
      this.assetRisks = new HashMap<String, Pair<Double,Double>>();
      this.liabilityRisks = new HashMap<String, Pair<Double,Double>>();
      this.initialAssetHoldings = new HashMap<String, Double>();
      this.initialLiabilityHoldings = new HashMap<String, Double>();
   }
   
   /**
     * Specify the properties of an asset.
     * @param assetName
     *        The uniquely-identifying name of the asset
     * @param riskPremium
     *        The premium applied to return rates for this asset
     * @param marketImpactRate
     *        The market impact (derivative of expected return with
     *        respect to investment) for this asset.
     * @param currentHolding
     *        The value of existing assets of this type held by 
     *        the participant.
     */
   public void addAsset(
      final String assetName,
      final double riskPremium,
      final double marketImpactRate,
      final double currentHolding
      ) {
      assets.add(assetName);
      assetRisks.put(assetName, Pair.create(riskPremium, marketImpactRate));
      initialAssetHoldings.put(assetName, currentHolding);
   }
   
   /**
     * Specify the properties of a liability.
     * @param liabilityName
     *        The uniquely-identifying name of the liability
     * @param riskPremium
     *        The premium applied to return rates for this liability
     * @param marketImpactRate
     *        The market impact (derivative of expected return with
     *        respect to investment) for this liability.
     * @param currentHolding
     *        The value of existing liabilities of this type held by 
     *        the participant.
     */
   public void addLiability(
      final String liabilityName,
      final double riskPremium,
      final double marketImpactRate,
      final double currentHolding
      ) {
      liabilities.add(liabilityName);
      liabilityRisks.put(liabilityName, Pair.create(riskPremium, marketImpactRate));
      initialLiabilityHoldings.put(liabilityName, currentHolding);
   }
   
   /**
     * Specify the equity of the participant.
     */
   public void setParticipantEquity(final double equity) {
      this.equity = equity;
   }
   
   /**
     * Specify the target leverage of the participant.
     */
   public void setParticipantLeverage(final double leverage) {
      Preconditions.checkArgument(leverage >= 0.);
      this.lambda = leverage;
   }
   
   /**
     * Specify the target leverage of the participant.
     */
   public void setParticipantCashToSpend(final double existingCashToSpend) {
      Preconditions.checkArgument(existingCashToSpend >= 0.);
      this.existingCashToSpend = existingCashToSpend;
   }
   
   /**
     * Create a Market Response Function (MRF) from the state of this
     * builder. This method will raise an IllegalStateException if the
     * information known to the builder is insufficient (or inappropriate)
     * for the construction of a valid MRF.
     */
   public MarketResponseFunction build() throws IllegalStateException {
      if(Double.isNaN(equity) ||
         Double.isNaN(lambda) ||
         Double.isNaN(existingCashToSpend) ||
         assets.size() == 0 ||
         liabilities.size() == 0 ||
         assets.size() != assetRisks.size() ||
         liabilities.size() != liabilityRisks.size())
         throw new IllegalStateException();
      for(final String assetName : assets)
         if(!assetRisks.containsKey(assetName))
            throw new IllegalStateException();
      for(final String liabilityName : liabilities)
         if(!liabilityRisks.containsKey(liabilityName))
            throw new IllegalStateException();
      return UncollateralizedReturnMaximizerMRFAdapater.create(
         equity, lambda, existingCashToSpend,
         assets, liabilities, assetRisks,
         liabilityRisks, initialAssetHoldings, initialLiabilityHoldings
         );
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "UncollateralizedReturnMaximizerMRFBuilder, known equity: " + equity
            + ", known leverage: " + lambda + ", asset names: " + assets
            + ", liability names: " + liabilities + ", asset treatments:" + assetRisks
            + ", liability treatments: " + liabilityRisks + ", initial asset holdings: "
            + initialAssetHoldings + ", initial liability holdings: " + initialLiabilityHoldings
            + ".";
   }
}
