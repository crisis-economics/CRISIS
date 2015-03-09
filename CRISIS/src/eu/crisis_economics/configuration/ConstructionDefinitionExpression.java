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
import java.util.ArrayList;
import java.util.List;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class ConstructionDefinitionExpression extends CommaListExpression {
    
    private ConstructionDefinitionExpression(
        TypeDeclarationExpression typeDefinitionExpression,
        List<ArgumentAssignmentExpression> argumentAssignmentExpressions,
        FromFileConfigurationContext interpretationContext) {
        super(
           typeDefinitionExpression, 
           argumentAssignmentExpressions,
           interpretationContext
           );
    }
    
    static ConstructionDefinitionExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        List<String> expressions = 
            ConstructionDefinitionExpression.
                isExpressionOfType(expression, context);
        if(expressions == null) throw new ParseException(expression, 0);
        TypeDeclarationExpression typeDeclarationExpression =
            TypeDeclarationExpression.tryCreate(expressions.get(0).trim(), context);
        List<ArgumentAssignmentExpression> argumentExpressions = 
            new ArrayList<ArgumentAssignmentExpression>();
        for(int i = 1; i< expressions.size(); ++i)
            argumentExpressions.add(ArgumentAssignmentExpression.tryCreate(
                expressions.get(i), context));
        return new ConstructionDefinitionExpression(
            typeDeclarationExpression, argumentExpressions, context);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        return CommaListExpression.isExpressionOfType(
            rawExpression.trim(), '(', ')', context);
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'construction expression: 
     * super.toString()
     * '
     */
    @Override
    public String toString() {
        String result = "construction expression: \n";
        result += super.toString();
        return result;
    }
}