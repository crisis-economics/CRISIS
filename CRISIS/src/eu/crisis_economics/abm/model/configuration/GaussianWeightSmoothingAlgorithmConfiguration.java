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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.smoothing.GaussianWeightSmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link GaussianWeightSmoothingAlgorithm} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "No Sharp Change"
   )
public class GaussianWeightSmoothingAlgorithmConfiguration
   extends AbstractSmoothingAlgorithmConfiguration {
   
   private static final long serialVersionUID = -6811296371482820555L;
   
   private final static double
      DEFAULT_MINIMUM_WEIGHT = .1;
   
   @Parameter(
      ID = "GAUSSIAN_WEIGHT_SMOOTHING_MINIMUM_WEIGHT"
      )
   private double
      minimumWeight = DEFAULT_MINIMUM_WEIGHT;
   
   public double getMinimumWeight() {
      return minimumWeight;
   }
   
   public void setMinimumWeight(
      final double minimumWeight) {
      this.minimumWeight = minimumWeight;
   }
   
   /**
     * Create a {@link GaussianWeightSmoothingAlgorithmConfiguration} object
     * with custom parameters.
     * 
     * @param minimumWeight
     *        The minimum weight term to be used by {@link GaussianWeightSmoothingAlgorithm}
     *        objects configured by this component. See also
     *        {@link GaussianWeightSmoothingAlgorithm}.
     */
   public GaussianWeightSmoothingAlgorithmConfiguration(
      final double smoothingWeight) {
      this.minimumWeight = smoothingWeight;
   }
   
   /**
     * Create a {@link GaussianWeightSmoothingAlgorithmConfiguration} object
     * with default parameters.
     */
   public GaussianWeightSmoothingAlgorithmConfiguration() {
      this(DEFAULT_MINIMUM_WEIGHT);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "GAUSSIAN_WEIGHT_SMOOTHING_MINIMUM_WEIGHT",  minimumWeight));
      if(getScopeString().isEmpty())
         bind(SmoothingAlgorithm.class).to(GaussianWeightSmoothingAlgorithm.class);
      else
         bind(SmoothingAlgorithm.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(GaussianWeightSmoothingAlgorithm.class);
   }
}
