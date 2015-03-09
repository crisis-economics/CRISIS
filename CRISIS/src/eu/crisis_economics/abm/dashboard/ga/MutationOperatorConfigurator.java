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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jgap.Configuration;
import org.jgap.GeneticOperator;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.MutationOperator;

import eu.crisis_economics.abm.dashboard.FormsUtils;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;

/**
 * @author Tamás Máhr
 *
 */
public class MutationOperatorConfigurator implements IGAOperatorConfigurator {


	private JSpinner mutationRateSpinner;
	private SpinnerNumberModel model = new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1);

	/** {@inheritDoc} 
	 */
	@Override
	public String getName() {
		return "Mutation operator";
	}

	/** {@inheritDoc} 
	 */
	@Override
	public String getDescription() {
		return "The mutation operator runs through the genes in each of the Chromosomes in the population and mutates them in statistical accordance to the given mutation rate. Mutated Chromosomes are then added to the list of candidate Chromosomes destined for the natural selection process.";
	}

	/** {@inheritDoc} 
	 */
	public GeneticOperator getConfiguredOperator(final Configuration config) throws InvalidConfigurationException {
		final int mutationRate = (Integer) model.getValue();
		return new MutationOperator(config,mutationRate);
	}
	public JPanel getSettingspanel() {
		mutationRateSpinner = new JSpinner(model);
		return FormsUtils.build("p ~ p p f:p:g", 
				"012_||" +
				"_333", 
				"Mutation rate", new JLabel("1 / "), mutationRateSpinner,
				"(Zero value disables mutation entirely.)").getPanel();
	}

	@Override
	public String toString(){
		return getName();
	}
	
	public Map<String,String> getConfiguration() {
		final Map<String,String> result = new HashMap<String,String>();
		result.put("mutationRate",String.valueOf(model.getValue()));
		
		return result;
	}
	
	public void setConfiguration(final Map<String,String> configuration) throws ModelInformationException {
		final String mutationRateStr = configuration.get("mutationRate");
		if (mutationRateStr == null)
			throw new ModelInformationException("Missing setting: mutationRate.");
		
		try {
			final int mutationRate = Integer.parseInt(mutationRateStr.trim());
			if (mutationRate < 0)
				throw new NumberFormatException();
			model.setValue(mutationRate);
		} catch (final NumberFormatException e) {
			throw new ModelInformationException("Invalid setting for 'mutationRate': " + mutationRateStr + ".");

		}
	}
}