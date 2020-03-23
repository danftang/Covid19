import java.util.*
import kotlin.random.Random

class Simulation(val pTraceContact: Double) {
    val detectedCases = ArrayDeque<Agent>()
    val events = PriorityQueue<Event>()
    var currentTime = 0.0
    var lastInfectionTime = 0.0

    val nCases: Int
        get() = events.size + detectedCases.size

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
        } else {
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
            if(Random.nextDouble() < pTraceContact) {
                if(!contact.isDetected) {
                    contact.isDetected = true
                    detectedCases.addLast(contact)
                }
            }
        }
    }

}