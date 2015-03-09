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
package eu.crisis_economics.abm.dashboard.ga.operators;

import java.util.List;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.IUniversalRateCalculator;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.impl.CrossoverOperator;

import eu.crisis_economics.abm.dashboard.ga.IdentifiableDoubleGene;
import eu.crisis_economics.abm.dashboard.ga.IdentifiableListGene;
import eu.crisis_economics.abm.dashboard.ga.IdentifiableLongGene;

/**
 * This genetic operator randomly selects pairs of chromosomes from the population, and creates a single offspring
 * with gene values that are the avarage of the two corresponding parent genes.
 * 
 * @author Tamás Máhr
 *
 */
public class GeneAveragingCrossover extends CrossoverOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7770745561637082009L;

	/**
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover() throws InvalidConfigurationException {
		super();
	}

	/**
	 * @param a_configuration
	 * @param a_crossoverRatePercentage
	 * @param a_allowFullCrossOver
	 * @param a_xoverNewAge
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, double a_crossoverRatePercentage, boolean a_allowFullCrossOver, boolean a_xoverNewAge)
			throws InvalidConfigurationException {
		super(a_configuration, a_crossoverRatePercentage, a_allowFullCrossOver, a_xoverNewAge);
	}

	/**
	 * @param a_configuration
	 * @param a_crossoverRatePercentage
	 * @param a_allowFullCrossOver
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, double a_crossoverRatePercentage, boolean a_allowFullCrossOver)
			throws InvalidConfigurationException {
		super(a_configuration, a_crossoverRatePercentage, a_allowFullCrossOver);
	}

	/**
	 * @param a_configuration
	 * @param a_crossoverRatePercentage
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, double a_crossoverRatePercentage) throws InvalidConfigurationException {
		super(a_configuration, a_crossoverRatePercentage);
	}

	/**
	 * @param a_configuration
	 * @param a_desiredCrossoverRate
	 * @param a_allowFullCrossOver
	 * @param a_xoverNewAge
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, int a_desiredCrossoverRate, boolean a_allowFullCrossOver, boolean a_xoverNewAge)
			throws InvalidConfigurationException {
		super(a_configuration, a_desiredCrossoverRate, a_allowFullCrossOver, a_xoverNewAge);
	}

	/**
	 * @param a_configuration
	 * @param a_desiredCrossoverRate
	 * @param a_allowFullCrossOver
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, int a_desiredCrossoverRate, boolean a_allowFullCrossOver)
			throws InvalidConfigurationException {
		super(a_configuration, a_desiredCrossoverRate, a_allowFullCrossOver);
	}

	/**
	 * @param a_configuration
	 * @param a_desiredCrossoverRate
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, int a_desiredCrossoverRate) throws InvalidConfigurationException {
		super(a_configuration, a_desiredCrossoverRate);
	}

	/**
	 * @param a_configuration
	 * @param a_crossoverRateCalculator
	 * @param a_allowFullCrossOver
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, IUniversalRateCalculator a_crossoverRateCalculator, boolean a_allowFullCrossOver)
			throws InvalidConfigurationException {
		super(a_configuration, a_crossoverRateCalculator, a_allowFullCrossOver);
	}

	/**
	 * @param a_configuration
	 * @param a_crossoverRateCalculator
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration, IUniversalRateCalculator a_crossoverRateCalculator)
			throws InvalidConfigurationException {
		super(a_configuration, a_crossoverRateCalculator);
	}

	/**
	 * @param a_configuration
	 * @throws InvalidConfigurationException
	 */
	public GeneAveragingCrossover(Configuration a_configuration) throws InvalidConfigurationException {
		super(a_configuration);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doCrossover(IChromosome firstMate, IChromosome secondMate, @SuppressWarnings("rawtypes") List a_candidateChromosomes, RandomGenerator generator) {
		// reset the gene values in firstMate to be the average of the
		// corresponding gene values in firstMate and secondMate
		Gene[] genes1 = firstMate.getGenes();
		Gene[] genes2 = secondMate.getGenes();
		for ( int i = 0; i < genes1.length; i++ ) {
			Object allele1 = genes1[i].getAllele();
			Object allele2 = genes2[i].getAllele();

			if ( genes1[i] instanceof IdentifiableDoubleGene ){
				double newValue = (((Double)allele1).doubleValue() + ((Double)allele2).doubleValue()) / 2.;
				genes1[i].setAllele(newValue);
			}
			
			if ( genes1[i] instanceof IdentifiableLongGene ){
				long newValue = (long)((((Long)allele1).longValue() + ((Long)allele2).longValue()) / 2. + 0.5);
				genes1[i].setAllele(newValue);
			}
			
			if ( genes1[i] instanceof IdentifiableListGene ){
				
				if ( allele1 instanceof Number && allele2 instanceof Number ){
					if ( allele1 instanceof Double || 
						allele1 instanceof Float ||
						allele2 instanceof Double ||
						allele2 instanceof Float ){
						double newValue = ( ((Number)allele1).doubleValue() + ((Number)allele2).doubleValue() ) / 2.;
						genes1[i].setAllele(newValue);
					}
				}
			}
		}
		
		
	    // Add the modified chromosome to the candidate pool so that
	    // they'll be considered for natural selection during the next
	    // phase of evolution.
	    // -----------------------------------------------------------
	    a_candidateChromosomes.add(firstMate);

	}
}
