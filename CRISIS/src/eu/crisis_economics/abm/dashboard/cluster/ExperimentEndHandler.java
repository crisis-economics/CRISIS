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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;

import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

import eu.crisis_economics.abm.dashboard.ModelClassPool;
import eu.crisis_economics.abm.dashboard.ModelHandler;
import eu.crisis_economics.abm.dashboard.generated.Model;
import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecorderInfo;
import ai.aitia.meme.paramsweep.platform.repast.impl.ResultFileMerger;

/**
 * @author Tamás Máhr
 *
 */
public class ExperimentEndHandler implements ExperimentEndSignaller {

	@Autowired
	private ApplicationContext context;
	
	private static Logger log = LoggerFactory.getLogger(ExperimentEndHandler.class);
	
	/**
	 * 
	 */
	public ExperimentEndHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	@Async
	public void signalEnd(final List<RecorderInfo> recorders, final File workingDir) {
		// let's merge the results
		String fileName = recorders.get(0).getOutputFile().getName();
		
		int i = 1;
		File[] genDirs = workingDir.listFiles(new PatternFilenameFilter(".*gen-\\d+"));
		for (File genDir : genDirs) {
			File file = new File(genDir, fileName);
			try {
				Files.copy(file, new File(fileName + ".part" + i++));
			} catch (IOException e) {
				log.error("Could not copy '" + file.getName() + "' to '" + fileName + ".part" + i , e);
			}
			
		}
		
		ResultFileMerger merger = new ResultFileMerger();
		merger.merge(recorders, workingDir);
		
		context.publishEvent(new ClusterMain.ExperimentEndedEvent(this));
		
	}

	public static void main(String[] args) throws JAXBException, ClassNotFoundException, ModelInformationException {
		Model model = (Model) JAXBContext.newInstance(Model.class).createUnmarshaller().unmarshal(new File(args[0]));
		ModelHandler modelHandler = new ModelHandler(model.getClazz(), new ModelClassPool());
		modelHandler.getParameters(); // we need to call this to perform model inspection

		ArrayList<RecorderInfo> list = new ArrayList<RecorderInfo>();
		// we need to set the delimiter and the file name only
		RecorderInfo recorderInfo = modelHandler.getRecorders().get(0);
		final RecorderInfo newRecorder = new RecorderInfo();
		newRecorder.setDelimiter(recorderInfo.getDelimiter());
		newRecorder.setOutputFile(recorderInfo.getOutputFile());
		list.add(newRecorder);

		ExperimentEndHandler experimentEndHandler = new ExperimentEndHandler();
		experimentEndHandler.signalEnd(list, new File(args[1]));
	}
}
