import extensions.nextSkewNormal
import extensions.skewNormalDensity
import lib.gnuplot
import org.junit.Test
import kotlin.math.pow
import kotlin.random.Random

class TestDistributions {

    @Test
    fun testPreSymptomInfection() {
        val agentParams = AgentParams()
        val N = 100000
        var nNeg = 0
        for(i in 1..N) {
            val x = agentParams.exposureToTransmissionTime(10.0)
            if(x<10.0) nNeg++
        }
        println(nNeg.toDouble()/N)
    }

    @Test
    fun plotDistribution() {
        val N = 100000
        val nBins = 50
        val samples = DoubleArray(N) {
//            InfectedAgent.exposureToTransmissionTime(4.0)
//            InfectedAgent.incubationTime()
            Random.nextSkewNormal(1.95, 2.0, 1.0)
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
        gnuplot {
            val data = heredoc(population.mapIndexed {i, v -> Pair((i+0.5)*step + min, v.toDouble()/N)})
            invoke("plot [0:10] $data with lines")
        }
    }

    @Test
    fun plotIntDistribution() {
        val agentParams = AgentParams()
        val N = 100000
        val samples = IntArray(N) {
            agentParams.numberOfInfected(2.5, false)
        }
        val max = samples.max()?:0
        val min = samples.min()?:0
        val nBins = max - min + 1
        val population = IntArray(nBins) { 0 }
        samples.forEach { x ->
            population[x - min]++
        }
        for(i in 0 until nBins) {
            println("${i+min} ${population[i].toDouble()/N}")
        }
        gnuplot {
            val data = heredoc(population.mapIndexed {i, v -> Pair(i + min, v.toDouble()/N)})
            invoke("plot $data with lines")
        }
    }

    @Test
    fun plotPDF() {
        val N = 100
        val density = DoubleArray(N) {
            skewNormalDensity(1.95, 2.0, 4.0, it/10.0)
        }
        var total = 0.0
        var preSymptom = 0.0
        for(i in 0 until N) {
            total += density[i]*0.1
            if(i < 40) preSymptom += density[i]*0.1
            println("${i} ${density[i]}")
        }
        println("total = $total preSymptom = $preSymptom")
        gnuplot {
            val data = heredoc(density.asList())
            invoke("plot $data with lines")
        }
    }


    @Test
    fun testDistribution() {
        val agentParams = AgentParams()
        val N = 100000
        var total = 0.0
        var totsq = 0.0
        var nNeg = 0
        for(i in 1..N) {
            val x = agentParams.exposureToTransmissionTime(10.0)
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