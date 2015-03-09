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
package eu.crisis_economics.abm.simulation;

import java.util.ArrayList;
import java.util.List;

import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

/**
  * A minimal implementation of the {@link Simulation} class.
  * 
  * @author phillips
  */
public class EmptySimulation extends Simulation {
   
   private static final long serialVersionUID = -4319881089135151227L;
   
   public EmptySimulation(long seed) {
      super(seed);
   }
   
   public EmptySimulation(final long seed, final Schedule schedule) {
      super(seed, schedule);
   }
   
   public EmptySimulation(final MersenneTwisterFast random, final Schedule schedule) {
      super(random, schedule);
   }
   
   public EmptySimulation(final MersenneTwisterFast random) {
      super(random);
   }
   
   @Override
   public int getNumberOfBanks() {
      return 0;
   }
   
   @Override
   public int getNumberOfFirms() {
      return 0;
   }
   
   @Override
   public int getNumberOfHouseholds() {
      return 0;
   }
   
   @Override
   public DepositMarket getDepositMarket() {
      return null;
   }
   
   @Override
   public CommercialLoanMarket getCommercialLoanMarket() {
      return null;
   }
   
   @Override
   public InterbankLoanMarket getInterbankLoanMarket() {
      return null;
   }
   
   @Override
   public StockMarket getStockMarket() {
      return null;
   }
   
   @Override
   public List<Bank> getBanks() {
      return new ArrayList<Bank>();
   }
   
   @Override
   public List<Bank> getRegularBanks() {
      return new ArrayList<Bank>();
   }
   
   @Override
   public List<Firm> getFirms() {
      return new ArrayList<Firm>();
   }
   
   @Override
   public List<Fund> getFunds() {
      return new ArrayList<Fund>();
   }
   
   @Override
   public Government getGovernment() {
      return null;
   }
   
   @Override
   public ClearingHouse getClearingHouse() {
      return null;
   }
   
   @Override
   public List<Household> getHouseholds() {
      return new ArrayList<Household>();
   }
   
   @Override
   public CentralBank getCentralBank() {
      return null;
   }
   
   @Override
   public BadBank getBadBank() {
      return null;
   }
}
