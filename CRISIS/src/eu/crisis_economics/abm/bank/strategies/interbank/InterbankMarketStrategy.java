/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Aurélien Vermeir
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
package eu.crisis_economics.abm.bank.strategies.interbank;

import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.bank.strategies.riskassessment.BankRiskAssessment;
import eu.crisis_economics.abm.bank.strategies.riskassessment.SimpleLeverageRisk;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;

/**
 * This class handles placing orders onto the interbank market. A lot of the methods here can be made static, which may allow
 * more centralized control over the interbank market. Most of the high-level order matching logic is in InterbankLoanInstrument;
 * low-level matching logic is in the BookEngine class.
 * @author olaf
 */
public class InterbankMarketStrategy {
    
    protected final int INTERBANK_LOAN_MATURITY = 1;
    protected final double EPSILON = 10.0; //tiny cash 'buffer' to ensure we don't get negative liquidity when we try to repay cash injections
    //protected final double FACTOR = 1.0; //how much cash do we try to raise on the interbank market, w.r.t. how much cash we need 
                                        //(i.e. cash injections we got)
    protected final double MIN_RATE = 0.0000001;
    protected final double MAX_RATE = 100.0;
    protected final double MAX_VOLUME = 10E100;
    
    protected StrategyBank bank;
    protected boolean centralBank = false;
    protected BankRiskAssessment riskAssessment;
    protected InterbankPricingStrategy iBStrategy;
    
    protected boolean interbankMarketEnabled = true; //if interbank is disabled, this class does nothing
    protected boolean replaceInjectionsOnly = false; //if this is true, interbank is only used to replace cash injections (regularBanks only trade with CB) - "debug" mode
    protected boolean depositWithCBAtRealDepositRate = false; //if this is false, commercial regularBanks still deposit cash overnight with CB but at near-zero interest
    
    protected boolean loggingEnabled = true;
    
    public InterbankMarketStrategy(StrategyBank bank) 
    {
        this(bank, new SimpleLeverageRisk(), new RandomInterbankPricingStrategy());
    }
    
    public InterbankMarketStrategy(
       StrategyBank bank,
       BankRiskAssessment riskAssessmentStrategy,
       InterbankPricingStrategy
       interbankPricingStrategy
       ) {
        this.bank = bank;
        if (bank instanceof CentralBank)
            centralBank = true;
        this.riskAssessment = riskAssessmentStrategy;
        this.iBStrategy = interbankPricingStrategy;
    }
    
    public void considerInterbankMarkets()
    {
        if (centralBank)
            considerInterbankMarketsCentralBank();
        else
            considerInterbankMarketsCommercialBank();
    }
    
    public void afterInterbankMarkets() {
        if (loggingEnabled)
            System.out.println("afterInterbankMarkets before bank " + bank.getUniqueName() + 
                    ", cash reserves = " + bank.getCashReserveValue() + 
                    ", liabilities = " + bank.getTotalLiabilities() + 
                    ", assets = " + bank.getTotalAssets());
        
        if (interbankMarketEnabled)
            bank.extractCBInjections(true);
        
        if (loggingEnabled)
            System.out.println("afterInterbankMarkets after bank " + bank.getUniqueName() + 
                    ", cash reserves = " + bank.getCashReserveValue() + 
                    ", liabilities = " + bank.getTotalLiabilities() + 
                    ", assets = " + bank.getTotalAssets());
    }
    
    public void setInterbankMarketEnabled(boolean enabled) {
        interbankMarketEnabled = enabled;
    }
    
    public boolean isInterbankMarketEnabled() {
        return interbankMarketEnabled;
    }
    
    public double getRiskAdjustedInterbankBorrowingRate(double nominalRate) {
        return getRiskAdjustedInterbankBorrowingRate(bank, nominalRate, riskAssessment);
    }
    
    public double getNominalInterbankBorrowingRate(double riskAdjustedRate) {
        return getNominalInterbankBorrowingRate(bank, riskAdjustedRate, riskAssessment);
    }
    
    protected void considerInterbankMarketsCentralBank() {
        for (final Order order : bank.getOrders()) {
            if (order instanceof InterbankLoanOrder){
                ((InterbankLoanOrder)order).cancel();
            }
        }
        
        if (interbankMarketEnabled)
        {
            double refinanceRate, depositRate;
            
            refinanceRate = ((CentralBankStrategy)(bank.getStrategy())).getRefinancingRate();
            
            if (replaceInjectionsOnly || !depositWithCBAtRealDepositRate)
            {
                depositRate = MIN_RATE;
            } else
            {
                depositRate = ((CentralBankStrategy)(bank.getStrategy())).getOvernightDepositRate();
            }
            
            for (final Market market : bank.getMarketsOfType(InterbankLoanMarket.class)) {
                try {
                    ((InterbankLoanMarket)market).addSellOrder(bank, INTERBANK_LOAN_MATURITY, MAX_VOLUME, refinanceRate);
                    ((InterbankLoanMarket)market).addBuyOrder(bank, INTERBANK_LOAN_MATURITY, MAX_VOLUME, depositRate);
                    
                    //((InterbankLoanMarket)market).addOrder(bank,MRO_MATURITY,DEPOSIT_VOLUME,this.interestRate - this.interestRateSpread);
                    
                    //((InterbankLoanMarket)market).addOrder(bank,LTRO_MATURITY,MRO_VOLUME,this.interestRate + this.interestRateSpread);
    
                    //((InterbankLoanMarket)market).addOrder(bank,LTRO_MATURITY,DEPOSIT_VOLUME,this.interestRate - this.interestRateSpread);
    
                    //System.out.println("Interbank order Bank" + interestRate);
                    
                } catch (final OrderException e) {
                    e.printStackTrace();
                } catch (AllocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected void considerInterbankMarketsCommercialBank() {
        if(bank.getCentralBank() != null){
            double cbSpread, cash;
            double[] injectedCash = bank.extractCBInjections(false); //count cash injections, but don't cancel them yet
            
            cash = bank.getCashReserveValue() - injectedCash[0] - EPSILON;
            
            if (loggingEnabled)
                System.out.println(" INTERBANK ------BANK " + bank.getUniqueName() + 
                        " CASH = " + cash + 
                        " = " + bank.getCashReserveValue() + 
                        " - " + injectedCash[0]);
            
            cbSpread = getCBSpread(bank);
            
            if (!interbankMarketEnabled)
            {           
                return;
            } else
            {           
                // first remove existing interbank orders. This should remove the order both from the bank and from the market
                for (final Order order : bank.getOrders()) 
                    if (order instanceof InterbankLoanOrder) ((InterbankLoanOrder)order).cancel();
        
                // LENDER SIDE : If bank is a lender, compute premium and make rate proposal
                if (cash > 0){
                    double premium = iBStrategy.getLenderPremium((ClearingBank)bank);
                    double rate = getCBRefinanceRate(bank) - cbSpread * premium;
                    
                    if (replaceInjectionsOnly)
                        rate = MAX_RATE;
                    //System.out.println("InterBank - Lender offers rate "+rate + ", CASH = " + cash);
                    
                    for (final Market market : bank.getMarketsOfType(InterbankLoanMarket.class)) {
                        try {
                            ((InterbankLoanMarket) market).addSellOrder(bank,
                                    INTERBANK_LOAN_MATURITY,
                                    cash,
                                    rate);
                        } catch (final OrderException e) {
                            e.printStackTrace();
                            Simulation.getSimState().finish();
                        } catch (AllocationException e) {
                            e.printStackTrace();
                            Simulation.getSimState().finish();
                        }
                    }
                }
                // If bank is a borrower, compute risk and make rate proposal
                //else if (cash < 0){ // BORROWER
                if (injectedCash[0] > 0) { //without interbank, regularBanks pay interest on injections. Even if we have enough cash, 
                    //we want to recreate "injections" via interbank borrowing so that we pay interest and can compare the rest of the model
                    double premium   = iBStrategy.getBorrowerPremium((ClearingBank)bank);
                    double nominalRate = getCBDepositRate(bank) + cbSpread * premium;
                    double riskAdjustedRate = getRiskAdjustedInterbankBorrowingRate(bank, nominalRate, riskAssessment);
                    
                    if (replaceInjectionsOnly)
                        riskAdjustedRate = MIN_RATE;
                    //System.out.println("InterBank - Borrower offers rate "+nominalRate + ", risk adjusted rate is " + getRiskAdjustedInterbankBorrowingRate(bank, nominalRate) + ", CASH = " + cash);
                    //System.out.println("Borrower " + bank.getUniqueName() + " cash = " + cash + ", (-cash) = " + (-cash) + ", (-cash) + delta = " + ((-cash) + delta));
                    if (riskAdjustedRate < 0) riskAdjustedRate = MIN_RATE; //negative rates not allowed             
                    for (final Market market : bank.getMarketsOfType(InterbankLoanMarket.class)) {
                        try {
                            ((InterbankLoanMarket) market).addBuyOrder(bank,
                                    INTERBANK_LOAN_MATURITY,
                                    injectedCash[0],
                                    riskAdjustedRate);
                        } catch (final OrderException e) {
                            e.printStackTrace();
                            Simulation.getSimState().finish();
                        } catch (AllocationException e) {
                            e.printStackTrace();
                            Simulation.getSimState().finish();
                        }
                    }
                }
            }
        }
    }
    
    protected static double getCBDepositRate(StrategyBank bank) {   
        return bank.getCBDepositRate();
    }
    
    protected static double getCBRefinanceRate(StrategyBank bank) { 
        return bank.getCBRefinancingRate();
    }
    
    protected static double getCBSpread(StrategyBank bank) {
        return getCBRefinanceRate(bank) - getCBDepositRate(bank);
    }
    
    public static double getRiskAdjustedInterbankBorrowingRate(StrategyBank bank, double nominalRate, BankRiskAssessment riskAssessmentStrategy) {
        double riskAdjustment = riskAssessmentStrategy.getAdjustment((ClearingBank)bank);
        return nominalRate - getCBSpread(bank) * riskAdjustment;
    }
    
    public static double getNominalInterbankBorrowingRate(StrategyBank bank, double riskAdjustedRate, BankRiskAssessment riskAssessmentStrategy) {
        double riskAdjustment = riskAssessmentStrategy.getAdjustment((ClearingBank)bank);
        return riskAdjustedRate + getCBSpread(bank) * riskAdjustment;
    }

}
