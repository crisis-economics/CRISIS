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

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * This class represents a list model that maintans the extra information about each element whether it is checked or not. 
 * 
 * @author Tamás Máhr
 *
 */
@SuppressWarnings("serial")
public class CheckListModel extends AbstractListModel implements ListModel {

	List<?> elements = new ArrayList<Object>();
	
	List<Boolean> checkedState = new ArrayList<Boolean>();
	
	/**
	 * Creates a new list model containing the specified elements. The checked state of all elements are initialized to false.
	 * 
	 * @param elements the elements this list model should contain
	 */
	public CheckListModel(final List<?> elements) {
		this.elements = new ArrayList<Object>(elements);

		for (int i = 0; i < elements.size(); i++) {
			checkedState.add(false);
		}
	}

	/** {@inheritDoc} 
	 */
	@Override
	public int getSize() {
		return elements.size();
	}

	/** {@inheritDoc} 
	 */
	@Override
	public Object getElementAt(int index) {
		return elements.get(index);
	}

	
	public void setCheckedState(final int index, final boolean state){
		checkedState.set(index, state);

		fireContentsChanged(this, index, index);
	}
	
	public boolean getCheckedState(final int index){
		if (index < 0) return false;
		
		return checkedState.get(index);
	}
	
}
