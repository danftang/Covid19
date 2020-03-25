import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import kotlin.math.pow
import kotlin.random.Random

fun main() {
    for(i in 0..5) {
        var pTraceContact = i*0.2
        println("$pTraceContact ${pControl(pTraceContact, 300)}")
    }
}


fun pControl(pTraceContact: Double, nTrials: Int): Double {
    val initialCases = 20
    var nControlled = 0

    for(trial in 1..nTrials) {
        val sim = Simulation(pTraceContact, 1.5, 0.0)
        for(i in 1..initialCases) sim.addUndetectedCase(HellewellAgent(sim))
        if(sim.runHellewellSim()) nControlled++
    }
    return nControlled.toDouble()/nTrials
}

