package com.tarc.edu.etrack.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.RecyclerView.MyAdapter
import com.tarc.edu.etrack.RecyclerView.StationNavigator
import com.tarc.edu.etrack.databinding.FragmentHomeBinding
import com.tarc.edu.etrack.ui.station_details.StationData
import com.tarc.edu.etrack.ui.station_details.StationDetailFragment

class HomeFragment : Fragment(), StationNavigator {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: MyAdapter

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference.child("Station")


        adapter = MyAdapter({ selectedStationName ->
            navigateToAnotherFragment(selectedStationName)
        }, this)

        binding.recyclerViewStation.adapter = adapter
        binding.recyclerViewStation.layoutManager = LinearLayoutManager(requireContext())

        try {
            if (auth.currentUser != null) {
                val userId = auth.currentUser?.uid ?: ""
                database.child(userId).child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        binding.textViewWelcome.text = "Welcome to use E-track"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("HomeFragment", "Database Error: ${databaseError.message}")
                    }
                })
            } else {
                binding.textViewWelcome.text = "Login to E-track to access more features!"
            }

            database.addValueEventListener(object : ValueEventListener {
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
        } catch (e: Exception) {
            Log.e("HomeFragment", "Exception: ${e.message}")
        }
        return binding.root
    }
}