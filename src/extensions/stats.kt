package extensions

import org.apache.commons.math3.distribution.GammaDistribution
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.distribution.WeibullDistribution
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.random.Random

fun Random.nextNegativeBinomial(r: Double, mu: Double): Int {
    val theta = mu/r
    val lambda = GammaDistribution(r, theta).sample()
    return PoissonDistribution(lambda).sample()
}


fun Random.nextWeibull(alpha: Double, beta: Double): Double {
    return WeibullDistribution(alpha, beta).sample()
}


fun Random.nextSkewNormal(shape: Double, scale: Double, location: Double): Double {
    val normal = NormalDistribution(0.0,1.0)
    val x1 = normal.sample()
    val x2 = normal.sample()
    val x = (shape*x1.absoluteValue + x2)/sqrt(1.0 + shape*shape)
    return x*scale + location
}