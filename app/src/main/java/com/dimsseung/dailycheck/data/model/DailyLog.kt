package com.dimsseung.dailycheck.data.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyLog(
    // Firestore properties
    var id: String? = null,
    val userId : String? = null,

    // User properties
    val title: String? = null,
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val mood: String? = null,
    val tags: List<String>? = null,
    val isFavorite: Boolean = false,
    val location: GeoPoint? = null // punya firestore
)
