#!/bin/env/python
#
# This file is part of CRISIS, an economics simulator.
#
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
# Create batch processing scripts for a parameter sweep.
# Usage: python ./generateBatchScripts.py \ 
#           PARAMETER_TO_VARY
#           START_VALUE
#           END_VALUE
#           NUMBER_OF_SAMPLES
#           NUMBER_OF_CORES
#           MAX_SIMULATION_STEPS
#

import sys, os, time, argparse
from os import path
from subprocess import Popen, PIPE

parser = argparse.ArgumentParser()
parser.add_argument('-p', '--parameter-to-vary', \
   required=True, help="The parameter to test.")
parser.add_argument('-s', '--start-value', type=float, \
   required=True, help="The minimal value of the parameter.")
parser.add_argument('-e', '--end-value', type=float, \
   required=True, help="The maximum value of the parameter.")
parser.add_argument('-n', '--number-of-samples', type=int, \
   required=True, help="The number simulations to run.")
parser.add_argument('-c', '--number-of-cores', type=int, \
   required=True, help="The number of cores on which to run.")
parser.add_argument('-t', '--max-simulation-steps', type=int, \
   required=True, help="The number of simulation steps, per sample, before termination.")
parsedArgs = vars(parser.parse_args())
print parsedArgs

print "Preparing parallel Manhattan sweep.."

parameterToVary = parsedArgs['parameter_to_vary']
parameterStartValue = parsedArgs['start_value']
parameterEndValue = parsedArgs['end_value']
numSamples = parsedArgs['number_of_samples']
numParallelBatches = parsedArgs['number_of_cores']
maxSimulationSteps = parsedArgs['max_simulation_steps']
outputDirectory = "./yield_" + parameterToVary
configurationFile = './model-conf.xml'

if(numSamples <= 1):
   print "Error: Number of samples must be at least 2."
   quit()

samplingInterval = (parameterEndValue - parameterStartValue)/float(numSamples - 1)

print "Parameter To Sweep:          " + str(parameterToVary)
print "Paramter Start Value:        " + str(parameterStartValue)
print "Parameter End Value:         " + str(parameterEndValue)
print "Number Samples:              " + str(numSamples)
print "Number Parallel Executions:  " + str(numParallelBatches)
print "Simulation Steps Per Sample: " + str(maxSimulationSteps)
print "Output Location:             " + str(outputDirectory)

if(parameterStartValue >= parameterEndValue):
   print "Error: parameter start value is greater than end value."
   quit()

#
# Generate an xml configuration file.
#
with open('./conf-template.xml', 'r') as reader:
   caseToRemove = parameterToVary
   confString = ''
   for line in reader:
      if not caseToRemove in line:
         confString += line
confString = confString.replace('#MAX_SIMULATION_STEPS', str(maxSimulationSteps))
confString = confString.replace('#PARAMETER_NAME', str(parameterToVary))
confString = confString.replace('#MIN_PARAMETER_VALUE', str(parameterStartValue))
confString = confString.replace('#MAX_PARAMETER_VALUE', str(parameterEndValue))
confString = confString.replace('#SAMPLING_INTERVAL', str(samplingInterval))
with open(configurationFile, 'w') as writer:
   writer.write(confString)

#
# Generate parallel execution batch files in the local directory.
#
parallelExecutionFiles = \
   ["./Batch" + str(i) + ".bash" for i in range(0, numParallelBatches)]
writers = []
for filename in parallelExecutionFiles:
   try:
      os.remove(filename)
   except OSError:
      pass
for i in range(0, len(parallelExecutionFiles)):
   writers.append(open(parallelExecutionFiles[i], 'w'))

for i in range(0, numSamples):
   shellCommand = \
      "./startSimulation.sh " + \
      configurationFile + " " + str(i+1) + " " \
      + outputDirectory + "\n"
   writers[i % numParallelBatches].write(shellCommand);

#
# Close streams
#
for writer in writers:
   writer.close()

for filename in parallelExecutionFiles:
   Popen("chmod +x " + filename, stderr=PIPE, stdout=PIPE, shell=True)

print "Spawing Proccesses.."

if not os.path.isdir(outputDirectory):
   os.mkdir(outputDirectory)

procs = []
for i in range(0, len(parallelExecutionFiles)):
   batchName = parallelExecutionFiles[i]
   print "Launching Batch " + str(i) + ".. "
   procs.append( \
      Popen("nohup bash " + batchName, stderr=PIPE, stdout=PIPE, shell=True))
   time.sleep(1)

print "Manhattan sweep running. Results written to " + outputDirectory

quit()