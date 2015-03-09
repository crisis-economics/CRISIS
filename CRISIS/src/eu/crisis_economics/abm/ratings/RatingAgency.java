/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.ratings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.algorithms.statistics.DiscreteTimeSeries;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A "rating agency" with no balance sheet and no market participation.
  * This entity periodically records measurements for other agents, and
  * makes these measurements available in the form of timeseries. This 
  * object is currently implemented as a globally accessible singleton.
  * 
  * I. Adding measurements:
  * To enable a custom measurement, call RatingAgency.Instance.
  * addTrackingMeasurement with (a) a unique measurement name and (b) an
  * algorithm to extract this measurement from known (tracked) agents.
  * 
  * II. Tracking agents (extracing measurements from agents):
  * To enable tracking of a particular agent, call RatingAgency.Instance.
  * trackAgent. Thereafter all measurements specified by (I) will be made
  * for this agent.
  * 
  * III. Extracting timeseries data for tracked agents:
  * To extract a timeseries of historical measurements (Eg. risk, distance-
  * to default) for a particular agent, call getTimeSeries(measurement name, 
  * agent). The resulting data structure is a copy of the stored timeseries
  * data.
  * 
  * @author phillips
  */
public enum RatingAgency {
   Instance();
   
   /*
    * A structure for storing:
    *   (a) an agent reference, and 
    *   (b) timeseries data for several different measurements.
    * This class is implementation detail and should retain private visibility.
    */
   static private final class AgentMeasurements{
      private Agent agent;
      private Map<String, DiscreteTimeSeries> timeSeries;
      
      public AgentMeasurements(final Agent agent) {
         Preconditions.checkNotNull(agent);
         this.agent = agent;
         this.timeSeries = new HashMap<String, DiscreteTimeSeries>();
      }
      
      /**
        * Update this structure with new (timestamped) measurements.
        * @param measurements
        *        A map of named measurements to extract.
        */
      public void extractNewRecordsWithTimestamp(
         final Map<String, AgentTrackingMeasurement> measurements) {
         final double simulationTime = Simulation.getTime();
         for(final Entry<String, AgentTrackingMeasurement> measurement : measurements.entrySet()) {
            final Pair<Double, Double> newEntry =
               Pair.create(simulationTime, agent.accept(measurement.getValue()));
            if(!timeSeries.containsKey(measurement.getKey()))
               timeSeries.put(measurement.getKey(), new DiscreteTimeSeries());
            timeSeries.get(measurement.getKey()).put(newEntry);
         }
      }
      
      /**
        * Remove all timeseries data for measurements with the given name.
        * @param timeSeriesName
        *        The name of the dataset to remove from this object.
        */
      public void removeTimeSeries(final String timeSeriesName) {
         timeSeries.remove(timeSeriesName);
      }
      
      /**
        * Remove all data stored since before the specified simulation time.
        * @param simulationTime
        *        The least time for which to retain records.
        */
      public void removeAllDataBeforeTime(final double simulationTime) {
         for(final Entry<String, DiscreteTimeSeries> record : timeSeries.entrySet()) {
            final DiscreteTimeSeries timeSeries = record.getValue();
            while(!timeSeries.isEmpty() && timeSeries.firstKey() < simulationTime)
               timeSeries.pollFirstEntry();
         }
      }
      
      public DiscreteTimeSeries getTimeSeries(final String measurementName) {
         DiscreteTimeSeries result = timeSeries.get(measurementName);
         return result == null ? null : result.deepCopy();
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "AgentRecord, agent name:" + agent.getUniqueName() 
              + ", number of measurements: " + timeSeries.size() + ".";
      }
   }
   
   private Map<String, AgentMeasurements> records;
   private Map<String, AgentTrackingMeasurement> measurements;
   private final static double SIMULATION_TIME_TO_STORE_RECORDS = 500.;
   
   private RatingAgency() {
      this.records = new HashMap<String, AgentMeasurements>();
      this.measurements = new HashMap<String, AgentTrackingMeasurement>();
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      // Extract new measurements for all tracked agents.
      Simulation.repeat(this, "queryAgentStates", NamedEventOrderings.BEFORE_ALL);
      // Remove old records.
      Simulation.repeat(this, "removeOldRecords", NamedEventOrderings.BEFORE_ALL);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void queryAgentStates() {
      for(final AgentMeasurements record : records.values())
         record.extractNewRecordsWithTimestamp(measurements);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void removeOldRecords() {
      final double
         simulationTime = Simulation.getTime(),
         leastTimeToStoreRecords = simulationTime - SIMULATION_TIME_TO_STORE_RECORDS;
      for(final AgentMeasurements record : records.values())
         record.removeAllDataBeforeTime(leastTimeToStoreRecords);
   }
   
   /**
     * Begin tracking, and recording data for, the given agent.
     */
   public void trackAgent(final Agent agent) {
      if(records.containsKey(agent.getUniqueName())) return;
      records.put(agent.getUniqueName(), new AgentMeasurements(agent));
   }
   
   /**
     * Discountinue tracking, and remove any outstanding stored data for, the
     * given agent.
     */
   public void discontinueAgentTracking(final Agent agent) {
      records.remove(agent.getUniqueName());
   }
   
   /**
     * Is the given agent currently being tracked?
     */
   public boolean isTrackingAgent(final Agent agent) {
      return records.containsKey(agent.getUniqueName());
   }
   
   /**
     * Add a new tracking measurement.
     * @param measurementName
     *        A unique, custom, name for the measurement.
     * @param measurement
     *        An algorithm to extract the measurement from an agent.
     * If a measurement already exists with the given name, IllegalStateException is
     * raised.
     */
   public void addTrackingMeasurement(
      final String measurementName,
      final AgentTrackingMeasurement measurement
      ) {
      StateVerifier.checkNotNull(measurementName, measurement);
      if(measurements.containsKey(measurementName))
         throw new IllegalStateException(
            "RatingsAgency.addTrackingMeasurement: a measurement with name "
           + measurementName + " already exists.");
      measurements.put(measurementName, measurement);
   }
   
   /**
     * Remove a tracking measurement, and all timeseries associated with
     * the measurement.
     * @param measurementName
     *        The unique name of the measurement to remove.
     */
   public void removeTrackingMeasurement(final String measurementName) {
      if(!hasTrackingMeasurement(measurementName)) return;
      measurements.remove(measurementName);
      for(final AgentMeasurements record : records.values())
         record.removeTimeSeries(measurementName);
   }
   
   public boolean hasTrackingMeasurement(final String measurementName) {
      Preconditions.checkNotNull(measurementName);
      return measurements.containsKey(measurementName);
   }
   
   
   /**
     * Return a copy of a data timeseries for the specified agent.
     * @param measurementName
     *        The name of the measurement to return.
     * @param agent
     *        A reference to the tracked agent.
     * @return
     *        A copy of a data timeseries for the given agent, if this 
     *        data is known, or else null.
     */
   public DiscreteTimeSeries getTimeSeries(final String measurementName, final Agent agent) {
      if(!isTrackingAgent(agent)) return null;
      final AgentMeasurements record = records.get(agent.getUniqueName());
      return record.getTimeSeries(measurementName);
   }
   
   /**
     * Restore this object to its default state. This operation clears all
     * known measurements and data from this object.
     */
   public void flush() {
      this.records.clear();
      this.measurements.clear();
   }
}
