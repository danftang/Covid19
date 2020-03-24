import java.util.*
import kotlin.random.Random

class Simulation(val pTraceContact: Double) {
    val detectedCases = ArrayDeque<Agent>()
    val events = PriorityQueue<Event>()
    var currentTime = 0.0
    var lastInfectionTime = 0.0

    fun step() {

        if(events.isNotEmpty()) {
            val event = events.poll()
            while(detectedCases.isNotEmpty()) {
                contactTrace(detectedCases.pollFirst())
            }
            currentTime = event.time
            val nextEvent = event.agent.processNextEvent(this)
            if(nextEvent != null) {
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
        lastInfectionTime = currentTime
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