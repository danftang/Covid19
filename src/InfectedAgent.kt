import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import java.lang.IllegalStateException
import java.util.*
import kotlin.random.Random

class InfectedAgent {
    companion object {
        // TODO: Calibrate this
        val familyMemberInteractionWeight = 10.0
    }

    val eventQueue = PriorityQueue<Event>()
    var isDetected = false
    val onsetTime: Double
    val isSubclinical: Boolean
    val household: Household
    val workplace: Workplace
//    val communityInfected = ArrayList<InfectedAgent>()
    val isIsolated: Boolean
        get() = eventQueue.isEmpty()

    constructor(sim: Simulation, household: Household = Household(), workplace: Workplace = Workplace()) {
        this.household = household
        this.workplace = workplace
        household.add(this)
        workplace.add(this)
        val incubationPeriod = incubationTime()
        onsetTime = sim.currentTime + incubationPeriod
        isSubclinical = isSubclinical(sim)
        val selfIsolationTime = if(!isSubclinical) {
//            eventQueue.add(Event(onsetTime, Event.Type.BECOMESYMPTOMATIC, this))
            val selfIsolationTime =  onsetTime + symptomOnsetToSelfIsolation()
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

    fun isolate() {
        eventQueue.clear()
    }

    fun isSymptomatic(time: Double) = !isSubclinical && time > onsetTime

    fun peekNextEvent(): Event? {
        return if(eventQueue.isEmpty()) null else eventQueue.peek()
    }


    fun processNextEvent(sim: Simulation): Event? {
        if(eventQueue.isEmpty()) return null
        val nextEvent = eventQueue.poll()
        return when(nextEvent.type) {
            Event.Type.TRANSMIT -> {
                val newInfectedAgent = transmitInfection(sim)
                sim.addUndetectedCase(newInfectedAgent)
                peekNextEvent()
            }

            Event.Type.SELFISOLATE -> {
                sim.accessTestingCentre(this)
                isolate()
                null
            }

            else -> throw(IllegalStateException("Unrecognized event"))
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
        return Random.nextDouble() < sim.pSubclinical
    }

    fun symptomOnsetToSelfIsolation(): Double {
//        return Random.nextWeibull(2.5,5.0)
        // long delay: shape = 2.305172, scale = 9.483875
//        return Random.nextWeibull(1.651524,4.287786)
        return 1.5
    }

    fun transmitInfection(sim: Simulation): InfectedAgent {
        val homeWeight = household.nUninfected() * familyMemberInteractionWeight
        val totalWeight = homeWeight + 2.0
        val choice = Random.nextDouble(0.0, totalWeight).toInt()
        return when(choice) {
            0 -> InfectedAgent(sim)
            1 -> {
                InfectedAgent(sim, workplace = this.workplace)
            }
            else -> {
                InfectedAgent(sim, household = this.household)
            }
        }
    }
}
