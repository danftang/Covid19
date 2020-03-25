
fun main() {
    val nTrials = 10
    val R0 = 2.5
    val initialCases = 20
    val pSubclinical = 0.1
    val pTraceInWorkplace = 1.0

    var nControlled = 0
    for (trial in 1..nTrials) {
        val sim = Simulation(pTraceInWorkplace, R0, pSubclinical)
        for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
        if (sim.run()) nControlled++
    }
    val pControl = nControlled.toDouble() / nTrials
    println("$pTraceInWorkplace $pControl")

}