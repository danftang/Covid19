import kotlin.random.Random

object ContactTracingStrategies {
    var swabTestTime: Double = 1.0
//    var delayToQuarrantine = 1.0
    var pTraceInWorkplace = 1.0
    var pTraceInCommunity = 0.5
    var testLimitOfDetection = 0.02     // TODO: Calibrate this


    fun noTracing(sim : Simulation, agent: InfectedAgent) {}

    fun householdQuarantine(sim : Simulation, agent: InfectedAgent) {
        agent.household.forEach { familyMember ->
            familyMember.quarantine()
        }
    }


    fun householdAndWorkQuarantine(sim : Simulation, agent: InfectedAgent) {
        agent.quarantine() // should be in quarantine anyway by now?
        if (agent.tracedVia !== agent.household) {
            agent.household.forEach { familyMember ->
                if(familyMember !== agent && familyMember.tracedVia == null) {
                    familyMember.tracedVia = agent.household
                    swabTest(sim, familyMember)
                    familyMember.quarantine()
                }
            }
        }

        if (agent.tracedVia !== agent.workplace) {
            agent.workplace.forEach { colleague ->
                if (colleague !== agent && colleague.tracedVia == null) {
                    colleague.tracedVia = agent.workplace
                    swabTest(sim, colleague)
                    colleague.quarantine()
                }
            }
        }
    }


    fun fullTrace(sim : Simulation, agent: InfectedAgent) {
        agent.quarantine() // should be in quarantine anyway by now?
        if (agent.tracedVia !== agent.household) {
            agent.household.forEach { familyMember ->
                if(familyMember !== agent && familyMember.tracedVia == null) {
                    familyMember.tracedVia = agent.household
                    swabTest(sim, familyMember)
                    familyMember.quarantine()
                }
            }
        }

        if (agent.tracedVia !== agent.workplace) {
            agent.workplace.forEach { colleague ->
                if (colleague !== agent && colleague.tracedVia == null && Random.nextDouble() < pTraceInWorkplace) {
                    colleague.tracedVia = agent.workplace
                    swabTest(sim, colleague)
                    colleague.quarantine()
                }
            }
        }

        agent.communityInfected.forEach { stranger ->
            if (stranger.tracedVia == null && Random.nextDouble() < pTraceInCommunity) {
                stranger.tracedVia = agent.communityInfected
                swabTest(sim, stranger)
                stranger.quarantine()
            }
        }
    }

    fun swabTest(sim: Simulation, agent: InfectedAgent) {
        if (InfectedAgent.infectiousness(sim.currentTime, agent.onsetTime) > testLimitOfDetection) {
            sim.events.add(Event(sim.currentTime + swabTestTime, Event.Type.TESTPOSITIVE, agent))
        }
    }


}