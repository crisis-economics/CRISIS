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

import org.testng.Assert;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class InstanceAssignmentExpression extends AssignmentExpression {
    
    private NameDeclarationExpression asigneeNameDeclaration;
    
    private InstanceAssignmentExpression(
        NameDeclarationExpression nameDeclarationExpression,
        DefinitionExpression definitionExpression,
        FromFileConfigurationContext interpretationContext) {
        super(nameDeclarationExpression, definitionExpression, interpretationContext);
        this.asigneeNameDeclaration = nameDeclarationExpression;
        definitionExpression.setLinkedNameDefinition(nameDeclarationExpression);
        interpretationContext.registerDefinition(definitionExpression);
    }
    
    static InstanceAssignmentExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            InstanceAssignmentExpression.
               isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        String
            nameDeclString = expressions.get(0),
            defnExpressionString = expressions.get(1);
        NameDeclarationExpression nameDeclExpression =
            NameDeclarationExpression.tryCreate(nameDeclString, context);
        DefinitionExpression defnExpression = 
            DefinitionExpression.tryCreate(defnExpressionString, context);
        return new InstanceAssignmentExpression(
            nameDeclExpression, defnExpression, context);
    }
    
    @Override
   NameDeclarationExpression getAssigneeExpression() {
        return asigneeNameDeclaration;
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        int firstEqualsIndex = expression.indexOf('=');
        if(firstEqualsIndex == -1) return null;
        String nameDeclString = 
            expression.substring(0, firstEqualsIndex);
        if(NameDeclarationExpression.isExpressionOfType(
            nameDeclString, context) == null) return null;
        String defnString = expression.substring(firstEqualsIndex+1);
        if(DefinitionExpression.isExpressionOfType(
           defnString, context) != null) {
            return Arrays.asList(
                nameDeclString.trim(),
                defnString.trim()
                );
        }
        return null;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'instance assignment expression: 
     * typename = getLiteralTypename().toString()
     * array size = getNumberOfObjectsDeclared().toString()
     * '
     */
    @Override
    public String toString() {
        return
            "type declaration expression: \n" + 
            "assignee = " + getAssigneeExpression().toString() + "\n" +
            super.toString() + "\n";
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
           new TestData("objName = {}",             true),
           new TestData("Typename[2] = foo",        false),
           new TestData("Typename",                 false),
           new TestData("Type name",                false),
           new TestData("Type-name",                false),
           new TestData("Type_name",                false),
           new TestData("objName[$(1 + 1)]",        false),
           new TestData("TypeName[$(1 + 1/(1+2))]", false),
           new TestData("objName[value[1]]",        false),
           new TestData("objName=$(1)",             true), 
           new TestData("$(2+2)",                   false), 
           new TestData("{ a = 2 }",                false), 
           new TestData("// comment",               false),
           new TestData("/*comment*/",              false),
           new TestData("/*\n*comment\n*\n*/",      false),
           new TestData("(ARG_NAME = 2)",           false)
        };
        System.out.println("testing InstanceAssignmentExpression type..");
        for(TestData test : tokens) {
            Assert.assertEquals(
                (InstanceAssignmentExpression.isExpressionOfType(
                    test.token, null) != null), 
                test.isMatch
                );
        }
        System.out.println("InstanceAssignmentExpression tests pass.");
    }
}
