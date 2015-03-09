/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 Milan Lovric
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
 * Copyright (C) 2015 Fabio Caccioli
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.firm;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;

/**
  * @author aymanns
  */
public abstract class LoanStrategyFirm extends StrategyFirm implements StrategyFirmInterface {
   
   public LoanStrategyFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final double numberOfShares,
      final double emissionPrice,
      final AgentNameFactory nameGenerator) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator
            );
      this.debit(initialDeposit);
   }
   
   @Override
   protected abstract void considerCommercialLoanMarkets();
   
   @Override
   protected abstract void considerProduction();
   
   @Override
   protected abstract void considerDepositPayment();
   
   @RecorderSource("Revenue")
   public abstract double getRevenue();
   
   @RecorderSource("Profit")
   public abstract double getProfit();
   
   @Override
   @RecorderSource("Employment")
   public abstract double getEmployment();
   
   @Override
   @RecorderSource("Production")
   public abstract double getProduction();
   
   public abstract double getUnsold();
   
   @RecorderSource("GoodsSellingPrice")
   public abstract double getGoodsSellingPrice();
   
   /**
     * Get the total value of {@link Loan} liabilities at this time.
     */
   public final double getTotalLoanLiabilityValue() {
       double result = 0;
       for(final Loan loan : getLiabilitiesLoans())
           result += loan.getValue();  
       return result;
   }
   
   @Override
   @RecorderSource("StockPrice")
   public final double getMarketValue() {
      if(UniqueStockExchange.Instance.hasStock(this))
         return UniqueStockExchange.Instance.getStockPrice(this);
      else return 0.;
   }
   
   @RecorderSource("LoansPerEquity")
   public final double getLoansPerEquity() {
       return getTotalLoanLiabilityValue()/getEquity();
   }
   
   @RecorderSource("LoansDividedByLoansPlusEquity")
   public final double getLoansDividedByLoansPlusEquity() {
       return getTotalLoanLiabilityValue()/(getEquity() + getTotalLoanLiabilityValue());
   }
}