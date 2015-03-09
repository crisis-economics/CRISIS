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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXLabel;

import aurelienribon.ui.css.Style;

/**
 * @author Tamás Máhr
 *
 */
public class DescriptionPopupFactory {

	private static final int EXTENDED_POPUP_HEIGHT = 200;
	private static final int EXTENDED_POPUP_WIDTH = 540;
	private static final int POPUP_HEIGHT = 120;
	private static final int POPUP_WIDTH = 350;
	private static final int DEFAULT_POPUP_TEXT_LENGTH = 200;
	private static DescriptionPopupFactory instance;
	
	
	
	/**
	 * 
	 */
	private DescriptionPopupFactory() {
	}

	public static DescriptionPopupFactory getInstance(){
		if (instance == null){
			instance = new DescriptionPopupFactory();
		}
		
		return instance;
	}
	
   @SuppressWarnings("serial")
   public Popup getPopup(final Component owner, final String description, final Style cssStyle) {
		final PopupFactory popupFactory = PopupFactory.getSharedInstance();
		int periodIndex = description.indexOf('.');
		int exclamationMarkIndex = description.indexOf('!');
		int questionMarkIndex = description.indexOf('?');
		int endOfSentence = DEFAULT_POPUP_TEXT_LENGTH;
		
		if (periodIndex > 0 && periodIndex < endOfSentence){
			endOfSentence = periodIndex;
		}
		
		if (exclamationMarkIndex > 0 && exclamationMarkIndex < endOfSentence){
			endOfSentence = exclamationMarkIndex;
		}
		
		if (questionMarkIndex > 0 && questionMarkIndex < endOfSentence){
			endOfSentence = questionMarkIndex;
		}
		
		endOfSentence = Math.min(description.length(),endOfSentence);
		
		String firstSentence = endOfSentence == description.length() ? description : description.substring(0, endOfSentence + 1) + " ...";
		final JXLabel message = new JXLabel(firstSentence);
		message.setLineWrap(true);
		message.setBackground(Color.WHITE);
		
		JLabel status = new JLabel("<html><b>Press F2 key for more information</b></html>");
		status.setHorizontalAlignment(SwingConstants.RIGHT);
		
		final JPanel panel = new JPanel(new BorderLayout(10,10));
		panel.add(status, BorderLayout.SOUTH);
		panel.add(message, BorderLayout.CENTER);
		Style.registerCssClasses(panel, Dashboard.CSS_CLASS_COMMON_PANEL);

		panel.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT));
		panel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2, true), new EmptyBorder(5,5,5,5)));

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   final Point locationOnScreen = owner.getLocationOnScreen();
		
		int positionX = Math.max(locationOnScreen.x - 10, 0);

		if (positionX + POPUP_WIDTH > screenSize.width){
			positionX = screenSize.width - POPUP_WIDTH;
		}
		
		int positionY = locationOnScreen.y + 30;
		
		if (positionY + POPUP_HEIGHT > screenSize.height){
			positionY = locationOnScreen.y - 30 - POPUP_HEIGHT;
		}
		
		final Popup popup = popupFactory.getPopup(owner, panel, positionX, positionY);
		final MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				popup.hide();
				owner.removeMouseListener(this);
			}
		};
		owner.addMouseListener(mouseAdapter);
		
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), "extendedPopup");
		panel.getActionMap().put("extendedPopup", new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_F2){
					owner.removeMouseListener(mouseAdapter);
					popup.hide();
					
					final JXLabel message = new JXLabel(description);
					message.setLineWrap(true);
					message.setVerticalAlignment(SwingConstants.TOP);
					message.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JScrollPane scrollPane = new JScrollPane(message);
					scrollPane.setPreferredSize(new Dimension(EXTENDED_POPUP_WIDTH, EXTENDED_POPUP_HEIGHT));
					scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					
					JLabel status = new JLabel("<html><b>Press Esc key to close</b></html>");
					status.setHorizontalAlignment(SwingConstants.RIGHT);
					
					final JPanel panel2 = new JPanel(new BorderLayout(10,10));
					Style.registerCssClasses(panel2, Dashboard.CSS_CLASS_COMMON_PANEL);
					panel2.add(status, BorderLayout.SOUTH);
					panel2.add(scrollPane, BorderLayout.CENTER);
					
					panel2.setPreferredSize(new Dimension(EXTENDED_POPUP_WIDTH, EXTENDED_POPUP_HEIGHT));
					panel2.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2, true), new EmptyBorder(5,5,5,5)));

					int positionX = Math.max(locationOnScreen.x - 10, 0);
					
					if (positionX + EXTENDED_POPUP_WIDTH > screenSize.width){
						positionX = screenSize.width - EXTENDED_POPUP_WIDTH;
					}
					
					int positionY = locationOnScreen.y + 30;
					
					if (positionY + EXTENDED_POPUP_HEIGHT > screenSize.height){
						positionY = locationOnScreen.y - 30 - EXTENDED_POPUP_HEIGHT;
					}

					Style.apply(panel2, cssStyle);
					
					final Popup popup2 = popupFactory.getPopup(owner, panel2, positionX, positionY);
					
					panel2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "exitExtendedPopup");
					panel2.getActionMap().put("exitExtendedPopup", new AbstractAction() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							popup2.hide();
						}
					});
					
					popup2.show();
			}
		});
		

		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "exitPopup");
		panel.getActionMap().put("exitPopup", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popup.hide();
			}
		});

		Style.apply(panel, cssStyle);
		
		return popup;
	}
}
