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

import eu.crisis_economics.abm.firm.FirmSectorProvider;
import eu.crisis_economics.abm.firm.IndexFileFirmSectorProvider;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link IndexFileFirmSectorProviderConfiguration} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Indices From File"
   )
public final class IndexFileFirmSectorProviderConfiguration
   extends AbstractFirmSectorProviderConfiguration implements FirmSectorProviderConfiguration {
   
   private static final long serialVersionUID = 7188640210308607911L;
   
   @Parameter(
      ID = "FROM_FILE_FIRM_SECTOR_NAME_PROVIDER_SOURCE_FILENAME"
      )
   private File
      sourceFile = new File("");
   
   public File getSourceFile() {
      return sourceFile;
   }

   public void setSourceFile(
      final File sourceFile) {
      this.sourceFile = sourceFile;
   }
   
   public IndexFileFirmSectorProviderConfiguration() { }
   
   /**
     * Create a {@link IndexFileFirmSectorProviderConfiguration} object with
     * custom parameters.
     * 
     * @param filename
     *        The source filename from which to create a {@link IndexFileFirmSectorProvider}
     *        object. This argument must be non-<code>null</code>.
     */
   public IndexFileFirmSectorProviderConfiguration(
      final String filename) {
      this.sourceFile = new File(Preconditions.checkNotNull(filename));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FROM_FILE_FIRM_SECTOR_NAME_PROVIDER_SOURCE_FILENAME", 
         sourceFile.getAbsolutePath()
         ));
      bind(FirmSectorProvider.class)
         .to(IndexFileFirmSectorProvider.class)
         .in(Singleton.class);
      super.addBindings();
   }
}