import extensions.nextNegativeBinomial
import extensions.nextSkewNormal
import extensions.nextWeibull
import extensions.skewNormalDensity
import kotlin.random.Random

class AgentParams(
    var pCompliant: Double = 0.75,
    val pHasSmartPhone: Double = 0.95, // 90% penetration but 95% prob that a person involved in a contact has a phone
    val pInfectedViaCloseContact: Double = 0.9,
    val pUnemployed: Double = 0.04,

    // This will be different for different countries
    // to be calibrated after the fact.
    // Currently set at 5% as representative
    // TODO: Check this against expert opinion / SIR model / Data
    val pImmune: Double = 0.05,

    // Given a transmission event, how many times more probable
    // was that event in the infected's household
    // compared to in the community
    // Calibrated to give equal numbers of transmission in all three locations
    // Ferguson et.al, 2020, Impact of non-pharmaceutical interventions (NPIs) to reduce COVID-
    // 19 mortality and healthcare demand, Imperial College COVID-19 response team, Report 9
    val familyMemberInteractionWeight: Double = 3.0,


    // ratio of average number of infections caused by an asymptomatic
    // over that of a symptomatic.
    // Ferguson et.al, 2020, Impact of non-pharmaceutical interventions (NPIs) to reduce COVID-
    // 19 mortality and healthcare demand, Imperial College COVID-19 response team, Report 9
    val subclinicalInfectiveness: Double = 0.66,

    // Sample whether a person will be asymptomatic
    // given that they are infected.
    // Mizumoto et.al., 2020, Estimating the asymptomatic proportion of coronavirus
    // disease 2019 (COVID-19) cases on board the Diamond
    // Princess cruise ship, Yokohama, Japan,
    // Mizumoto Kenji et.al.,2020, Estimating the asymptomatic proportion of coronavirus
    // disease 2019 (COVID-19) cases on board the Diamond Princess cruise ship, Yokohama,
    // Japan, 2020. Euro Surveill. 2020;25(10):pii=2000180.
    // https://doi.org/10.2807/1560-7917.ES.2020.25.10.2000180
    var pSubclinical: Double = 0.179,


    // Sample from time from first exposure to infection of another person.
    // Hellewell et.al., 2020, Feasibility of controlling COVID-19 outbreaks by isolation of
    // cases and contacts. The Lancet, 8:e488-96
    // https://doi.org/10.1016/S2214-109X(20)30074-7
    // shape = 30, 1.95, 0.7 for <1%, 15% and 30% before symptom onset respectively
    var generationIntervalShape: Double = 1.95,
    val generationIntervalScale: Double = 2.0
)
{
    fun isSubclinical() = Random.nextDouble() < pSubclinical

    fun exposureToTransmissionTime(incubationTime: Double): Double {
        val t = Random.nextSkewNormal(generationIntervalShape, generationIntervalScale, incubationTime)
        return if (t < 1.0) 1.0 else t
    }

    // Sample from the total number of people a person will infect over
    // the course of the disease.
    // The original Hellewell model used a dispersion of 0.16 reported in
    // Lloyd-Smith Et.al., 2005, Superspreading and the effect of individual
    // variation on disease emergence. Nature, 438:17
    // doi:10.1038/nature04153
    // However, this is too low for COVID-19 so we go for 10.0 based on
    // Zhuang, Z., Zhao, S., Lin, Q., Cao, P., Lou, Y., Yang, L., . . . He, D. (2020). Preliminary
    // estimating the reproduction number of the coronavirus disease (covid-19) outbreak in republic
    // of korea from 31 january to 1 march 2020. medRxiv.
    // and
    // Riou, J., & Althaus, C. L. (2020). Pattern of early human-to-human transmission of wuhan 2019
    // novel coronavirus (2019-ncov), december 2019 to january 2020. Eurosurveillance, 25(4).
    fun numberOfInfected(R0: Double, isSubclinical: Boolean): Int {
        val scale = 1.0 / (1.0 - (1.0 - subclinicalInfectiveness) * pSubclinical)
        val R = R0 * scale * if (isSubclinical) subclinicalInfectiveness else 1.0
//            return Random.nextNegativeBinomial(0.16, R)
        return Random.nextNegativeBinomial(10.0, R)
    }


    // Sample from time from first exposure to onset of symptoms (if subclinical,
    // there are no symptoms but this value still anchors infectivity
    // over time).
    // Backer et.al., 2020, The incubation period of 2019-nCoV infections
    // among travellers from Wuhan, China. MedRxiv
    // https://doi.org/10.1101/2020.01.27.20018986
    fun incubationTime(): Double {
        return Random.nextWeibull(2.322737, 6.492272)
    }


    fun infectiousness(time: Double, onsetTime: Double): Double {
        return skewNormalDensity(generationIntervalShape, generationIntervalScale, onsetTime, time)
    }


}