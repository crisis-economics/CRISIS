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
package eu.crisis_economics.abm.model.configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FromMathExpressionDoubleParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Expression",
   Description =
      "A model parameter (with floating point value) parsed from an expression, E(X). This model "
    + "parameter accepts a string representation of a univariate function as input. The function "
    + "is fully customizable."
   )
public final class FromMathExpressionDoubleModelParameterConfiguration
   extends AbstractDoubleModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration, 
      TimeseriesDoubleModelParameterConfiguration {
   
   private final static String
      DEFAULT_MATH_EXPRESSION = "X",
      DEFAULT_VARIABLE_NAME = "X";
   
   @Layout(
      FieldName = "Expression",
      VerboseDescription = "The mathematical expression (E) to parse",
      Order = 0.0
      )
   private String
      mathExpression;
   @Layout(
      FieldName = "Variable Name",
      VerboseDescription = "The variable X with respect to which E is a univariate function.",
      Order = 0.1
      )
   private String
      variableName;
   
   /**
     * Create a {@link FromMathExpressionDoubleModelParameterConfiguration} object with
     * custom parameters.
     * 
     * @param mathExpression (<code>E</code>)<br>
     *        A mathematical expression to parse. This expression should be a univariate 
     *        function of a variable <code>X</code>. The name of <code>X</code> is 
     *        configurable using the next argument.
     * @param variabeName (<code>X</code>)<br>
     *        The name of the variable with respect to which <code>E</code> is a
     *        univariate function <code>E(X)</code>.
     */
   public FromMathExpressionDoubleModelParameterConfiguration(
      final String mathExpression,
      final String variabeName
      ) {
      this.mathExpression = Preconditions.checkNotNull(mathExpression);
      this.variableName = Preconditions.checkNotNull(variabeName);
   }
   
   /**
     * Create a {@link FromMathExpressionDoubleModelParameterConfiguration} object with
     * default parameters.
     */
   public FromMathExpressionDoubleModelParameterConfiguration() {
      this(DEFAULT_MATH_EXPRESSION, DEFAULT_VARIABLE_NAME);
   }
   
   public String getMathExpression() {
      return mathExpression;
   }
   
   public void setMathExpression(
      String mathExpression) {
      this.mathExpression = mathExpression;
   }
   
   public String getVariableName() {
      return variableName;
   }
   
   public void setVariableName(
      String variableName) {
      this.variableName = variableName;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         !mathExpression.isEmpty(), toParameterValidityErrMsg("expression must be nonempty."));
      Preconditions.checkArgument(
         !variableName.isEmpty(), toParameterValidityErrMsg("variable name must be nonempty."));
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(FromMathExpressionDoubleParameter.class)
         .in(Singleton.class);
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FROM_MATH_EXPRESSION_DOUBLE_MODEL_PARAMETER_EXPRESSION", mathExpression,
         "FROM_MATH_EXPRESSION_DOUBLE_MODEL_VARIABLE_NAME",        variableName,
         "FROM_MATH_EXPRESSION_DOUBLE_MODEL_PARAMETER_NAME",       getScopeString()
         ));
      expose(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
   
   private static final long serialVersionUID = 2625598671671566077L;
}
