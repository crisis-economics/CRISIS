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
package eu.crisis_economics.abm.model.configuration;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.inject.Singleton;

import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.model.Parameter;

/**
  * A model configuration component for the {@link SimpleLabourMarket} class.
  * 
  * @author phillips
  */
public final class GoodsMarketConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = 176258561160985581L;
   
   @Submodel
   @Parameter(
      ID = "SIMPLE_GOODS_MARKET_MATCHING_ALGORITHM"
      )
   private MarketMatchingAlgorithmConfiguration
      sellerBuyerMatchingAlgorithm = new ForagerMatchingAlgorithmConfiguration();
   
   public MarketMatchingAlgorithmConfiguration
      getSellerBuyerMatchingAlgorithm() {
      return sellerBuyerMatchingAlgorithm;
   }
   
   public void setSellerBuyerMatchingAlgorithm(
      final MarketMatchingAlgorithmConfiguration sellerBuyerMatchingAlgorithm) {
      this.sellerBuyerMatchingAlgorithm = sellerBuyerMatchingAlgorithm;
   }
   
   @Override
   protected void addBindings() {
      bind(SimpleGoodsMarket.class).in(Singleton.class);
   }
}
