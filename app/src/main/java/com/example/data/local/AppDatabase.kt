package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrackedProductEntity::class,
        PriceHistoryEntity::class,
        PriceAlertEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pricePulseDao(): PricePulseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "price_pulse_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.pricePulseDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PricePulseDao) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            // 1. Blinkit: Amul Butter (500g) - Quick Commerce
            val butterId = dao.insertProduct(
                TrackedProductEntity(
                    title = "Amul Butter - Pasteurized (500 g)",
                    store = "Blinkit",
                    productUrl = "https://blinkit.com/prid/amul-butter-pasteurized/281",
                    imageUrl = "",
                    currentPrice = 275.0,
                    initialPrice = 295.0,
                    lowestPrice = 275.0,
                    highestPrice = 295.0,
                    targetPrice = 278.0,
                    alertOnPercentage = 5.0,
                    alertOnAnyDrop = true,
                    alertOnPriceIncrease = true,
                    checkIntervalMinutes = 15,
                    lastCheckedTimestamp = now - 5 * 60 * 1000L,
                    category = "Dairy & Breakfast (Quick Commerce)",
                    inStock = true,
                    deliveryEta = "10 mins",
                    isQuickCommerce = true,
                    locationArea = "Bengaluru (560034)",
                    rating = 4.8f,
                    ratingCount = 38400,
                    isFavorite = true
                )
            )

            dao.insertPriceHistories(
                listOf(
                    PriceHistoryEntity(productId = butterId, price = 295.0, timestamp = now - 10 * dayMs, changeReason = "MRP"),
                    PriceHistoryEntity(productId = butterId, price = 285.0, timestamp = now - 4 * dayMs, changeReason = "Dark store promotion"),
                    PriceHistoryEntity(productId = butterId, price = 275.0, timestamp = now - 5 * 60 * 1000L, changeReason = "Weekend flash savings: Target Met!")
                )
            )

            dao.insertAlert(
                PriceAlertEntity(
                    productId = butterId,
                    productTitle = "Amul Butter - Pasteurized (500 g)",
                    store = "Blinkit",
                    oldPrice = 285.0,
                    newPrice = 275.0,
                    dropAmount = 10.0,
                    dropPercentage = 3.5,
                    alertType = "DROP",
                    timestamp = now - 5 * 60 * 1000L,
                    isRead = false
                )
            )

            // 2. Zepto: Fortune Sunflower Oil (1L) - Quick Commerce
            val oilId = dao.insertProduct(
                TrackedProductEntity(
                    title = "Fortune Sunlite Refined Sunflower Oil (1 L Pouch)",
                    store = "Zepto",
                    productUrl = "https://www.zeptonow.com/pn/fortune-sunlite-sunflower-oil-1l/1829",
                    imageUrl = "",
                    currentPrice = 138.0,
                    initialPrice = 155.0,
                    lowestPrice = 138.0,
                    highestPrice = 155.0,
                    targetPrice = 142.0,
                    alertOnPercentage = 8.0,
                    alertOnAnyDrop = true,
                    alertOnPriceIncrease = true,
                    checkIntervalMinutes = 30,
                    lastCheckedTimestamp = now - 12 * 60 * 1000L,
                    category = "Staples & Groceries (Quick Commerce)",
                    inStock = true,
                    deliveryEta = "9 mins",
                    isQuickCommerce = true,
                    locationArea = "Bengaluru (560034)",
                    rating = 4.7f,
                    ratingCount = 29100,
                    isFavorite = true
                )
            )

            dao.insertPriceHistories(
                listOf(
                    PriceHistoryEntity(productId = oilId, price = 155.0, timestamp = now - 14 * dayMs, changeReason = "Standard retail price"),
                    PriceHistoryEntity(productId = oilId, price = 145.0, timestamp = now - 6 * dayMs, changeReason = "Super Saver discount"),
                    PriceHistoryEntity(productId = oilId, price = 138.0, timestamp = now - 12 * 60 * 1000L, changeReason = "Price drop: Target reached!")
                )
            )

            dao.insertAlert(
                PriceAlertEntity(
                    productId = oilId,
                    productTitle = "Fortune Sunlite Refined Sunflower Oil (1 L)",
                    store = "Zepto",
                    oldPrice = 145.0,
                    newPrice = 138.0,
                    dropAmount = 7.0,
                    dropPercentage = 4.8,
                    alertType = "DROP",
                    timestamp = now - 12 * 60 * 1000L,
                    isRead = false
                )
            )

            // 3. Swiggy Instamart: Maggi 2-Minute Noodles
            val maggiId = dao.insertProduct(
                TrackedProductEntity(
                    title = "Maggi 2-Minute Instant Masala Noodles (Pack of 12)",
                    store = "Swiggy Instamart",
                    productUrl = "https://www.swiggy.com/instamart/item/maggi-2-minute-instant-masala-noodles-pack-of-12",
                    imageUrl = "",
                    currentPrice = 158.0,
                    initialPrice = 180.0,
                    lowestPrice = 158.0,
                    highestPrice = 180.0,
                    targetPrice = 165.0,
                    alertOnPercentage = 5.0,
                    alertOnAnyDrop = true,
                    alertOnPriceIncrease = true,
                    checkIntervalMinutes = 60,
                    lastCheckedTimestamp = now - 25 * 60 * 1000L,
                    category = "Snacks & Drinks (Instant 10m)",
                    inStock = true,
                    deliveryEta = "12 mins",
                    isQuickCommerce = true,
                    locationArea = "Bengaluru (560034)",
                    rating = 4.9f,
                    ratingCount = 54200,
                    isFavorite = false
                )
            )

            dao.insertPriceHistories(
                listOf(
                    PriceHistoryEntity(productId = maggiId, price = 180.0, timestamp = now - 15 * dayMs, changeReason = "MRP"),
                    PriceHistoryEntity(productId = maggiId, price = 168.0, timestamp = now - 7 * dayMs, changeReason = "Instamart combo offer"),
                    PriceHistoryEntity(productId = maggiId, price = 158.0, timestamp = now - 25 * 60 * 1000L, changeReason = "Swiggy One special discount")
                )
            )

            // 4. Apple iPhone 15 on Flipkart
            val iphoneId = dao.insertProduct(
                TrackedProductEntity(
                    title = "Apple iPhone 15 (Black, 128 GB)",
                    store = "Flipkart",
                    productUrl = "https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4",
                    imageUrl = "",
                    currentPrice = 65999.0,
                    initialPrice = 79900.0,
                    lowestPrice = 65999.0,
                    highestPrice = 79900.0,
                    targetPrice = 68000.0,
                    alertOnPercentage = 10.0,
                    alertOnAnyDrop = true,
                    alertOnPriceIncrease = true,
                    checkIntervalMinutes = 60,
                    lastCheckedTimestamp = now - 45 * 60 * 1000L,
                    category = "Smartphones & Mobiles",
                    inStock = true,
                    deliveryEta = "2 Days",
                    isQuickCommerce = false,
                    locationArea = "All India",
                    rating = 4.7f,
                    ratingCount = 42800,
                    isFavorite = true
                )
            )

            dao.insertPriceHistories(
                listOf(
                    PriceHistoryEntity(productId = iphoneId, price = 79900.0, timestamp = now - 28 * dayMs, changeReason = "Official Launch MSRP"),
                    PriceHistoryEntity(productId = iphoneId, price = 71999.0, timestamp = now - 12 * dayMs, changeReason = "Bank offer price reduction"),
                    PriceHistoryEntity(productId = iphoneId, price = 65999.0, timestamp = now - 45 * 60 * 1000L, changeReason = "Big Billion Days Deal: Target Reached!")
                )
            )

            // 5. boAt Airdopes on Blinkit (Instant Delivery)
            val boatId = dao.insertProduct(
                TrackedProductEntity(
                    title = "boAt Airdopes 141 Bluetooth Truly Wireless Earbuds (42H Playtime)",
                    store = "Blinkit",
                    productUrl = "https://blinkit.com/prid/boat-airdopes-141-earbuds/129481",
                    imageUrl = "",
                    currentPrice = 1099.0,
                    initialPrice = 4490.0,
                    lowestPrice = 1099.0,
                    highestPrice = 4490.0,
                    targetPrice = 1299.0,
                    alertOnPercentage = 20.0,
                    alertOnAnyDrop = true,
                    alertOnPriceIncrease = true,
                    checkIntervalMinutes = 30,
                    lastCheckedTimestamp = now - 1 * 60 * 60 * 1000L,
                    category = "Audio & Wearables",
                    inStock = true,
                    deliveryEta = "10 mins",
                    isQuickCommerce = true,
                    locationArea = "Bengaluru (560034)",
                    rating = 4.5f,
                    ratingCount = 142000,
                    isFavorite = false
                )
            )

            dao.insertPriceHistories(
                listOf(
                    PriceHistoryEntity(productId = boatId, price = 4490.0, timestamp = now - 30 * dayMs, changeReason = "MRP"),
                    PriceHistoryEntity(productId = boatId, price = 1299.0, timestamp = now - 10 * dayMs, changeReason = "Dark store discount"),
                    PriceHistoryEntity(productId = boatId, price = 1099.0, timestamp = now - 1 * 60 * 60 * 1000L, changeReason = "Quick delivery mega drop")
                )
            )
        }
    }
}
