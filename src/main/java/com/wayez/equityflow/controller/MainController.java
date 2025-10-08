package com.wayez.equityflow.controller;

import com.wayez.equityflow.db.DatabaseManager;
import com.wayez.equityflow.model.StockBalance;
import com.wayez.equityflow.model.Transaction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MainController class acts as the central hub of the application, handling user interactions and business logic.
 * It communicates between the UI (view) and the database (model) to perform CRUD operations and data fetching.
 */
public class MainController {

    // Logger for this class.
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    // --- Transaction Types ---
    private static final String TXN_BUY        = "BUY";
    private static final String TXN_SELL       = "SELL";
    private static final String TXN_CHARGES    = "CHARGES";
    private static final String TXN_ADD_FUNDS  = "ADD FUNDS";
    private static final String TXN_WITHDRAWAL = "WITHDRAWAL";
    private static final String TXN_REWARDS    = "REWARDS";
    private static final String TXN_CREDIT     = "CREDIT";
    private static final String TXN_DEBIT      = "DEBIT";

    // A list of valid transaction types for validation.
    private static final List<String> VALID_TXN_TYPES = Arrays.asList(
            TXN_BUY,
            TXN_SELL,
            TXN_CHARGES,
            TXN_ADD_FUNDS,
            TXN_WITHDRAWAL,
            TXN_REWARDS,
            TXN_CREDIT,
            TXN_DEBIT
    );

    // --- Date Formatters ---
    // A list of supported date formatters for parsing date strings.
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );
    // The database manager for handling database operations.
    private final DatabaseManager dbManager;
    // The table view for displaying transactions.
    private TableView<Transaction> transactionTable;
    // The observable list of transactions bound to the table view.
    private ObservableList<Transaction> transactionData;
    // The label for displaying the available balance.
    private Label balanceValueLabel;
    // A boolean property to track if the statement view is active.
    private final BooleanProperty isStatementView = new SimpleBooleanProperty(false);
    // A boolean property to track if the all balances view is active.
    private final BooleanProperty isAllBalancesView = new SimpleBooleanProperty(false);

    /**
     * Initializes the controller and the database manager.
     */
    public MainController() {
        this.dbManager = new DatabaseManager();
    }

    /**
     * Returns the boolean property that indicates if the statement view is active.
     *
     * @return The statement view boolean property.
     */
    public BooleanProperty isStatementViewProperty() {
        return isStatementView;
    }

    /**
     * Returns the boolean property that indicates if the all balances view is active.
     *
     * @return The all balances view boolean property.
     */
    public BooleanProperty isAllBalancesViewProperty() {
        return isAllBalancesView;
    }

    /**
     * Sets the TableView for displaying transactions.
     *
     * @param transactionTable The TableView from the UI.
     */
    public void setTransactionTable(TableView<Transaction> transactionTable) {
        this.transactionTable = transactionTable;
        this.transactionData = FXCollections.observableArrayList();
        this.transactionTable.setItems(transactionData);
    }

    /**
     * Sets the Label for displaying the available balance.
     *
     * @param balanceValueLabel The Label from the UI.
     */
    public void setBalanceValueLabel(Label balanceValueLabel) {
        this.balanceValueLabel = balanceValueLabel;
        updateBalance();
    }

    /**
     * Fetches and updates the available balance on the UI.
     */
    private void updateBalance() {
        try {
            // Get the available balance from the database.
            BigDecimal balance = dbManager.getAvailableBalance();
            // Format and set the balance on the label.
            balanceValueLabel.setText(formatInIndianNumberingSystem(balance));
        } catch (SQLException e) {
            // Show an error message if there is a database error.
            showError("Database error while fetching available balance: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching available balance", e);
        }
    }

    /**
     * Formats a BigDecimal value into the Indian currency format (e.g., ₹1,23,456.78).
     *
     * @param value The BigDecimal value to format.
     * @return The formatted currency string.
     */
    private String formatInIndianNumberingSystem(BigDecimal value) {
        // Return an empty string if the value is null.
        if (value == null) {
            return "";
        }
        // Create a NumberFormat instance for the Indian locale.
        NumberFormat formatter = NumberFormat.getNumberInstance(new java.util.Locale.Builder().setLanguage("en").setRegion("IN").build());
        // Set the maximum and minimum fraction digits to 2.
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        // Format the value and return the string.
        return formatter.format(value);
    }

    /**
     * Loads all transactions from the database and refreshes the transaction table.
     */
    public void loadAllTransactions() {
        try {
            // Get all transactions from the database.
            transactionData.setAll(dbManager.getAllTransactions());
            // Update the balance display.
            updateBalance();
            // Set the view to normal.
            setNormalView();
            // Set the view flags to false.
            isStatementView.set(false);
            isAllBalancesView.set(false);
        } catch (SQLException e) {
            // Show an error message if there is a database error.
            showError("Database error while loading data: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error loading all transactions", e);
        }
    }

    /**
     * Handles the "Add Transaction" action.
     * Validates input, creates a new transaction, adds it to the database, and refreshes the UI.
     */
    public void handleAddTransaction(TextField dateField, TextField typeField, TextField stockField, TextField qtyField, TextField rateField, TextField amountField) {
        try {
            // Create a new transaction from the form fields.
            Transaction newTransaction = createTransactionFromFields(dateField, typeField, stockField, qtyField, rateField, amountField);
            // Add the new transaction to the database.
            dbManager.addTransaction(newTransaction);
            // Reload all transactions to refresh the table.
            loadAllTransactions();
            // Set the table view to the normal mode.
            setNormalView();
            // Clear the input fields.
            clearInputFields(dateField, typeField, stockField, qtyField, rateField, amountField);
            // Show a success message.
        } catch (IllegalArgumentException | DateTimeParseException | SQLException e) {
            // Handle any errors that occur during the process.
            handleTransactionError(e);
        }
    }

    /**
     * Handles the "Delete Transaction" action.
     * Deletes the selected transaction from the database and refreshes the table.
     */
    public void handleDeleteTransaction() {
        // Get the selected transaction from the table.
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        // If no transaction is selected, show an error message.
        if (selectedTransaction == null) {
            showError("Please select a transaction to delete.");
            return;
        }
        try {
            // Delete the transaction from the database.
            dbManager.deleteTransaction(selectedTransaction.getId());
            // Reload all transactions to refresh the table.
            loadAllTransactions();
            // Show a success message.
            showSuccess("Transaction deleted successfully!");
        } catch (SQLException e) {
            // Handle any database errors.
            handleTransactionError(e);
        }
    }

    /**
     * Handles the "Modify Transaction" action.
     * Updates the selected transaction with the data from the input fields.
     */
    public void handleModifyTransaction(TextField dateField, TextField typeField, TextField stockField, TextField qtyField, TextField rateField, TextField amountField) {
        // Get the selected transaction from the table.
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        // If no transaction is selected, show an error message.
        if (selectedTransaction == null) {
            showError("Please select a transaction to modify.");
            return;
        }
        try {
            // Create a new transaction object from the form fields.
            Transaction updatedTransaction = createTransactionFromFields(dateField, typeField, stockField, qtyField, rateField, amountField);
            // Create a final transaction object with the ID of the selected transaction.
            Transaction finalTransaction = new Transaction(selectedTransaction.getId(), updatedTransaction.getTxnDate(), updatedTransaction.getTxnType(), updatedTransaction.getStockName(), updatedTransaction.getQty(), updatedTransaction.getRate(), updatedTransaction.getCredit(), updatedTransaction.getDebit());

            // Update the transaction in the database.
            dbManager.updateTransaction(finalTransaction);
            // Reload all transactions to refresh the table.
            loadAllTransactions();
            // Clear the input fields.
            clearInputFields(dateField, typeField, stockField, qtyField, rateField, amountField);
            // Show a success message.
            showSuccess("Transaction modified successfully!");
        } catch (IllegalArgumentException | DateTimeParseException | SQLException e) {
            // Handle any errors that occur during the process.
            handleTransactionError(e);
        }
    }

    /**
     * Creates a {@link Transaction} object from the user input fields.
     *
     * @return A new Transaction object.
     * @throws IllegalArgumentException if any input is invalid.
     */
    private Transaction createTransactionFromFields(TextField dateField, TextField typeField, TextField stockField, TextField qtyField, TextField rateField, TextField amountField) {
        // Validate and get the transaction type.
        String txnType = validateAndGetTransactionType(typeField.getText());
        // Parse the amount from the amount field.
        BigDecimal amount = parseBigDecimal(amountField.getText(), "Amount");
        // Calculate the credit and debit amounts based on the transaction type.
        BigDecimal[] creditDebit = calculateCreditAndDebit(txnType, amount);

        // Create and return a new Transaction object.
        return new Transaction(
                parseDate(dateField.getText()),
                txnType,
                java.util.Objects.requireNonNullElse(stockField.getText(), ""),
                parseBigDecimal(qtyField.getText(), "Quantity"),
                parseBigDecimal(rateField.getText(), "Rate"),
                creditDebit[0],  // Credit
                creditDebit[1]   // Debit
        );
    }

    /**
     * Validates and returns the uppercase transaction type.
     *
     * @param txnType The transaction type string.
     * @return The validated, uppercase transaction type.
     * @throws IllegalArgumentException if the transaction type is invalid.
     */
    private String validateAndGetTransactionType(String txnType) {
        // Check if the transaction type is null or empty.
        if (txnType == null || txnType.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        // Convert the transaction type to uppercase.
        String upperTxnType = txnType.trim().toUpperCase();
        // Check if the transaction type is valid.
        if (!VALID_TXN_TYPES.contains(upperTxnType)) {
            throw new IllegalArgumentException("Invalid transaction type. Please use one of: " + VALID_TXN_TYPES);
        }
        return upperTxnType;
    }

    /**
     * Calculates the credit and debit amounts based on the transaction type.
     *
     * @param txnType The transaction type.
     * @param amount  The transaction amount.
     * @return An array containing the credit and debit amounts.
     */
    private BigDecimal[] calculateCreditAndDebit(String txnType, BigDecimal amount) {
        BigDecimal credit = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;

        // Determine the credit and debit amounts based on the transaction type.
        switch (txnType) {
            case TXN_SELL:
            case TXN_ADD_FUNDS:
            case TXN_REWARDS:
            case TXN_CREDIT:
                credit = amount;
                break;
            case TXN_BUY:
            case TXN_WITHDRAWAL:
            case TXN_CHARGES:
            case TXN_DEBIT:
                debit = amount;
                break;
        }
        return new BigDecimal[]{credit, debit};
    }

    /**
     * Parses a date string into a {@link LocalDate} object using multiple supported formats.
     *
     * @param dateStr The date string to parse.
     * @return The parsed LocalDate object.
     * @throws DateTimeParseException if the date format is invalid.
     */
    private LocalDate parseDate(String dateStr) {
        // Try to parse the date string with each of the supported formatters.
        return DATE_FORMATTERS.stream()
                .map(formatter -> {
                    try {
                        return LocalDate.parse(dateStr, formatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new DateTimeParseException("Invalid date format for '" + dateStr + "'. Use formats like DD/MM/YYYY, D/M/YY, etc.", dateStr, 0));
    }

    /**
     * Parses a string into a {@link BigDecimal} object.
     *
     * @param valueStr The string to parse.
     * @param fieldName The name of the field being parsed, for error messages.
     * @return The parsed BigDecimal object.
     * @throws NumberFormatException if the string is not a valid number.
     */
    private BigDecimal parseBigDecimal(String valueStr, String fieldName) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Try to create a new BigDecimal from the string.
            return new BigDecimal(valueStr);
        } catch (NumberFormatException e) {
            // Throw a more informative exception if the format is invalid.
            throw new NumberFormatException("Invalid number format for " + fieldName + ": '" + valueStr + "'");
        }
    }

    /**
     * Clears the content of the given TextFields.
     */
    private void clearInputFields(TextField... fields) {
        // Iterate over the fields and clear them.
        for (TextField field : fields) {
            field.clear();
        }
    }

    /**
     * Handles exceptions that occur during transaction operations by showing an appropriate error message.
     *
     * @param e The exception to handle.
     */
    private void handleTransactionError(Exception e) {
        // Log the exception.
        LOGGER.log(Level.SEVERE, "Transaction error", e);
        // Show an error message based on the exception type.
        if (e instanceof NumberFormatException || e instanceof DateTimeParseException || e instanceof IllegalArgumentException) {
            showError(e.getMessage());
        } else if (e instanceof SQLException) {
            showError("Database error: " + e.getMessage());
        } else {
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Displays an error alert dialog.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a success information alert dialog.
     *
     * @param message The success message to display.
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the "View Statement" action.
     * Fetches and displays the transaction statement for a specific stock.
     *
     * @param stockName The name of the stock.
     */
    public void handleViewStatement(String stockName) {
        // Show an error if the stock name is empty.
        if (stockName == null || stockName.trim().isEmpty()) {
            showError("Please enter a stock name to view the statement.");
            return;
        }
        try {
            // Get the statement for the stock from the database.
            transactionData.setAll(dbManager.getStatementForStock(stockName.trim()));
            // Set the table view to the statement mode.
            setStatementView();
            // Set the view flags.
            isStatementView.set(true);
            isAllBalancesView.set(false);
        } catch (SQLException e) {
            // Handle any database errors.
            showError("Database error while fetching statement: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching statement for stock: " + stockName, e);
        }
    }

    /**
     * Handles the "View Other Statement" action.
     * Fetches and displays the transaction statement for a specific transaction type.
     *
     * @param transactionType The type of the transaction.
     */
    public void handleViewOtherStatement(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            showError("Please enter a transaction type to view the statement.");
            return;
        }
        try {
            transactionData.setAll(dbManager.getTransactionsByType(transactionType.trim().toUpperCase()));
            setOtherStatementView();
            isStatementView.set(true);
            isAllBalancesView.set(false);
        } catch (SQLException e) {
            showError("Database error while fetching statement: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching statement for transaction type: " + transactionType, e);
        }
    }

    /**
     * Handles the "View Balance" for a specific stock action.
     * Fetches and displays the consolidated balance for a specific stock.
     *
     * @param stockName The name of the stock.
     */
    public void handleViewBalanceForStock(String stockName) {
        // Show an error if the stock name is empty.
        if (stockName == null || stockName.trim().isEmpty()) {
            showError("Please enter a stock name to view the balance.");
            return;
        }
        try {
            // Get the balance for the stock from the database.
            Transaction balanceTransaction = dbManager.getBalanceForStock(stockName.trim());
            // If a balance is found, display it in the table.
            if (balanceTransaction != null) {
                transactionData.setAll(balanceTransaction);
                setStockBalanceView();
                isStatementView.set(false);
                isAllBalancesView.set(false);
            } else {
                // Show an error if no transactions are found for the stock.
                showError("No transactions found for stock: " + stockName.trim());
            }
        } catch (SQLException e) {
            // Handle any database errors.
            showError("Database error while fetching balance for stock: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching balance for stock: " + stockName, e);
        }
    }

    /**
     * Handles the "View Other Balance" action.
     * Fetches and displays the consolidated balance for a specific transaction type.
     *
     * @param transactionType The type of the transaction.
     */
    public void handleViewBalanceForTransactionType(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            showError("Please enter a transaction type to view the balance.");
            return;
        }
        try {
            Transaction balanceTransaction = dbManager.getBalanceForTransactionType(transactionType.trim().toUpperCase());
            if (balanceTransaction != null) {
                transactionData.setAll(balanceTransaction);
                setOtherBalanceView();
                isStatementView.set(false);
                isAllBalancesView.set(false);
            } else {
                showError("No transactions found for type: " + transactionType.trim());
            }
        } catch (SQLException e) {
            showError("Database error while fetching balance for transaction type: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching balance for transaction type: " + transactionType, e);
        }
    }

    /**
     * Handles the "View All Balances" action.
     * Fetches and displays the consolidated balances for all stocks.
     */
    public void handleViewAllBalances() {
        try {
            // Get all stock balances from the database.
            List<StockBalance> stockBalances = dbManager.getAllStockBalances();
            // Create a list of transactions from the stock balances.
            List<Transaction> transactions = new ArrayList<>();
            for (StockBalance sb : stockBalances) {
                transactions.add(new Transaction(sb.getStockName(), sb.getStockBalance(), sb.getBalance()));
            }
            // Sort the transactions by stock balance in descending order.
            transactions.sort(Comparator.comparing(Transaction::getStockBalance).reversed());
            // Set the transaction data in the table.
            transactionData.setAll(transactions);
            // Set the table view to the stock balance mode.
            setStockBalanceView();
            // Set the view flags.
            isStatementView.set(false);
            isAllBalancesView.set(true);
        } catch (SQLException e) {
            // Handle any database errors.
            showError("Database error while fetching all stock balances: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching all stock balances", e);
        }
    }

    /**
     * Handles the "Show All Transactions" action.
     * Loads all transactions and resets the table view to the normal mode.
     */
    /**
     * Handles the "View All Other Balances" action.
     * Fetches and displays the consolidated balances for all transaction types.
     */
    public void handleViewAllOtherBalances() {
        try {
            List<Transaction> balances = dbManager.getAllBalancesByTransactionType();
            transactionData.setAll(balances);
            setOtherBalanceView();
            isStatementView.set(false);
            isAllBalancesView.set(false);
        } catch (SQLException e) {
            showError("Database error while fetching all other balances: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error fetching all other balances", e);
        }
    }

    /**
     * Sets the visibility of table columns based on a list of column names.
     *
     * @param visibleColumns A list of column names that should be visible.
     */
    private void setColumnVisibility(List<String> visibleColumns) {
        // Iterate over the columns and set their visibility.
        transactionTable.getColumns().forEach(col -> col.setVisible(visibleColumns.contains(col.getText())));
    }

    /**
     * Configures the table to show stock balance view.
     */
    private void setStockBalanceView() {
        setColumnVisibility(Arrays.asList("Stock_Name", "Stock_Balance", "Balance"));
    }

    /**
     * Configures the table to show the normal transaction view.
     */
    private void setNormalView() {
        setColumnVisibility(Arrays.asList("ID", "Date", "Txn_Type", "Stock_Name", "Qty", "Rate", "Credit", "Debit"));
    }

    /**
     * Configures the table to show the statement view.
     */
    private void setStatementView() {
        setColumnVisibility(Arrays.asList("Date", "Txn_Type", "Stock_Name", "Qty", "Rate", "Credit", "Debit", "Stock_Balance", "Balance"));
    }

    /**
     * Configures the table to show the other statement view.
     */
    private void setOtherStatementView() {
        setColumnVisibility(Arrays.asList("Date", "Txn_Type", "Credit", "Debit", "Balance"));
    }

    /**
     * Configures the table to show the other balance view.
     */
    private void setOtherBalanceView() {
        setColumnVisibility(Arrays.asList("Txn_Type", "Balance"));
    }

    /**
     * Handles the "Delete All Transactions" action.
     * Shows a confirmation dialog and deletes all transactions if confirmed.
     */
    public void handleDeleteAllTransactions() {
        // Show a confirmation dialog.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete All Transactions");
        alert.setHeaderText("Are you sure you want to delete all transactions?");
        alert.setContentText("This action cannot be undone and is permanent.");

        // If the user confirms, delete all transactions.
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dbManager.deleteAllTransactions();
                loadAllTransactions();
                showSuccess("All transactions have been deleted.");
            } catch (SQLException e) {
                handleTransactionError(e);
            }
        }
    }

    /**
     * Handles the data export action.
     * Shows a file chooser to select the export path and triggers the database export.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleExportData(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Database");
        fileChooser.setInitialFileName("backup.sql");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    dbManager.exportDatabase(file.getAbsolutePath());
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    showSuccess("Database exported successfully to " + file.getAbsolutePath());
                }

                @Override
                protected void failed() {
                    super.failed();
                    showError("Database export failed: " + getException().getMessage());
                    LOGGER.log(Level.SEVERE, "Error exporting database", getException());
                }
            };

            new Thread(exportTask).start();
        }
    }

    /**
     * Handles the data import action.
     * Shows a file chooser to select the SQL file and triggers the database import.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleImportData(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null) {
            Task<Void> importTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    dbManager.importDatabase(file.getAbsolutePath());
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    loadAllTransactions();
                    setNormalView();
                    showSuccess("Database imported successfully from " + file.getAbsolutePath());
                }

                @Override
                protected void failed() {
                    super.failed();
                    showError("Database import failed: " + getException().getMessage());
                    LOGGER.log(Level.SEVERE, "Error importing database", getException());
                }
            };

            new Thread(importTask).start();
        }
    }

    /**
     * Handles the "Import CSV" action.
     * Shows a file chooser to select a CSV file, parses it, and imports transactions into the database.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleImportCsv(Stage ownerStage) {
        // Show a file chooser to select the CSV file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Transactions from CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(ownerStage);

        // If a file is selected, parse and import the transactions.
        if (file != null) {
            List<Transaction> transactionsToImport = new ArrayList<>();
            List<String> malformedLines = new ArrayList<>();
            int lineNumber = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                // Skip header row
                if ((line = reader.readLine()) != null) {
                    lineNumber++; // Increment for header
                }
                // Read each line of the CSV file.
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) continue; // Skip empty lines
                    try {
                        // Parse the CSV line into a Transaction object.
                        transactionsToImport.add(parseCsvLineToTransaction(line));
                    } catch (Exception e) {
                        // Log and store error for malformed lines.
                        LOGGER.log(Level.WARNING, "Skipping malformed CSV line " + lineNumber + ": " + line + " - " + e.getMessage());
                        malformedLines.add("Line " + lineNumber + ": " + e.getMessage());
                    }
                }
                // If there are transactions to import, add them to the database.
                if (!transactionsToImport.isEmpty()) {
                    dbManager.addTransactions(transactionsToImport); // New method in DatabaseManager
                    loadAllTransactions();
                    setNormalView();
                    showSuccess(transactionsToImport.size() + " transactions imported successfully from " + file.getName());
                } else {
                    showError("No valid transactions found in the CSV file.");
                }
            } catch (IOException e) {
                showError("Error reading CSV file: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error reading CSV file", e);
            } catch (SQLException e) {
                showError("Database error during CSV import: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Database error during CSV import", e);
            }

            if (!malformedLines.isEmpty()) {
                showError(malformedLines.size() + " lines in the CSV file were malformed. See application logs for details.");
                malformedLines.forEach(line -> LOGGER.log(Level.WARNING, "Malformed CSV line: " + line));
            }
        }
    }

    /**
     * Parses a single CSV line into an array of strings, handling quoted fields.
     *
     * @param csvLine The CSV line string.
     * @return An array of strings representing the columns.
     */
    private String[] parseCsvLine(String csvLine) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPart = new StringBuilder();
        for (char c : csvLine.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                // Don't append the quote to the part
            } else if (c == ',' && !inQuotes) {
                parts.add(currentPart.toString());
                currentPart = new StringBuilder();
            } else {
                currentPart.append(c);
            }
        }
        parts.add(currentPart.toString());
        return parts.toArray(new String[0]);
    }

    /**
     * Parses a single CSV line into a Transaction object.
     * Expected CSV format: Date,Txn_Type,Stock_Name,Qty,Rate,Credit,Debit
     *
     * @param csvLine The CSV line string.
     * @return A Transaction object.
     * @throws IllegalArgumentException if the CSV line is malformed or contains invalid data.
     */
    private Transaction parseCsvLineToTransaction(String csvLine) {
        // Split the CSV line by comma, handling quotes.
        String[] parts = parseCsvLine(csvLine);
        // Check if the line has the correct number of columns.
        if (parts.length != 7) { // Date,Txn_Type,Stock_Name,Qty,Rate,Credit,Debit
            throw new IllegalArgumentException("CSV line must have 7 columns: Date,Txn_Type,Stock_Name,Qty,Rate,Credit,Debit. Found " + parts.length + " columns.");
        }

        // Trim all parts to remove leading/trailing whitespace.
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        // Parse the data from the parts.
        LocalDate date = parseDate(parts[0]);
        String txnType = validateAndGetTransactionType(parts[1]);
        String stockName = parts[2];
        BigDecimal qty = parseBigDecimal(parts[3].replaceAll(",", ""), "Quantity");
        BigDecimal rate = parseBigDecimal(parts[4].replaceAll(",", ""), "Rate");
        BigDecimal credit = parseBigDecimal(parts[5].replaceAll(",", ""), "Credit"); // Parse Credit directly
        BigDecimal debit = parseBigDecimal(parts[6].replaceAll(",", ""), "Debit");   // Parse Debit directly

        // Create and return a new Transaction object.
        return new Transaction(date, txnType, stockName, qty, rate, credit, debit);
    }

    /**
     * Handles the "Export to CSV" action for the statement view.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleExportStatementToCsv(Stage ownerStage) {
        // If there is no data, show an error.
        if (transactionData.isEmpty()) {
            showError("There is no data to export.");
            return;
        }

        // Show a file chooser to select the export path.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Statement to CSV");
        fileChooser.setInitialFileName("statement.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(ownerStage);

        // If a file is selected, write the data to it.
        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Write the CSV header.
                writer.append("Date,Txn_Type,Stock_Name,Qty,Rate,Credit,Debit,Stock_Balance,Balance\n");

                // Write each transaction to a new line in the CSV file.
                for (Transaction transaction : transactionData) {
                    writer.append(transaction.toCsvString() + "\n");
                }

                // Show a success message.
                showSuccess("Statement exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                // Show an error message if there is an IO error.
                showError("Error exporting to CSV: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            }
        }
    }

    /**
     * Handles the "Export to CSV" action for the all balances view.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleExportAllBalancesToCsv(Stage ownerStage) {
        // If there is no data, show an error.
        if (transactionData.isEmpty()) {
            showError("There is no data to export.");
            return;
        }

        // Show a file chooser to select the export path.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Balances to CSV");
        fileChooser.setInitialFileName("balances.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(ownerStage);

        // If a file is selected, write the data to it.
        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Write the CSV header.
                writer.append("Stock_Name,Stock_Balance,Balance\n");

                // Write each transaction to a new line in the CSV file.
                for (Transaction transaction : transactionData) {
                    writer.append(transaction.toCsvStringForBalances() + "\n");
                }

                // Show a success message.
                showSuccess("Balances exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                // Show an error message if there is an IO error.
                showError("Error exporting to CSV: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            }
        }
    }

    /**
     * Handles the "Export to CSV" action for the other statement view.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleExportOtherStatementToCsv(Stage ownerStage) {
        // If there is no data, show an error.
        if (transactionData.isEmpty()) {
            showError("There is no data to export.");
            return;
        }

        // Show a file chooser to select the export path.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Other Statement to CSV");
        fileChooser.setInitialFileName("other_statement.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(ownerStage);

        // If a file is selected, write the data to it.
        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Write the CSV header.
                writer.append("Date,Txn_Type,Credit,Debit,Balance\n");

                // Write each transaction to a new line in the CSV file.
                for (Transaction transaction : transactionData) {
                    writer.append(transaction.toCsvStringForOtherStatement() + "\n");
                }

                // Show a success message.
                showSuccess("Statement exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                // Show an error message if there is an IO error.
                showError("Error exporting to CSV: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            }
        }
    }

    /**
     * Handles the "Export to CSV" action for the all other balances view.
     *
     * @param ownerStage The parent stage for the file chooser.
     */
    public void handleExportAllOtherBalancesToCsv(Stage ownerStage) {
        // If there is no data, show an error.
        if (transactionData.isEmpty()) {
            showError("There is no data to export.");
            return;
        }

        // Show a file chooser to select the export path.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Other Balances to CSV");
        fileChooser.setInitialFileName("other_balances.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(ownerStage);

        // If a file is selected, write the data to it.
        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Write the CSV header.
                writer.append("Txn_Type,Balance\n");

                // Write each transaction to a new line in the CSV file.
                for (Transaction transaction : transactionData) {
                    writer.append(transaction.toCsvStringForOtherBalances() + "\n");
                }

                // Show a success message.
                showSuccess("Balances exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                // Show an error message if there is an IO error.
                showError("Error exporting to CSV: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            }
        }
    }
}
