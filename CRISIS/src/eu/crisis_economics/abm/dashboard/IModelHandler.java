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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ai.aitia.meme.paramsweep.batch.IBatchController;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.batch.param.SubmodelInfo;
import ai.aitia.meme.paramsweep.platform.mason.impl.MasonRecorderListener;
import ai.aitia.meme.paramsweep.plugin.IIntelliDynamicMethodPlugin;
import eu.crisis_economics.abm.dashboard.ModelHandler.CompletionHandler;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author Tamás Máhr
 *
 */
public interface IModelHandler {

	String getRecorderAnnotationValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException;

	Class<? extends Simulation> getModelClass();

	void interruptRun();

	void runModel(double numberOfRequestedTurns, File workDir, PropertyChangeListener propertyChangeListener, MasonRecorderListener recorderListener, CompletionHandler completionHandler);

	List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> getParameters() throws ModelInformationException;

	void setParameters(ParameterTree parameterTree);
	ParameterTree getParameterTree();
	
	public List<ParameterInfo<?>> getSubparameters(final SubmodelInfo<?> submodelInfo) throws ModelInformationException;
	
	public boolean hasRecorder();
	public File getRecoderFile();
	
	public List<RecorderInfo> getRecorders();
	
	public InputStream getSettingsXMLInputStream();
	
	public ClassLoader getCustomClassLoader();
	
	public String getModelClassSimpleName();
	
	/**
	 * This methods may return the actual batch controller AFTER runModel() is called 
	 */
	public IBatchController getBatchController();
	
	public void setIntelliMethodPlugin(final IIntelliDynamicMethodPlugin plugin);
	public IIntelliDynamicMethodPlugin getIntelliMethodPlugin();
}
