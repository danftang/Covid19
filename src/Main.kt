import extensions.nextNegativeBinomial
import extensions.nextWeibull
import kotlin.math.pow
import kotlin.random.Random

fun main() {
    val sim = Simulation(1000)
    val initialCases = 40
    val nDays = 80

    for(i in 1..initialCases) sim.addUndetectedCase(InfectedAgent())
    for(day in 1..nDays) {
        sim.step()
        println("${sim.nCases} ${sim.detectedCases.size}")
    }
}

fun testDistribution() {
//    for(i in 1..20) {
//        println(Random.nextNegativeBinomial(0.16, 2.5))
//    }


    val N = 100000
    var total = 0.0
    var totsq = 0.0
    for(i in 1..N) {
        val x = Random.nextWeibull(2.322737, 6.492272)
        total += x
        totsq += x*x
    }
    println(total/N.toDouble())
    println(totsq/N.toDouble()- (total/N.toDouble()).pow(2))
    println((totsq/N.toDouble()- (total/N.toDouble()).pow(2)).pow(0.5))
//    println(2.5*(1.0 + 2.5/0.16))

}