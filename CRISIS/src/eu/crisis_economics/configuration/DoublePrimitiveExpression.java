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
final class DoublePrimitiveExpression extends PrimitiveValueExpression {
    
    String literalDoubleExpression;
    
    private static class DoubleParser 
        implements PrimitiveValueExpression.ParserFunction<Double> {
        @Override
        public Double tryParse(String literal) 
            throws NumberFormatException {
            return Double.parseDouble(literal);
        }
    }
    
    private DoublePrimitiveExpression(
        String literalDoubleExpression,
        FromFileConfigurationContext interpretationContext) {
        super(interpretationContext);
        this.literalDoubleExpression = literalDoubleExpression;
    }
    
    static DoublePrimitiveExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            DoublePrimitiveExpression.
                isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        return new DoublePrimitiveExpression(expressions.get(0), context);
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
    Double[] getObject() {
        return new Double[] { this.getValue() };
    }
    
    private static Double getValue(
        String literalDoubleExpression,
        FromFileConfigurationContext context) {
        String flattenedExpression = 
            context == null ? literalDoubleExpression :
            context.flattenPrimitiveLiteralExpression(
                literalDoubleExpression);
        return PrimitiveValueExpression.parseExpression(
            flattenedExpression, new DoubleParser(), Double.class);
    }
    
    @Override
   Double getValue() {
        return getValue(
            literalDoubleExpression,
            super.getConfigurationContext());
    }
    
    /** Get the type of this definition. */
    @Override
   TypeDeclarationExpression getTypeExpression() {
        return super.generateTypeExpression("Double");
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'floating primitive expression: 
     * value = getObject.toString()
     * '
     */
    @Override
    public String toString() {
        return "floating primitive expression: \n" + 
           "value = " + getValue().toString() + "\n";
    }
}