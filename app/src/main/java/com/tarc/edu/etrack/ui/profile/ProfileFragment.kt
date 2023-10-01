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

        if (auth.currentUser == null) {
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        } else {
        }

        val userinfoButton = view.findViewById<Button>(R.id.buttonUserInfo)

        userinfoButton.setOnClickListener{
            val userinfofragment = UserInfoFragment()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, userinfofragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val addcarButton = view.findViewById<Button>(R.id.buttonAddCar)

        addcarButton.setOnClickListener{
            val addcarfragment = AddCarFragment()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, addcarfragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)

        logoutButton.setOnClickListener {
            auth.signOut()

            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }
        return view
    }
}



