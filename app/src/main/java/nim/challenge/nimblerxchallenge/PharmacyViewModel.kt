package nim.challenge.nimblerxchallenge

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Author: Vanessa Johnson
 * File Name: PharmacyViewModel
 * All the logic of the app
 */

class PharmacyViewModel: ViewModel() {
    //    https://www.themathdoctors.org/distances-on-earth-2-the-haversine-formula/ -> formula
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val lonDifference = Math.toRadians(lon2 - lon1)
        val latDifference = Math.toRadians(lat2 - lat1)
        val a = sin(latDifference / 2) * sin(latDifference / 2) + cos(lat1) * cos(lat2) * sin(
            lonDifference / 2
        ) * sin(lonDifference / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

//    utilized https://www.javatpoint.com/kotlin-android-sharedpreferences
    class MyPreferences(context: Context, private val key: String) {
        private val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        private val gson = Gson()

//    for Medications
        fun saveSelectedMedsMap(selectedMedsMap: MutableMap<String?, MutableList<Medication>>) {
            val serializedMap = gson.toJson(selectedMedsMap)
            prefs.edit().putString(key, serializedMap).apply()
        }

        fun loadSelectedMedsMap(): MutableMap<String?, MutableList<Medication>> {
            val serializedMap = prefs.getString(key, null)
            return if (serializedMap != null) {
                gson.fromJson(serializedMap, object : TypeToken<MutableMap<String?, MutableList<Medication>>>() {}.type)
            } else {
                mutableMapOf()
            }
        }

//    for the mutableMap with string and pair of string and boolean
        fun saveSelectedPairStringMap(selectedPairStringMap: MutableMap<String?, Pair<String?, Boolean>>) {
            val serializedMap = gson.toJson(selectedPairStringMap)
            prefs.edit().putString(key, serializedMap).apply()
        }

        fun loadSelectedPairStringMap(): MutableMap<String?, Pair<String?, Boolean>> {
            val serializedMap = prefs.getString(key, null)
            return if (serializedMap != null) {
                gson.fromJson(serializedMap, object : TypeToken<MutableMap<String?, Pair<String?, Boolean>>>() {}.type)
            } else {
                mutableMapOf()
            }
        }
    }
}