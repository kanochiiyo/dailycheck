package com.dimsseung.dailycheck.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

class LocationHelper(
    private val activity: ComponentActivity,
    // fungsi lambda (tidak mengembalikan nilai apapun)
    private val onSuccess: (Location) -> Unit,
    private val onFailure: (Exception) -> Unit,
    private val onDenied: () -> Unit
) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    //    membuat launcher yang minta permission itu
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getLastLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getLastLocation()
            }
            else -> {
                onDenied()
            }
        }
    }

    fun requestLocation() {
        if (hasLocationPermission()) {
            getLastLocation()
        } else {
            // Minta izin
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Cek apakah sudah punya izin
    private fun hasLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    // Fungsi internal untuk ambil lokasi (setelah izin didapat)
    @SuppressLint("MissingPermission") // Izin sudah dicek di hasLocationPermission()
    private fun getLastLocation() {
        if (!hasLocationPermission()) {
            onDenied() // Safety check
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Kirim hasil sukses lewat lambda
                    onSuccess(location)
                } else {
                    // Kirim hasil gagal lewat lambda
                    onFailure(Exception("Tidak bisa dapat lokasi. Pastikan GPS menyala."))
                }
            }
            .addOnFailureListener { e ->
                // Kirim error lewat lambda
                onFailure(e)
            }
    }
}