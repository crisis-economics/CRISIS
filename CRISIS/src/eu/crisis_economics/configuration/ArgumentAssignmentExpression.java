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

import org.testng.Assert;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class ArgumentAssignmentExpression extends AssignmentExpression {
    
    private ArgumentDeclarationExpression argumentNameDeclaration;
    
    private ArgumentAssignmentExpression(
        ArgumentDeclarationExpression argNameDeclarationExpression,
        DefinitionExpression definitionExpression,
        FromFileConfigurationContext interpretationContext) {
        super(argNameDeclarationExpression, definitionExpression, interpretationContext);
        this.argumentNameDeclaration = argNameDeclarationExpression;
        interpretationContext.registerDefinition(definitionExpression);
    }
    
    static ArgumentAssignmentExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            ArgumentAssignmentExpression.
                isExpressionOfType(expression, context);
        if(expressions == null) throw new ParseException(expression, 0);
        String 
            argDeclString = expressions.get(0),
            defnExpressionString = expressions.get(1);
        ArgumentDeclarationExpression argDeclExpression = 
            ArgumentDeclarationExpression.tryCreate(argDeclString, context);
        DefinitionExpression defnExpression = 
            DefinitionExpression.tryCreate(defnExpressionString, context);
        return new ArgumentAssignmentExpression(
            argDeclExpression, 
            defnExpression, 
            context);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression, FromFileConfigurationContext context) {
        List<String> result =
            AssignmentExpression.isExpressionOfType(
                rawExpression, context);
        if(result == null) return null;
        String literalAsigneeName = result.get(0);
        if(ArgumentDeclarationExpression.isExpressionOfType(
            literalAsigneeName, context) == null)
            return null;
        return result;
    }
    
    @Override
    ArgumentDeclarationExpression getAssigneeExpression() {
        return argumentNameDeclaration;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'argument assignment expression: 
     * argument name = getAssigneeExpression.toString() 
     * definition = super.toString()
     * '
     */
    @Override
    public String toString() {
        return "argument assignment expression: \n" + 
           "argument name = " + getAssigneeExpression().toString() + "\n" +
           "definition = " + super.getDefinitionExpression() + "\n";
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
           new TestData("Typename objName = {}",    false),
           new TestData("objName = {}",             false),
           new TestData("Typename[2] = foo",        false),
           new TestData("Typename",                 false),
           new TestData("Type name",                false),
           new TestData("Type-name",                false),
           new TestData("Type_name",                false),
           new TestData("objName[$(1 + 1)]",        false),
           new TestData("TypeName[$(1 + 1/(1+2))]", false),
           new TestData("objName[value[1]]",        false),
           new TestData("objName=$(1)",             false),
           new TestData("ARGUMENT_NAME = {}",       true),
           new TestData("ARGUMENT-NAME = {}",       false),
           new TestData("ARGUMENT_NAME0 = {}",      false),
           new TestData("ARGUMENT_NaME = {}",       false),
           new TestData("ARGUMENT_NaME {}",         false),
           new TestData("ARGUMENT_NAME =",          false),
           new TestData("ARGUMENT_NAME",            false),
           new TestData("$(2+2)",                   false), 
           new TestData("{ a = 2 }",                false), 
           new TestData("// comment",               false),
           new TestData("/*comment*/",              false),
           new TestData("/*\n*comment\n*\n*/",      false),
           new TestData("(ARG_NAME = 2)",           false)
        };
        System.out.println("testing ArgumentAssignmentExpression type..");
        for(TestData test : tokens) {
            Assert.assertEquals(
                (ArgumentAssignmentExpression.
                    isExpressionOfType(test.token, null) != null), 
                test.isMatch
                );
        }
        System.out.println("ArgumentAssignmentExpression tests pass.");
    }
}
