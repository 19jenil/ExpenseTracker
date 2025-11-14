package com.example.expensetracker

import AddExpenseDialog
import CreateSheetDialog
import DeleteExpenseDialog
import EditExpenseDialog
import EditIncomeDialog
import ExpenseSheetDetailView
import ExpenseSheetList
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.compose.ui.text.font.FontWeight


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp() {
    val context = LocalContext.current
    val viewModel = remember {
        ViewModelProvider(
            context as ViewModelStoreOwner,
            ExpenseViewModelFactory(context)
        )[ExpenseViewModel::class.java]
    }

    // Store only the sheet ID instead of the whole object
    var selectedSheetId by remember { mutableStateOf<String?>(null) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    // Get the current sheet by ID every time we recompose
    val selectedSheet = selectedSheetId?.let { viewModel.getSheetById(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedSheet == null)
                            "Expense Tracker"
                        else
                            selectedSheet.getDisplayName()
                    )
                },
                navigationIcon = {
                    // Show back button only when viewing a sheet
                    if (selectedSheet != null) {
                        IconButton(onClick = { selectedSheetId = null }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back to list",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    // Show Graph button only on main screen
                    if (selectedSheet == null) {
                        TextButton(
                            onClick = {
                                val intent = Intent(context, GraphActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Text(
                                text = "Graph",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            // Only show FAB on main screen
            if (selectedSheet == null) {
                FloatingActionButton(
                    onClick = { viewModel.openCreateSheetDialog() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create New Sheet")
                }
            }
        }
    )  { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedSheet != null -> {
                    ExpenseSheetDetailView(
                        sheet = selectedSheet,
                        onEditIncome = { viewModel.openEditIncomeDialog() },
                        onAddExpense = { showAddExpenseDialog = true },
                        onEditExpense = { expense ->
                            viewModel.openEditExpenseDialog(selectedSheet.sheetId, expense)
                        },
                        onDeleteExpense = { expense ->
                            expenseToDelete = expense
                        }
                    )
                }

                viewModel.expenseSheets.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expense sheets created.\nClick + to create one!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    ExpenseSheetList(
                        sheets = viewModel.expenseSheets,
                        onSheetClick = { sheet -> selectedSheetId = sheet.sheetId }
                    )
                }
            }
        }

        // Create Sheet Dialog
        if (viewModel.showCreateSheetDialog.value) {
            CreateSheetDialog(
                onDismiss = { viewModel.closeCreateSheetDialog() },
                onConfirm = { month, year ->
                    viewModel.createNewSheet(month, year)
                },
                existingSheetChecker = { month, year ->
                    viewModel.checkIfSheetExists(month, year)
                }
            )
        }

        // Edit Income Dialog
        if (viewModel.showEditIncomeDialog.value && selectedSheet != null) {
            EditIncomeDialog(
                currentIncome = selectedSheet.income,
                onDismiss = { viewModel.closeEditIncomeDialog() },
                onConfirm = { newIncome ->
                    viewModel.updateIncome(selectedSheet.sheetId, newIncome)
                }
            )
        }

        // Add Expense Dialog
        if (showAddExpenseDialog && selectedSheet != null) {
            AddExpenseDialog(
                onDismiss = { showAddExpenseDialog = false },
                onConfirm = { expense ->
                    viewModel.addExpense(selectedSheet.sheetId, expense)
                    showAddExpenseDialog = false
                }
            )
        }
        // Edit Expense Dialog
        if (viewModel.showEditExpenseDialog.value && viewModel.editingExpense.value != null) {
            val (sheetId, expense) = viewModel.editingExpense.value!!
            EditExpenseDialog(
                expense = expense,
                onDismiss = { viewModel.closeEditExpenseDialog() },
                onConfirm = { updatedExpense ->
                    viewModel.updateExpense(sheetId, expense.expenseId, updatedExpense)
                }
            )
        }

        // Delete Expense Confirmation Dialog
        if (expenseToDelete != null && selectedSheet != null) {
            DeleteExpenseDialog(
                expense = expenseToDelete!!,
                onDismiss = { expenseToDelete = null },
                onConfirm = {
                    viewModel.deleteExpense(selectedSheet.sheetId, expenseToDelete!!.expenseId)
                    expenseToDelete = null
                }
            )
        }
    }
}

