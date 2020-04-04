import lib.gnuplot
import org.junit.Test

class Experiments {

    @Test
    fun manualContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            3.0,
            0.8,
            0.5,
            true,
            true,
            true,
            false
        )

        InfectedAgent.pCompliance = 0.75
        varyProcessingTime(tracingStrategy, 2.5)
    }

    @Test
    fun phoneAppContactTracing() {
        val tracingStrategy = ContactTracingStrategy(
            1.0,
            0.99,
            0.99,
            false,
            true,
            true,
            true
        )

        varyCompliance(tracingStrategy, 3.5)
    }



    fun varyCompliance(trackingStrategy: ContactTracingStrategy, R0: Double) {
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Pair<Double, Double>>()

        for (i in 0..5) {
            InfectedAgent.pCompliance = 0.5 + i * 0.1
            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
            println("${InfectedAgent.pCompliance} $pControl")
            data.add(Pair(InfectedAgent.pCompliance, pControl))
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
            trackingStrategy.processingTime = i * 0.5
            val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
            println("${trackingStrategy.processingTime} $pControl")
            data.add(Pair(trackingStrategy.processingTime, pControl))
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
}