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
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;

/**
  * Interfaces for a {@link ClearingBankStrategy} factories. This interface is used
  * by the dependency injector. It is not necessary for this interface to
  * be implemented.
  * 
  * @author phillips
  */
public interface ClearingBankStrategyFactory {
   
   public interface CentralBankStrategyFactory extends ClearingBankStrategyFactory {
      @Override
      public CentralBankStrategy create(ClearingBank bank);
   }
   
   public ClearingBankStrategy create(ClearingBank bank);
}
