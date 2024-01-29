package com.example.cuisinesync


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider


class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var firstnameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmpasswordEditText: EditText
    private lateinit var dateofbirthEditText: EditText
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Realm app and ViewModel
        val realmApp = application as MyApplication // Replace with your actual App ID
        val userRepository = UserRepository(realmApp.realmApp)
        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        initializeUI()
        setupObservers()
    }

    private fun initializeUI() {
        supportActionBar?.title = "Register"

        // Initialize UI components
        emailEditText = findViewById(R.id.email)
        firstnameEditText = findViewById(R.id.first_name)
        lastnameEditText = findViewById(R.id.last_name)
        passwordEditText = findViewById(R.id.password)
        confirmpasswordEditText = findViewById(R.id.confirm_password)
        dateofbirthEditText = findViewById(R.id.date_of_birth)
        progressBar = findViewById(R.id.progressBar)

        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }
    }

    private fun setupObservers() {
        viewModel.registrationStatus.observe(this) { result ->
            progressBar.visibility = View.GONE
            result.fold(
                onSuccess = { updateUIOnSuccess() },
                onFailure = { e -> updateUIOnError("Registration failed: ${e.message}") }
            )
        }
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val firstName = firstnameEditText.text.toString().trim()
        val lastName = lastnameEditText.text.toString().trim()
        val dateOfBirth = dateofbirthEditText.text.toString().trim()

        if (password == confirmpasswordEditText.text.toString().trim()) {
            progressBar.visibility = View.VISIBLE
            // Construct UserData object
            val userData = UserRepository.UserData(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth
            )
            // Call viewModel to register user
            viewModel.registerUser(userData)
        } else {
            confirmpasswordEditText.error = "Passwords do not match"
        }
    }


    private fun validateInput(editText: EditText, errorMessage: String): Boolean {
        if (editText.text.toString().trim().isEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            editText.error = errorMessage
            editText.requestFocus()
            return false
        }
        return true
    }

    private fun validateForm(): Boolean {
        when {
            !validateInput(firstnameEditText, "Please enter your first name") -> return false
            !firstnameEditText.text.toString().matches(Regex("[a-zA-Z]+")) -> {
                firstnameEditText.error = "Invalid first name"
                return false
            }

            !validateInput(lastnameEditText, "Please enter your last name") -> return false
            !lastnameEditText.text.toString().matches(Regex("[a-zA-Z]+")) -> {
                lastnameEditText.error = "Invalid last name"
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches() -> {
                val errorMessage = "Please enter a valid email"
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                emailEditText.error = errorMessage
                emailEditText.requestFocus()
                return false
            }

            !validateInput(passwordEditText, "Please enter your password") -> return false
            passwordEditText.text.toString().length < 8 -> {
                val errorMessage = "Password must be at least 8 characters"
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                passwordEditText.error = errorMessage
                passwordEditText.requestFocus()
                return false
            }

            passwordEditText.text.toString() != confirmpasswordEditText.text.toString() -> {
                confirmpasswordEditText.error = "Passwords do not match"
                return false
            }

            !validateInput(dateofbirthEditText, "Please enter your date of birth") -> return false
        }
        return true
    }

    private fun updateUIOnSuccess() {
        progressBar.visibility = View.GONE
        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

        // Start the LogInActivity
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)

        // Optionally, you can finish the current activity if you don't want users to return to it
        finish()
    }


    private fun updateUIOnError(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


}