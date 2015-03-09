/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class PercentJSlider extends JSlider {
	
	//====================================================================================================
	// members
	
	private static final long serialVersionUID = 2576610576224304791L;
	
	protected double doubleMinimum;
	protected double doubleMaximum;
	protected double step;
	protected int displayedPrecision;
	
	protected long longMinimum;
	protected long longMaximum;
	
	protected boolean isDouble;

	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public PercentJSlider(final double minimum, final double maximum, final double value) {
		super(0,100);
		
		if (minimum > maximum)
			throw new IllegalArgumentException("Interval is empty.");
		
		if (value < minimum || value > maximum)
			throw new IllegalArgumentException("The specified value is not in the interval.");
		
		this.doubleMinimum = minimum;
		this.doubleMaximum = maximum;
		this.isDouble = true;

		this.step = (this.doubleMaximum - this.doubleMinimum) / 100.; 
		this.displayedPrecision = calculateDisplayedPrecision();

		setValue(value);
	}
	
	//----------------------------------------------------------------------------------------------------
	public PercentJSlider(final long minimum, final long maximum, final long value) {
		super(0,100);
		
		if (minimum > maximum)
			throw new IllegalArgumentException("Interval is empty.");
		
		if (value < minimum || value > maximum)
			throw new IllegalArgumentException("The specified value is not in the interval.");
		
		this.longMinimum = minimum;
		this.longMaximum = maximum;
		this.isDouble = false;
		
		this.step = (this.longMaximum - this.longMinimum) / 100.;
		
		setValue(value);
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setValue(final double value) {
		if (!isDouble) 
			throw new IllegalArgumentException("This slider does not support double values.");
		if (value < doubleMinimum || value > doubleMaximum)
			throw new IllegalArgumentException("The specified value is not in the interval.");
		final double d = (value - doubleMinimum) / step + 0.5;
		setValue((int)d);
		setToolTipText(String.valueOf(getDoubleValue()));
	}
	
	//----------------------------------------------------------------------------------------------------
	public void setValue(final long value) {
		if (isDouble) 
			setValue((double)value);
		else {
			if (value < longMinimum || value > longMaximum)
				throw new IllegalArgumentException("The specified value is not in the interval.");
			final double d = (value - longMinimum) / step + 0.5;
			setValue((int)d);
			setToolTipText(String.valueOf(getLongValue()));
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	public double getDoubleValue() {
		if (!isDouble) 
			throw new IllegalArgumentException("This slider does not support double values.");
		final double result = doubleMinimum + getValue() * step;
		
		return round(result,displayedPrecision);
	}
	
	//----------------------------------------------------------------------------------------------------
	public long getLongValue() {
		if (isDouble)
			throw new IllegalArgumentException("This slider does not support long values.");
		final long result = (long) (longMinimum + getValue() * step + 0.5);
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Hashtable createStandardLabels(int increment, int start) {
		final Hashtable labels = super.createStandardLabels(increment,start);
		
		final JLabel first = new JLabel(isDouble ?  String.valueOf(doubleMinimum) : String.valueOf(longMinimum),SwingConstants.CENTER);
		labels.put(new Integer(0),first);

		final JLabel last = new JLabel(isDouble ?  String.valueOf(doubleMaximum) : String.valueOf(longMaximum),SwingConstants.CENTER);
		labels.put(new Integer(100),last);
		
		return labels;
	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private int calculateDisplayedPrecision() {
		final List<Integer> list = new ArrayList<Integer>(3); 
		list.add(getNumberOfDecimalPlaces(doubleMinimum));
		list.add(getNumberOfDecimalPlaces(doubleMaximum));
		list.add(getNumberOfDecimalPlaces(step));
		
		return Collections.max(list);
	}
	
	//----------------------------------------------------------------------------------------------------
	private static double round(final double value, final int precision) {
	    if (precision < 0) 
	    	throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(precision,RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	//----------------------------------------------------------------------------------------------------
	private static int getNumberOfDecimalPlaces(final double value) {
		if (Math.round(value) == value)
			return 0;
		
		final BigDecimal bd = new BigDecimal(String.valueOf(value));
	    final String string = bd.stripTrailingZeros().toPlainString();
	    final int index = string.indexOf(".");
	    return index < 0 ? 0 : string.length() - index - 1;
	}
}