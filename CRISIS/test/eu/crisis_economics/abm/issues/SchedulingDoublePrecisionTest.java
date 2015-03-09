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
package eu.crisis_economics.abm.issues;

import static org.testng.Assert.assertEquals;

import java.util.LinkedList;

import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.engine.Steppable;

public class SchedulingDoublePrecisionTest {
	
	private static final LinkedList<String> queue = new LinkedList<String>();
	
//	private static void log(final SimState state, final String msg) {
//		System.out.printf( "[%f] %s%n", state.schedule.getTime(), msg );
//	}
	
	private static final class Agent
			implements Steppable {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void step(final SimState state) {
			//log( state, "Agent" );
			queue.add( "Agent" );
		}
	}
	
	private static final class Observer
			implements Steppable {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void step(final SimState state) {
			//log( state, "Observer" );
			queue.add( "Observer" );
		}
	}
	
	@Test
	public void testSimulationCycleOrderingingIsPreciseUntilMaxExactlyRepresentedIntegerValueReached() {
		final SimState sim = new SimState( 0 );
		sim.start();
		
		final long period1 = 1;
		final long period2 = 2;
		
		final int beforePrior = - 1;
		final int defaultPrior = 0;
		
		// Max correctly represented integer value is 9007199254740993
		// See: http://introcs.cs.princeton.edu/java/91float/
		final long start = 9007198952740993L; // Just testing the last 2,000,000 elements to finish in a reasonable time
		final long endin = 9007198954740993L;
		
		sim.schedule.scheduleRepeating( start, beforePrior, new Agent(), 1 );
		sim.schedule.scheduleRepeating( start, defaultPrior, new Observer(), 2 );
		
		for (long i = start; i < endin; ++i) {
//			if ( 0 == i % 1000000L ) {
//				log( "" + i );
//			}
			
			assertEquals( queue.size(), 0, "Issue in timestep " + i );
			
			sim.schedule.step( sim );
			//log( "<--- Actual time" );
			
			if ( 0 == ( i - start ) % period1 || start == i ) {
				assertEquals( queue.poll(), "Agent", "Issue in timestep " + i );
			}
			
			if ( 0 == ( i - start ) % period2 || start == i ) {
				assertEquals( queue.poll(), "Observer", "Issue in timestep " + i );
			}
		}
		
		sim.finish();
	}
	
	public static void main(String[] args) {
	    SchedulingDoublePrecisionTest test = new SchedulingDoublePrecisionTest();
		test.testSimulationCycleOrderingingIsPreciseUntilMaxExactlyRepresentedIntegerValueReached();
		System.out.println("SchedulingDoublePrecisionTest tests pass.");
	}
}
