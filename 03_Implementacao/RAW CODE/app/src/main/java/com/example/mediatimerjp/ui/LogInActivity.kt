package com.example.mediatimerjp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.model.TimerWrapper
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var register: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var signIn: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimerWrapper.getInstance().loadTheme(this)
        setContentView(R.layout.activity_log_in)
        val sharedPreference = getSharedPreferences("MediaTimerSaveUser", Context.MODE_PRIVATE)
        val uID = sharedPreference.getString("uID" ,"")
        if (uID != ""){
            if (uID != null) {
                Database.setUserId(uID)
                Database.getDatabase()
                val runnable = Runnable {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                Handler().postDelayed(runnable, 3000)
            }
        }
        else{
            findViewById<Group>(R.id.loginGroup).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.background).background = null
        }
        mAuth = FirebaseAuth.getInstance()
        register = findViewById(R.id.registerTextView)
        register.setOnClickListener(this)
        signIn = findViewById(R.id.loginButton)
        signIn.setOnClickListener(this)
        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        forgotPassword = findViewById(R.id.forgotpasswordTextView)
        forgotPassword.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.registerTextView -> startActivity(Intent(this, RegisterActivity::class.java))
            R.id.loginButton -> userLogin()
            R.id.forgotpasswordTextView -> startActivity(
                Intent(
                    this,
                    ResetPasswordActivity::class.java
                )
            )
        }
    }

    private fun userLogin() {
        val email = etEmail.text.toString().trim { it <= ' ' }
        val password: String = etPassword.text.toString().trim { it <= ' ' }
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
        signIn(email, password)
    }

    private fun signIn(email:String, password:String){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user!!.isEmailVerified) {
                        findViewById<Group>(R.id.loginGroup).visibility = View.GONE
                        findViewById<ConstraintLayout>(R.id.background).setBackgroundResource(R.drawable.app_background)
                        Database.getDatabase()
                        val runnable = Runnable {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        Handler().postDelayed(runnable, 3000)


                    } else {
                        user.sendEmailVerification()
                        Toast.makeText(
                            this,
                            "Check your email to verify your account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to login! Check your credentials",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}