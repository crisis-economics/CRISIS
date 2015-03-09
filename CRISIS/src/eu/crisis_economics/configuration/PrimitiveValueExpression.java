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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.Assert;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
abstract class PrimitiveValueExpression extends DefinitionExpression {
    
    protected PrimitiveValueExpression(
        FromFileConfigurationContext interpretationContext) {
        super(null, interpretationContext);
    }
    
    protected static Object resolveJSExpression(String jsExpression) 
        throws ScriptException {
        ScriptEngineManager sciptManager = new ScriptEngineManager();
        ScriptEngine jsEngine = sciptManager.getEngineByName("JavaScript");
        return jsEngine.eval(jsExpression); // Integer, Double or other.
    }
    
    static PrimitiveValueExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        try {
            return IntegerPrimitiveExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return DoublePrimitiveExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        throw new ParseException(expression, 0);
    }
    
    protected static interface ParserFunction<T extends Number> {
        T tryParse(String literal) throws NumberFormatException;
    }
    
    protected static <T extends Number> T parseExpression(
        String numberExpression,
        ParserFunction<T> directParser,
        Class<T> returnType
        ) {
        T result = null;
        try { // Parse as Javascript
            Object resultObj = 
                PrimitiveValueExpression.resolveJSExpression(numberExpression);
            result = returnType.cast(resultObj);
            return result;
        }
        catch(Exception e) { }
        try { // Parse as non-Javascript
            result = directParser.tryParse(numberExpression);
            return result;
        }
        catch(Exception e) {
            throw new IllegalStateException(
                "PrimitiveValueExpression.parseExpression: could not parse " + 
                "js expression '" + numberExpression + "' to value of expected" + 
                " type (" + returnType.toString() + ")");
        }
    }
    
    @Override
    abstract Number[] getObject();
    
    abstract Number getValue();
    
    @Override
    void setObject(Object definition, int arrayIndex) {
        throw new IllegalAccessError(
            "PrimitiveValueExpression: primitives are immutable.");
    }
    
    @Override
    final boolean isPrimitive() { return true; }
    
    @Override
    final boolean isReference() { return false; }
    
    protected void raiseNotAnArrayError(int arrayIndexRequested) {
        throw new IllegalArgumentException(
            "PrimitiveValueExpression: string objects are " + 
            "not arrays (requested index " + arrayIndexRequested + ")");
    }
    
    /** Test whether the expression is inline javascript. */
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context
        ) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        if(expression.length() < 4) return null;
        boolean
            beginsWithDoller = (expression.charAt(0) == '$');
        if(!beginsWithDoller) return null;
        if(expression.charAt(1) != '(') return null;
        if(expression.charAt(expression.length() - 1) != ')')
            return null;
        return Arrays.asList(
            expression.substring(2, expression.length()-1));
    }
    
    /** Get the type of this definition. */
    protected TypeDeclarationExpression generateTypeExpression(
        String primitiveTypename) {
        try {
            return TypeDeclarationExpression.tryCreate(primitiveTypename,
                super.getConfigurationContext());
        } catch (ParseException neverThrows) {
            neverThrows.printStackTrace();
            throw new IllegalStateException(neverThrows);
        }
    }
    
    // TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("testing PrimitiveValueExpression type..");
        Assert.assertEquals(isExpressionOfType("objReference[$(1)]", null), null);
        Assert.assertEquals(isExpressionOfType("$(1+1)", null).get(0), "1+1");
        Assert.assertEquals(isExpressionOfType("$(1.0*2.0)", null).get(0), "1.0*2.0");
        System.out.println("PrimitiveValueExpression tests pass.");
    }
}