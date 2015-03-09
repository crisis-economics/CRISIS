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

import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.RiskAssessmentUncollateralizedCILApproval;
import eu.crisis_economics.abm.bank.central.UncollateralizedInjectionLoanApprovalOperation;
import eu.crisis_economics.abm.bank.strategies.riskassessment.BankRiskAssessment;
import eu.crisis_economics.abm.bank.strategies.riskassessment.SimpleLeverageRisk;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

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
   DisplayName = "Risk Based"
   )
public final class RiskAssessmentUncollateralizedLoanPolicyConfiguration
   extends AbstractPrivateConfiguration
   implements CentralBankUncollateralizedLoanPolicyConfiguration {
   
   private static final long serialVersionUID = -1808284646331985921L;
   
   public static final double
      DEFAULT_RISK_THRESHOLD_FOR_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL = .1;
   
   @Parameter(
      ID = "RISK_ASSESSMENT_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL_TOLERANCE"
      )
   private double
      riskThresholdForUncollateralizedCashInjectionLoanApproval =
         DEFAULT_RISK_THRESHOLD_FOR_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL;
   
   public double getRiskThresholdForUncollateralizedCashInjectionLoanApproval() {
      return riskThresholdForUncollateralizedCashInjectionLoanApproval;
   }
   
   public void setRiskThresholdForUncollateralizedCashInjectionLoanApproval(
      final double riskThresholdForUncollateralizedCashInjectionLoanApproval) {
      this.riskThresholdForUncollateralizedCashInjectionLoanApproval = 
          riskThresholdForUncollateralizedCashInjectionLoanApproval;
   }
   
   public String desRiskThresholdForUncollateralizedCashInjectionLoanApproval() {
      return "The bank borrower risk assessment threshold below which an "
           + "uncollateralized central bank cash injection loan will be approved";
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "RISK_ASSESSMENT_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL_TOLERANCE",
         riskThresholdForUncollateralizedCashInjectionLoanApproval
         ));
      bind(BankRiskAssessment.class)
         .annotatedWith(Names.named(
            "RISK_ASSESSMENT_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL_RISK_MEASURE"))
         .to(SimpleLeverageRisk.class);
      bind(UncollateralizedInjectionLoanApprovalOperation.class)
         .annotatedWith(
            Names.named("CENTRAL_BANK_UNCOLLATERALIZED_CASH_INJECTION_LOAN_POLICY"))
         .to(RiskAssessmentUncollateralizedCILApproval.class);
      expose(UncollateralizedInjectionLoanApprovalOperation.class)
         .annotatedWith(
            Names.named("CENTRAL_BANK_UNCOLLATERALIZED_CASH_INJECTION_LOAN_POLICY"));
   }
}
