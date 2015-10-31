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
# Full--Time Employee Earnings by Age Group (full-time median employee annual
# gross by employee age # bracket)
# 
# This Gnuplot script generates a .tex file for inclusion in the latex source
# of /doc/CRISIS.pdf.
#

set title "Full--Time Employee Earnings by Age Group (full-time median employee annual gross by employee age bracket)"
# set terminal png size 1200,500 enhanced
set terminal epslatex color size 7.5,6 "Serif,10" dl 2 linewidth 2
set output './earnings-by-age-group.tex'
set grid xtics mxtics ytics mytics lw 0.8 lt 0 linecolor rgb "#777777"
set key out vert top right nobox spacing 1.5
set xlabel "Youngest Representitive in Age Group"
set ylabel "Median Full-Time Earnings (per annum, GBP $\pounds$, not adjusted)"
set xrange [* : *]
set mxtics 2
set xtics (18,22,30,40,50,60)

#
# Add a data source for full-time employee earnings versus age group (median
# gross annual earnings) for inclusion in /doc/CRISIS.pdf:
#
plot  '' using 1:2 wi lp title columnhead,  \
      '' using 1:3 wi lp title columnhead,  \
      '' using 1:4 wi lp title columnhead,  \
      '' using 1:5 wi lp title columnhead,  \
      '' using 1:6 wi lp title columnhead,  \
      '' using 1:7 wi lp title columnhead
