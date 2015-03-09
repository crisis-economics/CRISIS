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
package eu.crisis_economics.abm.contracts.settlements;

import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.government.Government;

/**
  * Static method for creating specialized settlement objects.
  * @author phillips
  */
public final class SettlementFactory {
   private SettlementFactory() { }
   
   /**
     * Create a direct settlement with no intermediaries, taxation,
     * deductions or auxiliaries.
     */
   static public Settlement createDirectSettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty
      ) {
      return new SimpleTwoPartySettlement(firstParty, secondParty);
   }
   
   /**
     * Create a labour wage settlement. If a government agent is present
     * in the simulation, a proportion of of the underlying fund transfer 
     * (as specified by the government) will be deduced and issued to the 
     * government in tax. If no government agent is present in the 
     * simulation, no tax is deducted from funds transfered via the returned
     * settlement object.
     */
   static public Settlement createLabourSettlement(
      final Employer employer,
      final Employee employee
      ) {
      if(Simulation.getRunningModel().hasGovernment())
         return new TaxableWageSettlement(employer, employee);
      else
         return new WageSettlement(employer, employee);
   }
   
   /**
     * Create a settlement for goods payments. If a government agent is present
     * in the simulation, a proportion of of the underlying fund transfer 
     * (as specified by the government) will be deduced and issued to the 
     * government in tax. If no government agent is present in the 
     * simulation, no tax is deducted from funds transfered via the returned
     * settlement object.
     */
   static public Settlement createGoodsSettlement(
      final SettlementParty supplier,
      final SettlementParty buyer
      ) {
      if(Simulation.getRunningModel().hasGovernment())
         return new TaxableGoodsSettlement(buyer, supplier);
      else
         return new GoodsSettlement(buyer, supplier);
   }
   
   /**
     * Create a settlement for dividend payment. If a government agent is present
     * in the simulation, a proportion of of the underlying fund transfer 
     * (as specified by the government) will be deduced and issued to the 
     * government in tax. In the UK financial system, dividend payments are
     * subject to capital gains tax with credits. If no government agent is
     * present in the simulation, no tax is deducted from funds transfered 
     * via the returned settlement object.
     */
   static public Settlement createDividendSettlement(
      final StockReleaser stockReleaser,
      final StockHolder stockHolder
      ) {
      if(Simulation.getRunningModel().hasGovernment())
         return new TaxableDividendSettlement(stockReleaser, stockHolder);
      else
         return new DividendSettlement(stockReleaser, stockHolder);
   }
   
   /**
     * Create a settlement for a share transaction. If a government agent 
     * is present in the simulation, a proportion of of the underlying fund 
     * transfer (as specified by the government) will be deduced as capital
     * gains and issued to the government in tax. If no government agent is
     * present in the simulation, no tax is deducted from funds transfered 
     * via the returned settlement object.
     */
   static public Settlement createShareSaleSettlement(
      final StockHolder supplier,
      final StockHolder buyer
      ) {
      return new ShareSaleSettlement(buyer, supplier);
   }
   
   
   /**
     * Create a settlement for a welfare/benefit payment. Welfare and benefit
     * payments are not taxed.
     */
   static public Settlement createWelfareAndBenefitsSettlement(
      final Government government,
      final Employee employee
      ) {
      return new WelfareAndBenefitsSettlement(government, employee);
   }
}
