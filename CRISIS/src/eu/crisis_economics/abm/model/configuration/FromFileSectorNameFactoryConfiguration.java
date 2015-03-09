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

import java.io.File;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.io.FromFileSectorNameProvider;
import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link FromFileSectorNameProvider} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "From File"
   )
public final class FromFileSectorNameFactoryConfiguration
   extends AbstractSectorNameProviderConfiguration
   implements SectorNameProviderConfiguration {
   
   private static final long serialVersionUID = -6671938464356793340L;
   
   private File
      sourceFile = new File("");
   
   public File getSourceFile() {
      return sourceFile;
   }
   
   public void setSourceFile(
      final File sourceFile) {
      this.sourceFile = sourceFile;
   }
   
   public FromFileSectorNameFactoryConfiguration() { }
   
   public FromFileSectorNameFactoryConfiguration(
      final String sourceFilename) {
      this.sourceFile = new File(Preconditions.checkNotNull(sourceFilename));
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(!sourceFile.getAbsolutePath().isEmpty(),
         "From-File Sector Name Configuration: source filename is empty.");
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FROM_FILE_SECTOR_NAME_FACTORY_SOURCE_FILENAME", sourceFile.getAbsolutePath()
         ));
      if(getScopeString().isEmpty())
         bind(SectorNameProvider.class)
            .to(FromFileSectorNameProvider.class)
            .in(Singleton.class);
      else
         bind(SectorNameProvider.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(FromFileSectorNameProvider.class)
            .in(Singleton.class);
      super.addBindings();
   }
}
