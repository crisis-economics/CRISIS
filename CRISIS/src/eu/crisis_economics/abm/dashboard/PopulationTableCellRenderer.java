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

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class PopulationTableCellRenderer extends DefaultTableCellRenderer {
	
	//====================================================================================================
	// members
	
	
	private static final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	static { // print double numbers rounded to 10 decimal digits using US format
		numberFormatter.setMaximumFractionDigits(10);
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setGroupingUsed(false);
	}
	
	private int bestCombinationIdx;

	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public PopulationTableCellRenderer(final int bestCombinationIdx) {
		this.bestCombinationIdx = bestCombinationIdx;
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row,
												   final int column) {
		final JLabel result = (JLabel) super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		
		String text = "";
		if (row == bestCombinationIdx)
			text = "<html><b>";
		
		BigDecimal bd = (BigDecimal) value;
		bd = bd.setScale(10,BigDecimal.ROUND_HALF_UP);
		text += numberFormatter.format(bd);
		
		if (row == bestCombinationIdx) 
			text += "</b></html>";
		
		result.setBackground(bestCombinationIdx == row ? Color.GREEN : (row % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY));
		result.setText(text);
		
		return result;
	}
}
