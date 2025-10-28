package com.dimsseung.dailycheck.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyLog(
    // Firestore properties
    @DocumentId
    var id: String? = null,
    val userId : String? = null,

    // User properties
    val title: String? = null,
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val mood: String? = "😐",
    val tags: List<String>? = null,
    val isFavorite: Boolean = false,
    val location: GeoPoint? = null,
    val address: String? = null // punya firestore
)
