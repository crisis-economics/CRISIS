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
package eu.crisis_economics.abm.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.Aspects;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import sim.engine.SimState;
import sim.engine.Steppable;

import com.rits.cloning.Cloner;

import eu.crisis_economics.abm.annotation.Collect;
import eu.crisis_economics.abm.annotation.Collect.ChangeType;
import eu.crisis_economics.abm.annotation.DataCollector;
import eu.crisis_economics.abm.annotation.Report;
import eu.crisis_economics.abm.annotation.ReportKey;
import eu.crisis_economics.abm.annotation.ReportSource;
import eu.crisis_economics.abm.annotation.ReportValue;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author Tamás Máhr
 *
 */
@Aspect
public class DataCollection {

	/**
	 * A map containing {@link DataCollector} objects per annotation names. That
	 * is, the keys in the map are the values of the {@link Collect} annotations.
	 */
	protected Map<String, Set<Object>> collectors = new HashMap<String, Set<Object>>();
	
	// { class : { annotationValue : { changeType : [ field, ...]}}}
	protected Map<Class<?>, Map<String, Map<ChangeType, Set<Field>>>> collectorsFieldReflections = new HashMap<Class<?>, Map<String, Map<ChangeType, Set<Field>>>>();
	protected Map<Class<?>, Map<String, Map<ChangeType, Set<Method>>>> collectorsMethodReflections = new HashMap<Class<?>, Map<String, Map<ChangeType, Set<Method>>>>();

	// { interval : { order : [ field, ...]}}
	protected Map<Double, Map<Integer, Set<ScheduledReport>>> scheduledReporterFields = new HashMap<Double, Map<Integer,Set<ScheduledReport>>>();
	protected Map<Double, Map<Integer, Set<ScheduledReport>>> scheduledReporterMethods = new HashMap<Double, Map<Integer,Set<ScheduledReport>>>();

	private Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forClassLoader()).setScanners(
			new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner()));

	private Cloner cloner = new Cloner();
	
	protected class ScheduledReport {
		Object target;
		
		Field field;
		
		Method method;

		/**
		 * @param target
		 * @param field
		 * @param method
		 */
		public ScheduledReport(Object target, Field field, Method method) {
			super();
			this.target = target;
			this.field = field;
			this.method = method;
		}
		
	}
	
	@SuppressWarnings("serial")
	protected class ScheduledReporter implements Steppable {

		protected double interval;
		
		protected int order;
		
		/**
		 * @param interval
		 * @param order
		 */
		public ScheduledReporter(double interval, int order) {
			super();
			this.interval = interval;
			this.order = order;
		}

		@Override
		public void step(SimState state) {
			// first report the annotated field values
			Map<Integer, Set<ScheduledReport>> intervalMap = scheduledReporterFields.get(interval);
			if (intervalMap != null){
				Set<ScheduledReport> reporterFields = intervalMap.get(order);
				if (reporterFields != null){
					for (ScheduledReport report : reporterFields) {
						Report annotation = report.field.getAnnotation(Report.class);
						Set<Object> set = collectors.get(annotation.value());
						if (set != null){
							for (Object collector : set) {
								try {
									handleData(annotation.value(), report.field.get(report.target), collector, report.target, Collect.ChangeType.CHANGE);
								} catch (IllegalArgumentException e) {
									throw new RuntimeException(e);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
				}
			}
			
			// then report the annotated method values
			intervalMap = scheduledReporterMethods.get(interval);
			if (intervalMap != null){
				Set<ScheduledReport> reporterMethods = intervalMap.get(order);
				if (reporterMethods != null){
					for (ScheduledReport report : reporterMethods) {
						Report annotation = report.method.getAnnotation(Report.class);
						Set<Object> set = collectors.get(annotation.value());
						if (set != null){
							for (Object collector : set) {
								try {
									handleData(annotation.value(), report.method.invoke(report.target), collector, report.target, Collect.ChangeType.CHANGE);
								} catch (IllegalArgumentException e) {
									throw new RuntimeException(e);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								} catch (InvocationTargetException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
				}
			}
			state.schedule.scheduleOnceIn(interval, this, order);
		}		
	}
	
	//------------------- advices --------------
	
	/**
	 * An advice woven in any constructor of classes annotated as
	 * {@link eu.crisis_economics.abm.annotation.DataCollector}. The advice
	 * looks up the fields of the data collector annotated with the
	 * {@link Collect} annotation, and adds the data collector to the collectors
	 * map using the value of the {@link Collect} annotation as key.
	 * 
	 * @param collector an object of a class annotated as {@link eu.crisis_economics.abm.annotation.DataCollector}.
	 */
	@After("execution((@eu.crisis_economics.abm.annotation.DataCollector *).new(..)) && this(collector)")
	public void findUseNewsFields(Object collector){
		Map<String, Map<ChangeType, Set<Field>>> collectorFieldReflections = collectorsFieldReflections.get(collector.getClass());
		Map<String, Map<ChangeType, Set<Method>>> collectorMethodReflections = collectorsMethodReflections.get(collector.getClass());
		if (collectorFieldReflections == null){
			collectorFieldReflections = new HashMap<String, Map<ChangeType, Set<Field>>>();
			collectorsFieldReflections.put(collector.getClass(), collectorFieldReflections);

			Set<Field> fieldsAnnotatedWith = reflections.getFieldsAnnotatedWith(Collect.class);
			
			for (Field field : fieldsAnnotatedWith) {
				if (field.getDeclaringClass().equals(collector.getClass())){
					Collect collect = field.getAnnotation(Collect.class);
					Map<ChangeType, Set<Field>> useNewsMap = collectorFieldReflections.get(collect.value());
					if (useNewsMap == null){
						useNewsMap = new HashMap<Collect.ChangeType, Set<Field>>();
						collectorFieldReflections.put(collect.value(), useNewsMap);
					}

					List<ChangeType> changeTypes = new ArrayList<Collect.ChangeType>(Arrays.asList(collect.changeType()));
					
					if (changeTypes.contains(ChangeType.ANY)){
						changeTypes.clear();
						for (ChangeType changeType : ChangeType.values()) {
							changeTypes.add(changeType);
						}
					}
					for (ChangeType changeType : changeTypes) {
						Set<Field> useNewsSet = useNewsMap.get(changeType);
						if (useNewsSet == null){
							useNewsSet = new HashSet<Field>();
							useNewsMap.put(changeType, useNewsSet);
						}
						field.setAccessible(true);
						useNewsSet.add(field);
					}
				}
			}
			
			collectorMethodReflections = new HashMap<String, Map<ChangeType, Set<Method>>>();
			collectorsMethodReflections.put(collector.getClass(), collectorMethodReflections);
			
			Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Collect.class);
			
			for (Method method : methodsAnnotatedWith) {
				if (method.getDeclaringClass().equals(collector.getClass())){
					Collect annotation = method.getAnnotation(Collect.class);
					Map<ChangeType, Set<Method>> useNewsMap = collectorMethodReflections.get(annotation.value());
					if (useNewsMap == null){
						useNewsMap = new HashMap<Collect.ChangeType, Set<Method>>();
						collectorMethodReflections.put(annotation.value(), useNewsMap);
					}
					
					List<ChangeType> changeTypes = new ArrayList<Collect.ChangeType>(Arrays.asList(annotation.changeType()));
					
					if (changeTypes.contains(ChangeType.ANY)){
						changeTypes.clear();
						for (ChangeType changeType : ChangeType.values()) {
							changeTypes.add(changeType);
						}
					}
					for (ChangeType changeType : changeTypes) {
						Set<Method> useNewsSet = useNewsMap.get(changeType);
						if (useNewsSet == null){
							useNewsSet = new HashSet<Method>();
							useNewsMap.put(changeType, useNewsSet);
						}
						method.setAccessible(true);
						useNewsSet.add(method);
					}
				}
			}
		}
		
		for (String useNewsValue : collectorFieldReflections.keySet()) {
			Set<Object> set = collectors.get(useNewsValue);
			if (set == null){
				set = new HashSet<Object>();
				collectors.put(useNewsValue, set);
			}
			set.add(collector);
		}
		
		for (String useNewsValue : collectorMethodReflections.keySet()) {
			Set<Object> set = collectors.get(useNewsValue);
			if (set == null){
				set = new HashSet<Object>();
				collectors.put(useNewsValue, set);
			}
			set.add(collector);
		}
	}
	
	@After("execution((@eu.crisis_economics.abm.annotation.DataReporter *).new(..)) && this(reporter)")
	public void findScheduledReporters(Object reporter){
		
		Set<Field> fieldsAnnotatedWith = reflections.getFieldsAnnotatedWith(Report.class);
		
		for (Field field : fieldsAnnotatedWith) {
			if (field.getDeclaringClass().isAssignableFrom(reporter.getClass())){
				Report annotation = field.getAnnotation(Report.class);

				if (annotation.interval() > 0){
					boolean steppableExists = true;
					Map<Integer, Set<ScheduledReport>> intervalMap = scheduledReporterFields.get(annotation.interval());
					if (intervalMap == null){
						intervalMap = new HashMap<Integer, Set<ScheduledReport>>();
						scheduledReporterFields.put(annotation.interval(), intervalMap);
						steppableExists = false;
					}

					Set<ScheduledReport> scheduledReports = intervalMap.get(annotation.order());
					if (scheduledReports == null){
						scheduledReports = new HashSet<DataCollection.ScheduledReport>();
						intervalMap.put(annotation.order(), scheduledReports);
						steppableExists = false;
					}

					field.setAccessible(true);
					scheduledReports.add(new ScheduledReport(reporter, field, null));

					if (! steppableExists){
						Simulation.getSimState().schedule.scheduleOnceIn(annotation.interval(), new ScheduledReporter(annotation.interval(), annotation.order()), annotation.order());
					}
				}
			}
		}

		Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Report.class);
		for (Method method : methodsAnnotatedWith) {
			if (method.getDeclaringClass().isAssignableFrom(reporter.getClass())){
				Report annotation = method.getAnnotation(Report.class);

				if (annotation.interval() > 0){
					boolean steppableExists = true;
					Map<Integer, Set<ScheduledReport>> intervalMap = scheduledReporterMethods.get(annotation.interval());
					if (intervalMap == null){
						intervalMap = new HashMap<Integer, Set<ScheduledReport>>();
						scheduledReporterMethods.put(annotation.interval(), intervalMap);
						steppableExists = false;
					}

					Set<ScheduledReport> scheduledReports = intervalMap.get(annotation.order());
					if (scheduledReports == null){
						scheduledReports = new HashSet<DataCollection.ScheduledReport>();
						intervalMap.put(annotation.order(), scheduledReports);
						steppableExists = false;
					}

					method.setAccessible(true);
					scheduledReports.add(new ScheduledReport(reporter, null, method));

					if (! steppableExists){
						Simulation.getSimState().schedule.scheduleOnceIn(annotation.interval(), new ScheduledReporter(annotation.interval(), annotation.order()), annotation.order());
					}
				}
			}
		}
	}
	
	@After("set(@eu.crisis_economics.abm.annotation.Report * *) && args(newValue) && @annotation(newsAnnotation) && target(sender)")
	public void collectField(Object newValue, eu.crisis_economics.abm.annotation.Report newsAnnotation, Object sender){
		if (newsAnnotation.interval() == 0 && !(newValue instanceof Collection)){
			DataCollection aspectOf = Aspects.aspectOf(DataCollection.class);
			Set<Object> set = aspectOf.collectors.get(newsAnnotation.value());
			if (set != null){
				for (Object collector : set) {
					aspectOf.handleData(newsAnnotation.value(), newValue, collector, sender, Collect.ChangeType.CHANGE);
				}
			}
		}
	}
	
	@AfterReturning(pointcut="execution(@eu.crisis_economics.abm.annotation.Report * *(..)) && @annotation(newsAnnotation) && target(sender)", returning="returnValue")
	public void collectReturnValue(Object returnValue, eu.crisis_economics.abm.annotation.Report newsAnnotation, Object sender){
		if (newsAnnotation.interval() == 0){
			DataCollection aspectOf = Aspects.aspectOf(DataCollection.class);
			Set<Object> set = aspectOf.collectors.get(newsAnnotation.value());
			if (set != null){
				for (Object collector : set) {
					aspectOf.handleData(newsAnnotation.value(), returnValue, collector, sender, Collect.ChangeType.CHANGE);
				}
			}
		}
	}

	@Pointcut("call(* java.util.Collection+.add*(..))")
	public void addObject(){}

	@Pointcut("call(* java.util.Collection+.remove*(..))")
	public void removeObject(){}

	@Pointcut("@within(eu.crisis_economics.abm.annotation.DataReporter)")
	public void dataReporterCode(){}
	
	//@AfterReturning(pointcut="annotatedCollectionModification(jp, enc, reporter)", returning="result")
	@AfterReturning(pointcut="(addObject() || removeObject()) && dataReporterCode() && this(reporter) && args(.., arg)",
			returning="result")
	public void catchCollectionModification(JoinPoint jp, JoinPoint.EnclosingStaticPart enc, Object arg, Object reporter, Object result){
		DataCollection aspectOf = Aspects.aspectOf(DataCollection.class);
		Object targetCollection = jp.getTarget();
		Class<?> dataReporterClass = enc.getSignature().getDeclaringType();
		Field[] dataReporterFields = dataReporterClass.getDeclaredFields();
		Collect.ChangeType changeType = jp.getSignature().getName().startsWith("add") ? ChangeType.COLLECTION_ADD : ChangeType.COLLECTION_REMOVE;
		
		if (jp.getSignature().getName().startsWith("remove") && !(result instanceof Boolean)){
			arg = result;
		}
		
		try {
			for (Field field : dataReporterFields) {
				field.setAccessible(true);
				if (field.get(reporter) == targetCollection){
					Report newsAnnotation = field.getAnnotation(Report.class);
					if (newsAnnotation != null){
						if (newsAnnotation.interval() == 0){
							Set<Object> set = aspectOf.collectors.get(newsAnnotation.value());
							if (set != null){
								for (Object collector : set) {
									handleData(newsAnnotation.value(), arg, collector, reporter, changeType);
								}
							}
						}
					}
				}
			}
		} catch (IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}
	
	
	//------------------------- other methods----------------
	public void handleData(final String key, final Object data, Object collector, final Object sender, final Collect.ChangeType changeType){
		Map<String, Map<ChangeType, Set<Field>>> fieldMap = collectorsFieldReflections.get(collector.getClass());
		Map<ChangeType, Set<Field>> changeMap = fieldMap.get(key);
		if (changeMap != null){
			Set<Field> fields = changeMap.get(changeType);

			if (fields != null){
				for (Field field : fields) {
					try {
						Object member = field.get(collector);
						if (member instanceof Collection<?>) {
							@SuppressWarnings("unchecked")
							Collection<Object> list = (Collection<Object>) member;
							list.add(cloner.deepClone(data));
						} else if (member.getClass().isAssignableFrom(data.getClass())){
							field.set(collector, cloner.deepClone(data));
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		Map<String, Map<ChangeType, Set<Method>>> methodMap = collectorsMethodReflections.get(collector.getClass());
		
		Map<ChangeType, Set<Method>> changeMethodMap = methodMap.get(key);
		if (changeMethodMap != null){
			Set<Method> methods = changeMethodMap.get(changeType);
			if (methods != null){

				for (Method method : methods) {
					Annotation[][] parameterAnnotations = method.getParameterAnnotations();
					Object[] args = new Object[parameterAnnotations.length];
					for (int i = 0; i < parameterAnnotations.length; i++) {
						args[i] = null;

						for (Annotation annotation : parameterAnnotations[i]) {
							if (annotation instanceof ReportKey){
								args[i] = key;
							}
							if (annotation instanceof ReportValue){
								args[i] = data;
							}
							if (annotation instanceof ReportSource){
								args[i] = sender;
							}
						}
					}
					try {
						method.invoke(collector, args);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

}
