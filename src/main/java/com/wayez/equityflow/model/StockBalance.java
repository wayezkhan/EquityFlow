package com.wayez.equityflow.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the balance of a single stock, including both quantity and cash value.
 * This class is immutable, ensuring that balance data is not accidentally modified.
 */
public final class StockBalance {

    /**
     * The name or symbol of the stock.
     */
    private final String     stockName;
    /**
     * The total cash value or balance of the stock.
     */
    private final BigDecimal balance;
    /**
     * The total quantity or number of shares of the stock.
     */
    private final BigDecimal stockBalance;

    /**
     * Constructs a new StockBalance.
     *
     * @param stockName    The name of the stock. Must not be null.
     * @param stockBalance The total quantity of the stock.
     * @param balance      The total cash value of the stock.
     */
    public StockBalance(String stockName, BigDecimal stockBalance, BigDecimal balance) {
        this.stockName    = Objects.requireNonNull(stockName, "Stock name cannot be null.");
        this.stockBalance = stockBalance;
        this.balance      = balance;
    }

    // --- Getters ---

    /**
     * Returns the name of the stock.
     *
     * @return The stock name.
     */
    public String getStockName() {
        return stockName;
    }

    /**
     * Returns the total cash value of the stock.
     *
     * @return The balance.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Returns the total quantity of the stock.
     *
     * @return The stock balance.
     */
    public BigDecimal getStockBalance() {
        return stockBalance;
    }
}