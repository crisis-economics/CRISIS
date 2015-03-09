/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.model.configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;

/**
  * A simple configuration component for {@link Household} goods consumers.
  * This implementation signals that {@link Household} {@link Agent}{@code s} 
  * should consider only one in every {@code N} goods types when formulating
  * their consumption strategy. The number {@code N} is customizable.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Buy From One In N"
   )
public final class BuyFromOneInNSectorsHouseholdConsumptionConfiguration extends
   AbstractSectorNameProviderConfiguration
   implements HouseholdSectorsToBuyFromConfiguration {
   
   private static final long serialVersionUID = -6208549207166505093L;
   
   private final static int
      DEFAULT_N = 1,
      DEFAULT_OFFSET = 0;
   
   @Parameter(
      ID = "BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_N"
      )
   private int
      N = DEFAULT_N;
   @Parameter(
      ID = "BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_OFFSET"
      )
   private int
      offset = DEFAULT_OFFSET;
   
   public int getN() {
      return N;
   }
   
   /**
     * Set the modulus term (one in every {@code N} after offset) for this sector
     * selection scheme. The argument must be non-negative.
     */
   public void setN(final int n) {
      N = n;
   }
   
   public int getOffset() {
      return offset;
   }
   
   public void setOffset(final int offset) {
      this.offset = offset;
   }
   
   private static class HouseholdSectorsToConsiderProvider
      implements SectorNameProvider {
      
      @Inject
      private SectorNameProvider
         allSectors;
      @Inject @Named("BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_N")
      private int
         N;
      @Inject @Named("BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_OFFSET")
      private int
         offset;
      
      @Override
      public LinkedHashSet<String> asSet() {
         return new LinkedHashSet<String>(asList());
      }
      
      @Override
      public List<String> asList() {
         final List<String>
            allPossibilities = allSectors.asList(),
            result = new ArrayList<String>();
         for(int i = 0; i< allPossibilities.size(); ++i)
            if((i + offset) % N == 0)
               result.add(allPossibilities.get(i));
         return result;
      }
   }
   
   /**
     * Create a {@link BuyFromOneInNSectorsHouseholdConsumptionConfiguration} object
     * with default parameters.
     */
   public BuyFromOneInNSectorsHouseholdConsumptionConfiguration() {
      this(DEFAULT_N, DEFAULT_OFFSET);
   }
   
   /**
     * Create a {@link BuyFromOneInNSectorsHouseholdConsumptionConfiguration} object
     * with custom parameters.
     * 
     * @param N
     *        The index <code>N</code>. This argument must be non-negative.
     * @param offset
     *        The modulus offset to use when counting.
     */
   public BuyFromOneInNSectorsHouseholdConsumptionConfiguration(
      final int N,
      final int offset
      ) {
      Preconditions.checkArgument(N > 0);
      this.N = N;
      this.offset = offset;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(N > 0);
   }
   
   @Override
   protected void addBindings() {
      bind(Integer.class)
         .annotatedWith(Names.named("BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_N"))
         .toInstance(N);
      bind(Integer.class)
         .annotatedWith(Names.named("BUY_FROM_ONE_IN_HOUSEHOLD_CONSUMPTION_OFFSET"))
         .toInstance(offset);
      if(getScopeString().isEmpty())
         bind(SectorNameProvider.class)
            .to(HouseholdSectorsToConsiderProvider.class);
      else
         bind(SectorNameProvider.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(HouseholdSectorsToConsiderProvider.class);
      super.addBindings();
   }
}
