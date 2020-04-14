import lib.gnuplot
import org.junit.Test
import kotlin.math.roundToInt

class Experiments {

//    @Test
//    fun manualContactTracing() {
//        val tracingStrategy = ContactTracingStrategy(
//            Double.NaN,Double.NaN,Double.NaN,
//            true,
//            true,
//            true,
//            true
//        )
//
//        InfectedAgent.pCompliant = 0.75
//        processingTimepTrackContour(tracingStrategy, 3.5)
//    }

//    @Test
//    fun manualTraceWithWorkplaceEnforcement() {
//        val trackingStrategy = ContactTracingStrategy(
//            1.0, 0.5,
//            Double.NaN,
//            true,
//            true,
//            true,
//            true
//        )
//        InfectedAgent.pCompliant = 0.75
//        InfectedAgent.pForcedToIsolate = 0.9
//        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
//        InfectedAgent.pSubclinical = 0.3
//        val R0 = 3.5
//        val nTrials = 300
//        val initialCases = 100
//        val data = ArrayList<Triple<Float, Float, Float>>()
//
//        for (i in 2..6) {
//            for(j in 0..5) {
//                trackingStrategy.communityProcessingTime = i * 0.5
//                trackingStrategy.pTraceInCommunity = j*0.2
//                val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
//                println("${trackingStrategy.communityProcessingTime} ${trackingStrategy.pTraceInCommunity} $pControl")
//                data.add(Triple(trackingStrategy.communityProcessingTime.toFloat(), trackingStrategy.pTraceInCommunity.toFloat(), pControl.toFloat()))
//            }
//            println("")
//        }
//    }

    @Test
    fun baseline() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            false,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.0

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun immediatePCR() {
        val tracingStrategy = ContactTracingStrategy(
            0.01,
            false,
            false,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.0

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun secondaryContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            false,
            false,
            true
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.0

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun workplaceEnforcement() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            false,
            true,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.9 // UK unemployment around 4%

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun householdEnforcement() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            true,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.0 // UK unemployment around 4%

        contourR0TotalAsymptomatic(tracingStrategy)
    }

    @Test
    fun WorkplaceSymptomMonitoring() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            false,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.9 // UK unemployment around 4%

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun householdEnforcementWithWorkplaceSymptomMonitoring() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            true,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.9 // UK unemployment around 4%

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun workplaceAndHouseholdEnforcement() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            true,
            true,
            false
        )
        InfectedAgent.pCompliant = 0.8
        InfectedAgent.pForcedToIsolate = 0.9 // UK unemployment around 4%

        contourR0TotalAsymptomatic(tracingStrategy)
    }


    @Test
    fun phoneAppContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            false,
            true,
            true,
            false
        )
        InfectedAgent.pForcedToIsolate = 0.9
        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
        InfectedAgent.pSubclinical = 0.3

        varyCompliance(tracingStrategy, 3.5)
    }

    @Test
    fun phoneAppContactTracingR0() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            true,
            true,
            false,
            false
        )
        InfectedAgent.pCompliant = 0.75
        InfectedAgent.pForcedToIsolate = 0.9
        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
        InfectedAgent.pSubclinical = 0.3

        varyR0(tracingStrategy)
//        varyCompliance(tracingStrategy, 3.5)
    }


    fun contourR0TotalAsymptomatic(trackingStrategy: ContactTracingStrategy) {
        InfectedAgent.pCompliant = 0.75
        val nTrials = 500
        val initialCases = 100
        val data = ArrayList<Triple<Double, Double, Double>>()
        val skews = listOf(100.0, 3.08, 1.38, 0.72, 0.32, 0.0)
        var jMax = 6

        for (i in 0..3) {
            for (j in 0..5) {
                val R0 = 2.5 + i * 0.5
                InfectedAgent.pSubclinical = j * 0.1
                InfectedAgent.generationIntervalShape = skews[j]
                var pControl = 0.0
                if (j < jMax) {
                    pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
                    if (pControl == 0.0) jMax = j
                }
                val x = (R0 * 1024.0).roundToInt() / 1024.0
                val y = ((2.0*InfectedAgent.pSubclinical - InfectedAgent.pSubclinical*InfectedAgent.pSubclinical)* 1024.0).roundToInt() / 1024.0
                println("$x $y $pControl")
                data.add(Triple(x, y, pControl))
            }
            println("")
        }

        gnuplot {
            val plotData = heredoc(data, 6)
            invoke(
                """
                    set contour
                    unset surface
                    set view map
                    set cntrparam levels incremental 0.15,0.2,1.0
                    set key off
                    set xlabel 'R0'
                    set ylabel 'Proportion of transmission asymptomatic'
                    set key outside
                    splot ${plotData} with lines title ""
                """
            )
        }
    }

    fun contourAsymptomaticPreSymptomatic(trackingStrategy: ContactTracingStrategy, R0: Double) {
        InfectedAgent.pCompliant = 0.75
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Triple<Double, Double, Double>>()
        val skews = listOf(100.0, 3.08, 1.38, 0.72, 0.32, 0.0)
        var jMax = 6

        for (i in 0..5) {
            for (j in 0..5) {
                InfectedAgent.pSubclinical = i * 0.1
                InfectedAgent.generationIntervalShape = skews[j]
                var pControl = 0.0
                if (j < jMax) {
                    pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
                    if (pControl == 0.0) jMax = j
                }
                val x = (InfectedAgent.pSubclinical * 1024.0).roundToInt() / 1024.0
                val y = j * 0.1
                println("$x $y $pControl")
                data.add(Triple(x,y, pControl))
            }
            println("")
        }

        gnuplot {
            val plotData = heredoc(data, 6)
            invoke(
                """
                    set contour
                    unset surface
                    set view map
                    set cntrparam levels incremental 0.15,0.2,1.0
                    set key off
                    set xlabel 'Probability of compliance in population'
                    set ylabel 'Probability of tracing compliant contact'
                    set key outside
                    splot ${plotData} with lines title ""
                """
            )
        }
    }


    fun varyCompliance(trackingStrategy: ContactTracingStrategy, R0: Double) {
        val nTrials = 1000
        val initialCases = 100
        val data = ArrayList<Pair<Double, Double>>()

        for (i in 0..5) {
            InfectedAgent.pCompliant = 0.5 + i * 0.1
            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
            println("${InfectedAgent.pCompliant} $pControl")
            data.add(Pair(InfectedAgent.pCompliant, pControl))
        }
        gnuplot {
            val plotData = heredoc(data)
            invoke(
                """
            set xlabel "Proportion of population compliant"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """
            )
        }
    }


    fun varyR0(trackingStrategy: ContactTracingStrategy) {
        val nTrials = 1000
        val initialCases = 100
        val data = ArrayList<Pair<Double, Double>>()

        for (i in 0..5) {
            val R0 = 3.5 + i * 0.5
            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
            println("${R0} $pControl")
            data.add(Pair(R0, pControl))
        }
        gnuplot {
            val plotData = heredoc(data)
            invoke(
                """
            set xlabel "R0"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """
            )
        }
    }


//    fun varyProcessingTime(trackingStrategy: ContactTracingStrategy, R0: Double) {
//        val nTrials = 300
//        val initialCases = 100
//        val data = ArrayList<Pair<Double, Double>>()
//
//        for (i in 0..6) {
////            trackingStrategy.householdProcessingTime = i * 0.5
//            trackingStrategy.workplaceProcessingTime = i * 0.5
//            trackingStrategy.communityProcessingTime = i * 0.5
//            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
//            println("${trackingStrategy.communityProcessingTime} $pControl")
//            data.add(Pair(trackingStrategy.communityProcessingTime, pControl))
//        }
//        gnuplot {
//            val plotData = heredoc(data)
//            invoke(
//                """
//            set xlabel "Tracking processing time"
//            set ylabel "Probability of control"
//            plot $plotData with lines notitle
//        """
//            )
//        }
//    }

//    fun processingTimepTrackContour(trackingStrategy: ContactTracingStrategy, R0: Double) {
//        val nTrials = 300
//        val initialCases = 100
//        val data = ArrayList<Triple<Float, Float, Float>>()
//
//        for (i in 1..6) {
//            for(j in 0..5) {
////                trackingStrategy.householdProcessingTime = i * 0.5
//                trackingStrategy.workplaceProcessingTime = i * 0.5
//                trackingStrategy.communityProcessingTime = i * 0.5
//                trackingStrategy.pTraceInCommunity = 0.5 + j*0.1
//                trackingStrategy.pTraceInWorkplace = 0.5 + j*0.1
//                val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
//                println("${trackingStrategy.communityProcessingTime} ${trackingStrategy.pTraceInWorkplace} $pControl")
//                data.add(Triple(trackingStrategy.communityProcessingTime.toFloat(), trackingStrategy.pTraceInWorkplace.toFloat(), pControl.toFloat()))
//            }
//            println("")
//        }
//        gnuplot {
//            val plotData = heredoc(data, 6)
//            invoke(
//                """
//                    set contour
//                    unset surface
//                    set view map
//                    set cntrparam levels incremental 0.1,0.2,1.1
//                    set key off
//                    set xlabel 'First report to contact-isolation time (days)'
//                    set ylabel 'Proportion of contacts traced'
//                    set key outside
//                    splot ${plotData} with lines title ""
//                """
//            )
//        }
//    }
}