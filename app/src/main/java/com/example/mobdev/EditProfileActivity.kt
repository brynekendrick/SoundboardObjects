package com.example.mobdev

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
        if (!UserSession.isFirebaseSignedIn()) {
            bindLocalProfile()
            return
        }
        UserSession.firebaseReadProfile(
            onSuccess = { profile ->
                usernameEditText.setText(profile.displayName())
                emailEditText.setText(profile.email)
                val bio = profile.bio
                bioEditText.setText(
                    if (bio.isNullOrBlank()) "I love creating and sharing sound effects!" else bio
                )
            },
            onError = { bindLocalProfile() }
        )
    }

    private fun bindLocalProfile() {
        usernameEditText.setText(UserSession.getUsername(this))
        emailEditText.setText(UserSession.getEmail(this))
        val bio = UserSession.getBio(this)
        bioEditText.setText(
            if (bio.isBlank()) "I love creating and sharing sound effects!" else bio
        )
    }

    private fun setupListeners() {
        saveButton.setOnClickListener { saveUserData() }
        findViewById<Button>(R.id.button_delete_profile).setOnClickListener {
            confirmDeleteProfile()
        }
        profileImageView.setOnClickListener {
            Toast.makeText(this, "Change profile picture feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteProfile() {
        if (!UserSession.isFirebaseSignedIn()) {
            Toast.makeText(this, "Sign in to delete cloud profile.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete cloud profile?")
            .setMessage("Removes your Firestore user document and saved sounds. Your login account stays in Firebase Auth.")
            .setPositiveButton("Delete") { _, _ ->
                UserSession.firebaseDeleteProfile(
                    onSuccess = {
                        Toast.makeText(this, "Cloud profile deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        if (!UserSession.isFirebaseSignedIn()) {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val parts = username.split(" ", limit = 2)
        val firstName = parts[0]
        val lastName = if (parts.size > 1) parts[1] else ""

        saveButton.isEnabled = false
        UserSession.firebaseSaveProfile(
            firstName = firstName,
            lastName = lastName,
            email = email,
            bio = bio,
            onSuccess = {
                Toast.makeText(this, "Profile saved to Firestore", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                saveButton.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
