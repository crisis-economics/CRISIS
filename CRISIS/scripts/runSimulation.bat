#!/bin/bash
#
# This file is part of CRISIS, an economics simulator.
#
# Copyright (C) 2015 AITIA International, Inc.
# Copyright (C) 2015 John Kieran Phillips
#
# CRISIS is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# CRISIS is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
#
@echo off

for %%F in ("%0") do set DIRNAME=%%~dpF
echo %DIRNAME%
java -Xmx512m -cp %DIRNAME%/bin;%DIRNAME%/lib/asm-3.0.jar;%DIRNAME%/lib/asm-tree-3.0.jar;%DIRNAME%/lib/aspectjweaver.jar;%DIRNAME%/lib/cloning-1.8.5.jar;%DIRNAME%/lib/colt.jar;%DIRNAME%/lib/commons-math-2.2.jar;%DIRNAME%/lib/commons-math3-3.2.jar;%DIRNAME%/lib/concurrent.jar;%DIRNAME%/lib/guava-14.0.1.jar;%DIRNAME%/lib/jakarta-oro-2.0.8.jar;%DIRNAME%/lib/javaagent-0.0.1-SNAPSHOT-jar-with-dependencies.jar;%DIRNAME%/lib/javassist-3.16.1-GA.jar;%DIRNAME%/lib/javatuples-1.2.jar;%DIRNAME%/lib/jcommander-1.29.jar;%DIRNAME%/lib/jfreechart.jar;%DIRNAME%/lib/log4j-1.2.17.jar;%DIRNAME%/lib/mason-all-16.0.0-RELEASE.jar;%DIRNAME%/lib/mason-recording.jar;%DIRNAME%/lib/objenesis-1.2.jar;%DIRNAME%/lib/org.aspectj.matcher.jar;%DIRNAME%/lib/reflections-0.9.9-RC1.jar;%DIRNAME%/lib/slf4j-api-1.7.5.jar;%DIRNAME%/lib/slf4j-log4j12-1.7.5.jar;%DIRNAME%/lib/sysout-over-slf4j-1.0.2.jar;%DIRNAME%/lib/testng-6.7.jar;%DIRNAME%/lib/dashboard/css-engine-swing-api.jar;%DIRNAME%/lib/dashboard/eclipselink.jar;%DIRNAME%/lib/dashboard/forms-1.0.7.jar;%DIRNAME%/lib/dashboard/intellisweepPlugin.jar;%DIRNAME%/lib/dashboard/jgap.jar;%DIRNAME%/lib/dashboard/jgoodies-common-1.8.0.jar;%DIRNAME%/lib/dashboard/jgoodies-looks-2.6.0.jar;%DIRNAME%/lib/dashboard/laf-plugin-7.2.1.jar;%DIRNAME%/lib/dashboard/laf-widget-7.2.1.jar;%DIRNAME%/lib/dashboard/MasonPlugin-16.jar;%DIRNAME%/lib/dashboard/MEME-FormsUtils.jar;%DIRNAME%/lib/dashboard/meme-result-data.jar;%DIRNAME%/lib/dashboard/meme-wizard.jar eu.crisis_economics.abm.dashboard.cluster.SimulationRunner %*

@echo on