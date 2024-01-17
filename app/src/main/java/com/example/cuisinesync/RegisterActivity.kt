package com.example.cuisinesync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private lateinit var password: EditText
    private lateinit var confirm_password: EditText
    private lateinit var dateofbirth: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.title = "Register"

        Toast.makeText(this, "You Can Register Now ", Toast.LENGTH_SHORT).show()

        email = findViewById<EditText>(R.id.email)
        firstname = findViewById<EditText>(R.id.first_name)
        lastname = findViewById<EditText>(R.id.last_name)
        password = findViewById<EditText>(R.id.password)
        confirm_password = findViewById<EditText>(R.id.confirm_password)
        dateofbirth = findViewById<EditText>(R.id.date_of_birth)

        Button registerbtn = findViewById<Button>(R.id.registerbtn)






    }
}