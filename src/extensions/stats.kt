package extensions

import org.apache.commons.math3.distribution.GammaDistribution
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.special.Erf.erf
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.random.Random

fun Random.nextNegativeBinomial(r: Double, mu: Double): Int {
    val theta = mu/r
    val lambda = GammaDistribution(r, theta).sample()
    return PoissonDistribution(lambda).sample()
}


// shape is sometimes called alpha or k
// scale is sometimes called beta or lambda
fun Random.nextWeibull(shape: Double, scale: Double): Double {
    return WeibullDistribution(shape, scale).sample()
}


fun Random.nextSkewNormal(shape: Double, scale: Double, location: Double): Double {
    val normal = NormalDistribution(0.0,1.0)
    val x1 = normal.sample()
    val x2 = normal.sample()
    val x = (shape*x1.absoluteValue + x2)/sqrt(1.0 + shape*shape)
    return x*scale + location
}

fun skewNormalDensity(shape: Double, scale: Double, location: Double, x: Double): Double {
    return (1.0 + erf(shape*(x-location)/(scale*sqrt(2.0))))*NormalDistribution(location,scale).density(x)

}