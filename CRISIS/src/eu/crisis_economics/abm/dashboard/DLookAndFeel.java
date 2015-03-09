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

import javax.swing.UIDefaults;

import com.seaglasslookandfeel.SeaGlassLookAndFeel;

/**
 * @author Tamás Máhr
 *
 */
public class DLookAndFeel extends SeaGlassLookAndFeel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DLookAndFeel() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public UIDefaults getDefaults() {
		UIDefaults defaults = super.getDefaults();
		
        defaults.put("TabbedPaneUI", DTabbedPaneUI.class.getName());

		
		return defaults;
	}

}
