import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import java.lang.IllegalStateException
import java.util.*
import kotlin.random.Random

class InfectedAgent {
    companion object {
        // Given a transmission event, how many times more probable
        // was that event in the infected's household
        // compared to in the community
        // TODO: Calibrate this
        val familyMemberInteractionWeight = 10.0

        // Probability that a person will not develop symptoms
        // given that they are infected.
        // Mizumoto et.al., 2020, Estimating the asymptomatic proportion of coronavirus
        // disease 2019 (COVID-19) cases on board the Diamond
        // Princess cruise ship, Yokohama, Japan,
        // Mizumoto Kenji et.al.,2020, Estimating the asymptomatic proportion of coronavirus
        // disease 2019 (COVID-19) cases on board the Diamond Princess cruise ship, Yokohama,
        // Japan, 2020. Euro Surveill. 2020;25(10):pii=2000180.
        // https://doi.org/10.2807/1560-7917.ES.2020.25.10.2000180
        val pSubclinical = 0.179

        // Sample from the total number of people a person will infect over
        // the course of the disease
        // Lloyd-Smith Et.al., 2005, Superspreading and the effect of individual
        // variation on disease emergence. Nature, 438:17
        // doi:10.1038/nature04153
        fun numberOfInfected(sim: Simulation): Int {
            return Random.nextNegativeBinomial(0.16, sim.R0)
        }


        // Sample from time from first exposure to onset of symptoms (if subclinical,
        // there are no symptoms but this value still anchors infectivity
        // over time).
        // Backer et.al., 2020, The incubation period of 2019-nCoV infections
        // among travellers from Wuhan, China. MedRxiv
        // https://doi.org/10.1101/2020.01.27.20018986
        fun incubationTime(): Double {
            return Random.nextWeibull(2.322737, 6.492272)
        }


        // Sample from time from first exposure to infection of another person.
        // Hellewell et.al., 2020, Feasibility of controlling COVID-19 outbreaks by isolation of
        // cases and contacts. The Lancet, 8:e488-96
        // https://doi.org/10.1016/S2214-109X(20)30074-7
        fun exposureToTransmissionTime(incubationTime: Double): Double {
            // shape = 30, 1.95, 0.7 for <1%, 15% and 30% before symptom onset respectively
//        return Random.nextSkewNormal(1.95, 2.0, incubationTime)
            val t = Random.nextSkewNormal(1.95, 2.0, incubationTime)
            return if(t<1.0) 1.0 else t
        }


        // Sample whether a person will develop clinical symptoms.
        fun isSubclinical(): Boolean {
            return Random.nextDouble() < pSubclinical
        }


        // Sample from time from first symtoms to self-isolation
        // Donnelly CA, Ghani AC, Leung GM, et al. Epidemiological
        // determinants of spread of causal agent of severe acute respiratory
        // syndrome in Hong Kong. Lancet 2003; 361: 1761–66
        fun symptomOnsetToSelfIsolationTime(): Double {
//        return Random.nextWeibull(2.5,5.0)
            // long delay: shape = 2.305172, scale = 9.483875
//        return Random.nextWeibull(1.651524,4.287786)
            return if(Random.nextDouble() < 0.9) 1.0 else 10.0
        }

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
        isSubclinical = isSubclinical()
        val selfIsolationTime = if(!isSubclinical) {
//            eventQueue.add(Event(onsetTime, Event.Type.BECOMESYMPTOMATIC, this))
            val selfIsolationTime =  onsetTime + symptomOnsetToSelfIsolationTime()
            eventQueue.add(Event(selfIsolationTime, Event.Type.SELFISOLATE, this))
            selfIsolationTime
        } else {
            Double.POSITIVE_INFINITY
        }
        for(infection in 1..numberOfInfected(sim)) {
            val transmissionTime = sim.currentTime + exposureToTransmissionTime(incubationPeriod)
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
