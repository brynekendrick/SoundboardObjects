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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth


class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val settingsRef = database.getReference("settings")

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
            startActivity(Intent(this, EditProfileActivity::class.java))
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
        val notificationSwitch = findViewById<SwitchCompat>(R.id.switch_notifications)

        val userId = auth.currentUser?.uid ?: return // temporary (we will replace later with login user)

        // LOAD saved value
        settingsRef.child(userId).child("notifications").get()
            .addOnSuccessListener {
                val savedValue = it.getValue(Boolean::class.java) ?: false
                notificationSwitch.isChecked = savedValue
            }

        // SAVE value when changed
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsRef.child(userId)
                .child("notifications")
                .setValue(isChecked)

            val message = if (isChecked)
                "Notifications enabled"
            else
                "Notifications disabled"

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.text_app_version).text = "1.0.0"
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_settings

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(this, SoundboardActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    finish()
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(
                        Intent(this, SoundboardActivity::class.java)
                            .putExtra(SoundboardActivity.EXTRA_OPEN_FAVORITES, true)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
