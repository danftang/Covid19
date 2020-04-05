import lib.gnuplot
import org.junit.Test

class Experiments {

    @Test
    fun manualContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            Double.NaN,Double.NaN,Double.NaN,
            Double.NaN,
            Double.NaN,
            true,
            true,
            true,
            true
        )

        InfectedAgent.pCompliant = 0.75
        processingTimepTrackContour(tracingStrategy, 3.5)
    }

    @Test
    fun manualTraceWithWorkplaceEnforcement() {
        val trackingStrategy = ContactTracingStrategy(
            0.0, 1.5,
            Double.NaN,
            0.9,
            Double.NaN,
            true,
            true,
            true,
            true
        )
        InfectedAgent.pCompliant = 0.75
        InfectedAgent.pForcedToIsolate = 0.9
        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
        InfectedAgent.pSubclinical = 0.3
        val R0 = 3.5
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Triple<Float, Float, Float>>()

        for (i in 2..6) {
            for(j in 0..5) {
                trackingStrategy.communityProcessingTime = i * 0.5
                trackingStrategy.pTraceInCommunity = j*0.2
                val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
                println("${trackingStrategy.communityProcessingTime} ${trackingStrategy.pTraceInCommunity} $pControl")
                data.add(Triple(trackingStrategy.communityProcessingTime.toFloat(), trackingStrategy.pTraceInCommunity.toFloat(), pControl.toFloat()))
            }
            println("")
        }
    }

    @Test
    fun phoneAppContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            1.0, 1.0, 1.0,
            0.9,
            0.9,
            false,
            true,
            true,
            true
        )
        InfectedAgent.pForcedToIsolate = 0.9
        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
        InfectedAgent.pSubclinical = 0.3

        varyCompliance(tracingStrategy, 3.5)
    }

    @Test
    fun phoneAppContactTracingR0() {
        val tracingStrategy = ContactTracingStrategy(
            1.0, 1.0, 1.0,
            0.9,
            0.9,
            false,
            true,
            true,
            true
        )
        InfectedAgent.pCompliant = 0.95
        InfectedAgent.pForcedToIsolate = 0.9
        InfectedAgent.generationIntervalShape = 0.7 // 30% pre-symptomatic
        InfectedAgent.pSubclinical = 0.3

        varyR0(tracingStrategy)
    }


    fun varyCompliance(trackingStrategy: ContactTracingStrategy, R0: Double) {
        val nTrials = 300
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
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Pair<Double, Double>>()

        for (i in 0..4) {
            val R0 = 2.0 + i * 0.5
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


    fun varyProcessingTime(trackingStrategy: ContactTracingStrategy, R0: Double) {
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Pair<Double, Double>>()

        for (i in 0..6) {
            trackingStrategy.householdProcessingTime = i * 0.5
            trackingStrategy.workplaceProcessingTime = i * 0.5
            trackingStrategy.communityProcessingTime = i * 0.5
            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
            println("${trackingStrategy.communityProcessingTime} $pControl")
            data.add(Pair(trackingStrategy.communityProcessingTime, pControl))
        }
        gnuplot {
            val plotData = heredoc(data)
            invoke(
                """
            set xlabel "Tracking processing time"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """
            )
        }
    }

    fun processingTimepTrackContour(trackingStrategy: ContactTracingStrategy, R0: Double) {
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Triple<Float, Float, Float>>()

        for (i in 1..6) {
            for(j in 0..5) {
                trackingStrategy.householdProcessingTime = i * 0.5
                trackingStrategy.workplaceProcessingTime = i * 0.5
                trackingStrategy.communityProcessingTime = i * 0.5
                trackingStrategy.pTraceInCommunity = j*0.2
                trackingStrategy.pTraceInWorkplace = j*0.2
                val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
                println("${trackingStrategy.communityProcessingTime} ${trackingStrategy.pTraceInWorkplace} $pControl")
                data.add(Triple(trackingStrategy.communityProcessingTime.toFloat(), trackingStrategy.pTraceInWorkplace.toFloat(), pControl.toFloat()))
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
                    set cntrparam levels incremental 0.1,0.2,1.1
                    set key off
                    set xlabel 'First report to contact-isolation time (days)'
                    set ylabel 'Proportion of contacts traced'
                    set key outside
                    splot ${plotData} with lines title ""
                """
            )
        }
    }
}