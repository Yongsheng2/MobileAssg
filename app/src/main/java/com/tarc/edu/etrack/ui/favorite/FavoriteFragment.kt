package com.tarc.edu.etrack.ui.favorite

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
import com.tarc.edu.etrack.databinding.FragmentFavoriteBinding
import com.tarc.edu.etrack.ui.station_details.StationData
import com.tarc.edu.etrack.ui.station_details.StationDetailFragment
import java.util.concurrent.atomic.AtomicInteger


class FavoriteFragment : Fragment(), StationNavigator {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        adapter = MyAdapter({ selectedStationName ->
            navigateToAnotherFragment(selectedStationName)
        }, this)

        binding.RecyclerFavouriteView.adapter = adapter
        binding.RecyclerFavouriteView.layoutManager = LinearLayoutManager(requireContext())

        val completedTasks = AtomicInteger(0)
        val userId = auth.currentUser?.uid ?: ""
        database.child("users").child(userId).child("favorites")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totalFavorites = dataSnapshot.childrenCount.toInt()
                    val stationList = ArrayList<StationData>()

                    for (favoriteSnapshot in dataSnapshot.children) {
                        val isFavorite = favoriteSnapshot.getValue(Boolean::class.java) == true
                        val stationName = favoriteSnapshot.key ?: ""

                        if (isFavorite) {
                            database.child("Station").child(stationName)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(stationSnapshot: DataSnapshot) {
                                        val name = stationSnapshot.key ?: ""
                                        val stationName = stationSnapshot.child("Name").getValue(String::class.java) ?: ""
                                        val openTime = stationSnapshot.child("OpenTime").getValue(String::class.java) ?: ""
                                        val closeTime = stationSnapshot.child("CloseTime").getValue(String::class.java) ?: ""
                                        val stationData = StationData(stationName, name, openTime, closeTime)

                                        stationList.add(stationData)

                                        if (completedTasks.incrementAndGet() == totalFavorites) {
                                            adapter.setData(stationList)
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Log.e("FavouriteFragment", "Database Error: ${databaseError.message}")
                                    }
                                })
                        } else {
                            completedTasks.incrementAndGet()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FavouriteFragment", "Database Error: ${databaseError.message}")
                }
            })

        return binding.root
    }
}