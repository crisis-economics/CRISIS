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

import org.apache.commons.math3.exception.NullArgumentException;
import org.testng.Assert;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author      JKP
 * @category    Configuration
 * @see         
 * @since       1.0
 * @version     1.0
 */
class CommaListExpression extends DefinitionExpression {
    
    private Object[] definition;
    
    private TypeDeclarationExpression typeExpression;
    
    protected CommaListExpression(
        TypeDeclarationExpression typeExpression,
        List<ArgumentAssignmentExpression> argumentAssignmentExpressions,
        FromFileConfigurationContext interpretationContext) {
        super(initGuard(typeExpression, argumentAssignmentExpressions), 
            interpretationContext);
        this.typeExpression = typeExpression;
        this.definition = new Object[
            typeExpression.getNumberOfObjectsDeclared().getValue()];
    }
    
    private static List<ConfigurationExpression> initGuard(
        TypeDeclarationExpression typeExpression,
        List<ArgumentAssignmentExpression> assignmentExpressions
        ) {
        if(assignmentExpressions == null || typeExpression == null)
            throw new NullArgumentException();
        List<ConfigurationExpression> result =
            new ArrayList<ConfigurationExpression>();
        result.add(typeExpression);
        result.addAll(assignmentExpressions);
        return result;
    }
    
    static CommaListExpression tryCreate(
        String expression, FromFileConfigurationContext context)
        throws ParseException {
        try {
            return ArrayMemberDefinitionExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        try {
            return ConstructionDefinitionExpression.tryCreate(
               expression, context);
        }
        catch(Exception e) { }
        throw new ParseException(expression, 0);
    }
    
    @Override
    final Object[] getObject() { return definition; }
    
    @Override
    final void setObject(Object definition, int arrayIndex) {
        if(definition == null)
            throw new NullArgumentException();
        this.definition[arrayIndex] = definition;
    }
    
    @Override
   final boolean isPrimitive() { return false; }
    
    protected static List<String> isExpressionOfType(
        String rawExpression, 
        Character leftDelimiter,
        Character rightDelimiter,
        FromFileConfigurationContext context
        ) {
        String expression = rawExpression.trim();
        if(expression.length() < 2) return null;
        int cursor = expression.indexOf(leftDelimiter);
        if(cursor == -1) return null;
        String typename = expression.substring(0, cursor).trim();
        if(TypeDeclarationExpression.
           isExpressionOfType(typename, context) == null)
           return null;
        String definitionString = expression.substring(cursor);
        boolean
            beginsWithLeftDelimiter = 
                (definitionString.charAt(0) == leftDelimiter),
            endsWithRightDelimiter = 
                (definitionString.charAt(definitionString.length()-1) == 
                    rightDelimiter);
        if(!beginsWithLeftDelimiter || !endsWithRightDelimiter)
            return null;
        String declBody = definitionString.
            substring(1, definitionString.length()-1);
        List<String> result = new ArrayList<String>(); 
        result.add(typename);
        List<String> arguments = CommaListExpression.topLevelSplit(declBody);
        if(arguments == null) return null;
        result.addAll(arguments);
        return result;
    }
    
    private static List<String> topLevelSplit(String bodyText) {
        if(bodyText.length() == 0)
            return new ArrayList<String>();
        List<String> result = new ArrayList<String>();
        if(bodyText.charAt(bodyText.length()-1) == ',')
            return null;
        int cursor = 0;
        while(true) {
            int
                nextRoundBracket = bodyText.indexOf('(', cursor),
                nextCurlyBracket = bodyText.indexOf('{', cursor),
                nextComma = bodyText.indexOf(',', cursor),
                nextTopLevelBlock;
            nextCurlyBracket = (nextCurlyBracket < 0) ? 
                Integer.MAX_VALUE : nextCurlyBracket; 
            nextRoundBracket = (nextRoundBracket < 0) ? 
                Integer.MAX_VALUE : nextRoundBracket;
            nextComma = (nextComma < 0) ? Integer.MAX_VALUE : nextComma;
            if(nextComma < nextCurlyBracket && nextComma < nextRoundBracket) {
                String newBlock = bodyText.substring(cursor, nextComma).trim();
                if(newBlock.length() == 0)
                    return null;
                result.add(newBlock);
                nextTopLevelBlock = nextComma;
            }
            else {
            if(nextRoundBracket == Integer.MAX_VALUE && 
               nextCurlyBracket == Integer.MAX_VALUE) {
                String[] newBlocks = bodyText.substring(cursor).split(",");
                for(String newBlock : newBlocks) {
                    newBlock = newBlock.trim();
                    if(newBlock.length() == 0) return null;
                    result.add(newBlock);
                }
                return result;
            }
            else {
                int nextLayer = Math.min(nextCurlyBracket, nextRoundBracket);
                Character 
                    openDelimiter = (nextRoundBracket == nextLayer) ? '(' : '{',
                    closeDelimiter = (nextRoundBracket == nextLayer) ? ')' : '}';
                nextTopLevelBlock = 
                    ConfParser.seekHierarchicalClosingIndex(
                        bodyText, nextLayer, 
                        openDelimiter, closeDelimiter
                        );
                nextTopLevelBlock = bodyText.indexOf(',', nextTopLevelBlock);
                String newBlock;
                if(nextTopLevelBlock != -1)
                    newBlock =
                        bodyText.substring(cursor++, nextTopLevelBlock).trim();
                else
                    newBlock =
                        bodyText.substring(cursor++).trim();
                if(newBlock.length() == 0) return null;
                result.add(newBlock);
            }
            }
            if(nextTopLevelBlock == -1 || nextTopLevelBlock == bodyText.length()-1)
                break;
            cursor = nextTopLevelBlock + 1;
        }
        return result;
    }
    
    @Override
    boolean isReference() { return false; }
    
    @Override
    TypeDeclarationExpression getTypeExpression() {
        return typeExpression;
    }
    
    final List<ArgumentAssignmentExpression> getArgumentAssignmentExpressions() {
        List<ArgumentAssignmentExpression> result = 
            new ArrayList<ArgumentAssignmentExpression>();
        int numArguments = super.getSubExpressions().size();
        for(int i = 1; i< numArguments; ++i) {
            ArgumentAssignmentExpression newExpression = 
               (ArgumentAssignmentExpression)super.getSubExpressions().get(i);
            result.add(newExpression);
        }
        return result;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'delimited multiexpression: 
     * argument = firstAssignment.toString()
     * argument = secondAssignment.toString() ..
     * '
     */
    @Override
    public String toString() {
        String result = "delimited multiexpression: \n";
        result += "type = " + typeExpression.toString() + "\n";
        List<ConfigurationExpression> assignments = 
            super.getSubExpressions();
        if(assignments.size() == 0)
            result += "[none]\n";
        else {
            for(int i = 1; i< assignments.size(); ++i) {
                ArgumentAssignmentExpression expression = 
                    (ArgumentAssignmentExpression)assignments.get(i);
                result += "argument = " + expression.toString() + "\n";
            }
        }
        return result;
    }
    
    // TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("testing CommaListExpression type..");
        {
        String expression = 
            "Type[2] {  first, second , NUMBER=$( 1 + 1 ) }";
        List<String> 
            expected = Arrays.asList(
                "Type[2]", "first", "second", "NUMBER=$( 1 + 1 )"),
            gained = CommaListExpression.
                isExpressionOfType(expression, '{', '}', null);
        Assert.assertEquals(expected.size(), gained.size());
        for(int i = 0; i< expected.size(); ++i) {
            Assert.assertEquals(expected.get(i), gained.get(i));
        }
        }
        {
        String expression = 
            "Type{  A  }";
        List<String> 
            expected = Arrays.asList("Type", "A"),
            gained = CommaListExpression.
                isExpressionOfType(expression, '{', '}', null);
        Assert.assertEquals(expected.size(), gained.size());
        for(int i = 0; i< expected.size(); ++i) {
            Assert.assertEquals(expected.get(i), gained.get(i));
        }
        }
        {
        String expression = "Type{   }";
        Assert.assertEquals(
            CommaListExpression.isExpressionOfType(
                expression, '{', '}', null) == null, true);
        }
        
        {
        List<String> expected = 
            Arrays.asList(
                "ARGUMENT_ONE = OBJECT { FIRST = $(1.0/(1.0+1.0)), SECOND = objRef }",
                "ARGUMENT_TWO = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef)",
                "ARGUMENT_THREE = objRef",
                "ARGUMENT_FOUR = objRef",
                "ARGUMENT_FIVE = OBJECT { FIRST = $(1.0/(1.0+1.0)), SECOND = objRef }",
                "ARGUMENT_SIX = objRef",
                "ARGUMENT_SEVEN = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef)",
                "ARGUMENT_EIGHT = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef, " + 
                    " THIRD = OBJECT[$(2+2)] { FIRST=$(1), SECOND=OBJECT(FIRST=objRef) } )"
                );
        List<String> gained =
            CommaListExpression.topLevelSplit(
                "ARGUMENT_ONE = OBJECT { FIRST = $(1.0/(1.0+1.0)), SECOND = objRef }, " +
                "ARGUMENT_TWO = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef), " +
                "ARGUMENT_THREE = objRef, " +
                "ARGUMENT_FOUR = objRef, " +
                "ARGUMENT_FIVE = OBJECT { FIRST = $(1.0/(1.0+1.0)), SECOND = objRef }, " +
                "ARGUMENT_SIX = objRef, " +
                "ARGUMENT_SEVEN = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef), " +
                "ARGUMENT_EIGHT = OBJECT(FIRST = $(1.0/(1.0+1.0)), SECOND = objRef, " + 
                   " THIRD = OBJECT[$(2+2)] { FIRST=$(1), SECOND=OBJECT(FIRST=objRef) } )"
                );
        Assert.assertEquals(expected.size(), gained.size());
        for(int i = 0; i< expected.size(); ++i) {
            Assert.assertEquals(expected.get(i), gained.get(i));
        }
        }
        System.out.println("CommaListExpression tests pass.");
    }
}