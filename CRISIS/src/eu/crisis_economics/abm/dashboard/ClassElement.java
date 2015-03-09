/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import eu.crisis_economics.abm.model.ConfigurationComponent;

public class ClassElement {
	
	//====================================================================================================
	// members
	
	public final Class<?> clazz;
	public final Object instance;
	public String displayText;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
   public ClassElement(final Class<?> clazz, final Object instance) {
      this.clazz = clazz;
      this.instance = instance;
      this.displayText = "";
      if(clazz != null) {
         for(final Annotation element : clazz.getAnnotations()) {                // Proxies
            if(element.annotationType().getName() !=
               ConfigurationComponent.class.getName())
               continue;
            try {
               displayText =
                  (String) element.annotationType().getMethod("DisplayName").invoke(element);
               break;
            } catch (IllegalArgumentException e) {
            } catch (SecurityException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            } catch (NullPointerException e) {
            }
         }
         if(displayText.isEmpty())
            displayText = clazz.getSimpleName().replaceAll("([A-Z])", " $1");
      }
      else
         displayText = "Please select a type";
   }
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return displayText;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ClassElement) {
			final ClassElement that = (ClassElement) obj;
			
			if (clazz == null) 
				return that.clazz == null;
			
			return clazz.equals(that.clazz);
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return clazz == null ? 0 : clazz.hashCode();
	}
	
	//====================================================================================================
	// nested classes
	
	//----------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	static class ClassElementComboBoxTooltipRenderer extends DefaultListCellRenderer {
		
		//====================================================================================================
		// methods
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			final Component component = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		    if (-1 < index && null != value && (value instanceof ClassElement)) {
		    	final ClassElement ce = (ClassElement) value;
                list.setToolTipText(ce.clazz != null ? ce.clazz.getName() : "");
            }
		    
		    return component;
		}
	}
}