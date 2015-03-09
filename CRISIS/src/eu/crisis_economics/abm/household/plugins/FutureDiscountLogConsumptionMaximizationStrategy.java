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
package eu.crisis_economics.abm.household.plugins;

import java.util.Arrays;
import java.util.Random;

/**
  * Preliminary implementation of a future-discount consumption
  * maximization strategy. This strategy is intended for household
  * agents. At each iteration, agents engage in:
  *  (a) consumption, aggregated over all goods types,
  *  (b) contributions/withdrawals to/from funds, and
  *  (c) deposit account transactions,
  * in variable proportions.
  * 
  * The objective of the algorithm is to maximize the sum of 
  * expected discounted log consumption over all time. Deposit 
  * account interest rates, effective goods price (per unit) and
  * nominal income are external to the optimization, and must be 
  * specified by the caller each cycle.
  * @author phillips
  */
class FutureDiscountLogConsumptionMaximizationStrategy {
   
   private final double
      bet,
      gam,
      mmin,
      dmin;
   
   /**
     * Create a new future-discount consumption maximization strategy.
     * In the initial state, the expectation of all future variables
     * is zero.
     * 
     * @param beta Future discount
     * @param gamma Adaptation rate for expectations
     * @param fundInvestmentMin Minimum fund investment
     * @param depositsMin Minimum deposits
     */
   FutureDiscountLogConsumptionMaximizationStrategy(
      final double beta,
      final double gamma,
      final double fundInvestmentMin,
      final double depositsMin
      ) {
      this.bet = beta;
      this.gam = gamma;
      this.mmin = fundInvestmentMin;
      this.dmin = depositsMin;
      this.lastSolution = new double[6];
   }
   
   private double
      Rf,
      RfLast,
      exL,
      exLQ,
      q,
      p,
      y;
   
   private double[] lastSolution;
   
   private double Rf() { return Rf; }
   
   private double RfLast() { return RfLast; }
   
   private double exL() { return exL; }
   
   private double exLQ() { return exLQ; }
   
   private double q() { return q; }
   
   private double p() { return p; }
   
   private double y() { return y; }
   
   private double dLast() { return lastSolution[2]; }
   
   private double mLast() { return lastSolution[1]; }
   
   private double predictNextL() {
      return gam * lastSolution[3] + (1.-gam) * exL;
   }
   
   private double predictNextLQ() {
      return gam * lastSolution[3] * q + (1.-gam) * exLQ;
   }
   
   /**
     * Iterate the optimization.
     * @param Rf Interest rate (absolute) on deposits
     * @param q MutualFund price
     * @param p Effective goods price per unit
     * @param y Nominal income received
     * @return A 3-dimensional vector with the following format:
     *  { consumption budget, desired fund position, desired deposits }.
     */
   double[] computeNext(
      final double Rf,
      final double q,
      final double p,
      final double y
      ) {
      this.RfLast = this.Rf;
      this.Rf = Rf;
      this.q = q;
      this.p = p;
      this.y = y;
      
      double[] result = null;
      result = processBoundaryCaseTwo();
      if(result == null)
         result = processBoundaryCaseThree();
      if(result == null)
         result = processBoundaryCaseFour();
      lastSolution = Arrays.copyOf(result, result.length);
      
      this.exL = predictNextL();
      this.exLQ = predictNextLQ();
      
      System.out.println("- step complete");
      
      return Arrays.copyOf(result, 3);
   }
   
   // Constrained by fund investments
   private double[] processBoundaryCaseTwo() {
      final double
         c = (1 - bet*gam*Rf())/(bet*exL()*p()*Rf() - bet*exL()*gam*p()*Rf()),
         m = mmin,
         d = (1 - bet*Rf()*(gam + exL()*(-1 + gam)*(mmin - mLast())*q() - 
             exL()*(-1 + gam)*dLast()*RfLast() + exL()*y() - exL()*gam*y()))/
             (bet*exL()*(-1 + gam)*Rf()),
         lam = (bet*exL()*(-1 + gam)*Rf())/(-1 + bet*gam*Rf()),
         mu1 = 0.,
         mu2 = (bet*(-1 + gam)*(-exLQ() + (bet*exLQ()*gam + 
               (exL() - bet*exL()*gam)*q())*Rf()))/(-1 + bet*gam*Rf());
      if(isValidState(c, m, d, lam, mu1, mu2))
         return new double[] { c, m, d, lam, mu1, mu2 };
      else return null;
   }
   
   // Constrained by deposits
   private double[] processBoundaryCaseThree() {
      final double
         c = (q() - bet*gam*q())/(bet*exLQ()*p() - bet*exLQ()*gam*p()),
         m = ((1 - bet*gam + bet*exLQ()*(-1 + gam)*mLast())*q() - 
             bet*exLQ()*(-1 + gam)*(dmin - dLast()*RfLast() - y()))/(bet*exLQ()*(-1 + gam)*q()),
         d = dmin,
         lam = (bet*exLQ()*(-1 + gam))/((-1 + bet*gam)*q()),
         mu1 = (bet*(-1 + gam)*(exLQ() - (bet*exLQ()*gam + 
               (exL() - bet*exL()*gam)*q())*Rf()))/((-1 + bet*gam)*q()),
         mu2 = 0.;
      if(isValidState(c, m, d, lam, mu1, mu2))
         return new double[] { c, m, d, lam, mu1, mu2 };
      else return null;
   }
   
   // Maximum consumption
   private double[] processBoundaryCaseFour() {
      final double
         c = (-dmin + (-mmin + mLast())*q() + dLast()*RfLast() + y())/p(),
         m = mmin,
         d = dmin,
         lam = 1./(-dmin + (-mmin + mLast())*q() + dLast()*RfLast() + y()),
         mu1 = (-1 + bet*Rf()*(-(dmin*exL()) + gam + dmin*exL()*gam + 
               exL()*(-1 + gam)*(mmin - mLast())*q() - exL()*(-1 + gam)*dLast()*RfLast() + 
               exL()*y() - exL()*gam*y()))/(dmin + (mmin - mLast())*q() - dLast()*RfLast() - y()),
         mu2 = ((-1 + bet*(gam - exLQ()*mmin + exLQ()*gam*mmin) - 
               bet*exLQ()*(-1 + gam)*mLast())*q() + bet*exLQ()*(-1 + gam)*(dmin - 
               dLast()*RfLast() - y()))/(dmin + (mmin - mLast())*q() - dLast()*RfLast() - y());
      if(isValidState(c, m, d, lam, mu1, mu2))
         return new double[] { c, m, d, lam, mu1, mu2 };
      else return null;
   }
   
   private boolean isValidState(
      final double c,
      final double m,
      final double d,
      final double lambda,
      final double mu1, 
      final double mu2
      ) {
      boolean result = 
         mu1 >= 0. && d >= dmin &&
         mu2 >= 0. && m >= mmin &&
         c >= 0;
      final double residue = 
         p() * c + q() * m + d - y() - q() * mLast() - RfLast() * dLast();
      System.out.printf(
         "%s c: %16.10g m: %16.10g d: %16.10g lam: %16.10g mu1: %16.10g mu2: %16.10g\n",
         result ? "*" : " ", c, m, d, lambda, mu1, mu2);
      if(Math.abs(residue) > 1.e-8)
         return false;
      return result;
   }
   
   static public void main(String[] args) {
      FutureDiscountLogConsumptionMaximizationStrategy algorithm =
         new FutureDiscountLogConsumptionMaximizationStrategy(.98, .7, 0., 0.);
      final Random dice = new Random();
      
      System.out.println("New Session");
      
      for(int i = 0; i< 500; ++i) {
         final double
            Rf = 1. + Math.pow(dice.nextGaussian() * 5.e-2, 2.),
            q = 1. + dice.nextGaussian() * 5.e-2,
            p = Math.pow(dice.nextGaussian(), 2.),
            y = Math.random() + .5;
         double[] position = algorithm.computeNext(Rf, q, p, y);
         System.out.printf("  d: %16.10g m: %16.10g\n", position[1], position[2]);
      }
      
      System.out.println("Session Ends");
   }
}
