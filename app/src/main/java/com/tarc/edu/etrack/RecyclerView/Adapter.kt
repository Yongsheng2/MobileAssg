package com.tarc.edu.etrack.RecyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.databinding.ItemLayoutBinding
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tarc.edu.etrack.ui.home.HomeFragment
import com.tarc.edu.etrack.ui.station_details.StationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapter(private val onItemClick: (String) -> Unit, private val homeFragment: HomeFragment) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var stationList: List<StationData> = emptyList()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference.child("StationImage")

    inner class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(station: StationData) {
            binding.textViewStationTitle.text = station.stationName
            // Create a reference to the image in Firebase Storage
            val imageRef = storageRef.child("${station.name}.jpg")

            binding.buttonStationDetails.setOnClickListener {
                val name = station.name // Get the name from the station
                homeFragment.navigateToAnotherFragment(name)
            }
            // Load the image from the Firebase Storage reference
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageStorageUrl = uri.toString()

                Glide.with(binding.root)
                    .load(imageStorageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.imageViewStation)

                // Calculate and set the status based on OpenTime and CloseTime
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val openTime = station.openTime
                val closeTime = station.closeTime

                if (isStationOpen(currentTime, openTime, closeTime)) {
                    binding.textViewStationStatus.text = "Open"
                } else {
                    binding.textViewStationStatus.text = "Closed"
                }

            }.addOnFailureListener { exception ->
                // Handle any errors that occur while loading the image
                Log.e("MyAdapter", "Image download failed: ${exception.message}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stationList[position]
        holder.bind(station)
    }

    override fun getItemCount(): Int {
        return stationList.size
    }

    fun setData(data: List<StationData>) {
        stationList = data
        notifyDataSetChanged()
    }

    private fun isStationOpen(currentTime: String, openTime: String, closeTime: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeDate = sdf.parse(currentTime)
        val openTimeDate = sdf.parse(openTime)
        val closeTimeDate = sdf.parse(closeTime)

        return currentTimeDate.after(openTimeDate) && currentTimeDate.before(closeTimeDate)
    }


}

