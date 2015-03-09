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

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.Pair;

public final class GoodsForCashTransaction {
   
   private GoodsForCashTransaction() { }
   
   /**
     * A safe implementation of a "goods for cash" trade process.
     * The order of processing is as follows:
     *   (1) the cost of the desired trade is calculated, based on
     *       market prices;
     *   (2) the buyer disallocates cash for the trade. If the amount
     *       of cash disallocated is less than expected, the buyer
     *       demand for goods is trimmed;
     *   (3) the seller disallocates goods for the trade. If the
     *       quantity of goods disallocated is less than expected,
     *       the demand for goods is trimmed;
     *   (4) the buyer and seller process a cash settlement;
     *   (5) if (4) fails, goods remain with the seller.
     *   
     * Cash and goods disallocations are not reversed in the event of 
     * a failed transaction; it is assumed that these allocations were
     * made for this purpose.
     * 
     * @return one Pair<Double, Double> with the format:
     * Pair<Double: quantity of goods traded (number of units),
     *      Double: price per unit of the trade>.
     * 
     * @author phillips
     */
   static public Pair<Double, Double> process(
      final double unitPrice,
      final double quantity,
      final String goodsType,
      GoodsSeller seller,
      GoodsBuyer buyer
      ) {
      if(quantity == 0.)
         return Pair.create(0., unitPrice);
      double
         goodsWithdrawn = 0.;
      try {
         final double
            expectedTotalCost = unitPrice * quantity,
            buyerCash = buyer.disallocateCash(expectedTotalCost);           // Disallocate cash
         if(buyerCash == 0.)
            return Pair.create(0., unitPrice);                              // No trade
         final double
            affordableGoods =
               NumberUtil.decrementBySmallestAmount(
                  Math.min(buyerCash / expectedTotalCost, 1.0) * quantity),
            goodsAvailable = 
               -seller.getGoodsRepository().changeAllocatedQuantityBy(goodsType, -affordableGoods),
            goodsToTrade = Math.min(goodsAvailable, affordableGoods);
         goodsWithdrawn =                                                   // Withdraw goods
            seller.getGoodsRepository().pull(goodsType, goodsToTrade);
         final double
            costToBuyer = unitPrice * goodsWithdrawn;
         final Settlement settlement =
            SettlementFactory.createGoodsSettlement(seller, buyer);
         settlement.transfer(costToBuyer);                                  // Raises IFE
         buyer.getGoodsRepository().push(goodsType, goodsWithdrawn);        // Add goods to buyer
         buyer.registerIncomingTradeVolume(                                 // Notify buyer
            goodsType, goodsWithdrawn, unitPrice);
         seller.registerOutgoingTradeVolume(                                // Notify seller
            goodsType, goodsWithdrawn, unitPrice);
         return Pair.create(goodsWithdrawn, unitPrice);
      }
      catch (final InsufficientFundsException buyerCannotPay) {
         /*
          * Restore the Seller state. There is no need to re-allocate
          * goods and cash, as these were allocated for the above
          * (failed) transaction.
          */
         if(goodsWithdrawn > 0.)
            seller.getGoodsRepository().push(goodsType, goodsWithdrawn);
         return Pair.create(0., unitPrice);
      }
   }
}
