class Community: InfectionLocation, ArrayList<InfectedAgent> {

    constructor(): super(4)

    constructor(infector: InfectedAgent): this() {
        add(infector)
    }
}