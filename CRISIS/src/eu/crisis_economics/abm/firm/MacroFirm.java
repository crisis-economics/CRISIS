/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Ermanno Catullo
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.firm;

//REPORTS import java.io.File;
//REPORTS import java.io.FileNotFoundException;
//REPORTS import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import sim.util.Bag;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.bankruptcy.FirmBankruptcyHandler;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.goods.GoodsInventory;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.inventory.goods.GoodsStorage;
import eu.crisis_economics.abm.inventory.valuation.MarketPriceGoodsRepositoryValuation;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsInstrument;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarketOrder;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarketOrder;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.BoundedUnivariateFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingSimpleCommercialLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.InverseExpIOCPartitionFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.PartitionedResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.FirmProductionFunctionFactory;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

public final class MacroFirm
   extends ClearingFirm 
   implements GoodsBuyer, GoodsSeller, Employer {
   
   private FirmBankruptcyHandler
      bankruptcyHandler;
   
   /** 
    *  Local encapsulation for goods inventories and goods production data. 
    *  When using this inner class locally, avoid direct member field access 
    *  to the extent that this is possible.
    *  
    *  This object contains information about firm production optimisation 
    *  criteria, goods yields in the current business cycle, target production 
    *  and price selection algorithms, goods inventories and market valuation 
    *  functions.
    **/
   final class GoodsAndProduction {
      
      FirmProductionFunction                   
         goodsProductionFunction;                   // Goods production function 
      
      private FirmProductionFunction.Yield
         lastGoodsYieldFromProduction;              // Goods production yield in the last cycle.
      
      private double
         targetGoodsProductionLastCycle,            // Goods target production for the last cycle.
         actualGoodsProductionLastCycle;            // Actual yield for the last cycle.
      
      private FirmDecisionRule            
         goodsTargetProductionSelectionAlgorithm;   // Goods target production algorithm 
      
      private FirmDecisionRule            
         goodsMarketPriceAskSelectionAlgorithm;     // Goods pricing algorithm
      
      private SimpleGoodsMarket                     
         durableGoodsExchangeMarket;                // Durable goods exchange market
      
      private DurableGoodsRepository                      
         goodsWarehouse;                            // Goods repository, multiple types
      
      private GoodsInventory 
         localStoreForProductionGoods;              // Production goods storage
      
      LinkedHashMap<String, Double>
         demandForInputGoodsNow,                    // Current demand for input goods.
         goodsQuantitiesObtainedInThisStep;         // Goods obtained from the market (this step).
      
      private String 
         productionGoodsType;
      
      private double
         productionGoodsUnsoldInThisCycle,          // Amount of production goods unsold now.
         productionGoodsUnsoldLastCycle;            // Amount of production goods unsold last cycle.
      
      private SimpleGoodsMarketOrder                // Individual market goods orders.
         activeGoodsSaleOrder;                      
      private Map<String, SimpleGoodsMarketOrder>   
         activeGoodsBuyOrders;                      
      
      private Contract
         goodsInventoryValue;                       // Goods as a component of equity.
      
      /** Encapsulation for goods exchange and production activities. */
      private GoodsAndProduction(
         FirmProductionFunction goodsProductionFunction,
         FirmDecisionRule targetProductionAlgorithm,
         FirmDecisionRule goodsPricingAlgorithm,
         SimpleGoodsMarket durableGoodsMarket,
         double initialOwnedGoods,
         String goodsType
         ) {
         StateVerifier.checkNotNull(
            goodsProductionFunction, targetProductionAlgorithm,
            goodsPricingAlgorithm, durableGoodsMarket);
         
         this.goodsProductionFunction = goodsProductionFunction;
         this.goodsTargetProductionSelectionAlgorithm = targetProductionAlgorithm;
         this.goodsMarketPriceAskSelectionAlgorithm = goodsPricingAlgorithm;
         
         this.durableGoodsExchangeMarket = durableGoodsMarket;
         if(!durableGoodsMarket.hasInstrument(goodsType))
            durableGoodsMarket.addInstrument(goodsType);
         durableGoodsMarket.getGoodsInstrument(goodsType).registerSeller(MacroFirm.this);
         this.goodsWarehouse = new DurableGoodsRepository(durableGoodsMarket.getGoodsClassifier());
         this.goodsWarehouse.pushWithDelay(goodsType, initialOwnedGoods);
         this.localStoreForProductionGoods = goodsWarehouse.get(goodsType);
         
         this.productionGoodsType = Preconditions.checkNotNull(goodsType);
         this.activeGoodsBuyOrders = new HashMap<String, SimpleGoodsMarketOrder>();
         this.demandForInputGoodsNow = new LinkedHashMap<String, Double>();
         this.goodsQuantitiesObtainedInThisStep = new LinkedHashMap<String, Double>();
         
         this.goodsInventoryValue = new MarketPriceGoodsRepositoryValuation(
            goodsWarehouse, MacroFirm.this, durableGoodsMarket);
         
         this.schedule();
      }
      
      // Add repetitive event scheduling for goods and production.
      private void schedule() {
         Simulation.repeat(this, "sellGoodsProducedInTheLastCycle", 
            NamedEventOrderings.SELL_ORDERS);
         Simulation.repeat(this, "produce", NamedEventOrderings.PRODUCTION);
         Simulation.repeat(this, "decideNextSellingPriceAndTargetProduction",
            CustomSimulationCycleOrdering.create(NamedEventOrderings.FIRM_ACCOUNTING, 2));
      }
      
      /*
       * Select the next goods target production amount, optimize for input goods (and labour) 
       * required to yield this volume, and decide the next goods unit selling price. The 
       * algorithms that generate these terms are goodsTargetProductionSelectionAlgorithm and 
       * goodsMarketPriceAskSelectionAlgorithm, respectively.
       */
      private void considerProduction() {
         // Decide input goods and labour to yield the target production.
         goodsAndProduction.selectGoodsAndLabourInputDemandsForProduction();
      }
      
      @SuppressWarnings("unused")   // Scheduled
      private void decideNextSellingPriceAndTargetProduction() {
         // Generate a new target production volume.
         goodsAndProduction.decideNextTargetGoodsProduction();
         // Generate a new market goods unit selling price. Note that the selling price ,
         // combined with the unit manufacture cost, defines the markup rate. 
         goodsAndProduction.decideNextMarketSellingPrice();
      }
      
      private void flushMemories() {
         targetGoodsProductionLastCycle = 
            getTargetProductionAlgorithm().getLastValue();
         productionGoodsUnsoldLastCycle = 
            productionGoodsUnsoldInThisCycle;
         actualGoodsProductionLastCycle = 
            getMostRecentGoodsYield();
         productionGoodsUnsoldInThisCycle =
            getMostRecentGoodsYield();
         lastGoodsYieldFromProduction = null;
         demandForInputGoodsNow.clear();
         goodsQuantitiesObtainedInThisStep.clear();
         final String errMsg =
            "MacroFirm.flushMemories: [before-all] state indicates that the market has " + 
            "failed to deallocate (unblock) saleable goods from this firm's local production " + 
            "goods store before a new business cycle commenced. This behaviour is not " + 
            "expected in the current implementation.";
         // Bookkeeping
         for(SimpleGoodsMarketOrder order : activeGoodsBuyOrders.values()) {
            if(order == null)
               continue;
            else {
               order.cancel();
               System.err.println(
                  "MacroFirm.flushMemories: a goods market buy order is outstanding at "
                + "the close of the cycle. This behaviour is not expected in the current "
                + "implementation. The order has been terminated."
                  );
               System.err.flush();
            }
         }
         if(activeGoodsSaleOrder != null) {
            activeGoodsSaleOrder.cancel();
            activeGoodsSaleOrder = null;
            System.err.println(
                "MacroFirm.flushMemories: a goods market sale order is outstanding at "
              + "the close of the cycle. This behaviour is not expected in the current "
              + "implementation. The order has been terminated."
                );
         }
         
         for(Inventory store : goodsWarehouse.values()) {
            if(store.getAllocated() > 1.e-8)
               System.err.println(errMsg + store);
            store.disallocateAll();
         }
      }
      
      /** Get the firm production function. */
      private FirmProductionFunction getProductionFunction() {
         return goodsProductionFunction;
      }
      
      /** Get the firm target production selection algorithm. */
      private FirmDecisionRule getTargetProductionAlgorithm() {
         return goodsTargetProductionSelectionAlgorithm;
      }
      
      /** Get the firm goods price selection algorithm. */
      private FirmDecisionRule getGoodsPricingAlgorithm() {
         return goodsMarketPriceAskSelectionAlgorithm;
      }
      
      /** Get the durable goods exchange market. */
      private SimpleGoodsMarket getGoodsMarket() {
         return durableGoodsExchangeMarket;
      }
      
      /** Get the firm goods repository (containing all goods types). */
      private DurableGoodsRepository getGoodsRepository() {
         return goodsWarehouse;
      }
      
      /** Get the firm production goods store. */
      private GoodsStorage getProductionGoodsStorage() {
         return localStoreForProductionGoods;
      }
      
      /** 
        * Get a {@link LinkedHashMap} of goods market prices at the time of the
        * query. The order of elements in the return value is the same as the
        * iteration order of goods instruments in the goods market.s
        */
      public LinkedHashMap<String, Double> getGoodsMarketPrices() {
         final LinkedHashMap<String, Double>
            result = new LinkedHashMap<String, Double>();
         for(final SimpleGoodsInstrument instrument : getGoodsMarket())
            result.put(
               instrument.getGoodsType(),
               instrument.getWorstAskPriceAmongSellers()
               );
         
//REPORTS         // Reports
//REPORTS         for(int goodsType = 0; goodsType< getNumberOfSectors(); ++goodsType) {
//REPORTS            MacroFirm.reportWriter.println(
//REPORTS               MacroFirm.this,
//REPORTS               "market price for type " + goodsType + ": " + result.get(goodsType) + "."
//REPORTS               );
//REPORTS         }
         
         return result;
      }
      
      /**
        * Get a {@link LinkedHashMap} of economics costs (generalized prices, per unit)
        * for all goods on the market. The order of entries in the returned value is
        * the same as the iteration order of goods instruments in the goods market.
        */
      LinkedHashMap<String, Double> getEconomicCosts() {
         final LinkedHashMap<String, Double> 
            goodsUnitPrices = getGoodsMarketPrices(),
            economicCostsResult = new LinkedHashMap<String, Double>();
         for(final Entry<String, Double> record : goodsUnitPrices.entrySet()) {
            final String
               goodsType = record.getKey();
            // Durability characteristics are identical among producers of each type.
            final Pair<Double, Double> goodsCharacteristics = 
               durableGoodsExchangeMarket.getGoodsClassifier().getCharacteristics(goodsType);
            double
               delta_D = goodsCharacteristics.getFirst(),
               delta_C = goodsCharacteristics.getSecond(),
               economicUnitCost = goodsUnitPrices.get(goodsType) * 
                  (delta_D + delta_C - delta_D * delta_C);
            economicCostsResult.put(goodsType, economicUnitCost);
         }
         
//REPORTS         // Reports
//REPORTS         for(int goodsType = 0; goodsType< numberOfSectors; ++goodsType) {
//REPORTS            double economicUnitCost = economicCostsResult.get(goodsType); 
//REPORTS            reportWriter.println(
//REPORTS               MacroFirm.this, 
//REPORTS               "economic cost of goods type " + goodsType + ": " + economicUnitCost + 
//REPORTS               ", " + String.format("%6.3g%%", 100. * goodsUnitPrices.get(goodsType) / 
//REPORTS               economicUnitCost) + " of unit price."
//REPORTS               );
//REPORTS         }
         
         return economicCostsResult;
      }
      
      /** Get the current markup rate on production goods. */
      public double getMarkUp() {
         double unitCost = 
            depositsAndLoans.getLiquidityRequiredForCurrentProductionCycle()/
               getGoodsProductionTargetNow();
         return getProductionGoodsUnitSellingPrice()/unitCost - 1.;
      }
      
      // Get outstanding production goods unsold in this cycle.
      private double getUnsoldProductionGoodsNow() {
         return productionGoodsUnsoldInThisCycle;
      }
      
      // Get production goods unsold in the last cycle.
      private double getUnsoldProductionGoodsLastCycle() {
         return productionGoodsUnsoldLastCycle;
      }
      
      /** 
       *  Note: allow sales to occur even in a state of pending liquidation bankruptcy. 
       *  The firm cannot (directly) generate a lower equity value, or a negative revenue, 
       *  from this market activity. 
       */
      @SuppressWarnings("unused") // Scheduled
      private void sellGoodsProducedInTheLastCycle() {
         final double goodsAmountToSell = getUnreservedGoodsQuantity();
         final String errMsg = 
            "MacroFirm.goodsAndProduction.sellGoodsProducedInTheLastCycle: the durable " +
            "goods market rejected a ask order. The order had size " + goodsAmountToSell +
            " and market unit price " + getGoodsSellingPrice() + ".";
         if(goodsAmountToSell != getMostRecentGoodsYield()) {
            System.err.println(
               "MacroFirm.goodsAndProduction.sellGoodsProducedInTheLastCycle: warning: the "
             + " quantity of goods to sell is not equal to the previous production yield. "
             + "previous yield: " + getMostRecentGoodsYield() + ", current inventory: " 
             + goodsAmountToSell + ".");
         }
         try {
            if (goodsAmountToSell > 0.) {
               final SimpleGoodsMarketOrder order =
                  durableGoodsExchangeMarket.addOrder(
                     MacroFirm.this,
                     getGoodsType(),
                     goodsAmountToSell, 
                     getGoodsSellingPrice()
                     );
               activeGoodsSaleOrder = order;
            }
         }
         catch(final AllocationException nothingToSell) {                   // Failed, No action.
            System.err.println(errMsg);
         }
         catch(final OrderException unexpectedOrderRejection) {             // Failed, No action.
            System.err.println(errMsg);
         }
         
//REPORTS         //REPORTS Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            " executing sellGoodsProducedInTheLastCycle.");
//REPORTS         reportWriter.println(MacroFirm.this, "offering " + goodsAmountToSell + 
//REPORTS            " units of goods to the market for sale at price " + getSellingPrice() + ".");
      }
      
      // Minimise economic costs by optimising the Production Function at fixed yield.
      private void selectGoodsAndLabourInputDemandsForProduction() {
         
         final Map<String, Double>
            goodsUnitPrices = getGoodsMarketPrices(),
            economicCosts = getEconomicCosts();
         
         final OptimalProduction solution = 
            getProductionFunction().computeInputsDemand(
               getTargetProductionAlgorithm().getLastValue(), 
               labourAndWage.getMostRecentWageAsk(), 
               economicCosts
               );
         
         demandForInputGoodsNow.clear();
         demandForInputGoodsNow = new LinkedHashMap<String, Double>(solution.getDemandAmounts());
         
         labourAndWage.setLabourDesiredForProduction(
            solution.getDesiredUnitsOfLabour());
         double liquidityNeededNowForProduction = 
            labourAndWage.getLabourDesiredForProduction() * 
            labourAndWage.getMostRecentWageAsk();
         /* 
          * Account for existing durable goods. Note that production goods
          * from the last business cycle are reserved for sale, so input 
          * goods of the same class as the firms' production must be bought
          * from the market. For MFPS (Multiple Firms Per Sector) setups,
          * typically this will result in goods exchange among producers of the 
          * same type. For non-MFPS setups, firms will buy input goods from 
          * themselves (as is normal for macro models of the production sector).
          */
         {
         final LinkedHashMap<String, Double>
            inputDemandCopy = new LinkedHashMap<String, Double>(demandForInputGoodsNow);
         for(final Entry<String, Double> record : inputDemandCopy.entrySet()) {
            final String
               name = record.getKey();
            final Inventory
               localGoodsStore = goodsWarehouse.get(name);
            final double 
               ownedGoodsOfThisType = (localGoodsStore == null ? 
                  0. : localGoodsStore.getUnallocated()),
               demand = Math.max(
                  demandForInputGoodsNow.get(name) - ownedGoodsOfThisType, 0.);
               demandForInputGoodsNow.put(name, demand);
            liquidityNeededNowForProduction += 
               demandForInputGoodsNow.get(name) * goodsUnitPrices.get(name);
         }
         }
         depositsAndLoans.recordCashTransaction(
            liquidityNeededNowForProduction, CashEventType.LIQUIDITY_REQUIREMENT);
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "executing selectGoodsAndLabourInputDemandsForProduction."); 
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "labour demand: " + labourAndWage.getLabourDesiredForProduction());
//REPORTS         for(int goodsType = 0; goodsType< getNumberOfSectors(); ++goodsType) {
//REPORTS            reportWriter.println(
//REPORTS               MacroFirm.this, "demand for type " + goodsType + ", accouting for inventory: " + 
//REPORTS               demandForInputGoodsNow.get(goodsType) + "."
//REPORTS               );
//REPORTS         }
//REPORTS         {
//REPORTS         final double percentOfLiquidityDemandIsLabour = 
//REPORTS            100. * (labourAndWage.getLabourDesiredForProduction() * 
//REPORTS               labourAndWage.getMostRecentWageAsk()) / liquidityNeededNowForProduction;
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "percentage of production liquidity is wage: " + 
//REPORTS               String.format("%7.3g%%", percentOfLiquidityDemandIsLabour));
//REPORTS         }
         
      }
      
      // Select the next target goods production.
      private void decideNextTargetGoodsProduction() {
         
//REPORTS         final double lastProductonTarget = 
//REPORTS         getTargetProductionAlgorithm().getLastProductionTarget();
         
         getTargetProductionAlgorithm().
            computeNext(MacroFirm.this.getState());
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "new production target: " + 
//REPORTS               String.format("%+10.5g", 
//REPORTS                  getTargetProductionAlgorithm().getLastProductionTarget()) + ", " + 
//REPORTS               String.format("%+10.5g", lastProductonTarget) + " change since last " + 
//REPORTS               "business cycle.");
         
      }
      
      // Select the next goods market selling price (per unit).
      private void decideNextMarketSellingPrice() {
         
//REPORTS         final double lastMarketUnitSellingPrice = 
//REPORTS         getGoodsPricingAlgorithm().getLastValue();
         
         getGoodsPricingAlgorithm().computeNext(MacroFirm.this.getState());
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "new market selling ask price: " + 
//REPORTS               String.format("%+10.5g", 
//REPORTS                  getGoodsPricingAlgorithm().getLastValue()) + ", " + 
//REPORTS               String.format("%+10.5g", lastMarketUnitSellingPrice) + " change since last " + 
//REPORTS               "business cycle.");
         
      }
      
      @SuppressWarnings("unused") // Scheduled
      /*
       * Goods produced in the current business cycle will not appear in 
       * local repositories and storage until the next business cycle.
       * By design, firms in the production sector will sell goods at the 
       * beginning of the next cycle; "Produce Today Sell Tomorrow".
       */
      private void produce() {
         /*
          * Cancel outstanding goods buy orders. Goods market orders will not
          * be purged automatically until after domestic consumption, so all
          * orders need to be removed manually.
          */
         for(final SimpleGoodsMarketOrder buyOrder : activeGoodsBuyOrders.values())
            if(buyOrder != null) buyOrder.cancel();
         
         final LinkedHashMap<String, Double>
            goodsMarketUnitPrices = getGoodsMarketPrices();
         
         final double
            labourForceForProduction = MacroFirm.this.getLabourForce();
         if (labourForceForProduction == 0.) {
            final String
               warningMessage = "MacroFirm.produce(): [warning] no labour was purchased."; 
            System.err.println(warningMessage);
            System.err.println();
            
//REPORTS            reportWriter.println(MacroFirm.this, warningMessage); 
         } 
         
         final Map<String, Double>
            goodsToConsume = new HashMap<String, Double>();
         
         // All market goods instruments must be included
         for(final SimpleGoodsInstrument instrument : getGoodsMarket()) {
            final String
               name = instrument.getGoodsType();
            final GoodsInventory
               inventory = goodsWarehouse.get(name);
            final double
               inputQuantity = inventory == null ? 0. : inventory.getUnallocated();
            goodsToConsume.put(name, inputQuantity);
            if(inventory != null)
               inventory.consume(inputQuantity);
         }
         
         final FirmProductionFunction.Yield yield = 
            goodsProductionFunction.produce(
               labourForceForProduction,
               goodsToConsume, 
               labourAndWage.getMostRecentWageAsk(), 
               goodsMarketUnitPrices
               );
         
         localStoreForProductionGoods.pushWithDelay(              // Produce Today Sell Tomorrow
            Math.max(yield.getGoodsYield(), 1.e-5)); 
         lastGoodsYieldFromProduction = yield;
          
//REPORTS         // Production Reports
//REPORTS         
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "production: effective loan at production time: " + 
//REPORTS            depositsAndLoans.getLoanCashAquiredThisCycle());
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "production: labour force is " + labourAndWage.getLabourEmployedNow());
//REPORTS         for(int i = 0; i< getNumberOfSectors(); ++i)
//REPORTS            reportWriter.println(MacroFirm.this, 
//REPORTS               "production: consuming " + 
//REPORTS                  goodsToConsume.get(i) + " units of type " + i + ".");
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "production: yielded " + yield.getGoodsYield() + " units of new goods.");
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "production: liquidity now: " + getUnreservedCash() + ".");
      }
      
      // Accounting and bookkeeping at the end of the business cycle.
      private void closeOfCycle() {
         cancelGoodsOrders();
         
//REPORTS         //REPORTS Reports
//REPORTS         String[] goodsAndProductionReport = toString().split("\n");
//REPORTS         for(String note : goodsAndProductionReport)
//REPORTS            reportWriter.println(MacroFirm.this, note);
      }
      
      /** React, as appropriate, to an incoming trade volume. */
      void registerIncomingTradeVolume(
         final String goodsType,
         final double goodsAmount,
         final double unitPricePaid
         ) {
         final double alreadyReceived = 
            goodsQuantitiesObtainedInThisStep.containsKey(goodsType) ?
               goodsQuantitiesObtainedInThisStep.get(goodsType) : 0.;
         goodsQuantitiesObtainedInThisStep.put(goodsType, alreadyReceived + goodsAmount);
         depositsAndLoans.recordCashTransaction(
            goodsAmount * unitPricePaid, CashEventType.GOODS_ACQUISITION_CASH_COST);
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "recieved " + goodsAmount + " units of type " + goodsType + ". Total obtained " + 
//REPORTS            "in this cycle: " + goodsQuantitiesObtainedInThisStep.get(goodsType) + "."
//REPORTS            );
      }
      
      /** React, as appropriate, to an outgoing trade volume. */
      public void registerOutgoingTradeVolume(
         final String goodsType,
         final double goodsAmount,
         final double unitPricePaid
         ) {
         depositsAndLoans.recordCashTransaction(
            goodsAmount * unitPricePaid, CashEventType.REVENUE_FROM_GOODS_SALES);
         productionGoodsUnsoldInThisCycle -= goodsAmount;
         productionGoodsUnsoldInThisCycle = Math.max(productionGoodsUnsoldInThisCycle, 0.);
      }
      
      // Purchase input goods
      private void submitMarketOrdersToBuyGoods() {
         final Map<String, Double>
            marketPriceList = getGoodsMarketPrices();
         // Typically, a firm does buy from its own sector.
         for(final Entry<String, Double> record : 
            demandForInputGoodsNow.entrySet()) {
            final String
               name = record.getKey();
            final double
               demandForThisType = record.getValue(),
               marketPriceOfThisType = marketPriceList.get(name);
            final String errMsg = 
               "MacroFirm.goodsAndProduction.submitMarketOrdersForGoods: the durable " +
               "goods market rejected a ask order. The order had size " + demandForThisType +
               " and market unit price " + marketPriceOfThisType + ". ";
            if(demandForThisType > 0.) {
               try {
                  final SimpleGoodsMarketOrder order = 
                     durableGoodsExchangeMarket.addOrder(
                        MacroFirm.this, 
                        name, 
                        -demandForThisType,
                        marketPriceOfThisType
                        );
                  activeGoodsBuyOrders.put(name, order);
               }
               catch(final AllocationException e) {                             // Failed
                  System.err.println(errMsg + e);                               // No action
               }
               catch(final OrderException e) {                                  // Failed
                  System.err.println(errMsg + e);                               // No action
               }
            }
         }
         
//REPORTS         //REPORTS Reports
//REPORTS         for(int goodsType = 0; goodsType< getNumberOfSectors(); ++goodsType) {
//REPORTS            final double
//REPORTS               demandForThisType = demandForInputGoodsNow.get(goodsType),
//REPORTS               marketPriceOfThisType = marketPriceList.get(goodsType),
//REPORTS               expectedCostOfThisOrder = demandForThisType * marketPriceOfThisType;
//REPORTS            reportWriter.println(
//REPORTS               MacroFirm.this, 
//REPORTS               "ordering " + demandForThisType + " units of type " + goodsType + 
//REPORTS               ", price: " + marketPriceOfThisType + ". Total anticipated goods cost: " + 
//REPORTS               expectedCostOfThisOrder + "."
//REPORTS               );
//REPORTS         }
      }
      
      /** Ration all goods input demands by a fixed multiplier. */
      private void rationAllGoodsDemandsByMultiplier(double rationMultiplier) {
         if(rationMultiplier < 0.)
            throw new IllegalArgumentException(
               "MacroFirm.rationAllGoodsDemandsByMultiplier: argument (value " + 
               rationMultiplier + ") is negative.");
         for(final Entry<String, Double> record : demandForInputGoodsNow.entrySet())
            demandForInputGoodsNow.put(
               record.getKey(),
               demandForInputGoodsNow.get(record.getKey()) * rationMultiplier
               );
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(
//REPORTS            MacroFirm.this, "rationing all goods inputs demands by " + 
//REPORTS            String.format("%8.3g%%", rationMultiplier * 100.) + "."); 
      }
      
      // Remove a goods order from the market.
      private boolean removeGoodsOrder(SimpleGoodsMarketOrder order) {
         if(order == null)
            return false;
         if(order.getSide() == SimpleGoodsMarketOrder.Side.BUY) {
            final SimpleGoodsMarketOrder existingOrder = 
               activeGoodsBuyOrders.get(order.getGoodsType());
            if(existingOrder != order) return false;
            activeGoodsBuyOrders.put(order.getGoodsType(), null);
         } else {
            if(order != activeGoodsSaleOrder) return false;
            activeGoodsSaleOrder = null;
         }
         return true;
      }
      
      // Remove goods orders from the market.
      private void cancelGoodsOrders() {
         for(SimpleGoodsMarketOrder buyOrder : activeGoodsBuyOrders.values())
            if(buyOrder != null) buyOrder.cancel();
         if(activeGoodsSaleOrder != null) activeGoodsSaleOrder.cancel();
      }
      
      /** Get an unmodifiable list of goods received from the market 
       *  in this time step. */
      public Map<String, Double> getGoodsObtainedInThisTimeStep() {
         return Collections.unmodifiableMap(goodsQuantitiesObtainedInThisStep);
      }
      
      /** Get the current goods unit selling price. */
      public double getProductionGoodsUnitSellingPrice() {
         return goodsMarketPriceAskSelectionAlgorithm.getLastValue();
      }
      
      /** Get the goods target production now. */
      public double getGoodsProductionTargetNow() {
         return goodsTargetProductionSelectionAlgorithm.getLastValue();
      }
      
      /** Get the goods target production in the last cycle. */
      public double getGoodsProductionTargetLastCycle() {
         return targetGoodsProductionLastCycle;
      }
      
      /** Get the last recorded goods yield from production. */
      public double getMostRecentGoodsYield() {
         if(lastGoodsYieldFromProduction == null) // Queried during initialisation.
            return localStoreForProductionGoods.getStoredQuantity();
         return lastGoodsYieldFromProduction.getGoodsYield();
      }
      
      /** Get the class of goods produced by this firm. */
      public String getGoodsType() {
         return productionGoodsType;
      }
      
      /** Get the unreserved goods quantity now. */
      public double getUnreservedGoodsQuantity() {
         return localStoreForProductionGoods.getUnallocated();
      }
      
      /** Get the reserved goods quantity now. */
      public double getReservedGoodsQuantity() {
         return localStoreForProductionGoods.getAllocated();
      }
      
      /** Get the total production goods owned now. */
      public double getTotalGoodsOwnedOfOwnClass() { 
         return localStoreForProductionGoods.getStoredQuantity();
      }
      
      /** Get the total amount of goods owned by type. */
      public double getTotalGoodsOwned(int goodsType) {
         Inventory goodsStore = goodsWarehouse.get(goodsType);
         if(goodsStore == null) return 0.;
         return goodsStore.getStoredQuantity();
      }
      
      /**
       * Returns a brief description of this object. The exact details of the
       * string are subject to change.
       */
      @Override
      public String toString() {
         String result = 
            "goods obtained in this cycle: " + getGoodsObtainedInThisTimeStep() + ".\n" + 
            "goods unit selling price ask now: " + getProductionGoodsUnitSellingPrice() + ".\n" +
            "previous goods production target: " + getGoodsProductionTargetNow() + ".\n" +
            "latest goods yield: " + getMostRecentGoodsYield() + ".\n" +
            "unreserved production goods now: " + getUnreservedGoodsQuantity() + ".\n" +
            "reserved production goods now: " + getReservedGoodsQuantity() + ".\n" +
            "total production goods owned now: " + getTotalGoodsOwnedOfOwnClass() + ".\n";
         return result;
      }
      
      /**
        * Get the value of all goods inventory and capital goods owned by this firm.
        */
      public double getGoodsInventoryValue() {
         return goodsInventoryValue.getValue();
      }
   }
   
   final GoodsAndProduction goodsAndProduction;
   
   private enum CashEventType {
      REVENUE_FROM_GOODS_SALES,
      LABOUR_ACQUISITION_COST,
      GOODS_ACQUISITION_CASH_COST,
      OUTGOING_DIVIDEND_PAYMENT,
      DIVIDEND_SUM_TO_PAY,
      LOAN_REPAYMENT,
      CASH_FROM_NEW_LOANS,
      BANKRUPTCY_INTERVENTION,
      LIQUIDITY_REQUIREMENT
   }
   
   static private class LoanTransactionRecord {
      static private enum CashMovement {
         INCOMING,
         OUTGOING;
      }
      
      private double timeOfTransaction;
      
      private CashMovement cashFlowDirection;
      
      private double cashFlowAmount;
      
      private LoanTransactionRecord(
         double timeOfTransaction,
         CashMovement cashMovementDirection,
         double cashTransactionAmount
         ) {
         this.timeOfTransaction = timeOfTransaction;
         this.cashFlowDirection = cashMovementDirection;
         this.cashFlowAmount = cashTransactionAmount;
      }
       
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, however the following is typical:
        * 
        * 'Loan transaction record. Time of transaction: 1.0, cash flow
        * direction: INGOING, cash flow amount: 2.5.'
        */
       @Override
       public String toString() {
          String result = 
             "Loan transaction record. Time of transaction: "
             + timeOfTransaction + ", " + "cash flow direction: "
             + cashFlowDirection.toString() + ", cash flow amount: "
             + cashFlowAmount + ".";
          return result;
      }
   }
   
   static private class DividendTransactionRecord {
      private final double
         totalPayment,
         pricePerShareNow,
         expectedDividendPaymentRemains,
         simulationTimeNow,
         numberOfSharesOwnedByInvestor,
         totalSharesBelievedToExist;
      
      private DividendTransactionRecord(
         double totalPayment,
         double pricePerShareNow,
         double numberOfSharesOwnedByInvestor,
         double expectedDividendPaymentRemains,
         double totalSharesBelievedToExist
         ) {
         this.totalPayment = totalPayment;
         this.pricePerShareNow = pricePerShareNow;
         this.expectedDividendPaymentRemains = expectedDividendPaymentRemains;
         this.simulationTimeNow = Simulation.getFloorTime();
         this.numberOfSharesOwnedByInvestor = numberOfSharesOwnedByInvestor;
         this.totalSharesBelievedToExist = totalSharesBelievedToExist;
      }
      
      /**
       * Returns a brief description of this object. The exact details of the
       * string are subject to change, however the following is typical:
       * 
       * 'Dividend transaction record. Outgoing payment: 1e7, price per share: 
       *  0.1, dividend remains: 2e7, time: 10.0, shares owned: 2e7, total
       *  shares believed to exist: 3e7.'
       */
      @Override
      public String toString() {
         return
            "Dividend transaction records. Outgoing payment: " + totalPayment + 
            ", price per share: " + pricePerShareNow + ", "
            + "dividend remains: " + expectedDividendPaymentRemains + 
            ", time: " + simulationTimeNow + ", shares owned: " + 
            numberOfSharesOwnedByInvestor + ", total shares believed to exist: " +
            totalSharesBelievedToExist + ".";
      }
   }
   
   /**
    *  Local encapsulation for deposits and loan behaviour, including the Credit
    *  Demand Function (CDF) for interest-rate dependent loans. When using this 
    *  inner class locally, use getters and mutator methods (rather than direct
    *  field access) to the extent that this is possible.
    *  
    *  This object provides a cash bookkeeping function recordCashTransaction. This
    *  method accepts as arguments a cash value and a cash event type. Possible cash
    *  event types are listed descriptively in MacroFirm.CashEventType.
    */
   final class DepositsAndLoans {
      
      private double
         liquidityRequiredForCurrentProductionCycle,
            // Ideal liquidity required now for target production.
         liquidityRequiredForLastProductionCycle,
            // Ideal liquidity required for target production in the last cycle.
         profitsToDateThisCycle,
         dividendsToPayThisCycle,
         revenueFromGoodsSalesThisCycle,
         goodsAquisitionExpensesThisCycle,
         labourAquisitionExpensesThisCycle,
         depositsAtBeginningOfCycle,
         loanRepaymentsMadeThisCycle,
         loanCashAquiredThisCycle,
         bankruptcyInterventionThisCycle;
      
      private final FirmDecisionRule
         absoluteLiquidityTarget;
      
      private List<LoanTransactionRecord> 
         historicalLoanTransactionRecords;
      
      private List<DividendTransactionRecord>
         historicalDividendTransactionRecords;
      
      private CreditDemandFunction                    
         creditDemandFunction;                         // Credit demand function (CDF)
      
      private DepositsAndLoans(
         CreditDemandFunction creditDemandFunction,
         FirmDecisionRule absoluteLiquidityTarget
         ) {
         StateVerifier.checkNotNull(creditDemandFunction, absoluteLiquidityTarget);
         this.creditDemandFunction = creditDemandFunction;
         this.absoluteLiquidityTarget = absoluteLiquidityTarget;
         this.historicalLoanTransactionRecords =
            new ArrayList<LoanTransactionRecord>();
         this.historicalDividendTransactionRecords = 
            new ArrayList<MacroFirm.DividendTransactionRecord>();
         this.flushMemories();
      }
      
      private void flushMemories() {
         liquidityRequiredForLastProductionCycle = 
            liquidityRequiredForCurrentProductionCycle;
         liquidityRequiredForCurrentProductionCycle = 0.;
         profitsToDateThisCycle = 0.;
         revenueFromGoodsSalesThisCycle = 0.;
         goodsAquisitionExpensesThisCycle = 0.;
         labourAquisitionExpensesThisCycle = 0.;
         depositsAtBeginningOfCycle = cashLedger.getStoredQuantity();
         loanRepaymentsMadeThisCycle = 0.;
         loanCashAquiredThisCycle = 0.;
         bankruptcyInterventionThisCycle = 0.;
      }
      
      // Record a cash transaction.
      private void recordCashTransaction(
         double cashFlowValue, CashEventType eventType) {
         switch(eventType) {
         case REVENUE_FROM_GOODS_SALES:
            profitsToDateThisCycle += cashFlowValue;
            revenueFromGoodsSalesThisCycle += cashFlowValue;
            break;
         case LABOUR_ACQUISITION_COST:
            profitsToDateThisCycle -= cashFlowValue;
            labourAquisitionExpensesThisCycle += cashFlowValue;
            break;
         case GOODS_ACQUISITION_CASH_COST:
            profitsToDateThisCycle -= cashFlowValue;
            goodsAquisitionExpensesThisCycle += cashFlowValue;
            break;
         case OUTGOING_DIVIDEND_PAYMENT:
            dividendsToPayThisCycle -= cashFlowValue;
            break;
         case DIVIDEND_SUM_TO_PAY:
            dividendsToPayThisCycle += cashFlowValue;
            break;
         case LOAN_REPAYMENT:
            profitsToDateThisCycle -= cashFlowValue;
            loanRepaymentsMadeThisCycle += cashFlowValue;
            {
            LoanTransactionRecord newLoanRecord = 
               new LoanTransactionRecord(
                  Simulation.getFloorTime(), 
                  LoanTransactionRecord.CashMovement.OUTGOING,
                  cashFlowValue
                  );
            historicalLoanTransactionRecords.add(newLoanRecord);
            }
            break;
         case CASH_FROM_NEW_LOANS:
            profitsToDateThisCycle += cashFlowValue;
            loanCashAquiredThisCycle += cashFlowValue;
            {
            LoanTransactionRecord newLoanRecord = 
               new LoanTransactionRecord(
                  Simulation.getFloorTime(), 
                  LoanTransactionRecord.CashMovement.INCOMING,
                  cashFlowValue
                  );
             historicalLoanTransactionRecords.add(newLoanRecord);
            }
            break;
         case BANKRUPTCY_INTERVENTION:
            bankruptcyInterventionThisCycle += cashFlowValue;
            profitsToDateThisCycle += cashFlowValue;
            break;
         case LIQUIDITY_REQUIREMENT:
            liquidityRequiredForCurrentProductionCycle += cashFlowValue;
            break;
         default:
         }
      }
      
      /* Get the maximum loan required for production in this business cycle. 
       * Market conditions may mean that the loan actually acquired by the firm 
       * is less than this value. */
      private double getMaximumLoanRequest() {
          final double 
             maximumLoan = 
                depositsAndLoans.getLiquidityRequiredForCurrentProductionCycle() - 
                   cashLedger.getUnallocated();
          return Math.max(maximumLoan, 0.);
      }
      
      /* Get loan repayment liabilities due at this time, not including new loans 
       * taken out in this time step. */
      private double getLoanLiabilitiesDueNotIncludingNewLoans() {
         final double simulationTimeNow = Simulation.getFloorTime();
         double result = 0.;
         for(Contract contract : getLiabilities()){
            if(!(contract instanceof Loan)) continue; // TODO: fix this
            Loan loan = (Loan)contract;
            if(Math.floor(contract.getCreationTime()) == simulationTimeNow) continue;
            result += loan.getNextInstallmentPayment();
         }
         return result;
      }
      
      /* Compute the loan demand at a given interest rate. In this implementation,
       * firms are blocked from the loan market if it is the case that they are in
       * a preexisting state of default induced pending liquidation bankruptcy. */
      private double computeCreditDemand(double interestRate) {
          if(isBankrupt()) 
             return 0.;
          
          double
              loanToRequestAtThisInterestRate =
                 depositsAndLoans.getCreditDemandFunction().getCreditDemandResponse(
                    getMaximumLoanRequest(),
                    goodsAndProduction.getMarkUp(),
                    interestRate
                    );
          
          return Math.max(loanToRequestAtThisInterestRate, 0.);
      }
      
      // Compute the total dividend to pay.
      private double computeTotalDividendPayment() {
         
         double anticipatedDueRepayments = 0.;
         for(Loan loan : getLiabilitiesLoans())
            anticipatedDueRepayments += loan.getNextInstallmentPayment();
         
         double
            maximumDividend = 
               Math.max(
                  cashLedger.getUnallocated() - 
                     (depositsAndLoans.getLiquidityTarget() + anticipatedDueRepayments),
                  0. // depositsAndLoans.getProfitsToDateThisCycle() - anticipatedDueRepayments
                  ),
            dividendToPay = maximumDividend;
         
         dividendToPay = Math.max(dividendToPay, 0.);
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "dividends: cash less liabilities in excess of liquidity target: " + maximumDividend);
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "dividends: liquidity target: " + depositsAndLoans.getLiquidityTarget());
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "dividends: goods markup rate: " + getMarkUp());
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "dividends: profit measure: " + depositsAndLoans.getProfitsToDateThisCycle());
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "dividends: dividends payable: " + dividendToPay);
         
         this.recordCashTransaction(
            dividendToPay, CashEventType.DIVIDEND_SUM_TO_PAY);
         return dividendToPay;
      }
      
      // Accounting and bookkeeping at the end of the business cycle.
      private void closeOfCycle() {
         
         absoluteLiquidityTarget.computeNext(getState());
         
         computeTotalDividendPayment();
         
         if(getNumberOfEmittedShares() <= 0.)
            setDividendPerShare(0.);                    // No shares exist.
         else
            setDividendPerShare(getDividendsToPayThisCycle()/getNumberOfEmittedShares());
         
//REPORTS         // Reports
//REPORTS         String[] depositsAndLoansReport = toString().split("\n");
//REPORTS         for(String note : depositsAndLoansReport)
//REPORTS            reportWriter.println(MacroFirm.this, note);
      }
      
      /* Ration demands for goods and labour based on the liquidity the firm 
       * can actually use for production at this time. Ordinarily this method 
       * will be called after new loans have been deposited. Note that it is 
       * assumed that the firm production function has return to scale, and so
       * that a linear rationing of both goods demands and labour will result
       * in the same linear ration applied to the goods yield from production.
       */
      private void rationInputDemandsAfterCredit()
      {
         double
            loanRepaymentsDue,
            factorToRationInputs = 1.;
         
         if(isBankrupt())
            loanRepaymentsDue = 0.; // Loan is not to be (directly) repaid.
         else 
            loanRepaymentsDue = getLoanLiabilitiesDueNotIncludingNewLoans();
         
         final double
            liquidityNeededForIdealProduction = 
               depositsAndLoans.getLiquidityRequiredForCurrentProductionCycle(),
            unreservedCashNow = cashLedger.getUnallocated(),
            cashAvailableForProduction = 
               (unreservedCashNow - loanRepaymentsDue);
         
         if(cashAvailableForProduction < liquidityNeededForIdealProduction) {
            if(cashAvailableForProduction <= 0.)
               factorToRationInputs = 0.;
            else if(liquidityNeededForIdealProduction == 0.)
               factorToRationInputs = 1.;
            else
               factorToRationInputs =
                  cashAvailableForProduction / liquidityNeededForIdealProduction;
            factorToRationInputs = Math.min(factorToRationInputs, 1.);
            factorToRationInputs = Math.max(factorToRationInputs, 0.);
            
            // Proportionally reduce labour.
            labourAndWage.rationDesiredLabourByMultiplier(factorToRationInputs);
              
            // Proportionally reduce input goods demand.
            goodsAndProduction.rationAllGoodsDemandsByMultiplier(factorToRationInputs);
         }
         
//REPORTS         // Reports
//REPORTS         reportWriter.println(MacroFirm.this,
//REPORTS            "rationing: inputs demand ration factor: " + 
//REPORTS             String.format("%7.2g%%", factorToRationInputs * 100) + ".");
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "rationing: new labour demand: " + labourAndWage.getLabourDesiredForProduction());
//REPORTS         for(int goodsType = 0; goodsType < 
//REPORTS            goodsAndProduction.getNumberOfSectors(); ++goodsType) {
//REPORTS            reportWriter.println(MacroFirm.this,
//REPORTS               "rationing: new demand for type " + goodsType + ": " +
//REPORTS               goodsInputDemands.get(goodsType) + ".");
//REPORTS         }
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "rationing: liquidity now: " + getUnreservedCash() + ".");
      }
      
      /** Get the liquidity required to meet target production now (note that production
       *  may have already occurred, and yielded fewer goods than the target).
       */
      double getLiquidityRequiredForCurrentProductionCycle() {
         return liquidityRequiredForCurrentProductionCycle;
      }
      
      /** Get the ideal liquidity that was required to meet target production in the 
       *  last cycle.
       */
      private double getLiquidityRequiredForLastProductionCycle() {
         return liquidityRequiredForLastProductionCycle;
      }
      
      /** Get the current cash profit measurement in this business cycle. */
      private double getProfitsToDateThisCycle() {
         return profitsToDateThisCycle;
      }
      
      /** Get the pending dividend payment. */
      private double getDividendsToPayThisCycle() {
         return dividendsToPayThisCycle;
      }
      
      /** Get total revenue from goods sales in this cycle. */
      private double getRevenueFromGoodsSalesThisCycle() {
         return revenueFromGoodsSalesThisCycle;
      }
            
      /** Get the goods acquisition cost incurred so far in this business cycle. */
      private double getGoodsAquisitionExpensesThisCycle() {
         return goodsAquisitionExpensesThisCycle;
      }
      
      /** Get the labour acquisition cost incurred so far in this business cycle. */
      private double getLabourAquisitionExpensesThisCycle() {
         return labourAquisitionExpensesThisCycle;
      }
      
      /** Get the (unreserved) cash deposit position at the beginning of this cycle. */
      private double getDepositsAtBeginningOfCycle() {
         return depositsAtBeginningOfCycle;
      }
      
      /** Get total loan repayments made in this cycle. */
      private double getLoanRepaymentsMadeThisCycle() {
         return loanRepaymentsMadeThisCycle;
      }
      
      /** Get the cash loan volume acquired in this cycle. */
      private double getLoanCashAquiredThisCycle() {
         return loanCashAquiredThisCycle;
      }
      
      /** Get the cash change due to bankruptcy intervention in this business cycle. */
      private double getBankruptcyInterventionThisCycle() {
         return bankruptcyInterventionThisCycle;
      }
      
      /** Get the firm credit demand function. */
      private CreditDemandFunction getCreditDemandFunction() {
         return creditDemandFunction;
      }
      
      /** Get the firm liquidity target. */
      private double getLiquidityTarget() {
         return absoluteLiquidityTarget.getLastValue(); 
      }
      
      /**
       * Returns a brief description of this object. The exact details of the
       * string are subject to change.
       */
      @Override
      public String toString() {
         String result =
            "liquidity required for current production cycle:" + 
               getLiquidityRequiredForCurrentProductionCycle() + ".\n" +
            "profits to date this cycle:" + getProfitsToDateThisCycle() + ".\n" +
            "outgoing dividend payments expected now: " + getDividendsToPayThisCycle() + ".\n" +
            "total loan liabilities now as proportion of equity: " + 
               String.format("%10.5g%%", 1.e2 * getTotalLoanLiabilityValue() / getEquity()) + ".\n" +
            "after expected dividend payments: " + 
               String.format("%10.5g%%", 1.e2 * getTotalLoanLiabilityValue() / 
                  (getEquity() - getDividendsToPayThisCycle())) + ".\n" +
            "revenue from goods sales this cycle: " + getRevenueFromGoodsSalesThisCycle() + ".\n" +
            "goods acquisition expenses this cycle: " +
               getGoodsAquisitionExpensesThisCycle() + ".\n" +
            "get labour expenses this cycle: " + getLabourAquisitionExpensesThisCycle() + ".\n" +
            "cash deposits now: " + getDepositValue() + ".\n" +
            "percent of cash is reserved: " + 
               String.format("%10.5g%%", 1.e2 * getAllocatedCash()/getDepositValue()) + ".\n" +
            "deposits at beginning of cycle: " + getDepositsAtBeginningOfCycle() + ".\n" +
            "loan repayments made this cycle: " + getLoanRepaymentsMadeThisCycle() + ".\n" +
            "loan cash acquired this cycle: " + getLoanCashAquiredThisCycle() + ".\n" +
            "cash flows to due bankruptcy intervention this cycle: "
               + getBankruptcyInterventionThisCycle() + ".\n" +
            "liquidity target now: " + getLiquidityTarget() + ".\n" +
            "loan liabilities now: " + getTotalLoanLiabilityValue() + "." + 
            "active loans: \n";
         {
         int LoanIndex = 1;
         for(Loan loan : getLiabilitiesLoans()) {
            result += String.format("%3d interest rate: %10.6g principal value: %+16.10g.\n", 
                LoanIndex, loan.getInterestRate(), loan.getLoanPrincipalValue());
            ++LoanIndex;
         }
         }
         return result;
      }
   }
   
   final DepositsAndLoans depositsAndLoans;
   
   /**
    *  Local encapsulation for labour (employment) and wage behaviour. When using this 
    *  inner class locally, use getters and mutator methods (rather than direct
    *  field access) to the extent that this is possible.
    *  
    *  This class provides access to the labour exchange market, active labour orders,
    *  current employment and wage selection algorithms.
    */
   final class LabourAndWage {
      
      private SimpleLabourMarket 
         labourMarket;                                      // Labour Market
      
      private FirmDecisionRule
         labourWageSelectionAlgorithm;                      // Labour wage pricing algorithm
      
      private Bag
         activeLabourOrders,
         labourAcquired;
      
      private double
         labourDesiredForProduction;
      
      private LabourAndWage(
         SimpleLabourMarket labourMarket,
         FirmDecisionRule labourWageSelectionAlgorithm
         ) {
         StateVerifier.checkNotNull(labourMarket, labourWageSelectionAlgorithm);
         this.labourMarket = labourMarket;
         this.labourWageSelectionAlgorithm = labourWageSelectionAlgorithm;
         
         activeLabourOrders = new Bag();
         labourAcquired = new Bag();
      }
      
      private void flushMemories() {
         labourWageSelectionAlgorithm.computeNext(getState());
         labourDesiredForProduction = 0.;
         if(!labourAcquired.isEmpty())
            System.err.println();
         
//REPORTS         //REPORTS Reports
//REPORTS         reportWriter.println(MacroFirm.this, 
//REPORTS            "wage: next wage proposition: " + labourWageSelectionAlgorithm.getLastValue());
      }
      
      // Accounting and bookkeeping at the end of the business cycle.
      private void closeOfCycle() {
//REPORTS         //REPORTS Reports
//REPORTS         String[] labourAndWageReport = toString().split("\n");
//REPORTS         for(String note : labourAndWageReport)
//REPORTS            reportWriter.println(MacroFirm.this, note);
      }
      
      // Submit market orders for labour.
      private void hireFireWorkers() {
         final double
            labourDemand = labourDesiredForProduction - getLabourEmployedNow(),
            labourWage = getMostRecentWageAsk();
         final int
            labourContractMaturity = 1;
         final String errMsg = 
            "A firm was unable to submit labour order to the market; details follow. " + 
            "Amount of labour desired: " + labourDemand + ", labour unit wage ask: " +
            labourWage + ", labour contract maturity: " + labourContractMaturity + ".";
         if (labourDemand > 0.){
            try {
               labourMarket.addOrder(
                  MacroFirm.this,
                  labourContractMaturity,
                  -labourDemand,                                        // Negative is Buyer.
                  labourWage
                  );
            }
            catch (AllocationException e) {                             // Failed
               System.err.println(errMsg + e);
            }
            catch (OrderException e) {                                  // Failed
               System.err.println(errMsg + e);
            }
            
//REPORTS            //REPORTS Reports
//REPORTS            reportWriter.println(MacroFirm.this, 
//REPORTS               "ordering " + labourDemand + " units of labour at unit price " + labourWage
//REPORTS               + ". Total anticipated wage cost: " + labourWage * labourDemand + "."
//REPORTS               );
         }
         else
            System.err.println("MacroFirm.labour: [warning] zero demand for labour by this firm.");
      }
      
      /** Remove a labour object. */
      public void addLabourInstance(Labour labour) { 
         labourAcquired.add(labour); 
      }
      
      /** Install a labour object. */
      public boolean removeLabourInstance(Labour labour) {
         return labourAcquired.remove(labour);
      }
      
      /** Get the most recent labour unit wage. */
      public double getMostRecentWageAsk() {
         return labourWageSelectionAlgorithm.getLastValue();
      }
      
      /** Get the amount of labour employed now. */
      public double getLabourEmployedNow() {
         double totalLabourAcquired = 0;
         for(int i = 0; i< labourAcquired.size(); ++i) {
            Labour labour = (Labour)labourAcquired.get(i);
            totalLabourAcquired += labour.getQuantity();
         }
         return totalLabourAcquired;
      }
      
      /** Get the labour desired for production. Note that some or all
       *  of this quantity may have been obtained. */
      public double getLabourDesiredForProduction() {
         return labourDesiredForProduction;
      }
      
      /** Set the amount of labour desired for production. */
      public void setLabourDesiredForProduction(double labourAmount) {
         if(labourAmount < 0.)
            System.err.println(
               "MacroFirm.labour: desired labour for production (value " + 
               labourAmount  + ") is negative. The value has been trimmed to zero."
               );
         labourAmount = Math.max(labourAmount, 0.);
         labourDesiredForProduction = labourAmount;
      }
      
      /** Remove a labour order from the market. */
      private boolean removeLabourOrder(SimpleLabourMarketOrder order) {
         return activeLabourOrders.remove(order);
      }
      
      /** Cancel all outstanding market labour orders. */
      public void cancelLabourOrders() {
         Bag labourOrdersCopy = new Bag();
         for(Object obj : activeLabourOrders)
            labourOrdersCopy.add(obj);
         for(Object obj : activeLabourOrders) {
            SimpleLabourMarketOrder order = (SimpleLabourMarketOrder)obj;
            order.cancel();
         }
         if(activeLabourOrders.size() > 0)                              // Unexpected
            activeLabourOrders.clear();
      }
      
      // Ration labour demand by a constant multiplier.
      public void rationDesiredLabourByMultiplier(double multiplier) {
         if(multiplier < 0.)
            System.err.println(
               "MacroFirm.ration: labour rationing multiplier (value " + 
               multiplier + ") is negative. The value has been trimmed to zero.");
         multiplier = Math.max(multiplier, 0.);
         labourDesiredForProduction *= multiplier;
      }
      
      /**
       * Returns a brief description of this object. The exact details of the
       * string are subject to change.
       */
      @Override
      public String toString() {
         return
            "most recent wage ask unit price:" + 
               getMostRecentWageAsk() + ".\n" +
            "total labour employed now:" + getLabourEmployedNow() + ".\n" +
            "target labour desired for production:" + getLabourDesiredForProduction() + ".\n";
      }
   }
   
   final LabourAndWage labourAndWage;
   
//REPORTS   /** A detailed reports generator for macro economy activities. */
//REPORTS   private final static class MacroReportGenerator {
//REPORTS      private String reportFilename;
//REPORTS      private PrintWriter reportWriter;
//REPORTS      private boolean isEnabled;
//REPORTS      private int enableAtTime, disableAtTime;
//REPORTS      
//REPORTS      MacroReportGenerator(
//REPORTS         String outputFilename, int enableAtTime, int disableAtTime) {
//REPORTS         if (outputFilename == null)
//REPORTS            throw new NullArgumentException();
//REPORTS         if (enableAtTime < 0 || disableAtTime < 0
//REPORTS               || enableAtTime > disableAtTime)
//REPORTS            throw new IllegalArgumentException(
//REPORTS                  "ReportWriter: reporting window is invalid: t = ["
//REPORTS                        + enableAtTime + ", " + disableAtTime + "]");
//REPORTS         this.reportFilename = outputFilename;
//REPORTS         this.reportWriter = null;
//REPORTS         this.isEnabled = false;
//REPORTS         this.enableAtTime = enableAtTime;
//REPORTS         this.disableAtTime = disableAtTime;
//REPORTS      }
//REPORTS      
//REPORTS      /**
//REPORTS       * Print to the reporting stream, if (a) enabled and (b) the simulation is
//REPORTS       * in the reporting time window.
//REPORTS       * */
//REPORTS      void println(MacroFirm firm, String text) {
//REPORTS         if (isEnabled) {
//REPORTS            double simulationTimeNow = 
//REPORTS               Simulation.getSimState().schedule.getTime();
//REPORTS            if (simulationTimeNow >= enableAtTime
//REPORTS                  && simulationTimeNow <= disableAtTime)
//REPORTS               reportWriter.println(firm.getFirmReportPrefix() + text);
//REPORTS            else if (simulationTimeNow > disableAtTime)
//REPORTS               this.disable();
//REPORTS         }
//REPORTS      }
//REPORTS      
//REPORTS      /** Try to enable the reporting stream. */
//REPORTS      @SuppressWarnings("unused") //REPORTS Manually enabled.
//REPORTS      private void enable() {
//REPORTS         if (isEnabled)
//REPORTS            throw new IllegalStateException(
//REPORTS               "ReportWriter.enable: error: report writer is already enabled.");
//REPORTS         try {
//REPORTS            reportWriter = new PrintWriter(new File(reportFilename));
//REPORTS            isEnabled = true;
//REPORTS         } catch (FileNotFoundException fatalError) {
//REPORTS            fatalError.printStackTrace();
//REPORTS            Simulation.getSimState().kill();
//REPORTS            System.exit(1);
//REPORTS         }
//REPORTS      }
//REPORTS      
//REPORTS      /** Disable the reporting stream. */
//REPORTS      private void disable() {
//REPORTS         if (isEnabled) {
//REPORTS            reportWriter.flush();
//REPORTS            reportWriter.close();
//REPORTS         }
//REPORTS         isEnabled = false;
//REPORTS      }
//REPORTS   }
//REPORTS   
//REPORTS   private static MacroReportGenerator reportWriter;
//REPORTS   private static final int REPORT_START_TIME = 0, REPORT_END_TIME = 20;
//REPORTS   
//REPORTS   static {
//REPORTS      reportWriter = new MacroReportGenerator(
//REPORTS         "./FirmReports.log", REPORT_START_TIME, REPORT_END_TIME);
//REPORTS      //REPORTS reportWriter.enable();
//REPORTS   }
   
   /** 
     * Create a {@link MacroFirm} agent.
     * 
     * @param depositHolder
     *        The {@link DepositHolder} who manages the {@link Firm} deposit account.
     * @param initialDeposit
     *        The initial cash endowment.
     * @param stockHolder
     *        The {@link StockHolder} who, initially, owns shares in this {@link Firm}.
     * @param numberOfShares
     *        The number of shares to emit.
     * @param emissionPrice
     *        The initial price per share (PPS) for shares in this {@link Firm}
     * @param sectorName
     *        The productive sector to which this {@link Firm} belongs (the type of 
     *        goods this {@link Firm} is to produce).
     * @param numberOfSectors
     *        The number of sectors/number of differentiated goods types in this
     *        simulation.
     * @param initialOwnedGoods
     *        The quantity of goods initially owned by the {@link Firm}
     * @param goodsMarket
     *        The market over which the {@link Firm} buys and sells goods.
     * @param labourMarket
     *        The market over which the {@link Firm} bids for labour.
     * @param targetProductionAlgorithm
     *        The {@link FirmTargetProductionAlgorithm}, which decides the number of 
     *        goods the {@link Firm} should aim to produce.
     * @param goodsPricingAlgorithm
     *        The {@link FirmDecisionRule} that decides the price (per unit) of new
     *        production goods.
     * @param labourWageSelectionAlgorithm
     *        The {@link FirmDecisionRule} that decides the bid price per unit of 
     *        labour to employ (wage per unit labour).
     * @param goodsProductionFunction
     *        The {@link FirmProductionFunction} that governs the conversion of
     *        labour and input goods into new production goods. Examples of the 
     *        {@link FirmProductionFunction} include Cobb-Doubles and Leontieff.
     * @param creditDemandFunction
     *        The {@link Firm} {@link CreditDemandFunction}, which specifies the
     *        {@link Firm} loan demand as a funtion of commercial loan interest
     *        rates.
     * @param liquidityTarget
     *        The {@link FirmDecisionRule} that specifies the amount of liquidity
     *        (cash) the {@link Firm} aims to retain at the end of the business
     *        cycle. The remaining liquidity is issued in dividends.
     * @param bankruptcyHandler
     *        The {@link Firm} bankruptcy handler.
     * @param name
     *        A unique {@link String} name for this {@link Agent}.
     */
   @AssistedInject
   public MacroFirm(
   @Assisted
      DepositHolder depositHolder,
   @Named("FIRM_INITIAL_CASH_ENDOWMENT")
      double initialDeposit,
   @Assisted
      StockHolder stockHolder,
   @Assisted
      double numberOfShares,
   @Named("FIRM_INITIAL_PRICE_PER_SHARE")
      double emissionPrice,
   @Named("FIRM_SECTOR_NAME")
      String sectorName,
   @Named("FIRM_INITIAL_OWNED_GOODS")
      double initialOwnedGoods,
      SimpleGoodsMarket goodsMarket,
      SimpleLabourMarket labourMarket,
   @Named("FIRM_TARGET_PRODUCTION_RULE")
      FirmDecisionRule targetProductionAlgorithm,
   @Named("FIRM_PRODUCTION_GOODS_PRICING_RULE")
      FirmDecisionRule goodsPricingAlgorithm,
   @Named("FIRM_LABOUR_WAGE_BID_RULE")
      FirmDecisionRule labourWageSelectionAlgorithm,
   @Named("FIRM_PRODUCTION_FUNCTION")
      FirmProductionFunctionFactory goodsProductionFunctionFactory,
   @Named("FIRM_CREDIT_DEMAND_FUNCTION")
      CreditDemandFunction creditDemandFunction,
   @Named("FIRM_LIQUIDITY_TARGET_RULE_FUNCTION")
      FirmDecisionRule liquidityTarget,
   @Named("FIRM_BANKRUPTCY_HANDLER")
      FirmBankruptcyHandler bankruptcyHandler,
   @Named("FIRM_NAME_GENERATOR")
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator
            );
      
      StateVerifier.checkNotNull(bankruptcyHandler, sectorName);
      
      // Goods and production
      this.goodsAndProduction = new GoodsAndProduction(
         goodsProductionFunctionFactory.create(sectorName),
         targetProductionAlgorithm,
         goodsPricingAlgorithm,
         goodsMarket,
         initialOwnedGoods,
         sectorName
         );
      
      // Deposits and loans
      this.depositsAndLoans = new DepositsAndLoans(
         creditDemandFunction, 
         liquidityTarget
         );
      
      // Labour and wage
      this.labourAndWage = new LabourAndWage(
         labourMarket, 
         labourWageSelectionAlgorithm
         );
      
      this.bankruptcyHandler = bankruptcyHandler;
      
      // Scheduled methods
      Simulation.repeat(this, "flushMemories",
         CustomSimulationCycleOrdering.create(NamedEventOrderings.BEFORE_ALL, 2));
      Simulation.repeat(this, "submitMarketOrdersForProduction", NamedEventOrderings.FIRM_BUY_INPUTS);
      Simulation.repeat(this, "accounting", NamedEventOrderings.FIRM_ACCOUNTING);
   }
   
//REPORTS   /** Get a text prefix for report notes specific to this firm. */
//REPORTS   private String getFirmReportPrefix() {
//REPORTS      return
//REPORTS         String.format("[Time: %5.2g] ", Simulation.getSimState().schedule.getTime()) + 
//REPORTS         String.format(
//REPORTS            "[MacroFirm type %4d, c: $% 9.5g, a: $% 9.5g, " + 
//REPORTS            "l: $% 9.5g, eq: $% 9.5g] ",
//REPORTS            getGoodsType(), 
//REPORTS            getDepositValue(),
//REPORTS            getTotalAssets(),
//REPORTS            getTotalLiabilities(),
//REPORTS            getEquity()
//REPORTS            );
//REPORTS    }
    
    /** Get the minimal interest rate (below which this firm expects to borrow its maximum
     *  principal loan sum without hesitation). */
    public double getRmin() { return 5.e-4; }
    
    /** Get the maximum loan amount the firm anticipates at this time. */
    public double getMaximumLoanRequest() {
        return depositsAndLoans.getMaximumLoanRequest();
    }
    
    /** Get the principal loan value this firm would currently request at the given 
     *  interest rate. */
    private double computeLoanDemand(double interestRate) {
       return depositsAndLoans.computeCreditDemand(interestRate);
    }
    
    /**
      * Clearing market response function for Macro Firms.
      * This agent will respond only to markets clearing exactly
      * one resource of type "Commercial Loan".
      */
    @Override
    public MarketResponseFunction getMarketResponseFunction(
       final ClearingMarketInformation caller
       ) {
       if(caller == null) return null;
       if(caller.getMarketType() == ClearingSimpleCommercialLoanMarket.class ||
          caller.getMarketType() == ClearingGiltsBondsAndCommercialLoansMarket.class) {
          if(computeLoanDemand(0.) <= 0.)
             return null;                                        // No commercial loan requirement.
          if(getMarkUp() < 0.)
             return null;                                        // Poor user price selection.
          return new PartitionedResponseFunction(
             new InverseExpIOCPartitionFunction(1./getMarkUp()),
             new BoundedUnivariateFunction() {
               @Override
               public double value(double interestRate) {
                  return computeLoanDemand(interestRate);
               }
               @Override
               public double getMaximumInDomain() {
                  return getMarkUp();
               }
               @Override
               public double getMinimumInDomain() {
                  return 0;
               }
             }
          );
       }
       return null;
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void flushMemories() {
       labourAndWage.flushMemories();
       depositsAndLoans.flushMemories();
       goodsAndProduction.flushMemories();
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void submitMarketOrdersForProduction() {
       depositsAndLoans.rationInputDemandsAfterCredit();
       labourAndWage.hireFireWorkers();
       goodsAndProduction.submitMarketOrdersToBuyGoods();
    }
    
    @Override
    protected final void considerProduction() { 
        goodsAndProduction.considerProduction();
    }
    
    /*
     *  Trade registration. Firms have be ability to react to incoming and
     *  exported goods quantities, incoming employment, establishment of 
     *  new loans, loan repayment installments and dividend emission. Typically
     *  these registration methods will be called immediately after a
     *  settlement has been executed.
     */
    
    /** React, as appropriate, to an incoming volume of goods. */
    @Override
    public void registerIncomingTradeVolume(
       final String goodsType,
       final double goodsAmount,
       final double unitPricePaid
       ) {
       goodsAndProduction.registerIncomingTradeVolume(
          goodsType, goodsAmount, unitPricePaid);
       
//REPORTS       //REPORTS Reports
//REPORTS       reportWriter.println(
//REPORTS          this, "recieved " + goodsAmount + " units of goods type " + goodsType + ".");
    }
    
    /** React, as appropriate, to an outgoing volume of goods. */
    @Override
    public void registerOutgoingTradeVolume(
       final String goodsType,
       final double goodsAmount, 
       final double unitPricePaid
       ) {
       goodsAndProduction.registerOutgoingTradeVolume(
          goodsType, goodsAmount, unitPricePaid);
    }
    
    /** React, as appropriate, to an incoming labour supply. */
    @Override
    public void registerIncomingEmployment(
        double labourEmployed, double unitLabourWage) {
        // labourAndWage.recordIncomingLabour(labourEmployed);
        depositsAndLoans.recordCashTransaction(
           labourEmployed * unitLabourWage, CashEventType.LABOUR_ACQUISITION_COST);
        
//REPORTS        // Reports
//REPORTS        reportWriter.println(this, "recieved " + labourEmployed + " units labour");
    }
    
    @Override
    public void registerNewLoanTransfer(
       double principalCashValue,
       Lender lender, 
       double interestRate) {
       depositsAndLoans.recordCashTransaction(
          principalCashValue, CashEventType.CASH_FROM_NEW_LOANS);
       
//REPORTS       // Reports
//REPORTS       reportWriter.println(this, "received new loan of size " + principalCashValue + ".");
    }
    
    @Override
    public void registerNewLoanRepaymentInstallment(
       double installmentCashValue,
       Lender lender,
       double interestRate) {
       depositsAndLoans.recordCashTransaction(
          installmentCashValue, CashEventType.LOAN_REPAYMENT);
       
//REPORTS       // Reports
//REPORTS       reportWriter.println(
//REPORTS          this, "repaid loan installment of value " + installmentCashValue + ".");
    }
    
    @Override
    public void registerOutgoingDividendPayment(
       double dividendPayment,
       double pricePerShare, 
       double numberOfSharesHeldByInvestor
       ) {
       depositsAndLoans.recordCashTransaction(
          dividendPayment, CashEventType.OUTGOING_DIVIDEND_PAYMENT);
       
       // Reports
       depositsAndLoans.historicalDividendTransactionRecords.add(
          new DividendTransactionRecord(
             dividendPayment,
             pricePerShare,
             numberOfSharesHeldByInvestor,
             depositsAndLoans.dividendsToPayThisCycle,
             getNumberOfEmittedShares()
             ));
       
//REPORTS       reportWriter.println(
//REPORTS          this, "paid dividends of value " + dividendPayment + 
//REPORTS          ", price per share " + pricePerShare + ", number of shares held by investor " + 
//REPORTS          numberOfSharesHeldByInvestor + ".");
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void accounting() {
       depositsAndLoans.closeOfCycle();
       goodsAndProduction.closeOfCycle();
       labourAndWage.closeOfCycle();
    }
    
    /** Cancel and goods orders from the market. */
    public void cancelGoodsOrders() {
       goodsAndProduction.cancelGoodsOrders();
    }
    
    /** Cancel all labour orders from the market. */
    public void cancelLabourOrders() {
       labourAndWage.cancelLabourOrders();
    }
    
    FirmDecisionRule.FirmState getState() {
       FirmDecisionRule.FirmState.Builder builder = 
          new FirmDecisionRule.FirmState.Builder();
       
       double
          anticipatedDueRepayments = 0.;
       for(Loan loan : getLiabilitiesLoans())
          anticipatedDueRepayments += loan.getNextInstallmentPayment();
       
       return builder
          .assetsTotalValue(getTotalAssets())
          .totalLiquidityDemandNow(
             depositsAndLoans.getLiquidityRequiredForCurrentProductionCycle())
          .totalLiquidityDemandLastCycle(
             depositsAndLoans.getLiquidityRequiredForLastProductionCycle())
          .labourCurrentlyEmployed(
             labourAndWage.getLabourEmployedNow())
          .labourEmploymentTarget(
             labourAndWage.getLabourDesiredForProduction())
          .totalUnsoldGoodsThisCycle(
             goodsAndProduction.getUnsoldProductionGoodsNow())
          .targetGoodsProductionNow(
             goodsAndProduction.getGoodsProductionTargetNow())
          .targetGoodsProductionLastCycle(
             goodsAndProduction.getGoodsProductionTargetLastCycle())
          .effectiveGoodsProduction(
             goodsAndProduction.getMostRecentGoodsYield())
          .effectiveGoodsProductionLastCycle(
             goodsAndProduction.actualGoodsProductionLastCycle)
          .currentGoodsUnitPrice(
             goodsAndProduction.getProductionGoodsUnitSellingPrice())
          .lastRecordedProfit(
             depositsAndLoans.getProfitsToDateThisCycle())
          .lastRecordedLabourWage(
             labourAndWage.getMostRecentWageAsk())
          .goodsType(
             goodsAndProduction.getGoodsType())
           .firmName(getUniqueName())
          .goodsMarket(
             goodsAndProduction.getGoodsMarket())
          .loanRepaymentLiability(anticipatedDueRepayments)
          .unallocatedCash(getUnallocatedCash())
          .build();
    }
    
    /** Get the quantity of production goods that this firm owns. Note
     *  that this quantity may be lower or higher than the most recent
     *  production yield. */
    public double getOwnedProductionGoodsQuantity() {
        return goodsAndProduction.getProductionGoodsStorage().getStoredQuantity();
    }
    
    /** Get the type of goods that this firm produces. */
    public String getGoodsType() {
       return goodsAndProduction.getGoodsType();
    }
    
    /** This operation is not supported in the current implementation. */
    @Override
    public void addOrder(Order order) {
       throw new UnsupportedOperationException(
             "MacroFirm.addOrder: goods and labour orders for the current macro implementation " +
             "use specific classes (SimpleGoodsMarketOrder, SimpleLabourMarketOrder). Write to " + 
             "Jakob for more details.");
    }
    
    /** This operation is not supported in the current implementation. */
    @Override
    public boolean removeOrder(Order order) {
       throw new UnsupportedOperationException(
             "MacroFirm.removeOrder: goods and labour orders for the current macro implementation " +
             "use specific classes (SimpleGoodsMarketOrder, SimpleLabourMarketOrder). Write to " + 
             "Jakob for more details.");
    }
    
    @Override
    public void updateState(Order order) { }
    
   /** Get the total cash deposits owned by the firm. */
   @Override
   public double getDepositValue() {
      return cashLedger.getStoredQuantity();
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return cashLedger.changeAllocatedBy(+positiveAmount);
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      return -cashLedger.changeAllocatedBy(-positiveAmount);
   }
   
   @Override
   public double getAllocatedCash() {
      return cashLedger.getAllocated();
   }
   
   @Override
   public double getUnallocatedCash() {
      return cashLedger.getUnallocated();
   }
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() { 
      throw new UnsupportedOperationException();
   }
   
    /** Add an instance of labour to this firm. */
    @Override
    public void addLabour(Labour labour) {
       this.getLabourForce();
       labourAndWage.addLabourInstance(labour);
    }
    
    /** Remove an instance of labour from this firm. */
    @Override
    public boolean removeLabour(Labour labour) {
       return labourAndWage.removeLabourInstance(labour);
    }
    
    /** Get the amount of labour employed by this firm now. */
    @Override
    public double getLabourForce() {
       return labourAndWage.getLabourEmployedNow();
    }
    
    @Override
    public void disallocateLabour(double size) { }
    
    
    public double getWorkForceUsed() {
        return labourAndWage.getLabourEmployedNow();
    }

    public double getMaxAskedLoan() {
        return getMaximumLoanRequest();
    }

    public double getEffectiveLoan() {
        return depositsAndLoans.getLoanCashAquiredThisCycle();
    }

    @Override
    public double getProduction() {
        return goodsAndProduction.getMostRecentGoodsYield();
    }
    
    /** Return a copy of the total goods quantities bought by the firm in this business cycle. */
    public Map<String, Double> getGoodsPurchased() {
        return goodsAndProduction.getGoodsObtainedInThisTimeStep();
    }
    
    /** Return a copy of an array containing the current unit selling price of market goods (one
      * record for each class of goods). */
    public Map<String, Double> getPriceOfGoodsPurchased() {
       return goodsAndProduction.getGoodsMarketPrices();
    }
    
    /** Get the (most recent) markup rate on production goods. Note that this number is
     *  in excess of the unit production cost, namely markup = selling price / unit production
     *  cost - 1. 
     **/
    public double getMarkUp() {
       return goodsAndProduction.getMarkUp();
    }
    
    public double getProfit() {
        return depositsAndLoans.getProfitsToDateThisCycle();
    }
    
    public double getTotalDividend() {
        return depositsAndLoans.getDividendsToPayThisCycle();
    }
    
    @Override
    public double getGoodsSellingPrice() {
        return goodsAndProduction.getProductionGoodsUnitSellingPrice();
    }
    
    @Override
    public double getRevenue() {
        return depositsAndLoans.getRevenueFromGoodsSalesThisCycle();
    }
    
    /** Remove a goods market order. Typically this method is called from within
     *  a goods market order object when that order itself is cancelled. */
    public boolean removeOrder(SimpleGoodsMarketOrder order) {
        return goodsAndProduction.removeGoodsOrder(order);
    }
    
    /** Remove a labour market order. Typically this method is called from within 
     *  a labour market order object when that order itself is cancelled. */
    public boolean removeOrder(SimpleLabourMarketOrder order) {
        return labourAndWage.removeLabourOrder(order);
    }
    
    /** This method is not implemented in the current version. An implementation of 
     *  this method exists in a previous version, but is seldom used. */
    @Override
    protected void considerCommercialLoanMarkets() { }
    
    /** Get the goods repository (warehouse) for this firm. */
    @Override
    public DurableGoodsRepository getGoodsRepository() {
        return goodsAndProduction.getGoodsRepository();
    }
    
    @Override
    protected void considerDepositPayment() { }
    
    @Override
    public double getAskedLoan() {
        return depositsAndLoans.getLoanCashAquiredThisCycle();
    }
    
    /** Request a bankruptcy resolution. */
    @Override
    public void handleBankruptcy() {
       bankruptcyHandler.initializeBankruptcyProcedure(this); 
    }
    
    /** Get the current firm liquidity target. */
    public double getLiquidityTarget() {
        return depositsAndLoans.getLiquidityTarget();
    }
    
    /** Get the goods class type (sector index) for this firm. */
    public String getGoodType(){
       return goodsAndProduction.getGoodsType();
    }
    
    @Override
    public double getUnsold() {
       return goodsAndProduction.getUnsoldProductionGoodsLastCycle();
    }
    
    @Override
    public double getEmployment() {
       return getWorkForceUsed();
    }
}