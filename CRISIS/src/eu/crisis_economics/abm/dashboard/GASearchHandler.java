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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.math3.random.MersenneTwister;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessEvaluator;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.RandomGenerator;
import org.jgap.impl.DefaultConfiguration;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.IParameterSweepResultReader;
import ai.aitia.meme.paramsweep.batch.ReadingException;
import ai.aitia.meme.paramsweep.batch.ResultValueInfo;
import ai.aitia.meme.paramsweep.batch.output.RecordableInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.colt.DoubleArrayList;
import ai.aitia.meme.paramsweep.generator.IStatisticInfoGenerator;
import ai.aitia.meme.paramsweep.generator.OperatorsInfoGenerator;
import ai.aitia.meme.paramsweep.generator.StatisticsInfoGenerator;
import ai.aitia.meme.paramsweep.gui.info.ISubmodelGUIInfo;
import ai.aitia.meme.paramsweep.gui.info.MemberInfo;
import ai.aitia.meme.paramsweep.gui.info.OperatorGeneratedMemberInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.RecordableElement;
import ai.aitia.meme.paramsweep.gui.info.SimpleGeneratedMemberInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.intellisweepPlugin.utils.ga.GeneInfo;
import ai.aitia.meme.paramsweep.internal.platform.InfoConverter;
import ai.aitia.meme.paramsweep.operatorPlugin.Operator_TimeSeries;
import ai.aitia.meme.paramsweep.plugin.IIntelliContext;
import ai.aitia.meme.paramsweep.plugin.IIntelliDynamicMethodPlugin;
import ai.aitia.meme.paramsweep.plugin.IStatisticsPlugin;
import ai.aitia.meme.paramsweep.utils.WizardLoadingException;
import ai.aitia.meme.utils.Utils;
import eu.crisis_economics.abm.dashboard.ga.BestChromosomeSelectorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.CrossoverOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.DashboardBreeder;
import eu.crisis_economics.abm.dashboard.ga.GeneAveragingCrossoverOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IGAOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IGASelectorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.IIdentifiableGene;
import eu.crisis_economics.abm.dashboard.ga.IWorkspaceAware;
import eu.crisis_economics.abm.dashboard.ga.IdentifiableDoubleGene;
import eu.crisis_economics.abm.dashboard.ga.IdentifiableListGene;
import eu.crisis_economics.abm.dashboard.ga.IdentifiableLongGene;
import eu.crisis_economics.abm.dashboard.ga.MutationOperatorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.TournamentSelectorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.WeightedRouletteSelectorConfigurator;
import eu.crisis_economics.abm.dashboard.ga.operators.StandardPostSelectorFixed;
import eu.crisis_economics.abm.dashboard.generated.GeneralParameter;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;
import eu.crisis_economics.abm.dashboard.generated.Operator;
import eu.crisis_economics.abm.dashboard.generated.Parameter;
import eu.crisis_economics.abm.dashboard.generated.ParameterType;
import eu.crisis_economics.abm.dashboard.generated.Property;
import eu.crisis_economics.abm.dashboard.generated.Selector;
import eu.crisis_economics.abm.dashboard.generated.SubmodelParameter;
import eu.crisis_economics.abm.dashboard.jaxb.util.ParameterUtil;

@SuppressWarnings("unchecked")
public class GASearchHandler implements GASearchPanelModel, IIntelliDynamicMethodPlugin, IWorkspaceAware {
													 
	//====================================================================================================
	// members

	private static final int DEFAULT_TIMESERIES_LENGTH = 20;
	private static final long serialVersionUID = 45791730954138881L;
	private static final Logger log = LoggerFactory.getLogger(GASearchHandler.class);
	
	private static final String POPULATION_SIZE = "Population size";
	private static final String POPULATION_RANDOM_SEED = "Population random seed";
	private static final String FIX_NUMBER_OF_GENERATIONS = "Fix number of generations";
	private static final String NUMBER_OF_GENERATIONS = "Number of generations";
	private static final String FITNESS_LIMIT_CRITERION = "Fitness limit";
	private static final String OPTIMIZATION_DIRECTION = "Optimization direction";
	private static final String FITNESS_FUNCTION = "Fitness function"; 
	private static final String FITNESS_STATISTICS = "Fitness statistics"; 
	private static final String FITNESS_TIMESERIES_LENGTH = "Fitness time-series length"; 

	protected static final String ext = ".txt";
	protected static final String bckExt = ".bak";
	
	private static final List<Class<?>> acceptableReturnTypesForFitnessFunction;
	private static final List<IStatisticsPlugin> fitnessStatistics;
	
	public static final IStatisticsPlugin noStatistics = new IStatisticsPlugin() {

		@Override
		public String getLocalizedName() {
			return null;
		}

		@Override
		public int getNumberOfParameters() {
			return 0;
		}

		@Override
		public String getName() {
			return "none";
		}

		@Override
		public String getDescription() {
			return "no statistics selected";
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Class getReturnType() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List<Class> getParameterTypes() {
			return null;
		}

		@Override
		public List<String> getParameterNames() {
			return null;
		}

		@Override
		public List<String> getParameterDescriptions() {
			return null;
		}

		@Override
		public String getFullyQualifiedName() {
			return null;
		}
	};
	
	static {
		Class<?>[] array = { Byte.TYPE, Byte.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class,
							 Double.TYPE, Double.class };
		acceptableReturnTypesForFitnessFunction = Arrays.asList(array);

		Reflections reflections = new Reflections("ai.aitia.meme.paramsweep.coltPlugin");
		Set<Class<? extends IStatisticsPlugin>> subTypesOf = reflections.getSubTypesOf(IStatisticsPlugin.class);
		fitnessStatistics = new ArrayList<IStatisticsPlugin>();
		
		fitnessStatistics.add(noStatistics);
		
		for (Class<? extends IStatisticsPlugin> pluginClass : subTypesOf) {
			try {
				final IStatisticsPlugin plugin = pluginClass.newInstance();
				@SuppressWarnings("rawtypes")
            List<Class> parameterTypes = plugin.getParameterTypes();
				if (parameterTypes.size() == 1 && parameterTypes.get(0).isAssignableFrom(DoubleArrayList.class)) {
					fitnessStatistics.add(plugin);
				}
			} catch (InstantiationException e) {
				log.error("Could not instantiate " + pluginClass.getName(), e);
			} catch (IllegalAccessException e) {
				log.error("Could not instantiate " + pluginClass.getName(), e);
			}
		}
	}

	private double numberOfTurns = Dashboard.NUMBER_OF_TURNS;
	private int populationSize = 12;
	private int populationGenerationSeed = 1;
	private int numberOfGenerations = 12;
	private double fitnessLimitCriterion = 0;
	private boolean fixNumberOfGenerations = true;
	private FitnessFunctionDirection optimizationDirection = FitnessFunctionDirection.MINIMIZE;
	private RecordableInfo selectedFunction;

	private IStatisticsPlugin selectedFitnessStatistics;
	
	private final List<IGASelectorConfigurator> selectedSelectionOperators = new ArrayList<IGASelectorConfigurator>();

	private final List<IGAOperatorConfigurator> selectedGeneticOperators = new ArrayList<IGAOperatorConfigurator>();

	private DefaultConfiguration gaConfiguration;

	/**
	 * The available selection operators.
	 */
	private List<IGASelectorConfigurator> selectors;

	/**
	 * The available genetic operators.
	 */
	private List<IGAOperatorConfigurator> geneticOperators;

	/**
	 * The list of fitness functions.
	 */
	private final List<RecordableInfo> fitnessFunctions = new ArrayList<RecordableInfo>();

	private int fitnessTimeSeriesLength = DEFAULT_TIMESERIES_LENGTH;
	
	private final String populationFileName = "population";
	
	private DefaultTreeModel chromosomeTree;
	private List<GeneInfo> genes = null;
	private int iterationCounter = 1;
	private boolean reachFitnessLimit = false;
	
	private Genotype genotype;
	private List<ParameterInfo> paramList;

	private final List<ModelListener> listeners = new ArrayList<GASearchPanelModel.ModelListener>();
	
	private File workspace = new File(".");
	private IModelHandler currentModelHandler = null;
	
	//====================================================================================================
	// methods

	//----------------------------------------------------------------------------------------------------
	public GASearchHandler(final IModelHandler currentModelHandler) {
		selectors = Arrays.asList(new TournamentSelectorConfigurator(),new WeightedRouletteSelectorConfigurator(),new BestChromosomeSelectorConfigurator());
		geneticOperators = Arrays.asList(new GeneAveragingCrossoverOperatorConfigurator(), new CrossoverOperatorConfigurator(),new MutationOperatorConfigurator());
		
		init(currentModelHandler);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void init(final IModelHandler modelHandler) {
		this.currentModelHandler = modelHandler;
		numberOfTurns = Dashboard.NUMBER_OF_TURNS;
		populationSize = 12;
		populationGenerationSeed = 1;
		numberOfGenerations = 12;
		fitnessLimitCriterion = 0;
		fixNumberOfGenerations = true;
		optimizationDirection = FitnessFunctionDirection.MINIMIZE;
		selectedFunction = new RecordableInfo("Please select a function!",Object.class,"Please select a function!");

		selectedFitnessStatistics = fitnessStatistics.get(0);
		fitnessTimeSeriesLength = DEFAULT_TIMESERIES_LENGTH;
		
		selectedSelectionOperators.clear();
		selectedGeneticOperators.clear();

		gaConfiguration = null;
		fitnessFunctions.clear();

		iterationCounter = 1;
		reachFitnessLimit = false;
		
		genotype = null;
		paramList = null;
		
		workspace = new File(".");
		
		fitnessFunctions.add(selectedFunction);
		
		
		selectedSelectionOperators.add(selectors.get(2));

		selectedGeneticOperators.add(geneticOperators.get(0));
		selectedGeneticOperators.add(geneticOperators.get(2));
	}
	
	//----------------------------------------------------------------------------------------------------
	public void saveConfiguration(final ObjectFactory factory, final Model config) {
		saveGeneralParameters(factory,config);
		saveSelectors(factory,config);
		saveOperators(factory,config);
		saveChromosome(factory,config);
	}
	
	//----------------------------------------------------------------------------------------------------
	private void saveGeneralParameters(final ObjectFactory factory, final Model config) {
		final List<GeneralParameter> generalParameters = config.getGeneralParameterList();

		GeneralParameter gp = factory.createGeneralParameter();
		gp.setName(ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME);
		gp.setValue(String.valueOf(numberOfTurns));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(POPULATION_SIZE);
		gp.setValue(String.valueOf(populationSize));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(POPULATION_RANDOM_SEED);
		gp.setValue(String.valueOf(populationGenerationSeed));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(FIX_NUMBER_OF_GENERATIONS);
		gp.setValue(String.valueOf(fixNumberOfGenerations));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(NUMBER_OF_GENERATIONS);
		gp.setValue(String.valueOf(numberOfGenerations));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(FITNESS_LIMIT_CRITERION);
		gp.setValue(String.valueOf(fitnessLimitCriterion));
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(OPTIMIZATION_DIRECTION);
		gp.setValue(optimizationDirection.name());
		generalParameters.add(gp);
		
		gp = factory.createGeneralParameter();
		gp.setName(FITNESS_FUNCTION);
		gp.setValue(createUniqueName(selectedFunction));
		generalParameters.add(gp);

		gp = factory.createGeneralParameter();
		gp.setName(FITNESS_STATISTICS);
		gp.setValue(selectedFitnessStatistics.getName());
		generalParameters.add(gp);

		gp = factory.createGeneralParameter();
		gp.setName(FITNESS_TIMESERIES_LENGTH);
		gp.setValue(String.valueOf(fitnessTimeSeriesLength));
		generalParameters.add(gp);
}

	//----------------------------------------------------------------------------------------------------
	private String createUniqueName(final RecordableInfo fitnessFunction) {
		//TODO: support 'the fitness function is part of a submodel parameter' case too
		return fitnessFunction.getName();
	}
	
	//----------------------------------------------------------------------------------------------------
	private void saveSelectors(final ObjectFactory factory, final Model config) {
		final List<Selector> selectors = config.getSelectorList();
		
		for (final IGASelectorConfigurator selectorOperator : selectedSelectionOperators) {
			final Selector selector = factory.createSelector();
			selector.setType(selectorOperator.getName());
			final List<Property> selectorProperties = selector.getSelectorProperties();
			
			for (final Entry<String,String> entry : selectorOperator.getConfiguration().entrySet()) {
				final Property property = factory.createProperty();
				property.setKey(entry.getKey());
				property.setValue(entry.getValue());
				selectorProperties.add(property);
			}
			
			selectors.add(selector);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void saveOperators(final ObjectFactory factory, final Model config) {
		final List<Operator> operators = config.getOperatorList();
		
		for (final IGAOperatorConfigurator geneticOperator : selectedGeneticOperators) {
			final Operator operator = factory.createOperator();
			operator.setType(geneticOperator.getName());
			final List<Property> operatorProperties = operator.getOperatorProperties();
			
			for (final Entry<String,String> entry : geneticOperator.getConfiguration().entrySet()) {
				final Property property = factory.createProperty();
				property.setKey(entry.getKey());
				property.setValue(entry.getValue());
				operatorProperties.add(property);
			}
			
			operators.add(operator);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void saveChromosome(final ObjectFactory factory, final Model config) {
		final eu.crisis_economics.abm.dashboard.generated.Chromosome chromosome = factory.createChromosome();
		
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = ((DefaultMutableTreeNode)chromosomeTree.getRoot()).children();
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			saveParameterOrGene(node,factory,chromosome);
		}
		
		config.setChromosome(chromosome);
	}
	
	//----------------------------------------------------------------------------------------------------
	private void saveParameterOrGene(final DefaultMutableTreeNode node, final ObjectFactory factory, final Object parent) {
		final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
		final ParameterInfo info = userObj.getInfo();
		
		if (userObj.isGene()) {
			final GeneInfo geneInfo = userObj.getGeneInfo();
			
			final eu.crisis_economics.abm.dashboard.generated.Gene gene = factory.createGene();
			gene.setName(info.getName());
			gene.setGeneType(geneInfo.getValueType());
			
			if (GeneInfo.INTERVAL.equals(geneInfo.getValueType())) {
				gene.setInteger(geneInfo.isIntegerVals());
				gene.setMin(geneInfo.isIntegerVals() ? new BigDecimal(geneInfo.getMinValue().longValue()) : new BigDecimal(geneInfo.getMinValue().doubleValue()));
				gene.setMax(geneInfo.isIntegerVals() ? new BigDecimal(geneInfo.getMaxValue().longValue()) : new BigDecimal(geneInfo.getMaxValue().doubleValue()));
			} else {
				final List<String> geneValues = gene.getGeneValueList();
				for (final Object value : geneInfo.getValueRange()) 
					geneValues.add(String.valueOf(value));
			}
			
			if (parent instanceof eu.crisis_economics.abm.dashboard.generated.Chromosome) {
				final eu.crisis_economics.abm.dashboard.generated.Chromosome chromosome = (eu.crisis_economics.abm.dashboard.generated.Chromosome) parent;
				chromosome.getGeneList().add(gene);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getGeneList().add(gene);
			}
			
		} else if (info instanceof SubmodelInfo) {
			final SubmodelInfo sInfo = (SubmodelInfo) info;
			
			final SubmodelParameter parameter = factory.createSubmodelParameter();
			parameter.setName(info.getName());
			parameter.setType(sInfo.getActualType() == null ? "null" : sInfo.getActualType().getName());
			
			if (parent instanceof eu.crisis_economics.abm.dashboard.generated.Chromosome) {
				final eu.crisis_economics.abm.dashboard.generated.Chromosome chromosome = (eu.crisis_economics.abm.dashboard.generated.Chromosome) parent;
				chromosome.getSubmodelParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getSubmodelParameterList().add(parameter);
			}
			
			if (node.getChildCount() > 0) {
				@SuppressWarnings("rawtypes")
				final Enumeration childNodes = node.children();
				while (childNodes.hasMoreElements()) {
					final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
					saveParameterOrGene(childNode,factory,parameter);
 				}
			}
		} else {
			final Parameter parameter = factory.createParameter();
			parameter.setName(info.getName());
			parameter.setParameterType(ParameterType.CONSTANT);
			parameter.getContent().add(info.getValue().toString());
			
			if (parent instanceof eu.crisis_economics.abm.dashboard.generated.Chromosome) {
				final eu.crisis_economics.abm.dashboard.generated.Chromosome chromosome = (eu.crisis_economics.abm.dashboard.generated.Chromosome) parent;
				chromosome.getParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getParameterList().add(parameter);
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	/**
	 * This method creates a list of {@link ParameterInfo} instances for the {@link #loadConfiguration(Model, List)} method. How the list of {@link ai.aitia.meme.paramsweep.batch.param.ParameterInfo} is converted 
	 * to {@link ParameterInfo} is copied from {@link Page_Parameters#createAndDisplayAParameterPanel} method.
	 * 
	 * @param config the object representing the XML file
	 * @param parameters the list of model parameters
	 * @throws ModelInformationException if {@link #loadConfiguration(Model, List)} throws an exception
	 */
	public void loadConfigurationFromBatchInfo(final Model config, final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> parameters) throws ModelInformationException {
		final List<ParameterInfo> info = new ArrayList<ParameterInfo>();
		for (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> batchParameterInfo : parameters) {
			final ParameterInfo parameterInfo = InfoConverter.parameterInfo2ParameterInfo(batchParameterInfo);
			// prepare the parameterInfo for the param sweeps
			parameterInfo.setRuns(0);
			parameterInfo.setDefinitionType(ParameterInfo.CONST_DEF);
			parameterInfo.setValue(batchParameterInfo.getDefaultValue());
			info.add(parameterInfo);
		}
		
		loadConfiguration(config, info);
	}
	//----------------------------------------------------------------------------------------------------
	@Override
	public void loadConfiguration(final Model config, final List<ParameterInfo> parameters) throws ModelInformationException {
		final List<GeneralParameter> generalParameters = config.getGeneralParameterList();
		loadGeneralParameters(generalParameters);
		
		final List<Selector> selectors = config.getSelectorList();
		loadSelectors(selectors);
		
		final List<Operator> operators = config.getOperatorList();
		loadOperators(operators);
		
		loadChromosome(config.getChromosome(),parameters);
	}

	//----------------------------------------------------------------------------------------------------
	private void loadGeneralParameters(final List<GeneralParameter> generalParameters) {
		for (final GeneralParameter generalParameter : generalParameters) {
			if (ParameterTreeUtils.NUMBER_OF_TURNS_XML_NAME.equals(generalParameter.getName()))
				numberOfTurns = Double.parseDouble(generalParameter.getValue());
			else if (POPULATION_SIZE.equals(generalParameter.getName()))
				populationSize = Integer.parseInt(generalParameter.getValue());
			else if (POPULATION_RANDOM_SEED.equals(generalParameter.getName()))
				populationGenerationSeed = Integer.parseInt(generalParameter.getValue());
			else if (FIX_NUMBER_OF_GENERATIONS.equals(generalParameter.getName()))
				fixNumberOfGenerations = Boolean.parseBoolean(generalParameter.getValue());
			else if (NUMBER_OF_GENERATIONS.equals(generalParameter.getName()))
				numberOfGenerations = Integer.parseInt(generalParameter.getValue());
			else if (FITNESS_LIMIT_CRITERION.equals(generalParameter.getName()))
				fitnessLimitCriterion = Double.parseDouble(generalParameter.getValue());
			else if (OPTIMIZATION_DIRECTION.equals(generalParameter.getName()))
				optimizationDirection = FitnessFunctionDirection.valueOf(generalParameter.getValue());
			else if (FITNESS_FUNCTION.equals(generalParameter.getName()))
				loadSelectedFitnessFunction(generalParameter.getValue());
			else if (FITNESS_STATISTICS.equals(generalParameter.getName())) {
				loadSelectedFitnessStatistics(generalParameter.getValue());
			} else if (FITNESS_TIMESERIES_LENGTH.equals(generalParameter.getName())) {
				fitnessTimeSeriesLength = Integer.parseInt(generalParameter.getValue());
			}
		}
	}

	private void loadSelectedFitnessStatistics(final String fitnessStatisticsName) {
		for (IStatisticsPlugin statistics : fitnessStatistics) {
			if (statistics.getName().equals(fitnessStatisticsName)) {
				selectedFitnessStatistics = statistics;
				break;
			}
		}
	}

	//----------------------------------------------------------------------------------------------------
	private void loadSelectedFitnessFunction(final String identifier) {
		for (final RecordableInfo function : fitnessFunctions) {
			//TODO: support 'the fitness function is part of a submodel parameter' case too
			if (identifier.equals(function.getName())) {
				selectedFunction = function;
				break;
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private void loadSelectors(final List<Selector> selectorList) throws ModelInformationException {
		selectedSelectionOperators.clear();
		
		for (final Selector selector : selectorList) {
			final String selectorType = selector.getType();
			final IGASelectorConfigurator matchedConfigurator = findSelectorConfigurator(selectorType);
			final Map<String,String> config = convertPropertiesToMap(selector.getSelectorProperties());
			matchedConfigurator.setConfiguration(config);
			selectedSelectionOperators.add(matchedConfigurator);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private IGASelectorConfigurator findSelectorConfigurator(final String selectorType) throws ModelInformationException {
		for (final IGASelectorConfigurator configurator : selectors) {
			if (configurator.getName().equals(selectorType))
				return configurator;
		}
		
		throw new ModelInformationException("Unrecognized selector: " + selectorType + ".");
	}
	
	//----------------------------------------------------------------------------------------------------
	private void loadOperators(final List<Operator> operatorList) throws ModelInformationException {
		selectedGeneticOperators.clear();
		
		for (final Operator operator : operatorList) {
			final String operatorType = operator.getType();
			final IGAOperatorConfigurator matchedConfigurator = findOperatorConfigurator(operatorType);
			final Map<String,String> config = convertPropertiesToMap(operator.getOperatorProperties());
			matchedConfigurator.setConfiguration(config);
			selectedGeneticOperators.add(matchedConfigurator);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private IGAOperatorConfigurator findOperatorConfigurator(final String operatorType) throws ModelInformationException {
		for (final IGAOperatorConfigurator configurator : geneticOperators) {
			if (configurator.getName().equals(operatorType))
				return configurator;
		}
		
		throw new ModelInformationException("Unrecognized genetic operator: " + operatorType + ".");
	}
	
	//----------------------------------------------------------------------------------------------------
	private void loadChromosome(final eu.crisis_economics.abm.dashboard.generated.Chromosome chromosome, final List<ParameterInfo> parameters) 
																																	throws ModelInformationException {
		final int childCount = getChromosomeTree().getChildCount(getChromosomeTree().getRoot());
		if (childCount > 0) {
			final DefaultMutableTreeNode root = (DefaultMutableTreeNode) getChromosomeTree().getRoot();
			for (int i = childCount - 1; i >= 0; --i) {
				final MutableTreeNode node = (MutableTreeNode) root.getChildAt(i);
				getChromosomeTree().removeNodeFromParent(node); 
			}
			genes = null;
		}
		
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) getChromosomeTree().getRoot();
		loadChromosomeTreeLevel(root,parameters,chromosome.getParameterList(),chromosome.getSubmodelParameterList(),chromosome.getGeneList());
	}
	
	//----------------------------------------------------------------------------------------------------
	private void loadChromosomeTreeLevel(final DefaultMutableTreeNode parent, final List<ParameterInfo> infos, final List<Parameter> parameters, 
										 final List<SubmodelParameter> submodelParameters, final List<eu.crisis_economics.abm.dashboard.generated.Gene> genes)
												 																					throws ModelInformationException {
		
		for (final eu.crisis_economics.abm.dashboard.generated.Gene gene : genes) {
			final ParameterInfo info = findParameterInfo(infos,gene.getName());
			if (info == null)
				throw new ModelInformationException("Unrecognized parameter: " + gene.getName() + ".");
			
			//initializeParent(info,parent);
			final ParameterOrGene userObj = initializeUserObjectFromGene(gene,info);
			final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(userObj);
			getChromosomeTree().insertNodeInto(newNode,parent,parent.getChildCount());
			
		}
		
		
		for (final SubmodelParameter parameter : submodelParameters) {
			final ParameterInfo info = findParameterInfo(infos,parameter.getName());
			if (info == null)
				throw new ModelInformationException("Unrecognized parameter: " + parameter.getName() + ".");
			
			//initializeParent(info,parent);
			final SubmodelInfo submodelInfo = (SubmodelInfo) info;
			final String strType = parameter.getType();
			try {
				final Class<?> clazz = Class.forName(strType,true,currentModelHandler.getCustomClassLoader());
				if (submodelInfo.getActualType() == null || ! strType.equals(submodelInfo.getActualType().getName())) {
					submodelInfo.setActualType(clazz, null);
				}
				
				final ParameterOrGene userObj = new ParameterOrGene(info);
				final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(userObj);
				getChromosomeTree().insertNodeInto(newNode,parent,parent.getChildCount());
				
				final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchSubparameters = ParameterTreeUtils.fetchSubparameters(currentModelHandler,
																																			 submodelInfo);
				final List<ParameterInfo> subparameters = new ArrayList<ParameterInfo>(batchSubparameters.size());
				for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> parameterInfo : batchSubparameters) 
					subparameters.add(InfoConverter.parameterInfo2ParameterInfo(parameterInfo));
				
				loadChromosomeTreeLevel(newNode,subparameters,parameter.getParameterList(),parameter.getSubmodelParameterList(),parameter.getGeneList());
			} catch (final ClassNotFoundException e) {
				throw new ModelInformationException("Invalid actual type " + strType + " for submodel parameter: " + submodelInfo.getName(),e);
			}	
		}
		
		for (final Parameter parameter : parameters) {
			final ParameterInfo info = findParameterInfo(infos,parameter.getName());
			if (info == null)
				throw new ModelInformationException("Unrecognized parameter: " + parameter.getName() + ".");
			
			//initializeParent(info,parent);
			info.setDefinitionType(ParameterInfo.CONST_DEF);
			String parameterValue = ParameterUtil.getParameterValue(parameter);
			Object value;
			if (info.getType().equals("File") && parameterValue == null){
				value = new File("");
			} else {
				value = ParameterInfo.getValue(parameterValue,info.getJavaType());
			}
			info.setValue(value);
			
			final ParameterOrGene userObj = new ParameterOrGene(info);
			final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(userObj);
			getChromosomeTree().insertNodeInto(newNode,parent,parent.getChildCount());
		}
		
	}

	//----------------------------------------------------------------------------------------------------
	private ParameterInfo findParameterInfo(final List<ParameterInfo> parameters, final String name) {
		for (final ParameterInfo info : parameters) {
			if (name.equals(info.getName()))
				return info;
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
//	private void initializeParent(final ParameterInfo parameterInfo, final DefaultMutableTreeNode parent) {
//		if (parent.getUserObject() != null && parameterInfo instanceof ISubmodelGUIInfo) {
//			final ISubmodelGUIInfo sgi = (ISubmodelGUIInfo) parameterInfo;
//			final ParameterOrGene parentUserObject = (ParameterOrGene) parent.getUserObject();
//			final SubmodelInfo parentInfo = (SubmodelInfo) parentUserObject.getInfo();
//			sgi.setParentValue(parentInfo.getActualType());
//		}
//	}
	
	//----------------------------------------------------------------------------------------------------
	private ParameterOrGene initializeUserObjectFromGene(final eu.crisis_economics.abm.dashboard.generated.Gene gene, final ParameterInfo info) {
		if (GeneInfo.INTERVAL.equals(gene.getGeneType())) {
			final boolean integerInterval = gene.isInteger();
			return integerInterval ? new ParameterOrGene(info,gene.getMin().longValue(),gene.getMax().longValue()) :
									 new ParameterOrGene(info,gene.getMin().doubleValue(),gene.getMax().doubleValue());
		} else {
			final List<Object> valueList = new ArrayList<Object>(gene.getGeneValueList().size());
			
			for (final String geneValue : gene.getGeneValueList()) 
				valueList.add(parseListElement(info.getJavaType(),geneValue));
			return new ParameterOrGene(info,valueList);
		}
	}

	//----------------------------------------------------------------------------------------------------
	private Map<String,String> convertPropertiesToMap(final List<Property> properties) {
		final Map<String,String> result = new HashMap<String,String>();
		for (final Property property : properties) 
			result.put(property.getKey(),property.getValue());
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private Object parseListElement(final Class<?> type, final String strValue) {
		if (type.equals(Double.class) || type.equals(Float.class) || type.equals(Double.TYPE) || type.equals(Float.TYPE))
			return Double.parseDouble(strValue);
		
		return Long.parseLong(strValue);
	}

	//----------------------------------------------------------------------------------------------------
	public void addFitnessFunction(final RecordableInfo fitnessFunction) {
		fitnessFunctions.add((RecordableInfo) fitnessFunction);
		
		for (final ModelListener listener : listeners) 
			listener.fitnessFunctionAdded();
	}
	
	//----------------------------------------------------------------------------------------------------
	public void removeAllFitnessFunctions() {
		fitnessFunctions.clear();
		fitnessFunctions.add(new RecordableInfo("Please select a function!",Object.class,"Please select a function!"));
		
		for (final ModelListener listener : listeners) 
			listener.fitnessFunctionsRemoved();
	}
	
	//----------------------------------------------------------------------------------------------------
	public List<IGASelectorConfigurator> getSelectionOperators() { return selectors; }
	public List<RecordableInfo> getFitnessFunctions() { return fitnessFunctions; }
	@Override
	public List<IStatisticsPlugin> getFitnessStatistics() {	return fitnessStatistics; }
	public List<IGAOperatorConfigurator> getGeneticOperators() { return geneticOperators; }
	public double getNumberOfTurns() { return numberOfTurns; }
	public int getPopulationSize() { return populationSize; }
	public int getPopulationRandomSeed() { return populationGenerationSeed;	}
	public int getNumberOfGenerations() { return numberOfGenerations; }
	public double getFitnessLimitCriterion() { return fitnessLimitCriterion; }
	public FitnessFunctionDirection getFitnessFunctionDirection() { return optimizationDirection; }
	public RecordableInfo getSelectedFitnessFunction() { return selectedFunction; }
	@Override
	public IStatisticsPlugin getSelectedFitnessStatistics() { return selectedFitnessStatistics;	}
	@Override
	public int getFitnessTimeSeriesLength() { return fitnessTimeSeriesLength; }
	public List<IGAOperatorConfigurator> getSelectedGeneticOperators() { return selectedGeneticOperators; }
	public List<IGASelectorConfigurator> getSelectedSelectionOperators() { return selectedSelectionOperators; }
	public boolean isFixNumberOfGenerations() { return fixNumberOfGenerations; }
	public File getWorkspace() { return workspace; }

	//----------------------------------------------------------------------------------------------------
	public void setNumberOfTurns(final double numberOfTurns) { this.numberOfTurns = numberOfTurns; }
	public void setPopulationSize(final int populationSize) { this.populationSize = populationSize; }
	public void setPopulationRandomSeed(final int seed) { this.populationGenerationSeed = seed; }
	public void setNumberOfGenerations(final int numberOfGenerations) { this.numberOfGenerations = numberOfGenerations; }
	public void setFitnessLimitCriterion(final double fitnessLimit) { this.fitnessLimitCriterion = fitnessLimit; }
	public void setFitnessFunctionDirection(final FitnessFunctionDirection direction) { this.optimizationDirection = direction; }
	@Override
	public void setSelectedFitnessFunction(final RecordableInfo fitnessFunction) { this.selectedFunction = (RecordableInfo) fitnessFunction; }
	public void setSelectedFitnessFunction(final String fitnessFunctionName){loadSelectedFitnessFunction(fitnessFunctionName);}
	@Override
	public void setSelectedFitnessStatistics(IStatisticsPlugin fitnessStatistics) { this.selectedFitnessStatistics = fitnessStatistics; }
	public void setSelectedFitnessStatistics(final String fitnessStatisticsName) {loadSelectedFitnessStatistics(fitnessStatisticsName);}
	@Override
	public void setFitnessTimeSeriesLength(int fitnessTimeSeriesLength) { this.fitnessTimeSeriesLength = fitnessTimeSeriesLength; }
	public void setFixNumberOfGenerations(final boolean fixNumberOfGenerations) { this.fixNumberOfGenerations = fixNumberOfGenerations; }
	public void setWorkspace(final File workspace) { this.workspace = workspace; }

	//----------------------------------------------------------------------------------------------------
	public DefaultTreeModel getChromosomeTree() {
		if (chromosomeTree == null)
			chromosomeTree = new DefaultTreeModel(new DefaultMutableTreeNode());
		return chromosomeTree;
	}
	//----------------------------------------------------------------------------------------------------
	public void setSelectedSelectionOperator(final IGASelectorConfigurator operator) {
		if (operator != null && !selectedSelectionOperators.contains(operator)) 
			selectedSelectionOperators.add(operator);
	}

	//----------------------------------------------------------------------------------------------------
	public boolean unsetSelectedSelectionOperator(final IGASelectorConfigurator operator) {
		return selectedSelectionOperators.remove(operator);
	}

	//----------------------------------------------------------------------------------------------------
	public void setSelectedGeneticOperator(final IGAOperatorConfigurator operator) {
		if (operator != null && !selectedGeneticOperators.contains(operator)) 
			selectedGeneticOperators.add(operator);
	}

	//----------------------------------------------------------------------------------------------------
	public boolean unsetSelectedGeneticOperator(final IGAOperatorConfigurator operator) {
		return selectedGeneticOperators.remove(operator);
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean addModelListener(final ModelListener listener) {
		return listeners.add(listener);
	}

	//----------------------------------------------------------------------------------------------------
	public boolean removeModelListener(final ModelListener listener) {
		return listeners.remove(listener);
	}

	public void addSubmodelParameter(final SubmodelInfo sInfo, final DefaultMutableTreeNode node) throws ModelInformationException {
		if (sInfo.getActualType() == null) {
			if (node.getChildCount() > 0) {
				node.removeAllChildren();
				chromosomeTree.nodeStructureChanged(node);
			}
			
			return;
		}
		
		final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = ParameterTreeUtils.fetchSubparameters(
																											currentModelHandler,sInfo);
		
		final List<ParameterInfo> convertedSubparameters = new ArrayList<ParameterInfo>(subparameters.size());
		for (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> parameterInfo : subparameters) {
			final ParameterInfo converted = InfoConverter.parameterInfo2ParameterInfo(parameterInfo);
			converted.setRuns(0);
			convertedSubparameters.add(converted);
		}
		Collections.sort(convertedSubparameters);
		
		if (node.getChildCount() > 0) {
			node.removeAllChildren();
			chromosomeTree.nodeStructureChanged(node);
		}
		
		for (final ParameterInfo pInfo : convertedSubparameters) {
			final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new ParameterOrGene(pInfo));
			chromosomeTree.insertNodeInto(newNode,node,node.getChildCount());
			
			if (pInfo instanceof SubmodelInfo) {
				final SubmodelInfo ssInfo = (SubmodelInfo) pInfo;
				if (ssInfo.getActualType() != null)
					addSubmodelParameter(ssInfo, newNode);
			}

		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public void addParameter(final ParameterOrGene parameterOrGene) throws ModelInformationException {
		if (chromosomeTree == null) { 
			chromosomeTree = new DefaultTreeModel(new DefaultMutableTreeNode());
			genes = null;
		}
		
		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(parameterOrGene);
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) chromosomeTree.getRoot();
		chromosomeTree.insertNodeInto(newNode,root,root.getChildCount());
		
		if (parameterOrGene.info instanceof SubmodelInfo) {
			final SubmodelInfo ssInfo = (SubmodelInfo) parameterOrGene.info;
			if (ssInfo.getActualType() != null)
				addSubmodelParameter(ssInfo, newNode);
		}
		
		for (final ModelListener listener : listeners) 
			listener.parameterAdded();
	}
	
	//----------------------------------------------------------------------------------------------------
	public void removeAllParameters() {
		if (chromosomeTree == null)  
			chromosomeTree = new DefaultTreeModel(new DefaultMutableTreeNode());
		else 
			chromosomeTree.setRoot(new DefaultMutableTreeNode());
		
		genes = null;
		
		for (final ModelListener listener : listeners) 
			listener.parametersRemoved();
	}
	
	//----------------------------------------------------------------------------------------------------
	public String[] checkGAModel() {
		final List<String> errors = new ArrayList<String>();
		
		final RecordableInfo invalid = new RecordableInfo("Please select a function!",Object.class,"Please select a function!");
		if (selectedFunction.equals(invalid))
			errors.add("Please select a valid fitness function!");
		
		if (selectedSelectionOperators.isEmpty())
			errors.add("Please select at least one selector!");
		
		if (selectedGeneticOperators.isEmpty())
			errors.add("Please select at least one genetic operator!");

		if (calculateNumberOfGenes() == 0)
			errors.add("Please specify at least one gene!");
		
		return errors.size() == 0 ? null : errors.toArray(new String[0]);
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean canBeFitnessFunction(final RecordableInfo candidate) {
		return acceptableReturnTypesForFitnessFunction.contains(candidate.getType());
	}

	//----------------------------------------------------------------------------------------------------
	public int getNumberOfIterations() {
		if (fixNumberOfGenerations)
			return numberOfGenerations - 1;
		else
			return Integer.MAX_VALUE;
	}
	
	//----------------------------------------------------------------------------------------------------
	public int getCurrentIteration() {
		return iterationCounter;
	}
	
	//----------------------------------------------------------------------------------------------------
	public boolean noMoreIteration() {
		if (fixNumberOfGenerations) 
			return iterationCounter > getNumberOfGenerations();
		else 
			return reachFitnessLimit;
	}
	
	//----------------------------------------------------------------------------------------------------
	public ParameterTree getNextParameterTree(final IParameterSweepResultReader reader) {
		if (iterationCounter == 1) // first time
			initPopFile();
		
		iterationCounter++;
		
		((ResultFileFitnessFunction)gaConfiguration.getFitnessFunction()).setCurrentReader(reader);
		
		printPopulation(iterationCounter - 1);
		
		if (!fixNumberOfGenerations) {
			final IChromosome fittestChromosome = genotype.getFittestChromosome();
			final double fitnessValue = gaConfiguration.getFitnessFunction().getFitnessValue(fittestChromosome);
			reachFitnessLimit = optimizationDirection == FitnessFunctionDirection.MINIMIZE ? fitnessValue <= fitnessLimitCriterion 
																						   : fitnessValue >= fitnessLimitCriterion;
																						   
			if (reachFitnessLimit) 
				return null;
		} 
		
		if (iterationCounter > getNumberOfGenerations()) return null;
		
		genotype.evolve();
		
		final ParameterTree nextTree = new ParameterTree();
		final Population descendants = genotype.getPopulation();

		for (int i = 0; i < paramList.size(); ++i) {
			final ParameterInfo paramInfo = paramList.get(i);
			paramInfo.setRuns(1); // Run is always 1

			final List<Object> values = new ArrayList<Object>();

			final int genIdx = whichGene(calculateName(paramInfo));
			for (int j = 0; j < descendants.size(); ++j) {
				IChromosome chromosome = descendants.getChromosome(j);
				if ( chromosome.getFitnessValueDirectly() > -1.0D ) {
					if (genIdx >= 0) {
						final String strValue = String.valueOf(chromosome.getGene(genIdx).getAllele());
						values.add(ParameterInfo.getValue(strValue,paramInfo.getType()));
						paramInfo.setDefinitionType(ParameterInfo.LIST_DEF);
					} else {
						values.add(paramInfo.getValue());
						paramInfo.setDefinitionType(ParameterInfo.CONST_DEF);
					}
				}
			}

			paramInfo.setValues(values);

			nextTree.addNode(InfoConverter.parameterInfo2ParameterInfo(paramInfo));
		}

		return nextTree;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean alterParameterTree(final IIntelliContext ctx) {

		// create initial population
		final DefaultMutableTreeNode root = ctx.getParameterTreeRootNode();
		final DefaultMutableTreeNode newRoot = getAlteredParameterTreeRootNode(ctx);
		root.removeAllChildren();
		final int count = newRoot.getChildCount();

		for (int i = 0; i < count; ++i) 
			root.add((DefaultMutableTreeNode) newRoot.getChildAt(0));
		
		return true;
	}

	//----------------------------------------------------------------------------------------------------
	protected DefaultMutableTreeNode getAlteredParameterTreeRootNode(final IIntelliContext ctx) {
		genotype = generateInitialPopulation(ctx);

		final Population descendants = genotype.getPopulation();
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Parameter file");

		paramList = ctx.getParameters();
		for (int i = 0; i < paramList.size(); ++i) {
			final ParameterInfo paramInfo = paramList.get(i);
			paramInfo.setRuns(1); // Run is always 1

			final List<Object> values = new ArrayList<Object>();

			final int genIdx = whichGene(calculateName(paramInfo));
			for (int j = 0; j < populationSize; ++j) {
				if (genIdx >= 0) {
					final String strValue = String.valueOf(descendants.getChromosome(j).getGene(genIdx).getAllele());
					values.add(ParameterInfo.getValue(strValue,paramInfo.getType()));
					paramInfo.setDefinitionType(ParameterInfo.LIST_DEF);
				} else {
					values.add(paramInfo.getValue());
					paramInfo.setDefinitionType(ParameterInfo.CONST_DEF);
				}
			}

			paramInfo.setValues(values);

			// add and save the node
			root.add(new DefaultMutableTreeNode(paramInfo));
		}

		return root;
	}

	//----------------------------------------------------------------------------------------------------
	/**
	 * Searches the selected genes list for the specified parameter, and returns
	 * its index.
	 * 
	 * @param name
	 *            the parameter's name that is searched in the selected genes list
	 * @return the index of the gene that represents the given parameter, or -1
	 *         if the parameter is not selected as a gene
	 */
	private int whichGene(final String name) {
		for (int i = 0; i < genes.size(); i++) {
			final GeneInfo geneInfo = genes.get(i);
			if (geneInfo.getName().equals(name)) 
				return i;
		}

		return -1;
	}
	
	//----------------------------------------------------------------------------------------------------
	private List<GeneInfo> getSelectedGenes() {
		if (genes == null) {
			genes = new ArrayList<GeneInfo>();
			
			final DefaultMutableTreeNode root = (DefaultMutableTreeNode) chromosomeTree.getRoot();
			@SuppressWarnings("rawtypes")
			final Enumeration nodes = root.preorderEnumeration();
			nodes.nextElement();
			while (nodes.hasMoreElements()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
				if (userObj.isGene()) {
					convertGeneName(userObj);
					genes.add(userObj.getGeneInfo());
				}
			}
		}
		
		return genes;
	}
	
	//----------------------------------------------------------------------------------------------------
	private void convertGeneName(final ParameterOrGene param) {
		ParameterInfo info = param.getInfo();
		final String newName = calculateName(info);  
		param.getGeneInfo().setName(newName);
	}
	
	//----------------------------------------------------------------------------------------------------
	private String calculateName(final ParameterInfo info) {
		ParameterInfo _info = info;
		
		if (_info != null) {
			String resultName = _info.getName();
			while ((_info instanceof ISubmodelGUIInfo) && ((ISubmodelGUIInfo)_info).getParent() != null) {
				_info = ((ISubmodelGUIInfo)_info).getParent();
				resultName = _info.getName() + "#" + resultName;
			}
			
			return resultName;
		}
		
		return null;
	}

	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	protected Genotype generateInitialPopulation(final IIntelliContext context) {
		try {
			iterationCounter = 1;
			genes = null;
			
			Configuration.reset();
			gaConfiguration = new DefaultConfiguration();
			Configuration.reset();
			gaConfiguration.removeNaturalSelectors(true);
			gaConfiguration.removeNaturalSelectors(false);
			gaConfiguration.getGeneticOperators().clear();
			gaConfiguration.setAlwaysCaculateFitness(false);
			gaConfiguration.setPreservFittestIndividual(true);
			gaConfiguration.setKeepPopulationSizeConstant(false);
			gaConfiguration.setFitnessEvaluator(new FitnessEvaluator() {
				public boolean isFitter(IChromosome a_chrom1, IChromosome a_chrom2) {
					return isFitter(a_chrom1.getFitnessValue(),a_chrom2.getFitnessValue());				}
				
				public boolean isFitter(double a_fitness_value1, double a_fitness_value2) {
					return optimizationDirection == FitnessFunctionDirection.MINIMIZE ? a_fitness_value1 < a_fitness_value2 : a_fitness_value1 > a_fitness_value2;
				}
			});
			gaConfiguration.setFitnessFunction(new ResultFileFitnessFunction());
			gaConfiguration.setRandomGenerator(new RandomGenerator() {
				private static final long serialVersionUID = 1L;

				final MersenneTwister randomGenerator = new MersenneTwister(populationGenerationSeed);

				public long nextLong() {
					return randomGenerator.nextLong();
				}

				public int nextInt(final int a_ceiling) {
					return randomGenerator.nextInt(a_ceiling);
				}

				public int nextInt() {
					return randomGenerator.nextInt();
				}

				public float nextFloat() {
					return randomGenerator.nextFloat();
				}

				public double nextDouble() {
					return randomGenerator.nextDouble();
				}

				public boolean nextBoolean() {
					return randomGenerator.nextBoolean();
				}
			});
			
			final Gene[] initialGenes = new Gene[getSelectedGenes().size()];
			int i = 0;
			for (final GeneInfo geneInfo : getSelectedGenes()) {
				if (GeneInfo.INTERVAL.equals(geneInfo.getValueType())) {
					if (geneInfo.isIntegerVals()) {
						initialGenes[i] = new IdentifiableLongGene(geneInfo.getName(),gaConfiguration,geneInfo.getMinValue().longValue(),
																	  geneInfo.getMaxValue().longValue());
					} else {
						initialGenes[i] = new IdentifiableDoubleGene(geneInfo.getName(),gaConfiguration,geneInfo.getMinValue().doubleValue(),
																	 geneInfo.getMaxValue().doubleValue());
					}
				} else {
					final IdentifiableListGene listGene = new IdentifiableListGene(geneInfo.getName(),gaConfiguration);
					final List<Object> values = geneInfo.getValueRange();
					for (final Object value : values) {
						listGene.addAllele(value);
					}
					listGene.setAllele(values.get(0));
					initialGenes[i] = listGene;
				}

				i++;
			}
			gaConfiguration.setMinimumPopSizePercent(100);
			gaConfiguration.setBreeder(new DashboardBreeder());
			
			final Chromosome sampleChromosome = new Chromosome(gaConfiguration,initialGenes);
			gaConfiguration.setSampleChromosome(sampleChromosome);
			gaConfiguration.setPopulationSize(populationSize);

			for (final IGAOperatorConfigurator operator : selectedGeneticOperators) 
				gaConfiguration.addGeneticOperator(operator.getConfiguredOperator(gaConfiguration));
			
			for (IGASelectorConfigurator selectorConfig : selectedSelectionOperators)
				gaConfiguration.addNaturalSelector(selectorConfig.getSelector(gaConfiguration),true);

			gaConfiguration.addNaturalSelector(new StandardPostSelectorFixed(gaConfiguration),false);

			final Genotype initialGenotype = Genotype.randomInitialGenotype(gaConfiguration);
			
			return initialGenotype;
		} catch (final InvalidConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	//----------------------------------------------------------------------------------------------------
	/**
	 * Prints the header of the population file.
	 */
	protected void initPopFile() {
		// create backup file when file already exists
		final File file = new File(workspace,populationFileName + ext);
		if (file.exists()) {
			for (int i = 1; i <= 1000; ++i) {
				final File newFile = new File(workspace,populationFileName + bckExt + i + ext);
				if (!newFile.exists() || i == 1000) {
					file.renameTo(newFile);
					break;
				}
			}
		}
		
		try {
			final PrintWriter popWriter = new PrintWriter(new FileWriter(file,true));

			popWriter.print("#pop");
			for (int i = 0; i < genes.size(); ++i) 
				popWriter.print(";" + genes.get(i).getName());
			
			popWriter.print(";fitness");
			popWriter.println();
			popWriter.close();
		} catch (final IOException e) {
			log.error("Cannot print population to file:\n",e);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	protected void printPopulation(final int iteration) {
		try {
			final PrintWriter popWriter = new PrintWriter(new FileWriter(new File(workspace,populationFileName + ext),true));
			
			Population population = genotype.getPopulation();
			for (int i = 0;i < population.size();++i) {
				popWriter.print(iteration);
				for (int j = 0;j < population.getChromosome(i).size(); ++j) 
					popWriter.print(";" + population.getChromosome(i).getGene(j).getAllele().toString());
				
				popWriter.print(";" + population.getChromosome(i).getFitnessValue());
				popWriter.println();
			}
			popWriter.close();
		} catch (final IOException e) {
			log.error( "Cannot print population to file.",e);
		}
	}

	
	//----------------------------------------------------------------------------------------------------
	private int calculateNumberOfGenes() {
		int count = 0;
		
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) chromosomeTree.getRoot();
		@SuppressWarnings("rawtypes")
		final Enumeration nodes = root.breadthFirstEnumeration();
		nodes.nextElement();
		
		while (nodes.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			final ParameterOrGene userObj = (ParameterOrGene) node.getUserObject();
			if (userObj.isGene())
				count++;
		}
		
		return count;
	}
	
	//====================================================================================================
	// not implemented methods
	
	//----------------------------------------------------------------------------------------------------
	public void setRecordableVariables(final DefaultMutableTreeNode root) {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public String settingsOK(final DefaultMutableTreeNode recorders) {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public void setParameterTreeRoot(final DefaultMutableTreeNode root) {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}


	//----------------------------------------------------------------------------------------------------
	public boolean getReadyStatus() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public String getReadyStatusDetail() { 
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public JPanel getSettingsPanel(final IIntelliContext ctx) {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public void save(final Node node) {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public void load(final IIntelliContext context, final Element element) throws WizardLoadingException {
		throw new UnsupportedOperationException(); // intentionally not implemented
	} 

	//----------------------------------------------------------------------------------------------------
	public void invalidatePlugin() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public String getDescription() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public int getMethodType() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	//----------------------------------------------------------------------------------------------------
	public boolean isImplemented() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}
	
	//----------------------------------------------------------------------------------------------------
	public String getLocalizedName() {
		throw new UnsupportedOperationException(); // intentionally not implemented
	}

	@Override
	public RecordableInfo getSelectedFitnessStatisticsInfo() {
		final RecordableInfo selectedFitnessFunction = getSelectedFitnessFunction();
		IStatisticsPlugin selectedFitnessStatistics = getSelectedFitnessStatistics();
		int fitnessTimeSeriesLength = getFitnessTimeSeriesLength();
		// this seems to be useless, as the GASearchPanelModel can directly return the selected fitness function
//		final List<RecordableInfo> recordables = recorder.getRecordables();
//		for (final RecordableInfo recordableInfo : recordables) {
//			if (recordableInfo.equals(selectedFitnessFunction))
//				return recordableInfo;
//		}
		
		if (selectedFitnessStatistics.equals(GASearchHandler.noStatistics)){
			return selectedFitnessFunction;
		}
		
		// create a time series of the specified length using the selected fitness function
		Operator_TimeSeries timeSeriesOperator = new Operator_TimeSeries();
		OperatorsInfoGenerator timeSeriesInfoGenerator = new OperatorsInfoGenerator(timeSeriesOperator);
		
		List<Object> parameters = new ArrayList<Object>();
		
		MemberInfo fitnessMemberInfo = new MemberInfo(selectedFitnessFunction.getAccessibleName(), selectedFitnessFunction.getType().getSimpleName(), selectedFitnessFunction.getType());

		parameters.add(fitnessMemberInfo);
		parameters.add(fitnessTimeSeriesLength);
		
		OperatorGeneratedMemberInfo timeSeriesMemberInfo = timeSeriesInfoGenerator.generateInfoObject("timeseries", parameters.toArray());
		List<String> errors = timeSeriesInfoGenerator.getError();
		if (errors != null && errors.size() > 0) {
			throw new IllegalStateException("Could not create the timeseries of the last '" + selectedFitnessFunction.getName() + "' values!\n" + Utils.join(errors,"\n"));
		}
		timeSeriesMemberInfo.setGeneratorName(timeSeriesOperator.getClass().getName());
		timeSeriesMemberInfo.setBuildingBlock(parameters);
		
		// apply the selected statistics on the timeseries
		IStatisticInfoGenerator statisticsInfoGenerator = new StatisticsInfoGenerator(selectedFitnessStatistics);
		parameters = new ArrayList<Object>(selectedFitnessStatistics.getNumberOfParameters());
		List<Object> collectionParameter = new ArrayList<Object>();
		collectionParameter.add(timeSeriesMemberInfo);
		parameters.add(collectionParameter);
		SimpleGeneratedMemberInfo statMemberInfo = statisticsInfoGenerator.generateInfoObject(selectedFitnessStatistics.getName() + "_last" + fitnessTimeSeriesLength + "_" + selectedFitnessFunction.getName(), parameters.toArray());
		errors = statisticsInfoGenerator.getError();
		if (errors != null && errors.size() > 0) {
			throw new IllegalStateException("Could not create the specified statistics (" + selectedFitnessStatistics.getName() + ") of '" + selectedFitnessFunction.getName() + "'!\n" + Utils.join(errors,"\n"));
		}
		statMemberInfo.clearBuildingBlocks();
		statMemberInfo.setGeneratorName(selectedFitnessStatistics.getClass().getName());
		
		List<MemberInfo> l = new ArrayList<MemberInfo>(1);
		l.add(timeSeriesMemberInfo);
		statMemberInfo.addBuildingBlock(l);
		
		String alias = statMemberInfo.getName().endsWith("()") ? statMemberInfo.getName().substring(0, statMemberInfo.getName().length() - 2) : statMemberInfo.getName();
		RecordableElement stdRecordableElement = new RecordableElement(statMemberInfo, alias); 
		
		
		return InfoConverter.convertRecordableElement2RecordableInfo(stdRecordableElement);
	}

	
	//====================================================================================================
	// nested classes
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private class ResultFileFitnessFunction extends FitnessFunction {
		
		//====================================================================================================
		// members
		
		private IParameterSweepResultReader currentReader;
		
		//====================================================================================================
		// methods
		
		//----------------------------------------------------------------------------------------------------
		public void setCurrentReader(final IParameterSweepResultReader currentReader) { this.currentReader = currentReader; } 

		//----------------------------------------------------------------------------------------------------
		protected double evaluate(final IChromosome a_subject) {
			if (currentReader == null)
				throw new IllegalStateException("Fitness function evaluation cannot access the results.");
			
			final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> combination = createCombinationFromChromosome(a_subject);
			try {
				final List<ResultValueInfo> infos = currentReader.getResultValueInfos(getSelectedFitnessStatisticsInfo(),combination);
				if (infos == null || infos.size() == 0) {
					return 1e19; // dirty hack to make Ross going; 
				}
				
				if(Double.isInfinite(getFitnessValue(infos).doubleValue())){
					return Double.MAX_VALUE;
				}
				if(Double.isNaN(getFitnessValue(infos).doubleValue())){
					return 1e19;
				}
				return getFitnessValue(infos).doubleValue();
			} catch (final ReadingException e) {
				throw new RuntimeException(e);
			}
		}

		//----------------------------------------------------------------------------------------------------
		private List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> createCombinationFromChromosome(final IChromosome a_subject) {
			final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> combination = 
																	new ArrayList<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>>(a_subject.getGenes().length);
			
			for (int i = 0;i < a_subject.getGenes().length;++i) {
				final IIdentifiableGene geneWithId = (IIdentifiableGene) a_subject.getGene(i);
				final GeneInfo geneInfo = genes.get(whichGene(geneWithId.getId()));
				final ParameterInfo info = new ParameterInfo(geneInfo.getName(),geneInfo.getType(),geneInfo.getJavaType());
				
				final String strValue = String.valueOf(geneWithId.getAllele());
				info.setValue(ParameterInfo.getValue(strValue,info.getType()));
				
				combination.add((ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>)InfoConverter.parameterInfo2ParameterInfo(info));
			}
			
			return combination;
		}
		
		//----------------------------------------------------------------------------------------------------
		private Number getFitnessValue(final List<ResultValueInfo> candidates) {
			if (candidates == null || candidates.isEmpty())
				return new Double(FitnessFunction.NO_FITNESS_VALUE);
//				return new Double(optimizationDirection == FitnessFunctionDirection.MAXIMIZE ? 0. :  Double.MAX_VALUE);
			
			Collections.sort(candidates,new Comparator<ResultValueInfo>() {
				public int compare(final ResultValueInfo info1, final ResultValueInfo info2) {
					final double tick1 = (Double) info1.getLabel();
					final double tick2 = (Double) info2.getLabel();
					
					return Double.compare(tick1,tick2);
				}
			});
			
			final Object value = candidates.get(candidates.size() - 1).getValue();
			return (Number) value;
		}
	}
}