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
package eu.crisis_economics.abm.model;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.bank.bankruptcy.BailinBankruptcyResolution;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptyHandler;
import eu.crisis_economics.abm.bank.bankruptcy.ComputeCARBankruptcyResolutionAmountOperation;
import eu.crisis_economics.abm.bank.bankruptcy.LiquidateBankruptcyResolution;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.abm.model.configuration.AbstractBankBankruptcyPolicyConfiguration;
import eu.crisis_economics.abm.model.configuration.ChartistAndFundamentalistBankSubeconomy;
import eu.crisis_economics.abm.model.configuration.MacroeconomicFirmsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;

/**
  * An implementation of the abstract {@link AbstractBankBankruptcyModel} model.
  * This implementation specializes the abstract class {@link AbstractBankBankruptcyModel}
  * to the liquidation bankruptcy resolution case.
  * 
  * @author phillips
  */
public class BankLiquidationModel extends AbstractBankBankruptcyModel {
   
   public BankLiquidationModel() {
      super(1L);
   }
   
   public BankLiquidationModel(long seed) {
      super(seed);
   }
   
   static class BankBankruptcyResolutionPolicyProvider
      implements Provider<BankBankruptyHandler> {
      
      @Inject
      private Injector
         injector;
      
      private static int
         useCounter = 0;
      
      @Override
      public BankBankruptyHandler get() {
         BankBankruptyHandler result = null;
         if(useCounter == 0)
            result = injector.getInstance(LiquidateBankruptcyResolution.class);
         else
            result = injector.getInstance(BailinBankruptcyResolution.class);
         ++useCounter;
         return result;
      }
   }
   
   @Override
   protected MasterModelConfiguration configureBankruptcyPolicy(
      MasterModelConfiguration agentsConfiguration) {
      
      final MacroeconomicFirmsSubEconomy
         firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
      
      agentsConfiguration.setFirmsSubEconomy(firmsSubEconomy);
      
      final ChartistAndFundamentalistBankSubeconomy
         financialSubeconomy = new ChartistAndFundamentalistBankSubeconomy();
      
      financialSubeconomy.setBankBankruptcyResolutionPolicy(
         new AbstractBankBankruptcyPolicyConfiguration() {
            private static final long serialVersionUID = 1787536842118613505L;
            
            @Override
            protected void addBindings() {
               super.addBindings();
               bind(BankBankruptyHandler.class)
                  .toProvider(BankBankruptcyResolutionPolicyProvider.class);
               bind(new TypeLiteral<AgentOperation<Double>>(){})
                  .annotatedWith(Names.named("BANK_BAILIN_RESOLUTION_EQUITY_ADJUSTMENT"))
                  .to(ComputeCARBankruptcyResolutionAmountOperation.class);
               install(new FactoryModuleBuilder()
                  .build(SingleBeneficiaryIntermediaryFactory.class)
                  );
               bind(BailinBankruptcyResolution.class);
               expose(BailinBankruptcyResolution.class);
               expose(BankBankruptyHandler.class);
            }
            }
         );
      
      agentsConfiguration.setBankingSubEconomy(financialSubeconomy);
      
      return agentsConfiguration;
   }
   
   private static final long serialVersionUID = 4304298943519243651L;
}
