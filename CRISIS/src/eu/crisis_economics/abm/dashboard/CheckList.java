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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;

import aurelienribon.ui.css.Style;

/**
 * @author Tamás Máhr
 *
 */
public class CheckList extends JList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3350728243532095810L;


	public CheckList(CheckListModel dataModel){
		super(dataModel);
		
		init();
	}
	
	/**
	 * @param dataModel
	 */
	public CheckList(ListModel dataModel) {
		super();
		
		ArrayList<Object> list = new ArrayList<Object>();
		for (int i = 0 ; i < dataModel.getSize() ; i++){
			list.add(dataModel.getElementAt(i));
		}
		dataModel = new CheckListModel(list);
			
		setModel(dataModel);
	}

	/**
	 * @param listData
	 */
	public CheckList(Object[] listData) {
		super(new CheckListModel(Arrays.asList(listData)));
		init();
	}

	/**
	 * @param listData
	 */
	public CheckList(Vector<?> listData) {
		super(new CheckListModel(listData));
		init();
	}
	
	@Override
	public void setModel(ListModel model) {
		if (!(model instanceof CheckListModel)){
			throw new IllegalArgumentException("CheckList instances must use CheckListModel model instances! (" + model.getClass().getName() + " is not allowed!)");
		}
		super.setModel(model);
		init();
	}
	
	@Override
   public CheckListModel getModel(){
		return (CheckListModel)super.getModel();
	}
	
	private void init(){
		setCellRenderer(new CheckListCellRenderer());
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				Rectangle checkBoxBounds = ((CheckListCellRenderer)getCellRenderer()).getCheckBox(index).getBounds();
				Insets borderInsets = getBorder().getBorderInsets(CheckList.this);
				int listComponentTop = indexToLocation(index).y;
				
				if (e.getX() < borderInsets.left + checkBoxBounds.x + checkBoxBounds.width &&
						e.getX() > borderInsets.left + checkBoxBounds.x &&
						e.getY() < listComponentTop + checkBoxBounds.y + checkBoxBounds.height &&
						e.getY() > listComponentTop + checkBoxBounds.y){
					setChecked(index, !getModel().getCheckedState(index));
				}
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	private class CheckListCellRenderer extends DefaultListCellRenderer {
		
		List<JPanel> renderers = new ArrayList<JPanel>();
		List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		
		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			if (renderers.size() == index){
				//Component originalRenderer=  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				JLabel originalRenderer = new JLabel(value.toString());
				final JCheckBox checkBox = new JCheckBox();
				checkBox.setSelected(getModel().getCheckedState(index));
				
				checkBox.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						getModel().setCheckedState(index, checkBox.isSelected());
					}
				});

				JPanel renderer = new JPanel(new FlowLayout(FlowLayout.LEFT));
				Style.registerCssClasses(renderer, Dashboard.CSS_CLASS_COMMON_PANEL);

				renderer.add(checkBox);
				renderer.add(originalRenderer);
				
				renderers.add(renderer);
				checkBoxes.add(checkBox);
			}
			
			return renderers.get(index);
		}
		
		public void setCheckBox(final int index, final boolean state){
			if (index < checkBoxes.size()){
				checkBoxes.get(index).setSelected(state);
			}
		}
		
		public JCheckBox getCheckBox(final int index){
			return checkBoxes.get(index);
		}
	}
	
	public void setChecked(final int index, final boolean checked){
		//checkBoxes.get(index).setSelected(checked);
		getModel().setCheckedState(index, checked);
		((CheckListCellRenderer)getCellRenderer()).setCheckBox(index, checked);
	}
}
