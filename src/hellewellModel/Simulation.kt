package hellewellModel

import org.junit.Test
import kotlin.math.max

object Simulation {
//    var pMissed: Double = 0.2
//    var pAsymptomatic = 0.0
//    var R0 = 2.5
    var maxOnsetTime = 12.0*7.0
    var maxCases = 5000
    var nInitialAgents = 20

    fun run(initialCases: List<Agent>): Boolean {
        var lastOnsetTime = 0.0
        var totalCases = initialCases.size
        var cases = initialCases
        while(lastOnsetTime < maxOnsetTime && totalCases < maxCases && cases.isNotEmpty()) {
            cases = cases.flatMap {
                val newCases = it.spawnNewCases()
                lastOnsetTime = max(
                    lastOnsetTime,
                    newCases.map { it.exposureTime + it.incubationPeriod }.max()?:0.0
                )
                newCases
            }
            totalCases += cases.size
        }
        return cases.isEmpty()
    }


    fun pControl(nTrials: Int): Double {
        var nControlled = 0
        for (trial in 1..nTrials) {
            val initialAgents = Array(nInitialAgents) {
                Agent(0.0, Double.POSITIVE_INFINITY)
            }.toList()
            val controlled = run(initialAgents)
            if(controlled) nControlled++
        }
        return nControlled.toDouble()/nTrials
    }
}