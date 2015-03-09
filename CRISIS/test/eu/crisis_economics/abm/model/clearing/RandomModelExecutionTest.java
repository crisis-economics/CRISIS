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
package eu.crisis_economics.abm.model.clearing;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import eu.crisis_economics.abm.model.MasterModel;
import eu.crisis_economics.abm.simulation.Simulation;

public class RandomModelExecutionTest {
   
   /**
     * The default number of timesteps (simulation cycles) for which to run 
     * the model in this unit test.
     */
   public final static int
      DEFAULT_NUMBER_OF_TIMESTEPS_TO_RUN = 250;
   
   private MasterModel
      model;
   private int
      numberOfStepsToRun = DEFAULT_NUMBER_OF_TIMESTEPS_TO_RUN;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing RandomModelExecutionTest..");
      model = new MasterModel(1L);
      Assert.assertTrue(numberOfStepsToRun > 0);
   }
   
   /**
     * Test the {@link MasterModel} model file for execution errors. The test
     * behaves as follows:<br>
     * 
     * <ul>
     *   <li> one instance ({@code M}) of {@link MasterModel} is created;
     *   <li> {@code M} is executed for {@code N} simulation cycles, where
     *        {@code N} is a customizable integer greater than {@code zero};
     *   <li> if any exception is raised during the execution of {@code M},
     *        this test fails. Otherwise, this test succeeds.
     * </ul>
     * 
     * Important notes: model files consume a relatively large amount of
     * memory. It is recommended that the java heap space is increased
     * when running this unit test (eg. -Xms6g).
     */
   private void testASingleRandomModelExecution() {
      try {
         model.start();
         while(Simulation.getTime() < numberOfStepsToRun) {
            model.schedule.step(model);
         }
      }
      catch(final Exception e) {
         System.err.println(
            "Test failed: " + ExceptionUtils.getRootCause(e).getClass().getSimpleName() 
          + " was raised.");
         Assert.fail();
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
      model.finish();
      model = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(final String[] args) {
      try {
         final RandomModelExecutionTest
            test = new RandomModelExecutionTest();
         test.setUp();
         test.testASingleRandomModelExecution();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
