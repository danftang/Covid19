import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import kotlin.math.pow
import kotlin.random.Random

fun main() {
    for(i in 0..5) {
        var pTraceContact = i*0.2
        println("$pTraceContact ${pControl(pTraceContact)}")
    }
}


fun pControl(pTraceContact: Double): Double {
    var nControlled = 0
    val nTrials = 20
    for(trial in 1..nTrials) {
        val sim = Simulation(pTraceContact)
        val initialCases = 20
        val nDays = 112

        for(i in 1..initialCases) sim.addUndetectedCase(HellewellAgent(0.0))
        while(sim.currentTime < nDays && sim.events.size < 5000 && sim.events.isNotEmpty()) {
            sim.step()
        }
        if(sim.lastInfectionTime < 12.0*7.0 && sim.events.size < 5000) {
            nControlled++
//            println("Controlled")
        } else {
//            println("out of control")
        }
    }
    return nControlled.toDouble()/nTrials
}

