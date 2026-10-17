package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_products")
data class TrackedProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val store: String,
    val productUrl: String,
    val imageUrl: String = "",
    val currentPrice: Double,
    val initialPrice: Double,
    val lowestPrice: Double,
    val highestPrice: Double,
    val targetPrice: Double = 0.0,
    val alertOnPercentage: Double = 0.0,
    val alertOnAnyDrop: Boolean = true,
    val alertOnPriceIncrease: Boolean = true,
    val checkIntervalMinutes: Int = 60,
    val lastCheckedTimestamp: Long = System.currentTimeMillis(),
    val category: String = "General",
    val inStock: Boolean = true,
    val deliveryEta: String = "10 mins",
    val isQuickCommerce: Boolean = false,
    val locationArea: String = "Bengaluru",
    val rating: Float = 4.5f,
    val ratingCount: Int = 120,
    val isFavorite: Boolean = false,
    val notificationSentForLowest: Boolean = false
) {
    val priceDropAmount: Double
        get() = (initialPrice - currentPrice).coerceAtLeast(0.0)

    val priceDropPercentage: Double
        get() = if (initialPrice > 0.0 && currentPrice < initialPrice) {
            ((initialPrice - currentPrice) / initialPrice) * 100.0
        } else 0.0

    val priceIncreaseAmount: Double
        get() = (currentPrice - initialPrice).coerceAtLeast(0.0)

    val priceIncreasePercentage: Double
        get() = if (initialPrice > 0.0 && currentPrice > initialPrice) {
            ((currentPrice - initialPrice) / initialPrice) * 100.0
        } else 0.0

    val isPriceIncreased: Boolean
        get() = currentPrice > initialPrice

    val isAtLowestPrice: Boolean
        get() = currentPrice <= lowestPrice && lowestPrice > 0.0

    val isTargetMet: Boolean
        get() = targetPrice > 0.0 && currentPrice <= targetPrice
}
