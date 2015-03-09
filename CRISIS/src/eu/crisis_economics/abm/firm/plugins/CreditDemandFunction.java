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
package eu.crisis_economics.abm.firm.plugins;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.loans.Borrower;

/**
  * Credit Demand Functions (CDFs).<br><br>
  * 
  * CDFs are univariate functions accepting (as input) a loan interest rate,
  * and returning (as output) a demand for loan cash.
  * @author phillips
  */
public interface CreditDemandFunction {
    
    /** 
      * A {@link CreditDemandFunction} for risk-averse {@link Borrower}s.<br><br>
      * 
      * In this {@link CreditDemandFunction}, {@link Borrower}s will refuse
      * to borrow any loan cash at rates above their production markup
      * rate. At a given (fixed) interest rate r_min, {@link Borrower}s will
      * request their entire ideal liquidity shortfall in loans. For interest
      * rates greater than r_min and less than the markup rate, the {@link Borrower}
      * profile is a linear hinge function with the value zero at the markup rate.
      */
    static public class RiskAverseCreditDemandFunction
        implements CreditDemandFunction {
        
        private double
           rMin;
        
        /**
          * Create a {@link RiskAverseCreditDemandFunction} object.
          * @param alwaysAcceptableInterestRate
          *        The interest rate below which the {@link Borrower} will
          *        request its entire ideal credit volume loans. The 
          *        {@link Borrower} is indifferent to interest rates below
          *        this threshold.
          */
        @Inject
        public RiskAverseCreditDemandFunction(   // Immutable
        @Named("INTEREST_RATE_INDIFFERENCE_THRESHOLD")
            final double alwaysAcceptableInterestRate
            ) {
            if(alwaysAcceptableInterestRate <= 0.)
                throw new IllegalArgumentException(
                    "CreditDemandFunctions.RiskAverseBorrower: " +
                    " minimal interest rate is non-positive. ");
            this.rMin = alwaysAcceptableInterestRate;
        }
        
        @Override
        public double getCreditDemandResponse(
            double desiredIdealCredit, 
            double productionMarkupRate,
            double interestRate
            ) {
            double
               creditDemand = desiredIdealCredit,
               rMax = productionMarkupRate;
            
            if (interestRate > rMin && interestRate < rMax )
                creditDemand *= rMax/(rMax - rMin) - interestRate/(rMax - rMin);
            else if (interestRate >= rMax)
                return 0.;
            creditDemand = Math.max(creditDemand, 0.);
            
            return creditDemand;
        }
    }
    
    /** 
      * A {@link CreditDemandFunction} for {@link Borrower}{@code s} with
      * custom risk affinity.<br><br>
      * 
      * In this {@link CreditDemandFunction}, {@link Borrower}{@code s} will refuse
      * to take out any loans at rates above their production markup rate.
      * <br><br>
      * 
      * For interest rates r < r_min (where r_min is as follows), 
      * {@link Borrower}s will submit orders for their entire ideal
      * liquidity shortfall in loans. For interest rates r > r_min, and less
      * than the production markup rate, the loan profile is a polynomial 
      * function with a root at r_max.<br><br>
      * 
      * The exact profile of the credit demand, C, as a function of interest
      * rate, r, is:<br><br>
      * 
      * <code><center>
      * C(r) = L * Min(1.0, Max((1 - ((r - r_min)/(r_max-r_min))**B, 0)),
      * </code></center><br><br>
      * 
      * where C(r) is the credit demand, L is the ideal credit, r_min is 
      * the lower bound below which the {@link Borrower} will request C(R)
      * = L, r_max is the production markup rate, and B is the risk-bias
      * exponent.
      * 
      * The expression {@code r_min} is taken to be<br><br>
      * 
      * <code><center>
      * r_min = max(r*, f * mu)
      * </code></center><br><br>
      * 
      * where {@code r*} is a customizable constant, {@code f} is a constant
      * faction in the range {@code [0, 1]} and {@code mu} is the production
      * markup rate.
      */
    static public class CustomRiskToleranceCreditDemandFunction
       implements CreditDemandFunction {
       
       private final double
          rMin,
          riskBiasPower,
          indifferenceThresholdPropOfMarkup;
       
       /**
         * Create a {@link RiskAverseCreditDemandFunction} object with custom
         * parameters;
         * 
         * @param alwaysAcceptableInterestRate
         *        The interest rate below which the {@link Borrower} will
         *        request its entire ideal credit volume in loans. The 
         *        {@link Borrower} is indifferent to interest rates below
         *        this threshold.
         * @param riskBiasPower
         *        The exponent of the polynomial profile.
         * @param indifferenceThreshold
         *        This number ({@code T}) specifies the fraction of the production
         *        markup rate below which the {@link Borrower} will request its
         *        entire ideal credit volume in loans. Concretely, if this
         *        number is 0.1, then the {@link Borrower} will request its
         *        entire ideal credit volume in loans whenever the loan interest
         *        rate is less than {@code mu/10}, where {@code mu} is the 
         *        production markup rate. The credit demand profile between
         *        {@code T} and {@code mu} is a polynomial. This argument should
         *        be nonzero and less than {@code 1.0}.
         */
       @Inject
       public CustomRiskToleranceCreditDemandFunction(   // Immutable
       @Named("INTEREST_RATE_INDIFFERENCE_THRESHOLD")
          final double alwaysAcceptableInterestRate,
       @Named("CREDIT_RISK_AFFINITY")
          final double riskBiasPower,
       @Named("INDIFFERENCE_THRESHOLD_PROPORTION_OF_MARKUP")
          final double indifferenceThreshold
          ) {
          if(alwaysAcceptableInterestRate < 0.)
             throw new IllegalArgumentException(
                getClass().getSimpleName() + " minimal interest rate is non-positive. ");
          Preconditions.checkArgument(
             indifferenceThreshold >= 0. && indifferenceThreshold <= 1.0,
             getClass().getSimpleName() + ": credit expense indifference threshold must be "
           + "in the range [0, 1]."
             );
          this.rMin = alwaysAcceptableInterestRate;
          this.riskBiasPower = riskBiasPower;
          this.indifferenceThresholdPropOfMarkup = indifferenceThreshold;
       }
       
       @Override
       public double getCreditDemandResponse(
          double desiredIdealCredit,
          double productionMarkupRate,
          double interestRate
          ) {
          double
             creditDemand = desiredIdealCredit,
             rMax = productionMarkupRate,
             rIndifferent = Math.max(
                indifferenceThresholdPropOfMarkup * productionMarkupRate, rMin);
          
          if (interestRate > rIndifferent && interestRate < rMax) {
             double
                __deltaR = rMax - rIndifferent,
                __ration = 1. - Math.pow(
                   (interestRate - rIndifferent) / __deltaR, riskBiasPower);
             creditDemand *= __ration;
          } else if (interestRate >= rMax)
             return 0.;
          
          creditDemand = Math.max(creditDemand, 0.);
          return creditDemand;
       }
    }
    
    /** 
      * A {@link CreditDemandFunction} for risk-neutral {@link Borrower}s, 
      * who will submit orders for their entire ideal liquidity shortfall 
      * in loans whenever the borrowing rate is less than the production
      * markup rate. At greater interest rates, {@link Borrower}s will 
      * refrain from borrowing at all (as a marginal profit cannot be 
      * achieved).
      */
    static public class RiskNeutralCreditDemandFunction
        implements CreditDemandFunction {
        
        @Inject
        public RiskNeutralCreditDemandFunction() { }   // Stateless
        
        @Override
        public double getCreditDemandResponse(
            double desiredIdealCredit, 
            double productionMarkupRate,
            double interestRate
            ) {
            if(interestRate < productionMarkupRate)
                return Math.max(desiredIdealCredit, 0.);
            else return 0.;
        }
    }
    
//    /** Experimental CDF. In the current implementation, firms do not 
//     *  borrow at interest rates higher than the markup rate. However, 
//     *  when accounting for existing liquidity, firms are in principle 
//     *  able to command overall profitability at lending rates higher 
//     *  than the markup rate. This CDF (in which agents can borrow at 
//     *  interest rates greater than the markup) does at least have the 
//     *  advantage of safeguarding goods sale market share among producers. 
//     *  This approach is unlikely to be permanent, and the picture may 
//     *  change when labour contracts have extended maturities (among 
//     *  other things).
//     **/
//    static public class ExperimentalCDFBorrower
//        implements CreditDemandFunction { // Stateless
//        
//        public ExperimentalCDFBorrower() { }
//        
//        @Override
//        public double getCreditDemandResponse(
//            double desiredIdealCredit, 
//            double productionMarkupRate,
//            double unitSellingPrice,
//            double interestRate,
//            double liquidResourcesAvailiable
//            ) {
//            if(desiredIdealCredit == 0.) return 0.;
//            double
//               m = productionMarkupRate,
//               absoluteLoanDemand;
//            if(interestRate >= m) {
//                double provisionalCreditDemand = 
//                   (m / (interestRate - m)) * liquidResourcesAvailiable;
//                absoluteLoanDemand = 
//                   Math.min(
//                       Math.pow(provisionalCreditDemand / desiredIdealCredit, 2.) * 
//                          provisionalCreditDemand,
//                       desiredIdealCredit
//                       );
//            } else {
//               absoluteLoanDemand= desiredIdealCredit;
//            }
//            return Math.max(absoluteLoanDemand, 0);
//        }
//    }
    
    /**
      * Compute a credit (loan) demand at a given interest rate.
      *
      * @param desiredIdealCredit
      *        The maximum amount of credit (loans) a {@link Borrower} desires.
      * @param productionMarkupRate
      *        The production markup rate of the {@link Borrower}.
      * @param unitSellingPrice
      *        The price per unit of the goods the {@link Borrower} produces.
      * @param interestRate
      *        The interest rate to test.
      * @param liquidResourcesAvailable
      *        The existing liquid resources available to the {@link Borrower}.
      * @return
      */
    public double getCreditDemandResponse(
        double desiredIdealCredit, 
        double productionMarkupRate,
        double interestRate
        );
}
