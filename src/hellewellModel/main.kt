package hellewellModel


fun main() {
    Simulation.R0 = 1.5
    for(i in 0..5) {
        Simulation.pMissed = 1.0 - i*0.2
        println("${Simulation.pMissed} ${Simulation.pControl(1000)}")
    }
}
