set contour
unset surface
set view map
set cntrparam levels incremental 0.15,0.2,1.0
set key off
set xlabel 'First report to contact-isolation time (days)'
set ylabel 'Proportion of contacts traced'
set key outside
splot "contourWorkplace.dat" with lines title ""
