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

import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ScatterPlotGenerator;
import eu.crisis_economics.abm.model.Mark2Model;

/**
 * @author olaf
 *
 */
public class InstrumentChart extends ScatterPlotGenerator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6003901838551744721L;



	private final GUIState state;
	private Instrument instrument;


	/**
	 * plots the state of the limit order book 
	 * @param state
	 */
	public InstrumentChart(final GUIState state) {
		this.state = state;
		this.instrument = null;
	}

	/**
	 * plots the state of the limit order book 
	 * @param state
	 * @param instrument
	 */
	public InstrumentChart(final GUIState state, final Instrument instrument) {
		this.state = state;
		this.instrument = instrument;
		setTitle(instrument.getTickerSymbol());
		setYAxisLabel("Voume");
		setXAxisLabel("Price");
	}


	public void setupChart() {
		this.removeAllSeries();
		this.state.scheduleRepeatingImmediatelyAfter(new Steppable(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1281036803731750274L;

			@Override
			public void step(final SimState state) {
				removeAllSeries();
				if (instrument==null) {
					instrument = ((Mark2Model) state).getCommercialLoanMarket().getInstrument(7); // CHANGE IF MATURITY CHANGES
					setTitle(instrument.getTickerSymbol());
					setYAxisLabel("Voume");
					setXAxisLabel("Price");
				}
				for (int i = 0; i < instrument.getAskLimitOrders().numObjs; i++) {
					final double[][] values = new double[2][3];
					final double price = ((Order) instrument.getAskLimitOrders().get(i)).getPrice();
					final double size = ((Order) instrument.getAskLimitOrders().get(i)).getSize();
					values[0][0]= price;	// x1
					values[0][1]= price;	// x2
					values[0][2]= price;	// x3
					values[1][0]= 0.0;		// y1			
					values[1][1]= size;		// y2	
					values[1][2]= ((Order) instrument.getAskLimitOrders().get(i)).getOpenSize();		// y3
					if (state.schedule.getTime() >= Schedule.EPOCH && state.schedule.getTime() < Schedule.AFTER_SIMULATION) {
						addSeries(values, ((Order) instrument.getAskLimitOrders().get(i)).toString(), null);
					}
				}
				for (int i = 0; i < instrument.getBidLimitOrders().numObjs; i++) {
					final double[][] values = new double[2][3];
					final double price = ((Order) instrument.getBidLimitOrders().get(i)).getPrice();
					final double size = ((Order) instrument.getBidLimitOrders().get(i)).getSize();
					values[0][0]= price;	// x1
					values[0][1]= price;	// x2
					values[0][2]= price;	// x3
					values[1][0]=0.0;		// y1			
					values[1][1]= -size;	// y2					
					values[1][2]= -((Order) instrument.getBidLimitOrders().get(i)).getOpenSize();		// y3
					if (state.schedule.getTime() >= Schedule.EPOCH && state.schedule.getTime() < Schedule.AFTER_SIMULATION) {
						addSeries(values, ((Order) instrument.getBidLimitOrders().get(i)).toString(), null);
					}
				}
				
			}});
	}

}
