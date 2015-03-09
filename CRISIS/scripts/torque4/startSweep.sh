#!/bin/bash
#
# This file is part of CRISIS, an economics simulator.
#
# Copyright (C) 2015 AITIA International, Inc.
# Copyright (C) Ross Richardson
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

function usage() {
	echo -e "Usage: $0 [-t wallclock-time] [-v] paramsweep-config.xml"
	echo -e "\t-t wallclock-time: the maximum amount of time the simulation"
	echo -e "\t   experiment is allowed to run, in the format of hh:mm:ss"
    echo -e "\t-v verbose mode"
}

SCRIPTDIR=`dirname $0`
EXECUTOR='submit.sh'

WORKDIR=`pwd`

ARGC=$#

EXPERIMENT_NAME=CRISIS
ALLOWED_TIME=
SWEEP_CONFIG_FILE=
VERBOSE=0
QUIET=
PORT=0

i=1
while [ $i -le $ARGC ]; do
	if [ ${!i} = "-t" ];then
		i=$((i + 1))
		ALLOWED_TIME=${!i}
	elif [ ${!i} = "-v" ];then
		VERBOSE=1
	elif [ ${!i} = "-p" ];then
		i=$((i + 1))
		PORT=${!i}
	else
		SWEEP_CONFIG_FILE=${!i}
	fi

	i=$((i + 1))
done


if [ -z "$SWEEP_CONFIG_FILE" ]; then
	echo "Please specify a parameter-sweep config file!"
	usage
	exit 1
fi

if [ ! -r $SWEEP_CONFIG_FILE ]; then
	echo "The config file $SWEEP_CONFIG_FILE does not exists or is not readable!"
	exit 1
fi

if [ $VERBOSE -eq 1 ]; then
    echo "ALLOWED_TIME = $ALLOWED_TIME"
    echo "SWEEP_CONFIG_FILE = $SWEEP_CONFIG_FILE"
else
	QUIET="-q"
fi

NUM_OF_RUNS=-1
JOB_NAME="CRISIS"

check_nof_runs="<model .* number-of-runs=\"([0-9]+)\""
check_class="<model .* class=\"(.+)\""

while read LINE; do
	if [[ $LINE =~ $check_nof_runs ]]; then
		NUM_OF_RUNS=${BASH_REMATCH[1]}
		break
	fi
	if [[ $LINE =~ $check_class ]]; then
		CLASSNAME=(${BASH_REMATCH[1]//./ })
		LAST_INDEX=${#CLASSNAME[@]}
		LAST_INDEX=$((LAST_INDEX - 1))
		JOB_NAME=${CLASSNAME[$LAST_INDEX]}
		break
	fi
done < $SWEEP_CONFIG_FILE

if [ $NUM_OF_RUNS -eq -1 ];then
	echo "Could not find the number of requested runs in the configuration file ($SWEEP_CONFIG_FILE)."
	exit 1
fi


#
# ----- identify cluster
#
NUM_OF_CORES_PER_NODE=-1

if [ `hostname -f | grep ice.oerc` ]; then
    NUM_OF_CORES_PER_NODE=8
    CLUSTER_NAME="hal"
fi

if [ `hostname -f | grep arcus.osc` ]; then
    NUM_OF_CORES_PER_NODE=16
    CLUSTER_NAME="arcus"
fi

if [ `hostname -f | grep caribou` ]; then
    NUM_OF_CORES_PER_NODE=8
    CLUSTER_NAME="caribou"
fi

if [ $NUM_OF_CORES_PER_NODE -eq -1 ]; then
    echo " *** error: could not determine cluster"
    exit 1
fi

if [ $VERBOSE -eq 1 ]; then
    echo "running jobs on $CLUSTER_NAME"
    echo "number of simulations is $NUM_OF_RUNS"
    echo "number of simulations per job is $NUM_OF_CORES_PER_NODE"
fi


#
# ----- resources
#
RESOURCES="nodes=1:ppn=$NUM_OF_CORES_PER_NODE"

if [ -n "$ALLOWED_TIME" ]; then
    RESOURCES+=",walltime=$ALLOWED_TIME"
fi

#EMAIL="ross.richardson@maths.ox.ac.uk"


#
# ----- job submission
#
NUM_OF_JOBS=`python -c "from math import ceil; print int(ceil(float($NUM_OF_RUNS)/float($NUM_OF_CORES_PER_NODE)))"`

if [ $VERBOSE -eq 1 ]; then
    echo "submitting $NUM_OF_JOBS jobs"
fi

JOBID=`qsub -l $RESOURCES \
     -N $JOB_NAME \
     -v SWEEP_CONFIG_FILE=$SWEEP_CONFIG_FILE,WORKDIR=$WORKDIR,SCRIPTDIR=$SCRIPTDIR,QUIET=$QUIET \
     -d $WORKDIR \
     -t 1-$NUM_OF_JOBS \
     $SCRIPTDIR/$EXECUTOR`

if [ $PORT -ne 0 ]; then
	$SCRIPTDIR/endSweep.sh -p $PORT -d $WORKDIR $JOBID 
fi