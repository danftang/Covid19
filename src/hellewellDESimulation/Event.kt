package hellewellDESimulation

class Event(val time: Double, val type: Type, val agent: Agent): Comparable<Event> {
    enum class Type {
        TRANSMIT,
        BECOMESYMPTOMATIC,
        SELFISOLATE
    }

    override fun compareTo(other: Event): Int {
        return time.compareTo(other.time)
    }

}