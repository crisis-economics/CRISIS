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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBElement;

import ai.aitia.meme.paramsweep.batch.IModelInformation.ModelInformationException;
import ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.AbstractParameterInfo.ValueType;
import ai.aitia.meme.paramsweep.batch.param.IncrementalParameterInfo;
import ai.aitia.meme.paramsweep.batch.param.ParameterNode;
import ai.aitia.meme.paramsweep.batch.param.ParameterTree;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;
import ai.aitia.meme.paramsweep.internal.platform.InfoConverter;
import ai.aitia.meme.utils.Utils.Pair;
import eu.crisis_economics.abm.dashboard.generated.Combination;
import eu.crisis_economics.abm.dashboard.generated.DefaultParameter;
import eu.crisis_economics.abm.dashboard.generated.Model;
import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;
import eu.crisis_economics.abm.dashboard.generated.Parameter;
import eu.crisis_economics.abm.dashboard.generated.ParameterType;
import eu.crisis_economics.abm.dashboard.generated.SubmodelParameter;
import eu.crisis_economics.abm.dashboard.jaxb.util.ParameterUtil;

public class ParameterTreeUtils {

	public static final String UPDATE_CHARTS_XML_NAME = "Update charts in real-time";
	public static final String DISPLAY_ADVANCED_CHARTS_XML_NAME = "Display advanced set of charts";
	public static final String NUMBER_OF_TURNS_XML_NAME = "Maximum number of time-steps";
	public static final String NUMBER_OF_TIMESTEPS_TO_IGNORE_XML_NAME = "Start charting after time-step";
	public static final String FITNESS_FUNCTION_XML_NAME = "Fitness function";
	public static final String FITNESS_STATISTICS_XML_NAME = "Fitness statistics";
	public static final String FITNESS_TIMESERIES_LENGTH_XML_NAME = "Fitness time-series length";


	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------

	public static void addPSParameterTree2Config(final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> originalParameters, final ParameterTree parameterTree, final Model model) {
		final ObjectFactory factory = new ObjectFactory();
		ParameterNode rootNode = parameterTree.getRoot();
		int combinationNumber = 0;
		List<Integer> branchIndexes = new ArrayList<Integer>();
		List<Parameter> constantParameters = new ArrayList<Parameter>();

		// first add all default parameters and collect the parallel branches
		for (int i = 0 ; i < rootNode.getChildCount() ; i++) {
			ParameterNode parameterNode = (ParameterNode) rootNode.getChildAt(i);
			
			if (parameterNode.isLeaf()) {
				AbstractParameterInfo<Object> parameterInfo = parameterNode.getParameterInfo();
				if (parameterInfo.getValueType() == ValueType.CONSTANT) {
					final Parameter parameter = factory.createParameter();
					parameter.setName(parameterInfo.getName());
					parameter.setPosition(BigInteger.valueOf(constantParameters.size() + 1));
					ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object> constantInfo = (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object>) parameterInfo;
					parameter.setParameterType(ParameterType.CONSTANT);
					parameter.getContent().add(constantInfo.getValues().get(0).toString());
					constantParameters.add(parameter);	
					continue;
				}
			}

			branchIndexes.add(i);
			combinationNumber++;
		}
		
		// iterate over all branches and add them to the config as combinations
		for (int i = 0 ; i < combinationNumber ; i++) {
			final Combination combination = factory.createCombination();
			combination.setPosition(BigInteger.valueOf(i + 1));
			model.getCombination().add(combination);
			int parameterPosition = 1;
			// add all constant parameters to the first combination
			if (i == 0) {
				for (Parameter parameter : constantParameters) {
					combination.getParameterList().add(parameter);
				}
				
				parameterPosition = constantParameters.size();
			}
			
			ParameterNode parameterNode = (ParameterNode) rootNode.getChildAt(branchIndexes.get(i));
			savePSParameter(parameterNode, factory, combination, parameterPosition);
		}

	}
	
	//----------------------------------------------------------------------------------------------------
	public static DefaultMutableTreeNode loadSingleRunParameterIntoInfoValueTree(final Model config,
																				 final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infos,
																				 final IModelHandler modelHandler) throws ModelInformationException {
		final DefaultMutableTreeNode result = new DefaultMutableTreeNode();
		loadSingleRunParameterIntoInfoValueTree(result,config,infos,modelHandler);
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	public static void loadSingleRunParameterIntoInfoValueTree(final DefaultMutableTreeNode parent, final Model config, 
			 												   final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infos, 
			 												   final IModelHandler modelHandler) throws ModelInformationException {
		
		final List<Parameter> parameterList = config.getParameterList();
		fillParameterValueTree(parent,parameterList,infos);
		
		final List<SubmodelParameter> submodelParameterList = config.getSubmodelParameterList();
		fillParameterValueTreeWithSubModelParameters(parent,submodelParameterList,infos,modelHandler);
	}
	
	//----------------------------------------------------------------------------------------------------
	public static List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> fetchSubparameters(final IModelHandler modelHandler, final SubmodelInfo info)
																																	throws ModelInformationException {
		final ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<?> convertedInfo = 
															(ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<?>) InfoConverter.parameterInfo2ParameterInfo(info);
		final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = modelHandler.getSubparameters(convertedInfo);
		return subparameters;
	}
	
	//----------------------------------------------------------------------------------------------------
	public static void loadPSParameters(final DefaultListModel defaultParameterModel, final List<DefaultTreeModel> combinationModels, final IModelHandler modelHandler,
										final Model config) throws ModelInformationException {
		final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters = modelHandler.getParameters();
		
		final List<DefaultParameter> defaultParameters = config.getDefaultParameterList();
		loadPSDefaultParameters(defaultParameterModel,defaultParameters,batchParameters, modelHandler.getModelClass());
		
		final List<Combination> sortedCombinations = new ArrayList<Combination>(config.getCombination());
		Collections.sort(sortedCombinations,new Comparator<Combination>(){
			public int compare(final Combination o1, final Combination o2) {
				return o1.getPosition().compareTo(o2.getPosition());
			}
		});
		
		for (int i = 0;i < sortedCombinations.size();++i) {
			final DefaultMutableTreeNode combinationRoot = new DefaultMutableTreeNode();
			final DefaultTreeModel treeModel = new DefaultTreeModel(combinationRoot);
			ParameterTreeUtils.loadAPSCombinationTree(treeModel,combinationRoot,batchParameters,modelHandler,sortedCombinations.get(i));
			combinationModels.add(treeModel);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public static void loadAPSCombinationTree(final DefaultTreeModel treeModel, final DefaultMutableTreeNode combinationRoot, 
									  		final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters, final IModelHandler modelHandler,
									  		final Combination combination) throws ModelInformationException {
		fillACombinationTreeLevel(treeModel,combinationRoot,combination.getParameterList(),combination.getSubmodelParameterList(),batchParameters,modelHandler);
		
	}

	//----------------------------------------------------------------------------------------------------
	public static void loadPSDefaultParameters(final DefaultListModel listModel, final List<DefaultParameter> defaultParameters,
											   final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters,
											   final Class<?> modelClass) throws ModelInformationException {
		for (final DefaultParameter defaultParameter : defaultParameters) {
			final ParameterInfo info = findParameterInfo(batchParameters,defaultParameter.getName());
			
			if (info != null) 
				listModel.addElement(new AvailableParameter(info, modelClass));
			else
				throw new ModelInformationException("Unknown parameter: " + defaultParameter.getName());
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public static void addConstantParameterToParameterTree(final AbstractParameterInfo<?> batchParameter, final ParameterTree tree, final IModelHandler modelHandler)
																																	throws ModelInformationException {
		if (batchParameter instanceof ai.aitia.meme.paramsweep.batch.param.SubmodelInfo) {
			final ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<?> sInfo = (ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<?>) batchParameter;
			final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subParameters = modelHandler.getSubparameters(sInfo);
			for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> parameterInfo : subParameters) 
				addConstantParameterToParameterTree(parameterInfo,tree,modelHandler);
		} else {
			batchParameter.setRunNumber(1);
			tree.addNode(batchParameter);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public static void filterConstants(final ParameterInATree paramContainer, final ParameterTree tree, final IModelHandler modelHandler) {
		if (paramContainer.info instanceof SubmodelInfo) return;
		
		if (paramContainer.info.getMultiplicity() == 1) {
			transformToConstant(paramContainer.info);
			AbstractParameterInfo<?> batchParameter = InfoConverter.parameterInfo2ParameterInfo(paramContainer.info);
			try {
				addConstantParameterToParameterTree(batchParameter,tree,modelHandler);
			} catch (final ModelInformationException e) {
				// never happens
				throw new IllegalStateException(e);
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public static boolean isMutableParameter(final ParameterInATree paramContainer) {
		return !(paramContainer.info instanceof SubmodelInfo) && paramContainer.info.getMultiplicity() > 1;
	}

	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private ParameterTreeUtils() {}
	
	//----------------------------------------------------------------------------------------------------
	private static void fillParameterValueTree(final DefaultMutableTreeNode parent, final List<Parameter> config, 
										 	   final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infos) throws ModelInformationException {
		
		for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> batchParameterInfo : infos) {
			final ParameterInfo parameterInfo = InfoConverter.parameterInfo2ParameterInfo(batchParameterInfo);
			if (parameterInfo instanceof SubmodelInfo) continue;
			
			final Object configParameterObj = findInConfig(parameterInfo,config);
			if (configParameterObj != null) {
//				initializeParent(parameterInfo,parent);
				final Parameter configParameter = (Parameter) configParameterObj;
				final String strValue = configParameter.getContent().isEmpty() ? "" : ParameterUtil.getParameterValue(configParameter);
				final Pair<ParameterInfo,String> pair = new Pair<ParameterInfo,String>(parameterInfo,strValue);
				final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pair);
				parent.add(newNode);
			} else {
				//throw new ModelInformationException("Parameter " + batchParameterInfo.getName() + " not found in the model configuration.");
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void fillParameterValueTreeWithSubModelParameters(final DefaultMutableTreeNode parent, final List<SubmodelParameter> config,
												 				   	 final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infos,
												 				   	 final IModelHandler modelHandler) throws ModelInformationException {
		for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> batchParameterInfo : infos) {
			final ParameterInfo parameterInfo = InfoConverter.parameterInfo2ParameterInfo(batchParameterInfo);
			if (parameterInfo instanceof SubmodelInfo) {
				final Object configParameterObj = findInConfig(parameterInfo,config);
				if (configParameterObj != null) {
//					initializeParent(parameterInfo,parent);
					SubmodelParameter configParameter = (SubmodelParameter) configParameterObj;
					final String strType = configParameter.getType();
					final Pair<ParameterInfo,String> pair = new Pair<ParameterInfo,String>(parameterInfo,strType);
					final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pair);
					parent.add(newNode);
					
					final SubmodelInfo submodelInfo = (SubmodelInfo) parameterInfo;
					try {
						final Class<?> clazz = Class.forName(strType,true,modelHandler.getCustomClassLoader());
						if (submodelInfo.getActualType() == null || ! strType.equals(submodelInfo.getActualType().getName())) {
							submodelInfo.setActualType(clazz, null);
						}
						
						final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = fetchSubparameters(modelHandler,submodelInfo);
						fillParameterValueTree(newNode,configParameter.getParameterList(),subparameters);
						fillParameterValueTreeWithSubModelParameters(newNode,configParameter.getSubmodelParameterList(),subparameters,modelHandler);
					} catch (final ClassNotFoundException e) {
						throw new ModelInformationException("Invalid actual type " + strType + " for submodel parameter: " + submodelInfo.getName(),e);
					}
				} else {
					//throw new ModelInformationException("Parameter " + batchParameterInfo.getName() + " not found in the model configuration.");
				}
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private static Object findInConfig(final ParameterInfo parameterInfo, final List<?> config) {
		for (final Object obj : config) {
			if (obj instanceof Parameter) {
				final Parameter parameter = (Parameter) obj;
				if (parameter.getName().equals(parameterInfo.getName()))
					return parameter;
			} else if (obj instanceof SubmodelParameter) {
				final SubmodelParameter parameter = (SubmodelParameter) obj;
				if (parameter.getName().equals(parameterInfo.getName()))
					return parameter;
			}
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
//	private static void initializeParent(final ParameterInfo parameterInfo, final DefaultMutableTreeNode parent) {
//		if (parent.getUserObject() != null && parameterInfo instanceof ISubmodelGUIInfo) {
//			final ISubmodelGUIInfo sgi = (ISubmodelGUIInfo) parameterInfo;
//			@SuppressWarnings("unchecked")
//			final Pair<ParameterInfo,String> parentUserObject = (Pair<ParameterInfo,String>) parent.getUserObject();
//			final SubmodelInfo parentInfo = (SubmodelInfo) parentUserObject.getFirst();
//			sgi.setParentValue(parentInfo.getActualType());
//		}
//	}
	
	//----------------------------------------------------------------------------------------------------
	private static void fillACombinationTreeLevel(final DefaultTreeModel treeModel, final DefaultMutableTreeNode parent, final List<Parameter> parameters,
										   		  final List<SubmodelParameter> submodelParameters, 
										   		  final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infoList, final IModelHandler modelHandler)
										   				  																			throws ModelInformationException {
		final List<Object> allParameters = new ArrayList<Object>(parameters.size() + submodelParameters.size());
		allParameters.addAll(parameters);
		allParameters.addAll(submodelParameters);
		Collections.sort(allParameters,new ParameterPositionComparator());

		for (final Object paramObj : allParameters) {
			DefaultMutableTreeNode newNode = null;
			if (paramObj instanceof Parameter) 
				newNode = loadAParameterNode((Parameter)paramObj,infoList, modelHandler.getModelClass());
			else if (paramObj instanceof SubmodelParameter) 
				newNode = loadASubmodelNode((SubmodelParameter)paramObj,infoList,treeModel,modelHandler);
			
			if (newNode != null)
				treeModel.insertNodeInto(newNode,parent,parent.getChildCount());
		}
		
	}
	
	//----------------------------------------------------------------------------------------------------
	private static DefaultMutableTreeNode loadAParameterNode(final Parameter param, final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infoList, final Class<?> modelClass) 
																																	throws ModelInformationException {
		final ParameterInfo info = findParameterInfo(infoList,param.getName());
		
		if (info != null) {
			switch (param.getParameterType()) {
			case CONSTANT : loadConstantParameter(info,param);
							break;
			case LIST : loadListParameter(info,param);
						break;
			case INCREMENT : loadIncrementParameter(info,param);
							 break;
			}
		} else
			throw new ModelInformationException("Unknown parameter: " + param.getName());
		
		final ParameterInATree userObj = new ParameterInATree(info, modelClass);
		final DefaultMutableTreeNode result = new DefaultMutableTreeNode(userObj);
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	private static DefaultMutableTreeNode loadASubmodelNode(final SubmodelParameter param, final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> infoList,
													 		final DefaultTreeModel treeModel, final IModelHandler modelHandler) throws ModelInformationException {
		final ParameterInfo info = findParameterInfo(infoList,param.getName());
		
		if (info != null) {
			if (info instanceof SubmodelInfo) {
				final SubmodelInfo sInfo = (SubmodelInfo) info;
				
				final ParameterInATree userObj = new ParameterInATree(info, modelHandler.getModelClass());
				final DefaultMutableTreeNode result = new DefaultMutableTreeNode(userObj);
				
				if (!"null".equals(param.getType())) {
					try {
						final Class<?> clazz = Class.forName(param.getType(),true,modelHandler.getCustomClassLoader());
						if (sInfo.getActualType() == null || ! param.getType().equals(sInfo.getActualType().getName())) {
							sInfo.setActualType(clazz, null);
						}
						
						final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> subparameters = 
																									ParameterTreeUtils.fetchSubparameters(modelHandler,sInfo);
						fillACombinationTreeLevel(treeModel,result,param.getParameterList(),param.getSubmodelParameterList(),subparameters,modelHandler);
						
					} catch (final ClassNotFoundException e) {
						throw new ModelInformationException("Unknown type: " + param.getType(),e);
					}
				}
				
				return result;
			} else
				throw new ModelInformationException("We expected a submodel parameter for name " + param.getName() + ", but found an ordinary one instead.");
		} else
			throw new ModelInformationException("Unknown parameter: " + param.getName());
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void loadConstantParameter(final ParameterInfo info, final Parameter param) {
		info.setDefinitionType(ParameterInfo.CONST_DEF);
		info.setRuns(0);
		final String valueStr = ParameterUtil.getParameterValue(param);
		if (valueStr != null)
			info.setValue(convertValue(info,valueStr));
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void loadListParameter(final ParameterInfo info, final Parameter param) {
		info.setDefinitionType(ParameterInfo.LIST_DEF);
		info.setRuns(0);
		final List<String> parameterValuesAsStrings = ParameterUtil.getParameterValues(param);
		final List<Object> parameterValues = new ArrayList<Object>(parameterValuesAsStrings.size());
		
		for (final String valueStr : parameterValuesAsStrings)  
			parameterValues.add(convertValue(info,valueStr));
		info.setValues(parameterValues);
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void loadIncrementParameter(final ParameterInfo info, final Parameter param) throws ModelInformationException {
		info.clear();
		info.setRuns(0);
		info.setDefinitionType(ParameterInfo.INCR_DEF);
		
		String valueStr = ParameterUtil.getParameterStartValue(param);
		Object value = convertValue(info,valueStr);
		
		if (value instanceof Number)
			info.setStartValue((Number)value);
		else
			throw new ModelInformationException("Invalid start value (" + valueStr + ") of parameter " + param.getName());
		
		valueStr = ParameterUtil.getParameterEndValue(param);
		value = convertValue(info,valueStr);
		
		if (value instanceof Number)
			info.setEndValue((Number)value);
		else
			throw new ModelInformationException("Invalid end value (" + valueStr + ") of parameter " + param.getName());

		valueStr = ParameterUtil.getParameterStepValue(param);
		value = convertValue(info,valueStr);
		
		if (value instanceof Number)
			info.setStep((Number)value);
		else
			throw new ModelInformationException("Invalid step value (" + valueStr + ") of parameter " + param.getName());
	}
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings({"unchecked","rawtypes"})
	private static Object convertValue(final ParameterInfo info, final String value) {
		if (info.isEnum()) 
			return Enum.valueOf((Class<? extends Enum>)info.getJavaType(),value);
		else
			return ParameterInfo.getValue(value,info.getType());
	}
	
	//----------------------------------------------------------------------------------------------------
	private static ParameterInfo findParameterInfo(final List<ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?>> batchParameters, final String name) {
		for (final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<?> batchInfo : batchParameters) {
			if (name.equals(batchInfo.getName()))
				return InfoConverter.parameterInfo2ParameterInfo(batchInfo);
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void transformToConstant(final ParameterInfo info) {
		if (info.isConstant())
			return;
		else if (info.getDefinitionType() == ParameterInfo.INCR_DEF) {
			info.setValue(info.getStartValue());
			info.setStartValue(null);
			info.setEndValue(null);
			info.setStep(null);
		}
		
		info.setDefinitionType(ParameterInfo.CONST_DEF);
	}

	//----------------------------------------------------------------------------------------------------
	private static void savePSParameter(final ParameterNode node, final ObjectFactory factory, final Object parent, final int position) {
		final AbstractParameterInfo<Object> info = node.getParameterInfo();
		
		if (info instanceof ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<?>) {
			final ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<Object> sInfo = (ai.aitia.meme.paramsweep.batch.param.SubmodelInfo<Object>) info;
			
			final SubmodelParameter parameter = factory.createSubmodelParameter();
			parameter.setName(info.getName());
			parameter.setType(sInfo.getActualType() == null ? "null" : sInfo.getActualType().getName());
			parameter.setPosition(BigInteger.valueOf(position));
			
			if (parent instanceof Combination) {
				final Combination combination = (Combination) parent;
				combination.getSubmodelParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getSubmodelParameterList().add(parameter);
			}
			
			if (node.getChildCount() > 0) {
				@SuppressWarnings("rawtypes")
				final Enumeration childNodes = node.children();
				int childPosition = 1;
				while (childNodes.hasMoreElements()) {
					final ParameterNode childNode =  (ParameterNode) childNodes.nextElement();
					savePSParameter(childNode,factory,parameter,childPosition++);
 				}
			}
		} else {
			final Parameter parameter = factory.createParameter();
			parameter.setName(info.getName());
			parameter.setPosition(BigInteger.valueOf(position));
			
			switch (info.getValueType()) {
			case CONSTANT:	
				ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object> constantInfo = (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object>) info;
				parameter.setParameterType(ParameterType.CONSTANT);
				parameter.getContent().add(constantInfo.getValues().get(0).toString());
				break;
				
			case LIST:
				ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object> listInfo = (ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object>) info;
				parameter.setParameterType(ParameterType.LIST);
				savePSListParameter(listInfo,parameter,factory);
				break;
			
			case INCREMENT:
				parameter.setParameterType(ParameterType.INCREMENT);
				savePSIncrementParameter((IncrementalParameterInfo<? super Number>) info,parameter,factory);
				break;
			}
			
			if (parent instanceof Combination) {
				final Combination combination = (Combination) parent;
				combination.getParameterList().add(parameter);
			} else if (parent instanceof SubmodelParameter) {
				final SubmodelParameter submodelParameter = (SubmodelParameter) parent;
				submodelParameter.getParameterList().add(parameter);
			}
		}
	}

	//----------------------------------------------------------------------------------------------------
	private static void savePSListParameter(final ai.aitia.meme.paramsweep.batch.param.ParameterInfo<Object> info, final Parameter parameter, final ObjectFactory factory) {
		for (final Object listElement : info.getValues()) {
			final JAXBElement<String> element = factory.createParameterParameterValue(listElement.toString());
			parameter.getContent().add(element);
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	private static void savePSIncrementParameter(final IncrementalParameterInfo<? super Number> info, final Parameter parameter, final ObjectFactory factory) {
		JAXBElement<String> element = factory.createParameterStartValue(info.getStart().toString());
		parameter.getContent().add(element);
		element = factory.createParameterEndValue(info.getEnd().toString());
		parameter.getContent().add(element);
		element = factory.createParameterStepValue(info.getIncrement().toString());
		parameter.getContent().add(element);
	}

	//====================================================================================================
	// nested classes
	
	//----------------------------------------------------------------------------------------------------
	private static class ParameterPositionComparator implements Comparator<Object> {

		//====================================================================================================
		// methods
		
		//----------------------------------------------------------------------------------------------------
		public int compare(final Object o1, final Object o2) {
			int pos1 = Integer.MAX_VALUE;
			if (o1 instanceof Parameter) 
				pos1 = ((Parameter)o1).getPosition().intValue();
			else if (o1 instanceof SubmodelParameter)
				pos1 = ((SubmodelParameter)o1).getPosition().intValue();
			
			int pos2 = Integer.MAX_VALUE;
			if (o2 instanceof Parameter) 
				pos2 = ((Parameter)o2).getPosition().intValue();
			else if (o2 instanceof SubmodelParameter)
				pos2 = ((SubmodelParameter)o2).getPosition().intValue();
			
			if (pos1 == pos2) 
				return 0;
			return pos1 < pos2 ? -1 : 1; 
		}
	}

}