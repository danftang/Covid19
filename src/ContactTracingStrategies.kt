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
//                if (familyMember.isSymptomatic(currentTime)) {
//                    familyMember.isolate()
//                } else {
//                    accessTestingCentre(familyMember)
//                }
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
}