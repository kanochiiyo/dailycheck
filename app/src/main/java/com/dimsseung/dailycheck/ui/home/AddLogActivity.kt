package com.dimsseung.dailycheck.ui.home

import android.content.Intent
import android.location.Location
import android.net.Uri
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
import com.dimsseung.dailycheck.utils.LocationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class AddLogActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var et_log_title: TextInputEditText
    private lateinit var et_log_content: TextInputEditText
    private lateinit var btn_add_log: Button
    private lateinit var toolbar_add_log: MaterialToolbar
    private lateinit var btn_get_location: Button
    private lateinit var btn_open_maps: Button
    private lateinit var tv_location_status: TextView

    // Utils
    private lateinit var locationHelper: LocationHelper

    private var last_location: Location? = null

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

        // Inisialisasi views dari layout
        et_log_title = findViewById(R.id.et_log_title)
        et_log_content = findViewById(R.id.et_log_content)
        btn_add_log = findViewById(R.id.btn_add_log)
        btn_get_location = findViewById(R.id.btn_get_location)
        btn_open_maps = findViewById(R.id.btn_open_maps)
        tv_location_status = findViewById(R.id.tv_location_status)

        // Inisialisasi Toolbar dan set sebagai action bar
        toolbar_add_log = findViewById(R.id.toolbar_add_log)
        setSupportActionBar(toolbar_add_log)

        // Panggil locationHelper (minta permission, cek permission)
        setupLocationHelper()

        // Set Listener untuk button
        btn_get_location.setOnClickListener {
            locationHelper.requestLocation()
        }
        btn_open_maps.setOnClickListener {
            openMaps()
        }
        btn_add_log.setOnClickListener {
            addLog()
        }
    }
    private fun setupLocationHelper() {
        locationHelper = LocationHelper(
            activity = this,
            onSuccess = { location ->
                last_location = location
                tv_location_status.text = "Lokasi berhasil ditambahkan!"
                btn_open_maps.isEnabled = true // <-- Cara disable/enable tombol yang benar
                btn_open_maps.visibility = View.VISIBLE
            },
            onFailure = { exception ->
                last_location = null
                tv_location_status.text = "Gagal mendapat lokasi: ${exception.message}"
                btn_open_maps.isEnabled = false
                btn_open_maps.visibility = View.GONE // Sembunyikan jika tidak relevan
            },
            onDenied = {
                last_location = null
                tv_location_status.text = "Izin lokasi ditolak"
                btn_open_maps.isEnabled = false
                btn_open_maps.visibility = View.GONE
            }
        )
    }

    private fun openMaps() {
        if (last_location != null) {
            // ambil dari firebase
            val latitude = last_location!!.latitude
            val longitude = last_location!!.longitude
            val gmmIntentUri =
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Lokasi Catatan)")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(
                    this,
                    "Tidak ada aplikasi peta yang ditemukan",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Lokasi belum ditambahkan.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun addLog() {
        val userId = auth.currentUser?.uid
        val logTitle = et_log_title.text.toString().trim()
        val logContent = et_log_content.text.toString().trim()

        // Validasi
        if (userId == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }
        if(logContent.isEmpty()) {
            et_log_content.error = "Content tidak boleh kosong"
            return
        }
        // Ambil dari Firebase
        val location_data: GeoPoint? = if (last_location != null) {
            GeoPoint(last_location!!.latitude, last_location!!.longitude)
        } else {
            null
        }

        val newLog = DailyLog(
            userId = userId,
            title = logTitle.ifBlank { null },
            content = logContent,
            location = location_data
        )

        db.collection("logs")
            .add(newLog)
            .addOnSuccessListener { documentReference ->
                Log.d("LOG ADDED", "DocumentSnapshot written with ID: ${documentReference.id}")
                Toast.makeText(this, "Log berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish() // <-- PERBAIKAN: Tutup activity setelah sukses
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Log gagal disimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("LOG ERROR", "Error adding document", e)
                btn_add_log.isEnabled = true // Hidupkan lagi tombolnya kalau gagal
            }
    }
}