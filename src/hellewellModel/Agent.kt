package hellewellModel

import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Agent {
    companion object {
        var pAsymptomatic = 0.0
        var pMissed: Double = 0.2
        var R0 = 2.5
    }


    val exposureTime: Double
    val isolationTime: Double
    val incubationPeriod: Double

    constructor(exposureTime: Double, infectorIsolationTime: Double) {
        this.exposureTime = exposureTime
        val isAsymptomatic = Random.nextDouble() < pAsymptomatic
        val isMissed = Random.nextDouble() < pMissed
        incubationPeriod = incubationTime()
        val onsetTime = exposureTime + incubationPeriod
        isolationTime = if(isAsymptomatic)
            Double.POSITIVE_INFINITY
        else {
            val selfIsolationTime = onsetTime + symptomOnsetToSelfIsolation()
            if(isMissed) {
                selfIsolationTime
            } else {
                min(selfIsolationTime, max(onsetTime, infectorIsolationTime))
            }
        }
    }

    fun spawnNewCases(): List<Agent> {
        val newAgents = ArrayList<Agent>()
        for(case in 1..numberOfInfected()) {
            val newExposureTime = exposureTime + exposureToTransmissionTime(incubationPeriod)
            if(newExposureTime < isolationTime) {
                newAgents.add(Agent(newExposureTime, isolationTime))
            }
        }
        return newAgents
    }

    fun numberOfInfected() = Random.nextNegativeBinomial(0.16, R0)

    fun exposureToTransmissionTime(incubationTime: Double): Double {
        // shape = 30, 1.95, 0.7 for <1%, 15% and 30% before symptom onset respectively
//        return Random.nextSkewNormal(1.95, 2.0, incubationTime)
        val t = Random.nextSkewNormal(1.95, 2.0, incubationTime)
        return if(t<1.0) 1.0 else t
    }

    fun symptomOnsetToSelfIsolation() = Random.nextWeibull(1.651524,4.287786)

    fun incubationTime() = Random.nextWeibull(2.322737, 6.492272)

}