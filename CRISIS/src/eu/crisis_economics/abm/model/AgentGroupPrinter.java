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

import java.util.List;

import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.model.plumbing.AgentGroup;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A lightweight printer object for {@link AgentGroup} objects.
  * 
  * @author phillips
  */
final class AgentGroupPrinter {   // Stateless
   /**
     * Print a detailed summary of an {@link AgentGroup} to 
     * the console.
     */
   void print(final AgentGroup agentGroup) {
      final List<DepositingHousehold> depositingHouseholds =
         agentGroup.getAgentsOfType(DepositingHousehold.class);
      final List<StockReleasingFirm> stockReleasingFirms =
         agentGroup.getAgentsOfType(StockReleasingFirm.class);
      final List<CommercialBank> commercialBanks = 
         agentGroup.getAgentsOfType(CommercialBank.class);
      final List<CentralBank> centralBanks = 
         agentGroup.getAgentsOfType(CentralBank.class);
      final List<BadBank> badBanks = 
         agentGroup.getAgentsOfType(BadBank.class);
      final List<Fund> funds = 
         agentGroup.getAgentsOfType(Fund.class);
      
      System.out.printf(
         "---------------------------------------------\n"
       + "Agent Group State %s"
       + "---------------------------------------------\n",
         Simulation.getSimState() != null ? 
            "[Time: " + Simulation.getTime() + "]\n" : 
            "[no simulation state]\n"
         );
      System.out.printf("Depositing households: %d\n", depositingHouseholds.size());
      for(final DepositingHousehold household : depositingHouseholds)
         System.out.printf(
            "Household %s:\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n"
          + " Deposit Value:        %16.10g\n",
            household.getUniqueName(),
            household.getTotalAssets(), 
            household.getTotalLiabilities(),
            household.getEquity(),
            household.getDepositValue()
            );
      System.out.printf(
         "---------------------------------------------\n");
      System.out.printf("Firms: %d\n", stockReleasingFirms.size());
      for(final StockReleasingFirm firm : stockReleasingFirms)
         System.out.printf(
            "Firm %s:\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n"
          + " Number of Shares:     %16.10g\n"
          + " Dividend Per Share:   %16.10g\n",
            firm.getUniqueName(),
            firm.getTotalAssets(),
            firm.getTotalLiabilities(),
            firm.getEquity(),
            firm.getNumberOfEmittedShares(),
            firm.getDividendPerShare()
            );
      System.out.printf(
         "---------------------------------------------\n");
      System.out.printf("Commercial Banks: %d\n", commercialBanks.size());
      for(final CommercialBank bank : commercialBanks) {
         System.out.printf(
            "Commercial Bank %s:\n"
          + " Leverage (now):       %16.10g\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n"
          + " Cash reserve          %16.10g\n",
            bank.getUniqueName(),
            bank.getLeverage(),
            bank.getTotalAssets(),
            bank.getTotalLiabilities(),
            bank.getEquity(),
            bank.getCashReserveValue()
            );
      }
      System.out.printf(
         "---------------------------------------------\n");
      System.out.printf("Central Banks: %d\n", centralBanks.size());
      for(final CentralBank bank : centralBanks) {
         System.out.printf(
            "Central Bank %s:\n"
          + " Leverage (now):       %16.10g\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n"
          + " Cash reserve          %16.10g\n",
            bank.getUniqueName(),
            bank.getLeverage(),
            bank.getTotalAssets(),
            bank.getTotalLiabilities(),
            bank.getEquity(),
            bank.getCashReserveValue()
            );
      }
      System.out.printf(
         "---------------------------------------------\n");
      System.out.printf("Bad Banks: %d\n", badBanks.size());
      for(final BadBank bank : badBanks) {
         System.out.printf(
            "Bad Bank %s:\n"
          + " Leverage (now):       %16.10g\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n"
          + " Cash reserve          %16.10g\n",
            bank.getUniqueName(),
            bank.getLeverage(),
            bank.getTotalAssets(),
            bank.getTotalLiabilities(),
            bank.getEquity(),
            bank.getCashReserveValue()
            );
      }
      System.out.printf(
         "---------------------------------------------\n");
      System.out.printf("Funds: %d\n", funds.size());
      for(final Fund fund : funds)
         System.out.printf(
            "Fund %s:\n"
          + " Total assets:         %16.10g\n"
          + " Total liabilies:      %16.10g\n"
          + " Equity:               %16.10g\n",
            fund.getUniqueName(),
            fund.getTotalAssets(), 
            fund.getTotalLiabilities(),
            fund.getEquity()
            );
      System.out.printf(
         "---------------------------------------------\n");
      return;
   }
}