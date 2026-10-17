package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.TrackedProductEntity
import com.example.data.network.PriceScraperEngine
import com.example.ui.theme.EmeraldPrimary
import java.util.Locale

@Composable
fun AddProductDialog(
    scraperEngine: PriceScraperEngine,
    onDismiss: () -> Unit,
    onAddProduct: (TrackedProductEntity) -> Unit
) {
    var urlOrNameInput by remember { mutableStateOf("") }
    var detectedStore by remember { mutableStateOf("Flipkart") }
    var titleInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var targetPriceInput by remember { mutableStateOf("") }
    var selectedIntervalMinutes by remember { mutableIntStateOf(60) }
    var alertOnAnyDrop by remember { mutableStateOf(true) }
    var alertPercentage by remember { mutableDoubleStateOf(10.0) }

    val intervalOptions = listOf(
        Pair("1 min (Live)", 1),
        Pair("30 min", 30),
        Pair("1 hr", 60),
        Pair("12 hr", 720),
        Pair("1 day", 1440)
    )

    val quickPresets = listOf(
        Pair("Amul Butter", "https://blinkit.com/prid/amul-butter-500g"),
        Pair("Zepto Milk", "https://zeptonow.com/p/country-delight-cow-milk"),
        Pair("Swiggy Tea", "https://swiggy.com/instamart/item/tata-tea-gold-500g"),
        Pair("iPhone 15", "https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4"),
        Pair("OnePlus 12R", "https://www.amazon.in/dp/B0CQPNR347"),
        Pair("boAt Airdopes", "https://www.flipkart.com/boat-airdopes-141-bluetooth-headset/p/itmd5ff3e862ff61")
    )

    fun handleInputChange(input: String) {
        urlOrNameInput = input
        if (input.isNotBlank()) {
            val parsed = scraperEngine.parseProductFromInput(input)
            detectedStore = parsed.store
            titleInput = parsed.title
            priceInput = String.format(Locale.US, "%.0f", parsed.detectedPrice)
            val suggestedTarget = (parsed.detectedPrice * 0.90)
            targetPriceInput = String.format(Locale.US, "%.0f", suggestedTarget)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("add_product_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Track Indian Product",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Paste Flipkart/Amazon.in link or product name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick presets
                Text(
                    text = "Popular in India:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPresets.forEach { (presetName, presetUrl) ->
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { handleInputChange(presetUrl) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = presetName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // URL or Name Input
                OutlinedTextField(
                    value = urlOrNameInput,
                    onValueChange = { handleInputChange(it) },
                    label = { Text("Product URL or Name") },
                    placeholder = { Text("https://flipkart.com/... or iPhone 15") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = EmeraldPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_url_input"),
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product Title
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Product Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Detected Store & Current Price in INR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = detectedStore,
                        onValueChange = { detectedStore = it },
                        label = { Text("Store") },
                        leadingIcon = {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Price Alert in INR
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it },
                    label = { Text("Target Price Alert (₹)") },
                    placeholder = { Text("Alert when below this price in ₹") },
                    leadingIcon = {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Check Interval Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Check Frequency:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    intervalOptions.forEach { (label, minutes) ->
                        val isSelected = selectedIntervalMinutes == minutes
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIntervalMinutes = minutes },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color(0xFF022C22),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                selectedBorderColor = EmeraldPrimary
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alert Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alert on Any Price Drop",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant push notification when price decreases",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = alertOnAnyDrop,
                        onCheckedChange = { alertOnAnyDrop = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary,
                            checkedBorderColor = EmeraldPrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val current = priceInput.toDoubleOrNull() ?: 4999.0
                        val target = targetPriceInput.toDoubleOrNull() ?: (current * 0.90)
                        val title = if (titleInput.isNotBlank()) titleInput else "Monitored Item"
                        val url = if (urlOrNameInput.isNotBlank()) urlOrNameInput else "https://flipkart.com"

                        val product = TrackedProductEntity(
                            title = title,
                            store = detectedStore,
                            productUrl = url,
                            currentPrice = current,
                            initialPrice = current,
                            lowestPrice = current,
                            highestPrice = current,
                            targetPrice = target,
                            alertOnPercentage = alertPercentage,
                            alertOnAnyDrop = alertOnAnyDrop,
                            checkIntervalMinutes = selectedIntervalMinutes,
                            lastCheckedTimestamp = System.currentTimeMillis()
                        )
                        onAddProduct(product)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_add_product"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary
                    ),
                    enabled = urlOrNameInput.isNotBlank() || titleInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Tracking Price",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
