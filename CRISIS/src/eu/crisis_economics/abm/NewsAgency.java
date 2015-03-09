/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm;

import java.util.ArrayList;
import java.util.List;

import eu.crisis_economics.abm.annotation.DataCollector;
import eu.crisis_economics.abm.annotation.ReportKey;
import eu.crisis_economics.abm.annotation.ReportSource;
import eu.crisis_economics.abm.annotation.ReportValue;
import eu.crisis_economics.abm.annotation.Collect;
import eu.crisis_economics.abm.annotation.Collect.ChangeType;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author Tamás Máhr
  */
@DataCollector
public class NewsAgency {
   
   @Collect("contractValue")
   protected List<Double> contractValues;
   
   @Collect("loanValue")
   protected List<Double> loanValues;
   
   protected double totalBankEquity;
   
   @Collect("bankEquity")
   protected double lastBankEquity;
   
   public NewsAgency() {
      this.contractValues = new ArrayList<Double>();
      this.loanValues = new ArrayList<Double>();
      
      Simulation.repeat(this, "resetEquity", NamedEventOrderings.AFTER_ALL);
   }
   
   public void resetEquity() {
      System.out.println("Total bank equity in step "
         + Simulation.getFloorTime() + ": " + totalBankEquity);
      totalBankEquity = 0;
   }
   
   @Collect("loanValue")
   protected void saveLoanValues(@ReportKey final String key,
      @ReportValue final double value, @ReportSource final Contract source) {
   }
   
   @Collect(value = "assets", changeType = ChangeType.COLLECTION_REMOVE)
   protected void handleAssetRemovals(@ReportValue final Contract removedAsset,
      @ReportSource final Agent changedAgent) {
   }
   
   @Collect("bankEquity")
   protected void collectBankEquities(@ReportValue double equity,
      @ReportSource Bank bank) {
      totalBankEquity += equity;
   }
}
