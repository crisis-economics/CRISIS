/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.household;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.inventory.CashLedger;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.inventory.goods.SimpleGoodsRepository;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * Simple <i>household</i> {@link Agent} implementation that has a deposit account at a specified deposit holder.
 * 
 * <p>
 * A {@link DepositHolder} is required to create a new household, which is typically a
 * {@link eu.crisis_economics.abm.bank.Bank}. Its initial deposits can be set (which is <code>0.0</code> by default).
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 * @see Agent
 * @see Depositor
 */
public class DepositingHousehold extends Agent implements Household {
	
	/** Initial interest rate for the deposit account associated to the household by default. */
	public static final double INITIAL_INTEREST_RATE = 0.0;
	
	/** Usually a Bank (but can be any {@link DepositHolder} which can be reassigned later in the simulation. */
	protected DepositHolder depositHolder;
	
	/** Account that stores the household deposits. */
	protected DepositAccount depositAccount;
	
	protected CashLedger cashLedger;
	
	protected GoodsRepository m_goodsRepository;
	/**
	 * Creates a household with zero deposits.
	 * 
	 * @param depositHolder any class that implements {@link DepositHolder} (usually a bank); cannot be <code>null</code>
	 */
	public DepositingHousehold(final DepositHolder depositHolder) {
		this( depositHolder, 0.0 );
	}
	
	/**
	 * Creates a new household with the specified parameters.
	 * 
	 * @param depositHolder any class that implements {@link DepositHolder} (usually a bank); cannot be <code>null</code>
	 * @param initialDeposit determines the initial deposits of the household
	 */
	public DepositingHousehold(final DepositHolder depositHolder, final double initialDeposit) {
		super();
		if ( null == depositHolder ) {
			throw new IllegalArgumentException( "depositHolder == null" );
		}
		
		this.depositHolder = depositHolder;
		
		// Automatically added to the assets (!)
		depositAccount = new DepositAccount( depositHolder, this, 0, INITIAL_INTEREST_RATE );
		
		cashLedger = new CashLedger(depositAccount);
		
		this.debit(initialDeposit);
        
		m_goodsRepository = new SimpleGoodsRepository();
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Returns the total deposit value of the household.
	 * 
	 * @return value of the deposit account associated with the household
	 */
	public double getDepositValue() {
	    return cashLedger.getStoredQuantity();
	}
	
    public double getCash() {
        return this.getDepositValue();
    }
	
	// DOCME
    public void changeDepositAccountTo(final DepositAccount newDepositAccount) {
        synchronized(depositAccount) {
        if ( depositAccount != null ) {
            depositAccount.terminate();
        }
        depositAccount = newDepositAccount;
            CashLedger newCashLedger = new CashLedger(newDepositAccount);
            try {
                newCashLedger.setAllocated(cashLedger.getAllocated());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(this);
                Simulation.getSimState().finish();
            }
            cashLedger = newCashLedger;
        }
    }
    
    @Override
    public double credit(double cashAmount) 
       throws InsufficientFundsException {
       if(cashAmount <= 0.) return 0.;
       if(cashLedger.getUnallocated() < cashAmount)
          throw new InsufficientFundsException();
       return cashLedger.pull(cashAmount);
    }
    
    @Override
    public void debit(double cashAmount) {
        cashLedger.push(cashAmount);
    }
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Household [depositHolder=" + depositHolder + ", depositAccount=" + depositAccount + ", super.toString()=" +
				super.toString() + "]";
	}

	@Override
	public void cashFlowInjection(double amt) { 
	   // No Action.
	}
	
    @Override
    public <T> T accept(final AgentOperation<T> operation) {
       return operation.operateOn(this);
    }
}
