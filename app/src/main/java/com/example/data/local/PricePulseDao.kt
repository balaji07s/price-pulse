package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PricePulseDao {

    @Query("SELECT * FROM tracked_products ORDER BY lastCheckedTimestamp DESC")
    fun getAllProductsFlow(): Flow<List<TrackedProductEntity>>

    @Query("SELECT * FROM tracked_products WHERE id = :productId")
    suspend fun getProductById(productId: Long): TrackedProductEntity?

    @Query("SELECT * FROM tracked_products WHERE id = :productId")
    fun getProductFlowById(productId: Long): Flow<TrackedProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: TrackedProductEntity): Long

    @Update
    suspend fun updateProduct(product: TrackedProductEntity)

    @Delete
    suspend fun deleteProduct(product: TrackedProductEntity)

    @Query("DELETE FROM tracked_products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("SELECT COUNT(*) FROM tracked_products")
    suspend fun getProductCount(): Int

    // Price History
    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    fun getPriceHistoryFlow(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp ASC")
    suspend fun getPriceHistorySync(productId: Long): List<PriceHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistories(histories: List<PriceHistoryEntity>)

    // Price Alerts
    @Query("SELECT * FROM price_alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<PriceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Query("UPDATE price_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Long)

    @Query("DELETE FROM price_alerts")
    suspend fun clearAllAlerts()
}
