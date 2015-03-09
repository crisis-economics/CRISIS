/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Victor Spirin
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
package eu.crisis_economics.abm.markets.nonclearing;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

/**
 * The market for inter-bank loans, that is loans provided to banks.
 * 
 * @author Tamás Máhr, Victor Spirin
 */
public class InterbankLoanMarket extends LoanMarket {
   
   private InterbankNetwork
      bankNetwork;
   
   @Inject
   public InterbankLoanMarket(
      InterbankNetwork interbankNetwork
      ) {
      super(NamedEventOrderings.INTERBANK_MARKET_MATCHING);
      Preconditions.checkNotNull(interbankNetwork);
      this.bankNetwork = interbankNetwork;
   }
   
   @Override
   public InterbankLoanOrder addOrder(
      final Party party,
      final int maturity,
      final double size,
      final double price
      ) throws OrderException, AllocationException {
      return addOrder(party, maturity, size, price, DefaultFilters.any());
   }
   
   @Override
   public InterbankLoanOrder addOrder(
      final Party party,
      final int maturity,
      final double size,
      final double price,
      final Filter filter
      ) throws OrderException, AllocationException {
      if (null == party)
         throw new IllegalArgumentException("party == null");
      if (Double.compare(size, 0) == 0)
         throw new IllegalArgumentException("size == 0");
      if (size > 0) // sell order: allocate cash for principal
         ((Buyer) party).allocateCash(size);
      if (price < 0)
         throw new IllegalArgumentException(price + " == price < 0");
      InterbankLoanInstrument instrument = getInstrument(maturity);
      if (instrument == null) {
         addInstrument(maturity);
         instrument = getInstrument(maturity);
      }
      return new InterbankLoanOrder(party, instrument, size, price, filter);
   }
   
   /**
     * Add an {@link InterbankLoanInstrument} of given maturity.
     */
   @Override
   public void addInstrument(final int maturity) {
      // super.addInstrument(LoanInstrument.generateTicker(maturity));
      if (getInstrument(maturity) == null) {
         if (instrumentMatchingMode == InstrumentMatchingMode.DEFAULT) {
            instruments.put(InterbankLoanInstrument.generateTicker(maturity),
                  new InterbankLoanInstrument(maturity, updatedOrders));
         } else if (instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS) {
            instruments.put(InterbankLoanInstrument.generateTicker(maturity),
                  new InterbankLoanInstrument(maturity, updatedOrders,
                        Instrument.MatchingMode.ASYNCHRONOUS, listeners));
         } else if (instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS) {
            instruments.put(InterbankLoanInstrument.generateTicker(maturity),
                  new InterbankLoanInstrument(maturity, updatedOrders,
                        Instrument.MatchingMode.SYNCHRONOUS, listeners));
         }
      }
   }
   
   /**
     * Retrieve an {@link InterbankLoanInstrument} of given maturity.
     */
   @Override
   public InterbankLoanInstrument getInstrument(final int maturity) {
      return (InterbankLoanInstrument) getInstrument(InterbankLoanInstrument
            .generateTicker(maturity));
   }
   
   @Override
   public InterbankLoanOrder addBuyOrder(
      final Party party,
      final int maturity,
      final double size,
      final double price,
      final Filter filter
      ) throws OrderException, AllocationException {
      if (size < 0)
         throw new IllegalArgumentException("size should be positive (" + size + ")");
      return addOrder(party, maturity, -size, price, filter);
   }
   
   @Override
   public InterbankLoanOrder addSellOrder(
      final Party party,
      final int maturity,
      final double size,
      final double price,
      final Filter filter
      ) throws OrderException, AllocationException {
      if (size < 0)
         throw new IllegalArgumentException("size should be positive (" + size + ")");
      return addOrder(party, maturity, size, price, filter);
   }
   
   // public interface
   
   /**
    * Add a borrow order onto the interbank market. Price is the nominal
    * interest at which Party is willing to borrow.
    */
   @Override
   public InterbankLoanOrder addBuyOrder(final Party party, final int maturity,
         final double size, final double price) throws OrderException,
         AllocationException {
      Filter orderFilter = DefaultFilters.any();
      
      if (bankNetwork != null)
         orderFilter = bankNetwork.generateFilter(party);
      return addBuyOrder(party, maturity, size, price, orderFilter);
   }
   
   /**
    * Add a lend order onto the interbank market. Price is the (risk-adjusted)
    * interest at which Party is willing to lend.
    */
   @Override
   public InterbankLoanOrder addSellOrder(final Party party,
         final int maturity, final double size, final double price)
         throws OrderException, AllocationException {
      Filter orderFilter = DefaultFilters.any();
      
      if (bankNetwork != null)
         orderFilter = bankNetwork.generateFilter(party);
      
      return addSellOrder(party, maturity, size, price, orderFilter);
   }
   
   /**
    * Set an interbank network to be used by the market
    */
   public void setNetwork(InterbankNetwork network) {
      this.bankNetwork = network;
   }
   
   /**
    * Get the interbank network used by the market (if it hasn't been set, will
    * return null). The network can then be modified (add or remove bank
    * relationships). This method returns a reference to the network, and not a
    * copy. Any changes to the network returned will be applied to the network
    * used by the market.
    */
   public InterbankNetwork getNetwork(InterbankNetwork network) {
      return bankNetwork;
   }
   
}
