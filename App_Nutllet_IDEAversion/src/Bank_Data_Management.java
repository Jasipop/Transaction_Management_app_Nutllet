//package Merge;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
/**
 * The Bank_Data_Management class is a JavaFX application for managing bank transactions
 * and account information. It provides a user-friendly interface to view transaction records,
 * manage multiple bank accounts, and import transaction data from CSV files.
 *
 * <p>Main features include:
 * <ul>
 *     <li>Styled table view for transaction history</li>
 *     <li>Card-style display for bank account list</li>
 *     <li>CSV import functionality for WeChat-format and standard bank files</li>
 *     <li>Dynamic navigation bar and modular design</li>
 * </ul>
 *
 * This class contains two inner data models:
 * {@link Bank_Data_Management.BankTransaction} and {@link Bank_Data_Management.BankAccount}.
 *
 * @author Jingyi Liang
 * @version 2.2
 */
public class Bank_Data_Management extends Application {
    private Stage primaryStage;
    private ScrollPane accountScroll;
    private VBox accountCard;
    public static class BankTransaction {
        private final String date, description, amount, type;
        /**
         * Constructs a BankTransaction with specified parameters.
         *
         * @param date        the transaction date
         * @param desc        a description of the transaction (e.g., merchant or item)
         * @param amount      the monetary value of the transaction
         * @param type        the type of transaction (e.g., "Debit", "Credit", or raw CSV label)
         */
        public BankTransaction(String date, String desc, String amount, String type) {
            this.date = date;
            this.description = desc;
            this.amount = amount;
            this.type = type;
        }
        /** @return the transaction date */
        public String getDate() { return date; }
        /** @return the transaction description */
        public String getDescription() { return description; }
        /** @return the transaction amount */
        public String getAmount() { return amount; }
        /** @return the transaction type */
        public String getType() { return type; }
    }

    /**
     * Data model class representing a bank account entity.
     * <p>
     * Contains essential account information including account number and bank institution.
     */
    public static class BankAccount {
        private final String accountNumber, bankName;
        /**
         * Constructs a BankAccount with specified account number and bank name.
         *
         * @param accNum   the full bank account number
         * @param bankName the name of the bank (e.g., "ICBC", "Bank of China")
         */
        public BankAccount(String accNum, String bankName) {
            this.accountNumber = accNum; this.bankName = bankName;
        }
        /** @return the account number */
        public String getAccountNumber() { return accountNumber; }
        /** @return the bank name */
        public String getBankName() { return bankName; }
    }

    private final ObservableList<BankTransaction> transactions = FXCollections.observableArrayList();


    private final ObservableList<BankAccount> accounts = FXCollections.observableArrayList(
            new BankAccount("6222 1234 5678 9012", "ICBC"),
            new BankAccount("6217 8888 0000 9999", "Bank of China"),
            new BankAccount("6234 5678 9012 3456", "China Merchants Bank"),
            new BankAccount("6225 4321 9876 5432", "CCB"),
            new BankAccount("6210 1122 3344 5566", "ABC"),
            new BankAccount("6233 6655 4477 8899", "SPDB"),
            new BankAccount("6228 8765 4321 0987", "CMBC"),
            new BankAccount("6216 2233 4455 6677", "Ping An Bank")
    );

    /**
     *JavaFX Application Startup Entry Method
     *@ param stage main window object
     */
    @Override

    public void start(Stage stage) {
        this.primaryStage = stage;

        Label pageTitle = new Label("Bank Data Management");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 38));
        pageTitle.setTextFill(Color.WHITE);
        pageTitle.setEffect(new DropShadow(15, Color.web("#4c3092")));

        Label subtitle = new Label("Manage your bank accounts and transactions");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        subtitle.setTextFill(Color.web("#e6d5ff"));


        VBox titleBox = new VBox(8, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);");
        titleBox.setPadding(new Insets(30, 0, 30, 0));

        TableView<BankTransaction> table = createTransactionTable();
        VBox tableCard = createStyledCard(table, "Transaction Records");

        VBox accountCard = createAccountCard();

        HBox mainContent = new HBox(30, tableCard, accountCard);
        mainContent.setPadding(new Insets(25, 50, 25, 50));
        mainContent.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: #fafafa; -fx-padding: 20 0;");

        VBox contentWrapper = new VBox(scrollPane);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(0));
        contentWrapper.setStyle("-fx-background-color: #fafafa;");

        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(50);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(70);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);

        VBox mainLayout = new VBox(0, titleBox, contentWrapper, navBar);
        mainLayout.setStyle("-fx-background-color: #fafafa;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(mainLayout, 1366, 768);
        stage.setTitle("Bank Data Viewer");
        stage.setScene(scene);
        stage.show();

        loadTransactionsFromCSV("deals.csv");
    }


    /**
     * Creates a TableView to display bank transactions with styled columns.
     *
     * @return a TableView configured to display BankTransaction objects
     */
    private TableView<BankTransaction> createTransactionTable() {
        TableView<BankTransaction> table = new TableView<>(transactions);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-font-size: 15px; " +
                        "-fx-table-cell-size: 40px; " +
                        "-fx-selection-bar: #d1c4e9; " +
                        "-fx-selection-bar-non-focused: #b39ddb;" +
                        "-fx-text-fill: black;" +
                        "-fx-font-family: 'Microsoft YaHei';"
        );

        table.getColumns().addAll(
                createStyledColumn("Date", BankTransaction::getDate),
                createStyledColumn("Description", BankTransaction::getDescription),
                createStyledColumn("Amount", BankTransaction::getAmount),
                createStyledColumn("Type", BankTransaction::getType)
        );

        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            Node header = table.lookup("TableHeaderRow");
            if (header != null && header.isVisible()) {
                header.setStyle("-fx-background-color: #d1c4e9;");
            }
        });

        return table;
    }
    /**
     * Generates styled table columns with centralized alignment
     * @param title Column header text
     * @param prop Property value extractor for column data
     * @return Configured TableColumn with unified styling
     */
    private TableColumn<BankTransaction, String> createStyledColumn(String title,
                                                                    java.util.function.Function<BankTransaction, String> prop) {
        TableColumn<BankTransaction, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(prop.apply(cell.getValue())));
        col.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: black;");
        return col;
    }
    /**
     * Constructs scrollable account list container
     * @return VBox containing interactive account entries with delete functionality
     */
    private VBox createAccountList() {
        VBox accountList = new VBox(10);
        accountList.setPadding(new Insets(5));

        for (BankAccount account : accounts) {
            HBox accountEntry = new HBox();
            accountEntry.setPadding(new Insets(10));
            accountEntry.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 8;"
            );

            VBox infoVBox = new VBox(5);
            Label bankLabel = createInfoLabel("🏦 " + account.getBankName());
            Label accountLabel = createInfoLabel("💳 " + account.getAccountNumber());
            infoVBox.getChildren().addAll(bankLabel, accountLabel);

            Button deleteBtn = new Button("×");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-size: 16px;");
            deleteBtn.setOnAction(e -> {
                accounts.remove(account);
                refreshAccountList();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            accountEntry.getChildren().addAll(infoVBox, spacer, deleteBtn);
            accountList.getChildren().add(accountEntry);
        }
        return accountList;
    }
    /**
     * Creates standardized card title label
     * @param title Text content for the title
     * @return Prestyled label component with specified typography
     */
    private Label createCardTitle(String title) {
        Label cardTitle = new Label(title);
        cardTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        cardTitle.setTextFill(Color.web("#6c757d"));
        return cardTitle;
    }
    /**
     * Generates UI card container with shadow effects
     * @param table Transaction table to embed
     * @param title Card header text
     * @return Styled VBox container with embedded content
     */
    private VBox createStyledCard(TableView<BankTransaction> table, String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);" +
                        "-fx-pref-width: 700;" +
                        "-fx-pref-height: 620;"
        );

        table.setFixedCellSize(40);
        table.setPrefHeight(480);

        table.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        Label infoLabel = new Label("The system will automatically integrate and display transaction details.");
        infoLabel.setFont(Font.font("Microsoft YaHei", 12));
        infoLabel.setTextFill(Color.web("#666"));
        infoLabel.setWrapText(true);
        infoLabel.setPadding(new Insets(15, 10, 10, 10));
        infoLabel.setMaxWidth(680);

        card.getChildren().addAll(createCardTitle(title), table, infoLabel);

        return card;
    }
    /**
     * Creates CSV import button with hover effects
     * @return Styled button component with icon and text
     */
    private Button createImportButton() {
        Button importBtn = new Button("📁 Import Bank CSV");
        importBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;"
        );
        importBtn.setOnAction(e -> handleCSVImport(primaryStage));
        return importBtn;
    }
    /**
     * Builds account management card with scrollable list
     * @return Complete account card with CRUD operations
     */
    private VBox createAccountCard() {
        accountCard = new VBox(10);
        accountCard.setPadding(new Insets(15));
        accountCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);" +
                        "-fx-pref-width: 400;"
        );

        accountScroll = new ScrollPane(createAccountList());
        accountScroll.setFitToWidth(true);
        accountScroll.setPrefHeight(550);
        accountScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        Button importBtn = createImportButton();
        Button addAccountBtn = createAddAccountButton();

        HBox buttonBox = new HBox(10, importBtn, addAccountBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        accountCard.getChildren().addAll(
                createCardTitle("Bank Accounts"),
                accountScroll,
                buttonBox
        );

        return accountCard;
    }

    /**
     * Creates account addition button with validation
     * @return Styled button triggering account creation dialog
     */
    private Button createAddAccountButton() {
        Button addBtn = new Button("➕ Add Account");
        addBtn.setStyle(
                "-fx-background-color: #28a745;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;"
        );
        addBtn.setOnAction(e -> showAddAccountDialog());
        return addBtn;
    }
    /**
     * Validates input fields for account creation
     * @param accountNumberField Input field for account numbers
     * @param bankNameField Input field for bank names
     * @param addButton Button to enable/disable based on validation
     */
    private void validateInputs(TextField accountNumberField, TextField bankNameField, Node addButton) {
        boolean disable = accountNumberField.getText().trim().isEmpty() ||
                bankNameField.getText().trim().isEmpty();
        addButton.setDisable(disable);
    }
    /**
     * Displays modal dialog for new account creation
     * <p>
     * Contains form validation and confirmation handling
     */
    private void showAddAccountDialog() {
        Dialog<BankAccount> dialog = new Dialog<>();
        dialog.setTitle("Add New Bank Account");
        dialog.setHeaderText("Please enter the account details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField accountNumberField = new TextField();
        accountNumberField.setPromptText("Account Number");
        TextField bankNameField = new TextField();
        bankNameField.setPromptText("Bank Name");

        grid.add(new Label("Account Number:"), 0, 0);
        grid.add(accountNumberField, 1, 0);
        grid.add(new Label("Bank Name:"), 0, 1);
        grid.add(bankNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        accountNumberField.textProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(accountNumberField, bankNameField, addButton));
        bankNameField.textProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(accountNumberField, bankNameField, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new BankAccount(
                        accountNumberField.getText().trim(),
                        bankNameField.getText().trim()
                );
            }
            return null;
        });

        Optional<BankAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            accounts.add(account);
            refreshAccountList();
            accountScroll.setVvalue(1.0);
        });
    }
    /**
     * Refreshes account list UI components
     */
    private void refreshAccountList() {
        accountScroll.setContent(createAccountList());
    }
    /**
     * Generates standardized info labels
     * @param text Label content
     * @return Prestyled text label with wrapping
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", 14));
        label.setTextFill(Color.web("#444"));
        label.setWrapText(true);
        return label;
    }
    /**
     * Handles CSV file selection and parsing
     * @param stage Parent window for file chooser dialog
     */
    private void handleCSVImport(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select WeChat CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                transactions.clear();
                String line;
                boolean dataSection = false;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("交易时间")) {
                        dataSection = true;
                        continue;
                    }
                    if (!dataSection || line.isEmpty()) continue;

                    String[] parts = line.split("\",\"");
                    if (parts.length >= 6) {
                        for (int i = 0; i < parts.length; i++) {
                            parts[i] = parts[i].replaceAll("^\"|\"$", "").trim();
                        }

                        String date = parts[0];
                        String thirdCol = parts[2];
                        String fourthCol = parts[3];
                        String desc = (fourthCol.equals("/") || fourthCol.matches("^\\d+$")) ? thirdCol : fourthCol;
                        String amount = parts[5];
                        String type = parts[4].contains("支出") ? "Debit" : "Credit";

                        transactions.add(new BankTransaction(date, desc, amount, type));

                    }
                }
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to read file.").showAndWait();
            }
        }
    }
    /**
     * Loads transaction data from a specified CSV file path.
     * Supports WeChat-style and standard bank CSV formats.
     *
     * @param path the path to the CSV file to load
     * @return a List of BankTransaction objects parsed from the file
     * @throws IOException if the file cannot be read or parsing fails
     */
    private void loadTransactionsFromCSV(String path) {
        File file = new File(path);
        if (!file.exists()) {
            new Alert(Alert.AlertType.WARNING, "CSV doesn't exist: " + path).showAndWait();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            transactions.clear();
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split("\",\"");

                if (parts.length >= 8) {
                    parts[0] = parts[0].replaceFirst("^\"", "");
                    parts[parts.length - 1] = parts[parts.length - 1].replaceFirst("\"$", "");

                    String date = parts[0].trim();
                    String col1 = parts[1].trim();
                    String col2 = parts[2].trim();
                    String col3 = parts[3].trim();
                    String col4 = parts[4].trim();
                    String rawAmount = parts[5].trim();
                    String col6 = parts[6].trim();
                    String col7 = parts[7].trim();

                    String description;
                    if (col3.equals("/") || col3.matches("^\\d+$")) {
                        description = col2;
                    } else {
                        description = col3;
                    }

                    String amount = rawAmount.replace("¥", "").trim();

                    String type = col7;

                    transactions.add(new BankTransaction(date, description, amount, type));
                }
            }
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "failed to read csv: " + path).showAndWait();
        }
    }

    /**
     * Creates navigation buttons with emoji icons
     * @param label Button text label
     * @param emoji Unicode emoji character
     * @return Compound button with vertical layout
     */
    // Helper method with emoji
    private Button createNavButtonWithEmoji(String label, String emoji) {
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(2);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        Button button = new Button();
        button.setPrefWidth(456);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        return button;
    }
    /**
     * Generates basic navigation button
     * @param label Button text content
     * @return Minimalist styled navigation button
     */
    private Button createNavButton(String label) {
        Button button = new Button(label);
        button.setPrefWidth(456); // 1366 / 3
        button.setPrefHeight(60);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #7f8c8d;" +
                        "-fx-border-color: transparent;"
        );
        return button;
    }
    /**
     * Application main method
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}