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

import eu.crisis_economics.abm.firm.io.FromFileInputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link FromFileInputOutputMatrixFactoryConfiguration}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Custom Network"
   )
public final class FromFileInputOutputMatrixFactoryConfiguration
   extends AbstractInputOutputMatrixFactoryConfiguration
   implements InputOutputMatrixFactoryConfiguration {
   
   private static final long serialVersionUID = -675936715572795770L;
   
   @Parameter(
      ID = "FROM_FILE_INPUT_OUTPUT_MATRIX_FACTORY_FILENAME"
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
   
   public FromFileInputOutputMatrixFactoryConfiguration() { }
   
   /**
     * Create a {@link FromFileInputOutputMatrixFactoryConfiguration} with custom
     * parameters.
     * 
     * @param filename
     *        The name of the file from which {@link FromFileInputOutputMatrixFactory}
     *        objects will be created. This argument must be non-<code>null</code>.
     */
   public FromFileInputOutputMatrixFactoryConfiguration(
      final String filename) { 
      this.sourceFile = new File(Preconditions.checkNotNull(filename));
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "FROM_FILE_INPUT_OUTPUT_MATRIX_FACTORY_FILENAME", sourceFile.getAbsolutePath()));
      bind(InputOutputMatrixFactory.class)
         .to(FromFileInputOutputMatrixFactory.class);
      super.addBindings();
   }
}
