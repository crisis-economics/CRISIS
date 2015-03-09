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
package eu.crisis_economics.configuration;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
abstract class AssignmentExpression extends ConfigurationExpression {
    
    private DefinitionExpression rhsDefinitionExpression; 
    
    protected AssignmentExpression(
        ConfigurationExpression asigneeExpression,
        DefinitionExpression definitionExpression,
        FromFileConfigurationContext interpretationContext) {
        super(initGuard(asigneeExpression, definitionExpression), 
            interpretationContext);
        this.rhsDefinitionExpression = definitionExpression;
    }
    
    private static List<ConfigurationExpression> initGuard(
        ConfigurationExpression asigneeExpression,
        ConfigurationExpression definitionExpression) {
        if(asigneeExpression == null || definitionExpression == null)
            throw new NullArgumentException();
        return Arrays.asList(asigneeExpression, definitionExpression);
    }
    
    static AssignmentExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        try {
            return ArgumentAssignmentExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return InstanceAssignmentExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        throw new ParseException(expression, 0);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression, FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(!expression.contains("=")) return null;
        int firstEqualsIndex = expression.indexOf('=');
        if(firstEqualsIndex == rawExpression.length() - 1) return null;
        String declExpressionString = 
            rawExpression.substring(0, firstEqualsIndex);
        if(declExpressionString.length() == 0) return null;
        List<String> result = Arrays.asList(
            declExpressionString, rawExpression.substring(firstEqualsIndex + 1));
        return result;
    }
    
    /** Get the RHS definition expression. */
    final DefinitionExpression getDefinitionExpression() {
        return rhsDefinitionExpression;
    }
    
    /** Get the LHS assignee expression. */
    abstract ConfigurationExpression getAssigneeExpression();
}
