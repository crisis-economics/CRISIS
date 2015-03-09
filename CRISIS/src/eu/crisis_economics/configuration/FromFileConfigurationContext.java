/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.math3.exception.NullArgumentException;

final public class FromFileConfigurationContext
    extends ConfigurationContext {
    
    private boolean awaitingUserCreationRequest;
    
    private Hashtable<String, NameDeclarationExpression>
        knownNameDeclarations;  // All declarations
    private Hashtable<String, DefinitionExpression>
        knownDefinitions;       // All definitions
    
    private Hashtable<String, IntegerPrimitiveExpression>
        knownIntegerPrimitiveDefinitions;
    private Hashtable<String, DoublePrimitiveExpression>
        knownDoublePrimitiveDefinitions;
    private Hashtable<String, IntegerIndexOfMaximum>
        arrayCreationCounters;
    
    private List<CompoundExpression>
        compoundConfigurationExpressions;
    
    private LinkedList<UserObjectCreationTask>
        userObjectCreationPending;
    
    private ArrayList<DefinitionExpression>
        definitionExpressionRegistrationOrder;
    
    private boolean configFileParseIsComplete;
    
    public FromFileConfigurationContext(String configurationFilename) {
        if(configurationFilename == null)
            throw new NullArgumentException();
        String fileContents = null;
        try {
            fileContents = new Scanner(
               new File(configurationFilename)).useDelimiter("\\Z").next();
        } catch (IOException e) {
            System.out.println("FromFileConfigurationContext: file not found: '"
                + configurationFilename + "'");
        }
        this.configFileParseIsComplete = false;
        this.knownNameDeclarations = 
            new Hashtable<String, NameDeclarationExpression>();
        this.knownDefinitions = 
            new Hashtable<String, DefinitionExpression>();
        this.knownIntegerPrimitiveDefinitions = 
            new Hashtable<String, IntegerPrimitiveExpression>();
        this.knownDoublePrimitiveDefinitions = 
            new Hashtable<String, DoublePrimitiveExpression>();
        this.userObjectCreationPending = 
            new LinkedList<UserObjectCreationTask>();
        this.definitionExpressionRegistrationOrder =
            new ArrayList<DefinitionExpression>();
        this.arrayCreationCounters = 
            new Hashtable<String, IntegerIndexOfMaximum>();
        this.compoundConfigurationExpressions =
            ConfParser.parse(fileContents, this); // Populates the above hashtables
        this.awaitingUserCreationRequest = false;
        this.configFileParseIsComplete = true;
    }
    
    private static class UserObjectCreationTask {
        String uniqueName;
        DefinitionExpression definitionExpression;
    }
    
    private static class IntegerIndexOfMaximum {
        private Integer
            currentIndex,
            maximumIndex;
        IntegerIndexOfMaximum(int maximumIndex) {
            if(maximumIndex < 0)
                throw new IllegalArgumentException(
                    "IntegerIndexOfMaximum: maximum value (" +
                    maximumIndex + ") is non-positive.");
            this.currentIndex = 0;
            this.maximumIndex = maximumIndex;
        }
        void increment() { ++currentIndex; }
        boolean isComplete() {
            return currentIndex == maximumIndex;
        }
    }
    
    @Override
    public boolean isDeclared(String literalName) {
        if(literalName == null)
            throw new NullArgumentException();
        return knownNameDeclarations.containsKey(literalName);
    }
    
    @Override
    public boolean hasDefinedObject(String literalName) {
        if(literalName == null)
            throw new NullArgumentException();
        if(!knownDefinitions.containsKey(literalName))
            return false;
        return 
            (knownDefinitions.get(literalName).isPendingUserObjectCreation());
    }
    
    public boolean hasDefinitionExpression(String literalName) {
        if(literalName == null)
            throw new NullArgumentException();
        if(!knownDefinitions.containsKey(literalName))
            return false;
        return true;
    }
    
    @Override
    public Object[] tryGetDefinition(String literalName) {
        return this.tryGetDefinitionExpression(literalName).getObject();
    }
    
    public DefinitionExpression
        tryGetDefinitionExpression(String literalName) {
        if(!hasDefinitionExpression(literalName))
            throw new NoSuchElementException(
                "'" + literalName + "' has not been defined.");
        return knownDefinitions.get(literalName);
    }
    
    @Override
    public Integer hasPrimitiveIntegerValue(String literalName) {
        return this.hasPrimitiveIntegerValueExpression(literalName).getValue();
    }
    
    IntegerPrimitiveExpression 
        hasPrimitiveIntegerValueExpression(String literalName) {
        if(!knownIntegerPrimitiveDefinitions.containsKey(literalName))
            throw new NoSuchElementException(
                "'" + literalName + "' does not have a primitive" + 
                "integer definition.");
        return knownIntegerPrimitiveDefinitions.get(literalName);
    }
    
    @Override
    public Double hasPrimitiveDoubleValue(String literalName) {
        return this.hasPrimitiveDoubleValueExpression(literalName).getValue();
    }
    
    /** Flatten a primitive numerical expression by replacing 
     *  references to other numerical values with their true 
     *  numerical values. */
    String flattenPrimitiveLiteralExpression(String expression) {
        String result = expression;
        if(configFileParseIsComplete)
            for(String counterName : arrayCreationCounters.keySet()) {
                String name = "#" + counterName;
                if(!containsNameAsWholeWord(result, name)) continue;
                Number numericalValue = 
                    arrayCreationCounters.get(counterName).currentIndex;
                result = scanForNamesAndReplace(result, name, numericalValue);
            }
        else { // Array creation counters are zero.
            result = removeCounterExpressions(result);
        }
        for(String integerDefName : knownIntegerPrimitiveDefinitions.keySet()) {
            String name = integerDefName;
            if(!containsNameAsWholeWord(result, name)) continue;
            Number numericalValue = 
                knownIntegerPrimitiveDefinitions.
                    get(integerDefName).getValue();
            result = scanForNamesAndReplace(result, name, numericalValue);
        }
        for(String doubleDefName : knownDoublePrimitiveDefinitions.keySet()) {
            String name = doubleDefName;
            if(!containsNameAsWholeWord(result, name)) continue;
            Number numericalValue = 
                knownDoublePrimitiveDefinitions.
                    get(doubleDefName).getValue();
            result = scanForNamesAndReplace(result, name, numericalValue);
        }
        return result;
    }
    
    private static String scanForNamesAndReplace(
        String expression, String name, Number value) {
        int 
            cursor = 0;
        String 
            replacement = value.toString(),
            result = expression;
        int replacementCursorDelta = replacement.length() - name.length();
        while(true) {
            cursor = result.indexOf(name, cursor);
            if(cursor == -1) break;
            int nextCharAfterWordIndex = 
               cursor + name.length();
            if(nextCharAfterWordIndex == result.length()) {
               result = result.substring(0, cursor) + replacement;
               break;
            }
            Character nextCharAfterWord = 
               result.charAt(nextCharAfterWordIndex);
            if(Character.isLetter(nextCharAfterWord) ||
               nextCharAfterWord == '_')
               ++cursor;
            else {
               result = result.substring(0, cursor) + 
                  replacement + result.substring(nextCharAfterWordIndex);
               cursor += replacementCursorDelta;
            }
        }
        return result;
    }
    
    // Replace counter-type tokens ("#name") that appear in an 
    // expression with zeros. 
    private static String removeCounterExpressions(String expression) {
        String result = expression;
        for(int i = 0; i< result.length(); ++i) {
            if(result.charAt(i) == '#') {
                int j = i + 1;
                String newNameToRemove = "#";
                for(; j < result.length(); ++j) {
                    Character nextChar = result.charAt(j);
                    if(Character.isLetter(nextChar) || nextChar == '_')
                       newNameToRemove += nextChar;
                    else break;
                }
                result = result.substring(0, i) + "0" + result.substring(j);
                j += 1 - newNameToRemove.length();
                i = j;
            }
        }
        return result;
    }
    
    private static boolean containsNameAsWholeWord(
        String expression, String name) {
        int cursor = expression.indexOf(name, 0);
        if(cursor == -1) return false;
        int nextCharAfterWordIndex = cursor + name.length();
        if(nextCharAfterWordIndex == expression.length()) 
            return true;
        Character nextCharAfterWord = 
           expression.charAt(nextCharAfterWordIndex);
        if(Character.isLetter(nextCharAfterWord) ||
           nextCharAfterWord == '_')
           return false;
        else return true;
    }
    
    public static void main(String[] args) {
        String 
            expression = "Math.exp(#my_first_number/#my_second_number) + #my_first_number**#my_second_number + my_first_number/#my_second_number + my_second_number/#my_first_number";
        String expressionCopy = expression;
        expressionCopy = scanForNamesAndReplace(expressionCopy, "#my_first_number", 1.1);
        expressionCopy = scanForNamesAndReplace(expressionCopy, "#my_second_number", 2.2);
        System.out.println(expressionCopy);
        expressionCopy = expression;
        expressionCopy = removeCounterExpressions(expressionCopy);
        System.out.println(expressionCopy);
    }
    
    public DoublePrimitiveExpression 
        hasPrimitiveDoubleValueExpression(String literalName) {
        if(!knownDoublePrimitiveDefinitions.containsKey(literalName))
            throw new NoSuchElementException(
                "'" + literalName + "' does not have a primitive" + 
                "double definition.");
        return knownDoublePrimitiveDefinitions.get(literalName);
    }
    
    // Register a general definition expression
    void registerDefinition(DefinitionExpression newDefinition) {
        String definitionUniqueName = 
            tryGetDefinitionName(newDefinition);
        if(newDefinition.isPrimitive()) {
            if(newDefinition.getObject()[0].getClass() == Integer.class) {
                knownIntegerPrimitiveDefinitions.put(
                    definitionUniqueName, 
                    (IntegerPrimitiveExpression)newDefinition
                    );
            }
            else if(newDefinition.getObject()[0].getClass() == Double.class) {
                knownDoublePrimitiveDefinitions.put(
                    definitionUniqueName, 
                    (DoublePrimitiveExpression)newDefinition
                    );
            }
        }
        knownDefinitions.put(definitionUniqueName, newDefinition);
        definitionExpressionRegistrationOrder.add(newDefinition);
        Integer arraySize = 
            newDefinition.getTypeExpression().
                getNumberOfObjectsDeclared().getValue();
        if(newDefinition.isPendingUserObjectCreation()
           && !newDefinition.isReference()) {
            UserObjectCreationTask newCreationTask = 
                new UserObjectCreationTask();
            newCreationTask.uniqueName = definitionUniqueName;
            newCreationTask.definitionExpression = newDefinition;
            for(int i = 0; i< arraySize; ++i)
                userObjectCreationPending.add(newCreationTask);
        }
        IntegerIndexOfMaximum newCounter = 
            new IntegerIndexOfMaximum(arraySize);
        newCounter.currentIndex = 0;
        arrayCreationCounters.put(definitionUniqueName, newCounter);
    }
    
    // Register a primitive integer definition expression
    void registerDefinition(IntegerPrimitiveExpression newDefinition) {
        knownIntegerPrimitiveDefinitions.put(
            tryGetDefinitionName(newDefinition), newDefinition);
        this.registerDefinition(newDefinition);
    }
    
    // Register a name declaration
    void registerNameDeclaration(NameDeclarationExpression newDeclaration) {
        knownNameDeclarations.put(newDeclaration.getLiteralName(), newDeclaration);
    }
    
    private String tryGetDefinitionName(DefinitionExpression definition) {
        return definition.isAnonymousDefinition() ?
            "0" + java.util.UUID.randomUUID().toString() : 
                definition.getLinkedNameDeclaration().getLiteralName();
    }
    
    final class ObjectCreationRequest 
        extends ConfigurationContext.ObjectCreationRequest {
        
        private DefinitionExpression
            owningDefinitionExpression;
        
        ObjectCreationRequest(
            String typeName,
            String declaredName,
            List<ConfigurationContext.Argument> arguments,
            DefinitionExpression owningDefinitionExpression
            ) {
            super(typeName, declaredName, arguments);
            if(owningDefinitionExpression == null)
                throw new NullArgumentException();
            this.owningDefinitionExpression = owningDefinitionExpression;
        }
        
        @Override
        public void set(Object creationResult) {
            IntegerIndexOfMaximum arrayCounter =
                arrayCreationCounters.get(this.getDeclaredName());
            if(arrayCounter.isComplete())
                throw new IllegalStateException(
                    "FromFileConfigurationContext: this object array has already been " + 
                    "populated. Details follow\n: " + this.toString());
            owningDefinitionExpression.setObject(
                creationResult, arrayCounter.currentIndex);
            arrayCounter.increment();
            FromFileConfigurationContext.this.
                awaitingUserCreationRequest = false;
        }
    }
    
    /** Get the next user object creation request. */
    @Override
    public ObjectCreationRequest nextCreationRequest() {
        if(awaitingUserCreationRequest)
            throw new IllegalStateException(
                "The configuration process has encountered a referenced object " +
                "with no user definition. The method ObjectCreationRequest.set() " +
                "should be called upon completion of each request.");
        if(userObjectCreationPending.size() == 0)
            return null;
        UserObjectCreationTask nextPendingUserTask = 
            userObjectCreationPending.pollFirst();
        ArrayList<ConfigurationContext.Argument> arguments = 
            new ArrayList<ConfigurationContext.Argument>();
        CommaListExpression commaListExpression =
            (CommaListExpression)nextPendingUserTask.definitionExpression;
        List<ArgumentAssignmentExpression> argAssignmentExpressions = 
            commaListExpression.getArgumentAssignmentExpressions();
        for(ArgumentAssignmentExpression argExpression : argAssignmentExpressions) {
            String literalArgumentName = 
               argExpression.getAssigneeExpression().getLiteralArgumentName();
            Object createdObject = 
               argExpression.getDefinitionExpression().getObject()[0];
            if(createdObject == null)
                throw new IllegalStateException(
                    "The configuration process has encountered a referenced object " +
                    "with no user definition. The method ObjectCreationRequest.set() " +
                    "should be called upon completion of each request.");
            ConfigurationContext.Argument newArgument = 
               ConfigurationContext.createArgument(
                  literalArgumentName, createdObject);
            arguments.add(newArgument);
        }
        FromFileConfigurationContext.ObjectCreationRequest result = 
           new FromFileConfigurationContext.ObjectCreationRequest(
              nextPendingUserTask.definitionExpression.
                 getTypeExpression().getLiteralTypename(),
              nextPendingUserTask.uniqueName,
              arguments,
              nextPendingUserTask.definitionExpression
              );
        this.awaitingUserCreationRequest = true;
        return result;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'from-file configuration context with 8 compound expressions and '
     * '10 definitions'.
     */
    @Override
    public String toString() {
        return "from-file configuration context with " + 
            compoundConfigurationExpressions.size() + " compound expressions " + 
            "and " + knownDefinitions.size() + " definitions.";
    }
    
    void printStdoutVerboseSummary() {
        System.out.println(toString());
        System.out.println("interpretation:");
        for(CompoundExpression expression : compoundConfigurationExpressions)
            System.out.println(expression.toString());
        System.out.println("known names:");
        for(String name : knownNameDeclarations.keySet())
            System.out.println(name);
        System.out.println("\nall known definitions:");
        System.out.println(String.format("%-70s %-50s %-50s", 
            "Unique name", "Type", "Value"));
        for(Map.Entry<String, DefinitionExpression> record : 
            knownDefinitions.entrySet()) {
            String name = record.getKey();
            Object objValue = record.getValue().getObject()[0];
            System.out.println(String.format("%-70s %-50s %-50s",
               (name.charAt(0) != '0' ? name : "Anonymous def. " + name.toString()),
               record.getValue().getTypeExpression().getSizeIndexedLiteralTypename(), 
               (objValue == null ? "[pending user definition]" : 
                   objValue.toString())));
        }
        System.out.println("\nuser creation requests pending:");
        {
        int index = 1;
        for(UserObjectCreationTask pending : userObjectCreationPending) {
            String name = pending.uniqueName;
            Object objValue = pending.definitionExpression.getObject()[0];
            System.out.println(String.format("%5d %-64s %-50s %-50s",
               index,
               (name.charAt(0) != '0' ? name : "Anonymous def. " + name.toString()),
               pending.definitionExpression.
                  getTypeExpression().getSizeIndexedLiteralTypename(), 
               (objValue == null ? "[pending user definition]" : 
                  objValue.toString())));
            ++index;
        }
        }
    }
}
