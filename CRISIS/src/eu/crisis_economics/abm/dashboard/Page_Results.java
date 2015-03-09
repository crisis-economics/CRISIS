/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Christoph Aymanns
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
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import kcl.waterloo.graphics.GJAbstractGraphContainer;
import kcl.waterloo.graphics.GJGraph;
import kcl.waterloo.graphics.GJGraphContainer;
import kcl.waterloo.graphics.GJGraphInterface;
import kcl.waterloo.gui.gjgraph.GeneralOptions;
import kcl.waterloo.serviceproviders.GJEditorInterface;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.aitia.meme.gui.Wizard;
import ai.aitia.meme.gui.Wizard.Button;
import ai.aitia.meme.gui.Wizard.IWizardPage;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import aurelienribon.ui.css.Style;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.google.common.primitives.Doubles;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.looks.common.RGBGrayFilter;

import eu.crisis_economics.abm.dashboard.plot.DynamicPlot;
import eu.crisis_economics.abm.dashboard.plot.LinePlot;
import eu.crisis_economics.abm.dashboard.plot.Preprocessor;
import eu.crisis_economics.abm.dashboard.plot.ScatterPlot;
import eu.crisis_economics.abm.dashboard.results.Run;

/**
 * @author Tamás Máhr
 * @author Ross Richardson - also now contains references to methods used in outputting Dashboard charts
 */
public class Page_Results implements IWizardPage, IIconsInHeader {
	
	public static final class NullEditor implements GJEditorInterface {
		@Override
		public void setVisible(boolean flag) {
		}

		@Override
		public void setState(int i) {
		}

		@Override
		public void setSelectedTab(int i) {
		}

		@Override
		public void refresh() {
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		}

		@Override
		public boolean isShowing() {
			return false;
		}

		@Override
		public JTabbedPane getTabbedPane() {
			return null;
		}

		@Override
		public int getState() {
			return 0;
		}

		@Override
		public int getSelectedTab() {
			return 0;
		}

		@Override
		public GJGraphInterface getLayerForTab() {
			return null;
		}

		@Override
		public GJAbstractGraphContainer getGraphContainer() {
			return null;
		}

		@Override
		public GeneralOptions getContainerOptionsPanel() {
			return null;
		}
	}
//
//	private static final String POPUPMENU_CONFIG = "CONFIG";
//
//	private static final String INDEX_TAB_LABEL_EXPERIMENT = "Run ";

	private static final String TITLE = "Charts";
//the set height of a chart in the index pane
	public static final int INDEX_CHART_HEIGHT = 200;
//the set width of a chart in the index pane
	public static final int INDEX_CHART_WIDTH = 250;
//the set diameter of the circle markers in charts
	public static final int MARKER_CIRCLE_WIDTH = 3;
//the maximal number of datasets to be plotted on a single chart, before creating a second chart, thus avoiding clutter
	private static final int MAX_ELEMENTS_PER_PLOT = 99;
//the label of the time axis in charts
	public static final String TURN_SERIES_NAME = "Time";

	public static final String LAG_SERIES_NAME = "Lag";

	private static final Icon CLOSE_TAB_ICON = new ImageIcon(Dashboard.class.getResource("icons/closeTabButton.png"));
	
//	protected static final GJEditorInterface nullEditor = new NullEditor();

	protected int waterlooContextMenuEditGraphPosition = -1;
	
//	private static int experimentCount = 0;
	private static Map<String, Integer> experimentCounts = new HashMap<String, Integer>();

	private Wizard wizard;

	private Dashboard dashboard;

	private Container container;
//this is the box containing all chart previews
	private JTabbedPane indexBox;
//this is the main part of the window, with full-sized charts in a tabbed view
	private JTabbedPane tabPane;
	
	private JPanel populationPanel;
	private JTable populationTable;

//	private BlockingQueue<Pair<Map<String, Object>, Map<String, Object>>> resultsQueue;

	/**
	 * The map of plots currently updated by the result queue consumer thread.
	 */
//	private Map<String, List<GJPlotInterface>> currentOnLinePlots = new HashMap<String, List<GJPlotInterface>>();
	private List<DynamicPlot> currentOnLinePlots = new ArrayList<DynamicPlot>();
	
	private Map<DynamicPlot, Preprocessor> currentOffLinePlots = new HashMap<DynamicPlot, Preprocessor>();
	
	private Map<String, DynamicPlot> tabbedPlots = new HashMap<String, DynamicPlot>();
	
	/**
	 * The parameters of the simulation run from which data is being received and processed by the result queue consumer thread.
	 */
	private Map<String, Object> currentParameters;

	protected Logger log = LoggerFactory.getLogger(Page_Results.class);

	/**
	 * The number of result rows to collect before sending them to be displayed. If results are generated too fast, this can be used to limit the
	 * {@link SwingUtilities#invokeLater(Runnable)} calls that bigs down the GUI.
	 */
	protected int numOfResultsToDisplay = 100;

	/**
	 * The number of milliseconds to wait between updating the charts.
	 */
	protected long chartUpdatePeriod = 2000;

	private boolean advancedCharts = true;
	
	private ParameterTree bestCombinationTree = null; 
	
	public Page_Results(final Wizard wizard, final Dashboard dashboard, final int numOfResultsToDisplay, final int chartUpdatePeriod) {
		this(wizard, dashboard, null, numOfResultsToDisplay, chartUpdatePeriod);
	}
	
	
	public Page_Results(final Wizard wizard, final Dashboard dashboard,
			final BlockingQueue<Pair<Map<String, Object>, Map<String, Object>>> resultsQueue, final int numOfResultsToDisplay,
			final int chartUpdatePeriod) {
		this.wizard = wizard;
		this.dashboard = dashboard;
		this.numOfResultsToDisplay = numOfResultsToDisplay;
		this.chartUpdatePeriod = chartUpdatePeriod;

		if (resultsQueue != null){
			Thread resultQueueConsumerThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						LinkedList<Pair<Map<String,Object>,Map<String,Object>>> list = new LinkedList<Pair<Map<String, Object>, Map<String, Object>>>();
						Map<String, List<Object>> values = new HashMap<String, List<Object>>();
						Pair<Map<String, Object>, Map<String, List<Object>>> pair = new Pair<Map<String,Object>, Map<String, List<Object>>>(null, values);
						int numberOfProcessedResults = 0;
						Run run = null;
						
						while (true){
							try {
								if (getChartUpdatePeriod() > 0){
									Thread.sleep(getChartUpdatePeriod());
								}
							} catch (InterruptedException e){
								// ignored
							}
							
							list.add(resultsQueue.take());
							resultsQueue.drainTo(list);
							
							if (pair.getValue0() == null){
								pair = pair.setAt0(list.get(0).getValue0());
								run = new Run();
								if (pair.getValue0() != null){
									for (String paramName : pair.getValue0().keySet()){
										run.addParameter(paramName, pair.getValue0().get(paramName));
									}
								}
							}
							
							// we received a list of pairs, which should be converted to a pair, where the value map (pair.getValue1()) contains lists
							// as values
							// [(parameters, {datasourcename0 => value, datasourcename1 => value}), (parameters, {datasourcename0 => value, datasourcename1 => value})]
							// (parameters, {datasourcename0 => [value0, value1, ...], datasourcename1 => [value0, value1, ...]})
							for (Pair<Map<String,Object>,Map<String,Object>> actPair : list) {
								// if the pair contains (null, null), then the simulation is finished and no more data is to be expected
								// if the parameters changed update the charts with data received so far, then continue to collect data
								if ((actPair.getValue0() == null && actPair.getValue1() == null) || 
										(!actPair.getValue0().equals(pair.getValue0()) ||
												(numberOfProcessedResults >= getNumOfResultsToDisplay()))){
									SwingUtilities.invokeLater(new ChartUpdater(pair));
									values = new HashMap<String, List<Object>>();
									pair = new Pair<Map<String,Object>, Map<String, List<Object>>>(actPair.getValue0(), values);
									numberOfProcessedResults = 0;
								}

								// if the pair contains (null, null), then the simulation is finished and no more data is to be expected
								if (actPair.getValue0() == null && actPair.getValue1() == null){
									SwingUtilities.invokeLater(new ChartUpdater(new Pair<Map<String,Object>, Map<String,List<Object>>>(null, null)));
									
									SwingUtilities.invokeLater(new OfflineChartUpdater(run));
									continue;
								}
								
								for (String dataSourceName : actPair.getValue1().keySet()){
									List<Object> valueList = values.get(dataSourceName);
									if (valueList == null){
										valueList = new ArrayList<Object>();
										values.put(dataSourceName, valueList);
									}
									valueList.add(actPair.getValue1().get(dataSourceName));
									
									run.addResult(dataSourceName, actPair.getValue1().get(dataSourceName));
								}
								
								numberOfProcessedResults++;
							}
							
							list.clear();
							
//							if (numberOfResults >= getNumOfResultsToDisplay()){
//								SwingUtilities.invokeLater(new ChartUpdater(pair));
//								values = new HashMap<String, List<Object>>();
//								pair = new Pair<Map<String,Object>, Map<String, List<Object>>>(null, values);
//								numberOfResults = 0;
//							}
						}
					} catch (InterruptedException e){
						log.error("Result queue consumer thread interrupted.", e);
					}
				}
			}, "ResultQueueConsumer");
			
			resultQueueConsumerThread.start();
		}
		
		container = initContainer();
	}

	/** {@inheritDoc} 
	 * 
	 */
	@Override
	public Container getPanel() {
		return container;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public String getInfoText(Wizard w) {
		return w.getArrowsHeader("");
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("icons/chart.png"));
	}
	/** {@inheritDoc} 
	 */
	@Override
	public boolean isEnabled(Button b) {
		if (b == Button.BACK){
			return true;
		}
		return false;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public boolean onButtonPress(Button b) {
		if (b == Button.BACK){
			if (dashboard.getModelHandler() == null){
				wizard.gotoPage(0);
			} else if (dashboard.isRunning()) {
				return true;
			} else {
				wizard.gotoPageRel(-2);
			}
		}
		return false;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public void onPageChange(boolean show) {
		if (show)
			dashboard.getSaveConfigMenuItem().setEnabled(true);

//		if (show && !dashboard.isRunning()){
//			// during the initial setup of the wizard, this page is visited and the recorderFileName is null
//			final String recorderFileName = dashboard.getRecorderFileName();
//			if (recorderFileName == null){
//				return;
//			}
//
//			final File file = new File(recorderFileName);
//			if (! file.exists()){
//				// if we come from the parameters page, then there is already a result file name, but no file has yet been generated
////				JOptionPane.showMessageDialog(dashboard, "Could not find the result file!", "No result file", JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//		}
	}
	
	 /**
	   * Adds a component to a JTabbedPane with a little "close tab" button on the
	   * right side of the tab.
	   *
	   * @param tabbedPane the JTabbedPane
	   * @param c any JComponent
	   * @param title the title for the tab
	   * @param icon the icon for the tab, if desired
	   */
	  @SuppressWarnings("serial")
   public void addClosableTab(final JTabbedPane tabbedPane, final JComponent c, final String title,
	          final Icon icon) {
	    // Add the tab to the pane without any label
	    tabbedPane.addTab(null, c);
	    int pos = tabbedPane.indexOfComponent(c);

	    // Create a FlowLayout that will space things 5px apart
//	    FlowLayout f = new FlowLayout(FlowLayout.CENTER, 5, 0);

	    // Make a small JPanel with the layout and make it non-opaque
	    JPanel pnlTab = new JPanel(new BorderLayout(5, 0));
	    pnlTab.setOpaque(false);

	    // Add a JLabel with title and the left-side tab icon
	    JLabel lblTitle = new JLabel(title);
	    lblTitle.setIcon(icon);

	    // Create a JButton for the close tab button
	    JButton btnClose = new JButton();
	    btnClose.setOpaque(false);

	    // Configure icon and rollover icon for button
	    btnClose.setRolloverIcon(CLOSE_TAB_ICON);
	    btnClose.setRolloverEnabled(true);
	    btnClose.setIcon(RGBGrayFilter.getDisabledIcon(btnClose, CLOSE_TAB_ICON));

	    // Set border null so the button doesn't make the tab too big
	    btnClose.setBorder(null);

	    // Make sure the button can't get focus, otherwise it looks funny
	    btnClose.setFocusable(false);

	    // Put the panel together
	    pnlTab.add(lblTitle, BorderLayout.CENTER);
	    pnlTab.add(btnClose, BorderLayout.EAST);

	    // Add a thin border to keep the image below the top edge of the tab
	    // when the tab is selected
	    pnlTab.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

	    // Now assign the component for the tab
	    tabbedPane.setTabComponentAt(pos, pnlTab);

	    // Add the listener that removes the tab
	    ActionListener listener = new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        // The component parameter must be declared "final" so that it can be
	        // referenced in the anonymous listener class like this.
	        tabbedPane.remove(c);
	        
			String chartId = c.getName();
			if (chartId != null){
				if (tabbedPlots.remove(chartId) == null){
					throw new IllegalStateException("Could not find the plot that is being removed from the tabbed panel!");
				}
			} else {
				throw new IllegalStateException("Component being removed does not have a chartId set as component name.");
			}

	      }
	    };
	    btnClose.addActionListener(listener);

	    // Optionally bring the new tab to the front
	    tabbedPane.setSelectedComponent(c);

	    //-------------------------------------------------------------
	    // Bonus: Adding a <Ctrl-W> keystroke binding to close the tab
	    //-------------------------------------------------------------
	    AbstractAction closeTabAction = new AbstractAction() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        tabbedPane.remove(c);
	      }
	    };

	    // Create a keystroke
	    KeyStroke controlW = KeyStroke.getKeyStroke("control W");

	    // Get the appropriate input map using the JComponent constants.
	    // This one works well when the component is a container. 
	    InputMap inputMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

	    // Add the key binding for the keystroke to the action name
	    inputMap.put(controlW, "closeTab");

	    // Now add a single binding for the action name to the anonymous action
	    c.getActionMap().put("closeTab", closeTabAction);
	  }


	private Container initContainer() {
		final JPanel theContainer = new JPanel(new CardLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setOneTouchExpandable(true);

		indexBox = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
//		JScrollPane scrollPane = new JScrollPane(indexBox);
		splitPane.setLeftComponent(indexBox);

		tabPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		splitPane.setRightComponent(tabPane);
		splitPane.setDividerLocation(INDEX_CHART_WIDTH + 200);
		splitPane.setName("CHARTS");
		theContainer.add(splitPane,"CHARTS");

		populationTable = new JTable();
		populationTable.setShowGrid(true);
		populationTable.setSelectionModel(new NullSelectionModel());
		final DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) populationTable.getTableHeader().getDefaultRenderer();
		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		final JScrollPane populationTableScroll = new JScrollPane(populationTable,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
																   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		final JButton runBestButton = new JButton("Run experiment with the best parameter settings");
		runBestButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(final ActionEvent e) {
				if (bestCombinationTree != null) {
					dashboard.getModelHandler().setIntelliMethodPlugin(null);
					dashboard.getModelHandler().setParameters(bestCombinationTree);
					dashboard.setOnLineCharts(false);
					dashboard.setDisplayAdvancedCharts(true);
					try {
						dashboard.getModelHandler().getParameters(); // necessary to initialize model
						wizard.gotoPageRel(-1);
					} catch (final ModelInformationException ex) {
						JOptionPane.showMessageDialog(dashboard,ex.getMessage(), "Error while analyzing model", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				} else 
					JOptionPane.showMessageDialog(dashboard,"Cannot run experiment.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		populationPanel = FormsUtils.build("~ f:p:g ~ p ~",
										   "00 f:p:g||" +
										   "_1 p",
										   populationTableScroll,
										   runBestButton).getPanel();
		populationPanel.setName("POPULATIONS");
		populationPanel.setBorder(BorderFactory.createTitledBorder(null,"Fitness of chromosomes",TitledBorder.LEADING,TitledBorder.BELOW_TOP));
		
		theContainer.add(populationPanel,"POPULATIONS");
		
		return theContainer;
	}

	/**
	 * Displays charts for parameter sweep experiments. The results parameter contains the recorded values for each simulation run in sequence. That
	 * is, the first Map<String, List<Number>> contains the results of the first run, and so on.
	 * 
	 * @param results the recorded values for each simulation run
	 */
	public void displayCharts(List<Run> results, boolean displayAdvancedCharts, int numInitialStepsToIgnore){
		if (results == null){ //this signals the end of an experiment that has been displayed on-line
			currentParameters = null;
			return;
		}
		
		for (Run run : results) {
			displayCharts(run, displayAdvancedCharts, numInitialStepsToIgnore, false);
		}
	}
	
	/**
	 * Displays the charts which are pre-selected in the model
	 * <p>
	 * This method will display charts in the index box. It will display all the charts it can out of a list of known
	 * data which may be in the model results file.
	 * 
	 * @param results the parameters and the results of a given run
	 */
	public void displayCharts(final Run results, boolean displayAdvancedCharts, final int numInitialStepsToIgnore, final boolean partialResults){
		final CardLayout layout = (CardLayout) getPanel().getLayout();
		layout.show(getPanel(),"CHARTS");

		results.ignoreInitialResults(numInitialStepsToIgnore);
//		Set<String> seriesNames = results.resultNames(); 	//all the names of datasets in the results map
		this.advancedCharts = displayAdvancedCharts;
		double[] turnList = new double[results.getResults(TURN_SERIES_NAME).size()]; 	//the list of time values in each iteration of the simulation (which may be variable-length steps!)
		
		//make the list of time-steps using a loop
		int i = 0;
		for (Object turn : results.getResults(TURN_SERIES_NAME)){
			turnList[i++] = ((Number)turn).doubleValue();
		}

		//Make color palette
		int colorCounter=0;
		final ArrayList<Color> colors = new ArrayList<Color>(); 
		java.util.Random rand = new java.util.Random();
		for (colorCounter=0; colorCounter < results.getResultsSize(); colorCounter++){	
			Color c = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
			colors.add(c);
		}
		
//new experimental pane to replace the tabbed one used previously, which can keep track of multiple experiments.
		//JOutlookBar experimentPane = new JOutlookBar(false);
		
		JTabbedPane experimentPane = new JTabbedPane(SwingConstants.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
		
		int experimentCount = 1;
		if (experimentCounts.containsKey(dashboard.getModelHandler().getModelClass().getName())){
			experimentCount = experimentCounts.get(dashboard.getModelHandler().getModelClass().getName());
			experimentCount++;
		}
		indexBox.addTab(dashboard.getModelHandler().getModelClass().getSimpleName() + " " + experimentCount, experimentPane);
		experimentCounts.put(dashboard.getModelHandler().getModelClass().getName(), experimentCount);
		
		indexBox.setSelectedIndex(indexBox.getTabCount() - 1);
		
		
		JOutlookBar miscPane = new JOutlookBar();
		JOutlookBar banksPane = new JOutlookBar();
		JOutlookBar banksPane2 = new JOutlookBar();
		JOutlookBar centralBankPane = new JOutlookBar();
		JOutlookBar firmsPane = new JOutlookBar();
		JOutlookBar firmsStatisticsPane = new JOutlookBar();
		JOutlookBar firmsPane2 = new JOutlookBar();
		JOutlookBar firmsStatisticsPane2 = new JOutlookBar();
		JOutlookBar fundsPane = new JOutlookBar();
		JOutlookBar governmentPane = new JOutlookBar();
		JOutlookBar householdsPane = new JOutlookBar();
		JOutlookBar householdsStatisticsPane = new JOutlookBar();
		JOutlookBar globalTimePane = new JOutlookBar();
		JOutlookBar globalTimePane2 = new JOutlookBar();
		JOutlookBar globalTimePane3 = new JOutlookBar();
		JOutlookBar globalScatterPane = new JOutlookBar();
		JOutlookBar optimizePane = new JOutlookBar();
		
		
		
		experimentPane.addTab("Parameters", miscPane);
		experimentPane.addTab("Banks", banksPane);
		experimentPane.addTab("Banks 2", banksPane2);
		if (true) { // !(dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModel.class.getName()))){
//		if (! dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModel.class.getName())){
			experimentPane.addTab("Central Bank", centralBankPane);
		}
		if (advancedCharts){
			experimentPane.addTab("Firms", firmsPane);
			experimentPane.addTab("Firms 2", firmsPane2);
		}
		experimentPane.addTab("Firms' Summary Statistics", firmsStatisticsPane);
		experimentPane.addTab("Firms' Summary Statistics 2", firmsStatisticsPane2);
//		if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())){
			experimentPane.addTab("Funds", fundsPane);
			experimentPane.addTab("Government", governmentPane);
			if(advancedCharts){
				experimentPane.addTab("Households", householdsPane);
			}
			experimentPane.addTab("Households' Summary Statistics", householdsStatisticsPane);
//		}
		experimentPane.addTab("Global Time Series 1", globalTimePane);
		experimentPane.addTab("Global Time Series 2", globalTimePane2);
		experimentPane.addTab("Global Time Series 3", globalTimePane3);
		experimentPane.addTab("Global Scatter Plots", globalScatterPane);
		experimentPane.addTab("Optimization", optimizePane);

		// first of all, add the parameters
		DefaultFormBuilder formBuilder = FormsUtils.build("l:p ~ l:p", "");
//		Set<String> parameterNames = results.parameterNames();
		TreeSet<String> parameterNames = new TreeSet<String>(results.parameterNames());
		for (String parameterName : parameterNames) {
			formBuilder.append(new JLabel(parameterName), new JLabel(results.getParameter(parameterName).toString()));
		}
		JPanel parameterPanel = formBuilder.getPanel();
		Style.registerCssClasses(parameterPanel, ".commonPanel");
		
		miscPane.addBar("Parameters", new JScrollPane(parameterPanel));
		JComponent plotObjects;
		
		//plot BANK CAPITAL ADEQUACY RATIO - getCapitalAdequacyRatio() method
		plotObjects = multiPlotObjects(results, "bankCapitalAdequacyRatio", "Time", "Banks' Capital Adequacy Ratios", false, colors, true);
		if (plotObjects != null){
			banksPane.addBar("Capital Adequacy Ratio", new JScrollPane(plotObjects));
		}	

		//plot BANK CASH RESERVES - getBankCashOverTime() method in Simulation.java
		plotObjects = multiPlotObjects(results, "bankCashReserves", "Time", "Banks' Cash Reserves (monetary units)", false, colors, true);
		if (plotObjects != null){
			banksPane.addBar("Cash Reserves", new JScrollPane(plotObjects));
		}	
	
		//plot BANKS' COMMERCIAL LOANS (ALSO KNOWN AS CREDIT) - bank loans to non-banks such as firms and households, getCommercialLoans() in Bank.java
		plotObjects = multiPlotObjects(results, "bankCommercialLoans", "Time", "Banks' Loans to Non-Banks (monetary units)", false, colors, true);
		if (plotObjects != null){
			banksPane.addBar("Commercial Loans (Credit)", new JScrollPane(plotObjects));
		}
		
		//plot BANKS' INTERBANK LOANS - bank loans to banks, getInterbankLoans() in Bank.java
		plotObjects = multiPlotObjects(results, "bankInterbankLoans", "Time", "Banks' Loans to other Banks on the Interbank Market (monetary units)", false, colors, advancedCharts);
		if (plotObjects != null){
			banksPane.addBar("Interbank Loans", new JScrollPane(plotObjects));
		}

/*		//plot BANKS' TOTAL LOANS - bank loans to all agents, getLoans() in Bank.java
		plotObjects = multiPlotObjects(results, "bankLoans", "Time", "Banks' Total Loans (monetary units)",true, colors, true);
		if (plotObjects != null){
			banksPane.addBar("Loans (Total)", new JScrollPane(plotObjects));
		}		
*/		
		
//		if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) 
		{
			//plot BANK TOTAL DEPOSIT ACCOUNT VALUES (NOTE CONTAINS DEPOSITS FROM NOT ONLY HOUSEHOLDS, BUT ALSO FIRMS... NOT SURE BUT POSSIBLY EVEN BANKS???) - method in Simulation.java
			plotObjects = multiPlotObjects(results, "bankDepositAccountHoldings", "Time", "Banks' Total Deposit Account Holdings (firms and households if included in the model) (monetary units)", false, colors, true);
			if (plotObjects != null){
				banksPane.addBar("Deposit Account Holdings", new JScrollPane(plotObjects));
			}
			
			//plot BANK DEPOSITS FROM HOUSEHOLDS ONLY - method getBankDepositsFromHouseholds()
			plotObjects = multiPlotObjects(results, "bankDepositsFromHouseholds", "Time", "Banks' Deposits from the Household Sector (monetary units)", false, colors, true);
			if (plotObjects != null){
				banksPane2.addBar("Deposits from Households", new JScrollPane(plotObjects));
			}
		}
/*		else// if ((dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModel.class.getName()))||(dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModelWithCentralBank.class.getName()))) 
		{
			//plot BANK TOTAL DEPOSIT ACCOUNT VALUES - method in Simulation.java
			plotObjects = multiPlotObjects(results, "bankDepositAccountHoldings", "Time", "Banks' Total Deposit Account Holdings (from firms) (monetary units)", false, colors, true);
			if (plotObjects != null){
				banksPane.addBar("Deposit Account Holdings", new JScrollPane(plotObjects));
			}
		}
*/ 
//		else {System.out.println("Model class case not in Page_Results.java");}					
		
		//plot BANK EQUITY - method in agent.java
		plotObjects = multiPlotObjects(results, "bankEquity", "Time", "Banks' Equity (monetary units)",false, colors);
		if (plotObjects != null){
			banksPane2.addBar("Equity", new JScrollPane(plotObjects));
		}
		
		//plot BANK LEVERAGE - method in Simulation.java
		plotObjects = multiPlotObjects(results, "BankLeverage", "Time", "Banks' Leverage (total assets / equity)",false, colors);
		if (plotObjects != null){
			banksPane2.addBar("Leverage", new JScrollPane(plotObjects));
		}
		
		//plot BANK RISK WEIGHTED ASSETS RATIO - getRiskWeightedAssets() method in Bank.java
		plotObjects = multiPlotObjects(results, "bankRiskWeightedAssets", "Time", "Banks' Risk-Weighted Assets (monetary units)", false, colors, true);
		if (plotObjects != null){
			banksPane2.addBar("Risk-Weighted Assets", new JScrollPane(plotObjects));
		}
		
	    //plot BANK STOCK PRICE - note this uses methods for getMarketValue() method in AbstractModel.java
      plotObjects = multiPlotObjects(results, "bankStockPrice", "Time", "Banks' Stock Prices (monetary units)", false, colors, advancedCharts);
      if (plotObjects != null){
         banksPane2.addBar("Stock Prices", new JScrollPane(plotObjects));
      }
		
		//plot BANK TOTAL ASSETS - method in Simulation.java
		plotObjects = multiPlotObjects(results, "bankTotalAssets", "Time", "Banks' Total Assets on their Balance Sheets (monetary units)",false, colors);
		if (plotObjects != null){
			banksPane2.addBar("Total Assets", new JScrollPane(plotObjects));
		}
		
		JComponent graphContainer;		//Need for using miscBox in Central Bank Indicators and Global Indicators below
//CENTRAL BANK INDICATORS UNDER ONE TAB USING MISC BOX///////////////////////////////////////////////////////////////////////////////	
		if (true) { // !(dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModel.class.getName()))){
		
		Box.createVerticalBox();
			
			//plot CENTRAL BANK REFINANCING AND OVERNIGHT DEPOSIT INTEREST RATE (rate banks borrow from / lend to central bank) using getKeyInterestRate() and getCBovernightDepositRate() methods in MarketClearingModelWithCentralBank.java, refinancing rate is the target interest rate plus a spread, note the Overnight Deposit rate at the central bank is the target interest rate minus the spread (see CentralBankStrategy.java)
			graphContainer = plotNVariables(new String[]{"CBrefinancingRate", "CBovernightDepositRate"}, results, turnList, "Time", new String[]{"Central Bank Refinancing and Overnight Deposit Rates (cost of borrowing from / lending to Central Bank)"}, true, false, false, colors);
			if (graphContainer != null){
				centralBankPane.addBar("Refinancing and Deposit Rates", new JScrollPane(graphContainer));
//					miscBox2.add(graphContainer);					
			}
		
			//plot getBankCashOverTime() method in Simulation.java, and getNegativeAggregateBankLiabilitiesToCentralBank() and getAggregateBankCashReservesMinusLiabilitiesToCentralBank() in MarketClearingModelWithCentralBank.java
			graphContainer = plotNVariables(new String[]{"sum(bankCashReserves)", "negativeAggregateBankLiabilitiesToCentralBank", "aggregateBankCashReservesMinusLiabilitiesToCentralBank"}, results, turnList, "Time", new String[]{"Aggregate Banks' Cash Reserves, Banks' Liabilities to Central Bank and the Net Value (monetary units)"}, true, false, false, colors, true);
			if (graphContainer != null){
				centralBankPane.addBar("Banking Sector Deposits and Liabilities", new JScrollPane(graphContainer));
//					miscBox2.add(graphContainer);
			}
			
			//Method getCBLoanVolume() in MarketClearingModelWithCentralBank.java
			graphContainer = plotNVariables(new String[]{"CBLoanVolume"}, results, turnList, "Time", new String[]{"Volume of Loans from Central Bank (monetary units)"}, true, false, false, colors, true);
			if (graphContainer != null){
				centralBankPane.addBar("Loan Volume", new JScrollPane(graphContainer));
//					miscBox2.add(graphContainer);
			}
					
			//Method getOvernightVolume() in MarketClearingModelWithCentralBank.java
			graphContainer = plotNVariables(new String[]{"CBdepositVolume"}, results, turnList, "Time", new String[]{"Volume of Overnight Deposits lent to Central Bank (monetary units)"}, true, false, false, colors, true);
			if (graphContainer != null){
				centralBankPane.addBar("Deposit Volume", new JScrollPane(graphContainer));
//					miscBox2.add(graphContainer);
			}
		
		}
	//END OF CENTRAL BANK INDICATORS//////////////////////////////////////////////////////////////////////////////////////////////////
	
		//plot FIRM DIVIDEND - method in StockReleasingFirm.java
		plotObjects = multiPlotObjects(results, "firmDividend", "Time", "Firms' Dividend Payout (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Dividend Payout", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmDividend", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Dividend Payout Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRM EQUITY VALUE - method in Agent.java
		plotObjects = multiPlotObjects(results, "firmEquity", "Time", "Firms' Equity (monetary units)", true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Equity", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmEquity", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Equity Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRMS' LOANS PER EQUITY - getLoansPerEquity() in LoanStrategyFirm.java, is this correct for I-O model too?  Ross
		plotObjects = multiPlotObjects(results, "firmLoansPerEquity", "Time", "Firms' Loans Per Equity",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Loans Per Equity", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmLoansPerEquity", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Loans Per Equity Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRMS' LOANS AS A PROPORTION OF LOANS + EQUITY - getLoansDividedByLoansPlusEquity() in LoanStrategyFirm.java, is this correct for I-O model too?  Ross
		plotObjects = multiPlotObjects(results, "firmLoansDividedByLoansPlusEquity", "Time", "Firms' Loans As a Proportion of their Total Loans Plus Equity",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Loans / (Loans + Equity)", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmLoansDividedByLoansPlusEquity", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Loans / (Loans + Equity) Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRMS' GOODS SELLING PRICES - getSellingPrice() in MacroFirm.java (only applicable for I-O model)
		plotObjects = multiPlotObjects(results, "firmGoodsSellingPrice", "Time", "Firms' Selling Price for Output Goods (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Output Prices", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmGoodsSellingPrice", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Output Prices Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRM MARKET CAPITALIZATION - getMarketCapitalization() in StockReleasingFirm.java
		plotObjects = multiPlotObjects(results, "firmMarketCapitalization", "Time", "Firms' Market Capitalization (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Market Capitalization", new JScrollPane(plotObjects));
		}		
		plotObjects = plotSummaryStats(results, turnList, "firmMarketCapitalization", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Market Capitalization Stats", new JScrollPane(plotObjects));
		}

		//plot FIRM PRODUCTION - getProduction() in LoanStrategyFirm.java and MacroFirm.java
		plotObjects = multiPlotObjects(results, "firmProduction", "Time", "Firms' Production (units produced)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Production", new JScrollPane(plotObjects));
		}		
		plotObjects = plotSummaryStats(results, turnList, "firmProduction", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane.addBar("Production Stats", new JScrollPane(plotObjects));
		}

		//plot FIRM PROFIT - getProfit() method in Simulation.java for Fixed Labour market (formerly Cobb-Douglas model) looking at LoanStrategyFirms' getTellProfit() method.  For AbstractModel.java (I-O model), new getProfit() method written picking up profit from MacroFirm.java getProfit() method - Ross
		plotObjects = multiPlotObjects(results, "firmProfit", "Time", "Firms' Profits (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Profits", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmProfit", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Profits Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRM NUMBER OF SHARES - StockReleasingFirm#getNumberOfEmittedShares()
		plotObjects = multiPlotObjects(results, "firmNumberOfEmittedShares", "Time", "Firms' Quantity of Outstanding Shares (number of shares)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Number of Shares", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmNumberOfEmittedShares", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Number of Shares Stats", new JScrollPane(plotObjects));
		}

		//plot FIRM REVENUE - LoanStrategyFirm::getRevenue() and MacroFirm::getRevenue() for Fixed Labour market (formerly Cobb-Douglas) models and I-O macro models respectively.
		plotObjects = multiPlotObjects(results, "firmRevenue", "Time", "Firms' Revenue (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Revenue", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmRevenue", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Revenue Stats", new JScrollPane(plotObjects));
		}
		
		//plot FIRM STOCK PRICE - note this uses methods for getMarketValue() method in ClearingFirm.java (for models relying on the market clearing mechanism) that returns stock price rather than market capitalization.  Note there is an old version of getMarketValue in StockReleasingFirm.java but that is a relic of Limit Order Book models.
		plotObjects = multiPlotObjects(results, "firmStockPrice", "Time", "Firms' Stock Prices (monetary units)", false, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Stock Prices", new JScrollPane(plotObjects));
		}
        
        // 10th, 20th, 30th ... Firm Stock Price Percentiles 
        {
           List<Color> sunrisePalette = new ArrayList<Color>();
           for(int j = 1; j<= 10; ++j)
              sunrisePalette.add(new Color(1.f, j / 10.f, 0.f));
           plotObjects = multiPlotObjects(
              results, "FirmPercentileStockPrices", "Time",
              "Firms' Stock Prices - 10%, 20%, 30% ... Deciles",
              false,
              sunrisePalette,
              advancedCharts
              );
           if (plotObjects != null) {
              firmsPane2.addBar("Stock Prices Deciles", new JScrollPane(plotObjects));
           }
        }
		
		plotObjects = plotSummaryStats(results, turnList, "firmStockPrice", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Stock Prices Stats", new JScrollPane(plotObjects));
		}

		
/*		//plot FIRM CHANGE IN STOCK PRICE - method in ClearingFirm.java
		plotObjects = multiPlotObjects(results, "firmLastChangeInStockPrice", "Time", "Firms' Change in Stock Price (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane.addBar("Stock Price Change", new JScrollPane(plotObjects));
		}
*/
	
		//plot FIRM LOG STOCK RETURN (log(P_t/P_t-1)) - method in ClearingFirm.java
		plotObjects = multiPlotObjects(results, "firmLogStockReturn", "Time", "Firms' Log Stock Returns (log(Price_t/Price_t-1))",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Log Stock Return", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmLogStockReturn", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Log Stock Return Stats", new JScrollPane(plotObjects));
		}
			
		//plot AUTOCORRELATION OF FIRMS LOG STOCK RETURN (log(P_t/P_t-1)) - method in ClearingFirm.java
		plotObjects = this.multiPlotAutoCorrelation(results, "firmLogStockReturn", true, colors, partialResults);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Autocorrelation of Log Stock Returns", new JScrollPane(plotObjects));
		}		

		//plot FIRM TOTAL ASSETS - method in Simulation.java
		plotObjects = multiPlotObjects(results, "firmTotalAssets", "Time", "Firms' Total Assets on their Balance Sheets",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Total Assets", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmTotalAssets", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Total Assets Stats", new JScrollPane(plotObjects));
		}		

		//plot FIRM TOTAL STOCK RETURN PER SHARE (includes both dividend and change in stock price) - method in ClearingFirm.java
		plotObjects = multiPlotObjects(results, "firmTotalStockReturn", "Time", "Total Return to Stockholders per share including dividend and capital gain (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			firmsPane2.addBar("Total Stock Return", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "firmTotalStockReturn", false, false, colors, true);
		if (plotObjects != null){
			firmsStatisticsPane2.addBar("Total Stock Return Stats", new JScrollPane(plotObjects));
		}

//	if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName()))
		
		//plot FUNDS' BALANCES - getBalance(Depositor) method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundBalance", "Time", "The Value of a MutualFund's Cash Deposit in its Bank Account (monetary units)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Balance at Bank", new JScrollPane(plotObjects));
		}

		//plot FUNDS' EMISSION PRICE (is this fund share price?) - getEmissionPrice() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundEmissionPrice", "Time", "Funds' Emission Prices",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Emission Price", new JScrollPane(plotObjects));
		}

		//plot FUNDS' EQUITY - getEquity method in Agent.java 
		plotObjects = multiPlotObjects(results, "fundEquity", "Time", "Funds' Equity (monetary units)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Equity", new JScrollPane(plotObjects));
		}
		
		//plot FUNDS' PERCENTAGE OF INVESTMENT ALLOCATED TO BONDS - getPercentageValueInBonds() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundPercentageValueInBonds", "Time", "Funds' Proportion of Investments allocated to Bonds (%)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Investment in Bonds (%)", new JScrollPane(plotObjects));
		}
		
		//plot FUNDS' PERCENTAGE OF INVESTMENT ALLOCATED TO STOCKS - getPercentageValueInStocks() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundPercentageValueInStocks", "Time", "Funds' Proportion of Investments allocated to Stocks (%)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Investment in Stocks (%)", new JScrollPane(plotObjects));
		}
		
		//plot FUNDS' PERCENTAGE OF INVESTMENT ALLOCATED TO BANK STOCKS - getPercentageValueInBankStocks() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundPercentageValueInBankStocks", "Time", "Funds' Proportion of Investments allocated to Banks' Stocks (%)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Investment in Bank Stocks (%)", new JScrollPane(plotObjects));
		}
		
		//plot FUNDS' PERCENTAGE OF INVESTMENT ALLOCATED TO STOCKS - getPercentageValueInFirmStocks() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundPercentageValueInFirmStocks", "Time", "Funds' Proportion of Investments allocated to Firms' Stocks (%)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Investment in Firm Stocks (%)", new JScrollPane(plotObjects));
		}
		
		//plot FUNDS' TOTAL ASSETS - getTotalAssets() method in MutualFund.java 
		plotObjects = multiPlotObjects(results, "fundTotalAssets", "Time", "Funds' Total Assets (monetary units)",true, colors, true);
		if (plotObjects != null){
			fundsPane.addBar("Total Assets", new JScrollPane(plotObjects));
		}

		//plot GOVERNMENT'S CUMULATIVE BAILOUT COSTS - getGovernmentCumulativeBailoutCosts() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentCumulativeBailoutCosts"}, results, turnList, "Time", new String[]{"The Government's Cumulative Bailout Costs (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Bailout Costs", new JScrollPane(graphContainer));
		}

		//plot GOVERNMENT'S BUDGET - getTotalExpensesBudget() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentBudget"}, results, turnList, "Time", new String[]{"The Government's Total Expenses Budget (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Budget", new JScrollPane(graphContainer));
		}

		//plot GOVERNMENT'S CASH RESERVE - getGovernmentCashReserveValue() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentCashReserve"}, results, turnList, "Time", new String[]{"The Government's Cash Reserve (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Cash Reserve", new JScrollPane(graphContainer));
		}
		
		//plot GOVERNMENT'S TOTAL EQUITY - getGovernmentEquity() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentEquity"}, results, turnList, "Time", new String[]{"The Government's Equity (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Equity", new JScrollPane(graphContainer));
		}
		
		//plot GOVERNMENT'S PUBLIC SECTOR WORKFORCE - getPublicSectorWorkforcePerHousehold() etc. method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"PublicSectorWorkforcePerTotalEmployedPercentage", "PublicSectorWorkforcePerTotalAvailableWorkforcePercentage"}, results, turnList, "Time", new String[]{"The labour employed by the Government per total labour employed, and per total available workforce including unemployed (%)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Public Sector Employees (%)", new JScrollPane(graphContainer));
		}	

		//plot GOVERNMENT'S TAX REVENUE - getTaxRevenue() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentTaxRevenue"}, results, turnList, "Time", new String[]{"The Government's Tax Revenue (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Tax Revenue", new JScrollPane(graphContainer));
		}

		//plot GOVERNMENT'S TOTAL ASSETS - getTotalGovernmentAssets() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentTotalAssets"}, results, turnList, "Time", new String[]{"The Government's Total Assets (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Total Assets", new JScrollPane(graphContainer));
		}
		
		//plot GOVERNMENT'S TOTAL LIABILITIES - getTotalGovernmentLiabilities() method in AbstractModel.java 
		graphContainer = plotNVariables(new String[]{"GovernmentTotalLiabilities"}, results, turnList, "Time", new String[]{"The Government's Total Liabilities (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			governmentPane.addBar("Total Liabilities", new JScrollPane(graphContainer));
		}	
				
		//plot GOVERNMENT'S TOTAL WELFARE COSTS - getGovernmentWelfareCosts() method in Government.java 
//      graphContainer = plotNVariables(new String[]{"governmentWelfareCosts"}, results, turnList, "Time", new String[]{"The Government's Welfare Costs (monetary units)"}, true, false, false, colors);
      plotObjects = multiPlotObjects(results, "governmentWelfareCosts", "Time", "The Government's Welfare Costs (monetary units)", false, colors, advancedCharts);
      if (plotObjects != null){
         governmentPane.addBar("Welfare Costs", new JScrollPane(plotObjects));
      }
				
		/*
		//plot HOUSEHOLD CONSUMPTION - see Simulation::getHouseholdConsumptionOverTime()
		plotObjects = multiPlotObjects(results, "householdConsumptionOverTime", "Time", "Households' Consumption (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Consumption", new JScrollPane(plotObjects));
		}
		*/
		//plot HOUSEHOLD CONSUMPTION BUDGET - see AbstractModel::getHouseholdConsumption(), which outputs MacroHouseholds' consumptionBudget = consumptionPropensity*Budget.  Ross
		plotObjects = multiPlotObjects(results, "householdConsumptionBudget", "Time", "Households' Consumption Budget (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Consumption Budget", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdConsumptionBudget", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Consumption Budget Stats", new JScrollPane(plotObjects));
		}
/*		graphContainer = plotNVariables(new String[]{"min(householdConsumptionBudget)", "median(householdConsumptionBudget)", "max(householdConsumptionBudget)", "avg(householdConsumptionBudget)"}, results, turnList, "Time", new String[]{"Households' Consumption Budget (monetary units)"}, true, true, false, colors, advancedCharts);
		if (graphContainer != null){
			householdsPane.addBar("Consumption Budget Summary", new JScrollPane(graphContainer));
		}
*/
		//plot HOUSEHOLD DOMESTIC BUDGET - see MacroHousehold::getDomesticBudgetAtThisTime()
		plotObjects = multiPlotObjects(results, "householdDomesticBudget", "Time", "Households' Domestic Budget (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Domestic Budget", new JScrollPane(plotObjects));
		}
		
		//plot HOUSEHOLD CASH SPEND ON GOODS - see MacroHousehold::getTotalCashSpentOnGoodsAtThisTime()
		plotObjects = multiPlotObjects(results, "householdTotalCashSpentOnGoods", "Time", "Households' Consumer Spending (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Consumer Spending", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdTotalCashSpentOnGoods", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Consumer Spending Stats", new JScrollPane(plotObjects));
		}		
				
		//plot HOUSEHOLD INVESTMENTS - see getFundContributionAtThisTime() in MacroHousehold.java
		plotObjects = multiPlotObjects(results, "householdFundContribution", "Time", "Households' Net Investments In Funds (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("MutualFund Contributions", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdFundContribution", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("MutualFund Contributions Stats", new JScrollPane(plotObjects));
		}		

		//plot HOUSEHOLD SAVINGS - see Simulation::getHouseholdSavingsOverTime()
		plotObjects = multiPlotObjects(results, "householdSavings", "Time", "Households' Savings (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Savings", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdSavings", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Savings Stats", new JScrollPane(plotObjects));
		}		
		
		//plot HOUSEHOLD TOTAL ASSETS - method in Simulation.java
		plotObjects = multiPlotObjects(results, "householdTotalAssets", "Time", "Households' Total Assets on their Balance Sheets",false, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Total Assets", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdTotalAssets", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Total Assets Stats", new JScrollPane(plotObjects));
		}		

		//plot HOUSEHOLD TOTAL INCOME - see getTotalIncome() in MacroHousehold.java.  This is calculated as the change in value of deposits plus cash spent on consumption.  This should capture all income from wages, dividends received from banks, bank deposit interest, potentially capital gains in holdings of mutual funds if/when they are introduced into the model - Ross
		plotObjects = multiPlotObjects(results, "householdTotalIncome", "Time", "Households' Total Income (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Total Income", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdTotalIncome", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Total Income Stats", new JScrollPane(plotObjects));
		}		
		
		//plot HOUSEHOLD WAGES - see getTotalRemunerationAtThisTime() in MacroHousehold.java.  Also see global indicators chart for 'meanLabourBidPrice' which is an average wage weighted by amount of labour in all contracts.
		plotObjects = multiPlotObjects(results, "householdTotalRemuneration", "Time", "Households' Wages (monetary units)",true, colors, advancedCharts);
		if (plotObjects != null){
			householdsPane.addBar("Wages", new JScrollPane(plotObjects));
		}
		plotObjects = plotSummaryStats(results, turnList, "householdTotalRemuneration", false, false, colors, true);
		if (plotObjects != null){
			householdsStatisticsPane.addBar("Wages Stats", new JScrollPane(plotObjects));
		}		
		
	    //plot HOUSEHOLD WELFARE ALLOWANCE - see getHouseholdWelfareAllowancee() in MasterModel.java.
      plotObjects = multiPlotObjects(results, "householdWelfareAllowance", "Time", "Households' Welfare Allowance (monetary units)",true, colors, advancedCharts);
      if (plotObjects != null){
         householdsPane.addBar("Welfare", new JScrollPane(plotObjects));
      }
      plotObjects = plotSummaryStats(results, turnList, "householdWelfareAllowance", false, false, colors, true);
      if (plotObjects != null){
         householdsStatisticsPane.addBar("Welfare Stats", new JScrollPane(plotObjects));
      }     
//	}

/*		//plot Stock Index - see MarketClearingModel::getStockIndexArray()
		plotObjects = multiPlotObjects(results, "stockIndexArray", "Time", "Stock Index [min, avg, max] (market cap in monetary units)", false, colors, advancedCharts);
		if (plotObjects != null){
			miscPane.addBar("Stock Index [min, avg, max]", new JScrollPane(plotObjects));
		}
*/		
		
/*		//Not working 
		//plot change in log of share price ('log returns').  Note that share price is also known as 'firmStockPrice' - Ross
		plotObjects = multiLogReturnPlotObjects(seriesNames, results, "firmStockPrice", "Time", "Log(SharePrice_(t+1)/SharePrice_t)");
		if (plotObjects != null){
			firmsPane.addBar("Log Stock Return (excluding dividend)", new JScrollPane(plotObjects));
		}
*/
				
Box.createVerticalBox();

		//Uses getEquity() method in Agent.java.  Central Bank Equity far larger (approx 10^300 larger!) so best to plot separately in the next plot below.
//		if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) 
		{
			graphContainer = plotNVariables(new String[]{"sum(bankEquity)", "sum(firmEquity)", /*"sum(fundEquity)", */"sum(householdEquity)"}, results, turnList, "Time", new String[]{"Aggregate Bank, Firm and Household Sector Equities (monetary units)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane.addBar("Agent Class Equity w/o Central Bank", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
			}
		}
//		else if (dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModelWithCentralBank.class.getName())) {
//			graphContainer = plotNVariables(new String[]{"sum(bankEquity)", "sum(firmEquity)"}, results, turnList, "Time", new String[]{"Aggregate Bank and Firm Sector Equities (monetary units)"}, true, true, false, colors, true);
//			if (graphContainer != null){
//				globalTimePane.addBar("Agent Class Equity w/o Central Bank", new JScrollPane(graphContainer));
////				miscBox.add(graphContainer);
//			}
//		}
/*		else //if (dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModel.class.getName()))
		{
			graphContainer = plotNVariables(new String[]{"sum(bankEquity)", "sum(firmEquity)"}, results, turnList, "Time", new String[]{"Aggregate Bank and Firm Sector Equities (monetary units)"}, true, false, false, colors, true);
			if (graphContainer != null){
				globalTimePane.addBar("Agent Class Equity", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
			}
		}
*/	
//		else {System.out.println("Model class case not in Page_Results.java");}

		//plots Central Bank equity using getCBequity in MarketClearingModelWithCentralBank.java, however we get problems plotting very large numbers, so ensure that Central Bank is initialized with cash of at most 10^303 if you want to see graph, whereas it was initialized with Double.MAX_VALUE cash originally (which was too large to plot!). Ross
//		if (dashboard.getModelHandler().getModelClass().getName().equals(MarketClearingModelWithCentralBank.class.getName())) {
//			graphContainer = plotNVariables(new String[]{"sum(bankEquity)", "sum(firmEquity)", "CBequity"}, results, turnList, "Time", new String[]{"Central Bank Equity dominates Aggregate Bank and Firm Equities (monetary units)"}, true, true, false, colors, true);
//			if (graphContainer != null){
//				globalTimePane.addBar("Agent Class Equity incl. Central Bank", new JScrollPane(graphContainer));
////				miscBox.add(graphContainer);
//			}
//		}
//		else
//		if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) {
			graphContainer = plotNVariables(new String[]{"sum(bankEquity)", "sum(firmEquity)", /*"sum(fundEquity)", "GovernmentEquity", */"sum(householdEquity)", "CBequity"}, results, turnList, "Time", new String[]{"Central Bank Equity dominates Aggregate Bank, Firm, and Household Sector Equities (monetary units)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane.addBar("Agent Class Equity incl. Central Bank", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
			}
//		}
//		else {System.out.println("Model class case not in Page_Results.java");}

		//Method getTaxMoneySpentForBanks() in MarketClearingModelWithCentralBank.java
		graphContainer = plotNVariables(new String[]{"TaxMoneySpentForBanks"}, results, turnList, "Time", new String[]{"Cumulative Value of Taxpayers Money Used to Bail Out Banks (monetary units)"}, true, false, false, colors, true);
		if (graphContainer != null){
		   globalTimePane.addBar("Bailout Value", new JScrollPane(graphContainer));
		   //	          miscBox2.add(graphContainer);
		}

		//Method getCrisisInterventions() in MarketClearingModelWithCentralBank.java
		graphContainer = plotNVariables(new String[]{"BankruptcyEvents"}, results, turnList, "Time", new String[]{"Number of banks becoming bankrupt at each time-step"}, true, false, false, colors, true);
		if (graphContainer != null){
		   globalTimePane.addBar("Bankruptcy Events", new JScrollPane(graphContainer));
		   //	          miscBox2.add(graphContainer);

		}
			
		if(results.resultNames().contains("aggregateBankAssets") && results.resultNames().contains("aggregateBankLeverage")){
			//Calculate 'Growth' over one time-step of Bank Assets and Bank Leverage - to mimic figures 2.2 to 2.5 in Adrian and Shin 2008 paper "Liquidity and Leverage"
			
			ArrayList<Object> aggBankAssets = (ArrayList<Object>)results.getResults("aggregateBankAssets");
			ArrayList<Object> aggBankLeverage = (ArrayList<Object>)results.getResults("aggregateBankLeverage");
			final ArrayList<Double> logAggBankAssetsGrowth = new ArrayList<Double>();
			for(int t = 1; t < aggBankAssets.size(); t++){
				logAggBankAssetsGrowth.add(Math.log((Double)aggBankAssets.get(t)/(Double)aggBankAssets.get(t-1)));
			}
			final ArrayList<Double> logAggBankLeverageGrowth = new ArrayList<Double>();
			for(int t = 1; t < aggBankLeverage.size(); t++){
				logAggBankLeverageGrowth.add(Math.log(((Number)aggBankLeverage.get(t)).doubleValue()/((Number)aggBankLeverage.get(t-1)).doubleValue()));
			}
			
			graphContainer=this.scatterTwoSeries("logAggBankLeverageGrowth", "logAggBankAssetsGrowth", Doubles.toArray(logAggBankLeverageGrowth), Doubles.toArray(logAggBankAssetsGrowth), "Aggregate Banks' Leverage Growth (log(L_t/L_t-1))", "Aggregate Banks' Assets Growth (log(A_t/A_t-1))", true, true, colors.get(0), true);
			if (graphContainer != null){
				globalScatterPane.addBar("Banks' Leverage Growth vs. Assets Growth", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
				
			}	
		}
				
		//plot Aggregate Commercial Loans (bank loans to non-banks) using getAggregateCommercialLoans() method in MarketClearingModel.java
		graphContainer = plotNVariables(new String[]{"AggregateCommercialLoans"}, results, turnList, "Time", new String[]{"Credit (aggregate bank loans to non-banks in monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			globalTimePane.addBar("Credit (commercial loan volume)", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}
		
		//plot Aggregate Interbank Loans (bank loans to banks) using getAggregateInterbankLoans() method in MarketClearingModel.java
		graphContainer = plotNVariables(new String[]{"AggregateInterbankLoans"}, results, turnList, "Time", new String[]{"Credit (aggregate bank loans to other banks on the interbank market in monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			globalTimePane.addBar("Credit (interbank loan volume)", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}
		
		//plot Interest Rate using getInterestRates() method in Simulation.java vs Credit (aggregate bank loans to non-banks) using getAggregateCommercialLoans() method in MarketClearingModel.java
		graphContainer=scatterTwoSeries("meanLoanInterestRate","AggregateCommercialLoans", results, "Commercial Loan Interest Rate", "Credit (total bank loans to non-banks in monetary units)", true, true, colors.get(0), true);
		if (graphContainer != null){
			globalScatterPane.addBar("Credit vs. Loan Rate", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}
		
/*		//CONDITIONAL STATEMENT ALLOWS US TO PLOT PRODUCTION AS GDP PROXY IN FIXED LABOUR MARKET MODELS (MarketClearingModel and MarketClearingModelWithCentralBank) BUT NOT TO REPEAT FOR INPUT/OUTPUT MACRO ECONOMY WHERE GOODS PRICES AND GDP EXISTS (AbstractModel)
		if (!dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) {		
			//plot Credit-to-Production ratio (where credit is aggregate bank loans to non-banks using getAggregateCommercialLoans() method in MarketClearingModel.java) and production uses getTotalProduction() method in MarketClearingModel.java as a proxy for GDP in Fixed Labour Market model (previously known as the Cobb-Douglas model) as unit price is set to 1, so monetary units and units produced is identical)
			graphContainer = plotNVariables(new String[]{"CommercialLoansToProductionRatio"}, results, turnList, "Time", new String[]{"Credit-to-Production ratio (Loans to non-banks / Production)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane.addBar("Credit-to-Production ratio", new JScrollPane(graphContainer));
	//			miscBox.add(graphContainer);
			}
		}
*/
//		else if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) {
			//plot Credit-to-GDP ratio (where credit is aggregate bank loans) using getAggregateCommercialLoans() method in MarketClearingModel.java and getGDP() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"CommercialLoansToGDPratio"}, results, turnList, "Time", new String[]{"Credit-to-GDP ratio (credit is bank loans to non-banks)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane.addBar("Credit-to-GDP ratio", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
			}
//		}
			
		//Method getAggEmployment() in AbstractModel.java
		graphContainer = plotNVariables(new String[]{"aggEmployment"}, results, turnList, "Time", new String[]{"Aggregate Employment (amount of labour)"}, true, false, false, colors, true);
		if (graphContainer != null){
			globalTimePane2.addBar("Employment", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
		}
		
	    //Method getAggregateFundAssets() and getAggregateHouseholdFundContribution() in AbstractModel.java
      graphContainer = plotNVariables(new String[]{"aggregateFundAssets", "aggregateHouseholdNetContributionsToFunds"}, results, turnList, "Time", new String[]{"Aggregate Funds' Assets and Aggregate Households' Net Contributions (monetary units)"}, true, false, false, colors, true);
      if (graphContainer != null){
         globalTimePane2.addBar("Fund Assets & Household Contributions", new JScrollPane(graphContainer));
      }     

//		if (dashboard.getModelHandler().getModelClass().getName().equals(AbstractModel.class.getName())) {
			//Method getGDP() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"GDP"}, results, turnList, "Time", new String[]{"Gross Domestic Product (monetary units)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane2.addBar("GDP", new JScrollPane(graphContainer));					
//					miscBox.add(graphContainer);
			}		

			//plot Credit (aggregate commercial loans using getAggregateCommercialLoans() method in MarketClearingModel.java) vs GDP	
			graphContainer=scatterTwoSeries("AggregateCommercialLoans","GDP", results, "Credit (bank loans to non-banks in monetary units)", "GDP (monetary units)", true, true, colors.get(0), true);
			if (graphContainer != null){
				globalScatterPane.addBar("GDP vs. Credit", new JScrollPane(graphContainer));
//					miscBox.add(graphContainer);
			}
	
			//plot Employment (getUnemploymentPercentage() in AbstractModel.java) vs GDP (getGDP() in AbstractModel.java)
			graphContainer=scatterTwoSeries("UnemploymentPercentage", "GDP", results, "Unemployment Rate (% of workforce)", "GDP (monetary units)", true, true, colors.get(0), true);
			if (graphContainer != null){
				globalScatterPane.addBar("GDP vs Unemployment Rate", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
			}
//		}
		
		//plot Average Loan Interest Rate using getMeanLoanInterestRates() method in Simulation.java and Stock Dividend-Price Ratio using getStockReturn() method in MarketClearingModel.java, note getStockReturn() as defined in this model is actually only the dividend-price ratio and does not include gains/losses due to share price (a.k.a. capital gains).
		graphContainer = plotNVariables(new String[]{"meanLoanInterestRate"}, results, turnList, "Time", new String[]{"Commercial Loan Interest Rate"}, true, false, false, colors);
		if (graphContainer != null){
		   globalTimePane2.addBar("Loan Rate", new JScrollPane(graphContainer));
		}

	      //plot Average Loan Interest Rate using getMeanLoanInterestRates() method in Simulation.java and Stock Dividend-Price Ratio using getStockReturn() method in MarketClearingModel.java, note getStockReturn() as defined in this model is actually only the dividend-price ratio and does not include gains/losses due to share price (a.k.a. capital gains).
		graphContainer = plotNVariables(new String[]{"meanLoanInterestRate", "dividendPriceRatio"}, results, turnList, "Time", new String[]{"Commercial Loan Interest Rate & Average Stock Dividend-Price Ratio"}, true, false, false, colors);
		if (graphContainer != null){
			globalTimePane2.addBar("Loan Rate & Stock Dividend-Price Ratio", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}

		//plot Average Loan Interest Rate using getInterestRates() method in Simulation.java and Average Realized Stockholder Return using getTotalStockReturn() method in ClearingFirm.java, note this includes both dividend payout and gains/losses due to stock price change
		graphContainer = plotNVariables(new String[]{"meanLoanInterestRate", "dividendPriceRatio", "avg(firmTotalStockReturn)"}, results, turnList, "Time", new String[]{"Commercial Loan Interest Rate, Average Dividend-Price Ratio & Average Total Stock Return"}, true, true, false, colors, true);
		if (graphContainer != null){
			globalTimePane2.addBar("Loan Rate, Stock Dividend-Price Ratio & Total Stock Return", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}

		//plot getBankCashOverTime() method in Simulation.java and Aggregate Commercial Loans (bank loans to non-banks) using getAggregateCommercialLoans() method in MarketClearingModel.java
		graphContainer = plotNVariables(new String[]{"sum(bankCashReserves)", "AggregateCommercialLoans"}, results, turnList, "Time", new String[]{"Money measures: Aggregate Bank Cash Reserves and Commercial Loans (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			globalTimePane2.addBar("Money Measures: Cash Reserves and Credit", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}
		
		//plot Total Production using getTotalProduction() method in MarketClearingModel.java as a proxy for GDP in Fixed Labour Market model (previously known as the Cobb-Douglas model) as unit price is set to 1, so monetary units and units produced is identical
		graphContainer = plotNVariables(new String[]{"TotalProduction"}, results, turnList, "Time", new String[]{"Total Production (units produced)"}, true, true, false, colors, true);
		if (graphContainer != null){
			globalTimePane3.addBar("Production", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
		}
		
		//plot Production (using getTotalProduction() method in MarketClearingModel.java as a proxy for GDP in Simple Market Clearing Model as unit price is set to 1, so monetary units and units produced is identical) vs. Credit (aggregate commercial loans using getAggregateCommercialLoans() method in MarketClearingModel.java)	
		graphContainer=scatterTwoSeries("AggregateCommercialLoans","TotalProduction", results, "Credit (loans to non-banks in monetary units)", "Production (proxy for GDP in units produced)", true, true, colors.get(0), true);
		if (graphContainer != null){
			globalScatterPane.addBar("Production vs. Credit", new JScrollPane(graphContainer));
//				miscBox.add(graphContainer);
		}	
		
		//plot Stock Market Capitalization
		graphContainer = plotNVariables(new String[]{"StockMarketCapitalization"}, results, turnList, "Time", new String[]{"Stock Market Capitalization (monetary units)"}, true, true, false, colors, true);
		if (graphContainer != null){
			globalTimePane3.addBar("Stock Market Capitalization", new JScrollPane(graphContainer));
		}

		//plot Average Loan Interest Rate using getInterestRates() method in Simulation.java and Average Stock Price using getMarketValue() method in ClearingFirm.java
		graphContainer = plotNVariables(new String[]{"meanLoanInterestRate", "avg(firmStockPrice)"}, results, turnList, "Time", new String[]{"Commercial Loan Interest Rate & Average Stock Price"}, true, true, false, colors, true);
		if (graphContainer != null){
			globalTimePane3.addBar("Stock Price & Loan Rate", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}
				
		//plot Average Loan Interest Rate using getInterestRates() method in Simulation.java vs Average Stock Price Ratio using getMarketValue() method in ClearingFirm.java
		graphContainer=scatterTwoSeries("meanLoanInterestRate","avg(firmStockPrice)", results, "Commercial Loan Interest Rate", "Average Stock Price", true, true, colors.get(0), true);
		if (graphContainer != null){
			globalScatterPane.addBar("Stock Price vs. Loan Rate", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}	

		//plot Average Loan Interest Rate using getInterestRates() method in Simulation.java vs Average Realized Stockholder Return using getTotalStockReturn() method in ClearingFirm.java, note this includes both dividend payout and gains/losses due to stock price change
		graphContainer=scatterTwoSeries("meanLoanInterestRate","avg(firmTotalStockReturn)", results, "Commercial Loan Interest Rate", "Average Realized Stockholder Return", true, true, colors.get(0), true);
		if (graphContainer != null){
			globalScatterPane.addBar("Total Stock Return vs. Loan Rate", new JScrollPane(graphContainer));
//			miscBox.add(graphContainer);
		}	
			
			//Methods in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"CapitalGainsTaxRate", "DividendTaxRate", "IncomeTaxRate", "ValueAddedTaxRate"}, results, turnList, "Time", new String[]{"Capital Gains, Dividend, Income and Value Added Tax Rates"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane3.addBar("Tax Rates", new JScrollPane(graphContainer));					
			}

			//Method getUnemploymentPercentage() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"UnemploymentPercentage"}, results, turnList, "Time", new String[]{"Unemployment Rate (% of workforce)"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane3.addBar("Unemployment Rate", new JScrollPane(graphContainer));
	//				miscBox.add(graphContainer);
			}
	
			//Method getAverageUnsoldGoods() etc. in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"AverageUnsoldGoods", "MinUnsoldGoods", "MaxUnsoldGoods"}, results, turnList, "Time", new String[]{"Aggregate quantity of goods produced by firms in one time-step that are not sold during the same time-step"}, true, true, false, colors, true);
			if (graphContainer != null){
				globalTimePane3.addBar("Unsold Goods", new JScrollPane(graphContainer));
			}
			
			//Method getMeanLabourBidPrice() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"meanLabourBidPrice"}, results, turnList, "Time", new String[]{"Household (Labour Unit Volume-Weighted) Average Wage (monetary units)"}, true, false, false, colors, true);
			if (graphContainer != null){
				globalTimePane3.addBar("Wages (average over households)", new JScrollPane(graphContainer));
	//				miscBox.add(graphContainer);
			}
			
/*			//Method getPercentageOfPopulationReceivingWelfareBenefits() in MasterModel.java
			graphContainer = plotNVariables(new String[]{"PercentageOfPopulationOnWelfare"}, results, turnList, "Time", new String[]{"Proportion of Households registered to receive welfare support from Government (%)"}, true, false, false, colors, true);
			if (graphContainer != null){
				globalTimePane3.addBar("Welfare Recipients (%)", new JScrollPane(graphContainer));
			}
*/		
		
		//Method getObjectiveFunctionValue() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"objectiveFunctionValue"}, results, turnList, "Time", new String[]{"Objective Function Value (sum of square errors to UK data 2014)"}, true, true, false, colors, true);
			if (graphContainer != null){
				optimizePane.addBar("UK Economic Levels", new JScrollPane(graphContainer));					
			}
			
			//Method GDPvolatilityObjectiveFunction() in AbstractModel.java
			graphContainer = plotNVariables(new String[]{"GDPvolatilityObjectiveFunction"}, results, turnList, "Time", new String[]{"Relative change in GDP - 0.03"}, true, true, false, colors, true);
			if (graphContainer != null){
				optimizePane.addBar("Relative Change In GDP", new JScrollPane(graphContainer));					
			}
						


		
		
/*		//Problem - shows the same scale for vastly different numbers - Ariel to sort out plotting with 2 y-axes.
		//Method getGDP() and getUnemploymentRate in AbstractModel.java
		graphContainer = plotNVariables(new String[]{"UnemploymentRate","GDP"}, results, "Time", new String[]{"Unemployment Rate (% households) and GDP (monetary units)"}, true, false, false, colors);
		if (graphContainer != null){
			miscBox.add(graphContainer);
		}
*/		
		
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				for (DynamicPlot plot : currentOnLinePlots) {
//					plot.getGraphContainer().getView().autoScale();
//				}
//				
//				for (DynamicPlot plot : currentOffLinePlots.keySet()) {
//					plot.getGraphContainer().getView().autoScale();
//				}
//			}
//		});
		
//		Style.apply(indexBox, dashboard.getCssStyle());
//		indexBox.validate();
//		indexBox.repaint();
	}
	

//	/**
//	 * 
//	 * Plots a number of datasets on a single plot.
//	 * <p>
//	 * Can plot multiple datasets over the same time axis, using a line plot with circular markers, and optionally drawing y-axes for
//	 * each dataset.
//	 *   
//	 * NOTE - This is an Adapter Pattern / Wrapper to allow String[] for yLabel input. (Ross)
//	 * 
//	 * @param tempBankArray
//	 * @param results is the map from datasets' names to the datasets themselves
//	 * @param xLabel is the label for the common x axis
//	 * @param yLabels is the list of labels for each dataset
//	 * @param paintYAxis a flag to determine if the y axis should be displayed for all datasets
//	 * @param rescale a flag to rescale the datasets so that they fit on the same plot.
//	 * @return A graph container with the required chart in it.
//	 * @author Ariel Hoffman
//	 *
//	 */
//	private GJGraphContainer plotNVariables(String[] tempBankArray,
//			Run run, String xLabel, String yLabels, boolean paintYAxis, boolean rescale, boolean plotAvgFlag) {
//		String[] yLabelArray = {yLabels};
//		return plotNVariables(tempBankArray, run, xLabel, yLabelArray, paintYAxis, rescale, plotAvgFlag);
//	}
//
//	private GJGraphContainer plotNVariables(String[] tempBankArray,
//			Run run, String xLabel, String yLabels, boolean paintYAxis, boolean rescale, String plotAvgFlag) {
//		String[] yLabelArray = {yLabels};
//		return plotNVariables(tempBankArray, run, xLabel, yLabelArray, paintYAxis, rescale, plotAvgFlag);
//	}
	
	@Override
	public String getTitle() {
		return TITLE;
	}

//	/**
//	 * 
//	 * Plots a number of datasets on a single plot.
//	 * <p>
//	 * Can plot multiple datasets over the same time axis, using a line plot with circular markers, and optionally drawing y-axes for
//	 * each dataset.
//	 *   
//	 * 
//	 * 
//	 * @param tempBankArray
//	 * @param results is the map from datasets' names to the datasets themselves
//	 * @param xLabel is the label for the common x axis
//	 * @param yLabels is the list of labels for each dataset
//	 * @param paintYAxis a flag to determine if the y axis should be displayed for all datasets
//	 * @param rescale a flag to rescale the datasets so that they fit on the same plot.
//	 * @return A graph container with the required chart in it.
//	 * @author Ariel Hoffman
//	 *
//	 */
//	private GJGraphContainer plotNVariables(String[] names, Run run, final String xLabel, final String[] yLabel, final boolean paintYAxis, final boolean rescale) {
//		return plotNVariables(names,run,xLabel,yLabel, paintYAxis, rescale, false ) ;
//	}
//	private GJGraphContainer plotNVariables(String[] names, Run run, final String xLabel, final String[] yLabel, final boolean paintYAxis, final boolean rescale, String plotAvgFlag ) {
//		if (plotAvgFlag.equalsIgnoreCase("plotAverage")){	
//		return plotNVariables(names,run,xLabel,yLabel, paintYAxis, rescale, true) ;
//		}
//		else {
//			return plotNVariables(names,run,xLabel,yLabel, paintYAxis, rescale, false);
//		}
//	}

	/**
	 * 
	 * Plots a number of statistics from a population of time-series. 
	 * Given the name of the underlying recorder source, this method automatically plots the:
	 * average
	 * average + standard deviation
	 * average - standard deviation
	 * minimum
	 * maximum
	 * median
	 * provided that the recorder sources for these time-series are defined.  
	 * 
	 * For example, if the recorder source list contains avg(householdSavings), sd(householdSavings), min(householdSavings), max(householdSavings), median(householdSavings),
	 * the method will plot all these time-series just by using the householdSavings name as an argument (the objectName).
	 * 
	 * @param objectName is the common name appearing within parentheses of statistics being recorded i.e. householdSavings.
	 * 
	 * @return
	 * @author Ross Richardson
	 */
	private JComponent plotSummaryStats(final Run run, final double[] turnList, final String objectName, final boolean rescale, final boolean plotAvgFlag, 
			List<Color> colors, final boolean plotChart) {
		if(plotChart){
			//plot summary statistics of objectName (if they are recorded)
			final String xLabel = TURN_SERIES_NAME;
			
			//count the objects
			int nObjects=0;
			
//Name of underlying recorder source, e.g. householdSavings
			Set<String> objectNames = new TreeSet<String>();
			
//Create list of statistics to plot, e.g. avg(householdSavings) is the population average of household savings, sd(householdSavings) is the 
//standard deviation (uses the Colt statistics library).  This requires these time-series to be included on the recorder source list (see Model class)
			ArrayList<String> statsList = new ArrayList<String>();
			statsList.add("min(" + objectName + ")");
			statsList.add("median(" + objectName + ")");
			statsList.add("max(" + objectName + ")");
			statsList.add("avg(" + objectName + ")");
			statsList.add("sd(" + objectName + ")");

			for(String s: statsList) {
				int count = nObjects; 
				for (String series_i : run.resultNames()) {
					if (series_i.equals(s)) {
						if(objectNames.add(series_i))
						{
							nObjects++;
						}
					}
				}
				if(count != nObjects-1)
				{
					System.out.println("Warning - " + s + " is not being recorded.  Add to recorder source list in model class if desired.");
				}
			}	
/*			if(nObjects < statsList.size())
			{
				System.out.println("Warning - some statistics of " + objectName + " are not being recorded.  Add to recorder source list in model class if desired.");
			}				
			else if(nObjects > statsList.size())
			{
				System.out.println("Warning - something weird - more statistics found than there are columns of " + objectName);
			}
*/	
			if (nObjects == 0){
				return null;
			}
			
			// put the chart in a vbox, to prevent resizing it to fit the available space
			Box verticalBox = Box.createVerticalBox();
			
			if(nObjects > 0){
				
				Set<String> plotNames = new TreeSet<String>();
				plotNames.addAll(objectNames);
				for(String p: plotNames)
				{
					if(p.startsWith("sd("))
					{
						plotNames.remove(p);	//Remove sd from plotNames as we do not want to plot standard deviation alongside the other statistics (as it might be orders of magnitude different).  What we want to do is add/subtract the standard deviation from the mean.
					}
				}
				plotNames.add("meanPlusStandardDeviation");
				plotNames.add("meanMinusStandardDeviation");			
								
				String[] objectNameArray=objectNames.toArray(new String[0]);
				final String[] tempObjectArray = Arrays.copyOf(objectNameArray, nObjects);


				Map<String, List<Object>> summarystats = new HashMap<String, List<Object>>();
				String[] columnNames = tempObjectArray;
				List<Object> xData = new ArrayList<Object>();
				for (int j = 0; j < run.getResults(columnNames[0]).size() ; j++){
					xData.add(j);
				}
				
				summarystats.put(TURN_SERIES_NAME, xData);
				
				for (int m = 0 ; m < columnNames.length ; m++){
					String name = columnNames[m];
										
					if(columnNames[m].startsWith("sd("))
					{
						List<Object> posSpread = new ArrayList<Object>();
						List<Object> negSpread = new ArrayList<Object>();
						for (int n = 0 ; n < columnNames.length ; n++){
							if(columnNames[n].startsWith("avg("))
							{
								for (Object timestep : xData){
									double mean = ((Number)run.getResults(columnNames[n]).get(((Number)timestep).intValue())).doubleValue();
									Double spread = ((Number)run.getResults(columnNames[m]).get(((Number)timestep).intValue())).doubleValue();
									if(spread.isNaN())
									{
										double positiveSpread = mean;												
										double negativeSpread = mean;
										posSpread.add(positiveSpread);
										negSpread.add(negativeSpread);
									}
									else
									{
										double positiveSpread = mean + spread;												
										double negativeSpread = mean - spread;
										posSpread.add(positiveSpread);
										negSpread.add(negativeSpread);
									}
								}
								summarystats.put("meanPlusStandardDeviation", posSpread);
								summarystats.put("meanMinusStandardDeviation", negSpread);	
							}
						}
					}
					else
					{
						summarystats.put(name, run.getResults(name));
					}
				}
					
				final Map<String, List<Object>> summarystatsData = summarystats;
				final Color[] colorArray = colors.toArray(new Color[0]);
				final LinePlot linePlot = new LinePlot(TURN_SERIES_NAME, objectName, summarystatsData, plotAvgFlag, rescale, colorArray);
				currentOnLinePlots.add(linePlot);
				linePlot.setXLabel(null);
				linePlot.setYLabel(null);
				linePlot.disableMouseInteraction();
				GJGraphContainer objects = linePlot.getGraphContainer();
				GJGraphInterface graph = objects.getView();
				((GJGraph)graph).addMouseListener(new MouseAdapter() {
		
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2){
							String yLabels0 = objectName;//((summarystatsData.keySet().toArray())[0]).toString();
							if (!tabbedPlots.containsKey((yLabels0))){
								LinePlot newPlot = new LinePlot(TURN_SERIES_NAME, objectName, summarystatsData, plotAvgFlag, rescale, colorArray);
								currentOnLinePlots.add(newPlot);
								tabbedPlots.put(yLabels0, newPlot);
								newPlot.setYLabel(null);
								GJGraphContainer graphContainer = newPlot.getGraphContainer();
								graphContainer.getView();
								graphContainer.setName(yLabels0);
								graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
								graphContainer.setTitle(new JFormattedTextField(yLabels0+" / "+xLabel));
								addClosableTab(tabPane, graphContainer, yLabels0, null);
								tabPane.setSelectedComponent(graphContainer);
								tabPane.validate();
							} else {
								tabPane.setSelectedComponent(tabbedPlots.get(yLabels0).getGraphContainer());
							}
						}
					}
				});
				
				verticalBox.add(objects);

				Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
				Style.apply(verticalBox, dashboard.getCssStyle());
					
			}	//If nObjects
			return verticalBox;
		}	//If plotChart
		else return null;
	}	//End of method
///////  ////////////////////////////  /////////////////////  ///////////////  /////////////////////  //////////////					
	
	private JComponent plotNVariables(final String[] names, final Run run, final double[] turnList, final String xLabel,
			final String[] yLabels, final boolean paintYAxis, final boolean rescale, final boolean plotAvgFlag, List<Color> colors, final boolean plotChart) {
		if(plotChart){
			final int numVars = names.length;
			
			//check that variables were sent
			if (numVars == 0)
				return null;
			
			if (!run.resultNames().contains(names[0])){
				return null;
			}
			final Color[] colorArray = colors.toArray(new Color[0]);
			final LinePlot linePlot = new LinePlot(turnList, xLabel, names, run, yLabels, rescale, plotAvgFlag, colorArray);
			currentOnLinePlots.add(linePlot);
			linePlot.setXLabel(null);
			linePlot.setYLabel(null);
			linePlot.disableMouseInteraction();
			GJGraphContainer objects = linePlot.getGraphContainer();
			GJGraphInterface graph = objects.getView();
			((GJGraph)graph).addMouseListener(new MouseAdapter() {
	
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2){
						if (!tabbedPlots.containsKey(yLabels[0])){
							Pair<String[],double[][]> data = linePlot.getYData();
							LinePlot newPlot = new LinePlot(xLabel, yLabels, linePlot.getXData(), data.getValue1(), TURN_SERIES_NAME, data.getValue0(), plotAvgFlag, rescale, colorArray);
							currentOnLinePlots.add(newPlot);
							tabbedPlots.put(yLabels[0], newPlot);
							newPlot.setYLabel(null);
							GJGraphContainer graphContainer = newPlot.getGraphContainer();
							graphContainer.getView();
//							graph.autoScale();
							graphContainer.setName(yLabels[0]);
							graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
							graphContainer.setTitle(new JFormattedTextField(yLabels[0]+" / "+xLabel));
							addClosableTab(tabPane, graphContainer, yLabels[0], null);
	//						tabPane.add(yLabels[0], graphContainer);
							tabPane.setSelectedComponent(graphContainer);
							tabPane.validate();
						} else {
							tabPane.setSelectedComponent(tabbedPlots.get(yLabels[0]).getGraphContainer());
						}
					}
				}
			});
			
//			graph.autoScale();

			// put the chart in a vbox, to prevent resizing it to fit the available space
			Box verticalBox = Box.createVerticalBox();
			verticalBox.add(objects);

			Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
			Style.apply(verticalBox, dashboard.getCssStyle());
			
			return verticalBox;
		}
		else return null;
	}

	//If no plotChart boolean flag given as argument, then plot chart anyway
	private JComponent plotNVariables(final String[] names, final Run run, final double[] turnList, final String xLabel,
			final String[] yLabels, final boolean paintYAxis, final boolean rescale, final boolean plotAvgFlag, List<Color> colors) {
		return plotNVariables(names, run, turnList, xLabel,	yLabels, paintYAxis, rescale, plotAvgFlag, colors, true);
	}
	
	//If no plotChart boolean flag given as argument, then plot chart anyway
	private JComponent multiPlotAutoCorrelation(final Run run, final String objectName,
			final boolean plotAvgFlag, List<Color> colors, final boolean partialResults) {
		return multiPlotAutoCorrelation(run, objectName, plotAvgFlag, colors, partialResults, true);
	}
	
	private JComponent multiPlotAutoCorrelation(final Run run, final String objectName,
			final boolean plotAvgFlag, List<Color> colors, final boolean partialResults, final boolean plotChart) {
		if(plotChart){
			//plot time-series autocorrelation
			final String xLabel = "lag";
			final String yLabel = "auto-correlation of "+objectName;
			
			//count the objects
			int i;
			int nObjects=0;
	//		String plotAverageStr=new String("");
			Set<String> objectNames = new TreeSet<String>(new Comparator<String>() {
	
				@Override
				public int compare(String o1, String o2) {
					int index1 = o1.lastIndexOf('_');
					int index2 = o2.lastIndexOf('_');
					
					int number1 = Integer.parseInt(o1.substring(index1 + 1));
					int number2 = Integer.parseInt(o2.substring(index2 + 1));
					
					if (number1 < number2){
						return -1;
					}
					
					if (number1 > number2){
						return 1;
					}
					
					return 0;
				}
			});
			
			for (String series_i : run.resultNames()) {
				if (series_i.startsWith(objectName+"Multi_")) {
					objectNames.add(series_i);
					nObjects++;
				}
			}
	
			if (nObjects == 0){
				return null;
			}
	
	/*		//make turn list (=time) from results map
			final double[] turnList = new double[run.getResults(TURN_SERIES_NAME).size()];
			i = 0;
			for (Object turn : run.getResults(TURN_SERIES_NAME)){
				turnList[i++] = ((Number)turn).doubleValue();
			}
	*/
	//		if (nObjects > 1 && plotAvgFlag){
	//			 plotAverageStr="plotAverage";
	//		}
			Box verticalBox = Box.createVerticalBox();
			//If they exist, plot them a few at a time
			if(nObjects > 0){
				String[] objectNameArray=objectNames.toArray(new String[0]);
				for (i=0;i <= nObjects/MAX_ELEMENTS_PER_PLOT; i++){
					final Color[] tempColorArray = Arrays.copyOfRange(colors.toArray(new Color[0]), i*MAX_ELEMENTS_PER_PLOT, Math.min((i+1)*MAX_ELEMENTS_PER_PLOT-1, nObjects));				
	
					final String[] tempObjectArray = Arrays.copyOfRange(objectNameArray, i*MAX_ELEMENTS_PER_PLOT, Math.min((i+1)*MAX_ELEMENTS_PER_PLOT-1, nObjects));
	//				final LinePlot linePlot = new LinePlot(turnList, xLabel, tempObjectArray, run, new String[]{yLabel}, false, plotAvgFlag, tempColorArray);
	//				final LinePlotAutoCorrelation linePlot = new LinePlotAutoCorrelation(tempObjectArray, run, new String[]{yLabel}, false, plotAvgFlag, tempColorArray);
					 
					final Preprocessor preprocessor = new Preprocessor() {
						
						String[] columnNames = tempObjectArray;
						
						@Override
						public Map<String, List<Object>> process(Run run) {
							Map<String, List<Object>> autocorrelations = new HashMap<String, List<Object>>();
							
							List<Object> xData = new ArrayList<Object>();
							for (int i = 0; i < run.getResults(columnNames[0]).size() ; i++){
								xData.add(i);
							}
							
							autocorrelations.put(LAG_SERIES_NAME, xData);
							
							for (int m = 0 ; m < columnNames.length ; m++){
								String name = columnNames[m];
								List<Object> results = run.getResults(name);
								DoubleArrayList doubleList = new DoubleArrayList();
								
								double sum = 0;
								double sumOfSquares = 0;
								
								for (Object element : results){
									double doubleValue = ((Number)element).doubleValue();
									doubleList.add(doubleValue);
									
									sum += doubleValue;
									sumOfSquares += doubleValue * doubleValue;
								}
								
								double mean = sum / results.size();
								double variance = Descriptive.variance(results.size(), sum, sumOfSquares);
								
								List<Object> yData = new ArrayList<Object>();
								for (Object lag : xData){
									double autoCorrelation = Descriptive.autoCorrelation(doubleList, ((Integer)lag).intValue(), mean, variance);
									yData.add(autoCorrelation);
								}
								
								autocorrelations.put(name, yData);
							}
							
							return autocorrelations;
						}
					};
	
					final LinePlot linePlot = new LinePlot(xLabel, new String[]{yLabel}, new double[0], new double[tempObjectArray.length][], LAG_SERIES_NAME, tempObjectArray, plotAvgFlag, true, tempColorArray);
					if (!partialResults){
						Map<String, List<Object>> data = preprocessor.process(run);
						linePlot.addDataCollection(data);
						
						currentOffLinePlots.put(linePlot, null);
					} else {
						currentOffLinePlots.put(linePlot, preprocessor);
					}
					
					linePlot.setXLabel(null);
					linePlot.setYLabel(null);
					linePlot.disableMouseInteraction();
					GJGraphContainer graphContainer = linePlot.getGraphContainer();
					
					GJGraphInterface graph = graphContainer.getView();
					if (partialResults){
						linePlot.setWaitingForData(true);
					}
					((GJGraph)graph).addMouseListener(new MouseAdapter() {
	
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() == 2){
								if (!tabbedPlots.containsKey(yLabel)){
									Pair<String[],double[][]> data = linePlot.getYData();
									LinePlot newPlot = new LinePlot(xLabel, new String[]{yLabel}, linePlot.getXData(), data.getValue1(), LAG_SERIES_NAME, data.getValue0(), plotAvgFlag, false, tempColorArray);
									if (partialResults){
										currentOffLinePlots.put(newPlot, preprocessor);
									} else {
										currentOffLinePlots.put(newPlot, null);
									}
									tabbedPlots.put(yLabel, newPlot);
									//							newPlot.setLeftAxisLabelled(false);
									newPlot.setYLabel(null);
									GJGraphContainer newGraphContainer = newPlot.getGraphContainer();
									newGraphContainer.getView();
//									graph.autoScale();
									newGraphContainer.setName(yLabel);
									newGraphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
									newGraphContainer.setTitle(new JFormattedTextField(yLabel+" / "+xLabel));
									addClosableTab(tabPane, newGraphContainer, yLabel, null);
	//								tabPane.add(yLabel, newGraphContainer);
									tabPane.setSelectedComponent(newGraphContainer);
									tabPane.validate();
								} else {
									tabPane.setSelectedComponent(tabbedPlots.get(yLabel).getGraphContainer());
								}
							}
						}
					});
	
	//				GJGraphContainer objects=plotNVariables(tempObjectArray, run, xLabel, yLabel,false,false,plotAverageStr);
	
//					graph.autoScale();
					verticalBox.add(graphContainer);
	//				container.addTab(tabName+ " " + (i+1), objects);
				}
			}
	
			Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
			Style.apply(verticalBox, dashboard.getCssStyle());
			
			return verticalBox;
		}
		else return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void displayGAResults(final String[] header, final List<String[]> values, final int bestCombinationIdx, final ParameterTree bestCombinationTree) {
		populationTable.setModel(new PopulationTableModel(header,values));
		populationTable.setDefaultRenderer(BigDecimal.class,new PopulationTableCellRenderer(bestCombinationIdx));
		scrollToVisible(populationTable,bestCombinationIdx,0);
		
		this.bestCombinationTree = bestCombinationTree;

		final CardLayout layout = (CardLayout) getPanel().getLayout();
		layout.show(getPanel(),"POPULATIONS");
	}
	
//	/**
//	 * Plot a single time series - possibly unnecessary.
//	 * @param variable
//	 * @param results
//	 * @param xLabel
//	 * @param yLabel
//	 * @return
//	 */
//	private GJGraphContainer plotVariable(String variable, Map<String,List<Number>> results, String xLabel, String yLabel) {
//		
//		if(results.get(variable) == null){return null;}
//		
//		//make turn list (=time) from results map
//		double[] turnList = new double[results.get(TURN_SERIES_NAME).size()];
//		int i = 0;
//		for (Number turn : results.get(TURN_SERIES_NAME)){
//			turnList[i++] = turn.doubleValue();
//		}
//
//		// get series data from results map
//		double[] series = new double[results.get(variable).size()];
//		i = 0;
//		for (Number value : results.get(variable)){
//			series[i++] = value.doubleValue();
//		}
//
//		//make a scatter plot
//		GJPlotInterface scatter= GJScatter.createInstance();
//		scatter.setXData(turnList);
//		scatter.setYData(series);
//		scatter.setMarker(GJMarker.circle(MARKER_CIRCLE_WIDTH));
//
//		//make a line plot
//		GJPlotInterface line= GJLine.createInstance();
//		line.setXData(turnList);
//		line.setYData(series);
//
//		//add both plots to a graph and graph-container
//		GJGraph graph = GJGraph.createInstance();
//		GJGraphContainer graphContainer = GJGraphContainer.createInstance(graph);
//
//		graph.add(scatter);
//		graph.add(line);
//		graph.autoScale();
//
//		//set labels
//		graph.setXLabel(xLabel);
//		graph.setBottomAxisLabelled(true);
////		graph.setYLabel(yLabel);
//		graph.setMinorGridPainted(false);
//
//		graphContainer.setTitleText(yLabel);
//
//
//		return graphContainer;
//	}

/**
 * 
	 * Plots a number of (time-)series per chart. 
	 * This Method automatically plots all series from the results map with the name obectName
	 * <p>
	 * Can plot multiple datasets over the same time axis, using a line plot with circular markers, and optionally drawing y-axes for
	 * each dataset.
	 *   
	 * 
	 * 
	 * @param seriesNames is the set of all names appearing in the results map
	 * @param results is the map from datasets' names to the datasets themselves
	 * @param objectName is the common name appearing at the beginning of the datasets' names i.e. firmMarketValue is the objectName, and this method picks out all dataset 
	 * 			names beginning with firmMarketValue_Multi followed by an index number
	 * @param xLabel is the label for the common x axis
	 * @param yLabel is the label for all datasets
	 * @return
	 * @author Ross Richardson, Ariel Hoffman
	 */
 
	
//	private JComponent multiPlotObjects(final Run run, final String objectName,
//			final String xLabel, final String yLabel) {
//		return  multiPlotObjects(run,objectName, xLabel, yLabel,false);
//	}
	
	/**
	 * @param run
	 * @param objectName
	 * @param xLabel
	 * @param yLabel
	 * @param plotAvgFlag
	 * @param colors
	 * @return
	 */
	private JComponent multiPlotObjects(final Run run, final String objectName,
			final String xLabel, final String yLabel, final boolean plotAvgFlag, List<Color> colors, final boolean plotChart) {
		if(plotChart){
			//plot object's Value over time
			//count the objects
			int i;
			int nObjects=0;
	//		String plotAverageStr=new String("");
			Set<String> objectNames = new TreeSet<String>(new Comparator<String>() {
	
				@Override
				public int compare(String o1, String o2) {
					int index1 = o1.lastIndexOf('_');
					int index2 = o2.lastIndexOf('_');
					
					int number1 = Integer.parseInt(o1.substring(index1 + 1));
					int number2 = Integer.parseInt(o2.substring(index2 + 1));
					
					if (number1 < number2){
						return -1;
					}
					
					if (number1 > number2){
						return 1;
					}
					
					return 0;
				}
			});
			
			for (String series_i : run.resultNames()) {
				if (series_i.startsWith(objectName+"Multi_")) {
					objectNames.add(series_i);
					nObjects++;
				}
			}
	
			if (nObjects == 0){
				return null;
			}
	
			//make turn list (=time) from results map
			final double[] turnList = new double[run.getResults(TURN_SERIES_NAME).size()];
			i = 0;
			for (Object turn : run.getResults(TURN_SERIES_NAME)){
				turnList[i++] = ((Number)turn).doubleValue();
			}
	
	//		if (nObjects > 1 && plotAvgFlag){
	//			 plotAverageStr="plotAverage";
	//		}
			Box verticalBox = Box.createVerticalBox();
			
			//If they exist, plot them a few at a time
			if(nObjects > 0){
				String[] objectNameArray=objectNames.toArray(new String[0]);
				for (i=0;i <= nObjects/MAX_ELEMENTS_PER_PLOT; i++){
					// this is used in the tabbedPlots map to identify the charts
					final String chartId = yLabel + i;
					final String[] tempObjectArray = Arrays.copyOfRange(objectNameArray, i*MAX_ELEMENTS_PER_PLOT, Math.min((i+1)*MAX_ELEMENTS_PER_PLOT-1, nObjects));
					final Color[] tempColorArray = Arrays.copyOfRange(colors.toArray(new Color[0]), i*MAX_ELEMENTS_PER_PLOT, Math.min((i+1)*MAX_ELEMENTS_PER_PLOT-1, nObjects));
					final LinePlot linePlot = new LinePlot(TURN_SERIES_NAME,turnList, xLabel, tempObjectArray, run, new String[]{yLabel}, false, plotAvgFlag, tempColorArray);
					currentOnLinePlots.add(linePlot);
	//				linePlot.setBottomAxisLabelled(false);
	//				linePlot.setLeftAxisLabelled(false);
					linePlot.setXLabel(null);
					linePlot.setYLabel(null);
					linePlot.disableMouseInteraction();
					GJGraphContainer graphContainer = linePlot.getGraphContainer();
					GJGraphInterface graph = graphContainer.getView();
					((GJGraph)graph).addMouseListener(new MouseAdapter() {
	
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() == 2){
								if (!tabbedPlots.keySet().contains(chartId)){
									Pair<String[],double[][]> data = linePlot.getYData();
									LinePlot newPlot = new LinePlot(xLabel, new String[]{yLabel}, linePlot.getXData(), data.getValue1(), TURN_SERIES_NAME, data.getValue0(), plotAvgFlag, false, tempColorArray);
									currentOnLinePlots.add(newPlot);
									tabbedPlots.put(chartId, newPlot);
									//							newPlot.setLeftAxisLabelled(false);
									newPlot.setYLabel(null);
									GJGraphContainer graphContainer = newPlot.getGraphContainer();
									graphContainer.getView();
//									graph.autoScale();
									graphContainer.setName(chartId);
									graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
									graphContainer.setTitle(new JFormattedTextField(yLabel+" / "+xLabel));
									addClosableTab(tabPane, graphContainer, yLabel, null);
	//								tabPane.add(yLabel, graphContainer);
									tabPane.setSelectedComponent(graphContainer);
									tabPane.validate();
								} else {
									tabPane.setSelectedComponent(tabbedPlots.get(chartId).getGraphContainer());
								}
							}
						}
					});
	
	//				GJGraphContainer objects=plotNVariables(tempObjectArray, run, xLabel, yLabel,false,false,plotAverageStr);
	
//					graph.autoScale();
					
					verticalBox.add(graphContainer);
	//				container.addTab(tabName+ " " + (i+1), objects);
				}
			}
			
			Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
			Style.apply(verticalBox, dashboard.getCssStyle());
			
			return verticalBox;
		}
		else return null;
	}
	
	//If no plotChart boolean flag given as argument, then plot chart anyway
	private JComponent multiPlotObjects(final Run run, final String objectName,
			final String xLabel, final String yLabel, final boolean plotAvgFlag, List<Color> colors) {
		return multiPlotObjects(run, objectName, xLabel, yLabel, plotAvgFlag, colors, true);
	}
	
	/**
	 * 
	 * Plots a number of (time-)series per chart, where the values are calculated as the logarithm of the ratio of successive dataset values, i.e. log(p[t+1]/p[t]).  
	 * This method has been written to chart multiple time-series of log share return data, where the return is the capital return (price ratio) excluding dividend payouts
	 * <p>
	 * Can plot multiple datasets over the same time axis, using a line plot with circular markers, and optionally drawing y-axes for
	 * each dataset.
	 *   
	 * 
	 * 
	 * @param  seriesNamesis the set of all names appearing in the results mapt
	 * @param results is the map from datasets' names to the datasets themselves
	 * @param objectName is the common name appearing at the beginning of the datasets' names i.e. firmMarketValue is the objectName, and this method picks out all dataset 
	 * 			names beginning with firmMarketValue_Multi followed by an index number
	 * @param xLabel is the label for the common x axis
	 * @param yLabel is the label for each dataset
	 * @return
	 * @author Ross Richardson
	 *
	 */
/*	private JComponent multiLogReturnPlotObjects(final Set<String> seriesNames, final Map<String, List<Number>> results, final String objectName,
			final String xLabel, final String yLabel) {
		//plot object's Value over time
		//count the objects
		int i;
		int nObjects=0;
		Set<String> objectNames = new HashSet<String>(); 
		for (String series_i : seriesNames) {
			if (series_i.startsWith(objectName+"Multi_")) {
				objectNames.add(series_i);
				nObjects++;
			}
		}

		if (nObjects == 0){
			return null;
		}
		
		Box verticalBox = Box.createVerticalBox();
		//If they exist, plot them a few at a time
		if(nObjects > 0){
			String[] objectNameArray=objectNames.toArray(new String[0]);
			for (i=0;i <= nObjects/MAX_ELEMENTS_PER_PLOT; i++){
				String[] tempObjectArray = Arrays.copyOfRange(objectNameArray, i*MAX_ELEMENTS_PER_PLOT, Math.min((i+1)*MAX_ELEMENTS_PER_PLOT-1, nObjects));
				GJGraphContainer objects=plotNlogReturnVariables(tempObjectArray, results, xLabel, yLabel,false,false);
								//The plotNlogReturnVariables method call is the only difference between this method and the multiPlotObjects method
				verticalBox.add(objects);
//				container.addTab(tabName+ " " + (i+1), objects);
			}
		}

		return verticalBox;
	}
*/
	
//	private void savePlot(final String name, GJPlotInterface plot){
//		List<GJPlotInterface> plotList = currentOnLinePlots.get(name);
//		if (plotList == null){
//			plotList = new ArrayList<GJPlotInterface>();
//			currentOnLinePlots.put(name, plotList);
//		}
//		plotList.add(plot);
//
//	}
//	
//	private GJGraph createChart(String xLabel, boolean paintYAxis, boolean rescale, String[] yLabels, double[] timeList,
//			List<double[]> series, double[] avg) {
//
//		//make a line plot for variable 1
//		GJPlotInterface line1= GJLine.createInstance();
//		line1.setXData(timeList);
//		line1.setYData(series.get(0));
//
//		//add both plots to a graph and graph-container
//		GJGraph parent_graph = GJGraph.createInstance();
//		
//		parent_graph.add(line1);
//		//parent_graph.add(scatter1);
//
//		parent_graph.autoScale();
//		parent_graph.setXLabel(xLabel);
//		parent_graph.setBottomAxisLabelled(true);
//		parent_graph.setYLabel(""); // parent_graph.setYLabel(yLabel[0]); causes overlap with numbers!
//		parent_graph.setTopAxisLabelled(false);
//
//		int j = 0;
//		for (String yLabel_i: yLabels){
//			// make scatter plot
//			java.util.Random rand = new java.util.Random();
//			Color c = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
//
//
//
//			// make line plot
//
//
//			GJPlotInterface line_temp= GJLine.createInstance();
//			line_temp.setLineColor(c);
//			line_temp.setXData(timeList);
//			line_temp.setYData(series.get(j++));
//			
//			
//			parent_graph.add(line_temp, rescale);
//
////			savePlot(yLabel_i, line_temp);
//		}
//		
//		//plot the average data
//		GJPlotInterface avg_temp= GJLine.createInstance();
//		avg_temp.setLineColor(Color.black);
//		final float dash1[] = {10.0f};
//		BasicStroke s = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
//		avg_temp.setLineStroke(s);
//		avg_temp.setXData(timeList);
//		avg_temp.setYData(avg);
//		
//		parent_graph.add(avg_temp, rescale);
//
////		savePlot("avg_" + yLabels[0], avg_temp);
//
//		// remove the Edit graph menu option since it does not work with insubstantial
//		JPopupMenu contextMenu = parent_graph.getContextMenu();
//		
//		Component[] components = contextMenu.getComponents();
//		for (Component component : components) {
//			if (component instanceof JMenuItem) {
//				JMenuItem menuItem = (JMenuItem) component;
//				if ("Edit Graph".equals(menuItem.getText())){
//					contextMenu.remove(menuItem);
//					break;
//				}
//			}
//		}
//		
//		return parent_graph;
//	}
//	
//	private GJGraph createChart(String xLabel, boolean paintYAxis, boolean rescale, String[] yLabels, double[] timeList,
//			List<double[]> series) {
//
//		//make a line plot for variable 1
//		GJPlotInterface line1= GJLine.createInstance();
//		line1.setXData(timeList);
//		line1.setYData(series.get(0));
//
//		//add both plots to a graph and graph-container
//		GJGraph parent_graph = GJGraph.createInstance();
//		
//		parent_graph.add(line1);
//		//parent_graph.add(scatter1);
//
//		parent_graph.autoScale();
//		parent_graph.setXLabel(xLabel);
//		parent_graph.setBottomAxisLabelled(true);
//		parent_graph.setYLabel(""); // parent_graph.setYLabel(yLabel[0]); causes overlap with numbers!
//		parent_graph.setTopAxisLabelled(false);
//
//		int j = 0;
//		for (String yLabel_i: yLabels){
//			// make scatter plot
//			java.util.Random rand = new java.util.Random();
//			Color c = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
//
//
//
//			// make line plot
//
//
//			GJPlotInterface line_temp= GJLine.createInstance();
//			line_temp.setLineColor(c);
//			line_temp.setXData(timeList);
//			line_temp.setYData(series.get(j++));
//			
//			
//			parent_graph.add(line_temp, rescale);
//
////			savePlot(yLabel_i, line_temp);
//		}
//		
//		// remove the Edit graph menu option since it does not work with insubstantial
//		JPopupMenu contextMenu = parent_graph.getContextMenu();
//		
//		Component[] components = contextMenu.getComponents();
//		for (Component component : components) {
//			if (component instanceof JMenuItem) {
//				JMenuItem menuItem = (JMenuItem) component;
//				if ("Edit Graph".equals(menuItem.getText())){
//					contextMenu.remove(menuItem);
//					break;
//				}
//			}
//		}
//		
//		return parent_graph;
//	}
//	
//	private GJGraph createScatterChart(String xLabel, boolean paintYAxis, boolean rescale, String[] yLabels, double[] timeList,
//			List<double[]> series) {
//
//		//make a line plot for variable 1
//		GJPlotInterface scatter1= GJScatter.createInstance();
//		scatter1.setXData(timeList);
//		scatter1.setYData(series.get(0));
//
//		//add both plots to a graph and graph-container
//		GJGraph parent_graph = GJGraph.createInstance();
//		
//		parent_graph.add(scatter1);
//		
//		parent_graph.autoScale();
//		parent_graph.setXLabel(xLabel);
//		parent_graph.setBottomAxisLabelled(true);
//		parent_graph.setYLabel(""); // parent_graph.setYLabel(yLabel[0]); causes overlap with numbers!
//		parent_graph.setTopAxisLabelled(false);
//
//		int j = 0;
//		for (String yLabel_i: yLabels){
//			// make scatter plot
//			java.util.Random rand = new java.util.Random();
//			Color c = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
//
//			GJPlotInterface scatter_temp= GJScatter.createInstance();
//			scatter_temp.setEdgeColor(c);
//			scatter_temp.setXData(timeList);
//			scatter_temp.setYData(series.get(j++));
//
//			parent_graph.add(scatter_temp, rescale);
//
////			savePlot(yLabel_i, scatter_temp);
//		}
//		
//		// remove the Edit graph menu option since it does not work with insubstantial
//		JPopupMenu contextMenu = parent_graph.getContextMenu();
//		
//		Component[] components = contextMenu.getComponents();
//		for (Component component : components) {
//			if (component instanceof JMenuItem) {
//				JMenuItem menuItem = (JMenuItem) component;
//				if ("Edit Graph".equals(menuItem.getText())){
//					contextMenu.remove(menuItem);
//					break;
//				}
//			}
//		}
//		
//		return parent_graph;
//	}
//
//	private GJGraph createScatterChart(String xLabel, boolean paintYAxis, boolean rescale, String yLabel, String xData, String yData, Map<String,List<Number>> results) {
//
//		double[] xTimeSeries = new double[results.get(xData).size()];
//		int i = 0;
//		for (Number step : results.get(xData)){
//			xTimeSeries[i++] = step.doubleValue();
//		}
//
//		double[] yTimeSeries = new double[results.get(yData).size()];
//		i = 0;
//		for (Number step : results.get(yData)){
//			yTimeSeries[i++] = step.doubleValue();
//		}
//		
//		//make a line plot for variable 1
//		GJPlotInterface scatter1= GJScatter.createInstance();
//		scatter1.setXData(xData);
//		scatter1.setYData(yData);
//
//		//add both plots to a graph and graph-container
//		GJGraph parent_graph = GJGraph.createInstance();
//		
//		//parent_graph.add(line1);
//		parent_graph.add(scatter1);
//
//		parent_graph.autoScale();
//		parent_graph.setXLabel(xLabel);
//		parent_graph.setBottomAxisLabelled(true);
//		parent_graph.setYLabel(yLabel); // parent_graph.setYLabel(yLabel[0]); causes overlap with numbers!
//		parent_graph.setTopAxisLabelled(false);
//
//		
//		
//		// remove the Edit graph menu option since it does not work with insubstantial
//		JPopupMenu contextMenu = parent_graph.getContextMenu();
//		
//		Component[] components = contextMenu.getComponents();
//		for (Component component : components) {
//			if (component instanceof JMenuItem) {
//				JMenuItem menuItem = (JMenuItem) component;
//				if ("Edit Graph".equals(menuItem.getText())){
//					contextMenu.remove(menuItem);
//					break;
//				}
//			}
//		}
//		
//		return parent_graph;
//	}

	/**
	 * Adapter Pattern / Wrapper to allow String[] for yLabel input.
	 * Plots a number of (time-)series per chart, where the values are calculated as the logarithm of the ratio of successive dataset values, i.e. log(p[t+1]/p[t]).  
	 * This method has been written to chart multiple time-series of log share return data, where the return is the capital return (price ratio) excluding dividend payouts
	 * 
	 * <p>
	 * Can plot multiple datasets over the same time axis, using a line plot with circular markers, and optionally drawing y-axes for
	 * each dataset.
	 *   
	 * 
	 * 
	 * @param tempBankArray
	 * @param results is the map from datasets' names to the datasets themselves
	 * @param xLabel is the label for the common x axis
	 * @param yLabels is the list of labels for each dataset
	 * @param paintYAxis a flag to determine if the y axis should be displayed for all datasets
	 * @param rescale a flag to rescale the datasets so that they fit on the same plot.
	 * @return A graph container with the required chart in it.
	 * @author Ross Richardson
	 *
	 */
//	private GJGraphContainer plotNlogReturnVariables(String[] tempBankArray,
//			Map<String, List<Number>> results, String xLabel, String yLabels, boolean paintYAxis, boolean rescale) {
//		String[] yLabelArray = {yLabels};
//		return plotNlogReturnVariables(tempBankArray, results, xLabel, yLabelArray, paintYAxis, rescale);
//	}
	
	
//	private GJGraphContainer plotNlogReturnVariables(String[] names, Map<String,List<Number>> results, final String xLabel, final String[] yLabel, final boolean paintYAxis, final boolean rescale) {
//		int i;
//		final int numVars = names.length;
//		final int numLabels = yLabel.length;
//
//		//check that variables were sent
//		if (numVars == 0)
//			return null;
//
//		if (!results.containsKey(names[0])){
//			return null;
//		}
//
//		//Check no. of labels. If only 1 use it otherwise use all given and pad if necessary.
//		final String[] yLabels = new String[numVars];
//		if(numLabels==1){								
//			yLabels[0]=yLabel[0];
//			for (i = 1; i < numVars; i++)
//				yLabels[i] = "";
//		}	else {
//			for (i = 0; i < numLabels; i++){
//				yLabels[i] = yLabel[i];
//			}
//			for (i = numLabels; i < numVars; i++){
//				yLabels[i] = "";
//			}
//		}
//
//		//make turn list (=time) from results map
//		final double[] timeList = new double[results.get(TURN_SERIES_NAME).size()];
//		i = 0;
//		for (Number turn : results.get(TURN_SERIES_NAME)){
//			timeList[i++] = turn.doubleValue();
//		}
//
//		//cycle through all variables and get data, store as row vectors in the list: series
//		final List<double[]> series = new ArrayList<double[]>();
//
//
//		for (String name : names){
//			double[] temp = new double[results.get(TURN_SERIES_NAME).size()];
//			i = 0;
//			for (Number value : results.get(name)){
//				temp[i++] = value.doubleValue();
//			}
//			double[] logTemp = new double[results.get(TURN_SERIES_NAME).size()-1];
//			for (i=0; i<results.get(TURN_SERIES_NAME).size()-1; i++){
//				logTemp[i]=Math.log(temp[i+1]/temp[i]);
//			}
//
//			series.add(logTemp);
//		}
//		//make a scatter plot for each variable
//		//make a scatter plot for variable 1
//		GJGraph chart = createChart(xLabel, paintYAxis, rescale, yLabels, timeList, series);
//
////		chart.setEditor(nullEditor);
//
//		final JPopupMenu popupMenu = new JPopupMenu();
//
//		JMenuItem menuItem = popupMenu.add(new JMenuItem("Chart configuration"));
//		menuItem.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				if (POPUPMENU_CONFIG.equals(event.getActionCommand())){
//
//				}
//
//			}
//		});
//
//		chart.addMouseListener(new MouseAdapter() {
//
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (e.getClickCount() == 2){
//					GJGraph chart2 = createChart(xLabel, paintYAxis, rescale, yLabels, timeList, series);
////					chart2.setEditor(nullEditor);
//					chart2.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart2.getYTransform()));
//					GJGraphContainer graphContainer = GJGraphContainer.createInstance(chart2);
//					graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
//					graphContainer.setTitle(new JFormattedTextField(yLabel[0]+" / "+xLabel));
//					tabPane.add(yLabels[0], graphContainer);
//					tabPane.setSelectedComponent(graphContainer);
//					tabPane.validate();
//				}
//			}
//
//			@Override
//			public void mousePressed(MouseEvent event) {
//				maybeShowPopup(event);
//			}
//
//			@Override
//			public void mouseReleased(MouseEvent event) {
//				maybeShowPopup(event);
//			}
//
//			private void maybeShowPopup(final MouseEvent event){
//				if (event.isPopupTrigger()){
//					popupMenu.show(event.getComponent(), event.getX(), event.getY());
//				}
//
//			}
//		});
//
//		chart.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
//		chart.setXTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
//
//		GJGraphContainer graphContainer = GJGraphContainer.createInstance(chart);
//		graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
//		graphContainer.setTitle(new JFormattedTextField(yLabel[0]+" / "+xLabel));
//
//		graphContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, INDEX_CHART_HEIGHT));
//		graphContainer.setMinimumSize(new Dimension(INDEX_CHART_WIDTH, INDEX_CHART_HEIGHT));
//		graphContainer.setPreferredSize(new Dimension(INDEX_CHART_WIDTH, INDEX_CHART_HEIGHT));
//
//		return graphContainer;
//	}

	private JComponent scatterTwoSeries(final String xName, final String yName, final Run run, final String xLabel, final String yLabel, final boolean paintYAxis, final boolean rescale, final Color color, final boolean plotChart){
		if(plotChart){
			final ScatterPlot scatterPlot = new ScatterPlot(xName, yName, run, xLabel, yLabel, rescale, color);
	
			scatterPlot.setXLabel(null);
			scatterPlot.setYLabel(null);
			scatterPlot.disableMouseInteraction();
	
			currentOnLinePlots.add(scatterPlot);
			GJGraphContainer graphContainer = scatterPlot.getGraphContainer();
	
			final GJGraphInterface graph = graphContainer.getView();
			((GJGraph)graph).addMouseListener(new MouseAdapter() {
	
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2){
						if (!tabbedPlots.containsKey(yLabel)){
							//					GJGraphContainer graphContainer = new ScatterPlot(xName, yName, run, xLabel, yLabel, rescale).createGraphContainer();
							ScatterPlot scatterPlot2 = new ScatterPlot(xName, yName, scatterPlot.getXData(), scatterPlot.getYData(), xLabel, yLabel, rescale, color);
							currentOnLinePlots.add(scatterPlot2);
							tabbedPlots.put(yLabel, scatterPlot2);
	
							GJGraphContainer graphContainer = scatterPlot2.getGraphContainer();
							graphContainer.getView();
//							graph.autoScale();
							graphContainer.setName(yLabel);
							graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
							graphContainer.setTitle(new JFormattedTextField(yLabel + " / " + xLabel));
							addClosableTab(tabPane, graphContainer, yLabel, null);
	//						tabPane.add(yLabel, graphContainer);
							tabPane.setSelectedComponent(graphContainer);
							tabPane.validate();
						} else {
							tabPane.setSelectedComponent(tabbedPlots.get(yLabel).getGraphContainer());
						}
					}
				}
			});
			
			// put the chart in a vbox, to prevent resizing it to fit the available space
			Box verticalBox = Box.createVerticalBox();
			verticalBox.add(graphContainer);

			Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
			Style.apply(verticalBox, dashboard.getCssStyle());
			
			return verticalBox;
		}
		else return null;
	}

	//Create a scatterTwoSeries method that doesn't require a Run object in the argument list, for use in plotting data created locally e.g. log asset or leverage growth data.  Ross
	private JComponent scatterTwoSeries(final String xName, final String yName, final double[] XData, final double[] YData, final String xLabel, final String yLabel, final boolean paintYAxis, final boolean rescale, final Color color, final boolean plotChart){
		if(plotChart){
			final ScatterPlot scatterPlot = new ScatterPlot(xName, yName, XData, YData, xLabel, yLabel, rescale, color);
			currentOnLinePlots.add(scatterPlot);
			scatterPlot.disableMouseInteraction();
			GJGraphContainer graphContainer = scatterPlot.getGraphContainer();
			
			GJGraphInterface graph = graphContainer.getView();
			((GJGraph)graph).addMouseListener(new MouseAdapter() {
	
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2){
						if (!tabbedPlots.containsKey(yLabel)){
							ScatterPlot scatterPlot2 = new ScatterPlot(xName, yName, scatterPlot.getXData(), scatterPlot.getYData(), xLabel, yLabel, rescale, color);
							currentOnLinePlots.add(scatterPlot2);
							tabbedPlots.put(yLabel, scatterPlot2);
							GJGraphContainer graphContainer = scatterPlot2.getGraphContainer();
							graphContainer.getView();
//							graph.autoScale();
							graphContainer.setName(yLabel);
							graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
							graphContainer.setTitle(new JFormattedTextField(yLabel + " / " + xLabel));
							addClosableTab(tabPane, graphContainer, yLabel, null);
	//						tabPane.add(yLabel, graphContainer);
							tabPane.setSelectedComponent(graphContainer);
							tabPane.validate();
						} else {
							tabPane.setSelectedComponent(tabbedPlots.get(yLabel).getGraphContainer());
						}
					}
				}
			});
	
//			graph.autoScale();
			
			// put the chart in a vbox, to prevent resizing it to fit the available space
			Box verticalBox = Box.createVerticalBox();
			verticalBox.add(graphContainer);

			Style.registerCssClasses(verticalBox, Dashboard.CSS_CLASS_COMMON_PANEL);
			Style.apply(verticalBox, dashboard.getCssStyle());
			
			return verticalBox;
		}
		else return null;
	}
	
	
	
//	private GJGraphContainer scatterTwoSeries(final String xName, final String yName, final Run run, final String xLabel, final String yLabel, final boolean paintYAxis, final boolean rescale) {
//
//		if (!run.resultNames().contains(xName) || !run.resultNames().contains(yName)){
//			return null;
//		}
//
//
//		//make turn list (=time) from results map
//		final double[] xSeries = new double[run.getResults(xName).size()];
//		int i = 0;
//		for (Object nextDataPoint : run.getResults(xName)){
//			xSeries[i++] = ((Number)nextDataPoint).doubleValue();
//		}
//
//		//	final double[] ySeries = new double[results.get(names[1]).size()];
//		//	i = 0;
//		//	for (Number nextDataPoint : results.get(names[1])){
//		//		ySeries[i++] = nextDataPoint.doubleValue();
//		//	}
//		//	
//		//cycle through all variables and get data, store as row vectors in the list: series
//		final List<double[]> ySeries = new ArrayList<double[]>();
//
//
//
//		double[] temp = new double[run.getResults(yName).size()];
//		i = 0;
//		for (Object value : run.getResults(yName)){
//			temp[i++] = ((Number)value).doubleValue();
//		}
//
//		ySeries.add(temp);
//
//
//		final String[] yLabels= {yLabel};
//		//make a scatter plot for each variable
//		//make a scatter plot for variable 1
//		GJGraph chart  = createScatterChart(xLabel, true, false, yLabels,xSeries, ySeries);
//
//		chart.setEditor(nullEditor);
//
//		final JPopupMenu popupMenu = new JPopupMenu();
//
//		JMenuItem menuItem = popupMenu.add(new JMenuItem("Chart configuration"));
//		menuItem.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				if (POPUPMENU_CONFIG.equals(event.getActionCommand())){
//
//				}
//
//			}
//		});
//
//		chart.addMouseListener(new MouseAdapter() {
//
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (e.getClickCount() == 2){
//					GJGraph chart2 = createScatterChart(xLabel, true, false, yLabels,xSeries, ySeries);
//					chart2.setEditor(nullEditor);
//					chart2.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart2.getYTransform()));
//					GJGraphContainer graphContainer = GJGraphContainer.createInstance(chart2);
//					graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
//					graphContainer.setTitle(new JFormattedTextField(yLabels[0] +" / "+xLabel));
//					tabPane.add(yLabels[0], graphContainer);
//					tabPane.setSelectedComponent(graphContainer);
//					tabPane.validate();
//				}
//			}
//
//			@Override
//			public void mousePressed(MouseEvent event) {
//				maybeShowPopup(event);
//			}
//
//			@Override
//			public void mouseReleased(MouseEvent event) {
//				maybeShowPopup(event);
//			}
//
//			private void maybeShowPopup(final MouseEvent event){
//				if (event.isPopupTrigger()){
//					popupMenu.show(event.getComponent(), event.getX(), event.getY());
//				}
//
//			}
//		});
//
//		chart.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
//
//		GJGraphContainer graphContainer = GJGraphContainer.createInstance(chart);
//		graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
//		graphContainer.setTitle(new JFormattedTextField(yLabels[0]+" / "+xLabel));
//
//		graphContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, INDEX_CHART_HEIGHT));
//		graphContainer.setMinimumSize(new Dimension(INDEX_CHART_WIDTH, INDEX_CHART_HEIGHT));
//		graphContainer.setPreferredSize(new Dimension(INDEX_CHART_WIDTH, INDEX_CHART_HEIGHT));
//
//		return graphContainer;
//	}

	/**
	 * @return the numOfResultsToDisplay
	 */
	public int getNumOfResultsToDisplay() {
		return numOfResultsToDisplay;
	}

	/**
	 * @return the chartUpdatePeriod
	 */
	public long getChartUpdatePeriod() {
		return chartUpdatePeriod;
	}

	/**
	 * @param numOfResultsToDisplay the numOfResultsToDisplay to set
	 */
	public void setNumOfResultsToDisplay(int numOfResultsToDisplay) {
		this.numOfResultsToDisplay = numOfResultsToDisplay;
	}

	/**
	 * @param chartUpdatePeriod the chartUpdatePeriod to set
	 */
	public void setChartUpdatePeriod(long chartUpdatePeriod) {
		this.chartUpdatePeriod = chartUpdatePeriod;
	}
	
	private static void scrollToVisible(final JTable table, final int rowIndex, final int vColIndex) {
		if (!(table.getParent() instanceof JViewport)) return;
		
		final JViewport viewport = (JViewport) table.getParent();
	    final Rectangle rect = table.getCellRect(rowIndex,vColIndex,true);
	    final Point pt = viewport.getViewPosition();
	    rect.setLocation(rect.x - pt.x,rect.y - pt.y);
	    table.scrollRectToVisible(rect);
    }

	private class ChartUpdater implements Runnable {
		Pair<Map<String,Object>,Map<String,List<Object>>> results;

		public ChartUpdater(final Pair<Map<String,Object>,Map<String, List<Object>>> results) {
			if (results.getValue0() != null && results.getValue1() != null){
				this.results = new Pair<Map<String,Object>, Map<String,List<Object>>>(new HashMap<String, Object>(results.getValue0()), new HashMap<String, List<Object>>(results.getValue1()));
			} else {
				this.results = new Pair<Map<String,Object>, Map<String,List<Object>>>(null, null);
			}
		}
		
		@Override
      public void run() {
			try {
				Map<String, Object> parameters = results.getValue0();
				Map<String, List<Object>> values = results.getValue1();

				// the last pair signalling the end of the results stream is (null, null)
				if (parameters == null || values == null){
					currentParameters = null;
					return;
				}
				
				if (currentParameters == null || !currentParameters.equals(parameters)){
					Run run = new Run();
					for (String parameterName: parameters.keySet()) {
						run.addParameter(parameterName, parameters.get(parameterName));
					}

					for (String columnName : values.keySet()){
						run.addResults(columnName, values.get(columnName));
					}

					currentOnLinePlots.clear();
					currentOffLinePlots.clear();
					displayCharts(run, dashboard.isDisplayAdvancedCharts(), dashboard.getNumberTimestepsToIgnore(), true);
					currentParameters = parameters;
				} else {
					for (DynamicPlot plot : currentOnLinePlots){
						plot.addDataCollection(values);
					}
				}
			} catch (Throwable e){
				log.error("Exception while updating the charts", e);
			}
		}
	}
	
	private class OfflineChartUpdater implements Runnable {

		Run run;
		
		public OfflineChartUpdater(final Run run) {
			this.run = run;
		}
		
		@Override
		public void run() {
			Set<DynamicPlot> offlinePlots = currentOffLinePlots.keySet();
			for (DynamicPlot plot : offlinePlots) {
				Preprocessor preprocessor = currentOffLinePlots.get(plot);
				plot.addDataCollection(preprocessor.process(run));
			}

		}
		
	}
}

