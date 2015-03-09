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

import eu.crisis_economics.abm.algorithms.matching.RandomDenyRationing;
import eu.crisis_economics.abm.algorithms.matching.RationingAlgorithm;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

public final class RandomDenyRationingAlgorithmConfiguration
   extends AbstractPrivateConfiguration
   implements MarketRationingAlgorithmConfiguration {
   
   private static final long serialVersionUID = 2390709879220500886L;
   
   @Parameter(
      ID = "RANDOM_DENY_RATIONING_ALGORITHM_INHOMOGENEITY_FACTOR"
      )
   private double
      inhomogeneityOfRationing = RandomDenyRationing.DEFAULT_INHOMOGENEITY_OF_RATIONING;
   
   public double getInhomogeneityOfRationing() {
      return inhomogeneityOfRationing;
   }
   
   /**
     * Set the degree of inhomogeneity for this rationing algorithm. The argument 
     * should be non-negative and less than {@code 1.0}.
     */
   public void setInhomogeneityOfRationing(
      final double inhomogeneityOfRationing) {
      this.inhomogeneityOfRationing = inhomogeneityOfRationing;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         inhomogeneityOfRationing >= 0. && inhomogeneityOfRationing <= 1.,
         toParameterValidityErrMsg("Inhomogeneity of Rationing is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "RANDOM_DENY_RATIONING_ALGORITHM_INHOMOGENEITY_FACTOR",  inhomogeneityOfRationing
         ));
      bind(
         RationingAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(RandomDenyRationing.class);
      expose(
         RationingAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
