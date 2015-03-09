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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Injector;

import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsInstrument;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.Market.InstrumentMatchingMode;
import eu.crisis_economics.utilities.InjectionUtils;

public final class FinancialSystemWithMacroeconomy implements Plumbing {   // Stateless
   
   private Plumbing
      base;
   
   private static final boolean
      VERBOSE_MODE = true;
   
   public FinancialSystemWithMacroeconomy() {
      this.base = new FinancialSystemWithIOFirms();
   }
   
   @Override
   public BaseAgentGroup populate(final Injector injector) {
      base = new FinancialSystemWithIOFirms();
      
      BaseAgentGroup result = base.populate(injector);
      
      final boolean
         isInterbankMarketEnabled = InjectionUtils.getParameter(
            injector, Boolean.class, "IS_INTERBANK_MARKET_ENABLED");
      
      final boolean
         isCentralBankEnabled = InjectionUtils.getParameter(
            injector, Boolean.class, "IS_CENTRAL_BANK_ENABLED"),
         isBadBankEnabled = InjectionUtils.getParameter(
            injector, Boolean.class, "IS_BAD_BANK_ENABLED");
      
      { // Central Bank and Bad Bank
      CentralBank centralBank = null;
      if(isCentralBankEnabled) {
         centralBank = injector.getInstance(CentralBank.class);
         result.addAgent(centralBank);
         
         if(VERBOSE_MODE)
            System.out.printf("adding central bank: [%s]\n",
               centralBank.getClass().getSimpleName());
         
         centralBank.getStrategy().addFirms(
            result.getAgentsOfType(LoanStrategyFirm.class));
         
         for(final CommercialBank bank : result.getAgentsOfType(CommercialBank.class))
            bank.setCentralBank(centralBank);
      }
      
      BadBank badBank = null;
      if(isBadBankEnabled) {
         badBank = injector.getInstance(BadBank.class);
         result.addAgent(badBank);
         
         if(VERBOSE_MODE)
            System.out.printf("adding bad bank: [%s]\n",
               badBank.getClass().getSimpleName());
         
         for(final CommercialBank bank : result.getAgentsOfType(CommercialBank.class))
            bank.setBadBank(badBank);
      }
      
      if(isInterbankMarketEnabled) {
         InterbankLoanMarket interbankLoanMarket =
            injector.getInstance(InterbankLoanMarket.class);
         interbankLoanMarket.setInstrumentMatchingMode(
            InstrumentMatchingMode.SYNCHRONOUS);
         
         if(isCentralBankEnabled)
            centralBank.addMarket(interbankLoanMarket);
         if(isBadBankEnabled)
            badBank.addMarket(interbankLoanMarket);
         
         result.addMarketStructure(interbankLoanMarket);
      }
      }
      
      if(VERBOSE_MODE)
         System.out.printf("interbank market: %s\n",
            isInterbankMarketEnabled ? "enabled" : "disabled");
      
      final ClearingHouse
         clearingHouse = injector.getInstance(ClearingHouse.class);
      
      Government government = null;
      {   // Government
      final double
         governmentTotalGiltSupply = InjectionUtils.getParameter(
            injector, Double.class, "GOVERNMENT_TOTAL_GILT_SUPPLY"),
         governmentWagePerUnitLabourAskPrice = InjectionUtils.getParameter(
            injector, Double.class, "GOVERNMENT_WAGE_PER_UNIT_LABOUR_ASK_PRICE");
      final boolean
         doEnableGovernment = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_ENABLE_GOVERNMENT"),
         doEnableGovernmentLabourMarketParticipation = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_ENABLE_GOVERNMENT_LABOUR_MARKET_PARTICIPATION"),
         doEnableGovernmentGoodsMarketParticipation = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_ENABLE_GOVERNMENT_GOODS_MARKET_PARTICIPATION"),
         doEnableGovernmentGiltsMarketParticipation = InjectionUtils.getParameter(
            injector, Boolean.class, "DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION");
      
      if(doEnableGovernment) {
         government = injector.getInstance(Government.class);
         
         if(doEnableGovernmentLabourMarketParticipation)
            government.enableLabourMarketParticipation(
               injector.getInstance(SimpleLabourMarket.class), 
               governmentWagePerUnitLabourAskPrice
               );
         
         if(doEnableGovernmentGoodsMarketParticipation) {
            final Map<String, Double>
               consumptionWeights = new LinkedHashMap<String, Double>();
            for(final SimpleGoodsInstrument instrument :
                result.getMarketsOfType(SimpleGoodsMarket.class).get(0)
                )
               consumptionWeights.put(instrument.getGoodsType(), 1.0);
            for(final Entry<String, Double> record : consumptionWeights.entrySet())
               record.setValue(record.getValue() / consumptionWeights.size());
            government.enableGoodsMarketParticipation(
               injector.getInstance(SimpleGoodsMarket.class), consumptionWeights);
         }
         
         if(doEnableGovernmentGiltsMarketParticipation)
            government.enableGiltMarketParticipation(governmentTotalGiltSupply);
         
         clearingHouse.addStockMarketParticipant(government);
         clearingHouse.addBorrower(government);
         
         for(final Household household : result.getAgentsOfType(Household.class))
            government.addBenefitsRecipient((Employee) household);
         
         result.addAgent(government);
      }
      }
      
      if(isInterbankMarketEnabled) {
         for(final Bank bank : result.getAgentsOfType(Bank.class))
            bank.addMarket(injector.getInstance(InterbankLoanMarket.class));
      }
      
      return result;
   }
}