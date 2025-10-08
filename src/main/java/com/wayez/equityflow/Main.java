package com.wayez.equityflow;

import com.wayez.equityflow.controller.MainController;
import com.wayez.equityflow.model.Transaction;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The Main class is the entry point of the trading application.
 * It sets up the user interface (UI) and initializes the main controller.
 */
public class Main extends Application {

    // The controller that handles the application's logic.
    private final MainController controller = new MainController();
    // Date formatter for displaying dates in the table.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // UI Components
    private Label balanceValueLabel;
    private TableView<Transaction> transactionTable;
    private TextField dateField;
    private TextField typeField;
    private TextField stockField;
    private TextField qtyField;
    private TextField rateField;
    private TextField amountField;

    // Table Columns
    private TableColumn<Transaction, Integer> idCol;
    private TableColumn<Transaction, LocalDate> dateCol;
    private TableColumn<Transaction, String> typeCol;
    private TableColumn<Transaction, String> stockCol;
    private TableColumn<Transaction, BigDecimal> qtyCol;
    private TableColumn<Transaction, BigDecimal> rateCol;
    private TableColumn<Transaction, BigDecimal> creditCol;
    private TableColumn<Transaction, BigDecimal> debitCol;
    private TableColumn<Transaction, BigDecimal> balanceCol;
    private TableColumn<Transaction, BigDecimal> stockBalanceCol;


    /**
     * The main method, which launches the JavaFX application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method is called when the application is launched.
     * It sets up the primary stage and the main scene.
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        initializePrimaryStage(primaryStage);
        Scene scene = setupUI(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initializes the primary stage with the title.
     *
     * @param primaryStage The primary stage for this application.
     */
    private void initializePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("EquityFlow");
    }

    /**
     * Sets up the user interface components and returns the main scene.
     *
     * @param primaryStage The primary stage for this application.
     * @return The main scene of the application.
     */
    private Scene setupUI(Stage primaryStage) {
        BorderPane root = createRootPane();
        VBox navPanel = createNavigationPanel();

        // Create a VBox to hold the transaction table and the dynamic content pane
        VBox mainContentArea = new VBox(10); // Add spacing between table and content pane
        mainContentArea.setPadding(new Insets(10)); // Add padding around the main content area
        mainContentArea.getStyleClass().add("main-content-area");

        transactionTable = createTransactionTable();
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        mainContentArea.getChildren().add(transactionTable);

        StackPane dynamicContentPane = createDynamicContentPane();
        mainContentArea.getChildren().add(dynamicContentPane);

        root.setLeft(navPanel);
        root.setCenter(mainContentArea);

        GridPane dashboardPane = createDashboardPane(primaryStage);
        GridPane dataEntryPane = createInputForm(primaryStage);
        VBox fetchResultsPane = createFetchResultsPane(primaryStage);

        dynamicContentPane.getChildren().add(dashboardPane); // Add initial dashboard to dynamic content

        // Set up the navigation logic to switch between the different panes.
        setupNavigation(navPanel, dynamicContentPane, dashboardPane, dataEntryPane, fetchResultsPane);

        controller.setTransactionTable(transactionTable);
        controller.setBalanceValueLabel(balanceValueLabel);

        setEmptyTableState();

        transactionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            } else {
                clearForm();
            }
        });

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        return scene;
    }

    /**
     * Creates the root BorderPane for the application.
     *
     * @return The configured BorderPane.
     */
    private BorderPane createRootPane() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        return root;
    }

    /**
     * Creates the StackPane that will hold dynamically changing content (dashboard, data entry, etc.).
     *
     * @return The configured StackPane.
     */
    private StackPane createDynamicContentPane() {
        StackPane dynamicContentPane = new StackPane();
        dynamicContentPane.getStyleClass().add("content-pane");
        return dynamicContentPane;
    }

    /**
     * Sets up the navigation logic to switch between the different panes.
     *
     * @param navPanel           The navigation panel containing the buttons.
     * @param dynamicContentPane The content pane that holds the different views.
     * @param dashboardPane      The dashboard pane.
     * @param dataEntryPane      The data entry pane.
     * @param fetchResultsPane   The fetch results pane.
     */
    private void setupNavigation(VBox navPanel, StackPane dynamicContentPane, GridPane dashboardPane, GridPane dataEntryPane, VBox fetchResultsPane) {
        // Get the buttons from the navigation panel.
        Button dashboardButton = (Button) navPanel.getChildren().get(0);
        Button dataEntryButton = (Button) navPanel.getChildren().get(1);
        Button fetchResultsButton = (Button) navPanel.getChildren().get(2);

        // Set the action for the dashboard button to show the dashboard pane.
        dashboardButton.setOnAction(e -> {
            dynamicContentPane.getChildren().setAll(dashboardPane);
            setEmptyTableState();
        });

        // Set the action for the data entry button to show the data entry pane.
        dataEntryButton.setOnAction(e -> {
            dynamicContentPane.getChildren().setAll(dataEntryPane);
            setEmptyTableState();
        });

        // Set the action for the fetch results button to show the fetch results pane.
        fetchResultsButton.setOnAction(e -> {
            dynamicContentPane.getChildren().setAll(fetchResultsPane);
            setEmptyTableState();
        });
    }

    /**
     * Creates the navigation panel with buttons to switch between different views.
     *
     * @return A VBox containing the navigation buttons.
     */
    private VBox createNavigationPanel() {
        VBox navPanel = new VBox(15);
        navPanel.setPadding(new Insets(15));
        Button dashboardButton = new Button("Dashboard");
        Button dataEntryButton = new Button("Data Entry");
        Button fetchResultsButton = new Button("Fetch Results");

        // Apply CSS styles to the navigation buttons.
        dashboardButton.getStyleClass().add("nav-button");
        dataEntryButton.getStyleClass().add("nav-button");
        fetchResultsButton.getStyleClass().add("nav-button");

        dashboardButton.setMaxWidth(Double.MAX_VALUE);
        dataEntryButton.setMaxWidth(Double.MAX_VALUE);
        fetchResultsButton.setMaxWidth(Double.MAX_VALUE);

        navPanel.getChildren().addAll(dashboardButton, dataEntryButton, fetchResultsButton);
        return navPanel;
    }

    /**
     * Creates the dashboard pane, which displays the available balance and buttons for data operations.
     *
     * @param primaryStage The primary stage for this application.
     * @return A GridPane for the dashboard.
     */
    private GridPane createDashboardPane(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Create the available balance display.
        Label availableBalanceLabel = new Label("Available Balance:");
        this.balanceValueLabel = new Label();
        this.balanceValueLabel.getStyleClass().add("balance-value");
        HBox balanceBox = new HBox(15, availableBalanceLabel, balanceValueLabel);
        balanceBox.getStyleClass().add("balance-box");
        grid.add(balanceBox, 0, 0, 8, 1);

        // Create the "Import Data" and "Export Data" buttons in a VBox.
        Button importButton = new Button("Import Database");
        importButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleImportData(primaryStage);
        });
        importButton.getStyleClass().add("dashboard-button");

        Button exportButton = new Button("Export Database");
        exportButton.setOnAction(e -> controller.handleExportData(primaryStage));
        exportButton.getStyleClass().add("dashboard-button");

        VBox importExportBox = new VBox(15, importButton, exportButton);
        grid.add(importExportBox, 0, 1, 8, 1);

        return grid;
    }

    /**
     * Creates the fetch results pane, which contains controls for fetching statements and balances.
     *
     * @param primaryStage The primary stage for this application.
     * @return A VBox for the fetch results section.
     */
    private VBox createFetchResultsPane(Stage primaryStage) {
        // Create controls for fetching a stock statement.
        TextField statementStockField = new TextField();
        statementStockField.setId("statementStockField");
        statementStockField.setPromptText("Stock Name for Statement");

        // Create controls for fetching other details.
        TextField otherDetailsField = new TextField();
        otherDetailsField.setId("otherDetailsField");
        otherDetailsField.setPromptText("add funds, charges, withdrawal");

        statementStockField.setOnMouseClicked(e -> otherDetailsField.clear());
        otherDetailsField.setOnMouseClicked(e -> statementStockField.clear());

        // "Get Stock Details:" label and text field
        HBox statementInputBox = new HBox(15, new Label("Get Stock Details:"), statementStockField);

        Button statementButton = new Button("View Stock Stmt");
        statementButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewStatement(statementStockField.getText());
        });
        statementButton.getStyleClass().add("fetch-results-button");

        Button balanceButton = new Button("View Stock Bal");
        balanceButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewBalanceForStock(statementStockField.getText());
        });
        balanceButton.getStyleClass().add("fetch-results-button");

        Button exportCsvButton = new Button("Export Stock Stmt");
        exportCsvButton.setOnAction(e -> controller.handleExportStatementToCsv(primaryStage));
        exportCsvButton.getStyleClass().add("fetch-results-button");
        exportCsvButton.disableProperty().bind(controller.isStatementViewProperty().not());

        HBox statementActionsBox = new HBox(15, statementButton, balanceButton, exportCsvButton);

        // Create the "View All Balances" button.
        Button allBalancesButton = new Button("View All Stock Bal");
        allBalancesButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewAllBalances();
            statementStockField.clear();
        });
        allBalancesButton.getStyleClass().add("fetch-results-button");

        Button exportBalancesCsvButton = new Button("Export Stock Bal");
        exportBalancesCsvButton.setOnAction(e -> controller.handleExportAllBalancesToCsv(primaryStage));
        exportBalancesCsvButton.getStyleClass().add("fetch-results-button");
        exportBalancesCsvButton.disableProperty().bind(controller.isAllBalancesViewProperty().not());

        HBox allBalancesActionsBox = new HBox(15, allBalancesButton, exportBalancesCsvButton);

        // Add a separator
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.HORIZONTAL);

        HBox otherDetailsInputBox = new HBox(15, new Label("Get Other Details:"), otherDetailsField);

        Button exportOtherStatementButton = new Button("Export Other Stmt");
        exportOtherStatementButton.setOnAction(e -> controller.handleExportOtherStatementToCsv(primaryStage));
        exportOtherStatementButton.getStyleClass().add("fetch-results-button");
        exportOtherStatementButton.setDisable(true);

        Button otherStatementButton = new Button("View Other Stmt");
        otherStatementButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewOtherStatement(otherDetailsField.getText());
            exportOtherStatementButton.setDisable(false);
        });
        otherStatementButton.getStyleClass().add("fetch-results-button");

        Button otherBalanceButton = new Button("View Other Bal");
        otherBalanceButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewBalanceForTransactionType(otherDetailsField.getText());
            exportOtherStatementButton.setDisable(true);
        });
        otherBalanceButton.getStyleClass().add("fetch-results-button");

        HBox otherStatementActionsBox = new HBox(15, otherStatementButton, otherBalanceButton, exportOtherStatementButton);

        Button allOtherBalancesButton = new Button("View All Other Bal");
        allOtherBalancesButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleViewAllOtherBalances();
            otherDetailsField.clear();
        });
        allOtherBalancesButton.getStyleClass().add("fetch-results-button");

        Button exportAllOtherBalancesButton = new Button("Export Other Bal");
        exportAllOtherBalancesButton.setOnAction(e -> controller.handleExportAllOtherBalancesToCsv(primaryStage));
        exportAllOtherBalancesButton.getStyleClass().add("fetch-results-button");

        HBox allOtherBalancesActionsBox = new HBox(15, allOtherBalancesButton, exportAllOtherBalancesButton);

        // Arrange the controls in a VBox.
        VBox fetchResultsBox = new VBox(15, statementInputBox, statementActionsBox, allBalancesActionsBox, separator, otherDetailsInputBox, otherStatementActionsBox, allOtherBalancesActionsBox);
        fetchResultsBox.setPadding(new Insets(20));
        return fetchResultsBox;
    }

    /**
     * Creates the transaction table with all its columns.
     *
     * @return A TableView for displaying transactions.
     */
    private TableView<Transaction> createTransactionTable() {
        TableView<Transaction> table = new TableView<>();
        table.setId("transactionTable");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create and configure each column for the table.
        idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));

        dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("txnDate"));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));
        // Custom cell factory to format the date.
        dateCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(date));
                }
            }
        });

        typeCol = new TableColumn<>("Txn_Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("txnType"));
        typeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));

        stockCol = new TableColumn<>("Stock_Name");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockName"));
        stockCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20));

        // Columns for numerical data, with custom cell factories for formatting.
        qtyCol = createBigDecimalColumn("Qty", "qty");
        qtyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08));
        rateCol = createBigDecimalColumn("Rate", "rate");
        rateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));
        creditCol = createBigDecimalColumn("Credit", "credit");
        creditCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));
        debitCol = createBigDecimalColumn("Debit", "debit");
        debitCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));
        balanceCol = createBigDecimalColumn("Balance", "balance");
        balanceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        stockBalanceCol = createBigDecimalColumn("Stock_Balance", "stockBalance");
        stockBalanceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));

        idCol.setSortType(TableColumn.SortType.DESCENDING);

        // Add all columns to the table.
        table.getColumns().setAll(Arrays.asList(idCol, dateCol, typeCol, stockCol, qtyCol, rateCol, creditCol, debitCol, stockBalanceCol, balanceCol));
        table.getSortOrder().add(idCol);
        return table;
    }

    /**
     * Creates a TableColumn for BigDecimal values with a custom cell factory for formatting.
     *
     * @param title        The title of the column.
     * @param propertyName The name of the property in the Transaction class.
     * @return A new TableColumn for BigDecimal values.
     */
    private TableColumn<Transaction, BigDecimal> createBigDecimalColumn(String title, String propertyName) {
        TableColumn<Transaction, BigDecimal> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        // Custom cell factory to format the BigDecimal value.
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("Stock_Balance".equals(title)) {
                        setText(String.valueOf(item.intValue()));
                    } else {
                        setText(formatToIndianNumberSystem(item));
                    }
                }
            }
        });
        return column;
    }

    /**
     * Formats a BigDecimal value to a string with the Indian numbering system.
     *
     * @param value The BigDecimal value to format.
     * @return The formatted string.
     */
    private String formatToIndianNumberSystem(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat formatter = NumberFormat.getInstance(new java.util.Locale.Builder().setLanguage("en").setRegion("IN").build());
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        return formatter.format(value);
    }

    /**
     * Creates the input form for adding and modifying transactions.
     *
     * @param primaryStage The primary stage for this application.
     * @return A GridPane containing the input form.
     */
    private GridPane createInputForm(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Create the form fields and assign them to instance variables.
        dateField = new TextField();
        dateField.setId("dateField");
        dateField.setPromptText("DD/MM/YYYY");

        typeField = new TextField();
        typeField.setId("typeField");
        typeField.setPromptText("e.g., BUY, SELL");

        stockField = new TextField();
        stockField.setId("stockField");
        stockField.setPromptText("Stock Name");

        qtyField = new TextField();
        qtyField.setId("qtyField");
        qtyField.setPromptText("Quantity");

        rateField = new TextField();
        rateField.setId("rateField");
        rateField.setPromptText("Rate");

        amountField = new TextField();
        amountField.setId("amountField");
        amountField.setPromptText("Amount");
        amountField.setEditable(false); // Amount is calculated automatically.

        // Add a listener to automatically calculate the amount when quantity or rate changes.
        Runnable calculateAmount = () -> {
            List<String> noStockTxnTypes = Arrays.asList("CHARGES", "ADD FUNDS", "WITHDRAWAL", "REWARDS", "CREDIT", "DEBIT");
            if (noStockTxnTypes.stream().noneMatch(type -> type.equalsIgnoreCase(typeField.getText().trim()))) {
                try {
                    BigDecimal qty = new BigDecimal(qtyField.getText());
                    BigDecimal rate = new BigDecimal(rateField.getText());
                    amountField.setText(qty.multiply(rate).toString());
                } catch (NumberFormatException e) {
                    amountField.setText("");
                }
            }
        };

        qtyField.textProperty().addListener((obs, oldText, newText) -> calculateAmount.run());
        rateField.textProperty().addListener((obs, oldText, newText) -> calculateAmount.run());

        typeField.textProperty().addListener((obs, oldText, newText) -> {
            List<String> noStockTxnTypes = Arrays.asList("CHARGES", "ADD FUNDS", "WITHDRAWAL", "REWARDS", "CREDIT", "DEBIT");
            boolean isNoStockTxn = noStockTxnTypes.stream().anyMatch(type -> type.equalsIgnoreCase(newText.trim()));
            stockField.setDisable(isNoStockTxn);
            qtyField.setDisable(isNoStockTxn);
            rateField.setDisable(isNoStockTxn);
            amountField.setEditable(isNoStockTxn);
            if (isNoStockTxn) {
                stockField.clear();
                qtyField.clear();
                rateField.clear();
                amountField.clear();
            } else {
                calculateAmount.run();
            }
        });

        // Add the form fields to the grid.
        grid.add(new Label("Txn_Date:"),    0, 0);
        grid.add(dateField,         1, 0);
        GridPane.setHgrow(dateField, Priority.ALWAYS);
        grid.add(new Label("Txn_Type:"),    2, 0);
        grid.add(typeField,         3, 0);
        GridPane.setHgrow(typeField, Priority.ALWAYS);
        grid.add(new Label("Stock_Name:"),  4, 0);
        grid.add(stockField,        5, 0);
        GridPane.setHgrow(stockField, Priority.ALWAYS);
        grid.add(new Label("Qty:"),         0, 1);
        grid.add(qtyField,          1, 1);
        GridPane.setHgrow(qtyField, Priority.ALWAYS);
        grid.add(new Label("Rate:"),        2, 1);
        grid.add(rateField,         3, 1);
        GridPane.setHgrow(rateField, Priority.ALWAYS);
        grid.add(new Label("Amount:"),      4, 1);
        grid.add(amountField,       5, 1);
        GridPane.setHgrow(amountField, Priority.ALWAYS);

        // Create the buttons for the form.
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> controller.handleAddTransaction(dateField, typeField, stockField, qtyField, rateField, amountField));
        addButton.getStyleClass().add("form-button");

        Button modifyButton = new Button("Modify");
        modifyButton.setOnAction(e -> controller.handleModifyTransaction(dateField, typeField, stockField, qtyField, rateField, amountField));
        modifyButton.getStyleClass().add("form-button");

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> controller.handleDeleteTransaction());
        deleteButton.getStyleClass().add("form-button");

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearForm());
        clearButton.getStyleClass().add("form-button");

        // First row of buttons
        HBox buttonBox1 = new HBox(15, addButton, modifyButton, deleteButton, clearButton);
        grid.add(buttonBox1, 0, 2, 8, 1);

        Button viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.loadAllTransactions();
        });
        viewAllButton.getStyleClass().add("form-button");

        Button deleteAllButton = new Button("Delete All");
        deleteAllButton.setOnAction(e -> controller.handleDeleteAllTransactions());
        deleteAllButton.getStyleClass().add("form-button");

        Button importCsvButton = new Button("Import CSV");
        importCsvButton.setOnAction(e -> {
            restoreTransactionTableColumns();
            controller.handleImportCsv(primaryStage);
        });
        importCsvButton.getStyleClass().add("form-button");

        // Second row of buttons
        HBox buttonBox2 = new HBox(15, viewAllButton, deleteAllButton, importCsvButton);
        grid.add(buttonBox2, 0, 3, 8, 1);

        return grid;
    }

    /**
     * Populates the input form with the data from a selected transaction.
     *
     * @param transaction The selected Transaction object.
     */
    private void populateForm(Transaction transaction) {
        if (transaction == null) {
            clearForm();
            return;
        }
        // Set the values of the form fields using the instance variables.
        dateField.setText(transaction.getTxnDate() != null ? transaction.getTxnDate().format(DATE_FORMATTER) : "");
        typeField.setText(transaction.getTxnType() != null ? transaction.getTxnType() : "");
        stockField.setText(transaction.getStockName() != null ? transaction.getStockName() : "");
        qtyField.setText(transaction.getQty() != null ? transaction.getQty().toString() : "");
        rateField.setText(transaction.getRate() != null ? transaction.getRate().toString() : "");

        // Calculate and set the amount field.
        BigDecimal amount = BigDecimal.ZERO;
        if (transaction.getCredit() != null && transaction.getCredit().compareTo(BigDecimal.ZERO) != 0) {
            amount = transaction.getCredit();
        } else if (transaction.getDebit() != null) {
            amount = transaction.getDebit();
        }
        amountField.setText(amount.toString());
    }

    /**
     * Clears all input fields in the form.
     */
    private void clearForm() {
        // Clear all the form fields using the instance variables.
        dateField.clear();
        typeField.clear();
        stockField.clear();
        qtyField.clear();
        rateField.clear();
        amountField.clear();

        // Clear the selection in the transaction table.
        if (transactionTable != null) {
            transactionTable.getSelectionModel().clearSelection();
        }
    }

    private void setEmptyTableState() {
        transactionTable.getItems().clear();
        transactionTable.getColumns().clear();
        TableColumn<Transaction, ?> emptyColumn = new TableColumn<>("empty");
        emptyColumn.getStyleClass().add("empty-column");
        transactionTable.getColumns().add(emptyColumn);
        transactionTable.setPlaceholder(new StackPane(new Label("no content in table")));
    }

    private void restoreTransactionTableColumns() {
        transactionTable.getColumns().setAll(Arrays.asList(idCol, dateCol, typeCol, stockCol, qtyCol, rateCol, creditCol, debitCol, stockBalanceCol, balanceCol));
        idCol.setSortType(TableColumn.SortType.DESCENDING);
        transactionTable.getSortOrder().add(idCol);
    }
}
