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
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PopulationTableModel extends AbstractTableModel {
	
	//====================================================================================================
	// members
	
	private final String[] header;
	private final List<String[]> values;

	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public PopulationTableModel(final String[] header, final List<String[]> values) {
		this.header = header;
		this.values = values;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public String getColumnName(final int columnIndex) {
		if (columnIndex == 0)
			return "Population";
		else if (columnIndex == header.length - 1)
			return "Fitness value";
		
		return header[columnIndex];
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override 
	public Class<?> getColumnClass(final int columnIndex) { return BigDecimal.class; }

	//----------------------------------------------------------------------------------------------------
	public int getRowCount() {
		return values.size();
	}

	//----------------------------------------------------------------------------------------------------
	public int getColumnCount() {
		return header.length;
	}

	//----------------------------------------------------------------------------------------------------
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final String strValue = values.get(rowIndex)[columnIndex];
		return new BigDecimal(strValue);
	}
}