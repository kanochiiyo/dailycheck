package com.dimsseung.dailycheck.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.data.model.DailyLog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var rv_logs: RecyclerView
    private lateinit var fab_add_log: FloatingActionButton
    private lateinit var toolbar_home: MaterialToolbar

    private var logList = mutableListOf<DailyLog>()
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialiasai Views
        toolbar_home = findViewById(R.id.toolbar_home)
        rv_logs = findViewById(R.id.rv_logs)
        fab_add_log = findViewById(R.id.fab_add_log)

        // Setup Views
        setSupportActionBar(toolbar_home)
        setupRecyclerView()

        fab_add_log.setOnClickListener {
            startActivity(Intent(this, AddLogActivity::class.java))
        }

        // Ambil data dari Firestore
        fetchLogsFromFirestore()
    }

    private fun fetchLogsFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("logs")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get(   )
            .addOnSuccessListener { result ->
                // kosongkan biar data gak numpuk
                logList.clear()
                for (document in result) {
                    // Konversi tiap data ke data model DailyLog
                    val log = document.toObject(DailyLog::class.java)
                    log.id = document.id

                    // Masukkan objek log ke list utama
                    logList.add(log)

                }
                // Beri tau adapter ada data baru
                logAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("GET ERROR", "Error getting documents: ", exception)
            }
    }
    // penting biar list otomatis ke update ketika ada data baru
    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            fetchLogsFromFirestore()
        }
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter(logList) { selectedLog ->
            val detailIntent = Intent(this, DetailLogActivity::class.java)
            // kirim ID nya aja
            detailIntent.putExtra("LOG_ID", selectedLog.id)
            startActivity(detailIntent)
        }
        rv_logs.layoutManager = LinearLayoutManager(this)
        rv_logs.adapter = logAdapter
    }

}