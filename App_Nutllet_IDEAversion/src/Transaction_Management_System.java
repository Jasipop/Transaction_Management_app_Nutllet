//package Merge;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A JavaFX application for managing financial transactions with a modern user interface.
 * This application provides functionality for tracking, analyzing, and managing financial transactions
 * with support for both online and offline transactions.
 *
 * <p>The application features include:
 * <ul>
 *     <li>Transaction entry and management</li>
 *     <li>Visual data representation using pie charts</li>
 *     <li>Transaction filtering and analysis</li>
 *     <li>Data export capabilities</li>
 *     <li>Statistical analysis of transactions</li>
 * </ul>
 *
 * @author Jingyi Liang
 * @version final
 */
public class Transaction_Management_System extends Application {
    private HBox chartContent;

    /**
     * Represents a financial transaction with its associated details.
     * This inner class encapsulates the data structure for individual transactions.
     */
    public static class Transaction {
        private final String date;
        private final String name;
        private final String amount;
        private String type;

        /**
         * Constructs a new Transaction with the specified details.
         *
         * @param date The date of the transaction
         * @param name The name or description of the transaction
         * @param amount The monetary amount of the transaction
         * @param type The type of transaction (Online/Offline)
         */
        public Transaction(String date, String name, String amount, String type) {
            this.date = date;
            this.name = name;
            this.amount = amount;
            this.type = type;
        }

        /**
         * Sets the type of the transaction.
         *
         * @param type The new type of the transaction
         */
        public void setType(String type) { this.type = type; }

        /**
         * Gets the date of the transaction.
         *
         * @return The transaction date
         */
        public String getDate() { return date; }

        /**
         * Gets the name of the transaction.
         *
         * @return The transaction name
         */
        public String getName() { return name; }

        /**
         * Gets the amount of the transaction.
         *
         * @return The transaction amount
         */
        public String getAmount() { return amount; }

        /**
         * Gets the type of the transaction.
         *
         * @return The transaction type
         */
        public String getType() { return type; }
    }

    private ObservableList<Transaction> transactions;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private PieChart typeChart;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the primary stage of the application.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        initializeData();
        setupTypeChart();

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();
        HBox mainContent = createMainContent(leftPanel, rightPanel);
        VBox mainLayout = createMainLayout(mainContent);

        // Set background color for the entire scene
        mainLayout.setStyle("-fx-background-color: #f8f0ff;");

        Scene scene = new Scene(mainLayout, 1366, 768);
        String scrollBarCss =
                ".scroll-bar:vertical {" +
                        "    -fx-background-color: transparent;" +
                        "    -fx-pref-width: 8px;" +
                        "}" +
                        ".scroll-bar:vertical .track {" +
                        "    -fx-background-color: transparent;" +
                        "}" +
                        ".scroll-bar:vertical .thumb {" +
                        "    -fx-background-color: rgba(150,150,150,0.5);" +
                        "    -fx-background-radius: 4px;" +
                        "}" +
                        ".scroll-bar:vertical .thumb:hover {" +
                        "    -fx-background-color: rgba(150,150,150,0.7);" +
                        "}" +
                        ".scroll-bar .increment-button, .scroll-bar .decrement-button {" +
                        "    -fx-background-color: transparent;" +
                        "    -fx-padding: 0;" +
                        "}" +
                        ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow {" +
                        "    -fx-shape: \" \";" +
                        "}";

        scene.getRoot().setStyle(scrollBarCss);
        primaryStage.setTitle("Transaction Management");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn,discoverBtn,settingsBtn  );

        mainLayout.getChildren().add(navBar);
    }

    /**
     * Initializes the transaction data by reading from a CSV file.
     * Loads existing transaction records and populates the data structure.
     */
    private void initializeData() {
        transactions = FXCollections.observableArrayList();
        String csvFile = "deals.csv";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip first three lines
            br.readLine();
            br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replaceAll("^\"|\"$", "").trim();
                }

                if (fields.length < 11) continue;

                String fullDate = fields[0];
                String altDesc = fields[1];
                String desc = fields[2];
                String amountWithSymbol = fields[5];

                if (desc.equals("/") || desc.matches("\\d+")) {
                    desc = altDesc;
                }

                String date = fullDate.split(" ")[0];
                String amount = amountWithSymbol.replaceAll("[¥¥,]", "").trim();

                try {
                    Double.parseDouble(amount);
                    // Set initial type as Online
                    transactions.add(new Transaction(date, desc, amount, "Online"));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid amount format: " + amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the left panel of the application containing the transaction table.
     *
     * @return A VBox containing the transaction table and related controls
     */
    private VBox createLeftPanel() {
        TableView<Transaction> table = new TableView<>(transactions);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: #fff;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-table-header-border-color: transparent;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        table.setPrefHeight(350);
        table.setFixedCellSize(40);
        table.setPrefWidth(560);  

        // Add hover effect to table rows
        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setStyle("-fx-font-family: 'Microsoft YaHei';");
            deleteItem.setOnAction(event -> {
                Transaction selected = row.getItem();
                if (selected != null) {
                    transactions.remove(selected);
                    updateStatsAndChart();
                }
            });

            contextMenu.getItems().add(deleteItem);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setStyle("-fx-background-color: transparent;");
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f8f0ff;");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: transparent;");
                }
            });

            return row;
        });

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        dateCol.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold; -fx-text-fill: #616161;");

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Transaction Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty("¥" + cell.getValue().getAmount()));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        typeCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setCellFactory(column -> {
            return new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.equals("Online")) {
                            setStyle("-fx-text-fill: #6200ee; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #03dac6; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        typeCol.setCellFactory(ComboBoxTableCell.forTableColumn("Online", "Offline"));

        typeCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setType(event.getNewValue());
            updateStatsAndChart(); 
        });

        typeCol.setStyle("-fx-alignment: CENTER;");
        table.getColumns().addAll(dateCol, nameCol, amountCol, typeCol);

        Label title = new Label("Transaction Entry");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        Button addButton = new Button("Add Transaction");
        addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 12 30;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        addButton.setOnMouseEntered(e ->
                addButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-family: 'Microsoft YaHei';" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12 30;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.5, 0, 4);"
                )
        );

        addButton.setOnMouseExited(e ->
                addButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-family: 'Microsoft YaHei';" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12 30;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
                )
        );

        addButton.setOnAction(e -> showAddTransactionDialog());

        VBox leftPanel = new VBox(15);
        leftPanel.getChildren().addAll(title, table, addButton);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        leftPanel.setMinWidth(600);
        leftPanel.setMaxWidth(600);
        leftPanel.setPrefHeight(480);
        leftPanel.setMinHeight(480);
        leftPanel.setMaxHeight(480);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        
        VBox wrapper = new VBox(leftPanel);
        wrapper.setAlignment(Pos.TOP_CENTER);

        return wrapper;
    }

    /**
     * Updates both the statistics and chart display.
     * Refreshes the visual representation of transaction data.
     */
    private void updateStatsAndChart() {
        updateTypeChart();
        refreshStatsBox();
    }

    /**
     * Refreshes the statistics box with current transaction data.
     * Updates the display of transaction statistics.
     */
    private void refreshStatsBox() {
        VBox newStatsBox = createStatsBox();
        newStatsBox.setMaxWidth(220);
        newStatsBox.setPrefWidth(220);

        Platform.runLater(() -> {
            chartContent.getChildren().set(1, newStatsBox);
        });
    }

    /**
     * Creates the right panel of the application containing charts and statistics.
     *
     * @return A VBox containing the chart and statistics display
     */
    private VBox createRightPanel() {
        Label chartTitle = new Label("Transaction Analysis");
        chartTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        chartTitle.setTextFill(Color.web("#6c757d"));

        VBox statsBox = createStatsBox();
        statsBox.setMaxWidth(220);
        statsBox.setPrefWidth(220);

        StackPane chartWrapper = new StackPane(typeChart);
        chartWrapper.setPrefSize(420, 420);
        chartWrapper.setMinSize(420, 420);
        chartWrapper.setMaxSize(420, 420);
        chartWrapper.setAlignment(Pos.CENTER);

        chartContent = new HBox(20);
        chartContent.setAlignment(Pos.CENTER);
        chartContent.getChildren().addAll(chartWrapper, statsBox);

        HBox buttonBox = createActionButtons();
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setMaxWidth(560);

        VBox chartBox = new VBox(15);
        chartBox.getChildren().addAll(chartTitle, chartContent, buttonBox);
        chartBox.setPadding(new Insets(20));
        chartBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        chartBox.setMinWidth(700);
        chartBox.setMaxWidth(700);

        ScrollPane scrollPane = new ScrollPane(chartBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));

        VBox rightPanel = new VBox(0);
        rightPanel.getChildren().add(scrollPane);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        return rightPanel;
    }

    /**
     * Creates a statistics box displaying transaction summaries.
     *
     * @return A VBox containing statistical information
     */
    private VBox createStatsBox() {
        VBox statsBox = new VBox(10);  
        statsBox.setStyle(
                "-fx-padding: 15;" +
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.5, 0, 2);"
        );

        double totalAmount = transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double onlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Online"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double offlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Offline"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        Label titleLabel = new Label("Statistics");
        titleLabel.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        double avg = transactions.isEmpty() ? 0 : totalAmount / transactions.size();

        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.CHINA);
        VBox totalBox = createStatsItemBox("Total Amount", fmt.format(totalAmount), "💰");
        VBox onlineBox = createStatsItemBox("Online Total", fmt.format(onlineAmount), "🌐");
        VBox offlineBox = createStatsItemBox("Offline Total", fmt.format(offlineAmount), "🏪");
        VBox avgBox = createStatsItemBox("Average Amount", fmt.format(avg), "📊");

        statsBox.getChildren().addAll(titleLabel, totalBox, onlineBox, offlineBox, avgBox);

        return statsBox;
    }

    /**
     * Creates a statistics item box for displaying individual statistics.
     *
     * @param title The title of the statistic
     * @param value The value to display
     * @param iconCode The icon code for the statistic
     * @return A VBox containing the statistic display
     */
    private VBox createStatsItemBox(String title, String value, String iconCode) {
        VBox itemBox = new VBox(8);
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text(iconCode);
        icon.setFont(Font.font("FontAwesome", 20));
        icon.setFill(Color.web("#7b1fa2"));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-text-fill: #757575; -fx-font-size: 14;");

        header.getChildren().addAll(icon, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 18; -fx-text-fill: #212121; -fx-font-weight: bold;");

        itemBox.setOnMouseEntered(e -> {
            itemBox.setStyle("-fx-background-color: #f3e5f5; -fx-background-radius: 12; -fx-padding: 15;");
            itemBox.setCursor(Cursor.HAND);
            itemBox.setEffect(new DropShadow(10, Color.web("#d1c4e940")));
        });
        itemBox.setOnMouseExited(e -> {
            itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
            itemBox.setEffect(null);
        });

        itemBox.getChildren().addAll(header, valueLabel);
        return itemBox;
    }

    /**
     * Creates action buttons for the application.
     *
     * @return An HBox containing the action buttons
     */
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button exportButton = new Button("Export Data");
        exportButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100;" +
                        "-fx-max-width: 100;"
        );
        exportButton.setOnAction(e -> exportData());

        Button filterButton = new Button("Filter");
        filterButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100;" +
                        "-fx-max-width: 100;"
        );
        filterButton.setOnAction(e -> showFilterDialog());

        buttonBox.getChildren().addAll(exportButton, filterButton);
        return buttonBox;
    }

    /**
     * Handles the export of transaction data.
     * Provides options for exporting data in different formats.
     */
    private void exportData() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Data");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Results cannot be changed");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Button csvButton = new Button("Export as CSV");

        String buttonStyle = "-fx-background-color: linear-gradient(to right, #7b1fa2, #9c27b0);"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Microsoft YaHei';"
                + "-fx-font-size: 14px;"
                + "-fx-padding: 12 30;"
                + "-fx-background-radius: 25;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);";

        csvButton.setStyle(buttonStyle);

        csvButton.setOnAction(e -> {
            exportToCSV();
            dialog.close();
        });


        content.getChildren().addAll(titleLabel, csvButton);

        Scene dialogScene = new Scene(content, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    /**
     * Creates a preview table for transaction data.
     *
     * @return A TableView containing the transaction preview
     */
    private TableView<Transaction> createPreviewTable() {
        TableView<Transaction> table = new TableView<>();

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty("¥" + cell.getValue().getAmount()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));

        table.getColumns().addAll(dateCol, nameCol, amountCol, typeCol);
        table.setItems(transactions);
        table.setPrefHeight(300);

        return table;
    }

    /**
     * Calculates the total amount of all transactions.
     *
     * @return The total amount as a formatted string
     */
    private String calculateTotalAmount() {
        double total = transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();
        return String.format("%.2f", total);
    }

    /**
     * Exports transaction data to CSV format.
     * Generates and saves transaction data in CSV format.
     */
    private void exportToCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Transaction Name,Amount,Type\n");
        for (Transaction t : transactions) {
            csv.append(String.format("%s,%s,%s,%s\n",
                    t.getDate(),
                    t.getName(),
                    t.getAmount(),
                    t.getType()));
        }
        String csvData = csv.toString();

        System.out.println("Exporting to CSV...");
        System.out.println(csvData);

        Platform.runLater(() -> {
            showExportDataDialog(csvData);
            showSuccessAlert();
        });
    }

    /**
     * Shows a dialog displaying exported CSV data.
     *
     * @param csvData The CSV data to display
     */
    private void showExportDataDialog(String csvData) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Exported CSV Data");

        Node csvIcon = new Text("\uD83D\uDCC0"); // 📀
        csvIcon.setStyle("-fx-font-size: 24px;");

        TextArea textArea = new TextArea(csvData);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(800, 500);

        Button saveButton = new Button("Save to Local");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setGraphic(new Text("\uD83D\uDCBE")); 
        saveButton.setOnAction(e -> saveToLocal(csvData, dialog));

        HBox hintBox = new HBox(10);
        hintBox.setAlignment(Pos.CENTER_RIGHT);
        Text warningIcon = new Text("\u2754"); // ❔
        warningIcon.setStyle("-fx-font-family: 'Segoe UI Symbol'; -fx-font-size: 16px; -fx-fill: #666;");
        Label hintLabel = new Label("Any questions? Try again!");
        hintLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-font-size: 12px;");

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(hintLabel, warningIcon, saveButton);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(
                new HBox(5, csvIcon, new Label("CSV Export Content:")),
                scrollPane,
                bottomBox
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Saves CSV data to a local file.
     *
     * @param csvData The CSV data to save
     * @param parentStage The parent stage for the file chooser
     */
    private void saveToLocal(String csvData, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("transactions_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(csvData);
                showSaveSuccessAlert(); 
            } catch (IOException ex) {
                showErrorAlert("Save Failed", "Error saving file: " + ex.getMessage());
            }
        }
    }

    /**
     * Shows a success alert for successful operations.
     */
    private void showSuccessAlert() {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.NONE);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label("✅ Successfully Exported!");
        label.setStyle("-fx-font-size: 18px;"
                + "-fx-text-fill: #4CAF50;"
                + "-fx-font-weight: bold;"
                + "-fx-background-color: rgba(255,255,255,0.9);"
                + "-fx-padding: 15px 25px;"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        alertStage.setX((screenBounds.getWidth() - 300) / 2);
        alertStage.setY((screenBounds.getHeight() - 100) / 2);

        alertStage.setScene(scene);
        alertStage.setWidth(300);
        alertStage.setHeight(100);
        alertStage.show();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2),
                        e -> alertStage.close()
                ));
        timeline.play();
    }

    /**
     * Shows a success alert for successful file save operations.
     */
    private void showSaveSuccessAlert() {
        Stage alertStage = new Stage();
        alertStage.initStyle(StageStyle.UTILITY);
        alertStage.initModality(Modality.NONE);

        Label label = new Label("✅ File saved successfully!");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        Scene scene = new Scene(label);
        alertStage.setScene(scene);
        alertStage.setWidth(300);
        alertStage.setHeight(100);
        alertStage.show();

        new Timeline(new KeyFrame(Duration.seconds(2), e -> alertStage.close())).play();
    }

    /**
     * Shows an error alert with the specified title and message.
     *
     * @param title The title of the error alert
     * @param message The error message to display
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a dialog for filtering transactions.
     */
    private void showFilterDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Filter Transactions");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Date Range
        Label dateLabel = new Label("Date Range");
        dateLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        HBox dateBox = new HBox(10);
        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();
        startDate.setPromptText("Start Date");
        endDate.setPromptText("End Date");
        dateBox.getChildren().addAll(startDate, endDate);

        // Amount Range
        Label amountLabel = new Label("Amount Range");
        amountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        HBox amountBox = new HBox(10);
        TextField minAmount = new TextField();
        TextField maxAmount = new TextField();
        minAmount.setPromptText("Min Amount");
        maxAmount.setPromptText("Max Amount");
        amountBox.getChildren().addAll(minAmount, maxAmount);

        // Type Selection
        Label typeLabel = new Label("Transaction Type");
        typeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton allRadio = new RadioButton("All");
        RadioButton onlineRadio = new RadioButton("Online");
        RadioButton offlineRadio = new RadioButton("Offline");
        allRadio.setToggleGroup(typeGroup);
        onlineRadio.setToggleGroup(typeGroup);
        offlineRadio.setToggleGroup(typeGroup);
        allRadio.setSelected(true);

        HBox typeBox = new HBox(20);
        typeBox.getChildren().addAll(allRadio, onlineRadio, offlineRadio);

        Button applyButton = new Button("Apply Filter");
        applyButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 30;" +
                        "-fx-background-radius: 8;"
        );

        applyButton.setOnAction(e -> {
            filterTransactions(
                    startDate.getValue(),
                    endDate.getValue(),
                    minAmount.getText(),
                    maxAmount.getText(),
                    getSelectedType(typeGroup)
            );
            dialog.close();
        });

        content.getChildren().addAll(
                dateLabel, dateBox,
                amountLabel, amountBox,
                typeLabel, typeBox,
                applyButton
        );

        Scene dialogScene = new Scene(content, 400, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    /**
     * Gets the selected transaction type from a toggle group.
     *
     * @param group The toggle group containing the type selection
     * @return The selected transaction type
     */
    private String getSelectedType(ToggleGroup group) {
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        return selected.getText();
    }

    /**
     * Filters transactions based on specified criteria.
     *
     * @param startDate The start date for filtering
     * @param endDate The end date for filtering
     * @param minAmount The minimum amount for filtering
     * @param maxAmount The maximum amount for filtering
     * @param type The transaction type for filtering
     */
    private void filterTransactions(LocalDate startDate, LocalDate endDate,
                                    String minAmount, String maxAmount, String type) {
        ObservableList<Transaction> filteredList = FXCollections.observableArrayList();

        double min = minAmount.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minAmount);
        double max = maxAmount.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxAmount);

        for (Transaction t : transactions) {
            LocalDate transDate = LocalDate.parse(t.getDate());
            double amount = Double.parseDouble(t.getAmount());

            boolean dateInRange = (startDate == null || !transDate.isBefore(startDate)) &&
                    (endDate == null || !transDate.isAfter(endDate));
            boolean amountInRange = amount >= min && amount <= max;
            boolean typeMatches = type.equals("All") || t.getType().equals(type);

            if (dateInRange && amountInRange && typeMatches) {
                filteredList.add(t);
            }
        }

        transactions = filteredList;
        updateStatsAndChart();
    }

    /**
     * Shows a dialog for adding new transactions.
     */
    private void showAddTransactionDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Transaction");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );
        datePicker.setPromptText("Select Date");

        TextField nameField = new TextField();
        nameField.setPromptText("Transaction Name");
        nameField.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton onlineRadio = new RadioButton("Online");
        RadioButton offlineRadio = new RadioButton("Offline");
        onlineRadio.setToggleGroup(typeGroup);
        offlineRadio.setToggleGroup(typeGroup);
        onlineRadio.setSelected(true);
        onlineRadio.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");
        offlineRadio.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");

        HBox typeBox = new HBox(20, onlineRadio, offlineRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        Button submitButton = new Button("Submit");
        submitButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        submitButton.setOnAction(e -> {
            String date = datePicker.getValue() != null ? datePicker.getValue().format(dateFormatter) : "";
            String name = nameField.getText();
            String amount = amountField.getText().replaceAll("[^\\d.]", ""); // Clean invalid characters
            String type = onlineRadio.isSelected() ? "Online" : "Offline";

            if (!date.isEmpty() && !name.isEmpty() && !amount.isEmpty()) {
                try {
                    // Add amount validation
                    Double.parseDouble(amount);
                    transactions.add(new Transaction(date, name, amount, type));
                    updateStatsAndChart(); // Adjust to correct position
                    dialog.close();
                } catch (NumberFormatException ex) {

//                    System.err.println("Invalid amount format: " + amount);
                    amountField.setStyle("-fx-border-color: #ff0000;");
                }
            } else {
                // Add input validation prompt
                System.err.println("Please fill in all fields");
            }
        });

        VBox dialogContent = new VBox(15, datePicker, nameField, amountField, typeBox, submitButton);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(dialogContent, 300, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    /**
     * Sets up the type chart for transaction visualization.
     */
    private void setupTypeChart() {
        typeChart = new PieChart();
        typeChart.setLegendSide(Side.BOTTOM);
        typeChart.setLabelsVisible(true);
        typeChart.setClockwise(true);
        typeChart.setStartAngle(90);

        // Set chart size
        typeChart.setPrefSize(400, 400);
        typeChart.setMinSize(400, 400);
        typeChart.setMaxSize(400, 400);

        // Wrap as StackPane for center display
        StackPane chartWrapper = new StackPane(typeChart);
        chartWrapper.setPrefSize(420, 420);
        chartWrapper.setMinSize(420, 420);
        chartWrapper.setMaxSize(420, 420);
        chartWrapper.setAlignment(Pos.CENTER);

        chartContent = new HBox(20);
        chartContent.setAlignment(Pos.CENTER);
        chartContent.getChildren().addAll(chartWrapper, createStatsBox());

        // Initial chart data population
        updateTypeChart();
    }

    /**
     * Updates the type chart with current transaction data.
     */
    private void updateTypeChart() {
        double onlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Online"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double offlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Offline"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double total = onlineAmount + offlineAmount;

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
                new PieChart.Data("Online", onlineAmount),
                new PieChart.Data("Offline", offlineAmount)
        );

        Platform.runLater(() -> {
            typeChart.setData(chartData);

            for (PieChart.Data data : chartData) {
                String name = data.getName();
                double amount = data.getPieValue();
                double percent = (total == 0) ? 0 : (amount / total) * 100;

                String tooltipText = String.format(
                        "Category: %s\nAmount: %.2f\nProportion: %.1f%%",
                        name, amount, percent
                );

                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(data.getNode(), tooltip);
            }
        });
    }

    /**
     * Creates the main content area of the application.
     *
     * @param leftPanel The left panel of the application
     * @param rightPanel The right panel of the application
     * @return An HBox containing the main content
     */
    private HBox createMainContent(VBox leftPanel, VBox rightPanel) {
        HBox content = new HBox(20);
        content.setPadding(new Insets(0));  // Remove all padding
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(leftPanel, rightPanel);

        // Set panel alignment in HBox
        HBox.setHgrow(leftPanel, Priority.NEVER);
        HBox.setHgrow(rightPanel, Priority.NEVER);

        return content;
    }

    /**
     * Creates the main layout of the application.
     *
     * @param content The main content to display
     * @return A VBox containing the main layout
     */
    private VBox createMainLayout(HBox content) {
        Label pageTitle = new Label("Transaction Management");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 38));
        pageTitle.setTextFill(Color.WHITE);
        pageTitle.setEffect(new DropShadow(15, Color.web("#4c3092")));

        Label subtitle = new Label("Manage your transaction records, support categorized viewing and visual display");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        subtitle.setTextFill(Color.web("#e6d5ff"));


        HBox titleContent = new HBox(15);
        titleContent.setAlignment(Pos.CENTER);
        ImageView logo = new ImageView(new Image("expenses.png")); // Add illustration
        logo.setFitHeight(48);
        logo.setFitWidth(48);
        titleContent.getChildren().addAll(logo, new VBox(5, pageTitle, subtitle));

        VBox titleBox = new VBox(8, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);");
        titleBox.setPadding(new Insets(30, 0, 30, 0));

        // Add content wrapper to ensure content is centered with no extra margins
        VBox contentWrapper = new VBox(content);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(20, 0, 20, 0));  // Keep only top and bottom margins
        contentWrapper.setStyle("-fx-background-color: #f8f0ff;");

        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(titleBox, contentWrapper);
        mainLayout.setStyle("-fx-background-color: #f8f0ff;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        return mainLayout;
    }

    /**
     * Creates a navigation button with an emoji icon.
     *
     * @param label The button label
     * @param emoji The emoji to display
     * @return A Button with the specified label and emoji
     */
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
     * Creates a navigation button.
     *
     * @param label The button label
     * @return A Button with the specified label
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
     * The main method that launches the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}