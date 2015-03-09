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

import java.util.StringTokenizer;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.UnsupportedRepresentationException;
import org.jgap.impl.NumberGene;
import org.jgap.impl.StockRandomGenerator;

import com.google.common.base.Preconditions;

public class IdentifiableLongGene extends NumberGene implements IIdentifiableGene {
	
	//====================================================================================================
	// members

	private static final long serialVersionUID = -5829673649011898224L;

	protected final String id;
	protected long upperBound;
	protected long lowerBound;
	
	//====================================================================================================
	// members
	
	//----------------------------------------------------------------------------------------------------
	public IdentifiableLongGene(final String id, final Configuration a_config, final long lowerBound, final long upperBound) throws InvalidConfigurationException {
		super(a_config);
		Preconditions.checkNotNull(id);
		this.id = id;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	//----------------------------------------------------------------------------------------------------
	public String getId() { return id; }

	//----------------------------------------------------------------------------------------------------
	public String getPersistentRepresentation() {
		// The persistent representation includes the value, lower bound,
		// and upper bound. Each is separated by a colon.
		// --------------------------------------------------------------
		String s;
		if (getInternalValue() == null) 
			s = "null";
		else
			s = getInternalValue().toString();
		
		return s + PERSISTENT_FIELD_DELIMITER + lowerBound + PERSISTENT_FIELD_DELIMITER + upperBound;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setValueFromPersistentRepresentation(final String a_representation) throws UnsupportedRepresentationException {
		if (a_representation != null) {
			final StringTokenizer tokenizer = new StringTokenizer(a_representation,PERSISTENT_FIELD_DELIMITER);
			// Make sure the representation contains the correct number of
			// fields. If not, throw an exception.
			// -----------------------------------------------------------
			if (tokenizer.countTokens() != 3) {
				throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: it does not contain three tokens: "
															 + a_representation);
			}
			
			final String valueRepresentation = tokenizer.nextToken();
			final String lowerBoundRepresentation = tokenizer.nextToken();
			final String upperBoundRepresentation = tokenizer.nextToken();
			
			// First parse and set the representation of the value.
			// ----------------------------------------------------
			if (valueRepresentation.equals("null")) 
				setAllele(null);
			else {
				try {
					setAllele(new Long(Long.parseLong(valueRepresentation)));
				} catch (final NumberFormatException e) {
					throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: field 1 does not appear to be " +
																 "a long value.");
				}
			}
			
			// Now parse and set the lower bound.
			// ----------------------------------
			try {
				lowerBound = Long.parseLong(lowerBoundRepresentation);
			} catch (final NumberFormatException e) {
				throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: field 2 does not appear to be " +
															 "a long value.");
			}
			
			// Now parse and set the upper bound.
			// ----------------------------------
			try {
			    upperBound = Long.parseLong(upperBoundRepresentation);
			} catch (final NumberFormatException e) {
			    throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: field 3 does not appear to be " +
			    											 "a long value.");
			}
		}
	}

	//----------------------------------------------------------------------------------------------------
	public long longValue() {
	    return ((Long)getAllele()).longValue();
	}

	//----------------------------------------------------------------------------------------------------
	public void setToRandomValue(final RandomGenerator a_numberGenerator) {
		final double randomValue = (upperBound - lowerBound) * a_numberGenerator.nextDouble() + lowerBound;
		setAllele(new Long(Math.round(randomValue)));
	}

	//----------------------------------------------------------------------------------------------------
	public void applyMutation(final int a_index, final double a_percentage) {
		double range = (upperBound - lowerBound) * a_percentage;
	    if (getAllele() == null) 
	    	setAllele(new Long((long)range + lowerBound));
	    else {
	      final long newValue = Math.round(longValue() + range);
	      setAllele(new Long(newValue));
	    }
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
	    if (getInternalValue() == null) 
	    	return -1;
	    else 
	    	return super.hashCode();
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		String s = "IdentifiableLongGene(" + lowerBound + "," + upperBound + ")=";
	    if (getInternalValue() == null) 
	    	s += "null";
	    else 
	    	s += getInternalValue().toString();
	    
	    return s;
	}

	//----------------------------------------------------------------------------------------------------
	public long getLowerBound() { return lowerBound; }
	public long getUpperBound() { return upperBound; }
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object a_other) {
		if (a_other instanceof IdentifiableLongGene) {
			final IdentifiableLongGene that = (IdentifiableLongGene) a_other;
			return this.id.equals(that.id) && super.equals(that);
		}
		
		return false;
	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	protected Gene newGeneInternal() {
		try {
			final IdentifiableLongGene result = new IdentifiableLongGene(id,getConfiguration(),lowerBound,upperBound);
			return result;
		} catch (final InvalidConfigurationException iex) {
			throw new IllegalStateException(iex.getMessage());
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	protected int compareToNative(final Object a_o1, final Object a_o2) {
		return ((Long)a_o1).compareTo((Long)a_o2);
	}
	
	//----------------------------------------------------------------------------------------------------
	protected void mapValueToWithinBounds() {
		if (getAllele() != null) {
			final Long value = (Long) getAllele();
			// If the value exceeds either the upper or lower bounds, then
			// map the value to within the legal range. To do this, we basically
			// calculate the distance between the value and the long min,
			// determine how many bounds units that represents, and then add
			// that number of units to the upper bound.
			// -----------------------------------------------------------------
			if (value.longValue() > upperBound || value.longValue() < lowerBound) {
				RandomGenerator rn;
				if (getConfiguration() != null) 
					rn = getConfiguration().getRandomGenerator();
	        	else 
	        		rn = new StockRandomGenerator();
				
				if (upperBound == lowerBound) {
					setAllele(new Long(lowerBound));
	        } else 
	        	setToRandomValue(rn);
	        }
		}
	}
}