package com.dimsseung.dailycheck.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dimsseung.dailycheck.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: TextView
    private lateinit var btn_logout: Button
    private lateinit var tv_latitude: TextView
    private lateinit var tv_longitude: TextView
    private lateinit var btn_get_location: Button
    private lateinit var btn_open_maps: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    //    membuat launcher yang minta permission itu
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions -> when {
        permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            getLastLocation()
        }
        permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else -> {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            btn_open_maps.isEnabled = false
        }
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        email = findViewById(R.id.tv_user_email)
        val user = auth.currentUser
        user?.let {
            val userEmail = user.email
            email.text = userEmail
        }

//      Inisialisasi komponen
        tv_latitude = findViewById(R.id.tv_latitude_value)
        tv_longitude = findViewById(R.id.tv_longitude_value)
        btn_get_location = findViewById(R.id.btn_get_location)
        btn_open_maps = findViewById(R.id.btn_open_maps)


        btn_logout = findViewById(R.id.btn_logout)
        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        btn_get_location.setOnClickListener {
            checkLocationPermission()
        }

        btn_open_maps.setOnClickListener {
            openMaps()
        }

        // biar pas launching langsung minta location
        checkLocationPermission()
    }


    // Function to check and request location permissions
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are already granted, proceed to get location
            getLastLocation()
        } else {
            // Permissions are not granted, request them
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getLastLocation() {
        // Double-check permissions before requesting location (important for API < 23 where permissions are granted at install)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // This case should ideally be handled by checkLocationPermission() first.
            // If we reach here, it means permissions were somehow revoked or not granted.
            Toast.makeText(
                this,
                "Location permissions not granted to fetch location.",
                Toast.LENGTH_SHORT
            ).show()
            btn_open_maps.isEnabled = false
            return
        }

        // Request the last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastKnownLocation = location
                    tv_latitude.text = String.format("%.6f", location.latitude)
                    tv_longitude.text = String.format("%.6f", location.longitude)
                    btn_open_maps.isEnabled = true // Enable maps button
                } else {
                    Toast.makeText(
                        this,
                        "Could not get last known location. Make sure GPS is on.",
                        Toast.LENGTH_LONG
                    ).show()
                    tv_latitude.text = "N/A"
                    tv_longitude.text = "N/A"
                    btn_open_maps.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_LONG)
                    .show()
                tv_latitude.text = "Error"
                tv_longitude.text = "Error"
                btn_open_maps.isEnabled = false
            }
    }

    private fun openMaps() {
        if (lastKnownLocation != null) {
            val latitude = lastKnownLocation!!.latitude
            val longitude = lastKnownLocation!!.longitude

            // Create a geo URI. The 'q' parameter is for a search query,
            // which can also be your coordinates to place a marker.
            val gmmIntentUri =
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(My Location)")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            // Try to force opening with Google Maps specifically.
            // If Google Maps is not installed, it won't crash but will return null.
//            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                // Google Maps app is available
                startActivity(mapIntent)
            } else {
                // Google Maps app not found, try to open with any available map app
                Toast.makeText(
                    this,
                    "Google Maps not found, trying generic map app.",
                    Toast.LENGTH_SHORT
                ).show()
                val genericMapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                if (genericMapIntent.resolveActivity(packageManager) != null) {
                    startActivity(genericMapIntent)
                } else {
                    // No map application found on the device
                    Toast.makeText(
                        this,
                        "No maps application found on this device.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            // Location not yet available
            Toast.makeText(
                this,
                "Location not available. Please click 'Get My Location' first.",
                Toast.LENGTH_SHORT
            ).show()
            btn_open_maps.isEnabled = false
        }
    }


}