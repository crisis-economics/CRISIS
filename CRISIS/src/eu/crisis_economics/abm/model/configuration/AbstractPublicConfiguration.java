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

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.crisis_economics.abm.model.ModelUtils;
import eu.crisis_economics.abm.model.Scoped;
import eu.crisis_economics.abm.model.SimpleScoped;

/**
  * A skeletal implementation of the {@link ComponentConfiguration} interface. This
  * implementation provides an implementation of a dependency injector {@link Module}.
  * This implementation publically exposes all module class bindings to its parent
  * module.<br><br>
  * 
  * When deriving from this class, implement the following methods:<br><br>
  * 
  * (a) {@link #assertParameterValidity}: a method called once by this skeletal
  *     class during the construction of an {@link Injector}. This method should
  *     not change the state of your derived class. If all customizable parameters
  *     for your {@link ComponentConfiguration} are valid, no action is taken 
  *     and {@link assertParameterValidity} does nothing. Otherwise, 
  *     {@link IllegalArgumentException} is raised.<br><br>
  * (b) {@link #addBindings}: use this method to install class bindings, providers
  *     and any factory builders defined by your {@link Module}.<br><br>
  * (c) {@link #getChildren}: override this method when your derived class contains
  *     child {@link ComponentConfiguration} objects. This method should return
  *     a list of all such objects.
  * 
  * @author phillips
  */
public abstract class AbstractPublicConfiguration
   extends AbstractModule implements ComponentConfiguration {
   
   private static final long serialVersionUID = -3435850023677138874L;
   
   private Scoped
      scoped;
   
   /**
     * Create a {@link AbstractPublicConfiguration} object with no custom scope.
     */
   protected AbstractPublicConfiguration() {
      this.scoped = new SimpleScoped();
   }
   
   @Override
   public final void checkParameterValidity() {
      for(final ComponentConfiguration node : getChildren())
          node.checkParameterValidity();
      assertParameterValidity();
   }
   
   /**
     * When implementing this class, use this method to assert the validity of
     * any customizable component parameters. If all parameters are valid, this
     * method does nothing. Otherwise, the state of this object remains unchanged
     * and {@link IllegalArgumentException} is raised.
     */
   protected void assertParameterValidity() { }
   
   @Override
   protected final void configure() {
      for(final ComponentConfiguration node : getChildren())
         install(node);
      setRequiredBindings();
      addBindings();
   }
   
   /**
    * When implementing this class, use this method to specify mandatory 
    * {@link Module} class bindings (providers, regular bindings, factories
    * and otherwise).
    */
   protected void setRequiredBindings() { }
   
   /**
     * When implementing this class, use this method to add {@link Module} class
     * bindings (providers, regular bindings, factories and otherwise).
     */
   protected abstract void addBindings();
   
   @Override
   public final String getScopeString() {
      return scoped.getScopeString();
   }
   
   @Override
   public final void setScope(final String scope) {
      scoped.setScope(scope == null ? "" : scope);
   }
   
   @Override
   public final void setScope(String scope, Scoped parent) {
      scoped.setScope(scope, parent);
   }
   
   /**
     * Get all (direct) children of this {@link ComponentConfiguration}.
     * Adding or removing elements from the return value will not affect
     * this object.
     */
   private List<ComponentConfiguration> getChildren() {
      return ModelUtils.getSubconfigurators(this);
   }
   
   @Override
   public final Injector createInjector() {
      checkParameterValidity();
      return Guice.createInjector(this);
   }
   
   /**
     * Get a simple space-separated, capitalized name for this object.
     */
   protected final String simpleName() {
      return getClass().getSimpleName().replaceAll("([A-Z])"," $1").trim();
   }
   
   /**
     * Convert the argument to a parameter validity-assertion error message
     * {@link String}.
     */
   protected final String toParameterValidityErrMsg(final String body) {
      return (simpleName() + ": " + body).trim();
   }
   
   /**
     * This method is equivalent to:<br>
     * {@code return body == null || body.isEmpty() ? getScope() : getScope() + "_" + body}.
     */
   protected final String prefixScope(final String body) {
      return (body == null || body.isEmpty()) ? getScopeString() : getScopeString() + "_" + body;
   }
}
