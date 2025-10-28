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
import com.dimsseung.dailycheck.utils.LocationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class EditLogActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var et_log_title: TextInputEditText
    private lateinit var et_log_content: TextInputEditText
    private lateinit var btn_update_log: Button
    private lateinit var toolbar_edit_log: MaterialToolbar
    private lateinit var btn_get_location: Button
    private lateinit var btn_open_maps: Button
    private lateinit var tv_location_status: TextView

    private lateinit var tvMoodHappy: TextView
    private lateinit var tvMoodNeutral: TextView
    private lateinit var tvMoodSad: TextView
    private var selectedMood: String = "😐"

    // Utils
    private lateinit var locationHelper: LocationHelper
    private var currentLogId: String? = null
    private var existingLocation: GeoPoint? = null
    private var last_location: Location? = null // Lokasi baru jika user mengambil ulang

    // Kunci Intent
    companion object {
        const val EXTRA_LOG_ID = "EXTRA_LOG_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_CONTENT = "EXTRA_CONTENT"
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LON = "EXTRA_LON"
        const val EXTRA_MOOD = "EXTRA_MOOD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_log) // Set layout baru
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
        btn_update_log = findViewById(R.id.btn_update_log)
        btn_get_location = findViewById(R.id.btn_get_location)
        btn_open_maps = findViewById(R.id.btn_open_maps)
        tv_location_status = findViewById(R.id.tv_location_status)
        toolbar_edit_log = findViewById(R.id.toolbar_edit_log)
        tvMoodHappy = findViewById(R.id.tvMoodHappy)
        tvMoodNeutral = findViewById(R.id.tvMoodNeutral)
        tvMoodSad = findViewById(R.id.tvMoodSad)

        tvMoodHappy.setOnClickListener { updateMoodSelection("😄") }
        tvMoodNeutral.setOnClickListener { updateMoodSelection("😐") }
        tvMoodSad.setOnClickListener { updateMoodSelection("😢") }


        // Setup Toolbar
        setSupportActionBar(toolbar_edit_log)

        // Panggil locationHelper
        setupLocationHelper()

        // Ambil data dari Intent (Wajib ada)
        if (intent.hasExtra(EXTRA_LOG_ID)) {
            currentLogId = intent.getStringExtra(EXTRA_LOG_ID)
            val title = intent.getStringExtra(EXTRA_TITLE)
            val content = intent.getStringExtra(EXTRA_CONTENT)
            val mood = intent.getStringExtra(EXTRA_MOOD) ?: "😐"
            selectedMood = mood
            updateMoodSelection(selectedMood)

            // Set data ke UI
            et_log_title.setText(title)
            et_log_content.setText(content)

            // Cek jika ada lokasi lama
            if (intent.hasExtra(EXTRA_LAT)) {
                val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                val lon = intent.getDoubleExtra(EXTRA_LON, 0.0)
                existingLocation = GeoPoint(lat, lon)

                // Update UI lokasi
                tv_location_status.text = String.format("Lokasi tersimpan: %.6f, %.6f", lat, lon)
                btn_open_maps.isEnabled = true
                btn_open_maps.visibility = View.VISIBLE
            } else {
                tv_location_status.text = "Tidak ada lokasi tersimpan"
                btn_open_maps.visibility = View.GONE
            }

        } else {
            // Jika tidak ada ID, ini error. Tutup activity.
            Toast.makeText(this, "Error: Log ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set Listener untuk button
        btn_get_location.setOnClickListener {
            locationHelper.requestLocation()
        }
        btn_open_maps.setOnClickListener {
            openMaps()
        }
        btn_update_log.setOnClickListener {
            updateLog()
        }
    }

    private fun setupLocationHelper() {
        locationHelper = LocationHelper(
            activity = this,
            onSuccess = { location ->
                last_location = location
                tv_location_status.text = String.format("Lokasi baru: %.6f, %.6f", location.latitude, location.longitude)
                btn_open_maps.isEnabled = true
                btn_open_maps.visibility = View.VISIBLE
            },
            onFailure = { exception ->
                last_location = null
                tv_location_status.text = "Gagal mendapat lokasi: ${exception.message}"
            },
            onDenied = {
                last_location = null
                tv_location_status.text = "Izin lokasi ditolak"
            }
        )
    }

    private fun openMaps() {
        var locationToOpen: GeoPoint? = null
        // Prioritaskan lokasi baru jika ada
        if (last_location != null) {
            locationToOpen = GeoPoint(last_location!!.latitude, last_location!!.longitude)
        } else if (existingLocation != null) {
            locationToOpen = existingLocation
        }

        if (locationToOpen != null) {
            val latitude = locationToOpen.latitude
            val longitude = locationToOpen.longitude
            val gmmIntentUri =
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Lokasi Catatan)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Tidak ada aplikasi peta", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Lokasi belum ditambahkan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMoodSelection(mood: String) {
        selectedMood = mood

        tvMoodHappy.alpha = 0.5f
        tvMoodNeutral.alpha = 0.5f
        tvMoodSad.alpha = 0.5f

        when (mood) {
            "😄" -> tvMoodHappy.alpha = 1.0f
            "😐" -> tvMoodNeutral.alpha = 1.0f
            "😢" -> tvMoodSad.alpha = 1.0f
        }
    }

    private fun updateLog() {
        val logTitle = et_log_title.text.toString().trim()
        val logContent = et_log_content.text.toString().trim()

        if (logContent.isEmpty()) {
            et_log_content.error = "Content tidak boleh kosong"
            return
        }

        // Tentukan lokasi final
        val location_data: GeoPoint? = if (last_location != null) {
            // 1. User mengambil lokasi baru
            GeoPoint(last_location!!.latitude, last_location!!.longitude)
        } else if (existingLocation != null) {
            // 2. User tidak ambil lokasi baru, pakai lokasi lama
            existingLocation
        } else {
            // 3. User tidak punya lokasi lama & tidak ambil baru
            null
        }

        val updates = mutableMapOf<String, Any?>()
        updates["title"] = logTitle.ifBlank { null }
        updates["content"] = logContent
        updates["location"] = location_data
        updates["mood"] = selectedMood
        // Kita tidak update createdAt atau userId

        currentLogId?.let { id ->
            btn_update_log.isEnabled = false // Cegah double click
            db.collection("logs").document(id)
                .update(updates)
                .addOnSuccessListener {
                    Log.d("LOG UPDATED", "DocumentSnapshot updated with ID: $id")
                    Toast.makeText(this, "Log berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke DetailActivity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Log gagal diupdate: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("LOG ERROR", "Error updating document", e)
                    btn_update_log.isEnabled = true
                }
        } ?: run {
            Toast.makeText(this, "Error: ID Log tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}