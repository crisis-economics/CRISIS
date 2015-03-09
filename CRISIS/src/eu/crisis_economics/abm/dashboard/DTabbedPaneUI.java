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

import java.awt.Component.BaselineResizeBehavior;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;

import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;

import com.seaglasslookandfeel.SeaGlassContext;
import com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI;

/**
 * @author Tamás Máhr
 *
 */
public class DTabbedPaneUI extends BasicTabbedPaneUI {

	SeaGlassTabbedPaneUI seaglassTabbedPaneUi = (SeaGlassTabbedPaneUI) SeaGlassTabbedPaneUI.createUI(null);

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
   public int hashCode() {
		return seaglassTabbedPaneUi.hashCode();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
   public boolean equals(Object obj) {
		return seaglassTabbedPaneUi.equals(obj);
	}

	/**
	 * @param c
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#installUI(javax.swing.JComponent)
	 */
	@Override
   public void installUI(JComponent c) {
		seaglassTabbedPaneUi.installUI(c);
	}

	/**
	 * @param c
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#uninstallUI(javax.swing.JComponent)
	 */
	@Override
   public void uninstallUI(JComponent c) {
		seaglassTabbedPaneUi.uninstallUI(c);
	}

	/**
	 * @param c
	 * @return
	 * @see javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
	 */
	@Override
   public Dimension getPreferredSize(JComponent c) {
		return seaglassTabbedPaneUi.getPreferredSize(c);
	}

	/**
	 * @param c
	 * @param x
	 * @param y
	 * @return
	 * @see javax.swing.plaf.ComponentUI#contains(javax.swing.JComponent, int, int)
	 */
	@Override
   public boolean contains(JComponent c, int x, int y) {
		return seaglassTabbedPaneUi.contains(c, x, y);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
   public String toString() {
		return seaglassTabbedPaneUi.toString();
	}

	/**
	 * @param c
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#getContext(javax.swing.JComponent)
	 */
	public SeaGlassContext getContext(JComponent c) {
		return seaglassTabbedPaneUi.getContext(c);
	}

	/**
	 * @param c
	 * @param state
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#getContext(javax.swing.JComponent, int)
	 */
	public SeaGlassContext getContext(JComponent c, int state) {
		return seaglassTabbedPaneUi.getContext(c, state);
	}

	/**
	 * @param c
	 * @return
	 * @see javax.swing.plaf.ComponentUI#getAccessibleChildrenCount(javax.swing.JComponent)
	 */
	@Override
   public int getAccessibleChildrenCount(JComponent c) {
		return seaglassTabbedPaneUi.getAccessibleChildrenCount(c);
	}

	/**
	 * @param c
	 * @param subregion
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#getContext(javax.swing.JComponent, javax.swing.plaf.synth.Region)
	 */
	public SeaGlassContext getContext(JComponent c, Region subregion) {
		return seaglassTabbedPaneUi.getContext(c, subregion);
	}

	/**
	 * @param c
	 * @param i
	 * @return
	 * @see javax.swing.plaf.ComponentUI#getAccessibleChild(javax.swing.JComponent, int)
	 */
	@Override
   public Accessible getAccessibleChild(JComponent c, int i) {
		return seaglassTabbedPaneUi.getAccessibleChild(c, i);
	}

	/**
	 * @param c
	 * @param tabIndex
	 * @param tabIsMousedOver
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#getCloseButtonState(javax.swing.JComponent, int, boolean)
	 */
	public int getCloseButtonState(JComponent c, int tabIndex, boolean tabIsMousedOver) {
		return seaglassTabbedPaneUi.getCloseButtonState(c, tabIndex, tabIsMousedOver);
	}

	/**
	 * @param e
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent e) {
		seaglassTabbedPaneUi.propertyChange(e);
	}

	/**
	 * @param g
	 * @param c
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#update(java.awt.Graphics, javax.swing.JComponent)
	 */
	@Override
   public void update(Graphics g, JComponent c) {
		seaglassTabbedPaneUi.update(g, c);
	}

	/**
	 * @param context
	 * @param g
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#paintBorder(javax.swing.plaf.synth.SynthContext, java.awt.Graphics, int, int, int, int)
	 */
	public void paintBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
		seaglassTabbedPaneUi.paintBorder(context, g, x, y, w, h);
	}

	/**
	 * @param g
	 * @param c
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#paint(java.awt.Graphics, javax.swing.JComponent)
	 */
	@Override
   public void paint(Graphics g, JComponent c) {
		seaglassTabbedPaneUi.paint(g, c);
	}

	/**
	 * @param c
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getMinimumSize(javax.swing.JComponent)
	 */
	@Override
   public Dimension getMinimumSize(JComponent c) {
		return seaglassTabbedPaneUi.getMinimumSize(c);
	}

	/**
	 * @param c
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getMaximumSize(javax.swing.JComponent)
	 */
	@Override
   public Dimension getMaximumSize(JComponent c) {
		return seaglassTabbedPaneUi.getMaximumSize(c);
	}

	/**
	 * @param c
	 * @param width
	 * @param height
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getBaseline(javax.swing.JComponent, int, int)
	 */
	@Override
   public int getBaseline(JComponent c, int width, int height) {
		return seaglassTabbedPaneUi.getBaseline(c, width, height);
	}

	/**
	 * @param c
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getBaselineResizeBehavior(javax.swing.JComponent)
	 */
	@Override
   public BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
		return seaglassTabbedPaneUi.getBaselineResizeBehavior(c);
	}

	/**
	 * @param tabPlacement
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#calculateMaxTabHeight(int)
	 */
	@Override
   public int calculateMaxTabHeight(int tabPlacement) {
		return seaglassTabbedPaneUi.calculateMaxTabHeight(tabPlacement);
	}

	/**
	 * @param tabPlacement
	 * @return
	 * @see com.seaglasslookandfeel.ui.SeaGlassTabbedPaneUI#calculateMaxTabWidth(int)
	 */
	@Override
   public int calculateMaxTabWidth(int tabPlacement) {
		return seaglassTabbedPaneUi.calculateMaxTabWidth(tabPlacement);
	}

	/**
	 * @param pane
	 * @param i
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getTabBounds(javax.swing.JTabbedPane, int)
	 */
	@Override
   public Rectangle getTabBounds(JTabbedPane pane, int i) {
		return seaglassTabbedPaneUi.getTabBounds(pane, i);
	}

	/**
	 * @param pane
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getTabRunCount(javax.swing.JTabbedPane)
	 */
	@Override
   public int getTabRunCount(JTabbedPane pane) {
		return seaglassTabbedPaneUi.getTabRunCount(pane);
	}

	/**
	 * @param pane
	 * @param x
	 * @param y
	 * @return
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#tabForCoordinate(javax.swing.JTabbedPane, int, int)
	 */
	@Override
   public int tabForCoordinate(JTabbedPane pane, int x, int y) {
		return seaglassTabbedPaneUi.tabForCoordinate(pane, x, y);
	}

}
