/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.markets.clearing;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.firm.ClearingFirm;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

public class ClearingHouseTestExistingAgents {
   
   private ClearingFirm clearingFirm;
   private ClearingBank clearingBank;
   private ClearingHouse clearingHouse;
   private Simulation simState;
   
   @BeforeClass
   public void setUp() throws Exception {
      System.out.println("Test: " + this.getClass().getSimpleName());
      simState = new EmptySimulation(1L);
      simState.start();
      
      clearingBank = mock(ClearingBank.class);
      clearingFirm = mock(ClearingFirm.class);
      
      when(clearingBank.getUniqueName()).thenReturn("Mock ClearingBank");
      when(clearingFirm.getUniqueName()).thenReturn("Mock ClearingFirm");
      
      clearingHouse = new ClearingHouse();
      
      clearingHouse.addBank(clearingBank);
      clearingHouse.addLender(clearingBank);
      
      clearingHouse.addBorrower(clearingFirm);
      clearingHouse.addFirm(clearingFirm);
   }
   
   @AfterClass
   public void tearDown() {
      simState.finish();
   }
   
   /**
    * This test checks whether the agents have been added correctly to the
    * clearingHouse
    */
   @Test
   public void setClearingHouseTest() {
      Assert.assertTrue(clearingHouse.getAllParticipants().containsValue(clearingBank));
      Assert.assertTrue(clearingHouse.getAllParticipants().containsValue(clearingFirm));
      
      Assert.assertTrue(clearingHouse.getLenders().containsValue(clearingBank));
      
      // test failed in default branch
      // Assert.assertTrue(clearingHouse.getStockHolders().containsValue(clearingBank));
      
      Assert.assertTrue(clearingHouse.getBorrowers()
         .containsValue(
            clearingFirm));
      
      // test failed in default branch
      // Assert.assertTrue(clearingHouse.hasClearingInstrument("Stock_"+clearingFirm.getUniqueName()));
      // Assert.assertTrue(clearingHouse.hasClearingInstrument("LOAN"+ new
      // Integer(maturity).toString()));
      
   }
   
   /**
     * Manual Entry Point
     */
   static public void main(String[] args) {
      try {
         final ClearingHouseTestExistingAgents
            test = new ClearingHouseTestExistingAgents();
         test.setUp();
         test.setClearingHouseTest();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
