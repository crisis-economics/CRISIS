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
package eu.crisis_economics.abm.model.configuration;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.GoodsClassifier;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.ManualGoodsClassifier;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.utilities.Pair;

// TODO: subclass
@ConfigurationComponent(
   DisplayName = "Goods Characteristics"
   )
public class AbstractGoodsCharacteristicsConfiguration extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -1818389586606760464L;
   
   public final int 
      DEFAULT_NUMBER_OF_SECTORS = 52,
      DEFAULT_NUMBER_OF_SECTORS_PRODUCE_DURABLE_GOODS = 0;
   
   /** 
     * Durable Goods<br><br>
     * 
     * Durable goods offer distinct {@code decay} and {@code consumption} behaviours.
     * {@code Decay} occurs in time, whenever the simulation clock ticks.
     * {@code Consumption} occurs when goods are used by firms to produce
     * new goods.<br><br>
     *  
     * These events are parametrised by 'loss factors', which describe 
     * the degree of inventory loss suffered by goods owners as a result of 
     * {@code decay} and {@code consumption}.<br><br>
     * 
     * In particular, {@code DURABLE_GOODS_CONSUMPTION_LOSS} is the
     * proportion of goods that disappear as a result of their use during 
     * {@code consumption}. {@code DURABLE_GOODS_CONSUMPTION_LOSS = .4} means
     * that 40% of goods will be destroyed following their usage.
     * Similarly, {@code DURABLE_GOODS_DECAY_LOSS = .8} means that 80% 
     * of goods will be destroyed (will decay) in each simulation cycle.
     */
   public final double
      DEFAULT_DURABLE_GOODS_CONSUMPTION_LOSS = .1,
      DEFAULT_DURABLE_GOODS_DECAY_LOSS = .1;
   
   @Parameter(
      ID = "NUMBER_OF_SECTORS_PRODUCE_DURABLE_GOODS"
      )
   private int
      numberOfSectorsProduceDurableGoods = DEFAULT_NUMBER_OF_SECTORS_PRODUCE_DURABLE_GOODS;
   
   /**
     * Get the number of sectors in this model which produce durable goods types.
     */
   public final int getNumberOfSectorsProduceDurableGoods() {
      return numberOfSectorsProduceDurableGoods;
   }
   
   /**
     * Set the number of sectors in this model which produce durable goods types.
     * The argument should be non-negative.
     */
   public final void setNumberOfSectorsProduceDurableGoods(final int value) {
      numberOfSectorsProduceDurableGoods = value;
   }
   
   /**
     * Verbose description for parameter {@link #getNumberOfSectorsProduceDurableGoods}.
     */
   public final String desNumberOfSectorsProduceDurableGoods() {
      return "The number of sectors producing goods in the Input-Output macroeconomy. Each "
           + "sector corresponds to a distinct good type, so this is also equal to the number "
           + "of different type of goods produced in the economy. Each sector contains a "
           + "number of productive firms. The population of each sector is determined by "
           + "the 'Number of Firms Per Sector' parameter.";
   }
   
   @Parameter(
      ID = "DURABLE_GOODS_CONSUMPTION_LOSS"
      )
   private double 
      durableGoodsConsumptionLoss = DEFAULT_DURABLE_GOODS_CONSUMPTION_LOSS;  // Delta_D
   @Parameter(
      ID = "DURABLE_GOODS_DECAY_LOSS"
      )
   private double
      durableGoodsDecayLoss = DEFAULT_DURABLE_GOODS_DECAY_LOSS;              // Delta_C
   
   /**
     * Get the durable goods depreciation rate for this model.
     */
   public final double getInputGoodsDepreciationRate() {
      return durableGoodsConsumptionLoss;
   }
   
   /**
     * Set the durable goods depreciation rate for this model. The argument
     * should be non-negative and not greater than 1.0.
     */
   public final void setInputGoodsDepreciationRate(final double value) {
      durableGoodsConsumptionLoss = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getInputGoodsDepreciationRate}.
     */
   public final String desInputGoodsDepreciationRate() {
      return 
         "The decay rate corresponding to the depreciation (or abrasion) of goods due" + 
         " to their use as inputs in the production of other goods.  Abrasion " + 
         "occurs when goods are used by firms to produce new goods, and a" + 
         " Input Goods Depreciation Rate = 0.4 means that 40% of goods will be " + 
         "destroyed following consumption activities.   Note that the depreciation " + 
         "is modelled as a decrease in the quantity of goods instead of their value," + 
         " so that there is only one price for each good in the model.";
   }
   
   /**
     * Get the durable goods time-decay rate for this model.
     */
   public final double getDecayableGoodsDepreciationRate() {
       return durableGoodsDecayLoss;
   }
   
   /**
     * Set the durable goods time-decay rate for this model. The argument
     * should be non-negative and not greater than 1.0.
     */
   public final void setDecayableGoodsDepreciationRate(final double value) {
       durableGoodsDecayLoss = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getDecayableGoodsDepreciationRate}.
     */
   public final String desDecayableGoodsDepreciationRate() {
      return 
         "The decay rate corresponding to the depreciation of durable" + 
         " goods due to time.  Decay occurs in time, whenever the simulation clock ticks," + 
         " and a Decayable Goods Depreciation Rate = 0.8 means that 80% of goods will be" + 
         " destroyed when the simulation clock advances.  Note that the depreciation" + 
         " is modelled as a decrease in the quantity of goods instead of their value, so that" +
         " there is only one price for each good in the model.";
   }
   
   /**
     * An on-demand {@link Provider} of {@link GoodsClassifier} objects.
     * 
     * @author phillips
     */
   private static class GoodsClassifierProvider implements Provider<GoodsClassifier> {
      
      @Inject private SectorNameProvider
         sectorNameProvider;
      
      @Inject @Named("NUMBER_OF_SECTORS_PRODUCE_DURABLE_GOODS") private int
         numberOfSectorsProduceDurableGoods;
      @Inject @Named("DURABLE_GOODS_CONSUMPTION_LOSS") private double
         durableGoodsConsumptionLoss;
      @Inject @Named("DURABLE_GOODS_DECAY_LOSS") private double
         durableGoodsDecayLoss;
      
      @Override
      public GoodsClassifier get() {
         final List<String>
            sectorNames = sectorNameProvider.asList();
         /*
          * Properties of Durable Goods
          */
         final Map<String, Pair<Double, Double>> goodsProperties =
            new TreeMap<String, Pair<Double, Double>>();
         int index = 0;
         for(final String sectorName : sectorNames) {
            final double 
               goodDecayLoss,
               goodConsumptionLoss;
            if(index < numberOfSectorsProduceDurableGoods) {                   // Durable
               goodDecayLoss = durableGoodsDecayLoss;
               goodConsumptionLoss = durableGoodsConsumptionLoss; 
            } else {                                                           // Non-Durable
               goodDecayLoss = 1.0;
               goodConsumptionLoss = 1.0;
            }
            goodsProperties.put(sectorName, Pair.create(goodDecayLoss, goodConsumptionLoss));
            index++;
         }
         return new ManualGoodsClassifier(goodsProperties);
      }
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(numberOfSectorsProduceDurableGoods >= 0,
         toParameterValidityErrMsg("the number of sectors producing durable goods is negative."));
      Preconditions.checkArgument(durableGoodsDecayLoss >= 0,
         toParameterValidityErrMsg("the decay loss rate of durable goods is negative."));
      Preconditions.checkArgument(durableGoodsConsumptionLoss >= 0,
         toParameterValidityErrMsg("the consumption loss rate of durable goods is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_SECTORS_PRODUCE_DURABLE_GOODS", numberOfSectorsProduceDurableGoods,
         "DURABLE_GOODS_CONSUMPTION_LOSS",          durableGoodsConsumptionLoss,
         "DURABLE_GOODS_DECAY_LOSS",                durableGoodsDecayLoss
         ));
      bind(GoodsClassifier.class)
         .toProvider(GoodsClassifierProvider.class)
         .in(Singleton.class);
      expose(GoodsClassifier.class);
   }
}
