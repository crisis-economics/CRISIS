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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;

//----------------------------------------------------------------------------------------------------
@SuppressWarnings("serial")
public class ParameterBoxTreeRenderer extends DefaultTreeCellRenderer {
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value,	final boolean sel, final boolean expanded, final boolean leaf, final int row,
												  final boolean focus) {
		String tooltipText = null;
		
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		
		if (node.isRoot()) {
			return new JLabel("<html><b>All combinations of:</b></html>");
		}
		
		final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
		
		long multiplicity = 0;
		if (!(userObj.info instanceof SubmodelInfo))
			multiplicity = userObj.info.getMultiplicity();

		if (userObj.info.isFile()) {
			final File file = (File) userObj.info.getValue();
			if (file != null)
				tooltipText = file.getAbsolutePath();
		}
		
		setToolTipText(tooltipText);
		final JLabel label = (JLabel) super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,focus);
		label.setIcon(null);
		
		if (multiplicity > 0) {
			final JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(false);
			
			panel.add(new JLabel("<html><b>" + multiplicity + "x</b>&nbsp;</html>"),BorderLayout.WEST);
			panel.add(label,BorderLayout.CENTER);
			panel.setSize(new Dimension(tree.getSize().width,tree.getRowHeight()));
			
			return panel;
		}
		
		return label;
	}
}