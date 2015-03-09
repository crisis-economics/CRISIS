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
final class ArgumentDeclarationExpression extends ConfigurationExpression {
    
    private String literalArgumentName;
    
    private ArgumentDeclarationExpression(
        String literalArgumentName,
        FromFileConfigurationContext interpretationContext) {
        super(null, interpretationContext);
        if(ArgumentDeclarationExpression.isExpressionOfType(
            literalArgumentName, interpretationContext) == null)
            throw new IllegalArgumentException(
                "ArgumentDeclarationExpression: '" + literalArgumentName + 
                "' is not a valid argument name declaration.");
        this.literalArgumentName = literalArgumentName;
    }
    
    static ArgumentDeclarationExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        List<String> expressions = 
            ArgumentDeclarationExpression.
               isExpressionOfType(expression, context);
        if(expression == null) throw new ParseException(expression, 0);
        return new ArgumentDeclarationExpression(
            expressions.get(0), context);
    }
    
    /** Is the argument specified in STATIC_FIELD format? */
    public static List<String> isExpressionOfType(
        String rawExpression,
        FromFileConfigurationContext context) {
        String expression = rawExpression.trim();
        for(int i = 0; i< expression.length(); ++i) {
            Character charNow = expression.charAt(i);
            if(charNow == '_') continue;
            if(!Character.isLetter(charNow) || Character.isLowerCase(charNow))
                return null;
        }
        return Arrays.asList(rawExpression);
    }
    
    /** Get the literal string name of this argument. */
    final String getLiteralArgumentName() {
        return literalArgumentName;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'argument declaration expression: 
     * literal name = getLiteralArgumentName.toString()
     * '
     */
    @Override
    public String toString() {
        return "argument declaration expression: \n" + 
           "literal name = " + getLiteralArgumentName().toString() + "\n";
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
           new TestData("objName",                  false),
           new TestData("ARGUMENTNAME",             true),
           new TestData("ARGUMENT_NAME",            true),
           new TestData("ARGUMENT_NAME0",           false),
           new TestData("ARGUMENT-NAME",            false),
           new TestData("ARGUMENT_NaME",            false),
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
        System.out.println("testing ArgumentDeclarationExpression type..");
        for(TestData test : tokens) {
            Assert.assertEquals(
                (ArgumentDeclarationExpression.
                    isExpressionOfType(test.token, null) != null), 
                test.isMatch
                );
        }
        System.out.println("ArgumentDeclarationExpression tests pass.");
    }
}
