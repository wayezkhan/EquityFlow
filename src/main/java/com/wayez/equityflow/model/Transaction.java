package com.wayez.equityflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single, immutable financial transaction.
 * This class holds all information related to a transaction, such as date, type, stock name, quantity, rate, and credit/debit amounts.
 * It also includes calculated running balances for both cash and stock quantity.
 * All fields are final to ensure that transaction data is not modified after creation.
 */
public final class Transaction {

    /**
     * The unique identifier for the transaction in the database.
     */
    private final int        id;
    /**
     * The date on which the transaction occurred.
     */
    private final LocalDate  txnDate;
    /**
     * The type of the transaction (e.g., BUY, SELL, ADD_FUNDS).
     */
    private final String     txnType;
    /**
     * The name or symbol of the stock involved in the transaction.
     */
    private final String     stockName;
    /**
     * The quantity of the stock traded.
     */
    private final BigDecimal qty;
    /**
     * The rate at which the stock was traded.
     */
    private final BigDecimal rate;
    /**
     * The credit amount of the transaction (money in).
     */
    private final BigDecimal credit;
    /**
     * The debit amount of the transaction (money out).
     */
    private final BigDecimal debit;
    /**
     * The running cash balance after this transaction.
     */
    private final BigDecimal balance;
    /**
     * The running balance of the stock quantity after this transaction.
     */
    private final BigDecimal stockBalance;

    /**
     * Constructs a new Transaction with all fields.
     *
     * @param id           The unique ID from the database.
     * @param txnDate      The date of the transaction.
     * @param txnType      The type of the transaction.
     * @param stockName    The name of the stock.
     * @param qty          The quantity of the stock.
     * @param rate         The rate of the stock.
     * @param credit       The credit amount.
     * @param debit        The debit amount.
     * @param balance      The running cash balance.
     * @param stockBalance The running stock quantity balance.
     */
    public Transaction(int id, LocalDate txnDate, String txnType, String stockName, BigDecimal qty, BigDecimal rate, BigDecimal credit, BigDecimal debit, BigDecimal balance, BigDecimal stockBalance) {
        this.id           = id;
        this.txnDate      = txnDate;
        this.txnType      = txnType;
        this.stockName    = stockName;
        this.qty          = qty;
        this.rate         = rate;
        this.credit       = credit;
        this.debit        = debit;
        this.balance      = balance;
        this.stockBalance = stockBalance;
    }

    /**
     * Constructs a new Transaction with a database ID.
     *
     * @param id        The unique ID from the database.
     * @param txnDate   The date of the transaction. Must not be null.
     * @param txnType   The type of the transaction. Must not be null or empty.
     * @param stockName The name of the stock.
     * @param qty       The quantity of the stock.
     * @param rate      The rate of the stock.
     * @param credit    The credit amount.
     * @param debit     The debit amount.
     */
    public Transaction(int id, LocalDate txnDate, String txnType, String stockName, BigDecimal qty, BigDecimal rate, BigDecimal credit, BigDecimal debit) {
        this(id, txnDate, txnType, stockName, qty, rate, credit, debit, null, null);
    }

    /**
     * Constructs a new Transaction to be inserted into the database (without an ID).
     *
     * @param txnDate   The date of the transaction. Must not be null.
     * @param txnType   The type of the transaction. Must not be null or empty.
     * @param stockName The name of the stock.
     * @param qty       The quantity of the stock.
     * @param rate      The rate of the stock.
     * @param credit    The credit amount.
     * @param debit     The debit amount.
     */
    public Transaction(LocalDate txnDate, String txnType, String stockName, BigDecimal qty, BigDecimal rate, BigDecimal credit, BigDecimal debit) {
        this(0, txnDate, txnType, stockName, qty, rate, credit, debit, null, null);
    }

    /**
     * Constructs a new Transaction with running balances, typically for statement views.
     *
     * @param txnDate      The date of the transaction.
     * @param txnType      The type of the transaction.
     * @param stockName    The name of the stock.
     * @param qty          The quantity of the stock.
     * @param rate         The rate of the stock.
     * @param credit       The credit amount.
     * @param debit        The debit amount.
     * @param balance      The running cash balance.
     * @param stockBalance The running stock quantity balance.
     */
    public Transaction(LocalDate txnDate, String txnType, String stockName, BigDecimal qty, BigDecimal rate, BigDecimal credit, BigDecimal debit, BigDecimal balance, BigDecimal stockBalance) {
        this(0, txnDate, txnType, stockName, qty, rate, credit, debit, balance, stockBalance);
    }

    /**
     * Constructs a new Transaction representing the balance of a single stock.
     *
     * @param stockName    The name of the stock.
     * @param stockBalance The total quantity of the stock.
     * @param balance      The total cash value of the stock.
     */
    public Transaction(String stockName, BigDecimal stockBalance, BigDecimal balance) {
        this(0, null, null, stockName, null, null, null, null, balance, stockBalance);
    }

    /**
     * Constructs a new Transaction representing the balance of a single transaction type.
     *
     * @param txnType The type of the transaction.
     * @param balance The total cash value of the transaction type.
     */
    public Transaction(String txnType, BigDecimal balance) {
        this(0, null, txnType, null, null, null, null, null, balance, null);
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public LocalDate getTxnDate() {
        return txnDate;
    }

    public String getTxnType() {
        return txnType;
    }

    public String getStockName() {
        return stockName;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getStockBalance() {
        return stockBalance;
    }

    /**
     * Returns a CSV-formatted string representation of the transaction.
     *
     * @return A comma-separated string of the transaction's properties.
     */
    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.join(",",
                Objects.toString(getTxnDate()      != null ? getTxnDate().format(formatter) : ""),
                Objects.toString(getTxnType()      != null ? getTxnType()                   : ""),
                Objects.toString(getStockName()    != null ? getStockName()                 : ""),
                Objects.toString(getQty()          != null ? getQty().toString()            : "0"),
                Objects.toString(getRate()         != null ? getRate().toString()           : "0"),
                Objects.toString(getCredit()       != null ? getCredit().toString()         : "0"),
                Objects.toString(getDebit()        != null ? getDebit().toString()          : "0"),
                Objects.toString(getStockBalance() != null ? getStockBalance().toString()   : "0"),
                Objects.toString(getBalance()      != null ? getBalance().toString()        : "0")
        );
    }

    /**
     * Returns a CSV-formatted string representation of the transaction for the balances view.
     *
     * @return A comma-separated string of the transaction's properties for the balances view.
     */
    public String toCsvStringForBalances() {
        return String.join(",",
                Objects.toString(getStockName()    != null ? getStockName()                 : ""),
                Objects.toString(getStockBalance() != null ? getStockBalance().toString()   : "0"),
                Objects.toString(getBalance()      != null ? getBalance().toString()        : "0")
        );
    }

    /**
     * Returns a CSV-formatted string representation of the transaction for the other statement view.
     *
     * @return A comma-separated string of the transaction's properties for the other statement view.
     */
    public String toCsvStringForOtherStatement() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.join(",",
                Objects.toString(getTxnDate() != null ? getTxnDate().format(formatter) : ""),
                Objects.toString(getTxnType() != null ? getTxnType() : ""),
                Objects.toString(getCredit() != null ? getCredit().toString() : "0"),
                Objects.toString(getDebit() != null ? getDebit().toString() : "0"),
                Objects.toString(getBalance() != null ? getBalance().toString() : "0")
        );
    }

    /**
     * Returns a CSV-formatted string representation of the transaction for the other balances view.
     *
     * @return A comma-separated string of the transaction's properties for the other balances view.
     */
    public String toCsvStringForOtherBalances() {
        return String.join(",",
                Objects.toString(getTxnType() != null ? getTxnType() : ""),
                Objects.toString(getBalance() != null ? getBalance().toString() : "0")
        );
    }
}
