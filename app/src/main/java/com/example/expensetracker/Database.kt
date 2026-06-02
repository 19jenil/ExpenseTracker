package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "expense_tracker.db"
        private const val DATABASE_VERSION = 1


        private const val TABLE_SHEETS = "expense_sheets"
        private const val TABLE_EXPENSES = "expenses"


        private const val COLUMN_SHEET_ID = "sheet_id"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_INCOME = "income"


        private const val COLUMN_EXPENSE_ID = "expense_id"
        private const val COLUMN_EXPENSE_SHEET_ID = "expense_sheet_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE_ADDED = "date_added"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createSheetsTable = """
            CREATE TABLE $TABLE_SHEETS (
                $COLUMN_SHEET_ID TEXT PRIMARY KEY,
                $COLUMN_MONTH INTEGER NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_INCOME REAL DEFAULT 0.0
            )
        """.trimIndent()


        val createExpensesTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_EXPENSE_ID TEXT PRIMARY KEY,
                $COLUMN_EXPENSE_SHEET_ID TEXT NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE_ADDED INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_EXPENSE_SHEET_ID) 
                    REFERENCES $TABLE_SHEETS($COLUMN_SHEET_ID) 
                    ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createSheetsTable)
        db.execSQL(createExpensesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SHEETS")
        onCreate(db)
    }


    fun insertSheet(sheet: ExpenseSheet): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SHEET_ID, sheet.sheetId)
            put(COLUMN_MONTH, sheet.monthValue)
            put(COLUMN_YEAR, sheet.yearValue)
            put(COLUMN_INCOME, sheet.income)
        }
        return db.insert(TABLE_SHEETS, null, values)
    }


    fun insertExpense(sheetId: String, expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_ID, expense.expenseId)
            put(COLUMN_EXPENSE_SHEET_ID, sheetId)
            put(COLUMN_TITLE, expense.title)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_DATE_ADDED, expense.dateAdded)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }


    fun updateSheetIncome(sheetId: String, newIncome: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INCOME, newIncome)
        }
        return db.update(
            TABLE_SHEETS,
            values,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId)
        )
    }


    fun getAllSheets(): List<ExpenseSheet> {
        val sheets = mutableListOf<ExpenseSheet>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SHEETS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_YEAR ASC, $COLUMN_MONTH ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val sheetId = it.getString(it.getColumnIndexOrThrow(COLUMN_SHEET_ID))
                val month = it.getInt(it.getColumnIndexOrThrow(COLUMN_MONTH))
                val year = it.getInt(it.getColumnIndexOrThrow(COLUMN_YEAR))
                val income = it.getDouble(it.getColumnIndexOrThrow(COLUMN_INCOME))

                // Get expenses for this sheet
                val expenses = getExpensesForSheet(sheetId)

                sheets.add(
                    ExpenseSheet(
                        sheetId = sheetId,
                        monthValue = month,
                        yearValue = year,
                        income = income,
                        expenses = expenses
                    )
                )
            }
        }

        return sheets
    }


    fun getExpensesForSheet(sheetId: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_SHEET_ID = ?",
            arrayOf(sheetId),
            null,
            null,
            "$COLUMN_DATE_ADDED DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val expenseId = it.getString(it.getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val dateAdded = it.getLong(it.getColumnIndexOrThrow(COLUMN_DATE_ADDED))

                expenses.add(
                    Expense(
                        expenseId = expenseId,
                        title = title,
                        amount = amount,
                        category = category,
                        dateAdded = dateAdded
                    )
                )
            }
        }

        return expenses
    }


    fun deleteSheet(sheetId: String): Int {
        val db = writableDatabase
        // Delete expenses first
        db.delete(TABLE_EXPENSES, "$COLUMN_EXPENSE_SHEET_ID = ?", arrayOf(sheetId))
        // Then delete the sheet
        return db.delete(TABLE_SHEETS, "$COLUMN_SHEET_ID = ?", arrayOf(sheetId))
    }


    fun deleteExpense(expenseId: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_EXPENSES, "$COLUMN_EXPENSE_ID = ?", arrayOf(expenseId))
    }


    fun sheetExists(month: Int, year: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SHEETS,
            arrayOf(COLUMN_SHEET_ID),
            "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
            arrayOf(month.toString(), year.toString()),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }


    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_EXPENSES")
        db.execSQL("DELETE FROM $TABLE_SHEETS")
    }
}
