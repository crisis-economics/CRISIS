/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.model.charts;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sim.field.continuous.Continuous2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.model.plumbing.AgentGroup;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.SimulationGui;
import eu.crisis_economics.utilities.NumberUtil;

/**
  * This class is a type of two-dimensional {@link SimulationGui} chart. 
  * Data is plotted as follows:<br><br>
  * 
  *   (a) all {@link Bank}{@code s}, {@link Household}{@code s}, {@link Fund}{@code s}
  *       and {@link Firm}{@code s} known to the active {@link Simulation} are collected;<br>
  *   (b) the equity of all {@link Agent}{@code s} in (a) is computed;<br>
  *   (c) each {@link Agent} is rendered as a colored circle. The length of the bounding box
  *       for each circule is taken to be {@link #SCALE_FACTOR}{@code /max(agent equities)}. Each
  *       circle is plotted at a random point on the plane. The color of each circle 
  *       corresponds to the type of agent represented by it. In this implementation,
  *       {@link Bank}{@code s} are red, {@link Household}{@code s} are blue,
  *       {@link Fund}{@code s} are green and {@link Firm}{@code s} are dark gray.<br><br>
  * 
  * An example of how to use this chart inside {@link SimulationGui} code:<br><br>
    {@code 
      // During GUI initialization:<br>
      final Display2D display = new Display2D(WIDTH, HEIGHT, this);
      display.setClipping(false);
      subEconomyVisualizationFrame = display.createFrame();
      subEconomyVisualizationFrame.setTitle("Components");                 // Name of chart
      c.registerFrame(subEconomyVisualizationFrame);
      subEconomyVisualizationFrame.pack();
      subEconomyVisualizationFrame.setVisible(true);
      economyPortreyal = new EconomyPortrayal((Simulation) super.state);   // Field
      display.attach(economyPortreyal, "Grid")
      
      // Immediately after model initialization:
      economyPortreyal.setupPortrayal();
    }
  * 
  * @author olaf
  * @author phillips (rewrote)
  */
public final class EconomyPortrayal extends ContinuousPortrayal2D {
   
   /**
     * The amount by which to upscale all objects to render. If this 
     * value is doubled, all rendered objects will double in size.
     */
   final private static double SCALE_FACTOR = 3;
   
   private static final long serialVersionUID = 6003901838551744721L;
   
   private final AgentGroup
      population;
   private final Continuous2D
      map;
   private final Map<String, OvalPortrayal2D>
      agentDisplayObjects;
   
   /**
     * creates a time series chart for prices of all instruments in the market
     */
   public EconomyPortrayal(final AgentGroup population) {
      this.population = population;
      this.map = new Continuous2D(1.0, 100, 100);
      this.agentDisplayObjects = new HashMap<String, OvalPortrayal2D>();
   }
   
   public void setupPortrayal() {
      map.clear();
      Simulation.repeat(this, "plot", CustomSimulationCycleOrdering.create(
         NamedEventOrderings.AFTER_ALL, Integer.MAX_VALUE));
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void plot() {
      Map<String, Double>
         bankEquities = new LinkedHashMap<String, Double>(),
         householdEquities = new LinkedHashMap<String, Double>(),
         fundEquities = new LinkedHashMap<String, Double>(),
         firmEquities = new LinkedHashMap<String, Double>();
      
      for(final Bank bank : population.getAgentsOfType(Bank.class))
         bankEquities.put(bank.getUniqueName(), convertMeasurement(bank.getEquity()));
      
      for(final Household household : population.getAgentsOfType(Household.class))
         householdEquities.put(
            household.getUniqueName(), convertMeasurement(household.getEquity()));
      
      for(final Fund fund : population.getAgentsOfType(Fund.class))
         fundEquities.put(fund.getUniqueName(), convertMeasurement(fund.getEquity()));
      
      for(final Firm firm : population.getAgentsOfType(Firm.class))
         firmEquities.put(firm.getUniqueName(), convertMeasurement(firm.getEquity()));
      
      // Find the maximum agent equity among all known model agents.
      final double
         maxPositiveEquity = NumberUtil.max(1.e-2,
            NumberUtil.max(bankEquities).getSecond(),
            NumberUtil.max(householdEquities).getSecond(),
            NumberUtil.max(fundEquities).getSecond(),
            NumberUtil.max(firmEquities).getSecond()
            );
      
      updateOvals(
         population.getAgentsOfType(Bank.class),
         agentDisplayObjects, SCALE_FACTOR / maxPositiveEquity, Color.RED);
      updateOvals(
         population.getAgentsOfType(Household.class),
         agentDisplayObjects, SCALE_FACTOR / maxPositiveEquity, Color.BLUE);
      updateOvals(
         population.getAgentsOfType(Fund.class),
         agentDisplayObjects, SCALE_FACTOR / maxPositiveEquity, Color.GREEN);
      updateOvals(
         population.getAgentsOfType(Firm.class),
         agentDisplayObjects, SCALE_FACTOR / maxPositiveEquity, Color.DARK_GRAY);
      
      // tell the portrayals what to portray and how to portray them
      setField(map);
   }
   
   /**
     * Convert the underlying measurement (eg. agent equity) to
     * the scale of the object to plot. In this implementation,
     * the area of the plotted object is assumed to be proportional
     * to the underlying measurement (scale ~ sqrt(measurement)).
     */
   private double convertMeasurement(double measurement) {
      return Math.pow(Math.max(0., measurement), .5);
   }
   
   /**
     * Implementation detail, common code.
     */
   private <T extends IAgent & UniquelyIdentifiable> void updateOvals(
      final List<T> agents,
      final Map<String, OvalPortrayal2D> displays,
      final double scaleFactor,
      final Color color
      ) {
      for(final T agent : agents) {
         final double
            absoluteScale = scaleFactor * Math.max(convertMeasurement(agent.getEquity()), 0.);
         if(map.exists(agent))
            agentDisplayObjects.get(agent.getUniqueName()).scale = absoluteScale;
         else {
            OvalPortrayal2D displayObject = new OvalPortrayal2D(color, absoluteScale);
            agentDisplayObjects.put(agent.getUniqueName(), displayObject);
            setPortrayalForClass(agent.getClass(), displayObject);
         }
      }
   }
}
