package com.example.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ─── CALORIE PROGRESS RING ───────────────────────────────────────────────

@Composable
fun CalorieRingCard(
    consumed: Double,
    target: Double,
    protein: Double,
    proteinTarget: Double = 130.0,
    carbs: Double,
    carbsTarget: Double = 220.0,
    fat: Double,
    fatTarget: Double = 60.0,
    modifier: Modifier = Modifier
) {
    val remaining = (target - consumed).coerceAtLeast(0.0)
    val percentage = if (target > 0) (consumed / target).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "calorie_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Calories",
                    tint = NutriGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Calorie Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Foreground progress ring
                val ringColor = when {
                    consumed > target -> CalorieExceeded
                    remaining < 200 -> CalorieWarning
                    else -> CalorieSafe
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${remaining.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "kcal remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = NutriGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target: ${target.toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = NutriGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Macro bars row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroBar(
                    label = "Protein",
                    amount = protein,
                    target = proteinTarget,
                    color = ProteinColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MacroBar(
                    label = "Carbs",
                    amount = carbs,
                    target = carbsTarget,
                    color = CarbsColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MacroBar(
                    label = "Fat",
                    amount = fat,
                    target = fatTarget,
                    color = FatColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroBar(
    label: String,
    amount: Double,
    target: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (amount / target).toFloat().coerceIn(0f, 1f) else 0f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "${amount.toInt()}/${target.toInt()}g", style = MaterialTheme.typography.labelSmall, color = NutriGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// ─── WATER TRACKER ────────────────────────────────────────────────────────

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    targetMl: Int,
    onAddWater: (Int) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Water intake",
                    tint = WaterBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Hydration Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target: $targetMl ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = NutriGray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset hydration", tint = NutriGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WaterBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Wave/Fill Background
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(WaterBlue, WaterBlue.copy(alpha = 0.8f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currentMl ml",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (progress > 0.4f) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% completed",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (progress > 0.8f) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onAddWater(250) },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("250 ml (Glass)")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { onAddWater(500) },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue.copy(alpha = 0.2f), contentColor = WaterBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("500 ml (Bottle)")
                }
            }
        }
    }
}

// ─── FOOD CARD ITEM ──────────────────────────────────────────────────────

@Composable
fun FoodItemCard(
    name: String,
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    servingSize: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Serving: $servingSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = NutriGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroChip(label = "P: ${protein.toInt()}g", color = ProteinColor)
                    MacroChip(label = "C: ${carbs.toInt()}g", color = CarbsColor)
                    MacroChip(label = "F: ${fat.toInt()}g", color = FatColor)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${calories.toInt()} kcal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NutriGreenDark
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete food",
                        tint = Color.Red.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─── STEP COUNTER ────────────────────────────────────────────────────────

@Composable
fun StepCounterCard(
    steps: Int,
    onAddSteps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (steps.toFloat() / 10000f).coerceIn(0f, 1f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = FastingGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Step Counter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$steps / 10,000 steps",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FastingGold,
                    trackColor = FastingGold.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calories Burned: ${(steps * 0.04).toInt()} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = NutriGray
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            FloatingActionButton(
                onClick = onAddSteps,
                containerColor = FastingGold,
                contentColor = Color(0xFF1C1C1E),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add steps", modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ─── FASTING TIMER CARD ──────────────────────────────────────────────────

@Composable
fun FastingTimerCard(
    isFasting: Boolean,
    protocol: String,
    startTime: Long?,
    onToggle: () -> Unit,
    onChangeProtocol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(key1 = isFasting, key2 = startTime) {
        if (isFasting && startTime != null) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                kotlinx.coroutines.delay(1000)
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val totalHours = protocol.substringBefore(":").toInt()
    val totalSecondsGoal = totalHours * 3600
    val progress = if (totalSecondsGoal > 0) (elapsedSeconds.toFloat() / totalSecondsGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedTime = if (isFasting) {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        "00:00:00"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = FastingGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Intermittent Fasting Tracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isFasting) {
                Text(text = "Choose fasting window:", style = MaterialTheme.typography.bodySmall, color = NutriGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("16:8", "18:6", "20:4").forEach { item ->
                        val selected = protocol == item
                        Button(
                            onClick = { onChangeProtocol(item) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) FastingGold else Color.LightGray.copy(alpha = 0.2f),
                                contentColor = if (selected) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = item, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Fasting Protocol active: $protocol window",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FastingGold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = formattedTime,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = if (isFasting) FastingGold else MaterialTheme.colorScheme.onSurface
            )

            if (isFasting) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FastingGold,
                    trackColor = FastingGold.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}% of fasting period elapsed",
                    style = MaterialTheme.typography.labelSmall,
                    color = NutriGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFasting) CalorieExceeded else FastingGold
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isFasting) "End Fasting Window" else "Start Fasting Window",
                    fontWeight = FontWeight.Bold,
                    color = if (isFasting) Color.White else Color(0xFF1C1C1E)
                )
            }
        }
    }
}
