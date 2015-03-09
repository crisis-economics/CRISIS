/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.contracts;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.Simulation.ScheduledMethodInvokation;

/**
 * @author mahr
 * @author bochmann
 * @author phillips
 */
public class DepositAccount 
//   extends TwoPartyContract {
	 extends Contract {
   private FixedValueContract presentValue;
   private boolean isTerminated;
	
	private ScheduledMethodInvokation scheduledStepMethod;
	DepositHolder depositHolder;
	Depositor     depositor;
		
	public DepositAccount(
       final DepositHolder iDepositHolder,
	   final Depositor iDepositor,
	   final double amount,
	   final double interestRate
	   ) {
//	      super(depositHolder, depositor);
	      super(Double.POSITIVE_INFINITY);
	      depositHolder = iDepositHolder;
	      depositor		= iDepositor;
	      this.presentValue = new FixedValueContract(
	         Double.POSITIVE_INFINITY, interestRate, 0., 0.);
	      this.deposit(amount);
	      this.isTerminated = false;
	      
	      depositHolder.addLiability(this);
	      depositor.addAsset(this);
	      
	      this.scheduledStepMethod = Simulation.once(this, "step",
	         NamedEventOrderings.DEPOSIT_PAYMENT);
	   }

	    protected void decreaseValue(final double amount) {
	       incrementValue(-amount);
	       setFaceValue(getValue());
	    }
	
	public void withdraw(double amount)
			throws InsufficientFundsException {
		if (getValue() < 0 ) {
			throw new IllegalArgumentException( "Cannot withdraw negative money: " + getValue() );
		}

		if (getValue() < amount) {
		   final String errMsg =
		      "DepositAccount.withdraw: amount to withdraw is less than the account value.";
		   if(getValue() < 1.e-7) { // Numerical Residuals
		      if(amount > 1.e-7)
		         throw new InsufficientFundsException(errMsg);
		   } 
		   else if(getValue() < 1.) {
		      if(Math.abs(amount / getValue() - 1.) > 1.e-4)
		         throw new InsufficientFundsException(errMsg);
		   }
		   else if(Math.abs(amount / getValue() - 1.) > 1.e-8)
		      throw new InsufficientFundsException(errMsg);
		   amount = getValue();
      }
      try {
         getDepositHolder().decreaseCashReserves(amount);
         decreaseValue(amount);
      } catch (LiquidityException e) {
         throw new InsufficientFundsException(e);
      }
	}

	public void deposit(final double amount) {
		if ( amount < 0 ) {
			throw new IllegalArgumentException( "Cannot deposit negative money: " + amount );
		}

		if (getValue() + amount > Double.MAX_VALUE ) {
			throw new ArithmeticException( "Cannot add " + amount +
					", the result is too large to represent as a double number" );
		}
		getDepositHolder().increaseCashReserves(amount);		
		incrementValue(amount);
	    setFaceValue(getValue());
	}
	
	public void step() {
		if (!isTerminated) {
			double amount = getValue() * getInterestRate();
			
			super.incrementValue(amount);
			try {
				getDepositHolder().decreaseCashReserves(amount);
			} catch (LiquidityException e) {
				getDepositHolder().cashFlowInjection(amount);
				try {
					getDepositHolder().decreaseCashReserves(amount);
				} catch (LiquidityException e1) {
					throw new IllegalStateException(e);
				}		
			}

			Simulation.enqueue(scheduledStepMethod);
		}
	}
	
	public void terminate() {
      getDepositHolder().removeLiability(this);
      getDepositor().removeAsset(this);
      isTerminated = true;
   }

   public DepositHolder getDepositHolder() {
      return(depositHolder);
   }
   
   public final Depositor getDepositor() {
      return(depositor);
   }
   
   /** Convert the deposit account to a new deposit holder. */
   public final void setDepositHolder(DepositHolder newDepositHolder) {
      if (newDepositHolder == null)
         throw new NullArgumentException();
      getDepositHolder().removeLiability(this);
      depositHolder = newDepositHolder;
      getDepositHolder().addLiability(this);
   }

   @Override
   public double getValue() {
      return presentValue.getValue();
   }

   @Override
   public void setValue(double newValue) {
      presentValue.setValue(newValue);
	}

   @Override
   public double getFaceValue() {
      return presentValue.getFaceValue();
	}

   @Override
   public void setFaceValue(double value) {
      presentValue.setFaceValue(value);
	}

   @Override
   public double getInterestRate() {
      return presentValue.getInterestRate();
	}

   @Override
   public void setInterestRate(double interestRate) {
      presentValue.setInterestRate(interestRate);
   }
}
