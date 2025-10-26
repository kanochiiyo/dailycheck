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
import com.google.firebase.firestore.SetOptions

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
    private var isEditMode = false
    private var editLogId: String? = null
    private var existingLocation: GeoPoint? = null

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

        if (intent.hasExtra("EDIT_LOG_ID")) {
            isEditMode = true
            editLogId = intent.getStringExtra("EDIT_LOG_ID")

            // Ambil data dari intent
            val title = intent.getStringExtra("EDIT_TITLE")
            val content = intent.getStringExtra("EDIT_CONTENT")

            // Set data ke UI
            toolbar_add_log.title = "Edit Log"
            et_log_title.setText(title)
            et_log_content.setText(content)
            btn_add_log.text = "Update Log"

            // Cek jika ada lokasi lama
            if (intent.hasExtra("EDIT_LOCATION_LAT")) {
                val lat = intent.getDoubleExtra("EDIT_LOCATION_LAT", 0.0)
                val lon = intent.getDoubleExtra("EDIT_LOCATION_LON", 0.0)
                existingLocation = GeoPoint(lat, lon)

                // Update UI lokasi
                tv_location_status.text = "Lokasi: $lat, $lon"
                btn_open_maps.isEnabled = true
                btn_open_maps.visibility = View.VISIBLE
            }

        } else {
            isEditMode = false
            toolbar_add_log.title = "Tambah Log"
            btn_add_log.text = "Simpan Log"
        }

        // Set Listener untuk button
        btn_get_location.setOnClickListener {
            locationHelper.requestLocation()
        }
        btn_open_maps.setOnClickListener {
            openMaps()
        }
        btn_add_log.setOnClickListener {
            saveOrUpdateLog()
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
        var locationToOpen: GeoPoint? = null
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
            // ... (sisanya sudah benar) ...
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Tidak ada aplikasi peta", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Lokasi belum ditambahkan.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun saveOrUpdateLog() {
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
        val location_data: GeoPoint?
        if (last_location != null) {
            // User mengambil lokasi baru
            location_data = GeoPoint(last_location!!.latitude, last_location!!.longitude)
        } else if (isEditMode && existingLocation != null) {
            // User mode edit dan tidak ambil lokasi baru, pakai lokasi lama
            location_data = existingLocation
        } else {
            // User mode tambah (tanpa lokasi) atau mode edit (dan menghapus lokasi)
            location_data = null
        }


        if (isEditMode) {
            // --- MODE UPDATE ---
            val updates = mutableMapOf<String, Any?>()
            updates["title"] = logTitle.ifBlank { null }
            updates["content"] = logContent
            updates["location"] = location_data
            // gausah ganti created at sama userId

            editLogId?.let { id ->
                db.collection("logs").document(id)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("LOG UPDATED", "DocumentSnapshot updated with ID: $id")
                        Toast.makeText(this, "Log berhasil diupdate", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Log gagal diupdate: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.w("LOG ERROR", "Error updating document", e)
                        btn_add_log.isEnabled = true
                    }
            } ?: run {
                // Ini akan dieksekusi jika editLogId ternyata null
                Toast.makeText(this, "Error: ID Log tidak ditemukan saat update", Toast.LENGTH_SHORT).show()
                btn_add_log.isEnabled = true
            }
        } else {
            // --- MODE ADD BARU ---
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
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Log gagal disimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("LOG ERROR", "Error adding document", e)
                    btn_add_log.isEnabled = true
                }
        }

    }
}