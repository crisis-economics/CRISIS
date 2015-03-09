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
package eu.crisis_economics.abm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.Test;

import sim.engine.SimState;

public class RecordingTest {
    @Test(enabled = false)
    public void recording() throws URISyntaxException, IOException {
        SimState model;
        model = new RecordingTestModel(0);
        model.start();
        
        for (int step = 0; step < 23; ++step)
            model.schedule.step(model);
        
        model.finish();
        
        File resultFile = new File("testrecorder.csv");
        File goodResultFile = new File(
            RecordingTest.class.getResource(
               "testrecorder.csv").toURI());
        
        Assert.assertTrue(resultFile.exists());
        Assert.assertTrue(areSameFiles(resultFile, goodResultFile),
            "results file does not match the predefined results file");
    }
    
    private boolean areSameFiles(
        File resultFile,
        File goodResultFile) throws IOException {
        if ( !(resultFile.isFile() && goodResultFile.isFile()
            && resultFile.exists() && goodResultFile.exists()) ) {
            return false;
        }
        
        String
            line1,
            line2;
        BufferedReader
            resultReader = new BufferedReader(
               new FileReader(resultFile)),
            goodResultReader = new BufferedReader(
               new FileReader(goodResultFile));
        
        // The header and footer will not match due to timestamps.
        resultReader.readLine();
        goodResultReader.readLine();
        
        while (
            (line1 = resultReader.readLine()) != null
         && (line2 = goodResultReader.readLine()) != null) {
            // The last line is also a timestamp.
            if (line1.startsWith("End Time"))
                continue;
            if (!line1.equals(line2)) {
                resultReader.close();
                goodResultReader.close();
                return false;
            }
        }
        
        resultReader.close();
        goodResultReader.close();
        return true;
    }

    @Test(enabled = false)
    public void endOfRunRecording() throws URISyntaxException, IOException {
        SimState model = new RecordingTestModel2(0);
        
        model.start();
        
        for (int step = 0; step < 23; step++)
            model.schedule.step(model);
        
        model.finish();
        
        File resultFile = new File("testrecorder2.csv");
        File goodResultFile = new File(RecordingTest.class.getResource(
                "testrecorder2.csv").toURI());
        
        Assert.assertTrue(resultFile.exists());
        Assert.assertTrue(areSameFiles(resultFile, goodResultFile),
                "results file does not match the predefined results file");
    }

    @Test
    public void nullCollectionLength() {
        try {
            new RecordingTestModel3(0);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (!message.startsWith("You are trying to record a collection (")
                    || !message.contains(" of length 0")) {
                throw e;
            }
        }
    }
}
