/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.fund;

import java.util.List;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.utilities.StateVerifier;

final class MLender implements Lender {
    
    public <T extends SettlementParty & HasAssets & Party>
       MLender(final T parent, final Contract sourceOfCashToLend) {
       this(parent, parent, parent, sourceOfCashToLend);
    }
    
    public MLender(
       final SettlementParty settlement,
       final HasAssets assets,
       final Party party,
       final Contract sourceOfCashToLend
       ) {
       StateVerifier.checkNotNull(settlement, assets, party, sourceOfCashToLend);
       iSettlementParty = settlement;
       iHasAssets = assets;
       iParty = party;
       cashReserve = sourceOfCashToLend;
    }
    
    Portfolio  iPortfolio;
    Contract    cashReserve;
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Lender interface
    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public double getUnallocatedCash() {
        return(cashReserve.getValue());
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // SettlementParty forwarding
    ////////////////////////////////////////////////////////////////////////////////////////
        
    SettlementParty iSettlementParty;

    @Override
    public double credit(double amt) throws InsufficientFundsException {
        return(iSettlementParty.credit(amt));
    }

    @Override
    public void debit(double amt) {
        iSettlementParty.debit(amt);
    }

    @Override
    public void cashFlowInjection(double amt) {
        iSettlementParty.cashFlowInjection(amt);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HasAssets interface
    ////////////////////////////////////////////////////////////////////////////////////////////////

    HasAssets iHasAssets;
    
    @Override
    public void addAsset(Contract asset) {
        iHasAssets.addAsset(asset);
    }

    @Override
    public boolean removeAsset(Contract asset) {
        return(iHasAssets.removeAsset(asset));
    }

    @Override
    public List<Contract> getAssets() {
        return(iHasAssets.getAssets());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Party interface
    /////////////////////////////////////////////////////////////////////////////////////////////////

    Party iParty;
    
    @Override
    public void addOrder(Order order) {
        iParty.addOrder(order);
    }

    @Override
    public boolean removeOrder(Order order) {
        return(iParty.removeOrder(order));
    }

    @Override
    @Deprecated
    public void updateState(Order order) {
        iParty.updateState(order);
    }
    
    @Override
    public String getUniqueName() {
        return(iParty.getUniqueName());
    }
}
