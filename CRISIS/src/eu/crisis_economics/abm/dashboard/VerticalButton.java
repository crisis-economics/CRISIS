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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
 
/**
*
* @author Michal
*/
public class VerticalButton extends JButton implements Cloneable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = -1410573686815926214L;
	
	//<editor-fold defaultstate="collapsed" desc="Clockwise">
    private boolean clockwise = false;

    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

    public boolean getClockwise() {
        return clockwise;
    }

    public boolean isClockwise() {
        return clockwise;
    }
    //</editor-fold>
 
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public VerticalButton() {
    }
 
    public VerticalButton(Action a) {
        super(a);
    }
 
    public VerticalButton(Icon icon) {
        super(icon);
    }
 
    public VerticalButton(String text) {
        super(text);
    }
 
    public VerticalButton(String text, Icon icon) {
        super(text, icon);
    }
 
    public VerticalButton(String text, boolean clockwise) {
        this(text);
        this.clockwise = clockwise;
    }
    //</editor-fold>
 
    @Override
    public Dimension getPreferredSize() {
        return rotate(super.getPreferredSize());
    }
 
    private Dimension rotate(Dimension d) {
        return new Dimension(d.height, d.width);
    }
 
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        VerticalButton clone = clone();
        clone.setSize(rotate(this.getSize()));
        if (clockwise) {
            g2.rotate(Math.PI / 2.0);
            g2.translate(0, -getSize().width);
        } else {
            g2.translate(0, getSize().height);
            g2.rotate(-Math.PI / 2.0);
        }
        clone.paintSuperComponent(g2);
        g2.dispose();
    }
 
    public void paintSuperComponent(Graphics g) {
        super.paintComponent(g);
    }
 
    @Override
    protected VerticalButton clone() {
        try {
            return (VerticalButton) super.clone();
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            return null;
        }
    }
 
}
