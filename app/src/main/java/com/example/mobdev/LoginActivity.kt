package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.soundboard.SoundboardActivity

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val usernameEditText: EditText = findViewById(R.id.edittxt_username)
        val passwordEditText: EditText = findViewById(R.id.edittxt_password)
        val loginButton: Button = findViewById(R.id.button_login)
        val forgotPasswordText: TextView = findViewById(R.id.txt_editpassword)
        val registerText: TextView = findViewById(R.id.LoginRegister)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                UserSession.onLogin(this, username)
                val intent = Intent(this, SoundboardActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        registerText.setOnClickListener {
            Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
