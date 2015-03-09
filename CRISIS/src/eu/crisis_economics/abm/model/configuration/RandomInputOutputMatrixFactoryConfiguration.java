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

import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.io.RandomInputOutputMatrixFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link RandomInputOutputMatrixFactory} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Random Network"
   )
public final class RandomInputOutputMatrixFactoryConfiguration
   extends AbstractInputOutputMatrixFactoryConfiguration
   implements InputOutputMatrixFactoryConfiguration {
   
   private static final long serialVersionUID = 5606551462382975319L;
   
   @Parameter(
      ID = "RANDOM_INPUT_OUTPUT_MATRIX_FACTORY_SEED"
      )
   private long
      seed = 0L;
   
   public long getSeed() {
      return seed;
   }
   
   public void setSeed(
      final long seed) {
      this.seed = seed;
   }
   
   public RandomInputOutputMatrixFactoryConfiguration() { }
   
   /**
     * Create a {@link RandomInputOutputMatrixFactoryConfiguration} with custom
     * parameters.
     * 
     * @param seed
     *        A random number generator seed to be used to configure 
     *        {@link RandomInputOutputMatrixFactory} objects.
     */
   public RandomInputOutputMatrixFactoryConfiguration(final long seed) { 
      this.seed = seed;
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "RANDOM_INPUT_OUTPUT_MATRIX_FACTORY_SEED", seed));
      bind(InputOutputMatrixFactory.class)
         .to(RandomInputOutputMatrixFactory.class);
      super.addBindings();
   }
}
