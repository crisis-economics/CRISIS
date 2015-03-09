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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;

import eu.crisis_economics.abm.model.Layout;
import ai.aitia.meme.paramsweep.gui.info.ISubmodelGUIInfo;
import ai.aitia.meme.paramsweep.gui.info.MasonChooserParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.ParameterInfo;
import ai.aitia.meme.paramsweep.gui.info.SubmodelInfo;

//----------------------------------------------------------------------------------------------------
public class AvailableParameter {
	

	//====================================================================================================
	// members
	
	public final ParameterInfo info;
	protected String displayName;
	
	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public AvailableParameter(final ParameterInfo info, final Class<?> modelClass) {
		if (info == null) 
			throw new IllegalArgumentException("AvailableParameter(): null parameter.");
		
		this.info = info;

		Class<?> parentClass = modelClass; 
		if ( info.isSubmodelParameter() ) {
			parentClass = ((ISubmodelGUIInfo)info).getParentValue();			
		}
		
		Field declaredField = null;
		while ( declaredField == null ) {
			try {
				declaredField = parentClass.getDeclaredField(StringUtils.uncapitalize(info.getName()));
			} catch (NoSuchFieldException e) {
				parentClass = parentClass.getSuperclass();
				
				if (parentClass == null) {
//					throw new IllegalStateException("Could not find field for parameter " + info.getName() + " in " + startClass);
					break;
				}
			}
		}
		
		if ( declaredField != null ) {
			for(final Annotation element : declaredField.getAnnotations()) {
				if(element.annotationType().getName() != Layout.class.getName())     // Proxies
					continue;

				final Class<? extends Annotation> type = element.annotationType();

				try {
					displayName = (String)(type.getMethod("FieldName").invoke(element));
				} catch (IllegalArgumentException e) {
				} catch (SecurityException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				} catch (NoSuchMethodException e) {
				}
			}
		}
		
		if ( displayName == null ) {
			displayName = info.getName().replaceAll("([A-Z])", " $1");
		}
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
//		final String humanName = info.getName().replaceAll("([A-Z])", " $1");
		String result = displayName;
		if (info.getValue() != null) {
			if (info instanceof MasonChooserParameterInfo) {
				final MasonChooserParameterInfo mpInfo = (MasonChooserParameterInfo) info;
				result += ": " + mpInfo.getValidStrings()[((Integer)info.getValue())];
			} else if (info instanceof SubmodelInfo) {
				final SubmodelInfo sInfo = (SubmodelInfo) info;
				if (sInfo.getActualType() != null)
					result += ": " + sInfo.getActualType().getSimpleName();
			} else
 				result += ": " + info.getValue().toString();
		}
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AvailableParameter) {
			final AvailableParameter that = (AvailableParameter) obj;
			return this.info.equals(that.info);
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return info.hashCode();
	}
}