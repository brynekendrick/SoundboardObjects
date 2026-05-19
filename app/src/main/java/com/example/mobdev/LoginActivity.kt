package com.example.mobdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.soundboard.SoundboardActivity

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val emailEditText: EditText = findViewById(R.id.edittxt_username)
        val passwordEditText: EditText = findViewById(R.id.edittxt_password)
        val loginButton: Button = findViewById(R.id.button_login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

            UserSession.firebaseLogin(
                email = email,
                password = password,
                onSuccess = {
                    UserSession.firebaseReadProfile(
                        onSuccess = { profile ->
                            UserSession.onFirebaseAuthSuccess(
                                this,
                                profile.email.ifBlank { email },
                                profile.displayName().ifBlank { email.substringBefore("@") },
                                profile.bio
                            )
                            goToSoundboard()
                        },
                        onError = {
                            UserSession.onFirebaseAuthSuccess(
                                this,
                                email,
                                email.substringBefore("@")
                            )
                            goToSoundboard()
                        }
                    )
                },
                onError = { message ->
                    loginButton.isEnabled = true
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            )
        }

        findViewById<TextView>(R.id.txt_editpassword).setOnClickListener {
            Toast.makeText(this, "Use Firebase Console to reset password for now.", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.LoginRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToSoundboard() {
        startActivity(Intent(this, SoundboardActivity::class.java))
        finish()
    }
}
