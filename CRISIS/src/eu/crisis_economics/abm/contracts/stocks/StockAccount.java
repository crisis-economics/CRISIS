/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.contracts.stocks;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.TwoPartyContract;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A contract that represents a stock account.
  * 
  * <p>
  * A stock account is a relation between a {@link StockReleaser} and a {@link StockHolder}.
  * The former pays dividends to the latter. The amount of dividend is determined by 
  * the <code>StockReleaser</code>.
  * </p>
  * 
  * @author rlegendi
  * @author phillips
  * @since 1.0
  */
public final class StockAccount extends TwoPartyContract {
    private double numberOfSharesOwned;
    
    public double allocatedShares = 0.; // TODO: Remove This ASAP
    
    /**
      * Creates and initializes a stock account. Stock accounts are assets 
      * belonging to the specified {@link StockHolder}, and the new stock
      * account is registered with (but is not a liability of) the specified
      * {@link StockReleaser}.
      * 
      * The arguments to this method cannot be null or negative, and the 
      * {@link StockHolder} may not have a preexisting stock account with
      * the {@link StockReleaser}. If the argument is negative or zero,
      * a preexiting stock account exists, or the argument is null, then no
      * action is taken and this method returns null.
      */
    public static StockAccount create(
       final StockHolder stockHolder,
       final StockReleaser stockReleaser,
       final double numberOfShares
       ) {
       if(stockHolder == null || stockReleaser == null)
          return null;
       if(numberOfShares <= 0.) {
          System.err.println(
             "StockAccount: cannot create an account with negative or zero shares. No action"
           + "was taken.");
          return null;
       }
       if(stockHolder.hasShareIn(stockReleaser.getUniqueName()))
          return null;
       final StockAccount result = new StockAccount(stockHolder, stockReleaser, numberOfShares);
       stockHolder.addAsset(result);
       stockReleaser.addStockAccount(result);
       return result;
    }
    
    /**
      * Implementation detail. Create a stock account contract and register 
      * this as a stockHolder asset. Inform the stockReleaser of the creation
      * of the account.
      */
    private StockAccount(
        final StockHolder stockHolder,
        final StockReleaser stockReleaser,
        final double numberOfSharesOwned
        ) {
        super(stockHolder, stockReleaser, Double.POSITIVE_INFINITY);
        StateVerifier.checkNotNull(stockHolder, stockReleaser);
        if(numberOfSharesOwned <= 0.)
           throw new IllegalArgumentException(
              "StockAccount: cannot create a stock account with a negative number of shares." + 
              "Stock holder: " + stockHolder.getUniqueName() +
              "Stock releaser/instrument name: " + stockReleaser.getUniqueName() + "."
              );
        this.numberOfSharesOwned = numberOfSharesOwned;
        UniqueStockExchange.Instance.getOwnershipTracker(
           stockReleaser.getUniqueName()).trackStockHolder(stockHolder);
    }
    
    /**
      * Get the stock releaser for this stock account.
      */
    public StockReleaser getReleaser() {
       return (StockReleaser)super.getSecondSettlementParty();
    }
    
    /**
      * Get the stock holder for this stock account.
      */
    public StockHolder getHolder() {
       return (StockHolder)super.getFirstSettlementParty();
    }
    
    /**
      * Get the name of the stock instrument trading shares of this type.
      */
    public String getInstrumentName() {
        return getReleaser().getUniqueName();
    }
    
    /**
      * Get the number of shares owned in the stock releaser.
      */
    public double getQuantity() {
        return numberOfSharesOwned;
    }
    
    /**
      * Set the number of shares owned.
      * @param numberOfSharesOwned
      *        The number of shares owned. If this value is negative, it
      *        is silently set to zero. If this argument is zero, the 
      *        stock account is terminated.
      */
    public void setQuantity(double numberOfShares) {
       numberOfShares = Math.max(numberOfShares, 0.);
       this.numberOfSharesOwned = numberOfShares;
       if(numberOfShares == 0) terminateAccountWithoutCompensatingStockHolder();
    }
    
    /**
      * Disown the number of shares indicated. This method returns the stated
      * number of shares to the {@link StockExchange}, so that the 
      * {@link StockExchange} may exercise a policy for redistributing the 
      * disowned shares.
      * 
      * @param numberOfSharesToDisown
      *        The number of shares to disown. This argument should be non-negative
      *        and less than {@link #getQuantity()}. If negative, no action is taken
      *        and this method does nothing. If greater than {@code N = }{@link #getQuantity()}, 
      *        the value of the argument is reduced to {@code N}.
      */
    public void disownShares(double numberOfSharesToDisown) {
       if(numberOfSharesToDisown <= 0.) return;
       numberOfSharesToDisown = Math.min(numberOfSharesToDisown, getQuantity());
       UniqueStockExchange.Instance.disownShares(
          getHolder(), getInstrumentName(), numberOfSharesToDisown);
    }
    
    /**
      * This method is equivalent to {@link StockAccount.#disownShares(double)}
      * with argument {@code getQuantity()}.
      */
    public void disownShares() {
       disownShares(getQuantity());
    }
    
    /**
      * This method is equivalent to setQuantity(getQuantity() + increment).
      */
    void incrementQuantity(final double increment) {
       setQuantity(getQuantity() + increment);
    }
    
    /**
      * Get the price per share of this stock.
      */
    public double getPricePerShare() {
        return UniqueStockExchange.Instance.getStockPrice(getInstrumentName());
    }
    
    /**
      * Terminate this stock account without compensating the stock holder.
      * This operation removes the stock account from the asset list of
      * the stock holder and modifies the equity of the holder to reflect
      * the deduction of the asset.
      */
   public void terminateAccountWithoutCompensatingStockHolder() {
      getReleaser().removeStockAccount(this);
      getHolder().removeAsset(this);
      UniqueStockExchange.Instance.getOwnershipTracker(
         getReleaser().getUniqueName()).discontinueStockHolderTracking(getHolder());
   }
   
   /**
     * Add shares to the stock holder at no cost. The argument to this method
     * must be positive; otherwise, no action is taken.
     */
   private void addSharesAtNoCost(final double numberOfShares) {
      if(numberOfShares <= 0.) return;
      this.numberOfSharesOwned += numberOfShares;
   }
   
   /**
    * This method is equivalent to 
    *   sellSharesTo(getQuantity(), newStockHolder).
    */
   public double sellSharesTo(final StockHolder newStockHolder)
      throws InsufficientFundsException {
      return sellSharesTo(getQuantity(), newStockHolder);
   }
   
   /**
     * This method is equivalent to 
     *   sellSharesTo(positiveNumberOfShares, newStockHolder, getPricePerShare()).
     */
   public double sellSharesTo(
      final double positiveNumberOfShares,
      final StockHolder newStockHolder
      ) throws InsufficientFundsException {
      return sellSharesTo(positiveNumberOfShares, newStockHolder, getPricePerShare());
   }
   
   /**
     * Sell shares to another stock holder. The number of shares to sell
     * is indicated by the first argument and must be positive, otherwise
     * no action is taken.
     * 
     * The number of shares to sell must be less than the number of shares
     * contained in the contract; otherwise the number of shares to sell
     * is silently trimmed to {@link StockAccount.getQuantity}.
     * 
     * If the stated pricePerShare is negative, its value is silently
     * set to zero.
     * 
     * If the receiving stockholder cannot affort the transaction at the
     * stated price, InsufficientFundsException is raised.
     * 
     * @param positiveNumberOfShares
     *        The desired number of shares to sell. If this quantity results
     *        in the original owner holding zero shares, then then this
     *        stock account is terminated.
     * @param newStockHolder
     *        The receiving stockholder.
     * @param pricePerShare
     *        The price per share at which to sell the shares.
     * @return
     *        The number of shares transferred to the receiving stockholder.
     * @throws InsufficientFundsException
     */
   public double sellSharesTo(
      double positiveNumberOfShares,
      final StockHolder newStockHolder,
      double pricePerShare
      ) throws InsufficientFundsException {
      StateVerifier.checkNotNull(newStockHolder);
      if(positiveNumberOfShares <= 0.) {
         System.err.println(
            "StockAccount.sellSharesTo: the transaction quantity is negative or zero (value: " 
          + positiveNumberOfShares + "or zero. No action was taken."
            );
         return 0.;
      }
      pricePerShare = Math.max(pricePerShare, 0.);                                  // Silent
      positiveNumberOfShares = Math.min(positiveNumberOfShares, getQuantity());     // Silent
      final double
         transactionValue = (pricePerShare * positiveNumberOfShares);
      if(pricePerShare > 0.) {
         final Settlement settlement =
            SettlementFactory.createDirectSettlement(newStockHolder, getHolder());
         settlement.transfer(transactionValue);
      }
      
      // Cash transaction has succeeded.
      numberOfSharesOwned -= positiveNumberOfShares;
      numberOfSharesOwned = Math.max(numberOfSharesOwned, 0.);
      if(!newStockHolder.hasShareIn(getInstrumentName())) {
         StockAccount.create(newStockHolder, getReleaser(), positiveNumberOfShares);
      } else {
         newStockHolder.getStockAccount(
            getInstrumentName()).addSharesAtNoCost(positiveNumberOfShares);
      }
      
      // Verify that shares are still owned by the original holder.
      if(getQuantity() == 0.)
         this.terminateAccountWithoutCompensatingStockHolder();
      return positiveNumberOfShares;
   }
   
   /**
     * This method is equivalent to
     *  transferSharesTo(getQuantity(), newStockHolder).
     */
   public double transferSharesTo(final StockHolder newStockHolder) {
      return transferSharesTo(getQuantity(), newStockHolder);
   }
   
   /**
     * Transfer shares to the stated stockholder at no cost to the
     * recipient.
     * 
     * @param positiveNumberOfShares
     *        The number of shares to transfer; this argument
     *        cannot exceed the number of shares owned. Otherwise,
     *        the value is silently trimmed to {@link
     *        StockAccount.getQuantity}.
     * @param newStockHolder
     *        The stockholder to which to transfer the shares. This
     *        argument should not be null.
     * @return
     *        The number of shares transferred.
     */
   public double transferSharesTo(
      double positiveNumberOfShares,
      StockHolder newStockHolder
      ) {
      StateVerifier.checkNotNull(newStockHolder);
      positiveNumberOfShares = Math.min(positiveNumberOfShares, getQuantity());     // Silent
      try {
         return this.sellSharesTo(positiveNumberOfShares, newStockHolder, 0.);      // No cost
      } catch (final InsufficientFundsException programmingError) {
         final String errorMessage = 
            "StockExchange.transferSharesTo: share transfer failed. This error is not expected " + 
            "and is indicative of a programming error. Details follow: receiving stock holder: " +
            newStockHolder.getUniqueName() + ", number of shares: " + positiveNumberOfShares + ".";
         System.err.println(errorMessage);
         throw new IllegalStateException(errorMessage);
      }
   }
   
   /**
     * Execute a dividend payment form the stock releaser to the stock
     * holder. The argument should be strictly positive; otherwise no
     * action is taken. Should the transaction fail, this method raises
     * InsufficientFundsException and the state of all parties remains
     * unchanged.
     */
   public void makeDividentPayment(double positiveValue)
      throws InsufficientFundsException {
      if(positiveValue <= 0.) {
         System.err.println(
            "StockAccount.makeDividendPayment: intended dividend payment (value " + positiveValue 
          + ") is negative or zero. No action was taken.");
         System.err.flush();
         return;
      }
      final Settlement settlement =
         SettlementFactory.createDividendSettlement(getReleaser(), getHolder());
      settlement.transfer(positiveValue);
   }
   
   @Override
   public double getValue() {
      return (numberOfSharesOwned * getPricePerShare());
   }
   
   @Override
   public double getFaceValue() {
      return getValue();
   }
   
   @Override
   public void setValue(double newValue) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void setFaceValue(double value) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public double getInterestRate() {
      return 0.;
   }
   
   @Override
   public void setInterestRate(double interestRate) {
      throw new UnsupportedOperationException();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "StockAccount. Number of Shares:" + numberOfSharesOwned
            + ", Stock Releaser: " + getReleaser() + ", Stock Holder: "
            + getHolder() + ", Price Per Share: " + getPricePerShare()
            + ", Value: " + getValue() + ".";
   }
}
