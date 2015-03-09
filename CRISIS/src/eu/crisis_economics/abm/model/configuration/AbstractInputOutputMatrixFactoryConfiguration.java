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

/**
  * An abstract base class for {@link InputOutputMatrixFactory} configuration
  * components.
  * 
  * @author phillips
  */
//
// TODO: the following snippets were taken from an enum-based Input-Output selection
//       dialogue. This content will need to be distributed among the implementations
//       of this class.
//
//public final String desInputOutputNetworkType() {
//   return 
//      "The type of Input-Output (IO) table to determine the network of interdependencies "
//    + "between sectors of production in the macroeconomy. Possible settings include:\n\n"
//    + "GENERATE_RANDOM_IO_TABLE generates an IO table using the Random(1L) method from "
//    + "java.util.Random.  All rows are normalized so that the sum of entries in each "
//    + "row equals 1.\n\n"
//    + "USE_HOMOGENEOUS_IO_TABLE generates an IO table in which technological weights are "
//    + "such that sectors are not biased towards purchasing their own goods class for production."
//    + " The entries of a homogeneous IO table all have the same value, which is the reciprocal "
//    + "of the number of sectors.\n\n"
//    + "USE_SIMPLE_SELF_BIASING_IO_TABLE generates an IO table in which sectors are biased "
//    + "toward purchasing their own goods class for production.  The entries of a simple self "
//    + "biasing IO table are larger on the main diagonal than the other positions in the matrix."
//    + "  Note that all rows sum to 1.\n\n"
//    + "USE_AN_EXTERNAL_IO_TABLE allows the user to upload their own table, which requires the "
//    + "path of the file to be specified in the 'External I O Table Data Filename' parameter "
//    + "box.\n\n";}
//
///**
//  * Verbose description for the {@link getInputOutputExternalTableFilename} 
//  * parameter.
//  */
//public final String desInputOutputExternalTableFilename() {
//   return 
//      "The path of the file containing the Input-Output table that the user provides" + 
//      " when the 'Input Output Network Type' drop-down menu is set to "
//      + "'USE_AN_EXTERNAL_IO_TABLE'.";
//}
public class AbstractInputOutputMatrixFactoryConfiguration
   extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = 1419601760190348421L;
   
   /**
     * Create a {@link AbstractInputOutputMatrixFactoryConfiguration} object.
     * 
     * Implementatinos of this class must provide the following bindings:
     * <ul>
     *   <li> An implementation of {@link InputOutputMatrixFactory}{@code .class}
     * </ul>
     */
   protected AbstractInputOutputMatrixFactoryConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      requireBinding(InputOutputMatrixFactory.class);
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a last
     * instruction.
     */
   @Override
   protected void addBindings() {
      expose(InputOutputMatrixFactory.class);
   }
}
