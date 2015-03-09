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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.NullArgumentException;

public abstract class ConfigurationContext {
    
    /** Has a name been declared? */
    abstract boolean isDeclared(String literalName);
    
    /** Has a name been defined? */
    abstract boolean hasDefinedObject(String literalName);
    
    /** Get an object definition. */
    abstract Object tryGetDefinition(String literalName)
       throws IllegalArgumentException;
    
    /** Does a name have a primitive integer value? */
    abstract Integer hasPrimitiveIntegerValue(String literalName);
    
    /** Does a name have a primitive double value? */
    abstract Double hasPrimitiveDoubleValue(String literalName);
    
    protected final static Argument createArgument(
        String argumentLiteralName,
        Object objectDefinition
        ) {
        return new ConfigurationContext.Argument(
            argumentLiteralName,
            objectDefinition
            );
    }
    
    protected final static Argument createArgument(
        String argumentLiteralName,
        String literalDefinition
        ) {
        return new ConfigurationContext.Argument(
            argumentLiteralName,
            literalDefinition
            );
    }
    
    /** An argument in a construction expression. */
    public final static class Argument { // Immutable
        private String argumentName;
        
        private Object definition;
        
        private boolean isTextDefinition;
        
        private Argument(
            String argumentLiteralName,
            Object objectDefinition
            ) {
            this.isTextDefinition = false;
            this.definition = objectDefinition;
            initCommon(argumentLiteralName);
        }
        
        private Argument(
            String argumentLiteralName,
            String literalDefinition
            ) {
            this.isTextDefinition = true;
            this.definition = literalDefinition;
            initCommon(argumentLiteralName);
        }
       
        private void initCommon(String argumentLiteralName) {
            if(argumentLiteralName == null)
                throw new NullArgumentException();
            this.argumentName = argumentLiteralName;
        }
        
        /** Name of the argument. */
        public String getArgumentName() { return argumentName; }
        
        /** Existing definition of the argument. */
        public Object getDefinition() { return definition; }
        
        /** Is the definition an object or an instruction? */
        public boolean isLiteralDefinition() { return isTextDefinition; }
        
        @Override
        public int hashCode() { return argumentName.hashCode(); }
        
        /**
         * Returns a brief description of this object. The exact details of the
         * string are subject to change, however the following is typical:
         * 
         * 'argument: ' + getArgumentName() + ' = ' + definition().toString()
         */
        @Override
        public String toString() {
            return 
               "argument: " + getArgumentName() + " = " + 
                    getDefinition().toString() + 
                        (isTextDefinition ? " (literal)" : "");
        }
    }
    
    /** A request to create a new object. */
    public abstract class ObjectCreationRequest
        implements Iterable<Argument> {
        
        private String
            typeName,
            declaredName; // random UUID if anonymous
        
        Map<String, Argument> arguments;
        
        ObjectCreationRequest(
            String typeName,
            String declaredName,
            List<Argument> arguments
            ) {
            this.typeName = typeName;
            if(declaredName == null)
                this.declaredName = java.util.UUID.randomUUID().toString();
            else
                this.declaredName = declaredName;
            HashMap<String, Argument> __arguments = 
                new HashMap<String, Argument>();
            for(Argument argument : arguments)
                __arguments.put(argument.getArgumentName(), argument);
            this.arguments = Collections.unmodifiableMap(__arguments);
        }
        
        /** Get the typename of this object. */
        public final String getTypename() { return typeName; }
        
        /** Get the declared name of this object. */
        public final String getDeclaredName() { return declaredName; }
        
        /** Check for an argument by name. Returns null if not found. */
        public final Argument hasArgument(String argumentName) {
            return arguments.get(argumentName);
        }
        
        public final int getNumArguments() {
            return arguments.size();
        }
        
        @Override
        public final Iterator<Argument> iterator() {
            return arguments.values().iterator();
        }
        
        /** Assign a created object to this argument. */
        public abstract void set(Object creationResult);
        
        /** Get a mandatory argument. Raises an exception on failure. */
        public final ConfigurationContext.Argument getMandatoryArgument(
            String argumentName) {
            ConfigurationContext.Argument result = 
               this.hasArgument(argumentName);
            if(result == null)
                raiseMissingArgumentError(argumentName);
            return result;
        }
        
        /** Get a mandatory primitive value. Raises an exception on failure. */
        public final Number getMandatoryPrimitiveValue(String argumentName) {
            ConfigurationContext.Argument argument =
                this.getMandatoryArgument(argumentName);
            if(argument.isLiteralDefinition())
                raiseExpectedNonLiteralError(argumentName);
            try {
                Number result = (Number)argument.getDefinition();
                return result;
            }
            catch(ClassCastException e) {
                raiseExpectedPrimitiveValueError(argumentName);
            }
            return null;
        }
        
        /** Get a mandatory double value definition. Raises an exception 
         *  on failure. */
        public final Double getMandatoryDoubleValue(String argumentName) {
            Number primitiveResult = 
                getMandatoryPrimitiveValue(argumentName);
            try {
                Double result = (Double)primitiveResult;
                return result;
            }
            catch(ClassCastException e) {
                raiseExpectedDoubleValueError(argumentName);
            }
            return null;
        }
        
        /** Get a mandatory integer value definition. Raises an exception 
         *  on failure. */
        public final Integer getMandatoryIntegerValue(String argumentName) {
            Number primitiveResult = 
                getMandatoryIntegerValue(argumentName);
            try {
                Integer result = (Integer)primitiveResult;
                return result;
            }
            catch(ClassCastException e) {
                raiseExpectedIntegerValueError(argumentName);
            }
            return null;
        }
        
        /** Ensure an argument does not exist. Raises an exception if the
         *  argument does exist. */
        public final void ensureDoesNotHaveArgument(String argumentName) {
            if(this.hasArgument(argumentName) != null)
                raiseUnexpectedArgumentError(argumentName);
        }
        
        /** Get a mandatory literal (String) value definition. Raises an 
         *  exception on failure. */
        public final String getMandatoryLiteralValue(String argumentName) {
            ConfigurationContext.Argument argument = 
                getMandatoryArgument(argumentName);
            if(!argument.isLiteralDefinition())
                raiseExpectedLiteralError(argumentName);
            try {
                String result = (String)argument.getDefinition();
                return result;
            }
            catch(ClassCastException e) {
                raiseExpectedLiteralError(argumentName);
            }
            return null;
        }
        
        /** Get a mandatory object value with the given type. Raises an 
         *  exception if the argument is not defined, is literal, or cannot 
         *  be cast to the specified value. */
        public final <T> T getMandatoryObjectValue(String argumentName, Class<T> type) {
            ConfigurationContext.Argument argument = 
                getMandatoryArgument(argumentName);
            if(argument.isLiteralDefinition())
                raiseExpectedNonLiteralError(argumentName);
            try {
                T result = type.cast(argument.getDefinition());
                return result;
            }
            catch(ClassCastException e) {
                raiseExpectedNonLiteralError(argumentName);
            }
            return null;
        }
        
        /** Raise an illegal-argument value error. */
        public final void raiseIllegalArgumentValueError(String argName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argName + "' has an illegal " + 
                    "value. Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        // Raise a general interpretation error.
        private void raiseGeneralError() {
            throw new IllegalStateException(
                "Interpreter encountered an illegal state when " +
                "processing a configuration object of type '" + 
                getTypename() + "'. Details of this object follow:\n" +
                toString());
        }
        
        // Raise a missing argument interpretation error.
        private void raiseMissingArgumentError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "Interpreter could not find the argument '" +
                    argumentName + "'. " +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        // Raise an unexpected argument interpretation error.
        private void raiseUnexpectedArgumentError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "Interpreter found, but does not expect, the argument '" +
                    argumentName + "'. " +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        private void raiseExpectedLiteralError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argumentName + "'. Is expected to " + 
                    "have a string literal value, rather than a custom object." +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        private void raiseExpectedNonLiteralError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argumentName + "'. Is expected to " + 
                    "have a custom object value, rather than a string literal." +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        // Raise a primitive value definition error.
        private void raiseExpectedPrimitiveValueError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argumentName + "'. Is expected to " + 
                    "have a primitive value definition." +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        // Raise a Double value definition error.
        private void raiseExpectedDoubleValueError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argumentName + "'. Is expected to " + 
                    "have a double value definition." +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        // Raise an Integer value definition error.
        private void raiseExpectedIntegerValueError(String argumentName) {
            try { raiseGeneralError(); }
            catch(Exception e) {
                throw new IllegalStateException(
                    "The argument '" + argumentName + "'. Is expected to " + 
                    "have a integer value definition." +
                    "Details follow:\n " + e.getMessage()
                    );
            }
        }
        
        /**
         * Returns a brief description of this object. The exact details of the
         * string are subject to change, however the following is typical:
         * 
         * 'object creation request, '
         * 'typename = ' + getTypename()
         * 'declared name = ' + getDeclaredName() + 
         * 'with ' + getNumArguments() + ' arguments' +
         * arguments.toString() ..
         */
        @Override
        public String toString() {
            String result = 
                "object creation request, typename = " + getTypename() +
                ", declared name = " + getDeclaredName() + ", " +
                "with " + getNumArguments() + " arguments:\n";
            for(Argument argument : arguments.values())
                result += argument.toString() + "\n";
            return result;
        }
    }
    
    /** Get the next object to create. */
    abstract ObjectCreationRequest nextCreationRequest();
}
