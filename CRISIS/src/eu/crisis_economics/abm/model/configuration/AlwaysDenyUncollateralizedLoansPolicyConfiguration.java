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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.bank.central.AlwaysDenyUncollateralizedCILApproval;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.UncollateralizedInjectionLoanApprovalOperation;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A simple implementation of the {@link CentralBankUncollateralizedLoanPolicyConfiguration} 
  * interface. This component is used by the dashboard GUIs to configure and bind
  * an uncollateralized cash injection loan policy to the {@link CentralBank}.
  * This component corresponds to an "always deny" policy in which the
  * {@link CentralBank} will never approve an uncollateralized cash injection loan.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Always Deny"
   )
public final class AlwaysDenyUncollateralizedLoansPolicyConfiguration
   extends AbstractPublicConfiguration
   implements CentralBankUncollateralizedLoanPolicyConfiguration {
   
   private static final long serialVersionUID = -126587321669055945L;
   
   @Override
   protected void addBindings() {
      bind(UncollateralizedInjectionLoanApprovalOperation.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(AlwaysDenyUncollateralizedCILApproval.class);
   }
}
