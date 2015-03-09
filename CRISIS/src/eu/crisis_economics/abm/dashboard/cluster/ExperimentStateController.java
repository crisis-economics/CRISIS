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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller receives messages about the state of an experiment
 * 
 * @author Tamás Máhr
 *
 */
@RestController
public class ExperimentStateController {

	Logger log = LoggerFactory.getLogger(ExperimentStateController.class);
	
	@Autowired
	RunnerManager runnerManager;
	
	@ResponseStatus(value=HttpStatus.OK)
	@RequestMapping(value="/finished/")
	public void finished(){
		log.debug("param-sweep finished");
		
		Runner runner = runnerManager.getRunner();
		
		runner.runExperiment();	
	}
}
