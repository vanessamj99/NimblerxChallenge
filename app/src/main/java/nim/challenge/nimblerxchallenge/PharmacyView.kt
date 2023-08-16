package nim.challenge.nimblerxchallenge

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking

/**
 * Author: Vanessa Johnson
 * File Name: PharmacyView
 * For the View of the App
 */

class PharmacyView : AppCompatActivity() {
//    given user latitude and longitude coordinations
    private val userLatitude: Double = 37.48771670017411
    private val userLongitude: Double = -122.22652739630438

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            involved with saving and loading saved maps
            val selectedMedsMapPreferences: PharmacyViewModel.MyPreferences =
                PharmacyViewModel.MyPreferences(this, "selectedMedsMap")
            val tempMapPreferences: PharmacyViewModel.MyPreferences =
                PharmacyViewModel.MyPreferences(this, "tempMapMedsMap")
            val pharmacyModel = PharmacyModel()
            PharmacyProject(tempMapPreferences, selectedMedsMapPreferences, pharmacyModel)
        }
    }


    @Composable
    fun PharmacyNames(buttonTitle: String?, ordered: Boolean, onClick: () -> Unit) {
        if (buttonTitle != null) {
//            seeing if the user ordered from the pharmacy and if they did,
//            add a check mark next to the name
            if (!ordered) {
                Text(
                    text = buttonTitle,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onClick() }
                )
            } else {
                Text(
                    text = "\u2713 $buttonTitle",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onClick() }
                )
            }
        }
    }


//    displaying the details of the specific pharmacy
    @Composable
    fun PharmacyDetailScreen(pharmId: String) {
        val pharmacy: PharmacyDataClass
        runBlocking {
            pharmacy = PharmacyModel().getData(pharmId) ?: PharmacyDataClass(null,null,null,null,null)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(pharmacy.value?.name ?: stringResource(R.string.name_not_available))
            Text(pharmacy.value?.address?.streetAddress1 + "\n" + pharmacy.value?.address?.city + " " + pharmacy.value?.address?.postalCode)
            Text(pharmacy.value?.primaryPhoneNumber ?: stringResource(R.string.phone_number_not_available))
            Text(
                (pharmacy.value?.pharmacyHours ?: stringResource(R.string.hours_not_available)).replace("\\n", "\n"),
                softWrap = true
            )
        }
    }


//    list of pharmacy name titles
    @Composable
    fun PharmacyNamesList(
        navController: NavController,
        pharmacyMap: MutableMap<String?, Pair<String?, Boolean>>
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.direction_to_order))
            for ((pharmId, pharmInfo) in pharmacyMap) {
                val (name, ordered) = pharmInfo
                PharmacyNames(name, ordered = ordered) {
                    var pharmacyData: PharmacyDataClass
                    runBlocking {
                        pharmacyData = PharmacyModel().getData(pharmId) ?: PharmacyDataClass(null,null,null,null,null)
                        navController.navigate("pharmacyDetails/${pharmacyData.value?.id}")
                    }
                }
            }
        }
    }

//    most of the ui and navigation
    @Composable
    fun PharmacyProject(
        tempMapPreferences: PharmacyViewModel.MyPreferences,
        selectedMedsMapPreferences: PharmacyViewModel.MyPreferences,
        pharmacyModel: PharmacyModel,
    ) {
        val navController = rememberNavController()
        val selectedMedsMap = selectedMedsMapPreferences.loadSelectedMedsMap()
        var tempMap = tempMapPreferences.loadSelectedPairStringMap()

        if (tempMap.isEmpty()) {
            tempMap = pharmacyModel.tempMap
        }

        val selectedMeds = remember { mutableStateListOf<Medication>() }
        val closestPharm: MutableMap<PharmacyDataClass, Double> = mutableMapOf()
        val args = Bundle()

//        source: https://developer.android.com/jetpack/compose/navigation#:~:text=The%20NavHost%20links%20the%20NavController,the%20NavHost%20is%20automatically%20recomposed
//        navigation for the different composable screens
        NavHost(navController = navController, startDestination = "pharmacyProject") {

            composable("pharmacyProject") {
                PharmacyNamesList(navController = navController, pharmacyMap = tempMap)
                Button(
                    onClick = {
                        for ((pharmId, _) in tempMap) {
                            val pharmacy: PharmacyDataClass
                            runBlocking {
                                pharmacy = PharmacyModel().getData(pharmId) ?: PharmacyDataClass(null,null,null,null,null)
                            }
                            pharmacy.value?.address?.latitude?.let { it1 ->
                                pharmacy.value.address.longitude?.let { it2 ->
                                    closestPharm[pharmacy] = (PharmacyViewModel().calculateDistance(
                                        userLatitude, userLongitude,
                                        it1, it2
                                    ))

                                }
                            }
                        }

                        closestPharm.entries.sortedBy { it.value }.toMutableList()

                        for ((pharm, _) in closestPharm) {
                            if (tempMap[pharm.value?.id]?.second == false) {
                                navController.navigate("pharmacyDetails/${pharm.value?.id}")
                            }
                        }

                        val allValuesAreTrue = tempMap.all { entry ->
                            entry.value.second
                        }
                        if (allValuesAreTrue) {
                            val toastMessage = R.string.already_ordered
                            Toast.makeText(
                                this@PharmacyView,
                                toastMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 200.dp),
                ) {
                    Text(stringResource(R.string.order_from_closest_pharm))
                }
            }
            composable("orderingScreen") {
                var searchForMeds by remember { mutableStateOf(TextFieldValue()) }


                Box(modifier = Modifier.fillMaxSize()) {
                    OrderingScreen(
                        navController = navController,
                        selectedMeds = selectedMeds.toMutableList(),
                        pharmacyModel = pharmacyModel
                    )
                }

                OutlinedTextField(
                    value = searchForMeds,
                    onValueChange = { searchForMeds = it },
                    label = { Text(text = stringResource(R.string.enter_medication_name))},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                )

                Button(
                    onClick = {
                        val med =
                            pharmacyModel.allMedications.find { it.name.lowercase() == searchForMeds.text.lowercase() }
                        if (med != null && !selectedMeds.contains(med)) {
                            selectedMeds.add(med)
                        } else if (med != null && selectedMeds.contains(med)) {
                            val toastMessage = R.string.already_on_list
                            Toast.makeText(
                                this@PharmacyView,
                                toastMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val toastMessage = R.string.not_on_preset_list
                            Toast.makeText(
                                this@PharmacyView,
                                toastMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 120.dp),
                ) {
                    Text(text = stringResource(R.string.add))
                }
                Button(
                    onClick = {
                        val med =
                            pharmacyModel.allMedications.find { it.name.lowercase() == searchForMeds.text.lowercase() }
                        if (med != null && selectedMeds.contains(med)) {
                            selectedMeds.remove(med)
                        } else {
                            val toastMessage = R.string.not_on_current_list
                            Toast.makeText(
                                this@PharmacyView,
                                toastMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 170.dp),
                ) {
                    Text(text = stringResource(R.string.remove))
                }

                val id = args.getString("getID")
                if (selectedMedsMap[id] == null) {
                    LazyColumn(Modifier.padding(top = 240.dp)) {
                        items(selectedMeds) {
                            Text(it.name)
                        }
                    }
                } else {
                    LazyColumn(Modifier.padding(top = 240.dp)) {
                        items(selectedMedsMap[id].orEmpty()) {
                            Text(it.name)
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val pharmId = args.getString("getID")
                            tempMap[pharmId]?.let { (stringValue, _) ->
                                val updatedPair = Pair(stringValue, true)
                                tempMap[pharmId] = updatedPair
                            }
                            if(selectedMeds.isEmpty()){
                                val toastMessage = R.string.no_medications_chosen
                                Toast.makeText(this@PharmacyView,toastMessage,Toast.LENGTH_LONG).show()
                            }
                            else {
                                selectedMedsMap[pharmId] = selectedMeds.toMutableList()
                                selectedMeds.clear()
                                tempMapPreferences.saveSelectedPairStringMap(tempMap)
                                selectedMedsMapPreferences.saveSelectedMedsMap(selectedMedsMap)
                                navController.navigate("pharmacyProject")
                            }
                        }, modifier = Modifier.padding(top = 410.dp)
                    ) {
                        Text(text = stringResource(R.string.confirm))
                    }
                }
            }
            composable("pharmacyDetails/{pharmId}", arguments = listOf(navArgument("pharmId") {
                type = NavType.StringType
            })) { it ->
                val pharmId = it.arguments?.getString("pharmId")
                args.putString("getID", pharmId)

                if (pharmId != null) {
                    PharmacyDetailScreen(pharmId)
                }
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(5.dp),

                    ) {
                    Text(stringResource(R.string.back_to_pharmacies))
                }

                if(tempMap.isNotEmpty()){
                    if (tempMap[pharmId]?.second == false) {
                        Box(
                            modifier = Modifier.padding(top = 200.dp, start = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("orderingScreen")
                                }
                            ) {
                                Text(stringResource(R.string.order_medications))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.padding(top = 200.dp, start = 5.dp)
                        ) {
                            Text(
                                stringResource(R.string.already_ordered_specific_pharm),
                                Modifier.padding(top = 80.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.ordered_medications),
                                Modifier.padding(top = 96.dp),
                                style = TextStyle(textDecoration = TextDecoration.Underline)
                            )
                            LazyColumn(Modifier.padding(top = 112.dp)) {
                                items(selectedMedsMap[pharmId].orEmpty()) {
                                    Text(it.name)
                                }
                            }
                        }
                    }
            }

            }
        }

    }


//    the function for the ordering screen and being able to go back to the pharmacy list screen
    @Composable
    fun OrderingScreen(
        navController: NavController,
        selectedMeds: MutableList<Medication>,
        pharmacyModel: PharmacyModel
    ) {
        val localContext = LocalContext.current

        val savedMeds = remember {
            localContext.getSharedPreferences("SavedMeds", Context.MODE_PRIVATE)
        }

        LaunchedEffect(Unit) {
            val savedSelectedMeds = savedMeds.getStringSet("selectedMeds", setOf())
            savedSelectedMeds?.let { it ->
                selectedMeds.clear()
                selectedMeds +=
                    it.mapNotNull { med ->
                        pharmacyModel.allMedications.firstOrNull {
                            it.name == med
                        }
                    }.toMutableList()
            }
        }

        Button(
            onClick = { navController.navigate("pharmacyProject") }
        ) {
            Text(stringResource(R.string.back_to_pharm_screen))
        }

    }

}
