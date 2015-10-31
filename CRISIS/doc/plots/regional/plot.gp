#
# This file is part of CRISIS, an economics simulator.
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
# A Gnuplot script for generating the following /doc/CRISIS.pdf plot:
# Regional Employee Earnings (full-time median employee annual earnings per
# region)
# 
# This Gnuplot script generates a .tex file for inclusion in the latex source
# of /doc/CRISIS.pdf.
#

set title "Regional Employee Earnings (full-time median employee annual earnings per region)"
# set terminal png size 1200,500 enhanced
set terminal epslatex color size 9,6 "Serif,10" dl 2 linewidth 2
set output './regional-rankings.tex'
set grid xtics mxtics ytics mytics lw 0.8 lt 0 linecolor rgb "#777777"
set key out vert top right nobox spacing 1.5
set xlabel "Year"
set ylabel "Median Full-Time Earnings (per annum, GBP £, not adjusted)"
set xrange [1999:2014]

#
# Add a data source for UK regional employee earnings (full-time median
# earnings) versus UK region for inclusion in /doc/CRISIS.pdf:
#
plot '' using 1:2 wi lp title "United Kingdom (Whole)",\
     '' using 1:3 wi lp title "England (Whole)",\
     '' using 1:4 wi lp title "North--East England",\
     '' using 1:5 wi lp title "North--West England",\
     '' using 1:6 wi lp title "Yorkshire and The Humber",\
     '' using 1:7 wi lp title "East Midlands",\
     '' using 1:8 wi lp title "West Midlands",\
     '' using 1:8 wi lp title "East England",\
     '' using 1:9 wi lp title "London",\
     '' using 1:10 wi lp title "South--East England",\
     '' using 1:11 wi lp title "South--West England",\
     '' using 1:12 wi lp title "Wales",\
     '' using 1:13 wi lp title "Scotland",\
     '' using 1:11 wi lp title "Northern Ireland"
