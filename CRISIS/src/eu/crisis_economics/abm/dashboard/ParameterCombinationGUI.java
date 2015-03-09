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

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

//----------------------------------------------------------------------------------------------------
class ParameterCombinationGUI {
	
	//====================================================================================================
	// members
	
	private static final ImageIcon WARNING = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/warning.png")).getImage().
			 										getScaledInstance(Page_Parameters.ICON_WIDTH_AND_HEIGHT,Page_Parameters.ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));


	final JTree tree;
	final DefaultMutableTreeNode combinationRoot;
	final JLabel runDisplay;
	final JLabel warningDisplay;
	
	final JButton addButton;
	final JButton removeButton;
	final JButton upButton;
	final JButton downButton;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public ParameterCombinationGUI(final JTree tree, final DefaultMutableTreeNode combinationRoot, final JLabel runDisplay, final JLabel warningDisplay,
								   final JButton addButton, final JButton removeButton, final JButton upButton, final JButton downButton) {
		this.tree = tree;
		this.combinationRoot = combinationRoot;
		this.runDisplay = runDisplay;
		this.warningDisplay = warningDisplay;
		
		this.addButton = addButton;
		this.removeButton = removeButton;
		this.upButton = upButton;
		this.downButton = downButton;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void updateRunDisplay(final int runNumber) {
		String text = "<html><b>Number of runs:</b> ";
		text += String.valueOf(runNumber > 0 ? runNumber : 0);
		text += "</html>";
		
		runDisplay.setText(text);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setWarning(final boolean enabled) {
		Icon icon = null;
		String tooltip = null;
		if (enabled) {
			icon = WARNING;
			tooltip = "<html>There are two or more combination boxes that contains non-constant parameters.<br>"
					+ "Simulation will exit before all parameter values are assigned.</html>";
		} 
		
		warningDisplay.setIcon(icon);
		warningDisplay.setToolTipText(tooltip);
	}
}