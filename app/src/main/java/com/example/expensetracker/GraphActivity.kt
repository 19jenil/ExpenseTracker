package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
        ViewModelProvider(context as ViewModelStoreOwner)[ExpenseViewModel::class.java]
    }

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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (viewModel.expenseSheets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display. Create expense sheets first.")
                }
            } else {
                // Get the most recent 4 sheets
                val recentSheets = viewModel.expenseSheets
                    .sortedBy { it.yearValue * 12 + it.monthValue }
                    .takeLast(4)

                CustomIncomeExpenseGraph(sheets = recentSheets)

                Spacer(modifier = Modifier.height(24.dp))

                // Legend
                GraphLegend()
            }
        }
    }
}

@Composable
fun CustomIncomeExpenseGraph(sheets: List<ExpenseSheet>) {
    // Calculate max value for y-axis scaling
    val maxIncome = sheets.maxOfOrNull { it.income } ?: 0.0
    val maxExpense = sheets.maxOfOrNull { it.expenses.sumOf { exp -> exp.amount } } ?: 0.0
    val maxValue = max(maxIncome, maxExpense)
    val yAxisMax = ((maxValue / 1000).toInt() + 1) * 1000.0 // Round up to nearest thousand

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square aspect ratio as required
            .padding(8.dp)
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
                    drawText(
                        "$${(yValue / 1000).toInt()}k",
                        leftMargin - 60f,
                        yPos + 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }

            // Draw bars for each month
            val barWidth = graphWidth / (sheets.size * 3)
            val groupWidth = graphWidth / sheets.size

            sheets.forEachIndexed { index, sheet ->
                val groupStartX = leftMargin + (groupWidth * index)
                val totalExpenses = sheet.expenses.sumOf { it.amount }

                // Income bar (Blue)
                val incomeHeight = (sheet.income / yAxisMax * graphHeight).toFloat()
                val incomeBarX = groupStartX + barWidth * 0.5f

                drawRect(
                    color = Color(0xFF2196F3),
                    topLeft = Offset(incomeBarX, canvasHeight - bottomMargin - incomeHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, incomeHeight)
                )

                // Expense bar (Red)
                val expenseHeight = (totalExpenses / yAxisMax * graphHeight).toFloat()
                val expenseBarX = groupStartX + barWidth * 1.8f

                drawRect(
                    color = Color(0xFFF44336),
                    topLeft = Offset(expenseBarX, canvasHeight - bottomMargin - expenseHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, expenseHeight)
                )

                // X-axis label (Month abbreviation)
                val monthAbbrev = getMonthAbbreviation(sheet.monthValue)
                val labelX = groupStartX + groupWidth / 2

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        monthAbbrev,
                        labelX,
                        canvasHeight - bottomMargin + 40f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 35f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    // Year below month
                    drawText(
                        "'${sheet.yearValue % 100}",
                        labelX,
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
                rotate(-90f, 30f, canvasHeight / 2)
                drawText(
                    "Amount ($)",
                    30f,
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
