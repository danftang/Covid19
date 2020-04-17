set contour
unset surface
set view map
set cntrparam levels incremental 0.15,0.2,1.0
set key off
set xlabel 'R0'
set ylabel 'Proportion of transmission asymptomatic'
set key outside
splot "baseline.dat" with lines title ""
