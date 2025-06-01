//package Merge;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A JavaFX application for managing and visualizing festival budgets.
 * This application provides a user interface for tracking income and expenses
 * for various festivals, with features for data visualization and CSV import/export.
 *
 * @author Junfeng Wang
 * @version final
 */
public class UI_1 extends Application {

    private TableView<BudgetData> tableView;
    private BarChart<String, Number> barChart1;
    private BarChart<String, Number> barChart2;
    private final List<BudgetData> dataList = new ArrayList<>();
    private static final String CSV_FILE = "deals.csv";

    /**
     * The Seasonal spike function
     * Initializes and displays the main application window with all UI components.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();

        // Create main content container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #FFF0F5;");

        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(10);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(20));
        contentGrid.setStyle("-fx-background-color: #FFF0F5;");

        // Title
        Label titleLabel = new Label("Localized Budgeting");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.PURPLE);
        titleLabel.setAlignment(Pos.CENTER);
        contentGrid.add(titleLabel, 0, 0, 2, 1);

        // Left top input area
        VBox leftTopBox = createLeftTopBox();
        contentGrid.add(leftTopBox, 0, 1);

        // Right top data table
        VBox dataDisplayBox = createDataDisplayBox();
        contentGrid.add(dataDisplayBox, 1, 1);

        // Bottom chart area
        HBox chartsBox = createChartsBox();
        contentGrid.add(chartsBox, 0, 2, 2, 1);

        // Put content grid into scroll pane
        scrollPane.setContent(contentGrid);

        // Bottom navigation bar
        HBox navBar = createNavigationBar(primaryStage);
        mainLayout.setBottom(navBar);
        mainLayout.setCenter(scrollPane);

        initializeData(); // Load default data first

        Scene scene = new Scene(mainLayout, 1366, 768);
        primaryStage.setTitle("Localized Budgeting");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the navigation bar with buttons for different sections of the application.
     *
     * @param primaryStage The primary stage of the application
     * @return An HBox containing the navigation buttons
     */
    private HBox createNavigationBar(Stage primaryStage) {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
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
        return navBar;
    }

    /**
     * Creates a navigation button with an emoji icon and text label.
     *
     * @param label The text label for the button
     * @param emoji The emoji icon to display
     * @return A styled Button with emoji and text
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
     * Creates the left top input box containing festival selection and budget input fields.
     *
     * @return A VBox containing all input components
     */
    private VBox createLeftTopBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #FFF0F5; -fx-border-color: #FFC0CB; -fx-border-width: 2px;");

        ComboBox<String> festivalComboBox = new ComboBox<>();
        festivalComboBox.getItems().addAll(
                "Spring Festival",
                "Dragon Boat Festival",
                "Mid-Autumn Festival",
                "Christmas",
                "Harvest Day",
                "Others"
        );
        festivalComboBox.setPrefWidth(200);
        festivalComboBox.setEditable(true);

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        TextField incomeField = new TextField("0");
        TextField expensesField = new TextField("0");
        TextArea notesArea = new TextArea();

        box.getChildren().addAll(
                createLabel("Festival Selection *", Color.PURPLE),
                festivalComboBox,
                createNoteLabel("Choose the festival and set your preferred budget"),
                createLabel("Festival Date Range *", Color.PURPLE),
                createDateRangeBox(startDatePicker, endDatePicker),
                createNoteLabel("Choose the time range you will receive the budget"),
                createAmountBox(incomeField, expensesField),
                createNoteLabel("Enter the amount value"),
                createLabel("Notes", Color.PURPLE),
                notesArea,
                createToolbar(festivalComboBox, startDatePicker, endDatePicker, incomeField, expensesField, notesArea)
        );

        return box;
    }

    /**
     * Creates a toolbar with save functionality for the input form.
     *
     * @param comboBox The festival selection combo box
     * @param startDate The start date picker
     * @param endDate The end date picker
     * @param income The income text field
     * @param expenses The expenses text field
     * @param notes The notes text area
     * @return An HBox containing the save button
     */
    private HBox createToolbar(ComboBox<String> comboBox, DatePicker startDate, DatePicker endDate,
                               TextField income, TextField expenses, TextArea notes) {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> handleSave(comboBox, startDate, endDate, income, expenses, notes));

        toolbar.getChildren().add(saveBtn);
        return toolbar;
    }

    /**
     * Creates the data display box containing the table view and control buttons.
     *
     * @return A VBox containing the table and its controls
     */
    private VBox createDataDisplayBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        tableView = new TableView<>();

        // Table column definitions
        TableColumn<BudgetData, String> festivalCol = new TableColumn<>("Festival");
        festivalCol.setCellValueFactory(c -> c.getValue().festivalProperty());

        TableColumn<BudgetData, String> dateCol = new TableColumn<>("Date Range");
        dateCol.setCellValueFactory(c -> c.getValue().dateRangeProperty());

        TableColumn<BudgetData, Number> incomeCol = new TableColumn<>("Income");
        incomeCol.setCellValueFactory(c -> c.getValue().incomeProperty());

        TableColumn<BudgetData, Number> expensesCol = new TableColumn<>("Expenses");
        expensesCol.setCellValueFactory(c -> c.getValue().expensesProperty());

        TableColumn<BudgetData, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(c -> c.getValue().notesProperty());
        notesCol.setPrefWidth(200);

        tableView.getColumns().addAll(festivalCol, dateCol, incomeCol, expensesCol, notesCol);

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            BudgetData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dataList.remove(selected);
                refreshDataDisplay();
            } else {
                showAlert("Please select a row to delete!");
            }
        });

        // Import/Export buttons, placed to the right of delete button
        Button importBtn = new Button("Import bill");
        importBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        importBtn.setOnAction(e -> handleImportBill());

        Button exportBtn = new Button("Export bill");
        exportBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> handleExportBill());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #009688; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> refreshDataDisplay());

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.getChildren().addAll(deleteBtn, importBtn, exportBtn, refreshBtn);

        VBox container = new VBox(10);
        container.getChildren().addAll(
                createLabel("Budget Data", Color.PURPLE, 16),
                tableView,
                btnBox
        );

        return container;
    }

    /**
     * Creates the charts box containing income vs expenses and net income charts.
     *
     * @return An HBox containing the two charts
     */
    private HBox createChartsBox() {
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setPadding(new Insets(20));

        // Income vs Expenses comparison chart (vertical)
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        barChart1 = new BarChart<>(xAxis1, yAxis1);
        barChart1.setTitle("Income vs Expenses Comparison");
        xAxis1.setLabel("Festival");
        yAxis1.setLabel("Amount");
        barChart1.setCategoryGap(20);
        barChart1.setPrefSize(600, 400);

        // Income - Expenses difference chart (vertical)
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        barChart2 = new BarChart<>(xAxis2, yAxis2);
        barChart2.setTitle("Income - Expenses");
        xAxis2.setLabel("Festival");
        yAxis2.setLabel("Net Income");
        barChart2.setCategoryGap(20);
        barChart2.setPrefSize(600, 400);

        chartsBox.getChildren().addAll(barChart1, barChart2);
        return chartsBox;
    }

    /**
     * Saves the current budget data to a CSV file.
     * The data is saved to a temporary file to avoid conflicts with the main CSV file.
     */
    private void saveDataToCSV() {
        // Use local temporary file to store UI data, instead of directly operating deals.csv
        String tempFile = "ui_budget_data.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Festival,Income,Expenses,StartDate,EndDate,Notes\n");
            for (BudgetData data : dataList) {
                String notes = data.getNotes().replace("\"", "\"\"");
                if (notes.contains(",") || notes.contains("\"")) {
                    notes = "\"" + notes + "\"";
                }
                String line = String.format("%s,%d,%d,%s,%s,%s",
                        data.getFestival(),
                        data.getIncome(),
                        data.getExpenses(),
                        data.getStartDate(),
                        data.getEndDate(),
                        notes);
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            showAlert("Failed to save data to temporary file!");
        }
    }

    /**
     * Loads budget data from the CSV file.
     * Aggregates data by festival and updates the UI accordingly.
     */
    private void loadDataFromCSV() {
        dataList.clear();
        Map<String, BudgetData> aggregatedDataMap = new HashMap<>(); // 用于聚合数据

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",", -1);

                if (values.length > 1 && "Festival Budget".equals(values[1])) {
                    if (values.length > 5) {
                        String festivalName = values[2];
                        String csvDate = values[0];
                        int csvAmount = 0;
                        try {
                            String amountStr = values[5].replaceAll("[^0-9]", "");
                            if (!amountStr.isEmpty()) {
                                csvAmount = Integer.parseInt(amountStr);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }

                        String incomeOrExpenseType = values[4];
                        String csvNotes = (values.length > 10 && values[10] != null) ? values[10] : "None.";

                        BudgetData budgetEntry = aggregatedDataMap.get(festivalName);

                        if (budgetEntry == null) {
                            int currentIncome = 0;
                            int currentExpenses = 0;
                            if ("Income".equals(incomeOrExpenseType)) {
                                currentIncome = csvAmount;
                            } else if ("Expenditure".equals(incomeOrExpenseType)) {
                                currentExpenses = csvAmount;
                            }
                            budgetEntry = new BudgetData(
                                    festivalName,
                                    currentIncome,
                                    currentExpenses,
                                    csvDate, // startDate
                                    csvDate, // endDate
                                    csvNotes
                            );
                            aggregatedDataMap.put(festivalName, budgetEntry);
                        } else {
                            if ("Income".equals(incomeOrExpenseType)) {
                                budgetEntry.incomeProperty().set(budgetEntry.getIncome() + csvAmount);
                            } else if ("Expenditure".equals(incomeOrExpenseType)) {
                                budgetEntry.expensesProperty().set(budgetEntry.getExpenses() + csvAmount);
                            }
                            // Note: Date and notes are updated only when first encountered, not updated here
                        }
                    }
                }
            }
            dataList.addAll(aggregatedDataMap.values());
            refreshDataDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the application data.
     * Attempts to load data from temporary file first, then from CSV if needed,
     * and finally loads default data if no other data is available.
     */
    private void initializeData() {
        // Try to load data from local temporary data file first, then from CSV if needed,
        // and finally load default data if no other data is available.
        String tempFile = "ui_budget_data.csv";
        boolean loaded = false;

        if (new File(tempFile).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                dataList.clear();
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        String festival = parts[0];
                        int income = Integer.parseInt(parts[1]);
                        int expenses = Integer.parseInt(parts[2]);
                        String startDate = parts[3];
                        String endDate = parts[4];
                        String notes = parts[5].replace("\"\"", "\"");
                        if (notes.startsWith("\"") && notes.endsWith("\"")) {
                            notes = notes.substring(1, notes.length() - 1);
                        }
                        dataList.add(new BudgetData(festival, income, expenses, startDate, endDate, notes));
                    }
                }
                loaded = !dataList.isEmpty();
            } catch (IOException | NumberFormatException e) {
                // Read temporary file failed, continue with subsequent processing
            }
        }

        // If no data is loaded from the temporary file, try to load from CSV
        if (!loaded) {
            loadDataFromCSV();
        }

        // If still no data is loaded, load default data
        if (dataList.isEmpty()) {
            dataList.addAll(Arrays.asList(
                    new BudgetData("Spring Festival", 3000, 1900, "2024-02-10", "2024-02-17", ""),
                    new BudgetData("Dragon Boat Festival", 500, 800, "2024-06-10", "2024-06-12", ""),
                    new BudgetData("Mid-Autumn Festival", 700, 750, "2024-09-15", "2024-09-17", ""),
                    new BudgetData("Christmas", 1000, 700, "2024-12-25", "2024-12-26", ""),
                    new BudgetData("Harvest Day", 500, 800, "2024-10-01", "2024-10-07", "")
            ));
            saveDataToCSV(); // Save default data to temporary file
        }

        refreshDataDisplay();
    }

    /**
     * Updates the charts with the current festival data.
     *
     * @param festivals List of festivals to display in the charts
     */
    private void updateCharts(List<String> festivals) {
        // Only count festivals that actually exist in the current table
        updateChartCategories(festivals);

        // Clear existing data
        barChart1.getData().clear();
        barChart2.getData().clear();

        // Group data by festival and calculate income and expenses
        Map<String, Double> festivalIncome = dataList.stream()
                .collect(Collectors.groupingBy(
                        BudgetData::getFestival,
                        Collectors.summingDouble(BudgetData::getIncome)
                ));
        Map<String, Double> festivalExpenses = dataList.stream()
                .collect(Collectors.groupingBy(
                        BudgetData::getFestival,
                        Collectors.summingDouble(BudgetData::getExpenses)
                ));

        // Create income bar chart data
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenditure");
        XYChart.Series<String, Number> netSeries = new XYChart.Series<>();
        netSeries.setName("Net income");
        for (String festival : festivals) {
            double income = festivalIncome.getOrDefault(festival, 0.0);
            double expenses = festivalExpenses.getOrDefault(festival, 0.0);
            incomeSeries.getData().add(new XYChart.Data<>(festival, income));
            expensesSeries.getData().add(new XYChart.Data<>(festival, expenses));
            double net = income - expenses;
            XYChart.Data<String, Number> netData = new XYChart.Data<>(festival, net);
            netSeries.getData().add(netData);
        }
        barChart1.getData().addAll(incomeSeries, expensesSeries);
        barChart2.getData().add(netSeries);
        // Set net income bar chart colors
        for (XYChart.Data<String, Number> data : netSeries.getData()) {
            double value = data.getYValue().doubleValue();
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    if (value >= 0) {
                        newNode.setStyle("-fx-bar-fill: #2196F3;"); // Blue
                    } else {
                        newNode.setStyle("-fx-bar-fill: #e74c3c;"); // Red
                    }
                }
            });
            if (data.getNode() != null) {
                if (value >= 0) {
                    data.getNode().setStyle("-fx-bar-fill: #2196F3;");
                } else {
                    data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
                }
            }
        }
    }

    /**
     * Handles saving new budget data from the input form.
     *
     * @param festivalComboBox The festival selection combo box
     * @param startDatePicker The start date picker
     * @param endDatePicker The end date picker
     * @param incomeField The income text field
     * @param expensesField The expenses text field
     * @param notesArea The notes text area
     */
    private void handleSave(ComboBox<String> festivalComboBox,
                            DatePicker startDatePicker,
                            DatePicker endDatePicker,
                            TextField incomeField,
                            TextField expensesField,
                            TextArea notesArea) {
        try {
            String festival = festivalComboBox.getValue();
            if (festival == null || festival.trim().isEmpty()) {
                showAlert("Festival cannot be empty!");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                showAlert("Invalid date range!");
                return;
            }

            int income = Integer.parseInt(incomeField.getText());
            int expenses = Integer.parseInt(expensesField.getText());
            String notes = notesArea.getText().isEmpty() ? "None." : notesArea.getText();

            BudgetData newData = new BudgetData(
                    festival,
                    income,
                    expenses,
                    startDate.toString(),
                    endDate.toString(),
                    notes
            );

            boolean exists = dataList.stream()
                    .anyMatch(d -> d.getFestival().equalsIgnoreCase(festival));

            if (exists) {
                dataList.replaceAll(d ->
                        d.getFestival().equalsIgnoreCase(festival) ? newData : d);
            } else {
                dataList.add(newData);
            }

            refreshDataDisplay();
            saveDataToCSV(); // Save only to temporary file, not to deals.csv

            // Reset input fields
            festivalComboBox.setValue(null);
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now());
            incomeField.setText("0");
            expensesField.setText("0");
            notesArea.clear();

        } catch (NumberFormatException e) {
            showAlert("Invalid number format in income/expenses!");
        } catch (Exception e) {
            showAlert("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Refreshes the data display in the table and charts.
     */
    private void refreshDataDisplay() {
        tableView.getItems().setAll(FXCollections.observableArrayList(dataList));

        // Calculate festivals list here, ensuring it's the latest and unique source
        List<String> festivals = dataList.stream()
                .map(BudgetData::getFestival)
                .distinct()
                .collect(Collectors.toList());

        updateChartCategories(festivals); // Use latest festivals to update axis
        updateCharts(festivals); // Pass latest festivals to updateCharts
    }

    /**
     * Updates the chart categories with the current festival list.
     *
     * @param festivals List of festivals to display in the charts
     */
    private void updateChartCategories(List<String> festivals) {
        CategoryAxis xAxis1 = (CategoryAxis) barChart1.getXAxis();
        if (xAxis1.getCategories() != null) {
            xAxis1.getCategories().setAll(festivals);
        } else {
            xAxis1.setCategories(FXCollections.observableArrayList(festivals));
        }

        CategoryAxis xAxis2 = (CategoryAxis) barChart2.getXAxis();
        if (xAxis2.getCategories() != null) {
            xAxis2.getCategories().setAll(festivals);
        } else {
            xAxis2.setCategories(FXCollections.observableArrayList(festivals));
        }
    }

    /**
     * Shows an error alert dialog with the specified message.
     *
     * @param message The error message to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Creates a styled label with the specified text and color.
     *
     * @param text The text to display
     * @param color The color of the text
     * @return A styled Label
     */
    private Label createLabel(String text, Color color) {
        return createLabel(text, color, 14);
    }

    /**
     * Creates a styled label with the specified text, color, and font size.
     *
     * @param text The text to display
     * @param color The color of the text
     * @param size The font size
     * @return A styled Label
     */
    private Label createLabel(String text, Color color, int size) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(Font.font("Arial", FontWeight.BOLD, size));
        return label;
    }

    /**
     * Creates a note label with gray text.
     *
     * @param text The text to display
     * @return A styled Label
     */
    private Label createNoteLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.GRAY);
        return label;
    }

    /**
     * Creates a date range selection box with start and end date pickers.
     *
     * @param start The start date picker
     * @param end The end date picker
     * @return An HBox containing the date pickers
     */
    private HBox createDateRangeBox(DatePicker start, DatePicker end) {
        HBox box = new HBox(10);
        start.setPrefWidth(150);
        end.setPrefWidth(150);
        box.getChildren().addAll(
                new Label("Start Date:"), start,
                new Label("End Date:"), end
        );
        return box;
    }

    /**
     * Creates an amount input box with income and expenses fields.
     *
     * @param income The income text field
     * @param expenses The expenses text field
     * @return An HBox containing the input fields
     */
    private HBox createAmountBox(TextField income, TextField expenses) {
        HBox box = new HBox(10);
        income.setPrefWidth(150);
        expenses.setPrefWidth(150);
        box.getChildren().addAll(
                createLabel("Income", Color.PURPLE), income,
                createLabel("Expenses", Color.PURPLE), expenses
        );
        return box;
    }

    /**
     * Handles importing bill data from a CSV file.
     */
    private void handleImportBill() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select billing file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Copy file to program directory
                Files.copy(file.toPath(), new File(CSV_FILE).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                // Reload data
                dataList.clear(); // Clear existing data
                loadDataFromCSV(); // Reload from CSV
                showInfo("Bill imported successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Import failed：" + e.getMessage());
            }
        }
    }

    /**
     * Handles exporting bill data to a CSV file.
     */
    private void handleExportBill() {
        try {
            File csvFile = new File(CSV_FILE);
            List<String> otherLines = new ArrayList<>(); // Non-festival budget data
            List<String> header = new ArrayList<>();
            // 1. Read existing deals.csv, separate header and non-festival budget data
            if (csvFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (isHeader) {
                            header.add(line);
                            isHeader = false;
                            continue;
                        }
                        if (!line.contains("Festival Budget")) {
                            otherLines.add(line);
                        }
                    }
                }
            }
            // 2. Generate current UI festival budget data rows
            List<String> exportLines = new ArrayList<>();
            for (BudgetData data : dataList) {
                String tradeTime = data.getStartDate();
                String tradeType = "Festival Budget";
                String tradeTarget = data.getFestival();
                String product = ""; 
                String payType = "Other"; 
                String status = "Exported"; 
                String tradeNo = ""; 
                String merchantNo = ""; 
                String notes = data.getNotes() == null || data.getNotes().equals("None.") ? "" : data.getNotes();
                String finalNotes = notes.replace(",", " "); 

                //  If there is income, export one row of income data
                if (data.getIncome() > 0) {
                    String incomeOrExpense = "Income";
                    int amount = data.getIncome();
                    exportLines.add(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s",
                            tradeTime, tradeType, tradeTarget, product, incomeOrExpense,
                            amount, payType, status, tradeNo, merchantNo,
                            finalNotes));
                }

                //  If there is expenditure, export one row of expenditure data
                if (data.getExpenses() > 0) {
                    String incomeOrExpense = "Expenditure";
                    int amount = data.getExpenses();
                    exportLines.add(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s",
                            tradeTime, tradeType, tradeTarget, product, incomeOrExpense,
                            amount, payType, status, tradeNo, merchantNo,
                            finalNotes));
                }
            }
            // 3. Merge header + non-festival budget data + current UI festival budget data, write back to deals.csv
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(CSV_FILE, false), "UTF-8"))) {
                if (!header.isEmpty()) {
                    writer.write(header.get(0) + "\n");
                } else {
                    writer.write("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note\n");
                }
                for (String line : otherLines) {
                    writer.write(line + "\n");
                }
                for (String line : exportLines) {
                    writer.write(line + "\n");
                }
            }
            showInfo("Bill exported successfully! deals.csv has been synchronized with the current data。");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("current CSV path: " + CSV_FILE);
            showAlert("Bill export failed：" + ex.getMessage());
        }
    }

    /**
     * Shows an information alert dialog with the specified message.
     *
     * @param message The message to display
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prompt");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Inner class representing budget data for a festival.
     * Contains properties for festival name, income, expenses, dates, and notes.
     */
    public static class BudgetData {
        private final SimpleStringProperty festival;
        private final SimpleIntegerProperty income;
        private final SimpleIntegerProperty expenses;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleStringProperty dateRange;
        private final SimpleStringProperty notes;

        /**
         * Creates a new BudgetData instance.
         *
         * @param festival The name of the festival
         * @param income The income amount
         * @param expenses The expenses amount
         * @param startDate The start date
         * @param endDate The end date
         * @param notes Additional notes
         */
        public BudgetData(String festival, int income, int expenses,
                          String startDate, String endDate, String notes) {
            this.festival = new SimpleStringProperty(festival);
            this.income = new SimpleIntegerProperty(income);
            this.expenses = new SimpleIntegerProperty(expenses);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.dateRange = new SimpleStringProperty(startDate + " - " + endDate);
            this.notes = new SimpleStringProperty(notes.isEmpty() ? "None." : notes);
        }

        // Property getters
        /**
         * @return The festival property
         */
        public SimpleStringProperty festivalProperty() { return festival; }

        /**
         * @return The income property
         */
        public SimpleIntegerProperty incomeProperty() { return income; }

        /**
         * @return The expenses property
         */
        public SimpleIntegerProperty expensesProperty() { return expenses; }

        /**
         * @return The date range property
         */
        public SimpleStringProperty dateRangeProperty() { return dateRange; }

        /**
         * @return The notes property
         */
        public SimpleStringProperty notesProperty() { return notes; }

        // Value getters
        /**
         * @return The festival name
         */
        public String getFestival() { return festival.get(); }

        /**
         * @return The income amount
         */
        public int getIncome() { return income.get(); }

        /**
         * @return The expenses amount
         */
        public int getExpenses() { return expenses.get(); }

        /**
         * @return The start date
         */
        public String getStartDate() { return startDate.get(); }

        /**
         * @return The end date
         */
        public String getEndDate() { return endDate.get(); }

        /**
         * @return The notes
         */
        public String getNotes() { return notes.get(); }
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}