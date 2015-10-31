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
# Median New Dwelling Prices and Price Composition
# 
# This Gnuplot script generates a .tex file for inclusion in the latex source
# of /doc/CRISIS.pdf.
#

set title "Median New Dwelling Prices and Price Composition"
# set terminal png size 1200,500 enhanced
set terminal epslatex color size 9,6 "Serif,10" dl 2 linewidth 2
set output './median-house-prices.tex'
set grid xtics mxtics ytics mytics lw 0.8 lt 0 linecolor rgb "#777777"
set key on out vert top right nobox spacing 1.8
set xlabel "Year"
set ylabel "Median House Prices (GBP $\\pounds$, not adjusted, new dwellings)"
set xrange [*:*]

set style fill transparent solid 0.80 noborder
set rmargin 30

#
# Add a data source for median new dwelling prices vs. year for inclusion 
# in /doc/CRISIS.pdf:
#
plot '' using 1:(($2+$3+$4+$5+$6+$7+$8+$9+$10+$11+$12)*$13/100.) title '$\\pounds 1,000,000+$' lt rgb '#ffff00' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6+$7+$8+$9+$10+$11)*$13/100.)                         title '$\\pounds 500k-1,000,000$' lt rgb '#ffcc00' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6+$7+$8+$9+$10)*$13/100.)                             title '$\\pounds 400k-500k$' lt rgb '#ffbb00' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6+$7+$8+$9)*$13/100.)                                 title '$\\pounds 300k-400k$' lt rgb '#eeaa11' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6+$7+$8)*$13/100.)                                    title '$\\pounds 250k-300k$' lt rgb '#cc8833' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6+$7)*$13/100.)                                       title '$\\pounds 200k-250k$' lt rgb '#aa6655' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5+$6)*$13/100.)                                          title '$\\pounds 150k-200k$' lt rgb '#884477' wi filledcurve x1,\
     '' using 1:(($2+$3+$4+$5)*$13/100.)                                             title '$\\pounds 125k-150k$' lt rgb '#663399' wi filledcurve x1,\
     '' using 1:(($2+$3+$4)*$13/100.)                                                title '$\\pounds 100k-125k$' lt rgb '#4433bb' wi filledcurve x1,\
     '' using 1:(($2+$3)*$13/100.)                                                   title '$\\pounds 80k-100k$' lt rgb '#2222dd' wi filledcurve x1,\
     '' using 1:(($2)*$13/100.)                                                      title '$< \\pounds 80k$' lt rgb '#0022ff' wi filledcurve x1,\
     '' using 1:13                                                                   title 'median price, all new dwellings' wi lp ls 1 lw 2
