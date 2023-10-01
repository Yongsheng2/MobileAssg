package com.tarc.edu.etrack.ui.find_station

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.RecyclerView.MyAdapter
import com.tarc.edu.etrack.RecyclerView.StationNavigator
import com.tarc.edu.etrack.databinding.FragmentFindStationBinding
import com.tarc.edu.etrack.ui.station_details.StationData
import com.tarc.edu.etrack.ui.station_details.StationDetailFragment

class FindStationFragment : Fragment(), StationNavigator {

    private lateinit var binding: FragmentFindStationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: MyAdapter
    private var currentFilterType: FilterType = FilterType.ALL

    enum class FilterType {
        ALL, BY_NAME, BY_CAR
    }

    override fun navigateToStationDetail(stationName: String) {
        navigateToAnotherFragment(stationName)
    }
    fun navigateToAnotherFragment(selectedStationName: String) {
        val fragment = StationDetailFragment()

        val bundle = Bundle()
        bundle.putString("stationName", selectedStationName)
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindStationBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        adapter = MyAdapter({ selectedStationName ->
            navigateToAnotherFragment(selectedStationName)
        }, this)

        binding.recyclerViewStation.adapter = adapter
        binding.recyclerViewStation.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonSearch.setOnClickListener {
            currentFilterType = FilterType.BY_NAME
            updateStations()
        }

        binding.buttonSearchOnCar.setOnClickListener {
            currentFilterType = FilterType.BY_CAR
            updateStations()
        }

        val usercars = binding.textViewUserCar.text
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.value as Map<String, Any>

                        if (user.containsKey("usercar")) {
                            val userCars = user["usercar"] as List<String>

                            if (userCars.isNotEmpty()) {
                                val carString = userCars.joinToString(", ")
                                binding.textViewUserCar.text = carString
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FindStationFragment", "Database Error: ${databaseError.message}")
                }
            })
        }
        updateStations()
        return binding.root
    }

    private fun updateStations() {
        when (currentFilterType) {
            FilterType.BY_NAME -> searchStationsByName()
            FilterType.BY_CAR -> filterStationsByCarChargerType()
            FilterType.ALL -> fetchAndDisplayAllStations()

        }
    }

    private fun fetchAndDisplayAllStations() {
        val stationRef = database.child("Station")
        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stationList = ArrayList<StationData>()
                for (stationSnapshot in dataSnapshot.children) {
                    val stationName = stationSnapshot.child("Name").getValue(String::class.java) ?: ""
                    val name = stationSnapshot.key ?: ""
                    val openTime = stationSnapshot.child("OpenTime").getValue(String::class.java) ?: ""
                    val closeTime = stationSnapshot.child("CloseTime").getValue(String::class.java) ?: ""
                    val stationData = StationData(stationName, name, openTime, closeTime)
                    stationList.add(stationData)
                }

                adapter.setData(stationList)
                binding.recyclerViewStation.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("HomeFragment", "Database Error: ${databaseError.message}")
            }
        })
    }

    private fun searchStationsByName() {
        val stationNameQuery = binding.editTextFindStationName.text.toString().trim()

        if (stationNameQuery.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a station name", Toast.LENGTH_SHORT).show()
            return
        }

        val stationRef = database.child("Station")

        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stations = mutableListOf<StationData>()

                for (snapshot in dataSnapshot.children) {
                    val stationName = snapshot.child("Name").getValue(String::class.java) ?: ""
                    val name = snapshot.key ?: ""
                    val openTime = snapshot.child("OpenTime").getValue(String::class.java) ?: ""
                    val closeTime = snapshot.child("CloseTime").getValue(String::class.java) ?: ""
                    if (stationName.contains(stationNameQuery, ignoreCase = true)) {
                        val stationData = StationData(stationName, name, openTime, closeTime)
                        stations.add(stationData)
                    }
                }

                adapter.setData(stations)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun filterStationsByCarChargerType() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.value as Map<String, Any>
                        val userCars = user["usercar"] as List<String>
                        val userCarChargerTypes = getUserCarChargerTypes(userCars)

                        filterStationsByChargerTypes(userCarChargerTypes)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FindStationFragment", "Database Error: ${databaseError.message}")
                }
            })
        }
    }

    private fun getUserCarChargerTypes(userCars: List<String>): List<String> {
        val userCarChargerTypes = mutableListOf<String>()
        val carChargerTypes = mapOf(
            "Audi e-tron" to "CCS Combo 2",
            "Audi e-tron GT" to "CCS Combo 2",
            "BMW i4" to "CCS Combo 2",
            "BMW iX" to "CCS Combo 2",
            "BMW iX3" to "CCS Combo 2",
            "Chevrolet Bolt EUV" to "CCS Combo 2",
            "Chevrolet Bolt EV" to "CCS Combo 2",
            "Ford Mustang Mach-E" to "CCS Combo 2",
            "Hyundai Ioniq 5" to "CCS Combo 2",
            "Hyundai Kona Electric" to "CCS Combo 2",
            "Kia EV6" to "CCS Combo 2",
            "Kia Niro EV" to "CCS Combo 2",
            "Mercedes-Benz EQE" to "CCS Combo 2",
            "Mercedes-Benz EQE SUV" to "CCS Combo 2",
            "Mercedes-Benz EQS" to "CCS Combo 2",
            "Mercedes-Benz EQS SUV" to "CCS Combo 2",
            "Nissan Leaf" to "CCS Combo 2",
            "Tesla Model 3" to "TeslaSupercharger",
            "Tesla Model S" to "TeslaSupercharger",
            "Tesla Model X" to "TeslaSupercharger",
            "Tesla Model Y" to "TeslaSupercharger",
            "Volkswagen ID4" to "CHAdeMo",
            "Volvo C40 Recharge" to "CHAdeMo",
            "Volvo XC40 Recharge" to  "CHAdeMo"

        )

        for (car in userCars) {
            val chargerType = carChargerTypes[car]
            chargerType?.let {
                userCarChargerTypes.add(it)
            }
        }

        return userCarChargerTypes
    }

    private fun filterStationsByChargerTypes(userCarChargerTypes: List<String>) {
        val stationRef = database.child("Station")

        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val filteredStations = mutableListOf<StationData>()

                for (stationSnapshot in dataSnapshot.children) {
                    val stationChargerType = stationSnapshot.child("Chargertype").getValue(String::class.java) ?: ""
                    if (userCarChargerTypes.contains(stationChargerType)) {
                        val stationName = stationSnapshot.child("Name").getValue(String::class.java) ?: ""
                        val name = stationSnapshot.key ?: ""
                        val openTime = stationSnapshot.child("OpenTime").getValue(String::class.java) ?: ""
                        val closeTime = stationSnapshot.child("CloseTime").getValue(String::class.java) ?: ""
                        val stationData = StationData(stationName, name, openTime, closeTime)
                        filteredStations.add(stationData)
                    }
                }

                adapter.setData(filteredStations)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FindStationFragment", "Database Error: ${databaseError.message}")
            }
        })
    }
}

