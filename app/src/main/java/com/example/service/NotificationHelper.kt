package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.util.CurrencyUtils
import java.util.Locale

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "price_change_alerts_channel"
        const val CHANNEL_NAME = "Price Pulse Alerts"
        const val CHANNEL_DESCRIPTION = "Alerts for price drops, increases, and dark store availability across Blinkit, Zepto, Swiggy, Flipkart, Amazon"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendPriceChangeNotification(
        productId: Long,
        productTitle: String,
        store: String,
        oldPrice: Double,
        newPrice: Double,
        isIncrease: Boolean,
        changeAmount: Double,
        changePercentage: Double
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_product_id", productId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedChange = CurrencyUtils.formatInr(changeAmount)
        val formattedNewPrice = CurrencyUtils.formatInr(newPrice)
        val formattedOldPrice = CurrencyUtils.formatInr(oldPrice)
        val formattedPercent = String.format(Locale.US, "%.1f%%", changePercentage)

        val title = if (isIncrease) {
            "⚠️ Price Increased on $store"
        } else {
            "🎉 Price Dropped on $store!"
        }

        val contentText = if (isIncrease) {
            "$productTitle jumped from $formattedOldPrice to $formattedNewPrice (+$formattedPercent)"
        } else {
            "$productTitle dropped to $formattedNewPrice (Saved $formattedChange, -$formattedPercent)"
        }

        val bigText = if (isIncrease) {
            "$productTitle price increased by $formattedChange (+$formattedPercent) on $store.\n" +
                    "Previous: $formattedOldPrice ➔ Current: $formattedNewPrice"
        } else {
            "$productTitle dropped by $formattedChange (-$formattedPercent) on $store.\n" +
                    "Previous: $formattedOldPrice ➔ Current: $formattedNewPrice"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(productId.toInt(), notification)
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    fun sendPriceDropNotification(
        productId: Long,
        productTitle: String,
        store: String,
        oldPrice: Double,
        newPrice: Double,
        dropAmount: Double,
        dropPercentage: Double
    ) {
        sendPriceChangeNotification(
            productId = productId,
            productTitle = productTitle,
            store = store,
            oldPrice = oldPrice,
            newPrice = newPrice,
            isIncrease = false,
            changeAmount = dropAmount,
            changePercentage = dropPercentage
        )
    }
}
