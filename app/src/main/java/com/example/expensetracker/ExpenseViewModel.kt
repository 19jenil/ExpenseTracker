package com.example.expensetracker

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider


class ExpenseViewModel(private val context: Context) : ViewModel() {

    private val dbHelper = ExpenseDatabaseHelper(context)

    val expenseSheets = mutableStateListOf<ExpenseSheet>()

    var showCreateSheetDialog = mutableStateOf(false)
        private set

    var showEditIncomeDialog = mutableStateOf(false)
        private set

    init {
        // Load data from database on initialization
        loadSheetsFromDatabase()
    }

    var showEditExpenseDialog = mutableStateOf(false)
        private set

    var editingExpense = mutableStateOf<Pair<String, Expense>?>(null)
        private set

    fun openEditExpenseDialog(sheetId: String, expense: Expense) {
        editingExpense.value = Pair(sheetId, expense)
        showEditExpenseDialog.value = true
    }

    fun closeEditExpenseDialog() {
        showEditExpenseDialog.value = false
        editingExpense.value = null
    }

    fun updateExpense(sheetId: String, oldExpenseId: String, updatedExpense: Expense) {
        // Delete old expense from database
        dbHelper.deleteExpense(oldExpenseId)

        // Insert updated expense to database
        dbHelper.insertExpense(sheetId, updatedExpense)

        // Update in memory
        val index = expenseSheets.indexOfFirst { it.sheetId == sheetId }
        if (index != -1) {
            val oldSheet = expenseSheets[index]
            val updatedExpenses = oldSheet.expenses
                .filter { it.expenseId != oldExpenseId }
                .plus(updatedExpense)
            val updatedSheet = oldSheet.copy(expenses = updatedExpenses)
            expenseSheets[index] = updatedSheet
        }

        closeEditExpenseDialog()
    }

    fun deleteExpense(sheetId: String, expenseId: String) {
        // Delete from database
        dbHelper.deleteExpense(expenseId)

        // Update in memory
        val index = expenseSheets.indexOfFirst { it.sheetId == sheetId }
        if (index != -1) {
            val oldSheet = expenseSheets[index]
            val updatedExpenses = oldSheet.expenses.filter { it.expenseId != expenseId }
            val updatedSheet = oldSheet.copy(expenses = updatedExpenses)
            expenseSheets[index] = updatedSheet
        }
    }

    private fun loadSheetsFromDatabase() {
        expenseSheets.clear()
        expenseSheets.addAll(dbHelper.getAllSheets())
    }

    fun openCreateSheetDialog() {
        showCreateSheetDialog.value = true
    }

    fun closeCreateSheetDialog() {
        showCreateSheetDialog.value = false
    }



    fun createNewSheet(monthValue: Int, yearValue: Int) {
        val newSheet = ExpenseSheet(
            monthValue = monthValue,
            yearValue = yearValue
        )

        // Save to database
        dbHelper.insertSheet(newSheet)

        // Update UI list
        expenseSheets.add(newSheet)
        closeCreateSheetDialog()
    }

    fun checkIfSheetExists(monthValue: Int, yearValue: Int): Boolean {
        return dbHelper.sheetExists(monthValue, yearValue)
    }

    fun openEditIncomeDialog() {
        showEditIncomeDialog.value = true
    }

    fun closeEditIncomeDialog() {
        showEditIncomeDialog.value = false
    }

    fun updateIncome(sheetId: String, newIncome: Double) {
        // Update in database
        dbHelper.updateSheetIncome(sheetId, newIncome)

        // Update in memory
        val index = expenseSheets.indexOfFirst { it.sheetId == sheetId }
        if (index != -1) {
            val oldSheet = expenseSheets[index]
            val updatedSheet = oldSheet.copy(income = newIncome)
            expenseSheets[index] = updatedSheet
        }
        closeEditIncomeDialog()
    }

    fun addExpense(sheetId: String, expense: Expense) {
        // Save expense to database
        dbHelper.insertExpense(sheetId, expense)

        // Update in memory
        val index = expenseSheets.indexOfFirst { it.sheetId == sheetId }
        if (index != -1) {
            val oldSheet = expenseSheets[index]
            val updatedExpenses = oldSheet.expenses + expense
            val updatedSheet = oldSheet.copy(expenses = updatedExpenses)
            expenseSheets[index] = updatedSheet
        }
    }

    fun getSheetById(sheetId: String): ExpenseSheet? {
        return expenseSheets.find { it.sheetId == sheetId }
    }

    override fun onCleared() {
        super.onCleared()
        dbHelper.close()
    }
}


class ExpenseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
