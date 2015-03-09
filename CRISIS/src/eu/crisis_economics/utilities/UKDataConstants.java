/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.utilities;

/**
  * TODO
  */
public class UKDataConstants {
   // real-world inputs to objective function - need to create class for objective function value and move these in there.  They are currently moved up here outside of the getObjectiveFunctionValue method to increase speed of genetic algorithm
   static final double     //UK data taken around July 2014, some data may from as early as 2009.  For more information see Excel Spreadsheet 'Summary of UK Targets'
      UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO = 0.0005,   //Average Rate of return for Private Non-Financial Corporations (PNFC) is 12.6% per year, therefore 0.05% per day.  Quite variable though, goes up to 36% for UK Continental Shelf (UKCS) industry i.e. oil and gas.
//    UK_AVERAGE_WAGE = 108 * 26414000 / this.getNumberOfHouseholds(), //£27,000 per person per year, so £108 per day (250 working days in a year).  Also 26,414,000 households in UK, so must adjust for size of household, i.e. if only simulating with 10,000 households, size of household wage is multiplied by 26,414,000/10,000
      UK_AVERAGE_DAILY_WAGE = 108.0, //£108 per day, as £27,000 per year for average wage.
      UK_FIRMS_TOTAL_OPERATING_SURPLUS = 5.8e8,    //£144bn per year, so divide by 250 to get per day.
      UK_GDP = 6.6e9,          //£1.65 trillion per year, so £6.6 bn per day (which corresponds to the time-step)
      UK_OVERNIGHT_UNSECURED_LOAN_RATE = 0.005,    //Sterling Overnight Index Average (SONIA) was 0.47% around 10/07/2014.  We only consider overnight lending in this version of the model so this seems the most appropriate rate.
      UK_NUMBER_OF_HOUSEHOLDS = 26414000,      //26.414 million households in the UK
      UK_STOCK_MARKET_CAPITALIZATION = 3.5e12,//2.1e12,    //Updated to £3.5tn on CapitalIQ.... £2.1 trillion, or 127% of GDP.  This is a stock concept calculated from 127% of GDP flow concept.  Should we rescale to be consistent with daily GDP?
      UK_TOTAL_BANK_CREDIT = 3.2e12,   //£3.2 trillion
      UK_TOTAL_BANK_DEPOSITS = 2.6e12, //£2.6 trillion
      UK_UNEMPLOYMENT_PERCENTAGE = 6.2,    //Calculated in the model in percentage terms, so this is 6.2% as of July 2014  
      UK_AVERAGE_BANK_LEVERAGE = 18.0;  //How do we get Leverage constraint into the model?  Is it a hard-coded value in simulation, or a variable we can vary to minimise the objectiveFunction?
   
   public UKDataConstants() { }   // Immutable
   
   /**
    * The method calculates the value of the objective function, given (currently) hard-coded values
    *  for recent macroeconomic and financial data for the UK.  We use a simple sum of square 
    *  errors functional form.  Need to think about how best to input real-world data into the 
    *  model to make more flexible and user-friendly, i.e. be able to do it from the Dashboard.
    *  @author Ross Richardson
    *  @return The value of the objective function that we want to minimise using the genetic 
    *          algorithm functionality of the Dashboard 
    */
//   @RecorderSource("objectiveFunctionValue")
//   public double getObjectiveFunctionValue(){  //Quick and dirty initially, could improve by creating a class structure!  Ross
//       
//       
//       //Most UK_ data is declared at start of AbstractModel class (around line 126) so that they are not declared at each iteration, which would slow down GA.
//
//       //Variable relies on other model variables, so does it need to be calculated here - could calculate outside of method but inside class if done in a class structure...
//       final double UK_AVERAGE_WAGE = UK_AVERAGE_DAILY_WAGE * UK_NUMBER_OF_HOUSEHOLDS / this.getNumberOfHouseholds();  //£27,000 per year, so £108 per day (250 working days in a year).  Also 26,414,000 households in UK, so must adjust for size of household, i.e. if only simulating with 10,000 households, size of household wage is multiplied by 26,414,000/10,000
//
//       final List<Double> errors = new ArrayList<Double>();
////       errors.add(((this.getStockReturn() - UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO)/UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO)); //Will there be a problem using this when there is a already a mechanism to equalise (arbitrage) this value to the loan rate?
//       errors.add((this.getMeanLabourBidPrice() - UK_AVERAGE_WAGE)/UK_AVERAGE_WAGE);
//       errors.add(((Double)this.getAggProfit() - UK_FIRMS_TOTAL_OPERATING_SURPLUS)/UK_FIRMS_TOTAL_OPERATING_SURPLUS); //Note, getAggProfit() is only for Firms, not Banks!
//       errors.add((this.getGDP() - UK_GDP)/UK_GDP);
//       errors.add((this.getMeanLoanInterestRate() - UK_OVERNIGHT_UNSECURED_LOAN_RATE)/UK_OVERNIGHT_UNSECURED_LOAN_RATE);
//       errors.add((this.getAggBankInvestmentsInStocks() - UK_STOCK_MARKET_CAPITALIZATION)/UK_STOCK_MARKET_CAPITALIZATION);
//       errors.add((this.getAggregateCommercialLoans() - UK_TOTAL_BANK_CREDIT)/UK_TOTAL_BANK_CREDIT);
//       errors.add((this.getAggregateBankDeposits() - UK_TOTAL_BANK_DEPOSITS)/UK_TOTAL_BANK_DEPOSITS);
//       errors.add((this.getUnemploymentPercentage() - UK_UNEMPLOYMENT_PERCENTAGE)/UK_UNEMPLOYMENT_PERCENTAGE);
//       
//       double sumOfSquares = 0;
//       for(int i=0; i < errors.size(); i++)
//       {
//           sumOfSquares += (errors.get(i) * errors.get(i));
//       }
//       return (double)sumOfSquares/(double)errors.size();
//   }
}
