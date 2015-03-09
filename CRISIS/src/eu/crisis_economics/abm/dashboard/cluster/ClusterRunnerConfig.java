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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import eu.crisis_economics.abm.dashboard.cluster.script.BashScheduler;
import eu.crisis_economics.abm.dashboard.cluster.script.Scheduler;
import eu.crisis_economics.abm.dashboard.generated.Model;

/**
 * @author Tamás Máhr
 *
 */
@Configuration
@ComponentScan(basePackages="eu.crisis_economics.abm.dashboard.cluster")
@EnableAsync
public class ClusterRunnerConfig {
	
	static Map<String, Class<? extends Scheduler>> schedulerTypes = ImmutableMap.<String, Class<? extends Scheduler>>of(
			"torque4", BashScheduler.class
			,"local", BashScheduler.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		
		return configurer;
	}
	
	@Bean
	public static Scheduler getScheduler() throws InstantiationException, IllegalAccessException {
		String schedulerType = System.getProperty("scheduler.type");
		
		Preconditions.checkNotNull(schedulerType, "Please specify the type of the scheduler as a property: -Dscheduler.type=...");
		
		return schedulerTypes.get(schedulerType).newInstance();
	}

	@Bean
	@Scope("prototype")
	public ExperimentEndSignaller getExperimentEndSignaller() {
		return new ExperimentEndHandler();
	}
	
	@Bean
	public Model getModel() throws JAXBException {
		String configFileName = context.getEnvironment().getProperty("configFile");
		Preconditions.checkNotNull(configFileName, "No config file specified (--configFile configFile)!");
		return (Model) JAXBContext.newInstance(Model.class).createUnmarshaller().unmarshal(new File(configFileName));
	}

}
