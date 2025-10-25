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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var et_register_email: EditText
    private lateinit var et_register_password: EditText
    private lateinit var et_confirm_password: EditText
    private lateinit var btn_register: Button
    private lateinit var tv_user_login: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        // Inisialisasi komponen view yang sudah di-define di atas sebagai props class
        et_register_email = findViewById(R.id.et_register_email)
        et_register_password = findViewById(R.id.et_register_password)
        et_confirm_password = findViewById(R.id.et_confirm_password)
        btn_register = findViewById(R.id.btn_register)
        tv_user_login = findViewById(R.id.tv_user_login)

        // kasih on click listener ketika pencet textview register
        tv_user_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // kasih on click listener ketika button login diclick
        btn_register.setOnClickListener {
            register()
        }

    }

    private fun register() {
        val email = et_register_email.text.toString()
        val password = et_register_password.text.toString()
        val confirm = et_confirm_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if(password != confirm) {
            Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("REGISTER SUCCESS", "createUserWithEmail:success")
                    val user = auth.currentUser

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    val exception = task.exception
                    if (exception != null) {
                        if (exception is FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "Password terlalu lemah", Toast.LENGTH_SHORT).show()
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                        } else if (exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Authentication failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Log.w("REGISTER FAILED", "createUserWithEmail:failure", task.exception)

                }
            }
    }
}