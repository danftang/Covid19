package hellewellModel


fun main() {
    Agent.R0 = 1.5
    for(i in 0..5) {
        Agent.pMissed = 1.0 - i*0.2
        println("${Agent.pMissed} ${Simulation.pControl(1000)}")
    }
}
