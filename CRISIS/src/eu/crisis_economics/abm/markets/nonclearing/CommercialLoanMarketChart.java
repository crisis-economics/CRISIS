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

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sim.util.media.chart.TimeSeriesChartGenerator;
import eu.crisis_economics.abm.contracts.loans.Loan;

/**
 * @author olaf
 *
 */
public class CommercialLoanMarketChart extends TimeSeriesChartGenerator implements InstrumentListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6003901838551744721L;


	private Market market;


	/**
	 * creates a time series chart for prices of all instruments in the market 
	 */
	public CommercialLoanMarketChart() {
		this.setTitle("Prices of Loan Instruments");
		this.setYAxisLabel("Price");
		this.setXAxisLabel("Time");
	}


	public void setupChart() {
		this.removeAllSeries();
	}


	public Market getMarket() {
		return market;
	}


	public void setMarket(final Market market) {
		if (this.market != null) {
			market.removeListener(this);
		}
		this.market = market;
		this.market.addListener(this);
	}


	@Override
	public void newContract(final InstrumentEvent instrumentEvent) {
		final int key = ((Loan)instrumentEvent.getContract()).getTotalNumberOfInstallmentsToRepaySinceInception();
		final int index = getSeriesDataset().indexOf(key);
		XYSeries series;
		
		   if (index < 0) { // new series
			   series = new org.jfree.data.xy.XYSeries(key,false);
			   addSeries(series, null);
		   } else {
			   series = ((XYSeriesCollection)getSeriesDataset()).getSeries(index);
		   }
           series.add(instrumentEvent.getContract().getCreationTime(), instrumentEvent.getContract().getInterestRate(), true);
	}


	@Override
	public void orderUpdated(final InstrumentEvent instrumentEvent) {
	}

}
