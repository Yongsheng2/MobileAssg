package com.tarc.edu.etrack.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tarc.edu.etrack.MainActivity
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddressRegister)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPasswordRegister)
        val rePasswordEditText = findViewById<EditText>(R.id.editTextTextRePasswordRegister)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val tologinButton = findViewById<Button>(R.id.buttontoLg)

        firebaseAuth = FirebaseAuth.getInstance()

        tologinButton.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val rePassword = rePasswordEditText.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                showToast("All fields are required.")
                return@setOnClickListener
            }

            val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9]).{6,}\$".toRegex()
            if (!passwordPattern.matches(password)) {
                showToast("Password must contain at least one uppercase letter, one number, and be at least 6 characters long.")
                return@setOnClickListener
            }

            if (password != rePassword) {
                showToast("Passwords do not match.")
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId != null) {
                        val userReference = databaseReference.child("users").child(userId)

                        val userData = HashMap<String, Any>()
                        userData["username"] = username
                        userData["email"] = email
                        userData["password"] = password

                        userReference.setValue(userData)
                    }
                    startActivity(Intent(this, MainActivity::class.java))

                    finish()
                }
                .addOnFailureListener { exception ->
                    showToast("Registration failed: ${exception.message}")
                }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

