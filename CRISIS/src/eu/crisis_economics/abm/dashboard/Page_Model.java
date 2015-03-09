/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Ariel Y. Hoffman
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ai.aitia.meme.gui.Wizard;
import ai.aitia.meme.gui.Wizard.Button;
import ai.aitia.meme.gui.Wizard.IWizardPage;
import aurelienribon.ui.css.Style;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import eu.crisis_economics.utilities.Pair;

/**
 * @author Tamás Máhr
 *
 */
public class Page_Model implements IWizardPage, IIconsInHeader {

	private static final String CSS_CLASS_COMMON_PANEL = ".commonPanel";
	private static final int DECORATION_IMAGE_WIDTH = 600;
	private static final String DECORATION_IMAGE = "Decisions.png";
	private static final String TITLE = "Model";
	private static final String INFO_TEXT = "Please select the model to run!";
	private static final int STARTBUTTON_HEIGHT = 50;
	private static final int STARTBUTTON_WIDTH = 350;

	private Wizard wizard;
	private Dashboard dashboard;

	private Container container;
	
	public Page_Model(Wizard wizard, final Dashboard dashboard) {
		this.wizard = wizard;
		this.dashboard = dashboard;
		
		container = initContainer();
	}
	
	@Override
	public String getInfoText(Wizard wizard) {
		
		return wizard.getArrowsHeader(INFO_TEXT);
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("icons/bank.png"));
	}
	
	@Override
	public Container getPanel() {
//		ImageIcon buttonIcon = new ImageIcon(getClass().getResource("white-background-gold-button-hi.png"));
//		buttonIcon = new ImageIcon(buttonIcon.getImage().getScaledInstance(STARTBUTTON_WIDTH, -STARTBUTTON_HEIGHT, java.awt.Image.SCALE_SMOOTH));
//
//		int buttonHeight = buttonIcon.getIconHeight();
//		int buttonWidth = buttonIcon.getIconWidth();
		
		return container;
	}

	private Container initContainer() {
//		BufferedImage image = null;
//		try {
//			image = ImageIO.read(getClass().getResource(DECORATION_IMAGE));
//		} catch (IOException e) {
//			throw new IllegalStateException(e);
//		}
//		
//		ImageIcon imageIcon = new ImageIcon(image.getScaledInstance(DECORATION_IMAGE_WIDTH, -1, BufferedImage.SCALE_SMOOTH));
		
		JLabel label = null;
		label = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource(DECORATION_IMAGE)).getImage().getScaledInstance(DECORATION_IMAGE_WIDTH, -1, Image.SCALE_SMOOTH)));
//		label = new JLabel(imageIcon);
//		label.setOpaque(true);
		
      final DefaultFormBuilder
         buttonFormBuilder = FormsUtils.build("p ~ p", "");
      for (final Entry<String, Pair<String, Color>> record :
         Dashboard.availableModels.entrySet()) {
         final String
            modelURI = record.getKey(),
            modelName = record.getValue().getFirst();
         final Color
            buttonColor = record.getValue().getSecond();
         
			JButton button = new JButton(
			   "<html><p align='center'>" + modelName + "</html>");
			
			button.setPreferredSize(new Dimension(STARTBUTTON_WIDTH, STARTBUTTON_HEIGHT));
			button.setBackground(buttonColor);
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							IModelHandler modelHandler = null;
							try {
								modelHandler = dashboard.createModelHandler(modelURI);
								
							} catch (ClassNotFoundException e) {
								Logger log = Logger.getLogger(getClass());
								log.error("Could not load the model class!", e);
							}
							
							try {
								modelHandler.getRecorderAnnotationValue();
							} catch (Exception e) {
								ErrorDialog errorDialog = new ErrorDialog(wizard, "No recorder specified", "The model does not have '@RecorderSource' annotation");
								errorDialog.show();
								
								return;
							}
							
							dashboard.setTitle(modelName.substring(modelName.lastIndexOf('.') + 1));
							wizard.gotoPage(1);
						}
					});
				}
			});
			
			buttonFormBuilder.append(button);
		}
		final JButton loadButton = new JButton("<html><p align='center'>Load model configuration..." + "</html>");
		loadButton.setPreferredSize(new Dimension(STARTBUTTON_WIDTH, STARTBUTTON_HEIGHT));
		loadButton.setBackground(new Color(60, 193, 250));
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent _) {
				dashboard.loadConfiguration();
			}
		});
		
		buttonFormBuilder.append(loadButton);
		
		JPanel buttonPanel = buttonFormBuilder.getPanel();
		Style.registerCssClasses(buttonPanel, CSS_CLASS_COMMON_PANEL);
		
		JPanel panel = FormsUtils.build("p ~ f:p:g",
				"01 f:p:g",
				label, buttonPanel, CellConstraints.CENTER).getPanel();
		
//		panel.setBackground(Color.WHITE);
		Style.registerCssClasses(panel, CSS_CLASS_COMMON_PANEL);

		final JScrollPane pageScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pageScrollPane.setBorder(null);
		pageScrollPane.setViewportBorder(null);
		pageScrollPane.setViewportView(panel);

		return pageScrollPane;
	}

	@Override
	public boolean isEnabled(Button button) {
		switch (button){
		case BACK:
			return false;
			
		case NEXT:
			return false;
			
		case CANCEL: // this is the charts page
			return true;
			
		case FINISH:
			return false;
			
		case CUSTOM:
			return false;
		}
		return false;
	}

	@Override
	public boolean onButtonPress(Button button) {
		return true;
	}

	@Override
	public void onPageChange(boolean show) {
		if (show){
			dashboard.getSaveConfigMenuItem().setEnabled(false);
			dashboard.setTitle(null);
		} else {
			IModelHandler modelHandler = dashboard.getModelHandler();
			if (modelHandler != null){
				dashboard.setTitle(modelHandler.getModelClassSimpleName());
			}
		}
	}
	
	@Override
	public String getTitle() {
		return TITLE;
	}
}
