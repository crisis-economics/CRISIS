#!/bin/bash
#
# This file is part of CRISIS, an economics simulator.
#
# Copyright (C) 2015 AITIA International, Inc.
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
# This script is intended to be used as a torque array job.
# It starts the runSimulation.sh script with appropriate parameters taken 
# from the scheduler supplied environment.
#

RUN_ID=$(((PBS_ARRAYID-1)*PBS_NUM_PPN+1))

for RUN_NUM in `seq 1 $PBS_NUM_PPN`; do
  LOCALDIR=$WORKDIR/run-$RUN_ID
  mkdir -p $LOCALDIR
  $SCRIPTDIR/runSimulation.sh $SWEEP_CONFIG_FILE $RUN_ID $LOCALDIR &
  RUN_ID=$((RUN_ID+1))
done

wait
