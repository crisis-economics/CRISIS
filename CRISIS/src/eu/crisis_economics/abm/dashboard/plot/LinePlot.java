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
package eu.crisis_economics.abm.dashboard.plot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import kcl.waterloo.annotation.GJAnnotation;
import kcl.waterloo.annotation.GJAnnotation.TextAnnotation;
import kcl.waterloo.graphics.GJGraph;
import kcl.waterloo.graphics.GJGraphContainer;
import kcl.waterloo.graphics.plots2D.GJAbstractPlot;
import kcl.waterloo.graphics.plots2D.GJLine;
import kcl.waterloo.graphics.plots2D.GJPlotInterface;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.crisis_economics.abm.dashboard.LabelFormatterDataTransform;
import eu.crisis_economics.abm.dashboard.Page_Results;
import eu.crisis_economics.abm.dashboard.results.Run;


/**
 * @author Tamás Máhr
 *
 */
public class LinePlot implements DynamicPlot {

	private static final String AVG_PLOT_NAME = "average_plot";

	private static final int PLOT_SELECTION_PRECISION = 5;

	private String title;
	
	private boolean plotAverage;
	
	private String[] seriesNames;
	
	private String xDataName;
	
	private Map<String, GJPlotInterface> plots = new HashMap<String, GJPlotInterface>();

	private static Logger logger = LoggerFactory.getLogger(LinePlot.class);

	private GJGraph graph;

	private GJGraphContainer graphContainer;

	private TextAnnotation waitingAnnotation;
	
	private double xMin = Double.NaN;
	
	private double xMax = Double.NaN;
	
	private double yMin = Double.NaN;
	
	private double yMax = Double.NaN;

	private Map<String, Boolean> nanSeries= new HashMap<String, Boolean>();
	
	public LinePlot(final double[] xData, final String xLabel, final String[] columnNames, final Run run, final String[] yLabels,
			final boolean rescale, final boolean plotAverage, final Color[] colors) {
		this(Page_Results.TURN_SERIES_NAME, xData, xLabel, columnNames, run, yLabels, rescale, plotAverage, colors);
	}
	
	/**
	 * Creates a {@link LinePlot} instance and configures it with the given data. No plots are created at construction time. Call the
	 * {@link #getGraphContainer()} method to actually create the waterloo chart objects.
	 * 
	 * @param xData the data for the x axis
	 * @param xLabel the label for the x axis
	 * @param seriesNames the data source names under which the data to be displayed is stored in the {@link Run} object
	 * @param run the {@link Run} object containing the data
	 * @param yLabels the labels for the individual data series, it may contain only one String
	 * @param rescale true if the charts should be rescaled after adding the plots
	 * @param plotAverage true if a the average of the plotted series should also be plotted
	 */
	public LinePlot(final String xDataName, final double[] xData, final String xLabel, final String[] columnNames, final Run run, final String[] yLabels,
			final boolean rescale, final boolean plotAverage, final Color[] colors) {

		title = yLabels[0]+" / "+xLabel;
		this.plotAverage = plotAverage;
		this.seriesNames = columnNames;
		this.xDataName = xDataName;
		
		final double[][]series = new double[columnNames.length][]; 
		for (int i = 0 ; i < columnNames.length ; i++){
			String name = columnNames[i];
			series[i]= new double[xData.length];

			List<Object> results = run.getResults(name);
			if (results != null){
				for (int j = 0 ; j < results.size() ; j++){
					series[i][j] = ((Number)results.get(j)).doubleValue();
					
					if (series[i][j] > 1.0e307){
						series[i][j] = 1.0e307;
					}
					
//					if (java.lang.Double.isNaN(series[i][j])){
//						series[i][j] = 0;
//					}
				}
			}
		}
		
		
		//Check no. of labels. If only 1 use it otherwise use all given and pad if necessary.
		final String[] actYLabels = new String[Math.max(columnNames.length, yLabels.length)];
		if(yLabels.length==1){								
			actYLabels[0]=yLabels[0];
			for (int i = 1 ; i < columnNames.length ; i++)
				actYLabels[i] = "";
		} else {
			for (int i = 0 ; i < yLabels.length ; i++){
				actYLabels[i] = yLabels[i];
			}
			for (int i = yLabels.length ; i < columnNames.length ; i++){
				actYLabels[i] = "";
			}
		}

		checkSeries(columnNames, series);
		
		graph = plotNVariables(xLabel, yLabels, xData, series, columnNames, plotAverage, colors, rescale);
		
		autoscale();
	}
	
////////////////////////////////////
	public LinePlot(final String xLabel, final String label, final Map<String, List<Object>> data, final boolean plotAverage, final boolean rescale, final Color[] colors){
		title = label + " / " + xLabel;
		this.plotAverage = plotAverage;
		this.xDataName = xLabel;
		
		//Convert map data into type that can be used by existing code:		
		final double[] xData = new double[data.get(xLabel).size()];
		final String[] yLabels = new String[data.keySet().size()-1]; 
		final double[][] series = new double[yLabels.length][data.get(xLabel).size()];
		int ind = 0;
		for(String s: data.keySet())
		{
			if(s.matches(xDataName))
			{
				for(int t = 0; t < xData.length; t++)
				{
					xData[t] = ((Number)data.get(s).get(t)).doubleValue();					
				}
			}
			else
			{
				yLabels[ind] = s;
				for(int t = 0; t < xData.length; t++)
				{
					if (((Number)data.get(s).get(t)).doubleValue() > 1.0e307){
						series[ind][t] = 1.0e307;
					}
					else
					{
						series[ind][t] = ((Number)data.get(s).get(t)).doubleValue();
					}
				}
				ind++;
			}
		}
		
		this.seriesNames = yLabels;
		
		checkSeries(seriesNames, series);
		graph = plotNVariables(xLabel, yLabels, xData, series, seriesNames, plotAverage, colors, rescale);		
		autoscale();
	}	
////////////////////////////////////
	
	public LinePlot(String xLabel, String[] yLabels, double[] xData, final double[][] series, final String xDataName, final String[] seriesNames, boolean plotAverage, boolean rescale, Color[] colors){
		title = yLabels[0]+" / "+xLabel;
		this.plotAverage = plotAverage;
		this.seriesNames = seriesNames;
		this.xDataName = xDataName;
		
		checkSeries(seriesNames, series);
		
		graph = plotNVariables(xLabel, yLabels, xData, series, seriesNames, plotAverage, colors, rescale);
		
		autoscale();
	}
	
	/**
	 * Checks whether all series contains at least one non-NaN element.
	 * 
	 * @param series the data series to check
	 * @return an index array containing indexes of series containing only NaN elements
	 */
	private void checkSeries(final String[] seriesNames, final double[][] series){
		for (int i = 0 ; i < series.length ; i++) {
			checkOneSeries(seriesNames[i], series[i]);
		}
	}
	
	private void checkOneSeries(final String seriesName, final double[] aSeries){
		Boolean isNaNSeries = nanSeries.get(seriesName);
		if (aSeries == null && (isNaNSeries == null || isNaNSeries.booleanValue())){
			nanSeries.put(seriesName, true);
		} else {
			for (int j = 0 ; j < aSeries.length ; j++) {
				if (!Double.isNaN(aSeries[j])){
					nanSeries.put(seriesName, false);
					break;
				}

				if (j == aSeries.length - 1){
					nanSeries.put(seriesName, true);
				}
			}
		}
	}
	
	/**
	 * Creates a {@link LinePlot} instance and configures it with the given data. No plots are created at construction time. Call the
	 * {@link #getGraphContainer()} method to actually create the waterloo chart objects.
	 * 
	 * @param xData the data for the x axis
	 * @param xLabel the label for the x axis
	 * @param seriesNames the data source names under which the data to be displayed is stored in the {@link Run} object
	 * @param run the {@link Run} object containing the data
	 * @param yLabels the labels for the individual data series, it may contain only one String
	 * @param rescale true if the charts should be rescaled after adding the plots
	 * @param plotAverage true if a the average of the plotted series should also be plotted
	 */
/*	public void LinePlotAutoCorrelation(final String[] columnNames, final Run run, final String[] yLabels,
			final boolean rescale, final boolean plotAverage, final Color[] colors) {

		String xLabel = "lag"; 
		title = yLabels[0]+" / "+xLabel;
		this.plotAverage = plotAverage;
		this.seriesNames = columnNames;
		final double[][]series = new double[columnNames.length][]; 
		for (int i = 0 ; i < columnNames.length ; i++){
			String name = columnNames[i];
			List<Object> results = run.getResults(name);
			int n = results.size();
			double[] timeSeries = new double[n];
			double[] autoCorrResults = new double[n];
			for(int t = 0; t < n; t++){
				timeSeries[t] = ((Number)results.get(t)).doubleValue();
			}
			autoCorrResults = autocorr(timeSeries);
			if (autoCorrResults != null){
				series[i]= autoCorrResults;
			}
		}
		
		//Check no. of labels. If only 1 use it otherwise use all given and pad if necessary.
		final String[] actYLabels = new String[columnNames.length];
		if(yLabels.length==1){								
			actYLabels[0]=yLabels[0];
			for (int i = 1 ; i < columnNames.length ; i++)
				actYLabels[i] = "";
		} else {
			for (int i = 0 ; i < yLabels.length ; i++){
				actYLabels[i] = yLabels[i];
			}
			for (int i = yLabels.length ; i < columnNames.length ; i++){
				actYLabels[i] = "";
			}
		}
		int n=0;
		for(int j = 0; j< series.length; j++){
			n = Math.max(n, series[j].length);
		}
		double[] xData = new double[n];
		for(int lag = 0; lag < n; lag++){
			xData[lag] = lag;
		}
		graph = plotNVariables(xLabel, yLabels, xData, series, columnNames, plotAverage, colors, rescale);
	}
*/
	
	@Override
	public GJGraphContainer getGraphContainer(){
		return graphContainer;
	}

	@Override
	public void setWaitingForData(boolean waiting) {
		if (waiting){
			waitingAnnotation = GJAnnotation.createText(1, 1, "Waiting for all data to arrive...");
			graphContainer.add(waitingAnnotation);
			graph.setAxesBounds(new Rectangle2D.Double(0, 0, 3, 3));
		} else {
			if (waitingAnnotation != null){
				waitingAnnotation.remove();
				waitingAnnotation = null;
			}
		}
	}
	
	@Override
	public void addData(Map<String, Object> result){
		double avg = 0.0;
		Number tick = (Number) result.get(xDataName);
		int numberOfSeries = plots.size();
		if (plotAverage){
			numberOfSeries--;
		}
		
		for (String columnName : plots.keySet()){
			if (!columnName.equals(AVG_PLOT_NAME)){
				try {
					GJAbstractPlot plot= (GJAbstractPlot) plots.get(columnName);
					Number yValue = (Number)result.get(columnName);
					if (yValue == null){
						logger.warn("Cannot plot value for '" + columnName + "', no data in results!");
						continue;
					}
					
					if (plotAverage){
						avg += yValue.doubleValue() / numberOfSeries;
					}

					addValues(plot, tick.doubleValue(), yValue.doubleValue());
					
					if (tick.doubleValue() < xMin){
						xMin = tick.doubleValue();
					}
					
					if (tick.doubleValue() > xMax){
						xMax = tick.doubleValue();
					}
					
					if (yValue.doubleValue() < yMin){
						yMin = yValue.doubleValue();
					}
					
					if (yValue.doubleValue() > yMax){
						yMax = yValue.doubleValue();
					}

				} catch (ClassCastException e){
					logger.warn("Result '" + columnName + "' is not a number(" + result.get(columnName).getClass() + "), cannot be plotted!");
				}
			}
		}
		
		if (plotAverage){
			addValues(plots.get(AVG_PLOT_NAME), tick.doubleValue(), avg);
		}
		
		setWaitingForData(false);
		double width = xMax - xMin;
		double height = yMax - yMin;
		graph.setAxesBounds(xMin - width / 40, yMin - height / 40, width + width / 20, height + height / 20);
//		graph.autoScale();
	}

	private static void addValues(GJPlotInterface plot, double tick, double yValue) {
		double[] yData = Arrays.copyOf(plot.getYData().getRawDataValues(), plot.getYData().getRawDataValues().length + 1);
		yData[yData.length - 1] = yValue;
		plot.setYData(yData);

		double[] xData = Arrays.copyOf(plot.getXData().getRawDataValues(), plot.getXData().getRawDataValues().length + 1);
		xData[xData.length - 1] = tick;
		plot.setXData(xData);
	}
	
	@Override
	public void addDataCollection(Map<String, List<Object>> result) {
		List<Object> tick = result.get(xDataName);
		double[] avg = null;
		int numberOfSeries = plots.size();
		if (plotAverage){
			avg = new double[tick.size()];
			numberOfSeries--;
		}

		for (String columnName : plots.keySet()){
			if (!columnName.equals(AVG_PLOT_NAME)){
				try {
					GJAbstractPlot plot= (GJAbstractPlot) plots.get(columnName);
					List<Object> yValues = result.get(columnName);
					if (yValues == null){
						logger.warn("Cannot plot value for '" + columnName + "', no data in results!");
						continue;
					}
					
					int currentLength = plot.getYData().getRawDataValues().length;
					double[] yData = Arrays.copyOf(plot.getYData().getRawDataValues(), currentLength + yValues.size());
					double[] xData = Arrays.copyOf(plot.getXData().getRawDataValues(), currentLength + tick.size());
					
					for (int i = 0 ; i < yValues.size() ; i++){
						double yValue = ((Number)yValues.get(i)).doubleValue();
						yData[currentLength + i] = yValue;
						double xValue = ((Number)tick.get(i)).doubleValue();
						xData[currentLength + i] = xValue;
						
						if (xValue < xMin){
							xMin = xValue;
						}
						
						if (xValue > xMax){
							xMax = xValue;
						}
						
						if (yValue < yMin){
							yMin = yValue;
						}
						
						if (yValue > yMax){
							yMax = yValue;
						}
						
						if (plotAverage){
							avg[i] += yValue / numberOfSeries;
						}
						
					}
					
					plot.setYData(yData);
					plot.setXData(xData);
					
				} catch (ClassCastException e){
					logger.warn("Result '" + columnName + "' is not a number(" + result.get(columnName).getClass() + "), cannot be plotted!");
				}
			}
		}
		
		if (plotAverage){
			GJPlotInterface avgPlot = plots.get(AVG_PLOT_NAME);

			int currentLength = avgPlot.getYData().getRawDataValues().length;
			double[] yData = Arrays.copyOf(avgPlot.getYData().getRawDataValues(), currentLength + avg.length);
			double[] xData = Arrays.copyOf(avgPlot.getXData().getRawDataValues(), currentLength + tick.size());
			
			if (avg.length > 0){
				xMin = Double.MAX_VALUE;
				xMax = -1 * Double.MAX_VALUE;
				
				yMin = Double.MAX_VALUE;
				yMax = -1 * Double.MAX_VALUE;
			}
			
			for (int i = 0 ; i < avg.length ; i++){
				yData[currentLength + i] = avg[i];
				xData[currentLength + i] = ((Number)tick.get(i)).doubleValue();
				if (xData[currentLength + i] < xMin){
					xMin = xData[currentLength + i];
				}
				
				if (xData[currentLength + i] > xMax){
					xMax = xData[currentLength + i];
				}
				
				if (yData[currentLength + i]< yMin){
					yMin = yData[currentLength + i];
				}
				
				if (yData[currentLength + i]> yMax){
					yMax = yData[currentLength + i];
				}
			}
			
			avgPlot.setYData(yData);
			avgPlot.setXData(xData);
		}
		
		setWaitingForData(false);
//		double width = xMax - xMin;
//		double height = yMax - yMin;
//		graph.setAxesBounds(xMin - width / 40, yMin - height / 40, width + width / 20, height + height / 20);
//		graph.autoScale();
		autoscale();
	}
	
	public double[] getXData(){
		return graph.getAllPlots().get(0).getDataModel().getXData().getRawDataValues();
	}
	
	public Pair<String[], double[][]> getYData(){
		double[][] values = new double[seriesNames.length][];
		
		int i = 0;
		for (String name : seriesNames){
			GJPlotInterface plot = plots.get(name);
			if (plot == null){
				values[i++] = null;
			} else {
				values[i++] = plot.getDataModel().getYData().getRawDataValues();
			}
		}
		
		return new Pair<String[], double[][]>(seriesNames, values);
	}
	
	private GJGraph plotNVariables(final String xLabel, final String[] yLabels, final double[] xData, final double[][] series, final String[] seriesNames,
			final boolean plotAverage, final Color[] colors, final boolean rescale) {
		
		//check that variables were sent
//		if (series.length == 0)
//			return null;

		int noOfTimeSteps = xData.length;
		if (noOfTimeSteps > 0){
			xMin = xData[0];
			xMax = xData[xData.length - 1];
		}
		
		final double[] avg = new double[noOfTimeSteps];

		// we compute the average even if not painted, because we have to find the min/max values anyway
		for (int i=0;i<noOfTimeSteps;i++){
			avg[i]=0;
			yMin = 0;
			yMax = 0;
		}
		int count = 0;
		for (double[] results : series){
			if (results != null){
				for (int i = 0 ; i < results.length ; i++){
					if((Math.abs(results[i]) >= 1e306) || Double.isInfinite(results[i]))
					{
						series[count][i] = Double.NaN;
					}
					avg[i] += results[i]  / series.length;

					if (results[i] < yMin){
						yMin = results[i];
					}
					if (results[i] > yMax){
						yMax = results[i];
					}
				}
			}
			count++;
		}
		
		
		//make a scatter plot for each variable
		//make a scatter plot for variable 1
		final GJGraph chart;
		chart = (plotAverage)?  createChart(xLabel, yLabels, xData, series, seriesNames, avg, colors, rescale): createChart(xLabel, yLabels, xData, series, seriesNames, colors, rescale);	

		chart.setEditor(new Page_Results.NullEditor());
		
		chart.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
		chart.setXTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
//		chart.setXLeft(xData[0] - 0.5);
//		chart.setXRight(xData[xData.length - 1] + 0.5);
//		Rectangle2D bounds = chart.getDataRange();
//		chart.setYTop(bounds.getMaxY());
//		chart.setYBottom(bounds.getMinY());
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				chart.autoScale();
//			}
//		});
//		chart.setAxesBounds(xData[0], (int)bounds.getY(), xData[xData.length - 1], (int)(bounds.getHeight() + 1));
//		chart.resetView();

		chart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for (String columnName : plots.keySet()) {
					GJPlotInterface plot = plots.get(columnName);
					if (plot.intersects(e.getX(), e.getY())){ // this is based on Shape.intersects(), which is returns true if the click is "inside" the shape of the plot
						ArrayList<Shape> screenDataArray = plot.getScreenDataArray();
						for (Shape shape : screenDataArray) {
							double[] coords = new double[6];
							PathIterator pathIterator = shape.getPathIterator(null);
							Point2D startPoint = null;
							Point2D endPoint = null;
							while (!pathIterator.isDone()){
								if (pathIterator.currentSegment(coords) == PathIterator.SEG_LINETO){
									endPoint = new Point2D.Double(coords[0], coords[1]);
									
									Line2D.Double line = new Line2D.Double(startPoint, endPoint);
									
									double distance = line.ptSegDist(e.getX(), e.getY());
									
									if (distance < PLOT_SELECTION_PRECISION){
										ArrayList<GJPlotInterface> plotList = new ArrayList<GJPlotInterface>();
										plotList.add(plot);
										chart.setSelectedPlots(plotList);
										JPopupMenu popup = new JPopupMenu();
										popup.setLayout(new BorderLayout());
										popup.add(new JLabel(columnName), BorderLayout.CENTER);
										popup.show(chart, e.getX(), e.getY());
										return;
									}
								}
								
								startPoint = new Point2D.Double(coords[0], coords[1]);
								
								pathIterator.next();
							}
						}
						
						
					}
				}
			}
		});

		return chart;
	}

	private void autoscale(){
		
		if (!Double.isNaN(xMin) &&
				!Double.isNaN(xMax) &&
				!Double.isNaN(yMin) &&
				!Double.isNaN(yMax)){
			double yMargin = (yMax - yMin) * 0.1;
			if (yMargin == 0){ // intentional comparison with zero
				if (yMargin == yMax){ // if we only have zero values 
					yMargin = 0.1 * yMax + 1;
				} else {
					yMargin = 0.1 * yMax;
				}
			}
			
			
			double xMargin = (xMax - xMin) * 0.1;
			if (xMargin == 0){ // intentional comparison with zero
				xMargin = 0.1 * xMax + 1;
			}
			
			graph.setAxesBounds(xMin - xMargin, yMin - yMargin, (xMax - xMin) + 2 * xMargin, (yMax - yMin) + 2 * yMargin);
		}
	}
	
	private GJGraph createChart(final String xLabel, final String[] yLabels, final double[] timeList, final double[][] series, final String[] seriesNames, final double[] avg, final Color[] colors, final boolean rescale) {

		final GJGraph parent_graph = createChart(xLabel, yLabels, timeList, series, seriesNames, colors, rescale); 
		//plot the average data
		GJPlotInterface avg_temp= GJLine.createInstance();
		avg_temp.setLineColor(Color.black);
		final float dash1[] = {10.0f};
		BasicStroke s = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
		avg_temp.setLineStroke(s);
		avg_temp.setXData(timeList);
		avg_temp.setYData(avg);
		
		parent_graph.add(avg_temp, rescale);

		plots.put(AVG_PLOT_NAME, avg_temp);

		
		return parent_graph;
	}
	
	private GJGraph createChart(final String xLabel, final String[] yLabels, final double[] timeList, final double[][] series, final String[] seriesNames, final Color[] colors, final boolean rescale) {

		if (series.length != seriesNames.length){
			throw new IllegalArgumentException("The number of data series (" + series.length + ") and the number of series names (" + seriesNames.length + ") should coincide!");
		}

		final GJGraph parent_graph = GJGraph.createInstance();
		
		graphContainer = GJGraphContainer.createInstance(parent_graph);
		graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		graphContainer.setTitle(new JFormattedTextField(title));
		
		graphContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, Page_Results.INDEX_CHART_HEIGHT));
		graphContainer.setMinimumSize(new Dimension(Page_Results.INDEX_CHART_WIDTH, Page_Results.INDEX_CHART_HEIGHT));
		graphContainer.setPreferredSize(new Dimension(Page_Results.INDEX_CHART_WIDTH, Page_Results.INDEX_CHART_HEIGHT));
		
		parent_graph.createAxes();
		parent_graph.setXLabel(xLabel);
		parent_graph.setBottomAxisLabelled(true);
		parent_graph.setYLabel(yLabels[0]);
		parent_graph.setLeftAxisLabelled(true);
//		parent_graph.autoScale();
		
		for (int i = 0 ; i < series.length ; i++){
			Boolean isNanSeries = nanSeries.get(seriesNames[i]);
			
			
			Color c = colors[i];



			// make line plot


			GJPlotInterface line_temp= GJLine.createInstance();
			line_temp.setLineColor(c);
			line_temp.setXData(timeList);

			if (isNanSeries == null || !isNanSeries.booleanValue()){
				line_temp.setYData(series[i]);
			}
			
			parent_graph.add(line_temp, rescale);

			plots.put(seriesNames[i], line_temp);
		}

		// remove the Edit graph menu option since it does not work with insubstantial
		JPopupMenu contextMenu = parent_graph.getContextMenu();
		
		Component[] components = contextMenu.getComponents();
		for (Component component : components) {
			if (component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) component;
				if ("Edit Graph".equals(menuItem.getText())){
					contextMenu.remove(menuItem);
					break;
				}
			}
		}

		Action action = new AbstractAction() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
				try {
					parent_graph.setEditor(null);
				
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					
					MouseEvent mouseEvent = new MouseEvent(parent_graph, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), InputEvent.BUTTON1_MASK, 0, 0, 2, false, MouseEvent.BUTTON1);
					parent_graph.getMouseHandler().mouseClicked(mouseEvent);
					
				} catch (ClassNotFoundException e) {
					logger.error("Exception during nasty hack for the graph editor", e);
				} catch (InstantiationException e) {
					logger.error("Exception during nasty hack for the graph editor", e);
				} catch (IllegalAccessException e) {
					logger.error("Exception during nasty hack for the graph editor", e);
				} catch (UnsupportedLookAndFeelException e) {
					logger.error("Exception during nasty hack for the graph editor", e);
				} finally {
					try {
						UIManager.setLookAndFeel(lookAndFeel);
					} catch (UnsupportedLookAndFeelException e) {
						logger.error("Exception during nasty hack for the graph editor", e);
					}
				}
			}
		};
		
		action.putValue(Action.NAME, "Configure");
		
		contextMenu.add(action);
		
		return parent_graph;
	}

	public void disableMouseInteraction() {
		MouseListener[] mouseListeners = graph.getMouseListeners();
		for (MouseListener mouseListener : mouseListeners) {
			graph.removeMouseListener(mouseListener);
		}
		
		MouseMotionListener[] mouseMotionListeners = graph.getMouseMotionListeners();
		for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
			graph.removeMouseMotionListener(mouseMotionListener);
		}
		
		MouseWheelListener[] mouseWheelListeners = graph.getMouseWheelListeners();
		for (MouseWheelListener mouseWheelListener : mouseWheelListeners) {
			graph.removeMouseWheelListener(mouseWheelListener);
		}
		
		mouseMotionListeners = graph.getLeftAxisPanel().getMouseMotionListeners();
		for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
			graph.getLeftAxisPanel().removeMouseMotionListener(mouseMotionListener);
		}

		mouseMotionListeners = graph.getBottomAxisPanel().getMouseMotionListeners();
		for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
			graph.getBottomAxisPanel().removeMouseMotionListener(mouseMotionListener);
		}
	}
	
	public void setBottomAxisLabelled(final boolean flag){
		graph.setBottomAxisLabelled(flag);
	}
	
	public void setLeftAxisLabelled(final boolean flag){
		graph.setLeftAxisLabelled(flag);
	}
	
	public void setXLabel(final String label){
		graph.setXLabel(label);
	}

	public void setYLabel(final String label){
		graph.setYLabel(label);
	}
}
