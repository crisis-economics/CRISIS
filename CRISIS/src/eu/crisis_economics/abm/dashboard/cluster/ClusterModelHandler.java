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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import ai.aitia.meme.paramsweep.batch.IClusterBatchController;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.platform.mason.impl.MasonRecorderListener;
import eu.crisis_economics.abm.dashboard.GASearchHandler;
import eu.crisis_economics.abm.dashboard.ModelClassPool;
import eu.crisis_economics.abm.dashboard.ModelHandler;

public class ClusterModelHandler extends ModelHandler {
	
	//====================================================================================================
	// members
	
	protected long runNumber;
	protected File generatedModelDir;
	protected String fitnessFunctionName;
	protected String fitnessStatisticsName;
	protected int fitnessTimeseriesLength;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public ClusterModelHandler(final String modelName, final ModelClassPool classPool, final long runNumber, final File generatedModelDir)
																																		throws ClassNotFoundException {
		super(modelName,classPool);
		this.runNumber = runNumber;
		this.generatedModelDir = generatedModelDir;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void runModel(final double numberOfTurns, final File workDir) {
		runModel(numberOfTurns,workDir, null,null,null);
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public String getModelRoot() {
		return generatedModelDir.getAbsolutePath(); // We use this method to pass destination information to the model generator
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public void runModel(final double numberOfTurns, final File workDir, final PropertyChangeListener _, final MasonRecorderListener __, final CompletionHandler ___) {
		this.workDir = workDir; 
		
		recorderFiles.clear();
		getRecorders();
		for (RecorderInfo recorderInfo : recorders) {
			final File outputFile = recorderInfo.getOutputFile();
			final File newFile = new File(workDir,changeFileName(outputFile.getName()));
			recorderInfo.setOutputFile(newFile);
			recorderInfo.setName(newFile.getAbsolutePath());
			recorderFiles.add(newFile);
		}
				
		try {
			setNumberOfRequiredTurns(numberOfTurns);
					
			// if fitnessFunction is specified, set plugin to allow sortOutRecordersIfNecessary() to replace the recorder
			if (fitnessFunctionName != null) {
				GASearchHandler searchHandler = new GASearchHandler(this);
				final List<RecordableInfo> recordables = getRecorders().get(0).getRecordables();
				for (final RecordableInfo recordableInfo : recordables) {
					if (searchHandler.canBeFitnessFunction(recordableInfo))
						searchHandler.addFitnessFunction(recordableInfo);
				}
				
				searchHandler.setSelectedFitnessFunction(fitnessFunctionName);
				searchHandler.setSelectedFitnessStatistics(fitnessStatisticsName);
				searchHandler.setFitnessTimeSeriesLength(fitnessTimeseriesLength);
				
				plugin = searchHandler;
			}
			recorders = sortOutRecordersIfNecessary(getRecorders());

			// this should be called before prepareModel to properly initialize the recorder file path
			String error = masonPlatform.checkModel(this);
			if (error != null) 
				throw new IllegalStateException(error);
					
			error = masonPlatform.prepareModel(this); // model generation happens here, recorder manipulation should happen earlier
			if (error != null)
				throw new IllegalStateException(error);
			
			batchController = masonPlatform.getBatchController();
			((IClusterBatchController)batchController).setOneRunOnly(true);
			((IClusterBatchController)batchController).setSkipCount(runNumber - 1);
			batchController.setParameters(parameters);
			
			
			//batchController.setRecorders(recorders); // this has no effect in MasonBatchController
			batchController.setModelPath(getOriginalModelRoot());
			batchController.setEntryPoint(getEntryPoint());
			batchController.startBatch();
			interruptRun = false;
			batchController = null;
		} catch (final Throwable e) {
			e.printStackTrace();
			System.exit(-100);
		}
	}
	
	public void setFitnessFunction(final String fitnessFunctionName, final String fitnessStatisticsName, final int fitnessTimeseriesLength) {
		this.fitnessFunctionName = fitnessFunctionName;
		this.fitnessStatisticsName = fitnessStatisticsName;
		this.fitnessTimeseriesLength = fitnessTimeseriesLength;
	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private String changeFileName(final String original) {
		if (original == null) return null;
		
		String newName = original;
		final int idx = original.lastIndexOf('.');
		if (idx > 0)
			newName = original.substring(0,idx) + "_" + runNumber + original.substring(idx);
		else
			newName = original + "_" + runNumber;
		
		return newName;
	}
}