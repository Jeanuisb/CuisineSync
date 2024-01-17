package com.example.cuisinesync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LogInActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginbtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        loginbtn = findViewById<Button>(R.id.loginbtn)

        loginbtn.setOnClickListener {
            val username = username.text.toString()
            val password = password.text.toString()

            if (username == "admin" && password == "admin") {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }



    }
}