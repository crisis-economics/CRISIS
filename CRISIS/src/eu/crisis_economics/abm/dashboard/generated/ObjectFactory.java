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


package eu.crisis_economics.abm.dashboard.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.crisis_economics.abm.dashboard.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    public final static QName _ParameterParameterValue_QNAME = new QName("http://crisis-economics.eu/model-configuration", "parameter-value");
    public final static QName _ParameterEndValue_QNAME = new QName("http://crisis-economics.eu/model-configuration", "end-value");
    public final static QName _ParameterStartValue_QNAME = new QName("http://crisis-economics.eu/model-configuration", "start-value");
    public final static QName _ParameterStepValue_QNAME = new QName("http://crisis-economics.eu/model-configuration", "step-value");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.crisis_economics.abm.dashboard.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link Gene }
     * 
     */
    public Gene createGene() {
        return new Gene();
    }

    /**
     * Create an instance of {@link Selector }
     * 
     */
    public Selector createSelector() {
        return new Selector();
    }

    /**
     * Create an instance of {@link SubmodelParameter }
     * 
     */
    public SubmodelParameter createSubmodelParameter() {
        return new SubmodelParameter();
    }

    /**
     * Create an instance of {@link DefaultParameter }
     * 
     */
    public DefaultParameter createDefaultParameter() {
        return new DefaultParameter();
    }

    /**
     * Create an instance of {@link Chromosome }
     * 
     */
    public Chromosome createChromosome() {
        return new Chromosome();
    }

    /**
     * Create an instance of {@link Operator }
     * 
     */
    public Operator createOperator() {
        return new Operator();
    }

    /**
     * Create an instance of {@link Parameter }
     * 
     */
    public Parameter createParameter() {
        return new Parameter();
    }

    /**
     * Create an instance of {@link Combination }
     * 
     */
    public Combination createCombination() {
        return new Combination();
    }

    /**
     * Create an instance of {@link GeneralParameter }
     * 
     */
    public GeneralParameter createGeneralParameter() {
        return new GeneralParameter();
    }

    /**
     * Create an instance of {@link Model }
     * 
     */
    public Model createModel() {
        return new Model();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crisis-economics.eu/model-configuration", name = "parameter-value", scope = Parameter.class)
    public JAXBElement<String> createParameterParameterValue(String value) {
        return new JAXBElement<String>(_ParameterParameterValue_QNAME, String.class, Parameter.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crisis-economics.eu/model-configuration", name = "end-value", scope = Parameter.class)
    public JAXBElement<String> createParameterEndValue(String value) {
        return new JAXBElement<String>(_ParameterEndValue_QNAME, String.class, Parameter.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crisis-economics.eu/model-configuration", name = "start-value", scope = Parameter.class)
    public JAXBElement<String> createParameterStartValue(String value) {
        return new JAXBElement<String>(_ParameterStartValue_QNAME, String.class, Parameter.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crisis-economics.eu/model-configuration", name = "step-value", scope = Parameter.class)
    public JAXBElement<String> createParameterStepValue(String value) {
        return new JAXBElement<String>(_ParameterStepValue_QNAME, String.class, Parameter.class, value);
    }

}
