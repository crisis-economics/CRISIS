/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.dashboard.ga;

import java.util.Collections;
import java.util.Map;

import javax.swing.JPanel;

import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.jgap.NaturalSelector;
import org.jgap.impl.WeightedRouletteSelector;

/**
 * @author Tamás Máhr
 *
 */
public class WeightedRouletteSelectorConfigurator implements IGASelectorConfigurator {

	/** {@inheritDoc} 
	 */
	@Override
	public String getName() {
		return "Weighted roulette selector";
	}

	/** {@inheritDoc} 
	 */
	@Override
	public String getDescription() {
		return "This selector models a roulette wheel. When a Chromosome is added, it gets a number of 'slots' on the wheel equal to its fitness value. When the select method is invoked, the wheel is 'spun' and the Chromosome occupying the spot on which it lands is selected. Then the wheel is spun again and again until the requested number of Chromosomes have been selected. Since Chromosomes with higher fitness values get more slots on the wheel, there's a higher statistical probability that they'll be chosen, but it's not guaranteed.";
	}

	/** {@inheritDoc} 
	 */
	@Override
	public JPanel getSettingspanel() {
		return new JPanel();
	}

	@Override
   public String toString(){
		return getName();
	}

	//----------------------------------------------------------------------------------------------------
	public NaturalSelector getSelector(final Configuration config) throws InvalidConfigurationException {
		final WeightedRouletteSelector selector = new WeightedRouletteSelector(config);
		
		return selector;
	}
	
	//----------------------------------------------------------------------------------------------------
	public Map<String,String> getConfiguration() { return Collections.<String,String>emptyMap(); }
	
	//----------------------------------------------------------------------------------------------------
	public void setConfiguration(final Map<String,String> configuration) {}
}