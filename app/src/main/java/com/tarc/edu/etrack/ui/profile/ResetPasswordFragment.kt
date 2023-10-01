package com.tarc.edu.etrack.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tarc.edu.etrack.R

class ResetPasswordFragment : Fragment() {

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9]).{6,}\$".toRegex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        resetButton = view.findViewById(R.id.changeButton)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (isPasswordValid(newPassword)) {
                resetPassword(currentPassword, newPassword, confirmPassword)
            } else {
                showToast("Password must contain at least one uppercase letter, one number, and be at least 6 characters long.")
            }
        }
    }
    private fun resetPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val user = auth.currentUser

        if (user != null) {
            if (newPassword == confirmPassword) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        showToast("Password updated successfully")
                                        val userId = user.uid
                                        databaseReference.child("users").child(userId).child("password").setValue(newPassword)
                                    } else {
                                        showToast("Password update failed")
                                    }
                                }
                        } else {
                            showToast("Reauthentication failed. Incorrect current password.")
                        }
                    }
            } else {
                showToast("Passwords do not match.")
            }
        } else {
            showToast("User not signed in.")
        }
    }

    private fun showToast(message: String) {
        val context = requireContext()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isPasswordValid(password: String): Boolean {
        return passwordPattern.matches(password)
    }
}