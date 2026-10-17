package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatInr(amount: Double): String {
        return try {
            val formatted = inrFormat.format(amount)
            // Ensure standard rupee symbol
            if (!formatted.startsWith("₹")) {
                "₹${NumberFormat.getNumberInstance(Locale("en", "IN")).format(amount.toLong())}"
            } else {
                formatted
            }
        } catch (_: Exception) {
            "₹${amount.toLong()}"
        }
    }
}
