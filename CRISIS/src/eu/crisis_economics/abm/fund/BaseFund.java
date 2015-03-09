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
package eu.crisis_economics.abm.fund;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.government.Government;

/**
  * An abstract base class for {@link Fund} {@link Agent}{@code s}. This base class
  * contains @Recorder methods common to all {@link Fund} implementations. In future
  * this class may either become a forwarding implementation, a Java-8 interface with
  * default methods or a similar alternative. This base class is currently necessary
  * to work around limitations in the @Recorder architecture.
  * 
  * @author phillips
  */
public abstract class BaseFund extends Agent implements Fund {
   
   /**
     * Create a {@link BaseFund} {@link Agent}.
     */
   protected BaseFund() { }
   
   @RecorderSource("PercentageValueInStocks")
   public double getPercentageValueInStocks() {
       double valueOfStocks = 0;
       for(final String name : getStockAccounts().keySet()){
           valueOfStocks += getStockAccount(name).getValue();
       }
       return valueOfStocks * 100.0/getTotalAssets();
   }
   
   @RecorderSource("PercentageValueInBankStocks")
   public double getPercentageValueInBankStocks() {
       double valueOfStocks = 0;
       for(final StockAccount account : getStockAccounts().values()){
          if(account.getReleaser() instanceof Bank){
              valueOfStocks += account.getValue();
          }
      }
       return valueOfStocks * 100.0/getTotalAssets();
   }
   
   @RecorderSource("PercentageValueInFirmStocks")
   public double getPercentageValueInFirmStocks() {
       double valueOfStocks = 0;
       for(final StockAccount account : getStockAccounts().values()){
           if(account.getReleaser() instanceof Firm){
               valueOfStocks += account.getValue();
           }
       }
       return valueOfStocks * 100./getTotalAssets();
   }
   
   @RecorderSource("PercentageValueInBonds")
   public double getPercentageValueInBonds() {
       double valueOfLoans = 0;
       for(final Loan loan : getAssetLoans()) {
           valueOfLoans += loan.getFaceValue();
       }
       return valueOfLoans * 100./getTotalAssets();
   }
   
   @RecorderSource("PercentageValueInBankBonds")
   public double getPercentageValueInBankBonds() {
       double valueOfLoans = 0;
       for(final Loan loan : getAssetLoans())
       {
           if(loan.getBorrower() instanceof Bank)
           valueOfLoans += loan.getFaceValue();
       }
       return valueOfLoans * 100.0/getTotalAssets();
   }
   
   @RecorderSource("PercentageValueInGovernmentBonds")
   public double getPercentageValueInGovernmentBonds() {
       double valueOfLoans = 0;
       for(final Loan loan : getAssetLoans())
       {
           if(loan.getBorrower() instanceof Government)
           valueOfLoans += loan.getFaceValue();
       }
       return valueOfLoans * 100.0/getTotalAssets();
   }
}
