#!/bin/bash -x
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
#
# Starts a GA search experiment on the cluster
#

ARGC=$#

CONFIGFILE=
SCRIPTDIR=
ALLOWED_TIME=
VERBOSE=0

i=1
while [ $i -le $ARGC ]; do
	if [ ${!i} = "-t" ];then
		i=$((i + 1))
		ALLOWED_TIME=${!i}
	elif [ ${!i} = "-v" ];then
		VERBOSE=1
	else
		CONFIGFILE=${!i}
	fi

	i=$((i + 1))
done


case `uname` in 
	Darwin )
		SCRIPTDIR=`dirname $0`
		;;
	Linux )
		SCRIPTDIR=`readlink -f $0`
		SCRIPTDIR=`dirname $SCRIPTDIR`
esac

SCRIPTDIR=`dirname $SCRIPTDIR`

CLASSPATH="`ls $SCRIPTDIR/../lib/*.jar $SCRIPTDIR/../lib/dashboard/*.jar $SCRIPTDIR/../lib/cluster/*.jar $SCRIPTDIR/../lib/waterloo/*.jar | tr [:space:] :`$SCRIPTDIR/../bin"

if [ -z "$CONFIGFILE" ]; then
	echo "Please specify a ga configuration file as argument!"
	exit 1
fi

if [ -n "$ALLOWED_TIME" ];then
	ALLOWED_TIME="--timeLimit=$ALLOWED_TIME"
fi

java -Xmx1024m -classpath $CLASSPATH -Dscheduler.type=local -Dscripts.dir=$SCRIPTDIR eu.crisis_economics.abm.dashboard.cluster.ClusterMain --configFile=$CONFIGFILE $ALLOWED_TIME
