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
package eu.crisis_economics.abm.dashboard.ga;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.jgap.BaseGene;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.ICompareToHandler;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.UnsupportedRepresentationException;

import com.google.common.base.Preconditions;

public class IdentifiableListGene extends BaseGene implements IIdentifiableGene {
	
	//====================================================================================================
	// members
	
	private static final long serialVersionUID = -4604894607355220213L;
	
	protected String id;
	protected Object value;
	protected List<Object> validValues = new ArrayList<Object>();
	
	//====================================================================================================
	// methods

	//----------------------------------------------------------------------------------------------------
	public IdentifiableListGene(final String id, final Configuration a_configuration) throws InvalidConfigurationException {
		super(a_configuration);
		
		Preconditions.checkNotNull(id);
		this.id = id;
	}
	
	//----------------------------------------------------------------------------------------------------
	public String getId() { return id; }
	
	//----------------------------------------------------------------------------------------------------
	public void addAllele(final Object allele) {
		validValues.add(allele);
	}

	//----------------------------------------------------------------------------------------------------
	public void setAllele(final Object a_newValue) {
		if (validValues.contains(a_newValue)) {
			value = a_newValue;
		} else if (a_newValue instanceof Number) {
			value = findClosestValue((Number)a_newValue);
		} else { 
	        throw new IllegalArgumentException("Allele value being set is not an element of the list of permitted values.");
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private Object findClosestValue(final Number a_newValue) {
		if (a_newValue instanceof Long) {
			return findClosestLongValue(a_newValue.longValue());
		} else if (a_newValue instanceof Double) {
			return findClosestDoubleValue(a_newValue.doubleValue());
		} else {
	        throw new IllegalArgumentException("Invalid value type: " + a_newValue.getClass());
		}
	}

	//----------------------------------------------------------------------------------------------------
	private Object findClosestLongValue(final long a_newValue) {
		int idx = -1;
		long minDistance = Long.MAX_VALUE;
		
		for (int i = 0; i < validValues.size(); ++i) {
			final long longValue = ((Number)validValues.get(i)).longValue();
			final long distance = Math.abs(longValue - a_newValue);
			if (distance < minDistance) {
				minDistance = distance;
				idx = i;
			}
		}
		
		if (idx >= 0) {
			return validValues.get(idx);
		}
		
        throw new IllegalArgumentException("Allele value being set is not an element of the list of permitted values.");
	}
	
	//----------------------------------------------------------------------------------------------------
	private Object findClosestDoubleValue(final double a_newValue) {
		int idx = -1;
		double minDistance = Long.MAX_VALUE;
		
		for (int i = 0; i < validValues.size(); ++i) {
			final double doubleValue = ((Number)validValues.get(i)).doubleValue();
			final double distance = Math.abs(doubleValue - a_newValue);
			if (distance < minDistance) {
				minDistance = distance;
				idx = i;
			}
		}
		
		if (idx >= 0) {
			return validValues.get(idx);
		}
		
        throw new IllegalArgumentException("Allele value being set is not an element of the list of permitted values.");
	}

	//----------------------------------------------------------------------------------------------------
	public String getPersistentRepresentation() throws UnsupportedOperationException {
		final Iterator<Object> it = validValues.iterator();
		final StringBuffer strbf = new StringBuffer();
		while (it.hasNext()) {
	    	strbf.append(PERSISTENT_FIELD_DELIMITER);
	    	strbf.append(it.next().toString());
	    }
	    
	    return value.toString() + strbf.toString();
	}

	//----------------------------------------------------------------------------------------------------
	public void setValueFromPersistentRepresentation(final String a_representation) throws UnsupportedOperationException, UnsupportedRepresentationException {
		if (a_representation != null) {
			final StringTokenizer tokenizer = new StringTokenizer(a_representation,PERSISTENT_FIELD_DELIMITER);
			
	        // Make sure the representation contains the correct number of
	        // fields. If not, throw an exception.
	        // -----------------------------------------------------------
	        if (tokenizer.countTokens() < 2) 
	          throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: it must contain at least two tokens.");
	        
	        final String valueRepresentation = tokenizer.nextToken();
	        
	        // First parse and set the representation of the value.
	        // ----------------------------------------------------
	        if (valueRepresentation.equals("null")) 
	        	value = null;
	        else {
	        	try {
	        		value = new Long(Long.parseLong(valueRepresentation));
	        	} catch (final NumberFormatException _) {
	        		try {
	        			value = new Double(Double.parseDouble(valueRepresentation));
	        		} catch (final NumberFormatException __) {
	        			throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: field 1 does not appear to" +
	        													     " be a number.");
	        			
	        		}
	        		
	        	}
	        }
	        
	        // Parse the potential categories.
	        // -------------------------------
	        Object allele;
	        while (tokenizer.hasMoreTokens()) {
	        	final String token = tokenizer.nextToken();
	        	try {
	        		allele = new Long(Long.parseLong(token));
	        		validValues.add(allele);
	        	} catch (final NumberFormatException _) {
	        		try {
	        			allele = new Double(Double.parseDouble(token));
	        		} catch (final NumberFormatException __) {
	        			throw new UnsupportedRepresentationException("The format of the given persistent representation is not recognized: a member of the list of" +
	        														 " eligible values does not appear to be a number.");
	        		}
	        	}
	        }
	   }
	}

	//----------------------------------------------------------------------------------------------------
	public void setToRandomValue(final RandomGenerator a_numberGenerator) {
	    value = validValues.get(a_numberGenerator.nextInt(validValues.size()));
	}

	//----------------------------------------------------------------------------------------------------
	public void applyMutation(final int index, final double a_percentage) {
	    RandomGenerator rn;
	    if (getConfiguration() != null) 
	    	rn = getConfiguration().getRandomGenerator();
	    else 
	    	rn = getConfiguration().getJGAPFactory().createRandomGenerator();
	    
	    setToRandomValue(rn);
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object a_other) {
		if (a_other instanceof IdentifiableListGene) {
			final IdentifiableListGene that = (IdentifiableListGene) a_other;
			return this.id.equals(that.id) && super.equals(that);
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------------------------------------
	public int compareTo(final Object a_other) {
		final IdentifiableListGene otherGene = (IdentifiableListGene) a_other;
		
		// First, if the other gene (or its value) is null, then this is
		// the greater allele. Otherwise, just use the overridden compareToNative
		// method to perform the comparison.
		// ---------------------------------------------------------------
		if (otherGene == null) 
			return 1;
		else if (otherGene.value == null) {
			// If our value is also null, then we're the same. Otherwise,
			// this is the greater gene.
			// ----------------------------------------------------------
			return value == null ? 0 : 1;
		} else {
			final ICompareToHandler handler = getConfiguration().getJGAPFactory().getCompareToHandlerFor(value,value.getClass());
			if (handler != null) {
				try {
					return ((Integer)handler.perform(value,null,otherGene.value)).intValue();
				} catch (final Exception ex) {
					throw new Error(ex);
				}
			} else
				return 0;
		}
	}

	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	@Override
	protected Object getInternalValue() { return value; }

	//----------------------------------------------------------------------------------------------------
	@Override
	protected Gene newGeneInternal() {
		try {
			final IdentifiableListGene result = new IdentifiableListGene(id,getConfiguration());
			result.value = value;
			result.validValues = new ArrayList<Object>(validValues);
			return result;
		} catch (final InvalidConfigurationException iex) {
			throw new IllegalStateException(iex.getMessage());
		}
	}
}