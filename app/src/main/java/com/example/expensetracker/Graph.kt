package com.example.expensetracker

import LightPink
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.math.max

class GraphActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GraphScreen(onBackPressed = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        ViewModelProvider(
            context as ViewModelStoreOwner,
            ExpenseViewModelFactory(context)
        )[ExpenseViewModel::class.java]
    }

    // Track which 4-month window we're viewing
    var currentOffset by remember { mutableStateOf(0) }

    // Get all sheets sorted chronologically
    val allSheets = viewModel.expenseSheets
        .sortedBy { it.yearValue * 12 + it.monthValue }

    // Calculate how many 4-month windows we can show
    val maxOffset = (allSheets.size - 4).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income/Expenses Graph") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightPink
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Income/Expenses",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (allSheets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No data to display. Create expense sheets first.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // Show swipe instruction
                Text(
                    text = "← Swipe to navigate →",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Get the current 4 sheets to display
                val startIndex = currentOffset.coerceIn(0, maxOffset)
                val endIndex = (startIndex + 4).coerceAtMost(allSheets.size)
                val currentSheets = allSheets.subList(startIndex, endIndex)

                // Display date range
                if (currentSheets.isNotEmpty()) {
                    Text(
                        text = "${currentSheets.first().getDisplayName()} - ${currentSheets.last().getDisplayName()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                CustomIncomeExpenseGraph(
                    sheets = currentSheets,
                    onSwipeLeft = {
                        if (currentOffset < maxOffset) {
                            currentOffset++
                        }
                    },
                    onSwipeRight = {
                        if (currentOffset > 0) {
                            currentOffset--
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentOffset > 0) currentOffset-- },
                        enabled = currentOffset > 0
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = if (currentOffset > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }

                    Text(
                        text = "${currentOffset + 1} / ${maxOffset + 1}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = { if (currentOffset < maxOffset) currentOffset++ },
                        enabled = currentOffset < maxOffset
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = if (currentOffset < maxOffset)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                GraphLegend()
            }
        }
    }
}


@Composable
fun CustomIncomeExpenseGraph(
    sheets: List<ExpenseSheet>,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
) {
    if (sheets.isEmpty()) return

    // Calculate max value for y-axis scaling
    val maxIncome = sheets.maxOfOrNull { it.income } ?: 0.0
    val maxExpense = sheets.maxOfOrNull { it.expenses.sumOf { exp -> exp.amount } } ?: 0.0
    val maxValue = max(maxIncome, maxExpense)
    val yAxisMax = ((maxValue / 1000).toInt() + 1) * 1000.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Gesture completed
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()

                        // Detect swipe direction
                        if (dragAmount > 50) {
                            // Swiped right - go to previous
                            onSwipeRight()
                        } else if (dragAmount < -50) {
                            // Swiped left - go to next
                            onSwipeLeft()
                        }
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Margins
            val leftMargin = 80f
            val bottomMargin = 80f
            val topMargin = 40f
            val rightMargin = 40f

            val graphWidth = canvasWidth - leftMargin - rightMargin
            val graphHeight = canvasHeight - topMargin - bottomMargin

            // Draw Y-axis
            drawLine(
                color = Color.Black,
                start = Offset(leftMargin, topMargin),
                end = Offset(leftMargin, canvasHeight - bottomMargin),
                strokeWidth = 3f
            )

            // Draw X-axis
            drawLine(
                color = Color.Black,
                start = Offset(leftMargin, canvasHeight - bottomMargin),
                end = Offset(canvasWidth - rightMargin, canvasHeight - bottomMargin),
                strokeWidth = 3f
            )

            // Draw Y-axis labels and grid lines
            val ySteps = 5
            for (i in 0..ySteps) {
                val yValue = (yAxisMax / ySteps) * i
                val yPos = canvasHeight - bottomMargin - (graphHeight / ySteps) * i

                // Grid line
                drawLine(
                    color = Color.LightGray,
                    start = Offset(leftMargin, yPos),
                    end = Offset(canvasWidth - rightMargin, yPos),
                    strokeWidth = 1f
                )

                // Y-axis label
                drawContext.canvas.nativeCanvas.apply {
                    val labelText = if (yValue >= 1000) {
                        val thousands = yValue / 1000.0
                        if (thousands == thousands.toInt().toDouble()) {
                            "$${thousands.toInt()}k"
                        } else {
                            "$${String.format("%.1f", thousands)}k"
                        }
                    } else {
                        "$${yValue.toInt()}"
                    }

                    drawText(
                        labelText,
                        leftMargin - 15f,
                        yPos + 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }

            // Calculate points for line graphs
            val spacing = if (sheets.size > 1) {
                graphWidth / (sheets.size - 1)
            } else {
                0f
            }

            // Income line points
            val incomePoints = sheets.mapIndexed { index, sheet ->
                val x = leftMargin + (spacing * index)
                val y = canvasHeight - bottomMargin - (sheet.income / yAxisMax * graphHeight).toFloat()
                Offset(x, y)
            }

            // Expense line points
            val expensePoints = sheets.mapIndexed { index, sheet ->
                val totalExpenses = sheet.expenses.sumOf { it.amount }
                val x = leftMargin + (spacing * index)
                val y = canvasHeight - bottomMargin - (totalExpenses / yAxisMax * graphHeight).toFloat()
                Offset(x, y)
            }

            // Draw Income line (Blue)
            if (incomePoints.size >= 2) {
                val incomePath = Path().apply {
                    moveTo(incomePoints[0].x, incomePoints[0].y)
                    for (i in 1 until incomePoints.size) {
                        lineTo(incomePoints[i].x, incomePoints[i].y)
                    }
                }
                drawPath(
                    path = incomePath,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }

            // Draw Expense line (Red)
            if (expensePoints.size >= 2) {
                val expensePath = Path().apply {
                    moveTo(expensePoints[0].x, expensePoints[0].y)
                    for (i in 1 until expensePoints.size) {
                        lineTo(expensePoints[i].x, expensePoints[i].y)
                    }
                }
                drawPath(
                    path = expensePath,
                    color = Color(0xFFF44336),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }

            // Draw points on income line
            incomePoints.forEach { point ->
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = point
                )
            }

            // Draw points on expense line
            expensePoints.forEach { point ->
                drawCircle(
                    color = Color(0xFFF44336),
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = point
                )
            }

            // Draw X-axis labels
            sheets.forEachIndexed { index, sheet ->
                val monthAbbrev = getMonthAbbreviation(sheet.monthValue)
                val x = leftMargin + (spacing * index)

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        monthAbbrev,
                        x,
                        canvasHeight - bottomMargin + 40f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 35f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    drawText(
                        "'${sheet.yearValue % 100}",
                        x,
                        canvasHeight - bottomMargin + 70f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // Draw Y-axis title
            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate(-90f, -20f, canvasHeight / 2)
                drawText(
                    "Amount (€)",
                    -40f,
                    canvasHeight / 2,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 40f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
                restore()
            }

            // Draw X-axis title
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "Months",
                    canvasWidth / 2,
                    canvasHeight - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 40f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}



@Composable
fun GraphLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = Color(0xFF2196F3), label = "Income")
        LegendItem(color = Color(0xFFF44336), label = "Expenses")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .padding(end = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = color)
            }
        }
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getMonthAbbreviation(monthValue: Int): String {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return if (monthValue in 1..12) months[monthValue - 1] else "???"
}
