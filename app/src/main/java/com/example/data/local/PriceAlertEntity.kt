package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productTitle: String,
    val store: String,
    val oldPrice: Double,
    val newPrice: Double,
    val dropAmount: Double,
    val dropPercentage: Double,
    val alertType: String = "DROP", // "DROP" or "INCREASE"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    val isIncrease: Boolean
        get() = alertType.equals("INCREASE", ignoreCase = true) || newPrice > oldPrice
}
