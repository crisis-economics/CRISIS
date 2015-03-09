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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import kcl.waterloo.annotation.GJAnnotation;
import kcl.waterloo.annotation.GJAnnotation.TextAnnotation;
import kcl.waterloo.graphics.GJGraph;
import kcl.waterloo.graphics.GJGraphContainer;
import kcl.waterloo.graphics.plots2D.GJPlotInterface;
import kcl.waterloo.graphics.plots2D.GJScatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.crisis_economics.abm.dashboard.LabelFormatterDataTransform;
import eu.crisis_economics.abm.dashboard.Page_Results;
import eu.crisis_economics.abm.dashboard.results.Run;

/**
 * @author Tamás Máhr
 *
 */
public class ScatterPlot implements DynamicPlot {

	protected String xColumnName;
	
	protected String yColumnName;

	protected Color color;
	
	protected String title;
	
	protected GJPlotInterface scatterPlot;
	
	private static Logger logger = LoggerFactory.getLogger(ScatterPlot.class);

	private GJGraph graph;

	private GJGraphContainer graphContainer;

	private TextAnnotation waitingAnnotation;

	private double xMin;
	
	private double xMax;
	
	private double yMin;
	
	private double yMax;
	

	/**
	 * @param xLabel
	 * @param xColumnName
	 * @param yColumnName
	 * @param run
	 * @param yLabel
	 * @param rescale
	 */
	public ScatterPlot(String xColumnName, String yColumnName, Run run, String xLabel, String yLabel, boolean rescale, final Color color) {
		this.xColumnName = xColumnName;
		this.yColumnName = yColumnName;
		this.title = yLabel+" / "+xLabel;
		this.color = color;

		//make turn list (=time) from results map
		double[] xSeries = null;
		if (run.getResults(xColumnName) != null){
			xSeries = new double[run.getResults(xColumnName).size()];
			int i = 0;
			for (Object nextDataPoint : run.getResults(xColumnName)){
				xSeries[i++] = ((Number)nextDataPoint).doubleValue();
			}
		}
		
		double[] ySeries = null;
		if (run.getResults(yColumnName) != null){
			ySeries = new double[run.getResults(yColumnName).size()];
			int i = 0;
			for (Object nextDataPoint : run.getResults(yColumnName)){
				ySeries[i++] = ((Number)nextDataPoint).doubleValue();
			}
		}

		graph = scatterTwoSeries(xLabel, yLabel, xSeries, ySeries, rescale);
		
		autoscale();
		
//		if (!run.resultNames().contains(xColumnName) || !run.resultNames().contains(yColumnName)){
//			throw new IllegalArgumentException("Unknown data sources: " + xColumnName + " or " + yColumnName);
//		}
	}
	
	public ScatterPlot(final String xColumName, final String yColumnName, final double[] xData, final double[] yData, final String xLabel,
			final String yLabel, final boolean rescale, final Color color) {
		this.xColumnName = xColumName;
		this.yColumnName = yColumnName;
		this.title = yLabel + " / " + xLabel;
		this.color = color;
		
		graph = scatterTwoSeries(xLabel, yLabel, xData, yData, rescale);
		
		autoscale();
	}
	
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
		try {
			Number x = (Number) result.get(xColumnName);
			Number y = (Number) result.get(yColumnName);

			if (x == null){
				logger.warn("Cannot plot value for '" + xColumnName + "', no data in results!");
				return;
			}

			if (y == null){
				logger.warn("Cannot plot value for '" +yColumnName + "', no data in results!");
				return;
			}

			double[] yData = Arrays.copyOf(scatterPlot.getYData().getRawDataValues(), scatterPlot.getYData().getRawDataValues().length + 1);
			yData[yData.length - 1] = y.doubleValue();
			scatterPlot.setYData(yData);

			double[] xData = Arrays.copyOf(scatterPlot.getXData().getRawDataValues(), scatterPlot.getXData().getRawDataValues().length + 1);
			xData[xData.length - 1] = x.doubleValue();
			scatterPlot.setXData(xData);
			
			if (x.doubleValue() < xMin){
				xMin = x.doubleValue();
			}
			
			if (x.doubleValue() > xMax){
				xMax = x.doubleValue();
			}
			
			if (y.doubleValue() < yMin){
				yMin = y.doubleValue();
			}
			
			if (y.doubleValue() > yMax){
				yMax = y.doubleValue();
			}

			
			setWaitingForData(false);
			
			double width = xMax - xMin;
			double height = yMax - yMin;
			graph.setAxesBounds(xMin - width / 40, yMin - height / 40, width + width / 20, height + height / 20);

//			graph.autoScale();
		} catch (ClassCastException up){
			logger.warn("x = " + result.get(xColumnName)+ " (" + result.get(xColumnName).getClass() + ") or "+
					"y = " + result.get(yColumnName)+ " (" + result.get(yColumnName).getClass() + ") "+
					"is not a number, cannot be plotted!");
			throw up;
		}
	}

	@Override
	public void addDataCollection(Map<String, List<Object>> result) {
		List<Object> xData = result.get(xColumnName);
		List<Object> yData = result.get(yColumnName);

		if (xData == null){
			logger.warn("Cannot plot value for '" + xColumnName + "', no data in results!");
			return;
		}

		if (yData == null){
			logger.warn("Cannot plot value for '" +yColumnName + "', no data in results!");
			return;
		}

		if (xData.size() != yData.size()){
			logger.error("x and y data list has different length (" + xData.size() + " != " + yData.size() + ")");
			return;
		}
		
		int currentLength = scatterPlot.getYData().getRawDataValues().length;
		double[] newYData = Arrays.copyOf(scatterPlot.getYData().getRawDataValues(), currentLength + yData.size());
		double[] newXData = Arrays.copyOf(scatterPlot.getXData().getRawDataValues(), currentLength + xData.size());

		try{
			for (int i = 0 ; i < xData.size() ; i++){
				double yValue = ((Number)yData.get(i)).doubleValue();
				newYData[currentLength + i] = yValue;
				double xValue = ((Number)xData.get(i)).doubleValue();
				newXData[currentLength + i] = xValue;

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
			}
		} catch (ClassCastException up){
			logger.warn("xData = " + result.get(xColumnName)+ " (" + result.get(xColumnName).getClass() + ") or "+
					"yData = " + result.get(yColumnName)+ " (" + result.get(yColumnName).getClass() + ") "+
					"does not contain numbers, cannot be plotted!");
			throw up;
		}

		scatterPlot.setYData(newYData);
		scatterPlot.setXData(newXData);

		setWaitingForData(false);

		double width = xMax - xMin;
		double height = yMax - yMin;
		graph.setAxesBounds(xMin - width / 40, yMin - height / 40, width + width / 20, height + height / 20);

		
//		graph.autoScale();
		autoscale();
	}

	private void autoscale(){
		
		double[] xData = graph.getPlots().get(0).getXDataValues();
		if (xData != null && xData.length > 0){
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
	
	public double[] getXData(){
		return scatterPlot.getDataModel().getXData().getRawDataValues();
	}
	
	public double[] getYData(){
		return scatterPlot.getDataModel().getYData().getRawDataValues();
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

	private GJGraph scatterTwoSeries(final String xLabel, final String yLabel, final double[] xSeries, final double[] ySeries, final boolean rescale) {
		GJGraph chart  = createScatterChart(xLabel, yLabel, xSeries, ySeries, rescale);

		chart.setEditor(new Page_Results.NullEditor());

		chart.setYTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));
		chart.setXTransform(new LabelFormatterDataTransform(new DecimalFormat("##0.####E0"), 1000, 0.001, chart.getYTransform()));

		return chart;
	}

	private GJGraph createScatterChart(String xLabel, String yLabel, double[] xData, double[] yData, boolean rescale) {

		scatterPlot = GJScatter.createInstance();
		
		if (xData != null && yData != null){
			scatterPlot.setXData(xData);
			scatterPlot.setYData(yData);
			
			xMin = Double.MAX_VALUE;
			xMax = -1 * Double.MAX_VALUE;
			yMin = Double.MAX_VALUE;
			yMax = -1 * Double.MAX_VALUE;

			for (int i = 0 ; i < yData.length ; i++){
				if((Math.abs(yData[i]) >= 1e306) || Double.isInfinite(yData[i]))
				{
					yData[i] = Double.NaN;
				}
					
				if (yData[i] < yMin){
					yMin = yData[i];
				}
				if (yData[i] > yMax){
					yMax = yData[i];
				}
			}

			for (int j = 0 ; j < xData.length ; j++){			
				if((Math.abs(xData[j]) >= 1e306) || Double.isInfinite(xData[j]))
				{
					xData[j] = Double.NaN;
				}
				
				if (xData[j] < xMin){
					xMin = xData[j];
				}
				if (xData[j] > xMax){
					xMax = xData[j];
				}
			}

		}
		
		scatterPlot.setEdgeColor(color);
		
		
		//add both plots to a graph and graph-container
		final GJGraph parent_graph = GJGraph.createInstance();

		graphContainer = GJGraphContainer.createInstance(parent_graph);
		graphContainer.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		graphContainer.setTitle(new JFormattedTextField(title));

		graphContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, Page_Results.INDEX_CHART_HEIGHT));
		graphContainer.setMinimumSize(new Dimension(Page_Results.INDEX_CHART_WIDTH, Page_Results.INDEX_CHART_HEIGHT));
		graphContainer.setPreferredSize(new Dimension(Page_Results.INDEX_CHART_WIDTH, Page_Results.INDEX_CHART_HEIGHT));
	
		parent_graph.add(scatterPlot);

		parent_graph.createAxes();
		parent_graph.setXLabel(xLabel);
		parent_graph.setBottomAxisLabelled(true);
		parent_graph.setYLabel(yLabel);
		parent_graph.setTopAxisLabelled(true);
//		parent_graph.autoScale();
		
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

	public void setXLabel(final String label){
		graph.setXLabel(label);
	}
	
	public void setYLabel(final String label){
		graph.setYLabel(label);
	}
}
