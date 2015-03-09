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
import org.testng.Assert;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class TypeDeclarationExpression extends ConfigurationExpression {
    
    private String literalTypename;
    
    private IntegerPrimitiveExpression arraySizeExpression;
    
    /** A singleton declaration (an array of size 1). */
    private TypeDeclarationExpression(
        String literalTypename,
        FromFileConfigurationContext interpretationContext) {
        this(
           literalTypename, 
           initMakePrimitive("$(1)", interpretationContext),
           interpretationContext
           );
    }
    
    /** An array declaration using a reference to a primitive. */
    private TypeDeclarationExpression(
        String literalTypename,
        NameDeclarationExpression arraySizeExpression,
        FromFileConfigurationContext interpretationContext) {
        this(
           literalTypename,
           interpretationContext.hasPrimitiveIntegerValueExpression(
               arraySizeExpression.getLiteralName()),
           interpretationContext
           );
    }
    
    /** An array declaration. */
    private TypeDeclarationExpression(
        String literalTypename,
        IntegerPrimitiveExpression arraySizeExpression,
        FromFileConfigurationContext interpretationContext) {
        super(initGuard(literalTypename, arraySizeExpression, interpretationContext), 
            interpretationContext);
        this.literalTypename = literalTypename;
        this.arraySizeExpression = 
            (IntegerPrimitiveExpression)super.getSubExpressions().get(0);
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
    
    private static List<ConfigurationExpression> initGuard(
        String literalTypename,
        ConfigurationExpression arraySizeExpression,
        FromFileConfigurationContext context
        ) {
        if(literalTypename == null ||
                arraySizeExpression == null)
           throw new NullArgumentException();
        if(TypeDeclarationExpression.
              isExpressionOfType(literalTypename, context) == null)
           throw new IllegalArgumentException(
              "TypeDeclarationExpression: '" + literalTypename + "' is not " + 
              "a type declaration.");
        return Arrays.asList(arraySizeExpression);
    }
    
    static TypeDeclarationExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            TypeDeclarationExpression.
                isExpressionOfType(expression, context);
        if(expressions == null) throw new ParseException(expression, 0);
        String 
            literalTypename = expressions.get(0);
        if(expressions.size() == 1) {
            return new TypeDeclarationExpression(
                literalTypename, context);
        }
        else {
            String indexExpression = expressions.get(1);
            if(IntegerPrimitiveExpression.
                isExpressionOfType(
                    expressions.get(1), context) != null) {
                IntegerPrimitiveExpression primitiveIndexExpression = 
                    IntegerPrimitiveExpression.tryCreate(
                        indexExpression, context);
                return new TypeDeclarationExpression(
                    literalTypename, primitiveIndexExpression, context);
            }
            else {
                NameDeclarationExpression nameDeclExpression = 
                    NameDeclarationExpression.tryCreate(
                        indexExpression, context);
                return new TypeDeclarationExpression(
                    literalTypename, nameDeclExpression, context);
            }
        }
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        boolean beginsWithUppercase = 
            Character.isUpperCase(expression.charAt(0));
        if(!beginsWithUppercase) return null;
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
    
    /** Get the name of this type. */
    final String getLiteralTypename() {
        return literalTypename;
    }
    
    /** Get the name of this type plus an array size token. */
    final String getSizeIndexedLiteralTypename() {
        return this.getLiteralTypename() + 
            "[" + arraySizeExpression.getValue() + "]";
    }
    
    /** Get the number of objects declared. */
    final IntegerPrimitiveExpression getNumberOfObjectsDeclared() {
        return arraySizeExpression;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'type declaration expression: 
     * typename = getLiteralTypename().toString()
     * array size = getNumberOfObjectsDeclared().toString()
     * '
     */
    @Override
    public String toString() {
        return
            "type declaration expression: \n" + 
            "typename = " + getLiteralTypename().toString() + "\n" +
            "array size = " + getNumberOfObjectsDeclared().toString() + "\n";
    }
    
    // TODO: migrate to /test/
    public static void main(String[] args) {
        class TestData {
            String token;
            boolean isMatch;
            TestData(String token, boolean isMatch) {
                this.token = token;
                this.isMatch = isMatch;
            }
        }
        TestData[] tokens = { 
           new TestData("Typename objName",         false), 
           new TestData("TypeName[$(1 + 1/(1+2))]", false), // double in JS
           new TestData("Typename[2]",              true),
           new TestData("Typename[2+2]",            true),
           new TestData("Typename[$(2)]",           true),
           new TestData("Typename",                 true),
           new TestData("Type name",                false),
           new TestData("Type-name",                false),
           new TestData("Type_name",                true),
           new TestData("objName[$(1 + 1)]",        false),
           new TestData("TypeName[$(1.0 + 1.5)]",   false),
           new TestData("objName[value[1]]",        false),
           new TestData("objName=1",                false), 
           new TestData("$(2+2)",                   false), 
           new TestData("{ a = 2 }",                false), 
           new TestData("// comment",               false),
           new TestData("/*comment*/",              false),
           new TestData("/*\n*comment\n*\n*/",      false),
           new TestData("(ARG_NAME = 2)",           false)
        };
        System.out.println("testing TypeDeclarationExpression type..");
        for(TestData test : tokens) {
            Assert.assertEquals(
                (TypeDeclarationExpression.
                    isExpressionOfType(test.token, null) != null), 
                test.isMatch
                );
        }
        System.out.println("TypeDeclarationExpression tests pass.");
    }
}
