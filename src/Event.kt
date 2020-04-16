
class Event(val time: Double, val type: Type, val agent: InfectedAgent): Comparable<Event> {
    enum class Type {
        TRANSMIT,
        PCRTESTPOSITIVE,
        PCRTESTNEGATIVE,
        BECOMESYMPTOMATIC
    }

    override fun compareTo(other: Event): Int {
        return time.compareTo(other.time)
    }

}