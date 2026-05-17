package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.soundboard.SoundboardActivity


class LogoutActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.logout)

            val button_back = findViewById<Button>(R.id.btnCan)
            button_back.setOnClickListener {
                Toast.makeText(this, "Welcome Back!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SoundboardActivity::class.java)
                startActivity(intent)
            }

            val button_logout = findViewById<Button>(R.id.btnOut)
            button_logout.setOnClickListener {
                UserSession.firebaseSignOut()
                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LandingActivity::class.java)
                startActivity(intent)

                finish()
            }


        }
    }