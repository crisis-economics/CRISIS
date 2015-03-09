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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.google.common.base.Preconditions;
import com.google.common.io.PatternFilenameFilter;

import eu.crisis_economics.abm.dashboard.cluster.ClusterMain.PortAvailableEvent;
import eu.crisis_economics.abm.dashboard.generated.Model;

/**
 * @author Tamás Máhr
 *
 */
public class BashScheduler implements Scheduler, ApplicationContextAware {

	private static final String CMD_SUBSTITUTION_NAME_FILE = "FILE";

	private static final String paramSweepCmd = "startSweep.sh";
	
	private static final String scriptsDir = System.getProperty("scripts.dir");
	
	private static final String schedulerType = System.getProperty("scheduler.type");

	private static final File cmdFile = new File(scriptsDir + File.separator + schedulerType + File.separator + paramSweepCmd);

	protected AnnotationConfigWebApplicationContext context;

	protected int serverPort;

	/**
	 * 
	 */
	public BashScheduler() {
		Preconditions.checkNotNull(scriptsDir, "Please specify the scripts directory as a property: -Dscripts.dir=...");
		Preconditions.checkNotNull(schedulerType, "Please specify the scheduler type as a property: -Dscheduler.type=...");
		Preconditions.checkArgument(cmdFile.exists(), "Script '" + cmdFile.getPath() + "' is not found!");
//		Preconditions.checkArgument(cmdFile.canExecute(), "Script '" + cmdFile.getPath() + "' is not executable!");
		makeScriptsExecutable();
	}
	
	private void makeScriptsExecutable() {
		File scriptsDirectory = new File(scriptsDir + File.separator + schedulerType);
		String[] scripts = scriptsDirectory.list(new PatternFilenameFilter(".*\\.sh"));
		
		CommandLine commandLine = new CommandLine("chmod");
		commandLine.addArgument("755");
		for (String script : scripts) {
			commandLine.addArgument(scriptsDir + File.separator + schedulerType + File.separator + script, false);
		}
		
		DefaultExecutor executor = new DefaultExecutor();
		
		try {
			executor.execute(commandLine);
		} catch (ExecuteException e) {
			// ignore this; there will be an exception later, if this scheduler is used
		} catch (IOException e) {
			// ignore this; there will be an exception later, if this scheduler is used
		}
	}

	/** {@inheritDoc} 
	 * @throws SchedulerException 
	 */
	@Override
	public String runParameterSweep(final Model paramSweepConfig, final String timeLimit, final File workDir) throws SchedulerException {
		
		File file = null;
		try {
//			file = File.createTempFile("paramsweep-", ".xml");
			file = new File(workDir, "paramsweep-config.xml");
			Marshaller marshaller = JAXBContext.newInstance(Model.class).createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(paramSweepConfig, file);
//		} catch (IOException e) {
//			throw new SchedulerException("Could not create temporary parameter-sweep configuration xml.", e);
		} catch (JAXBException e) {
			throw new SchedulerException("Could not write temporary parameter-sweep configuration xml: " + file.toString(), e);
		}
		CommandLine cmd = new CommandLine(cmdFile);
		Map<String, Object> substitutions = new HashMap<String, Object>();
		substitutions.put(CMD_SUBSTITUTION_NAME_FILE, file);
		cmd.setSubstitutionMap(substitutions);
		
		if (timeLimit != null && ! timeLimit.isEmpty()){
			cmd.addArgument("-t", false);
			cmd.addArgument(timeLimit, false);
		}
		
		// add server port argument
		cmd.addArgument("-p", false);
		cmd.addArgument(String.valueOf(serverPort), false);
		
		cmd.addArgument("${" + CMD_SUBSTITUTION_NAME_FILE + "}", false);
		
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workDir);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(byteArrayOutputStream);
		
		executor.setStreamHandler(streamHandler);
		
		try {
			executor.execute(cmd);
		} catch (ExecuteException e) {
			throw new SchedulerException(paramSweepCmd + " exited with " + e.getExitValue() + ". Output:\n" +
					byteArrayOutputStream, e);
		} catch (IOException e) {
			throw new SchedulerException("Execution of " + paramSweepCmd + " failed. Output:\n" + byteArrayOutputStream, e);
		}
		
		// the standard output of the script is the job id
		 final String jobId = byteArrayOutputStream.toString();
		 
		 return jobId;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public void runJobEndedSignal() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = (AnnotationConfigWebApplicationContext) context;

		this.context.addApplicationListener(new ApplicationListener<PortAvailableEvent>() {

			@Override
			public void onApplicationEvent(PortAvailableEvent event) {
				serverPort = event.getPort();
			}
		});
}

}
