/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm;

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

public class AgentTest {

    private Simulation sim = null;
    private Agent agent = null;

    @BeforeMethod
    public void setUp() {
        System.out.println("Test: " + this.getClass().getSimpleName() + " begins.");
        sim = new EmptySimulation(1);
        agent = new Agent() {
           @Override
           public <T> T accept(final AgentOperation<T> operation) {
              return operation.operateOn(this);
           }
        };
        sim.start();
    }

    @AfterMethod
    public void tearDown() {
        sim.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " ends.");
    }

    // ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testFindMarketByClass() {
        System.out.println(this.getClass().getSimpleName() + 
            ".testFindMarketByClass..");
        addAndFindMarket(new CommercialLoanMarket());
    }

    @Test
    public void testFindMarketBySuperClass() {
        System.out.println(this.getClass().getSimpleName() + 
            ".testFindMarketBySuperClass..");
        class SomeSubclassedCommercialLoanMarket 
            extends CommercialLoanMarket { }
        addAndFindMarket(new SomeSubclassedCommercialLoanMarket());
    }

    private void addAndFindMarket(final CommercialLoanMarket market) {
        agent.addMarket(market);

        Set<CommercialLoanMarket> foundMarkets = agent
                .getMarketsOfType(CommercialLoanMarket.class);
        assertEquals(foundMarkets.size(), 1);
        assertEquals(foundMarkets.iterator().next(), market);
    }

    // Standalone test
    static public void main(String args[]) {
        {
        AgentTest thisTest = new AgentTest();
        thisTest.setUp();
        thisTest.testFindMarketByClass();
        thisTest.tearDown();
        }
        {
        AgentTest thisTest = new AgentTest();
        thisTest.setUp();
        thisTest.testFindMarketBySuperClass();
        thisTest.tearDown();
        }
    }
}
