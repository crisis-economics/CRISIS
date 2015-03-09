/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXLabel;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.aitia.meme.gui.Wizard;
import ai.aitia.meme.gui.Wizard.Button;
import ai.aitia.meme.gui.Wizard.IWizardPage;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.IncrementalParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterNode;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.gui.info.ISubmodelGUIInfo;
import ai.aitia.meme.paramsweep.gui.info.MasonChooserParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.MasonIntervalParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.internal.platform.IGUIController.RunOption;
import ai.aitia.meme.paramsweep.internal.platform.InfoConverter;
import ai.aitia.meme.paramsweep.internal.platform.PlatformSettings;
import ai.aitia.meme.paramsweep.plugin.IIntelliContext;
import ai.aitia.meme.paramsweep.plugin.IIntelliDynamicMethodPlugin;
import ai.aitia.meme.paramsweep.utils.Utilities;
import ai.aitia.meme.utils.GUIUtils;
import ai.aitia.meme.utils.Utils.Pair;
import aurelienribon.ui.css.Style;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.looks.common.RGBGrayFilter;
import com.wordpress.tips4java.ListAction;

import eu.crisis_economics.abm.dashboard.ga.DashboardIntelliContext;
import eu.crisis_economics.abm.dashboard.generated.Combination;
import eu.crisis_economics.abm.dashboard.generated.DefaultParameter;
import eu.crisis_economics.abm.dashboard.generated.GeneralParameter;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ModelType;
import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;
import eu.crisis_economics.abm.dashboard.generated.Parameter;
import eu.crisis_economics.abm.dashboard.generated.ParameterType;
import eu.crisis_economics.abm.dashboard.generated.SubmodelParameter;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;

/**
 * @author Tamás Máhr
 *
 */
public class Page_Parameters implements IWizardPage, IIconsInHeader, ActionListener {
	
	//====================================================================================================
	// members
	
	private static final String CSS_ID_BREADCRUMB = "#breadcrumb";
	private static final String LOADING_MODEL_ID = "LOADING_MODEL";
	private static final String PARAMETERS_PANEL_ID = "PARAMETERS_PANEL";
	private static final String UPDATE_CHARTS_LABEL_TEXT = " Update charts in real-time";
	private static final String DISPLAY_ADVANCED_CHARTS_LABEL_TEXT = " Show individual agents' measures";
	private static final String NUMBER_OF_TURNS_LABEL_TEXT = " Terminate simulation at time-step";
	private static final String NUMBER_OF_TIMESTEPS_TO_IGNORE_LABEL_TEXT = " Start charting after time-step";

	static final String BROWSE_BUTTON_TEXT = "Browse";
	
	private static final String DECORATION_IMAGE = "Audio_mixer_faders.jpg";
	static final int DECORATION_IMAGE_WIDTH = 300;
	private static final int PARAMETER_DESCRIPTION_LABEL_HEIGHT = 200;
	private static final int PARAMETER_DESCRIPTION_LABEL_WIDTH = 400;
	
	private static final int MAX_SIZE_OF_SUBPARAMETER_COMBOBOX = 100;
	
	static final int ICON_WIDTH_AND_HEIGHT = 25;
	
	private static final ImageIcon PARAMETER_ADD_ICON_DIS = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_silver_right.png")).
															getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_ADD_ICON = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_green_right.png")).
														getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_ADD_ICON_RO = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_red_right.png")).
														   getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	
	private static final ImageIcon PARAMETER_REMOVE_ICON_DIS = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_silver_left.png")).
														   	   getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_REMOVE_ICON = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_green_left.png")).
														   getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_REMOVE_ICON_RO = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_red_left.png")).
															  getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
		
	private static final ImageIcon PARAMETER_UP_ICON_DIS = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_silver_up.png")).
														   getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_UP_ICON = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_green_up.png")).
													   getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_UP_ICON_RO = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_red_up.png")).
														  getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
																																				
	private static final ImageIcon PARAMETER_DOWN_ICON_DIS = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_silver_down.png")).
															 getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_DOWN_ICON = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_green_down.png")).
														 getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon PARAMETER_DOWN_ICON_RO = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_red_down.png")).
															getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));

	private static final ImageIcon PARAMETER_BOX_REMOVE = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/remove_box.png")).getImage().
														  getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	
	private static final ImageIcon SHOW_SUBMODEL_ICON_DIS = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_silver_right.png")).
			getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon SHOW_SUBMODEL_ICON = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_green_right.png")).
			getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));
	private static final ImageIcon SHOW_SUBMODEL_ICON_RO = new ImageIcon(new ImageIcon(Dashboard.class.getResource("icons/arrow_button_metal_red_right.png")).
			getImage().getScaledInstance(ICON_WIDTH_AND_HEIGHT,ICON_WIDTH_AND_HEIGHT,Image.SCALE_SMOOTH));

	private static final int GASEARCH_GUI_TABINDEX = 2;
	private static final int PARAMSWEEP_GUI_TABINDEX = 1;
	private static final int SINGLE_RUN_GUI_TABINDEX = 0;

	private static final String ACTIONCOMMAND_ADD_PARAM = "ADD_PARAM",
								ACTIONCOMMAND_REMOVE_PARAM = "REMOVE_PARAM",
								ACTIONCOMMAND_ADD_BOX = "ADD_BOX",
								ACTIONCOMMAND_REMOVE_BOX = "REMOVE_BOX",
								ACTIONCOMMAND_MOVE_UP = "MOVE_UP",
								ACTIONCOMMAND_MOVE_DOWN = "MOVE_DOWN",
								ACTIONCOMMAND_EDIT = "EDIT",
								ACTIONCOMMAND_BROWSE = "BROWSE",
								ACTIONCOMMAND_SHOW_SUBMODEL = "SHOW_SUBMODEL";

	private static final String TITLE = "Parameters";
	private static final String ORIGINAL_TEXT = "Select a parameter to modify its settings.";
	private static final String INFO_TEXT = "Please select the parameters to run";

	private static Logger log = LoggerFactory.getLogger(Page_Parameters.class);

	private final Dashboard dashboard;
	private IModelHandler currentModelHandler;
	private final DWizard wizard;
	private final Container container;
	private JScrollPane parametersScrollPane;
	
	private DefaultMutableTreeNode parameterValueComponentTree = new DefaultMutableTreeNode();
	private final Map<String,List<Class<?>>> possibleTypesCache = new HashMap<String,List<Class<?>>>();

	private JFormattedTextField numberOfTurnsField;
	private JFormattedTextField numberOfTurnsFieldPSW;
	private JFormattedTextField numberTimestepsIgnored;
	private JFormattedTextField numberTimestepsIgnoredPSW;
	private JCheckBox onLineChartsCheckBox;
	private JCheckBox advancedChartsCheckBox;
//	private JCheckBox onLineChartsCheckBoxPSW;

	private Popup errorPopup;

	private JRadioButton constDef;
	private JRadioButton listDef;
	private JRadioButton incrDef;
	private JTextField constDefField;
	private JTextArea listDefArea;
	private JTextField incrStartValueField;
	private JTextField incrEndValueField;
	private JTextField incrStepField;
	private JComboBox enumDefBox;
	private JComboBox submodelTypeBox;
	private JButton cancelButton;
	private JButton modifyButton;
	private JTextField fileTextField;
	private JButton fileBrowseButton;

	private JLabel editedParameterText;
	private JPanel sweepPanel;
	private JPanel rightMiddle;
	private JTabbedPane tabbedPane;

	private JList parameterList;
	private JXLabel parameterDescriptionLabel;

	private GASearchPanelModel gaSearchHandler;
	private GASearchPanel gaSearchPanel;
	
	private JPanel singleRunParametersPanel;
	
	private DefaultMutableTreeNode editedNode;
	private JTree editedTree;
	private List<ParameterCombinationGUI> parameterTreeBranches = new ArrayList<ParameterCombinationGUI>(5);
	private List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters;

	private JPanel combinationsPanel;
	private JScrollPane combinationsScrPane;

	protected String currentDirectory = "";
	protected Breadcrumb breadcrumb;
	
	/**
	 * The list of active submodel buttons that need to be turned red. The index in the list corresponds to the submodel level.
	 */
	protected List<JButton> submodelButtons = new ArrayList<JButton>();
	
	//====================================================================================================
	// methods

	//----------------------------------------------------------------------------------------------------
	public Page_Parameters(final DWizard wizard, final Dashboard dashboard) {
		this.wizard = wizard;
		this.dashboard = dashboard;
		
		container = initContainer();
		
		Style.apply(container, dashboard.getCssStyle());
	}

	//----------------------------------------------------------------------------------------------------
	/** {@inheritDoc} 
	 */
	@Override
	public String getInfoText(final Wizard w) {
		return w.getArrowsHeader(INFO_TEXT);
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("icons/params.png"));
	}
	
	//----------------------------------------------------------------------------------------------------
	/** {@inheritDoc} 
	 */
	@Override
	public Container getPanel() {
		
		return container;
	}

	//----------------------------------------------------------------------------------------------------
	private Container initContainer() {
		JPanel containerPanel = new JPanel(new CardLayout());
		JLabel loadingModelLabel = new JLabel("Loading model...");
		loadingModelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		containerPanel.add(loadingModelLabel, LOADING_MODEL_ID);
		tabbedPane = new JTabbedPane();
		containerPanel.add(tabbedPane, PARAMETERS_PANEL_ID);
		
		// create two number of turns field
		numberOfTurnsField = new JFormattedTextField();
		numberOfTurnsField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		numberOfTurnsField.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(final JComponent input) {
				if (!checkNumberOfTurnsField(true)){
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = numberOfTurnsField.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify a (possibly floating point) number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					if (errorPopup != null)
						errorPopup.hide();
					errorPopup = popupFactory.getPopup(numberOfTurnsField, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					
					errorPopup.show();
					
					return false;
				} else {
					if (errorPopup != null){
						errorPopup.hide();
						errorPopup = null;
					}
					return true;
				}
			}
		});
		numberOfTurnsField.setText(String.valueOf(Dashboard.NUMBER_OF_TURNS));
		numberOfTurnsField.setToolTipText("The turn when the simulation should stop specified as an integer value.");
		numberOfTurnsField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				wizard.clickDefaultButton();
			}
		});

		numberOfTurnsFieldPSW = new JFormattedTextField();
		numberOfTurnsFieldPSW.setFocusLostBehavior(JFormattedTextField.COMMIT);
		numberOfTurnsFieldPSW.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(final JComponent input) {
				if (!checkNumberOfTurnsField(false)){
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = numberOfTurnsFieldPSW.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify a (possibly floating point) number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					if (errorPopup != null)
						errorPopup.hide();
					errorPopup = popupFactory.getPopup(numberOfTurnsFieldPSW, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					
					errorPopup.show();
					
					return false;
				} else {
					if (errorPopup != null){
						errorPopup.hide();
						errorPopup = null;
					}
					return true;
				}
			}
		});
		numberOfTurnsFieldPSW.setText(String.valueOf(Dashboard.NUMBER_OF_TURNS));
		numberOfTurnsFieldPSW.setToolTipText("The turn when the simulation should stop specified as an integer value.");
		numberOfTurnsFieldPSW.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				wizard.clickDefaultButton();
			}
		});

		// create two number of time-steps to ignore field
		numberTimestepsIgnored = new JFormattedTextField();
		numberTimestepsIgnored.setFocusLostBehavior(JFormattedTextField.COMMIT);
		numberTimestepsIgnored.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(final JComponent input) {
				if (!checkNumberTimestepsIgnored(true)){
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = numberTimestepsIgnored.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify an integer number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					if (errorPopup != null)
						errorPopup.hide();
					errorPopup = popupFactory.getPopup(numberTimestepsIgnored, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					
					errorPopup.show();
					
					return false;
				} else {
					if (errorPopup != null){
						errorPopup.hide();
						errorPopup = null;
					}
					return true;
				}
			}
		});
		numberTimestepsIgnored.setText(String.valueOf(Dashboard.NUMBER_OF_TIMESTEPS_TO_IGNORE));
		numberTimestepsIgnored.setToolTipText("The turn when the simulation should start charting specified as an integer value.");
		numberTimestepsIgnored.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				wizard.clickDefaultButton();
			}
		});

		numberTimestepsIgnoredPSW = new JFormattedTextField();
		numberTimestepsIgnoredPSW.setFocusLostBehavior(JFormattedTextField.COMMIT);
		numberTimestepsIgnoredPSW.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(final JComponent input) {
				if (!checkNumberTimestepsIgnored(false)){
					final PopupFactory popupFactory = PopupFactory.getSharedInstance();
					final Point locationOnScreen = numberTimestepsIgnoredPSW.getLocationOnScreen();
					final JLabel message = new JLabel("Please specify an integer number!");
					message.setBorder(new LineBorder(Color.RED, 2, true));
					if (errorPopup != null)
						errorPopup.hide();
					errorPopup = popupFactory.getPopup(numberTimestepsIgnoredPSW, message, locationOnScreen.x - 10, locationOnScreen.y - 30);
					
					errorPopup.show();
					
					return false;
				} else {
					if (errorPopup != null){
						errorPopup.hide();
						errorPopup = null;
					}
					return true;
				}
			}
		});
		numberTimestepsIgnoredPSW.setText(String.valueOf(Dashboard.NUMBER_OF_TIMESTEPS_TO_IGNORE));
		numberTimestepsIgnoredPSW.setToolTipText("The turn when the simulation should start charting specified as an integer value.");
		numberTimestepsIgnoredPSW.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				wizard.clickDefaultButton();
			}
		});
		
		onLineChartsCheckBox = new JCheckBox();
//		onLineChartsCheckBoxPSW = new JCheckBox();
		advancedChartsCheckBox = new JCheckBox();
		advancedChartsCheckBox.setSelected(true);
		
		// create the scroll pane for the simple parameter setting page
		JLabel label = null;
		label = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource(DECORATION_IMAGE)).getImage().getScaledInstance(DECORATION_IMAGE_WIDTH, -1, Image.SCALE_SMOOTH)));
		Style.registerCssClasses(label, Dashboard.CSS_CLASS_COMMON_PANEL);
		parametersScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		parametersScrollPane.setBorder(null);
		parametersScrollPane.setViewportBorder(null);
		
		breadcrumb = new Breadcrumb();
		Style.registerCssClasses(breadcrumb, CSS_ID_BREADCRUMB);
		
		singleRunParametersPanel = new ScrollableJPanel(new SizeProvider() {
			
			@Override
			public int getHeight() {
				Component component = tabbedPane.getSelectedComponent();
				if (component == null) {
					return 0;
				}
				JScrollBar scrollBar = parametersScrollPane.getHorizontalScrollBar();
				return component.getSize().height - breadcrumb.getHeight() - (scrollBar.isVisible() ? scrollBar.getPreferredSize().height : 0);
			}
			
			@Override
			public int getWidth() {
				final int hScrollBarWidth = parametersScrollPane.getHorizontalScrollBar().getPreferredSize().width;
				final int width = dashboard.getSize().width - Page_Parameters.DECORATION_IMAGE_WIDTH - hScrollBarWidth;
				return width;
			}
		});
		BoxLayout boxLayout = new BoxLayout(singleRunParametersPanel, BoxLayout.X_AXIS);
		singleRunParametersPanel.setLayout(boxLayout);
		parametersScrollPane.setViewportView(singleRunParametersPanel);

		JPanel breadcrumbPanel = new JPanel(new BorderLayout());
		breadcrumbPanel.add(breadcrumb, BorderLayout.NORTH);
		breadcrumbPanel.add(parametersScrollPane, BorderLayout.CENTER);
		
		final JPanel simpleForm = FormsUtils.build("p ~ p:g", 
				"01 t:p",
				label, breadcrumbPanel).getPanel();
		Style.registerCssClasses(simpleForm, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		tabbedPane.add("Single run", simpleForm);

		// create the form for the parameter sweep setting page
		tabbedPane.add("Parameter sweep", createParamsweepGUI());
		
		gaSearchHandler = new GASearchHandler(currentModelHandler);
		gaSearchPanel = new GASearchPanel(gaSearchHandler,this);
		tabbedPane.add("Genetic Algorithm Search", gaSearchPanel);
		
		return containerPanel;
	}

	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private JPanel createParamsweepGUI() {
		
		// left
		parameterList = new JList();
		parameterList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		new ListAction(parameterList,new AbstractAction() {
         public void actionPerformed(final ActionEvent event) {
				final AvailableParameter selectedParameter = (AvailableParameter) parameterList.getSelectedValue();
				addParameterToTree(new AvailableParameter[] { selectedParameter },parameterTreeBranches.get(0));
				enableDisableParameterCombinationButtons();
			}
		});
		parameterList.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
				if (!parameterList.isSelectionEmpty()) {
					boolean success = true;
					if (editedNode != null) 
						success = modify();
					
					if (success) {
						cancelAllSelectionBut(parameterList);
						resetSettings();
						updateDescriptionField(parameterList.getSelectedValues());
						enableDisableParameterCombinationButtons();
					} else
						parameterList.clearSelection();
				}
			}
		});
		
		final JScrollPane parameterListPane = new JScrollPane(parameterList);
		parameterListPane.setBorder(BorderFactory.createTitledBorder("")); // for rounded border
		parameterListPane.setPreferredSize(new Dimension(300,300));
		
		final JPanel parametersPanel = FormsUtils.build("p ' p:g",
										  "[DialogBorder]00||" + 
														"12||" +
														"34||" +
//														"56||" + 
														"55||" +
														"66 f:p:g",
														new FormsUtils.Separator("<html><b>General parameters</b></html>"),
														NUMBER_OF_TURNS_LABEL_TEXT,numberOfTurnsFieldPSW,
														NUMBER_OF_TIMESTEPS_TO_IGNORE_LABEL_TEXT,numberTimestepsIgnoredPSW,
//														UPDATE_CHARTS_LABEL_TEXT,onLineChartsCheckBoxPSW,
														new FormsUtils.Separator("<html><b>Model parameters</b></html>"),
														parameterListPane).getPanel();
		
		combinationsPanel = new JPanel(new GridLayout(0,1,5,5)); 
		combinationsScrPane = new JScrollPane(combinationsPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		combinationsScrPane.setBorder(null);
		combinationsScrPane.setPreferredSize(new Dimension(550,500));

		parameterDescriptionLabel = new JXLabel();
		parameterDescriptionLabel.setLineWrap(true);
		parameterDescriptionLabel.setVerticalAlignment(SwingConstants.TOP);
		final JScrollPane descriptionScrollPane = new JScrollPane(parameterDescriptionLabel);
		descriptionScrollPane.setBorder(BorderFactory.createTitledBorder(null,"Description",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		descriptionScrollPane.setPreferredSize(new Dimension(PARAMETER_DESCRIPTION_LABEL_WIDTH, PARAMETER_DESCRIPTION_LABEL_HEIGHT));
		descriptionScrollPane.setViewportBorder(null);
		
		final JButton addNewBoxButton = new JButton("Add new combination"); 
		addNewBoxButton.setActionCommand(ACTIONCOMMAND_ADD_BOX);
		
		final JPanel left = FormsUtils.build("p ~ f:p:g ~ p",
				 "011 f:p:g ||" +
				 "0_2 p",
				 parametersPanel,combinationsScrPane,
				 addNewBoxButton).getPanel();
		left.setBorder(BorderFactory.createTitledBorder(null,"Specify parameter combinations",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		Style.registerCssClasses(left, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel leftAndDesc = new JPanel(new BorderLayout());
		leftAndDesc.add(left,BorderLayout.CENTER);
		leftAndDesc.add(descriptionScrollPane,BorderLayout.SOUTH);
		Style.registerCssClasses(leftAndDesc,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		// right
		editedParameterText = new JLabel(ORIGINAL_TEXT);
		editedParameterText.setPreferredSize(new Dimension(280,40));
		constDef = new JRadioButton("Constant");
		listDef = new JRadioButton("List");
		incrDef = new JRadioButton("Increment");
		
		final JPanel rightTop = FormsUtils.build("p:g",
				   "[DialogBorder]0||" +
						   		 "1||" + 
					             "2||" +
					             "3",
	                editedParameterText,
					constDef,
					listDef,
					incrDef).getPanel();
		
		Style.registerCssClasses(rightTop, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		constDefField = new JTextField();
		final JPanel constDefPanel = FormsUtils.build("p ~ p:g",
								 "[DialogBorder]01 p",
								 "Constant value: ",CellConstraints.TOP,constDefField).getPanel();
		
		listDefArea = new JTextArea();
		final JScrollPane listDefScr = new JScrollPane(listDefArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		final JPanel listDefPanel = FormsUtils.build("p ~ p:g",
								"[DialogBorder]01|" +
								              "_1 f:p:g||" +
								              "_2 p",
								"Value list: ",listDefScr,
								"(Separate values with spaces!)").getPanel();
		
		incrStartValueField = new JTextField();
		incrEndValueField = new JTextField();
		incrStepField = new JTextField();
		
		final JPanel incrDefPanel = FormsUtils.build("p ~ p:g",
								"[DialogBorder]01||" +
											  "23||" +
											  "45",
											  "Start value: ",incrStartValueField,
											  "End value: ",incrEndValueField,
											  "Step: ",incrStepField).getPanel();
		
		enumDefBox = new JComboBox(new DefaultComboBoxModel());
		final JPanel enumDefPanel = FormsUtils.build("p ~ p:g", 
								"[DialogBorder]01 p", 
								"Constant value:", CellConstraints.TOP, enumDefBox).getPanel();
		
		submodelTypeBox = new JComboBox();
		final JPanel submodelTypePanel = FormsUtils.build("p ~ p:g",
											"[DialogBorder]01",
											"Constant value:", CellConstraints.TOP,submodelTypeBox).getPanel();
		
		fileTextField = new JTextField();
		fileTextField.addKeyListener(new KeyAdapter() {
			public void keyTyped(final KeyEvent e) {
				final char character = e.getKeyChar();
				final File file = new File(Character.isISOControl(character) ? fileTextField.getText() : fileTextField.getText() + character);
				fileTextField.setToolTipText(file.getAbsolutePath());
			}
		});
		fileBrowseButton = new JButton(BROWSE_BUTTON_TEXT);
		fileBrowseButton.setActionCommand(ACTIONCOMMAND_BROWSE);
		final JPanel fileDefPanel = FormsUtils.build("p ~ p:g ~p",
									   "[DialogBorder]012",
									   "File:",fileTextField,fileBrowseButton).getPanel();
		
		constDefPanel.setName("CONST");
		listDefPanel.setName("LIST");
		incrDefPanel.setName("INCREMENT");
		enumDefPanel.setName("ENUM");
		submodelTypePanel.setName("SUBMODEL");
		fileDefPanel.setName("FILE");
		
		Style.registerCssClasses(constDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		Style.registerCssClasses(listDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		Style.registerCssClasses(incrDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		Style.registerCssClasses(enumDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		Style.registerCssClasses(submodelTypePanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		Style.registerCssClasses(fileDefPanel,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		rightMiddle = new JPanel(new CardLayout());
		Style.registerCssClasses(rightMiddle, Dashboard.CSS_CLASS_COMMON_PANEL);
		rightMiddle.add(constDefPanel,constDefPanel.getName());
		rightMiddle.add(listDefPanel,listDefPanel.getName());
		rightMiddle.add(incrDefPanel,incrDefPanel.getName());
		rightMiddle.add(enumDefPanel, enumDefPanel.getName());
		rightMiddle.add(submodelTypePanel,submodelTypePanel.getName());
		rightMiddle.add(fileDefPanel,fileDefPanel.getName());
		
		modifyButton = new JButton("Modify");
		cancelButton = new JButton("Cancel");
		
		final JPanel rightBottom = FormsUtils.build("p:g p ~ p ~ p:g",
							  "[DialogBorder]_01_ p",
							  modifyButton,cancelButton).getPanel();
		
		Style.registerCssClasses(rightBottom, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final JPanel right = new JPanel(new BorderLayout());
		right.add(rightTop,BorderLayout.NORTH);
		right.add(rightMiddle,BorderLayout.CENTER);
		right.add(rightBottom,BorderLayout.SOUTH);
		right.setBorder(BorderFactory.createTitledBorder(null,"Parameter settings",TitledBorder.LEADING,TitledBorder.BELOW_TOP));

		Style.registerCssClasses(right, Dashboard.CSS_CLASS_COMMON_PANEL);

		// the whole paramsweep panel
		
		final JPanel content = FormsUtils.build("p:g p",
								   "01 f:p:g",
								   leftAndDesc,right).getPanel();
		Style.registerCssClasses(content, Dashboard.CSS_CLASS_COMMON_PANEL);
		
		sweepPanel = new JPanel();
		Style.registerCssClasses(sweepPanel, Dashboard.CSS_CLASS_COMMON_PANEL);
		sweepPanel.setLayout(new BorderLayout());
		final JScrollPane sp = new JScrollPane(content);
		sp.setBorder(null);
		sp.setViewportBorder(null);
		sweepPanel.add(sp,BorderLayout.CENTER);
		
		GUIUtils.createButtonGroup(constDef,listDef,incrDef);
		constDef.setSelected(true);
		constDef.setActionCommand("CONST");
		listDef.setActionCommand("LIST");
		incrDef.setActionCommand("INCREMENT");
		
		constDefField.setActionCommand("CONST_FIELD");
		incrStartValueField.setActionCommand("START_FIELD");
		incrEndValueField.setActionCommand("END_FIELD");
		incrStepField.setActionCommand("STEP_FIELD");
		
		modifyButton.setActionCommand("EDIT");
		cancelButton.setActionCommand("CANCEL");
		
		listDefArea.setLineWrap(true);
		listDefArea.setWrapStyleWord(true);
		listDefScr.setPreferredSize(new Dimension(100,200));
		
		GUIUtils.addActionListener(this,modifyButton,cancelButton,constDef,listDef,incrDef,constDefField,incrStartValueField,incrEndValueField,incrStepField,
								   addNewBoxButton,submodelTypeBox,fileBrowseButton);
		
		return sweepPanel;
	}
	
	//----------------------------------------------------------------------------------------------------
	private JPanel createAParameterBox(final boolean first) {
		
		final JLabel runLabel = new JLabel("<html><b>Number of runs:</b> 0</html>");
		final JLabel warningLabel = new JLabel();
		
		final JButton closeButton = new JButton();
		closeButton.setOpaque(false);
	    closeButton.setBorder(null);
	    closeButton.setFocusable(false);
	    
	    if (!first) {
		    closeButton.setRolloverIcon(PARAMETER_BOX_REMOVE);
		    closeButton.setRolloverEnabled(true);
		    closeButton.setIcon(RGBGrayFilter.getDisabledIcon(closeButton,PARAMETER_BOX_REMOVE));
		    closeButton.setActionCommand(ACTIONCOMMAND_REMOVE_BOX);
	    }
	    
	    final JScrollPane treeScrPane = new JScrollPane();
	    final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode();
	    final JTree tree = new JTree(treeRoot);
	    ToolTipManager.sharedInstance().registerComponent(tree);
	    
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new ParameterBoxTreeRenderer());
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				final TreePath selectionPath = tree.getSelectionPath();
				boolean success = true;
				if (editedNode != null && (selectionPath == null || !editedNode.equals(selectionPath.getLastPathComponent()))) 
					success = modify();
				
				if (success) {
					if (selectionPath != null) {
						cancelAllSelectionBut(tree);
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
						if (!node.equals(editedNode)) {
							ParameterInATree userObj = null;
							final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
							if (!node.isRoot() && selectionPath.getPathCount() == model.getPathToRoot(node).length) {
								userObj = (ParameterInATree) node.getUserObject();
								final ParameterInfo info = userObj.info;
								editedNode = node;
								editedTree = tree;
								edit(info);
							} else {
								tree.setSelectionPath(null);
								if (cancelButton.isEnabled())
									cancelButton.doClick();
								resetSettings();
								enableDisableSettings(false);
								editedNode = null;
								editedTree = null;
							}
							
							updateDescriptionField(userObj);
						} else
							updateDescriptionField();
						
					} else
						updateDescriptionField();
					
					enableDisableParameterCombinationButtons();
				} else {
					final DefaultTreeModel model = (DefaultTreeModel) editedTree.getModel();
					final DefaultMutableTreeNode storedEditedNode = editedNode;
					editedNode = null;
					tree.setSelectionPath(null);
					editedNode = storedEditedNode;
					editedTree.setSelectionPath(new TreePath(model.getPathToRoot(editedNode)));
				}
			}
		});

	    treeScrPane.setViewportView(tree);
	    treeScrPane.setBorder(null);
	    treeScrPane.setViewportBorder(null);
		treeScrPane.setPreferredSize(new Dimension(450,250));
	    
		final JButton upButton = new JButton();
		upButton.setOpaque(false);
		upButton.setRolloverEnabled(true);
		upButton.setIcon(PARAMETER_UP_ICON);
		upButton.setRolloverIcon(PARAMETER_UP_ICON_RO);
		upButton.setDisabledIcon(PARAMETER_UP_ICON_DIS);
		upButton.setBorder(null);
		upButton.setToolTipText("Move up the selected parameter");
		upButton.setActionCommand(ACTIONCOMMAND_MOVE_UP);
		
		final JButton downButton = new JButton();
		downButton.setOpaque(false);
		downButton.setRolloverEnabled(true);
		downButton.setIcon(PARAMETER_DOWN_ICON);
		downButton.setRolloverIcon(PARAMETER_DOWN_ICON_RO);
		downButton.setDisabledIcon(PARAMETER_DOWN_ICON_DIS);
		downButton.setBorder(null);
		downButton.setToolTipText("Move down the selected parameter");
		downButton.setActionCommand(ACTIONCOMMAND_MOVE_DOWN);

		
		final JPanel mainPanel = FormsUtils.build("~ f:p:g ~ p ~ r:p",
												  "012||" +
												  "333||" +
												  "44_||" + 
												  "445||" +
												  "446||" + 
												  "44_ f:p:g",
												  runLabel,first ? "" : warningLabel,first ? warningLabel : closeButton,
												  new FormsUtils.Separator(""),
												  treeScrPane,
												  upButton,
												  downButton).getPanel(); 
				
				
		mainPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		final JButton addButton = new JButton();
		addButton.setOpaque(false);
		addButton.setRolloverEnabled(true);
		addButton.setIcon(PARAMETER_ADD_ICON);
		addButton.setRolloverIcon(PARAMETER_ADD_ICON_RO);
		addButton.setDisabledIcon(PARAMETER_ADD_ICON_DIS);
		addButton.setBorder(null);
		addButton.setToolTipText("Add selected parameter");
		addButton.setActionCommand(ACTIONCOMMAND_ADD_PARAM);
		
		final JButton removeButton = new JButton();
		removeButton.setOpaque(false);
		removeButton.setRolloverEnabled(true);
		removeButton.setIcon(PARAMETER_REMOVE_ICON);
		removeButton.setRolloverIcon(PARAMETER_REMOVE_ICON_RO);
		removeButton.setDisabledIcon(PARAMETER_REMOVE_ICON_DIS);
		removeButton.setBorder(null);
		removeButton.setToolTipText("Remove selected parameter");
		removeButton.setActionCommand(ACTIONCOMMAND_REMOVE_PARAM);
		
		final JPanel result = FormsUtils.build("p ~ f:p:g",
											   "_0 f:p:g||" +
											   "10 p ||" +
											   "20 p||" +
											   "_0 f:p:g",
											   mainPanel,
											   addButton,
											   removeButton).getPanel();
		
		Style.registerCssClasses(result,Dashboard.CSS_CLASS_COMMON_PANEL);
		
		final ParameterCombinationGUI pcGUI = new ParameterCombinationGUI(tree,treeRoot,runLabel,warningLabel,addButton,removeButton,upButton,downButton);
		parameterTreeBranches.add(pcGUI);
		
		final ActionListener boxActionListener = new ActionListener() {
			
			//====================================================================================================
			// methods
			
			//----------------------------------------------------------------------------------------------------
			public void actionPerformed(final ActionEvent e) {
				final String cmd = e.getActionCommand();
				
				if (ACTIONCOMMAND_ADD_PARAM.equals(cmd)) 
					handleAddParameter(pcGUI);
				else if (ACTIONCOMMAND_REMOVE_PARAM.equals(cmd)) 
					handleRemoveParameter(tree);
				else if (ACTIONCOMMAND_REMOVE_BOX.equals(cmd)) 
					handleRemoveBox(tree);
				else if (ACTIONCOMMAND_MOVE_UP.equals(cmd)) 
					handleMoveUp();
				else if (ACTIONCOMMAND_MOVE_DOWN.equals(cmd))
					handleMoveDown();
			}

			//----------------------------------------------------------------------------------------------------
			private void handleAddParameter(final ParameterCombinationGUI pcGUI) {
				final Object[] selectedValues = parameterList.getSelectedValues();
				if (selectedValues != null && selectedValues.length > 0) {
					final AvailableParameter[] params = new AvailableParameter[selectedValues.length];
					System.arraycopy(selectedValues,0,params,0,selectedValues.length);
					addParameterToTree(params,pcGUI);
					enableDisableParameterCombinationButtons();
				}
			}
			
			//----------------------------------------------------------------------------------------------------
			private void handleRemoveParameter(final JTree tree) {
				final TreePath selectionPath = tree.getSelectionPath();
				if (selectionPath != null) {
					cancelButton.doClick();
					
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
					if (!node.isRoot()) {
						final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
						if (parentNode.isRoot()) { 
							removeParameter(tree,node,parentNode);
							enableDisableParameterCombinationButtons();
						}
					}
				}
			}
			
			//----------------------------------------------------------------------------------------------------
			private void handleRemoveBox(final JTree tree) {
				final int answer = Utilities.askUser(dashboard,false,"Comfirmation","This operation deletes the combination.", 
																			  		"All related parameter returns back to the list on the left side.",
																			  		"Are you sure?");
				if (answer == 1) {
					final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

					if (tree.getSelectionCount() > 0) {
						editedNode = null;
						tree.setSelectionPath(null);
						if (cancelButton.isEnabled())
							cancelButton.doClick();
					}
					
					final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
					for (int i = 0;i < root.getChildCount();++i) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
						removeParameter(tree,node,root);
					}
					
					enableDisableParameterCombinationButtons();

					parameterTreeBranches.remove(pcGUI);
					combinationsPanel.remove(result);
					combinationsPanel.revalidate();
					
					updateNumberOfRuns();
				}
			}
			
			//----------------------------------------------------------------------------------------------------
			private void removeParameter(final JTree tree, final DefaultMutableTreeNode node, final DefaultMutableTreeNode parentNode) {
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				final ParameterInfo originalInfo = findOriginalInfo(userObj.info);
				if (originalInfo != null) {
					final DefaultListModel model = (DefaultListModel) parameterList.getModel();
					model.addElement(new AvailableParameter(originalInfo, currentModelHandler.getModelClass()));
					final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
					treeModel.removeNodeFromParent(node);
					updateNumberOfRuns();
					tree.expandPath(new TreePath(treeModel.getPathToRoot(parentNode)));
				} else 
					throw new IllegalStateException("Parameter " + userObj.info.getName() + " is not found in the model.");
			}
			
			//----------------------------------------------------------------------------------------------------
			private void handleMoveUp() {
				final TreePath selectionPath = tree.getSelectionPath();
				if (selectionPath != null) {
					boolean success = true;
					if (editedNode != null) 
						success = modify();
					
					if (success) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
						final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
						
						if (parent == null || parent.getFirstChild().equals(node)) {
							tree.setSelectionPath(null); // we need this to preserve the state of the parameter settings panel
							tree.setSelectionPath(new TreePath(node.getPath()));
							return;
						}
						
						final int index = parent.getIndex(node);
						final DefaultTreeModel treemodel = (DefaultTreeModel) tree.getModel();
						treemodel.removeNodeFromParent(node);
						treemodel.insertNodeInto(node,parent,index - 1);
						tree.setSelectionPath(new TreePath(node.getPath()));
					}

				}
			}
				
			//----------------------------------------------------------------------------------------------------
			private void handleMoveDown() {
				final TreePath selectionPath = tree.getSelectionPath();
				if (selectionPath != null) {
					boolean success = true;
					if (editedNode != null) 
						success = modify();
					
					if (success) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
						final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
						
						if (parent == null || parent.getLastChild().equals(node)) {
							tree.setSelectionPath(null); // we need this to preserve the state of the parameter settings panel
							tree.setSelectionPath(new TreePath(node.getPath()));
							return;
						}
						
						final int index = parent.getIndex(node);
						final DefaultTreeModel treemodel = (DefaultTreeModel) tree.getModel();
						treemodel.removeNodeFromParent(node);
						treemodel.insertNodeInto(node,parent,index + 1);
						tree.setSelectionPath(new TreePath(node.getPath()));
					}
				}
			}
		};
		
		GUIUtils.addActionListener(boxActionListener,closeButton,upButton,downButton,addButton,removeButton);
		
		result.setPreferredSize(new Dimension(500,250));
		enableDisableParameterCombinationButtons();
		
		Style.apply(result, dashboard.getCssStyle());

		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private void addParameterToTree(final AvailableParameter[] parameters, final ParameterCombinationGUI pcGUI) {
		final DefaultListModel listModel = (DefaultListModel) parameterList.getModel();
		final DefaultTreeModel treeModel = (DefaultTreeModel) pcGUI.tree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		
		for (final AvailableParameter parameter : parameters) {
			listModel.removeElement(parameter);
			final ParameterInfo selectedInfo = parameter.info;
			final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new ParameterInATree(selectedInfo, currentModelHandler.getModelClass()));
			treeModel.insertNodeInto(newNode,root,root.getChildCount());
			
			if (selectedInfo instanceof SubmodelInfo) {
				final SubmodelInfo sInfo = (SubmodelInfo) selectedInfo;
				if (sInfo.getActualType() != null) {
					addSubParametersToTree(sInfo,pcGUI.tree,newNode);
					pcGUI.tree.expandPath(new TreePath(treeModel.getPathToRoot(newNode)));				
				}
			}
		}

		updateNumberOfRuns();
		pcGUI.tree.expandPath(new TreePath(treeModel.getPathToRoot(root)));
	}

 
	//----------------------------------------------------------------------------------------------------
	public void resetParamsweepGUI(){
		changeText(null, null);
		constDef.setSelected(true);
		constDefField.setText(null);
		incrStartValueField.setText(null);
		incrEndValueField.setText(null);
		incrStepField.setText(null);
		listDefArea.setText(null);
		fileTextField.setText(null);
		fileTextField.setToolTipText(null);
		combinationsPanel.removeAll();
		parameterTreeBranches.clear();
		combinationsPanel.add(createAParameterBox(true));
		combinationsPanel.revalidate();
	}
	
	//----------------------------------------------------------------------------------------------------
	/** {@inheritDoc} 
	 */
	@Override
	public boolean isEnabled(final Button button) {
		switch (button){
		case BACK:
			return true;
			
		case NEXT:
			return true;
			
		case CANCEL: // this is the charts page
			return true;
			
		case FINISH:
			return false;
			
		case CUSTOM:
			return false;
		}
		return false;
	}

	//----------------------------------------------------------------------------------------------------
	/** {@inheritDoc} 
	 */
	@Override
	public boolean onButtonPress(final Button button) {
		if (Button.NEXT.equals(button)) {
			ParameterTree parameterTree = null;
			if (tabbedPane.getSelectedIndex() == SINGLE_RUN_GUI_TABINDEX) {
				currentModelHandler.setIntelliMethodPlugin(null);
				parameterTree = new ParameterTree();
				final Set<ParameterInfo> invalids = new HashSet<ParameterInfo>();
				
				@SuppressWarnings("rawtypes")
				final Enumeration treeValues = parameterValueComponentTree.breadthFirstEnumeration();
				treeValues.nextElement(); // root element
				while (treeValues.hasMoreElements()) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeValues.nextElement();
					@SuppressWarnings("unchecked")
					final Pair<ParameterInfo,JComponent> userData = (Pair<ParameterInfo,JComponent>) node.getUserObject();
					final ParameterInfo parameterInfo = userData.getFirst();
					final JComponent valueContainer = userData.getSecond();
					
					if (parameterInfo instanceof ISubmodelGUIInfo) {
						final ISubmodelGUIInfo sgi = (ISubmodelGUIInfo) parameterInfo;
						if (sgi.getParent() != null) {
							final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
							@SuppressWarnings("unchecked")
							final Pair<ParameterInfo,JComponent> parentUserData = (Pair<ParameterInfo,JComponent>) parentNode.getUserObject();
							final SubmodelInfo parentParameterInfo = (SubmodelInfo) parentUserData.getFirst();
							if (invalids.contains(parentParameterInfo) || !classEquals(parentParameterInfo.getActualType(),sgi.getParentValue())) {
								invalids.add(parameterInfo);
								continue;
							}
						}
					}
					
					if (parameterInfo.isBoolean()){
						final JCheckBox checkBox = (JCheckBox) valueContainer;
						parameterInfo.setValue(checkBox.isSelected());
					} else if (parameterInfo.isEnum()) {
						final JComboBox comboBox = (JComboBox) valueContainer;
						parameterInfo.setValue(comboBox.getSelectedItem());
					} else if (parameterInfo instanceof MasonChooserParameterInfo) {
						final JComboBox comboBox = (JComboBox) valueContainer;
						parameterInfo.setValue(comboBox.getSelectedIndex());
					} else if (parameterInfo instanceof SubmodelInfo) {
						// we don't need the SubmodelInfo parameters anymore (all descendant parameters are in the tree too)
						// but we need to check that an actual type is provided
						final SubmodelInfo smi = (SubmodelInfo) parameterInfo;
						final JComboBox comboBox = (JComboBox) ((JPanel)valueContainer).getComponent(0);
						final ClassElement selected = (ClassElement) comboBox.getSelectedItem();
						smi.setActualType(selected.clazz, selected.instance);
						
						if (smi.getActualType() == null) {
							final String errorMsg = "Please select a type from the dropdown list of " + smi.getName().replaceAll("([A-Z])"," $1");
							JOptionPane.showMessageDialog(wizard, new JLabel(errorMsg), "Error", JOptionPane.ERROR_MESSAGE);
							return false;
						}
						
						//continue;
					} else if (parameterInfo.isFile()) {
						final JTextField textField = (JTextField) valueContainer.getComponent(0);
						if (textField.getText().trim().isEmpty())
							log.warn("Empty string was specified as file parameter " + parameterInfo.getName().replaceAll("([A-Z])"," $1"));
						
						final File file = new File(textField.getToolTipText());
						if (!file.exists()) {
							final String errorMsg = "Please specify an existing file parameter " + parameterInfo.getName().replaceAll("([A-Z])"," $1");
							JOptionPane.showMessageDialog(wizard,new JLabel(errorMsg),"Error",JOptionPane.ERROR_MESSAGE);
							return false;
						}
						
						parameterInfo.setValue(ParameterInfo.getValue(textField.getToolTipText(),parameterInfo.getType()));
					} else if (parameterInfo instanceof MasonIntervalParameterInfo) {
						final JTextField textField = (JTextField) valueContainer.getComponent(0);
						parameterInfo.setValue(ParameterInfo.getValue(textField.getText().trim(),parameterInfo.getType()));
					} else {
						final JTextField textField = (JTextField) valueContainer;
						parameterInfo.setValue(ParameterInfo.getValue(textField.getText(),parameterInfo.getType()));
						if ("String".equals(parameterInfo.getType()) && parameterInfo.getValue() == null)
							parameterInfo.setValue(textField.getText().trim());
					}
					
					final AbstractParameterInfo<?> batchParameterInfo = InfoConverter.parameterInfo2ParameterInfo(parameterInfo);
					parameterTree.addNode(batchParameterInfo);
				}
				
				dashboard.setOnLineCharts(onLineChartsCheckBox.isSelected());
				dashboard.setDisplayAdvancedCharts(advancedChartsCheckBox.isSelected());
			}
			
			if (tabbedPane.getSelectedIndex() == PARAMSWEEP_GUI_TABINDEX) {
				currentModelHandler.setIntelliMethodPlugin(null);
				boolean success = true;
				if (editedNode != null) 
					success = modify();
				
				if (success) {
					String invalidInfoName = checkInfos(true); 
					if (invalidInfoName != null) {
						Utilities.userAlert(sweepPanel,"Please select a type from the dropdown list of " + invalidInfoName);
						return false;
					}
					
					invalidInfoName = checkInfos(false);
					if (invalidInfoName != null) {
						Utilities.userAlert(sweepPanel,"Please specify a file for parameter " + invalidInfoName);
						return false;
					}
					
					if (needWarning()) {
						final int result = Utilities.askUser(sweepPanel,false,"Warning","There are two or more combination boxes that contains non-constant parameters." +
													   " Parameters are unsynchronized:", "simulation may exit before all parameter values are assigned.",
													   " ","To explore all possible combinations you must use only one combination box.",
													   " ","Do you want to run simulation with these parameter settings?");  
						if (result == 0) {
							return false;
						}
					}
					
					try {
							parameterTree = createParameterTreeFromParamSweepGUI();
							
							final ParameterNode rootNode = parameterTree.getRoot();
							dumpParameterTree(rootNode);
							
	//						dashboard.setOnLineCharts(onLineChartsCheckBoxPSW.isSelected());
					} catch (final ModelInformationException e) {
						JOptionPane.showMessageDialog(wizard, new JLabel(e.getMessage()), "Error while creating runs", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
						return false;
					}
				} else return false;
			}
			
			if (tabbedPane.getSelectedIndex() == GASEARCH_GUI_TABINDEX) {
				final IIntelliDynamicMethodPlugin gaPlugin = (IIntelliDynamicMethodPlugin)gaSearchHandler;
				currentModelHandler.setIntelliMethodPlugin(gaPlugin);
				boolean success = gaSearchPanel.closeActiveModification();
				
				if (success) {
					String invalidInfoName = checkInfosInGeneTree(); 
					if (invalidInfoName != null) {
						Utilities.userAlert(sweepPanel,"Please select a type from the dropdown list of " + invalidInfoName);
						return false;
					} 
					
					final String[] errors = gaSearchHandler.checkGAModel();
					if (errors != null) {
						Utilities.userAlert(sweepPanel,(Object[])errors);
						return false;
					}
					
					final DefaultMutableTreeNode parameterTreeRootNode = new DefaultMutableTreeNode();
					final IIntelliContext ctx = new DashboardIntelliContext(parameterTreeRootNode,gaSearchHandler.getChromosomeTree());
					gaPlugin.alterParameterTree(ctx);
					parameterTree = InfoConverter.node2ParameterTree(parameterTreeRootNode);
					dashboard.setOptimizationDirection(gaSearchHandler.getFitnessFunctionDirection());
					
				} else return false;
			}

			currentModelHandler.setParameters(parameterTree);
		}
		
		return true;
	}

	//----------------------------------------------------------------------------------------------------
	public Model saveConfiguration() throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		
		final Model result = factory.createModel();
		result.setClazz(currentModelHandler.getModelClass().getName());
		
		if (tabbedPane.getSelectedIndex() == SINGLE_RUN_GUI_TABINDEX) {
			result.setRunStrategy(ModelType.SINGLE);
			
			final List<GeneralParameter> generalParameters = result.getGeneralParameterList();
			
			GeneralParameter gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME);
			gp.setValue(numberOfTurnsField.getText());
			generalParameters.add(gp);
			
			gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.NUMBER_OF_TIMESTEPS_TO_IGNORE_XML_NAME);
			gp.setValue(numberTimestepsIgnored.getText());
			generalParameters.add(gp);
			
			gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.UPDATE_CHARTS_XML_NAME);
			gp.setValue(String.valueOf(onLineChartsCheckBox.isSelected()));
			generalParameters.add(gp);
			
			gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.DISPLAY_ADVANCED_CHARTS_XML_NAME);
			gp.setValue(String.valueOf(advancedChartsCheckBox.isSelected()));
			generalParameters.add(gp);
			
			final Set<ParameterInfo> invalids = new HashSet<ParameterInfo>();
			
			final List<Parameter> parameterList = result.getParameterList();
			final List<SubmodelParameter> submodelParameterList = result.getSubmodelParameterList();
			
			@SuppressWarnings("rawtypes")
			final Enumeration firstLevelParameters = parameterValueComponentTree.children();
			saveSubTree(firstLevelParameters,parameterList,submodelParameterList,invalids);
		} else if (tabbedPane.getSelectedIndex() == PARAMSWEEP_GUI_TABINDEX) {
			result.setRunStrategy(ModelType.PARAMETER_SWEEP);
			
			//calculate the number of runs, which is the minimum of the number of runs in the parameter combination boxes
			int validMin = Integer.MAX_VALUE;
			for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
				final int run = Page_Parameters.calculateNumberOfRuns(pGUI.combinationRoot);
				if (run> 1 && run< validMin)
					validMin = run;
			}
			
			result.setNumberOfRuns(validMin);
			
			final List<GeneralParameter> generalParameters = result.getGeneralParameterList();
			
			GeneralParameter gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME);
			gp.setValue(numberOfTurnsFieldPSW.getText());
			generalParameters.add(gp);
			
			gp = factory.createGeneralParameter();
			gp.setName(ParameterTreeUtils.NUMBER_OF_TIMESTEPS_TO_IGNORE_XML_NAME);
			gp.setValue(numberTimestepsIgnoredPSW.getText());
			generalParameters.add(gp);
			
			savePSParameterList(factory,result);
			
			for (int i = 0;i < parameterTreeBranches.size();++i) 
				savePSBranch(factory,result,parameterTreeBranches.get(i),i + 1);
		} else {
			result.setRunStrategy(ModelType.GA);
			gaSearchHandler.saveConfiguration(factory,result);
		}
		
		return result;
	}

	//----------------------------------------------------------------------------------------------------
	private void saveSubTree(@SuppressWarnings("rawtypes") final Enumeration children, final List<Parameter> parameterList,
							 final List<SubmodelParameter> submodelParameterList, final Set<ParameterInfo> invalids) throws IOException {
		
		final ObjectFactory factory = new ObjectFactory();
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			
			@SuppressWarnings("unchecked")
			final Pair<ParameterInfo,JComponent> userData = (Pair<ParameterInfo,JComponent>) node.getUserObject();
			final ParameterInfo parameterInfo = userData.getFirst();
			final JComponent valueContainer = userData.getSecond();
			
			Parameter parameter = null;
			SubmodelParameter submodelParameter = null;
			if (parameterInfo instanceof ISubmodelGUIInfo) {
				final ISubmodelGUIInfo sgi = (ISubmodelGUIInfo) parameterInfo;
				if (sgi.getParent() != null) {
					final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
					@SuppressWarnings("unchecked")
					final Pair<ParameterInfo,JComponent> parentUserData = (Pair<ParameterInfo,JComponent>) parentNode.getUserObject();
					final SubmodelInfo parentParameterInfo = (SubmodelInfo) parentUserData.getFirst();
					if (invalids.contains(parentParameterInfo) || !classEquals(parentParameterInfo.getActualType(),sgi.getParentValue())) {
						invalids.add(parameterInfo);
						continue;
					}
				}
			}
			
			if (parameterInfo instanceof SubmodelInfo) {
				submodelParameter = factory.createSubmodelParameter();
				submodelParameter.setName(parameterInfo.getName());
			} else {
				parameter = factory.createParameter();
				parameter.setName(parameterInfo.getName());
			}
			
			if (parameterInfo.isBoolean()){
				final JCheckBox checkBox = (JCheckBox) valueContainer;
				parameter.getContent().add(String.valueOf(checkBox.isSelected()));
			} else if (parameterInfo.isEnum()) {
				final JComboBox comboBox = (JComboBox) valueContainer;
				parameter.getContent().add(String.valueOf(comboBox.getSelectedItem()));
			} else if (parameterInfo instanceof MasonChooserParameterInfo) {
				final JComboBox comboBox = (JComboBox) valueContainer;
				parameter.getContent().add(String.valueOf(comboBox.getSelectedIndex()));
			} else if (parameterInfo instanceof SubmodelInfo) {
				final JComboBox comboBox = (JComboBox) ((JPanel)valueContainer).getComponent(0);
				final ClassElement selectedElement = (ClassElement) comboBox.getSelectedItem();
				
				if (selectedElement.clazz == null)
					throw new IOException("No type is selected for parameter " + parameterInfo.getName().replaceAll("([A-Z])", " $1") + ".");
				
				submodelParameter.setType(selectedElement.clazz.getName());
				saveSubTree(node.children(),submodelParameter.getParameterList(),submodelParameter.getSubmodelParameterList(),invalids);
			} else if (parameterInfo.isFile()) {
				final JTextField textField = (JTextField) valueContainer.getComponent(0);
				parameter.getContent().add(textField.getToolTipText());
			} else if (parameterInfo instanceof MasonIntervalParameterInfo) {
				final JTextField textField = (JTextField) valueContainer.getComponent(0);
				parameter.getContent().add(textField.getText().trim());
			} else {
				final JTextField textField = (JTextField) valueContainer;
				String text = String.valueOf(ParameterInfo.getValue(textField.getText(),parameterInfo.getType()));
				if ("String".equals(parameterInfo.getType()) && parameterInfo.getValue() == null)
					text = textField.getText().trim();
				parameter.getContent().add(text);
			}
			
			if (parameter != null)
				parameterList.add(parameter);
			else if (submodelParameter != null)
				submodelParameterList.add(submodelParameter);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void savePSParameterList(final ObjectFactory factory, final Model model) {
		final DefaultListModel listModel = (DefaultListModel) parameterList.getModel();
		
		if (!listModel.isEmpty()) {
			for (int i = 0;i < listModel.getSize();++i) {
				final AvailableParameter availableParameter = (AvailableParameter) listModel.get(i);
				
				final DefaultParameter defaultParameter = factory.createDefaultParameter();
				defaultParameter.setName(availableParameter.info.getName());
				model.getDefaultParameterList().add(defaultParameter);
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void savePSBranch(final ObjectFactory factory, final Model model, final ParameterCombinationGUI combinationGUI, final int position) {
		final Combination combination = factory.createCombination();
		combination.setPosition(BigInteger.valueOf(position));
		model.getCombination().add(combination);
		
		int pPosition = 1;
		@SuppressWarnings("rawtypes")
		final Enumeration paramNodes = combinationGUI.combinationRoot.children();
		while (paramNodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) paramNodes.nextElement();
			savePSParameter(node,factory,combination,pPosition++);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void savePSParameter(final DefaultMutableTreeNode node, final ObjectFactory factory, final Object parent, final int position) {
		final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
		final ParameterInfo info = userObj.info;
		
		if (info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) info;
			
			final SubmodelParameter parameter = factory.createSubmodelParameter();
			parameter.setName(info.getName());
			parameter.setType(sInfo.getActualType() == null ? "null" : sInfo.getActualType().getName());
			parameter.setPosition(BigInteger.valueOf(position));
			
			if (parent instanceof Combination) {
				final Combination combination = (Combination) parent;
				combination.getSubmodelParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getSubmodelParameterList().add(parameter);
			}
			
			if (node.getChildCount() > 0) {
				@SuppressWarnings("rawtypes")
				final Enumeration childNodes = node.children();
				int childPosition = 1;
				while (childNodes.hasMoreElements()) {
					final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
					savePSParameter(childNode,factory,parameter,childPosition++);
 				}
			}
		} else {
			final Parameter parameter = factory.createParameter();
			parameter.setName(info.getName());
			parameter.setPosition(BigInteger.valueOf(position));
			
			switch (info.getDefinitionType()) {
			case ParameterInfo.CONST_DEF:	
				parameter.setParameterType(ParameterType.CONSTANT);
				parameter.getContent().add(info.getValue().toString());
				break;
				
			case ParameterInfo.LIST_DEF:
				parameter.setParameterType(ParameterType.LIST);
				savePSListParameter(info,parameter,factory);
				break;
			
			case ParameterInfo.INCR_DEF:
				parameter.setParameterType(ParameterType.INCREMENT);
				savePSIncrementParameter(info,parameter,factory);
				break;
			}
			
			if (parent instanceof Combination) {
				final Combination combination = (Combination) parent;
				combination.getParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getParameterList().add(parameter);
			}
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void savePSListParameter(final ParameterInfo info, final Parameter parameter, final ObjectFactory factory) {
		for (final Object listElement : info.getValues()) {
			final JAXBElement<String> element = factory.createParameterParameterValue(listElement.toString());
			parameter.getContent().add(element);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void savePSIncrementParameter(final ParameterInfo info, final Parameter parameter, final ObjectFactory factory) {
		JAXBElement<String> element = factory.createParameterStartValue(info.getStartValue().toString());
		parameter.getContent().add(element);
		element = factory.createParameterEndValue(info.getEndValue().toString());
		parameter.getContent().add(element);
		element = factory.createParameterStepValue(info.getStep().toString());
		parameter.getContent().add(element);
	}

	//----------------------------------------------------------------------------------------------------
	public void loadConfiguration(final Model model) {
		try {
			currentModelHandler = dashboard.getModelHandler();
			singleRunParametersPanel.removeAll();
			
			parameterValueComponentTree = new DefaultMutableTreeNode();
			possibleTypesCache.clear();
			batchParameters = currentModelHandler.getParameters();
			
			final ModelType runStrategy = model.getRunStrategy();

			if (ModelType.SINGLE == runStrategy)
				fillParameterValueComponentTree(model);
				
			final List<ParameterInfo> modelParameters = createAndDisplayAParameterPanel(
			   batchParameters, "Model parameters", null, true, currentModelHandler);

			parametersScrollPane.setViewportView(singleRunParametersPanel);
			singleRunParametersPanel.revalidate();
			

			if (ModelType.SINGLE == runStrategy) {
				tabbedPane.setSelectedIndex(SINGLE_RUN_GUI_TABINDEX);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						numberOfTurnsField.grabFocus();
					}
				});
			}
				
			// initialize paramsweep panel
			Collections.sort(modelParameters);
			enableDisableSettings(false);
			modifyButton.setEnabled(false);
			resetParamsweepGUI();
			
			if (ModelType.PARAMETER_SWEEP == runStrategy) {
				fillParameterSweepPage(model);
				tabbedPane.setSelectedIndex(PARAMSWEEP_GUI_TABINDEX);
			} else 
				createDefaultParameterList(modelParameters);
			
			updateNumberOfRuns();
			
			// initialize the GA seach panel
			gaSearchHandler.init(currentModelHandler);
			gaSearchHandler.removeAllFitnessFunctions();
			final List<RecordableInfo> recordables = dashboard.getModelHandler().getRecorders().get(0).getRecordables();
			for (final RecordableInfo recordableInfo : recordables) {
				if (gaSearchHandler.canBeFitnessFunction(recordableInfo))
					gaSearchHandler.addFitnessFunction(recordableInfo);
			}
			
			if (ModelType.GA == runStrategy) {
				gaSearchHandler.loadConfiguration(model,modelParameters);
				tabbedPane.setSelectedIndex(GASEARCH_GUI_TABINDEX);
//				gaSearchPanel.setSelectedFitnessFunction(gaSearchHandler); this is handled by gaSearchPanel.reset()
			}
			
			gaSearchPanel.reset(gaSearchHandler);
		} catch (final ModelInformationException e) {
			JOptionPane.showMessageDialog(wizard, new JLabel(e.getMessage()), "Error while analyizing model and/or configuration file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} finally {
			((CardLayout)container.getLayout()).show(container, PARAMETERS_PANEL_ID);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void fillParameterValueComponentTree(final Model model) throws ModelInformationException {
		final List<GeneralParameter> generalParameters = model.getGeneralParameterList();
		for (final GeneralParameter gp : generalParameters) {
			if (NUMBER_OF_TURNS_LABEL_TEXT.equals(gp.getName()))
				numberOfTurnsField.setText(gp.getValue());
			else if (NUMBER_OF_TIMESTEPS_TO_IGNORE_LABEL_TEXT.equals(gp.getName()))
				numberTimestepsIgnored.setText(gp.getValue());
			else if (UPDATE_CHARTS_LABEL_TEXT.equals(gp.getName())) {
				final boolean selected = Boolean.parseBoolean(gp.getValue());
				onLineChartsCheckBox.setSelected(selected);
			} else if (DISPLAY_ADVANCED_CHARTS_LABEL_TEXT.equals(gp.getName())) {
				final boolean selected = Boolean.parseBoolean(gp.getValue());
				advancedChartsCheckBox.setSelected(selected);
			}
		}
		
		final DefaultMutableTreeNode infoValueTree = ParameterTreeUtils.loadSingleRunParameterIntoInfoValueTree(model,batchParameters,currentModelHandler);
		parameterValueComponentTree = convertInfoValueTreeToInfoComponentTree(infoValueTree);
	}
	
	//----------------------------------------------------------------------------------------------------
	private DefaultMutableTreeNode convertInfoValueTreeToInfoComponentTree(DefaultMutableTreeNode infoValueTree) throws ModelInformationException {
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = infoValueTree.breadthFirstEnumeration();
		nodes.nextElement(); // root
		
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			if (node.getUserObject() != null) {
				@SuppressWarnings("unchecked")
				final Pair<ParameterInfo,String> userObj = (Pair<ParameterInfo,String>) node.getUserObject();
				final JComponent component = convertStringToComponent(userObj.getKey(),userObj.getValue());
				final Pair<ParameterInfo,JComponent> newUserObj = new Pair<ParameterInfo,JComponent>(userObj.getKey(),component);
				node.setUserObject(newUserObj);
			}
		}
		
		return infoValueTree;
	}

	//----------------------------------------------------------------------------------------------------
	private JComponent convertStringToComponent(final ParameterInfo info, final String value) throws ModelInformationException {
		if (info instanceof SubmodelInfo)
			return initializeJComponentForSubmodelParameter(value,(SubmodelInfo)info);
		
		return initializeJComponentForParameter(value,info);
	}

	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private JComponent initializeJComponentForParameter(final String strValue, final ParameterInfo info) throws ModelInformationException {
		JComponent field = null;
		
		if (info.isBoolean()){
			field = new JCheckBox();
			boolean value = Boolean.parseBoolean(strValue); 
			((JCheckBox)field).setSelected(value);
		} else if (info.isEnum() || info instanceof MasonChooserParameterInfo) {
			Object[] elements = null;
			if (info.isEnum()){
				final Class<Enum<?>> type = (Class<Enum<?>>) info.getJavaType();
				elements = type.getEnumConstants();
			} else {
				final MasonChooserParameterInfo chooserInfo = (MasonChooserParameterInfo) info;
				elements = chooserInfo.getValidStrings();
			}
			final JComboBox list = new JComboBox(elements);

			if (info.isEnum()) {
				try {
					@SuppressWarnings("rawtypes")
					final Object value = Enum.valueOf((Class<? extends Enum>)info.getJavaType(),strValue);
					list.setSelectedItem(value);
				} catch (final IllegalArgumentException e) {
					throw new ModelInformationException(e.getMessage() + " for parameter: " + info.getName() + ".");
				}
			} else {
				try {
					final int value = Integer.parseInt(strValue);
					list.setSelectedIndex(value);
				} catch (final NumberFormatException e) {
					throw new ModelInformationException("Invalid value for parameter " + info.getName() + " (not a number).");
				}
			}
			field = list;
		} else if (info.isFile()) {
			field = new JPanel();
			final JTextField textField = new JTextField();
			
			if (!strValue.isEmpty()) {
				final File file = new File(strValue);
				textField.setText(file.getName());
				textField.setToolTipText(file.getAbsolutePath());
			}
			
			field.add(textField);
		} else if (info instanceof MasonIntervalParameterInfo) {
			field = new JPanel();
			final JTextField textField = new JTextField(strValue);
			field.add(textField);
		} else {
			field = new JTextField(strValue);
		}
		
		return field;
	}
	
	//----------------------------------------------------------------------------------------------------
	private JComponent initializeJComponentForSubmodelParameter(final String type, final SubmodelInfo submodelInfo) throws ModelInformationException {
		try {
			final Class<?> clazz = Class.forName(type,true,currentModelHandler.getCustomClassLoader());
			final Object value = new ClassElement(clazz, null);
			submodelInfo.setActualType(clazz, null);
			final Object[] elements = new Object[] { value }; 
			final JComboBox list = new JComboBox(elements);
			list.setSelectedIndex(0);
			JPanel panel = new JPanel();
			panel.add(list);
			
			return panel;
		} catch (final ClassNotFoundException e) {
			throw new ModelInformationException("Invalid actual type " + type + " for submodel parameter: " + submodelInfo.getName(),e);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void fillParameterSweepPage(final Model model) throws ModelInformationException {
		final List<GeneralParameter> generalParameters = model.getGeneralParameterList();
		for (final GeneralParameter gp : generalParameters) {
			if (NUMBER_OF_TURNS_LABEL_TEXT.equals(gp.getName()))
				numberOfTurnsFieldPSW.setText(gp.getValue());
			else if (NUMBER_OF_TIMESTEPS_TO_IGNORE_LABEL_TEXT.equals(gp.getName()))
				numberTimestepsIgnoredPSW.setText(gp.getValue());
		}
		
		final DefaultListModel listModel = new DefaultListModel();
		final List<DefaultParameter> defaultParameters = model.getDefaultParameterList();
		ParameterTreeUtils.loadPSDefaultParameters(listModel,defaultParameters,batchParameters, currentModelHandler.getModelClass());
		parameterList.setModel(listModel);
		
		// creating combination boxes
		for (int i = 0;i < model.getCombination().size() - 1;addBox(),++i);
		
		final List<Combination> sortedCombinations = new ArrayList<Combination>(model.getCombination());
		Collections.sort(sortedCombinations,new Comparator<Combination>(){
			public int compare(final Combination o1, final Combination o2) {
				return o1.getPosition().compareTo(o2.getPosition());
			}
		});
		
		for (int i = 0;i < sortedCombinations.size();++i) {
			final ParameterCombinationGUI pGUI = parameterTreeBranches.get(i);
			final DefaultTreeModel treeModel = (DefaultTreeModel) pGUI.tree.getModel();
			final DefaultMutableTreeNode combinationRoot = pGUI.combinationRoot;
			ParameterTreeUtils.loadAPSCombinationTree(treeModel,combinationRoot,batchParameters,currentModelHandler,sortedCombinations.get(i));
			
			for (int j = 0;j < pGUI.tree.getRowCount();++j) 
		         pGUI.tree.expandRow(j);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void dumpParameterTree(final ParameterNode rootNode) {
		dumpParameterTreeBranch(rootNode, "");
	}
	
	//----------------------------------------------------------------------------------------------------
	private void dumpParameterTreeBranch(final ParameterNode node, final String prefix){
		@SuppressWarnings("unchecked")
		final Enumeration<ParameterNode> children = node.children();
		
		while (children.hasMoreElements()) {
			final ParameterNode child = children.nextElement();
			final ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo<?> info = (ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo<?>) child.getUserObject();
			if (info instanceof IncrementalParameterInfo<?>) {
				final IncrementalParameterInfo<?> incrementalInfo = (IncrementalParameterInfo<?>) info;
				log.debug(prefix + info.getName() + ": start = " + incrementalInfo.getStart() + "; end = " + incrementalInfo.getEnd() + "; increment = " + incrementalInfo.getIncrement() + "; run = " + incrementalInfo.getRunNumber());
			} else if (info instanceof ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>) {
				final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> simpleInfo = (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>) info;
				log.debug(prefix + info.getName() + ": " + simpleInfo.getValues() + "; run = " + simpleInfo.getRunNumber());
			} else {
				throw new IllegalStateException("Trying to dump unknown parameterinfo: " + info);
			}
			
			if (info.getMultiplicity() > 1) {
				dumpParameterTreeBranch(child, prefix + "\t");
			}
		}
	}

	//----------------------------------------------------------------------------------------------------
	/** {@inheritDoc} 
	 */
	@Override
	public void onPageChange(final boolean show) {
		if (show){
			dashboard.getSaveConfigMenuItem().setEnabled(true);
			if (currentModelHandler == null || !currentModelHandler.equals(dashboard.getModelHandler())){
				((CardLayout)container.getLayout()).show(container, LOADING_MODEL_ID);
			
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							currentModelHandler = dashboard.getModelHandler();
							
							breadcrumb.setRoot(
							   currentModelHandler.getModelClassSimpleName().replaceAll("([A-Z])", " $1"));
							Style.apply(breadcrumb, dashboard.getCssStyle());
							parameterValueComponentTree = new DefaultMutableTreeNode();
							possibleTypesCache.clear();
							singleRunParametersPanel.removeAll();
							batchParameters = currentModelHandler.getParameters();
							final List<ParameterInfo> modelParameters = createAndDisplayAParameterPanel(
							   batchParameters,"Model parameters",null, true,
							   currentModelHandler
							   );

							//parametersScrollPane.setViewportView(singleRunParametersPanel);

							singleRunParametersPanel.revalidate();

							// initialize paramsweep panel
							Collections.sort(modelParameters);
							createDefaultParameterList(modelParameters);
							enableDisableSettings(false);
							modifyButton.setEnabled(false);
							updateNumberOfRuns();
							resetParamsweepGUI();

							// initialize the GA seach panel
							gaSearchHandler.init(currentModelHandler);
							gaSearchPanel.reset(gaSearchHandler);
							gaSearchHandler.removeAllFitnessFunctions();
							final List<RecordableInfo> recordables = dashboard.getModelHandler().getRecorders().get(0).getRecordables();
							for (final RecordableInfo recordableInfo : recordables) {
								if (gaSearchHandler.canBeFitnessFunction(recordableInfo))
									gaSearchHandler.addFitnessFunction(recordableInfo);
							}

							gaSearchHandler.removeAllParameters();
							for (final ParameterInfo parameterInfo : modelParameters) 
								gaSearchHandler.addParameter(new ParameterOrGene(parameterInfo));
						} catch (final ModelInformationException e) {
							JOptionPane.showMessageDialog(wizard, new JLabel(e.getMessage()), "Error while analyizing model", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						} finally {
							((CardLayout)container.getLayout()).show(container, PARAMETERS_PANEL_ID);
						}
					}
				});
			}
		} else { // this should only run when moving onto Page_Run
		}

	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public String getTitle() {
		return TITLE;
	}

   //-----------------------------------------------------------------------------------
   
   private final static class ParameterMetaData implements Comparable<ParameterMetaData> {
      String
         banner = "",
         verboseDescription = "",
         fieldName = "",
         imageFileName = "";
      double
         layoutOrder = Double.MIN_VALUE;
      ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>
         parameter;
      @Override
      public int compareTo(final ParameterMetaData other) {
         if(layoutOrder != other.layoutOrder)
            return Double.compare(layoutOrder, other.layoutOrder);
         return parameter.getName().compareTo(other.parameter.getName());
      }
      
      static void createAndRegisterUnknown(
         final String expecteFieldName,
         final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> paramter,
         final List<ParameterMetaData> collection
         ) {
         System.err.println(
            "Dashboard: no field with name " + expecteFieldName + " is present.");
         System.err.flush();
         final ParameterMetaData
            datum = new ParameterMetaData();
         datum.parameter = paramter;
         datum.fieldName = paramter.getName().replaceAll("([A-Z])", " $1");
         datum.imageFileName = "";
         collection.add(datum);
      }
   }
   
   private void appendMinimumWidthHintToPresentation(
      final DefaultFormBuilder formBuilder,
      final int widthInPt
      ) {
      formBuilder.append(
         new JLabel(
         "<html><div style=\"margin: 0pt; display: block; width:" + widthInPt 
       + "pt; height:0pt\"></div></html>") { 
            private static final long serialVersionUID = -722208290456978697L;
         }, 3);
   }
   
   private void appendTextToPresentation(
      DefaultFormBuilder formBuilder,
      final String text
      ) {
      if(text == null || text.isEmpty()) return;
      /*
       * Subclass JLabel to lie about the with of the component. We understate the preferred 
       * width to avoid that this component influences the width of the columns. It is unclear 
       * why we need to use colspan = 3 here.
       */
      formBuilder.append(
         new JLabel(
            "<html><div style=\"margin-bottom: 8pt; margin-left: 4pt; margin-top: 12pt;"
          + "line-height:1.5; color:#555555\"><i>" + text.replace("\n", "<br/>") 
          + "</i></div></html>"
          ) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Dimension getPreferredSize() {
               Dimension preferredSize = super.getPreferredSize();
               return new Dimension(10, preferredSize.height);
            }
         }, 3);
   }
   
   private void appendImageToPresentation(
      DefaultFormBuilder formBuilder,
      final String imageFilename
      ) {
      if(imageFilename == null || imageFilename.isEmpty()) return;
      ImageIcon
         icon = null;
      try {
         final ClassLoader
            classLoader = Thread.currentThread().getContextClassLoader();
         final InputStream
            imageStream = classLoader.getResourceAsStream(imageFilename);
         final Image
            image = ImageIO.read(imageStream);
         icon = new ImageIcon(image);
      } catch (final IOException e) {
         icon = new ImageIcon(imageFilename);
      }
      
      final JLabel 
         component = new JLabel(
            "<html><div style=\"margin: 8pt\"></div></html>"
            );
      component.setIcon(icon);
      formBuilder.append(component, 3);
   }
   
   private void appendHeaderTextToPresentation(
      DefaultFormBuilder formBuilder,
      final String text
      ) {
      if(text == null || text.isEmpty()) return;
      
      formBuilder.append(
         new JLabel(
            "<html><div style=\"margin-bottom: 8pt; margin-left: 4pt; margin-top: 12pt;"
          + " display: block; color:#666666; display: block\"><i>" + text.replace("\n", "<br/>") 
          + "</i></div></html>"
          ) {
          private static final long serialVersionUID = 1L;
          
          @Override
          public Dimension getPreferredSize() {
             Dimension preferredSize = super
                .getPreferredSize();
             return new Dimension(
                10,
                preferredSize.height);
          }
       }, 3);
   }
   
   private void appendBannerToPresentation(
      DefaultFormBuilder formBuilder,
      final String text
      ) {
      if(text == null || text.isEmpty()) return;
      formBuilder.appendSeparator(text);
   }
   
   private void appendVerticalSpaceToPresentation(DefaultFormBuilder formBuilder) {
      formBuilder.append("", new JPanel());
   }
   
   private void appendCheckBoxFieldToPresentation(
      DefaultFormBuilder formBuilder,
      final String fieldText,
      final JCheckBox checkBox
      ) {
      formBuilder.append(
         "<html><div style=\"margin-bottom: 4pt; margin-top: 6pt; margin-left: 4pt\">"
       + fieldText + "</div></html>", checkBox
         );
   }
   
   @SuppressWarnings("unchecked")
   private List<ParameterInfo> createAndDisplayAParameterPanel(
      final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters,
      final String title,
      final SubmodelInfo parent,
      final boolean submodelSelectionWithoutNotify,
      final IModelHandler currentModelHandler)
      {
      final List<ParameterMetaData>
         metadata = new LinkedList<ParameterMetaData>(),
         unknownFields = new ArrayList<ParameterMetaData>();
      for(final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> record : batchParameters) {
         final String
            parameterName = record.getName(),
            fieldName = StringUtils.uncapitalize(parameterName);
         Class<?>
            modelComponentType = parent == null ?
               currentModelHandler.getModelClass() : parent.getActualType();
         while(true) {
            try {
               final Field
                  field = modelComponentType.getDeclaredField(fieldName);
               final ParameterMetaData
                  datum = new ParameterMetaData();
               for(final Annotation element : field.getAnnotations()) {
                  if(element.annotationType().getName() != Layout.class.getName())     // Proxies
                     continue;
                  final Class<? extends Annotation>
                     type = element.annotationType();
                  datum.verboseDescription = (String)
                     type.getMethod("VerboseDescription").invoke(element);
                  datum.banner = (String)
                     type.getMethod("Title").invoke(element);
                  datum.fieldName = (String)
                     " " + type.getMethod("FieldName").invoke(element);
                  datum.imageFileName = (String)
                     type.getMethod("Image").invoke(element);
                  datum.layoutOrder = (Double) 
                     type.getMethod("Order").invoke(element);
               }
               datum.parameter = record;
               if(datum.fieldName.trim().isEmpty())
                  datum.fieldName = parameterName.replaceAll("([A-Z])", " $1");
               metadata.add(datum);
               break;
            } catch (final SecurityException e) {
            } catch (final NoSuchFieldException e) {
            } catch (final IllegalArgumentException e) {
            } catch (final IllegalAccessException e) {
            } catch (final InvocationTargetException e) {
            } catch (final NoSuchMethodException e) {
            }
            modelComponentType = modelComponentType.getSuperclass();
            if(modelComponentType == null) {
               ParameterMetaData.createAndRegisterUnknown(fieldName, record, unknownFields);
               break;
            }
         }
      }
      Collections.sort(metadata);
      for(int i = unknownFields.size() - 1; i>= 0; --i)
         metadata.add(0, unknownFields.get(i));
      
      // initialize single run form
      final DefaultFormBuilder formBuilder = FormsUtils.build("p ~ p:g", "");
      appendMinimumWidthHintToPresentation(formBuilder, 550);
      
      if (parent == null) {
         SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
               numberOfTurnsField.grabFocus();
            }
         });
         
         appendBannerToPresentation(formBuilder, "General Parameters");
         appendTextToPresentation(formBuilder,
            "Global parameters affecting the entire simulation");
         
         formBuilder.append(NUMBER_OF_TURNS_LABEL_TEXT, numberOfTurnsField);
         formBuilder.append(NUMBER_OF_TIMESTEPS_TO_IGNORE_LABEL_TEXT, numberTimestepsIgnored);
         
         appendCheckBoxFieldToPresentation(
            formBuilder, UPDATE_CHARTS_LABEL_TEXT, onLineChartsCheckBox);
         appendCheckBoxFieldToPresentation(
            formBuilder, DISPLAY_ADVANCED_CHARTS_LABEL_TEXT, advancedChartsCheckBox);
      } 
      
      appendBannerToPresentation(formBuilder, title);
      
      final DefaultMutableTreeNode
         parentNode = (parent == null) ?
            parameterValueComponentTree : findParameterInfoNode(parent, false);
      
      final List<ParameterInfo> info = new ArrayList<ParameterInfo>();
      
      // Search for a @ConfigurationComponent annotation
      {
         String
            headerText = "",
            imagePath = "";
         final Class<?> parentType =
            parent == null ?
               currentModelHandler.getModelClass() :
               parent.getActualType();
         for(final Annotation element : parentType.getAnnotations()) {               // Proxies
            if(element.annotationType().getName() != ConfigurationComponent.class.getName())
               continue;
            boolean
               doBreak = false;
            try{
               try {
                  headerText =
                     (String) element.annotationType().getMethod("Description").invoke(element);
                  if(headerText.startsWith("#")) {
                     headerText = (String) parent.getActualType().getMethod(
                        headerText.substring(1)).invoke(parent.getInstance());
                  }
                  doBreak = true;
               } catch (IllegalArgumentException e) {
               } catch (SecurityException e) {
               } catch (IllegalAccessException e) {
               } catch (InvocationTargetException e) {
               } catch (NoSuchMethodException e) {
               }
            }
            catch(final Exception e) {
            }
            try {
               imagePath =
                  (String) element.annotationType().getMethod("ImagePath").invoke(element);
               doBreak = true;
            } catch (IllegalArgumentException e) {
            } catch (SecurityException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
            if(doBreak)
               break;
         }
         if(!headerText.isEmpty())
            appendHeaderTextToPresentation(formBuilder, headerText);
         if(!imagePath.isEmpty())
            appendImageToPresentation(formBuilder, imagePath);
      }
      
      if(metadata.isEmpty()) {
         // No fields to display.
         appendTextToPresentation(formBuilder, "No configuration is required for this module.");
      }
      else {
    	  for(final ParameterMetaData record : metadata) {
    		  final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>
    		  batchParameterInfo = record.parameter;

    		  if(!record.banner.isEmpty())
    			  appendBannerToPresentation(formBuilder, record.banner);
    		  if(!record.imageFileName.isEmpty())
    			  appendImageToPresentation(formBuilder, record.imageFileName);
    		  appendTextToPresentation(formBuilder, record.verboseDescription);

    		  final ParameterInfo parameterInfo = InfoConverter.parameterInfo2ParameterInfo(batchParameterInfo);
    		  if (parent != null && parameterInfo instanceof ISubmodelGUIInfo) {
//					sgi.setParentValue(parent.getActualType());
    		  }

    		  final JComponent field;
    		  final DefaultMutableTreeNode oldNode = findParameterInfoNode(parameterInfo,true);
    		  Pair<ParameterInfo,JComponent> userData = null;
    		  JComponent oldField = null;
    		  if (oldNode != null) {
    			  userData = (Pair<ParameterInfo,JComponent>) oldNode.getUserObject();
    			  oldField = userData.getSecond();
    		  }

    		  if (parameterInfo.isBoolean()) {
    			  field = new JCheckBox();
    			  boolean value = oldField != null ? ((JCheckBox)oldField).isSelected()
    					  : ((Boolean)batchParameterInfo.getDefaultValue()).booleanValue(); 
    			  ((JCheckBox)field).setSelected(value);
    		  } else if (parameterInfo.isEnum() || parameterInfo instanceof MasonChooserParameterInfo) {
    			  Object[] elements = null;
    			  if (parameterInfo.isEnum()) {
    				  final Class<Enum<?>> type = (Class<Enum<?>>) parameterInfo.getJavaType();
    				  elements = type.getEnumConstants();
    			  } else {
    				  final MasonChooserParameterInfo chooserInfo = (MasonChooserParameterInfo)parameterInfo;
    				  elements = chooserInfo.getValidStrings();
    			  }
    			  final JComboBox list = new JComboBox(elements);

    			  if (parameterInfo.isEnum()) {
    				  final Object value = oldField != null ? ((JComboBox)oldField).getSelectedItem()
    						  : parameterInfo.getValue();
    				  list.setSelectedItem(value);
    			  } else {
    				  final int value = oldField != null ? ((JComboBox)oldField).getSelectedIndex()
    						  : (Integer) parameterInfo.getValue();
    				  list.setSelectedIndex(value);
    			  }

    			  field = list;
    		  } else if (parameterInfo instanceof SubmodelInfo) {
    			  final SubmodelInfo submodelInfo = (SubmodelInfo) parameterInfo;
    			  final Object[] elements = new Object[] { "Loading class information..." }; 
    			  final JComboBox list = new JComboBox(elements);
    			  //				field = list;

    			  final Object value = oldField != null ? ((JComboBox)((JPanel)oldField).getComponent(0)).getSelectedItem()
    					  : new ClassElement(submodelInfo.getActualType(), null);

    			  new ClassCollector(this,list,submodelInfo,value,submodelSelectionWithoutNotify).execute();


    			  final JButton rightButton = new JButton();
    			  rightButton.setOpaque(false);
    			  rightButton.setRolloverEnabled(true);
    			  rightButton.setIcon(SHOW_SUBMODEL_ICON);
    			  rightButton.setRolloverIcon(SHOW_SUBMODEL_ICON_RO);
    			  rightButton.setDisabledIcon(SHOW_SUBMODEL_ICON_DIS);
    			  rightButton.setBorder(null);
    			  rightButton.setToolTipText("Display submodel parameters");
    			  rightButton.setActionCommand(ACTIONCOMMAND_SHOW_SUBMODEL);
    			  rightButton.addActionListener(new ActionListener() {

    				  @Override
    				  public void actionPerformed(ActionEvent arg0) {
    					  if (parameterInfo instanceof SubmodelInfo) {
    						  SubmodelInfo submodelInfo = (SubmodelInfo) parameterInfo;
    						  int level = 0;

    						  showHideSubparameters(list, submodelInfo);

    						  List<String> components = new ArrayList<String>();
    						  components.add(submodelInfo.getName());
    						  while (submodelInfo.getParent() != null){
    							  submodelInfo = submodelInfo.getParent();
    							  components.add(submodelInfo.getName());
    							  level++;
    						  }
    						  Collections.reverse(components);
    						  final String[]
    								  breadcrumbText = components.toArray(new String[components.size()]);
    						  for(int i = 0; i< breadcrumbText.length; ++i)
    							  breadcrumbText[i] = breadcrumbText[i].replaceAll("([A-Z])", " $1");
    						  breadcrumb.setPath(
    								  currentModelHandler.getModelClassSimpleName().replaceAll("([A-Z])", " $1"),
    								  breadcrumbText
    								  );
    						  Style.apply(breadcrumb, dashboard.getCssStyle());

    						  // reset all buttons that are nested deeper than this to default color
    						  for (int i = submodelButtons.size() - 1; i >= level ; i--) {
    							  JButton button = submodelButtons.get(i);
    							  button.setIcon(SHOW_SUBMODEL_ICON);
    							  submodelButtons.remove(i);
    						  }

    						  rightButton.setIcon(SHOW_SUBMODEL_ICON_RO);
    						  submodelButtons.add(rightButton);
    					  }
    				  }
    			  });

    			  field = new JPanel(new BorderLayout());
    			  field.add(list, BorderLayout.CENTER);
    			  field.add(rightButton, BorderLayout.EAST);
    		  } else if (File.class.isAssignableFrom(parameterInfo.getJavaType())){
    			  field = new JPanel(new BorderLayout());

    			  String oldName = "";
    			  String oldPath = "";
    			  if (oldField != null) {
    				  final JTextField oldTextField = (JTextField) oldField.getComponent(0);
    				  oldName = oldTextField.getText();
    				  oldPath = oldTextField.getToolTipText();
    			  } else if (parameterInfo.getValue() != null) {
    				  final File file = (File) parameterInfo.getValue();
    				  oldName = file.getName();
    				  oldPath = file.getAbsolutePath();
    			  }

    			  final JTextField textField = new JTextField(oldName);
    			  textField.setToolTipText(oldPath);
    			  textField.setInputVerifier(new InputVerifier() {

    				  @Override
    				  public boolean verify(final JComponent input) {
    					  final JTextField inputField = (JTextField) input;
    					  if (inputField.getText() == null || inputField.getText().isEmpty()) {
    						  final File file = new File("");
    						  inputField.setToolTipText(file.getAbsolutePath());
    						  hideError();
    						  return true;
    					  }

    					  final File oldFile = new File(inputField.getToolTipText());
    					  if (oldFile.exists() && oldFile.getName().equals(inputField.getText().trim())) {
    						  hideError();
    						  return true;
    					  }

    					  inputField.setToolTipText("");
    					  final File file = new File(inputField.getText().trim());
    					  if (file.exists()) {
    						  inputField.setToolTipText(file.getAbsolutePath());
    						  inputField.setText(file.getName());
    						  hideError();
    						  return true;
    					  } else {
    						  final PopupFactory popupFactory = PopupFactory.getSharedInstance();
    						  final Point locationOnScreen = inputField.getLocationOnScreen();
    						  final JLabel message = new JLabel("Please specify an existing file!");
    						  message.setBorder(new LineBorder(Color.RED, 2, true));
    						  if (errorPopup != null)
    							  errorPopup.hide();
    						  errorPopup = popupFactory.getPopup(inputField,message,locationOnScreen.x - 10,locationOnScreen.y - 30);
    						  errorPopup.show();
    						  return false;
    					  }
    				  }
    			  });

    			  final JButton browseButton = new JButton(BROWSE_BUTTON_TEXT);
    			  browseButton.addActionListener(new ActionListener() {
    				  @Override
    				  public void actionPerformed(ActionEvent e) {
    					  final JFileChooser fileDialog = new JFileChooser(!"".equals(textField.getToolTipText()) ? textField.getToolTipText() : currentDirectory);
    					  if (!"".equals(textField.getToolTipText()))
    						  fileDialog.setSelectedFile(new File(textField.getToolTipText()));
    					  int dialogResult = fileDialog.showOpenDialog(dashboard);
    					  if (dialogResult == JFileChooser.APPROVE_OPTION) {
    						  final File selectedFile = fileDialog.getSelectedFile();
    						  if (selectedFile != null) {
    							  currentDirectory = selectedFile.getAbsoluteFile().getParent();
    							  textField.setText(selectedFile.getName());
    							  textField.setToolTipText(selectedFile.getAbsolutePath());
    						  }
    					  }
    				  }
    			  });

    			  field.add(textField,BorderLayout.CENTER);
    			  field.add(browseButton,BorderLayout.EAST);
    		  } else if (parameterInfo instanceof MasonIntervalParameterInfo) {
    			  final MasonIntervalParameterInfo intervalInfo = (MasonIntervalParameterInfo) parameterInfo;

    			  field = new JPanel(new BorderLayout());

    			  String oldValueStr = String.valueOf(parameterInfo.getValue());
    			  if (oldField != null) {
    				  final JTextField oldTextField = (JTextField) oldField.getComponent(0);
    				  oldValueStr = oldTextField.getText();
    			  }

    			  final JTextField textField = new JTextField(oldValueStr);

    			  PercentJSlider tempSlider =  null;
    			  if (intervalInfo.isDoubleInterval())
    				  tempSlider = new PercentJSlider(intervalInfo.getIntervalMin().doubleValue(),intervalInfo.getIntervalMax().doubleValue(),
    						  Double.parseDouble(oldValueStr));
    			  else
    				  tempSlider = new PercentJSlider(intervalInfo.getIntervalMin().longValue(),intervalInfo.getIntervalMax().longValue(),
    						  Long.parseLong(oldValueStr));

    			  final PercentJSlider slider = tempSlider;
    			  slider.setMajorTickSpacing(100);
    			  slider.setMinorTickSpacing(10);
    			  slider.setPaintTicks(true);
    			  slider.setPaintLabels(true);
    			  slider.addChangeListener(new ChangeListener() {
    				  public void stateChanged(final ChangeEvent _) {
    					  if (slider.hasFocus()) {
    						  final String value = intervalInfo.isDoubleInterval() ? String.valueOf(slider.getDoubleValue()) :
    							  String.valueOf(slider.getLongValue());
    						  textField.setText(value);
    						  slider.setToolTipText(value);
    					  }
    				  }
    			  });

    			  textField.setInputVerifier(new InputVerifier() {
    				  public boolean verify(JComponent input) {
    					  final JTextField inputField = (JTextField) input;

    					  try {
    						  hideError();
    						  final String valueStr = inputField.getText().trim();
    						  if (intervalInfo.isDoubleInterval()) {
    							  final double value = Double.parseDouble(valueStr);
    							  if (intervalInfo.isValidValue(valueStr)) {
    								  slider.setValue(value);
    								  return true;
    							  } else 
    								  showError("Please specify a value between " + intervalInfo.getIntervalMin() + " and " + intervalInfo.getIntervalMax() + ".",
    										  inputField);
    							  return false;
    						  } else {
    							  final long value = Long.parseLong(valueStr);
    							  if (intervalInfo.isValidValue(valueStr)) {
    								  slider.setValue(value);
    								  return true;
    							  } else {
    								  showError("Please specify an integer value between " + intervalInfo.getIntervalMin() + " and " + intervalInfo.getIntervalMax() +
    										  ".",inputField);
    								  return false;
    							  }
    						  }
    					  } catch (final NumberFormatException _) {
    						  final String message = "The specified value is not a" + (intervalInfo.isDoubleInterval() ? "" : "n integer") + " number.";
    						  showError(message,inputField);
    						  return false;
    					  }

    				  }
    			  });

    			  textField.getDocument().addDocumentListener(new DocumentListener() {
    				  //					private Popup errorPopup;

    				  public void removeUpdate(final DocumentEvent _) {
    					  textFieldChanged();
    				  }

    				  public void insertUpdate(final DocumentEvent _) {
    					  textFieldChanged();
    				  }

    				  public void changedUpdate(final DocumentEvent _) {
    					  textFieldChanged();
    				  }

    				  private void textFieldChanged() {
    					  if (!textField.hasFocus()) {
    						  hideError();
    						  return;
    					  }

    					  try {
    						  hideError();
    						  final String valueStr = textField.getText().trim();
    						  if (intervalInfo.isDoubleInterval()) {
    							  final double value = Double.parseDouble(valueStr);
    							  if (intervalInfo.isValidValue(valueStr))
    								  slider.setValue(value);
    							  else 
    								  showError("Please specify a value between " + intervalInfo.getIntervalMin() + " and " + intervalInfo.getIntervalMax() + ".",
    										  textField);
    						  } else {
    							  final long value = Long.parseLong(valueStr);
    							  if (intervalInfo.isValidValue(valueStr))
    								  slider.setValue(value);
    							  else 
    								  showError("Please specify an integer value between " + intervalInfo.getIntervalMin() + " and " + intervalInfo.getIntervalMax() +
    										  ".",textField);
    						  }
    					  } catch (final NumberFormatException _) {
    						  final String message = "The specified value is not a" + (intervalInfo.isDoubleInterval() ? "" : "n integer") + " number.";
    						  showError(message,textField);
    					  }
    				  }
    			  });

    			  field.add(textField,BorderLayout.CENTER);
    			  field.add(slider,BorderLayout.SOUTH);
    		  } else {
    			  final Object value = oldField != null ? ((JTextField)oldField).getText()
    					  : parameterInfo.getValue();
    			  field = new JTextField(value.toString());
    			  ((JTextField)field).addActionListener(new ActionListener() {

    				  @Override
    				  public void actionPerformed(final ActionEvent e) {
    					  wizard.clickDefaultButton();
    				  }
    			  });
    		  }

    		  final JLabel parameterLabel = new JLabel(record.fieldName);

    		  final String description = parameterInfo.getDescription();
    		  if (description != null && !description.isEmpty()){
    			  parameterLabel.addMouseListener(new MouseAdapter() {

    				  @Override
    				  public void mouseEntered(final MouseEvent e) {
    					  final DescriptionPopupFactory popupFactory = DescriptionPopupFactory.getInstance();

    					  final Popup parameterDescriptionPopup = popupFactory.getPopup(parameterLabel, description, dashboard.getCssStyle());

    					  parameterDescriptionPopup.show();
    				  }

    			  });
    		  }

    		  if (oldNode != null) 
    			  userData.setSecond(field);
    		  else {
    			  final Pair<ParameterInfo,JComponent> pair = new Pair<ParameterInfo,JComponent>(parameterInfo,field);
    			  final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pair);
    			  parentNode.add(newNode);
    		  }

    		  if(field instanceof JCheckBox) {
    			  parameterLabel.setText("<html><div style=\"margin-bottom: 4pt; margin-top: 6pt; margin-left: 4pt\">" + parameterLabel.getText()
    					  + "</div></html>");
    			  formBuilder.append( parameterLabel, field );

    			  //            appendCheckBoxFieldToPresentation(
    			  //               formBuilder, parameterLabel.getText(), (JCheckBox) field);
    		  } else {
    			  formBuilder.append(parameterLabel, field);
    			  final CellConstraints constraints = formBuilder.getLayout().getConstraints(parameterLabel);
    			  constraints.vAlign = CellConstraints.TOP;
    			  constraints.insets = new Insets(5,0,0,0);
    			  formBuilder.getLayout().setConstraints(parameterLabel,constraints);
    		  }

    		  // prepare the parameterInfo for the param sweeps
    		  parameterInfo.setRuns(0);
    		  parameterInfo.setDefinitionType(ParameterInfo.CONST_DEF);
    		  parameterInfo.setValue(batchParameterInfo.getDefaultValue());
    		  info.add(parameterInfo);
    	  }
      }
      	appendVerticalSpaceToPresentation(formBuilder);
      
		final JPanel panel = formBuilder.getPanel();
		singleRunParametersPanel.add(panel);

		if (singleRunParametersPanel.getComponentCount() > 1) {
			panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,1,0,0,Color.GRAY),BorderFactory.createEmptyBorder(0,5,0,5)));
		} else {
			panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}
		
		Style.apply(panel, dashboard.getCssStyle());
		
		return info;
	}
	
	//----------------------------------------------------------------------------------------------------
	private DefaultMutableTreeNode findParameterInfoNode(final ParameterInfo info, final boolean checkParentValue) {
		if (info == null) return null;
		
		@SuppressWarnings("rawtypes")
		final Enumeration treeValues = parameterValueComponentTree.breadthFirstEnumeration();
		treeValues.nextElement(); // root node
		
		while (treeValues.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeValues.nextElement();
			@SuppressWarnings("unchecked")
			final Pair<ParameterInfo,JComponent> userData = (Pair<ParameterInfo,JComponent>) node.getUserObject();
			final ParameterInfo parameterInfo = userData.getFirst();
			
			if (checkParentValue && info instanceof ISubmodelGUIInfo && parameterInfo instanceof ISubmodelGUIInfo) {
				final ISubmodelGUIInfo sgi = (ISubmodelGUIInfo) info;
				final ISubmodelGUIInfo otherSgi = (ISubmodelGUIInfo) parameterInfo;
				if (info.equals(parameterInfo) && classEquals(sgi.getParentValue(),otherSgi.getParentValue()))
					return node;
			} else if (info.equals(parameterInfo)) 
				return node;
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	boolean classEquals(final Class<?> class1, final Class<?> class2) {
		if (class1 == class2) return true;
		
		if (class1 == null) return false;
		
		return class1.equals(class2);
	}

	//------------------------------------------------------------------------------
	/** Builds the parameter list on the gui from the <code>parameters</code> list. */
	private void createDefaultParameterList(final List<ParameterInfo> parameters) {
		final DefaultListModel model = new DefaultListModel();
		for (final ParameterInfo parameterInfo : parameters) 
			model.addElement(new AvailableParameter(parameterInfo, currentModelHandler.getModelClass()));
		parameterList.setModel(model); 
	}
	
	//------------------------------------------------------------------------------
	/** Edits the information object <code>info</code> so this method fills the appropriate
	 *  components with the informations of <code>info</code>.
	 */
	private void edit(final ParameterInfo info) { 
		changeText(info.getName(),info.getType());
		if (info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) info;
			editSubmodelInfo(sInfo);
		} else {
			String view = null;
			switch (info.getDefinitionType()) {
			case ParameterInfo.CONST_DEF : {
				if (info.isEnum() || info instanceof MasonChooserParameterInfo) {
					view = "ENUM";
					constDef.setSelected(true);
					incrDef.setEnabled(false);
					listDef.setEnabled(false);
					Object[] elements = null;
					if (info.isEnum()){
						@SuppressWarnings("unchecked")
						final
						Class<Enum<?>> type = (Class<Enum<?>>) info.getJavaType();
						elements = type.getEnumConstants();
					} else {
						final MasonChooserParameterInfo chooserInfo = (MasonChooserParameterInfo) info;
						elements = chooserInfo.getValidStrings();
					}
					final DefaultComboBoxModel model = (DefaultComboBoxModel)enumDefBox.getModel();
					model.removeAllElements();
					for (final Object object : elements) {
						model.addElement(object);
					}
					if (info.isEnum())
						enumDefBox.setSelectedItem(info.getValue());
					else 
						enumDefBox.setSelectedIndex((Integer)info.getValue());
				} else if (info.isFile()) {
					view = "FILE";
					constDef.setSelected(true);
					final File file = (File) info.getValue();
					if (file != null) {
						fileTextField.setText(file.getName());
						fileTextField.setToolTipText(file.getAbsolutePath());
					}
				} else {
					view = "CONST";
					constDef.setSelected(true);
					constDefField.setText(info.valuesToString());
				}
				break;
			}
			case ParameterInfo.LIST_DEF  : view = "LIST";
				listDef.setSelected(true);
				listDefArea.setText(info.valuesToString());
				break;
			case ParameterInfo.INCR_DEF  : view = "INCREMENT";
				incrDef.setSelected(true);
				incrStartValueField.setText(info.startToString());
				incrEndValueField.setText(info.endToString());
				incrStepField.setText(info.stepToString());
			}
			
			final CardLayout cl = (CardLayout) rightMiddle.getLayout();
			cl.show(rightMiddle,view);
			enableDisableSettings(true);
			if (!info.isNumeric()) 
				incrDef.setEnabled(false);
			if (info.isEnum() || info instanceof MasonChooserParameterInfo || info.isFile()) { 
				incrDef.setEnabled(false);
				listDef.setEnabled(false);
			}
			modifyButton.setEnabled(true);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void editSubmodelInfo(final SubmodelInfo sInfo) {
		constDef.setSelected(true);
		incrDef.setEnabled(false);
		listDef.setEnabled(false);
		cancelButton.setEnabled(true);
		final DefaultComboBoxModel model = (DefaultComboBoxModel) submodelTypeBox.getModel();
		model.removeAllElements();
		model.addElement("Loading class information...");
		final CardLayout cl = (CardLayout) rightMiddle.getLayout();
		cl.show(rightMiddle,"SUBMODEL");
		
		try {
			final ClassElement[] elements = fetchPossibleTypes(sInfo);
			model.removeAllElements();
			for (final ClassElement classElement : elements) 
				model.addElement(classElement);
			
			if (sInfo.getActualType() != null)
				submodelTypeBox.setSelectedItem(new ClassElement(sInfo.getActualType(), sInfo.getInstance()));
			submodelTypeBox.setEnabled(true);
			modifyButton.setEnabled(true);
		} catch (final ComboBoxIsTooFullException e) {
			String errorMsg = "Too much possibilities for " + sInfo.getName() + ". Please narrow down the domain by " + 
					  		  "defining the possible classes in the @Submodel annotation.";
			JOptionPane.showMessageDialog(wizard,new JLabel(errorMsg),"Error while analyizing model",JOptionPane.ERROR_MESSAGE);
			cancelButton.doClick();
		}
	}

	//------------------------------------------------------------------------------
	/** Changes the text on the right top corner of the page. The text can be the initial
	 *  commentary text ("Select a parameter to modify its settings") or
	 *  the name and type of the edited parameter.
	 * @param name name of the edited parameter (null if there is no editing parameter)
	 * @param type name of type of the edited parameter
	 */
	private void changeText(final String name, final String type) {
		if (name == null) {
			editedParameterText.setText(ORIGINAL_TEXT);
		} else {
			final String humanName = name.replaceAll("([A-Z])", " $1");
			editedParameterText.setText("<html><b>" + humanName + "</b>: " + type + "</html>");
		}
	}

	//----------------------------------------------------------------------------------------------------
	private boolean checkNumberOfTurnsField(final boolean singleRun) {
		try {
			final String value = singleRun ? numberOfTurnsField.getText() : numberOfTurnsFieldPSW.getText(); 
			Double.parseDouble(value);
			return true;
		} catch (final NumberFormatException _) {
			return false;
		}
	}

	//----------------------------------------------------------------------------------------------------
	private boolean checkNumberTimestepsIgnored(final boolean singleRun) {
		try {
			final String value = singleRun ? numberTimestepsIgnored.getText() : numberTimestepsIgnoredPSW.getText();
			Double.parseDouble(value);
			return true;
		} catch (final NumberFormatException _) {
			return false;
		}
	}

	//----------------------------------------------------------------------------------------------------
	private boolean modify() {
		final ParameterInATree userObj = (ParameterInATree) editedNode.getUserObject();
		
		final String[] errors = checkInput(userObj.info);
		if (errors != null && errors.length != 0) {
			Utilities.userAlert(sweepPanel,(Object)errors);
			return false;
		} else {
			modifyParameterInfo(userObj.info);
			resetSettings();
			modifyButton.setEnabled(false);
			
			if (!(userObj.info instanceof SubmodelInfo)) {
				final DefaultTreeModel model = (DefaultTreeModel) editedTree.getModel();
				model.nodeStructureChanged(editedNode);
			}
			
			editedNode = null;
			editedTree = null;
			
			updateNumberOfRuns();
			enableDisableSettings(false);
			return true;
		}
	}

	//------------------------------------------------------------------------------
	/** Checks the content of the editing components. 
	 * @param info the info object that belongs to the edited parameter
	 * @return the error messages in an array (or null if there is no error)
	 */
	private String[] checkInput(final ParameterInfo info) {
		final ArrayList<String> errors = new ArrayList<String>();
		if (constDef.isSelected()) {
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
			} else if (constDefField.getText().trim().equals(""))
				errors.add("'Constant value' cannot be empty.");
			else {
				final boolean valid = ParameterInfo.isValid(constDefField.getText().trim(),info.getType());
				if (!valid)
					errors.add("'Constant value' must be a valid " + getTypeText(info.getType()));
				errors.addAll(PlatformSettings.additionalParameterCheck(info,new String[] { constDefField.getText().trim() },ParameterInfo.CONST_DEF));
			}
		} else if (listDef.isSelected()) {
			String text = listDefArea.getText().trim();
			if (text.equals("")) 
				errors.add("'List of values' cannot be empty.");
			else {
				text = text.replaceAll("[\\s]+"," ");
				final String[] elements = text.split(" ");
				boolean goodList = true;
				for (final String element : elements)
					goodList = goodList && ParameterInfo.isValid(element.trim(),info.getType());
				if (!goodList)
					errors.add("All elements of the list must be a valid " + getTypeText(info.getType()));
//				else {
//					final Set<String> previous = new HashSet<String>(elements.length);
//					for (final String element : elements) {
//						if (previous.contains(element)) {
//							goodList = false;
//							break;
//						}
//						previous.add(element);
//					}
//					if (!goodList) {
//						final int result = Utilities.askUser(wizard,false,"Warning", "The list contains repetitive element(s).\nAre you sure?");
//						if (result == 0 && errors.size() == 0) // this doesn't work
//							return new String[0];
//					}
//				}
				errors.addAll(PlatformSettings.additionalParameterCheck(info,elements,ParameterInfo.LIST_DEF));
			}
		} else {
			boolean s = false, e = false, st = false; 
			if (incrStartValueField.getText().trim().equals("")) {
				errors.add("'Start value' cannot be empty.");
				s = true;
			} else {
				final boolean valid = ParameterInfo.isValid(incrStartValueField.getText().trim(),info.getType());
				if (!valid)
					errors.add("'Start value' must be a valid " + getTypeText(info.getType()));
			}
			if (incrEndValueField.getText().trim().equals("")) {
				errors.add("'End value' cannot be empty.");
				e = true;
			} else {
				final boolean valid = ParameterInfo.isValid(incrEndValueField.getText().trim(),info.getType());
				if (!valid)
					errors.add("'End value' must be a valid " + getTypeText(info.getType()));
			}
			if (incrStepField.getText().trim().equals("")) {
				errors.add("'Step' cannot be empty.");
				st = true;
			} else {
				final double d = Double.valueOf(incrStepField.getText().trim());
				if (d == 0)
					errors.add("'Step' cannot be zero.");
				else {
					final boolean valid = ParameterInfo.isValid(incrStepField.getText().trim(),info.getType());
					if (!valid)
						errors.add("'Step' must be a valid " + getTypeText(info.getType()));
				}
			}
			if (!(s || e || st))
				errors.addAll(PlatformSettings.additionalParameterCheck(info,new String[] { incrStartValueField.getText().trim(), 
																							incrEndValueField.getText().trim(), 
																							incrStepField.getText().trim() },ParameterInfo.INCR_DEF));
		}
		return errors.size()== 0 ? null : errors.toArray(new String[0]);
	}

	//------------------------------------------------------------------------------
	/** Returns the appropriate error message part according to the value of <code>type</code>. */
	String getTypeText(final String type) {
		if ("int".equals(type) || "Integer".equals(type))
			return "integer value.";
		if ("float".equals(type) || "Float".equals(type)
			|| "double".equals(type) || "Double".equals(type))
			return "real value.";
		if ("boolean".equals(type) || "Boolean".equals(type))
			return "boolean value (\"true\" or \"false\").";
		if ("String".equals(type))
			return "string that not contains any white space.";
		return type + ".";
	}

	//----------------------------------------------------------------------------------------------------
	private void updateNumberOfRuns() {
		final List<Integer> runNumbers = new ArrayList<Integer>(parameterTreeBranches.size());
		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			final int run = Page_Parameters.calculateNumberOfRuns(pGUI.combinationRoot);
			pGUI.updateRunDisplay(run);
			runNumbers.add(run);
		}
		
		int validMin = Integer.MAX_VALUE;
		for (final int runNumber : runNumbers) {
			if (runNumber > 1 && runNumber < validMin)
				validMin = runNumber;
		}
		
		for (int i = 0;i < parameterTreeBranches.size();++i) {
			final int runNumber = runNumbers.get(i);
			parameterTreeBranches.get(i).setWarning(runNumber > 1 && runNumber != validMin);
		}
	}


	//----------------------------------------------------------------------------------------------------
	public boolean needWarning() {
		if (parameterTreeBranches.isEmpty()) return false;
		
		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			if (pGUI.warningDisplay.getIcon() != null) return true;
		}
		
		return false;
	}
	
	//------------------------------------------------------------------------------
	/** Enables/disables the parameter editing components of the page according to
	 *  the value of <code>enabled</code>.
	 */
	private void enableDisableSettings(final boolean enabled) {
		constDef.setEnabled(enabled);
		listDef.setEnabled(enabled);
		incrDef.setEnabled(enabled);
		constDefField.setEnabled(enabled);
		listDefArea.setEnabled(enabled);
		incrStartValueField.setEnabled(enabled);
		incrEndValueField.setEnabled(enabled);
		incrStepField.setEnabled(enabled);
		enumDefBox.setEnabled(enabled);
		submodelTypeBox.setEnabled(enabled);
		fileTextField.setEnabled(enabled);
		fileBrowseButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);
	}

	//------------------------------------------------------------------------------
	/** Resets the parameter editing related components. */
	private void resetSettings() { 
		changeText(null,null);
		constDefField.setText("");
		listDefArea.setText("");
		incrStartValueField.setText("");
		incrEndValueField.setText("");
		incrStepField.setText("");
		fileTextField.setText("");
		fileTextField.setToolTipText("");
		
		constDef.setSelected(true);
		final CardLayout cl = (CardLayout) rightMiddle.getLayout();
		cl.show(rightMiddle,"CONST");
	}

	//------------------------------------------------------------------------------
	// Pre-condition : all input are valid
	/** Modifies the information object from the contents of the editing components of the page.<br>
	 *  Pre-condition: all input values are valid.
	 */
	private void modifyParameterInfo(final ParameterInfo info) { 
		info.clear();
		int defType = ParameterInfo.CONST_DEF;
		if (listDef.isSelected())
			defType = ParameterInfo.LIST_DEF;
		else if (incrDef.isSelected())
			defType = ParameterInfo.INCR_DEF;
		info.setDefinitionType(defType);
		switch (defType) {
		case ParameterInfo.CONST_DEF : {
			if (info.isEnum()) {
				info.setValue(enumDefBox.getSelectedItem());
			} else if (info instanceof MasonChooserParameterInfo) {
				info.setValue(enumDefBox.getSelectedIndex()); 
			} else if (info instanceof SubmodelInfo) {
				final SubmodelInfo sInfo = (SubmodelInfo) info;
				modifySubmodelParameterInfo(sInfo);
			} else if (info.isFile()) {
				info.setValue(ParameterInfo.getValue(fileTextField.getToolTipText(),info.getType()));
			} else {
				info.setValue(ParameterInfo.getValue(constDefField.getText().trim(),info.getType()));
			}
			
			break;
		}
		case ParameterInfo.LIST_DEF  : String text = listDefArea.getText().trim();
									   text = text.replaceAll("[\\s]+"," ");
									   final String[] elements = text.split(" ");
									   final List<Object> list = new ArrayList<Object>(elements.length);
									   for (final String element : elements) 
										  list.add(ParameterInfo.getValue(element,info.getType()));
									   info.setValues(list);
									   break;
		case ParameterInfo.INCR_DEF  : info.setStartValue((Number)ParameterInfo.getValue(incrStartValueField.getText().trim(),info.getType()));
									   info.setEndValue((Number)ParameterInfo.getValue(incrEndValueField.getText().trim(),info.getType()));
									   info.setStep((Number)ParameterInfo.getValue(incrStepField.getText().trim(),info.getType()));
									   break;
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void modifySubmodelParameterInfo(final SubmodelInfo sInfo) {
		ClassElement aClassElement = (ClassElement) submodelTypeBox.getSelectedItem();
		final Class<?> newActualType = aClassElement.clazz;
		
		if (!newActualType.equals(sInfo.getActualType())) {
			sInfo.setActualType(newActualType, aClassElement.instance);
			addSubParametersToTree(sInfo,editedTree,editedNode);
			editedTree.expandPath(new TreePath(((DefaultTreeModel) editedTree.getModel()).getPathToRoot(editedNode)));
		} // else do nothing
		
	}
	
	//----------------------------------------------------------------------------------------------------
	private void addSubParametersToTree(final SubmodelInfo sInfo, final JTree tree, final DefaultMutableTreeNode node) {
		try {
			final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = ParameterTreeUtils.fetchSubparameters(currentModelHandler,sInfo);
			
			final List<ParameterInfo> convertedSubparameters = new ArrayList<ParameterInfo>(subparameters.size());
			for (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> parameterInfo : subparameters) {
				final ParameterInfo converted = InfoConverter.parameterInfo2ParameterInfo(parameterInfo);
				converted.setRuns(0);
				convertedSubparameters.add(converted);
			}
			Collections.sort(convertedSubparameters);
			
			final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
			
			if (node.getChildCount() > 0) {
				node.removeAllChildren();
				treeModel.nodeStructureChanged(node);
			}
			
			for (final ParameterInfo pInfo : convertedSubparameters) {
				final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new ParameterInATree(pInfo, currentModelHandler.getModelClass()));
				treeModel.insertNodeInto(newNode,node,node.getChildCount());

				if (pInfo instanceof SubmodelInfo) {
					final SubmodelInfo ssInfo = (SubmodelInfo) pInfo;
					if (ssInfo.getActualType() != null)
						addSubParametersToTree(ssInfo, tree, newNode);
				}
			}

			//tree.expandPath(new TreePath(treeModel.getPathToRoot(node)));
		} catch (final ModelInformationException e) {
			sInfo.setActualType(null, null);
			JOptionPane.showMessageDialog(wizard, new JLabel(e.getMessage()), "Error while analyizing model", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			submodelTypeBox.setSelectedIndex(0);
		}
	}

	//----------------------------------------------------------------------------------------------------
	/**
	 * @return the numberOfTurnsField
	 */
	public double getNumberOfRequestedTurns() {
		if (tabbedPane.getSelectedIndex() == SINGLE_RUN_GUI_TABINDEX) 
			return Double.parseDouble(numberOfTurnsField.getText());
		else if (tabbedPane.getSelectedIndex() == PARAMSWEEP_GUI_TABINDEX) 
			return Double.parseDouble(numberOfTurnsFieldPSW.getText());
		else
			return gaSearchPanel.getNumberOfRequestedTurns();
	}

	//----------------------------------------------------------------------------------------------------
	public double getNumberTimestepsToIgnore() {
		if (tabbedPane.getSelectedIndex() == SINGLE_RUN_GUI_TABINDEX){
			return Double.parseDouble(numberTimestepsIgnored.getText());
		} else if (tabbedPane.getSelectedIndex() == PARAMSWEEP_GUI_TABINDEX) {
			return Double.parseDouble(numberTimestepsIgnoredPSW.getText());
		} else
			return 0.;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String command = e.getActionCommand();

		if (ACTIONCOMMAND_EDIT.equals(command)){
			if (editedNode != null)
				cancelAllSelectionBut(null);
		} else if (command.equals("CONST") || command.equals("LIST") || command.equals("INCREMENT")) {
			final CardLayout cl = (CardLayout) rightMiddle.getLayout();
			cl.show(rightMiddle,command);
		} else if (command.equals("CANCEL")) {
			resetSettings();
			enableDisableSettings(false);
			modifyButton.setEnabled(false);
			editedNode = null;
			editedTree = null;
			cancelAllSelectionBut(null);
			enableDisableParameterCombinationButtons();
		} else if (command.equals("RUNS_FIELD") && PlatformSettings.getGUIControllerForPlatform().getRunOption() == RunOption.LOCAL)  
			constDef.grabFocus();
		else if (command.equals("CONST_FIELD") || command.equals("STEP_FIELD"))
			modifyButton.doClick();
		else if (command.equals("START_FIELD"))
			incrEndValueField.grabFocus();
		else if (command.equals("END_FIELD"))
			incrStepField.grabFocus();
		else if (ACTIONCOMMAND_ADD_BOX.equals(command)) 
			addBox();
		else if (ACTIONCOMMAND_BROWSE.equals(command))
			chooseFile();
	}

	//----------------------------------------------------------------------------------------------------
	private void addBox() {
		final JPanel panel = createAParameterBox(false);
		combinationsPanel.add(panel);
		combinationsPanel.revalidate();

		combinationsScrPane.invalidate();
		combinationsScrPane.validate();
		final JScrollBar verticalScrollBar = combinationsScrPane.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
	}
	
	//----------------------------------------------------------------------------------------------------
	private void chooseFile() {
		final JFileChooser fileDialog = new JFileChooser(!"".equals(fileTextField.getToolTipText()) ? fileTextField.getToolTipText() : currentDirectory);
		if (!"".equals(fileTextField.getToolTipText()))
			fileDialog.setSelectedFile(new File(fileTextField.getToolTipText()));
		int dialogResult = fileDialog.showOpenDialog(dashboard);
		if (dialogResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileDialog.getSelectedFile();
			if (selectedFile != null) {
				currentDirectory = selectedFile.getAbsoluteFile().getParent();
				fileTextField.setText(selectedFile.getName());
				fileTextField.setToolTipText(selectedFile.getAbsolutePath());
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	String getCurrentDirectory() { return currentDirectory; }
	void setCurrentDirectory(final String currentDirectory) { this.currentDirectory = currentDirectory; }
		
	//----------------------------------------------------------------------------------------------------
	private void cancelAllSelectionBut(JComponent exception) {
		if (exception != parameterList)
			parameterList.clearSelection();

		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			if (exception != pGUI.tree) 
				pGUI.tree.setSelectionPath(null);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void updateDescriptionField(final Object... selectedValues) {
		if (selectedValues.length == 0) 
			parameterDescriptionLabel.setText("");
		else if (selectedValues.length == 1) {
			parameterDescriptionLabel.setText(selectedValues[0] != null ? ((AvailableParameter)selectedValues[0]).info.getDescription() : "");
		} else {
			String text = "";
			for (final Object selectedValue : selectedValues) {
				final AvailableParameter selectedParameter = (AvailableParameter) selectedValue;
				if (selectedParameter.info.getDescription() != null && !selectedParameter.info.getDescription().trim().isEmpty()) {
					final String humanName = selectedParameter.info.getName().replaceAll("([A-Z])", " $1");
					text += humanName + " -- " + selectedParameter.info.getDescription() + "\n\n";
				}
			}
			parameterDescriptionLabel.setText(text);
		}
		
		parameterDescriptionLabel.revalidate();
	}
	
	//----------------------------------------------------------------------------------------------------
	void showHideSubparameters(final JComboBox combobox, final SubmodelInfo info) {
		if ( !combobox.isDisplayable() ) {
			return;
		}
		final ClassElement classElement = (ClassElement) combobox.getSelectedItem();
		
		info.setActualType(classElement.clazz, classElement.instance);
		final int level = calculateParameterLevel(info);
		for (int i = singleRunParametersPanel.getComponentCount() - 1;i >= level + 1;--i) 
			singleRunParametersPanel.remove(i);
		
		if (classElement.clazz != null) {
			try {
				final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = ParameterTreeUtils.fetchSubparameters(currentModelHandler,info);
				String title = "Configure " + info.getName().replaceAll("([A-Z])"," $1").trim();
				createAndDisplayAParameterPanel(subparameters,title,info, true, currentModelHandler);
			} catch (final ModelInformationException e) {
				info.setActualType(null, null);
				JOptionPane.showMessageDialog(wizard, new JLabel(e.getMessage()), "Error while analyizing model", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				combobox.setSelectedIndex(0);
			}
		}
		
		parametersScrollPane.invalidate();
		parametersScrollPane.validate();
		final JScrollBar horizontalScrollBar = parametersScrollPane.getHorizontalScrollBar();
		horizontalScrollBar.setValue(horizontalScrollBar.getMaximum());
	}

	//----------------------------------------------------------------------------------------------------
	private int calculateParameterLevel(final SubmodelInfo info) {
		SubmodelInfo _info = info;
		int level = 0;
		while (_info.getParent() != null) {
			level++;
			_info = _info.getParent();
		}
		
		return level;
	}
	
	//----------------------------------------------------------------------------------------------------
	private ParameterInfo findOriginalInfo(final ParameterInfo info) {
		for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> pInfo : batchParameters) {
			final ParameterInfo converted = InfoConverter.parameterInfo2ParameterInfo(pInfo);
			if (info.equals(converted)) {
				converted.setRuns(0);
				return converted;
			}
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private void enableDisableParameterCombinationButtons() {
		final boolean selectionInTheList = parameterList.getSelectedIndex() >= 0; 
		
		for (ParameterCombinationGUI pGUI : parameterTreeBranches) {
			final boolean selectionInTheTree = pGUI.tree.getSelectionPath() != null;
			final boolean topLevelSelectionInTheTree = selectionInTheTree &&  
														pGUI.combinationRoot.isNodeChild((DefaultMutableTreeNode)pGUI.tree.getSelectionPath().getLastPathComponent()); 
					
			
			pGUI.addButton.setEnabled(selectionInTheList);
			pGUI.removeButton.setEnabled(topLevelSelectionInTheTree);
			pGUI.upButton.setEnabled(selectionInTheTree);
			pGUI.downButton.setEnabled(selectionInTheTree);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	DWizard getWizard() { return wizard; }
	IModelHandler getCurrentModelHandler() { return currentModelHandler; }
	Dashboard getDashboard() { return dashboard; }
	
	//----------------------------------------------------------------------------------------------------
	ClassElement[] fetchPossibleTypes(final SubmodelInfo parameterInfo) throws ComboBoxIsTooFullException {
		List<Class<?>> possibleTypes = possibleTypesCache.get(parameterInfo.getName()); 
		
		if (possibleTypes == null)
			possibleTypes = parameterInfo.getPossibleTypes();
		if (possibleTypes == null || possibleTypes.isEmpty()) {
			possibleTypes = new ArrayList<Class<?>>();
			possibleTypes.add(parameterInfo.getJavaType());
			
			possibleTypes.addAll(getSubtypes(parameterInfo.getJavaType()));
			
			// delete all interfaces and abstract classes
			for (final Iterator<Class<?>> iter = possibleTypes.iterator();iter.hasNext();) {
				final Class<?> clazz = iter.next();
				if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers()))
					iter.remove();
			}
		}
		
		possibleTypesCache.put(parameterInfo.getName(),possibleTypes);
		Collections.sort(possibleTypes,new Comparator<Class<?>>() {
			public int compare(final Class<?> class1, final Class<?> class2) {
				return class1.getName().compareTo(class2.getName());
			}
			
		});
		
		final ClassElement[] result = new ClassElement[possibleTypes.size() + 1];
		result[0] = new ClassElement(null, null);
		
		for (int i = 0;i < possibleTypes.size();++i) {
			if (possibleTypes.get(i).equals(parameterInfo.getActualType())) {
				result[i + 1] = new ClassElement(possibleTypes.get(i), parameterInfo.getInstance());
			} else {
				result[i + 1] = new ClassElement(possibleTypes.get(i), null);
			}
		}
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private Set<Class<?>> getSubtypes(final Class<?> type) throws ComboBoxIsTooFullException {
		final ClassLoader[] classloaders = new ClassLoader[] { currentModelHandler.getCustomClassLoader(),
															   currentModelHandler.getCustomClassLoader().getParent() };
		final ConfigurationBuilder builder = new ConfigurationBuilder().
				addUrls(ClasspathHelper.forClassLoader(currentModelHandler.getCustomClassLoader())). 
				addClassLoaders(classloaders).
				setScanners(new SubTypesScanner(false));
		final Reflections ref = new Reflections(builder);
		final Set<Class<?>> result = new HashSet<Class<?>>();
		final Set<String> subTypesStr = ref.getStore().getSubTypesOf(type.getName());
		if (subTypesStr.size() > MAX_SIZE_OF_SUBPARAMETER_COMBOBOX)
			throw new ComboBoxIsTooFullException();
		
		for (final String clsName : subTypesStr) {
			try {
				result.add(ReflectionUtils.forName(clsName,classloaders));
			} catch (final ReflectionsException e) {
				// intentionally blank
			}
		}

		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private String checkInfos(final boolean submodel) {
		final DefaultListModel listModel = (DefaultListModel) parameterList.getModel();
		for (int i = 0;i < listModel.getSize();++i) {
			final AvailableParameter param = (AvailableParameter) listModel.get(i);
			final String invalid = submodel ? checkSubmodelInfo(param.info) : checkFileInfo(param);
			if (invalid != null)
				return invalid;
		}
		
		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			final DefaultMutableTreeNode root = pGUI.combinationRoot;
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.breadthFirstEnumeration();
			nodes.nextElement(); // root
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				final String invalid = submodel ? checkSubmodelInfo(userObj.info) : checkFileInfo(userObj);
				if (invalid != null)
					return invalid;
			}
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private String checkSubmodelInfo(final ParameterInfo info) {
		if (info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) info;
			if (sInfo.getActualType() == null)
				return sInfo.getName().replaceAll("([A-Z])"," $1");
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private String checkFileInfo(final AvailableParameter parameter) {
		if (parameter.info.isFile()) {
			if (parameter.info.getValue() == null)
				return parameter.info.getName().replaceAll("([A-Z])"," $1");
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private String checkInfosInGeneTree() {
		final DefaultTreeModel treeModel = gaSearchHandler.getChromosomeTree();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = root.breadthFirstEnumeration();
		nodes.nextElement(); // root
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
			final String invalid = checkSubmodelInfo(userObj.getInfo());
			if (invalid != null)
				return invalid;
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private ParameterTree createParameterTreeFromParamSweepGUI() throws ModelInformationException {
		final ParameterTree result = new ParameterTree();
		
		final DefaultListModel listModel = (DefaultListModel) parameterList.getModel();
		for (int i = 0;i < listModel.getSize();++i) {
			final AvailableParameter param = (AvailableParameter) listModel.get(i);
			final AbstractParameterInfo<?> batchParameter = InfoConverter.parameterInfo2ParameterInfo(param.info);
			ParameterTreeUtils.addConstantParameterToParameterTree(batchParameter,result,currentModelHandler);
		}
		
		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			final DefaultMutableTreeNode root = pGUI.combinationRoot;
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.breadthFirstEnumeration();
			nodes.nextElement(); // root;
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				ParameterTreeUtils.filterConstants(userObj,result,currentModelHandler);
			}
		}
		
		for (final ParameterCombinationGUI pGUI : parameterTreeBranches) {
			final DefaultMutableTreeNode root = pGUI.combinationRoot;
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.preorderEnumeration();
			nodes.nextElement(); // root
			ParameterNode parent = result.getRoot();
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				if (ParameterTreeUtils.isMutableParameter(userObj)) {
					final AbstractParameterInfo<?> batchParameter = InfoConverter.parameterInfo2ParameterInfo(userObj.info);
					batchParameter.setRunNumber(1);
					final ParameterNode parameterNode = new ParameterNode(batchParameter);
					parent.add(parameterNode);
					parent = parameterNode;
				}
			}
		}
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private void hideError() {
		if (errorPopup != null) {
			errorPopup.hide();
			errorPopup = null;
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void showError(final String message, final JTextField textField) {
		PopupFactory popupFactory = PopupFactory.getSharedInstance();
		Point locationOnScreen = textField.getLocationOnScreen();
		JLabel messageLabel = new JLabel(message);
		messageLabel.setBorder(new LineBorder(Color.RED, 2, true));
		errorPopup = popupFactory.getPopup(textField,messageLabel,locationOnScreen.x - 10,locationOnScreen.y - 30);
		errorPopup.show();
	}

	private static int calculateNumberOfRuns(DefaultMutableTreeNode combinationRoot) {
		int result = 1;
		@SuppressWarnings("rawtypes")
		final Enumeration combinationElements = combinationRoot.children();
		
		while (combinationElements.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) combinationElements.nextElement();
			final ParameterInfo info = ((ParameterInATree)node.getUserObject()).info;
			
			if (info instanceof SubmodelInfo) 
				result *= calculateNumberOfRuns(node);
			else 
				result *= info.getMultiplicity();
		}
		
		return result;
	}
	
	
	//====================================================================================================
	// nested classes
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	static class ComboBoxIsTooFullException extends Exception {}
}
