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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import eu.crisis_economics.abm.dashboard.ParameterOrGene;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.plugin.IIntelliContext;

//----------------------------------------------------------------------------------------------------
public class DashboardIntelliContext extends HashMap<Object,Object> implements IIntelliContext {
	
	//====================================================================================================
	// members
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4897579675057004811L;
	protected DefaultMutableTreeNode parameterTreeRootNode;
	protected DefaultTreeModel geneTreeModel;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public DashboardIntelliContext(final DefaultMutableTreeNode parameterTreeRootNode, final DefaultTreeModel geneTreeModel) {
		this.parameterTreeRootNode = parameterTreeRootNode;
		this.geneTreeModel = geneTreeModel; 
	}

	//----------------------------------------------------------------------------------------------------
	public List<ParameterInfo> getParameters() {
		final List<ParameterInfo> result = new ArrayList<ParameterInfo>();
		
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) geneTreeModel.getRoot();
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = root.preorderEnumeration();
		nodes.nextElement();
		
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
			if (userObj.getInfo() instanceof SubmodelInfo) continue;
			
			result.add(userObj.getInfo());
		}
		
		return result;
	}

	//----------------------------------------------------------------------------------------------------
	public DefaultMutableTreeNode getParameterTreeRootNode() { return parameterTreeRootNode; }

	//----------------------------------------------------------------------------------------------------
	public JButton getNewParametersButton() {
		throw new UnsupportedOperationException();
	}

	//----------------------------------------------------------------------------------------------------
	public File getPluginResourcesDirectory() {
		throw new UnsupportedOperationException();
	}
}