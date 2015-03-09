/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
import org.jgap.GeneticOperator;
import org.jgap.InvalidConfigurationException;

import eu.crisis_economics.abm.dashboard.ga.operators.GeneAveragingCrossover;

/**
 * @author Tamás Máhr
 *
 */
public class GeneAveragingCrossoverOperatorConfigurator implements IGAOperatorConfigurator {

	/** {@inheritDoc} 
	 */
	@Override
	public String getName() {
		return "Gene averaging crossover operator";
	}

	/** {@inheritDoc} 
	 */
	@Override
	public String getDescription() {
		return "The crossover operator randomly selects two Chromosomes from the population and 'mates' them by averaging the values of the numerical genes. The new Chromosome with the average gene values is then added to the list of candidate Chromosomes.";
	}

	/** {@inheritDoc} 
	 */
	@Override
	public JPanel getSettingspanel() {
		return new JPanel();
	}

	/** {@inheritDoc} 
	 */
	public GeneticOperator getConfiguredOperator(final Configuration config) throws InvalidConfigurationException {
		GeneAveragingCrossover operator = new GeneAveragingCrossover(config, 1.);
		
		return operator;
	}

	@Override
   public String toString(){
		return getName();
	}
	
	public Map<String,String> getConfiguration() { return Collections.<String,String>emptyMap(); }
	public void setConfiguration(final Map<String,String> configuration) {}
}
