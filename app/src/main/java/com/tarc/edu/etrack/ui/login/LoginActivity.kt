package com.tarc.edu.etrack.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tarc.edu.etrack.MainActivity
import com.tarc.edu.etrack.R
import com.tarc.edu.etrack.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private var loginAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.editTextTextUsernameLogin)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPasswordLogin)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerButton = findViewById<Button>(R.id.buttongetRegister)

        firebaseAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Email and password are required.")
                return@setOnClickListener
            }
            val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9]).{6,}\$".toRegex()
            if (!passwordPattern.matches(password)) {
                showToast("Password must contain at least one uppercase letter, one number, and be at least 6 characters long.")
                return@setOnClickListener
            }

            if (loginAttempts >= 5) {
                showToast("Maximum login attempts reached. Please try again later.")
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    showToast("Login failed: ${exception.message}")
                    loginAttempts++
                }
        }
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

