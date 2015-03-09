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
package eu.crisis_economics.abm.contracts.settlements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettlementAdjacencyMatrix implements SettlementListener {

	private Map<Key<?,?>,Double> a = new HashMap<Key<?,?>,Double>();
	ArrayList<SettlementParty> nodes = new ArrayList<SettlementParty>();

	private class Key<K1, K2>{
		private final K1 part1;
		private final K2 part2;

		public Key(K1 part1, K2 part2) {
			this.part1 = part1;
			this.part2 = part2;
		}

		@Override 
		public boolean equals(Object other) {
			if (!(other instanceof Key)) {
				return false;
			}
			// Can't find out the type arguments, unfortunately
			Key<?, ?> rawOther = (Key<?, ?>) other;
			// TODO: Handle nullity
			return part1.equals(rawOther.part1) && part2.equals(rawOther.part2);
		}

		@Override 
		public int hashCode() {
			// TODO: Handle nullity
			int hash = 23;
			hash = hash * 31 + part1.hashCode();
			hash = hash * 31 + part2.hashCode();
			return hash;
		}
	}

	@Override
	public void transferDone(SettlementEvent settlementEvent) {
		//System.out.println("settlement transfer event: "+settlementEvent.toString());
		SettlementParty party1 = ((Settlement)settlementEvent.getSource()).getFirstParty();
		SettlementParty party2 = ((Settlement)settlementEvent.getSource()).getSecondParty();
		if(!nodes.contains(party1)) {nodes.add(party1);}
		if(!nodes.contains(party2)) {nodes.add(party2);}
		double amount = settlementEvent.getAmount();
		Key<SettlementParty, SettlementParty> key;
		if(amount < 0.) {
		   key = new Key<SettlementParty, SettlementParty>(party2, party1);
		   amount = -amount;
		}
		else
		   key = new Key<SettlementParty, SettlementParty>(party1, party2);
		if (a.containsKey(key)) {
			double value = a.get(key);
			value += amount;
			a.put(key, value);
		} else
			a.put(key, amount);
	}

	public SettlementParty getNode(int i) {
		return nodes.get(i);
	}

	public double maxOutDegree() {
		double degree = 0.0;
		for (int i = 0; i < nodes.size(); i++) {
			degree = Math.max(outDegree(i), degree);
		}
		return degree;
	}

	public double maxInDegree() {
		double degree = 0.0;
		for (int i = 0; i < nodes.size(); i++) {
			degree = Math.max(inDegree(i), degree);
		}
		return degree;
	}

	public double maxDegree() {
		return Math.max(maxInDegree(), maxOutDegree());
	}

	public double totDegree(int i) {
		return outDegree(i)+inDegree(i);
	}

	public double outDegree(int i) {
		double degree = 0.0;
		for (int j = 0; j < nodes.size(); j++) {
			Key<SettlementParty, SettlementParty> key = new Key<SettlementParty, SettlementParty> (nodes.get(i),nodes.get(j));
			if (a.containsKey(key)) {
				degree += a.get(key);
			} 
		}
		return degree;
	}

	public double inDegree(int j) {
		double degree = 0.0;
		for (int i = 0; i < nodes.size(); i++) {
			Key<SettlementParty, SettlementParty> key = new Key<SettlementParty, SettlementParty> (nodes.get(i),nodes.get(j));
			if (a.containsKey(key)) {
				degree += a.get(key);
			} 
		}
		return degree;
	}

	public int size() {
		return nodes.size();
	}

	@Override
	public void inverseTransferDone(SettlementEvent settlementEvent) {
		//System.out.println("settlement inverse transfer event: "+settlementEvent.toString());
		SettlementParty party1 = ((Settlement)settlementEvent.getSource()).getFirstParty();
		SettlementParty party2 = ((Settlement)settlementEvent.getSource()).getSecondParty();
		if(!nodes.contains(party1)) {nodes.add(party1);}
		if(!nodes.contains(party2)) {nodes.add(party2);}
		Key<SettlementParty, SettlementParty> key = new Key<SettlementParty, SettlementParty> (party2,party1);
		if (a.containsKey(key)) {
			double value = a.get(key);
			value += settlementEvent.getAmount();
			a.put(key, value);
		} else {
			a.put(key, settlementEvent.getAmount());
		}
	}

	public double getValue(SettlementParty party1,SettlementParty party2) {
		Key<SettlementParty, SettlementParty> key = new Key<SettlementParty, SettlementParty> (party1,party2);
		if (a.containsKey(key)) {
			return a.get(key);
		} else {
			return 0.0;
		}
	}

	public double getValue(int i, int j) {
		return getValue(nodes.get(i),nodes.get(j));
	}
}

