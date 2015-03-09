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
package eu.crisis_economics.abm.firm.bankruptcy;

import ai.aitia.crisis.aspectj.Agent;
import eu.crisis_economics.abm.firm.Firm;

/**
  * An interface for a {@link FirmBankruptcyHandler} algorithm. Implementations of this
  * interface provide on-demand bankruptcy resolution for {@link Firm} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public interface FirmBankruptcyHandler {
   /**
     * Perform a {@link Firm} bankruptcy resolution process for the specified {@link Agent}.
     * <br><br>
     *
     * @param firm
     *        The {@link Firm} {@link Agent} for which to perform a bankruptcy resolution
     *        process.
     */
   public void initializeBankruptcyProcedure(Firm firm);
}
