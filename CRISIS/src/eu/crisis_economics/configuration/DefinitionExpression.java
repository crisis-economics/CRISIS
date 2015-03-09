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
import java.util.List;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
abstract class DefinitionExpression extends ConfigurationExpression {
    
    private NameDeclarationExpression linkedNameDeclaration;
    
    protected DefinitionExpression(
        List<ConfigurationExpression> configurationExpressions,
        FromFileConfigurationContext interpretationContext) {
        super(configurationExpressions, interpretationContext);
        this.linkedNameDeclaration = null;
    }
    
    static DefinitionExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        try {
            return CommaListExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return PrimitiveValueExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return ObjectReferenceExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return StringDefinitionExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        throw new ParseException(expression, 0);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        List<String> result = IntegerPrimitiveExpression.
           isExpressionOfType(expression, context);
        if(result != null) return result;
        result = DoublePrimitiveExpression.
            isExpressionOfType(expression, context);
        if(result != null) return result;
        result = ArrayMemberDefinitionExpression.
            isExpressionOfType(expression, context);
        if(result != null) return result;
        result = ConstructionDefinitionExpression.
            isExpressionOfType(expression, context);
        if(result != null) return result;
        result = ObjectReferenceExpression.
            isExpressionOfType(expression, context);
        if(result != null) return result;
        result = StringDefinitionExpression.
            isExpressionOfType(expression, context);
        if(result != null) return result;
        return null;
    }
    
    /** Get the underlying object array. A singleton is interpreted 
     *  as an array of size one. */
    abstract Object[] getObject();
    
    final Object getObject(int arrayIndex) {
        return this.getObject()[arrayIndex];
    }
    
    abstract void setObject(Object definition, int arrayIndex);
    
    abstract boolean isPrimitive();
    
    abstract boolean isReference();
    
    /** Does the user need to create this object? */
    final boolean isPendingUserObjectCreation() {
        for(Object obj : getObject())
            if(obj == null) return true;
        return false;
    }
    
    final boolean isAnonymousDefinition() {
        return (linkedNameDeclaration == null);
    }
    
    final void setLinkedNameDefinition(
        NameDeclarationExpression linkedNameDeclaration) {
        this.linkedNameDeclaration = linkedNameDeclaration;
    }
    
    final NameDeclarationExpression getLinkedNameDeclaration() {
        return linkedNameDeclaration;
    }
    
    /** Get the type of this definition. */
    abstract TypeDeclarationExpression getTypeExpression();
}