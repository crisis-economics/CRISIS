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
package eu.crisis_economics.abm.simulation;

/**
  * Fatal runtime exception with compulsory error message.
  * @author phillips
  */
public final class FatalSimulationException extends RuntimeException {
   
   private static final long serialVersionUID = -6059800585822022663L;
   
   public FatalSimulationException(final String s) {
      super("Fatal simulation exception: " + s);
   }
   
   public FatalSimulationException(final String s, final Throwable cause) {
      super("Fatal simulation exception: " + s, cause);
   }
   
   public FatalSimulationException(final Throwable cause) {
      super(cause);
   }
}
