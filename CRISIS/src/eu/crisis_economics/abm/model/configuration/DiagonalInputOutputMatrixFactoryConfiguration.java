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

import eu.crisis_economics.abm.firm.io.DiagonalInputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A lightweight component for the {@link DiagonalInputOutputMatrixFactoryConfiguration}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Trivial Network"
   )
public final class DiagonalInputOutputMatrixFactoryConfiguration
   extends AbstractInputOutputMatrixFactoryConfiguration
   implements InputOutputMatrixFactoryConfiguration {
   
   private static final long serialVersionUID = 2650147807227766155L;
   
   @Override
   protected void addBindings() {
      bind(InputOutputMatrixFactory.class)
         .to(DiagonalInputOutputMatrixFactory.class);
      super.addBindings();
   }
}
