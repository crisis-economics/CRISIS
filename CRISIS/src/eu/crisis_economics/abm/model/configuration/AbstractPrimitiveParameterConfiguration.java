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
package eu.crisis_economics.abm.model.configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.utilities.Pair;

/**
  * An abstract base class for {@link Parameter}{@code <Double>} configuration components.
  * 
  * @author phillips
  */
public abstract class AbstractPrimitiveParameterConfiguration<T>
   extends AbstractPrivateConfiguration {
   
   static public abstract class AbstractDoubleModelParameterConfiguration
      extends AbstractPrimitiveParameterConfiguration<Double> {
      
      private static final long serialVersionUID = -4716069933943181850L;

      protected AbstractDoubleModelParameterConfiguration() {
         super(Double.class);
      }
      
      @Override
      protected final Pair<TypeLiteral<?>, TypeLiteral<?>> getLiterals() {
         return Pair.<TypeLiteral<?>, TypeLiteral<?>>create(
            new TypeLiteral<ModelParameter<Double>>(){},
            new TypeLiteral<TimeseriesParameter<Double>>(){}
            );
      }
      
      @Override
      protected final TypeLiteral<?> getProviderType() {
         return new TypeLiteral<SamplingProvider<Double>>(){};
      }
   }
   
   static public abstract class AbstractIntegerModelParameterConfiguration
      extends AbstractPrimitiveParameterConfiguration<Integer> {
      
      private static final long serialVersionUID = 6276320851784447973L;

      protected AbstractIntegerModelParameterConfiguration() {
         super(Integer.class);
      }
      
      @Override
      protected final Pair<TypeLiteral<?>, TypeLiteral<?>> getLiterals() {
         return Pair.<TypeLiteral<?>, TypeLiteral<?>>create(
            new TypeLiteral<ModelParameter<Integer>>(){},
            new TypeLiteral<TimeseriesParameter<Integer>>(){}
            );
      }
      
      @Override
      protected final TypeLiteral<?> getProviderType() {
         return new TypeLiteral<SamplingProvider<Integer>>(){};
      }
   }
   
   static public abstract class AbstractLongModelParameterConfiguration
      extends AbstractPrimitiveParameterConfiguration<Long> {
      
      private static final long serialVersionUID = -5214177252029099987L;

      protected AbstractLongModelParameterConfiguration() {
         super(Long.class);
      }
      
      @Override
      protected final Pair<TypeLiteral<?>, TypeLiteral<?>> getLiterals() {
         return Pair.<TypeLiteral<?>, TypeLiteral<?>>create(
            new TypeLiteral<ModelParameter<Long>>(){},
            new TypeLiteral<TimeseriesParameter<Long>>(){}
            );
      }
      
      @Override
      protected final TypeLiteral<?> getProviderType() {
         return new TypeLiteral<SamplingProvider<Long>>(){};
      }
   }
   
   private static final long serialVersionUID = 7143119705825800671L;
   
   private final 
      Class<T> type;
   
   /**
     * Create an {@link AbstractDoubleModelParameterConfiguration} object. Implementations
     * of this type should provide the following bindings:<br><br>
     * 
     * If {@link #getScopeString()} is nonempty:
     * 
     * <ul>
     *   <li> An implementation of {@link Parameter}{@code <Double>} annotated
     *        with the scope string;
     *   <li> A {@link Provider} of {@link Double} annotated with the scope string.
     *   <li> An implementation of {@link TimeseriesParameter}{@code <Double>}
     *        annotated with the scope string.
     * </ul>
     * 
     * Otherwise the implementation should provide:<br><br>
     * 
     * <ul>
     *   <li> An implementation of {@link Parameter}{@code <Double>} with no annotation;
     *   <li> A {@link Provider} of {@link Double} with no annotation.
     *   <li> An implementation of {@link TimeseriesParameter}{@code <Double>} with no
     *        annotation.
     * </ul>
     */
   private AbstractPrimitiveParameterConfiguration(final Class<T> type) {
      this.type = Preconditions.checkNotNull(type);
   }
   
   @Override
   protected void setRequiredBindings() {
      if(getScopeString().isEmpty()) {
         requireBinding(Key.get(new TypeLiteral<ModelParameter<T>>(){}));
         requireBinding(type);
         requireBinding(Key.get(new TypeLiteral<TimeseriesParameter<T>>(){}));
      } else {
         requireBinding(Key.get(getLiterals().getFirst(), Names.named(getScopeString())));
         requireBinding(Key.get(type, Names.named(getScopeString())));
         requireBinding(Key.get(getLiterals().getSecond(), Names.named(getScopeString())));
      }
   }
   
   /**
     * A private static inner class implementing {@link Provider}{@code <Double>}.
     * This class satisfies the required {@link Provider} binding for this
     * {@link ComponentConfiguration}.
     * 
     * @author phillips
     */
   @Singleton
   static private final class SamplingProvider<T> implements Provider<T> {
      @Inject
      @Named("PROVIDER_IMPLEMENTATION")
      private ModelParameter<T>
         source;
      
      @Override
      public T get() {
         return source.get();
      }
   }
   
   /**
     * When overriding this method, call {@code super.}{@link #addBindings} as a last
     * instruction.
     */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   protected void addBindings() {
      final TypeLiteral
         modelParameterType = getLiterals().getFirst(),
         timeseriesType = getLiterals().getSecond();
      /*
       * Bind the parameter-typed Provider:
       */
      bind(
         Key.get(modelParameterType, Names.named("PROVIDER_IMPLEMENTATION")))
        .to(getScopeString().isEmpty() ? 
            Key.get(modelParameterType) 
          : Key.get(modelParameterType, Names.named(getScopeString()))
         );
      bind(
         Key.get(modelParameterType, Names.named("PROVIDER_IMPLEMENTATION")))
        .to(getScopeString().isEmpty() ? 
            Key.get(modelParameterType) 
          : Key.get(modelParameterType, Names.named(getScopeString()))
         );
      /*
       * Bind the timeseries model parameter:
       */
      if(getScopeString().isEmpty())
         bind(Key.get(timeseriesType));
      else {
         bind(Key.get(timeseriesType, Names.named(getScopeString()))).to(Key.get(timeseriesType));
      }
      bind(
         Key.get(modelParameterType,
            Names.named("TIMESERIES_MODEL_PARAMETER_IMPLEMENTATION")))
        .to(getScopeString().isEmpty() ? 
            Key.get(modelParameterType) 
          : Key.get(modelParameterType, Names.named(getScopeString()))
         );
      expose(
         getScopeString().isEmpty() ?
         Key.get(timeseriesType) 
       : Key.get(timeseriesType, Names.named(getScopeString())));
      /*
       * Bind the Double Provider:
       */
      bind(getScopeString().isEmpty() ? 
         Key.get(type) : Key.get(type, Names.named(getScopeString())))
        .toProvider((TypeLiteral) getProviderType());
      expose(getScopeString().isEmpty() ? 
         Key.get(type) : Key.get(type, Names.named(getScopeString())));
   }
   
   protected abstract Pair<TypeLiteral<?>, TypeLiteral<?>> getLiterals();
   
   protected abstract TypeLiteral<?> getProviderType();
}
