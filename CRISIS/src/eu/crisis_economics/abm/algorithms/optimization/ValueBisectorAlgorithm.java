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
package eu.crisis_economics.abm.algorithms.optimization;

import org.testng.Assert;

/**
 * @author      JKP
 * @category    Utilities
 * @see         
 * @since       1.0
 * @version     1.0
 */
public class ValueBisectorAlgorithm<T extends ValueBisectorAlgorithm.Bisectable> {
    
    private final int
      maximumIterations;
   
    public static interface Bisectable {
        double functionValue(double queryCoordinate);
    }
    
    public final static class BisectionResult {
       private double
          bracketLowerBound,
          predictedRootLocation,
          bracketUpperBound;
       
       private BisectionResult(
          final double bracketLowerBound,
          final double predictedRootLocation,
          final double bracketUpperBound) {
          this.bracketLowerBound = bracketLowerBound;
          this.predictedRootLocation = predictedRootLocation;
          this.bracketUpperBound = bracketUpperBound;
       }
       
       public double getBracketLowerBound() {
          return bracketLowerBound;
       }
       
       public double getPredictedRootLocation() {
          return predictedRootLocation;
       }
       
       public double getBracketUpperBound() {
          return bracketUpperBound;
       }
    }
    
    public ValueBisectorAlgorithm() { this(Integer.MAX_VALUE); }
    
    public ValueBisectorAlgorithm(final int maximumIterations) {
       if(maximumIterations < 0)
          throw new IllegalArgumentException(
             "ValueBisectorAlgorithm: maximum number of iterations (value " +
             maximumIterations + ") is negative.");
       this.maximumIterations = maximumIterations;
    }
    
    public BisectionResult bisectInRange(
        T meritFunction,
        double minCoord, 
        double maxCoord,
        double desiredValue
        ) {
        if(maxCoord < minCoord)
            throw new IllegalArgumentException(
                "ValueBisectorAlgorithm.bisectInRange:" +
                " maxCoord < minCoord.");
        double 
            myMaxCoord = maxCoord,
            myMinCoord = minCoord;
        boolean invertDomain;
        {
            double
                lowerEval = meritFunction.functionValue(myMinCoord),
                upperEval = meritFunction.functionValue(myMaxCoord);
            if(lowerEval == desiredValue)
                return new BisectionResult(myMinCoord, myMinCoord, myMinCoord);
            if(upperEval == desiredValue)
                return new BisectionResult(myMaxCoord, myMaxCoord, myMaxCoord);
            if((lowerEval - desiredValue) * (upperEval - desiredValue) > 0)
                throw new IllegalArgumentException(
                    "ValueBisectorAlgorithm.bisectInRange: desired value " +
                    "cannot be found by bisection.");
            invertDomain = (lowerEval > upperEval);
            if(invertDomain) {
                double t = myMaxCoord;
                myMaxCoord = myMinCoord;
                myMinCoord = t;
            }
        }
        double queryCoord;
        int iterations = 0;
        while(Math.abs(myMaxCoord - myMinCoord) > 10*Math.ulp(myMinCoord) && 
             iterations < maximumIterations) {
            queryCoord = (myMinCoord + myMaxCoord)/2.;
            double bisectionPointEval = 
                meritFunction.functionValue(queryCoord);
            if(bisectionPointEval >= desiredValue)
                myMaxCoord = queryCoord;
            else
                myMinCoord = queryCoord;
            ++iterations;
        }
        return invertDomain ? 
           new BisectionResult(myMaxCoord, (myMaxCoord + myMinCoord) / 2., myMinCoord) :
           new BisectionResult(myMinCoord, (myMaxCoord + myMinCoord) / 2., myMaxCoord);
    }
    
    //TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("testing ValueBisectorAlgorithm type..");
        
        {
            class MyBisectable implements ValueBisectorAlgorithm.Bisectable {
                @Override
                public double functionValue(double queryCoordinate) {
                    return queryCoordinate * queryCoordinate;
                }
            }
            
            MyBisectable meritFunction = new MyBisectable();
            
            ValueBisectorAlgorithm<MyBisectable> algorithm = 
                new ValueBisectorAlgorithm<MyBisectable>(); 
            double resultCoord = 
                algorithm.bisectInRange(meritFunction, 0, 1, .5).getPredictedRootLocation();
            
            Assert.assertEquals(
                resultCoord,
                Math.sqrt(.5),
                1.e-12
                );
        }
        
        {
            class MyBisectable implements ValueBisectorAlgorithm.Bisectable {
                @Override
                public double functionValue(double queryCoordinate) {
                    return 1.-queryCoordinate * queryCoordinate;
                }
            }
            
            MyBisectable meritFunction = new MyBisectable();
            
            ValueBisectorAlgorithm<MyBisectable> algorithm = 
                new ValueBisectorAlgorithm<MyBisectable>(); 
            double resultCoord = 
                algorithm.bisectInRange(meritFunction, 0, 1, .7).getPredictedRootLocation();
            
            Assert.assertEquals(
                resultCoord,
                Math.sqrt(.3),
                1.e-12
                );
        }
        
        System.out.println("ValueBisectorAlgorithm tests pass");
        System.out.flush();
    }
}
