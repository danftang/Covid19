import kotlin.random.Random

object ContactTracingStrategies {
    var swabTestTime: Double = 1.0
//    var contactTraceTime = 1.0
    var pTraceInWorkplace = 0.9
    var pTraceInCommunity = 0.1
    var testSensitivity = 0.02     // TODO: Calibrate this


    fun noTracing(sim : Simulation, agent: InfectedAgent) {}


    fun trace(sim : Simulation, agent: InfectedAgent) {
        val now = sim.currentTime
        agent.quarantine()
        if (agent.tracedVia !== agent.household) {
            agent.household.forEach { familyMember ->
                if(familyMember !== agent) {
                    familyMember.tracedVia = agent.household
                    swabTest(sim, familyMember)
                    familyMember.quarantine()
                }
            }
        }

        if (agent.tracedVia !== agent.workplace) {
            agent.workplace.forEach { colleague ->
                if (colleague !== agent && Random.nextDouble() < pTraceInWorkplace) {
                    colleague.tracedVia = agent.workplace
                    swabTest(sim, colleague)
                    if (colleague.isSymptomatic(now)) colleague.quarantine()
                }
            }
        }

        agent.communityInfected.forEach { stranger ->
            if (Random.nextDouble() < pTraceInCommunity) {
                stranger.tracedVia = agent.communityInfected
                swabTest(sim, stranger)
                if (stranger.isSymptomatic(now)) stranger.quarantine()
            }
        }
    }

    fun swabTest(sim: Simulation, agent: InfectedAgent) {
        if (InfectedAgent.infectiousness(sim.currentTime, agent.onsetTime) > testSensitivity) {
            sim.events.add(Event(sim.currentTime + swabTestTime, Event.Type.TESTPOSITIVE, agent))
        }
    }


}