import kotlin.random.Random

class ContactTracingParams(
    val enforceHouseholdTesting: Boolean,   // are household members forced into quarantine
    val enforceWorkplaceTracingAndTesting: Boolean,
    val workplaceSymptomMonitoring: Boolean,
    val testLimitOfDetection: Double = 0.02,         // sensitivity of swab test
    val pAntigenPositive: Double = 0.85             // probability that an antigen test is positive given infection

)