set contour
unset surface
set view map
set cntrparam levels incremental 0.65,0.1,1.1
set key off
set xlabel 'First report to contact-isolation time (days)'
set ylabel 'Proportion of contacts traced'
set key outside
splot "contour15presymptomatic.dat" with lines title ""
