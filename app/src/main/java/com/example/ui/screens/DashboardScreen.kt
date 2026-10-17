package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackedProductEntity
import com.example.ui.components.AddProductDialog
import com.example.ui.components.DashboardMetricCard
import com.example.ui.components.LocationSelectorDialog
import com.example.ui.components.PriceDropAlertBanner
import com.example.ui.components.PriceHistoryChart
import com.example.ui.components.StoreBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlinkitColor
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SwiggyColor
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ThemeSetting
import com.example.util.CurrencyUtils
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    val isConnected by viewModel.networkMonitor.isConnected.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()
    val showSavingsCard by viewModel.showSavingsCard.collectAsState()
    val showDropsCard by viewModel.showDropsCard.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    val spotlightProduct = remember(products) {
        products.maxByOrNull { it.priceDropAmount } ?: products.firstOrNull()
    }

    LaunchedEffect(spotlightProduct?.id) {
        spotlightProduct?.id?.let { viewModel.loadProductHistory(it) }
    }

    val spotlightHistory by viewModel.selectedProductHistory.collectAsState()

    val totalSavings = remember(products) {
        products.sumOf { it.priceDropAmount }
    }

    val targetMetCount = remember(products) {
        products.count { it.isTargetMet }
    }

    val dropsCount = remember(products) {
        products.count { it.priceDropAmount > 0.0 }
    }

    val unreadAlert = alerts.firstOrNull { !it.isRead }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Location Bar with 1-tap change
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showLocationDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delivering to: ${userLocation.area}, ${userLocation.city} (${userLocation.pincode})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // App Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PricePulse",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Battery & Network Pill
                            Box(
                                modifier = Modifier
                                    .background(
                                        EmeraldPrimary.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatterySaver,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "0% Battery Drain",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Swiggy • Zepto • Blinkit • Flipkart • Amazon.in",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.checkAllPricesNow(forcePriceChangeForDemo = false) },
                            enabled = !isRefreshing && isConnected,
                            modifier = Modifier.testTag("refresh_prices_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = EmeraldPrimary
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Check Prices Now",
                                    tint = if (isConnected) EmeraldPrimary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.setTheme(
                                    if (themeSetting == ThemeSetting.DARK) ThemeSetting.LIGHT else ThemeSetting.DARK
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (themeSetting == ThemeSetting.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Unread Alert Banner (Supports Drops & Hikes)
            if (unreadAlert != null) {
                item {
                    PriceDropAlertBanner(
                        alert = unreadAlert,
                        onDismiss = { viewModel.markAlertRead(unreadAlert.id) },
                        onViewProduct = { productId ->
                            viewModel.markAlertRead(unreadAlert.id)
                            viewModel.navigateTo(AppScreen.PRODUCT_DETAIL, productId)
                        },
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
            }

            // Search / Add Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Paste Blinkit, Zepto, Swiggy or Flipkart link…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ Track",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Commerce Fast-Access Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(BlinkitColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(AppScreen.COMPARE_STORES) }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = BlinkitColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Instant 10m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlinkitColor)
                                Text("Blinkit • Zepto • Swiggy", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(CyanAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(AppScreen.COMPARE_STORES) }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Compare All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                Text("Find lowest in ${userLocation.city}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // KPI Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        title = "Monitored",
                        value = "${products.size}",
                        subtitle = "Active products",
                        icon = Icons.Default.PriceCheck,
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )

                    if (showSavingsCard) {
                        DashboardMetricCard(
                            title = "Total Saved",
                            value = CurrencyUtils.formatInr(totalSavings),
                            subtitle = "Across all drops",
                            icon = Icons.Default.Savings,
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (showDropsCard) {
                        DashboardMetricCard(
                            title = "Drops & Hits",
                            value = "$dropsCount / $targetMetCount",
                            subtitle = "$targetMetCount targets hit",
                            icon = Icons.Default.TrendingDown,
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Spotlight Product Chart
            if (spotlightProduct != null) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Price History Spotlight",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = spotlightProduct.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StoreBadge(storeName = spotlightProduct.store)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        PriceHistoryChart(
                            priceHistory = spotlightHistory,
                            targetPrice = spotlightProduct.targetPrice,
                            modifier = Modifier.clickable {
                                viewModel.navigateTo(AppScreen.PRODUCT_DETAIL, spotlightProduct.id)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }

            // Monitored Items Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monitored Items (${products.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View All →",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.TRACKED_ITEMS) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Products List
            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No products tracked yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Track milk, butter, phones, or gadgets from Blinkit, Zepto, Swiggy, Flipkart & Amazon.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("+ Track Product", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(products.take(5)) { product ->
                    DashboardProductRow(
                        product = product,
                        onClick = { viewModel.navigateTo(AppScreen.PRODUCT_DETAIL, product.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // FAB to add product
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("fab_add_product")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product to Track")
        }

        if (showAddDialog) {
            AddProductDialog(
                scraperEngine = viewModel.scraperEngine,
                onDismiss = { showAddDialog = false },
                onAddProduct = { newProduct ->
                    viewModel.addProduct(newProduct)
                    showAddDialog = false
                }
            )
        }

        if (showLocationDialog) {
            LocationSelectorDialog(
                currentLocation = userLocation,
                onDismiss = { showLocationDialog = false },
                onLocationSelected = { city, area, pincode ->
                    viewModel.setLocation(city, area, pincode)
                }
            )
        }
    }
}

@Composable
fun DashboardProductRow(
    product: TrackedProductEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StoreBadge(storeName = product.store)
                    if (product.deliveryEta.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ ${product.deliveryEta}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.targetPrice > 0.0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (product.isTargetMet) "🎯 Target ${CurrencyUtils.formatInr(product.targetPrice)} Met!"
                        else "🎯 Target: ${CurrencyUtils.formatInr(product.targetPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.isTargetMet) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatInr(product.currentPrice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (product.priceDropAmount > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CurrencyUtils.formatInr(product.initialPrice),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-${String.format(Locale.US, "%.0f%%", product.priceDropPercentage)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
