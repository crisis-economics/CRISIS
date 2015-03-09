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

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class NullSelectionModel implements ListSelectionModel {
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public boolean isSelectionEmpty() { return true; }  
	public boolean isSelectedIndex(final int index) { return false; }  
	public int getMinSelectionIndex() { return -1; }  
	public int getMaxSelectionIndex() { return -1; }  
	public int getLeadSelectionIndex() { return -1; }  
	public int getAnchorSelectionIndex() { return -1; }  
	public void setSelectionInterval(final int index0, final int index1) {}  
	public void setLeadSelectionIndex(final int index) {}  
	public void setAnchorSelectionIndex(final int index) {}  
	public void addSelectionInterval(final int index0, final int index1) {}  
	public void insertIndexInterval(final int index, final int length, final boolean before) {}  
	public void clearSelection() {}  
	public void removeSelectionInterval(final int index0, final int index1) {}  
	public void removeIndexInterval(final int index0, final int index1) { }  
	public void setSelectionMode(final int selectionMode) {}  
	public int getSelectionMode() { return SINGLE_SELECTION; }  
	public void addListSelectionListener(final ListSelectionListener lsl) {}  
	public void removeListSelectionListener(final ListSelectionListener lsl) {}  
	public void setValueIsAdjusting(final boolean valueIsAdjusting) {}  
	public boolean getValueIsAdjusting() { return false; }  
}  