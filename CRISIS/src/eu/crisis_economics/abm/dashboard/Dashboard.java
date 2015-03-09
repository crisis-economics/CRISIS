/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Rajmund Bocsi
 * Copyright (C) 2015 Milan Lovric
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.aitia.meme.database.Columns;
import ai.aitia.meme.database.Result.Row;
import ai.aitia.meme.database.Run;
import ai.aitia.meme.gui.SimpleFileFilter;
import ai.aitia.meme.paramsweep.batch.IBatchController;
import ai.aitia.meme.paramsweep.batch.param.ISubmodelParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterNode;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.batch.param.SubmodelInfo;
import ai.aitia.meme.paramsweep.internal.platform.PlatformSettings;
import ai.aitia.meme.paramsweep.platform.PlatformManager.PlatformType;
import ai.aitia.meme.paramsweep.platform.mason.impl.IRecorderListenerAware;
import ai.aitia.meme.paramsweep.platform.mason.impl.MasonRecorderListener;
import ai.aitia.meme.paramsweep.utils.Util;
import ai.aitia.meme.paramsweep.utils.Utilities;
import ai.aitia.meme.pluginmanager.Parameter;
import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.swing.SwingStyle;

import com.google.common.collect.ImmutableMap;

import eu.crisis_economics.abm.dashboard.GASearchPanelModel.FitnessFunctionDirection;
import eu.crisis_economics.abm.dashboard.ModelHandler.CompletionHandler;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.results.ResultParser;
import eu.crisis_economics.utilities.Pair;

/**
 * @author Tamás Máhr
 *
 */
public class Dashboard extends JFrame {

	private static final String MODEL_CONFIGURATION_FILE_FILTER_DESCRIPTION = "Model configuration files (*.xml)";
	private static final String DEFAULT_CONFIG_NAME = "model-configuration.xml";
	
	public static final String CSS_CLASS_COMMON_PANEL = ".commonPanel";

	private static final String MASON_RECORDER_TIME_SERIES_NAME = "tick";

	private static final String ERRORDIALOG_BUTTON_CHARTS = "Charts";

	public static final String WORKSPACE_PREFERENCE_NODE = "workspace";

	public static final String NUMBER_OF_RESULTS_PREFERENCE_NODE = "number-of-results";

	public static final String CHART_UPDATE_PERIOD_PREFERENCE_NODE = "chart-update-period";

	private static final String TITLE_PREFIX = "Dashboard";

	public static final int NUMBER_OF_TURNS = 400;
	
	public static final int NUMBER_OF_TIMESTEPS_TO_IGNORE = 0;

	private static final int APP_WINDOW_HEIGHT = 300;

	private static final int APP_WINDOW_WIDTH = 500;
	
	private static final Color DEFAULT_MODEL_BUTTON_COLOR = new Color(0, 147, 201);

   public static ImmutableMap<String, Pair<String, Color>>
      availableModels = ImmutableMap.<String, Pair<String, Color>>builder()
      .put("eu.crisis_economics.abm.model.MasterModel",
           Pair.create("Master Model", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.BusinessCycleModel",
           Pair.create("Business Cycle", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.MultipleFirmsPerSectorModel",
           Pair.create("Multiple Firms Per Sector Model", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.Basel2Model",
           Pair.create("Basel II Model", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.RepoLoanModel",
           Pair.create("Central Bank Repo Loan Model", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.Basel2ModelWithNoiseTraders",
           Pair.create("Basel II Model With Noise Traders", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.Basel2ModelWithBusinessCycle",
           Pair.create("Basel II Model With Business Cycle", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.GBCLModel",
           Pair.create("Gilts, Bonds & Graded Commercial Loans", new Color(190, 100, 100)))
      .put("eu.crisis_economics.abm.model.BankBailinModel",
           Pair.create("Bailin Bank Bankruptcy", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.GBCLModelWithBasel2",
           Pair.create("Gilts, Bonds & Graded Commercial Loans With Basel II",
              new Color(190, 100, 100)))
      .put("eu.crisis_economics.abm.model.BankBailoutModel",
           Pair.create("Bailout Bank Bankruptcy", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.GBCLModelWithBasel2NoiseTradersAndBusinessCycle",
           Pair.create("Gilts, Bonds & Graded Commercial Loans With Basel II"
           + " Regulation, Macroeconomic Cycles and Noise Traders",
              new Color(190, 100, 100)))
      .put("eu.crisis_economics.abm.model.BankLiquidationModel",
           Pair.create("Liquidation Bank Bankruptcy", DEFAULT_MODEL_BUTTON_COLOR))
//      .put("eu.crisis_economics.abm.model.FTSE100Model",
//           Pair.create("FTSE100 Model", DEFAULT_MODEL_BUTTON_COLOR))
//      .put("eu.crisis_economics.abm.model.ProductionChainModel",
//           Pair.create("Production Chain", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.FirmBankruptcyModel",
           Pair.create("Firm Bankruptcy", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.NoInputOutputNetworkModel",
          Pair.create("No Input-Output Network", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.OrsteinUhlenbeckTFPModel",
          Pair.create("Orstein Uhlenbeck Model", DEFAULT_MODEL_BUTTON_COLOR))
      .put("eu.crisis_economics.abm.model.RandomModel",
         Pair.create("Random Model", DEFAULT_MODEL_BUTTON_COLOR))
      .build();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(Dashboard.class);
	
	private static Preferences preferences = Preferences.userNodeForPackage(Dashboard.class);

	private Page_Model modelPage;

	private Page_Parameters parametersPage;

	private Page_Results resultsPage;

	private Page_Run runPage;
	
	private DWizard wizard;

	private IModelHandler modelHandler;
	
	private static File workspace;
	
	private boolean isRunning;
	
	private JMenuItem saveConfigMenuItem;
	
	private PreferencesDialog preferencesDialog = new PreferencesDialog(this);

	private Style cssStyle;


	/**
	 * This queue is used to communicate results to Page_Results during a simulation run. The pairs in the queue are received from the batch
	 * controller via the {@link IRecorderListenerAware} interface.
	 */
	private BlockingQueue<org.javatuples.Pair<Map<String, Object>, Map<String, Object>>>
	   resultsQueue = new LinkedBlockingQueue<org.javatuples.Pair<Map<String,Object>,Map<String,Object>>>();
	
	private boolean onLineCharts = false;
	private boolean displayAdvancedCharts = true;

	private long startOfTimer;
	
	private FitnessFunctionDirection optimizationDirection = null;
	
	/**
	 * 
	 */
	public Dashboard(final File workspace) {
	    super(TITLE_PREFIX);

		SwingStyle.init();
		
		cssStyle = new Style(getClass().getResource("dashboard.css"));

	    Dashboard.workspace = workspace;
	    
	    wizard = new DWizard(cssStyle);
	    wizard.setBackground(Color.WHITE);
	    
	    modelPage = new Page_Model(wizard, this);
	    wizard.addPage(modelPage);
	    
	    parametersPage = new Page_Parameters(wizard, this);
	    wizard.addPage(parametersPage);
	    
	    runPage = new Page_Run(wizard, this);
	    wizard.addPage(runPage);
	    
	    int numberOfResults = preferences.getInt(NUMBER_OF_RESULTS_PREFERENCE_NODE, 1);
	    int chartUpdatePeriod = preferences.getInt(CHART_UPDATE_PERIOD_PREFERENCE_NODE, 0);
	    
	    preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
			
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals(NUMBER_OF_RESULTS_PREFERENCE_NODE)){
					if (!evt.getNewValue().isEmpty()){
						resultsPage.setNumOfResultsToDisplay(Integer.parseInt(evt.getNewValue()));
					} else {
						resultsPage.setNumOfResultsToDisplay(1);
					}
				}
				
				if (evt.getKey().equals(CHART_UPDATE_PERIOD_PREFERENCE_NODE)){
					if (!evt.getNewValue().isEmpty()){
						resultsPage.setChartUpdatePeriod(Integer.parseInt(evt.getNewValue()));
					} else {
						resultsPage.setChartUpdatePeriod(0);
					}
				}
			}
		});
	    
	    resultsPage = new Page_Results(wizard, this, resultsQueue, numberOfResults, chartUpdatePeriod);
	    wizard.addChartsPage(resultsPage);
	    
//	    // magic needed for the proper initialization of the wizard
//	    wizard.gotoPage(3);
//	    wizard.gotoPage(0);

	    wizard.populateInfoPane();
	    
	    wizard.setPreferredSize(new Dimension(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT));
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - APP_WINDOW_WIDTH) / 2, (screenSize.height - APP_WINDOW_HEIGHT) / 2);

	    setContentPane(wizard);

	    JMenuBar menuBar = new JMenuBar();
	    setJMenuBar(menuBar);
	    
	    menuBar.setBackground(new Color(0, 147, 201));
	    
	    JMenu menu = new JMenu("Settings");
	    menuBar.add(menu);
	    menu.setMnemonic(KeyEvent.VK_S);
	    
	    menu.add(getSaveConfigMenuItem());
	    
	    JMenuItem menuItem = new JMenuItem("Preferences", KeyEvent.VK_P);
	    menu.add(menuItem);
	    if (System.getProperty("os.name").contains("OS X")){
	    	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_DOWN_MASK));
	    } else {
	    	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));	    	
	    }
	    menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				preferencesDialog.setVisible(true);
			}
		});
	    
	    menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
	    menu.add(menuItem);
	    if (System.getProperty("os.name").contains("OS X")){
	    	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK));
	    } else {
	    	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
	    }
	    menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
	    
//	    JMenu helpMenu = new JMenu("Help");
//	    menuBar.add(helpMenu);
//	    helpMenu.setMnemonic(KeyEvent.VK_H);
//	    
//	    menuItem = new JMenuItem("About", KeyEvent.VK_A);
//	    helpMenu.add(menuItem);
	    
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
		Style.apply(getContentPane(), cssStyle);
		Style.apply(menuBar, cssStyle);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Style.apply(this, cssStyle);
			}
		});
	}
	
	public IModelHandler createModelHandler(final String modelName) throws ClassNotFoundException{
		ModelClassPool modelClassPool = new ModelClassPool();
		modelHandler = new ModelHandler(modelName, modelClassPool);
		
		return modelHandler;
	}
	
	
	//----------------------------------------------------------------------------------------------------
	public void saveConfiguration(final File workDirOrFile) throws IOException {
		File f = workDirOrFile;
		if (f.isDirectory()) 
			f = new File(workDirOrFile,DEFAULT_CONFIG_NAME);
		
		final Model modelConfig = parametersPage.saveConfiguration();
		if (modelConfig != null) {
			try {
				final JAXBContext context = JAXBContext.newInstance(Model.class);
				final Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
				marshaller.marshal(modelConfig,f);
			} catch (final JAXBException e) {
				throw new IOException(e.getMessage() != null ? e.getMessage() : e.getLinkedException().getMessage(),e.getLinkedException());
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public void loadConfiguration() {
		final SimpleFileFilter fileFilter = new SimpleFileFilter(MODEL_CONFIGURATION_FILE_FILTER_DESCRIPTION);
		File f = fileDialog(true,workspace,fileFilter);
		if (f != null) 
			loadConfiguration(f);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void loadConfiguration(final File file) {
		try {
			final JAXBContext context = JAXBContext.newInstance(Model.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final Model model = (Model) unmarshaller.unmarshal(file);
			
			IModelHandler modelHandler = null;
			try {
				modelHandler = createModelHandler(model.getClazz());
				modelHandler.getRecorderAnnotationValue();
			} catch (final ClassNotFoundException e) {
				log.error("Could not load the model class!", e);
				final ErrorDialog errorDialog = new ErrorDialog(this,"Load configuration error","Model configuration loading is failed: " + file.getName() + ".");
				errorDialog.show();
				return;
			} catch (final Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(wizard, "No recorder specified", "The model does not have '@RecorderSource' annotation.");
				errorDialog.show();
				return;
			}
			
			parametersPage.loadConfiguration(model);
			
			setTitle(model.getClazz().substring(model.getClazz().lastIndexOf('.') + 1));
			wizard.gotoPage(1);
		} catch (final JAXBException e) {
			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getLinkedException().getMessage();
			final ErrorDialog errorDialog = new ErrorDialog(Dashboard.this, "Load configuration error",errorMsg);
			errorDialog.show();
		}
	}

	
	public void runModel(){
		final boolean hasRecorder = modelHandler.hasRecorder();
		
		if (!hasRecorder) {
			//JOptionPane.showMessageDialog(this, "The model does not have '@RecorderSource' annotation", "No recorder specified", JOptionPane.ERROR_MESSAGE);
			ErrorDialog errorDialog = new ErrorDialog(this, "No recorder specified", "The model does not have '@RecorderSource' annotation");
			errorDialog.show();
			wizard.gotoPageRel(-1);
			return;
		}
		
		isRunning = true;
		
		startTimer();
		final File workDir =  new File(workspace + File.separator + modelHandler.getModelClass().getName() + File.separator + 
									   new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(new Date()));

		if (!workDir.exists()){
			workDir.mkdirs();
		}

		try {
			saveConfiguration(workDir);
		} catch (final IOException e) {
			log.error("Exception during saving the model configuration", e);
			
			String errorMsg = e.getMessage();
			if (e.getCause() != null)
				errorMsg = e.getCause().getMessage();
			final ErrorDialog errorDialog = new ErrorDialog(this, "Save configuration error",errorMsg);
			errorDialog.show();
		}
		
		modelHandler.runModel(getNumberOfRequestedTurns(),workDir, 
				new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progressAndIteration".equals(evt.getPropertyName())) {
					@SuppressWarnings("unchecked")
					final Map<String,Number> values = (Map<String,Number>) evt.getNewValue();
					
					runPage.setProgress((Integer)values.get("progress"));
					final long iteration = (Long) values.get("iteration");
					runPage.setIteration(iteration);
				}
			}
		},
		(isOnLineCharts() ?
		new MasonRecorderListener() {
			
			@Override
			public void recordingPerformed(Map<String, Object> parameters, Map<String, Object> values) {
				// this is called from the simulation thread, we should pass the data to Page_Results on the event dispatch thread!
				// in fact, we pass it to a queue, which is consumed by another thread
				
				try {
					values.put(Page_Results.TURN_SERIES_NAME, values.get(MASON_RECORDER_TIME_SERIES_NAME));
					org.javatuples.Pair<Map<String, Object>, Map<String, Object>>
					   pair = new org.javatuples.Pair<Map<String,Object>, Map<String,Object>>(parameters, values);
					resultsQueue.add(pair);
				} catch (IllegalStateException e){
					// there is no memory to store these results in the queue, let's drop them
				}
			}
		} : null),
		new CompletionHandler() {
			
			private boolean readResultsFile = !isOnLineCharts() && !isGARun();
			private IBatchController batchController = null;
			
			//----------------------------------------------------------------------------------------------------
			@Override
			public void completed(final Throwable simulationException) {
				long runTime = stopTimer();
				System.out.flush();
				System.out.println("Experiment completed in " + runTime / 1000 + " seconds");
				System.out.flush();
				isRunning = false;
				
				if (simulationException == null){
					if (readResultsFile){
						batchController = modelHandler.getBatchController();
						SwingWorker<List<eu.crisis_economics.abm.dashboard.results.Run>,Integer> worker = new SwingWorker<List<eu.crisis_economics.abm.dashboard.results.Run>, Integer>(){

							private List<eu.crisis_economics.abm.dashboard.results.Run> results;

							private Exception exception;

							//----------------------------------------------------------------------------------------------------
							@Override
							protected List<eu.crisis_economics.abm.dashboard.results.Run> doInBackground() throws Exception {
								try {
									ResultParser resultParser = new ResultParser(modelHandler.getRecoderFile());
									resultParser.readFile();
									resultParser.replaceNaPlaceholders(modelHandler.getSettingsXMLInputStream());

									Set<String> parameterNames = getParameterNames();
									Map<String, Object> parameterValues = new HashMap<String, Object>();

									for (final String name : parameterNames) {
										parameterValues.put(name, null);
									}

									// collect the fixed parameters
									Columns parameterColumns = resultParser.getFixedParameters().getColumns();
									for (int i = 0; i < parameterColumns.size(); i++) {
										parameterValues.put(parameterColumns.get(i).getName(), resultParser.getFixedParameters().get(i));
									}

									ArrayList<Run> runs = resultParser.getRuns();
									results = new ArrayList<eu.crisis_economics.abm.dashboard.results.Run>();
									for (Run actRun : runs) {
										eu.crisis_economics.abm.dashboard.results.Run run = new eu.crisis_economics.abm.dashboard.results.Run();
										results.add(run);

										// collect the parameters specific to this run
										for (String parameterName : parameterValues.keySet()) {
											// if a parameter does not have a fixed value, then search the row
											if (parameterValues.get(parameterName) == null){
												Row firstRow = actRun.rows.get(0);
												for (int i = 0; i < firstRow.getColumns().size(); i++) {
													if (firstRow.getColumns().get(i).getName().equals(parameterName)){
														run.addParameter(parameterName, firstRow.get(i));
														break;
													}
												}
											} else {
												run.addParameter(parameterName, parameterValues.get(parameterName));
											}
										}

										ArrayList<Row> rows = actRun.rows;
										for (Row row : rows) {
											Columns columns = row.getColumns();
											run.addResult(Page_Results.TURN_SERIES_NAME, row.getTick());
											for (int i = 0 ; i < row.size() ; i++){
												Object value = row.get(i);
												Parameter parameter = columns.get(i);

												run.addResult(parameter.getName(), value);
											}
										}
									}

									return results;
								} catch (Exception e) {
									Logger log = Dashboard.log;
									log.error("Exception during parsing the results", e);
									exception = e;
								}
								return null;
							}

							//----------------------------------------------------------------------------------------------------
							@SuppressWarnings("unchecked")
							private Set<String> getParameterNames() {
								final Set<String> result = new HashSet<String>();
								
								try {
									Method method = batchController.getClass().getMethod("getConstantParameterNames");
									Map<String,Object> params = (Map<String,Object>) method.invoke(batchController);
									for (final String name : params.keySet())
										result.add(prettyPrint(name));
									
									method = batchController.getClass().getMethod("getMutableParameterNames");
									params = (Map<String,Object>) method.invoke(batchController);
									for (final String name : params.keySet())
										result.add(prettyPrint(name));
									
									return result;
								} catch (final NoSuchMethodException e) {
									// never happens
									throw new IllegalStateException(e);
								} catch (final InvocationTargetException e) {
									// never happens
									throw new IllegalStateException(e);
								} catch (final IllegalAccessException e) {
									// never happens
									throw new IllegalStateException(e);
								}
							}
							
							//----------------------------------------------------------------------------------------------------
							private String prettyPrint(final String id) {
								if (id == null) return null;
								
								String result = id;
								if (result.startsWith("#"))
									result = result.substring(1);
								result = Util.capitalize(result);
								
								return result;
							}

							//----------------------------------------------------------------------------------------------------
							@Override
							protected void done() {
								if (exception != null){
									ErrorDialog errorDialog = new ErrorDialog(wizard, "Parser error", "Error while parsing the results file:\n" + exception.getMessage());
									errorDialog.show();
									wizard.gotoPage(modelPage);
									return;
								}

								Dashboard.this.resultsPage.displayCharts(results, displayAdvancedCharts, getNumberTimestepsToIgnore());
								wizard.gotoPage(resultsPage);
							}
						};
						worker.execute();
					} else if (isGARun()) {
						final SwingWorker<Void,Integer> worker = new SwingWorker<Void,Integer>() {
							
							private String[] header;
							private List<String[]> values;
							private int bestCombinationIdx;
							private ParameterTree bestCombination;
							private Exception exception;
							

							//----------------------------------------------------------------------------------------------------
							@Override
							protected Void doInBackground() {
								values = new ArrayList<String[]>();
								BufferedReader reader = null;
								try {
									reader = new BufferedReader(new FileReader(new File(workDir,"population.txt")));
									String[] bestIndividual = null;
									double bestFitnessValue = FitnessFunctionDirection.MINIMIZE == optimizationDirection ? Double.MAX_VALUE : 0;
									
									String line = reader.readLine();
									header = line.trim().split(";");
									
									int idx = 0;
									while ((line = reader.readLine()) != null) {
										final String[] individual = line.trim().split(";");
										final double individualFitnessValue = Double.parseDouble(individual[individual.length - 1]);
										if (FitnessFunctionDirection.MINIMIZE == optimizationDirection && individualFitnessValue < bestFitnessValue) {
											bestIndividual = individual;
											bestFitnessValue = individualFitnessValue;
											bestCombinationIdx = idx;
										} else if (FitnessFunctionDirection.MAXIMIZE == optimizationDirection && individualFitnessValue > bestFitnessValue) {
											bestIndividual = individual;
											bestFitnessValue = individualFitnessValue;
											bestCombinationIdx = idx;
										}
										
										values.add(individual);
										idx++;
									}
									
									if (bestIndividual == null)
										throw new IllegalStateException("No combinations found.");
									
									bestCombination = createParameterCombination(header,bestIndividual);
									
								} catch (final Exception e) {
									Logger log = Dashboard.log;
									log.error("Exception during parsing the results", e);
									exception = e;
								} finally {
									if (reader != null) {
										try {
											reader.close();
										} catch (final IOException e) {
											// and do what, FBI?
											log.warn("Cannot close file: population.txt.",e);
										}
									}
								}
								
								return null;
							}
							
							//----------------------------------------------------------------------------------------------------
							@Override
							protected void done() {
								if (exception != null){
									ErrorDialog errorDialog = new ErrorDialog(wizard, "Parser error", "Error while parsing the populations file:\n" + exception.getMessage());
									errorDialog.show();
									wizard.gotoPage(modelPage);
									return;
								}

								resultsPage.displayGAResults(header,values,bestCombinationIdx,bestCombination);

								try {
//									createModelHandler(modelHandler.getModelClass().getName());
									ModelHandler oldHandler = (ModelHandler) modelHandler;
									final ModelClassPool oldClassPool = (ModelClassPool) oldHandler.getClassPool();
									final Map<String,Class<?>> oldClassCache = oldHandler.getClassCache();
									modelHandler = new ModelHandler(modelHandler.getModelClass().getName(),oldClassPool,oldClassCache);

								} catch (final ClassNotFoundException e) {
									log.error("Could not load the model class!", e);
								}
								
								wizard.gotoPage(resultsPage);
							};
							
							//----------------------------------------------------------------------------------------------------
							@SuppressWarnings("unchecked")
							private ParameterTree createParameterCombination(final String[] header, final String[] values) {
								final ParameterTree tree = modelHandler.getParameterTree();
								
								//set everybody to constants (using its first value as constant value or using the optimum value in case of a gene);
								final Enumeration<ParameterNode> nodes = tree.breadthFirstEnumeration();
								while (nodes.hasMoreElements()) {
									final ParameterNode node = nodes.nextElement();
									@SuppressWarnings("rawtypes")
									final ParameterInfo info = (ParameterInfo) node.getParameterInfo();
									if (isGene(info,header)) {
										final String optValueStr = getOptimumValue(info,header,values);
										info.setValue(ai.aitia.meme.paramsweep.gui.info.ParameterInfo.getValue(optValueStr,
																											Utilities.toTypeString1(info.getDefaultValue().getClass())));
									} else 
										info.setValue(info.getValues().get(0));
								}
								
								return tree;
							}

							//----------------------------------------------------------------------------------------------------
							private boolean isGene(final ParameterInfo<?> info, final String[] header) {
								final String fullName = calculateFullName(info);
								
								for (int i = 1;i < header.length - 1;++i) { // first is "#pop", last is "fitness"
									if (fullName.equals(header[i].trim())) return true;
								}
								
								return false;
							}
							
							//----------------------------------------------------------------------------------------------------
							private String calculateFullName(final ParameterInfo<?> info) {
								if (info instanceof ISubmodelParameterInfo) {
									final ISubmodelParameterInfo spi = (ISubmodelParameterInfo) info;
									final String parentInstanceId = getSubmodelInfoFullName(spi.getParentInfo());
									return parentInstanceId + "#" + info.getName();
								} 
								
								return info.getName();
							}
							
							//----------------------------------------------------------------------------------------------------
							private String getSubmodelInfoFullName(final SubmodelInfo<?> info) {
								if (info == null) 
									return "";
								
								return getSubmodelInfoFullName(info.getParentInfo()) + "#" + info.getName();
							}
							
							//----------------------------------------------------------------------------------------------------
							private String getOptimumValue(final ParameterInfo<?> info, final String[] header, final String[] values) {
								final String fullName = calculateFullName(info);
								
								int idx = -1;
								for (int i = 1;i < header.length - 1;++i) { // first is "#pop", last is "fitness"
									if (fullName.equals(header[i].trim())) {
										idx = i;
										break;
									}
								}
								
								if (idx > 0)
									return values[idx].trim(); 
								
								throw new IllegalStateException("Optimum value is not found for gene " + fullName + ".");
							}
						};
						worker.execute();
					} else {
					   org.javatuples.Pair<Map<String, Object>, Map<String, Object>>
					      pair = new org.javatuples.Pair<Map<String,Object>, Map<String,Object>>(null, null);
						resultsQueue.add(pair);
//						Dashboard.this.resultsPage.displayCharts((List<eu.crisis_economics.abm.dashboard.results.Run>)null, displayAdvancedCharts, getNumberTimestepsToIgnore());
					}
				} else {
					log.error("Exception during executing simulation", simulationException);

					String message = simulationException.toString();
					
					if (simulationException.getCause() != null && simulationException.getCause().getCause() instanceof InvocationTargetException){
						message = ((InvocationTargetException)simulationException.getCause().getCause()).getTargetException().toString();
					}
					
					if (!isGARun())
						message += "\nIf you want to chart the data collected so far, please press the '" + ERRORDIALOG_BUTTON_CHARTS + "' button";
					
					//JOptionPane.showMessageDialog(wizard, message, "Simulation Error", JOptionPane.ERROR_MESSAGE);
					ErrorDialog errorDialog = isGARun() ? new ErrorDialog(Dashboard.this,"Simulation Error",message,new Object[] { "Go back" }) : 
														  new ErrorDialog(Dashboard.this, "Simulation Error", message,
																  		  new Object[]{ "Go back", ERRORDIALOG_BUTTON_CHARTS });
					errorDialog.show();
					
					if (ERRORDIALOG_BUTTON_CHARTS.equals(errorDialog.getValue())){
						wizard.gotoPage(resultsPage);
					} else {
						wizard.gotoPage(parametersPage);
					}
				}
			}
		});
		
	}
	
	//----------------------------------------------------------------------------------------------------
	public JMenuItem getSaveConfigMenuItem() {
		if (saveConfigMenuItem == null) {
			saveConfigMenuItem = new JMenuItem("Save model configuration", KeyEvent.VK_S);
		    if (System.getProperty("os.name").contains("OS X")){
		    	saveConfigMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK));
		    } else {
		    	saveConfigMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));	    	
		    }
		    saveConfigMenuItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent paramActionEvent) {
					final SimpleFileFilter fileFilter = new SimpleFileFilter(MODEL_CONFIGURATION_FILE_FILTER_DESCRIPTION);
					File f = fileDialog(false,new File(workspace + File.separator + DEFAULT_CONFIG_NAME),fileFilter);
					if (f == null) return;
					
					if (!f.getName().endsWith(".xml"))
						f = new File(f.getPath() + ".xml");
					
					while (f.exists()) {
						int result = Utilities.askUser(Dashboard.this,false,"Override comfirmation",String.format("%s exists. The dashboard may change its" +
													   " content.",f.getName()),"Are you sure?");
						if (result == 0) {
							f = fileDialog(false,f,fileFilter);
							if (f == null) return;
						} else break;
					}
					
					try {
						saveConfiguration(f);
					} catch (final IOException e) {
						log.error("Exception during saving the model configuration", e);
						
						String errorMsg = e.getMessage();
						if (e.getCause() != null)
							errorMsg = e.getCause().getMessage();
						final ErrorDialog errorDialog = new ErrorDialog(Dashboard.this, "Save configuration error",errorMsg);
						errorDialog.show();
					}
				}
			});
		}
		
		return saveConfigMenuItem;
	}
	
	public double getNumberOfRequestedTurns(){
		return parametersPage.getNumberOfRequestedTurns();
	}
	
	public int getNumberTimestepsToIgnore(){
		return (int)parametersPage.getNumberTimestepsToIgnore();
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean isGARun() { 
		return modelHandler.getIntelliMethodPlugin() != null;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setOptimizationDirection(final FitnessFunctionDirection direction) {
		this.optimizationDirection = direction;
	}
	
	public void interruptRun(){
		modelHandler.interruptRun();
		isRunning = false;
	}
	
//	public String getRecorderFileName(){
//		if (modelHandler != null){
//			return modelHandler.getRecorderFile().getAbsolutePath();
//		}
//		
//		return null;
//	}
	
	public IModelHandler getModelHandler(){
		return modelHandler;
	}
	
	public boolean isRunning(){
		return isRunning;
	}
	
	/**
	 * @return the onLineCharts
	 */
	public boolean isOnLineCharts() {
		return onLineCharts;
	}

	/**
	 * @param onLineCharts the onLineCharts to set
	 */
	public void setOnLineCharts(boolean onLineCharts) {
		this.onLineCharts = onLineCharts;
	}

	/**
	 * @return the displayAdvancedCharts
	 */
	public boolean isDisplayAdvancedCharts() {
		return displayAdvancedCharts;
	}

	/**
	 * @param displayAdvancedCharts the displayAdvnacedCharts to set
	 */
	public void setDisplayAdvancedCharts(boolean displayAdvancedCharts) {
		this.displayAdvancedCharts = displayAdvancedCharts;
	}
	

	@Override
   public void setTitle(final String title){
		if (title != null){
			super.setTitle(TITLE_PREFIX + " -- " + title);
		} else {
			super.setTitle(TITLE_PREFIX);
		}
	}
	
	public void startTimer(){
		startOfTimer = System.currentTimeMillis();
	}
	
	public long stopTimer(){
		return System.currentTimeMillis() - startOfTimer;
	}
	
	public Style getCssStyle(){
		return cssStyle;
	}
	
	public static void main(String[] args) {
		// this would turn all System.out.println calls to log4 calls
		// on the cost of increased (doubled) runtime
		//SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		
		// the MEME classes assume the platfrom type to be set
		PlatformSettings.setSelectedPlatform(PlatformType.MASON);
		JFrame.setDefaultLookAndFeelDecorated(true);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
         public void run() {
				try {
//					SubstanceOfficeSilver2007LookAndFeel substanceOfficeSilver2007LookAndFeel = new SubstanceOfficeSilver2007LookAndFeel();
//					UIManager.setLookAndFeel(substanceOfficeSilver2007LookAndFeel);
//					UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel());
//					UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
//					UIManager.setLookAndFeel("eu.crisis_economics.abm.dashboard.DLookAndFeel");
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					// if nimbus is present, use it
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
						if ("Nimbus".equals(info.getName())){
							UIManager.setLookAndFeel(info.getClassName());
						}
					}
					
				} catch (Exception e) {
					log.error("Substance failed to initialize", e);
//					System.out.println("Substance Graphite failed to initialize");
				}
				
				String workspaceString = Dashboard.preferences.get(WORKSPACE_PREFERENCE_NODE, null);
				
				if (workspaceString == null){
//					JFileChooser fileChooser = new JFileChooser();
//					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//					fileChooser.setMultiSelectionEnabled(false);
//					//fileChooser.setDialogTitle("Please select a workspace directory!");
//					
//					final JDialog dialog = new JDialog((JDialog)null, "Please select a workspace directory!", true);
//					Box box = new Box(BoxLayout.PAGE_AXIS);
//					box.setAlignmentX(CENTER_ALIGNMENT);
//					
//					box.add(Box.createVerticalStrut(WORKSPACE_DIALOG_MESSAGE_MARGIN));
//					JLabel label = new JLabel("<html>&nbsp;&nbsp;&nbsp;<b>Please select a directory to store all data generated during the simulations (results, logs, etc.)!</b>&nbsp;&nbsp;&nbsp;</html>");
//					label.setAlignmentX(CENTER_ALIGNMENT);
//					box.add(label);
//					box.add(Box.createVerticalStrut(WORKSPACE_DIALOG_MESSAGE_MARGIN));
//					box.add(fileChooser);
//					
//					dialog.add(box);
//					
//					fileChooser.addActionListener(new ActionListener() {
//						
//						@Override
//						public void actionPerformed(ActionEvent e) {
//							if (e.getActionCommand().equals("CancelSelection")){
//								workspace = null;
//							}
//							if (e.getActionCommand().equals("ApproveSelection")){
//								workspace = ((JFileChooser)e.getSource()).getSelectedFile();
//							}
//							dialog.setVisible(false);
//						}
//					});
//					
//					dialog.pack();
//					dialog.setVisible(true);

					WorkspaceSelectionDialog workspaceSelectionDialog = new WorkspaceSelectionDialog(null);
					workspace = workspaceSelectionDialog.display();
				} else {
					workspace = new File(workspaceString);
				}
				
				if (workspace == null){
					System.exit(0);
				} else {
					Dashboard.preferences.put(WORKSPACE_PREFERENCE_NODE, workspace.getAbsolutePath());
				}
				
				Dashboard w = new Dashboard(workspace);
				
				w.pack();
				w.setVisible(true);
				w.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
		});
	}
	
	//-------------------------------------------------------------------------------
	/** Shows a file dialog and returns the selected file object.
	 * @param open true if dialog is for open file, false if it is for save to file 
	 * @param file the initial selected file (null if there is not)
	 * @param filter an filter object (null is permitted)
	 * @return the selected file 
	 */
	private File fileDialog(boolean open, File file, FileFilter filter) {
		File ans = null;
		final JFileChooser chooser = new JFileChooser(file == null ? workspace : file);
		if (file != null && !file.isDirectory())
			chooser.setSelectedFile(file);
		chooser.setAcceptAllFileFilterUsed(true);
		if (filter != null) 
			chooser.addChoosableFileFilter(filter);
		int result = -1;
		if (open)
			result = chooser.showOpenDialog(this);
		else
			result = chooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) 
			ans = chooser.getSelectedFile();
		
		return ans;
	}
}
