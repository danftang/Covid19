import kotlin.random.Random

class ContactTracingStrategy(
    var PCRTestResultTime: Double,
//    var workplaceProcessingTime: Double,             // time from a possible case being reported to quarantine of contacts if positive
//    var communityProcessingTime: Double,
//    var pTraceInWorkplace: Double,          // probability of being traced in the workplace
//    var pTraceInCommunity: Double,          // probability of beting traced in the community
    val enforceCommunityTracing: Boolean,   // e.g. force people to install app
    val enforceHouseholdTracing: Boolean,   // are household members forced into quarantine
    val enforceWorkplaceTracing: Boolean,
    val traceSecondaryContacts: Boolean
) {

    companion object {
        val testLimitOfDetection = 0.02         // sensitivity of swab test
        val pAntigenPositive = 0.85         // probability that an antigen test is positive given infection
//        var traceSecondaryContacts = true
    }


//    fun traceAgentsHousehold(sim: Simulation, agent: InfectedAgent, traceSecondaryContacts: Boolean) {
//        agent.household.forEach { familyMember ->
//            if (familyMember.isCompliant || enforceHouseholdTracing) {
//                reportPossibleCase(sim, familyMember, agent.household)
//                if(traceSecondaryContacts) initiateTrace(sim, familyMember, false)
//            }
//        }
//    }
//
//
//    fun traceAgentsWorkplace(sim: Simulation, agent: InfectedAgent, traceSecondaryContacts: Boolean) {
//        agent.workplaceContacts.forEach { colleague ->
//            if (colleague.isCompliant || enforceWorkplaceTracing) {
//                reportPossibleCase(sim, colleague, agent.workplaceContacts)
//                if(traceSecondaryContacts) initiateTrace(sim, colleague, false)
//            }
//        }
//    }
//
//
//    fun traceAgentsCommunity(sim: Simulation, agent: InfectedAgent, traceSecondaryContacts: Boolean) {
//        if(agent.isCompliant || enforceCommunityTracing) {
//            agent.communityContacts.forEach { stranger ->
//                if (stranger.isCompliant || enforceCommunityTracing) {
//                    reportPossibleCase(sim, stranger, agent.communityContacts)
//                    if(traceSecondaryContacts) initiateTrace(sim, stranger, false)
//                }
//            }
//        }
//    }


    fun initiateTrace(sim: Simulation, agent: InfectedAgent) {
        var nonHouseholdContacts = agent.traceNonHouseholdContacts(enforceWorkplaceTracing, enforceCommunityTracing)
        val householdContacts = if(agent.tracedVia !== agent.household) {
            agent.traceHoueholdContacts(enforceHouseholdTracing)
        } else emptySequence()
        householdContacts.forEach {
            reportPossibleCase(sim, it, agent.household)
        }
        nonHouseholdContacts.forEach {
            reportPossibleCase(sim, it, agent.communityContacts)
        }
        if(traceSecondaryContacts) {
            nonHouseholdContacts.forEach { primaryContact ->
                primaryContact.traceNonHouseholdContacts(enforceWorkplaceTracing, enforceCommunityTracing).forEach {
                    reportPossibleCase(sim, it, primaryContact.communityContacts)
                }
                primaryContact.traceHoueholdContacts(enforceHouseholdTracing).forEach {
                    reportPossibleCase(sim, it, primaryContact.household)
                }
            }
            householdContacts.forEach { primaryContact ->
                primaryContact.traceNonHouseholdContacts(enforceWorkplaceTracing, enforceCommunityTracing).forEach {
                    reportPossibleCase(sim, it, primaryContact.communityContacts)
                }
            }
        }
    }


    fun reportPossibleCase(sim: Simulation, agent: InfectedAgent, tracedVia: InfectionLocation) {
        if(agent.tracedVia != null) return // don't trace agents we've already traced
        agent.tracedVia = tracedVia
        agent.quarantine()
//        if(enforceHouseholdTracing) agent.traceHoueholdContacts(true).forEach {
//            reportPossibleCase(sim, it, agent.household)
//        }
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