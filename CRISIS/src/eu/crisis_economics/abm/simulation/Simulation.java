/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
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
package eu.crisis_economics.abm.simulation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.google.common.eventbus.EventBus;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import ai.aitia.meme.paramsweep.platform.mason.recording.RecordingHelper;
import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.model.Mark2Model;
import eu.crisis_economics.abm.ratings.RatingAgency;
import eu.crisis_economics.utilities.CrisisTextBanner;

/**
 * This is a helper class that maintains a single SimState instancePtr as a private static variable. This singleton SimState
 * object can be accessed by a public static getter method. The goal of this class is to enable any object to access the
 * SimState object of the simulation, and avoid having to pass this object to every object in need.
 * 
 * This class can be used in two ways. It is either sub-classed by the programmer instead of sub-classing the SimState class
 * directly, or an existing SimState object can be registered by the {@link #initSimulation(SimState)} static method.
 * 
 * @author Tamás Máhr, Richard O. Legendi
 * @see SimState
 * @see Schedule
 */
public abstract class Simulation
     extends SimState
     implements Mark2Model {
   
    public static double getFloorTime() {
       return Math.floor(getSimState().schedule.getTime());
    }
    
    public static long getCycleIndex() {
       return Math.round(Simulation.getFloorTime());
    }
    
    public static boolean hasFurtherScheduledEvents() {
       return !getSimState().schedule.scheduleComplete();
    }
    
    /**
     * A random serial number.
     */
    private static final long serialVersionUID = 8687924062804654877L;
    
    /**
     * The singleton SimState object.
     */
    private static SimState simulationState = null;
    
    /** See {@link #isRecordingContracts()}. */
    private boolean recordingContracts = false;

	private RecordingHelper recordingHelper;
    
	/**
     * This method can be used to obtain a reference to the singleton SimState object.
     * 
     * @return the singleton SimState object.
     */
    public static SimState getSimState() {
        return simulationState;
    }
    
    /**
      * Get the current running simulation.
      */
    public static Simulation getRunningModel() { // TODO: not compatible with all models
       return (Simulation)simulationState;
    }
    
    public static double getTime() {
       if(getSimState() == null)
          return -1.;
       else return getSimState().schedule.getTime();
    }
    
    /**
     * Registers the provided SimState object as the singleton SimState of the simulation. Displays a warning if another
     * SimState is already registered.
     * 
     * @param simulationState
     */
    public static void initSimulation(final SimState simulationState) {
        if ( Simulation.simulationState != null ) {
            System.err.println( "The singleton SimState object has already been initialized, resetting simulation state." );
        }
        
        Simulation.simulationState = simulationState;
    }
    
    /**
     * This is a helper method for testing, it removes the registered SimState object, so that subsequent tests can use
     * separate SimStates.
     * 
     * <b>Note</b> that this is a private function and is always called when the simulation is terminated normally (either
     * killed or finished).
     */
    public static void unsetSimState() {
        simulationState = null;
    }
    
    
    public Simulation(final long seed) {
        super( seed );
        initSimulationAndResetSchedule();
        printBanner();
    }
    
    /**
     * Simply passes the provided arguments to the corresponding SimState constructor, and registers this new object as the
     * singleton SimState of the simulation.
     * 
     * @param seed {@inheritDoc}
     * @param schedule {@inheritDoc}
     */
    public Simulation(final long seed, final Schedule schedule) {
        super( seed, schedule );
        initSimulationAndResetSchedule();
        printBanner();
    }
    
    /**
     * Simply passes the provided arguments to the corresponding SimState constructor, and registers this new object as the
     * singleton SimState of the simulation.
     * 
     * @param random {@inheritDoc}
     * @param schedule {@inheritDoc}
     */
    public Simulation(final MersenneTwisterFast random, final Schedule schedule) {
        super( random, schedule );
        initSimulationAndResetSchedule();
        printBanner();
    }
    
    /**
     * Simply passes the provided arguments to the corresponding SimState constructor, and registers this new object as the
     * singleton SimState of the simulation.
     * 
     * @param random {@inheritDoc}
     */
    public Simulation(final MersenneTwisterFast random) {
        super( random );
        initSimulationAndResetSchedule();
        printBanner();
    }
    
    private void printBanner() {
       final CrisisTextBanner banner = new CrisisTextBanner();
       System.out.printf(banner.getBanner());
       System.out.printf("\nSchedule:\n");
       for(NamedEventOrderings record : NamedEventOrderings.values()) {
          System.out.printf("%45s at time %16.10g\n",
             record.name(), record.getUnitIntervalTime());
       }
       System.out.println();
    }
    
    /**
     * Model parameter which determines if the created contracts should be recorded.
     * 
     * <p>
     * <b>Note that it comes with a huge overhead,</b> because all instances are stored in the memory.
     * <p>
     * 
     * @return <code>true</code> if <b>all</b> the contracts should be recorded; <code>false</code> otherwise
     */
    public boolean isRecordingContracts() {
        return recordingContracts;
    }
    
    /**
     * Specifies if the created contracts should be recorded.
     * 
     * <p>
     * <i>set only if you understand the consequences described at {@link #isRecordingContracts()}.</i>
     * </p>
     * 
     * @param recordingContracts <code>true</code> if <b>all</b> the contracts should be recorded; <code>false</code>
     *            otherwise
     */
    public void setRecordingContracts(final boolean recordingContracts) {
        this.recordingContracts = recordingContracts;
    }
    public String desRecordingContracts() {return "Toggle to export detailed contract information to the output.txt file.";}
    
    @Override
    public void start() {
        super.start();
        
        // When the GUI resets
        if ( null == simulationState ) {
          initSimulationAndResetSchedule();
        }
        recordingHelper = new RecordingHelper(simulationState);
        
        recordingHelper.scheduleRecording(simulationState);
    }
    
    private void initSimulationAndResetSchedule() {
    	Agent.resetSerial();
    	Contract.resetInstanceCounterAndClearLogs();
//        if ( null == simulationState ) {
        	initSimulation( this );
//	    }
        
        //super.start(); // Resets the schedule
        schedule.reset();
        UniqueStockExchange.Instance.flush();
        UniqueStockExchange.Instance.initialize();
        RatingAgency.Instance.flush();
    }
    
    /**
     * This is always called when the simulation is terminated normally (either killed or finished).
     * 
     * <p>
     * It is important to call this method <b>after</b> any custom cleanup process, otherwise the global static reference is
     * set to <code>null</code>, so the schedule access results in an error.
     * </p>
     * 
     * @see sim.engine.SimState#kill()
     */
    @Override
    public void kill() {
    	recordingHelper.closeRecorder();
       
        UniqueStockExchange.Instance.flush();
        eventBus = new EventBus("Simulation Event Bus");
        super.kill();
        unsetSimState();
        
        Agent.resetSerial();
        Contract.resetInstanceCounterAndClearLogs();
    }
    
    private static EventBus
       eventBus = new EventBus("Simulation Event Bus");
    
    /**
      * Get the active {@link EventBus} for this {@link Simulation}.
      */
    public static EventBus events() {
       return eventBus;
    }
    
    @SuppressWarnings("serial")
    public final static class ScheduledMethodInvokation implements Steppable {
       private Object instancePtr;
       private Method functionPtr;
       private Object[] functionParams = null;
       private AbsoluteEventOrder orderedEvent;
       private ScheduledMethodInvokation(
          Object instance,
          Method functionPtr, 
          Object[] functionParams,
          AbsoluteEventOrder orderedEvent
          ) {
          this.instancePtr = instance;
          this.functionPtr = functionPtr;
          this.functionParams = functionParams;
          this.orderedEvent = orderedEvent;
          functionPtr.setAccessible(true);
          commitSelf(getSimState());
       }
       @Override
       public void step(SimState simulationState) {
          try {
             functionPtr.invoke(instancePtr, functionParams);
             if(orderedEvent.isRepeating()) 
                recommitSelf(simulationState);
          } catch(final IllegalAccessException accessException) {
             System.err.println(
                "Simulation: access to " + functionPtr + " is forbidden.");
             throw new FatalSimulationException(accessException);
          } catch (final Exception eventRaisedException) {
             final String errMsg = 
                "Simulation.ScheduledMethodInvokation.step: scheduled method has returned an "
                + "exception to the event processing queue. The underlying cause of the exception"
                + " could not be rectified at this layer. Event processing cannot continue as "
                + "the exception may indicate an illegal simulation state. The simulation will "
                + " now halt. Details follow: underlying cause: "
                + eventRaisedException.getMessage() + ", event: " + orderedEvent + ".";
             System.err.println(errMsg);
             Throwable cause = eventRaisedException.getCause();
             if(cause == null)
                throw new RuntimeException(eventRaisedException);
             else {
                if(cause.getCause() == null)
                   throw new RuntimeException(cause);
                else throw new RuntimeException(cause.getCause());
             }
          }
       }
       private void commitSelf(SimState simulationState) {
           final double timeToSchedule =
              Math.max(0, 
              CustomSimulationCycleOrdering.toAbsoluteSimulationTime(
                 orderedEvent.getCycleOrder(), orderedEvent.getDelayPeriod()));
           simulationState.schedule.scheduleOnce(
                 timeToSchedule,
                 orderedEvent.getCycleOrder().getPriority(),
                 this
                 );
       }
       private void recommitSelf(SimState simulationState) {
           final double timeToSchedule = 
              CustomSimulationCycleOrdering.toAbsoluteSimulationTime(
                 orderedEvent.getCycleOrder(), orderedEvent.getSuccessiveExecutionInterval());
           simulationState.schedule.scheduleOnce(
              timeToSchedule,
              orderedEvent.getCycleOrder().getPriority(), 
              this
              );
        }
     }
    
    private final static void securityHalt(Exception e) {
        e.printStackTrace();
        Simulation.getSimState().kill();
        throw new SecurityException(e);
    }
    
    private final static boolean isPermissibleMethod(Method methodPtr) {
        int methodModifiers = methodPtr.getModifiers();
        if(Modifier.isProtected(methodModifiers) ||
           Modifier.isPublic(methodModifiers))
            return true;
        else 
            return false;
    }
    
    /**
      * Attempt to reflect a {@link Class} method, by name, from an object {@code O}.
      * This method accepts (a) the name of the method to reflect, and (b) a list of
      * arguments with which to call the method. Initially (step i.) {@code O} will be checked
      * for declared methods with both the name and absolute types specified by the 
      * arguments. If such a method is found, and if the visibility of the method is compatible
      * with the calling class, then the method is returned. If such a method is not 
      * found, then (step ii.) this function will inspect each method in {@code O}, in order, for
      * compatibility with the desired method after casting the arguments. The first such
      * method that is deemed to be both compatible with the required signature and visible
      * from within the scope of the calling class is reflected. Otherwise, the reflection
      * request fails and this method halts the simulation with {@link SecurityException}.<br><br>
      * 
      * Concretely: it may be the case that a method of name X and signature X({@link Number})
      * is to be reflected from {@code O} with an {@link Integer} argument {@code A}. Since
      * it is not the case that X({@link Integer}) is the same method as X({@link Number}),
      * this function will not identify X({@link Number}) in step (i.). In step (ii.) this 
      * function will check whether X({@link Number}) is compatible with X(({@link Number}) 
      * {@code A}). This is {@code True}, therefor X({@link Number}) is a viable reflected
      * method.
      * 
      * @param callerType
      *        The {@link Class} type of the object that wishes to schedule a method execution.
      * @param functionRefName
      *        The {@link String} name of the method to schedule for execution.
      * @param eventOrdering
      *        A specification for the future time to execute the scheduled method.
      * @param paramList
      *        A list of parameters to the method.
      */
    private final static Method secureReflectMethod(
        Class<?> callerType,
        String functionRefName, 
        AbsoluteEventOrder eventOrdering,
        Object... paramList
        ) {
        Class<?>[] paramRefNames = new Class<?>[paramList.length];
        for(int i = 0; i< paramList.length; ++i)
            paramRefNames[i] = paramList[i].getClass();
        for(Class<?> calleeType = callerType; 
           calleeType != null; 
           calleeType = calleeType.getSuperclass()) {
           Method methodPtr = null;
           try {
              methodPtr = calleeType.getDeclaredMethod(functionRefName, paramRefNames);
           }
           catch (final NoSuchMethodException e) {
              // Try to downcast method arguments:
              final Method[]
                 allCalleeMethods = calleeType.getDeclaredMethods();
              for(final Method method : allCalleeMethods) {
                 if(!method.getName().equals(functionRefName))
                    continue;
                 Type[] argTypes = method.getGenericParameterTypes();
                 if(argTypes.length != paramList.length)
                    continue;
                 for(int j = 0; j< paramList.length; ++j) {
                    if(!paramList[j].getClass().isAssignableFrom(argTypes[j].getClass()))
                       continue;
                 }
                 methodPtr = method;
                 break;
              }
           }
           if(methodPtr == null)
              continue;
           try {
              if(calleeType == callerType)
                 return methodPtr;
              else if(Simulation.isPermissibleMethod(methodPtr))
                 return methodPtr;
              else 
                 throw new SecurityException(
                    "Simulation.secureReflectMethod: " + methodPtr + 
                    " is not accessible to type " + callerType);
            }
            catch (Exception reflSectException) {
                Simulation.securityHalt(reflSectException);
            }
        }
        Simulation.securityHalt(new SecurityException(
            "Simulation.secureReflectMethod: method not found."));
        return null;
    }
    
    // Enqueue a future event.
    private final static ScheduledMethodInvokation enqueue(   // TODO: Harden
        Object instancePtr, 
        String functionRefName, 
        AbsoluteEventOrder eventOrdering,
        Object... paramList
        ) {
        if(instancePtr == null || eventOrdering == null)
            throw new IllegalArgumentException(
                "Simulation.enqueue: null argument.");
        Class<?> callerType = null;
        try {
            callerType = Class.forName(
                Thread.currentThread().getStackTrace()[3].getClassName());
        } catch (Exception reflSectException) {
            Simulation.securityHalt(reflSectException);
        }
        Method functionPtr = Simulation.secureReflectMethod(
            callerType, functionRefName, 
            eventOrdering, paramList);
        return new ScheduledMethodInvokation(
            instancePtr, functionPtr, 
            paramList, eventOrdering);
    }
    
    public final static void enqueue(
       ScheduledMethodInvokation permittedMethod) {
       if(simulationState != null) // Terminated
           permittedMethod.commitSelf(simulationState);
    }
    
    /** Schedule a method to execute once. */
    public final static ScheduledMethodInvokation once(
        Object instancePtr, 
        String functionRefName, 
        SimulatedEventOrder cycleOrder,
        Object... paramList
        ) {
        return Simulation.enqueue(
            instancePtr, 
            functionRefName, 
            AbsoluteEventOrder.createEvent(cycleOrder, false), 
            paramList);
    }
    
    public final static ScheduledMethodInvokation onceCustom(
       Object instancePtr, 
       String functionRefName, 
       SimulatedEventOrder cycleOrder,
       ScheduleInterval executionDelay,
       Object... paramList
       ) {
       return Simulation.enqueue(
           instancePtr, 
           functionRefName, 
           AbsoluteEventOrder.createEvent(cycleOrder, false, executionDelay), 
           paramList);
    }
    
    /** Schedule a method to execute repeatedly. */
    public final static ScheduledMethodInvokation repeat(
        Object instancePtr, 
        String functionRefName, 
        SimulatedEventOrder cycleOrder,
        Object... paramList
        ) {
        return Simulation.enqueue(
           instancePtr, 
           functionRefName, 
           AbsoluteEventOrder.createEvent(cycleOrder, true), 
           paramList);
    }
    
    public final static ScheduledMethodInvokation repeatCustom(
       Object instancePtr, 
       String functionRefName, 
       SimulatedEventOrder cycleOrder,
       ScheduleInterval executionInterval,
       ScheduleInterval executionDelay,
       Object... paramList
       ) {
       return Simulation.enqueue(
          instancePtr, 
          functionRefName, 
          AbsoluteEventOrder.createEvent(
             cycleOrder, executionInterval, executionDelay), 
          paramList);
    }
    
    // ======================================================================================================================
    // Mark2Model interface
    
    @Override
    public void step(final SimState state) { }
    
    @Override
    public final boolean hasGovernment() { return getGovernment() != null; }
    
    @Override
    public final boolean hasClearingHouse() { return getClearingHouse() != null; }
}
