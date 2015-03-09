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

import java.util.ArrayList;
import java.util.List;

import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.OutputTime;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.RecordTime;

/**
 * @author Tamás Máhr
 */
@Recorder(
    value = "testrecorder.csv",
    recordAt = RecordTime.END_OF_ITERATION,
    outputAt = OutputTime.END_OF_RUN,
    sources = {
        "time",
        "modelDouble",
        "modelIntegers",
        "agentInteger",
        "agentDouble",
        "avg(integers)"
        }
    )
public class RecordingTestModel extends EmptySimulation {
    private static final long serialVersionUID = -2323741112793319942L;
    
    @RecorderSource("modelDouble")
    protected double someDouble;

    @RecorderSource(
        value = "modelIntegers",
        collectionLength = 5,
        NAFiller = "-1")
    protected List<Integer> intList = new ArrayList<Integer>();
    
    @RecorderSource(
        value = "agent",
        collectionLengthMember = "agentNum")
    protected List<RecordingTestAgent>
        agentList = new ArrayList<RecordingTestAgent>();
    
    protected int agentNum = 7;
    
    @RecorderSource(
        value = "agentDouble",
        collectionLengthMember = "bagSize()",
        innerType = RecordingTestAgent.class,
        member = "getValue()")
    protected Bag anonymList = new Bag();
    
    public RecordingTestModel(long seed) {
        super(seed);
        
        for (int i = 0; i < agentNum; i++) {
            RecordingTestAgent recordingTestAgent = new RecordingTestAgent(i);
            agentList.add(recordingTestAgent);
            anonymList.add(recordingTestAgent);
        }
        
        schedule.scheduleRepeating(
            new Steppable() {
                private static final long
                    serialVersionUID = 1023983258758828337L;
                
                @Override
                public void step(SimState state) {
                    someDouble = state.schedule.getTime();
                    intList.add((int) Math.round(someDouble));
                }
            }, 
            1);
    }
	
    public int bagSize() {
        return anonymList.size();
    }
    
    @RecorderSource("time")
    public double getValue() {
        return Simulation.getFloorTime();
    }
    
    @RecorderSource("integers")
    public List<Integer> getIntegers() {
        return intList;
    }
}
