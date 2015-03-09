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
package eu.crisis_economics.abm.dashboard.ga.operators;

import java.util.Collections;
import java.util.Iterator;

import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.NaturalSelector;
import org.jgap.Population;
import org.jgap.util.CloneException;
import org.jgap.util.ICloneable;

/**
 * @author Tamás Máhr
 *
 */
public class StandardPostSelectorFixed extends NaturalSelector implements ICloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7610440994271229278L;

	  /**
	   * Stores the chromosomes to be taken into account for selection
	   */
	  private Population m_chromosomes;

	  /**
	   * Indicated whether the list of added chromosomes needs sorting
	   */
	  private boolean m_needsSorting;

	  /**
	   * Comparator that is only concerned about fitness values
	   */
	  private FitnessValueComparator m_fitnessValueComparator;

	  /**
	   * Default constructor.<p>
	   * Attention: The configuration used is the one set with the static method
	   * Genotype.setConfiguration.
	   *
	   * @throws InvalidConfigurationException
	   *
	   * @author Klaus Meffert
	   * @since 3.2
	   */
	  public StandardPostSelectorFixed()
	      throws InvalidConfigurationException {
	    this(Genotype.getStaticConfiguration());
	  }

	  /**
	   * Constructor.
	   *
	   * @param a_config the configuration to use
	   * @throws InvalidConfigurationException
	   *
	   * @author Klaus Meffert
	   * @since 3.2
	   */
	  public StandardPostSelectorFixed(final Configuration a_config)
	      throws InvalidConfigurationException {
	    super(a_config);
	    m_chromosomes = new Population(a_config);
	    m_needsSorting = false;
	    m_fitnessValueComparator = new FitnessValueComparator();
	  }

	  /**
	   * Add a Chromosome instance to this selector's working pool of Chromosomes.
	   *
	   * @param a_chromosomeToAdd the specimen to add to the pool
	   *
	   * @author Klaus Meffert
	   * @since 3.2
	   */
	  protected void add(final IChromosome a_chromosomeToAdd) {
	    // New chromosome, insert it into the sorted collection of chromosomes.
	    // --------------------------------------------------------------------
	    a_chromosomeToAdd.setIsSelectedForNextGeneration(false);
	    m_chromosomes.addChromosome(a_chromosomeToAdd);
	    // Indicate that the list of chromosomes to add needs sorting.
	    // -----------------------------------------------------------
	    m_needsSorting = true;
	  }

	  /**
	   * Selects a given number of Chromosomes from the pool that will move on
	   * to the next generation population. This selection will be guided by the
	   * fitness values. The chromosomes with the best fitness value win.
	   *
	   * @param a_from_pop the population the Chromosomes will be selected from
	   * @param a_to_pop the population the Chromosomes will be added to
	   * @param a_howManyToSelect the number of Chromosomes to select
	   *
	   * @author Klaus Meffert
	   * @since 1.1
	   */
	  @SuppressWarnings("unchecked")
	public void select(final int a_howManyToSelect,
	                     final Population a_from_pop,
	                     final Population a_to_pop) {
	    if (a_from_pop != null) {
	      int popSize = a_from_pop.size();
	      if (popSize < 1) {
	        throw new IllegalStateException("Population size must be greater 0");
	      }
	      for (int i = 0; i < popSize; i++) {
	        add(a_from_pop.getChromosome(i));
	      }
	    }
	    int canBeSelected;
	    int chromsSize = m_chromosomes.size();
	    if (chromsSize < 1) {
	      throw new IllegalStateException(
	          "Number of chromosomes must be greater 0");
	    }
	    if (a_howManyToSelect > chromsSize) {
	      canBeSelected = chromsSize;
	    }
	    else {
	      canBeSelected = a_howManyToSelect;
	    }
//	    int neededSize = a_howManyToSelect;
	    // First select all chromosomes with no fitness value computed.
	    // ------------------------------------------------------------
	    Iterator<?> it = m_chromosomes.iterator();
	    while (it.hasNext()) {
	      IChromosome c = (IChromosome)it.next();
	      if (Math.abs(c.getFitnessValueDirectly() -
	                   FitnessFunction.NO_FITNESS_VALUE)
	          < FitnessFunction.DELTA) {
	        a_to_pop.addChromosome(c);
	        it.remove();
	        canBeSelected--;
	        if (canBeSelected < 1) {
	          break;
	        }
	      }
	    }

	    // Sort the collection of chromosomes previously added for evaluation.
	    // Only do this if necessary.
	    // -------------------------------------------------------------------
	    if (m_needsSorting && canBeSelected > 0) {
	      Collections.sort(m_chromosomes.getChromosomes(),
	                       m_fitnessValueComparator);
	      m_needsSorting = false;
	    }
	    // To select a chromosome, we just go thru the sorted list.
	    // --------------------------------------------------------
	    IChromosome selectedChromosome;
	    for (int i = 0; i < canBeSelected; i++) {
	      selectedChromosome = m_chromosomes.getChromosome(i);
	      selectedChromosome.setIsSelectedForNextGeneration(true);
	      a_to_pop.addChromosome(selectedChromosome);
	    }
//	    int toAdd;
//	    toAdd = neededSize - a_to_pop.size();
//	    // Add existing Chromosome's to fill up the return
//	    // result to contain the desired number of Chromosome's.
//	    // -----------------------------------------------------
//	    chromsSize = m_chromosomes.size();
//	    for (int i = 0; i < toAdd; i++) {
//	      selectedChromosome = m_chromosomes.getChromosome(i % chromsSize);
//	      selectedChromosome.setIsSelectedForNextGeneration(true);
//	      a_to_pop.addChromosome(selectedChromosome);
//	    }
	  }

	  /**
	   * Empties out the working pool of Chromosomes.
	   *
	   * @author Klaus Meffert
	   * @since 3.2
	   */
	  public void empty() {
	    // Clear the list of chromosomes
	    // -----------------------------
	    m_chromosomes.getChromosomes().clear();
	    m_needsSorting = false;
	  }

	  /**
	   * @return always true as no Chromosome can be returnd multiple times
	   *
	   * @author Klaus Meffert
	   * @since 3.2
	   */
	  public boolean returnsUniqueChromosomes() {
	    return true;
	  }

	  public boolean equals(Object a_o) {
	    if (a_o == null) {
	      return false;
	    }
	    StandardPostSelectorFixed other = (StandardPostSelectorFixed) a_o;
	    if (!m_fitnessValueComparator.getClass().getName().equals(
	        other.m_fitnessValueComparator.getClass().getName())) {
	      return false;
	    }
	    if (!m_chromosomes.equals(other.m_chromosomes)) {
	      return false;
	    }
	    return true;
	  }

	  public Object clone() {
	    try {
	      StandardPostSelectorFixed sel = new StandardPostSelectorFixed(getConfiguration());
	      sel.m_needsSorting = m_needsSorting;
	      return sel;
	    } catch (Throwable t) {
	      throw new CloneException(t);
	    }
	  }

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + m_fitnessValueComparator.hashCode();
		result = 31 * result + m_chromosomes.hashCode();
		
		return result;
	}
}
