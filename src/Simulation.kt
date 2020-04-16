import java.util.*

// Branching process model of Covid-19 transmission based on that described in
// Hellewell et.al., 2020, Feasibility of controlling COVID-19 outbreaks by isolation of
// cases and contacts. The Lancet, 8:e488-96
// https://doi.org/10.1016/S2214-109X(20)30074-7
class Simulation(val contactTrace: ContactTracingParams, val agentParams: AgentParams, val R0: Double) {

    val events = PriorityQueue<Event>()
    var currentTime = 0.0
    var cumulativeCases = 0


    fun monteCarloRun(nTrials: Int, nInitialCases: Int): Double {
        var nControlled = 0
        for (trial in 1..nTrials) {
            for (i in 1..nInitialCases) InfectedAgent(this)
            if (run()) nControlled++
            reset()
        }
        return nControlled.toDouble() / nTrials
    }


    fun run(): Boolean {
        val maxTime = 15.0*7.0

        while(currentTime < maxTime && cumulativeCases < 5000 && events.isNotEmpty()) {
            step()
        }
        return (currentTime < maxTime && cumulativeCases < 5000)
    }


    fun step() {
        if(events.isNotEmpty()) {
            val event = events.poll()
            currentTime = event.time
            event.agent.processNextEvent(event)
        }
    }


    fun reset() {
        events.clear()
        currentTime = 0.0
        cumulativeCases = 0
    }
}