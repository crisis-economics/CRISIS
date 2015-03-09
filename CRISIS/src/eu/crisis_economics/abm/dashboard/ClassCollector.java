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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;

//----------------------------------------------------------------------------------------------------
public class ClassCollector extends SwingWorker<ClassElement[],Object> {
	
	//====================================================================================================
	// members
	
	private final Page_Parameters owner;
	private final JComboBox combobox;
	private final SubmodelInfo info;
	private final Object selectedValue;
	private final boolean selectionBeforeListenerRegistration;
	
	//====================================================================================================
	// methods 
	
	//----------------------------------------------------------------------------------------------------
	public ClassCollector(final Page_Parameters owner, final JComboBox combobox, final SubmodelInfo info, final Object selectedValue, final boolean selectionBeforeListenerRegistration) {
		this.owner = owner;
		this.combobox = combobox;
		this.info = info;
		this.selectedValue = selectedValue; 
		this.selectionBeforeListenerRegistration = selectionBeforeListenerRegistration;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	protected ClassElement[] doInBackground() throws Exception {
		return owner.fetchPossibleTypes(info);
	}
	
	@Override
	protected void done() {
		try {
			ClassElement[] classElements = get();
			Object selectedValue = this.selectedValue; // redefine this to be able to set to the single possible classElement found
			if (classElements.length == 2) {
				selectedValue = classElements[1];
				combobox.setEnabled(false);
			}
			combobox.setModel(new DefaultComboBoxModel(classElements));
			if (selectionBeforeListenerRegistration)
				combobox.setSelectedItem(selectedValue);
			
			combobox.addActionListener(new ActionListener() {
				@Override
            public void actionPerformed(ActionEvent e) {
					if (combobox.getSelectedItem() instanceof ClassElement) {
						owner.showHideSubparameters(combobox,info);
					}
				}
			});
			combobox.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					if (combobox.getSelectedItem() instanceof ClassElement) {
						owner.showHideSubparameters(combobox,info);
					}
				}
			});
			combobox.setRenderer(new ClassElement.ClassElementComboBoxTooltipRenderer());
			if (!selectionBeforeListenerRegistration)
				combobox.setSelectedItem(selectedValue);
		} catch (final ExecutionException e) {
			String errorMsg = "Too much possibilities for " + info.getName() + ". Please narrow down the domain by " + 
							  "defining the possible classes in the @Submodel annotation.";
			JOptionPane.showMessageDialog(owner.getWizard(), new JLabel(errorMsg), "Error while analysing model", JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(owner.getWizard(), new JLabel(e.getMessage()), "Error while analyizing model", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}