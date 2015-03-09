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
# Sends a http GET request to $PBS_O_HOST:$PORT with the URL path: /finished/
# PBS_O_HOST and PORT have to be set in the environment

if [ -z "$PBS_O_HOST" -o -z "$PORT" ];then
	echo "Please specify HOST and PORT in the environment of this script!"
	exit 1
fi

# copy result files into the working directory under names that ResultMerger can handle
PATTERN=".*/([^_]+)_([0-9]*)\.csv" 
for FILE in run-*/*.csv; do
	if [[ $FILE =~ $PATTERN ]];then
		mv $FILE ${BASH_REMATCH[1]}.csv.part${BASH_REMATCH[2]}
	fi
done


curl http://$PBS_O_HOST:$PORT/finished/