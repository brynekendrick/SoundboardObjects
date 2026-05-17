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

    private lateinit var edtFirstName: EditText
    private lateinit var edtMiddleName: EditText
    private lateinit var edtLastName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        edtFirstName = findViewById(R.id.edt_first_name)
        edtMiddleName = findViewById(R.id.edt_middle_name)
        edtLastName = findViewById(R.id.edt_last_name)
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.RegisterBtnPass)
        edtConfirmPassword = findViewById(R.id.RegisterBtnCpass)
        btnRegister = findViewById(R.id.RegisterBtn)

        btnRegister.setOnClickListener { performRegistration() }

        findViewById<TextView>(R.id.txt_register_google).setOnClickListener {
            Toast.makeText(this, "Google sign-in is not set up yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRegistration() {
        val firstName = edtFirstName.text.toString().trim()
        val middleName = edtMiddleName.text.toString().trim()
        val lastName = edtLastName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString()
        val confirmPassword = edtConfirmPassword.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showToast("First name and last name are required.")
            return
        }
        if (email.isEmpty() || !email.contains("@")) {
            showToast("Please enter a valid email.")
            return
        }
        if (password.length < 6) {
            showToast("Password must be at least 6 characters.")
            return
        }
        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return
        }

        btnRegister.isEnabled = false
        showToast("Creating account...")

        UserSession.firebaseRegister(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName.ifBlank { null },
            onSuccess = {
                val displayName = listOfNotNull(
                    firstName,
                    middleName.takeIf { it.isNotBlank() },
                    lastName
                ).joinToString(" ")
                UserSession.onFirebaseAuthSuccess(this, email, displayName)
                showToast("Registered! Profile saved to Firestore.")
                startActivity(
                    Intent(this, SoundboardActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
                finish()
            },
            onError = { message ->
                btnRegister.isEnabled = true
                showToast(message)
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
