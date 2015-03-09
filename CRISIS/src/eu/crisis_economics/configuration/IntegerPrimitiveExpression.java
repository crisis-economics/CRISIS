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

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class IntegerPrimitiveExpression extends PrimitiveValueExpression {
    
    private String literalIntegerExpression;
    
    private static class IntParser 
        implements PrimitiveValueExpression.ParserFunction<Integer> {
        @Override
        public Integer tryParse(String literal) 
            throws NumberFormatException {
            return Integer.parseInt(literal);
        }
    }
    
    private IntegerPrimitiveExpression(
        String literalIntegerExpression,
        FromFileConfigurationContext interpretationContext) {
        super(interpretationContext);
        this.literalIntegerExpression = literalIntegerExpression;
    }
    
    static IntegerPrimitiveExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            IntegerPrimitiveExpression.isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        return new IntegerPrimitiveExpression(expressions.get(0), context);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression, 
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        List<String> result = 
           PrimitiveValueExpression.isExpressionOfType(expression, context);
        if(result != null) expression = result.get(0);
        try {
            getValue(expression, context);
            return Arrays.asList(expression);
        }
        catch(Exception e) { return null; }
    }
    
    @Override
    Integer[] getObject() {
        return new Integer[] { this.getValue() };
    }
    
    private static Integer getValue(
        String literalIntegerExpression,
        FromFileConfigurationContext context) {
        String flattenedExpression = 
            context == null ? literalIntegerExpression :
            context.flattenPrimitiveLiteralExpression(
                literalIntegerExpression);
        return PrimitiveValueExpression.parseExpression(
            flattenedExpression, new IntParser(), Integer.class);
    }
    
    @Override
   Integer getValue() {
        return getValue(
            literalIntegerExpression,
            super.getConfigurationContext());
    }
    
    /** Get the type of this definition. */
    @Override
   TypeDeclarationExpression getTypeExpression() {
        return super.generateTypeExpression("Integer");
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'integer primitive expression: 
     * value = getObject.toString()
     * '
     */
    @Override
    public String toString() {
        return "integer primitive expression: \n" + 
           "value = " + getValue().toString() + "\n";
    }
}