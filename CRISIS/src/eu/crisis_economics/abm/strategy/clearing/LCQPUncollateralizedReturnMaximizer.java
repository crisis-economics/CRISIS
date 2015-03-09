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
package eu.crisis_economics.abm.strategy.clearing;

import java.util.List;

import kcl.waterloo.math.ArrayUtils;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

import eu.crisis_economics.utilities.Pair;

/** An uncollateralized marginal profit maximization algorithm, implemented
  * as a single static public method.
  * 
  * This algorithm assumes that the caller has access to a number of asset
  * investment options A and is subject to a range of possible liabilities L. 
  * 
  * Assets offer marginal return rates rA and liabilities grow at a maginal
  * rate rL. The effective marginal return on an asset A is assumed to be 
  * a linear function of the size of the investment in A. Concretely: it
  * may be the case that a small investment in asset A has a higher 
  * probability of returning the stated amount (1 + rA) per unit invested,
  * whereas a large investment in A has an increasing probability
  * of failing to achieve this return. 
  * 
  * The effective return rate on each asset A is taken to to be a linear 
  * function of the size of the investment, and the growth rate in liabilities
  * is also taken to be linear. The profit maximization algorithm is otherwise
  * constrained by (a) constant equity, (b) minimum investment sizes in each
  * asset and liability type, and (c) a risk-weighted leverage upper bound. 
  * Constraint (c) prevents infinite scaling in assets versus liabilities.
  * 
  * @author phillips
  */
public final class LCQPUncollateralizedReturnMaximizer
   extends AbstractUncollateralizedPortfolioReturnMaximizer {
   
   /**
     * Set {@link #VERBOSE_MODE}{@code =true} for verbose (stdout) output.
     */
   private static boolean VERBOSE_MODE = false;
   
   /**
     * Create an {@link LCQPUncollateralizedReturnMaximizer} object.
     * @param toleranceFeasibleSolution
     *        The feasible solution tolerance of the algorithm.
     * @param solutionTolerance
     *        The numerical tolerance target for the algorithm.
     */
   public LCQPUncollateralizedReturnMaximizer() { }
   
   /** 
     * Attempt to perform an uncollateralized return maximization optimization
     * with the specified problem parameters. This method will attempt to solve
     * the optimization problem as follows:<br><br>
     * 
     * (a) Initially an attempt will be made to solve the problem using the 
     *     highest precision goal. On success, the method returns the 
     *     optimum;<br>
     * (b) otherwise, should (a) fail, this method will make two further 
     *     attempts to solve the optimization using lower precision goals.
     *     On success, the method returns the optimum;<br>
     * (c) otherwise, should (b) fail, this method will make one further 
     *     attempt to solve the problem by using the lowest of the above 
     *     precision goals with a larger number of numerical iterations. 
     *     On success, the method returns the optimum;<br>
     * (d) If the problem remains insoluble after step (c), this method 
     *     returns {@link null}.
     * 
     * @param knownAssetRates
     *         Known marginal returns per unit invested in each asset type.
     * @param knownLiabilityRates
     *        The marginal rate of growth for each liability type (Eg. loan
     *        interest to a Lender)
     * @param initialAssetHoldings
     *        The size of existing assets of each type. Elements in this list 
     *        correspond to asset choices in the argument riskAssetRates.
     * @param initialLiabilityHoldings
     *        The size of existing liabilities of each type. Elements in this 
     *        The equity of the caller. This algorithm operates at constant equity.
     * @param lambda
     *        The target leverage of the caller.
     * @return
     *        An unmodifiable list of desired investments in each asset type, and
     *        desired holdings in each liability.
     * 
     * This implementation assumes that all preexisting assets (up to equity) can
     * be discharged, Ie. for any asset A, it is acceptable to have zero investments
     * in A, and uses an LCQP algorithm to determine the optimal investment sums
     * and liability debts.
     */
   @Override
   public List<Double> performOptimization(
      final List<Pair<Double, Double>> knownAssetRates,      // r_a = (\alpha_i + const_i.)
      final List<Pair<Double, Double>> knownLiabilityRates,  // r_l = (\beta_j + const_j.)
      final double cashToSpend,
      final double capitalConstraint
      ) {
      List<Double> result =
         performOptimization(
            knownAssetRates, knownLiabilityRates,
            cashToSpend, capitalConstraint,
            1.e-14, 1.e-14, 10000
            );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-14 for 10000 iterations.");
         System.err.flush();
      }
      result = performOptimization(
         knownAssetRates, knownLiabilityRates,
         cashToSpend, capitalConstraint,
         1.e-13, 1.e-13, 10000
         );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-13 for 10000 iterations.\n");
         System.err.flush();
      }
      result = performOptimization(
         knownAssetRates, knownLiabilityRates,
         cashToSpend, capitalConstraint,
         1.e-12, 1.e-12, 10000
         );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-12 for 10000 iterations.\n");
         System.err.flush();
      }
      result = performOptimization(
         knownAssetRates, knownLiabilityRates,
         cashToSpend, capitalConstraint,
         1.e-12, 1.e-12, 50000
         );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-12 for 50000 iterations.\n");
         System.err.flush();
      }
      
      result = performOptimization(
         knownAssetRates, knownLiabilityRates,
         cashToSpend, capitalConstraint,
         1.e-11, 1.e-11, 50000
         );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-11 for 50000 iterations.\n");
         System.err.flush();
      }
      
      result = performOptimization(
         knownAssetRates, knownLiabilityRates,
         cashToSpend, capitalConstraint,
         1.e-10, 1.e-10, 50000
         );
      if(result != null) return result;
      
      if(VERBOSE_MODE) {
         System.err.printf("UncollateralizedReturnMaximizer.computeOptimalInvestments: " +
            "optimization was insoluble using precision goal 10**-10 for 50000 iterations.\n" +
            "This method was unable to solve the optimization to within tolerances after " +
            "multiple attempts.");
         System.err.flush();
      }
      
      return null;    // Insoluble within tolerances.
   }
   
   /*
    * Implementation detail.
    */
   private List<Double> performOptimization(
      final List<Pair<Double, Double>> knownAssetRates,
      final List<Pair<Double, Double>> knownLiabilityRates,
      final double cashToSpend,
      final double capitalConstraint,
      final double toleranceFeasibleSolution,
      final double solutionTolerance,
      final int numberOfIterations
      ) {
      if(VERBOSE_MODE) {
         System.out.printf("LCQP session:\n");
         for(int i = 0; i< knownAssetRates.size(); ++i) {
            Pair<Double, Double> assetRate = knownAssetRates.get(i);
            System.out.printf(
               "asset:     %16.10g*r + %16.10g\n",
               assetRate.getFirst(), assetRate.getSecond());
         }
         for(int i = 0; i< knownLiabilityRates.size(); ++i) {
            Pair<Double, Double> liabilityRate = knownLiabilityRates.get(i);
            System.out.printf(
               "liabiliy:  %16.10g*r + %16.10g\n",
               liabilityRate.getFirst(), liabilityRate.getSecond());
         }
         System.out.printf(
            "capital constraint: %16.10g\n" +
            "cash to spend:      %16.10g\n",
            capitalConstraint,
            cashToSpend
            );
      }
      
      // minimum market impact
      double
         smallestMarketImpact = Double.MAX_VALUE;
      for(final Pair<Double, Double> rate : knownAssetRates)
         if(rate.getFirst() != 0.)
            smallestMarketImpact = Math.min(smallestMarketImpact, Math.abs(rate.getFirst()));
      for(final Pair<Double, Double> rate : knownLiabilityRates)
         if(rate.getFirst() != 0.)
            smallestMarketImpact = Math.min(smallestMarketImpact, Math.abs(rate.getFirst()));
      
      // QP merit function
      final double
         scaleDown = 1./capitalConstraint,
         scaleUp = 1./scaleDown;
      final int dimension = knownAssetRates.size() + knownLiabilityRates.size();
      final double[][] P = new double[dimension][dimension];
      final double[] Q = new double[dimension];
      final int
         numAssetOptions = knownAssetRates.size(),
         numLiabilityOptions = knownLiabilityRates.size();
      for(int i = 0; i< knownAssetRates.size(); ++i) {
         P[i][i] = -2.*knownAssetRates.get(i).getFirst()*scaleUp;
         Q[i] = -knownAssetRates.get(i).getSecond();
      }
      for(int j = 0; j< knownLiabilityRates.size(); ++j) {
         P[j + numAssetOptions][j + numAssetOptions] = 
            2.*knownLiabilityRates.get(j).getFirst()*scaleUp;
         Q[j + numAssetOptions] = knownLiabilityRates.get(j).getSecond();
      }
      PDQuadraticMultivariateRealFunction objectiveFunction =
         new PDQuadraticMultivariateRealFunction(P, Q, 0);
      
      // G * x \leq h inequality constraints
      // Domain bounds
      final int
         numInequalityConstraints = (cashToSpend == 0.) ? dimension + 1 : dimension + 2;
      final ConvexMultivariateRealFunction[] ineqConstraints =
         new ConvexMultivariateRealFunction[numInequalityConstraints];
      for(int i = 0; i< numAssetOptions; ++i) {
         final double[] linearCoeffs = new double[dimension];
         linearCoeffs[i] = -1.;
         ineqConstraints[i] = new LinearMultivariateRealFunction(linearCoeffs, 0.);
      }
      for(int j = 0; j< numLiabilityOptions; ++j) {
         final double[] linearCoeffs = new double[dimension];
         linearCoeffs[j + numAssetOptions] = -1.;
         ineqConstraints[j + numAssetOptions] = 
            new LinearMultivariateRealFunction(linearCoeffs, 0.);
      }
      // Equity constraint, including cash to spend.
      if(cashToSpend > 0.) {
         double[]
            equityConstraintIneqs = new double[numAssetOptions + numLiabilityOptions];
         for(int i = 0; i< numAssetOptions; ++i)
            equityConstraintIneqs[i] = +1.;
         for(int j = 0; j< numLiabilityOptions; ++j)
            equityConstraintIneqs[j + numAssetOptions] = -1.;
         ineqConstraints[dimension + 1] = 
            new LinearMultivariateRealFunction(equityConstraintIneqs, -cashToSpend*scaleDown);
      }
      
      // Capital inequality constraint
      final double[] capitalIneqCoeffs = new double[dimension];
      double capitalIneqCoeffsScalar = 0.;
      for(int i = 0; i< numAssetOptions; ++i) {
         capitalIneqCoeffs[i] = 1.;
      }
      capitalIneqCoeffsScalar += 1.;
      ineqConstraints[dimension] = new LinearMultivariateRealFunction(
          capitalIneqCoeffs, -capitalIneqCoeffsScalar);
      
      OptimizationRequest session = new OptimizationRequest();
      final double[] strictlyFeasibleInitialPoint = new double[dimension];
      for(int i = 0; i< numAssetOptions; ++i)
         strictlyFeasibleInitialPoint[i] = 1.e-100 * numLiabilityOptions;
      for(int j = 0; j< numLiabilityOptions; ++j)
         strictlyFeasibleInitialPoint[j + numAssetOptions] = 1.e-100 * numAssetOptions;
      session.setInitialPoint(strictlyFeasibleInitialPoint);
      
      session.setF0(objectiveFunction);
      session.setFi(ineqConstraints);
      if(cashToSpend == 0.) {
         // A * x = b equality constraints
         final double[][] A = new double[1][dimension];
         for(int i = 0; i< numAssetOptions; ++i)
            A[0][i] = 1.;
         for(int j = 0; j< numLiabilityOptions; ++j)
            A[0][j + numAssetOptions] = -1.;
         final double[] b = new double[] { 0. };
         session.setA(A);
         session.setB(b);
      }
      session.setToleranceFeas(Math.max(smallestMarketImpact * 1.e-7, toleranceFeasibleSolution));
      session.setTolerance(Math.max(smallestMarketImpact * 1.e-7, solutionTolerance));
      session.setMaxIteration(numberOfIterations);
      session.setInteriorPointMethod(JOptimizer.PRIMAL_DUAL_METHOD);
      
      JOptimizer opt = new JOptimizer();
      opt.setOptimizationRequest(session);
      try {
         opt.optimize();
      } catch (final Exception e) {
         System.err.println("UncollateralizedReturnMaximizer: optimization failed.");
         System.err.flush();
         return null;
      }
      
      List<Double> solution = ArrayUtils.toArrayList(opt.getOptimizationResponse().getSolution());
      for(int i = 0; i< numAssetOptions; ++i) {
         solution.set(i, solution.get(i)*scaleUp);
      }
      for(int j = 0; j< numLiabilityOptions; ++j) {
         final int index = j + numAssetOptions;
         solution.set(index, solution.get(index)*scaleUp);
      }
      return solution;
   }
   
   static {
      /**
        * JOptimize dependency uses Apache Commons logging. The LCQP method 
        * itself may be called several times for a market clearing session,
        * which floods the console with unwanted state information. The
        * following property configuration disables the logging feature. Note
        * that the following switch disables logging for the entire project,
        * so more specific switches should be used in the event the Apache
        * logging is widely used in the project in future.
        */
      System.setProperty(
         "org.apache.commons.logging.Log",
         "org.apache.commons.logging.impl.NoOpLog"
         );
   }
}
