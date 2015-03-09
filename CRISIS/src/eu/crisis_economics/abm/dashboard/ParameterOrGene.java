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
package eu.crisis_economics.abm.dashboard;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import ai.aitia.meme.paramsweep.gui.info.MasonChooserParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.intellisweepPlugin.utils.ga.GeneInfo;

public class ParameterOrGene implements Serializable {

	//====================================================================================================
	// members
	
	private static final long serialVersionUID = -7139706205937012292L;

	protected final ParameterInfo info;
	protected boolean gene;
	protected GeneInfo geneInfo;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public ParameterOrGene(final ParameterInfo info) {
		this.info = info;
		this.gene = false;
		this.geneInfo = null;
	}
	
	//----------------------------------------------------------------------------------------------------
	public ParameterOrGene(final ParameterInfo info, final Double min, final Double max) {
		this.info = info;
		setGene(min,max);
	}
	
	//----------------------------------------------------------------------------------------------------
	public ParameterOrGene(final ParameterInfo info, final Long min, final Long max) {
		this.info = info;
		setGene(min,max);
	}
	
	//----------------------------------------------------------------------------------------------------
	public ParameterOrGene(final ParameterInfo info, final List<Object> values) {
		this.info = info;
		setGene(values);
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean isGene() { return gene; }
	public GeneInfo getGeneInfo() { return geneInfo; }
	public ParameterInfo getInfo() { return info; }
	
	//----------------------------------------------------------------------------------------------------
	public void setGene(final Double min, final Double max) {
		this.gene = true;
		this.geneInfo = new GeneInfo(info.getName(),min,max,info.getType(),info.getJavaType());
		this.geneInfo.setIntegerVals(false);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setGene(final Long min, final Long max) {
		this.gene = true;
		this.geneInfo = new GeneInfo(info.getName(),min,max,info.getType(),info.getJavaType());
		this.geneInfo.setIntegerVals(true);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setGene(final List<Object> values) {
		this.gene = true;
		this.geneInfo = new GeneInfo(info.getName(),values,info.getType(),info.getJavaType());
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setConstant(final Object value) {
		this.info.setValue(value);
		this.gene = false;
		this.geneInfo = null;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		final String humanName = info.getName().replaceAll("([A-Z])", " $1");
		
		if (info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) info;
			return humanName + " : " + (sInfo.getActualType() != null ? sInfo.getActualType().getSimpleName() : sInfo.getJavaType().getSimpleName());
		}
		
		String result = humanName + " : " + info.getType() + " - [";
		if (gene) {
			if (GeneInfo.INTERVAL.equals(geneInfo.getValueType())) 
				result += "min=" + geneInfo.getMinValue().toString() + ", max=" + geneInfo.getMaxValue().toString();
			else {
				result += "list=";
				for (int i = 0;i < geneInfo.getValueRange().size();++i) {
					 if  (i != 0) 
						 result += " ";
					 result += geneInfo.getValueRange().get(i).toString();
				 }
			}
		} else if (info.getValue() != null) {
			result += "value=";
			if (info.isFile()) {
				 final File file = (File) info.getValue();
				 if (file != null)
					 result += file.getAbsolutePath();
			} else if (info instanceof MasonChooserParameterInfo) {
				final MasonChooserParameterInfo mpInfo = (MasonChooserParameterInfo) info;
				result += mpInfo.getValidStrings()[((Integer)info.getValue())];
			} else
				result += info.getValue().toString();
		}
		
		result += "]";
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ParameterOrGene) {
			final ParameterOrGene that = (ParameterOrGene) obj;
			return this.info.equals(that.info);
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return info.hashCode();
	}
}