package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PriceHistoryEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDrop
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChartTimeRange(val label: String, val durationMs: Long) {
    HOURS_24("24H", 24 * 60 * 60 * 1000L),
    DAYS_7("7D", 7 * 24 * 60 * 60 * 1000L),
    DAYS_30("30D", 30 * 24 * 60 * 60 * 1000L),
    ALL("All", Long.MAX_VALUE)
}

@Composable
fun PriceHistoryChart(
    priceHistory: List<PriceHistoryEntity>,
    targetPrice: Double = 0.0,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldPrimary
) {
    var selectedRange by remember { mutableStateOf(ChartTimeRange.ALL) }
    var scrubX by remember { mutableFloatStateOf(-1f) }
    var hoveredPoint by remember { mutableStateOf<PriceHistoryEntity?>(null) }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(priceHistory, selectedRange) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }

    val now = System.currentTimeMillis()
    val filteredData = remember(priceHistory, selectedRange) {
        if (priceHistory.isEmpty()) emptyList()
        else {
            val cutoff = if (selectedRange.durationMs == Long.MAX_VALUE) 0L else now - selectedRange.durationMs
            val filtered = priceHistory.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
            if (filtered.isEmpty() && priceHistory.isNotEmpty()) listOf(priceHistory.last()) else filtered
        }
    }

    val minPrice = remember(filteredData, targetPrice) {
        val dataMin = filteredData.minOfOrNull { it.price } ?: 0.0
        val effectiveMin = if (targetPrice > 0.0) minOf(dataMin, targetPrice) else dataMin
        (effectiveMin * 0.95)
    }

    val maxPrice = remember(filteredData, targetPrice) {
        val dataMax = filteredData.maxOfOrNull { it.price } ?: 1000.0
        val effectiveMax = if (targetPrice > 0.0) maxOf(dataMax, targetPrice) else dataMax
        (effectiveMax * 1.05)
    }

    val currentPoint = filteredData.lastOrNull()
    val lowestPoint = filteredData.minByOrNull { it.price }
    val highestPoint = filteredData.maxByOrNull { it.price }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Chart Header with active price info and time filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val displayPrice = hoveredPoint?.price ?: currentPoint?.price ?: 0.0
                val displayDate = hoveredPoint?.timestamp ?: currentPoint?.timestamp ?: now
                val displayReason = hoveredPoint?.changeReason ?: "Current Price"

                Text(
                    text = CurrencyUtils.formatInr(displayPrice),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(displayDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $displayReason",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = EmeraldPrimary
                    )
                }
            }

            // Time range selector
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChartTimeRange.values().forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = {
                            selectedRange = range
                            scrubX = -1f
                            hoveredPoint = null
                        },
                        label = { Text(range.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null,
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (filteredData.size < 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tracking started. Awaiting next price data point…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val surfaceColor = MaterialTheme.colorScheme.surface
                val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredData) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    scrubX = offset.x
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    scrubX = change.position.x
                                },
                                onDragEnd = {
                                    scrubX = -1f
                                    hoveredPoint = null
                                },
                                onDragCancel = {
                                    scrubX = -1f
                                    hoveredPoint = null
                                }
                            )
                        }
                        .pointerInput(filteredData) {
                            detectTapGestures { offset ->
                                scrubX = offset.x
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val priceSpan = (maxPrice - minPrice).coerceAtLeast(1.0)
                    val timeStart = filteredData.first().timestamp
                    val timeEnd = filteredData.last().timestamp
                    val timeSpan = (timeEnd - timeStart).coerceAtLeast(1L)

                    // Draw subtle grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height * (i.toFloat() / gridLines)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute points
                    val points = filteredData.map { item ->
                        val x = if (timeSpan > 0) ((item.timestamp - timeStart).toFloat() / timeSpan) * width else width / 2
                        val y = height - (((item.price - minPrice) / priceSpan).toFloat() * height * animationProgress.value)
                        Offset(x, y.coerceIn(0f, height))
                    }

                    // Build path and gradient area
                    val linePath = Path()
                    val fillPath = Path()

                    points.forEachIndexed { index, pt ->
                        if (index == 0) {
                            linePath.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, height)
                            fillPath.lineTo(pt.x, pt.y)
                        } else {
                            val prevPt = points[index - 1]
                            val midX = (prevPt.x + pt.x) / 2
                            linePath.cubicTo(midX, prevPt.y, midX, pt.y, pt.x, pt.y)
                            fillPath.cubicTo(midX, prevPt.y, midX, pt.y, pt.x, pt.y)
                        }
                    }

                    if (points.isNotEmpty()) {
                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Gradient fill under curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.35f),
                                    lineColor.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Stroke line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Target price dashed line
                    if (targetPrice > 0.0 && targetPrice in minPrice..maxPrice) {
                        val targetY = height - (((targetPrice - minPrice) / priceSpan).toFloat() * height * animationProgress.value)
                        drawLine(
                            color = AmberAccent,
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Draw data dots
                    points.forEachIndexed { i, pt ->
                        val item = filteredData[i]
                        val isLowest = item == lowestPoint
                        val isHighest = item == highestPoint

                        val dotColor = when {
                            isLowest -> EmeraldPrimary
                            isHighest -> RoseDrop
                            else -> lineColor
                        }

                        drawCircle(
                            color = surfaceColor,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = dotColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    }

                    // Scrubbing line & nearest point
                    if (scrubX >= 0f && points.isNotEmpty()) {
                        val clampedX = scrubX.coerceIn(0f, width)
                        var closestIndex = 0
                        var minDistance = Float.MAX_VALUE
                        points.forEachIndexed { idx, pt ->
                            val dist = kotlin.math.abs(pt.x - clampedX)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestIndex = idx
                            }
                        }

                        val activePt = points[closestIndex]
                        hoveredPoint = filteredData[closestIndex]

                        drawLine(
                            color = onSurfaceColor.copy(alpha = 0.5f),
                            start = Offset(activePt.x, 0f),
                            end = Offset(activePt.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        drawCircle(
                            color = surfaceColor,
                            radius = 8.dp.toPx(),
                            center = activePt
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = activePt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Stats Footer in INR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            lowestPoint?.let {
                StatBadge(
                    label = "Lowest",
                    value = CurrencyUtils.formatInr(it.price),
                    color = EmeraldPrimary
                )
            }
            highestPoint?.let {
                StatBadge(
                    label = "Highest",
                    value = CurrencyUtils.formatInr(it.price),
                    color = RoseDrop
                )
            }
            if (targetPrice > 0.0) {
                StatBadge(
                    label = "Target",
                    value = CurrencyUtils.formatInr(targetPrice),
                    color = AmberAccent
                )
            }
        }
    }
}

val AmberAccent = Color(0xFFF59E0B)

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
