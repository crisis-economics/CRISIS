/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.contracts.stocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.statistics.DiscreteTimeSeries;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/**
  * Historical data logging for stock market measurements.
  * @author phillips
  */
public final class StockMarketDataLogger {
   
   private TreeMap<Double, TimestampedMarketRecord> historicalRecords;
   private double memoryAmountOfTime;
   
   /** 
     * A snapshot of a stock at one point in time.
     * @author phillips
     */
   public static final class StockRecord {
      private final String
         stockUniqueName;
      private final double
         pricePerShare,
         numberOfSharesInExistence;
      private double
         totalDividendPaid;
      
      StockRecord(final StockReleaser stockReleaser) {                  // Client Immutable.
         Preconditions.checkNotNull(stockReleaser);
         this.stockUniqueName = stockReleaser.getUniqueName();
         this.pricePerShare = UniqueStockExchange.Instance.getStockPrice(stockReleaser);
         this.numberOfSharesInExistence =
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser);
         this.totalDividendPaid = 0.;
      }
      
      void increaseDividendPaid(double increment) {
         totalDividendPaid += increment;
      }
      
      public String getStockUniqueName() {
         return stockUniqueName;
      }
      
      public double getPricePerShare() {
         return pricePerShare;
      }
      
      public double getNumberOfSharesInExistence() {
         return numberOfSharesInExistence;
      }
      
      public double getTotalDividendPaid() {
         return totalDividendPaid;
      }
   }
   
   /** 
     * A collection of snapshots of all stocks at one point in time.
     * @author phillips
     */
   public static final class TimestampedMarketRecord {
      private Map<String, StockRecord> stockRecords;
      
      TimestampedMarketRecord() {                                       // Client Immutable
         this.stockRecords = new HashMap<String, StockRecord>();
      }
      
      /**
        * Add a stock snapshot to this market record.
        */
      void addRecord(final StockRecord record) {
         Preconditions.checkNotNull(record);
         if(stockRecords.containsKey(record.getStockUniqueName()))
            throw new IllegalStateException();
         stockRecords.put(record.getStockUniqueName(), record);
      }
      
      /**
        * Does a snapshot exist for the specified stock?
        */
      public boolean hasRecord(final String stockUniqueName) {
         return stockRecords.containsKey(stockUniqueName);
      }
      
      /**
        * Get a snapshot for the specified stock, if a snapshot is available.
        */
      public StockRecord getRecord(final String stockUniqueName) {
         return stockRecords.get(stockUniqueName);
      }
   }
   
   /**
     * Create a stock market data logger.
     * 
     * @param memoryAmountOfTime
     *        The time interval over which to store market data snapshots. If the 
     *        current simulation time is T, then data at for times  t < T - memoryAmountOfTime
     *        is not guaranteed to exist.
     */
   StockMarketDataLogger(double memoryAmountOfTime) {                       // Client Immutable
      Preconditions.checkArgument(memoryAmountOfTime > 0.);
      this.historicalRecords = new TreeMap<Double, TimestampedMarketRecord>();
      this.memoryAmountOfTime = memoryAmountOfTime;
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "removeOutOfDateRecords", NamedEventOrderings.BEFORE_ALL);
   }
   
   @SuppressWarnings("unused")  // Scheduled
   private void removeOutOfDateRecords() {
      final double leastTimeToKeep = Simulation.getTime() - memoryAmountOfTime;
      if(historicalRecords.isEmpty()) return;
      while(historicalRecords.firstEntry().getKey() < leastTimeToKeep)
         historicalRecords.pollFirstEntry();
   }
   
   void commitNewRecordFor(final StockReleaser stockReleaser) {
      final double simulationTime = Simulation.getTime();
      if(!historicalRecords.containsKey(simulationTime))
         historicalRecords.put(simulationTime, new TimestampedMarketRecord());
      final TimestampedMarketRecord marketRecord = historicalRecords.get(simulationTime);
      final StockRecord snapshot = new StockRecord(stockReleaser);
      marketRecord.addRecord(snapshot);
   }
   
   /**
     * Get the earliest stock market record before (or equal to) the given 
     * simulation time.
     */
   public TimestampedMarketRecord getEarliestRecordBeforeTime(final double time) {
      Entry<Double, TimestampedMarketRecord> record = historicalRecords.floorEntry(time);
      return record == null ? null : record.getValue();
   }
   
   /**
     * Get the earliest stock market record later than (or equal to) the given 
     * simulation time.
     */
   public TimestampedMarketRecord getEarliestRecordAfterTime(final double time) {
      Entry<Double, TimestampedMarketRecord> record = historicalRecords.ceilingEntry(time);
      return record == null ? null : record.getValue();
   }
   
   /**
     * Get a copy of a price-per-share timeseries for the specified stock type.
     * This method cannot return null, but can return an empty timeseries, in 
     * the event that no historical data for the stock is known.
     *
     * @param stockUniqueName
     *        The unique name of the stock.
     * @param timeCutoff
     *        A lower bound (simulation time) for timeseries data to fetch.
     */
   public DiscreteTimeSeries
      getStockPricePerShareTimeSeries(
         final String stockUniqueName,
         final double timeCutoff
         ) {
      final DiscreteTimeSeries result = new DiscreteTimeSeries();
      for(final Entry<Double, TimestampedMarketRecord> record : 
         historicalRecords.tailMap(timeCutoff).entrySet()) {
         StockRecord snapshot = record.getValue().getRecord(stockUniqueName);
         if(snapshot == null) continue;
         result.put(Pair.create(record.getKey(), snapshot.getPricePerShare()));
      }
      return result;
   }
   
   /**
     * Get a copy of a dividend timeseries for the specified stock type.
     * This method cannot return null, but can return an empty timeseries,
     * in the event that no historical data for the stock is known.
     * 
     * @param stockUniqueName
     *        The unique name of the stock.
     * @param timeCutoff
     *        A lower bound (simulation time) for timeseries data to fetch.
     */
   public DiscreteTimeSeries
      getDividendPerShareTimeSeries(
         final String stockUniqueName,
         final double timeCutoff
         ) {
      final DiscreteTimeSeries result = new DiscreteTimeSeries();
      for(final Entry<Double, TimestampedMarketRecord> record : 
         historicalRecords.tailMap(timeCutoff).entrySet()) {
         StockRecord snapshot = record.getValue().getRecord(stockUniqueName);
         if(snapshot == null) continue;
         final double
            numberOfSharesInExistence = snapshot.getNumberOfSharesInExistence();
         result.put(Pair.create(record.getKey(),
            numberOfSharesInExistence == 0 ? 0. :
               snapshot.getTotalDividendPaid() / snapshot.getNumberOfSharesInExistence()));
      }
      return result;
   }
}
