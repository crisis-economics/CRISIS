/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.dashboard.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder;

/**
 * The {@link Run} class represents a single simulation run. Instances store the parameter settings and the results -- the recorded values.
 * 
 * @author Tamás Máhr
 * 
 */
public class Run {

	/**
	 * The recorded results. Keys are the data source names, the values are the list of values.
	 */
	Map<String, List<Object>> results = new HashMap<String, List<Object>>();
	
	/**
	 * The parameter values that were set for this simulation run.
	 */
	Map<String, Object> parameters = new HashMap<String, Object>();
	
	/**
	 * Sets a parameter value. This method can be used to build this {@link Run} instance. If a parameter value has already been set previously, it is
	 * returned.
	 * 
	 * @param name the name of the parameter, it is used as a key in the {@link #parameters} map.
	 * @param value the value of the parameter
	 * @return the previous value of the named parameter
	 */
	public Object addParameter(final String name, final Object value){
		return parameters.put(name, value);
	}

	/**
	 * Appends a recorded value to the list of values denoted by the given data source name. This can be used to build this {@link Run} instance.
	 * 
	 * @param columnName the data source name
	 * @param value the value to be appended.
	 * @return true as defined by {@link Collection#add(Object)}
	 */
	public boolean addResult(final String columnName, final Object value){
		List<Object> list = results.get(columnName);
		
		if (list == null){
			list = new ArrayList<Object>();
			results.put(columnName, list);
		}
		
		return list.add(value);
	}
	
	/**
	 * Appends a list recorded values to the list of values denoted by the given data source name. This can be used to build this {@link Run} instance.
	 * 
	 * @param columnName the data source name
	 * @param values the values to be appended.
	 * @return true as defined by {@link Collection#addAll(Collection)}
	 */
	public boolean addResults(final String columnName, final Collection<Object> values){
		List<Object> list = results.get(columnName);
		
		if (list == null){
			list = new ArrayList<Object>();
			results.put(columnName, list);
		}
		
		return list.addAll(values);
		
	}
	
	/**
	 * 
	 * Removes the first numberOfInitialResultsToIgnore values so that charts can be plotted without the initial transient of the simulation.
	 * @param numberOfInitialResultsToIgnore
	 * 
	 */
	public void ignoreInitialResults(final int numberOfInitialResultsToIgnore){
		for(String key : resultNames()){
			int j = 0;
			while((j < numberOfInitialResultsToIgnore) && (this.getResults(key).size() >= 30)){		//Buffer of 30, so that at least 30 time-steps will be charted
				this.getResults(key).remove(0);
				j++;
			}
		}
	}
	
	/**
	 * Returns the recorded data names. These are the column names in the recorder file, and they correspond to data source names as specified in the
	 * {@link Recorder} annotation.
	 * 
	 * @return the set of column names
	 */
	public Set<String> resultNames(){
		return results.keySet();
	}
	
	/**
	 * Returns the whole series recorded under the given name
	 * 
	 * @param columnName the name of the data column that should be returned
	 * @return a list of recorded values
	 */
	public List<Object> getResults(final String columnName){
		return results.get(columnName);
	}

	/**
	 * Returns the size of the list of results
	 * 
	 * @return an integer
	 */
	public int getResultsSize(){
		return results.size();
	}
	
	/**
	 * Returns tha parameter names.
	 * 
	 * @return the set of parameter names
	 */
	public Set<String> parameterNames(){
		return parameters.keySet();
	}
	
	/**
	 * Returns the parameter value that was set when running the simulation.
	 * 
	 * @param name the name of the parameter
	 * @return the value of the name parameter
	 */
	public Object getParameter(final String name){
		return parameters.get(name);
	}
}
