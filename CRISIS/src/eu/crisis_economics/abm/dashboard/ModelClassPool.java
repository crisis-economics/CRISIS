/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Rajmund Bocsi
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

import javassist.ClassPool;
import javassist.Loader;

public class ModelClassPool extends ClassPool {
	
	public ModelClassPool() {
		super(true);
//		String path = getClassLoader().getResource(getClass().getName().replace('.', '/') + ".class").getPath();
//		modelRoot = path.substring(0, path.lastIndexOf(getClass().getName().replace('.', '/')) - 1);
//		try {
//			appendClassPath(modelRoot);
//		} catch (NotFoundException e) {
//			throw new IllegalStateException(e);
//		}
		
		loader.delegateLoadingOf("sim.engine.");
		loader.delegateLoadingOf("sim.util.");
		loader.delegateLoadingOf("ec.util.");
		
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.output.");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.repast.info.");		
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.netlogo.info.");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.simphony.impl.info.");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.mason.impl.IRecorderListenerAware");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.mason.impl.MasonRecorderListener");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.ResultValueInfo");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.ReadingException");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.IBatchController");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.IClusterBatchController");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.IBatchListener");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.BatchEvent");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.BatchEvent$EventType");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.batch.param.");
		loader.delegateLoadingOf("ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel");
	}
	
	private Loader loader = new Loader(Dashboard.class.getClassLoader(), this);
	
	@Override
   public ClassLoader getClassLoader(){
		return loader;
	}
}
