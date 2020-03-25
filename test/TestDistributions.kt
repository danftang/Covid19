import hellewellDESimulation.Agent
import hellewellDESimulation.Simulation
import org.junit.Test
import kotlin.math.pow

class TestDistributions {

    @Test
    fun testPreSymptomInfection() {
        val sim = Simulation(0.8, 3.5, 0.1)
        val agent = Agent(sim)
        val N = 100000
        var nNeg = 0
        for(i in 1..N) {
            val x = agent.infectionTime(0.0)
            if(x<0.0) nNeg++
        }
        println(nNeg.toDouble()/N)
    }

    @Test
    fun plotDistribution() {
        val sim = Simulation(0.8, 3.5, 0.1)
        val agent = Agent(sim)
        val N = 100000
        val nBins = 50
        val samples = DoubleArray(N) {
            agent.symptomOnsetToSelfIsolation()
        }
        val max = samples.max()?:0.0
        val min = samples.min()?:0.0
        val step = (max - min)/nBins
        val population = IntArray(nBins) { 0 }
        samples.forEach { x ->
            population[((x - min)*(nBins-1e-4)/(max - min)).toInt()]++
        }
        for(i in 0 until nBins) {
            println("${(i+0.5)*step + min} ${population[i].toDouble()/N}")
        }
    }


    @Test
    fun testDistribution() {
        val sim = Simulation(0.8, 3.5, 0.1)
        val agent = Agent(sim)
        val N = 100000
        var total = 0.0
        var totsq = 0.0
        var nNeg = 0
        for(i in 1..N) {
            val x = agent.infectionTime(10.0)
            total += x
            totsq += x*x
            if(x < 10.0) nNeg++
        }
        println("proportion less than 10 = ${nNeg.toDouble()/N}")
        println("mean = ${total/N.toDouble()}")
        println("variance = ${totsq/N.toDouble()- (total/N.toDouble()).pow(2)}")
        println("SD = ${(totsq/N.toDouble()- (total/N.toDouble()).pow(2)).pow(0.5)}")
    }

}