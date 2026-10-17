package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AjioColor
import com.example.ui.theme.AmazonIndiaColor
import com.example.ui.theme.AmazonNowColor
import com.example.ui.theme.BbNowColor
import com.example.ui.theme.BlinkitColor
import com.example.ui.theme.CromaColor
import com.example.ui.theme.FlipkartColor
import com.example.ui.theme.FlipkartMinutesColor
import com.example.ui.theme.GenericStoreColor
import com.example.ui.theme.MyntraColor
import com.example.ui.theme.RelianceDigitalColor
import com.example.ui.theme.SwiggyColor
import com.example.ui.theme.TataCliqColor
import com.example.ui.theme.ZeptoColor

@Composable
fun StoreBadge(
    storeName: String,
    modifier: Modifier = Modifier
) {
    val storeLower = storeName.lowercase()
    val storeColor = when {
        storeLower.contains("minutes") -> FlipkartMinutesColor
        storeLower.contains("amazon now") || storeLower.contains("amazon fresh") -> AmazonNowColor
        storeLower.contains("bb now") || storeLower.contains("bigbasket") -> BbNowColor
        storeLower.contains("blinkit") -> BlinkitColor
        storeLower.contains("zepto") -> ZeptoColor
        storeLower.contains("swiggy") || storeLower.contains("instamart") -> SwiggyColor
        storeLower.contains("flipkart") -> FlipkartColor
        storeLower.contains("amazon") -> AmazonIndiaColor
        storeLower.contains("croma") -> CromaColor
        storeLower.contains("reliance") -> RelianceDigitalColor
        storeLower.contains("tata") || storeLower.contains("cliq") -> TataCliqColor
        storeLower.contains("myntra") -> MyntraColor
        storeLower.contains("ajio") -> AjioColor
        else -> GenericStoreColor
    }

    Box(
        modifier = modifier
            .background(storeColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .border(1.dp, storeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(storeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = storeName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = storeColor
            )
        }
    }
}
