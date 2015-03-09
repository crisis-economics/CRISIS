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

import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import ai.aitia.meme.paramsweep.batch.BatchEvent;
import ai.aitia.meme.paramsweep.batch.BatchEvent.EventType;
import ai.aitia.meme.paramsweep.batch.BatchException;
import ai.aitia.meme.paramsweep.batch.IBatchController;
import ai.aitia.meme.paramsweep.batch.IHierarchicalModelInformation;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.intellisweep.IIntelliSweepBatchListener;
import ai.aitia.meme.paramsweep.batch.intellisweep.IntelliSweepBatchEvent;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.batch.param.SubmodelInfo;
import ai.aitia.meme.paramsweep.platform.IPSWInformationProvider;
import ai.aitia.meme.paramsweep.platform.mason.MasonPlatform;
import ai.aitia.meme.paramsweep.platform.mason.impl.IRecorderListenerAware;
import ai.aitia.meme.paramsweep.platform.mason.impl.MasonModelInformation;
import ai.aitia.meme.paramsweep.platform.mason.impl.MasonRecorderListener;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.OutputTime;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.RecordTime;
import ai.aitia.meme.paramsweep.plugin.IIntelliDynamicMethodPlugin;
import eu.crisis_economics.abm.dashboard.ga.DynamicBatchController;
import eu.crisis_economics.abm.dashboard.ga.IWorkspaceAware;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author Tamás Máhr
 * 
 */
public class ModelHandler implements IPSWInformationProvider, IModelHandler {

	private static final String LOG_FILE_NAME = "output.log";

	/**
	 * @author Tamás Máhr
	 * 
	 */
	public interface CompletionHandler {
		public void completed(Throwable simulationException);
	}
	
	protected boolean interruptRun = false;

	/**
	 * The class cache that stores classes already loaded by javassist.
	 */
	Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	private CtClass modelClass;

	private IHierarchicalModelInformation modelInformation;
	
	private ModelClassPool classPool;

	protected ParameterTree parameters;
	protected MasonPlatform masonPlatform;

	private double numberOfRequiredTurns = 100;
	
	protected IBatchController batchController = null;

	protected File workDir;
	private File outFile;
	
	protected List<File> recorderFiles = new ArrayList<File>();
	protected List<RecorderInfo> recorders;
	
	protected IIntelliDynamicMethodPlugin plugin;

	public ModelHandler(final String modelName, final ModelClassPool classPool) throws ClassNotFoundException {
		try {
			
			this.classPool = classPool;
			modelClass = classPool.getCtClass(modelName);

			modelInformation = new MasonModelInformation(this, modelClass);
//			Class<?> modelInformationClass = Class.forName("ai.aitia.meme.paramsweep.platform.mason.impl.MasonModelInformation", true, getCustomClassLoader());
//			Class<?> providerClass = Class.forName("ai.aitia.meme.paramsweep.platform.IPSWInformationProvider", true, getCustomClassLoader());
//			Class<?> javassistClass = Class.forName("javassist.CtClass", true, getCustomClassLoader());
//			Constructor<?> constructor = modelInformationClass.getConstructor(IPSWInformationProvider.class, CtClass.class);
//			modelInformation = (IModelInformation) constructor.newInstance(this, modelClass);
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
//		} catch (NoSuchMethodException e) {
//			throw new IllegalStateException(e);
//		} catch (IllegalArgumentException e) {
//			throw new IllegalStateException(e);
//		} catch (InstantiationException e) {
//			throw new IllegalStateException(e);
//		} catch (IllegalAccessException e) {
//			throw new IllegalStateException(e);
//		} catch (InvocationTargetException e) {
//			throw new IllegalStateException(e);
		}


		masonPlatform = new MasonPlatform();
	}
	
	//----------------------------------------------------------------------------------------------------
	ModelHandler(final String modelName, final ModelClassPool classPool, final Map<String,Class<?>> classCache) throws ClassNotFoundException {
		this(modelName,classPool);
		this.classCache = classCache;
	}

	@Override
	public String getModelRoot() {
//		return getClassPool().find(modelClass.getName()).getPath().replace("/" + modelClass.getName().replace('.', '/') + ".class", "");
		return workDir.getAbsolutePath(); // We use this method to pass destination information to the model generator
	}
	
	protected String getOriginalModelRoot() {
		return getClassPool().find(modelClass.getName()).getPath().replace("/" + modelClass.getName().replace('.', '/') + ".class", "");
	}
	
	
//	String path = getClassLoader().getResource(getClass().getName().replace('.', '/') + ".class").getPath();
//	modelRoot = path.substring(0, path.lastIndexOf(getClass().getName().replace('.', '/')) - 1);

	@Override
	public String getEntryPoint() {
		try {
			return getModelFile().toURI().toURL().getPath();
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isLocalRun() {
		return true;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public File getModelFile() {
		try {
			return new File(modelClass.getURL().toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ClassLoader getCustomClassLoader() {
		return classPool.getClassLoader();
	}

	@Override
	public Map<String, Class<?>> getClassCache() {
		return classCache;
	}

	@Override
	public ClassPool getClassPool() {
		return classPool;
	}

	@Override
	public DefaultListModel getClassPathListModel() {
		return new DefaultListModel();
	}

	@Override
	public boolean rngSeedAsParameter() {
		return true;
	}

	@Override
	public boolean writeSource() {
		return true;
	}

	@Override
	public List<RecorderInfo> getRecorders() {
		if (recorders == null){
			try {
				recorders = modelInformation.getRecorders();
			} catch (ModelInformationException e) {
				throw new IllegalStateException(e);
			}
		}

		return recorders;
	}

	@Override
	public String getStoppingCondition() {
		return String.valueOf(numberOfRequiredTurns);
	}

	@Override
	public boolean isNumberStoppingCondition() {
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<AbstractParameterInfo> getNewParameters() {
		return new ArrayList<AbstractParameterInfo>();
	}

	@Override
	public void setGeneratedModelName(String generatedModelName) {
		// this is left intentionally empty
	}

	@Override
	public void setAutomaticResources(List<String> resources) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getAutomaticResources() {
		throw new UnsupportedOperationException();
	}

	@Override
   @SuppressWarnings("unchecked")
	public Class<? extends Simulation> getModelClass() {
		Class<? extends Simulation> mClass = (Class<? extends Simulation>) classCache.get(modelClass.getName());
		if (mClass == null) {
			try {
				modelClass.stopPruning(true);
				mClass = modelClass.toClass();
				modelClass.defrost();
				classCache.put(modelClass.getName(), mClass);
			} catch (CannotCompileException e) {
				throw new IllegalStateException(e);

			}
		}
		return mClass;
	}

	@Override
   public String getRecorderAnnotationValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException{
		@SuppressWarnings("unchecked")
		Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName("ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder", true, getCustomClassLoader());
		Method valueMethod = annotationClass.getMethod("value");
		return (String) valueMethod.invoke(getModelClass().getAnnotation(annotationClass));
	}
	@Override
   public void setParameters(final ParameterTree parameters){
		this.parameters = parameters;
	}
	
	public ParameterTree getParameterTree() {
		return parameters;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setIntelliMethodPlugin(final IIntelliDynamicMethodPlugin plugin) { this.plugin = plugin; }
	public IIntelliDynamicMethodPlugin getIntelliMethodPlugin() { return plugin; }
	
	//----------------------------------------------------------------------------------------------------
	@Override
   public boolean hasRecorder() {
		if (recorderFiles.size() > 0) return true;
		else {
			try {
				getRecorderAnnotationValue();
				return true;
			} catch (final Exception _) {
				return false;
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
   public File getRecoderFile() {
		if (recorderFiles.size() > 0)
			return recorderFiles.get(0);
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
   public IBatchController getBatchController() { return batchController; }
	
	//----------------------------------------------------------------------------------------------------
	@Override
   public void runModel(final double numberOfTurns, final File workDir, final PropertyChangeListener propertyChangeListener, final MasonRecorderListener recorderListener, final CompletionHandler completionHandler) {

		if (!workDir.exists()){
			workDir.mkdirs();
		}
		
		// best effort, this does not work on windows
		workDir.setWritable(true);
		
		this.workDir = workDir;
		this.outFile = new File(workDir, LOG_FILE_NAME);
		
		SwingWorker<Void, Integer> swingWorker = new SwingWorker<Void, Integer>() {

			private Throwable simulationException = null;
			
			@Override
			protected Void doInBackground() throws Exception {

				PrintStream outStream = System.out;
				PrintStream errStream = System.err;
				PrintStream logStream = null;
				
				recorderFiles.clear();
				getRecorders();
				for (RecorderInfo recorderInfo : recorders) {
					File outputFile = recorderInfo.getOutputFile();
					File newFile = new File(workDir, outputFile.getName());
					recorderInfo.setOutputFile(newFile);
					recorderInfo.setName(newFile.getAbsolutePath());
					recorderFiles.add(newFile);
				}
				
				final List<RecorderInfo> originalRecorders = getRecorders();
				try {
					setNumberOfRequiredTurns(numberOfTurns);
					
					// this should be called before prepareModel to properly initialize the recorder file path
					String error = masonPlatform.checkModel(ModelHandler.this);
					if (error != null){
						throw new IllegalStateException(error);
					}
					
					recorders = sortOutRecordersIfNecessary(getRecorders());
					
					error = masonPlatform.prepareModel(ModelHandler.this);
					if (error != null){
						throw new IllegalStateException(error);
					}
//					long nofRuns = Util.calculateNumberOfRuns(parameters, false);
					batchController = plugin != null ? new DynamicBatchController(plugin,masonPlatform,workDir) : masonPlatform.getBatchController();
					
					if (plugin instanceof IWorkspaceAware) {
						final IWorkspaceAware workspaceAwarePlugin = (IWorkspaceAware) plugin;
						workspaceAwarePlugin.setWorkspace(workDir);
					}
					
					batchController.setParameters(parameters);
					batchController.setRecorders(recorders);
					batchController.setModelPath(getOriginalModelRoot());
					batchController.setEntryPoint(getEntryPoint());
					batchController.addBatchListener(new IIntelliSweepBatchListener() {
						
						public void iterationChanged(final IntelliSweepBatchEvent event) {
							// TODO Auto-generated method stub
							
						}
						
						public void timeProgressed(final BatchEvent event) {
							// if the experiment has been interrupted
							if (batchController == null){
								return;
							}
							long nofRuns = batchController.getNumberOfRuns();
							if (nofRuns == 1 && EventType.STEP_ENDED.equals(event.getEventType())) {
								long iteration = -1;
								if (event instanceof IntelliSweepBatchEvent)
									iteration = ((IntelliSweepBatchEvent)event).getIteration();
								
								final int progress = (int)(event.getNumber() / numberOfTurns * 100 + 0.5);
								setProgressAndIteration(progress,iteration);
							}
						}
						
						public void runChanged(final BatchEvent event) {
							// if the experiment has been interrupted
							if (batchController == null){
								return;
							}
							long nofRuns = batchController.getNumberOfRuns();
							if (nofRuns > 1 && EventType.RUN_ENDED.equals(event.getEventType())) {
								long iteration = -1;
								if (event instanceof IntelliSweepBatchEvent)
									iteration = ((IntelliSweepBatchEvent)event).getIteration();
								
								final int progress = (int)(event.getNumber() / nofRuns * 100 + 0.5);
								setProgressAndIteration(progress,iteration);
							}
						}
						
						public void softCancelled(final BatchEvent event) {}
						public void hardCancelled(final BatchEvent event) {}
						public void batchEnded(final BatchEvent event) {}
						
						private void setProgressAndIteration(final int progress, final long iteration) {
							final Map<String,Number> newValues = new HashMap<String,Number>();
							newValues.put("progress",progress);
							newValues.put("iteration",iteration);
							
							getPropertyChangeSupport().firePropertyChange("progressAndIteration",null,newValues);
						}
					});
					if (recorderListener != null){
						((IRecorderListenerAware)batchController).addRecorderListener(recorderListener);
					}
					
					logStream = new PrintStream(outFile);
					System.setOut(logStream);
					System.setErr(logStream);
					
//					int numberOfRuns = batchController.getNumberOfRuns();
					batchController.startBatch();
					
				} catch (Throwable e) {
					Logger log = Logger.getLogger(getClass());
					log.error("Exception during executing simulation", e);
					simulationException = e;
				} finally {
					recorders = originalRecorders;
					System.setOut(outStream);
					System.setErr(errStream);
					
					if (logStream != null){
						logStream.close();
					}
				}
				
				return null;
			}
			

			@Override
			protected void done() {
				if (!interruptRun) {
					completionHandler.completed(simulationException);
				}
				interruptRun = false;
				batchController = null;
			}
		};

		swingWorker.addPropertyChangeListener(propertyChangeListener);

		swingWorker.execute();
	}

	protected List<RecorderInfo> sortOutRecordersIfNecessary(final List<RecorderInfo> recorders) {
		if (plugin == null)
			return recorders;
		
		final List<RecorderInfo> result = new ArrayList<RecorderInfo>(1);
		for (final RecorderInfo recorderInfo : recorders) {
			final RecordableInfo fitnessFunction = ((GASearchPanelModel) plugin).getSelectedFitnessStatisticsInfo();
			

			if (fitnessFunction != null) {
				final RecorderInfo newRecorder = new RecorderInfo();
				newRecorder.setName(recorderInfo.getName());
				newRecorder.setRecordType(RecordTime.END_OF_RUN.getMemeValue());
				newRecorder.setWriteType(OutputTime.SAME_AS_RECORD_TIME.getMemeValue());
				newRecorder.setDelimiter(recorderInfo.getDelimiter());
				newRecorder.setOutputFile(recorderInfo.getOutputFile());
				newRecorder.setRecordables(Arrays.asList(fitnessFunction));
				result.add(newRecorder);
				break;
			}
		}
		
		return result;
	}


	@Override
   public void interruptRun() {
		interruptRun = true;
		try {
			batchController.stopBatch();
		} catch (BatchException e) {
			throw new IllegalArgumentException(e);
		} finally {
			batchController = null;
		}
	}

	@Override
   public List<ParameterInfo<?>> getParameters() throws ModelInformationException {
		return modelInformation.getParameters();
	}
	
	@Override
   public List<ParameterInfo<?>> getSubparameters(final SubmodelInfo<?> submodelInfo) throws ModelInformationException {
		return modelInformation.getSubmodelParameters(submodelInfo); 
	}
	
	public void setNumberOfRequiredTurns(final double numberOfRequiredTurns){
		this.numberOfRequiredTurns = numberOfRequiredTurns;
	}
	
	@Override
   public InputStream getSettingsXMLInputStream(){
		try {
			return new ByteArrayInputStream(modelInformation.getRecordersXML().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (ModelInformationException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public String getModelClassSimpleName(){
		return modelClass.getSimpleName();
	}
}
