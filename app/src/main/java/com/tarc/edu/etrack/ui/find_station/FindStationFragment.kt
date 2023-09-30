package com.tarc.edu.etrack.ui.find_station

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

class FindStationFragment : Fragment(), StationNavigator {

    private lateinit var binding: FragmentFindStationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: MyAdapter
    private var currentFilterType: FilterType = FilterType.ALL

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindStationBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        adapter = MyAdapter({ selectedStationName ->
            // Handle item click
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

        // Fetch and display all stations initially
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
                val stations = mutableListOf<StationData>()
                for (snapshot in dataSnapshot.children) {
                    val station = snapshot.getValue(StationData::class.java)
                    if (station != null) {
                        stations.add(station)
                    }
                }
                adapter.setData(stations)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
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
        stationRef.orderByChild("Name").startAt(stationNameQuery).endAt(stationNameQuery + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val stations = mutableListOf<StationData>()
                    for (snapshot in dataSnapshot.children) {
                        val stationName = snapshot.child("Name").getValue(String::class.java) ?: ""
                        val name = snapshot.key ?: ""
                        val openTime = snapshot.child("OpenTime").getValue(String::class.java) ?: ""
                        val closeTime = snapshot.child("CloseTime").getValue(String::class.java) ?: ""
                        val stationData = StationData(stationName, name, openTime, closeTime)
                        stations.add(stationData)
                    }
                    adapter.setData(stations)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
                }
            })
    }
    enum class FilterType {
        ALL, BY_NAME, BY_CAR
    }

    private fun filterStationsByCarChargerType() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrieve the list of user's car names
        database.child("users").child(userId).child("usercar").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userCars = dataSnapshot.children.mapNotNull { it.key } // Filter out null values
                val textViewUserCar = view?.findViewById<TextView>(R.id.textViewUserCar)
                textViewUserCar?.text = "Your Text Here"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

    }

    private fun filterAndSetStationsByCarNames(carNames: List<String>) {
        val stationRef = database.child("Station")
        val stations = mutableListOf<StationData>()

        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val stationName = snapshot.child("Name").getValue(String::class.java) ?: ""
                    val name = snapshot.key ?: ""
                    val openTime = snapshot.child("OpenTime").getValue(String::class.java) ?: ""
                    val closeTime = snapshot.child("CloseTime").getValue(String::class.java) ?: ""
                    val chargerType = snapshot.child("Chargertype").getValue(String::class.java) ?: ""

                    // Check if the station's charger type is in the user's car charger types
                    if (chargerType in carNames) {
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

    override fun navigateToStationDetail(stationName: String) {
        // Handle the navigation
    }
}
