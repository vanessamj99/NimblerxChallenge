package nim.challenge.nimblerxchallenge

import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

/**
 * Author: Vanessa Johnson
 * File Name: PharmacyModel
 * Api calls and reading in any data that will be needed in the app
 */

class PharmacyModel {
    private val pharmacyJson = mapOf(
        "pharmacies" to listOf(
            mapOf(
                "name" to "ReCept",
                "pharmacyId" to "NRxPh-HLRS"
            ),
            mapOf(
                "name" to "My Community Pharmacy",
                "pharmacyId" to "NRxPh-BAC1"
            ),
            mapOf(
                "name" to "MedTime Pharmacy",
                "pharmacyId" to "NRxPh-SJC1"
            ),
            mapOf(
                "name" to "NY Pharmacy",
                "pharmacyId" to "NRxPh-ZEREiaYq"
            ),
        )
    )
    var allMedications: List<Medication> = listOf()
    var tempMap: MutableMap<String?, Pair<String?, Boolean>> = mutableMapOf()

    init {
        runBlocking {
            allMedications = readingInMedications()
            tempMap = pharmacyMap()
        }
    }

    /**
     * Parameters: None
     * Return Value: MutableMap<String?, Pair<String?, Boolean>>
     * Populates a map with the pharmId, name and a boolean
     */

    private fun pharmacyMap(): MutableMap<String?, Pair<String?, Boolean>> {
        val pharmacyDict = mutableMapOf<String?, Pair<String?, Boolean>>()
        val pharmacyNames = mutableListOf<String?>()
        for ((_, pharName) in pharmacyJson) {
            for (pharDet in pharName) {
                pharmacyNames += pharDet["pharmacyId"]
                pharmacyDict[pharDet["pharmacyId"]] = Pair(pharDet["name"], false)
            }
        }
        return pharmacyDict
    }

    /**
     * Parameters: None
     * Return Value: List<Medication>
     * Reads in the medications from a text file
     */
    private suspend fun readingInMedications(): List<Medication> {
        val stringUrl =
            "https://s3-us-west-2.amazonaws.com/assets.nimblerx.com/prod/medicationListFromNIH/medicationListFromNIH.txt"
        var body = mutableListOf<Medication>()
        withContext(Dispatchers.IO) {
            try {
                val stringToUrl = URL(stringUrl)
                val connection = stringToUrl.openConnection()
                val input = connection.getInputStream()
                val reader = BufferedReader(InputStreamReader(input))
                body = listOf<Medication>().toMutableList()
                var temp = reader.readLine()
                while (temp != null) {
                    val item = temp.replace(",", "")
                    val medicationItem = Medication(item)
                    body += medicationItem
                    temp = reader.readLine()
                }
                reader.close()
            } catch (e: Exception) {
                println(e)
            }
        }
        return body
    }

    /**
     * Parameters: String
     * Return Value: PharmacyDataClass (made in a separate file)
     * Api call with the pharmacy Id of the specific Pharmacy
     */
    suspend fun getData(pharmId: String?): PharmacyDataClass? {
        try {
            val url = "https://api-qa-demo.nimbleandsimple.com/pharmacies/info/${pharmId}"
            val pharmacy: PharmacyDataClass

            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }

            pharmacy = client.get(url).body()
            return pharmacy
        }
        catch (e: Exception){
            return null
        }
    }
}
