import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import java.util.*
import kotlin.random.Random

// Agent as described in Hellewell et.al., 2020,
// https://doi.org/10.1016/S2214-109X(20)30074-7
class HellewellAgent: Agent {
    val R0 = 2.5
    val pSubclinical = 0.0

    val eventQueue = PriorityQueue<Event>()
    override val contacts = ArrayList<HellewellAgent>()
    override var isDetected = false

    constructor(currentTime: Double) {
        val symptomOnsetTime = incubationTime()
        val selfIsolationTime = if(!isSubclinical()) {
            eventQueue.add(Event(currentTime + symptomOnsetTime, Event.Type.BECOMESYMPTOMATIC, this))
            val selfIsolationTime = symptomOnsetToSelfIsolation() + symptomOnsetTime
            eventQueue.add(Event(currentTime + selfIsolationTime, Event.Type.SELFISOLATE, this))
            selfIsolationTime
        } else {
            Double.POSITIVE_INFINITY
        }
        for(infection in 1..numberOfInfected()) {
            val transmissionTime = infectionTime(symptomOnsetTime)
            if(transmissionTime < selfIsolationTime) {
                eventQueue.add(Event(currentTime + transmissionTime, Event.Type.TRANSMIT, this))
            }
        }
    }

    constructor(currentTime: Double, infector: HellewellAgent): this(currentTime) {
        contacts.add(infector)
    }

    override fun peekNextEvent(): Event? {
        return if(eventQueue.isEmpty()) null else eventQueue.peek()
    }

    override fun processNextEvent(sim: Simulation): Event? {
        val nextEvent = eventQueue.poll()
        return when(nextEvent.type) {
            Event.Type.TRANSMIT -> {
                val newInfectedAgent = HellewellAgent(sim.currentTime,this)
                contacts.add(newInfectedAgent)
                sim.addUndetectedCase(newInfectedAgent)
                peekNextEvent()
            }
            Event.Type.BECOMESYMPTOMATIC -> {
                if(isDetected) {
                    sim.accessPrimaryCare(this)
                    null
                } else {
                    peekNextEvent()
                }
            }
            Event.Type.SELFISOLATE -> {
                sim.accessPrimaryCare(this)
                null
            }
        }
    }


    fun numberOfInfected(): Int {
        return Random.nextNegativeBinomial(0.16, R0)
    }

    fun incubationTime(): Double {
        return Random.nextWeibull(2.32277, 6.492272)
    }

    fun infectionTime(incubationTime: Double): Double {
        // shape = 30, 1.95, 0.7 for <1%, 15% and 30% before symptom onset respectively
        return Random.nextSkewNormal(1.95, 2.0, incubationTime)
    }

    fun isSubclinical(): Boolean {
        return Random.nextDouble() < pSubclinical
    }

    fun symptomOnsetToSelfIsolation(): Double {
//        return Random.nextWeibull(2.5,5.0)
        // long delay: shape = 2.305172, scale = 9.483875
        return Random.nextWeibull(1.651524,4.287786)
    }

}