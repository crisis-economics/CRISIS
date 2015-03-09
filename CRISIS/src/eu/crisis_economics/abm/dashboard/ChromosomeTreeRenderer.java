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
package eu.crisis_economics.abm.dashboard;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
public class ChromosomeTreeRenderer extends DefaultTreeCellRenderer {
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf,
												  final int row, final boolean hasFocus) {
		
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		
		if (node.isRoot()) 
			return new JLabel("");
		
		final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
		final JLabel label = (JLabel) super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
		
		if (userObj.isGene()) 
			label.setText("<html><b>" + label.getText() + "</b></html>");
		
		label.setIcon(null);
		
		return label;
	}
}