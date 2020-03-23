import kotlin.collections.ArrayList
import kotlin.random.Random

class Household: ArrayList<InfectedAgent> {
    val totalSize: Int

    constructor(): super() {
        totalSize = Random.nextInt(1,4)
    }

    fun nUninfected() = totalSize - this.size
}