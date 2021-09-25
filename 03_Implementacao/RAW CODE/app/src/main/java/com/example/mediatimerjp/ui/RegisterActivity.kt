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
import com.example.mediatimerjp.database.User
import com.example.mediatimerjp.model.TimerWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var register: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimerWrapper.getInstance().loadTheme(this)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        register = findViewById(R.id.resetButton)
        register.setOnClickListener {
            registerUser()
        }
        etName = findViewById(R.id.nameEditText)
        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        etConfirm = findViewById(R.id.confirmPasswordText)
        progressBar = findViewById(R.id.progressBar3)
    }


    private fun registerUser() {
        val name = etName.text.toString().trim { it <= ' ' }
        val email: String = etEmail.text.toString().trim { it <= ' ' }
        val password: String = etPassword.text.toString().trim { it <= ' ' }
        val confirm: String = etConfirm.text.toString().trim { it <= ' ' }
        if (name.isEmpty()) {
            etName.error = "Name is required!"
            etName.requestFocus()
            return
        }
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
        if (password.isEmpty()) {
            etPassword.error = "Password is required!"
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be longer than 6 characters!"
            etPassword.requestFocus()
            return
        }
        if (confirm.isEmpty()) {
            etConfirm.error = "Please confirm your password!"
            etConfirm.requestFocus()
            return
        }
        if (confirm != password) {
            etConfirm.error = "Password confirmation does not match!"
            etConfirm.requestFocus()
            return
        }
        progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(name, email, ArrayList())
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Registered successfully! Verify your email to sign in!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                progressBar.visibility = View.GONE
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
                                    "Failed to register!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressBar.visibility = View.GONE
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to register!",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
    }
}