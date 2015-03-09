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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="general-parameter" type="{http://crisis-economics.eu/model-configuration}generalParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="default-parameter" type="{http://crisis-economics.eu/model-configuration}defaultParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="parameter" type="{http://crisis-economics.eu/model-configuration}parameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="submodel-parameter" type="{http://crisis-economics.eu/model-configuration}submodelParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="combination" type="{http://crisis-economics.eu/model-configuration}combination" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="selector" type="{http://crisis-economics.eu/model-configuration}selector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="operator" type="{http://crisis-economics.eu/model-configuration}operator" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="chromosome" type="{http://crisis-economics.eu/model-configuration}chromosome" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="run-strategy" type="{http://crisis-economics.eu/model-configuration}modelType" default="single" />
 *       &lt;attribute name="number-of-runs" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "generalParameterList",
    "defaultParameterList",
    "parameterList",
    "submodelParameterList",
    "combination",
    "selectorList",
    "operatorList",
    "chromosome"
})
@XmlRootElement(name = "model")
public class Model {

    @XmlElement(name = "general-parameter")
    protected List<GeneralParameter> generalParameterList;
    @XmlElement(name = "default-parameter")
    protected List<DefaultParameter> defaultParameterList;
    @XmlElement(name = "parameter")
    protected List<Parameter> parameterList;
    @XmlElement(name = "submodel-parameter")
    protected List<SubmodelParameter> submodelParameterList;
    protected List<Combination> combination;
    @XmlElement(name = "selector")
    protected List<Selector> selectorList;
    @XmlElement(name = "operator")
    protected List<Operator> operatorList;
    protected Chromosome chromosome;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;
    @XmlAttribute(name = "run-strategy")
    protected ModelType runStrategy;
    @XmlAttribute(name = "number-of-runs")
    protected Integer numberOfRuns;

    /**
     * Gets the value of the generalParameterList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generalParameterList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneralParameterList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeneralParameter }
     * 
     * 
     */
    public List<GeneralParameter> getGeneralParameterList() {
        if (generalParameterList == null) {
            generalParameterList = new ArrayList<GeneralParameter>();
        }
        return this.generalParameterList;
    }

    /**
     * Gets the value of the defaultParameterList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultParameterList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultParameterList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DefaultParameter }
     * 
     * 
     */
    public List<DefaultParameter> getDefaultParameterList() {
        if (defaultParameterList == null) {
            defaultParameterList = new ArrayList<DefaultParameter>();
        }
        return this.defaultParameterList;
    }

    /**
     * Gets the value of the parameterList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameterList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameterList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Parameter }
     * 
     * 
     */
    public List<Parameter> getParameterList() {
        if (parameterList == null) {
            parameterList = new ArrayList<Parameter>();
        }
        return this.parameterList;
    }

    /**
     * Gets the value of the submodelParameterList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the submodelParameterList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubmodelParameterList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubmodelParameter }
     * 
     * 
     */
    public List<SubmodelParameter> getSubmodelParameterList() {
        if (submodelParameterList == null) {
            submodelParameterList = new ArrayList<SubmodelParameter>();
        }
        return this.submodelParameterList;
    }

    /**
     * Gets the value of the combination property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the combination property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCombination().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Combination }
     * 
     * 
     */
    public List<Combination> getCombination() {
        if (combination == null) {
            combination = new ArrayList<Combination>();
        }
        return this.combination;
    }

    /**
     * Gets the value of the selectorList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selectorList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelectorList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Selector }
     * 
     * 
     */
    public List<Selector> getSelectorList() {
        if (selectorList == null) {
            selectorList = new ArrayList<Selector>();
        }
        return this.selectorList;
    }

    /**
     * Gets the value of the operatorList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operatorList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperatorList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Operator }
     * 
     * 
     */
    public List<Operator> getOperatorList() {
        if (operatorList == null) {
            operatorList = new ArrayList<Operator>();
        }
        return this.operatorList;
    }

    /**
     * Gets the value of the chromosome property.
     * 
     * @return
     *     possible object is
     *     {@link Chromosome }
     *     
     */
    public Chromosome getChromosome() {
        return chromosome;
    }

    /**
     * Sets the value of the chromosome property.
     * 
     * @param value
     *     allowed object is
     *     {@link Chromosome }
     *     
     */
    public void setChromosome(Chromosome value) {
        this.chromosome = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the runStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType }
     *     
     */
    public ModelType getRunStrategy() {
        if (runStrategy == null) {
            return ModelType.SINGLE;
        } else {
            return runStrategy;
        }
    }

    /**
     * Sets the value of the runStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType }
     *     
     */
    public void setRunStrategy(ModelType value) {
        this.runStrategy = value;
    }

    /**
     * Gets the value of the numberOfRuns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * Sets the value of the numberOfRuns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfRuns(Integer value) {
        this.numberOfRuns = value;
    }

}
