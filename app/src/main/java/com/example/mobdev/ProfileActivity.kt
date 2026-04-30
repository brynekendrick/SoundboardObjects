package com.example.soundboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobdev.EditProfileActivity
import com.example.mobdev.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class ProfileActivity : AppCompatActivity() {

    private lateinit var textUsername: TextView
    private lateinit var textMemberSince: TextView
    private lateinit var textSoundsPlayedCount: TextView
    private lateinit var textFavoritesCount: TextView
    private lateinit var textCreatedCount: TextView
    private lateinit var buttonEditProfile: Button
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        textUsername = findViewById(R.id.text_username)
        textMemberSince = findViewById(R.id.text_member_since)
        textSoundsPlayedCount = findViewById(R.id.text_sounds_played_count)
        textFavoritesCount = findViewById(R.id.text_favorites_count)
        textCreatedCount = findViewById(R.id.text_created_count)
        buttonEditProfile = findViewById(R.id.button_edit_profile)
        buttonLogout = findViewById(R.id.button_logout)

        textUsername.text = "User123"
        textMemberSince.text = "Member since: May 2023"
        textSoundsPlayedCount.text = "15"
        textFavoritesCount.text = "8"
        textCreatedCount.text = "5"

        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val logout = Intent(this, Class.forName("com.example.mobdev.LogoutActivity"))
            startActivity(logout)
            finish()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.SoundboardActivity"))
                        startActivity(intent)
                        finish()
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening Soundboard: ${e.message}", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.nav_favorites -> {
                    Toast.makeText(this, "Favorites feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile, do nothing
                    true
                }
                R.id.nav_settings -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.SettingsActivity"))
                        startActivity(intent)
                        finish()
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening Settings: ${e.message}", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                else -> false
            }
        }
    }
}