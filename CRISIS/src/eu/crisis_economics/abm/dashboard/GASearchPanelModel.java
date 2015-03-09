/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.plugin.IStatisticsPlugin;
import eu.crisis_economics.abm.dashboard.ga.IGAOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IGASelectorConfigurator;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;

/**
 * @author Tamás Máhr
 *
 */
public interface GASearchPanelModel {
	
	public enum FitnessFunctionDirection {
		MINIMIZE,
		MAXIMIZE
	}
	
	/**
	 * The name of model methods that can be used as fitness functions.
	 * @return a list of method names
	 */
	List<RecordableInfo> getFitnessFunctions();

	/**
	 * Adds a new fitness function to the model. The object can be of any class. The {@link #getFitnessFunctions()} method returns the list of objects
	 * added by this method. The implementation of this method should notify {@link ModelListener} instances by invoking the
	 * {@link ModelListener#fitnessFunctionAdded()} method.
	 * 
	 * @param fitnessFunction
	 *            a fitness function of any class.
	 */
	void addFitnessFunction(final RecordableInfo fitnessFunction);
	
	/**
	 * Returns the fitness function name that should be selected in the list of fitness function names initially.
	 * 
	 * @return the default fitness function name
	 */
	RecordableInfo getSelectedFitnessFunction();
	
	/**
	 * When the user selects one of the fitness functions in the list, it is set in the model by calling this function.
	 * 
	 * @param fitnessFunctionName the selected fitness function name
	 */
	void setSelectedFitnessFunction(final RecordableInfo fitnessFunction);
	
	/**
	 * Removes all fitness functions from the model. The implementation of this method should notify {@link ModelListener} instances by invoking the
	 * {@link ModelListener#fitnessFunctionsRemoved()} method.
	 */
	void removeAllFitnessFunctions();

	/**
	 * The available statistics to be used in the fitness function.
	 * 
	 * @return a list of statistics plugins
	 */
	List<IStatisticsPlugin> getFitnessStatistics();
	
	/**
	 * Returns the selected fitness function statistics. Initially, this should be selected on the GUI.
	 * 
	 * @return a statistics plugin
	 */
	IStatisticsPlugin getSelectedFitnessStatistics();
	
	/**
	 * When the user selects one of the fitness statistics in the list, it is set in the model by calling this function.
	 * 
	 * @param fitnessStatistics the selected fitness statistics
	 */
	void setSelectedFitnessStatistics(final IStatisticsPlugin fitnessStatistics);
	
	/**
	 * Creates a RecordableInfo instance that represents the selected fitness function/statistics/time-series length.
	 * 
	 * @return a RecordableInfo describing the recordable statistics
	 */
	RecordableInfo getSelectedFitnessStatisticsInfo();
	
	/**
	 * Returns the length of time series that should collect fitness values before the specified statistics is applied. It returns -1 if no statistics
	 * is specified. Initially, this should be set on the GUI.
	 * 
	 * @return the length of the time-series, or -1
	 */
	int getFitnessTimeSeriesLength();
	
	/**
	 * When the user specifies the length of the time series on the GUI, it is set in the model via this method.
	 * 
	 * @param fitnessTimeSeriesLength the requested length of the time series
	 */
	void setFitnessTimeSeriesLength(final int fitnessTimeSeriesLength);
	
	/**
	 * Returns a list of selection operators. These operators will be displayed as selection operators, and their preferences panel will be displayed.
	 * 
	 * @return a list of selection operators
	 */
	List<IGASelectorConfigurator> getSelectionOperators();

	/**
	 * Returns a list of genetic operators. These operators will be displayed as genetic operators, and their preferences panel will be displayed.
	 * 
	 * @return a list of genetic operators
	 */
	List<IGAOperatorConfigurator> getGeneticOperators();

	/**
	 * Returns the list of genetic operators that are selected by the user.
	 * 
	 * @return the list of selected genetic operators
	 */
	List<IGAOperatorConfigurator> getSelectedGeneticOperators();

	/**
	 * Returns the list of selection operators that are selected by the user.
	 * 
	 * @return the list of selected selection operators
	 */
	List<IGASelectorConfigurator> getSelectedSelectionOperators();

	/**
	 * When the user selects a selection operator on the GUI, it is communicated to the model by calling this method.
	 * 
	 * @param operator the selection operator that has been selected by the user
	 */
	void setSelectedSelectionOperator(final IGASelectorConfigurator operator);
	
	/**
	 * When the user selects a genetic operator on the GUI, it is communicated to the model by calling this method.
	 * 
	 * @param operator the genetic operator that has been selected by the user
	 */
	void setSelectedGeneticOperator(final IGAOperatorConfigurator operator);
	
	/**
	 * When the user deselects a selection operator on the GUI, it is communicated to the model by calling this method.
	 * 
	 * @param operator the selection operator that has been deselected by the user
	 * @return true if the operator was present in the selectedSelectionOperator list before, false otherwise
	 */
	boolean unsetSelectedSelectionOperator(final IGASelectorConfigurator operator);
	
	/**
	 * When the user deselects a genetic operator on the GUI, it is communicated to the model by calling this method.
	 * 
	 * @param operator the genetic operator that has been deselected by the user
	 * @return true if the operator was present in the selectedGeneticOperator list before, false otherwise
	 */
	boolean unsetSelectedGeneticOperator(final IGAOperatorConfigurator operator);
	
	/**
	 * Returns the value that should be displayed in the 'Number of turns' input box initially.
	 * 
	 * @return the default number of turns
	 */
	double getNumberOfTurns();
	
	/**
	 * When the user navigates away from the 'Number of turns' text field, after verification, this method is called to set the number input by the
	 * user.
	 * 
	 * @param numberOfTurns the number input by the user
	 */
	void setNumberOfTurns(final double numberOfTurns);
	
	/**
	 * Returns the value that should be set in the 'Population size' input initially.
	 * 
	 * @return the default population size
	 */
	int getPopulationSize();
	
	/**
	 * When the user changes the 'Population size' input, the selected value is set in the model by calling this method.
	 * 
	 * @param populationSize the population size as input by the user
	 */
	void setPopulationSize(final int populationSize);
	
	/**
	 * Returns the value that has been set in the 'Random seed' input.
	 * 
	 * @return the default population random seed
	 */
	int getPopulationRandomSeed();
	
	/**
	 * When the user changes the 'Random seed' input, the selected value is set in the model by calling this method.
	 * 
	 * @param seed the population random seed as input by the user
	 */
	void setPopulationRandomSeed(final int seed);
	
	/**
	 * Retruns the value that should be set in the 'Number of generations' input initially. If positive, then this input will be active and the
	 * fitness limit disabled. If negative, this input will be disabled and the fitness limit enabled.
	 * 
	 * @return the default number of generations
	 */
	int getNumberOfGenerations();
	
	/**
	 * When the user changes the 'Number of generations' input, the selected value is set in the model by calling this method.
	 * 
	 * @param numberOfGenerations the number of generations as input by the user
	 */
	void setNumberOfGenerations(final int numberOfGenerations);
	
	/**
	 * Returns the value that should be set in the 'Fitness limit' input initially.
	 * 
	 * @return the default fitness limit
	 */
	double getFitnessLimitCriterion();
	
	/**
	 * When the user changes the 'Fitness limit' input, the selected value is set in the model by calling this method.
	 * 
	 * @param fitnessLimit the fitness limit criterion as input by the user
	 */
	void setFitnessLimitCriterion(final double fitnessLimit);
	
	
	/**
	 * Returns the value that governs whether 'Minimize' or 'Maximize' is selected in the Fitness panel initially.
	 * 
	 * @return the default fitness direction
	 */
	FitnessFunctionDirection getFitnessFunctionDirection();
	
	/**
	 * When the user selects either 'Minimize' or 'Maximize', the selected value is set in the model by calling this method.
	 * 
	 * @param direction the selected optimization direction
	 */
	void setFitnessFunctionDirection(final FitnessFunctionDirection direction);
	
	/**
	 * Returns the treemodel that is displayed in the Chromosome section of the GUI.
	 * 
	 */
	DefaultTreeModel getChromosomeTree();
	
	/**
	 * Adds submodel nodes to the model of the JTree for the chromosome.
	 * 
	 * @param sInfo the submodel info to add
	 * @param node the parent node
	 * @throws ModelInformationException @see {@link ParameterTreeUtils#fetchSubparameters(IModelHandler, SubmodelInfo)}
	 */
	public void addSubmodelParameter(final SubmodelInfo sInfo, final DefaultMutableTreeNode node) throws ModelInformationException;
	
	/**
	 * Adds the parameter (a gene or a constant parameter) to the model. This method can be used to initialize the chromosome tree. The implementation of
	 * this method should notifiy {@link ModelListener} instances by invoking the {@link ModelListener#parameterAdded()} method.
	 * @throws ModelInformationException @see {@link ParameterTreeUtils#fetchSubparameters(IModelHandler, SubmodelInfo)}
	 */
	void addParameter(final ParameterOrGene parameterOrGene) throws ModelInformationException;

	/**
	 * Removes all genes and constant parameters from the model. The implementation of this method should notify {@link ModelListener} instances by invoking
	 * the {@link ModelListener#parametersRemoved()} method. 
	 */
	void removeAllParameters();
	
	boolean addModelListener(final ModelListener listener);
	
	boolean removeModelListener(final ModelListener listener);
	
	String[] checkGAModel();
	
	boolean canBeFitnessFunction(final RecordableInfo candidate);
	
	boolean isFixNumberOfGenerations();
	
	void setFixNumberOfGenerations(final boolean fixNumberOfGenerations);
	
	void init(final IModelHandler modelHandler);
	void saveConfiguration(final ObjectFactory factory, final Model config);
	void loadConfiguration(final Model config, final List<ParameterInfo> parameters) throws ModelInformationException;
	
	/**
	 * The {@link ModelListener} interface is used to notify interested parties about changes in the model.
	 * 
	 * @author Tamás Máhr
	 *
	 */
	public interface ModelListener {
		
		/**
		 * Fired when a new fitness function has been added to the model.
		 */
		void fitnessFunctionAdded();
		
		/**
		 * Fired when all fitness functions are removed from the model.
		 */
		void fitnessFunctionsRemoved();
		
		/**
		 * Fired when a new parameter or gene has been added to the model.
		 */
		void parameterAdded();
		
		/**
		 * Fired when all parameters and genes are removed from the model.
		 */
		void parametersRemoved();
	}

}