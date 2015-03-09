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
package eu.crisis_economics.abm.model;

/**
  * A simple implementation of the {@link Scoped} interface.
  * 
  * @author phillips
  */
public final class SimpleScoped implements Scoped {
   
   private static final long serialVersionUID = -6377272632281780787L;
   
   private String
      scope;
   
   public SimpleScoped() { this.scope = ""; }
   
   @Override
   public final String getScopeString() {
      return scope;
   }
   
   @Override
   public final void setScope(final String scope) {
      setScope(scope, null);
   }
   
   @Override
   public final void setScope(final String scope, final Scoped parent) {
      if(parent != null) {
         this.scope = parent.getScopeString().isEmpty() ? scope : parent.getScopeString() + "_" + scope;
         return;
      }
      this.scope = scope;
   }
}
