package com.tarc.edu.etrack.ui.station_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tarc.edu.etrack.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StationDetailFragment : Fragment() {

    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stationdetail, container, false)

        val name = arguments?.getString("stationName")


        // Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val stationRef = database.getReference("Station/$name") // Adjust the reference path

        stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stationname = dataSnapshot.child("Name").getValue(String::class.java)
                val address = dataSnapshot.child("StationAddress").getValue(String::class.java)
                val openTime = dataSnapshot.child("OpenTime").getValue(String::class.java) ?: ""
                val closeTime = dataSnapshot.child("CloseTime").getValue(String::class.java) ?: ""

                val favouriteButton = rootView.findViewById<ImageButton>(R.id.favouritebutton)

                // Check if the user is authenticated
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid  // Assign the user's ID to userId

                    // Create a reference to the user's favorites node
                    val userFavoritesRef: DatabaseReference = database.getReference("users/$userId/favorites")

                    // Add a ValueEventListener to retrieve the favorite status for the specific station
                    userFavoritesRef.child(name.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            isFavorite = dataSnapshot.getValue(Boolean::class.java) ?: false

                            // Update the button's image resource based on isFavorite
                            if (isFavorite) {
                                favouriteButton.setImageResource(R.drawable.baseline_star_24)
                            } else {
                                favouriteButton.setImageResource(R.drawable.baseline_star_border_purple500_24)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle errors
                        }
                    })
                }

                stationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // Retrieve the "Chargertype" value from the Firebase data
                        val chargertype = dataSnapshot.child("Chargertype").getValue(String::class.java)

                        // Set the "Chargertype" value to the textViewChargertype
                        val textViewChargertype = rootView.findViewById<TextView>(R.id.textViewChargertype)
                        textViewChargertype.text = chargertype
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors
                    }
                })
                // Set up an OnClickListener for the favorite button
                favouriteButton.setOnClickListener {
                    if (user != null) {
                        val userId = user.uid  // Assign the user's ID to userId
                        // Create a reference to the user's favorites node
                        val userFavoritesRef: DatabaseReference = database.getReference("users/$userId/favorites")

                        if (isFavorite) {
                            // If already a favorite, set the value to false
                            userFavoritesRef.child(name.toString()).setValue(false)
                        } else {
                            // If not a favorite, set the value to true
                            userFavoritesRef.child(name.toString()).setValue(true)
                        }

                        // Toggle the isFavorite state
                        isFavorite = !isFavorite

                        // Update the button's image resource based on isFavorite
                        if (isFavorite) {
                            favouriteButton.setImageResource(R.drawable.baseline_star_24)
                        } else {
                            favouriteButton.setImageResource(R.drawable.baseline_star_border_purple500_24)
                        }
                    } else {
                        // User is not authenticated, handle this case (e.g., show a login prompt).
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

                // Load the image using FirebaseUI and Glide
                val storageRef = FirebaseStorage.getInstance().reference.child("StationImage")
                val imageViewDetail1 = rootView.findViewById<ImageView>(R.id.imageViewDetail1)

                // Set the image using FirebaseUI and Glide
                val imageRef = storageRef.child("$name.jpg")

                // Load the image using FirebaseUI and Glide
                val options = RequestOptions() // Add any desired options here
                val glide = Glide.with(requireContext())
                    .applyDefaultRequestOptions(options)

                glide.load(imageRef)
                    .into(imageViewDetail1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })

        // Firebase Storage for Images

        val stationImage1 = rootView.findViewById<ImageView>(R.id.imageViewDetail1)
        val stationImage2 = rootView.findViewById<ImageView>(R.id.imageViewDetail2)
        val stationImage3 = rootView.findViewById<ImageView>(R.id.imageViewDetail3)

        val storageRef = FirebaseStorage.getInstance().getReference().child("StationImage")

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)  // Cache images
            .error(R.drawable.error_image)  // Placeholder or error image

        // Load images using Glide from Firebase Storage
        val imageRef1 = storageRef.child("$name.jpg")
        val imageRef2 = storageRef.child("${name}1.jpg")
        val imageRef3 = storageRef.child("${name}2.jpg")

        Glide.with(this)
            .load(imageRef1)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOptions)
            .into(stationImage1)

        Glide.with(this)
            .load(imageRef2)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOptions)
            .into(stationImage2)

        Glide.with(this)
            .load(imageRef3)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOptions)
            .into(stationImage3)

        return rootView
    }

    private fun openWebBrowserWithAddress(address: String?) {
        if (address != null) {
            // Construct a URL to open in the browser
            val url = "https://www.google.com/maps?q=$address"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            val browserApps = requireActivity().packageManager.queryIntentActivities(intent, 0)

            if (browserApps.isNotEmpty()) {
                startActivity(intent)
            } else {
                // Handle the case where a web browser is not available
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

