package com.tarc.edu.etrack.ui.find_station

import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.RecyclerView.MyAdapter
import com.tarc.edu.etrack.RecyclerView.StationNavigator
import com.tarc.edu.etrack.databinding.FragmentFindStationBinding
import com.tarc.edu.etrack.ui.station_details.StationData
import com.tarc.edu.etrack.ui.station_details.StationDetailFragment
import java.util.ArrayList

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
        val fragment = StationDetailFragment() // Replace with the actual name of the fragment you want to navigate to.

        // Pass the selectedStationName to the new fragment
        val bundle = Bundle()
        bundle.putString("stationName", selectedStationName)
        fragment.arguments = bundle

        // Use FragmentTransaction to navigate to the new fragment
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment) // Replace 'fragmentContainer' with your actual container ID
        transaction.addToBackStack(null) // Add to back stack if needed
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
                // Handle errors
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

                    // Check if the stationName contains the search query (case-insensitive)
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

    }


}

