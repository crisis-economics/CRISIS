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
package eu.crisis_economics.abm.dashboard.cluster.script;

import java.io.File;

import eu.crisis_economics.abm.dashboard.generated.Model;

/**
 * @author Tamás Máhr
 *
 */
public interface Scheduler {
	
	/**
	 * Runs a parameter-sweep job on the cluster.
	 * 
	 * @param paramSweepConfig the configuration of the parameter sweep
	 * @param timeLimit an optional time limit string; may be null
	 * @param workDir the working directory for the sweep
	 * @return the job ID of the submitted job
	 * @throws SchedulerException if submitting the job failed for any reason
	 */
	public String runParameterSweep(Model paramSweepConfig, String timeLimit, File workDir) throws SchedulerException;
	
	public void runJobEndedSignal();

}
