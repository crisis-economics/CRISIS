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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.gui.info.MasonChooserParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.intellisweepPlugin.utils.ga.GeneInfo;
import ai.aitia.meme.paramsweep.internal.platform.PlatformSettings;
import ai.aitia.meme.paramsweep.plugin.IStatisticsPlugin;
import ai.aitia.meme.paramsweep.utils.Utilities;
import ai.aitia.meme.utils.GUIUtils;
import ai.aitia.meme.utils.Utils;
import aurelienribon.ui.css.Style;

import com.jgoodies.forms.layout.CellConstraints;

import eu.crisis_economics.abm.dashboard.GASearchPanelModel.FitnessFunctionDirection;
import eu.crisis_economics.abm.dashboard.GASearchPanelModel.ModelListener;
import eu.crisis_economics.abm.dashboard.Page_Parameters.ComboBoxIsTooFullException;
import eu.crisis_economics.abm.dashboard.ga.IGAConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IGAOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IGASelectorConfigurator;

/**
 * @author Tamás Máhr
 *
 */
public class GASearchPanel extends JPanel {
	
	//====================================================================================================
	// members
   
	private static final long serialVersionUID = 7957280032992002080L;
	
	private static final String NUMBER_OF_TURNS_IN_A_SIMULATION = "Number of turns in a simulation";
	
	private static final String GENE_SETTINGS_INT_RANGE_VALUE_PANEL = "int_range";
	private static final String GENE_SETTINGS_DOUBLE_RANGE_VALUE_PANEL = "double_range";
	private static final String GENE_SETTINGS_LIST_VALUE_PANEL = "list";
	private static final String PARAM_SETTINGS_CONSTANT_VALUE_PANEL = "constant";
	private static final String PARAM_SETTINGS_ENUM_VALUE_PANEL = "enum";
	private static final String PARAM_SETTINGS_FILE_VALUE_PANEL = "file";
	private static final String PARAM_SETTINGS_SUBMODEL_VALUE_PANEL = "submodel";
	
	private static final String DESCRIPTION_PANEL = "Description";
	private static final String OPERATOR_SETTINGS_PANEL_DEFAULT_PANEL = "DEFAULT";
	private static final String OPERATOR_SETTINGS_PANEL_TITLE_POSTFIX = "Operator settings";
	
	private static final Logger log = LoggerFactory.getLogger(GASearchPanel.class);
	private static final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	
	static { // print double numbers rounded to 2 decimal digits using US format
		numberFormatter.setMaximumFractionDigits(2);
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setGroupingUsed(false);
	}
	
	private Page_Parameters parentPage;
	private JTextField numberOfTurnsField;
	private JSpinner populationSizeField;
	private JTextField populationRandomSeedField;
	private JRadioButton numberOfGenerationsRadioButton;
	private JSpinner numberOfGenerationsField;
	private JRadioButton fitnessLimitRadioButton;
	private JTextField fitnessLimitField;
	private JRadioButton fitnessMinimizeRadioButton;
	private JRadioButton fitnessMaximizeRadioButton;
	private JComboBox fitnessFunctionList;
	private JComboBox fitnessStatistics;
	private JSpinner fitnessTimeSeriesLength;
	private JTree geneTree;
	private JLabel numberLabel;
	protected Popup errorPopup;
	private JTextField constantField;
	private JTextField intMinField;
	private JTextField intMaxField;
	private JTextField doubleMinField;
	private JTextField doubleMaxField;
	private JTextArea valuesArea;
	private JComboBox enumDefBox;
	private JComboBox submodelTypeBox;
	private JTextField fileTextField;
	private JButton fileBrowseButton;

	private JRadioButton constantRButton;
	private JRadioButton doubleGeneValueRangeRButton;
	private JRadioButton intGeneValueRangeRButton;
	private JRadioButton listGeneValueRButton;

	private JPanel geneSettingsValuePanel;
	private JXLabel descriptionLabel;
	private JButton modifyButton;
	
	private DefaultMutableTreeNode editedNode;

	private JButton cancelButton;

	private JScrollPane mainScrPane;

	private JPanel operatorSettingsPanel;

	private CheckList selectorList;

	private CheckList operatorList;

	private GASearchPanelModel model;

	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public GASearchPanel(final GASearchPanelModel model, final Page_Parameters parentPage) {
		this.model = model;
		this.parentPage = parentPage;
		initSearchPanel(model);
	}
	
	//----------------------------------------------------------------------------------------------------
	public double getNumberOfRequestedTurns() {
		return Double.parseDouble(numberOfTurnsField.getText());
	}
	
	//----------------------------------------------------------------------------------------------------
	public void reset(final GASearchPanelModel model) {
		numberOfTurnsField.setText(double2String(model.getNumberOfTurns()));
		populationSizeField.setValue(model.getPopulationSize());
		populationRandomSeedField.setText(String.valueOf(model.getPopulationRandomSeed()));
		
		// create the stopping conditions panel
		numberOfGenerationsRadioButton.setSelected(true);
		numberOfGenerationsField.setValue(model.getNumberOfGenerations());
		fitnessLimitField.setText(String.valueOf(model.getFitnessLimitCriterion()));
		
		if (model.getNumberOfGenerations() < 0) {
			fitnessLimitRadioButton.setSelected(true);
			numberOfGenerationsField.setEnabled(false);
		} else {
			fitnessLimitField.setEnabled(false);
			numberOfGenerationsRadioButton.setSelected(true);
		}
		
		if (model.getFitnessFunctionDirection() == FitnessFunctionDirection.MINIMIZE) 
			fitnessMinimizeRadioButton.setSelected(true);
		else 
			fitnessMaximizeRadioButton.setSelected(true);
		
		fitnessFunctionList.setSelectedItem(model.getSelectedFitnessFunction());
		fitnessStatistics.setSelectedItem(model.getSelectedFitnessStatistics());
		fitnessTimeSeriesLength.setValue(model.getFitnessTimeSeriesLength());
		
		final CardLayout settingslayout = (CardLayout) operatorSettingsPanel.getLayout();
		settingslayout.show(operatorSettingsPanel,OPERATOR_SETTINGS_PANEL_DEFAULT_PANEL);
		((TitledBorder)operatorSettingsPanel.getBorder()).setTitle(OPERATOR_SETTINGS_PANEL_TITLE_POSTFIX);
		descriptionLabel.setText("");
		
		final List<IGASelectorConfigurator> selectionOperators = model.getSelectionOperators();
		final List<IGASelectorConfigurator> selectedSelectionOperators = model.getSelectedSelectionOperators();
		
		int i = 0;
		for (final IGASelectorConfigurator selectionOperator : selectionOperators) {
			selectorList.setChecked(i,selectedSelectionOperators.contains(selectionOperator));
			++i;
		}
		selectorList.clearSelection();

		final List<IGAOperatorConfigurator> geneticOperators = model.getGeneticOperators();
		final List<IGAOperatorConfigurator> selectedGeneticOperators = model.getSelectedGeneticOperators();

		i = 0;
		for (final IGAOperatorConfigurator operator : geneticOperators){
			operatorList.setChecked(i,selectedGeneticOperators.contains(operator));
			++i;
		}
		operatorList.clearSelection();

		numberLabel.setText("<html><b>Number of genes:</b> 0</html>");
		
		for (int j = 0;j < geneTree.getRowCount();++j) 
			geneTree.expandRow(j);
		
		resetSettings();
		enableDisableSettings(false);
	}
	
	//----------------------------------------------------------------------------------------------------
//	public void setSelectedFitnessFunction(final GASearchPanelModel gaSearchHandler) {
//		fitnessFunctionList.setSelectedItem(gaSearchHandler.getSelectedFitnessFunction());
//	}
	
	
	//====================================================================================================
	// assistant methods
		
	//----------------------------------------------------------------------------------------------------
	private void initSearchPanel(final GASearchPanelModel model) {
		model.addModelListener(new ModelListener() {
			public void fitnessFunctionAdded() {
				fitnessFunctionList.removeAllItems();
				final List<?> list = model.getFitnessFunctions();
				for (final Object fitnessFunction : list) {
					fitnessFunctionList.addItem(fitnessFunction);
				}				
			}
			
			public void fitnessFunctionsRemoved() {
				fitnessFunctionList.removeAllItems();
			}

			public void parameterAdded() {
				final DefaultTreeModel treeModel = model.getChromosomeTree();
				final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
 				geneTree.expandPath(new TreePath(treeModel.getPathToRoot(root)));
			}

			public void parametersRemoved() {
				parameterAdded(); // :)
			}
		});
		
		numberOfTurnsField = new JTextField(double2String(model.getNumberOfTurns()));
		numberOfTurnsField.setInputVerifier(new InputVerifier() {

			public boolean verify(final JComponent input) {
				if (errorPopup != null) {
					errorPopup.hide();
					errorPopup = null;
				}

				try {
					final double value = Double.parseDouble(numberOfTurnsField.getText());
					model.setNumberOfTurns(value);
					return true;
				} catch (final NumberFormatException e) {
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = numberOfTurnsField.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify a (possibly floating point) number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					errorPopup = popupFactory.getPopup(numberOfTurnsField, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					errorPopup.show();
					
					return false;
				}
			}
		});
		
		final JPanel numberOfTurnsPanel = FormsUtils.build("p ' f:p:g", "01", NUMBER_OF_TURNS_IN_A_SIMULATION, numberOfTurnsField).getPanel();
		
		// create the population parameters panel
		populationSizeField = new JSpinner(new SpinnerNumberModel(model.getPopulationSize(),2,Integer.MAX_VALUE,1));
		populationSizeField.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				model.setPopulationSize((Integer)((JSpinner)e.getSource()).getValue());
			}
		});
		
		populationRandomSeedField = new JTextField(String.valueOf(model.getPopulationRandomSeed()));
		populationRandomSeedField.setInputVerifier(new InputVerifier() {
			
			public boolean verify(final JComponent input) {
				if (errorPopup != null) {
					errorPopup.hide();
					errorPopup = null;
				}
				
				try {
					final int value = Integer.parseInt(populationRandomSeedField.getText());
					model.setPopulationRandomSeed(value);
					return true;
				} catch (final NumberFormatException e) {
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = populationRandomSeedField.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify an integer number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					errorPopup = popupFactory.getPopup(populationRandomSeedField, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					errorPopup.show();
					
					return false;					
				}
			}
		});
		
		final JPanel populationPanel = FormsUtils.build("p ~ f:p:g", 
				"01 ||"+
				"23", 
				"Size",populationSizeField,
				"Random seed", populationRandomSeedField).getPanel();
		populationPanel.setBorder(BorderFactory.createTitledBorder(null,"Population",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(populationPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		// create the stopping conditions panel
		numberOfGenerationsRadioButton = new JRadioButton("Number of generations");
		numberOfGenerationsField = new JSpinner(new SpinnerNumberModel(model.getNumberOfGenerations(),1,Integer.MAX_VALUE,1));
		numberOfGenerationsField.addChangeListener(new ChangeListener() {
			
			public void stateChanged(final ChangeEvent e) {
				model.setNumberOfGenerations((Integer)((JSpinner)e.getSource()).getValue());				
			}
		});
		numberOfGenerationsRadioButton.addChangeListener(new ChangeListener() {
			
			public void stateChanged(final ChangeEvent e) {
				if (numberOfGenerationsRadioButton.isSelected()) {
					numberOfGenerationsField.setEnabled(true);
					model.setFixNumberOfGenerations(true);
				} else 
					numberOfGenerationsField.setEnabled(false);
			}
		});
		
		fitnessLimitRadioButton = new JRadioButton("Fitness limit");
		fitnessLimitField = new JTextField(String.valueOf(model.getFitnessLimitCriterion()));
		fitnessLimitField.setInputVerifier(new InputVerifier() {
			
			public boolean verify(final JComponent input) {
				if (errorPopup != null){
					errorPopup.hide();
					errorPopup = null;
				}

				try {
					final double value = Double.parseDouble(fitnessLimitField.getText());
					if (value < 0)
						throw new NumberFormatException();
					model.setFitnessLimitCriterion(value);
					return true;
				} catch (final NumberFormatException e) {
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = fitnessLimitField.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify a non-negative (possibly floating point) number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					errorPopup = popupFactory.getPopup(fitnessLimitField, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					errorPopup.show();
					
					return false;
				}
			}
		});
		fitnessLimitRadioButton.addChangeListener(new ChangeListener() {

			public void stateChanged(final ChangeEvent e) {
				if (fitnessLimitRadioButton.isSelected()) {
					fitnessLimitField.setEnabled(true);
					model.setFixNumberOfGenerations(false);
				} else 
					fitnessLimitField.setEnabled(false);
			}
		});
		
		GUIUtils.createButtonGroup(numberOfGenerationsRadioButton,fitnessLimitRadioButton);
		
		if (model.getNumberOfGenerations() < 0) {
			fitnessLimitRadioButton.setSelected(true);
			numberOfGenerationsField.setEnabled(false);
			model.setFixNumberOfGenerations(false);
		} else {
			fitnessLimitField.setEnabled(false);
			numberOfGenerationsRadioButton.setSelected(true);
			model.setFixNumberOfGenerations(true);
		}
		
		final JPanel stoppingPanel = FormsUtils.build("p ~ f:p:g", 
				"01 |"+
				"23", 
				numberOfGenerationsRadioButton,numberOfGenerationsField,
				fitnessLimitRadioButton,fitnessLimitField).getPanel();
		stoppingPanel.setBorder(BorderFactory.createTitledBorder(null,"Stopping",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(stoppingPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		// create the fitness function panel
		fitnessMinimizeRadioButton = new JRadioButton("Minimize");
		fitnessMaximizeRadioButton = new JRadioButton("Maximize");
		
		GUIUtils.createButtonGroup(fitnessMinimizeRadioButton,fitnessMaximizeRadioButton);
		GUIUtils.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fitnessMinimizeRadioButton.isSelected())
					model.setFitnessFunctionDirection(FitnessFunctionDirection.MINIMIZE);
				else if (fitnessMaximizeRadioButton.isSelected())
					model.setFitnessFunctionDirection(FitnessFunctionDirection.MAXIMIZE);
			}
		},fitnessMinimizeRadioButton,fitnessMaximizeRadioButton);
		
		if (model.getFitnessFunctionDirection() == FitnessFunctionDirection.MINIMIZE) 
			fitnessMinimizeRadioButton.setSelected(true);
		else 
			fitnessMaximizeRadioButton.setSelected(true);
		
		final JPanel fitnessButtonsPanel = new JPanel();
		Style.registerCssClasses(fitnessButtonsPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		fitnessButtonsPanel.add(fitnessMinimizeRadioButton);
		fitnessButtonsPanel.add(fitnessMaximizeRadioButton);
		
		fitnessFunctionList = new JComboBox(model.getFitnessFunctions().toArray(new Object[0]));
		fitnessFunctionList.setSelectedItem(model.getSelectedFitnessFunction());
		fitnessFunctionList.addItemListener(new ItemListener() {
			
			public void itemStateChanged(final ItemEvent e) {
				if (ItemEvent.SELECTED == e.getStateChange())
					model.setSelectedFitnessFunction((RecordableInfo) e.getItem());
			}
		});
		
		initializeStatistics(model);
		final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
		spinnerNumberModel.setStepSize(10);
		spinnerNumberModel.setValue(model.getFitnessTimeSeriesLength());
		fitnessTimeSeriesLength = new JSpinner(spinnerNumberModel);
		spinnerNumberModel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				model.setFitnessTimeSeriesLength((Integer)spinnerNumberModel.getValue());
			}
		});
		
		JXLabel fitnessMessage = new JXLabel("Please specify a function that always returns with non-negative value!");
		fitnessMessage.setLineWrap(true);
		final JPanel fitnessPanel = FormsUtils.build("p ~ f:p:g ~ p", 
				"000 |" +
				"122 ||" +
				"345 ||" +
				"677 ||" +
				"888",
				fitnessButtonsPanel,
				"Statistics", fitnessStatistics,
				"of the last", fitnessTimeSeriesLength, "values of",
				"Fitness function", fitnessFunctionList,
				fitnessMessage).getPanel();
		fitnessPanel.setBorder(BorderFactory.createTitledBorder(null,"Fitness",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(fitnessPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		operatorSettingsPanel = new JPanel(new CardLayout());
		operatorSettingsPanel.setBorder(BorderFactory.createTitledBorder(null,OPERATOR_SETTINGS_PANEL_TITLE_POSTFIX,TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(operatorSettingsPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		operatorSettingsPanel.add(new JLabel("Here come the operator settings!"), OPERATOR_SETTINGS_PANEL_DEFAULT_PANEL);
		
		final JPanel operatorDescriptionPanel = new JPanel(new BorderLayout());
		operatorDescriptionPanel.setBorder(BorderFactory.createTitledBorder(null,DESCRIPTION_PANEL,TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(operatorDescriptionPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		operatorDescriptionPanel.add(initDescriptionPanel(),BorderLayout.CENTER);
		
		final List<IGASelectorConfigurator> selectionOperators = model.getSelectionOperators();
		final List<IGASelectorConfigurator> selectedSelectionOperators = model.getSelectedSelectionOperators();
		final CheckListModel selectorListModel = new CheckListModel(selectionOperators);
		selectorList = new CheckList(selectorListModel);
		Style.registerCssClasses(selectorList, Dashboard.CSS_CLASS_COMMON_PANEL);
		selectorList.setBorder(BorderFactory.createTitledBorder(null,"Selection",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		selectorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		int i = 0;
		for (final IGASelectorConfigurator selectionOperator : selectionOperators) {
			final JPanel settingspanel = selectionOperator.getSettingspanel();
			Style.registerCssClasses(settingspanel, Dashboard.CSS_CLASS_COMMON_PANEL);
			
			String description = selectionOperator.getDescription();
			Style.registerCssClasses(description, Dashboard.CSS_CLASS_COMMON_PANEL);
			operatorSettingsPanel.add(settingspanel,selectionOperator.getName());
			
			if (selectedSelectionOperators.contains(selectionOperator))
				selectorList.setChecked(i, true);
			
			++i;
		}
		
		selectorList.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(final ListSelectionEvent e) {
				final IGASelectorConfigurator operator = (IGASelectorConfigurator) selectorList.getSelectedValue();
				showOperatorDetails(operatorSettingsPanel, operatorDescriptionPanel,operator);
			}
		});
		
		selectorListModel.addListDataListener(new ListDataListener() {
			
			public void intervalRemoved(final ListDataEvent e) {}
			public void intervalAdded(final ListDataEvent e) {}
			
			public void contentsChanged(final ListDataEvent e) {
				final IGASelectorConfigurator operator = (IGASelectorConfigurator) selectorList.getSelectedValue();
				
				if (selectorList.getModel().getCheckedState(e.getIndex0()))
					model.setSelectedSelectionOperator(operator);
				else 
					model.unsetSelectedSelectionOperator(operator);
			}
		});

		List<IGAOperatorConfigurator> geneticOperators = model.getGeneticOperators();
		List<IGAOperatorConfigurator> selectedGeneticOperators = model.getSelectedGeneticOperators();
		CheckListModel operatorListModel = new CheckListModel(geneticOperators);
		operatorList = new CheckList(operatorListModel);
		Style.registerCssClasses(operatorList, Dashboard.CSS_CLASS_COMMON_PANEL);
		operatorList.setBorder(BorderFactory.createTitledBorder(null,"Genetic operators",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		operatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		i = 0;
		for (final IGAOperatorConfigurator operator : geneticOperators){
			final JPanel settingsPanel = operator.getSettingspanel();
			Style.registerCssClasses(settingsPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
			
			String description = operator.getDescription();
			Style.registerCssClasses(description, Dashboard.CSS_CLASS_COMMON_PANEL);
			operatorSettingsPanel.add(settingsPanel,operator.getName());
			
			if (selectedGeneticOperators.contains(operator))
				operatorList.setChecked(i, true);
			
			++i;
		}

		operatorList.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(final ListSelectionEvent e) {
				final IGAOperatorConfigurator operator = (IGAOperatorConfigurator) operatorList.getSelectedValue();
				showOperatorDetails(operatorSettingsPanel, operatorDescriptionPanel, operator);
			}
		});
		
		operatorListModel.addListDataListener(new ListDataListener() {
			
			public void intervalRemoved(final ListDataEvent e) {}
			public void intervalAdded(final ListDataEvent e) {}
			
			public void contentsChanged(final ListDataEvent e) {
				final IGAOperatorConfigurator operator = (IGAOperatorConfigurator) operatorList.getSelectedValue();
				
				if (operatorList.getModel().getCheckedState(e.getIndex0()))
					model.setSelectedGeneticOperator(operator);
				else
					model.unsetSelectedGeneticOperator(operator);
			}
		});		

		final JPanel operatorDetailsPanel = FormsUtils.build("f:p:g(0.5) f:p:g(0.5)", 
				"01", operatorSettingsPanel, operatorDescriptionPanel).getPanel();
		Style.registerCssClasses(operatorDetailsPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel gaSettingsPanel = FormsUtils.build("f:p:g", 
				"0||" +
				"1||" +
				"2||" +
				"3||" +
				"4||" + 
				"5||" + 
				"6||" +
				"7",
				new FormsUtils.Separator("General parameters"),
				numberOfTurnsPanel,
				new FormsUtils.Separator("Genetic algorithm settings"),
				populationPanel,
				stoppingPanel,
				fitnessPanel,
				selectorList,
				operatorList).getPanel();
		Style.registerCssClasses(gaSettingsPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		// the chromosome panel
	    final JScrollPane treeScrPane = new JScrollPane();
	    geneTree = new JTree(model.getChromosomeTree()); 
		geneTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		geneTree.setCellRenderer(new ChromosomeTreeRenderer());
		
		geneTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				final TreePath selectionPath = geneTree.getSelectionPath();
				
				boolean success = true;
				if (editedNode != null && (selectionPath == null || !editedNode.equals(selectionPath.getLastPathComponent()))) 
					success = modify();
				
				final DefaultTreeModel treeModel = model.getChromosomeTree();
				if (success) {
					if (selectionPath != null) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
						final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
						if (userObj.getInfo().getDescription() != null && !userObj.getInfo().getDescription().trim().isEmpty())
							descriptionLabel.setText(userObj.getInfo().getDescription());
						else
							descriptionLabel.setText("");
						
						if (!node.equals(editedNode)) {
							if (!node.isRoot() && selectionPath.getPathCount() == treeModel.getPathToRoot(node).length) {
								editedNode = node;
								edit(userObj);
							} else {
								editedNode = null;
								geneTree.setSelectionPath(null);
								resetSettings();
								enableDisableSettings(false);
								descriptionLabel.setText("");
							}
						}
					} else {
						editedNode = null;
						resetSettings();
						enableDisableSettings(false);
						descriptionLabel.setText("");
					}
				} else 
					geneTree.setSelectionPath(new TreePath(treeModel.getPathToRoot(editedNode)));
				
			}
		});

	    treeScrPane.setViewportView(geneTree);
	    treeScrPane.setBorder(null);
	    treeScrPane.setViewportBorder(null);
		
		numberLabel = new JLabel("<html><b>Number of genes:</b> 0</html>");
		final JPanel treePanel = FormsUtils.build("~ f:p:g",
				  "0||" +
				  "1||" +
				  "2 f:p:g", 
				  numberLabel,
				  new FormsUtils.Separator(""),
				  treeScrPane).getPanel(); 


		treePanel.setBorder(BorderFactory.createTitledBorder(""));
		Style.registerCssClasses(treePanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		constantRButton = new JRadioButton("Constant");
		doubleGeneValueRangeRButton = new JRadioButton("Double value range");
		intGeneValueRangeRButton = new JRadioButton("Integer value range");
		listGeneValueRButton = new JRadioButton("Value list");
		GUIUtils.createButtonGroup(constantRButton,doubleGeneValueRangeRButton,intGeneValueRangeRButton,listGeneValueRButton);

		geneSettingsValuePanel = new JPanel(new CardLayout());
		Style.registerCssClasses(geneSettingsValuePanel, Dashboard.CSS_CLASS_COMMON_PANEL);

		constantField = new JTextField();
		constantField.setActionCommand("CONST_FIELD");
		
		final JPanel constantPanel = FormsUtils.build("p ~ p:g",
								 		"[DialogBorder]01 p",
								 		"Constant value",constantField).getPanel();
		Style.registerCssClasses(constantPanel,Dashboard.CSS_CLASS_COMMON_PANEL);

		
		enumDefBox = new JComboBox(new DefaultComboBoxModel());
		
		final JPanel enumDefPanel = FormsUtils.build("p ~ p:g", 
								"[DialogBorder]01 p", 
								"Constant value",enumDefBox).getPanel();
		Style.registerCssClasses(enumDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		submodelTypeBox = new JComboBox();
		
		final JPanel submodelTypePanel = FormsUtils.build("p ~ p:g",
											"[DialogBorder]01",
											"Constant value",submodelTypeBox).getPanel();
		Style.registerCssClasses(submodelTypePanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		fileTextField = new JTextField();
		fileTextField.setActionCommand("FILE_FIELD");
		fileTextField.addKeyListener(new KeyAdapter() {
			public void keyTyped(final KeyEvent e) { 
				final char character = e.getKeyChar();
				final File file = new File(Character.isISOControl(character) ? fileTextField.getText() : fileTextField.getText() + character);
				fileTextField.setToolTipText(file.getAbsolutePath());
			}
		});
		fileBrowseButton = new JButton(Page_Parameters.BROWSE_BUTTON_TEXT);
		fileBrowseButton.setActionCommand("FILE_BROWSE");
		
		final JPanel fileDefPanel = FormsUtils.build("p ~ p:g ~ p",
									   "[DialogBorder]012",
									   "File",fileTextField,fileBrowseButton).getPanel();
		Style.registerCssClasses(fileDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);

		intMinField = new JTextField();
		intMinField.setActionCommand("INT_MIN");
		intMaxField = new JTextField();
		intMaxField.setActionCommand("INT_MAX");
		doubleMinField = new JTextField();
		doubleMinField.setActionCommand("D_MIN");
		doubleMaxField = new JTextField();
		doubleMaxField.setActionCommand("D_MAX");
		valuesArea = new JTextArea();
		valuesArea.setToolTipText("Please specify ' ' (space) separated values!");
		valuesArea.setLineWrap(true);
		valuesArea.setWrapStyleWord(true);
		final JScrollPane valuesDefScr = new JScrollPane(valuesArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		final JPanel doublePanel = FormsUtils.build("p ' f:p:g",
									  "[DialogBorder]01 p ||" +
											  		"23 p",
											  		"Minimum value",doubleMinField,
											  		"Maximum value",doubleMaxField).getPanel();
		Style.registerCssClasses(doublePanel,Dashboard.CSS_CLASS_COMMON_PANEL);

		final JPanel intPanel = FormsUtils.build("p ' f:p:g",
								   "[DialogBorder]01 p ||" +
										   		 "23 p",
										   		 "Minimum value",intMinField,
										   		 "Maximum value",intMaxField).getPanel();
		Style.registerCssClasses(intPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel valuesPanel = FormsUtils.build("p ' p:g",
									  "[DialogBorder]01 f:p:g",
								      "Value list",CellConstraints.TOP,valuesDefScr).getPanel();
		Style.registerCssClasses(valuesPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel emptyPanel = new JPanel();
		Style.registerCssClasses(emptyPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		geneSettingsValuePanel.add(emptyPanel,"NULL");
		geneSettingsValuePanel.add(constantPanel,PARAM_SETTINGS_CONSTANT_VALUE_PANEL);
		geneSettingsValuePanel.add(enumDefPanel,PARAM_SETTINGS_ENUM_VALUE_PANEL);
		geneSettingsValuePanel.add(fileDefPanel,PARAM_SETTINGS_FILE_VALUE_PANEL);
		geneSettingsValuePanel.add(submodelTypePanel,PARAM_SETTINGS_SUBMODEL_VALUE_PANEL);
		geneSettingsValuePanel.add(doublePanel,GENE_SETTINGS_DOUBLE_RANGE_VALUE_PANEL);
		geneSettingsValuePanel.add(intPanel,GENE_SETTINGS_INT_RANGE_VALUE_PANEL);
		geneSettingsValuePanel.add(valuesPanel,GENE_SETTINGS_LIST_VALUE_PANEL);
		
		final ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent _) {
				if (editedNode != null) {
					final CardLayout layout = (CardLayout) geneSettingsValuePanel.getLayout();
					if (doubleGeneValueRangeRButton.isSelected())
						layout.show(geneSettingsValuePanel,GENE_SETTINGS_DOUBLE_RANGE_VALUE_PANEL);
					else if (intGeneValueRangeRButton.isSelected())
						layout.show(geneSettingsValuePanel,GENE_SETTINGS_INT_RANGE_VALUE_PANEL);
					else if (listGeneValueRButton.isSelected())
						layout.show(geneSettingsValuePanel,GENE_SETTINGS_LIST_VALUE_PANEL);
					else if (constantRButton.isSelected()) {
						final ParameterOrGene param = (ParameterOrGene) editedNode.getUserObject();
						if (param.getInfo() instanceof SubmodelInfo) 
							layout.show(geneSettingsValuePanel,PARAM_SETTINGS_SUBMODEL_VALUE_PANEL);
						else if (param.getInfo().isEnum() || param.getInfo() instanceof MasonChooserParameterInfo)
							layout.show(geneSettingsValuePanel,PARAM_SETTINGS_ENUM_VALUE_PANEL);
						else if (param.getInfo().isFile()) 
							layout.show(geneSettingsValuePanel,PARAM_SETTINGS_FILE_VALUE_PANEL);
						else
							layout.show(geneSettingsValuePanel,PARAM_SETTINGS_CONSTANT_VALUE_PANEL);
					}
				} else {
					resetSettings();
					enableDisableSettings(false);
				}
			}
		};
		constantRButton.addItemListener(itemListener);
		doubleGeneValueRangeRButton.addItemListener(itemListener);
		intGeneValueRangeRButton.addItemListener(itemListener);
		listGeneValueRButton.addItemListener(itemListener);
		
		final JPanel selectPanel = FormsUtils.build("f:p:g",
													"0||" +
													"1||" +
													"2||" +
													"3",
													constantRButton,
													doubleGeneValueRangeRButton,
													intGeneValueRangeRButton,
													listGeneValueRButton ).getPanel();
		Style.registerCssClasses(selectPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel buttonsPanel = new JPanel();
		modifyButton = new JButton("Modify");
		modifyButton.setActionCommand("EDIT");
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		GUIUtils.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String cmd = e.getActionCommand();
				if ("EDIT".equals(cmd)) 
					edit();
				else if ("CANCEL".equals(cmd)) 
					cancel();
				else if ("CONST_FIELD".equals(cmd) || "INT_MAX".equals(cmd) || "D_MAX".equals(cmd))
					modifyButton.doClick();
				else if ("INT_MIN".equals(cmd))
					intMaxField.grabFocus();
				else if ("D_MIN".equals(cmd))
					doubleMaxField.grabFocus();
				else if ("FILE_BROWSE".equals(cmd)) 
					chooseFile();
			}
			
			private void edit() {
				if (editedNode != null)
					geneTree.setSelectionPath(null);
			}
			
			private void cancel() {
				resetSettings();
				enableDisableSettings(false);
				editedNode = null;
				geneTree.setSelectionPath(null);
			}
			
			private void chooseFile() {
				final JFileChooser fileDialog = new JFileChooser(!"".equals(fileTextField.getToolTipText()) ? fileTextField.getToolTipText() :
					parentPage.getCurrentDirectory());
				if (!"".equals(fileTextField.getToolTipText()))
					fileDialog.setSelectedFile(new File(fileTextField.getToolTipText()));
				int dialogResult = fileDialog.showOpenDialog(parentPage.getWizard());
				if (dialogResult == JFileChooser.APPROVE_OPTION) {
					final File selectedFile = fileDialog.getSelectedFile();
					if (selectedFile != null) {
						parentPage.setCurrentDirectory(selectedFile.getAbsoluteFile().getParent());
						fileTextField.setText(selectedFile.getName());
						fileTextField.setToolTipText(selectedFile.getAbsolutePath());
					}
				}
			}
		},modifyButton,cancelButton,constantField,fileTextField,intMinField,intMaxField,doubleMinField,doubleMaxField,fileBrowseButton);
		buttonsPanel.add(modifyButton);
		buttonsPanel.add(cancelButton);
		
		final JPanel geneSetterPanel = FormsUtils.build("f:p:g",
														"0||" +
														"1 f:p:g||" +
														"2 p",
														selectPanel,
														geneSettingsValuePanel,
														buttonsPanel).getPanel();
		
		geneSetterPanel.setBorder(BorderFactory.createTitledBorder(null,"Gene/parameter settings",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(geneSetterPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel rightTop = FormsUtils.build("f:p:g p",
												 "01 f:p:g",
												 treePanel,geneSetterPanel).getPanel();
		rightTop.setBorder(BorderFactory.createTitledBorder(null,"Chromosome",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(rightTop,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel mainPanel = FormsUtils.build("p f:p:g", 
				"01 f:p:g||" +
				"02 p",
				gaSettingsPanel,rightTop,
				operatorDetailsPanel).getPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder(""));
		Style.registerCssClasses(mainPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		
		setLayout(new BorderLayout());
		mainScrPane = new JScrollPane(mainPanel);
		mainScrPane.setBorder(null);
		mainScrPane.setViewportBorder(null);
		add(mainScrPane, BorderLayout.CENTER);
		
		resetSettings();
		enableDisableSettings(false);
	}

	private void initializeStatistics(final GASearchPanelModel model) {
		fitnessStatistics = new JComboBox(model.getFitnessStatistics().toArray(new Object[0]));
		fitnessStatistics.setRenderer(new DefaultListCellRenderer() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				IStatisticsPlugin plugin = (IStatisticsPlugin) value;
				
				this.setText(plugin.getName());
				this.setToolTipText(plugin.getDescription());
				
				return this;
			}
		});
		fitnessStatistics.setSelectedItem(model.getSelectedFitnessStatistics());
		fitnessStatistics.addItemListener(new ItemListener() {
			
			public void itemStateChanged(final ItemEvent e) {
				if (ItemEvent.SELECTED == e.getStateChange())
					model.setSelectedFitnessStatistics((IStatisticsPlugin) e.getItem());
			}
		});

	}
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
    private JCheckBox createOperatorCheckBox(final JPanel operatorSettingsPanel, final JPanel operatorDescriptionPanel, final IGASelectorConfigurator operator, final GASearchPanelModel model) {
		JCheckBox checkBox = new JCheckBox(operator.getName());
		operatorSettingsPanel.add(operator.getSettingspanel(), operator.getName());
		JXLabel descriptionLabel = new JXLabel(operator.getDescription());
		descriptionLabel.setLineWrap(true);
		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
		descriptionLabel.setPreferredSize(new Dimension(200, 200));
		operatorDescriptionPanel.add(descriptionLabel, operator.getName());
		
		checkBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED){
					CardLayout settingslayout = (CardLayout) operatorSettingsPanel.getLayout();
					settingslayout.show(operatorSettingsPanel, operator.getName());
					((TitledBorder)operatorSettingsPanel.getBorder()).setTitle(operator.getName());
					operatorSettingsPanel.repaint();
					
					CardLayout descriptionLayout = (CardLayout) operatorDescriptionPanel.getLayout();
					descriptionLayout.show(operatorDescriptionPanel, operator.getName());
					
//					model.setSelectedOperator(operator);
				} else {
					CardLayout settingslayout = (CardLayout) operatorSettingsPanel.getLayout();
					settingslayout.show(operatorSettingsPanel, OPERATOR_SETTINGS_PANEL_DEFAULT_PANEL);
					((TitledBorder)operatorSettingsPanel.getBorder()).setTitle(OPERATOR_SETTINGS_PANEL_TITLE_POSTFIX);
					operatorSettingsPanel.repaint();

					CardLayout descriptionLayout = (CardLayout) operatorDescriptionPanel.getLayout();
					descriptionLayout.show(operatorDescriptionPanel, OPERATOR_SETTINGS_PANEL_DEFAULT_PANEL);
					
//					model.removeSelectedOperator(operator);
				}
			}
		});
		
		checkBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				CardLayout settingslayout = (CardLayout) operatorSettingsPanel.getLayout();
				settingslayout.show(operatorSettingsPanel, operator.getName());
				((TitledBorder)operatorSettingsPanel.getBorder()).setTitle(operator.getName());
				operatorSettingsPanel.repaint();
				
				CardLayout descriptionLayout = (CardLayout) operatorDescriptionPanel.getLayout();
				descriptionLayout.show(operatorDescriptionPanel, operator.getName());				
			}
		});
		
		return checkBox;
	}
	
	//----------------------------------------------------------------------------------------------------
	private JScrollPane initDescriptionPanel() {
		descriptionLabel = new JXLabel("");
		descriptionLabel.setLineWrap(true);
		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
		
		JScrollPane scrollPane = new JScrollPane(descriptionLabel);
		Style.registerCssClasses(scrollPane, ".borderless");
		scrollPane.getViewportBorder();
		scrollPane.setViewportBorder(null);
		scrollPane.setPreferredSize(new Dimension(200, 100));
		
		return scrollPane;
	}

	//----------------------------------------------------------------------------------------------------
	protected void showOperatorDetails(final JPanel operatorSettingsPanel, final JPanel operatorDescriptionPanel, final IGAConfigurator operator) {
		if (operator != null) {
			CardLayout settingslayout = (CardLayout) operatorSettingsPanel.getLayout();
			settingslayout.show(operatorSettingsPanel, operator.getName());
			((TitledBorder)operatorSettingsPanel.getBorder()).setTitle(operator.getName());
			operatorSettingsPanel.repaint();
			descriptionLabel.setText(operator.getDescription());
		} else
			descriptionLabel.setText("");

	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * Utility method that returns the string representation of a double value (rounded to 2 decimal
	 * digits using US format) 
	 */
	private static String double2String(final double d) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
		return numberFormatter.format(bd);
	}
	
	//------------------------------------------------------------------------------
	/** Enables/disables the parameter editing components of the page according to
	 *  the value of <code>enabled</code>.
	 */
	private void enableDisableSettings(final boolean enabled) {
		constantRButton.setEnabled(enabled);
		intGeneValueRangeRButton.setEnabled(enabled);
		doubleGeneValueRangeRButton.setEnabled(enabled);
		listGeneValueRButton.setEnabled(enabled);
		constantField.setEnabled(enabled);
		enumDefBox.setEnabled(enabled);
		submodelTypeBox.setEnabled(enabled);
		fileTextField.setEnabled(enabled);
		fileBrowseButton.setEnabled(enabled);
		intMinField.setEnabled(enabled);
		intMaxField.setEnabled(enabled);
		doubleMinField.setEnabled(enabled);
		doubleMaxField.setEnabled(enabled);
		valuesArea.setEnabled(enabled);
		modifyButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);
	}

	//------------------------------------------------------------------------------
	/** Resets the parameter editing related components. */
	private void resetSettings() { 
		constantRButton.setSelected(false);
		intGeneValueRangeRButton.setSelected(false);
		doubleGeneValueRangeRButton.setSelected(false);;
		listGeneValueRButton.setSelected(false);;
		constantField.setText("");
		fileTextField.setText("");
		intMinField.setText("");
		intMaxField.setText("");
		doubleMinField.setText("");
		doubleMaxField.setText("");
		valuesArea.setText("");
		modifyButton.setEnabled(false);
		cancelButton.setEnabled(false);
		
		final CardLayout cl = (CardLayout) geneSettingsValuePanel.getLayout();
		cl.show(geneSettingsValuePanel,"NULL");
	}
	
	//----------------------------------------------------------------------------------------------------
	private void edit(final ParameterOrGene param) {
		enableDisableSettings(true);
		String view = PARAM_SETTINGS_CONSTANT_VALUE_PANEL;
		if (param.isGene()) {
			editGene(param.getGeneInfo());
			return;
		} else if (param.getInfo() instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) param.getInfo();
			editSubmodelInfo(sInfo);
			return;
		} else if (param.getInfo().isEnum() || param.getInfo() instanceof MasonChooserParameterInfo) {
			view = PARAM_SETTINGS_ENUM_VALUE_PANEL;
			intGeneValueRangeRButton.setEnabled(false);
			doubleGeneValueRangeRButton.setEnabled(false);
			listGeneValueRButton.setEnabled(false);
			constantRButton.setSelected(true);
			
			final ParameterInfo info = param.getInfo();
			final Object selected = info.getValue();
			Object[] elements = null;
			if (info.isEnum()) {
				@SuppressWarnings("unchecked")
				final Class<Enum<?>> type = (Class<Enum<?>>) info.getJavaType();
				elements = type.getEnumConstants();
			} else {
				final MasonChooserParameterInfo chooserInfo = (MasonChooserParameterInfo) info;
				elements = chooserInfo.getValidStrings();
			}
			
			final DefaultComboBoxModel model = (DefaultComboBoxModel) enumDefBox.getModel();
			model.removeAllElements();
			for (final Object object : elements) 
				model.addElement(object);
				
			if (info.isEnum())
				enumDefBox.setSelectedItem(selected);
			else 
				enumDefBox.setSelectedIndex((Integer)selected);
		} else if (param.getInfo().isFile()) {
			view = PARAM_SETTINGS_FILE_VALUE_PANEL;
			constantRButton.setSelected(true);
			final File file = (File) param.getInfo().getValue();
			if (file != null) {
				fileTextField.setText(file.getName());
				fileTextField.setToolTipText(file.getAbsolutePath());
			}
		} else {
			constantRButton.setSelected(true);
			constantField.setText(param.getInfo().valuesToString());
		}
		
		final CardLayout cl = (CardLayout) geneSettingsValuePanel.getLayout();
		cl.show(geneSettingsValuePanel,view);
		if (!param.getInfo().isNumeric()) {
			intGeneValueRangeRButton.setEnabled(false);
			doubleGeneValueRangeRButton.setEnabled(false);
			listGeneValueRButton.setEnabled(false);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void editSubmodelInfo(final SubmodelInfo sInfo) {
		constantRButton.setSelected(true);
		intGeneValueRangeRButton.setEnabled(false);
		doubleGeneValueRangeRButton.setEnabled(false);
		listGeneValueRButton.setEnabled(false);
		final Class<?> actualType = sInfo.getActualType();
		final DefaultComboBoxModel model = (DefaultComboBoxModel) submodelTypeBox.getModel();
		model.removeAllElements();
		model.addElement("Loading class information...");
		final CardLayout cl = (CardLayout) geneSettingsValuePanel.getLayout();
		cl.show(geneSettingsValuePanel,PARAM_SETTINGS_SUBMODEL_VALUE_PANEL);
		
		try {
			final ClassElement[] elements = parentPage.fetchPossibleTypes(sInfo);
			model.removeAllElements();
			for (final ClassElement classElement : elements) 
				model.addElement(classElement);
			
			if (actualType != null)
				submodelTypeBox.setSelectedItem(new ClassElement(actualType, sInfo.getInstance()));
			submodelTypeBox.setEnabled(true);
		} catch (final ComboBoxIsTooFullException e) {
			String errorMsg = "Too much possibilities for " + sInfo.getName() + ". Please narrow down the domain by " + 
					  		  "defining the possible classes in the @Submodel annotation.";
			JOptionPane.showMessageDialog(parentPage.getWizard(),new JLabel(errorMsg),"Error while analyizing model",JOptionPane.ERROR_MESSAGE);
			
			resetSettings();
			enableDisableSettings(false);
			editedNode = null;
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void editGene(final GeneInfo geneInfo) {
		String view = null;
		if (GeneInfo.LIST.equals(geneInfo.getValueType())) {
			view = GENE_SETTINGS_LIST_VALUE_PANEL;
			listGeneValueRButton.setSelected(true);
			valuesArea.setText(Utils.join(geneInfo.getValueRange()," "));
		} else {
			if (geneInfo.isIntegerVals()) {
				view = GENE_SETTINGS_INT_RANGE_VALUE_PANEL;
				intGeneValueRangeRButton.setSelected(true);
				intMinField.setText(String.valueOf(geneInfo.getMinValue()));
				intMaxField.setText(String.valueOf(geneInfo.getMaxValue()));
			} else {
				view = GENE_SETTINGS_DOUBLE_RANGE_VALUE_PANEL;
				doubleGeneValueRangeRButton.setSelected(true);
				doubleMinField.setText(String.valueOf(geneInfo.getMinValue()));
				doubleMaxField.setText(String.valueOf(geneInfo.getMaxValue()));
			}
		}
		
		final CardLayout cl = (CardLayout) geneSettingsValuePanel.getLayout();
		cl.show(geneSettingsValuePanel,view);
	}
	
	//----------------------------------------------------------------------------------------------------
	private String[] checkInput(final ParameterOrGene param) {
		final List<String> errors = new ArrayList<String>();
		
		if (constantRButton.isSelected()) {
			final ParameterInfo info = param.getInfo();
			
			if (info.isEnum() || info instanceof MasonChooserParameterInfo) { 
				// well, the combo box must contain a valid value
			} else if (info instanceof SubmodelInfo) {
				final Object selectedObject = submodelTypeBox.getSelectedItem();
				if (!(selectedObject instanceof ClassElement))
					errors.add("Invalid type is selected for parameter " + info.getName());
				else {
					final ClassElement selectedElement = (ClassElement) selectedObject;
					if (selectedElement.clazz == null)
						errors.add("'Constant value' cannot be empty. Please select a type.");
				}
			} else if (info.isFile()) {
				if (fileTextField.getText().trim().isEmpty())
					log.warn("Empty string was specified as file parameter " + info.getName().replaceAll("([A-Z])"," $1"));

				final File file = new File(fileTextField.getToolTipText());
				if (!file.exists()) 
					errors.add("File " + fileTextField.getToolTipText() + " does not exist.");
			} else if (constantField.getText().trim().equals(""))
				errors.add("'Constant value' cannot be empty.");
			else {
				final boolean valid = ParameterInfo.isValid(constantField.getText().trim(),info.getType());
				if (!valid) 
					errors.add("'Constant value' must be a valid " + parentPage.getTypeText(info.getType()) + ".");
				
				errors.addAll(PlatformSettings.additionalParameterCheck(info,new String[] { constantField.getText().trim() },ParameterInfo.CONST_DEF));
			}
		} else if (intGeneValueRangeRButton.isSelected()) {
			final String minFieldText = intMinField.getText();
			final String maxFieldText = intMaxField.getText();
			Long minValue = null;
			Long maxValue = null;
			
			if (minFieldText == null || minFieldText.trim().isEmpty()) 
				errors.add("'Minimum value' cannot be empty.");
			else {
				try {
					minValue = new Long(minFieldText.trim());
					
					final boolean valid = ParameterInfo.isValid(minFieldText,param.getInfo().getType());
					if (!valid) 
						errors.add("'Minimum value' must be a valid " + parentPage.getTypeText(param.getInfo().getType()) + ".");
					
					errors.addAll(PlatformSettings.additionalParameterCheck(param.getInfo(),new String[] { minFieldText.trim() },ParameterInfo.CONST_DEF));
				} catch (final NumberFormatException _) {
					errors.add("'Minimum value' must be a valid integer number.");
				}
			}
			
			if (maxFieldText == null || maxFieldText.trim().isEmpty()) 
				errors.add("'Maximum value' cannot be empty.");
			else {
				try {
					maxValue = new Long(maxFieldText.trim());
					
					final boolean valid = ParameterInfo.isValid(maxFieldText,param.getInfo().getType());
					if (!valid) 
						errors.add("'Maximum value' must be a valid " + parentPage.getTypeText(param.getInfo().getType()) + ".");
					
					errors.addAll(PlatformSettings.additionalParameterCheck(param.getInfo(),new String[] { maxFieldText.trim() },ParameterInfo.CONST_DEF));
				} catch (final NumberFormatException _) {
					errors.add("'Maximum value' must be a valid integer number.");
				}
			}
			
			if (minValue != null && maxValue != null) {
				if (minValue.longValue() > maxValue.longValue())
					errors.add("Please specify a non-empty interval!");
			}
		} else if (doubleGeneValueRangeRButton.isSelected()) {
			final String minFieldText = doubleMinField.getText();
			final String maxFieldText = doubleMaxField.getText();
			Double minValue = null;
			Double maxValue = null;
			
			if (minFieldText == null || minFieldText.trim().isEmpty()) 
				errors.add("'Minimum value' cannot be empty.");
			else {
				try {
					minValue = new Double(minFieldText.trim());
					
					final boolean valid = ParameterInfo.isValid(minFieldText,param.getInfo().getType());
					if (!valid) 
						errors.add("'Minimum value' must be a valid " + parentPage.getTypeText(param.getInfo().getType()) + ".");
					
					errors.addAll(PlatformSettings.additionalParameterCheck(param.getInfo(),new String[] { minFieldText.trim() },ParameterInfo.CONST_DEF));
				} catch (final NumberFormatException _) {
					errors.add("'Minimum value' must be a valid number.");
				}
			}
			
			if (maxFieldText == null || maxFieldText.trim().isEmpty()) 
				errors.add("'Maximum value' cannot be empty.");
			else {
				try {
					maxValue = new Double(maxFieldText.trim());
					
					final boolean valid = ParameterInfo.isValid(maxFieldText,param.getInfo().getType());
					if (!valid) 
						errors.add("'Maximum value' must be a valid " + parentPage.getTypeText(param.getInfo().getType()) + ".");
					
					errors.addAll(PlatformSettings.additionalParameterCheck(param.getInfo(),new String[] { maxFieldText.trim() },ParameterInfo.CONST_DEF));
				} catch (final NumberFormatException _) {
					errors.add("'Maximum value' must be a valid number.");
				}
			}
			
			if (minValue != null && maxValue != null) {
				if (minValue.doubleValue() > maxValue.doubleValue())
					errors.add("Please specify a non-empty interval!");
			}

		} else if (listGeneValueRButton.isSelected()) {
			if (valuesArea.getText() == null || valuesArea.getText().trim().isEmpty())
				errors.add("'Value list' cannot be empty.");
			else {
				try {
					final List<Object> valueList = new ArrayList<Object>();
					String values = valuesArea.getText().trim();
					values = values.replaceAll("\\s+"," ");
					final StringTokenizer tokenizer = new StringTokenizer(values, " ");
					while (tokenizer.hasMoreTokens()) {
						final String strValue = tokenizer.nextToken();
						
						final boolean valid = ParameterInfo.isValid(strValue,param.getInfo().getType());
						if (!valid) 
							errors.add("'" + strValue + "' is not a valid " + parentPage.getTypeText(param.getInfo().getType()) + ".");
						
						valueList.add(parseListElement(param.getInfo().getJavaType(),strValue));
					}
					
					errors.addAll(PlatformSettings.additionalParameterCheck(param.getInfo(),values.split(" "),ParameterInfo.LIST_DEF));
				} catch (final NumberFormatException _) {
					errors.add("All element of the value list must be a valid number.");
				}
			}
		}
		
		return errors.isEmpty() ? null : errors.toArray(new String[0]);
	}
	
	//----------------------------------------------------------------------------------------------------
	private Object parseListElement(final Class<?> type, final String strValue) {
		if (type.equals(Double.class) || type.equals(Float.class) || type.equals(Double.TYPE) || type.equals(Float.TYPE))
			return Double.parseDouble(strValue);
		
		return Long.parseLong(strValue);
	}
	
	//----------------------------------------------------------------------------------------------------
	private boolean modify() {
		final ParameterOrGene userObj = (ParameterOrGene) editedNode.getUserObject();
		
		final String[] errors = checkInput(userObj);
		if (errors != null && errors.length != 0) {
			Utilities.userAlert(mainScrPane,(Object)errors);
			return false;
		} else {
			modifyParameterOrGene(userObj);
			resetSettings();
			
			if (!(userObj.info instanceof SubmodelInfo)) {
				final DefaultTreeModel model = (DefaultTreeModel) geneTree.getModel();
				model.nodeStructureChanged(editedNode);
			}
			
			editedNode = null;
			
			updateNumberOfGenes();
			enableDisableSettings(false);
			return true;
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean closeActiveModification() {
		return editedNode != null ? modify() : true;
	}
	
	//------------------------------------------------------------------------------
	private void modifyParameterOrGene(final ParameterOrGene param) {
		if (constantRButton.isSelected()) {
			final ParameterInfo info = param.getInfo();
			if (info.isEnum()) 
				param.setConstant(enumDefBox.getSelectedItem());
			else if (info instanceof MasonChooserParameterInfo) 
				param.setConstant(enumDefBox.getSelectedIndex());				
			else if (info instanceof SubmodelInfo) {
				final SubmodelInfo sInfo = (SubmodelInfo) info;
				modifySubmodelParameterInfo(sInfo);
			} else if (info.isFile()) 
				param.setConstant(ParameterInfo.getValue(fileTextField.getToolTipText(),info.getType()));
			else
				param.setConstant(ParameterInfo.getValue(constantField.getText().trim(),info.getType()));
		} else if (intGeneValueRangeRButton.isSelected()) {
			final Long minValue = new Long(intMinField.getText().trim());
			final Long maxValue = new Long(intMaxField.getText().trim());
			param.setGene(minValue,maxValue);
		} else if (doubleGeneValueRangeRButton.isSelected()) {
			final Double minValue = new Double(doubleMinField.getText().trim());
			final Double maxValue = new Double(doubleMaxField.getText().trim());
			param.setGene(minValue,maxValue);
		} else if (listGeneValueRButton.isSelected()) {
			final List<Object> valueList = new ArrayList<Object>();
			String values = valuesArea.getText().trim();
			values = values.replaceAll("\\s+"," ");
			final StringTokenizer tokenizer = new StringTokenizer(values, " ");
			while (tokenizer.hasMoreTokens()) 
				valueList.add(parseListElement(param.getInfo().getJavaType(),tokenizer.nextToken()));
			param.setGene(valueList);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void modifySubmodelParameterInfo(final SubmodelInfo sInfo) {
		final Object selectedItem = submodelTypeBox.getSelectedItem();
		if (selectedItem instanceof ClassElement) {
			ClassElement aClassElement = (ClassElement)selectedItem;
			final Class<?> newActualType = aClassElement.clazz;
			
			if (!parentPage.classEquals(newActualType,sInfo.getActualType())) {
				sInfo.setActualType(newActualType, aClassElement.instance);
				addSubParametersToTree(sInfo, editedNode);
			} // else do nothing
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void addSubParametersToTree(final SubmodelInfo sInfo, final DefaultMutableTreeNode node) { 
		try {
			final DefaultTreeModel treeModel = (DefaultTreeModel) geneTree.getModel();

			model.addSubmodelParameter(sInfo, node);

			geneTree.expandPath(new TreePath(treeModel.getPathToRoot(node)));
		} catch (final ModelInformationException e) {
			sInfo.setActualType(null, null);
			JOptionPane.showMessageDialog(parentPage.getWizard(),new JLabel(e.getMessage()), "Error while analyizing model", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			submodelTypeBox.setSelectedIndex(0);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void updateNumberOfGenes() {
		int count = 0;
		
		final DefaultTreeModel treeModel = (DefaultTreeModel) geneTree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = root.breadthFirstEnumeration();
		nodes.nextElement();
		
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
			if (userObj.isGene())
				count++;
		}
		
		numberLabel.setText("<html><b>Number of genes:</b> " + count + "</html>");
	}
}