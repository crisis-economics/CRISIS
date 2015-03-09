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
package eu.crisis_economics.abm.simulation;

/**
 * 
 * This class defines constants to specify the order in which the scheduler should execute the <a
 * href="http://cs.gmu.edu/~eclab/projects/mason/docs/classdocs/sim/engine/Steppable.html#step(sim.engine.SimState)"
 * >Steppable#step(sim.engine.SimState)</a> methods scheduled for the same time. This is used to 
 * maintain an order for events in each simulation round.
 * 
 * @author mahr
 * @author phillips
 */
public enum NamedEventOrderings implements SimulatedEventOrder {
   BEFORE_ALL,
   INTERBANK_CB_DEPOSIT_PAYMENTS,
   INTERBANK_LOAN_PAYMENTS,
   SELL_ORDERS,
   INIT_RANDOM,
   FIRM_DECIDE_PRODUCTION,
   INTERBANK_CB_LOAN_PAYMENTS,
   COMMERCIAL_LOAN_PAYMENTS,            // Commercial Loan Payments
   GILT_PAYMENTS,                       // Gilt Payments
   BOND_PAYMENTS,                       // Non-Gilt Bond Payments
   FIRM_SHARE_PAYMENTS,                 // Firm Dividends
   /*
    * TODO
    * 
    * REPO_LOAN_PAYMENTS was originally scheduled just before BANK_SHARE_PAYMENTS
    * in the schedule. That arrangement is known to cause oscillatory instabilities
    * in both central bank and commercial bank timeseries. If REPO_LOAN_PAYMENTS is
    * instead scheduled immediately below this comment, the same oscillations are 
    * eliminated. The schedule order of REPO_LOAN_PAYMENTS remains subject to change.
    */
   REPO_LOAN_PAYMENTS,
   BANK_CONSIDER_PORTFOLIO,             // Bank Portfolio Decisions
   COMMERCIAL_MARKET_BIDDING,           // Commercial Markets
   AFTER_COMMERCIAL_MARKET_BIDDING,     // 
   COMMERCIAL_MARKET_MATCHING,          // 
   COMMERCIAL_MARKET_CLEANING,          // 
   DEPOSIT_MARKET_BIDDING,              // Deposit Markets
   DEPOSIT_MARKET_MATCHING,             // 
   DEPOSIT_PAYMENT,                     // 
   PRE_BANKRUPTCY_STATS,                // Bankruptcy Resolution
   REPLACE_BANKRUPTED_FIRMS,            // 
   POST_BANKRUPTCY_STATS,               // 
   STOCK_MARKET_BIDDING,                // Stock Markets
   STOCK_MARKET_MATCHING,               // 
   HOUSEHOLD_FUND_PAYMENTS,             // Mutual Fund Investments
   /* 
    * This is for the clearing market. Uses market clearing for stocks and
    * loans, rather than Limit Order Book (LoB). 
    */
   PRE_CLEARING_MARKET_MATCHING,
   CLEARING_MARKET_MATCHING,
   POST_CLEARING_MARKET_MATCHING,
   OPEN_VACANCIES,                      // Vacancies
   HIRE_WORKERS,                        //
   FIRM_BUY_INPUTS,                     // Firm Inputs and Production
   LABOUR_MARKET_MATCHING,              //
   POST_LABOUR_MARKET_MATCHING,         //
   GOVERNMENT_PAY_WELFARE_AND_BENEFITS, // Government Welfare and Benefits
   GOODS_INPUT_MARKET_MATCHING,         //
   PRODUCTION,                          //
   /*
    * Previous period labour contracts are now paying wages.
    */
   WAGE_PAYMENT,
   CONSUMPTION,
   GOODS_CONSUMPTION_MARKET_MATCHING,   // Goods Market Consumption
   POST_GOODS_CONSUMPTION_MARKET,       //
   FIRM_ACCOUNTING,
   BANK_SHARE_PAYMENTS,           
   INTERBANK_MARKET_BIDDING,            // Interbank Markets
   INTERBANK_MARKET_MATCHING,           //
   INTERBANK_AFTER_MARKET_MATCHING,     //
   AFTER_ALL;

   double unitIntervalTime;
   
   /** Get the precidence of this event in the simulation cycle. */
   @Override
   public double getUnitIntervalTime() {
      return unitIntervalTime;
   }
   
   @Override
   public int getPriority() {
      return 0;
   }
   
   static public NamedEventOrderings getEventAtTime(double time) {
      time -= Math.floor(time);
      int i = 0;
      for(final NamedEventOrderings event : values()) {
         if(event.getUnitIntervalTime() > time)
            break;
         else ++i;
      }
      return values()[i-1];
   }
   
   static {
      double
         numValues = values().length,
         offset = .5/numValues;
      int counter = -1;
      for(NamedEventOrderings record : values())
         record.unitIntervalTime = ++counter/numValues + offset;
   }
}
