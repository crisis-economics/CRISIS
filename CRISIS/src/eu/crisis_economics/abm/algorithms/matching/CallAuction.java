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
package eu.crisis_economics.abm.algorithms.matching;

import java.util.Collection;
import java.util.Iterator;
import java.lang.IllegalArgumentException;
import java.lang.Math;
import java.security.InvalidAlgorithmParameterException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.matching.CallAuctionGroup;
import eu.crisis_economics.abm.algorithms.matching.Matching;
import eu.crisis_economics.abm.algorithms.matching.Matching.OneToOneMatch;

/**
  * Call Auction Bidder/Seller matching algorithm. The call auction looks at 
  * every price in the buy book and chooses a price maximising the traded volume. 
  * Some buyers, and sellers, are priced out of the market by the auction. For any 
  * given auction price, demand != supply in general: the RationingAlgorithm 
  * argument specifies the strategy used to ensure demand = supply before 
  * auction trades are committed.
  * 
  * @author phillips
  */
public final class CallAuction implements MatchingAlgorithm {
   
   private final RationingAlgorithm
      rationingStrategy;
   
   private double                                           // results, from the last use case
      auctionPriceResult,
      auctionTotalTradeResult;
   
   @Inject
   public CallAuction(
      // Immutable
      @Named("CALL_AUCTION_RATIONING_ALGORITHM")
         RationingAlgorithm rationingStrategy
      ) {
      this.auctionPriceResult = Double.NEGATIVE_INFINITY;
      this.auctionTotalTradeResult = Double.NEGATIVE_INFINITY;
      this.rationingStrategy = rationingStrategy;
   }
   
   @Override
   public Matching matchNodes(
      Collection<SimpleNode> left,
      Collection<SimpleNode> right
      ) throws InvalidAlgorithmParameterException {
      final CallAuctionGroup
         sellGroup, buyGroup;
      try {
         sellGroup = new CallAuctionGroup(left);
         buyGroup = new CallAuctionGroup(right);
      } catch (final IllegalArgumentException e) {
         Matching.Builder result = new Matching.Builder();
         return result.build();
      }
      
      CallAuctionGroup.Division
         bestSellResponse = null;
      double maxTradeVolume = -1.;
      
      for (Node node : buyGroup) {
         final double
            queryPrice = node.getPricePerUnit();
         CallAuctionGroup.Division sellDivision =
            sellGroup.splitByPrice(queryPrice),
            buyDivision = buyGroup.splitByPrice(queryPrice);
         final double sellVolume =
            sellDivision.lowerThanEqualTo(),
            buyVolume = buyDivision.greaterThanEqualTo(),
            tradeVolume = Math.min(sellVolume, buyVolume);
         if (maxTradeVolume < tradeVolume) {
            maxTradeVolume = tradeVolume;
            bestSellResponse = sellDivision;
            auctionPriceResult = queryPrice;
            auctionTotalTradeResult = tradeVolume;
         }
      }
      
      Matching.Builder
         builder = new Matching.Builder();
      final double
         auctionPrice = bestSellResponse.threshold;
      
      for (ComputeNode node : sellGroup)
         if (node.getPricePerUnit() > auctionPrice) {
            builder.addUnmatchedRightNode(node);
            node.setFullyUnusable();
         }
      for (ComputeNode node : buyGroup)
         if (node.getPricePerUnit() < auctionPrice) {
            builder.addUnmatchedLeftNode(node);
            node.setFullyUnusable();
         }
      
      // ration, if demand != supply
      rationingStrategy.rationNodes(sellGroup.nodes(), buyGroup.nodes());
      
      {                                                     // match, knowing demand == supply
         Iterator<ComputeNode>
            buyers = buyGroup.iterator(),
            sellers = sellGroup.iterator();
         ComputeNode
            buyer = buyers.next(),
            seller = sellers.next();
         while (true) {                                     // greedy algorithm
            final double
               demand = buyer.getUsable(),
               supply = seller.getUsable();
            if (demand <= 0) {
               if (!buyers.hasNext())
                  break;
               else
                  buyer = buyers.next();
               continue;
            }
            if (supply <= 0) {
               if (!sellers.hasNext())
                  break;
               seller = sellers.next();
               continue;
            }
            if (demand > supply) {
               buyer.incrementUnusable(supply);
               seller.setFullyUnusable();
               builder.addMatch(
                  new OneToOneMatch(seller, buyer, supply, auctionPrice));
            } else {
               buyer.incrementUnusable(demand);
               seller.incrementUnusable(demand);
               builder.addMatch(
                  new OneToOneMatch(seller, buyer, demand, auctionPrice));
            }
         }
      }
      
      return builder.build();
   }
   
   public double auctionPriceResult() {
      return auctionPriceResult;
   }
   
   public double auctionTotalTrade() {
      return auctionTotalTradeResult;
   }
}
