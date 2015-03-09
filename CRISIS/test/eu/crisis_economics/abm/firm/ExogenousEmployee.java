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
package eu.crisis_economics.abm.firm;

import java.util.ArrayList;
import java.util.List;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A simple, exogenous implementation of the {@link Employee} interface.
  * This {@link Employee} has a custom starting cash endowment.
  * @author phillips
  */
public final class ExogenousEmployee implements Employee, Party {
   private final String uniqueName;
   private final Inventory cash;
   private double
      labourSupply,
      askPricePerUnitLabour;
   private List<Labour>
      labourIssued;
   private SimpleLabourMarket market;
   
   /**
     * Create a {@link ExogenousEmployee} with a virtually
     * unlimited cash endowment and with no knowledge of the 
     * {@link SimpleLabourMarket} to which to submit labour orders.
     */
   public ExogenousEmployee() {
       this(null);
   }
   
   /**
     * Create a {@link ExogenousEmployee} with a virtually
     * unlimited cash endowment and with no knowledge of the 
     * {@link SimpleLabourMarket} to which to submit labour orders.
     */
   public ExogenousEmployee(final double initialCash) {
      this(null, initialCash);
   }
   
   /**
     * Create a {@link ExogenousEmployee} with a virtually
     * unlimited cash endowment.
     * @param market
     *        The {@link SimpleLabourMarket} on which the {@link Employee} will
     *        submit labour ask orders.
     */
   public ExogenousEmployee(final SimpleLabourMarket market) {
       this(market, Double.MAX_VALUE / 2.);
   }
   
   /**
     * Create a {@link ExogenousEmployee} object with a custom initial
     * cash endowment.
     * @param market
     *        The {@link SimpleLabourMarket} on which the {@link Employee} will
     *        submit labour ask orders.
     * @param cash
     *        The initial cash endowment. This argument should be non-negative.
     */
   public ExogenousEmployee(final SimpleLabourMarket market, final double cash) {
      this.uniqueName = java.util.UUID.randomUUID().toString();
      this.cash = new SimpleResourceInventory(cash);
      this.labourSupply = 0.;
      this.askPricePerUnitLabour = 0.;
      this.labourIssued = new ArrayList<Labour>();
      this.market = market;
      
       scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "submitLabourAskOrders", NamedEventOrderings.SELL_ORDERS);
      Simulation.repeat(this, "reset", NamedEventOrderings.AFTER_ALL);
   }
   
   @SuppressWarnings("unused")
   // Scheduled
   private void submitLabourAskOrders() {
      if(market == null) return;
      try {
         market.addOrder(this, 1, labourSupply, askPricePerUnitLabour);
      } catch (AllocationException e) {
         // Abandon
      } catch (OrderException e) {
         // Abandon
      }
   }
   
   /**
     * Reset goods demands for the next market session.
     */
   @SuppressWarnings("unused")   // Scheduled
   private void reset() {
      labourSupply = 0.;
      askPricePerUnitLabour = 0.;
   }
   
   /**
     * Set the supply of labour for the next market session.
     * @param supply
     *        The total supply of labour. This argument should
     *        be non-negative.
     * @param askPrice
     *        The ask price per unit of labour to supply. This
     *        argument should be non-negative.s
     */
   public void setLabourSupply(final double supply, final double askPrice) {
      labourSupply = supply;
      askPricePerUnitLabour = askPrice;
   }
   
   @Override
   public String getUniqueName() {
      return uniqueName;
   }
   
   @Override
   public double credit(double amount) throws InsufficientFundsException {
      cash.pull(amount);
      return amount;
   }
   
   @Override
   public void debit(double amount) {
      cash.push(amount);
   }
   
   @Override
   public void cashFlowInjection(double amt) {
      // No action.
   }
   
   @Override
   public void startContract(final Labour labour) throws DoubleEmploymentException {
      if(labourIssued.contains(labour))
         throw new DoubleEmploymentException();
      labourIssued.add(labour);
   }
   
   @Override
   public void endContract(final Labour labour) {
      labourIssued.remove(labour);
   }
   
   @Override
   public double getLabourAmountEmployed() {
      double amount = 0.;
      for(final Labour labour : labourIssued)
         amount += labour.getQuantity();
      return amount;
   }
   
   @Override
   public double getMaximumLabourSupply() {
      return labourSupply;
   }
   
   @Override
   public double getLabourAmountUnemployed() {
      return Math.max(labourSupply - getLabourAmountEmployed(), 0.);
   }
   
   @Override
   public void allocateLabour(double amount) throws AllocationException {
      // No action.
   }
   
   @Override
   public void disallocateLabour(double amount) throws IllegalArgumentException {
      // No action.
   }
   
   @Override
   public void notifyOutgoingEmployment(
      double labourEmployed,
      double wagePerUnitLabour) {
      // No action.
   }
   
   @Override
   public void notifyIncomingWageOrBenefit(double totalPayment) {
      // No action.
   }

   @Override
   public void addOrder(Order order) {
      // No action.
   }

   @Override
   public boolean removeOrder(Order order) {
      return false;
   }

   @Override
   public void updateState(Order order) {
      // No action.
   }
}