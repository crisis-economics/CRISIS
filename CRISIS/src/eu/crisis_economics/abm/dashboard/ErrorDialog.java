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

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXLabel;

class ErrorDialog extends JOptionPane {

	private static final int ERROR_DIALOG_WIDTH = 600;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JXLabel label = new JXLabel();
	private Component parent;
	
	private String title;

	private JDialog dialog;
	
	public ErrorDialog(final Component parent, final String title, final String message, final Object[] options) {
		super(null, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options);

		this.parent = parent;
		this.title = title;
		
		label.setText(message);
		label.setLineWrap(true);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setMaxLineSpan(ERROR_DIALOG_WIDTH);
		
		setMessage(label);
	}

	public ErrorDialog(final Component parent, final String title, final String message) {
		this(parent, title, message, null);
	}
	
	@Override
   public void show(){
		dialog = createDialog(parent, title);
		dialog.setVisible(true);
	}
}