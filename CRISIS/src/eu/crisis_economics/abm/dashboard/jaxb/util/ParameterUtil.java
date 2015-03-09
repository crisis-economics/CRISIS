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
package eu.crisis_economics.abm.dashboard.jaxb.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.crisis_economics.abm.dashboard.generated.ObjectFactory;
import eu.crisis_economics.abm.dashboard.generated.Parameter;

/*
 * This class needs to see the QName objects created by the ObjectFactory. So if you change the xsd, you need to modify the generated ObjectFactory again  
 *
 */
public class ParameterUtil {

	//====================================================================================================
	// methods
	
	//----------------------------------------------------------------------------------------------------
	public static String getParameterValue(final Parameter param) {
		final List<Serializable> list = param.getContent();
		
		for (final Serializable element : list) {
			if (element instanceof String)
				return (String) element;
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	public static List<String> getParameterValues(final Parameter param) {
		final List<String> result = new ArrayList<String>(param.getContent().size());
		final List<Serializable> list = param.getContent();
		
		for (final Serializable element : list) {
			if (element instanceof JAXBElement<?>) {
				@SuppressWarnings("unchecked")
				final JAXBElement<String> jaxbElement = (JAXBElement<String>) element;
				if (ObjectFactory._ParameterParameterValue_QNAME.equals(jaxbElement.getName()))
					result.add(jaxbElement.getValue());
			}
		}
		
		return result;
	}
	
	//----------------------------------------------------------------------------------------------------
	public static String getParameterStartValue(final Parameter param) {
		final List<Serializable> list = param.getContent();
		
		for (final Serializable element : list) {
			if (element instanceof JAXBElement<?>) {
				@SuppressWarnings("unchecked")
				final JAXBElement<String> jaxbElement = (JAXBElement<String>) element;
				if (ObjectFactory._ParameterStartValue_QNAME.equals(jaxbElement.getName()))
					return jaxbElement.getValue();
			}
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------------------------------
	public static String getParameterEndValue(final Parameter param) {
		final List<Serializable> list = param.getContent();
		
		for (final Serializable element : list) {
			if (element instanceof JAXBElement<?>) {
				@SuppressWarnings("unchecked")
				final JAXBElement<String> jaxbElement = (JAXBElement<String>) element;
				if (ObjectFactory._ParameterEndValue_QNAME.equals(jaxbElement.getName()))
					return jaxbElement.getValue();
			}
		}
		
		return null;
	}

	//----------------------------------------------------------------------------------------------------
	public static String getParameterStepValue(final Parameter param) {
		final List<Serializable> list = param.getContent();
		
		for (final Serializable element : list) {
			if (element instanceof JAXBElement<?>) {
				@SuppressWarnings("unchecked")
				final JAXBElement<String> jaxbElement = (JAXBElement<String>) element;
				if (ObjectFactory._ParameterStepValue_QNAME.equals(jaxbElement.getName()))
					return jaxbElement.getValue();
			}
		}
		
		return null;
	}
	
	//====================================================================================================
	// assistant methods
	
	//----------------------------------------------------------------------------------------------------
	private ParameterUtil() {}
}
