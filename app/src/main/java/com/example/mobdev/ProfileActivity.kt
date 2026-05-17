package com.example.soundboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobdev.EditProfileActivity
import com.example.mobdev.LogoutActivity
import com.example.mobdev.R
import com.example.mobdev.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        buttonEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        buttonLogout.setOnClickListener {
            startActivity(Intent(this, LogoutActivity::class.java))
            finish()
        }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        bindProfileFields()
    }

    private fun bindProfileFields() {
        textMemberSince.text = UserSession.getMemberSinceLabel(this)
        textSoundsPlayedCount.text = "—"
        textFavoritesCount.text = "—"
        textCreatedCount.text = UserSession.getCreatedSoundsCount(this).toString()
        textUsername.text = UserSession.getUsername(this)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_profile

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
                R.id.nav_profile -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
