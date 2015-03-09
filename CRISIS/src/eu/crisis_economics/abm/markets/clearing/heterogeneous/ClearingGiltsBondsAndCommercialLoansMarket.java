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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.map.HashedMap;

import com.google.common.collect.TreeMultiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.PeerToPeerSaleAlgorithm;
import eu.crisis_economics.abm.markets.clearing.UpfrontInjectionLoanDistributionAlgorithm;
import eu.crisis_economics.abm.markets.clearing.PeerToPeerSaleAlgorithm.SaleExecutionDelegate;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/** A network {@link ClearingMarket} involving:<br><br>
  * 
  *   {@code (a)}
  *       pure heterogeneous bonds, with interest rate set according to
  *       each borrower/lender pair.<br>
  *   {@code (b)}
  *       multiple grades of homogeneous commercial loan resources, with
  *       interest rate set according to the risk assessment of the 
  *       supplier.<br><br>
  * 
  * In this implementation, commercial loan risk grades are consist of
  * three bins ("Low Risk", "Medium Risk" and "High Risk"). Commercial loan
  * consumers are binned into risk categories according to their equity as
  * compared to all other consumers. The lowest equity consumer is deemed
  * to have the highest commerical loan risk profile.
  * 
  * @author phillips
  */
public final class ClearingGiltsBondsAndCommercialLoansMarket
   extends AbstractClearingMarket implements ClearingLoanMarket {
   
   private final Map<String, Boolean>
      bankIsParticipating,
      firmIsParticipating,
      fundIsParticipating;
   
   private List<String>
      commercialLoanRiskGrades;
   
   private final Map<String, ClearingInstrument>
      instruments;
   private final Map<ClearingInstrument, Double>
      lastTradeVolumes;
   private final Map<ClearingInstrument, Double>
      lastTradeWeightedReturnRates;
   
   private final Map<String, List<ClearingMarketParticipant>>
      commericalLoanClientRiskBuckets;
   
   @Inject
   public ClearingGiltsBondsAndCommercialLoansMarket(
      @Assisted
      final String marketName,
      @Assisted
      final ClearingHouse clearingHouse
      ) {
      super(marketName, clearingHouse);
      this.bankIsParticipating = new HashMap<String, Boolean>();
      this.firmIsParticipating = new HashMap<String, Boolean>();
      this.fundIsParticipating = new HashMap<String, Boolean>();
      this.commercialLoanRiskGrades = Arrays.asList("Low Risk", "Medium Risk", "High Risk");
      this.instruments = new HashMap<String, ClearingInstrument>();
      this.lastTradeVolumes = new HashMap<ClearingInstrument, Double>();
      this.lastTradeWeightedReturnRates = new HashMap<ClearingInstrument, Double>();
      this.commericalLoanClientRiskBuckets =
         new HashedMap<String, List<ClearingMarketParticipant>>();
      
      for(final String riskGrade : commercialLoanRiskGrades)
         addInstrument(riskGrade + " Commercial Loan", instruments);
      addInstrument("Gilt", instruments);
      addInstrument("Bank Bond", instruments);
      for(final ClearingInstrument instrument : instruments.values()) {
         lastTradeVolumes.put(instrument, 0.);
         lastTradeWeightedReturnRates.put(instrument, 0.);
      }
   }
   
   /**
     * Implementation detail. Add a {@link ClearingInstrument} to this market.
     * 
     * @param instrumentName
     *        The name of the instrument to add.
     * @param store
     *        The {@link Map} in which to store the resulting {@link ClearingInstrument}.
     */
   private void addInstrument(
      final String instrumentName,
      final Map<String, ClearingInstrument> store
      ) {
      store.put(instrumentName, new ClearingInstrument(getMarketName(), instrumentName));
   }
   
   /**
     * Assign commerical loan consumers to risk grades according
     * to their equity. In this implementation, the number of 
     * consumer assigned to each bucket is approximately equal.
     */
   private void assignCommericalLoanClientsToRiskBuckets() {
      final int
         numRiskBuckets = commercialLoanRiskGrades.size();
      final List<ClearingMarketParticipant>
         firms = getAllPossibleCommericalLoanConsumers();
      
      class SortedEquityID implements Comparable<SortedEquityID> {
         private double equity;
         private ClearingMarketParticipant firm;
         SortedEquityID(
            final double equity,
            final ClearingMarketParticipant firm
            ) {
            this.equity = equity;
            this.firm = firm;
         }
         @Override
         public int compareTo(SortedEquityID other) {
            return Double.compare(this.equity, other.equity);
         }
      }
      
      final TreeMultiset<SortedEquityID>
         firmEquities = TreeMultiset.create();
      for(final ClearingMarketParticipant participant : firms) {
         final LoanStrategyFirm
            firm = (LoanStrategyFirm) participant;     // Contracted by ClearingHouse
         firmEquities.add(new SortedEquityID(firm.getEquity(), participant));
      }
      final int
         numFirms = firms.size(),
         firmsPerRiskBucket = (int) Math.floor(numFirms/(double) numRiskBuckets);
      int
         firmCounter = 0,
         riskGradeIndex = 0;
      String
         riskGrade = commercialLoanRiskGrades.get(0);
      for(final SortedEquityID record : firmEquities) {
         commericalLoanClientRiskBuckets.get(riskGrade).add(record.firm);
         ++firmCounter;
         if( firmCounter == firmsPerRiskBucket &&
           !(riskGradeIndex == numRiskBuckets - 1)) {
            firmCounter = 0;
            ++riskGradeIndex;
            riskGrade = commercialLoanRiskGrades.get(riskGradeIndex);
         }
      }
      return;
   }
   
   /**
     * Process the Commerical Loan/Bond market and commit to contracts.
     */
   @Override
   public void process() {
      final ResourceExchangeAggregator
         bondTradeAggregator = new SimpleResourceExchangeAggregator();
      final Map<String, ResourceExchangeAggregator>
         gradedCommercianLoanAggregators = new HashedMap<String, ResourceExchangeAggregator>();
      final ResourceExchangeAggregator
         giltTradeAggregator = new SimpleResourceExchangeAggregator();
      
      // Build network
      MixedClearingNetwork.Builder networkBuilder =
         new MixedClearingNetwork.Builder();
      
      // Add risk grades for commerical loan consumers.
      for(final String riskGrade : commercialLoanRiskGrades)
         commericalLoanClientRiskBuckets.put(
            riskGrade, new ArrayList<ClearingMarketParticipant>());
      
      for(final String riskGrade : commericalLoanClientRiskBuckets.keySet()) {
         gradedCommercianLoanAggregators.put(
            riskGrade, new SimpleResourceExchangeAggregator());
      }
      
      bankIsParticipating.clear();
      firmIsParticipating.clear();
      fundIsParticipating.clear();
      
      // Assign Firms to risk buckets
      assignCommericalLoanClientsToRiskBuckets();
      
      // Install Banks, Firms and Funds
      int
         numSuppliers = addBondSuppliersToNetwork(networkBuilder),
         numConsumers = addCommercialLoanConsumersToNetwork(networkBuilder),
         numIntermediaries = addLoanRelayersToNetwork(networkBuilder);
      
      if(numSuppliers == 0 || numIntermediaries == 0 || numConsumers == 0) {
         System.out.printf(
            "---------------------------------------------\n" +
            "Commercial Loan/Bond Network Cleared [Time: %g]\n" +
            " No trade pathways in this session.\n" +
            " Bond Suppliers: %d\n" +
            " Intermediaries: %d\n" +
            " Commercial Loan Consumers: %d\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            numSuppliers,
            numIntermediaries,
            numConsumers
            );
         return;
      }
      
      final boolean doIncludeGiltSubnetwork = tryAddGovernmentNodeToNetwork(networkBuilder);
      
      // Connect Bond trade routes for banks and funds.
      addFundBondEdgesToNetwork(networkBuilder, bondTradeAggregator);
      
      // Create Commercial loan trade routes for firm and banks.
      addCommercialLoanEdgesToNetwork(networkBuilder, gradedCommercianLoanAggregators);
      
      // If applicable, create gilt/government bond trade routes for government.
      if(doIncludeGiltSubnetwork)
         addGovernmentBondSubnetworkEdges(networkBuilder, giltTradeAggregator);
      
      // Build the network and apply a clearing algorithm.
      final MixedClearingNetwork network = networkBuilder.build();
      final MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AdaptiveMarchHeterogeneousClearingAlgorithm(
            60, 1.e-8, new OrderOrIterationsStoppingCondition(10, 30, network));
      
      network.applyClearingAlgorithm(clearingAlgorithm);
      
      network.createContracts();
      
      // Commit bond trades
      commitBankBondContracts(bondTradeAggregator);
      
      // Commit commercial loan trades
      for(final Entry<String, ResourceExchangeAggregator> record :
          gradedCommercianLoanAggregators.entrySet()) {
         commitCommercialLoanContracts(record.getValue(), record.getKey());
      }
      
      // If applicable, commit government gilt contracts
      if(doIncludeGiltSubnetwork)
         commitGovernmentBondContracts(giltTradeAggregator);
      
      commericalLoanClientRiskBuckets.clear();
   }
   
   private void commitCommercialLoanContracts(
      final ResourceExchangeAggregator aggregator,
      final String riskGrade
      ) {
      ResourceDistributionAlgorithm<Borrower, Lender>
         assignmentAlgorithm = new UpfrontInjectionLoanDistributionAlgorithm();
      @SuppressWarnings("unchecked")
      final Pair<Double, Double> summary = assignmentAlgorithm.distributeResources(
         (Map<String, Borrower>)(Object) // Contracted by ClearingHouse
            aggregator.getKeyedConsumers(),
         (Map<String, Lender>)(Object)   // Contracted by ClearingHouse
            aggregator.getKeyedSuppliers(),
         aggregator.getUnmodifiableKeyedResourceExchanges()
         );
      
      double
         lastTradeWeightedLoanReturnRate = 0,
         lastTotalDesiredLoanTrade = 0.;
      
      lastTradeWeightedLoanReturnRate += summary.getFirst() * summary.getSecond();
      lastTotalDesiredLoanTrade += summary.getFirst();
      lastTradeWeightedLoanReturnRate = 
         (lastTotalDesiredLoanTrade == 0. ? 0. :
            lastTradeWeightedLoanReturnRate/lastTotalDesiredLoanTrade);
      
      lastTradeVolumes.put(
         instruments.get(riskGrade + " Commercial Loan"), lastTotalDesiredLoanTrade);
      lastTradeWeightedReturnRates.put(
         instruments.get(riskGrade + " Commercial Loan"), lastTradeWeightedLoanReturnRate);
      
      { // Commercial Loan trade results
         double
            commericalLoanTotalDemand = aggregator.getTotalConsumerDemand(),
            commericalLoanTotalSupply = aggregator.getTotalSupplierSupply();
         System.out.printf(
            "---------------------------------------------\n" +
            "Commerical Loan Market Cleared [Time: %g]\n" +
            " Grade: %s\n" +
            " Demand:                             %8.3g\n" +
            " Supply:                             %8.3g\n" +
            " Abs. Diff.:                         %8.3g\n" +
            " Rate:                               %8.3g\n" +
            " Created:                            %8.3g\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            riskGrade,
            commericalLoanTotalDemand,
            commericalLoanTotalSupply,
            Math.abs(commericalLoanTotalDemand - commericalLoanTotalSupply),
            summary.getSecond(),
            summary.getFirst()
            );
         System.out.flush();
      }
   }
   
   private void commitBankBondContracts(final ResourceExchangeAggregator aggregator) {
      PeerToPeerSaleAlgorithm.SaleExecutionDelegate<Borrower, Lender>
         saleExecutationDelegate = new SaleExecutionDelegate<Borrower, Lender>() {
            @Override
            public double processSale(
               final Borrower consumer,
               final Lender supplier,
               final Pair<Double, Double> resourceExchange
               ) {
               try {
                  LoanFactory.createCouponBond(
                     consumer,
                     supplier,
                     resourceExchange.getFirst(),
                     resourceExchange.getSecond(), 
                     1,
                     NamedEventOrderings.BOND_PAYMENTS,
                     ScheduleIntervals.ONE_DAY
                     );
                  return resourceExchange.getFirst();
               } catch (final LenderInsufficientFundsException e) {
                  final String warningMsg = 
                     "Bond Clearing Market: bond supplier " + supplier.getUniqueName() + 
                     "has insufficient funds to transfer a bond principal of value " + 
                     resourceExchange.getFirst() + ".";
                  System.err.println(warningMsg);           // Supplier insufficient liquidity.
               }
               return 0.;
            }
         };
      PeerToPeerSaleAlgorithm<Borrower, Lender> bondExchangeAlgorithm = 
         new PeerToPeerSaleAlgorithm<Borrower, Lender>(saleExecutationDelegate);
      
      @SuppressWarnings("unchecked")
      final Pair<Double, Double> summary = bondExchangeAlgorithm.distributeResources(
         (Map<String, Borrower>)(Object) // Contracted by ClearingHouse
            aggregator.getKeyedConsumers(),
         (Map<String, Lender>)(Object)   // Contracted by ClearingHouse
            aggregator.getKeyedSuppliers(),
         aggregator.getUnmodifiableKeyedResourceExchanges()
         );
      
      double
         lastTradeWeightedBondReturnRate = 0,
         lastTotalDesiredBondTrade = 0.;
      
      lastTradeWeightedBondReturnRate += summary.getFirst() * summary.getSecond();
      lastTotalDesiredBondTrade += summary.getFirst();
      lastTradeWeightedBondReturnRate = 
         (lastTotalDesiredBondTrade == 0. ? 0. :
            lastTradeWeightedBondReturnRate/lastTotalDesiredBondTrade);
      
      lastTradeVolumes.put(
         instruments.get("Bank Bond"), lastTotalDesiredBondTrade);
      lastTradeWeightedReturnRates.put(
         instruments.get("Bank Bond"), lastTradeWeightedBondReturnRate);
      
      { // Bond Trade Results
         double
            bondTotalDemand = aggregator.getTotalConsumerDemand(),
            bondTotalSupply = aggregator.getTotalSupplierSupply();
         System.out.printf(
            "---------------------------------------------\n" +
            "Bond Market Cleared [Time: %g]\n" +
            " Demand:                             %8.3g\n" +
            " Supply:                             %8.3g\n" +
            " Abs. Diff.:                         %8.3g\n" +
            " Rate:                               %8.3g\n" +
            " Created:                            %8.3g\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            bondTotalDemand,
            bondTotalSupply,
            Math.abs(bondTotalDemand - bondTotalSupply),
            summary.getSecond(),
            summary.getFirst()
            );
         System.out.flush();
      }
   }
   
   /**
     * Create gilt/government bond contracts.
     */
   private void commitGovernmentBondContracts(final ResourceExchangeAggregator aggregator) {
      PeerToPeerSaleAlgorithm.SaleExecutionDelegate<Borrower, Lender>
         saleExecutationDelegate = new SaleExecutionDelegate<Borrower, Lender>() {
            @Override
            public double processSale(
               final Borrower consumer,
               final Lender supplier,
               final Pair<Double, Double> resourceExchange
               ) {
               try {
                  LoanFactory.createCouponBond(
                     consumer,
                     supplier,
                     resourceExchange.getFirst(),
                     resourceExchange.getSecond(), 
                     1,
                     NamedEventOrderings.GILT_PAYMENTS,
                     ScheduleIntervals.ONE_DAY
                     );
                  return resourceExchange.getFirst();
               } catch (final LenderInsufficientFundsException e) {
                  final String warningMsg = 
                     "Bond Clearing Market: bond supplier " + supplier.getUniqueName() + 
                     "has insufficient funds to transfer a bond principal of value " + 
                     resourceExchange.getFirst() + ".";
                  System.err.println(warningMsg);           // Supplier insufficient liquidity.
               }
               return 0.;
            }
         };
      PeerToPeerSaleAlgorithm<Borrower, Lender> bondExchangeAlgorithm = 
         new PeerToPeerSaleAlgorithm<Borrower, Lender>(saleExecutationDelegate);
      
      @SuppressWarnings("unchecked")
      final Pair<Double, Double> summary = bondExchangeAlgorithm.distributeResources(
         (Map<String, Borrower>)(Object) // Contracted by ClearingHouse
            aggregator.getKeyedConsumers(),
         (Map<String, Lender>)(Object)   // Contracted by ClearingHouse
            aggregator.getKeyedSuppliers(),
         aggregator.getUnmodifiableKeyedResourceExchanges()
         );
      
      double
         lastTradeWeightedGiltReturnRate = 0,
         lastTotalDesiredGiltTrade = 0.;
      
      lastTradeWeightedGiltReturnRate += summary.getFirst() * summary.getSecond();
      lastTotalDesiredGiltTrade += summary.getFirst();
      lastTradeWeightedGiltReturnRate = 
         (lastTotalDesiredGiltTrade == 0. ? 0. :
            lastTradeWeightedGiltReturnRate/lastTotalDesiredGiltTrade);
      
      lastTradeVolumes.put(
         instruments.get("Gilt"), lastTotalDesiredGiltTrade);
      lastTradeWeightedReturnRates.put(
         instruments.get("Gilt"), lastTradeWeightedGiltReturnRate);
      
      { // Bond Trade Results
         double
            bondTotalDemand = aggregator.getTotalConsumerDemand(),
            bondTotalSupply = aggregator.getTotalSupplierSupply();
         System.out.printf(
            "---------------------------------------------\n" +
            "Gilt Market Cleared [Time: %g]\n" +
            " Demand:                             %8.3g\n" +
            " Supply:                             %8.3g\n" +
            " Abs. Diff.:                         %8.3g\n" +
            " Rate:                               %8.3g\n" +
            " Created:                            %8.3g\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            bondTotalDemand,
            bondTotalSupply,
            Math.abs(bondTotalDemand - bondTotalSupply),
            summary.getSecond(),
            summary.getFirst()
            );
         System.out.flush();
      }
   }
   
   /**
     * Get an unmodifiable list of all possible commerical loan consumers.
     */
   private List<ClearingMarketParticipant> getAllPossibleCommericalLoanConsumers() {
      return Collections.unmodifiableList(
         new ArrayList<ClearingMarketParticipant>(super.getClearingHouse().getFirms().values()));
   }
   
   /**
     * Get an unmodifiable list of all possible commerical loan lenders.
     */
   private List<ClearingMarketParticipant> getAllPossibleCommercialLoanLenders() {
      return Collections.unmodifiableList(
         new ArrayList<ClearingMarketParticipant>(super.getClearingHouse().getBanks().values()));
   }
   
   /**
     * Generate a map of commerical loan instruments keyed by risk grade.
     */
   private Map<String, ClearingInstrument> getCommericalLoanInstruments() {
      final Map<String, ClearingInstrument> commericalLoanInstruments =
         new HashMap<String, ClearingInstrument>();
      for(final String riskGrade : commericalLoanClientRiskBuckets.keySet())
         commericalLoanInstruments.put(
            riskGrade, instruments.get(riskGrade + " Commercial Loan"));
      return commericalLoanInstruments;
   }
   
   /**
     * Generate a list of gilt resources.
     */
   private List<ClearingInstrument> getGiltResources() {
      List<ClearingInstrument> giltResources =
         new ArrayList<ClearingInstrument>();
      giltResources.add(new ClearingInstrument(getMarketName(), "Gilt"));
      return giltResources;
   }
   
   /**
     * Get an unmodifiable list of all possible bond lenders.
     */
   private List<ClearingMarketParticipant> getAllPossibleBondLenders() {
      return Collections.unmodifiableList(
         new ArrayList<ClearingMarketParticipant>(super.getClearingHouse().getFunds().values()));
   }
   
   /**
     * Generate a list of bond resources (typed by fund suppliers)
     */
   private List<ClearingInstrument> getBondResources() {
      List<ClearingInstrument> bondResources =
         new ArrayList<ClearingInstrument>();
      bondResources.add(new ClearingInstrument(getMarketName(), "Bank Bond"));
      return bondResources;
   }
   
   /**
     * Add commercial loan consumers to the clearing network. In this
     * implementation, commercial loan consumers are only allowed to
     * participate in one submarket corresponding to their risk grade.
     */
   private int addCommercialLoanConsumersToNetwork(
      final MixedClearingNetwork.Builder network) {
      final Map<String, ClearingInstrument> commericalLoanResources = 
         getCommericalLoanInstruments();
      int numberOfParticipants = 0;
      for(final String riskGrade : commericalLoanClientRiskBuckets.keySet()) {
         final ArrayList<ClearingInstrument> resources =
            new ArrayList<ClearingInstrument>();
         resources.add(commericalLoanResources.get(riskGrade));
         for(final ClearingMarketParticipant firm :
             commericalLoanClientRiskBuckets.get(riskGrade)) {
            final MarketResponseFunction responseFunction =
               firm.getMarketResponseFunction(getSummaryInformation());
            if(responseFunction == null) {
               firmIsParticipating.put(firm.getUniqueName(), false);
               continue;
            }
            firmIsParticipating.put(firm.getUniqueName(), true);
            network.addNetworkNode(firm, responseFunction, firm.getUniqueName());
            ++numberOfParticipants;
         }
      }
      return numberOfParticipants;
   }
   
   /**
     * Add intermediaries (bond liability/commercial loan asset holders)
     * to the clearing network.
     */
   private int addLoanRelayersToNetwork(
      final MixedClearingNetwork.Builder network) { 
      final List<ClearingMarketParticipant> knownBanks = getAllPossibleCommercialLoanLenders();
      final List<ClearingInstrument> probeResourceList =
         new ArrayList<ClearingInstrument>();
      probeResourceList.add(new ClearingInstrument(getMarketName(), "Bank Bond"));
      probeResourceList.add(new ClearingInstrument(getMarketName(), "Commercial Loan"));
      int numberOfParticipants = 0;
      for(int i = 0; i< knownBanks.size(); ++i) {
         final ClearingMarketParticipant bank = knownBanks.get(i);
         MarketResponseFunction responseFunction = 
            bank.getMarketResponseFunction(getSummaryInformation());
         if(responseFunction == null) {
            bankIsParticipating.put(bank.getUniqueName(), false);
            continue;
         }
         bankIsParticipating.put(bank.getUniqueName(), true);
         network.addNetworkNode(bank, responseFunction, bank.getUniqueName());
         ++numberOfParticipants;
      }
      return numberOfParticipants;
   }
   
   /**
     * Add bond suppliers to the network.
     */
   private int addBondSuppliersToNetwork(
      final MixedClearingNetwork.Builder network) {
      final List<ClearingMarketParticipant> knownFunds = getAllPossibleBondLenders();
      final ClearingInstrument probeResource = 
         new ClearingInstrument(getMarketName(), "Bank Bond");
      final List<ClearingInstrument>
         probeResourceList = new ArrayList<ClearingInstrument>();
      probeResourceList.add(probeResource);
      int numberOfParticipants = 0;
      for(int i = 0; i< knownFunds.size(); ++i) {
         final ClearingMarketParticipant fund = knownFunds.get(i);
         MarketResponseFunction responseFunction = 
            fund.getMarketResponseFunction(getSummaryInformation());
         if(responseFunction == null) {
            fundIsParticipating.put(fund.getUniqueName(), false);
            continue;
         }
         fundIsParticipating.put(fund.getUniqueName(), true);
         network.addNetworkNode(fund, responseFunction, fund.getUniqueName());
         ++numberOfParticipants;
      }
      return numberOfParticipants;
   }
   
   /**
     * Try to add a government node (trading in gilts) to the network.
     * @return
     *      true if and only if a government agent exists, and it
     *      intends to participate in a government bond/gilt submarket.
     */
   private boolean tryAddGovernmentNodeToNetwork(MixedClearingNetwork.Builder network) {
      if(!Simulation.getRunningModel().hasGovernment())
         return false;
      final Government government = Simulation.getRunningModel().getGovernment();
      final MarketResponseFunction marketResponseFunction =
         government.getMarketResponseFunction(
            getSummaryInformation());
      if(marketResponseFunction == null)
         return false;
      network.addNetworkNode(
         government,
         marketResponseFunction,
         government.getUniqueName()
         );
      return true;
   }
   
   /**
     * Add MutualFund Bond resources to the network.
     */
   private void addFundBondEdgesToNetwork(
      final MixedClearingNetwork.Builder network,
      final ResourceExchangeDelegate bondTradeDelegate
      ) {
      final List<ClearingInstrument>
         bondResources = getBondResources();
      final List<ClearingMarketParticipant>
         knownBanks = getAllPossibleCommercialLoanLenders(),
         knownFunds = getAllPossibleBondLenders();
      
      // Add bonds to the network.
      {
      network.addHyperEdge("Bank Bond");
      final ClearingInstrument resource = bondResources.get(0);
      for(int i = 0; i< knownBanks.size(); ++i) {
         final ClearingMarketParticipant bank = knownBanks.get(i);
         if(!bankIsParticipating.get(bank.getUniqueName()))
            continue;
         for(int j = 0; j< knownFunds.size(); ++j) {
            final ClearingMarketParticipant fund = knownFunds.get(j);
            if(!fundIsParticipating.get(fund.getUniqueName()))
               continue;
            network.addToHyperEdge(
               bank.getUniqueName(),
               fund.getUniqueName(),
               bondTradeDelegate,
               "Bank Bond",
               resource
               );
         }
      }
      }
      
   }
   
   /**
     * Add Commercial Loan resources to the network.
     */
   private void addCommercialLoanEdgesToNetwork(
      MixedClearingNetwork.Builder network,
      Map<String, ResourceExchangeAggregator> commericalLoanTradeDelegates
      ) {
      final Map<String, ClearingInstrument>
         commericalLoanResources = getCommericalLoanInstruments();
      final List<ClearingMarketParticipant>
         knownBanks = getAllPossibleCommercialLoanLenders();
      
      // Add commerical loans to the network.
      for(final String riskGrade : commericalLoanClientRiskBuckets.keySet()) {
         { // Are any firms in this risk bucket participating? If not, omit the bucket.
         boolean riskBucketHasFirmParticipant = false;
         for(final ClearingMarketParticipant firm :
             commericalLoanClientRiskBuckets.get(riskGrade)) {
            if(!firmIsParticipating.get(firm.getUniqueName()))
               continue;
            else {
               riskBucketHasFirmParticipant = true;
               break;
            }
         }
         if(!riskBucketHasFirmParticipant) continue;
         }
         network.addHyperEdge(riskGrade);
         final ResourceExchangeDelegate delegate =
             commericalLoanTradeDelegates.get(riskGrade);
         final ClearingInstrument resource = commericalLoanResources.get(riskGrade);
         for(int i = 0; i< knownBanks.size(); ++i) {
            final ClearingMarketParticipant bank = knownBanks.get(i);
            if(!bankIsParticipating.get(bank.getUniqueName()))
               continue;
            for(final ClearingMarketParticipant firm :
                commericalLoanClientRiskBuckets.get(riskGrade)) {
               if(!firmIsParticipating.get(firm.getUniqueName()))
                  continue;
            network.addToHyperEdge(
               firm.getUniqueName(),
               bank.getUniqueName(),
               delegate,
               riskGrade,
               resource
               );
            }
         }
      }
   }
   
   /**
     * Add gilts/government bond trade edges to the network.
     */
   private void addGovernmentBondSubnetworkEdges(
      MixedClearingNetwork.Builder network,
      ResourceExchangeDelegate giltTradeDelegate
      ) {
      final Government government = Simulation.getRunningModel().getGovernment();
      final List<ClearingMarketParticipant>
         knownBanks = getAllPossibleCommercialLoanLenders(),
         knownFunds = getAllPossibleBondLenders();
      final ClearingInstrument resource = getGiltResources().get(0);
      network.addHyperEdge("Gilt");
      
      // Add banks to the gilt subnetwork
      for(int i = 0; i< knownBanks.size(); ++i) {
         final ClearingMarketParticipant
            bank = knownBanks.get(i);
         if(!bankIsParticipating.get(bank.getUniqueName()))
            continue;
         network.addToHyperEdge(
            government.getUniqueName(),
            bank.getUniqueName(),
            giltTradeDelegate,
            "Gilt",
            resource
            );
      }
      
      // Add funds to the gilt subnetwork
      for(int i = 0; i< knownFunds.size(); ++i) {
         final ClearingMarketParticipant
            fund = knownFunds.get(i);
         if(!fundIsParticipating.get(fund.getUniqueName()))
            continue;
         network.addToHyperEdge(
            government.getUniqueName(),
            fund.getUniqueName(),
            giltTradeDelegate,
            "Gilt",
            resource
            );
      }
   }
   
   @Override
   public ClearingMarketInformation getSummaryInformation() {
      final ClearingMarketInformation
         result = new ClearingMarketInformation(this);
      for(final ClearingInstrument instrument : instruments.values())
         result.add(
            instrument,
            lastTradeWeightedReturnRates.get(instrument), 
            lastTradeVolumes.get(instrument)
            );
      return result;
   }
   
   @Override
   public Set<ClearingInstrument> getInstruments() {
      final Set<ClearingInstrument>
         result = new HashSet<ClearingInstrument>();
      result.addAll(instruments.values());
      return result;
   }
}
