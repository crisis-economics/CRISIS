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

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Tamás Máhr
 *
 */
public class Breadcrumb extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1313920687321911771L;

	public Breadcrumb() {
		super(new FlowLayout(FlowLayout.LEFT));
	}

	public void setRoot(final String name) {
		removeAll();
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label);
	}
	
	public void setPath(final String root, final String ... components) {
		if (components.length > 0) {
			setRoot(root + " > ");
		} else {
			setRoot(root);
		}
		
		for (int i = 0; i < components.length; i++) {
			if (i < components.length - 1) {
				JLabel label = new JLabel(components[i] + " > ");
				label.setHorizontalAlignment(SwingConstants.LEFT);
				add(label);
			} else {
				JLabel label = new JLabel(components[i]);
				label.setHorizontalAlignment(SwingConstants.LEFT);
				add(label);
			}
		}
		
		validate();
		repaint();
	}
}
