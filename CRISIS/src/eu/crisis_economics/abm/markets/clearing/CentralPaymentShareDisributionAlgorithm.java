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
package eu.crisis_economics.abm.markets.clearing;

import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.events.ShareTransactionEvent;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ResourceDistributionAlgorithm;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
final public class CentralPaymentShareDisributionAlgorithm implements
   ResourceDistributionAlgorithm<StockHolder, StockReleaser> {
   
   public CentralPaymentShareDisributionAlgorithm() { } // Stateless
   
   /**
     * Enable {@code VERBOSE_MODE} for verbose console readoff. Enable
     * {@code PEDANTIC_MODE} for continuous assertions.
     */
   private static final boolean
      VERBOSE_MODE = true,
      PEDANTIC_MODE = false;
   
   /**
     * Share distribution algorithm with explicit cash payments. 
     * Shares are conserved during the redistribution process.
     * The distribution algorithm takes the following steps:
     *  1. Potential aggregate buy (B)/sell (S) share volumes are 
     *     both counted,
     *  2. B is reduced to min(B, S) by a fixed multipler per buyer,
     *  3. Buyers attempt to buy shares at the market price per share.
     *     The number of shares actually bought (B') is recorded.
     *  4. Each seller disowns S(seller)*B'/B shares, where S(seller)
     *     is the desired number of shares the seller wishes to 
     *     convert to cash.
     * 
     * @see ResourceDistributionAlgorithm.distributeResources
     */
   @Override
   public Pair<Double, Double> distributeResources(
      final Map<String, StockHolder> marketConsumers,
      final Map<String, StockReleaser> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      ) {
      if(marketSuppliers.size() > 1 || marketSuppliers.isEmpty())
         throw new IllegalArgumentException(
            "CentralPaymentShareDisributionAlgorithm.distributeResources: the caller "
            + "indicated that the stock does not have a unique releaser. This operating "
            + "mode is not supported."
            );
      
      final StockReleaser stockReleaser = marketSuppliers.values().iterator().next();
      
      if(VERBOSE_MODE)
         System.out.printf("Share disribution:\n");
      
      if(desiredResourceExchanges.isEmpty()) {
         if(VERBOSE_MODE)
            System.out.printf("No exchanged. Stock price: %16.10g\n", 
               UniqueStockExchange.Instance.getStockPrice(stockReleaser));
         return Pair.create(
            UniqueStockExchange.Instance.getStockPrice(stockReleaser)
              * UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser),
            UniqueStockExchange.Instance.getStockPrice(stockReleaser)
            );
      }
      
      final String
         stockUniqueName = stockReleaser.getUniqueName();
      final double
         pricePerShare =
            desiredResourceExchanges.entrySet().iterator().next().getValue().getSecond();
      double
         maximumNumberOfSharesToBuy = 0.,
         maximumNumberOfSharesToSell = 0.;
      
      final double
         sharesInExistenceAtOpen =
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser);
      
      if(VERBOSE_MODE) {
         System.out.printf(
            "Shares in existence: %16.10g\n" +
            "Price per share: %16.10g\n",
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser),
            UniqueStockExchange.Instance.getStockPrice(stockReleaser)
            );
      }
      
      // Count intended share transactions.
      for(final Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         final double
            changeInShares = record.getValue().getFirst() / pricePerShare;
         if(changeInShares > 0.)
            maximumNumberOfSharesToBuy += changeInShares;
         else
            maximumNumberOfSharesToSell -= changeInShares;
      }
      
      // Terminate if no shares are to available for trade.
      if(maximumNumberOfSharesToBuy == 0. || maximumNumberOfSharesToSell == 0.)
         return Pair.create(0., pricePerShare);
      
      final double buyerRationFactor = 
         (maximumNumberOfSharesToBuy > maximumNumberOfSharesToSell) ?
         maximumNumberOfSharesToSell/maximumNumberOfSharesToBuy : 1.;
      
      double
         numberOfSharesBought = 0.;
      
      // Attempt to credit stock holders who intend to buy shares.
      for(final Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         final StockHolder stockHolder = marketConsumers.get(record.getKey().getFirst());
         final String holderName = stockHolder.getUniqueName();
         final double
            additionalInvestment = buyerRationFactor * record.getValue().getFirst();
         if(additionalInvestment > 0.) {
            final double
               changeInShares = buyerRationFactor * additionalInvestment / pricePerShare;
            try {
               stockHolder.credit(additionalInvestment);
               @SuppressWarnings("unused")
               double
                  stockAccountValueChange = 0.;
               StockAccount account = stockHolder.getStockAccount(stockUniqueName);
               if(account == null)
                  account = StockAccount.create(stockHolder, stockReleaser, changeInShares);
               else {
                  stockAccountValueChange = -account.getValue();
                  account.setQuantity(account.getQuantity() + changeInShares);
               }
               stockAccountValueChange += account.getValue();
               numberOfSharesBought += changeInShares;
               if(PEDANTIC_MODE)
                  if(Math.abs(stockAccountValueChange / additionalInvestment - 1.) > 1.e-6
                     && Math.abs(changeInShares) > 1.e-8)
                     throw new IllegalStateException();
               if(VERBOSE_MODE)
                  System.out.printf("%s buys %g shares at cost %g. Value held: %g\n",
                     stockHolder.getUniqueName(),
                     changeInShares,
                     additionalInvestment,
                     account.getValue()
                     );
               
               Simulation.events().post(
                  new ShareTransactionEvent(
                     stockHolder, stockUniqueName, -additionalInvestment));
            }
            catch(final InsufficientFundsException buyerCannotPay) {
               final String warningMsg =
                  "CentralPaymentShareDistributionAlgorithm.distributeResources: stockholder " +
                  holderName + " cannot pay for " + changeInShares + " additional shares. The " + 
                  "position of this party in stocks of type " + stockUniqueName + " is unchanged.";
               System.err.println(warningMsg);
            }
         }
      }
      
      // Debit stock holders who intend to sell shares. 
      if(numberOfSharesBought > 0.) {
         // Ration according to the total number of shares already bought.
         final double
            stockSellerRationing = numberOfSharesBought / maximumNumberOfSharesToSell;
         
         for(final Entry<Pair<String, String>, Pair<Double, Double>> record : 
            desiredResourceExchanges.entrySet()) {
            final StockHolder
               stockHolder = marketConsumers.get(record.getKey().getFirst());
            final double
               additionalInvestment = stockSellerRationing * record.getValue().getFirst();
            if(additionalInvestment < 0.) {
               final double
                  changeInShares = additionalInvestment / pricePerShare;
               stockHolder.debit(-additionalInvestment);
               final StockAccount
                  account = stockHolder.getStockAccount(stockUniqueName);
               @SuppressWarnings("unused")
               double
                  changeInAccountValue = account.getValue();
               final double
                  newNumberOfSharesOwned = Math.max(account.getQuantity() + changeInShares, 0.);
               if(newNumberOfSharesOwned == 0.)
                  account.terminateAccountWithoutCompensatingStockHolder();
               else {
                  account.setQuantity(newNumberOfSharesOwned);
                  changeInAccountValue -= account.getValue();
               }
               numberOfSharesBought += changeInShares;
               if(PEDANTIC_MODE)
                  if(Math.abs(changeInAccountValue / -additionalInvestment - 1.) > 1.e-6
                     && Math.abs(changeInShares) > 1.e-8)
                     throw new IllegalStateException();
               if(VERBOSE_MODE)
                  System.out.printf("%s sells %g shares and receives %g. Value held: %g\n",
                     stockHolder.getUniqueName(),
                     changeInShares,
                     additionalInvestment,
                     account.getValue()
                     );
               
               Simulation.events().post(
                  new ShareTransactionEvent(
                     stockHolder, stockUniqueName, -additionalInvestment));
            }
         }
      }
      
      // Assert trading precision
      if(Math.abs(numberOfSharesBought) > 1.e-3)
         throw new IllegalStateException();
      
      if(VERBOSE_MODE)
         System.out.printf(
            "%g shares were bought. End of share distribution\n", numberOfSharesBought);
      
      // Flatten numerical residuals
      final double
         sharesInExistenceAtClose =
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser),
         globalMultiplier = sharesInExistenceAtOpen / sharesInExistenceAtClose;
      for(final StockHolder stockHolder :
          UniqueStockExchange.Instance.getOwnershipTracker(stockUniqueName)) {
         final StockAccount stockAccount = stockHolder.getStockAccount(stockUniqueName);
         stockAccount.setQuantity(stockAccount.getQuantity() * globalMultiplier);
      }
      
      return Pair.create(
         UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser) * pricePerShare,
         pricePerShare
         );
   }
}
