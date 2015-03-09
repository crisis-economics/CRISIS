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
package eu.crisis_economics.abm.firm.plugins;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.algorithms.optimization.ProductionCostMinimizationAlgorithm;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;

/** 
  * A {@link FirmProductionFunction} algorithm which uses labour resources only 
  * for production. This production function does not require input goods for
  * production.<br><br>
  * 
  * This production function has the form<br><br>
  * 
  * <center><code>
  *   P = Z(t) * L**Q
  * </code></center><br><br>
  * 
  * Where <code>P</code> is total production (yield), <code>Z(t)</code> is the total
  * factor productivity (TFP factor, as function of time t), <code>L</code> is the labour input for
  * production and, <code>Q</code> is a customizable labour productivity exponent.<br><br>
  * 
  * @author phillips
  */
public final class LabourOnlyProductionFunction extends FirmProductionFunction {
   
   private TimeseriesParameter<Double>
      z;
   private double
      exponent;
   
   /**
     * Create a {@link LabourOnlyProductionFunction} object with custom parameters.<br><br>
     * See also {@link LabourOnlyProductionFunction}.
     * 
     * @param z <code>(Z)</code><br>
     *        The total factor productivity (TFP factor) for this production function.
     *        This argument must be non-negative.
     * @param exponent <code>(Q)</code><br>
     *        The labour productivity exponent for this production function. This argument
     *        must be non-negative.
     */
   @Inject
   public LabourOnlyProductionFunction(
   @Named("LABOUR_ONLY_PRODUCTION_FUNCTION_TFP")
      final TimeseriesParameter<Double> z,
   @Named("LABOUR_ONLY_PRODUCTION_FUNCTION_EXPONENT")
      final double exponent
      ) {
      super(new HashMap<String, Double>());
      Preconditions.checkArgument(
         exponent >= 0., getClass().getSimpleName() + ": exponent is negative.");
      this.z = z;
      this.exponent = exponent;
   }
   
    /** 
      * Determine labour demand by optimizing the yield function.
      */
   @Override
   public OptimalProduction computeInputsDemand(
      double productionTarget,
      final double labourUnitCost,
      final Map<String, Double> economicUnitCosts
      ) {
      productionTarget = Math.max(productionTarget, 0.);
      double
         requiredLabour = Math.pow(productionTarget / z.get(), 1./exponent),
         totalEconomicCost = labourUnitCost * requiredLabour;
      final Map<String, Double>
         goodsDemands = new HashMap<String, Double>();
      final ProductionCostMinimizationAlgorithm.OptimizationSoluton
         solution = new ProductionCostMinimizationAlgorithm.OptimizationSoluton(
            goodsDemands,
            totalEconomicCost,
            requiredLabour
            );
      return solution;
   }
   
   /**
     * Get the yield (goods produced) and total cash cost of production.
     */
   @Override
   public FirmProductionFunction.Yield produce(
      double labourInput,
      final Map<String, Double> goodsInputVolumes,
      final double labourUnitCost,
      final Map<String, Double> cashGoodsUnitCosts
      ) {
      labourInput = Math.max(labourInput, 0.);
      final double
         totalProduction = z.get() * Math.pow(labourInput, exponent),
         totalLabourUnitCost = labourUnitCost * labourInput;
      return super.createYield(
         totalProduction,
         totalLabourUnitCost
         );
   }
   
   public double getTFPFactor() {
      return z.get();
   }
   
   public double getExponent() {
      return exponent;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Labour Only Production Function, TFP factor: " + getTFPFactor()
           + " labour productivity exponent: " + getExponent() + ".";
   }
}
