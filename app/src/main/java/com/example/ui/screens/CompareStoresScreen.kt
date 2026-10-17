package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackedProductEntity
import com.example.data.network.StoreOffer
import com.example.ui.components.LocationSelectorDialog
import com.example.ui.components.StoreBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CurrencyUtils

@Composable
fun CompareStoresScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compareQuery by viewModel.compareQuery.collectAsState()
    val compareResults by viewModel.compareResults.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    var textInput by remember(compareQuery) { mutableStateOf(compareQuery) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "QCOMMERCE", "ECOMMERCE"

    val quickSearches = listOf(
        "Amul Butter",
        "Fortune Sunflower Oil",
        "Maggi 12-Pack",
        "iPhone 15",
        "boAt Airdopes",
        "Coca-Cola 750ml",
        "OnePlus 12R"
    )

    val displayedOffers = remember(compareResults, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "QCOMMERCE" -> compareResults.filter { it.isQuickCommerce }
            "ECOMMERCE" -> compareResults.filter { !it.isQuickCommerce }
            else -> compareResults
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Location Selector Bar with high-contrast border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { showLocationDialog = true }
                .padding(horizontal = 12.dp, vertical = 9.dp),
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${userLocation.area}, ${userLocation.city} (${userLocation.pincode})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Text(
                text = "Change Location",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = EmeraldPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = "Cross-Platform Price Comparison",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Flipkart Minutes • Blinkit • Zepto • Swiggy • Amazon Now • BB Now",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text("Search butter, milk, oil, iPhone, earbuds…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
            },
            trailingIcon = {
                if (isComparing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = EmeraldPrimary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.performStoreComparison(textInput) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("compare_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Search Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSearches) { searchItem ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            textInput = searchItem
                            viewModel.performStoreComparison(searchItem)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = searchItem,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Tabs: All vs 10m Quick Commerce vs E-Commerce
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == "ALL",
                onClick = { selectedCategoryFilter = "ALL" },
                label = { Text("All (${compareResults.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color(0xFF022C22),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategoryFilter == "ALL",
                    borderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedCategoryFilter == "QCOMMERCE",
                onClick = { selectedCategoryFilter = "QCOMMERCE" },
                label = { Text("⚡ 10-15m Instant", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color(0xFF022C22),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategoryFilter == "QCOMMERCE",
                    borderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1.3f)
            )

            FilterChip(
                selected = selectedCategoryFilter == "ECOMMERCE",
                onClick = { selectedCategoryFilter = "ECOMMERCE" },
                label = { Text("🛍️ E-Commerce", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color(0xFF022C22),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategoryFilter == "ECOMMERCE",
                    borderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Best Deal Header Card
        if (displayedOffers.isNotEmpty()) {
            val bestOffer = displayedOffers.firstOrNull { it.isBestPrice } ?: displayedOffers.minByOrNull { it.price }
            if (bestOffer != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.15f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏆 Lowest Price on ${bestOffer.storeName}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                if (bestOffer.isQuickCommerce) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "⚡ ${bestOffer.deliveryEta}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                            Text(
                                text = "Available now in ${userLocation.city}: ${CurrencyUtils.formatInr(bestOffer.price)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        StoreBadge(storeName = bestOffer.storeName)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Stores Offers List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayedOffers) { offer ->
                val isAlreadyTracked = allProducts.any {
                    it.title.contains(textInput, ignoreCase = true) && it.store.equals(offer.storeName, ignoreCase = true)
                }

                StoreOfferCard(
                    offer = offer,
                    productTitle = textInput,
                    isAlreadyTracked = isAlreadyTracked,
                    onTrack = {
                        val product = TrackedProductEntity(
                            title = textInput,
                            store = offer.storeName,
                            productUrl = offer.url,
                            currentPrice = offer.price,
                            initialPrice = offer.listPrice,
                            lowestPrice = offer.price,
                            highestPrice = offer.listPrice,
                            targetPrice = (offer.price * 0.90).let { (it * 100).toInt() / 100.0 },
                            alertOnPercentage = 5.0,
                            alertOnAnyDrop = true,
                            alertOnPriceIncrease = true,
                            checkIntervalMinutes = if (offer.isQuickCommerce) 15 else 60,
                            deliveryEta = offer.deliveryEta,
                            isQuickCommerce = offer.isQuickCommerce,
                            locationArea = "${userLocation.area}, ${userLocation.city}",
                            lastCheckedTimestamp = System.currentTimeMillis()
                        )
                        viewModel.addProduct(product)
                    },
                    onOpenUrl = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.url))
                        context.startActivity(intent)
                    }
                )
            }
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
fun StoreOfferCard(
    offer: StoreOffer,
    productTitle: String,
    isAlreadyTracked: Boolean,
    onTrack: () -> Unit,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (offer.isBestPrice) EmeraldPrimary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (offer.isBestPrice) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StoreBadge(storeName = offer.storeName)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (offer.isQuickCommerce) {
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = offer.deliveryEta,
                                    color = EmeraldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (offer.isBestPrice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOWEST PRICE",
                                color = Color(0xFF022C22),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Rating & Reviews
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${offer.rating} (${offer.reviewsCount})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing and Delivery in INR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = CurrencyUtils.formatInr(offer.price),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (offer.isBestPrice) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = CurrencyUtils.formatInr(offer.listPrice),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${offer.shippingInfo} • ${offer.darkStoreLocation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons with high contrast
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onTrack,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAlreadyTracked) MaterialTheme.colorScheme.surfaceVariant else EmeraldPrimary,
                            contentColor = if (isAlreadyTracked) MaterialTheme.colorScheme.onSurface else Color(0xFF022C22)
                        ),
                        border = if (isAlreadyTracked) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                        enabled = !isAlreadyTracked
                    ) {
                        if (isAlreadyTracked) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tracked", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF022C22), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Track", color = Color(0xFF022C22), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    IconButton(
                        onClick = onOpenUrl,
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open Store Link",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
