/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

//----------------------------------------------------------------------------------------------------
@SuppressWarnings("serial")
public class ScrollableJPanel extends JPanel implements Scrollable {
	
	//====================================================================================================
	// members
	
	private final SizeProvider page;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public ScrollableJPanel(final LayoutManager layout, final SizeProvider page) {
		super(layout);
		this.page = page;
	}

	public ScrollableJPanel(final SizeProvider page) {
		super();
		this.page = page;
	}

	//----------------------------------------------------------------------------------------------------
	@Override
   public Dimension getPreferredScrollableViewportSize() {
		final int width = page.getWidth();
		final int height = page.getHeight();
		
		return new Dimension(width,height);
	}

	//----------------------------------------------------------------------------------------------------
	@Override
   public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) { return 20; }
	@Override
   public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) { return 40; }
	@Override
   public boolean getScrollableTracksViewportWidth() { return false; }
	@Override
   public boolean getScrollableTracksViewportHeight() { return false; }
}