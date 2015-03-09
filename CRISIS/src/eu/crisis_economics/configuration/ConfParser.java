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
import java.util.List;
import org.testng.Assert;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
final class ConfParser {
    
    private ConfParser() { } // Stateless, Uninstantiatable
    
    static List<CompoundExpression> parse(
        String expression, FromFileConfigurationContext context) {
        if(expression.length() == 0)
            return new ArrayList<CompoundExpression>();
        List<CompoundExpression> topLevelExpressions = 
             new ArrayList<CompoundExpression>();
        List<String> topLevelDeclarations = ConfTokenizer.tokenize(expression);
        for(String topLevelDeclaration : topLevelDeclarations) {
            topLevelExpressions.add(
                ConfParser.parseTopLevelExpression(
                    topLevelDeclaration, context));
        }
        return topLevelExpressions;
    }
    
    static CompoundExpression parseTopLevelExpression(
        String expression, FromFileConfigurationContext context) {
        if(expression.length() == 0)
            throw new IllegalArgumentException(
                "ConfParser: found an empty configuraton expression");
        try {
            return CompoundExpression.tryCreate(expression, context);
        }
        catch(Exception e) {
            throw new IllegalArgumentException(
                "ConfParser: could not parse this expression: '" +
                expression + "': " + e.getMessage());
        }
    }
    
    /** Find the end of a definition commencing at index. The character
     *  expression.charAt(index) is expected to be '='.
     *  Returns the index of the character one-after the end of the
     *  definition (even if this index is expression.length).
     * */
    static int jumpToEndOfDefinition(String expression, int index) {
        try {
        if(expression.charAt(index) != '=')
            throw new IllegalArgumentException(
                "ConfParser: expected '=': " + 
                expression.substring(index));
        ++index;
        if(index >= expression.length())
            throw new IllegalArgumentException(
                "ConfParser: definition is missing: " + expression);
        int cursor = index;
        Character firstDefnChar = expression.charAt(cursor);
        if(firstDefnChar == '$') {
            ++cursor;
            firstDefnChar = expression.charAt(cursor);
        }
        else if(firstDefnChar == '"') {
            return ConfParser.
                seekHierarchicalClosingIndex(expression, cursor, '"', '"');
        }
        else if(Character.isLetter(firstDefnChar) && 
           Character.isUpperCase(firstDefnChar)) {
            cursor = ConfParser.jumpToEndOfTypeDeclaration(
                expression, cursor);
            firstDefnChar = expression.charAt(cursor);
        }
        switch(firstDefnChar) {
            case('{'):
                cursor = ConfParser.seekHierarchicalClosingIndex(
                    expression, cursor, '{', '}') + 1;
                break;
            case('('):
                cursor = ConfParser.seekHierarchicalClosingIndex(
                    expression, cursor, '(', ')') + 1;
                break;
            default:
                cursor = 
                    ConfParser.jumpToEndOfWord(expression, cursor);
                if(expression.charAt(cursor) == '[')
                    cursor = ConfParser.seekHierarchicalClosingIndex(
                        expression, cursor, '[', ']') + 1;
                break;
        }
        return cursor;
        }
        catch(Exception e) {
            throw new IllegalStateException(
                "ConfParser: could not close this definition: " +
                expression.substring(index));
        }
    }
    
    /** Find the end of a type declaration (JavaTypeFormat[Index]). 
     *  Returns the index of the character one-after the end of the
     *  typename (even if this index is expression.length).
     * */
    static int jumpToEndOfTypeDeclaration(String expression, int index) {
        if(index >= expression.length())
            throw new IllegalArgumentException(
                "ConfParser.jumpToEndOfDefinition: index out of range.");
        int cursor = index;
        {
            Character firstChar = expression.charAt(index);
            if(!(Character.isUpperCase(firstChar) && Character.isLetter(firstChar)))
                throw new IllegalArgumentException(
                    "ConfParser: expected a type declaration: " + 
                    expression.substring(index));
        }
        while(cursor < expression.length()) {
            Character charNow = expression.charAt(cursor);
            if(Character.isLetter(charNow) || charNow == '_')
                { ++cursor; continue; }
            if(charNow == '[') {
                cursor = ConfParser.seekHierarchicalClosingIndex(
                    expression, cursor, '[', ']') + 1;
                return cursor;
            }
            else if(charNow == ' ' || charNow == '(' || charNow == '{') {
                return cursor;
            }
            else break;
        }
        throw new IllegalArgumentException(
            "ConfParser: could not close this declaration: " +
            expression.substring(index));
    }
    
    /** Find the end of a word (composed of letters and underscore). 
     *  Returns the index of the character one-after the end of the
     *  word (even if this index is expression.length).
     * */
    static int jumpToEndOfWord(String expression, int index) {
        if(index >= expression.length())
            throw new IllegalArgumentException(
                "ConfParser.jumpToEndOfWord: index out of range.");
        int cursor = index;
        while(cursor < expression.length()) {
            Character charNow = expression.charAt(cursor);
            if(Character.isLetter(charNow) || charNow == '_')
                ++cursor;
            else 
                return cursor;
        }
        return expression.length();
    }
    
    /** Seek the index of the character that closes a hierarchical bracket 
     *  expression delimited by Character openToken and Character closeToken.
     *  For example, if openToken = '{', closeToken = '}', expression = 
     *  "{{ }}" and startingCursor = 0, the expected result is 4.  
     */
    static Integer seekHierarchicalClosingIndex(
        String expression,
        Integer startingCursor,
        Character openToken,
        Character closeToken
        ) {
        if(startingCursor < 0 || startingCursor >= expression.length())
            throw new IllegalArgumentException(
                "ConfTokenizer.seekHierarchicalClosingIndex: starting " +
                "cursor is beyond the end of the input string.");
        if(startingCursor == expression.length() - 1)
            throw new IllegalArgumentException(
                "ConfTokenizer.seekHierarchicalClosingIndex: cannot " +
                "find a closing symbol when starting form the last " + 
                "character in the expression.");
        if(expression.charAt(startingCursor) != openToken)
            throw new IllegalStateException(
                "ConfTokenizer.seekHierarchicalClosingIndex: the character " + 
                "at the cursor is not " + openToken + ", as expected.");
        int 
            layerNow = 1,
            cursor = startingCursor + 1;
        while(cursor < expression.length()) {
            Character charNow = expression.charAt(cursor);
            if(charNow == openToken)
                ++layerNow;
            else if(charNow == closeToken)
                --layerNow;
            if(layerNow == 0)
                return cursor;
            ++cursor;
        }
        throw new IllegalStateException(
            "ConfTokenizer.seekHierarchicalClosingIndex: the character '" + 
            openToken + "' at index " + startingCursor + " is not closed " +
            "elsewhere in the expression.");
    }
    
    // TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("Testing ConfParser type..");
        class TestData {
            String
                defnExpression,
                tailExpression;
            TestData(String defnExpression, String tailExpression) {
                this.defnExpression = defnExpression;
                this.tailExpression = tailExpression;
            }
        }
        { // jumpToEndOfDefinition
            TestData[] tokens = {
                new TestData("=$(1),next", ",next"),
                new TestData("=$(1/2),next", ",next"),
                new TestData("=$(1/2 + (1+1)/(2+2)),next", ",next"),
                new TestData("={},next", ",next"),
                new TestData("={ARG1={{}},ARG2=({}),ARG3=$(1/2)},next", ",next"),
                new TestData("=(),next", ",next"),
                new TestData("=(ARG1={{}},ARG2=({}),ARG3=$(1/2)),next", ",next"),
                new TestData("=objRef,next", ",next"),
                new TestData("=objRef[0],next", ",next"),
                new TestData("=objRef[objRef],next", ",next"),
                new TestData("=objRef[$(1/2)],next", ",next")
             };
            System.out.println("testing ConfParser.jumpToEndOfDefinition..");
            for(TestData test : tokens) {
                String
                    input = test.defnExpression,
                    output = null;
                try {
                    output = input.substring(
                        ConfParser.jumpToEndOfDefinition(input, 0));
                }
                catch(Exception e) {
                    Assert.fail();
                }
                Assert.assertEquals(output, test.tailExpression);
            }
            System.out.println("ConfParser.jumpToEndOfDefinition tests pass.");
        }
        { // jumpToEndOfTypeDeclaration
            TestData[] tokens = {
                new TestData("Typename 0", " 0"),
                new TestData("Typename[1] 0", " 0"),
                new TestData("Typename[$(1)] 0", " 0"),
                new TestData("Typename[objRef] 0", " 0"),
                new TestData("Typename[objRef[1]] 0", " 0"),
                new TestData("Typename[objRef[$(1)]] 0", " 0")
            };
            System.out.println("testing ConfParser.jumpToEndOfTypeDeclaration..");
            for(TestData test : tokens) {
                String
                    input = test.defnExpression,
                    output = null;
                try {
                    output = input.substring(
                        ConfParser.jumpToEndOfTypeDeclaration(input, 0));
                }
                catch(Exception e) {
                    Assert.fail();
                }
                Assert.assertEquals(output, test.tailExpression);
            }
            System.out.println("ConfParser.jumpToEndOfTypeDeclaration tests pass.");
        }
        {
            String
                configFileToParse = "macro.config"; // Location of macro.config
            FromFileConfigurationContext context =
                new FromFileConfigurationContext(configFileToParse);
            context.printStdoutVerboseSummary();
            
            ConfigurationContext.ObjectCreationRequest 
                objCreationRequest = context.nextCreationRequest();
            while(objCreationRequest != null) {
                System.out.println("Next object creation request:");
                System.out.println(objCreationRequest.toString());
                objCreationRequest.set("callback ");
                objCreationRequest = context.nextCreationRequest();
            }
        }
        System.out.println("ConfParser tests pass.");
    }
}
