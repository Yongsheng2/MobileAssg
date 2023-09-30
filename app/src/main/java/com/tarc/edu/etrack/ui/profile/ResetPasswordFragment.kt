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

    // Password validation regex pattern
    private val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9]).{6,}\$".toRegex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        resetButton = view.findViewById(R.id.resetButton)

        auth = FirebaseAuth.getInstance() // Initialize Firebase Authentication
        databaseReference = FirebaseDatabase.getInstance().reference // Initialize Firebase Realtime Database reference

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if the new password meets your validation criteria
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
            // Check if the new password matches the confirmed password
            if (newPassword == confirmPassword) {
                // Reauthenticate the user with their current password
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Reauthentication successful, update the password
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        showToast("Password updated successfully")

                                        // Update the new password in Firebase Realtime Database
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
        // Validate the password against the regex pattern
        return passwordPattern.matches(password)
    }
}