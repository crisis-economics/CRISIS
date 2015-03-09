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
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.RecordTime;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;

/**
 * @author Tamás Máhr
 */
@Recorder(
    value = "testrecorder3.csv",
    recordAt = RecordTime.END_OF_RUN,
    sources = { "intList" }
    )
public class RecordingTestModel3 extends EmptySimulation {
    private static final long serialVersionUID = -5198890734391649714L;
    
    public RecordingTestModel3(long seed) {
        super(seed);
    }
    
    @RecorderSource("intList")
    List<Integer> intList = new ArrayList<Integer>();
}
