package com.example.cuisinesync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cuisinesync.databinding.ActivityLoginBinding
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.launch

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var realmApp: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Toast.makeText(this, "Welcome to CuisineSync!", Toast.LENGTH_SHORT).show()


        val myApplication = application as MyApplication
        realmApp = myApplication.realmApp


        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginbtn.setOnClickListener {
             // Disable the button to prevent multiple submissions
            lifecycleScope.launch {
                performLogin()

            }
        }

        binding.registerLogin.setOnClickListener {
            Log.d("LogInActivity", "Register button clicked") // Debug log
            navigateToRegisterActivity()
        }
    }

    private suspend fun performLogin() {
        val enteredUsername = binding.username.text.toString()
        val enteredPassword = binding.password.text.toString()

        if (enteredUsername.isNotBlank() && enteredPassword.isNotBlank()) {
            authenticateUser(enteredUsername, enteredPassword)
        } else {
            showToast("Please enter both username and password")
            binding.loginbtn.isEnabled = true // Re-enable the button if validation fails
        }
    }

    private suspend fun authenticateUser(email: String, password: String) {
        val emailPasswordCredentials = Credentials.emailPassword(email, password)
        try {
            val user = realmApp.login(emailPasswordCredentials)
            if (user.loggedIn) {
                navigateToHomePage()
            } else {
                showLoginFailedToast()
            }
        } catch (e: Exception) {
            showLoginFailedToast(e.message)
        }
        binding.loginbtn.isEnabled = true // Re-enable the button after attempt
    }

    private fun navigateToHomePage() {
        val homeIntent = Intent(this, RegisterActivity::class.java)
        startActivity(homeIntent)
        finish() // Close the login activity
    }

    private fun navigateToRegisterActivity() {
        Log.d("LogInActivity", "Navigating to RegisterActivity") // Debug log
        val intent = Intent(this, RegisterActivity::class.java)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("LogInActivity", "Failed to navigate to RegisterActivity", e)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoginFailedToast(error: String? = null) {
        val errorMessage = error ?: "Login Failed"
        runOnUiThread {
            Toast.makeText(this@LogInActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
