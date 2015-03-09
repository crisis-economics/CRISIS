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
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author Tamás Máhr
 *
 */
@ComponentScan
public class ClusterMain {

	public static final String RUNNER_BEAN_ALIAS = "runnerBean";
	
	private static final String CONTEXT_PATH = "/";

	private static final String MAPPING_URL = "/*";

	protected static String configFileName;

	private static Logger log = LoggerFactory.getLogger(ClusterMain.class);

	private static AnnotationConfigWebApplicationContext context;
	
	protected static void processArgs(final String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-c")){
				configFileName = args[i + 1];
				i++;
			}
		}
	}
	
	protected static boolean checkState(){
		
		if (configFileName == null || configFileName.isEmpty() || ! new File(configFileName).exists()) {
			log.error("No config file given!");
			return false;
		}
		
		return true;
	}
	
	protected static void usage(){
		log.error("Please specify -c configFile as argument!");
	}

	private static Server getServer() throws IOException {
		 // Create a basic jetty server object that will listen on port 8080.  Note that if you set this to port 0
       // then a randomly available port will be assigned that you can either look in the logs for the port,
       // or programmatically obtain it for use in test cases.
		Server server = new Server(0);
		
       server.setHandler(createServletContextHandler());

       return server;
	}

	private static ServletContextHandler createServletContextHandler() throws IOException {
		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setErrorHandler(null);
		contextHandler.setContextPath(CONTEXT_PATH);
		contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), MAPPING_URL);
		contextHandler.addEventListener(new ContextLoaderListener(context));
		return contextHandler;
	}

	public static void main(String[] args) throws Exception {
		SimpleCommandLinePropertySource cmdPropertySource = new SimpleCommandLinePropertySource(args);

		configFileName = cmdPropertySource.getProperty("configFile");
		
		context = new AnnotationConfigWebApplicationContext();
		
		context.getEnvironment().getPropertySources().addFirst(cmdPropertySource);
		context.register(ClusterRunnerConfig.class);
		context.refresh();

		final Server server = getServer();
//		// Start things up! By using the server.join() the server thread will join with the current thread.
//		// See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
		server.start();
		
		context.publishEvent(new PortAvailableEvent(server.getConnectors()[0].getLocalPort(), server));

		context.addApplicationListener(new ApplicationListener<ExperimentEndedEvent>() {

			@Override
			public void onApplicationEvent(ExperimentEndedEvent arg0) {
				try {
					server.stop();
				} catch (Exception e) {
					log.error("Exception while stopping jetty", e);
				}
			}
		});
		
		RunnerManager runnerManager = context.getBean(RunnerManager.class);
		Runner runner = runnerManager.getRunner();

		try {
			runner.runExperiment();
		} catch (Throwable e) {
			e.printStackTrace();
			server.stop();
		}
		
		server.join();
	}
	
	public static class PortAvailableEvent extends ApplicationEvent {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2610244761910294218L;
		
		int port;
		
		public PortAvailableEvent(final int port, final Object source) {
			super(source);
			
			this.port = port;
		}

		/**
		 * @return the port
		 */
		public int getPort() {
			return port;
		}
	}
	
	public static class ExperimentEndedEvent extends ApplicationEvent {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6263589266806786371L;

		public ExperimentEndedEvent(Object source) {
			super(source);
		}
		
	}
}
