/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.contracts;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.markets.Seller;

/**
 * A <code>Depositor</code> is an entity (e.g. a firm or household) that keeps its deposits in other entities' book as a
 * liability (e.g. in a bank).
 * 
 * @author Tamás Máhr
 */
public interface Depositor
   extends Seller, SettlementParty, HasAssets {
   
   public double getDepositValue();
}
