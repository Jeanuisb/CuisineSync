package com.example.cuisinesync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.launch


class LogInActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginbtn: Button
    private lateinit var registerbtn: Button

    private lateinit var realmApp: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Toast.makeText(this@LogInActivity,"abc",Toast.LENGTH_SHORT).show()


        val myApplication = application as MyApplication
        realmApp = myApplication.realmApp

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginbtn = findViewById(R.id.loginbtn)
        registerbtn = findViewById(R.id.register_login)
        Log.e("LogInActivity", "Failed to navigate to RegisterActivity")

            registerbtn.setOnClickListener {
                // Code here executes on main thread after user presses button

                val intent = Intent(this@LogInActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

        loginbtn.setOnClickListener {
            // Code here executes on main thread after user presses button
            loginbtn.isEnabled = false // Disable the button until attempt is complete
            lifecycleScope.launch {
                performLogin()
            }
        }
    }





    private suspend fun performLogin() {
        val enteredUsername = username.text.toString()
        val enteredPassword = password.text.toString()

        if (enteredUsername.isNotBlank() && enteredPassword.isNotBlank()) {
            authenticateUser(enteredUsername, enteredPassword)
        } else {
            showToast("Please enter both username and password")
            loginbtn.isEnabled = true // Re-enable the button if validation fails
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
        loginbtn.isEnabled = true // Re-enable the button after attempt
    }

    private fun navigateToHomePage() {
        val homeIntent = Intent(this, HomePageActivity::class.java)
        startActivity(homeIntent)
        finish()
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
