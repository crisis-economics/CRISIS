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
final class NameDeclarationExpression extends ConfigurationExpression {
    
    private String literalName;
    
    private NameDeclarationExpression(
        String literalName,
        FromFileConfigurationContext interpretationContext) {
        super(null, interpretationContext);
        if(literalName == null)
            throw new NullArgumentException();
        this.literalName = literalName;
        interpretationContext.registerNameDeclaration(this);
    }
    
    static NameDeclarationExpression tryCreate(
        String expression, FromFileConfigurationContext context) 
        throws ParseException {
        List<String> expressions = 
            NameDeclarationExpression.
                isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        return new NameDeclarationExpression(expressions.get(0), context);
    }
    
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        if(expression.length() == 0) return null;
        boolean beginsWithLowercase = 
            Character.isLowerCase(expression.charAt(0));
        if(!beginsWithLowercase) return null;
        boolean containsSyntaxTokens = 
            expression.matches(".*[\\s-/=,\\$\\[\\]\\{\\(\\)\\}].*");
        if(containsSyntaxTokens) return null;
        return Arrays.asList(expression);
    }
    
    /** Get the unique name of this object. */
    final String getLiteralName() {
        return literalName;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'name declaration expression: 
     * name = getObject.toString()
     * '
     */
    @Override
    public String toString() {
        return "name declaration expression: \n" + 
           "literal name = " + getLiteralName().toString() + "\n";
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
           new TestData("Typename[2]",              false),
           new TestData("Typename",                 false),
           new TestData("objName",                  true),
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
        System.out.println("testing TypeDeclarationExpression type..");
        for(TestData test : tokens) {
            Assert.assertEquals(
                (NameDeclarationExpression.
                    isExpressionOfType(test.token, null) != null), 
                test.isMatch
                );
        }
        System.out.println("TypeDeclarationExpression tests pass.");
    }
}
