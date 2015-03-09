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
package eu.crisis_economics.abm.model.plumbing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.FirmFactory;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.fund.ExogenousDepositHolder;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;
import eu.crisis_economics.abm.fund.FundInvestor;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.household.DomesticGoodsConsumer;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.ratings.AgentDividendPerShareMeasurement;
import eu.crisis_economics.abm.ratings.AgentEquityMeasurement;
import eu.crisis_economics.abm.ratings.AgentStockPricePerShareMeasurement;
import eu.crisis_economics.abm.ratings.RatingAgency;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.AbstractCollectionProvider;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.EmptyCollectionProvider;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingLoanMarketFactory;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;
import eu.crisis_economics.abm.simulation.injection.factories.HouseholdFactory;
import eu.crisis_economics.utilities.ArrayUtil;
import eu.crisis_economics.utilities.InjectionUtils;

public class FinancialSystemWithIOFirms implements Plumbing {   // Stateless
   
   private static final boolean
      VERBOSE_MODE = true;
   
   @Override
   public BaseAgentGroup populate(final Injector injector) {
      BaseAgentGroup
         result = new BaseAgentGroup();
      
      final ClearingHouse
         clearingHouse = injector.getInstance(ClearingHouse.class);
      
      {
      final SimpleGoodsMarket
         goodsMarket = injector.getInstance(SimpleGoodsMarket.class);
      
      if(VERBOSE_MODE)
         System.out.printf("goods market: [%s]\n", goodsMarket.getClass().getSimpleName());
      
      result.addMarketStructure(goodsMarket);
      
      result.addMarketStructure(injector.getInstance(SimpleLabourMarket.class));
      
      result.addMarketStructure(clearingHouse);
      }
      
      // Loan markets
      
      clearingHouse.addMarket(
         injector.getInstance(ClearingLoanMarketFactory.class).create(
            "Clearing Loan Market", clearingHouse));
      
      // Providers for bank investments options
      
      final boolean
         doEnableCommercialBankStockMarketParticipation = 
            InjectionUtils.getParameter(
               injector, Boolean.class,
               "DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION");
      final boolean
         doEnableStockTrading = 
            InjectionUtils.getParameter(
               injector, Boolean.class, "ENABLE_STOCK_TRADING");
      final boolean
         doBanksBuyBankStocks = 
            InjectionUtils.getParameter(
               injector, Boolean.class, "DO_BANKS_BUY_BANK_STOCKS");
      
      if(VERBOSE_MODE)
         System.out.printf("bank stock market participation: %s\n",
            doEnableCommercialBankStockMarketParticipation ? "enabled" : "disabled");
      
      // Bank markets to consider
      
      final CollectionProvider<ClearingStockMarket>
         bankStocksToConsider;
      if(doEnableStockTrading && doEnableCommercialBankStockMarketParticipation)
         bankStocksToConsider = new AbstractCollectionProvider<ClearingStockMarket>() {
            @Override
            public Collection<ClearingStockMarket> get() {
               if(doBanksBuyBankStocks)
                  return clearingHouse.getMarketsOfType(ClearingStockMarket.class);
               else {
                  final List<ClearingStockMarket>
                     allStocks = clearingHouse.getMarketsOfType(ClearingStockMarket.class),
                     stocksToConsider = new ArrayList<ClearingStockMarket>();
                  for(final ClearingStockMarket market : allStocks)
                     if(!market.getStockReleaserName().contains("Bank"))
                        stocksToConsider.add(market);
                  return stocksToConsider;
               }
            }
         };
      else
         bankStocksToConsider = new EmptyCollectionProvider<ClearingStockMarket>();
      final CollectionProvider<ClearingLoanMarket>
         bankLoansToConsider = new AbstractCollectionProvider<ClearingLoanMarket>() {
            @Override
            public Collection<ClearingLoanMarket> get() {
               return clearingHouse.getMarketsOfType(ClearingLoanMarket.class);
            }
         };
      
      /*
       * Fundamentalist-Chartist Banks
       */
      {
      final int
         numberOfFundamentalistBanks = InjectionUtils.getParameter(
            injector, Integer.class, "NUMBER_OF_COMMERCIAL_BANKS");
      
      for(int i = 0; i< numberOfFundamentalistBanks; ++i) {
         final CommercialBank
            bank = injector.getInstance(CommercialBank.class);
         
         if(VERBOSE_MODE)
            System.out.printf(
               "adding fundamentalist-chartist bank %d/%d: %s [%s]\n",
               (i + 1), numberOfFundamentalistBanks,
               bank.getUniqueName(), bank.getClass().getSimpleName()
               );
         
         bank.getStrategy().setStocksToInvestIn(bankStocksToConsider);
         bank.getStrategy().setLoansToInvestIn(bankLoansToConsider);
         result.addAgent(bank);
         
         if(doEnableStockTrading)
            clearingHouse.addMarket(new ClearingStockMarket(bank, clearingHouse));
      }
      }
      
      // Firms
      
      final int
         numberOfFirms = InjectionUtils.getParameter(
            injector, Integer.class, "NUMBER_OF_FIRMS");
      {
      final List<CommercialBank>
         banks = result.getAgentsOfType(CommercialBank.class);
      final int
         numBanks = banks.size();
      final double
         firmNumberOfSharesToEmit = InjectionUtils.getParameter(
            injector, Double.class, "FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED");
      
      final boolean
         doHoldFirmDepositsExogenously = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY");
      
      for(int i = 0; i< numberOfFirms; ++i) {
         
         final DepositHolder
            depositHolder = doHoldFirmDepositsExogenously ? 
               injector.getInstance(ExogenousDepositHolder.class) :
                  (DepositHolder) (banks.get(i % numBanks));
         final
            StockHolder stockHolder = (StockHolder) banks.get(i % numBanks);
         
         final LoanStrategyFirm firm =
            injector.getInstance(FirmFactory.class).create(
               depositHolder, stockHolder, 0.);
         
         final double
            firmPricePerShare = firm.getMarketValue();
         
         if(VERBOSE_MODE)
            System.out.printf(
               "adding firm %d/%d: %s [%s] share emission: %g\n",
               (i + 1), numberOfFirms, 
               firm.getUniqueName(), firm.getClass().getSimpleName(),
               firmPricePerShare
               );
         
         final List<StockHolder>
            stockHolders =
               doEnableCommercialBankStockMarketParticipation ?
               ArrayUtil.<StockHolder, CommercialBank>castList(
                  result.getAgentsOfType(CommercialBank.class)) :
               ArrayUtil.<StockHolder, Fund>castList(
                  result.getAgentsOfType(Fund.class));
         
         for(int j = 0; j< stockHolders.size(); ++j) {
            try {
               firm.releaseShares(
                  firmNumberOfSharesToEmit / stockHolders.size(), 0., stockHolders.get(j));
            } catch (final InsufficientFundsException neverThrows) {
               System.err.println(
                   "Model initialization has failed while emitting firm shares. This behaviour is "
                 + "not expected in the current implementation. Details follow: "
                   );
               System.err.println(neverThrows);
               System.err.flush();
            }
         }
         firm.debit(firmNumberOfSharesToEmit * firmPricePerShare);
         
         if(doEnableStockTrading)
            clearingHouse.addMarket(new ClearingStockMarket(firm, clearingHouse));
         
         result.addAgent(firm);
      }
      }
      
      { // Agencies
      RatingAgency.Instance.addTrackingMeasurement(
         "Equity", new AgentEquityMeasurement());                       // Equity measurements
      RatingAgency.Instance.addTrackingMeasurement(                     // Price per share 
         "Price Per Share", new AgentStockPricePerShareMeasurement());  //  measurements
      RatingAgency.Instance.addTrackingMeasurement(                     // Dividend per share 
         "Dividend Per Share", new AgentDividendPerShareMeasurement()); //  measurements
      
      if(VERBOSE_MODE) {
         System.out.printf("ratings agency: \n");
         System.out.printf("adding equity tracking measure [%s]\n", 
            AgentEquityMeasurement.class.getSimpleName());
         System.out.printf("adding price per share tracking measure [%s]\n",
            AgentStockPricePerShareMeasurement.class.getSimpleName());
         System.out.printf("dividend per share [%s]\n",
            AgentDividendPerShareMeasurement.class.getSimpleName());
      }
      
      List<Firm> firms = result.getAgentsOfType(Firm.class);
      
      for(Firm firm : firms) {
         RatingAgency.Instance.trackAgent(firm);
         
         if(VERBOSE_MODE)
            System.out.printf("tracking agent %s [%s]\n",
               firm.getUniqueName(), firm.getClass().getSimpleName());
      }
      }
      
      // Households
      
      final int
         numberOfHouseholds = InjectionUtils.getParameter(
            injector, Integer.class, "NUMBER_OF_HOUSEHOLDS");
      {
      final boolean
         doHoldHouseholdDepositsExogenously = 
            InjectionUtils.getParameter(
               injector, Boolean.class, "DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY");
      
      final List<CommercialBank>
         banks = result.getAgentsOfType(CommercialBank.class);
      
      for(int i = 0; i< numberOfHouseholds; i++) {
         
         final DepositHolder
            bank = doHoldHouseholdDepositsExogenously ? 
               injector.getInstance(ExogenousDepositHolder.class) : 
                  (DepositHolder) banks.get(i % banks.size());
         
         final DepositingHousehold
            household = injector.getInstance(HouseholdFactory.class).create(bank);
         
         if(VERBOSE_MODE)
            System.out.printf(
               "Adding household %d/%d %s [%s] deposit holder: [%s]\n", 
               i+1, numberOfHouseholds, 
               household.getUniqueName(),
               household.getClass().getSimpleName(), 
               bank
               );
         
         result.addAgent(household);
      }
      }
      
      // Specify which goods consumer households should consider:
      {
      for(final DomesticGoodsConsumer goodsConsumer :
          result.getAgentsOfType(DomesticGoodsConsumer.class))
         goodsConsumer.addGoodsToConsider(
            injector.getInstance(
               Key.get(SectorNameProvider.class,
                  Names.named("HOUSEHOLD_GOODS_TYPES_TO_CONSUME"))).asList());
      }
      
      final boolean
         doEnableFunds = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_ENABLE_FUNDS");
      final double
         numberOfSharesToEmitPerBank = InjectionUtils.getParameter(
            injector, Double.class, "NUMBER_OF_SHARES_TO_EMIT_PER_BANK"),
         valueOfInitialShareEmissionPerBank = InjectionUtils.getParameter(
            injector, Double.class, "INITIAL_STOCK_EMISSION_VALUE_PER_BANK");
      final List<CommercialBank>
         banks = result.getAgentsOfType(CommercialBank.class);
      final int
         numBanks = banks.size();
      
      if(doEnableFunds) {
         
         // Fund markets to consider
         
         final CollectionProvider<ClearingStockMarket>
            fundStocksToConsider = 
               doEnableStockTrading ?
                  new AbstractCollectionProvider<ClearingStockMarket>() {
                     @Override
                     public Collection<ClearingStockMarket> get() {
                        return clearingHouse.getMarketsOfType(ClearingStockMarket.class);
                     }
                  } :
                  new EmptyCollectionProvider<ClearingStockMarket>();
         final CollectionProvider<ClearingLoanMarket>
            fundLoansToConsider = new AbstractCollectionProvider<ClearingLoanMarket>() {
               @Override
               public Collection<ClearingLoanMarket> get() {
                  return ArrayUtil.castList(clearingHouse.getMarketsOfType(
                     ClearingGiltsBondsAndCommercialLoansMarket.class));
               }
            };
         
         /*
          * Add {@link Fund}{@code s} to the economy.<br><br>
          * 
          * This method creates {@link Fund}{@code s} as follows:<br>
          *   (a) The first {@link #getNumberOfFundsAreNoiseTraders()}
          *       {@link Fund}{@code s} are instance of {@link NoiseTraderFund};<br>
          *   (b) All remaining {@link Fund}{@code s}, if any, are instances of
          *       {@link MutualFund};<br>
          *   (c) {@link Fund}{@code s} take ownership of all initial {@link Bank} shares, 
          *       divided equally among all {@link Fund}{@code s}.
          */
         {
         int
            bankDepositHolderIndex = 0;
         
         final int
            numberOfFunds = InjectionUtils.getParameter(
               injector, Integer.class, "NUMBER_OF_FUNDS");
         final boolean
            doHoldFundDepositsExogenously = InjectionUtils.getParameter(
               injector, Boolean.class, "DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY");
         
         for(int i = 0; i< numberOfFunds; ++i) {
            DepositHolder
               depositHolder = null;
            if(doHoldFundDepositsExogenously)
               depositHolder = injector.getInstance(ExogenousDepositHolder.class);
            else {
               depositHolder = banks.get(bankDepositHolderIndex);
               bankDepositHolderIndex = (++bankDepositHolderIndex % numBanks);
            }
            
            final Fund fund = 
               injector.getInstance(FundFactory.class).create(depositHolder);
            
            if(VERBOSE_MODE)
               System.out.printf(
                  "Adding fund %d/%d [%s] deposit holder: [%s]\n", 
                  i+1, numberOfFunds, 
                  fund.getClass().getSimpleName(), 
                  depositHolder.getClass().getSimpleName()
                  );
            
            clearingHouse.addFund(fund);
            clearingHouse.addStockMarketParticipant(fund);
            clearingHouse.addLender(fund);
            
            fund.setLoansToInvestIn(fundLoansToConsider);
            fund.setStocksToInvestIn(fundStocksToConsider);
            
            /*
             * Assign bank shares to funds. Funds own all initial Bank shares.
             */
            for(int j = 0; j< numBanks; ++j) {
               final StockReleasingBank
                  bank = (StockReleasingBank) banks.get(j);
               try {
                  bank.releaseShares(
                     numberOfSharesToEmitPerBank / numberOfFunds, 0., fund);
               } catch (final InsufficientFundsException neverThrows) {
                  System.err.println(neverThrows);
                  System.err.flush();
                  Simulation.getSimState().finish();
               }
               
               UniqueStockExchange.Instance.setStockPrice(
                  bank.getUniqueName(),
                  valueOfInitialShareEmissionPerBank / numberOfSharesToEmitPerBank
                  );
            }
            
            result.addAgent(fund);
         }
         
         for(StockTradingBank bank : banks) 
            bank.debit(valueOfInitialShareEmissionPerBank);
         }
         
         // Initial Household-Fund investments
         
         {
         final double
            proportionOfInitialHouseholdCashInFundInvestments = InjectionUtils.getParameter(
               injector, Double.class,
               "PROPORTION_OF_INITIAL_HOUSEHOLDS_CASH_IN_FUND_INVESTMENTS"
               );
         
         final List<Fund>
            funds = result.getAgentsOfType(Fund.class);
         final List<Object>
            householdInvestors = result.getAgentsSatisfying(
               DepositingHousehold.class, FundInvestor.class);
         
         for(int i = 0; i< householdInvestors.size(); ++i) {
            final Fund
               fund = (Fund) funds.get(i % funds.size());
            final DepositingHousehold
               household = (DepositingHousehold) householdInvestors.get(i);
            final FundInvestor
               investor = (FundInvestor) household;
            investor.setFund(fund);
            investor.setFundInvestmentTargetDecisionRule(
               injector.getInstance(FundInvestmentDecisionRule.class));
            try {
               fund.invest(
                  household,
                  proportionOfInitialHouseholdCashInFundInvestments * household.getDepositValue()
                  );
            } catch (final InsufficientFundsException neverThrows) {
               neverThrows.printStackTrace();
               System.err.println(neverThrows);
               System.err.flush();
            }
         }
         }
      }
      else {
         /*
          * Assign bank shares to other banks. Banks own all initial Bank shares.
          * Each bank will release a proportion of its shares to itself.
          */
         for(int j = 0; j< numBanks; ++j) {
            final StockReleasingBank
               bank = (StockReleasingBank) banks.get(j);
            for(int k = 0; k< numBanks; ++k) {
               try {
                  bank.releaseShares(
                     numberOfSharesToEmitPerBank / numBanks, 0., banks.get(k));
               } catch (final InsufficientFundsException neverThrows) {
                  System.err.println(neverThrows);
                  System.err.flush();
                  Simulation.getSimState().finish();
               }
               
               UniqueStockExchange.Instance.setStockPrice(
                  bank.getUniqueName(),
                  valueOfInitialShareEmissionPerBank / numberOfSharesToEmitPerBank
                  );
            }
         }
         
         for(StockTradingBank bank : banks) 
            bank.debit(valueOfInitialShareEmissionPerBank);
      }
      
      return result;
   }
}
