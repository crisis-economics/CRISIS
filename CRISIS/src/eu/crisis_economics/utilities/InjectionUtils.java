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
package eu.crisis_economics.utilities;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

/**
  * Dependency injection utility functions.
  * @author phillips
  */
public final class InjectionUtils {
   /**
     * Create a {@link Provider} object for dependency injection
     * from a class type and an object instance. The provider will
     * return references to the instance.
     * @param type
     *        The generic type of the provider.
     * @param instance
     *        The common instance the provider refers to.
     * @return
     *        An instance of {@link Provider<T>} whose get method
     *        returns the instance provided.
     */
   static public <T> Provider<T> asProvider(final T instance) {
      return new Provider<T>() {
         @Override
         public T get() {
            return instance;
         }
      };
   }
   
   static public <T, X> Provider<T> castProvider(final Provider<X> other, final Class<T> type) {
      return new Provider<T>() {
         @SuppressWarnings("unchecked")
         @Override
         public T get() {
            return (T) other.get();
         }
      };
   }
   
   /**
     * Get an instance of an annotated object from a dependency injector.
     * 
     * @param i
     *        The dependency {@link Injector}.
     * @param token
     *        The type of the object to return.
     * @param name
     *        The annotation with which the object is labelled.
     * @return
     */
   static public <T> T getParameter(Injector i, final Class<T> token, final String name) {
      return i.getInstance(Key.get(token, Names.named(name)));
   }
}
