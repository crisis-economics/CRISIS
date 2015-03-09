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
package eu.crisis_economics.abm.dashboard.ga;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.jgap.NaturalSelector;
import org.jgap.impl.BestChromosomesSelector;

import eu.crisis_economics.abm.dashboard.FormsUtils;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;

public class BestChromosomeSelectorConfigurator implements IGASelectorConfigurator {

	private JSlider originalRateSlider;
//	private JCheckBox doubletteCheckBox;

	private JTextField originalRateValueField;
	
	private DefaultBoundedRangeModel model = new DefaultBoundedRangeModel(50, 1, 0, 100);
	
	@Override
	public String getName() {
		return "Best chromosome selector";
	}

	@Override
	public String getDescription() {
		return "Takes the top n chromosomes into the next generation. n can be specified. Which chromosomes are the best is decided by evaluating their fitness value.";
	}

	@Override
	public JPanel getSettingspanel() {
		originalRateSlider = new JSlider(model);
		originalRateSlider.setMajorTickSpacing(100);
		originalRateSlider.setMinorTickSpacing(10);
		originalRateSlider.setPaintTicks(true);
		originalRateSlider.setPaintLabels(true);
		originalRateValueField = new JTextField("50");
		originalRateValueField.setPreferredSize(new Dimension(40,30));
		originalRateSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (originalRateSlider.hasFocus())
					originalRateValueField.setText(String.valueOf(originalRateSlider.getValue()));
			}
		});
		originalRateValueField.getDocument().addDocumentListener(new DocumentListener() {
			private Popup errorPopup;
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				textFieldChanged();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				textFieldChanged();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				textFieldChanged();
			}
			
			private void textFieldChanged(){
				if (!originalRateValueField.hasFocus()){
					return;
				}
				
				try {
					int value = Integer.parseInt(originalRateValueField.getText());
					originalRateSlider.setValue(value);
					
					if (errorPopup != null){
						errorPopup.hide();
						errorPopup = null;
					}
				} catch (NumberFormatException ex){
					PopupFactory popupFactory = PopupFactory.getSharedInstance();
					Point locationOnScreen = originalRateValueField.getLocationOnScreen();
					JLabel message = new JLabel("Please specify an integer number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					errorPopup = popupFactory.getPopup(originalRateValueField, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					
					errorPopup.show();
				}
			}
		});

//		doubletteCheckBox = new JCheckBox();
		
		return FormsUtils.build("p ~ p p p", 
				"0123 |"
//				+"4555" 
				,new JLabel("Rate of original population to survive"), originalRateValueField, new JLabel("%"), originalRateSlider
//				,new JLabel("Doublettes are allowed"), doubletteCheckBox
				).getPanel();
	}

	@Override
   public String toString(){
		return getName();
	}

	//----------------------------------------------------------------------------------------------------
	public NaturalSelector getSelector(final Configuration config) throws InvalidConfigurationException {
		final BestChromosomesSelector selector = new BestChromosomesSelector(config,model.getValue() / 100.0);
		selector.setDoubletteChromosomesAllowed(false);
		//selector.setDoubletteChromosomesAllowed(doubletteCheckBox.isSelected());
		
		return selector;
	}
	
	//----------------------------------------------------------------------------------------------------
	public Map<String,String> getConfiguration() {
		final Map<String,String> result = new HashMap<String,String>();
		result.put("originalRate",String.valueOf(model.getValue()));
		//result.put("doublettesAllowed",String.valueOf(doubletteCheckBox.isSelected()));
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setConfiguration(final Map<String,String> configuration) throws ModelInformationException {
		final String originalRateStr = configuration.get("originalRate");
		if (originalRateStr == null)
			throw new ModelInformationException("Missing setting: originalRate.");
		
		try {
			final int originalRate = Integer.parseInt(originalRateStr.trim());
			if (originalRate < 0 || originalRate > 100)
				throw new NumberFormatException();
			model.setValue(originalRate);
		} catch (final NumberFormatException e) {
			throw new ModelInformationException("Invalid setting for 'originalRate': " + originalRateStr + ".");
		}
		
//		final String doubletteStr = configuration.get("doublettesAllowed");
//		if (doubletteStr == null)
//			throw new ModelInformationException("Missing setting: doublettesAllowed.");
//		
//		doubletteCheckBox.setSelected(Boolean.parseBoolean(doubletteStr));
	}
}
