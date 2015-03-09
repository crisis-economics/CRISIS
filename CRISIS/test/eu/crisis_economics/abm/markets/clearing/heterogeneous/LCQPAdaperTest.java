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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction.TradeOpportunity;
import eu.crisis_economics.abm.strategy.clearing.LCQPUncollateralizedReturnMaximizer;
import eu.crisis_economics.abm.strategy.clearing.UncollateralizedReturnMaximizerMRFBuilder;
import eu.crisis_economics.utilities.Pair;

public class LCQPAdaperTest {
   
   private final static double
      LOW_RISK_COMMERCIAL_LOAN_PREMIUM = 3.0,
      MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM = 3.1,
      HIGH_RISK_COMMERCIAL_LOAN_PREMIUM = 3.2,
      BOND_PREMIUM = 1.2,
      LOW_RISK_COMMERCIAL_LOAN_MARKET_IMPACT = -1.e-2,
      MEDIUM_RISK_COMMERCIAL_LOAN_MARKET_IMPACT = -2. * 1.e-2,
      HIGH_RISK_COMMERCIAL_LOAN_MARKET_IMPACT = -4. * 1.e-2,
      BOND_MARKET_IMPACT = 0.0;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing LCQPAdapter..");
   }
   
   /**
     * Abstract base class for a {@link ClearingMarketParticipant}.
     * @author phillips
     */
   static abstract class AbstractClearingMarketParticipant
      implements ClearingMarketParticipant {
      private String name;
      
      AbstractClearingMarketParticipant(final String name) {
         this.name = name;
      }
      
      @Override
      public String getUniqueName() {
         return name;
      }
   }
   
   /**
     * Mock implementation of a {@link ClearingMarketParticipant}
     * bond supplier.
     * @author phillips
     */
   static class BondSupplier extends AbstractClearingMarketParticipant {
      private double
         bondSupply;
      
      BondSupplier(
         final String name,
         final double bondSupply
         ) {
         super(name);
         this.bondSupply = bondSupply;
      }
      
      @Override
      public MarketResponseFunction getMarketResponseFunction(
         final ClearingMarketInformation marketInformation) {
         return new PartitionedResponseFunction(
            new ExpIOCPartitionFunction(),
            new PolynomialSupplyInnerResponse(0.2, 1.0, bondSupply)
            );
      }
   }
   
   /**
     * Mock implementation of a {@link ClearingMarketParticipant}
     * commercial loan consumer.
     * @author phillips
     */
   static class CommercialLoanConsumer extends AbstractClearingMarketParticipant {
      private double
         maximumLoanDemand;
      
      CommercialLoanConsumer(
         final String name,
         final double maximumLoanDemand
         ) {
         super(name);
         this.maximumLoanDemand = maximumLoanDemand;
      }
      
      @Override
      public MarketResponseFunction getMarketResponseFunction(
         final ClearingMarketInformation marketInformation) {
         return new PartitionedResponseFunction(
            new InverseExpIOCPartitionFunction(),
            new PolynomialDemandInnerResponse(1.0, 1.0, maximumLoanDemand)
            );
      }
   }
   
   static class TrivialResourceExchangeDelegate implements ResourceExchangeDelegate {
      @Override
      public void commit(final MixedClearingNetworkResult result) {
         // No action
      }
   }
   
   /**
     * Mock implementation of a {@link ClearingMarketParticipant}
     * portfolio optimizing Bank.
     * @author phillips
     */
   static class Bank extends AbstractClearingMarketParticipant {
      private double
         equity,
         leverageTarget,
         cashReserveSize;
      
      Bank(
         final String name,
         final double equity,
         final double leverageTarget,
         final double cashReserveSize
         ) {
         super(name);
         this.equity = equity;
         this.leverageTarget = leverageTarget;
         this.cashReserveSize = cashReserveSize;
      }
      
      @Override
      public MarketResponseFunction getMarketResponseFunction(
         final ClearingMarketInformation marketInformation) {
         final UncollateralizedReturnMaximizerMRFBuilder builder =
            new UncollateralizedReturnMaximizerMRFBuilder();
         builder.addAsset("Low Risk Commercial Loan",
            LOW_RISK_COMMERCIAL_LOAN_PREMIUM, LOW_RISK_COMMERCIAL_LOAN_MARKET_IMPACT, 0.);
         builder.addAsset("Medium Risk Commercial Loan",
            MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM, MEDIUM_RISK_COMMERCIAL_LOAN_MARKET_IMPACT, 0.);
         builder.addAsset("High Risk Commercial Loan",
            HIGH_RISK_COMMERCIAL_LOAN_PREMIUM, HIGH_RISK_COMMERCIAL_LOAN_MARKET_IMPACT, 0.);
         builder.addLiability("Bond",
            BOND_PREMIUM, BOND_MARKET_IMPACT, 0.);
         builder.setParticipantCashToSpend(cashReserveSize);
         builder.setParticipantEquity(equity);
         builder.setParticipantLeverage(leverageTarget);
         return builder.build();
      }
   }
   
   @Test
   public void testLCQPAdapter() {
      /*
       * Test parameters
       */
      final int
         numBondSuppliers = 1,
         numLowRiskBorrowers = 1,
         numMediumRiskBorrowers = 1,
         numHighRiskBorrowers = 1,
         numBorrowers = numLowRiskBorrowers + numMediumRiskBorrowers + numHighRiskBorrowers,
         numBanks = 1;
      
      List<ClearingMarketParticipant>
         bondSuppliers = new ArrayList<ClearingMarketParticipant>(),
         borrowersLowRisk = new ArrayList<ClearingMarketParticipant>(),
         borrowersMediumRisk = new ArrayList<ClearingMarketParticipant>(),
         borrowersHighRisk = new ArrayList<ClearingMarketParticipant>(),
         banks = new ArrayList<ClearingMarketParticipant>();
      
      for(int i = 0; i< numBondSuppliers; ++i)
         bondSuppliers.add(new BondSupplier("Bond Supplier " + i, 100.0));
      for(int i = 0; i< numLowRiskBorrowers; ++i)
         borrowersLowRisk.add(
            new CommercialLoanConsumer("Low Risk Loan Consumer " + i, 100./3.));
      for(int i = 0; i< numMediumRiskBorrowers; ++i)
         borrowersMediumRisk.add(
            new CommercialLoanConsumer("Medium Risk Loan Consumer " + i, 100./3.));
      for(int i = 0; i< numHighRiskBorrowers; ++i)
         borrowersHighRisk.add(
            new CommercialLoanConsumer("High Risk Loan Consumer " + i, 100./3.));
      for(int i = 0; i< numBanks; ++i)
         banks.add(new Bank("Bank " + i, 10./3., 3.0, 5.));
      
      /*
       * Call the LCQP response via the MRF:
       */
      {
         Bank testBank = new Bank("Test Bank", 1.0, 5.0, 0.1);
         int[] responsesSought = new int[numBondSuppliers + numBorrowers];
         TradeOpportunity[] trades = new TradeOpportunity[numBondSuppliers + numBorrowers];
         
         final double
            bondInterestRate = 0.05,
            lowRiskCommercialLoanRate = 0.1,
            mediumRiskCommercialLoanRate = 0.15,
            highRiskCommercialLoanRate = 0.2;
         
         {
         int index = 0;
         for(int i = 0; i< numLowRiskBorrowers; ++i)
            trades[index++] = TradeOpportunity.create(lowRiskCommercialLoanRate, 
               new ClearingInstrument("Low Risk Commercial Loan", "[test]"),
                  "Low Risk Loan Consumer " + i);
         for(int i = 0; i< numMediumRiskBorrowers; ++i)
            trades[index++] = TradeOpportunity.create(mediumRiskCommercialLoanRate, 
               new ClearingInstrument("Medium Risk Commercial Loan", "[test]"),
                  "Medium Risk Loan Consumer " + i);
         for(int i = 0; i< numHighRiskBorrowers; ++i)
            trades[index++] = TradeOpportunity.create(highRiskCommercialLoanRate, 
               new ClearingInstrument("High Risk Commercial Loan", "[test]"),
                  "High Risk Loan Consumer " + i);
         for(int i = 0; i< numBondSuppliers; ++i)
            trades[index++] = TradeOpportunity.create(bondInterestRate, 
               new ClearingInstrument("Bond", "[test]"), "Bond Supplier " + i);
         }
         for(int i = 0; i< responsesSought.length; ++i)
            responsesSought[i] = i;
         
         List<Double>
            initialAssetHoldings = new ArrayList<Double>(),
            initialLiabilityHoldings = new ArrayList<Double>();
         for(int i = 0; i< 3; ++i)
            initialAssetHoldings.add(0.);
         initialAssetHoldings.add(testBank.cashReserveSize);
         initialLiabilityHoldings.add(0.);
         
         List<Pair<Double, Double>>
            assetRates = new ArrayList<Pair<Double,Double>>(),
            liabilityRates = new ArrayList<Pair<Double,Double>>();
         
         assetRates.add(Pair.create(
            LOW_RISK_COMMERCIAL_LOAN_MARKET_IMPACT, 
            lowRiskCommercialLoanRate / LOW_RISK_COMMERCIAL_LOAN_PREMIUM
            ));
         assetRates.add(Pair.create(
            MEDIUM_RISK_COMMERCIAL_LOAN_MARKET_IMPACT,
            mediumRiskCommercialLoanRate / MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM
            ));
         assetRates.add(Pair.create(
            HIGH_RISK_COMMERCIAL_LOAN_MARKET_IMPACT,
            highRiskCommercialLoanRate / HIGH_RISK_COMMERCIAL_LOAN_PREMIUM
            ));
         assetRates.add(Pair.create(
            0., 0.));   // Cash
         liabilityRates.add(Pair.create(
            BOND_MARKET_IMPACT, 
            bondInterestRate / BOND_PREMIUM
            ));
         
         Assert.assertTrue(assetRates.size() == 4);
         Assert.assertTrue(liabilityRates.size() == 1);
         Assert.assertTrue(initialAssetHoldings.size() == 4);
         Assert.assertTrue(initialLiabilityHoldings.size() == 1);
         
         {
         for(int i = 0; i< 200; ++i) {
         
         List<Double> result = 
            (new LCQPUncollateralizedReturnMaximizer()).performOptimization(
               assetRates,
               liabilityRates,
               testBank.cashReserveSize,
               testBank.equity * testBank.leverageTarget
               );
         
         System.out.printf(
            "a: %16.10g [%16.10g] %16.10g [%16.10g] %16.10g [%16.10g]" + 
            " c: %16.10g l: %16.10g [%16.10g]\n",
            result.get(0) / numLowRiskBorrowers,
            assetRates.get(0).getSecond(),
            result.get(1) / numMediumRiskBorrowers,
            assetRates.get(1).getSecond(),
            result.get(2) / numHighRiskBorrowers,
            assetRates.get(2).getSecond(),
            result.get(3),
            result.get(4) / numBondSuppliers,
            liabilityRates.get(0).getSecond()
            );
         
         assetRates.get(0).setSecond(assetRates.get(0).getSecond() + 1.e-3);
         }
         }
//         
//         Assert.assertTrue(result.size() == 5);
//         
//         int index = 0;
//         for(int i = 0; i< numLowRiskBorrowers; ++i)
//            volumesExpected[index++] = -result.get(0) / numLowRiskBorrowers;
//         for(int i = 0; i< numMediumRiskBorrowers; ++i)
//            volumesExpected[index++] = -result.get(1) / numMediumRiskBorrowers;
//         for(int i = 0; i< numHighRiskBorrowers; ++i)
//            volumesExpected[index++] = -result.get(2) / numHighRiskBorrowers;
//         for(int i = 0; i< numBondSuppliers; ++i)
//            volumesExpected[index++] = +result.get(4) / numBondSuppliers;
//         }
//         
//         Assert.assertTrue(volumesExpected.length == volumesObserved.length);
//         
//         System.out.printf("Checking expected and observed volumes..\n");
//         
//         for(int i = 0; i< volumesExpected.length; ++i)
//            Assert.assertEquals(volumesObserved[i], volumesExpected[i], 1.e-12);
//         
//         System.out.println("volumes observed:");
//         System.out.println(Arrays.toString(volumesObserved));
//         
//         System.out.println("volumes expected:");
//         System.out.println(Arrays.toString(volumesExpected));
//         
//         System.out.printf("Expected and observed volumes agree.\n");
      }
      
      /*
       * Create an MCN and clear the network using all of the above participants.
       */
      MixedClearingNetwork.Builder builder = new MixedClearingNetwork.Builder();
      
      // Add low risk borrowers
      for(int i = 0; i< numLowRiskBorrowers; ++i)
         builder.addNetworkNode(
            borrowersLowRisk.get(i),
            borrowersLowRisk.get(i).getMarketResponseFunction(null), 
            borrowersLowRisk.get(i).getUniqueName()
            );
      
      // Add medium risk borrowers
      for(int i = 0; i< numMediumRiskBorrowers; ++i)
         builder.addNetworkNode(
            borrowersMediumRisk.get(i),
            borrowersMediumRisk.get(i).getMarketResponseFunction(null), 
            borrowersMediumRisk.get(i).getUniqueName()
            );
      
      // Add high risk borrowers
      for(int i = 0; i< numHighRiskBorrowers; ++i)
         builder.addNetworkNode(
            borrowersHighRisk.get(i),
            borrowersHighRisk.get(i).getMarketResponseFunction(null), 
            borrowersHighRisk.get(i).getUniqueName()
            );
      
      // Add bond suppliers
      for(int i = 0; i< numBondSuppliers; ++i)
         builder.addNetworkNode(
            bondSuppliers.get(i),
            bondSuppliers.get(i).getMarketResponseFunction(null), 
            bondSuppliers.get(i).getUniqueName()
            );
      
      // Add banks
      for(int i = 0; i< numBanks; ++i)
         builder.addNetworkNode(
            banks.get(i),
            banks.get(i).getMarketResponseFunction(null), 
            banks.get(i).getUniqueName()
            );
      
      // Hyperedges
      builder.addHyperEdge("Bond");
      builder.addHyperEdge("Low Risk Commercial Loan");
      builder.addHyperEdge("Medium Risk Commercial Loan");
      builder.addHyperEdge("High Risk Commercial Loan");
      
      // Resources
      ClearingInstrument
         lowRiskCommercialLoanResource =
            new ClearingInstrument("Commercial Loan Market", "Low Risk Commercial Loan"),
         mediumRiskCommercialLoanResource =
            new ClearingInstrument("Commercial Loan Market", "Medium Risk Commercial Loan"),
         highRiskCommercialLoanResource =
            new ClearingInstrument("Commercial Loan Market", "High Risk Commercial Loan"),
         bondResource  =
            new ClearingInstrument("Bond Market", "Bond");
      
      // Graph edges
      for(int i = 0; i< numLowRiskBorrowers; ++i)
         for(int j = 0; j< numBanks; ++j)
            builder.addToHyperEdge(
               borrowersLowRisk.get(i).getUniqueName(),
               banks.get(j).getUniqueName(),
               new TrivialResourceExchangeDelegate(),
               "Low Risk Commercial Loan",
               lowRiskCommercialLoanResource
               );
      
      for(int i = 0; i< numMediumRiskBorrowers; ++i)
         for(int j = 0; j< numBanks; ++j)
            builder.addToHyperEdge(
               borrowersMediumRisk.get(i).getUniqueName(),
               banks.get(j).getUniqueName(),
               new TrivialResourceExchangeDelegate(),
               "Medium Risk Commercial Loan",
               mediumRiskCommercialLoanResource
               );
      
      for(int i = 0; i< numHighRiskBorrowers; ++i)
         for(int j = 0; j< numBanks; ++j)
            builder.addToHyperEdge(
               borrowersHighRisk.get(i).getUniqueName(),
               banks.get(j).getUniqueName(),
               new TrivialResourceExchangeDelegate(),
               "High Risk Commercial Loan",
               highRiskCommercialLoanResource
               );
      
      for(int i = 0; i< numBanks; ++i)
         for(int j = 0; j< numBondSuppliers; ++j)
            builder.addToHyperEdge(
               banks.get(i).getUniqueName(),
               bondSuppliers.get(j).getUniqueName(),
               new TrivialResourceExchangeDelegate(),
               "Bond",
               bondResource
               );
      
      final MixedClearingNetwork network = builder.build();
      
      // Clear the network
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AdaptiveMarchHeterogeneousClearingAlgorithm(
            50, 1.e-10, new OrderOrIterationsStoppingCondition(10, 10, network));
         
      network.applyClearingAlgorithm(clearingAlgorithm);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("LCQPAdapter tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         LCQPAdaperTest test = new LCQPAdaperTest();
         test.setUp();
         test.testLCQPAdapter();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
