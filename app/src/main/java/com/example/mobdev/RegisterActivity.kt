package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.soundboard.SoundboardActivity

class RegisterActivity : Activity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtRegisterGoogle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        edtUsername = findViewById(R.id.RegisterBtnUser)
        edtPassword = findViewById(R.id.RegisterBtnPass)
        edtConfirmPassword = findViewById(R.id.RegisterBtnCpass)
        btnRegister = findViewById(R.id.RegisterBtn)
        txtRegisterGoogle = findViewById(R.id.txt_register_google)

        btnRegister.setOnClickListener {
            performRegistration()
        }

        txtRegisterGoogle.setOnClickListener {
            Toast.makeText(this, "Google Registration Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRegistration() {
        val username = edtUsername.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        if (username.isEmpty()) {
            showToast("Please enter a username.")
            return
        }

        if (password.isEmpty()) {
            showToast("Please enter a password.")
            return
        }

        if (confirmPassword.isEmpty()) {
            showToast("Please confirm your password.")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return
        }

        registerUser(username, password)
    }

    private fun registerUser(username: String, password: String) {
        UserSession.onRegister(this, username)
        showToast("Registration successful for $username")
        startActivity(
            Intent(this, SoundboardActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}