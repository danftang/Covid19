import kotlin.random.Random

object ContactTracingStrategies {

    fun noTracing(sim : Simulation, agent: InfectedAgent) {}

    fun householdAndWork(sim : Simulation, agent: InfectedAgent) {
        val pTraceInWorkplace = 0.9

        // test all in household
        agent.isolate()
        agent.household.forEach { familyMember ->
            if(!familyMember.isIsolated) {
                sim.accessTestingCentre(familyMember)
                familyMember.isolate()
            }
        }

        // isolate all symptomatic work colleagues and test some proportion of others
        agent.workplace.forEach { workColleague ->
            if(!workColleague.isIsolated) {
                if(workColleague.isSymptomatic(sim.currentTime)) {
                    sim.accessTestingCentre(workColleague)
                    workColleague.isolate()
                } else {
                    if(Random.nextDouble() < pTraceInWorkplace) sim.accessTestingCentre(workColleague)
                }
            }
        }
    }

    fun allLocations(sim : Simulation, agent: InfectedAgent) {
        val pTraceInWorkplace = 0.9
        val pTraceInCommunity = 0.1

        // test all in household
        agent.isolate()
        agent.household.forEach { familyMember ->
            if(!familyMember.isIsolated) {
                sim.accessTestingCentre(familyMember)
                familyMember.isolate()
            }
        }

        // isolate all symptomatic work colleagues and test some proportion of others
        agent.workplace.forEach { workColleague ->
            if(!workColleague.isIsolated) {
                if(workColleague.isSymptomatic(sim.currentTime)) {
                    sim.accessTestingCentre(workColleague)
                    workColleague.isolate()
                } else {
                    if(Random.nextDouble() < pTraceInWorkplace) sim.accessTestingCentre(workColleague)
                }
            }
        }

        // isolate all symptomatic work colleagues and test some proportion of others
        agent.communityInfected.forEach { stranger ->
            if(!stranger.isIsolated) {
                if(Random.nextDouble() < pTraceInCommunity) sim.accessTestingCentre(stranger)
            }
        }

    }

}