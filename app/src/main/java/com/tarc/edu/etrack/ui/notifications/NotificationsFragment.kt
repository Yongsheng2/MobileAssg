package com.tarc.edu.etrack.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tarc.edu.etrack.R

class NotificationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationsList = ArrayList<NotificationData>()
    private lateinit var notificationsRef: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_notifications, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerViewNotification)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(notificationsList)
        recyclerView.adapter = notificationAdapter

        // Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        notificationsRef = database.getReference("notifications")

        // Retrieve notifications from Firebase Realtime Database and add them to notificationsList
        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notificationsList.clear()
                for (notificationSnapshot in dataSnapshot.children) {
                    val notification = notificationSnapshot.getValue(NotificationData::class.java)
                    notification?.let {
                        notificationsList.add(it)
                    }
                }
                notificationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        return rootView
    }
}

