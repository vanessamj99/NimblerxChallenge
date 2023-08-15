package nim.challenge.nimblerxchallenge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Author: Vanessa Johnson
 */
//data classes for the api call for the pharmacies
@Serializable
data class PharmacyDataClass (
    val responseCode: String?,
    val href: String?,
    val details: String?,
    val generatedTs: String?,
    val value: Value?
)
@Serializable
data class Value(
    val id: String?,
    val pharmacyChainId: String?,
    val name: String?,
    val localId: String?,
    val testPharmacy: Boolean?,
    val address: Address?,
    val primaryPhoneNumber: String?,
    val defaultTimeZone: String?,
    val pharmacistInCharge: String?,
    val postalCodes: String?,
    val deliverableStates: Array<String>?,
    val pharmacyHours: String?,
    @SerialName("pharmacyHoursMap")
    val pharmacyHoursMap: HoursMap?,
    val deliverySubsidyAmount: String?,
    val pharmacySystem: String?,
    val acceptInvalidAddress: Boolean?,
    val pharmacyType: String?,
    val pharmacyLoginCode: String?,
    val npi: String?,
    val requiresRefillEnrollment: Boolean?,
    val cashOnlyPharmacy: Boolean?,
    val checkoutPharmacy: Boolean?,
    val marketplacePharmacy: Boolean?,
    val importActive: Boolean?,

)
@Serializable
data class Address(
    val streetAddress1: String?,
    val streetAddress2: String? = null,
    val city: String?,
    val usTerritory: String?,
    val postalCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val addressType: String?,
    val externalId: String?,
    val isValid: Boolean?,
    val googlePlaceId: String? = null,
)

@Serializable
data class HoursMap(
    @SerialName("M") val M: Times? = null,
    @SerialName("T") val T: Times? = null,
    @SerialName("S") val S: Times? = null,
    @SerialName("W") val W: Times? = null,
    @SerialName("F") val F: Times? = null,
    @SerialName("R") val R: Times? = null,
)


@Serializable
data class Times(
    val startTime: String?,
    val endTime: String?,
)