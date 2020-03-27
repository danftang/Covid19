
fun main(args: Array<String>) {
    varyExposureToPositiveTestTime()
}

fun defaultParams() {
    val nTrials = 100
    val R0 = 2.4
    val initialCases = 100

    var nControlled = 0
    for (trial in 1..nTrials) {
        val sim = Simulation(ContactTracingStrategies::allLocations, R0)
        for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
        if (sim.run()) nControlled++
    }
    val pControl = nControlled.toDouble() / nTrials
    println("$pControl")
    println("${InfectedAgent.nHome} ${InfectedAgent.nWork} ${InfectedAgent.nCommunity}")
}

fun varyOnsetToIsolationTime() {
    val nTrials = 300
    val R0 = 2.4
    val initialCases = 100

    for(i in 0..4) {
        InfectedAgent.onsetToIsolation = i*0.5
        var nControlled = 0
        for (trial in 1..nTrials) {
            val sim = Simulation(ContactTracingStrategies::allLocations, R0)
            for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
            if (sim.run()) nControlled++
        }
        val pControl = nControlled.toDouble() / nTrials
        println("${InfectedAgent.onsetToIsolation} $pControl")
    }
}

fun varyExposureToPositiveTestTime() {
    val nTrials = 300
    val R0 = 2.4
    val initialCases = 100

    InfectedAgent.onsetToIsolation = 1.0
    for(i in 0..10) {
        Simulation.exposureToPositiveTestTime = i*0.5
        var nControlled = 0
        for (trial in 1..nTrials) {
            val sim = Simulation(ContactTracingStrategies::allLocations, R0)
            for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
            if (sim.run()) nControlled++
        }
        val pControl = nControlled.toDouble() / nTrials
        println("${Simulation.exposureToPositiveTestTime} $pControl")
    }

}