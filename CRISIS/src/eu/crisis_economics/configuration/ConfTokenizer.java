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
final class ConfTokenizer {
    
    private ConfTokenizer() { } // Stateless, Uninstantiatable
    
    /** Tokenize (burst) a configuration string. Meaningless 
     *  characters are removed from the string. */
    static List<String> tokenize(String configurationString) {
        if(configurationString.length() == 0)
            throw new IllegalArgumentException(
                "ConfTokenizer: string is empty.");
        configurationString = 
            configurationString.replaceAll("\\r\\n", "\n");
        String canonicalForm = 
            ConfTokenizer.removeSuperfluousTokens(configurationString);
        List<String> result = new ArrayList<String>();
        {
        String[] tokens = canonicalForm.split(";");
        for(int i = 0; i< tokens.length; ++i) {
            List<String> subTokens = 
                ConfTokenizer.expandRepetitiveDeclarations(tokens[i]);
            result.addAll(subTokens);
        }
        }
        return result;
    }
    
    /** Convert (for example): 
     *  "Integer first = 1, second = 2;" to 
     *  "Integer first = 1; Integer second = 2;"*/
    static private List<String> expandRepetitiveDeclarations(
        String expression) {
        int typeDeclEndIndex =
            ConfParser.jumpToEndOfTypeDeclaration(expression, 0);
        String typenameDecl = expression.substring(0, typeDeclEndIndex);
        if(TypeDeclarationExpression.
           isExpressionOfType(typenameDecl, null) == null) 
           throw new IllegalStateException(
              "ConfTokenizer: invalid type expression: " + typenameDecl);
        List<String> result = new ArrayList<String>();
        int cursor = typeDeclEndIndex;
        while(cursor < expression.length()) {
           if(expression.charAt(cursor) == ' ') ++cursor;
           int equalsIndex = expression.indexOf('=', cursor);
           if(equalsIndex == -1)
              throw new IllegalStateException(
                 "ConfTokenizer: expression has no assignment: " + 
                 expression.substring(cursor));
           String nameDeclString =
              expression.substring(cursor, equalsIndex).trim();
           cursor = equalsIndex;
           int defnEndIndex = 
              ConfParser.jumpToEndOfDefinition(expression, cursor);
           String defnSubString = 
              expression.substring(cursor, defnEndIndex);
           result.add(typenameDecl + " " + nameDeclString + defnSubString);
           cursor = defnEndIndex;
           if(cursor == expression.length()-1)
               if(expression.charAt(cursor) != ';')
                   throw new IllegalStateException(
                       "ConfTokenizer: expression must end with a ';':" +
                       expression);
           if(cursor == expression.length()) return result;
           else {
              Character defnDelimiter = expression.charAt(cursor);
              if(defnDelimiter != ',')
                  throw new IllegalStateException(
                      "ConfTokenizer: repetitive definitions must be " + 
                      "delimited by commas: " + expression.substring(cursor));
              ++cursor; continue;
           }
        }
        return null;
    }
    
    /** Remove superfluous (meaningless) tokens from a string. */
    static String removeSuperfluousTokens(String expression) {
        String result = expression;
        result = ConfTokenizer.removeComments(result);
        result = result.trim();
        result = result.replace('\t', ' ');
        result = result.replace('\n', ' ');
        result = ConfTokenizer.removeDuplicates(result, "; ");
        result = ConfTokenizer.trimLeftNearSymbols(result, "=$}]),;");
        result = ConfTokenizer.trimRightNearSymbols(result, "=${[(,;");
        return result;
    }
    
    /** Remove all back-to-back duplicates of characters in the second 
     *  argument from the expression. */
    private static String removeDuplicates(String expression, String symbols) {
        String result = expression;
        for(int i = 0; i< symbols.length(); ++i) {
            Character symbol = symbols.charAt(i);
            result = result.replaceAll(
                symbol.toString() + "+", symbol.toString());
        }
        return result;
    }
    
    /** Trim one whitespace adjacent to characters in the second argument. */
    private static String trimLeftNearSymbols(String expression, String symbols) {
        String result = expression;
        for(int i = 0; i< symbols.length(); ++i) {
            Character symbol = symbols.charAt(i);
            result = result.replace(" " + symbol.toString(), symbol.toString());
        }
        return result;
    }
    private static String trimRightNearSymbols(String expression, String symbols) {
        String result = expression;
        for(int i = 0; i< symbols.length(); ++i) {
            Character symbol = symbols.charAt(i);
            result = result.replace(symbol.toString() + " ", symbol.toString());
        }
        return result;
    }
    
    /** Remove comments from an expression. */
    private static String removeComments(String expression) {
        String result = expression; 
        result = ConfTokenizer.splice(result, "//", "\n");
        result = ConfTokenizer.splice(result, "/*", "*/");
        return result;
    }
    
    /** Splice a string-delimited substring from an expression. 
     *  For example, splice("A$ *B", "$", "*") returns "AB". 
     */
    private static String splice(
        String expression,
        String subStringBeginsWith,
        String subStringEndsWith
        ) {
        int cursor = 0;
        class InclusiveBounds { 
            int left, right; 
            }
        List<InclusiveBounds> sectionsToPurge = 
            new ArrayList<InclusiveBounds>();
        cursor = expression.indexOf(subStringBeginsWith, 0);
        while(cursor != -1) {
            int endCursor = expression.indexOf(subStringEndsWith, cursor);
            if(endCursor == -1)
                endCursor = expression.length()-1;
            InclusiveBounds bounds = new InclusiveBounds();
            bounds.left = cursor;
            bounds.right = endCursor + subStringEndsWith.length();
            sectionsToPurge.add(bounds);
            cursor = expression.indexOf(subStringBeginsWith, endCursor);
        }
        if(sectionsToPurge.size() == 0)
            return expression;
        String result = expression;
        for(int i = sectionsToPurge.size() - 1; i>= 0; --i) {
            InclusiveBounds bounds = sectionsToPurge.get(i);
            result = result.substring(0, bounds.left) + 
                result.substring(bounds.right);
        }
        return result;
    }
    
    // TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("testing ConfTokenizer type..");
        String expression =
            "// Global parameters                                             \n\n" + 
            "Integer /*Integer global parameters*/                            \n" + 
            " numFirms                    = $( 50 ),     // Integer definition\n\n" + 
            " numHouseholds               = $(1000);;                         \n" + 
            "Double /*Flp. global parameters*/                                \n" + 
            " goodsInitialEndowment        = $(20.),   // Double definition   \n" + 
            " goodsInitialProductionTarget = $(20.),                          \n" + 
            " goodsInitialSellingPrice     = $(1.65e7),                       \n" + 
            " expansionGraceFactor         = $(.05);;                         \n\n\n\n" +
            "MacroFirm[2] macroFirms = { ARGUMENT=$(1), ARGUMENT2={} };\n\n\n\n";
        {
            String expected = 
                "Integer numFirms=$(50)\n" + 
                "Integer numHouseholds=$(1000)\n" + 
                "Double goodsInitialEndowment=$(20.)\n" + 
                "Double goodsInitialProductionTarget=$(20.)\n" + 
                "Double goodsInitialSellingPrice=$(1.65e7)\n" + 
                "Double expansionGraceFactor=$(.05)\n" + 
                "MacroFirm[2] macroFirms={ARGUMENT=$(1),ARGUMENT2={}}\n";
            List<String> expectedArray = 
                Arrays.asList(expected.split("\n"));
            List<String> gainedArray = 
                ConfTokenizer.tokenize(expression);
            Assert.assertEquals(expectedArray.size(), gainedArray.size());
            for(int i = 0; i< expectedArray.size(); ++i) {
                Assert.assertEquals(
                    expectedArray.get(i),
                    gainedArray.get(i)
                    );
            }
        }
        System.out.println("ConfTokenizer tests pass.");
        return;
    }
}
