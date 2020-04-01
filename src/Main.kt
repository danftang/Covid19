import lib.gnuplot

fun main(args: Array<String>) {
    varyCompliance(ContactTracingStrategies::trace)
}

fun defaultParams() {
    val nTrials = 100
    val R0 = 2.4
    val initialCases = 100

    var nControlled = 0
    for (trial in 1..nTrials) {
        val sim = Simulation(ContactTracingStrategies::trace, R0)
        for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
        if (sim.run()) nControlled++
    }
    val pControl = nControlled.toDouble() / nTrials
    println("$pControl")
    println("${InfectedAgent.nHome} ${InfectedAgent.nWork} ${InfectedAgent.nCommunity}")
}

fun varyOnsetToIsolationTime(trackingStrategy: (Simulation, InfectedAgent) -> Unit = ContactTracingStrategies::trace) {
    val nTrials = 300
    val R0 = 2.4
    val initialCases = 100
    val data = ArrayList<Pair<Double,Double>>()

    for(i in 0..4) {
        InfectedAgent.onsetToIsolation = i*0.5
        var nControlled = 0
        for (trial in 1..nTrials) {
            val sim = Simulation(trackingStrategy, R0)
            for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
            if (sim.run()) nControlled++
        }
        val pControl = nControlled.toDouble() / nTrials
        println("${InfectedAgent.onsetToIsolation} $pControl")
        data.add(Pair(InfectedAgent.onsetToIsolation, pControl))
    }
    gnuplot {
        val plotData = heredoc(data)
        invoke("""
            set xlabel "Onset to Isolation time (days)"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """)
    }
}

fun varyCompliance(trackingStrategy: (Simulation, InfectedAgent) -> Unit = ContactTracingStrategies::trace) {
    val nTrials = 300
    val R0 = 2.4
    val initialCases = 100
    val data = ArrayList<Pair<Double,Double>>()

    InfectedAgent.onsetToIsolation = 0.5
    for(i in 0..5) {
        InfectedAgent.compliance = 0.5 + i*0.1
        var nControlled = 0
        for (trial in 1..nTrials) {
            val sim = Simulation(trackingStrategy, R0)
            for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
            if (sim.run()) nControlled++
        }
        val pControl = nControlled.toDouble() / nTrials
        println("${InfectedAgent.compliance} $pControl")
        data.add(Pair(InfectedAgent.compliance, pControl))
    }
    gnuplot {
        val plotData = heredoc(data)
        invoke("""
            set xlabel "Proportion of population compliant"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """)
    }
}


//fun varyExposureToPositiveTestTime() {
//    val nTrials = 300
//    val R0 = 2.4
//    val initialCases = 100
//
//    InfectedAgent.onsetToIsolation = 1.0
//    for(i in 0..10) {
//        Simulation.exposureToPositiveTestTime = i*0.5
//        var nControlled = 0
//        for (trial in 1..nTrials) {
//            val sim = Simulation(ContactTracingStrategies::trace, R0)
//            for (i in 1..initialCases) sim.addUndetectedCase(InfectedAgent(sim))
//            if (sim.run()) nControlled++
//        }
//        val pControl = nControlled.toDouble() / nTrials
//        println("${Simulation.exposureToPositiveTestTime} $pControl")
//    }
//
//}