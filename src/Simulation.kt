import java.util.*
import kotlin.random.Random

// Branching process model of Covid-19 transmission based on that described in
// Hellewell et.al., 2020, Feasibility of controlling COVID-19 outbreaks by isolation of
// cases and contacts. The Lancet, 8:e488-96
// https://doi.org/10.1016/S2214-109X(20)30074-7
class Simulation(val contactTrace: (Simulation, InfectedAgent)->Unit, val R0: Double) {

    val events = PriorityQueue<Event>()
    var currentTime = 0.0
    var cumulativeCases = 0

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
            when(event.type) {

                Event.Type.TESTPOSITIVE -> {
                    contactTrace(this, event.agent)
//                    if(!event.agent.testedPositive && !event.agent.isIsolated) {
//                        event.agent.testedPositive = true
//                        contactTrace(this, event.agent)
//                    }
                }

//                Event.Type.CONTACTTRACE -> {
////                    println("Got contact trace event")
//                    contactTrace(this, event.agent)
//                }

                else -> {
                    val nextEvent = event.agent.processNextEvent(this)
                    if(nextEvent != null) events.add(nextEvent)
                }
            }
        }
    }


//    fun contactTrace(agent: InfectedAgent) {
//        // test all in household
//        agent.isolate()
//        agent.household.forEach { familyMember ->
//            if(!familyMember.isIsolated) {
//
//                accessTestingCentre(familyMember)
//                familyMember.isolate()
////                if (familyMember.isSymptomatic(currentTime)) {
////                    familyMember.isolate()
////                } else {
////                    accessTestingCentre(familyMember)
////                }
//            }
//        }
//
//        // isolate all symptomatic work colleagues and test some proportion of others
//        agent.workplace.forEach { workColleague ->
//            if(!workColleague.isIsolated) {
//                if(workColleague.isSymptomatic(currentTime)) {
//                    accessTestingCentre(workColleague)
//                    workColleague.isolate()
//                } else {
//                    if(Random.nextDouble() < pTraceInWorkplace) accessTestingCentre(workColleague)
//                }
//            }
//        }
//    }

    fun selfReport(agent: InfectedAgent) {
        contactTrace(this, agent)
    }


    fun addUndetectedCase(agent: InfectedAgent) {
        cumulativeCases++
        val firstEvent = agent.peekNextEvent()
        if(firstEvent != null) events.add(firstEvent)
    }

}