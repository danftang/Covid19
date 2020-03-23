import java.util.*
import kotlin.random.Random

class Simulation(val tracingCapacity: Int) {
    val undetectedCases = HashSet<InfectedAgent>()
    val detectedCases = ArrayDeque<InfectedAgent>()
    val pDetectHousehold = 1.0
    val pDetectWorkplace = 0.8


    val nCases: Int
        get() = undetectedCases.size + detectedCases.size

    fun step() {
        val currentCases = undetectedCases.toList()
        currentCases.forEach { it.step(this) }
        var dailyTests = 0
        while(dailyTests++ < tracingCapacity && detectedCases.isNotEmpty()) {
            contactTrace(detectedCases.pollFirst())
        }
    }


    fun accessPrimaryCare(agent: InfectedAgent) {
        undetectedCases.remove(agent)
        detectedCases.addLast(agent)
    }


    fun addUndetectedCase(agent: InfectedAgent) {
        undetectedCases.add(agent)
    }


    fun contactTrace(agent: InfectedAgent) {
        agent.household.forEach { cohabiter ->
            if(Random.nextDouble() < pDetectHousehold) {
                quarrantine(cohabiter)
            }
        }
        agent.workplace.forEach { colleague ->
            if(Random.nextDouble() < pDetectWorkplace) {
                quarrantine(colleague)
            }
        }
    }

    fun quarrantine(agent: InfectedAgent) {
        if(undetectedCases.contains(agent)) {
            undetectedCases.remove(agent)
            detectedCases.addLast(agent)
        }
    }
}