package com.dimsseung.dailycheck.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.data.model.DailyLog
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetailLogActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var toolbar_detail: MaterialToolbar
    private lateinit var tv_detail_title: TextView
    private lateinit var tv_detail_date: TextView
    private lateinit var tv_detail_content: TextView
    private lateinit var btn_edit: Button
    private lateinit var btn_delete: Button

    // Data
    private var currentLogId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi Views
        toolbar_detail = findViewById(R.id.toolbar_detail)
        tv_detail_title = findViewById(R.id.tv_detail_title)
        tv_detail_content = findViewById(R.id.tv_detail_content)
        tv_detail_date = findViewById(R.id.tv_detail_date)
        btn_edit = findViewById(R.id.btn_edit)
        btn_delete = findViewById(R.id.btn_delete)

        // Setup Toolbar
        setSupportActionBar(toolbar_detail)

        // Ambil LOG_ID dari Intent
        currentLogId = intent.getStringExtra("LOG_ID")

        if (currentLogId == null) {
            Toast.makeText(this, "LOG_ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            fetchLogDetails(currentLogId!!)
        }

        // edit
        // delete
    }

    private fun fetchLogDetails(currentLogId: String) {
        db.collection("logs").document(currentLogId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Konversi dokumen/data di Firebase ke objek DailyLog
                    val log = document.toObject(DailyLog::class.java)
                    if (log != null) {
                        populateUi(log)
                    } else {
                        Toast.makeText(this, "Gagal mengurai data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Catatan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    Log.d("DETAIL LOG ERROR", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("DETAIL LOG ERROR", "Error getting document", e)
            }
    }

    private fun populateUi(log: DailyLog) {
        // Set judul toolbar
        toolbar_detail.title = "Detail Log"

        // Set data
        tv_detail_title.text = log.title ?: "Catatan"
        tv_detail_content.text = log.content ?: ""
        if (log.createdAt != null) {
            val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
            tv_detail_date.text = formatter.format(log.createdAt)
        } else {
            tv_detail_date.text = "Tanggal tidak tersedia"
        }
    }
}