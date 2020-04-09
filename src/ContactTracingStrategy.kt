import kotlin.random.Random

class ContactTracingStrategy(
    var PCRTestResultTime: Double,
    var workplaceProcessingTime: Double,             // time from a possible case being reported to quarantine of contacts if positive
    var communityProcessingTime: Double,
    var pTraceInWorkplace: Double,          // probability of being traced in the workplace
    var pTraceInCommunity: Double,          // probability of beting traced in the community
    val enforceCommunityTracing: Boolean,   // e.g. force people to install app
    val enforceHouseholdTracing: Boolean,   // are household members forced into quarantine
    val enforceWorkplaceTracing: Boolean,
    val symptomaticsQuarantineHouseholdImmediately: Boolean
) {

    companion object {
        val testLimitOfDetection = 0.02         // sensitivity of swab test
        val pAntigenPositive = 0.85         // probability that an antigen test is positive given infection
    }


    fun traceAgentsHousehold(sim: Simulation, agent: InfectedAgent) {
        agent.household.forEach { familyMember ->
            if (familyMember.isCompliant || enforceHouseholdTracing) {
                reportPossibleCase(sim, familyMember, agent.household)
            }
        }
    }


    fun traceAgentsWorkplace(sim: Simulation, agent: InfectedAgent) {
        agent.workplaceContacts.forEach { colleague ->
            if ((colleague.isCompliant || enforceWorkplaceTracing) && Random.nextDouble() < pTraceInWorkplace) {
                reportPossibleCase(sim, colleague, agent.workplaceContacts)
            }
        }
    }


    fun traceAgentsCommunity(sim: Simulation, agent: InfectedAgent) {
        if(agent.isCompliant || enforceCommunityTracing) {
            agent.communityContacts.forEach { stranger ->
                if ((stranger.isCompliant || enforceCommunityTracing) && Random.nextDouble() < pTraceInCommunity) {
                    reportPossibleCase(sim, stranger, agent.communityContacts)
                }
            }
        }
    }

    fun initiateTrace(sim: Simulation, agent: InfectedAgent) {
        if(agent.tracedVia !== agent.household) traceAgentsHousehold(sim, agent)
        if(workplaceProcessingTime == 0.0) {
            traceAgentsWorkplace(sim, agent)
        } else {
            sim.events.add(Event(sim.currentTime + workplaceProcessingTime, Event.Type.TRACEWORKPLACE, agent))
        }
        if(communityProcessingTime == 0.0) {
            traceAgentsCommunity(sim, agent)
        } else {
            sim.events.add(Event(sim.currentTime + communityProcessingTime, Event.Type.TRACECOMMUNITY, agent))
        }
    }


    fun reportPossibleCase(sim: Simulation, agent: InfectedAgent, tracedVia: InfectionLocation) {
        if(agent.tracedVia != null) return // don't trace agents we've already traced
        agent.tracedVia = tracedVia
        agent.quarantine()
        if(symptomaticsQuarantineHouseholdImmediately && agent.tracedVia !== agent.household && agent.isSymptomatic(sim.currentTime)) {
            traceAgentsHousehold(sim, agent)
        }
        doSwabTests(sim, agent)
    }

    fun doSwabTests(sim: Simulation, agent: InfectedAgent) {
        if(agent.hasTestedPositive) return
        val virusIsDetectable = agent.infectiousness(sim.currentTime) > testLimitOfDetection || sim.currentTime > agent.onsetTime
        if(virusIsDetectable) {
            if(Random.nextDouble() < pAntigenPositive) {
                agent.hasTestedPositive = true
                initiateTrace(sim, agent)
            } else {
                sim.events.add(Event(sim.currentTime + PCRTestResultTime, Event.Type.PCRTESTPOSITIVE, agent))
            }
        }
    }
}