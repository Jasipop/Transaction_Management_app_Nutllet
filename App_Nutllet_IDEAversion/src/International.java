//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;

/**
 * The International class represents a JavaFX application for managing international currency transactions.
 * It provides functionality for recording and managing foreign exchange transactions with support for
 * multiple currencies and real-time exchange rate calculations.
 *
 * <p>The application features:
 * <ul>
 *     <li>Currency selection for both local and foreign currencies</li>
 *     <li>Real-time exchange rate calculations</li>
 *     <li>Transaction history recording</li>
 *     <li>Date-based exchange rate tracking</li>
 * </ul>
 *
 * @author Chang Liu
 * @version final
 */
public class International extends Application {

    /** Map storing exchange rates for different dates and currency pairs */
    private static final Map<LocalDate, Map<String, Double>> dateRatesMap = new HashMap<>();

    static {
        loadExchangeRates();
    }

    /**
     * Inner class representing currency pair information with normalized pair name and divisor.
     */
    private static class CurrencyPairInfo {
        /** Normalized currency pair name */
        String normalizedPair;
        /** Divisor for rate calculation */
        double divisor;

        /**
         * Constructs a new CurrencyPairInfo with specified pair and divisor.
         *
         * @param pair The currency pair name
         * @param divisor The divisor for rate calculation
         */
        CurrencyPairInfo(String pair, double divisor) {
            this.normalizedPair = pair;
            this.divisor = divisor;
        }
    }

    /**
     * Initializes and starts the JavaFX application.
     * Creates the main UI with currency selection, amount input, and transaction recording capabilities.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(25, 30, 25, 30));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        // Back button
        Button backButton = new Button("← Back");
        backButton.setStyle(
                "-fx-background-color: #855FAF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Data");
            alert.setHeaderText("Exit without saving?");
            alert.setContentText("Leaving now will discard the current international transaction form. Are you sure you want to go back?");

            ButtonType yes = new ButtonType("Yes");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yes, cancel);

            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    try {
                        new InternationalList().start(new Stage());
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.TOP_LEFT);

        // Title
        Text title = new Text("Add New International Transactions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));

        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(15, 0, 15, 0));

        // Required fields hint
        Text requiredHint = new Text("* Required fields");
        requiredHint.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        requiredHint.setFill(Color.web("#e74c3c"));

        // Local currency
        Label localCurrencyLabel = new Label("Local currency *");
        localCurrencyLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        ComboBox<String> localCurrencyCombo = new ComboBox<>();
        localCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "AUD", "NZD","SGD","CHF","CAD","MOP","MYR","RUB","ZAR","KRW","AED","SAR","HUF","PLN","DKK","SEK","NOK","TRY","MXN","THB");
        localCurrencyCombo.setValue("CNY");
        localCurrencyCombo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        localCurrencyCombo.setPrefWidth(500);

        // Foreign currency
        Label foreignCurrencyLabel = new Label("Foreign currency *");
        foreignCurrencyLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        ComboBox<String> foreignCurrencyCombo = new ComboBox<>();
        foreignCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "AUD", "NZD","SGD","CHF","CAD","MOP","MYR","RUB","ZAR","KRW","AED","SAR","HUF","PLN","DKK","SEK","NOK","TRY","MXN","THB");
        foreignCurrencyCombo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        foreignCurrencyCombo.setPromptText("Click here to input the kind of foreign currency");
        foreignCurrencyCombo.setPrefWidth(500);

        // Foreign currency amount
        Label amountLabel = new Label("Amount in foreign currency *");
        amountLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        TextField amountField = new TextField();
        amountField.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        amountField.setPromptText("Click here to input the amount in foreign currency");
        amountField.setPrefWidth(500);

        // Trading time
        Label timeLabel = new Label("Trading time *");
        timeLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        DatePicker timePicker = new DatePicker();
        timePicker.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        timePicker.setPromptText("Click here to select the trading time");
        timePicker.setPrefWidth(500);

        // Add form elements to the grid
        formGrid.add(requiredHint, 0, 0, 2, 1);
        formGrid.add(localCurrencyLabel, 0, 1);
        formGrid.add(localCurrencyCombo, 1, 1);
        formGrid.add(foreignCurrencyLabel, 0, 2);
        formGrid.add(foreignCurrencyCombo, 1, 2);
        formGrid.add(amountLabel, 0, 3);
        formGrid.add(amountField, 1, 3);
        formGrid.add(timeLabel, 0, 4);
        formGrid.add(timePicker, 1, 4);

        // Button area
        HBox buttonBox = new HBox(15);
        Button clearButton = new Button("Clear all");
        clearButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        buttonBox.getChildren().addAll(new Node[]{clearButton, confirmButton});
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Add all components to the main layout
        mainLayout.getChildren().addAll(
                backBox,
                title,
                formGrid,
                buttonBox
        );

        // Set scene and stage
        Scene scene = new Scene(mainLayout, 1366,768);
        primaryStage.setTitle("International Transaction Recorder");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button event processing
        clearButton.setOnAction(e -> {
            foreignCurrencyCombo.setValue(null);
            amountField.clear();
            timePicker.setValue(null);
        });

        confirmButton.setOnAction(e -> {
            // 1. Get user input
            String localCurrency = localCurrencyCombo.getValue();
            String foreignCurrency = foreignCurrencyCombo.getValue();
            String amountText = amountField.getText();
            LocalDate date = timePicker.getValue();

            if (date == null) {
                showAlert("Error", "Please select a date!");
                return;
            }

            String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 2. Validate input
            if (localCurrency == null || foreignCurrency == null ||
                    amountText.isEmpty()) {
                showAlert("Error", "Please fill all required fields!");
                return;
            }

            try {
                double foreignAmount = Double.parseDouble(amountText);
                double exchangeRate = getExchangeRate(foreignCurrency, localCurrency, formattedDate);
                double localAmount = foreignAmount * exchangeRate;

                // Modify save format, include foreign currency amount in foreign exchange description
                try (FileWriter writer = new FileWriter("deals.csv", true)) {
                    String record = String.format(
                            "\"%s 00:00:00\",\"International Transactions\",\"Foreign Exchange Trading\",\"%sExchange(%.2f)\",\"Expenditure\",\"¥%.2f\",\"Change\",\"Payment successful\",\"\",\"\",\"\"",
                            formattedDate, foreignCurrency, foreignAmount, localAmount
                    );
                    writer.write("\n" + record);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to save transaction: " + ex.getMessage());
                    return;
                }

                //  Return to the list page
                try {
                    new InternationalList().start(new Stage());
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount format!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to process transaction: " + ex.getMessage());
            }
        });
    }

    /**
     * Retrieves the exchange rate between two currencies for a specific date.
     * Supports direct rates, reverse rates, and CNY-based cross rates.
     *
     * @param fromCurrency The source currency code
     * @param toCurrency The target currency code
     * @param dateStr The date string in yyyy-MM-dd format
     * @return The exchange rate between the currencies
     * @throws Exception If the exchange rate cannot be determined
     */
    private double getExchangeRate(String fromCurrency, String toCurrency, String dateStr) throws Exception {
        // Parameter normalization
        fromCurrency = fromCurrency.toUpperCase().trim();
        toCurrency = toCurrency.toUpperCase().trim();
        if (fromCurrency.equals(toCurrency)) return 1.0;

        // Date parsing
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new Exception("Date format error, please use yyyy-MM-dd format");
        }

        // Get the exchange rate for the day
        Map<String, Double> rates = dateRatesMap.get(date);
        if (rates == null) {
            throw new Exception("Exchange rate data Without" + dateStr);
        }

        // Direct matching
        String directKey = fromCurrency + "/" + toCurrency;
        if (rates.containsKey(directKey)) {
            return rates.get(directKey);
        }

        // Reverse matching
        String reverseKey = toCurrency + "/" + fromCurrency;
        if (rates.containsKey(reverseKey)) {
            return 1.0 / rates.get(reverseKey);
        }

        // CNY intermediate logic
        boolean fromIsCNY = fromCurrency.equals("CNY");
        boolean toIsCNY = toCurrency.equals("CNY");

        // Case 1: From CNY to other currencies
        if (fromIsCNY) {
            String cnyToTarget = "CNY/" + toCurrency;
            if (rates.containsKey(cnyToTarget)) {
                return rates.get(cnyToTarget);
            }
            String targetToCNY = toCurrency + "/CNY";
            if (rates.containsKey(targetToCNY)) {
                return 1.0 / rates.get(targetToCNY);
            }
        }

        // Case 2: From other currencies to CNY
        if (toIsCNY) {
            String sourceToCNY = fromCurrency + "/CNY";
            if (rates.containsKey(sourceToCNY)) {
                return rates.get(sourceToCNY);
            }
            String cnyToSource = "CNY/" + fromCurrency;
            if (rates.containsKey(cnyToSource)) {
                return 1.0 / rates.get(cnyToSource);
            }
        }

        // Case 3: Cross rate through CNY
        String fromToCNY = fromCurrency + "/CNY";
        String cnyToTarget = "CNY/" + toCurrency;
        if (rates.containsKey(fromToCNY) && rates.containsKey(cnyToTarget)) {
            return rates.get(fromToCNY) * rates.get(cnyToTarget);
        }

        String fromToCNY2 = fromCurrency + "/CNY";
        String targetToCNY = toCurrency + "/CNY";
        if (rates.containsKey(fromToCNY2) && rates.containsKey(targetToCNY)) {
            return rates.get(fromToCNY2) / rates.get(targetToCNY);
        }

        throw new Exception("Unable to convert:" + fromCurrency + "→" + toCurrency);
    }

    /**
     * Loads historical exchange rate data from a CSV file.
     * Processes the data and stores it in the dateRatesMap for quick access.
     */
    private static void loadExchangeRates() {
        String filename = "Historical_data.csv";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            List<String> headers = new ArrayList<>();
            Map<String, CurrencyPairInfo> columnMap = new HashMap<>();
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll("\uFEFF", ""); // 彻底清除BOM
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    // Smart column header processing
                    String[] rawHeaders = line.split(",");
                    for (String header : rawHeaders) {
                        String processedHeader = header.trim();
                        if (processedHeader.equals("date")) {
                            headers.add(processedHeader);
                            continue;
                        }

                        // Special column processing (e.g., 100JPY/CNY)
                        if (processedHeader.startsWith("100")) {
                            String normalized = processedHeader.substring(3);
                            columnMap.put(processedHeader,
                                    new CurrencyPairInfo(normalized.toUpperCase(), 100.0));
                        } else {
                            columnMap.put(processedHeader,
                                    new CurrencyPairInfo(processedHeader.toUpperCase(), 1.0));
                        }
                        headers.add(processedHeader);
                    }
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",", -1); // 保留空值
                if (values.length < 2) continue;

                // Date parsing
                LocalDate date;
                try {
                    date = LocalDate.parse(values[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    System.err.println("Date resolution failed: " + values[0]);
                    continue;
                }

                // Exchange rate parsing
                Map<String, Double> rateMap = new HashMap<>();
                for (int i = 1; i < headers.size(); i++) {
                    if (i >= values.length) break;
                    String rawValue = values[i].trim();
                    if (rawValue.isEmpty()) continue;

                    String columnName = headers.get(i);
                    if (columnName.equals("date")) continue;

                    try {
                        double value = Double.parseDouble(rawValue);
                        CurrencyPairInfo info = columnMap.get(columnName);
                        if (info != null) {
                            double adjustedValue = value / info.divisor;
                            rateMap.put(info.normalizedPair, adjustedValue);
                        }
                    } catch (NumberFormatException e) {
                        System.err.printf("Numeric parsing error [column:%s value:%s]%n", columnName, rawValue);
                    }
                }
                dateRatesMap.put(date, rateMap);
            }
        } catch (IOException e) {
            System.err.println("Fatal error: failed to load exchange rate file");
            throw new RuntimeException("Unable to load exchange rate file: " + filename, e);
        }
    }

    /**
     * Parses a date string using multiple supported formats.
     * Supports various date formats including yyyy-M-d, yyyy/MM/dd, etc.
     *
     * @param dateStr The date string to parse
     * @return The parsed LocalDate object
     * @throws DateTimeParseException If the date string cannot be parsed
     */
    private static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        String[] patterns = {
                "yyyy-M-d",
                "yyyy/MM/dd",
                "yyyy年M月d日",
                "yyyyMMdd"
        };

        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {}
        }
        throw new DateTimeParseException("Unable to parse date format", dateStr, 0);
    }

    /**
     * Displays an alert dialog with the specified title and message.
     *
     * @param title The title of the alert
     * @param message The message to display
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * The main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}