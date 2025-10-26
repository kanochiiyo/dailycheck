package com.dimsseung.dailycheck.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.ui.auth.LoginActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var tv_profile_email: TextView
    private lateinit var btn_logout: Button
    private lateinit var toolbar_profile: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()

        // Inisialiasi Views
        toolbar_profile = findViewById(R.id.toolbar_profile)
        tv_profile_email = findViewById(R.id.tv_profile_email)
        btn_logout = findViewById(R.id.btn_logout)

        // Setup Views
        setSupportActionBar(toolbar_profile)

        // Ambil data dari Firebase
        val user = auth.currentUser
        user?.let {
            val userEmail = user.email
            tv_profile_email.text = userEmail
        }

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Anda berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            // menghapus semua flag aktivitas sebelumnya
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}