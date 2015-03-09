/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.dashboard;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;

import kcl.waterloo.graphics.transforms.GJDataTransformInterface;

/**
 * @author Tamás Máhr
 * @param <T>
 *
 */
public class LabelFormatterDataTransform implements GJDataTransformInterface {

	protected GJDataTransformInterface transform;
	
	protected Format format;
	
	protected Format normalFormat = DecimalFormat.getNumberInstance();
	
	protected double lowerLimit;
	
	protected double upperLimit;
	
	public LabelFormatterDataTransform(final Format format, final double lowerLimit, final double upperLimit, final GJDataTransformInterface transform) {
		super();
		this.transform = transform;
		this.format = format;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}
	
	/** {@inheritDoc} 
	 */
	@Override
	public String getName() {
		return transform.getName();
	}

	/** {@inheritDoc} 
	 */
	@Override
	public double getData(double val) {
		return transform.getData(val);
	}

	/** {@inheritDoc} 
	 */
	@Override
	public double[] getData(double[] arr) {
		return transform.getData(arr);
	}

	/** {@inheritDoc} 
	 */
	@Override
	public double getInverse(double val) {
		return transform.getInverse(val);
	}

	/** {@inheritDoc} 
	 */
	@Override
	public Object getTickLabel(double val) {
		if (val < upperLimit){
			return format.format(val);
		}
		
		if (val > lowerLimit){
			return format.format(val);
		}
		return normalFormat.format(val);
	}

	/** {@inheritDoc} 
	 */
	@Override
	public ArrayList<Double> getAxisTickPositions(double start, double stop, double inc) {
		return transform.getAxisTickPositions(start, stop, inc);
	}

	/** {@inheritDoc} 
	 */
	@Override
	public ArrayList<Double> getMinorGridPositions(double start, double stop, double inc, int n) {
		return transform.getMinorGridPositions(start, stop, inc, n);
	}

//	@Override
//	public double[][] getData(double[]... vals) {
//		return transform.getData(vals);
//	}

	@Override
	public double[] getInverse(double[] val) {
		return transform.getInverse(val);
	}


}
