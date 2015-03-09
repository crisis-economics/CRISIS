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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.UpfrontInjectionLoanDistributionAlgorithm;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author phillips
  */
public final class ClearingSimpleCommercialLoanMarket extends AbstractClearingMarket
   implements ClearingLoanMarket {
   
   private double
      lastTradeWeightedInterestRate,
      lastTotalLoansCreated;
   private final ClearingInstrument
      instrument;
   private final boolean
      useHeterogeneousLoans;
   
   /**
     * Create a {@link ClearingSimpleCommercialLoanMarket} market.<br><br>
     * 
     * {@link ClearingSimpleCommercialLoanMarket} is a simple implementation of the 
     * {@link ClearingMarket} interface. Markets of this type process exactly
     * one instrument of type {@code Commercial Loan}.
     * 
     * @param marketName
     *        The name of this {@link ClearingMarket}.
     * @param market
     *        The {@link ClearingHouse} to which this market belongs.
     * @param useHeterogeneousLoans
     *        Specify whether or not to treat all {@link Borrower}-{@link Lender}
     *        pairs heterogeneously. If disabled, all interest rates on all
     *        commercial loans formed by this instrument are identical. If enabled,
     *        interest rates on all loans depend on every unique {@link Borrower}-
     *        {@link Lender} pair. Heterogeneous market processing markedly
     *        increases the richness of the commercial loan market, at an increased
     *        computational cost.
     */
   @Inject
   public ClearingSimpleCommercialLoanMarket(
      @Assisted
      final String marketName,
      @Assisted
      final ClearingHouse market,
      @Named("CLEARING_SIMPLE_COMMERCIAL_LOAN_MARKET_USE_HETEROGENEOUS_LOANS")
      final boolean useHeterogeneousLoans
      ) {
      super(marketName, market);
      this.instrument = new ClearingInstrument(marketName, "Commercial Loan");
      this.useHeterogeneousLoans = useHeterogeneousLoans;
   }
   
   @Override
   public void process() {
      ResourceExchangeAggregator desiredTradesAggregator = 
         new SimpleResourceExchangeAggregator();
      CompleteNetworkMarket network =
         useHeterogeneousLoans ?
            new PureHeterogeneousNetworkMarket(
               instrument, desiredTradesAggregator) :
            new PureHomogeneousNetworkMarket(
               instrument, desiredTradesAggregator);
      final int
         numLenders = addLendersToNetwork(network),
         numBorrowers = addBorrowersToNetwork(network);
      lastTradeWeightedInterestRate = 0.;
      if(numLenders == 0 || numBorrowers == 0) {                            // No Participants
         System.out.printf(
            "---------------------------------------------\n" +
            "Loan Market Cleared [Time: %g]\n" +
            " * No Participants.\n" +
            "---------------------------------------------\n",
            Simulation.getSimState().schedule.getTime()
            );
         System.out.flush();
         return;
      }
      if(useHeterogeneousLoans)
         network.matchAllOrders(100, 15);
      else
         network.matchAllOrders(100, 1);
      updatePortfolios(desiredTradesAggregator);
      lastTradeWeightedInterestRate =
         desiredTradesAggregator.getDesiredTradeWeightedRate();
      
      { // Stdout
         double
            totalMarketSupply = desiredTradesAggregator.getTotalSupplierSupply(),
            totalMarketDemand = desiredTradesAggregator.getTotalConsumerDemand();
         System.out.printf(
            "---------------------------------------------\n" +
            "Loan Market Cleared [Time: %g]\n" +
            " Rate Selected:                      %8.3g *\n" +
            " Demand:                             %8.3g\n" +
            " Supply:                             %8.3g\n" +
            " Abs. Diff.:                         %8.3g\n" +
            "---------------------------------------------\n",
            Simulation.getSimState().schedule.getTime(),
            lastTradeWeightedInterestRate, 
            totalMarketDemand, 
            totalMarketSupply,
            Math.abs(totalMarketSupply - totalMarketDemand)
            );
         System.out.flush();
      }
   }
   
   private int addLendersToNetwork(final CompleteNetworkMarket network) {
      final List<ClearingInstrument> networkResources =
         new ArrayList<ClearingInstrument>();
      networkResources.add(instrument);
      int participants = 0;
      for(Entry<String, ClearingMarketParticipant> record : 
          getClearingHouse().getLenders().entrySet()) {
         MarketResponseFunction response =
            record.getValue().getMarketResponseFunction(
               getSummaryInformation());
         if(response == null) continue;
         network.addSellOrder(
            record.getValue(),
            record.getKey(),
            response
            );
         ++participants;
      }
      return participants;
   }
   
   private int addBorrowersToNetwork(final CompleteNetworkMarket network) {
      final List<ClearingInstrument> networkResources =
         new ArrayList<ClearingInstrument>();
      networkResources.add(instrument);
      int participants = 0;
      for(final Entry<String, ClearingMarketParticipant> record : 
          getClearingHouse().getBorrowers().entrySet()) {
         MarketResponseFunction response =
            record.getValue().getMarketResponseFunction(
               getSummaryInformation());
         if(response == null) continue;
         network.addBuyOrder(
            record.getValue(),
            record.getKey(),
            response
            );
         ++participants;
      }
      return participants;
   }
   
   @SuppressWarnings("unchecked")
   public void updatePortfolios(
      final ResourceExchangeAggregator desiredTradesAggregator) {
      ResourceDistributionAlgorithm<Borrower, Lender>
         assignmentAlgorithm = new UpfrontInjectionLoanDistributionAlgorithm();
      lastTotalLoansCreated = assignmentAlgorithm.distributeResources(
         (Map<String, Borrower>)(Object) // Contracted by ClearingHouse
            desiredTradesAggregator.getKeyedConsumers(),
         (Map<String, Lender>)(Object)   // Contracted by ClearingHouse
            desiredTradesAggregator.getKeyedSuppliers(),
         desiredTradesAggregator.getUnmodifiableKeyedResourceExchanges()
         ).getFirst();
   }
   
   @Override
   public ClearingMarketInformation getSummaryInformation() {
      final ClearingMarketInformation
         result = new ClearingMarketInformation(this);
      result.add(instrument, lastTradeWeightedInterestRate, lastTotalLoansCreated);
      return result;
   }
   
   @Override
   public Set<ClearingInstrument> getInstruments() {
      final Set<ClearingInstrument>
         result = new HashSet<ClearingInstrument>();
      result.add(instrument);
      return result;
   }
}
