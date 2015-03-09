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

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

@ConfigurationComponent(
   DisplayName = "Default Markets"
   )
public final class MarketsSubEconomy
   extends AbstractPublicConfiguration {
   
   /*            *
    * Submodels  *
    *            */
   
   private static final long serialVersionUID = 3958895382221306266L;
   
   @Submodel
   @Parameter(
      ID = "FIRM_SECTOR_NAME_PROVIDER",
      Scoped = false
      )
   private SectorNameProviderConfiguration
      sectorNames = new AutomaticSectorNameProviderConfiguration("SECTOR", 52);
   
   public SectorNameProviderConfiguration getSectorNames() {
      return sectorNames;
   }
   
   public void setSectorNames(
      final SectorNameProviderConfiguration sectorNames) {
      this.sectorNames = sectorNames;
   }
   
   @Submodel
   @Parameter(
      ID = "INPUT_OUTPUT_NETWORK_FACTORY",
      Scoped = false
      )
   private InputOutputMatrixFactoryConfiguration
      inputOutputNetwork = new HomogeneousInputOutputMatrixFactoryConfiguration();
   
   public InputOutputMatrixFactoryConfiguration getInputOutputNetwork() {
      return inputOutputNetwork;
   }
   
   public void setInputOutputNetwork(
      final InputOutputMatrixFactoryConfiguration inputOutputNetwork) {
      this.inputOutputNetwork = inputOutputNetwork;
   }
   
   @Submodel
   @Parameter(
      ID = "GOODS_CHARACTERISTICS_CONFIGURATION",
      Scoped = false
      )
   private AbstractGoodsCharacteristicsConfiguration
      goodsCharacteristics = new AbstractGoodsCharacteristicsConfiguration();
   
   public AbstractGoodsCharacteristicsConfiguration getGoodsCharacteristics() {
      return goodsCharacteristics;
   }

   public void setGoodsCharacteristics(
      final AbstractGoodsCharacteristicsConfiguration goodsCharacteristics) {
      this.goodsCharacteristics = goodsCharacteristics;
   }
   
   @Submodel
   @Parameter(
      ID = "SIMPLE_LABOUR_MARKET_MATCHING_ALGORITHM"
      )
   private LabourMarketConfiguration
      labourMarketComponent = new LabourMarketConfiguration();
   
   public LabourMarketConfiguration getLabourMarketComponent() {
      return labourMarketComponent;
   }
   
   public void setLabourMarketComponent(LabourMarketConfiguration value) {
      labourMarketComponent = value;
   }
   
   @Submodel
   @Parameter(
      ID = "SIMPLE_GOODS_MARKET_MATCHING_ALGORITHM"
      )
   GoodsMarketConfiguration
      goodsMarketComponent = new GoodsMarketConfiguration();
   
   public GoodsMarketConfiguration getGoodsMarketComponent() {
      return goodsMarketComponent;
   }
   
   public void setGoodsMarketComponent(GoodsMarketConfiguration value) {
      goodsMarketComponent = value;
   }
   
   @Submodel
   @Parameter(
      ID = "CLEARING_LOAN_MARKET_CONFIGURATION",
      Scoped = false
      )
   ClearingLoanMarketConfiguration
      loanMarket = new ClearingSimpleCommercialLoanMarketConfiguration();
   
   public ClearingLoanMarketConfiguration getLoanMarket() {
      return loanMarket;
   }
   
   public void setLoanMarket(final ClearingLoanMarketConfiguration value) {
      loanMarket = value;
   }
   
   public final static boolean
      DO_ENABLE_STOCK_TRADING = true;
   
   @Layout(
      FieldName = "Enable Stock Trading?"
      )
   @Parameter(
      ID = "ENABLE_STOCK_TRADING"
      )
   private boolean
      doEnableStockTrading = DO_ENABLE_STOCK_TRADING;
   
   public boolean isDoEnableStockTrading() {
      return doEnableStockTrading;
   }
   
   public void setDoEnableStockTrading(
      final boolean doEnableStockTrading) {
      this.doEnableStockTrading = doEnableStockTrading;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "ENABLE_STOCK_TRADING", doEnableStockTrading
         ));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Markets Subeconomy Component: " + super.toString();
   }
}
