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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.crisis_economics.abm.model.ModelUtils.GetterSetterPair;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.Pair;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A model with a selection of randomly generated parameters. This model is a descendant 
  * of the CRISIS master model backbone, however, a subset of all the available master model
  * parameters have been selected, isolated and extracted in such a way that these parameters
  * can be set randomly for each use of the model.
  * 
  * @author phillips
  */
public class RandomModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public RandomModel() {
      this(1L);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure..",
      Description = "#componentDescription"
      )
   protected static class RandomModelConfiguration {
      
      @Layout(
         Title = "Parameter Perturbation",
         FieldName = "Keep Small Parameters Below 1.0",
         VerboseDescription = "How to perturb existing model parameters",
         Order = 1.0
         )
      private boolean
         doNotIncreaseSmallParametersBeyondOnePointZero = true;
      @Layout(
         FieldName = "Size Sensitivity",
         VerboseDescription = "Should small parameters be purturbed less than large parameters?",
         Order = 1.1
         )
      private boolean
         perturbLargeParametersMoreThanSmallParameters = true;
      @Layout(
         FieldName = "Aggressiveness",
         VerboseDescription = "The degree of model parameter perturbation",
         Order = 1.2
         )
      private double
         aggressiveness = 1.e-2;
      
      public boolean isDoNotIncreaseSmallParametersBeyondOnePointZero() {
         return doNotIncreaseSmallParametersBeyondOnePointZero;
      }
      
      public void setDoNotIncreaseSmallParametersBeyondOnePointZero(
         final boolean value) {
         this.doNotIncreaseSmallParametersBeyondOnePointZero = value;
      }
      
      public boolean isPerturbLargeParametersMoreThanSmallParameters() {
         return perturbLargeParametersMoreThanSmallParameters;
      }
      
      public void setPerturbLargeParametersMoreThanSmallParameters(
         final boolean value) {
         this.perturbLargeParametersMoreThanSmallParameters = value;
      }
      
      public double getAggressiveness() {
         return aggressiveness;
      }
      
      public void setAggressiveness(
         final double value) {
         this.aggressiveness = value;
      }
      
      @Layout(
         Title = "Seed",
         FieldName = "Random Seed",
         Order = 2.0
         )
      private long
         randomSeed = 1L;
      
      public long getRandomSeed() {
         return randomSeed;
      }
      
      public void setRandomSeed(
         final long randomSeed) {
         this.randomSeed = randomSeed;
      }
      
      public RandomModelConfiguration() { }
      
      private Random
         random;
      
      /**
        * Apply a perturbation to a {@link Double} and return the result of this
        * operation.
        */
      private double applyRandomDoublePerturbation(final double input) {
         if(random == null)
            random = new Random(randomSeed);
         boolean
            isLessThanOnePointZero = (input <= 1.0);
         double
            result = input * (1. + aggressiveness * random.nextGaussian());
         if(isLessThanOnePointZero)
            result = NumberUtil.clamp(0., result, 1.);
         return result;
      }
      
      /**
        * Apply a perturbation to a {@link Integer} and return the result of this
        * operation.
        */
      private int applyRandomIntPerturbation(final int input) {
         if(random == null)
            random = new Random(randomSeed);
         final int
            maximum = input * 5;
         return (input == 0 ? 0 : 1) + (maximum <= 1 ? 0 : random.nextInt(maximum - 1));
      }
      
      /**
        * Flush (reset) the random number generator for this object.
        */
      private void flush() {
         this.random = null;
      }
      
      /**
        * Implementation detail.<br><br>
        * 
        * This method queries the model configuration file for getter/setter parameter
        * pairs. For each such pair (of type {@link Double} or {@link Integer}) this
        * method queries the existing parameter value and perturbs this. The resulting
        * list has the following format:
        * 
        * <ul>
        *   <li> A list of pairs whose first element is the {@link GetterSetterPair}
        *        object describing access and mutation for the parameter;
        *   <li> and whose second element is a {@link Pair} containing (a) the existing
        *        parameter value and (b) the intended, perturbed parameter value, 
        *        in that order.
        * </ul>
        */
      private List<Pair<GetterSetterPair, Pair<Number, Number>>>
         getExistingAndModifiedParameters() {
         flush();
         final List<Pair<GetterSetterPair, Pair<Number, Number>>>
            result = new ArrayList<Pair<GetterSetterPair,Pair<Number,Number>>>();
         /*
          * Search for candidate Number parameters to modify:
          */
         final List<GetterSetterPair>
            params = ModelUtils.parameters(new MasterModelConfiguration(), Double.class);
         params.addAll(ModelUtils.parameters(new MasterModelConfiguration(), Integer.class));
          /*
           * Perturb each parameter:
           */
          for(final GetterSetterPair pair : params) {
            final Object
               instance = pair.getInstance();
            Number
               existingSetting;
            {
            final String
               errMsg = getClass().getSimpleName() + ": failed to access parameter " +
                  pair.getGetter().getName().substring(3) + ". Continuing.";
            try {
               existingSetting = (Number) pair.getGetter().invoke(instance);
            } catch (final IllegalArgumentException e) {
               System.err.println(errMsg);
               System.err.flush();
               continue;
            } catch (final IllegalAccessException e) {
               System.err.println(errMsg);
               System.err.flush();
               continue;
            } catch (final InvocationTargetException e) {
               System.err.println(errMsg);
               System.err.flush();
               continue;
            }
            }
            final Number
               newSetting;
            if(existingSetting instanceof Double)
               newSetting = applyRandomDoublePerturbation((Double) existingSetting);
            else
               newSetting = applyRandomIntPerturbation((Integer) existingSetting);
            result.add(
               Pair.create(pair, Pair.create(existingSetting, newSetting)));
         }
         return result;
      }
       
      /**
        * Provides a detailed {@link String} description of this model component
        * and its state. This method is referenced by the {@link ConfigurationComponent}
        * annotation.
        */
      public String componentDescription() {
         final List<Pair<GetterSetterPair, Pair<Number, Number>>>
            settings = getExistingAndModifiedParameters();
         String
            result =
               "The following candidate settings were identified. The first and second "
             + "columns, respectively, represent the default (existing) and randomly perturbed"
             + " (intended) model settings for this simulation.<br><br>";
         result += "<table style=\"width:100%\">";
         result +=
               "<tr><td><b>Parameter</b></td>"
             + "<td><b>Current Value</b></td>"
             + "<td><b>Perturbed Value</b></td></tr>\n";
         for(final Pair<GetterSetterPair, Pair<Number, Number>> record : settings) {
            result += "<tr>";
            final Number
               existingValue = record.getSecond().getFirst(),
               newValue = record.getSecond().getSecond();
            result += String.format(
               existingValue instanceof Double ? 
                  "<td>%s</td><td>%g</td><td>%g</td>\n"
                : "<td>%s</td><td>%d</td><td>%d</td>\n",
               record.getFirst().getGetter().getName().substring(3),
               existingValue,
               newValue
               );
            result += "</tr>";
         }
         return result + "</table>";
      }
   }
   
   @Layout(
      Order = 0,
      Title = "Random Economy",
      FieldName = "Options",
      VerboseDescription = 
         "A model with a selection of randomly generated parameters. This model is a descendant "
       + "of the CRISIS master model backbone, however a subset of all the available parameters "
       + "have been selected, isolated and extracted in such a way that these parameters can be "
       + "set randomly for each use of the model."
      )
   @Submodel
   private RandomModelConfiguration
      components = new RandomModelConfiguration();
   
   public RandomModelConfiguration getComponents() {
      return components;
   }
   
   public void setComponents(
      final RandomModelConfiguration components) {
      this.components = components;
   }
   
   /**
     * Create a {@link RandomModel} with a custom seed.
     */
   public RandomModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      /*
       * Modify all available parameter settings:
       */
      System.out.println("RandomModel: modifying parameter settings:");
      final List<Pair<GetterSetterPair, Pair<Number, Number>>>
         settings = components.getExistingAndModifiedParameters();
      for(final Pair<GetterSetterPair, Pair<Number, Number>> record : settings) {
         final String
            errMsg = getClass().getSimpleName() + ": failed to modify parameter " +
               record.getFirst().getGetter().getName().substring(3) + ". Continuing.";
         try {
            System.out.printf(
               "Modifying %s from %s to %s\n",
               record.getFirst().getGetter().getName().substring(3),
               record.getSecond().getFirst().toString(),
               record.getSecond().getSecond().toString()
               );
            record.getFirst().getSetter().invoke(
               record.getFirst().getInstance(), record.getSecond().getSecond());
         } catch (final IllegalArgumentException e) {
            System.err.println(errMsg);
            System.err.flush();
            continue;
         } catch (final IllegalAccessException e) {
            System.err.println(errMsg);
            System.err.flush();
            continue;
         } catch (final InvocationTargetException e) {
            System.err.println(errMsg);
            System.err.flush();
            continue;
         }
      }
      System.out.println("RandomModel: modifications complete.");
      
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
      doLoop(RandomModel.class, argv);
      System.out.println("CRISIS Random Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = 9081231034818272794L;
}
