package hellewellDESimulation

fun main() {
    val nTrials = 100
    val R0 = 1.5
    val initialCases = 20
    val pSubclinical = 0.0

    for(i in 0..5) {
        val pTraceContact = i*0.2
        var nControlled = 0
        for(trial in 1..nTrials) {
            val sim = Simulation(pTraceContact, R0, pSubclinical)
            for(i in 1..initialCases) sim.addUndetectedCase(Agent(sim))
            if(sim.run()) nControlled++
        }
        val pControl = nControlled.toDouble()/nTrials
        println("$pTraceContact $pControl")
    }
}

