import lib.gnuplot
import org.junit.Test
import kotlin.math.roundToInt

class Experiments {
    @Test
    fun baseline() {
        val tracingStrategy = ContactTracingParams(
            false,
            false,
            false,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }

    @Test
    fun WorkplaceSymptomMonitoring() {
        val tracingStrategy = ContactTracingParams(
            false,
            false,
            true,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }


    @Test
    fun workplaceEnforcement() {
        val tracingStrategy = ContactTracingParams(
            false,
            true,
            true,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }


    @Test
    fun householdEnforcement() {
        val tracingStrategy = ContactTracingParams(
            true,
            false,
            false,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }



    @Test
    fun householdEnforcementWithWorkplaceSymptomMonitoring() {
        val tracingStrategy = ContactTracingParams(
            true,
            false,
            true,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }


    @Test
    fun workplaceAndHouseholdEnforcement() {
        val tracingStrategy = ContactTracingParams(
            true,
            true,
            true,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 0.8
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }

    @Test
    fun totalCompliance() {
        val tracingStrategy = ContactTracingParams(
            false,
            false,
            false,
            0.02,
            0.85
        )
        val agentParams = AgentParams(
            pCompliant = 1.0
        )

        contourR0TotalAsymptomatic(tracingStrategy, agentParams)
    }




    fun contourR0TotalAsymptomatic(trackingParams: ContactTracingParams, agentParams: AgentParams) {
        val nTrials = 500
        val initialCases = 100
        val data = ArrayList<Triple<Double, Double, Double>>()
        val skews = listOf(100.0, 3.08, 1.38, 0.72, 0.32, 0.0)
        var jMax = 6

        for (i in 0..3) {
            for (j in 0..4) {
                val R0 = 2.5 + i * 0.5
                agentParams.pSubclinical = j * 0.1
                agentParams.generationIntervalShape = skews[j]
                var pControl = 0.0
                if (j < jMax) {
                    pControl = Simulation(trackingParams, agentParams, R0).monteCarloRun(nTrials, initialCases)
                    if (pControl == 0.0) jMax = j
                }
                val x = (R0 * 1024.0).roundToInt() / 1024.0
                val y = ((agentParams.pSubclinical + agentParams.pSubclinical*agentParams.subclinicalInfectiveness - agentParams.pSubclinical*agentParams.pSubclinical*agentParams.subclinicalInfectiveness)* 1024.0).roundToInt() / 1024.0
                println("$x $y $pControl")
                data.add(Triple(x, y, pControl))
            }
            println("")
        }

        gnuplot {
            val plotData = heredoc(data, 5)
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

    fun contourAsymptomaticPreSymptomatic(trackingStrategy: ContactTracingParams, agentParams: AgentParams, R0: Double) {
        val nTrials = 300
        val initialCases = 100
        val data = ArrayList<Triple<Double, Double, Double>>()
        val skews = listOf(100.0, 3.08, 1.38, 0.72, 0.32, 0.0)
        var jMax = 6

        for (i in 0..5) {
            for (j in 0..5) {
                agentParams.pSubclinical = i * 0.1
                agentParams.generationIntervalShape = skews[j]
                var pControl = 0.0
                if (j < jMax) {
                    pControl = Simulation(trackingStrategy, agentParams, R0).monteCarloRun(nTrials, initialCases)
                    if (pControl == 0.0) jMax = j
                }
                val x = (agentParams.pSubclinical * 1024.0).roundToInt() / 1024.0
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



}