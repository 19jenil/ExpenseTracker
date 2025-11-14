import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.text.style.TextAlign
import com.example.expensetracker.Expense
import com.example.expensetracker.ExpenseSheet
import java.util.*

@Composable
fun CreateSheetDialog(
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit,
    existingSheetChecker: (Int, Int) -> Boolean
) {
    val calendar = Calendar.getInstance()

    // Default to current month and year
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var errorMessage by remember { mutableStateOf("") }

    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create New Expense Sheet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Year")
                    }

                    Text(
                        text = selectedYear.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Select Month",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Month grid selector
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(monthNames.size) { index ->
                        val monthNumber = index + 1
                        val isSelected = selectedMonth == monthNumber

                        Box(
                            modifier = Modifier
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedMonth = monthNumber
                                    errorMessage = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthNames[index],
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (existingSheetChecker(selectedMonth, selectedYear)) {
                                errorMessage = "Sheet for this month already exists"
                            } else {
                                onConfirm(selectedMonth, selectedYear)
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}



@Composable
fun ExpenseSheetList(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sheets) { sheet ->
            ExpenseSheetCard(
                sheet = sheet,
                onClick = { onSheetClick(sheet) }
            )
        }
    }
}

@Composable
fun ExpenseSheetCard(
    sheet: ExpenseSheet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sheet.getDisplayName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Expenses: ${sheet.expenses.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Income: €${String.format("%.2f", sheet.income)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Balance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "€${String.format("%.2f", sheet.calculateBalance())}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sheet.calculateBalance() >= 0)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun ExpenseSheetDetailView(
    sheet: ExpenseSheet,
    onEditIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Income Card with Edit Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monthly Income",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "€${String.format("%.2f", sheet.income)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                IconButton(onClick = onEditIncome) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Income",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expenses Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Expenses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (sheet.expenses.isNotEmpty()) {
                    Text(
                        text = "Total: €${String.format("%.2f", sheet.expenses.sumOf { it.amount })}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
            }

            Button(
                onClick = onAddExpense,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Expenses LazyList
        if (sheet.expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No expenses recorded",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Click 'Add Expense' to start tracking",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sheet.expenses) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onEdit = { onEditExpense(expense) },
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
            }
        }

        // Balance Summary Section
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (sheet.calculateBalance() >= 0)
                    Color(0xFFE8F5E9)
                else
                    Color(0xFFFFEBEE)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (sheet.calculateBalance() >= 0)
                            "Remaining Balance"
                        else
                            "Amount Owed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (sheet.calculateBalance() >= 0)
                            Color(0xFF2E7D32)
                        else
                            Color(0xFFC62828)
                    )
                    Text(
                        text = if (sheet.calculateBalance() >= 0)
                            "You're within budget!"
                        else
                            "Over budget",
                        fontSize = 12.sp,
                        color = if (sheet.calculateBalance() >= 0)
                            Color(0xFF388E3C)
                        else
                            Color(0xFFD32F2F)
                    )
                }

                Text(
                    text = "€${String.format("%.2f", Math.abs(sheet.calculateBalance()))}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sheet.calculateBalance() >= 0)
                        Color(0xFF1B5E20)
                    else
                        Color(0xFFB71C1C)
                )
            }
        }
    }
}


@Composable
fun EditIncomeDialog(
    currentIncome: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var incomeText by remember { mutableStateOf(currentIncome.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Monthly Income",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = {
                        incomeText = it
                        errorMessage = ""
                    },
                    label = { Text("Income Amount") },
                    placeholder = { Text("Enter monthly income") },
                    leadingIcon = { Text("€", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val income = incomeText.toDoubleOrNull()
                            when {
                                income == null -> {
                                    errorMessage = "Please enter a valid number"
                                }
                                income < 0 -> {
                                    errorMessage = "Income cannot be negative"
                                }
                                else -> {
                                    onConfirm(income)
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var errorMessage by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food", "Transportation", "Shopping", "Entertainment",
        "Bills", "Healthcare", "Education", "Other"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add New Expense",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = ""
                    },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g., Grocery shopping") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorMessage = ""
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("€", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> {
                                    errorMessage = "Please enter a title"
                                }
                                amount.isBlank() -> {
                                    errorMessage = "Please enter an amount"
                                }
                                amount.toDoubleOrNull() == null -> {
                                    errorMessage = "Please enter a valid amount"
                                }
                                amount.toDouble() <= 0 -> {
                                    errorMessage = "Amount must be greater than 0"
                                }
                                else -> {
                                    val expense = Expense(
                                        title = title,
                                        amount = amount.toDouble(),
                                        category = category
                                    )
                                    onConfirm(expense)
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var category by remember { mutableStateOf(expense.category) }
    var errorMessage by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food", "Transportation", "Shopping", "Entertainment",
        "Bills", "Healthcare", "Education", "Other"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Expense",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = ""
                    },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g., Grocery shopping") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorMessage = ""
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> {
                                    errorMessage = "Please enter a title"
                                }
                                amount.isBlank() -> {
                                    errorMessage = "Please enter an amount"
                                }
                                amount.toDoubleOrNull() == null -> {
                                    errorMessage = "Please enter a valid amount"
                                }
                                amount.toDouble() <= 0 -> {
                                    errorMessage = "Amount must be greater than 0"
                                }
                                else -> {
                                    val updatedExpense = Expense(
                                        expenseId = java.util.UUID.randomUUID().toString(),
                                        title = title,
                                        amount = amount.toDouble(),
                                        category = category,
                                        dateAdded = System.currentTimeMillis()
                                    )
                                    onConfirm(updatedExpense)
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Delete Expense?")
        },
        text = {
            Column {
                Text("Are you sure you want to delete this expense?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${expense.title} - $${String.format("%.2f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "-$${String.format("%.2f", expense.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )

                // More options button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

