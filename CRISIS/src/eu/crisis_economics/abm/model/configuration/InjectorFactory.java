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

import java.io.Serializable;

import com.google.inject.Injector;

/**
  * Simple interface for {@link Injector} object factories.
  * 
  * @author phillips
  */
public interface InjectorFactory extends Serializable {
   /**
     * Create a fully formed and configured {@link Injector} object. This method
     * cannot return {@code null}, but may raise a runtime exception.
     */
   public Injector createInjector();
}
