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

/**
 * Classes implementing this interface should be able to provide height and width values in pixels. Used to determine the size of a {@link ScrollableJPanel}.
 * 
 * @author Tamás Máhr
 *
 */
public interface SizeProvider {
	/**
	 * Returns the height of e.g. a JPanel on the GUI.
	 * 
	 * @return the height in pixels
	 */
	int getHeight();
	
	/**
	 * Returns the width of e.g. a JPanel on the GUI.
	 * 
	 * @return the width in pixels
	 */
	int getWidth();
}
