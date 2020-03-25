import java.util.*
import kotlin.random.Random

class Simulation(val pTraceContact: Double, val R0: Double, val pSubclinical: Double) {

    val detectedCases = ArrayDeque<Agent>()
    val events = PriorityQueue<Event>()
    var currentTime = 0.0
    var lastOnsetTime = 0.0
    var cumulativeCases = 0

    fun runHellewellSim(): Boolean {
        val maxOnset = 12.0*7.0

        while(lastOnsetTime < maxOnset && cumulativeCases < 5000 && events.isNotEmpty()) {
            step()
        }
        return (lastOnsetTime < 12.0*7.0 && cumulativeCases < 5000)
    }

    fun step() {
        if(events.isNotEmpty()) {
            val event = events.poll()
            while(detectedCases.isNotEmpty()) {
                contactTrace(detectedCases.pollFirst())
            }
            currentTime = event.time
            val nextEvent = event.agent.processNextEvent(this)
            if(nextEvent != null) {
                if(nextEvent.type == Event.Type.BECOMESYMPTOMATIC) lastOnsetTime = currentTime
                events.add(nextEvent)
            }
        }
        else {
            while(detectedCases.isNotEmpty()) {
                contactTrace(detectedCases.pollFirst())
            }
        }
    }


    fun accessPrimaryCare(agent: Agent) {
        detectedCases.addLast(agent)
    }


    fun addUndetectedCase(agent: Agent) {
        cumulativeCases++
        val firstEvent = agent.peekNextEvent()
        if(firstEvent != null) events.add(firstEvent)
    }


    fun contactTrace(agent: Agent) {
        agent.contacts.forEach { contact ->
            if(!contact.isDetected && Random.nextDouble() < pTraceContact) {
                contact.isDetected = true
                detectedCases.addLast(contact)
            }
        }
    }

}