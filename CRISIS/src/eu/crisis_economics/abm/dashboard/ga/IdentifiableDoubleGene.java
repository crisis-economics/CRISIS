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

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DoubleGene;

import com.google.common.base.Preconditions;

public class IdentifiableDoubleGene extends DoubleGene implements IIdentifiableGene {
	
	//====================================================================================================
	// members
	
	private static final long serialVersionUID = -1960035647721955619L;
	protected final String id;
	
	//====================================================================================================
	// methods

	//----------------------------------------------------------------------------------------------------
	public IdentifiableDoubleGene(final String id, final Configuration a_config, final double a_lowerBound, final double a_upperBound)
																																throws InvalidConfigurationException {
		super(a_config,a_lowerBound,a_upperBound);
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	//----------------------------------------------------------------------------------------------------
	public String getId() { return id; }
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object a_other) {
		if (a_other instanceof IdentifiableDoubleGene) {
			final IdentifiableDoubleGene that = (IdentifiableDoubleGene) a_other;
			return this.id.equals(that.id) && super.equals(that);
		}
		
		return false;
	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	protected Gene newGeneInternal() {
		try {
			final IdentifiableDoubleGene result = new IdentifiableDoubleGene(id,getConfiguration(),getLowerBound(),getUpperBound());
			return result;
		} catch (final InvalidConfigurationException iex) {
			throw new IllegalStateException(iex.getMessage());
		}
	}
}