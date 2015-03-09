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
package eu.crisis_economics.abm.bank.strategies;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;

/** A number of possible univariate time series for exogenous processes. */
public class TimeSeriesCatalogue {
   private final static double __2Pi = 2. * Math.PI;
   /** A sinusoidal time series with the given period. The sinusoid is
     * suspended in such a way that crests take the value maxY and troughs
     * take the value minY. */
   public static UnivariateFunction 
      sinusoidalFunction(
         final double period,
         final double minY,
         final double maxY
         ) {
      class SuspendedSinusoid implements UnivariateFunction {
         private final double
            __periodMult = __2Pi / period,
            __amplitude = (maxY - minY) / 2.,
            __minY = minY;
         @Override
         public double value(double time) {
            return (Math.sin(time * __periodMult) + 1.) * __amplitude + __minY;
         }
      };
      return new SuspendedSinusoid();
   }
   
   /** A univariate function that returns a constant, unchanging value. */
   public static UnivariateFunction 
      constantFunction(final double returnValue) {
      class ConstantReturnValue implements UnivariateFunction {
         private final double __returnValue = returnValue;
         @Override
         public double value(double time) {
            return __returnValue;
         }
      };
      return new ConstantReturnValue();
   }
   
   /** A univariate function that returns a constant, unchanging value. */
   public static UnivariateFunction
      squareWaveFunction(
         final double period,
         final double minY,
         final double maxY
         ) {
      class SquareWaveFunction implements UnivariateFunction {
         private final double 
            __periodMult = 1./period,
            __amplitude = (maxY - minY), __minY = minY;
         @Override
         public double value(double time) {
            return (Math.round((time * __periodMult) % 1.) * __amplitude + __minY);
         }
      };
      return new SquareWaveFunction();
   }
   
   // TODO: migrate to /test/
   static public void main(String[] args) {
      System.out.println("testing TimeSeriesCatalogue type..");
      {
      UnivariateFunction suspendedSinusoid = 
          TimeSeriesCatalogue.sinusoidalFunction(10., 1., 10.);
      Double[] expectedValues = 
         { 5.500, 8.145, 9.779, 9.779, 8.145,
           5.500, 2.854, 1.220, 1.220, 2.854 };
      for(int i = 0; i< 10; ++i) {
         final double 
            expectedValue = expectedValues[i],
            gainedValue = suspendedSinusoid.value(i);
         Assert.assertEquals(expectedValue, gainedValue, 1.e-2);
      }
      }
      {
         UnivariateFunction constantReturnFunction = 
             TimeSeriesCatalogue.constantFunction(1.);
         Double[] expectedValues = 
            { 1., 1., 1., 1., 1., 
              1., 1., 1., 1., 1. };
         for(int i = 0; i< 10; ++i) {
            final double 
               expectedValue = expectedValues[i],
               gainedValue = constantReturnFunction.value(i);
            Assert.assertEquals(expectedValue, gainedValue, 1.e-5);
         }
      }
      {
         UnivariateFunction squareWaveFunction = 
             TimeSeriesCatalogue.squareWaveFunction(10., 1., 10.);
         Double[] expectedValues = 
            {  1.,  1.,  1.,  1.,  1.,
              10., 10., 10., 10., 10. };
         for(int i = 0; i< 10; ++i) {
            final double 
               expectedValue = expectedValues[i],
               gainedValue = squareWaveFunction.value(i);
            Assert.assertEquals(expectedValue, gainedValue, 1.e-5);
         }
      }
      System.out.println("TimeSeriesCatalogue tests ok.");
   }
}