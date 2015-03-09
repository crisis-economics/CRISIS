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
final class ObjectReferenceExpression extends DefinitionExpression {
    
    private NameDeclarationExpression nameDeclaration;
    
    private IntegerPrimitiveExpression indexExpression;
    
    /** A name reference. */
    private ObjectReferenceExpression(
        NameDeclarationExpression nameDeclExpression,
        FromFileConfigurationContext interpretationContext) {
        this(
            nameDeclExpression, 
            initMakePrimitive("$(0)", interpretationContext),
            interpretationContext
            );
    }
    
    /** A name indexed by a reference-to-primitive. */
    private ObjectReferenceExpression(
        NameDeclarationExpression nameDeclExpression,
        NameDeclarationExpression arraySizeExpression,
        FromFileConfigurationContext interpretationContext) {
        this(
           nameDeclExpression,
           interpretationContext.hasPrimitiveIntegerValueExpression(
               arraySizeExpression.getLiteralName()),
           interpretationContext
           );
    }
    
    /** A name indexed with an integer primitive. */
    private ObjectReferenceExpression(
        NameDeclarationExpression nameDeclExpression,
        IntegerPrimitiveExpression indexExpression,
        FromFileConfigurationContext interpretationContext) {
        super(initGuard(nameDeclExpression, indexExpression), interpretationContext);
        this.nameDeclaration = nameDeclExpression;
        this.indexExpression = indexExpression;
        // Check that this reference is defined
        if(!interpretationContext.hasDefinitionExpression(
            nameDeclExpression.getLiteralName())) {
            final String errMsg =
               "the object with name '" + nameDeclExpression.getLiteralName() + "'" + 
               " is referenced before its definition in this configuration file.";
            System.err.println(errMsg);
            throw new IllegalStateException(errMsg);
        }
    }
    
    private static List<ConfigurationExpression> initGuard(
        ConfigurationExpression nameDeclExpression,
        ConfigurationExpression integerPrimitiveExpression) {
        if(nameDeclExpression == null ||
           integerPrimitiveExpression == null)
           throw new NullArgumentException();
        return Arrays.asList(
           integerPrimitiveExpression, integerPrimitiveExpression);
    }
    
    private static IntegerPrimitiveExpression initMakePrimitive(
        String expression, FromFileConfigurationContext context) {
        try {
            return IntegerPrimitiveExpression.tryCreate(
                expression, context);
        }
        catch(ParseException e) {
            throw new IllegalStateException(); // Should never throw
        }
    }
    
    static ObjectReferenceExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            ObjectReferenceExpression.
               isExpressionOfType(expression, context);
        if(expressions == null) throw new ParseException(expression, 0);
        String 
            refNameString = expressions.get(0);
        NameDeclarationExpression primeNameExpression = 
            NameDeclarationExpression.tryCreate(refNameString, context);
        if(expressions.size() == 1) {
            return new ObjectReferenceExpression(
                primeNameExpression, context);
        }
        else {
            String indexString = expressions.get(1);
            if(IntegerPrimitiveExpression.
                isExpressionOfType(indexString, context) != null) {
                IntegerPrimitiveExpression primitiveIndexExpression = 
                    IntegerPrimitiveExpression.tryCreate(
                        indexString, context);
                return new ObjectReferenceExpression(
                    primeNameExpression, primitiveIndexExpression, context);
            }
            else {
                NameDeclarationExpression subPrimeNameExpression = 
                    NameDeclarationExpression.tryCreate(
                        indexString, context);
                return new ObjectReferenceExpression(
                    primeNameExpression, subPrimeNameExpression, context);
            }
        }
    }
    
    /** Get the defined object corresponding to this reference. References 
     *  are strictly pointer to object - not pointer to array.*/
    @Override
    Object[] getObject() {
        try {
            Object[] result = new Object[] { null };
            result[0] = super.getConfigurationContext().
               tryGetDefinitionExpression(
                  this.nameDeclaration.getLiteralName()).
                     getObject(this.getIndex());
            return result;
        }
        catch(Exception e) {
            System.out.println(
                "ObjectReferenceExpression.getObject: the object named '" + 
                this.nameDeclaration.getLiteralName() + "' has not been" + 
                " defined.");
        }
        return null;
    }
    
    @Override
    final void setObject(Object definition, int arrayIndex) {
        throw new IllegalAccessError(
            "ObjectReferenceExpression: references cannot be assigned to.");
    }
    
    /** Get the index of the reference. */
    final Integer getIndex() {
        return indexExpression.getValue();
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        boolean beginsWithLowercase = 
            Character.isLowerCase(expression.charAt(0));
        if(!beginsWithLowercase) return null;
        int endOfTypename = -1;
        for(int i = 0; i< expression.length(); ++i) {
            Character charNow = expression.charAt(i);
            if(Character.isLetter(charNow) || charNow == '_') continue;
            endOfTypename = i; break;
        }
        if(endOfTypename == -1)
            return Arrays.asList(expression); // no indexing
        if(expression.charAt(endOfTypename) == '[') {
            if(expression.charAt(expression.length()-1) != ']')
                return null;
            String typeDeclExpression = 
                expression.substring(0, endOfTypename);
            String indexExpression = 
                expression.substring(endOfTypename+1, expression.length()-1);
            if(IntegerPrimitiveExpression.isExpressionOfType(
               indexExpression, context) == null)
               return null;
            // with indexing
            return Arrays.asList(typeDeclExpression, indexExpression);
        }
        else return null;
    }
    
    @Override
    final boolean isPrimitive() { return false; }
    
    @Override
    boolean isReference() { return true; }
    
    /** Get the type of this definition. */
    @Override
   TypeDeclarationExpression getTypeExpression() {
        return super.getConfigurationContext().
           tryGetDefinitionExpression(
              this.nameDeclaration.getLiteralName()).
                 getTypeExpression();
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'object reference expression: 
     * reference = getObject().toString()
     * array index = getIndex().toString()
     * '
     */
    @Override
    public String toString() {
        return
            "object reference expression: \n" + 
            "reference = " + getObject()[0].toString() + "\n" +
            "array index = " + getIndex().toString() + "\n";
    }
}