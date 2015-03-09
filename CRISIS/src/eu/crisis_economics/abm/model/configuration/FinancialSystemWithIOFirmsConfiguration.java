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
package eu.crisis_economics.abm.model.configuration;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.model.plumbing.FinancialSystemWithIOFirms;
import eu.crisis_economics.abm.model.plumbing.Plumbing;

/**
  * A simple implementation of the {@link AbstractPlumbingConfiguration}
  * class. This implementation binds the {@link Plumbing} interface to the
  * {@link FinancialSystemWithIOFirms} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Input-Output Firms & Financial System",
   ImagePath = "CRISIS-simple.jpg"
   )
public final class FinancialSystemWithIOFirmsConfiguration
   extends AbstractPlumbingConfiguration {
   
   private static final long serialVersionUID = 3651211194932584850L;
   
   @Override
   protected void addBindings() {
      bind(Plumbing.class).to(FinancialSystemWithIOFirms.class).asEagerSingleton();
   }
}
