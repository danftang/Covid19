import java.lang.IllegalStateException
import kotlin.random.Random

class InfectedAgent {
    companion object {
        var nHome = 0
        var nWork = 0
        var nCommunity = 0
    }

    val sim: Simulation
    val onsetTime: Double
    val exposureTime: Double
    val isSubclinical: Boolean
    val isCompliant: Boolean
    val isUnemployed: Boolean
    val hasSmartPhone: Boolean
    val household: Household
//    val workplace: Workplace
    val closeContacts = ArrayList<CloseContact>()
    var isQuarantined: Boolean = false
    var isBeingTested: Boolean = false
    val now: Double
        get() = sim.currentTime

    constructor(sim: Simulation, household: Household = Household(), infectedAtWork: Boolean = false) {
        this.sim = sim
        this.household = household
//        this.workplace = workplace
        hasSmartPhone =  Random.nextDouble() < sim.agentParams.pHasSmartPhone
        isUnemployed = if(infectedAtWork) false else Random.nextDouble() < sim.agentParams.pUnemployed
        household.add(this)
//        workplace.add(this)
        val incubationPeriod = sim.agentParams.incubationTime()
        exposureTime = sim.currentTime
        onsetTime = exposureTime + incubationPeriod
        isSubclinical = sim.agentParams.isSubclinical()
        isCompliant = Random.nextDouble() < sim.agentParams.pCompliant
        if(!isSubclinical) sim.events.add(Event(onsetTime, Event.Type.BECOMESYMPTOMATIC, this))
        for(infection in 1..sim.agentParams.numberOfInfected(sim.R0, isSubclinical)) {
            val transmissionTime = sim.currentTime + sim.agentParams.exposureToTransmissionTime(incubationPeriod)
            sim.events.add(Event(transmissionTime, Event.Type.TRANSMIT, this))
        }
        sim.cumulativeCases++
    }



    fun highRiskWarning() {
        if(isQuarantined) return
        if(isCompliant || (!isUnemployed && sim.contactTrace.enforceWorkplaceTracingAndTesting)) {
            testAndTrace(true)
        }
    }

    fun testAndTrace(retestIfNegative: Boolean) {
        if(isQuarantined) return
        if(!isBeingTested) {
            isBeingTested = true
//            sim.events.add(Event(now + 0.5, Event.Type.NOTTESTEDRECENTLY, this))
            isQuarantined = true
            if(antigenTestPositive()) {
                closeContacts.forEach {
                    it.contact.highRiskWarning()
                }
                if(sim.contactTrace.enforceHouseholdTesting) {
                    household.forEach {
                        if(it !== this) it.testAndTrace(true)
                    }
                } else {
                    household.forEach {
                        if(it !== this) highRiskWarning()
                    }
                }
                isBeingTested = false
            } else if(retestIfNegative) {
                // test again in a few days
                sim.events.add(Event(now + 4.0, Event.Type.RETEST, this))
            } else {
                isQuarantined = false
                isBeingTested = false
            }
        }
    }


    fun recordCloseContact(other: InfectedAgent) {
        closeContacts.add(CloseContact(now, other))
    }


    fun infectiousness() = sim.agentParams.infectiousness(now, onsetTime)

    fun isSymptomatic(time: Double) = !isSubclinical && time > onsetTime


    // returns next event for this agent
    // or null if no more events
    fun processNextEvent(event: Event) {
        when(event.type) {
            Event.Type.TRANSMIT -> {
                if (!isQuarantined) transmitInfection()
            }

            Event.Type.BECOMESYMPTOMATIC -> {
                // TODO: What to do when you become symptomatic while in quarrantine?
                if(!isQuarantined) {
                    if (isCompliant || (!isUnemployed && sim.contactTrace.workplaceSymptomMonitoring)) {
                        testAndTrace(true)
                    }
                }
//                if (isCompliant || (Random.nextDouble() < pForcedToIsolate)) {
//                    if(isQuarantined) {
//                        sim.contactTrace.doSwabTests(this)
//                    } else {
//                        highRiskWarning(sim)
//                    }
//                }
            }

            Event.Type.NOTTESTEDRECENTLY -> {
                isBeingTested = false
            }

            Event.Type.RETEST -> {
                testAndTrace(false)
            }

            else -> throw(IllegalStateException("Unrecognized event"))
        }
    }


    fun transmitInfection() {
        val homeWeight = household.nUninfected() * sim.agentParams.familyMemberInteractionWeight
        val totalWeight = homeWeight + 2.0
        var choice = Random.nextDouble(0.0, totalWeight).toInt()
        if(isUnemployed && choice == 1) choice = 0
        when(choice) {
            0 -> if(Random.nextDouble() > sim.agentParams.pImmune) { // community transmission
                nCommunity++
                val newInfectedAgent = InfectedAgent(sim, infectedAtWork = false)
                if(canSmartPhoneTrace(newInfectedAgent) && infectViaCloseContact()) {
                    recordCloseContact(newInfectedAgent)
                    newInfectedAgent.recordCloseContact(this)
                }
            }
            1 -> if(Random.nextDouble() > sim.agentParams.pImmune) { // workplace transmission
                nWork++
                val newInfectedAgent = InfectedAgent(sim, infectedAtWork = true)
                if((sim.contactTrace.enforceWorkplaceTracingAndTesting || canSmartPhoneTrace(newInfectedAgent)) && infectViaCloseContact()) {
                    recordCloseContact(newInfectedAgent)
                    newInfectedAgent.recordCloseContact(this)
                }
            }
            else -> { // household transmission
                nHome++
                val newInfectedAgent = InfectedAgent(sim, household = this.household, infectedAtWork = false)
                // Household automatically classed as close contacts
//                if(newInfectedAgent.isCompliant)  recordCloseContact(newInfectedAgent)
//                if(isCompliant) newInfectedAgent.recordCloseContact(this)
            }
        }
    }

    fun canSmartPhoneTrace(other: InfectedAgent): Boolean = isCompliant && hasSmartPhone && other.isCompliant && other.hasSmartPhone

    fun infectViaCloseContact() = Random.nextDouble() < sim.agentParams.pInfectedViaCloseContact

    fun antigenTestPositive(): Boolean {
        return virusIsDetectable() && (Random.nextDouble() < sim.contactTrace.pAntigenPositive)
    }


    fun virusIsDetectable() = (infectiousness() > sim.contactTrace.testLimitOfDetection) || (now > onsetTime)

    // Testing strategy stuff

//    fun takeTests() {
//        if(antigenTestPositive()) {
//            closeContacts.forEach { it.contact.highRiskWarning() }
//        }
//        sendSwabForPCR()
//    }


//    fun sendSwabForPCR() {
//        val resultTime = now + sim.contactTrace.PCRTestResultTime
//        val result = if(virusIsDetectable()) Event.Type.PCRTESTPOSITIVE else Event.Type.PCRTESTNEGATIVE
//        sim.events.add(Event(resultTime, result, this))
//    }

//    fun testPositivePCR() {
//        closeContacts.forEach { it.contact.highRiskWarning() }
//    }


}
