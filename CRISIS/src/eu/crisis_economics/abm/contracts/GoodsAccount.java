/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Alessandro Gobbi
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

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;

public class GoodsAccount extends FixedValueContract {


	
	/** The constant DEFAULT_EXPITATION_TIME that represents a never expiring contract. */
	private static final double DEFAULT_EXPIRATION_TIME = Double.POSITIVE_INFINITY;
	
	/** The constant DEFAULT_INTEREST_RATE that is 0 (interest rate is not defined on StockAccounts). */
	private static final int DEFAULT_INTEREST_RATE = 0;
	
	/** The instrument name (usually the stock releaser name). */
	//private final String goodsName;
	
	/** The number of shares owned in the associated stock releaser. */
	private double quantity;
	
	/** The price per share (stock) the contract was created, for details, refer to {@link #getPricePerShare()}. */
	
	private double price;  //last price when buying
	
//	/** The holder of the stock account. */
//	private final StockHolder holder;
	
//	/** The releaser of the stock account. */
//	private final StockReleaser releaser;
	
	private double allocatedQuantity;
	

    public double getAllocatedQuantity() {
		return allocatedQuantity;
	}

	public void setAllocatedQuantity(double allocatedQuantity) {
		this.allocatedQuantity = allocatedQuantity;
	}

	private GoodHolder holder; // this is the guy that buy goods. We use his name when we create 
    

	private double bookPrice; //average buying price

	private String goodsType;
	
	 public String getGoodsType() {
		return goodsType;
	}

	/**
	 * Returns the face value of this stock account which equals to the price by default.
	 * 
	 * @param value the initial value of the stock account
	 * @return the face value which equals to <code>value</code> by default
	 */
	private static double faceValueFor(final double value) {
		return value;
	}
	
	
	/**
	 * Creates and initializes a stock account: it is set as an asset of the specified {@link StockHolder}, the new stock
	 * account is registered for the {@link StockReleaser}, and it is scheduled in the scheduler with the default step
	 * interval.
	 * 
	 * @param instrumentName the instrument name; cannot be <code>null</code>
	 * @param holder the stock holder; cannot be <code>null</code>
	 * @param releaser the stock releaser; cannot be <code>null</code>
	 * @param quantity the number of stocks; cannot be negative
	 * @param pricePerShare the price per share for which the shares were bought; cannot be negative
	 * @return the properly initialized stock account
	 */
	public static GoodsAccount create(final String goodType, final GoodHolder holder,
										final double quantity, final double price) {
		
		final GoodsAccount account = new GoodsAccount(goodType, holder, quantity, price);
		
		holder.addAsset(account);
				
		return account;
	}
	
   private GoodsAccount(
      final String goodsType,
      final GoodHolder holder, 
      final double quantity,
      final double price
      ) {
      super(
         DEFAULT_EXPIRATION_TIME,
         DEFAULT_INTEREST_RATE,
         quantity * price,
         faceValueFor(quantity * price)
         );
      Preconditions.checkNotNull(goodsType);
      if (null == holder)
			throw new IllegalArgumentException( "holder == null" );
      if (quantity < 0)
			throw new IllegalArgumentException( quantity + " == quantity < 0" );
      if (price < 0)
			throw new IllegalArgumentException( price + " == price < 0" );
		
		this.goodsType = goodsType;
		this.quantity = quantity;
		this.price = price;
		this.holder = holder;
      this.bookPrice = price;
		}
	
	/**
	 * Acquire new shares in the associated stock releaser.
	 * 
	 * @param amount the amount of shares that was acquired; cannot be negative
	 * @param pricePerShare the price per share for which the shares were bought; cannot be negative
	 */
	public void acquire(final double amount, final double price) {
		if ( amount < 0 ) {
			throw new IllegalArgumentException( amount + " == amount < 0" );
		}
		
		if ( price < 0 ) {
			throw new IllegalArgumentException( price + " == price < 0" );
		}
		
		quantity += amount;
		this.price = price;
        resetBookPrice(amount, price);
        resetBookValue();
		resetValue();
	}
	
	
	public void useGood(final double amount){
		if ( amount < 0 ) {
			throw new IllegalArgumentException( amount + " == amount < 0" );
		}
		
		if ( amount > getUnallacotedQuantity() ) {
			throw new IllegalArgumentException( amount + " == amount > available quantity " + getUnallacotedQuantity() );
		}
		
		this.quantity = this.quantity - amount;
		resetValue();
		
		if (this.quantity < 0 ) {
			throw new IllegalArgumentException( quantity + " == quantity < 0!" );
		}
		
		
	}
	
	public void eraseAllocatedQuantity(final double amount){
		if ( amount < 0 ) {
			throw new IllegalArgumentException( amount + " == amount < 0" );
		}
		
		if ( amount > getAllocatedQuantity() ) {
			throw new IllegalArgumentException( amount + " == amount > quantity to be erased " + getUnallacotedQuantity() );
		}
		this.quantity = this.quantity - amount;
		resetValue();
		
	}
	
	/*
	 * the first function returns a sort of market value (the whole quantity * last price)
	 * the second function returns the book value by computing the average price. 
	 * 
	 */
	private void resetValue() {
		super.setValue(quantity * price);
		super.setFaceValue(super.getValue());
	}
	
	
	private void resetBookPrice(double amount, double price) {
		this.bookPrice = ((quantity - amount) * this.bookPrice + amount * price)/quantity;
		
		
	}
	
	private void resetBookValue(){
	}

	/**
	 * Sell some (or all) owned shares of the owned stocks.
	 * 
	 * <p>
	 * If all the stocks associated with this stock account were sold, the stock account is automatically removed from the
	 * asset list of the holder and also removed from the stock account owner list from the stock releaser.
	 * </p>
	 * 
	 * @param amount the amount of shares that was sold; cannot be negative and cannot be more than owned quantity
	 */
	
	
	
	public void sell(final double amount) {
		if ( amount < 0 ) {
			throw new IllegalArgumentException( amount + " == amount < 0" );
		}
		
		if ( quantity < amount ) {
			throw new IllegalArgumentException(
			    String.format( "quantity == %g < amount == %g", quantity, amount ) );
		}
		
		quantity -= amount;
		resetValue();
		resetBookValue();
		
		if ( quantity == 0 ) {
			holder.removeAsset( this );
		}
		
	}

	public double getQuantity() {
		return this.quantity;
	}
	
	public double getUnallacotedQuantity(){
		
		return this.quantity - allocatedQuantity;
	}
	
	public double getBookPrice(){
		return bookPrice;
	}
	//this is used in production, where wuantity is added without trade
	public void addQuantity(double quantity) {
		this.quantity += quantity;
	}

	public double getPrice(){
		return price;
	}
	
	
	
}
