import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import sun.management.HotspotMemoryMBean
import java.lang.IllegalStateException
import java.util.*
import kotlin.random.Random

//class InfectedAgent: Agent {
//    val R0 = 2.5
//    val pSubclinical = 0.1
//
//    enum class State {
//        ASYMPTOMATIC,
//        SYMPTOMATIC
////        CRITICAL
//    }
//
//    enum class Location {
//        HOME,
//        WORK,
//        COMMUNITY
//    }
//
//    var daysSinceInfection: Int = 0
//    val subclinical: Boolean
//    val symptomOnsetTime: Double
//    val infectionTimes: PriorityQueue<Double>
//    var state: State = State.ASYMPTOMATIC
//    val household: Household
//    val workplace: Workplace
//
//
//    constructor(household: Household = Household(), workplace: Workplace = Workplace()) {
//        this.household = household
//        this.workplace = workplace
//        household.add(this)
//        workplace.add(this)
//        subclinical = isSubclinical()
//
//        infectionTimes = PriorityQueue<Double>()
//        symptomOnsetTime = incubationTime()
//        for(infection in 1..numberOfInfected()) {
//            infectionTimes.add(infectionTime(symptomOnsetTime))
//        }
//    }
//
//
//    fun step(sim: Simulation) {
//        daysSinceInfection += 1
//        if(infectSomeoneToday()) {
//            when(chooseInfectionLocation()) {
//                Location.HOME -> {
//                    if(household.nUninfected() < household.size) {
//                        val newlyInfected = InfectedAgent(household)
//                        household.add(newlyInfected)
//                        sim.addUndetectedCase(newlyInfected)
//                    }
//                }
//
//                Location.WORK -> {
//                    val newlyInfected = InfectedAgent(workplace = workplace)
//                    workplace.add(newlyInfected)
//                    sim.addUndetectedCase(newlyInfected)
//                }
//
//                Location.COMMUNITY -> {
//                    sim.addUndetectedCase(InfectedAgent())
//                }
//            }
//        }
//        when(state) {
//            State.ASYMPTOMATIC ->
//                if(becomeSymptomatic()) {
//                    state = State.SYMPTOMATIC
//                    sim.accessPrimaryCare(this)
//                }
//
////            State.SYMPTOMATIC ->
////                if(becomeCritical()) {
////                    state = State.CRITICAL
////                    sim.accessPrimaryCare(this)
////                }
//        }
//    }
//
//    fun becomeSymptomatic(): Boolean {
//        return !subclinical && daysSinceInfection > symptomOnsetTime
//    }
//
//
//    fun infectSomeoneToday(): Boolean {
//        if(infectionTimes.isNotEmpty() && daysSinceInfection > infectionTimes.peek()) {
//            infectionTimes.poll()
//            return true
//        }
//        return false
//    }
//
//    // Equal distribution based on Imperial College Report 9
//    fun chooseInfectionLocation(): Location {
//        return when(Random.nextInt(1,4)) {
//            1 -> Location.HOME
//            2 -> Location.COMMUNITY
//            3 -> Location.WORK
//            else -> throw(IllegalStateException())
//        }
//    }
//
//    fun numberOfInfected(): Int {
//        return Random.nextNegativeBinomial(0.16, R0)
//    }
//
//    fun incubationTime(): Double {
//        return Random.nextWeibull(2.32277, 6.492272)
//    }
//
//    fun infectionTime(incubationTime: Double): Double {
//        return Random.nextSkewNormal(1.95, 2.0, incubationTime)
//    }
//
//    fun isSubclinical(): Boolean {
//        return Random.nextDouble() < pSubclinical
//    }
//}