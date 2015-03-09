/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.firm.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.parameters.FromFileStringParameter;

/**
  * An implementation of the {@link SectorNameProvider} interface. This implementation
  * proves sector name from a text file. Each record in the text file should be distinct.
  * The text file should be formatted with one sector names per line. An example of a 
  * valid input file is as follows:<br><br>
  * 
  * <code><table>
  * <tr><td>AGRICULTURE</td></tr>
  * <tr><td>MINING</td></tr>
  * <tr><td>LEGAL_SERVICES</td></tr>
  * </table></code>
  * 
  * @author phillips
  */
public final class FromFileSectorNameProvider implements SectorNameProvider {
   
   private final String
      filename;
   
   /**
     * Create a {@link FromFileStringParameter} from a filename.<br><br>
     * 
     * This constructor tests whether the specified filename can be parsed as
     * a source of sector names. If it is not the case that the input file
     * can be parsed as a source of sector names, {@link IOException} is raised.
     * 
     * @param filename
     *        The name of the file from which to read sector names. See 
     *        {@link FromFileStringParameter} in regard to the 
     *        expected format of this file.
     * 
     * @throws IOException
     *         An {@link IOException} is raised if the specified file does
     *         not exist, is inaccessible, is unreadable or has an unacceptable format.
     */
   @Inject
   public FromFileSectorNameProvider(
   @Named("FROM_FILE_SECTOR_NAME_FACTORY_SOURCE_FILENAME")
      final String filename
      ) throws IOException {
      this.filename = Preconditions.checkNotNull(filename);
      this.setResult = parse();
      this.listResult = new ArrayList<String>(setResult);
   }
   
   private final LinkedHashSet<String>
      setResult;
   private final List<String>
      listResult;
   
   @Override
   public LinkedHashSet<String> asSet() {
      return new LinkedHashSet<String>(setResult);
   }
   
   @Override
   public List<String> asList() {
      return new ArrayList<String>(listResult);
   }
   
   private LinkedHashSet<String> parse() {
      final FromFileStringParameter
         sectorNames;
      try {
         sectorNames = new FromFileStringParameter(filename, "SECTORS");
      } catch (final IOException e) {
         throw new IllegalStateException(e.getMessage());
      }
      final LinkedHashSet<String>
         result = new LinkedHashSet<String>();
      for(int i = 0; i< sectorNames.getNumberOfRecords(); ++i)
         result.add(sectorNames.get());
      return result;
   }
   
   /**
     * Get the source filename for this {@link FromFileSectorNameProvider}.
     */
   public String getSourceFilename() {
      return filename;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "From File Sector Name Factory, filename:" + filename + ".";
   }
}
