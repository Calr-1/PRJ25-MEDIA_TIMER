package com.example.mediatimerjp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.mediatimerjp.R
import com.example.mediatimerjp.model.TimerWrapper
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var resetButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimerWrapper.getInstance().loadTheme(this)
        setContentView(R.layout.activity_reset_password)
        etEmail = findViewById(R.id.resetPassEmail)
        resetButton = findViewById(R.id.resetButton)
        progressBar = findViewById(R.id.progressBar3)
        mAuth = FirebaseAuth.getInstance()
        resetButton.setOnClickListener({ resetPassword() })
    }

    private fun resetPassword() {
        val email = etEmail.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            etEmail.error = "Email is required!"
            etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please provide valid email!"
            etEmail.requestFocus()
            return
        }
        mAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                val isNewUser = task.result!!.signInMethods!!.isEmpty()
                if (isNewUser) {
                    progressBar.visibility = View.VISIBLE
                    mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Password reset email has been sent!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val handler = Handler()
                                handler.postDelayed({
                                    startActivity(
                                        Intent(
                                            this,
                                            LogInActivity::class.java
                                        )
                                    )
                                }, 1000)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Try again! Something wrong happened",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    etEmail.error = "Email does not have an account linked!"
                }
            }
    }
}