/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm;

import eu.crisis_economics.abm.simulation.EmptySimulation;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.RecordTime;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * @author Tamás Máhr
 */
@Recorder(
    value = "testrecorder2.csv",
    recordAt = RecordTime.END_OF_RUN,
    sources = { "totalSteps", "time" }
    )
public class RecordingTestModel2 extends EmptySimulation {
    @RecorderSource("totalSteps")
    protected long steps;
    
    public RecordingTestModel2(long seed) {
        super(seed);
        steps = 0;
    }
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public void start() {
        super.start();
        
        schedule.scheduleRepeating(
            0, 
            new Steppable() {
                private static final long serialVersionUID = -6714428269001305681L;
                @Override
                public void step(SimState state) {
                    steps = state.schedule.getSteps();
                }
            },
            0.5);
    }
    
    @RecorderSource("time")
    double getSimulationTime() {
        return schedule.getTime();
    }
}
