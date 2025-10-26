package com.dimsseung.dailycheck.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.data.model.DailyLog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddLogActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var et_log_title: TextInputEditText
    private lateinit var et_log_content: TextInputEditText
    private lateinit var btn_add_log: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi element dari layout
        et_log_title = findViewById(R.id.et_log_title)
        et_log_content = findViewById(R.id.et_log_content)
        btn_add_log = findViewById(R.id.btn_add_log)

        btn_add_log.setOnClickListener {
            addLog()
        }
    }

    private fun addLog() {
        val userId = auth.currentUser?.uid
        val logTitle = et_log_title.text.toString()
        val logContent = et_log_content.text.toString()

        // Validasi
        if (userId == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }
        if(logContent.isEmpty()) {
            Toast.makeText(this, "Content tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val newLog = DailyLog(
            userId = userId,
            title = logTitle.ifBlank { null },
            content = logContent
        )
        db.collection("logs")
            .add(newLog)
            .addOnSuccessListener { documentReference ->
                Log.d("ADD LOG ADDED", "DocumentSnapshot written with ID: ${documentReference.id}")
                Toast.makeText(this, "Log berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Log gagal disimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("DATA LOG ERROR", "Error adding document", e)
            }
    }
}
