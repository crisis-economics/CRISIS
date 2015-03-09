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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;

// Expression types                             Notes
// --------------------------------------------------
// # denotes a string literal.                  
//                                              
// Assignment Expressions                       
// ----------------------                       
// A1. InstanceAssignmentExpression             $NameDeclarationExpression = $DefinitionExpression
// A2. ArgumentAssignmentExpression             $ArgumentDeclarationExpression = 
//                                                  $DefinitionExpression
//                                              
// Declaration                                  
// -----------                                  
// B1. TypeDeclarationExpression                String ([$IntegerPrimitiveExpression])
//                                              A literal typename beginning with a capital letter. 
// B2. NameDeclarationExpression                String 
//                                              A type instance name in camelCase format.
// B3. ArgumentDeclarationExpression            String
//                                              An argument name in STATIC_FIELD format.
//                                              
// Definition                                   
// ----------                                   
// Primitive Value Expressions                  One Javascript Integer or Double numerical 
//                                              value that can be parsed. The expression may
//                                              not depend on references to the values of
//                                              other objects, but may be a self-contained 
//                                              mathematical equation. Always begins with '$'.
// C1. IntegerPrimitiveExpression               $(javascriptFormula)
// C2. DoublePrimitiveExpression                $(javascriptFormula)
//
// Non-Primitive Expressions
// C3. ArrayMemberDefinitionExpression          { $ArgumentAssignment, $ArgumentAssignment, ... }
//                                              Always begins with '{' and ends with '}'.
// C4. ConstructionDefinitionExpression         ( $ArgumentAssignment, $ArgumentAssignment, ... )
//                                              Always begins with '(' and ends with ')'.
// C5. ObjectReferenceExpression                $NameDeclarationExpression ([$IntegerPrimitiveExpression])
//
// Compound Expression
// -------------------
// D1. CompoundExpression                       $TypeDeclarationExpression / $TypeArrayDeclarationExpression,
//                                                  $NameDeclarationExpression, $Definition.

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
abstract class ConfigurationExpression 
    implements Iterable<ConfigurationExpression> {
    
    private List<ConfigurationExpression> configurationExpressions;
    
    private boolean isFinalExpression;
    
    private FromFileConfigurationContext configContext;
    
    protected ConfigurationExpression(
        List<ConfigurationExpression> configurationExpressions,
        FromFileConfigurationContext interpretationContext
        ) {
        if(interpretationContext == null)
            throw new NullArgumentException();
        this.configurationExpressions = new ArrayList<ConfigurationExpression>();
        if(configurationExpressions != null) {
            this.configurationExpressions.addAll(configurationExpressions);
            this.isFinalExpression = (configurationExpressions.size() == 0);
        }
        else this.isFinalExpression = true;
        this.configContext = interpretationContext;
    }
    
    /* Static methods:
     * When extending this type, you should implement a 
     * static method with signature:
     * 
     * public static List<String> isExpressionOfType(
     *  String, FromFileConfigurationContext).
     * 
     * This method should accept a String argument and return
     * a list of its substrings defining top-level parsable 
     * ConfExpressions in the argument. If no such well-defined
     * list can be formed from the argument, the method should
     * return null.
     */
    
    /** Get an (unmodifiable) list of sub-expressions. */
    final List<ConfigurationExpression> getSubExpressions() {
        return Collections.unmodifiableList(configurationExpressions);
    }
    
    /** Get: (is the expression dependent on other expressions?) */
    final boolean isFinalExpression() {
        return isFinalExpression;
    }
    
    @Override
   public Iterator<ConfigurationExpression> iterator() {
        return configurationExpressions.iterator();
    }
    
    FromFileConfigurationContext getConfigurationContext() {
        return configContext;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'configuration expression with 5 sub-expressions.' 
     */
    @Override
    public String toString() {
        return "configuration expression with " + 
            configurationExpressions.size() + 
            " sub-expressions.";
    }
}
