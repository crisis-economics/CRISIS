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

import kcl.waterloo.graphics.GJGraphContainer;

/**
 * A {@link DynamicPlot} instance is a plot that can add new data to the plot after being created. 
 * 
 * @author Tamás Máhr
 *
 */
public interface DynamicPlot {
	
	/**
	 * Updates the plot by adding the new data.
	 * 
	 * @param result a map containing new data
	 */
	void addData(Map<String, Object> result);

	/**
	 * Updates the plot by adding the new data.
	 * 
	 * @param result a map containing new data
	 */
	void addDataCollection(Map<String, List<Object>> result);

	
	/**
	 * Returns the {@link GJGraphContainer} instance that displays this plot.
	 * 
	 * @return the {@link GJGraphContainer} instance that displays this plot.
	 */
	GJGraphContainer getGraphContainer();
	
	/**
	 * Adds or removes a label from the plot. The label displays the message ,,Waiting for all data to arrive''.
	 * 
	 * @param waiting if true, the labed is displayed, if false it is removed from the plot
	 */
	void setWaitingForData(boolean waiting);
}
