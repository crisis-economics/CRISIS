/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Olaf Bochmann
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

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

/**
 * @author bochmann
 */
public class SystemNanoTest {
    /**
     * This test demonstrates that System.nanoTime() may return the same value
     * of the system timer in successive calls.
     */
    @Test(enabled = false)
    public void testSystemNano() {
        boolean condition = false;
        int i = 0;
        do {
            i++;
            long time1 = System.nanoTime();
            long time2 = System.nanoTime();
            if (time1 == time2) {
                fail("time equal after " + i + " try.");
                condition = true;
            }
        } while (condition || (i == 10000));
    }
}
