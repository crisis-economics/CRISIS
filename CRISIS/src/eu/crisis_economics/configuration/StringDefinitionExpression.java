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
final class StringDefinitionExpression extends DefinitionExpression {
    
    String definition;
    
    /** A name reference. */
    private StringDefinitionExpression(
        String literal,
        FromFileConfigurationContext interpretationContext) {
        super(null, interpretationContext);
        if(literal == null)
            throw new NullArgumentException();
        definition = literal;
    }
    
    static StringDefinitionExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            StringDefinitionExpression.
                isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        return new StringDefinitionExpression(expressions.get(0), context);
    }
    
    /** Get the literal string object. */
    @Override
    String[] getObject() {
        return new String[] { definition };
    }
    
    @Override
    final void setObject(Object objDefinition, int arrayIndex) {
        if(arrayIndex != 0) raiseNotAnArrayError(arrayIndex);
        definition = (String)objDefinition;
    }
    
    private void raiseNotAnArrayError(int arrayIndexRequested) {
        throw new IllegalArgumentException(
            "StringDefinitionExpression: string objects are " + 
            "not arrays (requested index " + arrayIndexRequested + ")");
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        boolean 
            beginsWithQuote = (expression.charAt(0) == '"'),
            endsWithQuite = (expression.charAt(
                expression.length()-1) == '"');
        if(!beginsWithQuote || !endsWithQuite) return null;
        String internalStr = expression.substring(1, expression.length() - 1);
        if(internalStr.indexOf('"') != -1) return null;
        return Arrays.asList(internalStr);
    }
    
    @Override
    final boolean isPrimitive() { return false; }
    
    @Override
    boolean isReference() { return false; }
    
    @Override
    TypeDeclarationExpression getTypeExpression() {
        try {
            return TypeDeclarationExpression.tryCreate(
                "String", super.getConfigurationContext());
        } catch (ParseException neverThrows) {
            neverThrows.printStackTrace();
        }
        return null;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'literal expression: 
     * literal = getObject().toString()
     * '
     */
    @Override
    public String toString() {
        return
            "literal expression: \n" + 
            "literal = " + getObject()[0].toString() + "\n";
    }
}