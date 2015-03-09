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

import java.awt.Color;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * @author Tamás Máhr
 *
 */
public class InfoHeaderLabel extends Box {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected JLabel textLabel;
	
	protected JLabel iconLabel;
	
	/**
	 * @param axis
	 */
	public InfoHeaderLabel(final String text, final Icon icon) {
		super(BoxLayout.X_AXIS);
		
		this.textLabel = new JLabel(text);
		this.iconLabel = new JLabel(icon);
		this.iconLabel.setOpaque(true);
		
		add(iconLabel);
		add(Box.createHorizontalStrut(5));
		add(textLabel);
	}
	
	@Override
	public void setForeground(Color fg) {
		textLabel.setForeground(fg);
		iconLabel.setBackground(fg);
	}

	@Override
	public void setFont(Font font) {
		textLabel.setFont(font);
	}
	
	public void setText(final String text){
		textLabel.setText(text);
	}
}
