/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.Iterator;

import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

/**
 * Local market restricts exchange to parties within a neighborhood.
 * 
 * @author olaf
 */
public abstract class ConstraintMarket extends Market {

	private final Network network;

    public ConstraintMarket(
      final Network network,
      final NamedEventOrderings matchingStepOrdering
      ) {
      super(matchingStepOrdering);
      this.network = network;
    }

	/**
	 * Returns true if we have a sell-buy or a buy-sell order pair, the order parties have adequate relationship 
	 * in the network and the orders have overlapping targets.
	 * @param orderA
	 * @param orderB
	 */
	public boolean isCompatibleOrderPair(final Order orderA, final Order orderB) {
		boolean pair = false;
		if (orderA.getSize()>0 && getBuyers(orderA.getParty()).contains(orderB.getParty()) && (orderA.getTarget().isEmpty()||orderA.getTarget().contains(orderB.getParty())) && orderB.getSize()<0 && (orderB.getTarget().isEmpty()||orderB.getTarget().contains(orderA.getParty()))) {
			pair = true;
		}
		if (orderB.getSize()>0 && getBuyers(orderB.getParty()).contains(orderA.getParty())  && (orderB.getTarget().isEmpty()||orderB.getTarget().contains(orderA.getParty())) && orderA.getSize()<0 && (orderA.getTarget().isEmpty()||orderA.getTarget().contains(orderB.getParty()))) {
			pair = true;
		}
		return pair;
	}

	@SuppressWarnings("unchecked")
	public Bag getSellers(final Party buyer) {
		final Bag edges = network.getEdgesIn(buyer);
		final Bag sellers = new Bag();
		for (final Iterator<Edge> iterEdges = edges.iterator(); iterEdges.hasNext();) {
			sellers.add(iterEdges.next().from());
		}
		return sellers;
	}

	@SuppressWarnings("unchecked")
	public Bag getBuyers(final Party seller) {
		final Bag edges = network.getEdgesOut(seller);
		final Bag buyers = new Bag();
		for (final Iterator<Edge> iterEdges = edges.iterator(); iterEdges.hasNext();) {
			buyers.add(iterEdges.next().to());
		}
		return buyers;
	}
}
