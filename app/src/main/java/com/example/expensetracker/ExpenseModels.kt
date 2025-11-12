package com.example.expensetracker

data class ExpenseSheet(
    val sheetId: String = java.util.UUID.randomUUID().toString(),
    val monthValue: Int,
    val yearValue: Int,
    val income: Double = 0.0,
    val expenses: List<Expense> = emptyList()
) {
    fun getDisplayName(): String {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${months[monthValue - 1]} $yearValue"
    }

    fun calculateBalance(): Double {
        val totalExpenses = expenses.sumOf { it.amount }
        return income - totalExpenses
    }
}

data class Expense(
    val expenseId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val dateAdded: Long = System.currentTimeMillis()
)
