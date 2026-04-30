package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobdev.R
import com.example.soundboard.SoundboardActivity

class DeveloperActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer)

        val button_landing = findViewById<ImageView>(R.id.btnPreviousDev)
        button_landing.setOnClickListener {
            Toast.makeText(this, "Welcome back to lobby!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SoundboardActivity::class.java)
                startActivity(intent)
        }
    }
}