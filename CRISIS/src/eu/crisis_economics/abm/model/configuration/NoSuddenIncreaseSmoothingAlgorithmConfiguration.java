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

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.smoothing.NoSuddenIncreaseSmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link NoSuddenIncreaseSmoothingAlgorithm} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Smooth Increase"
   )
public class NoSuddenIncreaseSmoothingAlgorithmConfiguration
   extends AbstractSmoothingAlgorithmConfiguration {
   
   private static final long serialVersionUID = -4468203838491494375L;
   
   private final static double
      DEFAULT_WEIGHT = .9;
   
   @Parameter(
      ID = "NO_SUDDEN_INCREASES_SMOOTHING_ALGORITHM_WEIGHT"
      )
   private double
      smoothingWeight = DEFAULT_WEIGHT;
   
   public double getSmoothingWeight() {
      return smoothingWeight;
   }
   
   public void setSmoothingWeight(
      final double smoothingWeight) {
      this.smoothingWeight = smoothingWeight;
   }
   
   /**
     * Create a {@link NoSuddenIncreaseSmoothingAlgorithmConfiguration} object
     * with custom parameters.
     * 
     * @param smoothingWeight
     *        The smoothing weight to be used by {@link NoSuddenIncreaseSmoothingAlgorithm}
     *        objects configured by this component. This argument should be non-negative
     *        and not greater than {@code 1.0}.
     */
   public NoSuddenIncreaseSmoothingAlgorithmConfiguration(
      final double smoothingWeight) {
      this.smoothingWeight = smoothingWeight;
   }
   
   /**
     * Create a {@link NoSuddenIncreaseSmoothingAlgorithmConfiguration} object
     * with default parameters.
     */
   public NoSuddenIncreaseSmoothingAlgorithmConfiguration() {
      this(DEFAULT_WEIGHT);
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(smoothingWeight >= 0.,
         toParameterValidityErrMsg("Exponential smoothing weight is negative."));
      Preconditions.checkArgument(smoothingWeight >= 0., 
         toParameterValidityErrMsg("Exponential smoothing weight is greather than 1.0."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NO_SUDDEN_INCREASES_SMOOTHING_ALGORITHM_WEIGHT",  smoothingWeight));
      if(getScopeString().isEmpty())
         bind(SmoothingAlgorithm.class).to(NoSuddenIncreaseSmoothingAlgorithm.class);
      else
         bind(SmoothingAlgorithm.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(NoSuddenIncreaseSmoothingAlgorithm.class);
   }
}
