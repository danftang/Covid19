import kotlin.random.Random

class ContactTracingStrategy(
    var processingTime: Double,             // time from a possible case being reported to quarantine of contacts if positive
    val pTraceInWorkplace: Double,          // probability of being traced in the workplace
    val pTraceInCommunity: Double,          // probability of beting traced in the community
    val enforceCommunityTracing: Boolean,   // e.g. force people to install app
    val enforceHouseholdTracing: Boolean,   // are household members forced into quarantine
    val enforceWorkplaceTracing: Boolean,
    val quarantineHouseholdImmediately: Boolean
) {

    companion object {
        val testLimitOfDetection = 0.02       // sensitivity of swab test
    }

    fun processConfirmedCase(sim: Simulation, agent: InfectedAgent) {
        if (agent.tracedVia !== agent.household && !quarantineHouseholdImmediately) {
            agent.household.forEach { familyMember ->
                if (familyMember.isCompliant || enforceHouseholdTracing) {
                    reportPossibleCase(sim, familyMember, agent.household)
                }
            }
        }

        if (agent.tracedVia !== agent.workplace) {
            agent.workplace.forEach { colleague ->
                if ((colleague.isCompliant || enforceWorkplaceTracing) && Random.nextDouble() < pTraceInWorkplace) {
                    reportPossibleCase(sim, colleague, agent.workplace)
                }
            }
        }

        if(agent.isCompliant || enforceCommunityTracing) {
            agent.communityInfected.forEach { stranger ->
                if ((stranger.isCompliant || enforceCommunityTracing) && Random.nextDouble() < pTraceInCommunity) {
                    reportPossibleCase(sim, stranger, agent.communityInfected)
                }
            }
        }
    }

    fun reportPossibleCase(sim: Simulation, agent: InfectedAgent, tracedVia: InfectionLocation) {
        if(agent.tracedVia != null) return // don't trace agents we've already traced
        agent.tracedVia = tracedVia
        agent.quarantine()
        if(quarantineHouseholdImmediately && agent.tracedVia !== agent.household) {
            agent.household.forEach { familyMember ->
                if (familyMember.isCompliant || enforceHouseholdTracing) {
                    reportPossibleCase(sim, familyMember, agent.household)
                }
            }
        }
        if (agent.infectiousness(sim.currentTime) > testLimitOfDetection) {
            sim.events.add(Event(sim.currentTime + processingTime, Event.Type.TESTPOSITIVE, agent))
        }

    }
}