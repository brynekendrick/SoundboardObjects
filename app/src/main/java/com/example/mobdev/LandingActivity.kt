package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.soundboard.SoundboardActivity

class LandingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val login = findViewById<Button>(R.id.button_login)
        val register = findViewById<Button>(R.id.button_register)
        val guest = findViewById<TextView>(R.id.text_guest)

        register.setOnClickListener {
            Toast.makeText(this, "Register your account here!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            Toast.makeText(this, "Login your account here!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        guest.setOnClickListener {
            UserSession.firebaseSignOut()
            UserSession.onGuestContinue(this)
            Toast.makeText(this, "Continuing as guest", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SoundboardActivity::class.java))
        }
    }
}