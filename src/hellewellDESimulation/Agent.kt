package hellewellDESimulation

import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

// Agent as described in Hellewell et.al., 2020,
// https://doi.org/10.1016/S2214-109X(20)30074-7
class Agent {

    val eventQueue = PriorityQueue<Event>()
    val contacts = ArrayList<Agent>()
    var isDetected = false
    var isSymptomatic = false

    constructor(sim: Simulation) {
        val incubationPeriod = incubationTime()
        val symptomOnsetTime = sim.currentTime + incubationPeriod
        val selfIsolationTime = if(!isSubclinical(sim)) {
            eventQueue.add(Event(symptomOnsetTime, Event.Type.BECOMESYMPTOMATIC, this))
            val selfIsolationTime =  symptomOnsetTime + symptomOnsetToSelfIsolation()
            eventQueue.add(Event(selfIsolationTime, Event.Type.SELFISOLATE, this))
            selfIsolationTime
        } else {
            Double.POSITIVE_INFINITY
        }
        for(infection in 1..numberOfInfected(sim)) {
            val transmissionTime = sim.currentTime + infectionTime(incubationPeriod)
            if(transmissionTime < selfIsolationTime) {
                eventQueue.add(Event(transmissionTime, Event.Type.TRANSMIT, this))
            }
        }
    }

    fun peekNextEvent(): Event? {
        return if(eventQueue.isEmpty()) null else eventQueue.peek()
    }


    fun processNextEvent(sim: Simulation): Event? {
        val nextEvent = eventQueue.poll()
        return when(nextEvent.type) {
            Event.Type.TRANSMIT -> {
                if(isSymptomatic && isDetected) {
                    null
                } else {
                    val newInfectedAgent = Agent(sim)
                    contacts.add(newInfectedAgent)
                    sim.addUndetectedCase(newInfectedAgent)
                    peekNextEvent()
                }
            }
            Event.Type.BECOMESYMPTOMATIC -> {
                isSymptomatic = true
                if(isDetected) {
                    sim.accessPrimaryCare(this)
                    null // Don't send next event to hellewellDESimulation.main event loop, stops further infection
                } else {
                    peekNextEvent()
                }
            }
            Event.Type.SELFISOLATE -> {
                sim.accessPrimaryCare(this)
                null // Don't send next event to hellewellDESimulation.main event loop, stops further infection
            }
        }
    }


    fun numberOfInfected(sim: Simulation): Int {
        return Random.nextNegativeBinomial(0.16, sim.R0)
    }

    fun incubationTime(): Double {
        return Random.nextWeibull(2.322737, 6.492272)
    }

    fun infectionTime(incubationTime: Double): Double {
        // shape = 30, 1.95, 0.7 for <1%, 15% and 30% before symptom onset respectively
//        return Random.nextSkewNormal(1.95, 2.0, incubationTime)
        val t = Random.nextSkewNormal(1.95, 2.0, incubationTime)
        return if(t<1.0) 1.0 else t
    }

    fun isSubclinical(sim: Simulation): Boolean {
        return kotlin.random.Random.nextDouble() < sim.pSubclinical
    }

    fun symptomOnsetToSelfIsolation(): Double {
//        return Random.nextWeibull(2.5,5.0)
        // long delay: shape = 2.305172, scale = 9.483875
        return Random.nextWeibull(1.651524,4.287786)
    }

}