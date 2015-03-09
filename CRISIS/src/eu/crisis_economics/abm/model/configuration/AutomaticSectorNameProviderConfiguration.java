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
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.io.AutomaticSectorNameProvider;
import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link AutomaticSectorNameProvider} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Automatic Sector Names"
   )
public final class AutomaticSectorNameProviderConfiguration
   extends AbstractSectorNameProviderConfiguration
   implements SectorNameProviderConfiguration {
   
   private static final long serialVersionUID = -6168324519210970657L;
   
   @Parameter(
      ID = "AUTOMATIC_SECTOR_NAME_FACTORY_BASE_NAME"
      )
   private String
      baseName = AutomaticSectorNameProvider.DEFAULT_SECTOR_BASE_NAME;
   
   public String getBaseName() {
      return baseName;
   }
   
   public void setBaseName(
      final String baseName) {
      this.baseName = baseName;
   }
   
   @Parameter(
      ID = "NUMBER_OF_SECTORS"
      )
   private int
      numberOfSectors;
   
   public int getNumberOfSectors() {
      return numberOfSectors;
   }
   
   /**
     * Set the number of sectors to be generated. This argument must be strictly
     * positive.
     */
   public void setNumberOfSectors(
      final int numberOfSectors) {
      this.numberOfSectors = numberOfSectors;
   }
   
   public AutomaticSectorNameProviderConfiguration() { }
   
   public AutomaticSectorNameProviderConfiguration(
      final String baseName,
      final int numberOfSectors
      ) {
      this.baseName = Preconditions.checkNotNull(baseName);
      this.numberOfSectors = numberOfSectors;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(numberOfSectors > 0,
         "Automatic Sector Name Configuration: number of sectors is zero or negative.");
      Preconditions.checkArgument(!baseName.isEmpty(),
         "Automatic Sector Name Configuration: sector base name string is empty.");
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "AUTOMATIC_SECTOR_NAME_FACTORY_BASE_NAME",            baseName,
         "AUTOMATIC_SECTOR_NAME_FACTORY_NUMBER_OF_SECTORS",    numberOfSectors
         ));
      bind(Integer.class)
         .annotatedWith(Names.named("NUMBER_OF_SECTORS"))
         .toInstance(numberOfSectors);
      if(getScopeString().isEmpty())
         bind(SectorNameProvider.class)
            .to(AutomaticSectorNameProvider.class)
            .in(Singleton.class);
      else
         bind(SectorNameProvider.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(AutomaticSectorNameProvider.class)
            .in(Singleton.class);
      super.addBindings();
   }
}
