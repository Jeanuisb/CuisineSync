package com.example.cuisinesync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Button

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var firstnameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmpasswordEditText: EditText
    private lateinit var dateofbirthEditText: EditText

    private lateinit var progressBar : ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.title = "Register"

        Toast.makeText(this, "You Can Register Now ", Toast.LENGTH_LONG).show()

        emailEditText = findViewById(R.id.email)
        firstnameEditText = findViewById(R.id.first_name)
        lastnameEditText = findViewById(R.id.last_name)
        passwordEditText = findViewById(R.id.password)
        confirmpasswordEditText = findViewById(R.id.confirm_password)
        dateofbirthEditText = findViewById(R.id.date_of_birth)

        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            if (validateForm()) {
                val firstName = firstnameEditText.text.toString().trim()
                val lastName = lastnameEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                val dateOfBirth = dateofbirthEditText.text.toString().trim()

                registerUser(firstName, lastName, email, password, dateOfBirth)
            }
        }
    }

    private fun registerUser(lastnameEditText: String, firstnameEditText: String, emailEditText: String, passwordEditText: String, dateofbirthEditText: String) {
        //TODO: Welela to implement this
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
                val errorMessage = "Password must be at least 6 characters"
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




}