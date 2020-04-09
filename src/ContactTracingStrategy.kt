import kotlin.random.Random

class ContactTracingStrategy(
    var householdProcessingTime: Double,
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
        val testLimitOfDetection = 0.02       // sensitivity of swab test
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


    fun reportPossibleCase(sim: Simulation, agent: InfectedAgent, tracedVia: InfectionLocation) {
        if(agent.tracedVia != null) return // don't trace agents we've already traced
        agent.tracedVia = tracedVia
        agent.quarantine()
        val wouldTestPositive = agent.infectiousness(sim.currentTime) > testLimitOfDetection
        if(agent.tracedVia !== agent.household) {
            if (symptomaticsQuarantineHouseholdImmediately && agent.isSymptomatic(sim.currentTime)) {
                traceAgentsHousehold(sim, agent)
            } else if (wouldTestPositive) {
                sim.events.add(Event(sim.currentTime + householdProcessingTime, Event.Type.TRACEHOUSEHOLD, agent))
            }
        }
        if (wouldTestPositive) {
            sim.events.add(Event(sim.currentTime + workplaceProcessingTime, Event.Type.TRACEWORKPLACE, agent))
            sim.events.add(Event(sim.currentTime + communityProcessingTime, Event.Type.TRACECOMMUNITY, agent))
        }
    }
}