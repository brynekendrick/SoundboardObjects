package com.example.mobdev

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        setupToolbar()
        initializeViews()
        loadUserData()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Edit Profile"
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initializeViews() {
        usernameEditText = findViewById(R.id.edit_username)
        emailEditText = findViewById(R.id.edit_email)
        bioEditText = findViewById(R.id.edit_bio)
        saveButton = findViewById(R.id.button_save)
        profileImageView = findViewById(R.id.profile_image)
    }

    private fun loadUserData() {
        usernameEditText.setText(UserSession.getUsername(this))
        val email = UserSession.getEmail(this)
        emailEditText.setText(if (email.isBlank()) "" else email)
        val bio = UserSession.getBio(this)
        bioEditText.setText(
            if (bio.isBlank()) "I love creating and sharing sound effects!" else bio
        )
    }

    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveUserData()
        }

        profileImageView.setOnClickListener {
            Toast.makeText(this, "Change profile picture feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (username.isEmpty()) {
            usernameEditText.error = "Username cannot be empty"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            return
        }

        UserSession.saveProfile(this, username, email, bio)
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

        override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}