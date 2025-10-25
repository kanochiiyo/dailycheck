package com.dimsseung.dailycheck.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var et_login_email: EditText
    private lateinit var et_login_password: EditText
    private lateinit var btn_login: Button
    private lateinit var tv_user_register: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        // Inisialisasi komponen view yang sudah di-define di atas sebagai props class
        et_login_email = findViewById(R.id.et_login_email)
        et_login_password = findViewById(R.id.et_login_password)
        btn_login = findViewById(R.id.btn_login)
        tv_user_register = findViewById(R.id.tv_user_register)

        // kasih on click listener ketika pencet textview register
        tv_user_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // kasih on click listener ketika button login diclick
        btn_login.setOnClickListener {
            login()
        }
    }

    private fun login() {

        val email = et_login_email.text.toString()
        val password = et_login_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LOGIN SUCCESS", "signInWithEmail:success")
                    val user = auth.currentUser

                    // arahkan ke HomeActivity jika berhasil login
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    val exception = task.exception
                    if (exception != null) {
                        if (exception is FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Authentication failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Log.w("REGISTER FAILED", "createUserWithEmail:failure", task.exception)

                    Log.w("LOGIN FAILED", "createUserWithEmail:failure", task.exception)
                }
            }
    }


}