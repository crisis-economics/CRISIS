/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.inventory.valuation;

import java.util.List;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.inventory.goods.GoodsInventory;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.inventory.valuation.
       MarketPriceInventoryValuation.MarketPricePerUnitDelegate;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * An implementation of the AbstractRepositoryValuation<Integer> skeletal
  * base class, with specialization to durable goods repositories.
  * 
  * This implementation computes the value of stored durable goods with 
  * reference to a market. The market specifies the price per unit (p) of 
  * goods contained in the repository. The total instantaneous value (V)
  * of the repository is taken to be V = \sum(all goods types) p * q, where
  * q is amount of the good contained in the repository.
  * @author phillips
  */
public final class MarketPriceGoodsRepositoryValuation
   extends AbstractRepositoryValuation<String, GoodsInventory> {
   
   private final SimpleGoodsMarket market;
   
   public MarketPriceGoodsRepositoryValuation(
      final DurableGoodsRepository repository,
      HasAssets holder,
      final SimpleGoodsMarket market
      ) {
      super(repository, holder);
      StateVerifier.checkNotNull(market);
      this.market = market;
   }
   
   @Override
   public void setValue(double newValue) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   protected InventoryValuation getInventoryValuation(final String key) {
      return
         new MarketPriceInventoryValuation(new MarketPricePerUnitDelegate() {
            @Override
            public double getPricePerUnit() {
               final List<Double>
                  lastTradingPrices = market.getInstrument(key).getTradingPrices();
               return lastTradingPrices.get(lastTradingPrices.size() - 1);
            }
         });
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Market Price Per Unit Inventory Valuation.";
   }
}
