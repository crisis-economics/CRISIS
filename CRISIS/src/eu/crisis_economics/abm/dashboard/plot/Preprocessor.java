/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.dashboard.plot;

import java.util.List;
import java.util.Map;

import eu.crisis_economics.abm.dashboard.results.Run;

/**
 * Implementations of this interface can be used to pre-process the results before plotting. 
 * 
 * 
 * @author Tamás Máhr
 *
 */
public interface Preprocessor {
	
	/**
	 * Pre-processes the results before plotting. The {@link Map} returned can be used to add to a plot by the
	 * {@link DynamicPlot#addDataCollection(Map)} method. The keys in the map should correspond to data-series names given to plots.
	 * 
	 * @param run the results to pre-process
	 * @return a map containing data series to be plotted
	 */
	Map<String, List<Object>> process(final Run run);

}
