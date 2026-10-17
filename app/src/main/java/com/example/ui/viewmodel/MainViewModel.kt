package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PriceAlertEntity
import com.example.data.local.PriceHistoryEntity
import com.example.data.local.TrackedProductEntity
import com.example.data.network.NetworkMonitor
import com.example.data.network.PriceScraperEngine
import com.example.data.network.StoreOffer
import com.example.service.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppScreen {
    DASHBOARD,
    TRACKED_ITEMS,
    COMPARE_STORES,
    PRODUCT_DETAIL,
    SETTINGS
}

enum class ThemeSetting {
    SYSTEM,
    DARK,
    LIGHT
}

data class UserLocation(
    val city: String,
    val area: String,
    val pincode: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.pricePulseDao()
    val scraperEngine = PriceScraperEngine()
    val networkMonitor = NetworkMonitor(application)
    val notificationHelper = NotificationHelper(application)

    // Location State for quick commerce & local availability
    private val _userLocation = MutableStateFlow(
        UserLocation(city = "Bengaluru", area = "Koramangala", pincode = "560034")
    )
    val userLocation: StateFlow<UserLocation> = _userLocation.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedProductId = MutableStateFlow<Long?>(null)
    val selectedProductId: StateFlow<Long?> = _selectedProductId.asStateFlow()

    // Search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStoreFilter = MutableStateFlow("All")
    val selectedStoreFilter: StateFlow<String> = _selectedStoreFilter.asStateFlow()

    private val _onlyDropsFilter = MutableStateFlow(false)
    val onlyDropsFilter: StateFlow<Boolean> = _onlyDropsFilter.asStateFlow()

    private val _onlyQuickCommerceFilter = MutableStateFlow(false)
    val onlyQuickCommerceFilter: StateFlow<Boolean> = _onlyQuickCommerceFilter.asStateFlow()

    // Settings
    private val _themeSetting = MutableStateFlow(ThemeSetting.DARK)
    val themeSetting: StateFlow<ThemeSetting> = _themeSetting.asStateFlow()

    private val _checkOnlyOnWifi = MutableStateFlow(false)
    val checkOnlyOnWifi: StateFlow<Boolean> = _checkOnlyOnWifi.asStateFlow()

    private val _globalCheckInterval = MutableStateFlow(60) // minutes
    val globalCheckInterval: StateFlow<Int> = _globalCheckInterval.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notifyOnPriceIncreases = MutableStateFlow(true)
    val notifyOnPriceIncreases: StateFlow<Boolean> = _notifyOnPriceIncreases.asStateFlow()

    // Customizable Dashboard toggles
    private val _showSavingsCard = MutableStateFlow(true)
    val showSavingsCard: StateFlow<Boolean> = _showSavingsCard.asStateFlow()

    private val _showDropsCard = MutableStateFlow(true)
    val showDropsCard: StateFlow<Boolean> = _showDropsCard.asStateFlow()

    // Refreshing status
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Compare Screen State
    private val _compareQuery = MutableStateFlow("Amul Butter")
    val compareQuery: StateFlow<String> = _compareQuery.asStateFlow()

    private val _compareResults = MutableStateFlow<List<StoreOffer>>(emptyList())
    val compareResults: StateFlow<List<StoreOffer>> = _compareResults.asStateFlow()

    private val _isComparing = MutableStateFlow(false)
    val isComparing: StateFlow<Boolean> = _isComparing.asStateFlow()

    // Local DB Flows
    val allProducts: StateFlow<List<TrackedProductEntity>> = dao.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<PriceAlertEntity>> = dao.getAllAlertsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts = combine(
        allProducts,
        _searchQuery,
        _selectedStoreFilter,
        _onlyDropsFilter,
        _onlyQuickCommerceFilter
    ) { products, query, store, onlyDrops, onlyQCommerce ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.store.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)

            val matchesStore = store == "All" || product.store.equals(store, ignoreCase = true)
            val matchesDrops = !onlyDrops || product.priceDropAmount > 0.0
            val matchesQCommerce = !onlyQCommerce || product.isQuickCommerce

            matchesQuery && matchesStore && matchesDrops && matchesQCommerce
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected product history
    private val _selectedProductHistory = MutableStateFlow<List<PriceHistoryEntity>>(emptyList())
    val selectedProductHistory: StateFlow<List<PriceHistoryEntity>> = _selectedProductHistory.asStateFlow()

    // Ultra-battery-efficient background job: wakes up, checks once, and goes back to sleep
    private var periodicCheckJob: Job? = null

    init {
        performStoreComparison("Amul Butter")
        startPeriodicPriceCheckJob()
    }

    fun setLocation(city: String, area: String, pincode: String) {
        _userLocation.value = UserLocation(city = city, area = area, pincode = pincode)
        // Refresh comparison with new location dark stores
        performStoreComparison(_compareQuery.value)
    }

    fun navigateTo(screen: AppScreen, productId: Long? = null) {
        if (productId != null) {
            _selectedProductId.value = productId
            loadProductHistory(productId)
        }
        _currentScreen.value = screen
    }

    fun loadProductHistory(productId: Long) {
        viewModelScope.launch {
            dao.getPriceHistoryFlow(productId).collect { history ->
                _selectedProductHistory.value = history
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStoreFilter(store: String) {
        _selectedStoreFilter.value = store
    }

    fun toggleOnlyDrops() {
        _onlyDropsFilter.value = !_onlyDropsFilter.value
    }

    fun toggleOnlyQuickCommerce() {
        _onlyQuickCommerceFilter.value = !_onlyQuickCommerceFilter.value
    }

    fun setTheme(theme: ThemeSetting) {
        _themeSetting.value = theme
    }

    fun setCheckOnlyOnWifi(wifiOnly: Boolean) {
        _checkOnlyOnWifi.value = wifiOnly
    }

    fun setGlobalCheckInterval(minutes: Int) {
        _globalCheckInterval.value = minutes
        startPeriodicPriceCheckJob()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setNotifyOnPriceIncreases(notify: Boolean) {
        _notifyOnPriceIncreases.value = notify
    }

    fun toggleSavingsCard() {
        _showSavingsCard.value = !_showSavingsCard.value
    }

    fun toggleDropsCard() {
        _showDropsCard.value = !_showDropsCard.value
    }

    fun performStoreComparison(query: String) {
        if (query.isBlank()) return
        _compareQuery.value = query
        viewModelScope.launch {
            _isComparing.value = true
            delay(250)
            val loc = _userLocation.value
            _compareResults.value = scraperEngine.searchAcrossStores(
                query = query,
                locationCity = loc.city,
                locationPincode = loc.pincode
            )
            _isComparing.value = false
        }
    }

    fun addProduct(product: TrackedProductEntity) {
        viewModelScope.launch {
            val id = dao.insertProduct(product)
            dao.insertPriceHistory(
                PriceHistoryEntity(
                    productId = id,
                    price = product.currentPrice,
                    timestamp = System.currentTimeMillis(),
                    changeReason = "Initial price tracked"
                )
            )
            navigateTo(AppScreen.TRACKED_ITEMS)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            dao.deleteProductById(productId)
            if (_selectedProductId.value == productId) {
                navigateTo(AppScreen.TRACKED_ITEMS)
            }
        }
    }

    fun updateTargetPrice(productId: Long, newTarget: Double) {
        viewModelScope.launch {
            val product = dao.getProductById(productId) ?: return@launch
            dao.updateProduct(product.copy(targetPrice = newTarget))
        }
    }

    fun markAlertRead(alertId: Long) {
        viewModelScope.launch {
            dao.markAlertAsRead(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            dao.clearAllAlerts()
        }
    }

    // Manual check or scheduled wake-and-stop runner
    fun checkAllPricesNow(forcePriceChangeForDemo: Boolean = false, simulateIncrease: Boolean = false) {
        val isConnected = networkMonitor.isConnected.value
        val isWifi = networkMonitor.isWifi.value

        if (!isConnected || (_checkOnlyOnWifi.value && !isWifi)) {
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            val list = allProducts.value

            for (product in list) {
                checkSingleProductPrice(product, forcePriceChangeForDemo, simulateIncrease)
            }

            delay(500)
            _isRefreshing.value = false
        }
    }

    private suspend fun checkSingleProductPrice(
        product: TrackedProductEntity,
        forcePriceChange: Boolean = false,
        simulateIncrease: Boolean = false
    ) {
        val (newPrice, reason) = if (forcePriceChange) {
            if (simulateIncrease) {
                val hike = (product.currentPrice * 1.08).roundToInt().toDouble()
                Pair(hike, "Price increased (+8%)")
            } else {
                val drop = (product.currentPrice * 0.92).roundToInt().toDouble()
                Pair(drop, "Simulated instant price drop (-8%)")
            }
        } else {
            scraperEngine.simulatePriceCheck(product.currentPrice, product.lowestPrice)
        }

        if (newPrice != product.currentPrice) {
            val isDrop = newPrice < product.currentPrice
            val isIncrease = newPrice > product.currentPrice
            val priceDiff = kotlin.math.abs(newPrice - product.currentPrice)
            val percent = if (product.currentPrice > 0.0) (priceDiff / product.currentPrice) * 100.0 else 0.0

            val newLowest = minOf(product.lowestPrice, newPrice)
            val newHighest = maxOf(product.highestPrice, newPrice)

            val updated = product.copy(
                currentPrice = newPrice,
                lowestPrice = newLowest,
                highestPrice = newHighest,
                lastCheckedTimestamp = System.currentTimeMillis()
            )
            dao.updateProduct(updated)

            dao.insertPriceHistory(
                PriceHistoryEntity(
                    productId = product.id,
                    price = newPrice,
                    timestamp = System.currentTimeMillis(),
                    changeReason = reason
                )
            )

            // Alert condition for Drop OR Increase
            val shouldAlertDrop = isDrop && (
                    product.alertOnAnyDrop ||
                            (product.targetPrice > 0.0 && newPrice <= product.targetPrice) ||
                            (product.alertOnPercentage > 0.0 && percent >= product.alertOnPercentage)
                    )

            val shouldAlertIncrease = isIncrease && product.alertOnPriceIncrease && _notifyOnPriceIncreases.value

            if (shouldAlertDrop || shouldAlertIncrease) {
                val alert = PriceAlertEntity(
                    productId = product.id,
                    productTitle = product.title,
                    store = product.store,
                    oldPrice = product.currentPrice,
                    newPrice = newPrice,
                    dropAmount = priceDiff,
                    dropPercentage = percent,
                    alertType = if (isIncrease) "INCREASE" else "DROP",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                dao.insertAlert(alert)

                if (_notificationsEnabled.value) {
                    notificationHelper.sendPriceChangeNotification(
                        productId = product.id,
                        productTitle = product.title,
                        store = product.store,
                        oldPrice = product.currentPrice,
                        newPrice = newPrice,
                        isIncrease = isIncrease,
                        changeAmount = priceDiff,
                        changePercentage = percent
                    )
                }
            }
        } else {
            dao.updateProduct(product.copy(lastCheckedTimestamp = System.currentTimeMillis()))
        }
    }

    // Battery-efficient wake-and-stop background loop
    private fun startPeriodicPriceCheckJob() {
        periodicCheckJob?.cancel()
        periodicCheckJob = viewModelScope.launch {
            while (isActive) {
                val intervalMs = _globalCheckInterval.value * 60 * 1000L
                delay(intervalMs)

                // 1. Check network condition before doing ANY wake action
                val isConnected = networkMonitor.isConnected.value
                val isWifi = networkMonitor.isWifi.value
                val allowed = isConnected && (!_checkOnlyOnWifi.value || isWifi)

                // 2. Wake up, check all prices, notify if changes occur, and immediately stop run
                if (allowed) {
                    allProducts.value.forEach { product ->
                        checkSingleProductPrice(product, forcePriceChange = false)
                    }
                }
                // Once loop finishes, it goes back to sleep until next scheduled interval
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        periodicCheckJob?.cancel()
    }
}
