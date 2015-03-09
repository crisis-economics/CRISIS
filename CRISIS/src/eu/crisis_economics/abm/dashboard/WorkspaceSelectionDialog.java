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
package eu.crisis_economics.abm.dashboard;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

/**
 * @author Tamás Máhr
 *
 */
public class WorkspaceSelectionDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2292634469863398018L;
	
	private static final int WORKSPACE_DIALOG_MESSAGE_MARGIN = 20;

	protected File workspace;


	/**
	 * @param owner
	 * @param title
	 */
	public WorkspaceSelectionDialog(Dialog owner) {
		super(owner, "Please select a workspace directory!", true);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		//fileChooser.setDialogTitle("Please select a workspace directory!");
		
		Box box = new Box(BoxLayout.PAGE_AXIS);
		box.setAlignmentX(CENTER_ALIGNMENT);
		
		box.add(Box.createVerticalStrut(WORKSPACE_DIALOG_MESSAGE_MARGIN));
		JLabel label = new JLabel("<html>&nbsp;&nbsp;&nbsp;<b>Please select a directory to store all data generated during the simulations (results, logs, etc.)!</b>&nbsp;&nbsp;&nbsp;</html>");
		label.setAlignmentX(CENTER_ALIGNMENT);
		box.add(label);
		box.add(Box.createVerticalStrut(WORKSPACE_DIALOG_MESSAGE_MARGIN));
		box.add(fileChooser);
		
		add(box);
		
		fileChooser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("CancelSelection")){
					workspace = null;
				}
				if (e.getActionCommand().equals("ApproveSelection")){
					workspace = ((JFileChooser)e.getSource()).getSelectedFile();
				}
				setVisible(false);
			}
		});
		
		pack();
	}

	public File display() {
		super.setVisible(true);
		
		return workspace;
	}
}
