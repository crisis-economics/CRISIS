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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Tamás Máhr
 *
 */
public class PreferencesDialog extends JDialog {

	private static final String CHART_UPDATE_PERIOD_TOOLTIP = "The dashboard will wait the given amount of milliseconds between updateing the charts with new data. Set to 0 to disable waiting!";
	private static final String NUMBER_OF_RESULTS_TOOLTIP = "The dashboard will not display new results until this amount of result rows are collected. Set to 1 to immediately display every row.";
	private static final int MARGIN = 10;
	private static final int DIALOG_HEIGHT = 300;
	private static final int DIALOG_WIDTH = 650;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String LABEL_PREFERENCES = "Preferences";
	private static final String LABEL_WORKSPACE_DIRECTORY = "Workspace directory";
	private static final String LABEL_NUMBER_OF_RESULTS = "Number of result rows to display in one go";
	private static final String LABEL_CHART_UPDATE_PERIOD = "Chart update period (milliseconds)";
	private static final String BUTTON_BROWSE = "Browse";
	private static final String BUTTON_CANCEL = "Cancel";
	private static final String BUTTON_OK = "Save & Go";
	private JTextField workspaceField;
	
	private Preferences preferences = Preferences.userNodeForPackage(Dashboard.class);
	private JTextField numberOfResultsField;
	private JTextField chartUpdatePeriodField;

	
	public PreferencesDialog(final JFrame parent) {
		super(parent);
		
		JLabel preferencesLabel = new JLabel(LABEL_PREFERENCES);
		
		JLabel workspaceLabel = new JLabel(LABEL_WORKSPACE_DIRECTORY);
		workspaceField = new JTextField();
		JButton workspaceBrowseButton = new JButton(BUTTON_BROWSE);
		workspaceBrowseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WorkspaceSelectionDialog workspaceSelectionDialog = new WorkspaceSelectionDialog(PreferencesDialog.this);
				
				File workspace = workspaceSelectionDialog.display();
				workspaceField.setText(workspace.getAbsolutePath());
			}
		});
		
		JLabel numberOfResultsLabel = new JLabel(LABEL_NUMBER_OF_RESULTS);
		numberOfResultsLabel.setToolTipText(NUMBER_OF_RESULTS_TOOLTIP);
		numberOfResultsField = new JTextField();
		numberOfResultsField.setToolTipText(NUMBER_OF_RESULTS_TOOLTIP);
		
		JLabel chartUpdatePeriodLabel = new JLabel(LABEL_CHART_UPDATE_PERIOD);
		chartUpdatePeriodLabel.setToolTipText(CHART_UPDATE_PERIOD_TOOLTIP);
		chartUpdatePeriodField = new JTextField();
		chartUpdatePeriodField.setToolTipText(CHART_UPDATE_PERIOD_TOOLTIP);
		
		JButton cancelButton = new JButton(BUTTON_CANCEL);
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				PreferencesDialog.this.setVisible(false);
			}
		});
		
		JButton okButton = new JButton(BUTTON_OK);
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				preferences.put(Dashboard.WORKSPACE_PREFERENCE_NODE, workspaceField.getText());
				boolean error = false;
				try {
					if (!numberOfResultsField.getText().isEmpty()){
						Integer.parseInt(numberOfResultsField.getText());
					}
					preferences.put(Dashboard.NUMBER_OF_RESULTS_PREFERENCE_NODE, numberOfResultsField.getText());

					try {
						if (!chartUpdatePeriodField.getText().isEmpty()){
							Integer.parseInt(chartUpdatePeriodField.getText());
						}
						preferences.put(Dashboard.CHART_UPDATE_PERIOD_PREFERENCE_NODE, chartUpdatePeriodField.getText());
					} catch (NumberFormatException e){
						new ErrorDialog(PreferencesDialog.this, "Wrong number", "Please specify an integer number as the chart update period!").show();
						error = true;
					}
				} catch (NumberFormatException e){
					new ErrorDialog(PreferencesDialog.this, "Wrong number", "Please specify an integer number as the number of results to display in one go!").show();
					error = true;
				}
				
				if (!error){
					PreferencesDialog.this.setVisible(false);
				}
			}
		});
		
		JPanel panel = FormsUtils.build("l:p ~ f:p:g ~ l:p", 
				"000 p||" +
				"111 ||"+
				"234 t:p|"+
				"56_ |" +
				"78_ ||" +
				"999 b:p:g||" +
				"A_B b:p",
				preferencesLabel,
				new FormsUtils.Separator(""),
				workspaceLabel, workspaceField, workspaceBrowseButton,
				numberOfResultsLabel, numberOfResultsField,
				chartUpdatePeriodLabel, chartUpdatePeriodField,
				new FormsUtils.Separator(""),
				cancelButton, okButton
				).getPanel();
		
		panel.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
		add(panel);
		setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		
		pack();
		setLocationRelativeTo(parent);
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	@Override
	public void setVisible(boolean paramBoolean) {
		String workspaceString = preferences.get(Dashboard.WORKSPACE_PREFERENCE_NODE, null);

		workspaceField.setText(workspaceString);
		numberOfResultsField.setText(preferences.get(Dashboard.NUMBER_OF_RESULTS_PREFERENCE_NODE, null));
		chartUpdatePeriodField.setText(preferences.get(Dashboard.CHART_UPDATE_PERIOD_PREFERENCE_NODE, null));
		
		super.setVisible(paramBoolean);
	}
	
}
