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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.firm.ExogenousFirm;
import eu.crisis_economics.abm.firm.FirmFactory;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;

@ConfigurationComponent(
   Description =
"   A simple implementation of the clearing firm class. This firm" + 
"   represents an exogenous producer with access to exogenous funds.<br><br>" + 
"   " + 
"   This firm holds its deposits in two distinct locations: <code> (a)</code> " + 
"   with an indogenous deposit holder, and <code> (b)</code> in an exogenous unmodelled" + 
"   location. The size (cash value) of the deposit account held by <code> (a)</code> is" + 
"   customizable. This firm otherwise has access to limitless exogenous funding" + 
"   via <code> (b)</code>.<br><br>" + 
"   " + 
"   In order to use this agent, you must specify the following quantities " + 
"   as timeseries in simulation time T:" + 
"   " + 
"   <ul>" + 
"     <li> The cash value <code> D(t)</code> of the deposit account to be held with the " + 
"          indogenous source. This firm will pay for all its expenses out of its " + 
"          exogenous funding source. Once per simulation cycle, at time " + 
"          <code> DEPOSIT_PAYMENT</code>, this firm will top up " + 
"          (or subtract from) the value of its indogenous deposit account in order " + 
"          to match your specification for the deposit account value. The indogenous" + 
"          deposit account will otherwise not be modified by this agent." + 
"          If you specify a negative account value <code> D(t) < 0</code>, the value will" + 
"          be taken to be <code> 0</code>.<br><br>" + 
"          " + 
"     <li> The commercial loan demand <code> C(t)</code> per simulation cycle. This firm" + 
"          will order <i>at most</i> <code> C(t)</code> units of commercial loans per simulation" + 
"          cycle. The exact size of commercial loan orders made by this firm is" + 
"          specified by the profile of the credit demand function, and this firm" + 
"          will order the same maximum quantity <code> C(t)</code> from all clearing commercial" + 
"          loan markets to which it belongs;<br><br>" + 
"          " + 
"     <li> The desired labour (workforce size) <code> L(t)</code> per simulation cycle. " + 
"          This firm will post a buy order for <code> max(0., L(t))</code> units of labour once" + 
"          per simulation cycle;<br><br>" + 
"          " + 
"     <li> The desired labour bid price (wage per unit labour) <code> W(t)</code> per " + 
"          simulation cycle. When posting an order for <code> W(t)</code> units of labour," + 
"          this firm will specify an ask price per unit of value <code> max(0, W(t))</code>;" + 
"          <br><br>" + 
"          " + 
"     <li> The production yield <code> Y(T)</code>. Once per simulation cycle, at time " + 
"          <code>PRODUCTION</code> this firm will produce " + 
"          <code> max(0., Y(T))</code> units of goods. The cost of this production activity " + 
"          cannot be detected by any other entity in the simulation. Note that the " + 
"          specification of production goods for this firm may be durable, and," + 
"          in particular, unsold production goods may accumulate in the inventory of" + 
"          this agent in time. This firm will attempt to sell all" + 
"          goods of the type it produces every simulation cycle." + 
"          <br><br>" + 
"        " + 
"     <li> The desired production goods ask price (per unit) <code> P(t)</code> per simulation" + 
"          cycle. If your selection <code> P(t)</code> is negative, it will be treated as " + 
"          <code> 0</code>.<br><br>" + 
"     " + 
"     <li> The total dividend payment <code> D*(t)</code> to be paid every simulation cycle. Once" + 
"          per simulation cycle, at time <code>FIRM_ACCOUNTING</code>, this " + 
"          firm will reset its dividend per share such that the total outgoings in " + 
"          pending dividends amount to <code> max(D*(t), 0)</code>. Note that this firm has " + 
"          no direct control over the recipients of this dividend payment. This dividend" + 
"          will be paid from exogenous funds and not the indogenous firm " + 
"          deposit account.<br><br>" + 
"          " + 
"     <li> The maximum lending rate <code> r(t)</code> at which the firm is willing" + 
"          to participate in commercial loan borrowing. The expression <code> max(r(t), 0.)</code> " + 
"          specifies the root of the firm credit demand function.<br><br>" + 
"   </ul>"
   )
public final class ExogenousFirmsSubEconomy extends AbstractFirmsSubEconomy {
   private static final long serialVersionUID = 1601016198790158864L;
   @Layout(
      Order = 5,
      Title = "Remote Control Decision Rules",
      FieldName = "Deposits D(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_DEPOSITS_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      depositsTimeseries = new ConstantDoubleModelParameterConfiguration(9.5e8);
   
   public TimeseriesDoubleModelParameterConfiguration getDepositsTimeseries() {
      return depositsTimeseries;
   }
   
   public void setDepositsTimeseries(
      final TimeseriesDoubleModelParameterConfiguration depositsTimeseries) {
      this.depositsTimeseries = depositsTimeseries;
   }
   
   @Layout(
      Order = 6,
      FieldName = "Credit Demand C(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_CREDIT_DEMAND_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      commercialLoanDemandTimeseries = new ConstantDoubleModelParameterConfiguration(2.8e9);
   
   public TimeseriesDoubleModelParameterConfiguration getCommercialLoanDemandTimeseries() {
      return commercialLoanDemandTimeseries;
   }
   
   public void setCommercialLoanDemandTimeseries(
      final TimeseriesDoubleModelParameterConfiguration commercialLoanDemandTimeseries) {
      this.commercialLoanDemandTimeseries = commercialLoanDemandTimeseries;
   }
   
   @Layout(
      Order = 7,
      FieldName = "Desired Labour L(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_LABOUR_DEMAND_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      desiredLabourTimeseries = new ConstantDoubleModelParameterConfiguration(40.);
   
   public TimeseriesDoubleModelParameterConfiguration getDesiredLabourTimeseries() {
      return desiredLabourTimeseries;
   }
   
   public void setDesiredLabourTimeseries(
      final TimeseriesDoubleModelParameterConfiguration desiredLabourTimeseries) {
      this.desiredLabourTimeseries = desiredLabourTimeseries;
   }
   
   @Layout(
      Order = 8,
      FieldName = "Wage Offer W(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_WAGE_BID_PRICE_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      wageBidPriceTimeseries = new ConstantDoubleModelParameterConfiguration(6.75e7);
   
   public TimeseriesDoubleModelParameterConfiguration getWageBidPriceTimeseries() {
      return wageBidPriceTimeseries;
   }
   
   public void setWageBidPriceTimeseries(
      final TimeseriesDoubleModelParameterConfiguration wageBidPriceTimeseries) {
      this.wageBidPriceTimeseries = wageBidPriceTimeseries;
   }
   
   @Layout(
      Order = 9,
      FieldName = "Production Y(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_PRODUCTION_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      productionYieldTimeseries = new ConstantDoubleModelParameterConfiguration(70.);
   
   public TimeseriesDoubleModelParameterConfiguration getProductionYieldTimeseries() {
      return productionYieldTimeseries;
   }
   
   public void setProductionYieldTimeseries(
      final TimeseriesDoubleModelParameterConfiguration productionYieldTimeseries) {
      this.productionYieldTimeseries = productionYieldTimeseries;
   }
   
   @Layout(
      Order = 10,
      FieldName = "Selling Price P(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_SELLING_PRICE_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      sellingPriceTimeseries = new ConstantDoubleModelParameterConfiguration(8.25e7);
   
   public TimeseriesDoubleModelParameterConfiguration getSellingPriceTimeseries() {
      return sellingPriceTimeseries;
   }
   
   public void setSellingPriceTimeseries(
      final TimeseriesDoubleModelParameterConfiguration sellingPriceTimeseries) {
      this.sellingPriceTimeseries = sellingPriceTimeseries;
   }
   
   @Layout(
      Order = 10,
      FieldName = "Dividend Payments D*(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_DIVIDEND_PAYMENT"
      )
   private TimeseriesDoubleModelParameterConfiguration
      dividendPaymentTimeseries = new ConstantDoubleModelParameterConfiguration(1.4e9);
   
   public TimeseriesDoubleModelParameterConfiguration getDividendPaymentTimeseries() {
      return dividendPaymentTimeseries;
   }
   
   public void setDividendPaymentTimeseries(
      final TimeseriesDoubleModelParameterConfiguration dividendPaymentTimeseries) {
      this.dividendPaymentTimeseries = dividendPaymentTimeseries;
   }
   
   @Layout(
      Order = 11,
      FieldName = "Maximum Lending Rates r(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_MAXIMUM_LENDING_RATE"
      )
   private TimeseriesDoubleModelParameterConfiguration
      maximumLendingRateTimeseries = new ConstantDoubleModelParameterConfiguration(0.5);
   
   public TimeseriesDoubleModelParameterConfiguration getMaximumLendingRateTimeseries() {
      return maximumLendingRateTimeseries;
   }
   
   public void setMaximumLendingRateTimeseries(
      final TimeseriesDoubleModelParameterConfiguration maximumLendingRateTimeseries) {
      this.maximumLendingRateTimeseries = maximumLendingRateTimeseries;
   }
   
   @Layout(
      Order = 11,
      FieldName = "Credit Demand Function"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FIRM_CREDIT_DEMAND_FUNCTION"
      )
   private CreditDemandFunctionConfiguration
      creditDemandFunction = new CustomRiskToleranceCreditDemandFunctionConfiguration();
   
   public CreditDemandFunctionConfiguration getCreditDemandFunction() {
      return creditDemandFunction;
   }
   
   public void setCreditDemandFunction(
      final CreditDemandFunctionConfiguration creditDemandFunction) {
      this.creditDemandFunction = creditDemandFunction;
   }
   
   @Override
   protected void addBindings() {
      install(new FactoryModuleBuilder()
         .implement(LoanStrategyFirm.class, ExogenousFirm.class)
         .build(FirmFactory.class)
         );
      expose(FirmFactory.class);
      super.addBindings();
   }
}
