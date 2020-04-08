import org.apache.commons.math3.distribution.EnumeratedDistribution
import org.apache.commons.math3.util.Pair
import java.util.function.BooleanSupplier
import kotlin.collections.ArrayList
import kotlin.random.Random

class Household: ArrayList<InfectedAgent>, InfectionLocation {
    val totalSize: Int

    // From ONS, Households and Household Composition in England and Wales: 2001-11
    constructor(): super(4) {
        totalSize = EnumeratedDistribution(listOf(
            Pair(1, 0.30245),
            Pair(2, 0.34229),
            Pair(3, 0.15587),
            Pair(4, 0.12972),
            Pair(5, 0.04643),
            Pair(6, 0.02324)
        )).sample()
    }

    fun nUninfected() = totalSize - this.size
}