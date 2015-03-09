/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.dashboard.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterNode;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.internal.platform.InfoConverter;
import ai.aitia.meme.paramsweep.internal.platform.PlatformSettings;
import ai.aitia.meme.paramsweep.platform.PlatformManager.PlatformType;
import ai.aitia.meme.utils.Utils.Pair;
import eu.crisis_economics.abm.dashboard.AvailableParameter;
import eu.crisis_economics.abm.dashboard.ModelClassPool;
import eu.crisis_economics.abm.dashboard.ParameterInATree;
import eu.crisis_economics.abm.dashboard.ParameterTreeUtils;
import eu.crisis_economics.abm.dashboard.generated.GeneralParameter;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ModelType;

public class SimulationRunner {
	
	//====================================================================================================
	// members
	
	private static final String LOG_FILE_NAME_PREFIX = "output_";
	private static final String LOG_FILE_EXTENSION = ".log";
	private static final String GENERATED_DIR = "generated";
//	private static final String NUMBER_OF_TURNS_GENERAL_PARAMETER_KEY = "Maximum number of time-steps";
	
	private File configFile;
	private File workspace;
	private long runNumber;
	private boolean quiet;
	
	private Model modelConfig;
	private File generatedModelDir;
	private PrintStream console;
	private ClusterModelHandler modelHandler;
	
	//====================================================================================================
	// methods

	//----------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: SimulationRunner <configuration-file-path> <run-number> <workspace>");
			return;
		}
	
		int i = 0;
		boolean quiet = false;
		if ("-q".equals(args[i])) {
			quiet = true;
			i++;
		}
		
		final File config = new File(args[i++]);
		if (!config.exists()) {
			System.err.println("Config file " + args[i - 1] + " not found.");
			System.exit(-1);
		}
		
		long runNumber = -1;
		try {
			runNumber = Long.parseLong(args[i++]);
			if (runNumber < 1) 
				throw new NumberFormatException();
		} catch (final NumberFormatException _) {
			System.err.println("Invalid run number: " + args[i - 1] + ". Please specify a positive integer.");
			System.exit(-2);
		}
		
		final File workspace = new File(args[i++]);
		if (!workspace.exists())
			workspace.mkdirs();
		
		if (!workspace.isDirectory()) {
			System.err.println("Workspace " + args[i - 1] + " is not a directory.");
			System.exit(-3);
		}
		
		final SimulationRunner runner = new SimulationRunner(config,runNumber,workspace, quiet);
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (runner.console != null) 
						runner.console.close();
				}
			});
			runner.init();
			runner.run();
		} catch (final JAXBException e) {
			System.err.println("Invalid config file " + config + ".");
			e.printStackTrace();
			System.exit(-4);
		} catch (final FileNotFoundException e) {
			System.err.println("Can't create log file.");
			e.printStackTrace();
			System.exit(-5);
		} catch (final ClassNotFoundException e) {
			System.err.println("Could not load the model class!");
			e.printStackTrace();
			System.exit(-6);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public SimulationRunner(final File configFile, final long runNumber, final File workspace, final boolean quiet) {
		this.configFile = configFile;
		this.runNumber = runNumber;
		this.workspace = workspace;
		this.quiet = quiet;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void init() throws JAXBException, FileNotFoundException, ClassNotFoundException {
		PlatformSettings.setSelectedPlatform(PlatformType.MASON);
		
		final JAXBContext context = JAXBContext.newInstance(Model.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		modelConfig = (Model) unmarshaller.unmarshal(configFile);
			
		generatedModelDir =  new File(workspace + File.separator + GENERATED_DIR + File.separator + modelConfig.getClazz() + File.separator +
									  new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(new Date()) + File.separator + runNumber);

		if (!generatedModelDir.exists())
			generatedModelDir.mkdirs();
		generatedModelDir.setWritable(true);
		
		if (quiet) {
			console = new PrintStream(new OutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					// silently discard output
				}
			});
		} else {
			console = new PrintStream(new File(workspace,LOG_FILE_NAME_PREFIX + runNumber + LOG_FILE_EXTENSION));
		}
		System.setOut(console);
		System.setErr(console);
		
		initModelHandler();
		
		final boolean hasRecorder = modelHandler.hasRecorder();
		
		if (!hasRecorder) {
			System.err.println("The model does not have '@RecorderSource' annotation");
			System.exit(-7);
		}
		
		try {
			final ParameterTree tree = createParameterTreeFromConfig();
			modelHandler.setParameters(tree);
		} catch (ModelInformationException e) {
			e.printStackTrace();
			System.exit(-8);
		}
	}

	//----------------------------------------------------------------------------------------------------
	public void run() {
		try {
			String numberOfTurnsStr = "100";
			String fitnessFunctionName = null;
			String fitnessStatisticsName = null;
			int fitnessTimeSeriesLength = 0;
			final List<GeneralParameter> generalParameters = modelConfig.getGeneralParameterList();
			for (final GeneralParameter gp : generalParameters) {
				if (ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME.equals(gp.getName())) {
					numberOfTurnsStr = gp.getValue();
				} else if (ParameterTreeUtils.FITNESS_FUNCTION_XML_NAME.equals(gp.getName())) {
					fitnessFunctionName = gp.getValue();
				} else if (ParameterTreeUtils.FITNESS_STATISTICS_XML_NAME.equals(gp.getName())) {
					fitnessStatisticsName = gp.getValue();
				} else if (ParameterTreeUtils.FITNESS_TIMESERIES_LENGTH_XML_NAME.equals(gp.getName())) {
					fitnessTimeSeriesLength = Integer.parseInt(gp.getValue());
				}
			}
			
			if (fitnessFunctionName != null) {
				modelHandler.setFitnessFunction(fitnessFunctionName, fitnessStatisticsName, fitnessTimeSeriesLength);
			}
		
			final double numberOfTurns = Double.parseDouble(numberOfTurnsStr);
			modelHandler.runModel(numberOfTurns,workspace);
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			System.exit(-9);
		}

	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private void initModelHandler() throws ClassNotFoundException {
		final ModelClassPool classPool = new ModelClassPool();
		modelHandler = new ClusterModelHandler(modelConfig.getClazz(),classPool,runNumber,generatedModelDir);
	}
	
	//----------------------------------------------------------------------------------------------------
	private ParameterTree createParameterTreeFromConfig() throws ModelInformationException {
		final ModelType runStrategy = modelConfig.getRunStrategy();
		return runStrategy == ModelType.SINGLE ? createSingleParameterTreeFromConfig() : createPSParameterTreeFromConfig();
	}

	//----------------------------------------------------------------------------------------------------
	private ParameterTree createSingleParameterTreeFromConfig() throws ModelInformationException {
		final DefaultMutableTreeNode infoValueTree = ParameterTreeUtils.loadSingleRunParameterIntoInfoValueTree(modelConfig,modelHandler.getParameters(),modelHandler);

		final ParameterTree parameterTree = new ParameterTree();
		@SuppressWarnings("rawtypes")
		final Enumeration treeValues = infoValueTree.breadthFirstEnumeration();
		treeValues.nextElement(); // root element
		while (treeValues.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeValues.nextElement();
			@SuppressWarnings("unchecked")
			final Pair<ParameterInfo,String> userData = (Pair<ParameterInfo,String>) node.getUserObject();
			final ParameterInfo parameterInfo = userData.getFirst();
			final String value = userData.getSecond();
			
			if (parameterInfo instanceof SubmodelInfo) {
				// we don't need the SubmodelInfo parameters anymore (all descendant parameters are in the tree too)
				// but we need to check that an actual type is provided
				final SubmodelInfo smi = (SubmodelInfo) parameterInfo;
				
				if (smi.getActualType() == null) 
					throw new ModelInformationException("Invalid type null for parameter " + smi.getName() + ".");
				
				continue;
			} else {
				parameterInfo.setValue(ParameterInfo.getValue(value,parameterInfo.getType()));
				if ("String".equals(parameterInfo.getType()) && parameterInfo.getValue() == null)
					parameterInfo.setValue(value.trim());
			}
			
			final AbstractParameterInfo<?> batchParameterInfo = InfoConverter.parameterInfo2ParameterInfo(parameterInfo);
			parameterTree.addNode(batchParameterInfo);
		}
		
		return parameterTree;
	}
	
	//----------------------------------------------------------------------------------------------------
	private ParameterTree createPSParameterTreeFromConfig() throws ModelInformationException {
		final DefaultListModel defaultParametersListModel = new DefaultListModel();
		final List<DefaultTreeModel> combinationModels = new ArrayList<DefaultTreeModel>(modelConfig.getCombination().size());
		
		ParameterTreeUtils.loadPSParameters(defaultParametersListModel,combinationModels,modelHandler,modelConfig);
		
		final String invalidInfoName = checkInfos(defaultParametersListModel,combinationModels); 
		if (invalidInfoName != null) 
			throw new ModelInformationException("Invalid type null for parameter " + invalidInfoName + ".");
			
		final ParameterTree result = new ParameterTree();
		
		for (int i = 0;i < defaultParametersListModel.getSize();++i) {
			final AvailableParameter param = (AvailableParameter) defaultParametersListModel.get(i);
			final AbstractParameterInfo<?> batchParameter = InfoConverter.parameterInfo2ParameterInfo(param.info);
			ParameterTreeUtils.addConstantParameterToParameterTree(batchParameter,result,modelHandler);
		}
	
		for (final DefaultTreeModel treeModel : combinationModels) {
			final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.breadthFirstEnumeration();
			nodes.nextElement(); // root;
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				ParameterTreeUtils.filterConstants(userObj,result,modelHandler);
			}
			
		}
		
		for (final DefaultTreeModel treeModel : combinationModels) {
			final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
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
	private String checkInfos(final DefaultListModel listModel, final List<DefaultTreeModel> treeModels) {
		for (int i = 0;i < listModel.getSize();++i) {
			final AvailableParameter param = (AvailableParameter) listModel.get(i);
			final String invalid = checkSubmodelInfo(param);
			if (invalid != null)
				return invalid;
		}
		
		for (final DefaultTreeModel treeModel : treeModels) {
			final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.breadthFirstEnumeration();
			nodes.nextElement(); // root
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterInATree userObj = (ParameterInATree) node.getUserObject();
				final String invalid = checkSubmodelInfo(userObj);
				if (invalid != null)
					return invalid;
			}
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private String checkSubmodelInfo(final AvailableParameter parameter) {
		if (parameter.info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) parameter.info;
			if (sInfo.getActualType() == null)
				return sInfo.getName().replaceAll("([A-Z])"," $1");
		}
		
		return null;
	}
}