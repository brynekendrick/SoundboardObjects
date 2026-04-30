package com.example.soundboard

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.example.mobdev.EditProfileActivity
import com.example.mobdev.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupNavigationListeners()
        setupSettingsSwitches()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun setupNavigationListeners() {
        findViewById<LinearLayout>(R.id.layout_edit_profile).setOnClickListener {
            try {
                val intent = Intent(this, Class.forName("com.example.mobdev.EditProfileActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening Edit Profile: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        findViewById<LinearLayout>(R.id.layout_change_password).setOnClickListener {
            Toast.makeText(this, "Change Password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_privacy).setOnClickListener {
            Toast.makeText(this, "Privacy Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_theme).setOnClickListener {
            Toast.makeText(this, "Theme Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_downloads).setOnClickListener {
            Toast.makeText(this, "Downloads Management feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_audio_quality).setOnClickListener {
            Toast.makeText(this, "Audio Quality Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_about).setOnClickListener {
            Toast.makeText(this, "About App feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_rate).setOnClickListener {
            Toast.makeText(this, "Rate App feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.layout_help).setOnClickListener {
            Toast.makeText(this, "Help & Support feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSettingsSwitches() {
        // Notification switch
        val notificationSwitch = findViewById<SwitchCompat>(R.id.switch_notifications)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        val appVersionTextView = findViewById<TextView>(R.id.text_app_version)
        appVersionTextView.text = "1.0.0" // Would normally get from BuildConfig
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_settings

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.SoundboardActivity"))
                        startActivity(intent)
                        finish()
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening Soundboard: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                        false
                    }
                }
                R.id.nav_favorites -> {
                    Toast.makeText(this, "Favorites feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.ProfileActivity"))
                        startActivity(intent)
                        finish()
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening Profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                        false
                    }
                }
                R.id.nav_settings -> {
                    // Already on settings, do nothing
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}