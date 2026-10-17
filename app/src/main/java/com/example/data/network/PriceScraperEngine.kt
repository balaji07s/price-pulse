package com.example.data.network

import java.net.URI
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

data class StoreOffer(
    val storeName: String,
    val price: Double,
    val listPrice: Double,
    val url: String,
    val inStock: Boolean,
    val rating: Float,
    val reviewsCount: Int,
    val shippingInfo: String,
    val deliveryEta: String = "10 mins",
    val isQuickCommerce: Boolean = false,
    val darkStoreLocation: String = "",
    val condition: String = "Brand New (Official Indian Warranty)",
    val isBestPrice: Boolean = false
)

data class ParsedProductInfo(
    val title: String,
    val store: String,
    val originalUrl: String,
    val detectedPrice: Double,
    val category: String,
    val inStock: Boolean,
    val deliveryEta: String,
    val isQuickCommerce: Boolean,
    val rating: Float
)

class PriceScraperEngine {

    fun identifyStore(url: String): String {
        val lower = url.lowercase(Locale.ROOT)
        return when {
            lower.contains("flipkart.com/minutes") || lower.contains("flipkartminutes") -> "Flipkart Minutes"
            lower.contains("amazon.in/now") || lower.contains("amazon.in/fresh") || lower.contains("amazonnow") -> "Amazon Now"
            lower.contains("bigbasket.com/bbnow") || lower.contains("bbnow") -> "BB Now"
            lower.contains("blinkit.") -> "Blinkit"
            lower.contains("zepto") || lower.contains("zeptonow.") -> "Zepto"
            lower.contains("swiggy.com/instamart") || (lower.contains("swiggy.") && lower.contains("instamart")) -> "Swiggy Instamart"
            lower.contains("swiggy.") -> "Swiggy"
            lower.contains("flipkart.") -> "Flipkart"
            lower.contains("amazon.in") -> "Amazon.in"
            lower.contains("amazon.") -> "Amazon.in"
            lower.contains("croma.") -> "Croma"
            lower.contains("reliancedigital.") || lower.contains("reliance.") -> "Reliance Digital"
            lower.contains("tatacliq.") -> "Tata CLiQ"
            lower.contains("myntra.") -> "Myntra"
            lower.contains("ajio.") -> "Ajio"
            lower.contains("jiomart.") -> "JioMart"
            else -> "Indian Store"
        }
    }

    fun parseProductFromInput(input: String, locationCity: String = "Bengaluru"): ParsedProductInfo {
        val trimmed = input.trim()
        val isUrl = trimmed.startsWith("http://") || trimmed.startsWith("https://") ||
                trimmed.contains(".com") || trimmed.contains(".in")

        if (isUrl) {
            val store = identifyStore(trimmed)
            val extractedName = extractTitleFromUrl(trimmed, store)
            val category = guessCategory(extractedName)
            val basePrice = estimateBasePriceInr(extractedName)
            val isQCommerce = store.contains("Blinkit", true) || store.contains("Zepto", true) ||
                    store.contains("Swiggy", true) || store.contains("Minutes", true) ||
                    store.contains("Now", true) || store.contains("BB", true)
            val eta = if (isQCommerce) "${Random.nextInt(8, 14)} mins" else "1-2 Days"

            return ParsedProductInfo(
                title = extractedName,
                store = store,
                originalUrl = trimmed,
                detectedPrice = basePrice,
                category = category,
                inStock = true,
                deliveryEta = eta,
                isQuickCommerce = isQCommerce,
                rating = (4.2f + Random.nextFloat() * 0.7f).let { (it * 10).roundToInt() / 10f }
            )
        } else {
            val category = guessCategory(trimmed)
            val basePrice = estimateBasePriceInr(trimmed)
            val searchUrl = "https://www.google.com/search?q=${trimmed.replace(" ", "+")}+price+in+$locationCity"
            val isGrocery = category.contains("Dairy") || category.contains("Staples") || category.contains("Snacks")
            val defaultStore = if (isGrocery) "Blinkit" else "Flipkart"
            val eta = if (isGrocery) "10 mins" else "2 Days"

            return ParsedProductInfo(
                title = trimmed,
                store = defaultStore,
                originalUrl = searchUrl,
                detectedPrice = basePrice,
                category = category,
                inStock = true,
                deliveryEta = eta,
                isQuickCommerce = isGrocery,
                rating = 4.6f
            )
        }
    }

    private fun extractTitleFromUrl(url: String, store: String): String {
        try {
            val uri = URI(if (!url.startsWith("http")) "https://$url" else url)
            val path = uri.path ?: ""
            val segments = path.split("/").filter { it.isNotBlank() }

            val candidate = segments.firstOrNull { seg ->
                seg.length > 4 && !seg.equals("dp", ignoreCase = true) &&
                        !seg.equals("gp", ignoreCase = true) &&
                        !seg.equals("p", ignoreCase = true) &&
                        !seg.equals("prid", ignoreCase = true) &&
                        !seg.equals("item", ignoreCase = true) &&
                        !seg.startsWith("B0") &&
                        !seg.startsWith("itm")
            }

            if (candidate != null) {
                val cleaned = candidate
                    .replace(Regex("[-_+]"), " ")
                    .replace(Regex("\\.(html|htm|php).*"), "")
                    .trim()
                if (cleaned.length > 3) {
                    return cleaned.split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { word ->
                            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                        }
                }
            }
        } catch (_: Exception) {
            // Fallback
        }

        return "Product on $store"
    }

    private fun guessCategory(title: String): String {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("milk") || lower.contains("butter") || lower.contains("paneer") ||
                    lower.contains("curd") || lower.contains("amul") || lower.contains("nandini") -> "Dairy & Breakfast (10m Quick Commerce)"
            lower.contains("oil") || lower.contains("atta") || lower.contains("rice") ||
                    lower.contains("dal") || lower.contains("salt") || lower.contains("sugar") -> "Staples & Groceries (10m Quick Commerce)"
            lower.contains("maggi") || lower.contains("chips") || lower.contains("coke") ||
                    lower.contains("lays") || lower.contains("biscuit") || lower.contains("snack") -> "Snacks & Drinks (10m Quick Commerce)"
            lower.contains("iphone") || lower.contains("oneplus") || lower.contains("samsung") ||
                    lower.contains("phone") -> "Smartphones & Mobiles"
            lower.contains("airdopes") || lower.contains("earbud") || lower.contains("boat") ||
                    lower.contains("headphone") || lower.contains("sony") -> "Audio & Wearables"
            lower.contains("charger") || lower.contains("cable") || lower.contains("power bank") -> "Mobile Accessories (10m Delivery)"
            lower.contains("laptop") || lower.contains("macbook") -> "Computers & Tablets"
            lower.contains("tv") -> "Smart TVs & Home Entertainment"
            else -> "Consumer Tech & Essentials"
        }
    }

    private fun estimateBasePriceInr(title: String): Double {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("amul butter") -> 275.0
            lower.contains("nandini milk") || lower.contains("amul milk") -> 30.0
            lower.contains("fortune sunflower oil") || lower.contains("fortune oil") -> 145.0
            lower.contains("maggi") -> 168.0
            lower.contains("coca-cola") || lower.contains("coke") -> 40.0
            lower.contains("lays") || lower.contains("chips") -> 20.0
            lower.contains("aashirvaad atta") -> 420.0
            lower.contains("iphone 15 pro") -> 124900.0
            lower.contains("iphone 15") -> 65999.0
            lower.contains("s24 ultra") -> 119999.0
            lower.contains("oneplus 12r") -> 37999.0
            lower.contains("macbook air m2") -> 84990.0
            lower.contains("sony wh-1000") -> 26990.0
            lower.contains("airdopes") || lower.contains("boat") -> 1099.0
            lower.contains("charger") || lower.contains("cable") -> 399.0
            else -> 499.0
        }
    }

    // Comprehensive search across Flipkart Minutes, Amazon Now, BB Now, Blinkit, Zepto, Swiggy Instamart, Flipkart, Amazon.in
    fun searchAcrossStores(
        query: String,
        locationCity: String = "Bengaluru",
        locationPincode: String = "560034"
    ): List<StoreOffer> {
        val basePrice = estimateBasePriceInr(query)
        val cleanQuery = query.replace(" ", "+")
        val category = guessCategory(query)
        val isGroceryOrSnack = category.contains("Dairy") || category.contains("Staples") ||
                category.contains("Snacks") || category.contains("10m") ||
                query.contains("butter", true) || query.contains("milk", true) ||
                query.contains("oil", true) || query.contains("maggi", true)

        val offers = mutableListOf<StoreOffer>()

        // 1. Flipkart Minutes (Flipkart 10-15m service)
        offers.add(
            StoreOffer(
                storeName = "Flipkart Minutes",
                price = (basePrice * (if (isGroceryOrSnack) 0.94 else 0.91) + Random.nextDouble() * 0.06).roundInr(),
                listPrice = (basePrice * 1.15).roundInr(),
                url = "https://www.flipkart.com/search?q=$cleanQuery&marketplace=MINUTES",
                inStock = true,
                rating = 4.6f,
                reviewsCount = 18400,
                deliveryEta = "11 mins",
                isQuickCommerce = true,
                darkStoreLocation = "$locationCity ($locationPincode Dark Pod)",
                shippingInfo = "⚡ 11 mins • Flipkart Minutes Dark Store"
            )
        )

        // 2. Blinkit (10 mins)
        offers.add(
            StoreOffer(
                storeName = "Blinkit",
                price = (basePrice * (if (isGroceryOrSnack) 0.95 else 0.92) + Random.nextDouble() * 0.05).roundInr(),
                listPrice = (basePrice * 1.12).roundInr(),
                url = "https://blinkit.com/s/?q=$cleanQuery",
                inStock = true,
                rating = 4.7f,
                reviewsCount = 52100,
                deliveryEta = "10 mins",
                isQuickCommerce = true,
                darkStoreLocation = "$locationCity ($locationPincode Hub)",
                shippingInfo = "⚡ 10 mins • Free delivery on ₹199+"
            )
        )

        // 3. Zepto (9 mins)
        offers.add(
            StoreOffer(
                storeName = "Zepto",
                price = (basePrice * (if (isGroceryOrSnack) 0.93 else 0.90) + Random.nextDouble() * 0.06).roundInr(),
                listPrice = (basePrice * 1.14).roundInr(),
                url = "https://www.zeptonow.com/search?q=$cleanQuery",
                inStock = true,
                rating = 4.8f,
                reviewsCount = 68400,
                deliveryEta = "9 mins",
                isQuickCommerce = true,
                darkStoreLocation = "$locationCity ($locationPincode Hub)",
                shippingInfo = "⚡ 9 mins • Zepto Pass Free Delivery"
            )
        )

        // 4. Swiggy Instamart (12 mins)
        offers.add(
            StoreOffer(
                storeName = "Swiggy Instamart",
                price = (basePrice * (if (isGroceryOrSnack) 0.94 else 0.92) + Random.nextDouble() * 0.05).roundInr(),
                listPrice = (basePrice * 1.13).roundInr(),
                url = "https://www.swiggy.com/instamart/search?query=$cleanQuery",
                inStock = true,
                rating = 4.6f,
                reviewsCount = 44200,
                deliveryEta = "12 mins",
                isQuickCommerce = true,
                darkStoreLocation = "$locationCity ($locationPincode Hub)",
                shippingInfo = "⚡ 12 mins • Swiggy One Extra Discount"
            )
        )

        // 5. Amazon Now / Fresh (15 mins)
        offers.add(
            StoreOffer(
                storeName = "Amazon Now",
                price = (basePrice * (if (isGroceryOrSnack) 0.96 else 0.91) + Random.nextDouble() * 0.05).roundInr(),
                listPrice = (basePrice * 1.16).roundInr(),
                url = "https://www.amazon.in/s?k=$cleanQuery&i=nowstore",
                inStock = true,
                rating = 4.7f,
                reviewsCount = 31900,
                deliveryEta = "15 mins",
                isQuickCommerce = true,
                darkStoreLocation = "$locationCity Amazon Now Fulfillment",
                shippingInfo = "⚡ 15 mins • Amazon Fresh Now Express"
            )
        )

        // 6. BB Now (BigBasket 10-15 mins)
        offers.add(
            StoreOffer(
                storeName = "BB Now",
                price = (basePrice * (if (isGroceryOrSnack) 0.92 else 0.93) + Random.nextDouble() * 0.06).roundInr(),
                listPrice = (basePrice * 1.11).roundInr(),
                url = "https://www.bigbasket.com/bbnow/search/?q=$cleanQuery",
                inStock = true,
                rating = 4.5f,
                reviewsCount = 27800,
                deliveryEta = "14 mins",
                isQuickCommerce = true,
                darkStoreLocation = "Tata BB Now Dark Hub $locationCity",
                shippingInfo = "⚡ 14 mins • Tata Neu Coin Rewards"
            )
        )

        // 7. Flipkart (Standard E-Commerce)
        offers.add(
            StoreOffer(
                storeName = "Flipkart",
                price = (basePrice * (0.88 + Random.nextDouble() * 0.12)).roundInr(),
                listPrice = (basePrice * 1.20).roundInr(),
                url = "https://www.flipkart.com/search?q=$cleanQuery",
                inStock = true,
                rating = 4.6f,
                reviewsCount = 98400,
                deliveryEta = "2 Days",
                isQuickCommerce = false,
                darkStoreLocation = "Central Warehouses",
                shippingInfo = "Flipkart Plus Assured • 2 Days Delivery"
            )
        )

        // 8. Amazon.in (Standard E-Commerce)
        offers.add(
            StoreOffer(
                storeName = "Amazon.in",
                price = (basePrice * (0.89 + Random.nextDouble() * 0.11)).roundInr(),
                listPrice = (basePrice * 1.18).roundInr(),
                url = "https://www.amazon.in/s?k=$cleanQuery",
                inStock = true,
                rating = 4.7f,
                reviewsCount = 104500,
                deliveryEta = "Tomorrow",
                isQuickCommerce = false,
                darkStoreLocation = "Amazon Fulfillment Center $locationCity",
                shippingInfo = "Amazon Prime • Free 1-Day Delivery"
            )
        )

        val minPrice = offers.minOf { it.price }
        return offers.map { offer ->
            offer.copy(isBestPrice = offer.price == minPrice)
        }.sortedBy { it.price }
    }

    fun simulatePriceCheck(currentPrice: Double, lowestPrice: Double): Pair<Double, String> {
        val roll = Random.nextInt(100)
        return when {
            roll < 45 -> {
                val dropPercent = (Random.nextDouble(2.0, 12.0) / 100.0)
                val newPrice = (currentPrice * (1.0 - dropPercent)).roundInr()
                val reason = if (newPrice < lowestPrice) "All-time low deal!" else "Flash discount (-${(dropPercent * 100).roundToInt()}%)"
                Pair(newPrice, reason)
            }
            roll < 70 -> {
                val hikePercent = (Random.nextDouble(2.0, 8.0) / 100.0)
                val newPrice = (currentPrice * (1.0 + hikePercent)).roundInr()
                Pair(newPrice, "Price increased (+${(hikePercent * 100).roundToInt()}%)")
            }
            else -> {
                Pair(currentPrice, "No price change detected")
            }
        }
    }

    private fun Double.roundInr(): Double {
        return (this.roundToInt()).toDouble()
    }
}
