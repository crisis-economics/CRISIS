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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;

/**
  * The default, configurable model file for use with the dashboard system. This
  * model file provies an extended and flexible drilldown structure when launched
  * via the dashboard. Subsections of the simulated economy can be enabled and
  * disabled at will, as can the relationships between {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public class MasterModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   @Layout(
      Title = "Agent && Market Types",
      Order = 1,
      FieldName = "Agents & Markets",
      VerboseDescription = "Agents and markets available to the simulator"
      )
   @Submodel({MasterModelConfiguration.class})
   private AbstractPublicConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   public AbstractPublicConfiguration getAgentsConfiguration() {
      return agentsConfiguration;
   }
   
   public void setAgentsConfiguration(final AbstractPublicConfiguration agents) {
      this.agentsConfiguration = agents;
   }
   
   /*                             *
    * Plumbing                    *
    *                             */
   
   @Layout(
      Title = "Trades && Relationships",
      Order = 2,
      FieldName = "Trades & Relationships",
      VerboseDescription = 
         "The population to simulate, relationships, and interconnections between agents."
      )
   @Submodel
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public MasterModel() {
      super();
   }
   
   public MasterModel(long seed) {
      super(seed);
   }
   
   public AbstractPlumbingConfiguration getPlumbingConfiguration() {
      return plumbingConfiguration;
   }
   
   public void setPlumbingConfiguration(
      final AbstractPlumbingConfiguration plumbing) {
      this.plumbingConfiguration = plumbing;
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      return agentsConfiguration;
   }
   
   @Override
   protected AbstractPlumbingConfiguration getPlumbing() {
      return plumbingConfiguration;
   }
   
   /**
     * Entry Point
     */
   public static void main(final String[] argv) {
      doLoop(MasterModel.class, argv);
      System.out.println("CRISIS Master Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -3582148240143753851L;
}
