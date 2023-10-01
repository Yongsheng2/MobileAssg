package com.tarc.edu.etrack.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.databinding.FragmentProfileBinding
import com.tarc.edu.etrack.pending_station
import com.tarc.edu.etrack.ui.login.LoginActivity

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        binding = FragmentProfileBinding.bind(view)

        // Check if the user is authenticated
        if (auth.currentUser == null) {
            // User is not authenticated, navigate to the LoginActivity
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish() // Close the MainActivity
        } else {
            // User is authenticated, you can display the profile
        }

        val userinfoButton = view.findViewById<Button>(R.id.buttonUserInfo)

        userinfoButton.setOnClickListener{
            val userinfofragment = UserInfoFragment()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, userinfofragment)
            transaction.addToBackStack(null) // Optional, adds the transaction to the back stack
            transaction.commit()
        }

        val addcarButton = view.findViewById<Button>(R.id.buttonAddCar)

        addcarButton.setOnClickListener{
            val addcarfragment = AddCarFragment()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, addcarfragment)
            transaction.addToBackStack(null) // Optional, adds the transaction to the back stack
            transaction.commit()
        }

        val pendingButton = view.findViewById<Button>(R.id.buttontpdpg)

        pendingButton.setOnClickListener{
            val pendingfragment = pending_station()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, pendingfragment)
            transaction.addToBackStack(null) // Optional, adds the transaction to the back stack
            transaction.commit()
        }

        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)

        logoutButton.setOnClickListener {
            // Sign out the user
            auth.signOut()

            // Navigate to the LoginActivity
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish() // Close the MainActivity
        }


        return view
    }
}



