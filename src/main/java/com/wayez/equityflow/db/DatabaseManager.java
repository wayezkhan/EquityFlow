package com.wayez.equityflow.db;

import com.wayez.equityflow.model.StockBalance;
import com.wayez.equityflow.model.Transaction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all database operations for the trading application.
 * This class handles the connection to the database and provides methods for CRUD operations on transactions,
 * as well as for fetching statements and balances.
 */
public class DatabaseManager {

    // Logger for this class.
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    // --- Database Column Names ---
    private static final String COL_ID            = "id";
    private static final String COL_TXN_DATE      = "txn_date";
    private static final String COL_TXN_TYPE      = "txn_type";
    private static final String COL_STOCK_NAME    = "stock_name";
    private static final String COL_QTY           = "qty";
    private static final String COL_RATE          = "rate";
    private static final String COL_CREDIT        = "credit";
    private static final String COL_DEBIT         = "debit";
    private static final String COL_BALANCE       = "balance";
    private static final String COL_STOCK_BALANCE = "stock_balance";

    // Properties for the database connection.
    private final Properties properties = new Properties();

    /**
     * Initializes the DatabaseManager by loading database connection properties.
     */
    public DatabaseManager() {
        loadProperties();
    }

    /**
     * Loads database connection properties from the 'database.properties' file.
     */
    private void loadProperties() {
        // Try to load the properties file from the classpath.
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            // If the file is not found, log an error.
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Unable to find database.properties file.");
                return;
            }
            // Load the properties from the file.
            properties.load(input);
        } catch (Exception ex) {
            // Log any errors that occur during loading.
            LOGGER.log(Level.SEVERE, "Error loading database properties", ex);
        }
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A new {@link Connection} object.
     * @throws SQLException if a database access error occurs.
     */
    private Connection getConnection() throws SQLException {
        // Get a connection to the database using the loaded properties.
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    /**
     * Adds a new transaction to the database.
     *
     * @param transaction The {@link Transaction} to add.
     * @throws SQLException if a database access error occurs.
     */
    public void addTransaction(Transaction transaction) throws SQLException {
        // SQL query to insert a new transaction.
        String sql = "INSERT INTO demat_transactions (txn_date, txn_type, stock_name, qty, rate, credit, debit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the parameters for the prepared statement.
            pstmt.setDate(1, Date.valueOf(transaction.getTxnDate()));
            pstmt.setString(2, transaction.getTxnType());
            pstmt.setString(3, transaction.getStockName());
            pstmt.setBigDecimal(4, transaction.getQty());
            pstmt.setBigDecimal(5, transaction.getRate());
            pstmt.setBigDecimal(6, transaction.getCredit());
            pstmt.setBigDecimal(7, transaction.getDebit());
            // Execute the update.
            pstmt.executeUpdate();
        }
    }

    /**
     * Adds a list of transactions to the database using batch insertion.
     *
     * @param transactions The list of {@link Transaction} objects to add.
     * @throws SQLException if a database access error occurs.
     */
    public void addTransactions(List<Transaction> transactions) throws SQLException {
        // SQL query to insert a new transaction.
        String sql = "INSERT INTO demat_transactions (txn_date, txn_type, stock_name, qty, rate, credit, debit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Iterate over the transactions and add them to the batch.
            for (Transaction transaction : transactions) {
                pstmt.setDate(1, Date.valueOf(transaction.getTxnDate()));
                pstmt.setString(2, transaction.getTxnType());
                pstmt.setString(3, transaction.getStockName());
                pstmt.setBigDecimal(4, transaction.getQty());
                pstmt.setBigDecimal(5, transaction.getRate());
                pstmt.setBigDecimal(6, transaction.getCredit());
                pstmt.setBigDecimal(7, transaction.getDebit());
                pstmt.addBatch();
            }
            // Execute the batch.
            pstmt.executeBatch();
        }
    }

    /**
     * Deletes a transaction from the database by its ID.
     *
     * @param id The ID of the transaction to delete.
     * @throws SQLException if a database access error occurs.
     */
    public void deleteTransaction(int id) throws SQLException {
        // SQL query to delete a transaction by ID.
        String sql = "DELETE FROM demat_transactions WHERE id = ?";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the ID parameter.
            pstmt.setInt(1, id);
            // Execute the update.
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing transaction in the database.
     *
     * @param transaction The {@link Transaction} with updated data.
     * @throws SQLException if a database access error occurs.
     */
    public void updateTransaction(Transaction transaction) throws SQLException {
        // SQL query to update a transaction.
        String sql = "UPDATE demat_transactions SET txn_date = ?, txn_type = ?, stock_name = ?, qty = ?, rate = ?, credit = ?, debit = ? WHERE id = ?";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the parameters for the prepared statement.
            pstmt.setDate(1, Date.valueOf(transaction.getTxnDate()));
            pstmt.setString(2, transaction.getTxnType());
            pstmt.setString(3, transaction.getStockName());
            pstmt.setBigDecimal(4, transaction.getQty());
            pstmt.setBigDecimal(5, transaction.getRate());
            pstmt.setBigDecimal(6, transaction.getCredit());
            pstmt.setBigDecimal(7, transaction.getDebit());
            pstmt.setInt(8, transaction.getId());
            // Execute the update.
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all transactions from the database, sorted by ID in descending order.
     *
     * @return A list of all {@link Transaction} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> getAllTransactions() throws SQLException {
        // Create a list to hold the transactions.
        List<Transaction> transactions = new ArrayList<>();
        // SQL query to get all transactions.
        String sql = "SELECT * FROM demat_transactions ORDER BY id DESC";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Iterate over the result set and create a Transaction object for each row.
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt(COL_ID),
                        rs.getDate(COL_TXN_DATE).toLocalDate(),
                        rs.getString(COL_TXN_TYPE),
                        rs.getString(COL_STOCK_NAME),
                        rs.getBigDecimal(COL_QTY),
                        rs.getBigDecimal(COL_RATE),
                        rs.getBigDecimal(COL_CREDIT),
                        rs.getBigDecimal(COL_DEBIT)
                ));
            }
        }
        return transactions;
    }

    /**
     * Retrieves a statement for a specific stock, including running cash and stock balances.
     *
     * @param stockName The name of the stock.
     * @return A list of {@link Transaction} objects for the specified stock.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> getStatementForStock(String stockName) throws SQLException {
        // Create a list to hold the transactions.
        List<Transaction> transactions = new ArrayList<>();
        // SQL query to get the statement for a stock.
        String sql = "SELECT txn_date, txn_type, stock_name, qty, rate, credit, debit, " +
                "SUM(CASE WHEN txn_type = 'SELL' THEN -qty ELSE qty END) OVER (ORDER BY txn_date, id) as stock_balance, " +
                "SUM(credit - debit) OVER (ORDER BY txn_date, id) as balance " +
                "FROM demat_transactions WHERE stock_name = ? ORDER BY txn_date, id";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the stock name parameter.
            pstmt.setString(1, stockName);
            // Execute the query.
            try (ResultSet rs = pstmt.executeQuery()) {
                // Iterate over the result set and create a Transaction object for each row.
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getDate(COL_TXN_DATE).toLocalDate(),
                            rs.getString(COL_TXN_TYPE),
                            rs.getString(COL_STOCK_NAME),
                            rs.getBigDecimal(COL_QTY),
                            rs.getBigDecimal(COL_RATE),
                            rs.getBigDecimal(COL_CREDIT),
                            rs.getBigDecimal(COL_DEBIT),
                            rs.getBigDecimal(COL_BALANCE),
                            rs.getBigDecimal(COL_STOCK_BALANCE)
                    ));
                }
            }
        }
        return transactions;
    }

    /**
     * Calculates and retrieves the total available cash balance from all transactions.
     *
     * @return The available balance as a {@link BigDecimal}.
     * @throws SQLException if a database access error occurs.
     */
    public BigDecimal getAvailableBalance() throws SQLException {
        // SQL query to get the available balance.
        String sql = "SELECT SUM(credit) - SUM(debit) AS balance FROM demat_transactions";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // If there is a result, return the balance.
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal(COL_BALANCE);
                return balance == null ? BigDecimal.ZERO : balance;
            }
        }
        // Return zero if there are no transactions.
        return BigDecimal.ZERO;
    }

    /**
     * Deletes all transactions from the database. This is a permanent action.
     *
     * @throws SQLException if a database access error occurs.
     */
    public void deleteAllTransactions() throws SQLException {
        String deleteSql = "DELETE FROM demat_transactions";
        String resetSql = "ALTER TABLE demat_transactions AUTO_INCREMENT = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(deleteSql);
            stmt.executeUpdate(resetSql);
        }
    }

    /**
     * Retrieves the consolidated balance for a specific stock.
     *
     * @param stockName The name of the stock.
     * @return A {@link Transaction} object containing the stock name, total stock quantity, and total cash balance.
     * @throws SQLException if a database access error occurs.
     */
    public Transaction getBalanceForStock(String stockName) throws SQLException {
        // SQL query to get the balance for a stock.
        String sql = "SELECT stock_name, SUM(CASE WHEN txn_type = 'SELL' THEN -qty ELSE qty END) as stock_balance, " +
                "SUM(credit) - SUM(debit) AS balance FROM demat_transactions WHERE stock_name = ? GROUP BY stock_name";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the stock name parameter.
            pstmt.setString(1, stockName);
            // Execute the query.
            try (ResultSet rs = pstmt.executeQuery()) {
                // If there is a result, create and return a Transaction object.
                if (rs.next()) {
                    BigDecimal stockBalance = rs.getBigDecimal(COL_STOCK_BALANCE);
                    BigDecimal balance = rs.getBigDecimal(COL_BALANCE);
                    return new Transaction(
                            rs.getString(COL_STOCK_NAME),
                            stockBalance == null ? BigDecimal.ZERO : stockBalance,
                            balance == null ? BigDecimal.ZERO : balance
                    );
                }
            }
        }
        // Return null if no balance is found.
        return null;
    }

    /**
     * Retrieves the consolidated balances for all stocks.
     *
     * @return A list of {@link StockBalance} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<StockBalance> getAllStockBalances() throws SQLException {
        // Create a list to hold the stock balances.
        List<StockBalance> stockBalances = new ArrayList<>();
        // SQL query to get all stock balances.
        String sql = "SELECT stock_name, SUM(CASE WHEN txn_type = 'SELL' THEN -qty ELSE qty END) as stock_balance, " +
                "SUM(credit) - SUM(debit) AS balance FROM demat_transactions WHERE txn_type IN ('BUY', 'SELL') GROUP BY stock_name";
        // Use a try-with-resources statement to ensure the connection and statement are closed.
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Iterate over the result set and create a StockBalance object for each row.
            while (rs.next()) {
                stockBalances.add(new StockBalance(
                        rs.getString(COL_STOCK_NAME),
                        rs.getBigDecimal(COL_STOCK_BALANCE),
                        rs.getBigDecimal(COL_BALANCE)
                ));
            }
        }
        return stockBalances;
    }

    /**
     * Exports the entire database to a SQL script file using JDBC.
     * This method is platform-independent and more robust than relying on external command-line tools.
     * It exports the schema (CREATE TABLE statements) and data (INSERT statements) for all tables.
     *
     * @param filePath The absolute path of the file to export to.
     * @throws SQLException if a database access error occurs.
     * @throws IOException if an I/O error occurs during file writing.
     */
    public void exportDatabase(String filePath) throws SQLException, IOException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             FileWriter writer = new FileWriter(filePath)) {

            String tableName = "demat_transactions";

            // 1. Export table schema (CREATE TABLE statement)
            writer.append("-- Schema for table: ").append(tableName).append("\n");
            writer.append("DROP TABLE IF EXISTS ").append(tableName).append(";\n");
            writer.append("CREATE TABLE demat_transactions (\n")
                  .append("    id INT AUTO_INCREMENT PRIMARY KEY,\n")
                  .append("    txn_date DATE NOT NULL,\n")
                  .append("    txn_type VARCHAR(50) NOT NULL,\n")
                  .append("    stock_name VARCHAR(255) NOT NULL,\n")
                  .append("    qty DECIMAL(10, 2) NOT NULL,\n")
                  .append("    rate DECIMAL(10, 2) NOT NULL,\n")
                  .append("    credit DECIMAL(10, 2) NOT NULL,\n")
                  .append("    debit DECIMAL(10, 2) NOT NULL\n")
                  .append(");\n");
            writer.append("\n");

            // 2. Export table data (INSERT statements)
            ResultSet rsData = stmt.executeQuery("SELECT * FROM " + tableName);
            java.sql.ResultSetMetaData metaData = rsData.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rsData.next()) {
                StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rsData.getObject(i);
                    if (value == null) {
                        insertSql.append("NULL");
                    } else if (value instanceof String || value instanceof Date) {
                        insertSql.append("'").append(value.toString().replace("'", "''")).append("'");
                    } else {
                        insertSql.append(value);
                    }
                    if (i < columnCount) {
                        insertSql.append(", ");
                    }
                }
                insertSql.append(");\n");
                writer.append(insertSql.toString());
            }
            writer.append("\n");
        }
    }

    /**
     * Imports data from a SQL script file into the database using JDBC.
     * This method reads the SQL file, parses it into individual statements, and executes them.
     * It handles comments and ensures proper transaction management.
     *
     * @param filePath The absolute path of the file to import from.
     * @throws SQLException if a database access error occurs during SQL execution.
     * @throws IOException if an I/O error occurs during file reading.
     */
    public void importDatabase(String filePath) throws SQLException, IOException {
        StringBuilder sqlStatement = new StringBuilder();
        try (Connection conn = getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            conn.setAutoCommit(false); // Start transaction

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                sqlStatement.append(line);
                if (line.endsWith(";")) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sqlStatement.toString());
                    }
                    sqlStatement = new StringBuilder(); // Reset for next statement
                } else {
                    sqlStatement.append(" "); // Add space for multi-line statements
                }
            }
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error importing database, attempting rollback", e);
            try (Connection conn = getConnection()) {
                conn.rollback(); // Rollback on error
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Error during rollback", rollbackEx);
            }
            throw e; // Re-throw the original exception
        } finally {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error restoring auto-commit", e);
            }
        }
    }


    /**
     * Retrieves all transactions of a specific type from the database, sorted by date.
     *
     * @param transactionType The type of the transaction to retrieve.
     * @return A list of all {@link Transaction} objects of the specified type.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> getTransactionsByType(String transactionType) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT txn_date, txn_type, credit, debit, " +
                "SUM(credit - debit) OVER (ORDER BY txn_date, id) as balance " +
                "FROM demat_transactions WHERE txn_type = ? ORDER BY txn_date, id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getDate(COL_TXN_DATE).toLocalDate(),
                            rs.getString(COL_TXN_TYPE),
                            null,
                            null,
                            null,
                            rs.getBigDecimal(COL_CREDIT),
                            rs.getBigDecimal(COL_DEBIT),
                            rs.getBigDecimal(COL_BALANCE),
                            null
                    ));
                }
            }
        }
        return transactions;
    }

    /**
     * Retrieves the consolidated balance for a specific transaction type.
     *
     * @param transactionType The type of the transaction.
     * @return A {@link Transaction} object containing the transaction type and total cash balance.
     * @throws SQLException if a database access error occurs.
     */
    public Transaction getBalanceForTransactionType(String transactionType) throws SQLException {
        String sql = "SELECT txn_type, SUM(credit) - SUM(debit) AS balance FROM demat_transactions WHERE txn_type = ? GROUP BY txn_type";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal(COL_BALANCE);
                    return new Transaction(
                            null,
                            rs.getString(COL_TXN_TYPE),
                            null,
                            null,
                            null,
                            null,
                            null,
                            balance == null ? BigDecimal.ZERO : balance,
                            null
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the consolidated balances for all transaction types.
     *
     * @return A list of {@link Transaction} objects, each representing the balance for a transaction type.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> getAllBalancesByTransactionType() throws SQLException {
        List<Transaction> balances = new ArrayList<>();
        String sql = "SELECT txn_type, SUM(credit) - SUM(debit) AS balance FROM demat_transactions GROUP BY txn_type";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BigDecimal balance = rs.getBigDecimal(COL_BALANCE);
                balances.add(new Transaction(
                        rs.getString(COL_TXN_TYPE),
                        balance == null ? BigDecimal.ZERO : balance
                ));
            }
        }
        return balances;
    }
}
