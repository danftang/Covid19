
fun main(args: Array<String>) {
    val nTrials = 1
    val R0 = 2.4
    val initialCases = 20

    var nControlled = 0
    for (trial in 1..nTrials) {
        val sim = Simulation(ContactTracingStrategies::noTracing, R0)
        for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
        if (sim.run()) nControlled++
    }
    val pControl = nControlled.toDouble() / nTrials
    println("$pControl")
    println("${InfectedAgent.nHome} ${InfectedAgent.nWork} ${InfectedAgent.nCommunity}")

}