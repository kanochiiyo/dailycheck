package com.dimsseung.dailycheck.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private lateinit var tv_detail_location: TextView
    private lateinit var btn_edit: Button
    private lateinit var btn_delete: Button
    private lateinit var tv_detail_mood: TextView

    // Data
    private var currentLogId: String? = null
    private var currentLog: DailyLog? = null


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
        tv_detail_location = findViewById(R.id.tv_detail_location)
        btn_edit = findViewById(R.id.btn_edit)
        btn_delete = findViewById(R.id.btn_delete)
        tv_detail_mood = findViewById(R.id.tv_detail_mood)

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

        btn_edit.setOnClickListener {
            // Pastikan currentLog dan ID-nya tidak null
            if (currentLog != null && currentLog!!.id != null) {
                // 1. Arahkan ke EditLogActivity
                val editIntent = Intent(this, EditLogActivity::class.java)

                // 2. Gunakan Kunci dari Companion Object
                editIntent.putExtra(EditLogActivity.EXTRA_LOG_ID, currentLog!!.id)
                editIntent.putExtra(EditLogActivity.EXTRA_TITLE, currentLog!!.title)
                editIntent.putExtra(EditLogActivity.EXTRA_CONTENT, currentLog!!.content)
                editIntent.putExtra(EditLogActivity.EXTRA_MOOD, currentLog!!.mood)

                // 3. Kirim lokasi jika ada
                if (currentLog!!.location != null) {
                    editIntent.putExtra(EditLogActivity.EXTRA_LAT, currentLog!!.location!!.latitude)
                    editIntent.putExtra(EditLogActivity.EXTRA_LON, currentLog!!.location!!.longitude)
                }
                startActivity(editIntent)
            } else {
                Toast.makeText(this, "Data log belum dimuat atau ID null, silakan tunggu...", Toast.LENGTH_SHORT).show()
            }
        }
        // delete
        btn_delete.setOnClickListener {
            if (currentLogId != null) {
                showDeleteConfirmationDialog(currentLogId!!)
            } else {
                Toast.makeText(this, "ID Log tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(logId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Log")
        builder.setMessage("Apakah kamu yakin ingin menghapus catatan ini?")

        // TOMBOL HAPUS
        builder.setPositiveButton("Hapus") { dialog, which ->
            deleteLogFromFirestore(logId)
        }
        // TOMBOL BATAL
        builder.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun deleteLogFromFirestore(logId: String) {
        db.collection("logs").document(logId)
            .delete()
            .addOnSuccessListener {
                Log.d("DELETE LOG", "DocumentSnapshot successfully deleted!")
                Toast.makeText(this, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish() // Tutup DetailActivity setelah berhasil hapus
            }
            .addOnFailureListener { e ->
                Log.w("DELETE LOG", "Error deleting document", e)
                Toast.makeText(this, "Gagal menghapus catatan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchLogDetails(currentLogId: String) {
        db.collection("logs").document(currentLogId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Konversi dokumen/data di Firebase ke objek DailyLog
                    val log = document.toObject(DailyLog::class.java)
                    if (log != null) {


                        log.id = document.id


                        this.currentLog = log
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
        tv_detail_mood.text = log.mood ?: "😐"

        if (log.createdAt != null) {
            val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
            tv_detail_date.text = formatter.format(log.createdAt)
        } else {
            tv_detail_date.text = "Tanggal tidak tersedia"
        }

        if (log.location != null) {
            // Format string dengan 6 angka di belakang koma
            val lat = String.format(Locale.US, "%.6f", log.location.latitude)
            val lon = String.format(Locale.US, "%.6f", log.location.longitude)

            tv_detail_location.text = "Lokasi: $lat, $lon"
            tv_detail_location.visibility = View.VISIBLE // Tampilkan TextView
        } else {
            tv_detail_location.visibility = View.GONE // Sembunyikan jika tidak ada lokasi
        }
    }

    // Panggil fetchLogDetails lagi saat kembali dari EditActivity
    override fun onResume() {
        super.onResume()
        if (currentLogId != null) {
            fetchLogDetails(currentLogId!!)
        }
    }
}