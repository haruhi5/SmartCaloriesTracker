package com.gemini.calories.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gemini.calories.data.local.FoodEntryEntity
import com.gemini.calories.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val todayCalories by viewModel.todayCalories.collectAsState()
    val targetCalories by viewModel.targetCalories.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val macros by viewModel.macros.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    
    var showConsumed by remember { mutableStateOf(true) }
    var showTarget by remember { mutableStateOf(true) }
    var showDifference by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalorieProgressCard(
                    consumed = todayCalories,
                    target = targetCalories,
                    protein = macros.first,
                    carbs = macros.second,
                    fat = macros.third
                )
            }

            item {
                CalorieChartCard(
                    chartData = chartData,
                    showConsumed = showConsumed,
                    showTarget = showTarget,
                    showDifference = showDifference,
                    onConsumedToggle = { showConsumed = it },
                    onTargetToggle = { showTarget = it },
                    onDifferenceToggle = { showDifference = it }
                )
            }

            item {
                Text(
                    text = "Today's Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(todayEntries) { entry ->
                FoodEntryCard(entry)
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Screen.Analysis.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add Food")
        }
    }
}

@Composable
fun CalorieProgressCard(
    consumed: Int,
    target: Int,
    protein: Float,
    carbs: Float,
    fat: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalorieRing(
                consumed = consumed,
                target = target,
                size = 180.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroItem("Protein", "${protein.toInt()}g", Color(0xFF2196F3), Modifier.weight(1f))
                MacroItem("Carbs", "${carbs.toInt()}g", Color(0xFFFF9800), Modifier.weight(1f))
                MacroItem("Fat", "${fat.toInt()}g", Color(0xFFE91E63), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CalorieRing(
    consumed: Int,
    target: Int,
    size: Dp
) {
    val progress = (consumed.toFloat() / target).coerceIn(0f, 1.5f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing), 
        label = "progress"
    )
    
    val color = when {
        progress < 0.8f -> Color(0xFF4CAF50)  // Green - under target
        progress < 1.0f -> Color(0xFFFFC107)  // Yellow - near target
        else -> Color(0xFFF44336)              // Red - over target
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background arc
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$consumed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "/ $target kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
fun CalorieChartCard(
    chartData: List<ChartDataPoint>,
    showConsumed: Boolean,
    showTarget: Boolean,
    showDifference: Boolean,
    onConsumedToggle: (Boolean) -> Unit,
    onTargetToggle: (Boolean) -> Unit,
    onDifferenceToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "7-Day Calorie Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Legend/Toggle chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showConsumed,
                    onClick = { onConsumedToggle(!showConsumed) },
                    label = { Text("Consumed") },
                    leadingIcon = {
                        if (showConsumed) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                )
                FilterChip(
                    selected = showTarget,
                    onClick = { onTargetToggle(!showTarget) },
                    label = { Text("Target") },
                    leadingIcon = {
                        if (showTarget) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0xFF2196F3))
                                }
                            }
                        }
                    }
                )
                FilterChip(
                    selected = showDifference,
                    onClick = { onDifferenceToggle(!showDifference) },
                    label = { Text("Diff") },
                    leadingIcon = {
                        if (showDifference) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0xFFFF9800))
                                }
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                SimpleLineChart(
                    chartData = chartData,
                    showConsumed = showConsumed,
                    showTarget = showTarget,
                    showDifference = showDifference,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                if (showDifference) {
                    val latest = chartData.last()
                    val diff = latest.difference
                    val diffText = when {
                        diff > 0 -> "${diff} kcal over target"
                        diff < 0 -> "${-diff} kcal under target"
                        else -> "On target"
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Latest diff: $diffText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    chartData: List<ChartDataPoint>,
    showConsumed: Boolean,
    showTarget: Boolean,
    showDifference: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(8.dp)) {
        if (chartData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val spacing = width / (chartData.size - 1).coerceAtLeast(1)
        
        // Find min/max for scaling
        val allValues = mutableListOf<Int>()
        if (showConsumed) allValues.addAll(chartData.map { it.consumed })
        if (showTarget) allValues.addAll(chartData.map { it.target })
        if (showDifference) allValues.addAll(chartData.map { it.difference })
        
        if (allValues.isEmpty()) return@Canvas
        
        val minValue = allValues.minOrNull() ?: 0
        val maxValue = allValues.maxOrNull() ?: 2000
        val range = (maxValue - minValue).coerceAtLeast(1)
        
        fun scaleY(value: Int): Float {
            return height - ((value - minValue).toFloat() / range * height)
        }
        
        // Draw consumed line
        if (showConsumed && chartData.size > 1) {
            for (i in 0 until chartData.size - 1) {
                val x1 = i * spacing
                val y1 = scaleY(chartData[i].consumed)
                val x2 = (i + 1) * spacing
                val y2 = scaleY(chartData[i + 1].consumed)
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                    strokeWidth = 4f
                )
            }
        }
        
        // Draw target line
        if (showTarget && chartData.size > 1) {
            for (i in 0 until chartData.size - 1) {
                val x1 = i * spacing
                val y1 = scaleY(chartData[i].target)
                val x2 = (i + 1) * spacing
                val y2 = scaleY(chartData[i + 1].target)
                drawLine(
                    color = Color(0xFF2196F3),
                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                    strokeWidth = 4f
                )
            }
        }
        
        // Draw difference line
        if (showDifference && chartData.size > 1) {
            for (i in 0 until chartData.size - 1) {
                val x1 = i * spacing
                val y1 = scaleY(chartData[i].difference)
                val x2 = (i + 1) * spacing
                val y2 = scaleY(chartData[i + 1].difference)
                drawLine(
                    color = Color(0xFFFF9800),
                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                    strokeWidth = 4f
                )
            }
        }
        
        // Draw points
        chartData.forEachIndexed { index, point ->
            val x = index * spacing
            if (showConsumed) {
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, scaleY(point.consumed))
                )
            }
            if (showTarget) {
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, scaleY(point.target))
                )
            }
            if (showDifference) {
                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, scaleY(point.difference))
                )
            }
        }
    }
}

@Composable
fun FoodEntryCard(entry: FoodEntryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.foodName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = "${entry.portion} • ${entry.mealType}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${entry.calories} kcal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
