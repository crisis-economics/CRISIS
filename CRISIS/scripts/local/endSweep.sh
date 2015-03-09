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
# This script schedules jobEnd.sh to run after the sweep-job is over.
#

function usage() {
	echo -e "Usage: $0 -p port -d workdir JOBID"
	echo -e "\t-p port -- the port on which the server process listens for the http request"
	echo -e "\t-d workdir -- working directory"
	echo -e "JOBID -- the ID of the job after which jobEnd.sh should run"
}

SCRIPTDIR=`dirname $0`
ARGC=$#

HOST=
PORT=
WORKDIR=
JOBID=

i=1
while [ $i -le $ARGC ]; do
	if [ ${!i} = "-d" ];then
		i=$((i + 1))
		WORKDIR=${!i}
	elif [ ${!i} = "-p" ];then
		i=$((i + 1))
		PORT=${!i}
	else
		JOBID=${!i}
	fi

	i=$((i + 1))
done

if [ -z "$WORKDIR" ]; then
	echo "Please specify -d workdir"
	usage
	exit 1
fi

if [ -z "$PORT" ]; then
	echo "Please specify -p port"
	usage
	exit 1
fi

# this is local, no JOBID
#if [ -z "$JOBID" ]; then
#	echo "Please specify JOBID"
#	usage
#	exit 1
#fi


#	qsub -W depend=afteranyarray:$JOBID \
#		-v PORT=$PORT \
#	    -d $WORKDIR \
#		$SCRIPTDIR/jobEnd.sh

export PBS_O_HOST=localhost
export PORT

$SCRIPTDIR/jobEnd.sh