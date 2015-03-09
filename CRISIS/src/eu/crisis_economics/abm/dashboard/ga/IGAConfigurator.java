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
package eu.crisis_economics.abm.dashboard.ga;

import java.util.Map;

import javax.swing.JPanel;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;

public interface IGAConfigurator {
	
	//====================================================================================================
	// methods
	
	
	//----------------------------------------------------------------------------------------------------
	/**
	 * 
	 * @return the operator's name.
	 */
	public String getName();

	//----------------------------------------------------------------------------------------------------
	/**
	 * 
	 * @return a short description of the operator.
	 */
	public String getDescription();

	//----------------------------------------------------------------------------------------------------
	/**
	 * 
	 * @return the operator's settings panel.
	 */
	public JPanel getSettingspanel();
	
	//----------------------------------------------------------------------------------------------------
	/**
	 * @return the operator's configuration map.
	 */
	public Map<String,String> getConfiguration();
	
	//----------------------------------------------------------------------------------------------------
	public void setConfiguration(final Map<String,String> configuration) throws ModelInformationException;
}