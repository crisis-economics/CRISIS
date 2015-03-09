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
package eu.crisis_economics.abm.inventory.goods;

import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.inventory.AbstractSetRepository;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

/** 
  * A repository for multiple differentiated types of goods.
  * @author phillips
  */
public final class DurableGoodsRepository extends AbstractSetRepository<GoodsInventory>
   implements GoodsRepository {
   
   private GoodsClassifier goodsClassifier;
   
   public DurableGoodsRepository(final GoodsClassifier goodsClassifier) { 
      StateVerifier.checkNotNull(goodsClassifier);
      this.goodsClassifier = goodsClassifier;
   }
   
   public interface GoodsClassifier {
      public Pair<Double, Double> getCharacteristics(String goodsType);
   }
   
   /**
     * A common specifications for goods types. This object should be shared
     * between multiple goods repositories in order to ensure that the
     * correct inventory type is created whenever a new storage space for
     * goods is requested.
     * @author phillips
     */
   public final static class ManualGoodsClassifier implements GoodsClassifier {
      private Map<String, Pair<Double, Double>> charactersistics;
      
      /**
        * @param charactersistic
        *    The characteristics of each goods type.
        *    The format of the argument is as follows:
        *    Map<
        *       String: unique ID (String label) of the good type,
        *       Pair<
        *         Double: decay (per unit time) of the good,
        *         Double: consumption depletion (per use) of the good
        *         >
        */
      public ManualGoodsClassifier(
         final Map<String, Pair<Double, Double>> charactersistics) {
         StateVerifier.checkNotNull(charactersistics);
         this.charactersistics = charactersistics;
      }
      
      /**
        * Return a copy of the classifier for the specified good type.
        * It is safe to modify the returned object.
        */
      public Pair<Double, Double> getCharacteristics(String goodsType) {
         return Pair.create(charactersistics.get(goodsType));
      }
   }
   
   /**
     * A trivial goods classifier that returns non-durable, single-use goods
     * types only.
     * @author phillips
     */
   public final static class SimpleGoodsClassifier implements GoodsClassifier {
      public Pair<Double, Double> getCharacteristics(String goodsType) {
         return Pair.create(1., 1.);                                // Non Durable, Single Use
      }
   }
   
   @Override
   protected GoodsInventory createEmptyInventory(final String inventoryType) {
      final Pair<Double, Double> properties =
         goodsClassifier.getCharacteristics(inventoryType);
      return GoodsInventoryFactory.createInventory(
         0., properties.getFirst(), properties.getSecond());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public final Set<Entry<String, GoodsInventory>> entrySet() {
      return (Set<Entry<String, GoodsInventory>>) (Object) super.entrySet();
   }
}
