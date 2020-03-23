interface Agent {
    var isDetected: Boolean
    val contacts: List<Agent>

    fun processNextEvent(sim: Simulation): Event?
    fun peekNextEvent(): Event?
}