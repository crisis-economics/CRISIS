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
package eu.crisis_economics.abm.simulation;

import java.awt.Frame;
import java.util.Arrays;

import sim.display.Console;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;

public class SimulationGui
		extends GUIState {
	
	public static String DUMMY = "dummy";
	
	public SimulationGui(final SimState state) {
		super( state );
	}
	
	public Console getConsole() {
		final Frame[] frames = Frame.getFrames();
		for (final Frame act : frames) {
			if ( act instanceof Console ) {
				final Console console = (Console) act;
				return console;
			}
		}
		
		throw new AssertionError( "Console not found in array of current frames: " + Arrays.toString( frames ) );
	}
	
	public static String getName() {
		return "[Crisis-Mason API]";
	}
	
	@Override
	public Object getSimulationInspectedObject() {
		return state;
	}
	
	@Override
	public Inspector getInspector() {
		final Inspector i = super.getInspector();
		i.setVolatile( true );
		return i;
	}
}
