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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.dashboard.cluster.annotation.ClusterRunner;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ModelType;

/**
 * @author Tamás Máhr
 *
 */
@Service
public class RunnerManager {

	@Autowired
	private ApplicationContext context;

	@Autowired
	protected Model model;
	
	protected Runner runner;
	/**
	 * Looks up a {@link Runner} implementation in the context that has the {@link ClusterRunner} annotation with the same run-strategy as required by
	 * the model.
	 * 
	 * @param model the model that should be handled by the {@link Runner}
	 * @return a {@link Runner} implementation that can handle the given model
	 */
	public Runner getRunner() {
		Preconditions.checkNotNull(model, "No model found in the application-context!");

		if (runner != null){
			return runner;
		}
		
		ModelType runStrategy = model.getRunStrategy();
		Map<String, Object> runners = context.getBeansWithAnnotation(ClusterRunner.class);
		for (String beanName : runners.keySet()) {
			Object runnerObject = runners.get(beanName);

			// it needs to be a runner instance
			if (runnerObject instanceof Runner) {
				Runner clusterRunner = (Runner) runnerObject;
				
				ClusterRunner annotation = context.findAnnotationOnBean(beanName, ClusterRunner.class);
				
				if (annotation.value() == runStrategy) {
					//context.registerAlias(beanName, RUNNER_BEAN_ALIAS);
					runner = clusterRunner;
					return runner;
				}
			}
			
		}
		throw new IllegalStateException("No Runner found for a model with run-strategy " + runStrategy.name());
	}
}
