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
import com.google.inject.Singleton;

import eu.crisis_economics.abm.firm.FirmSectorProvider;
import eu.crisis_economics.abm.firm.MultipleFirmsPerSectorProvider;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link MultipleFirmsPerSectorProvider} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Multiple Firms Per Sector"
   )
public final class MultipleFirmsPerSectorProviderConfiguration
   extends AbstractFirmSectorProviderConfiguration implements FirmSectorProviderConfiguration {
   
   private static final long serialVersionUID = 1789432858908491065L;
   
   public final static int
      DEFAULT_NUMBER_OF_FIRMS_PER_SECTOR = 1;
   
   @Parameter(
      ID = "NUMBER_OF_FIRMS_PER_SECTOR"
      )
   private int
      numberOfFirmsPerSector = DEFAULT_NUMBER_OF_FIRMS_PER_SECTOR;
   
   public int getNumberOfFirmsPerSector() {
      return numberOfFirmsPerSector;
   }
   
   /**
     * Set the number of firms per sector for {@link MultipleFirmsPerSectorProvider}
     * objects created with this configuration component. The argument must be 
     * strictly positive.
     */
   public void setNumberOfFirmsPerSector(
      final int numberOfFirmsPerSector) {
      this.numberOfFirmsPerSector = numberOfFirmsPerSector;
   }
   
   public MultipleFirmsPerSectorProviderConfiguration() { }
   
   /**
     * Create a {@link MultipleFirmsPerSectorProviderConfiguration} object with
     * custom parameters.
     * 
     * @param numberOfFirmsPerSector
     *        The number of firms per sector to be used for instances of 
     *        {@link MultipleFirmsPerSectorProvider} configured by this component.
     */
   public MultipleFirmsPerSectorProviderConfiguration(
      int numberOfFirmsPerSector) {
      this.numberOfFirmsPerSector = numberOfFirmsPerSector;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(numberOfFirmsPerSector > 0);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_FIRMS_PER_SECTOR", numberOfFirmsPerSector));
      bind(FirmSectorProvider.class)
         .to(MultipleFirmsPerSectorProvider.class)
         .in(Singleton.class);
      super.addBindings();
   }
}