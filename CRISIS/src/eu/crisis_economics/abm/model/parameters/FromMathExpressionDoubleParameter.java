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
package eu.crisis_economics.abm.model.parameters;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.utilities.StateVerifier;

/**
  * An implementation of the {@link Parameter}{@code <Double>} interface. This
  * implementation retrieves values from a parsed mathematical expression. The mathematical
  * expression to parse is customizable.<br><br>
  * 
  * A univariate expression (<code>E</code>) of a variable (<code>X</code>) must be
  * provided in {@link String} form. Each call to {@link #get()} is equivalent 
  * to <code>E(X)</code> where <code>X</code> takes the value <code>0</code> for the 
  * first such call, the value <code>1</code> for the second such call, and so on.<br><br>
  * 
  * The maximum number of calls to <code>E</code> is the maximum value of {@link Long}.
  * 
  * @author phillips
  */
public final class FromMathExpressionDoubleParameter extends AbstractParameter<Double> {
   
   private final String
      mathExpression,
      variableName;
   
   private final Expression
      parsedExpression;
   
   /**
     * Create a custom {@link FromMathExpressionDoubleParameter} object.<br><br>
     * 
     * See also {@link FromMathExpressionDoubleParameter} and {@link Parameter}.
     * 
     * @param mathExpression (<code>E</code>)<br>
     *        The mathematical expression to parse.
     * @param variableName (<code>X</code>)<br>
     *        The variable with respect to which <code>E</code> is a univariate function
     *        <code>E(X)</code>.
     * @param parameterName <br>
     *        The {@link String} name of this model parameter.
     */
   @Inject
   public FromMathExpressionDoubleParameter(
   @Named("FROM_MATH_EXPRESSION_DOUBLE_MODEL_PARAMETER_EXPRESSION")
      final String mathExpression,
   @Named("FROM_MATH_EXPRESSION_DOUBLE_MODEL_VARIABLE_NAME")
      final String variableName,
   @Named("FROM_MATH_EXPRESSION_DOUBLE_MODEL_PARAMETER_NAME")
      final String parameterName
      ) {
      super(parameterName);
      StateVerifier.checkNotNull(mathExpression, variableName);
      this.mathExpression = mathExpression;
      this.variableName = variableName;
      
      if(mathExpression.isEmpty())
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": input math expression is empty.");
      
      this.parsedExpression =
         new ExpressionBuilder(mathExpression)
            .variables(variableName)
            .build();
      
      this.useCounter = 0L;
   }
   
   private long
      useCounter;
   
   @Override
   public Double get() {
      return parsedExpression.setVariable(variableName, (double) (useCounter++)).evaluate();
   }
   
   public String getMathExpression() {
      return mathExpression;
   }
   
   @Override
   public Class<?> getParameterType() {
      return Double.class;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "From Math Expression Model Parameter: " + getMathExpression() + ", parameter name: "
            + getName() + ".";
   }
}
