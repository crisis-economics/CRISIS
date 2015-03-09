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
package eu.crisis_economics.abm.dashboard.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.internal.platform.InfoConverter;
import ai.aitia.meme.paramsweep.platform.mason.MasonPlatform;
import ai.aitia.meme.paramsweep.platform.repast.impl.IntelliSweepRepastResultParser;
import ai.aitia.meme.paramsweep.plugin.IIntelliContext;
import ai.aitia.meme.paramsweep.utils.Util;
import eu.crisis_economics.abm.dashboard.GASearchHandler;
import eu.crisis_economics.abm.dashboard.ModelClassPool;
import eu.crisis_economics.abm.dashboard.ModelHandler;
import eu.crisis_economics.abm.dashboard.ParameterTreeUtils;
import eu.crisis_economics.abm.dashboard.cluster.annotation.ClusterRunner;
import eu.crisis_economics.abm.dashboard.cluster.script.Scheduler;
import eu.crisis_economics.abm.dashboard.cluster.script.SchedulerException;
import eu.crisis_economics.abm.dashboard.ga.DashboardIntelliContext;
import eu.crisis_economics.abm.dashboard.generated.GeneralParameter;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ModelType;
import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;

/**
 * @author Tamás Máhr
 *
 */
@Service
@ClusterRunner(ModelType.GA)
public class GAClusterRunner implements Runner {

	private static final String GENERATION_DIRECTORY_PREFIX = "gen-";

	@Autowired
	protected Model model;
	
	@Autowired
	private ApplicationContext context;
	
	private Logger log = LoggerFactory.getLogger(GAClusterRunner.class);
	
	public String toString(){
		return "GA cluster runner.";
	}

	@Autowired
	protected Environment env;

	@Autowired
	protected Scheduler scheduler;
	
//	protected String configFile;

	protected String timeLimit;

	protected GASearchHandler searchHandler;

	protected ModelHandler modelHandler;
	
	protected int generationNumber = 0;

	protected File workDir;
	
	@Override
	public void runExperiment() {
		
		if (generationNumber == 0){
			runFirstGeneration();
		} else {
			runNextGeneration();
		}
	}

	protected void runFirstGeneration() {
		this.timeLimit = env.getProperty("timeLimit");
		try {
			// set up the model and ga-search handlers
			modelHandler = new ModelHandler(model.getClazz(), new ModelClassPool());
			List<ParameterInfo<?>> parameters = modelHandler.getParameters(); // we have to call getParameters() before getRecorders().get(0).getRecordables()

			searchHandler = new GASearchHandler(modelHandler);
			final List<RecordableInfo> recordables = modelHandler.getRecorders().get(0).getRecordables();
			for (final RecordableInfo recordableInfo : recordables) {
				if (searchHandler.canBeFitnessFunction(recordableInfo))
					searchHandler.addFitnessFunction(recordableInfo);
			}

			searchHandler.loadConfigurationFromBatchInfo(model, parameters);
			
			final DefaultMutableTreeNode parameterTreeRootNode = new DefaultMutableTreeNode();
			final IIntelliContext ctx = new DashboardIntelliContext(parameterTreeRootNode,searchHandler.getChromosomeTree());
			searchHandler.alterParameterTree(ctx);
			ParameterTree parameterTree = InfoConverter.node2ParameterTree(parameterTreeRootNode);

			runTree(parameterTree);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (ModelInformationException e) {
			throw new IllegalStateException(e);
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}
	}

	private void runTree(ParameterTree parameterTree) throws ModelInformationException, JAXBException, PropertyException, SchedulerException {
		// create the paramsweep configuration of the first iteration
		final ObjectFactory factory = new ObjectFactory();
		
		final Model newModel = factory.createModel();
		newModel.setClazz(model.getClazz());
		newModel.setRunStrategy(ModelType.PARAMETER_SWEEP);
		newModel.setNumberOfRuns((int) Util.calculateNumberOfRuns(parameterTree, true));

		List<GeneralParameter> oldParameterList = model.getGeneralParameterList();
		final List<GeneralParameter> generalParameters = newModel.getGeneralParameterList();
		
		GeneralParameter gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME);
		GeneralParameter parameterValue = findGeneralParameterValue(oldParameterList, ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME);
		if (parameterValue != null){
			gp.setValue(parameterValue.getValue());
			generalParameters.add(gp);
		}
		
		gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.NUMBER_OF_TIMESTEPS_TO_IGNORE_XML_NAME);
		parameterValue = findGeneralParameterValue(oldParameterList, ParameterTreeUtils.NUMBER_OF_TIMESTEPS_TO_IGNORE_XML_NAME);
		if (parameterValue != null){
			gp.setValue(parameterValue.getValue());
			generalParameters.add(gp);
		}
		
		gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.FITNESS_FUNCTION_XML_NAME);
		parameterValue = findGeneralParameterValue(oldParameterList, ParameterTreeUtils.FITNESS_FUNCTION_XML_NAME);
		if (parameterValue != null){
			gp.setValue(parameterValue.getValue());
			generalParameters.add(gp);
		}

		gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.FITNESS_STATISTICS_XML_NAME);
		parameterValue = findGeneralParameterValue(oldParameterList, ParameterTreeUtils.FITNESS_STATISTICS_XML_NAME);
		if (parameterValue != null){
			gp.setValue(parameterValue.getValue());
			generalParameters.add(gp);
		}

		gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.FITNESS_TIMESERIES_LENGTH_XML_NAME);
		parameterValue = findGeneralParameterValue(oldParameterList, ParameterTreeUtils.FITNESS_TIMESERIES_LENGTH_XML_NAME);
		if (parameterValue != null){
			gp.setValue(parameterValue.getValue());
			generalParameters.add(gp);
		}

		ParameterTreeUtils.addPSParameterTree2Config(modelHandler.getParameters(), parameterTree, newModel);
		Marshaller marshaller = JAXBContext.newInstance(Model.class).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		
//			File output = new File(new File(configFile).getParent() + File.separator + "new-config.xml");
//			marshaller.marshal(newModel, output);
		
		generationNumber++;
		workDir = new File(GENERATION_DIRECTORY_PREFIX + generationNumber);
		if (!workDir.getAbsoluteFile().mkdir()) {
			String postfix = "";
			if (workDir.exists()){
				postfix = "; directory already exists!";
			}
			throw new IllegalStateException("Could not create directory for generation " + generationNumber + ". (" + workDir.getAbsoluteFile() + ")" + postfix);
		}
		scheduler.runParameterSweep(newModel, timeLimit, workDir.getAbsoluteFile());
		scheduler.runJobEndedSignal();
	}
	
	protected void runNextGeneration() {
		// we need to collect results from separate files
		MasonPlatform masonPlatform = new MasonPlatform();
		
		List<File> mergedResults = masonPlatform.prepareResult(modelHandler.getRecorders(), workDir);
		
		if (mergedResults.size() != 1) {
			throw new IllegalStateException("Could not merge results into one file!");
		}
		
		ParameterTree newTree = searchHandler.getNextParameterTree(new IntelliSweepRepastResultParser(mergedResults.get(0), "|"));

		if (newTree == null) { // this is the end, my only friend, the end
			ArrayList<RecorderInfo> list = new ArrayList<RecorderInfo>();
			// we need to set the delimiter and the file name only
			RecorderInfo recorderInfo = modelHandler.getRecorders().get(0);
			final RecorderInfo newRecorder = new RecorderInfo();
			newRecorder.setDelimiter(recorderInfo.getDelimiter());
			newRecorder.setOutputFile(recorderInfo.getOutputFile());
			list.add(newRecorder);
			context.getBean(ExperimentEndSignaller.class).signalEnd(list, workDir.getAbsoluteFile().getParentFile());
			return;
		}
		
		try {
			runTree(newTree);
		} catch (ModelInformationException e) {
			throw new IllegalStateException(e);
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private GeneralParameter findGeneralParameterValue(final List<GeneralParameter> parameterList, final String name) {
		for (GeneralParameter generalParameter : parameterList) {
			if (generalParameter.getName().equals(name)) {
				return generalParameter;
			}
		}
		
		return null;
	}
}
