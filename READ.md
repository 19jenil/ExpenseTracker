# ExpenseTracker

A modern Android expense management application built using **Kotlin**, **Jetpack Compose**, and **SQLite**. The application helps users track income, manage expenses, monitor monthly spending, and visualize financial trends through interactive graphs.

## 📱 Overview

ExpenseTracker provides a simple and efficient way to manage personal finances. Users can create monthly expense sheets, record income and expenses, edit financial records, and analyze spending patterns using graphical reports.

The application follows modern Android development practices using Jetpack Compose for UI design and SQLite for persistent data storage.

---

## ✨ Features

### 📊 Expense Management

* Add new expenses
* Edit existing expenses
* Delete expenses
* Categorize spending records
* Track expenses by date

### 💰 Income Tracking

* Record monthly income
* Update income information
* Calculate remaining balance automatically

### 📅 Monthly Expense Sheets

* Create separate sheets for different months
* Manage yearly financial records
* Prevent duplicate monthly entries
* Organize expenses efficiently

### 📈 Financial Analytics

* Interactive income vs expense graphs
* Monthly trend visualization
* Swipe between financial periods
* Dynamic graph scaling

### 🗄️ Data Persistence

* SQLite database integration
* Secure local storage
* Automatic data retrieval
* Persistent financial records

### 🎨 Modern User Interface

* Built with Jetpack Compose
* Material Design 3 components
* Responsive layouts
* User-friendly dialogs and forms

---

## 🏗️ Architecture

The application follows a layered architecture:

UI Layer → ViewModel → Database Layer

### Main Components

#### MainActivity.kt

* Application entry point
* Navigation handling
* Dialog management
* Compose UI initialization

#### ExpenseViewModel.kt

* Business logic implementation
* State management
* Database communication
* Data synchronization

#### Database.kt

* SQLite database implementation
* CRUD operations
* Data persistence management

#### Graph.kt

* Income and expense visualization
* Financial trend analysis
* Interactive chart rendering

#### SharedComposables.kt

* Reusable UI components
* Expense cards
* Dialog components
* Sheet management interfaces

---

## 🗃️ Database Structure

### ExpenseSheet

Stores:

* Month
* Year
* Income
* Expense Collection

### Expense

Stores:

* Expense Name
* Amount
* Category
* Date
* Unique Identifier

---

## 🛠️ Technology Stack

* Kotlin
* Jetpack Compose
* SQLite Database
* Android Studio
* Material Design 3
* ViewModel Architecture

---

## 🚀 Installation

### Clone the Repository

```bash
git clone https://github.com/19jenil/ExpenseTracker.git
```

### Open in Android Studio

1. Open Android Studio
2. Select **Open Project**
3. Choose the cloned repository
4. Sync Gradle dependencies
5. Run on an emulator or Android device

---

## 📚 Learning Outcomes

This project demonstrates practical experience with:

* Android Application Development
* Kotlin Programming
* Jetpack Compose UI Development
* SQLite Database Design
* MVVM Architecture
* State Management
* Data Visualization
* Financial Data Processing

---

## 🔮 Future Improvements

* Budget Goal Tracking
* PDF Report Export
* Cloud Backup Integration
* Multi-Currency Support
* User Authentication
* Dark Mode Enhancements

---

## 👨‍💻 Author

**Jenil Patel**


GitHub: https://github.com/19jenil
