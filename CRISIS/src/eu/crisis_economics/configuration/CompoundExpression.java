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
final class CompoundExpression extends ConfigurationExpression {
    
    private TypeDeclarationExpression typeDeclarationExpression;
    
    private InstanceAssignmentExpression instanceAssignmentExpression;
    
    private CompoundExpression(
        TypeDeclarationExpression typeDeclarationExpression,
        InstanceAssignmentExpression instanceAssignmentExpression,
        FromFileConfigurationContext interpretationContext) {
        super(initGuard(typeDeclarationExpression, 
            instanceAssignmentExpression), interpretationContext);
        this.typeDeclarationExpression = typeDeclarationExpression;
        this.instanceAssignmentExpression = instanceAssignmentExpression;
    }
    
    private static List<ConfigurationExpression> initGuard(
        ConfigurationExpression typeDeclarationExpression,
        ConfigurationExpression instanceAssignmentExpression
        ) {
        if(typeDeclarationExpression == null ||
           instanceAssignmentExpression == null)
           throw new NullArgumentException();
        return Arrays.asList(
           typeDeclarationExpression, 
           instanceAssignmentExpression);
    }
    
    static CompoundExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            CompoundExpression.isExpressionOfType(expression, context);
        if(expressions == null) throw new ParseException(expression, 0);
        String
            typeDeclString = expressions.get(0),
            instanceAssignmentString = expressions.get(1);
        TypeDeclarationExpression typeExpression = 
            TypeDeclarationExpression.tryCreate(typeDeclString, context);
        return new CompoundExpression(
            typeExpression,
            InstanceAssignmentExpression.tryCreate(
                instanceAssignmentString, context),
            context
            );
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        int firstSpaceIndex = expression.indexOf(' ');
        if(firstSpaceIndex == -1) return null;
        String typeDeclString = expression.substring(0, firstSpaceIndex);
        if(firstSpaceIndex == expression.length() - 1)
            return null;
        if(TypeDeclarationExpression.isExpressionOfType(
            typeDeclString, context) == null) return null;
        String instanceAssignmentString = 
            expression.substring(firstSpaceIndex + 1);
        if(InstanceAssignmentExpression.isExpressionOfType(
           instanceAssignmentString, context) == null)
            return null;
        return Arrays.asList(typeDeclString, instanceAssignmentString);
    }
    
    /** Get the type of the declared object. */
    TypeDeclarationExpression getTypeExpression() {
        return typeDeclarationExpression;
    }
    
    /** Get the assignment expression. */
    InstanceAssignmentExpression getAssignmentExpression() {
        return instanceAssignmentExpression;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'compound expression: 
     * type = getTypeExpression.toString() 
     * assignment = getAssignmentExpression.toString()
     * '
     */
    @Override
    public String toString() {
        return ("compound expression: \n" + 
           "type = " + getTypeExpression().toString() + "\n" +
           "assignment = " + getAssignmentExpression().toString() + "\n").
           replaceAll("\\n+", "\n");
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
           new TestData("Double foo = $(1)",            true),
           new TestData("Double foo = $(1/2)",          true),
           new TestData("Double[2] foo = $(1/2)",       true),
           new TestData("Type foo = {ARGUMENT=$(1)}",   false),
           new TestData("Type[2] foo = {ARGUMENT=ref[1]}",
                                                        false),
           new TestData("Type foo = Type( ARGUMENT=$(1) )", 
                                                        true),
           new TestData("Type[2] foo = Type[2]{ARGUMENT=ref[1]}",
                                                        true),
           new TestData("Type foo = Type( ARGUMENT=$(1) )",
                                                        true),
           new TestData("Typename objName",         false), 
           new TestData("Typename[2]",              false),
           new TestData("Typename",                 false),
           new TestData("objName",                  false),
           new TestData("Type name",                false),
           new TestData("Type-name",                false),
           new TestData("Type_name",                false),
           new TestData("objName[$(1 + 1)]",        false),
           new TestData("TypeName[$(1 + 1/(1+2))]", false),
           new TestData("objName[value[1]]",        false),
           new TestData("objName=1",                false), 
           new TestData("$(2+2)",                   false), 
           new TestData("{ a = 2 }",                false), 
           new TestData("// comment",               false),
           new TestData("/*comment*/",              false),
           new TestData("/*\n*comment\n*\n*/",      false),
           new TestData("(ARG_NAME = 2)",           false)
        };
        System.out.println("testing CompoundExpression type..");
        for(TestData test : tokens) {
            String inputToken = test.token;
            List<String> burstTokens = null;
            try {
                burstTokens = ConfTokenizer.tokenize(inputToken);
            }
            catch(Exception e) {
                burstTokens = Arrays.asList(inputToken);
            }
            for(String compoundExpression : burstTokens)
                Assert.assertEquals(
                    (CompoundExpression.isExpressionOfType(
                        compoundExpression, null) != null), 
                    test.isMatch
                    );
        }
        System.out.println("CompoundExpression tests pass.");
    }
}
