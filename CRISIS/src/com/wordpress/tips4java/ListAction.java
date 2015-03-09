/*
 * This file is part of CRISIS, an economics simulator.
 *
 * This code is not copyrighted, it is in the public domain.
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

package com.wordpress.tips4java;

import java.awt.event.*;
import javax.swing.*;

/*
 *	Add an Action to a JList that can be invoked either by using
 *  the keyboard or a mouse.
 *
 *  By default the Enter will will be used to invoke the Action
 *  from the keyboard although you can specify and KeyStroke you wish.
 *
 *  A double click with the mouse will invoke the same Action.
 *
 *  The Action can be reset at any time.
 */
/**
 * 
 * @author Rob Camick
 *
 */
public class ListAction implements MouseListener {
	
	private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

	private JList list;
	private KeyStroke keyStroke;

	/*
	 *	Add an Action to the JList bound by the default KeyStroke
	 */
	public ListAction(JList list, Action action) {
		this(list, action, ENTER);
	}

	/*
	 *	Add an Action to the JList bound by the specified KeyStroke
	 */
	public ListAction(JList list, Action action, KeyStroke keyStroke) {
		this.list = list;
		this.keyStroke = keyStroke;

		//  Add the KeyStroke to the InputMap

		InputMap im = list.getInputMap();
		im.put(keyStroke, keyStroke);

		//  Add the Action to the ActionMap

		setAction(action);

		//  Handle mouse double click

		list.addMouseListener(this);
	}

	/*
	 *  Add the Action to the ActionMap
	 */
	public void setAction(Action action) {
		list.getActionMap().put(keyStroke, action);
	}

	//  Implement MouseListener interface

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Action action = list.getActionMap().get(keyStroke);

			if (action != null) {
				ActionEvent event = new ActionEvent(
					list,
					ActionEvent.ACTION_PERFORMED,
					"");
				
				if (list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
					final int idx = list.locationToIndex(e.getPoint());
					list.setSelectedIndex(idx);
				}
				
				action.actionPerformed(event);
			}
		}
	}

 	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}
}