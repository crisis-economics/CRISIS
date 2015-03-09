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
package eu.crisis_economics.abm.simulation.injection.factories;

import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.model.parameters.catalogue.ForwardingModelParameterCatalogue;
import eu.crisis_economics.abm.model.parameters.catalogue.ModelParameterCatalogue;
import eu.crisis_economics.abm.model.parameters.catalogue.ParameterCatalogueConfigurationModule;

/**
  * Static utility methods for dependency injection factories.
  * 
  * @author phillips
  */
public final class InjectionFactoryUtils {
   /**
     * Convert an existing {@link ClearingBankStrategyFactory} to a
     * {@link BankStrategyFactory}.
     * 
     * @param factory
     */
   public static BankStrategyFactory asBankStrategyFactory(
      final ClearingBankStrategyFactory factory) {
      return new BankStrategyFactory() {
         @Override
         public BankStrategy create(StrategyBank bank) {
            return factory.create((ClearingBank) bank);
         }
      };
   }
   
   public static ParameterCatalogueConfigurationModule asPrimitiveParameterModule(
      Object... objects) {
      final ModelParameterCatalogue parameters =
         new ForwardingModelParameterCatalogue();
      if(objects.length % 2 != 0)
         throw new IllegalArgumentException(
            "InjectionFactoryUtils.asParameterModule: odd number of arguments.");
      int numPairs = objects.length / 2;
      for(int i = 0; i< numPairs; ++i) {
         final String key = (String) objects[i * 2];
         final Object value = objects[i * 2 + 1];
         if(value instanceof Boolean)
            parameters.put(key, (Boolean) value);
         else if(value instanceof Integer)
            parameters.put(key, (Integer) value);
         else if(value instanceof String)
            parameters.put(key, (String) value);
         else if(value instanceof Double)
            parameters.put(key, (Double) value);
         parameters.putGeneric(key, value);
      }
      ParameterCatalogueConfigurationModule module =
         new ParameterCatalogueConfigurationModule();
      module.setParameterCatalogue(parameters);
      return module;
   }
}
