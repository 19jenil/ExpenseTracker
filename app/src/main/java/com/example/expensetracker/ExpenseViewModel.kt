package com.example.expensetracker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ExpenseViewModel : ViewModel() {

    // Use companion object to share data across all activities
    companion object {
        private val _sharedSheets = mutableStateListOf<ExpenseSheet>()

        fun getSharedSheets() = _sharedSheets
    }

    // Reference the shared list
    val expenseSheets = getSharedSheets()

    var showCreateSheetDialog = mutableStateOf(false)
        private set

    var showEditIncomeDialog = mutableStateOf(false)
        private set

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
        expenseSheets.add(newSheet)
        closeCreateSheetDialog()
    }

    fun checkIfSheetExists(monthValue: Int, yearValue: Int): Boolean {
        return expenseSheets.any {
            it.monthValue == monthValue && it.yearValue == yearValue
        }
    }

    fun openEditIncomeDialog() {
        showEditIncomeDialog.value = true
    }

    fun closeEditIncomeDialog() {
        showEditIncomeDialog.value = false
    }

    fun updateIncome(sheetId: String, newIncome: Double) {
        val index = expenseSheets.indexOfFirst { it.sheetId == sheetId }
        if (index != -1) {
            val oldSheet = expenseSheets[index]
            val updatedSheet = oldSheet.copy(income = newIncome)
            expenseSheets[index] = updatedSheet
        }
        closeEditIncomeDialog()
    }

    fun addExpense(sheetId: String, expense: Expense) {
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
}
