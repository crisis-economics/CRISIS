/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Sebastian Poledna
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 Milan Lovric
 * Copyright (C) 2015 Marotta Luca
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Fabio Caccioli
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Bob De Caux
 * Copyright (C) 2015 Aurélien Vermeir
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
package eu.crisis_economics.abm.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.CheckForNegativeAssetsOperation;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptcyPolicy;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.CashInjectionLoan;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.contracts.loans.LoanOperations;
import eu.crisis_economics.abm.contracts.loans.RepoLoan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanSeller;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * A simple bank that can borrow, lend, and submit orders.
 * 
 * @author olaf
 * @author Tamas Mahr
 */
public class Bank extends Agent implements Borrower, Lender, LoanSeller, DepositHolder {

	/**
	 * The amount of liquid money the bank has. It is a contract, as it has to
	 * be on the balance sheet.
	 */
    private final FixedValueContract cashReserve =
       new FixedValueContract(Double.POSITIVE_INFINITY); // Christoph changed expiration time to inf
    private final double initialEquity;
	
	/** allocated cash in active orders, accounted with allocateCash() and disallocateCash().*/
	private double allocatedCash = 0.0;
	
	private CentralBank centralBank;
	
	private BadBank badBank;
	
	private Boolean isBankrupt = false;

   public Bank(
      final double initialCash) {
      super();
      this.initialEquity = initialCash;
      this.cashReserve.setValue(initialCash);
      this.assets.add(cashReserve);
      
      (new CheckForNegativeAssetsOperation()).operateOn(this);
      
      Simulation.repeat(this, "observation", NamedEventOrderings.AFTER_ALL);
   }
   
   /**
     * XXX: remove
     */
   @SuppressWarnings("unused")   // Scheduled
   private void observation() {
      
   }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.crisis_economics.abm.contracts.SettlementParty#credit(double)
	 */
   @Override
   public double credit(final double amount) throws InsufficientFundsException {
      if(amount <= 0.) return 0.;
      try {
         decreaseCashReserves(amount);
      }
      catch(final LiquidityException e) {
         throw new InsufficientFundsException(e);
      }
      return amount;
   }
   
   @Override
   public void debit(final double amount) {
      if(amount <= 0.) return;
      cashReserve.setValue(cashReserve.getValue() + amount);
   }
	
	/**
	 * gets the cash reserves of the bank deposited at the Central Bank
	 * it returns the value of the "loaned" to the central bank
	 * @return the value of the bank's cash reserves held with the central bank
	 */
	private double getCBDeposits() {
		double CBDeposit = 0.0;
		for(int i=0; i<assets.size(); i++){
			Contract contract = assets.get(i);
			if(contract instanceof Loan){
				if (((Loan) contract).getBorrower() instanceof CentralBank){						
					CBDeposit += ((Loan) contract).getValue();					
				}
			}
		}
		return CBDeposit;
	}
	
	/**
	 * gets the cash reserves of the bank.
	 * it returns the value of the cashReserve contract the bank keeps on the asset side of the balance sheet.
	 * @return the value of the bank's actual cash reserve
	 */
	public double getCashReserveValue() {
		return cashReserve.getValue() + getCBDeposits();
	}
	
   @Override
   public void decreaseCashReserves(final double amount) throws LiquidityException {
      if (amount <= 0.) return;
      double cash = getCashReserveValue();
      if(cash < amount) {
         cashFlowInjection(amount);
         cash = getCashReserveValue();
         if(cash < amount) {
            liquidateAtAfterAll = true;
            throw new LiquidityException(
               "Bank.decreaseCashReserve: cannot decrease cash reserve by "
                     + amount + ", as " + "at most " + cash
                     + " units of cash are available.");
         }
      }
      cashReserve.setValue(Math.max(cash - amount, 0.));
   }
   
   @Override
   public void increaseCashReserves(final double amount) {
      if(amount <= 0.) return;
      cashReserve.setValue(cashReserve.getValue() + amount);
   }

	@Override
   public List<? extends Order> getOrders() {
		return Collections.unmodifiableList( new ArrayList<Order>(orders) );
	}

	public double getInitialEquity() {
		return initialEquity;
	}
	
	/**
	 * leverage is the ratio of assets over equity.
	 * @return the leverage
	 */
	@RecorderSource("Leverage")
	public double getLeverage() {
		return (getTotalAssets() - getCashReserveValue()) / getEquity();
	}
	
	@RecorderSource("Loans")
	public double getLoans() {
		double totalLoans=0;
		for(final Contract contract : new ArrayList<Contract>(assets)){
			if(contract instanceof Loan)
				totalLoans += contract.getValue();			
		}			
			
		return totalLoans;	
	}
	
    /**
      * Get the total size of repo liabilties to repay in the next simulation cycle.
      * @return repay
      */
    public double getRepoLoanAmountToPay() {
        double repay = 0.;
        for(final Contract contract : new ArrayList<Contract>(liabilities))
           if(contract instanceof RepoLoan)
              if (Math.floor(contract.getCreationTime()) == Simulation.getFloorTime())
                  repay += ((RepoLoan) contract).getFaceValue();
        return repay;
    }
    
    /**
     * returns the sum of loans given to non financial institutions
     * @return commercial loan
     */
    @RecorderSource("CommercialLoans")
    public double getCommercialLoans() {
        double totalLoans=0;
        for(final Contract contract : new ArrayList<Contract>(assets)){
            if(contract instanceof Loan) {
                if (!(((Loan)contract).getBorrower() instanceof Bank))
                    totalLoans += contract.getValue();
            }
        }   
        
        return totalLoans;  
    
    }
	
    /**
     * returns the sum of loans given to commercial financial institutions
     * @return interbank loan
     */
    @RecorderSource("InterbankLoans")
    public double getInterbankLoans() {
        double totalLoans=0;
        for(final Contract contract : new ArrayList<Contract>(assets)){
            if(contract instanceof Loan) {
                if ((((Loan)contract).getBorrower() instanceof Bank))
                	if (!(((Loan)contract).getBorrower() instanceof CentralBank))
                		totalLoans += contract.getValue();                          
            }
        }   
        
        return totalLoans;  
    
    }

	public double getStockInPortfolio() {
		double totalStocks=0;
		for(final Contract contract : new ArrayList<Contract>(assets)){
			if(contract instanceof StockAccount)
				totalStocks += contract.getValue();			
		}			
			
		return totalStocks;	
	
	}

   @Override
   public double allocateCash(double amount) {
      if(amount <= 0.) return 0.;
      double allocatable = Math.max(getCashReserveValue() - allocatedCash, 0.);
      amount = Math.min(allocatable, amount);
      allocatedCash += amount;
      return amount;
   }
   
   @Override
   public double disallocateCash(double amount) {
   	  if(amount <= 0.) return 0.;
      amount = Math.min(allocatedCash, amount);
      allocatedCash -= amount;
      return amount;
   }
   
   @Override
   public double getAllocatedCash() {
      return allocatedCash;
   }
   
   @Override
   public double getUnallocatedCash() {
      return getCashReserveValue() - allocatedCash;
   }
   
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.markets.nonclearing.Buyer#disallocateCash(double)
	 * @see eu.crisis_economics.abm.contracts.Lender#calculateAllocatedCashFromActiveOrders()
	 */
	@Override
	public double calculateAllocatedCashFromActiveOrders(){
		double cash = 0.0;
		for (Order order: orders) {
			if (order instanceof StockOrder){
				if (((StockOrder)order).getSide()==Order.Side.BUY) {
					cash += ((StockOrder)order).getOpenSize() * ((StockOrder)order).getPrice();
				}
			} else if ((order instanceof CommercialLoanOrder)||(order instanceof InterbankLoanOrder)){
				if (((LoanOrder)order).getSide()==Order.Side.SELL) {
					cash += ((LoanOrder)order).getOpenSize();
				}
			}
		}
		this.allocatedCash = cash; // adjust for floating point precision error
		return cash;
	}
	/**
	 * I just like to do some consistency check here.
	 * @see eu.crisis_economics.abm.Agent#afterAll()
	 */
	@Override
	public void afterAll() {
		calculateAllocatedCashFromActiveOrders();
		super.afterAll();
	}

	@Override
	public void cashFlowInjection(double amt) {
	   if(amt <= 0.) return;
      double cashToRaise = Math.max(amt-this.getCashReserveValue(), 0) * (1. + 1.e-6);
      if(cashToRaise <= 0.)
         return;
		double collateralValue = 0.0;
		if (this.centralBank != null) {
			Bag collateral = new Bag();
			List<String> cR = ((CentralBankStrategy) this.centralBank.getStrategy()).getCollateralRegister(); // TODO: can be improved
			for (final Contract contract : this.getAssets()){
				if (cR.contains(contract.getUniqueId().toString())) {
					continue;
				}
				if (contract instanceof StockAccount){
					collateral.add(contract);
					collateralValue += contract.getValue();
				}
				if (contract instanceof Loan){
					collateral.add(contract);
					collateralValue += contract.getValue();
				}
				if (collateralValue > cashToRaise) {
					break;
				}
			}
			if (cashToRaise < collateralValue) {
				this.getLoanFromCentralBank(cashToRaise, collateral);
				System.err.println(this.getUniqueName() +" got cash injection from central bank ( "+cashToRaise+" )");
			} else {
			    centralBank.applyForUncollateralizedCashInjectionLoan(this, cashToRaise);
			}
		} 
	}
	
  /**
   * returns an array with the total amount of cash injections from the Central Bank in the current time step, 
   * and terminate the corresponding contracts, in order to reallocate them in the Interbank Market.
   * The first element of the array contains the total amount of cash, the second contains the value 
   * of collateralized loans
   * @return results
   */  
	public double[] extractCBInjections(boolean terminate){
		double[] results={0, 0};
		//for(final Contract contract : liabilities){
		for(int i=0; i<liabilities.size(); i++){
			Contract contract = liabilities.get(i);
			if (contract.getCreationTime() == Simulation.getSimState().schedule.getTime()){
				if ((contract instanceof RepoLoan) && LoanOperations.isCashInjectionLoan((Loan) contract)){
					if (((RepoLoan) contract).getLender() instanceof CentralBank){
						results[0] += ((RepoLoan) contract).getPrincipalPayment();
						results[1] += ((RepoLoan) contract).getPrincipalPayment();
						System.out.println("Cancelling CB collateralized injection: bank " + this.getUniqueName() + 
								", amount: " + ((RepoLoan) contract).getPrincipalPayment() + 
								", total amount so far: " + results[0] + 
								", collateralized: " + results[1]);
						//need to check if this terminate also frees the collateralized assets
						if (terminate)
						{
							RepoLoan loan = ((RepoLoan) contract);
							try {
								loan.getLender().debit(loan.getBorrower().credit(loan.getValue()));
								System.out.println("Debited lender with " + loan.getValue());
							} catch (InsufficientFundsException e) {
								//this should never happen, as this method should only be called after funds got replaced from another source
								e.printStackTrace();
							}
							loan.terminateContract();
							i--;
						}
						
					}
				}
				else if (LoanOperations.isCashInjectionLoan((Loan) contract)){
					if (((Loan) contract).getLender() instanceof CentralBank){						
						results[0]+= ((Loan) contract).getValue();
						System.out.println("Cancelling CB injection: bank " + this.getUniqueName() + 
								", amount: " + ((Loan) contract).getValue() + 
								", total amount so far: " + results[0]);
						//just terminating the loan contract doesn't transfer the funds back!
						if (terminate)
						{
							Loan loan = ((Loan) contract);
							try {
								loan.getLender().debit(loan.getBorrower().credit(loan.getValue()));
								System.out.println("Debited lender with " + loan.getValue());
							} catch (InsufficientFundsException e) {
								//this should never happen, as this method should only be called after funds got replaced from another source
								e.printStackTrace();
							}
							loan.terminateContract();
							i--;
						}						
					}
				}				
			}

		}
		return results;
	}


	public void unwindSecuredLoans() {
		double collateralValue = 0.0;
		int nAssets = this.getAssets().size();
		List<String> cR = ((CentralBankStrategy) this.centralBank.getStrategy()).getCollateralRegister();
		for (int i=nAssets-1;i>=0;i--){
			Contract contract = this.getAssets().get(i);
			if (cR.contains(contract.getUniqueId().toString())) {
               if (contract instanceof StockAccount){
                  collateralValue += contract.getValue();
                  ((StockAccount) contract).transferSharesTo(badBank);
               }
               if (contract instanceof Loan){
                  collateralValue += contract.getValue();
                  ((Loan) contract).transferToNewLender(badBank);
               }
			}
		}
		int nLiabilities = this.getLiabilities().size();
		double securitizedValue = 0.0;
		for (int i=nLiabilities-1;i>=0;i--){
			Contract contract = this.getLiabilities().get(i);
			if (contract instanceof RepoLoan){
				securitizedValue += contract.getValue();
				((RepoLoan) contract).terminateContract();
			}
		}
		this.centralBank.assets.get(0).setValue(this.centralBank.assets.get(0).getValue() + securitizedValue);
		if (collateralValue-securitizedValue > 0){
			this.debit(collateralValue-securitizedValue);
		}
	}
   
   /**
     * Get the total liability in held in {@link DepositAccount}{@code s}.
     */
   public final double getTotalLiabilitiesDeposits() {
      double result = 0.;
      for(final Contract account : getLiabilities())
         if(account instanceof DepositAccount)
            result += account.getValue();
      return result;
   }
   
   private BankBankruptcyPolicy
      bankruptcyResolutionPolicy;
   
   @Override
   public final void handleBankruptcy() {
      System.out.printf(
         "--------------------------------------------\n"
       + "Bank Bankrupty Event\n"
       + "Bank: " + getUniqueName() + "\n"
       + "--------------------------------------------\n"
       );
      
      bankruptcyResolutionPolicy.applyTo(this);
      
      if (centralBank != null) {
         CentralBankStrategy cbs = ((CentralBankStrategy) centralBank.getStrategy());
         Bag interventions = cbs.getCrisisInterventions();
         boolean isInList = false;
         for (int i = 0; i < interventions.size(); i++) {
            if (((Bank) interventions.get(i)).equals(this))
               isInList = true;
         }
         if (!isInList)
            interventions.add(this);
         cbs.setCrisisInterventions(interventions);
      }
   }
   
   /**
     * Specify how this {@link Bank} should be resolved in cases of insolvency,
     * negative equity and/or bankruptcy. If the argument is {@code null} or
     * unspecified, this {@link Bank} will be liquidated upon bankruptcy.
     */
   @Inject
   public void setResolutionMethod(
   @Named("BANK_BANKRUPTCY_RESOLUTION_POLICY")
      final BankBankruptcyPolicy policy
      ) {
      this.bankruptcyResolutionPolicy = policy;
   }
   
   /**
     * Get the {@link BankBankruptcyPolicy} for this {@link Bank}.
     */
   public BankBankruptcyPolicy getResolutionMethod() {
      return bankruptcyResolutionPolicy;
   }
   
   @Override
   public boolean isBankrupt() {
      return this.isBankrupt;
   }
   
   /**
     * Flag this {@link Bank} as bankrupt.
     */
   public void setBankrupt() {
      isBankrupt = true;
   }
   
    /**
      * Get the total size of Cash Injection Loan liabilities held by this 
      * {@link Bank}.
      */
    public double getCashInjectionLoanAmount() {
       double result = 0.; 
       for(final Contract contract : getLiabilities())
          if(contract instanceof CashInjectionLoan)
             result += contract.getValue();
       return result;
    }
	
	private void getLoanFromCentralBank(final double principal, final Bag collateral){		
		if (collateral.size()>0){
			try {
				new RepoLoan(this, this.centralBank, principal, collateral, ((CentralBankStrategy) this.centralBank.getStrategy()).getRefinancingRate(), 1, true);
				//new Loan((Borrower) this, (Lender) this.centralBank, principal,  ((CentralBankStrategy) this.centralBank.getStrategy()).getRefinancingRate(), 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void makeCentralBankOvernightDeposit(final double orderSize){
		if (orderSize>0){
			try {
			   LoanFactory.createCashInjectionLoan(
			      this.centralBank,
			      this,
			      orderSize,
			      ((CentralBankStrategy) this.centralBank.getStrategy()).getOvernightDepositRate(),
			      1
			      );
			} catch (final LenderInsufficientFundsException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * non-expiring volume of loan assets of bank
	 * @return loan volume
	 */
	public double getNonExpiringLoans() {
		double nonExpiringLoans = 0; //value left over from non-expiring loans
		
		for(final Contract contract : new ArrayList<Contract>(assets)){
			if ((contract instanceof Loan) && (contract.getExpirationTime() > Simulation.getFloorTime())) {
				nonExpiringLoans = nonExpiringLoans + contract.getValue();
			}
		}	
		return nonExpiringLoans;
	}
	
	public double getCBRefinancingRate() {
		return ((CentralBankStrategy) this.centralBank.getStrategy()).getRefinancingRate();
	}
	
	public double getCBDepositRate() {
		return ((CentralBankStrategy) this.centralBank.getStrategy()).getOvernightDepositRate();
	}
   
   /**
     * Set the {@link CentralBank} {@link Agent} to be used by this {@link Bank}.
     * It is acceptable for the argument to be {@code null}. In this event, this
     * {@link Bank} will not communicate with any {@link CentralBank}.
     */
   public void setCentralBank(CentralBank centralBank) {
      this.centralBank = centralBank;
   }
   
   /**
     * Get the {@link CentralBank} {@link Agent}, if such an {@link Agent} is
     * known to this {@link Bank}. The return value may be {@code null}.
     */
   public CentralBank getCentralBank() {
      return centralBank;
   }
   
   /**
     * Set the {@link BadBank} {@link Agent} to be used by this {@link Bank}.
     */
   public void setBadBank(BadBank badBank) {
      this.badBank = badBank;
   }
   
   /**
     * Get the {@link BadBank} {@link Agent}, if such an {@link Agent} is
     * known to this {@link Bank}. The return value may be {@code null}.
     */
   public BadBank getBadBank() {
      return badBank;
   }
   
   @Override
   public void registerNewLoanTransfer(
      double principalCashValue, Lender lender, double interestRate) { }
   
   @Override
   public void registerNewLoanRepaymentInstallment(
      double installmentCashValue, Lender lender, double interestRate) { }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
}
