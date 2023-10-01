package com.tarc.edu.etrack.ui.station_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.ui.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StationDetailFragment : Fragment() {

    private var isFavorite = false
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageRef: StorageReference = storage.reference.child("StationImage")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stationdetail, container, false)
        val name = arguments?.getString("stationName")

        val database = FirebaseDatabase.getInstance()
        val stationRef = database.getReference("Station/$name")

        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stationname = dataSnapshot.child("Name").getValue(String::class.java)
                val address = dataSnapshot.child("StationAddress").getValue(String::class.java)
                val openTime = dataSnapshot.child("OpenTime").getValue(String::class.java) ?: ""
                val closeTime = dataSnapshot.child("CloseTime").getValue(String::class.java) ?: ""

                val favouriteButton = rootView.findViewById<ImageButton>(R.id.favouritebutton)

                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid
                    val userFavoritesRef: DatabaseReference = database.getReference("users/$userId/favorites")

                    userFavoritesRef.child(name.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            isFavorite = dataSnapshot.getValue(Boolean::class.java) ?: false

                            if (isFavorite) {
                                favouriteButton.setImageResource(R.drawable.baseline_star_24)
                            } else {
                                favouriteButton.setImageResource(R.drawable.baseline_star_border_purple500_24)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }

                val spinnerPendingStation = rootView.findViewById<Spinner>(R.id.spinnerPendingStation)
                val places = arrayOf("Place1", "Place2", "Place3", "Place4")

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, places)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPendingStation.adapter = adapter

                val backButton = view?.findViewById<Button>(R.id.buttonBack)

                backButton?.setOnClickListener{
                    val homefragment = HomeFragment()

                    val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.fragment_container, homefragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                val buttonPending = rootView.findViewById<Button>(R.id.buttonPending)
                buttonPending.setOnClickListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        val selectedPlace = spinnerPendingStation.selectedItem.toString()
                        val pendingPlaceRef = stationRef.child("PendingPlace").child(selectedPlace)
                        val statusRef = stationRef.child("Status")

                        statusRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val status = dataSnapshot.value as String?

                                if (status == "Closed") {
                                    Toast.makeText(requireContext(), "Sorry, station is closed", Toast.LENGTH_SHORT).show()
                                } else {
                                    pendingPlaceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val currentValue = dataSnapshot.value as String?

                                            if (currentValue == null || currentValue == "none") {
                                                pendingPlaceRef.setValue(userId)
                                                Toast.makeText(requireContext(), "Reserved by you", Toast.LENGTH_SHORT).show()
                                            } else if (currentValue == userId) {
                                                Toast.makeText(requireContext(), "Already reserved by you", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(requireContext(), "Already reserved by other people", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })
                    } else {
                        Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                    }
                }


                stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val chargertype = dataSnapshot.child("Chargertype").getValue(String::class.java)
                        val textViewChargertype = rootView.findViewById<TextView>(R.id.textViewChargertype)
                        textViewChargertype.text = chargertype

                        Log.d("Chargertype", "Chargertype: $chargertype")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Firebase data retrieval error: ${error.message}")                    }
                })
                favouriteButton.setOnClickListener {
                    if (user != null) {
                        val userId = user.uid
                        val userFavoritesRef: DatabaseReference = database.getReference("users/$userId/favorites")

                        if (isFavorite) {
                            userFavoritesRef.child(name.toString()).setValue(false)
                        } else {
                            userFavoritesRef.child(name.toString()).setValue(true)
                        }

                        isFavorite = !isFavorite

                        if (isFavorite) {
                            favouriteButton.setImageResource(R.drawable.baseline_star_24)
                        } else {
                            favouriteButton.setImageResource(R.drawable.baseline_star_border_purple500_24)
                        }
                    } else {
                    }
                }

                val buttonMaps = rootView.findViewById<Button>(R.id.buttonMaps)

                buttonMaps.setOnClickListener {
                    val address = dataSnapshot.child("StationAddress").getValue(String::class.java)
                    openWebBrowserWithAddress(address)
                }


                val textViewName = rootView.findViewById<TextView>(R.id.textViewStationName)
                textViewName.text = stationname

                val textViewAddress = rootView.findViewById<TextView>(R.id.textViewStationAddress)
                textViewAddress.text = address

                val textViewStatus = rootView.findViewById<TextView>(R.id.textViewStatus)

                val currentTime = getCurrentDeviceTime()

                val stationOpen = isStationOpen(currentTime, openTime, closeTime)
                if (stationOpen) {
                    textViewStatus.text = "Open"
                } else {
                    textViewStatus.text = "Closed"
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val imageReference1 = storageRef.child("${name}.jpg")
        val imageReference2 = storageRef.child("${name}1.jpg")
        val imageReference3 = storageRef.child("${name}2.jpg")

        val imageViewDetail1 = rootView.findViewById<ImageView>(R.id.imageViewDetail1)
        val imageViewDetail2 = rootView.findViewById<ImageView>(R.id.imageViewDetail2)
        val imageViewDetail3 = rootView.findViewById<ImageView>(R.id.imageViewDetail3)

        imageReference1.downloadUrl.addOnSuccessListener { uri ->
            val imageStorageUrl = uri.toString()

            Glide.with(this)
                .load(imageStorageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageViewDetail1)
        }
        imageReference2.downloadUrl.addOnSuccessListener { uri ->
            val imageStorageUrl = uri.toString()

            Glide.with(this)
                .load(imageStorageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageViewDetail2)
        }
        imageReference3.downloadUrl.addOnSuccessListener { uri ->
            val imageStorageUrl = uri.toString()

            Glide.with(this)
                .load(imageStorageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageViewDetail3)
        }
        return rootView
    }

    private fun openWebBrowserWithAddress(address: String?) {
        if (address != null) {
            val url = "https://www.google.com/maps?q=$address"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            val browserApps = requireActivity().packageManager.queryIntentActivities(intent, 0)

            if (browserApps.isNotEmpty()) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "No web browser found to open the address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isStationOpen(currentTime: String, openTime: String, closeTime: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeDate = sdf.parse(currentTime)
        val openTimeDate = sdf.parse(openTime)
        val closeTimeDate = sdf.parse(closeTime)

        return currentTimeDate.after(openTimeDate) && currentTimeDate.before(closeTimeDate)
    }
    private fun getCurrentDeviceTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeDate = Date(currentTimeMillis)
        return sdf.format(currentTimeDate)
    }

}
