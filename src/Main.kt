import lib.gnuplot

fun main(args: Array<String>) {
    InfectedAgent.pCompliance = 0.9
//    varyPTraceInCommunity()
}


//fun varyOnsetToIsolationTime(trackingStrategy: (Simulation, InfectedAgent) -> Unit = ContactTracingStrategies::fullTrace) {
//    val nTrials = 300
//    val R0 = 2.4
//    val initialCases = 100
//    val data = ArrayList<Pair<Double,Double>>()
//
//    for(i in 0..4) {
//        InfectedAgent.onsetToIsolation = i*0.5
//        val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
//        println("${InfectedAgent.onsetToIsolation} $pControl")
//        data.add(Pair(InfectedAgent.onsetToIsolation, pControl))
//    }
//    gnuplot {
//        val plotData = heredoc(data)
//        invoke("""
//            set xlabel "Onset to Isolation time (days)"
//            set ylabel "Probability of control"
//            plot $plotData with lines notitle
//        """)
//    }
//}


fun varyCompliance(trackingStrategy: ContactTracingStrategy) {
    val nTrials = 300
    val R0 = 2.4
    val initialCases = 100
    val data = ArrayList<Pair<Double,Double>>()

//    InfectedAgent.onsetToIsolation = 0.5
    for(i in 0..5) {
        InfectedAgent.pCompliance = 0.5 + i*0.1
        val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
        println("${InfectedAgent.pCompliance} $pControl")
        data.add(Pair(InfectedAgent.pCompliance, pControl))
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


fun varyR0(trackingStrategy: ContactTracingStrategy) {
    val nTrials = 300
    val initialCases = 100
    val data = ArrayList<Pair<Double,Double>>()

    for(i in 0..4) {
        val R0 = 2.0 + i*0.5
        val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
        println("${R0} $pControl")
        data.add(Pair(R0, pControl))
    }
    gnuplot {
        val plotData = heredoc(data)
        invoke("""
            set xlabel "R0"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """)
    }
}


fun varyPTraceInCommunity(trackingStrategy: ContactTracingStrategy) {
    val nTrials = 300
    val initialCases = 100
    val R0 = 3.5
    val data = ArrayList<Pair<Double,Double>>()

    for(i in 0..5) {
        ContactTracingStrategies.pTraceInCommunity = i * 0.2
        val pControl = Simulation(trackingStrategy, R0).monteCarloRun(nTrials, initialCases)
        println("${ContactTracingStrategies.pTraceInCommunity} $pControl")
        data.add(Pair(ContactTracingStrategies.pTraceInCommunity, pControl))
    }
    gnuplot {
        val plotData = heredoc(data)
        invoke("""
            set xlabel "Compliance through community tracing"
            set ylabel "Probability of control"
            plot $plotData with lines notitle
        """)
    }
}
