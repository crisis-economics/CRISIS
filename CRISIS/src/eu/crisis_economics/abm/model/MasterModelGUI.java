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
package eu.crisis_economics.abm.model;

import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.SimulationGui;

public class MasterModelGUI extends SimulationGui {
   
   final String
      modelName;
   
   public MasterModelGUI(final String modelName)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      super((SimState)
         Class.forName("eu.crisis_economics.abm.model." + modelName).newInstance());
      this.modelName = modelName;
   }
   
   protected JFrame
      chartFrame,
      subEconomyVisualizationFrame;
   
   @Override
   public void start() {
      super.start();
      initSeries();
      
      scheduleRepeatingImmediatelyAfter(new Steppable() {
         private static final long serialVersionUID = 1L;
         private int lastCycleIndex   = 0;
         
         @Override
         public void step(final SimState state) {
            final AbstractModel model = (AbstractModel) state;
            final int currentCycleIndex = (int) Simulation.getFloorTime();
            
            if (model.schedule.getTime() != Schedule.AFTER_SIMULATION
                && currentCycleIndex != lastCycleIndex) {
               scheduleSeries(model);
               lastCycleIndex = currentCycleIndex;
            }
         }
      });
   }
   
   @Override
   public void init(final Controller c) {
      super.init(c);
      
      final int
         screenWidth = (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth()),
         screenHeight = (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight()),
         graphFrameWidth = screenWidth / 2,
         graphFrameHeight = screenHeight / 2;
      
      // Graphs
      chartFrame = new JFrame("Model Statistics [" + modelName + "]");
      c.registerFrame(chartFrame);
      chartFrame.pack();
      chartFrame.setSize(graphFrameWidth, graphFrameHeight);
      chartFrame.setVisible(true);
      
      // Component Visualization
      final Display2D display = new Display2D(screenWidth / 4, screenHeight / 4, this);
      display.setClipping(false);
      subEconomyVisualizationFrame = display.createFrame();
      subEconomyVisualizationFrame.setTitle("Components");
      c.registerFrame(subEconomyVisualizationFrame);
      subEconomyVisualizationFrame.pack();
      subEconomyVisualizationFrame.setVisible(true);
      
      final JTabbedPane tabbedPane = new JTabbedPane();
      chartFrame.add(tabbedPane);
      setupCharts();
      addTabs(tabbedPane);
      
      final Console console = getConsole();
      
      console.setLocation(chartFrame.getLocationOnScreen().x + chartFrame.getWidth(), 0);
      console.setSize(150, graphFrameHeight);
      
   }
   
   public static String getName() {
      return "Financial System with Macro Economy";
   }
   
   public static String getInfo() {
      return "This is a model of central bank, commercial regularBanks, firms and households. "
            + "regularBanks and firms use market clearing for stock and commercial loan market.";
   }
   
   /**
     * Time series generators
     */
   private TimeSeriesChartGenerator
      meanHouseholdWealthChart,
      aggregateEmploymentChart,
      aggregateOustandingLoanDemandChart,
      aggregateCommercialLoanChart,
      meanGoodsSellingPriceChart,
      meanMinAndMaxFirmProfitChart,
      aggregateAvailableCashChart,
      aggregateDividendChart,
      meanMinMaxGoodsMarkUpRateChart,
      meanMinMaxUnsoldGoodsChart,
      fundAggregateTotalAssetsChart,
      householdSignedAggregateFundContributionChart,
      fundInvestmentDistributionChart,
      bankInvestmentDistributionChart,
      meanFirmProductionChart,
      meanFirmEquityChart,
      meanBankEquityChart,
      meanBankLeverageChart,
      firmStockPriceChart,
      bankStockPriceChart,
      stocksAndLoansReturnChart;
   
   private XYSeries
      avgWealthSeries,
      AggEmploymentSeries,
      AggMaxAskedLoanSeries,
      AggLoanSeries,
      avgPriceSeries,
      AvgProfitSeries,
      MinProfitSeries,
      MaxProfitSeries,
      AggDividendSeries,
      avgMarkUpSeries,
      minMarkUpSeries,
      maxMarkUpSeries,
      avgUnsoldSeries,
      maxUnsoldSeries,
      minUnsoldSeries,
      fundAggAssetsSeries,
      householdAggFundContributionSeries,
      fundAggregateCashReserveSeries,
      fundAggregateFirmStockInvestmentSeries,
      fundAggregateBankStockInvestmentSeries,
      fundAggregateGovernmentBondInvestmentSeries,
      fundAggregateBankBondInvestmentSeries,
      bankAggregateCashReserveSeries,
      bankAggregateFirmStockInvestmentSeries,
      bankAggregateBankStockInvestmentSeries,
      bankAggregateGovernmentBondInvestmentSeries,
      bankAggregateFirmLoanInvestmentSeries,
      avgFirmProductionSeries,
      avgFirmEquitySeries,
      avgBankEquitySeries,
      avgBankLeverageSeries,
      firmStockPriceMinSeries,
      firmStockPriceMaxSeries,
      firmStockPriceAvgSeries,
      bankStockPriceMinSeries,
      bankStockPriceMaxSeries,
      bankStockPriceAvgSeries,
      stockReturnSeries,
      loanReturnSeries;
   
   protected void scheduleSeries(Simulation value) {
      final AbstractModel model = (AbstractModel) value;
      final double sampleTime = model.schedule.getTime();
      
      avgFirmProductionSeries.add(
         model.schedule.getTime(),
         model.getAvgProduction(),
         true
         );
      avgFirmEquitySeries.add(
         model.schedule.getTime(),
         model.getAvgFirmEquity(),
         true
         );
      avgBankEquitySeries.add(
         model.schedule.getTime(),
         model.getAvgBankEquity(),
         true
         );
      avgBankLeverageSeries.add(
         model.schedule.getTime(),
         model.getAvgBankLeverage(),
         true
         );
      firmStockPriceAvgSeries.add(
         model.schedule.getTime(),
         model.getFirmStockPrices().getValue1(),
         true
         );
      firmStockPriceMinSeries.add(
         model.schedule.getTime(),
         model.getFirmStockPrices().getValue0(),
         true
         );
      firmStockPriceMaxSeries.add(
         model.schedule.getTime(),
         model.getFirmStockPrices().getValue2(),
         true
         );
      bankStockPriceAvgSeries.add(
         model.schedule.getTime(),
         model.getBankStockPrices().getValue1(),
         true
         );
      bankStockPriceMinSeries.add(
         model.schedule.getTime(),
         model.getBankStockPrices().getValue0(),
         true
         );
      bankStockPriceMaxSeries.add(
         model.schedule.getTime(),
         model.getBankStockPrices().getValue2(),
         true
         );
      stockReturnSeries.add(
         model.schedule.getTime(),
         model.getStockReturn(),
         true
         );
      loanReturnSeries.add(
         model.schedule.getTime(),
         model.getInterestRate(),
         true
         );
      avgWealthSeries.add(
         sampleTime,
         model.getAvgWealth(),
         true
         );
      AggEmploymentSeries.add(
         sampleTime,
         model.getAggEmployment(),
         true
         );
      AggMaxAskedLoanSeries.add(
         sampleTime,
         model.getAggMaxAskedLoan(),
         true
         );
      AggLoanSeries.add(
         sampleTime,
         model.getAggEffectiveLoan(),
         true
         );
      avgPriceSeries.add(
         sampleTime,
         model.getAvgPrice(),
         true
         );
      AvgProfitSeries.add(
         sampleTime,
         model.getAvgProfit(),
         true
         );
      MinProfitSeries.add(
         sampleTime,
         model.getMinProfits(),
         true
         );
      MaxProfitSeries.add(
         sampleTime,
         model.getMaxProfits(),
         true
         );
      AggDividendSeries.add(
         sampleTime,
         model.getAggDividend(),
         true
         );
      avgMarkUpSeries.add(
         sampleTime,
         model.getAverageMarkUp(),
         true
         );
      maxMarkUpSeries.add(
         sampleTime,
         model.getMaxMarkUp(),
         true
         );
      minMarkUpSeries.add(
         sampleTime,
         model.getMinMarkUp(),
         true
         );
      avgUnsoldSeries.add(
         sampleTime,
         model.getAverageUnsoldGoods(),
         true
         );
      minUnsoldSeries.add(
         sampleTime,
         model.getMinUnsoldGoods(),
         true
         );
      maxUnsoldSeries.add(
         sampleTime,
         model.getMaxUnsoldGoods(),
         true
         );
      fundAggAssetsSeries.add(
         sampleTime,
         model.getAggregateFundAssets(),
         true
         );
      householdAggFundContributionSeries.add(
         sampleTime,
         model.getAggregateHouseholdFundContribution(),
         true
         );
     
     /*
      * Aggregate fund investments.
      */
     fundAggregateCashReserveSeries.add(
        sampleTime, model.getAggreateFundAssetsByInvestmentType().get(0), true);
     fundAggregateFirmStockInvestmentSeries.add(
        sampleTime, model.getAggreateFundAssetsByInvestmentType().get(1), true);
     fundAggregateBankStockInvestmentSeries.add(
        sampleTime, model.getAggreateFundAssetsByInvestmentType().get(2), true);
     fundAggregateGovernmentBondInvestmentSeries.add(
        sampleTime, model.getAggreateFundAssetsByInvestmentType().get(3), true);
     fundAggregateBankBondInvestmentSeries.add(
        sampleTime, model.getAggreateFundAssetsByInvestmentType().get(4), true);
     
     /*
      * Aggregate bank investments.
      */
     bankAggregateCashReserveSeries.add(
        sampleTime, model.getAggreateBankAssetsByInvestmentType().get(0), true);
     bankAggregateFirmStockInvestmentSeries.add(
        sampleTime, model.getAggreateBankAssetsByInvestmentType().get(1), true);
     bankAggregateBankStockInvestmentSeries.add(
        sampleTime, model.getAggreateBankAssetsByInvestmentType().get(2), true);
     bankAggregateGovernmentBondInvestmentSeries.add(
        sampleTime, model.getAggreateBankAssetsByInvestmentType().get(3), true);
     bankAggregateFirmLoanInvestmentSeries.add(
        sampleTime, model.getAggreateBankAssetsByInvestmentType().get(4), true);
   }
   
   protected void initSeries() {
      avgFirmProductionSeries = initSeriesFor(meanFirmProductionChart);
      avgFirmEquitySeries = initSeriesFor(meanFirmEquityChart);
      avgBankEquitySeries = initSeriesFor(meanBankEquityChart);
      avgBankLeverageSeries = initSeriesFor(meanBankLeverageChart);
      firmStockPriceAvgSeries = initSeriesFor(firmStockPriceChart);
      firmStockPriceMinSeries = initSeriesForAlt(firmStockPriceChart);
      firmStockPriceMaxSeries = initSeriesForAlt(firmStockPriceChart);
      bankStockPriceAvgSeries = initSeriesFor(bankStockPriceChart);
      bankStockPriceMinSeries = initSeriesForAlt(bankStockPriceChart);
      bankStockPriceMaxSeries = initSeriesForAlt(bankStockPriceChart);
      stockReturnSeries = initSeriesFor(stocksAndLoansReturnChart);
      loanReturnSeries = initSeriesForAlt(stocksAndLoansReturnChart);
      
      avgWealthSeries = initSeriesFor(meanHouseholdWealthChart);
      AggEmploymentSeries = initSeriesFor(aggregateEmploymentChart);
      AggMaxAskedLoanSeries = initSeriesFor(aggregateOustandingLoanDemandChart);
      AggLoanSeries = initSeriesFor(aggregateCommercialLoanChart);
      avgPriceSeries = initSeriesFor(meanGoodsSellingPriceChart);
      AvgProfitSeries = initSeriesFor(meanMinAndMaxFirmProfitChart);
      MinProfitSeries = initSeriesForAlt(meanMinAndMaxFirmProfitChart);
      MaxProfitSeries = initSeriesForAlt(meanMinAndMaxFirmProfitChart);
      AggDividendSeries = initSeriesFor(aggregateDividendChart);
      avgMarkUpSeries = initSeriesFor(meanMinMaxGoodsMarkUpRateChart);
      minMarkUpSeries = initSeriesForAlt(meanMinMaxGoodsMarkUpRateChart);
      maxMarkUpSeries = initSeriesForAlt(meanMinMaxGoodsMarkUpRateChart);
      avgUnsoldSeries = initSeriesFor(meanMinMaxUnsoldGoodsChart);
      minUnsoldSeries = initSeriesForAlt(meanMinMaxUnsoldGoodsChart);
      maxUnsoldSeries = initSeriesForAlt(meanMinMaxUnsoldGoodsChart);
      fundAggAssetsSeries = initSeriesForAlt(fundAggregateTotalAssetsChart);
      householdAggFundContributionSeries = initSeriesForAlt(householdSignedAggregateFundContributionChart);
      
     /*
      * Aggregate fund investments.
      */
     fundAggregateCashReserveSeries = initSeriesForAlt(fundInvestmentDistributionChart);
     fundAggregateFirmStockInvestmentSeries = initSeriesForAlt(fundInvestmentDistributionChart);
     fundAggregateBankStockInvestmentSeries = initSeriesForAlt(fundInvestmentDistributionChart);
     fundAggregateGovernmentBondInvestmentSeries = initSeriesForAlt(fundInvestmentDistributionChart);
     fundAggregateBankBondInvestmentSeries = initSeriesForAlt(fundInvestmentDistributionChart);
     
     /*
      * Aggregate bank investments.
      */
     bankAggregateCashReserveSeries = initSeriesForAlt(bankInvestmentDistributionChart);
     bankAggregateFirmStockInvestmentSeries = initSeriesForAlt(bankInvestmentDistributionChart);
     bankAggregateBankStockInvestmentSeries = initSeriesForAlt(bankInvestmentDistributionChart);
     bankAggregateGovernmentBondInvestmentSeries = initSeriesForAlt(bankInvestmentDistributionChart);
     bankAggregateFirmLoanInvestmentSeries = initSeriesForAlt(bankInvestmentDistributionChart);
   }
   
   protected void addTabs(JTabbedPane tabbedPane) {
      addChartTab(tabbedPane, meanHouseholdWealthChart);
      addChartTab(tabbedPane, aggregateEmploymentChart);
      addChartTab(tabbedPane, aggregateOustandingLoanDemandChart);
      addChartTab(tabbedPane, aggregateCommercialLoanChart);
      addChartTab(tabbedPane, meanGoodsSellingPriceChart);
      addChartTab(tabbedPane, meanMinAndMaxFirmProfitChart);
      addChartTab(tabbedPane, aggregateAvailableCashChart);
      addChartTab(tabbedPane, aggregateDividendChart);
      addChartTab(tabbedPane, meanMinMaxGoodsMarkUpRateChart);
      addChartTab(tabbedPane, meanMinMaxUnsoldGoodsChart);
      addChartTab(tabbedPane, fundAggregateTotalAssetsChart);
      addChartTab(tabbedPane, householdSignedAggregateFundContributionChart);
      addChartTab(tabbedPane, fundInvestmentDistributionChart);
      addChartTab(tabbedPane, bankInvestmentDistributionChart);
      addChartTab(tabbedPane, meanFirmProductionChart);
      addChartTab(tabbedPane, meanFirmEquityChart);
      addChartTab(tabbedPane, meanBankEquityChart);
      addChartTab(tabbedPane, meanBankLeverageChart);
      addChartTab(tabbedPane, firmStockPriceChart);
      addChartTab(tabbedPane, bankStockPriceChart);
      addChartTab(tabbedPane, stocksAndLoansReturnChart);
      
      // TODO add your tabs here
   }
   
   protected void setupCharts() {
      meanFirmProductionChart = new TimeSeriesChartGenerator();
      setMetadata(meanFirmProductionChart,
         "Mean Firm Production", "Time", "Goods Produced");
      
      meanFirmEquityChart = new TimeSeriesChartGenerator();
      setMetadata(meanFirmEquityChart,
         "Mean Firm Equity", "Time", "Mean Firm Equity (Cash Units)");
      
      meanBankEquityChart = new TimeSeriesChartGenerator();
      setMetadata(meanBankEquityChart,
         "Mean Bank Equity", "Time", "Mean Bank Equity (Cash Units)");
      
      meanBankLeverageChart = new TimeSeriesChartGenerator();
      setMetadata(meanBankLeverageChart,
         "Mean Bank Leverage", "Time", "Mean Bank Leverage");
      
      firmStockPriceChart = new TimeSeriesChartGenerator();
      setMetadata(firmStockPriceChart, 
         "Firm Price Per Share [Min. Mean. Max.]", "Time", "Stock Price Per Share");
      
      bankStockPriceChart = new TimeSeriesChartGenerator();
      setMetadata(bankStockPriceChart, 
         "Bank Price Per Share [Min. Mean. Max.]", "Time", "Stock Price Per Share");
      
      stocksAndLoansReturnChart = new TimeSeriesChartGenerator();
      setMetadata(stocksAndLoansReturnChart,
         "Stock/Loan Return Rates", "Time", "Stock/Loan Return Rates");
      
      meanHouseholdWealthChart = new TimeSeriesChartGenerator();
      setMetadata(
         meanHouseholdWealthChart,
         "Mean Household Wealth", "Time", "Household Wealth");
      
      aggregateEmploymentChart = new TimeSeriesChartGenerator();
      setMetadata(
         aggregateEmploymentChart,
         "Aggregate Employment (of Max. Labour Supply)", "Time", "Labour Employed");
      
      aggregateOustandingLoanDemandChart = new TimeSeriesChartGenerator();
      setMetadata(
         aggregateOustandingLoanDemandChart,
         "Aggregate Unsatisfied Commercial Loan Demand",
         "Time", "Unsatisfied Commercial Loan Demand (Cash Units)");
      
      aggregateCommercialLoanChart = new TimeSeriesChartGenerator();
      setMetadata(
         aggregateCommercialLoanChart,
         "Aggregate Commercial Loans", "Time", "Commercial Loans (Cash Units)");
      
      meanGoodsSellingPriceChart = new TimeSeriesChartGenerator();
      setMetadata(
         meanGoodsSellingPriceChart,
         "Mean Goods Selling Price (Per Unit)", "Time", "Goods Selling Price");
      
      meanMinAndMaxFirmProfitChart = new TimeSeriesChartGenerator();
      setMetadata(
         meanMinAndMaxFirmProfitChart,
         "Maximum, Mean, Minimum Firm Cash Flow", "Time", "Firm Cash Flow (Cash Units)");
      
      aggregateAvailableCashChart = new TimeSeriesChartGenerator();
      setMetadata(
         aggregateAvailableCashChart,
         "Aggregate Availiable Cash", "Time", "Availiable Cash (Cash Units)");
      
      aggregateDividendChart = new TimeSeriesChartGenerator();
      setMetadata(
         aggregateDividendChart,
         "Aggregate Firm Dividends Paid", "Time", "Firm Dividends Paid");
      
      meanMinMaxGoodsMarkUpRateChart = new TimeSeriesChartGenerator();
      setMetadata(meanMinMaxGoodsMarkUpRateChart,
         "Maximum, Mean, Average Firm Production Markup", "Time",
         "Markup (Selling Price/Production Cost - 1)", 1.0, 2.0
         );
      
      meanMinMaxUnsoldGoodsChart = new TimeSeriesChartGenerator();
      setMetadata(
         meanMinMaxUnsoldGoodsChart,
         "Maximum, Mean, Average Unsold Goods (Units)", "Time",
         "Goods Unsold (Units)", 0.0, 2.50
         );
      
      fundAggregateTotalAssetsChart = new TimeSeriesChartGenerator();
      setMetadata(
         fundAggregateTotalAssetsChart,
         "Aggregate Fund Assets (All Assets)", "Time", "Fund Assets Value"
         );
      
      householdSignedAggregateFundContributionChart = new TimeSeriesChartGenerator();
      setMetadata(
          householdSignedAggregateFundContributionChart,
          "Aggregate Fund Contribution (+ve. ",
            "Time", "Fund Contribution/Withdrawal");
      
      fundInvestmentDistributionChart = new TimeSeriesChartGenerator();
      setMetadata(
         fundInvestmentDistributionChart,
         "Aggregate Fund Investments (Breakdown)"
       + " - Cash, Firm Stock, Bank Stock, Gov. Bond, Bank Bond",
         "Time", "Fund Cumulative Investment (% Assets)", 0, 100.
         );
      
      bankInvestmentDistributionChart = new TimeSeriesChartGenerator();
      setMetadata(
         bankInvestmentDistributionChart,
         "Aggregate Bank Investments (Breakdown)"
       + " - Cash, Firm Stock, Bank Stock, Gov. Bond, Firm Loan",
         "Time", "Bank Cumulative Investment (% Assets)", 0., 100
         );
      
        // TODO create your TimeSeriesChartGenerator here and set up metadata
    }
   
   protected void addChartTab(
      final JTabbedPane tabbedPane,
      final ChartGenerator chart
      ) {
      tabbedPane.addTab(chart.getYAxisLabel(), chart.getChartPanel());
   }
   
   protected void setMetadata(
      final ChartGenerator chart,
      final String title,
      final String xLabel,
      final String yLabel,
      final double lowerY,
      final double upperY
      ) {
      chart.setYAxisRange(lowerY, upperY);
      setMetadata(chart, title, xLabel, yLabel);
   }
   
   protected void setMetadata(
      final ChartGenerator chart,
      final String title,
      final String xLabel,
      final String yLabel
      ) {
      chart.setTitle(title);
      chart.setXAxisLabel(xLabel);
      chart.setYAxisLabel(yLabel);
   }
   
   protected XYSeries initSeriesFor(
      final TimeSeriesChartGenerator chart
      ) {
      chart.removeAllSeries();
      
      final String uniqueChartId = chart.getTitle();
      final XYSeries ret = new XYSeries(uniqueChartId, false);
      chart.addSeries(ret, null);
      return ret;
   }
   
   protected XYSeries initSeriesForAlt(
      final TimeSeriesChartGenerator chart
      ) {
      final String uniqueChartId = chart.getTitle();
      final XYSeries ret = new XYSeries(uniqueChartId, false);
      chart.addSeries(ret, null);
      return ret;
   }
   
   @Override
   public void quit() {
      super.quit();
      
      if (chartFrame != null) {
         chartFrame.dispose();
      }
   }
   
   /**
     * Manual Entry Point. Running this codefile with a single {@link String} argument
     * will result in the execution of a corresponding model file. The {@link String}
     * argument is the name of the desired model codefile not including any {@code .java} 
     * suffix.
     */
   public static void main(final String[] args) throws ClassNotFoundException {
      final String
         modelName = args[0];
      Console c;
      try {
         c = new Console(new MasterModelGUI(modelName));
      } catch (InstantiationException e) {
         throw new ClassNotFoundException();
      } catch (IllegalAccessException e) {
         throw new ClassNotFoundException();
      } catch (ClassNotFoundException e) {
         throw new ClassNotFoundException();
      }
      c.setVisible(true);
   }
}
