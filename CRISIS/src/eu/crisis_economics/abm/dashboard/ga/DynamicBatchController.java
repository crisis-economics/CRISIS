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
package eu.crisis_economics.abm.dashboard.ga;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ai.aitia.meme.paramsweep.batch.BatchEvent;
import ai.aitia.meme.paramsweep.batch.BatchException;
import ai.aitia.meme.paramsweep.batch.IBatchController;
import ai.aitia.meme.paramsweep.batch.IBatchListener;
import ai.aitia.meme.paramsweep.batch.InvalidEntryPointException;
import ai.aitia.meme.paramsweep.batch.intellisweep.IIntelliSweepBatchListener;
import ai.aitia.meme.paramsweep.batch.intellisweep.IntelliSweepBatchEvent;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.platform.Platform;
import ai.aitia.meme.paramsweep.plugin.IIntelliDynamicMethodPlugin;

public class DynamicBatchController implements IBatchController, IBatchListener {

	//====================================================================================================
	// members
	
	private List<IBatchListener> listeners = new ArrayList<IBatchListener>();
	private IIntelliDynamicMethodPlugin plugin = null;
	private Platform platform = null;

	private IBatchController actBatchController = null;

	private String entryPoint = null;
	private String modelPath = null;
	private List<RecorderInfo> recorders = null;
	private String conditionCode = null;
	
	private boolean interrupted = false;
	private File workspace;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public DynamicBatchController(IIntelliDynamicMethodPlugin plugin, final Platform platform, final File workspace) {
		this.plugin = plugin;
		this.platform = platform;
		this.workspace = workspace;
		this.actBatchController = platform.getBatchController();
		this.actBatchController.addBatchListener(this);
	}

	//====================================================================================================
	// implemented interfaces

	//----------------------------------------------------------------------------------------------------
	public void addBatchListener(IBatchListener listener) { listeners.add(listener); }
	public void removeBatchListener(IBatchListener listener) { listeners.remove(listener); }
	public int getNumberOfRuns() { return actBatchController.getNumberOfRuns(); }
	
	//----------------------------------------------------------------------------------------------------
	public void setEntryPoint(String entryPoint) throws InvalidEntryPointException {
		actBatchController.setEntryPoint(entryPoint);
		this.entryPoint = entryPoint;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setModelPath(String path) {
		actBatchController.setModelPath(path);
		this.modelPath = path;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setParameters(ParameterTree paramTree) { actBatchController.setParameters(paramTree); }
	
	//----------------------------------------------------------------------------------------------------
	public void setRecorders(List<RecorderInfo> recorders) {
		actBatchController.setRecorders(recorders);
		this.recorders = recorders;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setStopCondition(String conditionCode) {
		actBatchController.setStopCondition(conditionCode);
		this.conditionCode = conditionCode;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void stopCurrentRun() throws BatchException {}
	public void startBatch() throws BatchException { actBatchController.startBatch(); }

	//----------------------------------------------------------------------------------------------------
	public void stopBatch() throws BatchException {
		interrupted = true;
		actBatchController.stopBatch();
	}
	
	//====================================================================================================
	// IBatchListener
	
	//----------------------------------------------------------------------------------------------------
	public void hardCancelled(BatchEvent event) {}
	public void softCancelled(BatchEvent event) {}

	//----------------------------------------------------------------------------------------------------
	public void timeProgressed(BatchEvent event) {
		IntelliSweepBatchEvent _event = new IntelliSweepBatchEvent(event.getSource(),event.getEventType(),event.getNumber(),
																   plugin.getCurrentIteration(),plugin.getNumberOfIterations() + " iterations at " +
																   "the most.");
		fireBatchEvent(_event);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void runChanged(BatchEvent event) {
		IntelliSweepBatchEvent _event = new IntelliSweepBatchEvent(event.getSource(),event.getEventType(),event.getNumber(),
																   plugin.getCurrentIteration(),plugin.getNumberOfIterations() + " iterations at the" +
																   " most.");
		fireBatchEvent(_event);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void batchEnded(BatchEvent event) {
		ParameterTree newTree = plugin.getNextParameterTree(platform.getReader(recorders));
		if (plugin.noMoreIteration() || interrupted) {
			renameRecorderFiles();
			platform.prepareResult(recorders,workspace);
			deleteFileParts();
			IntelliSweepBatchEvent _event = new IntelliSweepBatchEvent(event.getSource(),event.getEventType(),0,plugin.getCurrentIteration(),"");
			fireBatchEvent(_event);
		} else {
			IntelliSweepBatchEvent _event = new IntelliSweepBatchEvent(event.getSource(),plugin.getCurrentIteration());
			fireBatchEvent(_event);
			changeBatchController( newTree );
			renameRecorderFiles();
			try {
				actBatchController.startBatch();
			} catch (BatchException e) {
				// never happens at this point: setEntryPoint didn't throw exception in the first iteration
				// TODO: is this true?
				throw new IllegalStateException(e);
			}
		}
	}

	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private void fireBatchEvent(IntelliSweepBatchEvent event) {
		for (IBatchListener listener : listeners) {
			switch (event.getEventType()) {
			case STEP_ENDED 		: listener.timeProgressed(event); break;
			case RUN_ENDED			: listener.runChanged(event); break;
			case BATCH_ENDED 		: listener.batchEnded(event); break;
			case ITERATION_ENDED	: if (listener instanceof IIntelliSweepBatchListener)
									  	  ((IIntelliSweepBatchListener)listener).iterationChanged(event);
									  break;
			default:
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void changeBatchController(ParameterTree newTree) {
		actBatchController.removeBatchListener(this);
		actBatchController = platform.getBatchController();
		actBatchController.addBatchListener(this);
		
		actBatchController.setParameters(newTree);
		actBatchController.setRecorders(recorders);
		actBatchController.setStopCondition(conditionCode);
		actBatchController.setModelPath(modelPath);
		try {
			actBatchController.setEntryPoint(entryPoint);
		} catch (InvalidEntryPointException e) {
			// never happens at this point: setEntryPoint didn't throw exception in the first iteration
			throw new IllegalStateException(e);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void renameRecorderFiles() {
		for (RecorderInfo info : recorders) {
			String prefix = "";
			if (plugin.getCurrentIteration() < 10)
				prefix = "00";
			else if (plugin.getCurrentIteration() < 100)
				prefix = "0";
			File dest = new File(info.getOutputFile().getParentFile(),info.getOutputFile().getName() + ".part" + prefix + plugin.getCurrentIteration());
			info.getOutputFile().renameTo(dest);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void deleteFileParts() {
		File[] files = workspace.listFiles();
		String pattern = ".*\\.part\\d+";	
		for (File f : files) {
			if (!f.isDirectory() && f.getName().matches(pattern))
				f.delete();
		}
	}
}