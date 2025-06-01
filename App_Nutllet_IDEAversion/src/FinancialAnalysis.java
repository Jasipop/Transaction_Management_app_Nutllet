//package Merge;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.net.URI;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.concurrent.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

/**
 * A JavaFX application for personal financial analysis and visualization.
 * This application provides interactive charts and analysis of financial transactions,
 * including spending trends, category breakdowns, and AI-powered recommendations.
 *
 * @author Jiachen Hou
 * @version final
 */
public class FinancialAnalysis extends Application {

    private final String lineColor = "#855FAF";
    private final String barColor = "#855FAF";
    private final String[] pieColors = {"#855FAF", "#CEA3ED", "#7D4B79", "#F05865", "#36344C"};
    private final String backgroundColor = "#FFD4EC54";

    /** List to store all financial transactions */
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the financial analysis dashboard.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        start(primaryStage, false);
    }

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the financial analysis dashboard.
     *
     * @param primaryStage The primary stage for this application
     * @param scrollToBottom Whether to scroll to bottom after showing the window
     */
    public void start(Stage primaryStage, boolean scrollToBottom) {
        // Load data from CSV first
        loadTransactionData();

        BorderPane root = new BorderPane();
        VBox mainContainer = new VBox();
        mainContainer.setPadding(new Insets(20));
        mainContainer.setSpacing(20);
        mainContainer.setStyle("-fx-background-color: " + backgroundColor + ";");

        Text title = new Text("Financial Analysis -- Personal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        title.setOpacity(0);
        title.setScaleX(0.5);
        title.setScaleY(0.5);

        Button pageButton = new Button("Go to Enterprise Edition");
        pageButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pageButton.setStyle("-fx-background-color: " + lineColor + "; -fx-text-fill: white; -fx-font-size: 18px;");
        pageButton.setOnAction(e -> {
            try { new EP_FinancialAnalysis().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        ParallelTransition titleAnimation = new ParallelTransition(
                new FadeTransition(Duration.seconds(1), title),
                new ScaleTransition(Duration.seconds(1), title)
        );
        ((FadeTransition)titleAnimation.getChildren().get(0)).setToValue(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToX(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToY(1);

        // Build charts with actual data
        LineChart<Number, Number> spendingTrendChart = buildSpendingTrendChart();
        BarChart<String, Number> categorySpendingChart = buildCategorySpendingChart();
        PieChart paymentMethodChart = buildCategoryChart();

        Label trendTitle = new Label("Monthly Spending Trend");
        trendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        trendTitle.setTextFill(Color.web("#855FAF"));

        Label categoryTitle = new Label("Spending by Category");
        categoryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        categoryTitle.setTextFill(Color.web("#855FAF"));

        Label methodTitle = new Label("Payment Methods");
        methodTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        methodTitle.setTextFill(Color.web("#855FAF"));

        WebView webView = new WebView();
        webView.getEngine().loadContent(generateFinancialAnalysis());
        webView.setPrefHeight(400);  // Set appropriate height

        // Replace original WebView and aiButton section
        TextArea aiContent = new TextArea();
        aiContent.setEditable(false);
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-background-color: white; -fx-text-fill: #666666; -fx-font-size: 14px;");
        aiContent.setPrefHeight(180);
        aiContent.setText("AI consumption analysis suggestions will be displayed here...");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        StackPane aiPane = new StackPane(aiContent, progress);

        Button aiButton = new Button("More Recommendations");
        aiButton.setStyle("-fx-background-color: " + lineColor + "; -fx-text-fill: white; -fx-font-size: 16px;");

        ScaleTransition scaleBtn = new ScaleTransition(Duration.millis(200), aiButton);
        aiButton.setOnMouseEntered(e -> {
            scaleBtn.setToX(1.05);
            scaleBtn.setToY(1.05);
            scaleBtn.play();
        });
        aiButton.setOnMouseExited(e -> {
            scaleBtn.setToX(1.0);
            scaleBtn.setToY(1.0);
            scaleBtn.play();
        });

        // Modify button click event
        aiButton.setOnAction(e -> getAIRecommendations(aiContent, progress));

        // Automatically run AI analysis once after window is shown
        primaryStage.setOnShown(e -> {
            getAIRecommendations(aiContent, progress);
        });

        VBox contentContainer = new VBox(30);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setStyle("-fx-background-color: " + backgroundColor + ";");
        contentContainer.setOpacity(0);

        contentContainer.getChildren().addAll(title, pageButton, spendingTrendChart,
                categoryTitle, categorySpendingChart, methodTitle, paymentMethodChart,
                webView, aiPane, aiButton);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Node node : contentContainer.getChildren()) {
            if (node instanceof Chart || node instanceof Label || node instanceof Text) {
                node.setOpacity(0);
                node.setTranslateY(20);

                ParallelTransition pt = new ParallelTransition(
                        new FadeTransition(Duration.seconds(0.5), node),
                        new TranslateTransition(Duration.seconds(0.5), node)
                );
                ((FadeTransition)pt.getChildren().get(0)).setFromValue(0);
                ((FadeTransition)pt.getChildren().get(0)).setToValue(1);
                ((TranslateTransition)pt.getChildren().get(1)).setFromY(20);
                ((TranslateTransition)pt.getChildren().get(1)).setToY(0);

                sequentialTransition.getChildren().add(pt);
            }
        }

        ParallelTransition allAnimations = new ParallelTransition(fadeIn, sequentialTransition, titleAnimation);
        allAnimations.play();

        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);

        mainContainer.getChildren().add(scrollPane);
        root.setCenter(mainContainer);

        // Bottom Navigation Bar
        HBox bottomNavigationBar = new HBox();
        bottomNavigationBar.setSpacing(0);
        bottomNavigationBar.setAlignment(Pos.CENTER);
        bottomNavigationBar.setPrefHeight(80);
        bottomNavigationBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeButton = createNavButtonWithEmoji("Home", "🏠");
        Button discoverButton = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsButton = createNavButtonWithEmoji("Settings", "⚙");

        homeButton.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverButton.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsButton.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        bottomNavigationBar.getChildren().addAll(homeButton, discoverButton, settingsButton);
        root.setBottom(bottomNavigationBar);

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Financial Analysis");
        primaryStage.show();

        // Scroll to bottom only if specified
        Platform.runLater(() -> {
            scrollPane.setVvalue(scrollToBottom ? 1.0 : 0.0);
            scrollPane.layout();
        });
    }

    /**
     * Loads transaction data from a CSV file.
     * The CSV file should be named "deals.csv" and contain transaction records.
     */
    private void loadTransactionData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    if (line.startsWith("Transaction Time")) {
                        headerSkipped = true;
                    }
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                // Parse CSV line with quotes
                String[] parts = parseCsvLine(line);
                if (parts.length >= 7) {
                    try {
                        LocalDate date = LocalDate.parse(parts[0].substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String type = parts[1];
                        String counterpart = parts[2];
                        String product = parts[3];
                        String direction = parts[4];
                        double amount = Double.parseDouble(parts[5].replace("¥", "").replace(",", ""));
                        String paymentMethod = parts[6];
                        String status = parts.length > 7 ? parts[7] : "";

                        transactions.add(new Transaction(date, type, counterpart, product, direction, amount, paymentMethod, status));
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses a CSV line, handling quoted values correctly.
     *
     * @param line The CSV line to parse
     * @return Array of strings containing the parsed values
     */
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString());
        return values.toArray(new String[0]);
    }

    /**
     * Builds a line chart showing daily spending trends.
     *
     * @return A LineChart object displaying spending trends
     */
    private LineChart<Number, Number> buildSpendingTrendChart() {
        NumberAxis xAxis = new NumberAxis(9, 19, 1);
        xAxis.setLabel("Date");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (¥)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Daily Spending Trend - APRIL (4.10-4.19)");
        chart.setLegendVisible(false);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // Group transactions by day of month
        Map<Integer, Double> dailySpending = new TreeMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                int day = t.date.getDayOfMonth();
                if (t.date.isBefore(LocalDate.of(2025, 4, 9)) || t.date.isAfter(LocalDate.of(2025, 4, 19))) continue;
                dailySpending.put(day, dailySpending.getOrDefault(day, 0.0) + t.amount);
            }
        }

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        for (Map.Entry<Integer, Double> entry : dailySpending.entrySet()) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Day: " + entry.getKey() + "\nAmount: ¥" + String.format("%.2f", entry.getValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<Number, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + lineColor + ", white;");
            }
        }

        Node line = chart.lookup(".chart-series-line");
        if (line != null) {
            line.setStyle("-fx-stroke: " + lineColor + "; -fx-stroke-width: 2px;");
        }

        return chart;
    }


    /**
     * Builds a bar chart showing spending by category.
     *
     * @return A BarChart object displaying category-wise spending
     */
    private BarChart<String, Number> buildCategorySpendingChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (¥)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Spending by Category");
        chart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        chart.setLegendVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // Group transactions by category (counterpart)
        Map<String, Double> categorySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                String category = t.counterpart;
                categorySpending.put(category, categorySpending.getOrDefault(category, 0.0) + t.amount);
            }
        }

        // Sort by amount descending
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categorySpending.entrySet());
        sortedCategories.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take top 10 categories
        int limit = Math.min(10, sortedCategories.size());
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedCategories.get(i);
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Category: " + entry.getKey() + "\nAmount: ¥" + String.format("%.2f", entry.getValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<String, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + barColor + ";");
            }
        }

        return chart;
    }



    /**
     * Builds a pie chart showing spending distribution by category.
     *
     * @return A PieChart object displaying category distribution
     */
    private PieChart buildCategoryChart() {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            String category = categorizeExpense(t);
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + t.amount);
        }

        PieChart chart = new PieChart();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        chart.setTitle("Spending by Category");
        return chart;
    }

    /**
     * Categorizes a transaction into predefined expense categories.
     *
     * @param t The transaction to categorize
     * @return The category name for the transaction
     */
    private String categorizeExpense(Transaction t) {
        String counterpart = t.counterpart.toLowerCase();
        String product = t.product.toLowerCase();

        if (counterpart.contains("meituan") || product.contains("meal") || counterpart.contains("canteen") || product.contains("tea")) return "Food";
        if (counterpart.contains("didi") || counterpart.contains("gasoline") || counterpart.contains("oil") || product.contains("transport")) return "Transport";
        if (counterpart.contains("cinema") || product.contains("game") || counterpart.contains("rest") || counterpart.contains("apple")) return "Entertainment";
        if (counterpart.contains("supermarket") || product.contains("daily necessities") || counterpart.contains("dingdong") || counterpart.contains("jd")) return "Living";
        if (product.contains("canon")|| product.contains("ulanzi")|| product.contains("beiyang")|| product.contains("filter")|| product.contains("godox")|| product.contains("battery")|| product.contains("camera")) return "Photographic equipment";
        if (product.contains("member")) return "Subscription";
        if (product.contains("fee")|| product.contains("labor")) return "Wage";
        if (counterpart.contains("foreign")) return "Foreign currency";
        if (counterpart.contains("festival")|| counterpart.contains("christmas")|| counterpart.contains("kfc")|| counterpart.contains("day")) return "Festival currency";
        if (counterpart.contains("reimbursement")) return "Reimbursement";
        if (product.contains("transfer") || product.contains("red envelope")) return "Social";
        if (product.contains("bank") || product.contains("financing")) return "Finance";
        if (product.contains("health") || product.contains("medicine")) return "Health";
        if (product.contains("education") || product.contains("tuition")) return "Education";
        if (product.contains("rental") || product.contains("rent")) return "Housing";
        if (counterpart.contains("meituan") || counterpart.contains("restaurant") || counterpart.contains("canteen") || product.contains("meal") || product.contains("tea") || counterpart.contains("nongfu spring")|| counterpart.contains("yuxi technology")) {
            return "Catering";
        } else if (counterpart.contains("didi") || counterpart.contains("petrochemical") || counterpart.contains("gasoline") || counterpart.contains("yikatong") || counterpart.contains("ctrip")) {
            return "Traffic";
        } else if (counterpart.contains("cinema") || product.contains("game") || counterpart.contains("rest") || counterpart.contains("apple")|| product.contains("apple")) {
            return "Entertainment";
        } else if (counterpart.contains("supermarket") || product.contains("daily necessities") || counterpart.contains("dingdong") || counterpart.contains("jd")) {
            return "Living";
        }else if (product.contains("member")) {
            return "Periodic";
        }else if (product.contains("transfer") || product.contains("/")) {
            return "Social";
        }else if (product.contains("bank")) {
            return "Funds flow";
        }else {
            return "Other";
        }
    }


    /**
     * Retrieves formatted transaction data for analysis.
     *
     * @return A formatted string containing transaction details
     */
    private String getTransactionsForAnalysis() {
        return transactions.stream()
                .filter(t -> "Expenditure".equals(t.direction))
                .map(t -> String.format("[%s] %s - ¥%.2f (%s)",
                        t.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        t.counterpart,
                        t.amount,
                        t.product))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Formats the AI response for better readability.
     *
     * @param raw The raw AI response string
     * @return Formatted response string
     */
    private String formatAIResponse(String raw) {
        return raw.replaceAll("(?m)^\\s*\\d+\\.?", "\n●")
                .replaceAll("\n+", "\n")
                .replaceAll("(\\p{Lu}):", "\n$1：")
                .trim();
    }

    /**
     * Retrieves AI-powered financial recommendations based on transaction data.
     *
     * @param aiContent The TextArea to display the recommendations
     * @param progress The ProgressIndicator to show loading state
     */
    private void getAIRecommendations(TextArea aiContent, ProgressIndicator progress) {
        aiContent.setText("Analying data...");
        progress.setVisible(true);

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                Process process = new ProcessBuilder(
                        "ollama", "run", "qwen2:1.5b"
                ).start();

                String prompt = "Please analyze the following transaction records and provide professional advice in Chinese:\n" +
                        getTransactionsForAnalysis() +
                        "\nPlease respond in the following format:\n" +
                        "1. Summary of spending trends (no more than 100 characters)\n" +
                        "2. Three optimization suggestions (each prefixed with ●)\n" +
                        "3. Risk warnings (if any)";

                OutputStream stdin = process.getOutputStream();
                stdin.write(prompt.getBytes());
                stdin.flush();
                stdin.close();

                InputStream stdout = process.getInputStream();
                StringBuilder analysis = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stdout.read(buffer)) != -1) {
                    analysis.append(new String(buffer, 0, bytesRead));
                }

                return formatAIResponse(analysis.toString());
            }
        };

        aiTask.setOnSucceeded(e -> {
            aiContent.setText(aiTask.getValue());
            progress.setVisible(false);
        });

        aiTask.setOnFailed(e -> {
            aiContent.setText("Fail to Analyze: " + aiTask.getException().getMessage());
            progress.setVisible(false);
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(aiTask);
        executor.shutdown();
    }


    /**
     * Generates HTML content for financial analysis display.
     *
     * @return HTML string containing financial analysis
     */
    private String generateFinancialAnalysis() {
        double totalSpending = transactions.stream()
                .filter(t -> "Expenditure".equals(t.direction))
                .mapToDouble(t -> t.amount)
                .sum();

        double totalIncome = transactions.stream()
                .filter(t -> "Income".equals(t.direction))
                .mapToDouble(t -> t.amount)
                .sum();

        Optional<Transaction> largestExpense = transactions.stream()
                .filter(t -> "Expenditure".equals(t.direction))
                .max(Comparator.comparingDouble(t -> t.amount));

        // Create HTML format analysis report
        StringBuilder html = new StringBuilder();
        html.append("""
        <html>
        <head>
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    background-color: #FFD4EC54;
                    color: #855FAF;
                    padding: 20px;
                    line-height: 1.6;
                }
                h1 { color: #855FAF; border-bottom: 2px solid #CEA3ED; padding-bottom: 10px; }
                h2 { color: #6a3093; margin-top: 20px; }
                ul { padding-left: 20px; }
                li { margin-bottom: 8px; }
                strong { color: #d14; }
                .highlight { background-color: #F0E6FF; padding: 2px 5px; border-radius: 3px; }
            </style>
        </head>
        <body>
        <h1>Financial Analysis</h1>
        
        <h2>Summary</h2>
        <ul>
            <li><strong>Total Income</strong>: ¥%.2f</li>
            <li><strong>Total Spending</strong>: ¥%.2f</li>
            <li><strong>Net Balance</strong>: <span class="highlight">¥%.2f</span></li>
        </ul>
        """.formatted(totalIncome, totalSpending, (totalIncome - totalSpending)));

        // Add largest expense
        if (largestExpense.isPresent()) {
            Transaction t = largestExpense.get();
            html.append("""
            <h2>Spending Highlights</h2>
            <ul>
                <li><strong>Largest Expense</strong>: ¥%.2f
                    <ul>
                        <li><em>Where</em>: %s</li>
                        <li><em>When</em>: %s</li>
                        <li><em>Category</em>: %s</li>
                    </ul>
                </li>
            </ul>
            """.formatted(t.amount, t.counterpart, t.date.toString(), categorizeExpense(t)));
        }

        // Add other analysis
        html.append("""
        <h2>Trends</h2>
        <ul>
            <li><strong>Most Active Day</strong>: %s</li>
            <li><strong>Top Category</strong>: %s</li>
            <li><strong>Primary Payment Method</strong>: %s</li>
        </ul>
        """.formatted(getMostSpendingDay(), getTopSpendingCategory(), getPrimaryPaymentMethod()));

        // Add category breakdown
        html.append("<h2>Category Breakdown</h2><ul>");
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                String category = categorizeExpense(t);
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + t.amount);
            }
        }

        categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = (entry.getValue() / totalSpending) * 100;
                    html.append(String.format("<li><strong>%s</strong>: ¥%.2f (%.1f%%)</li>",
                            entry.getKey(), entry.getValue(), percentage));
                });
        html.append("</ul>");

        html.append("""
        <h2>AI Recommendations</h2>
        <div id="aiRecommendations" style="background-color: white; padding: 15px; border-radius: 8px;">
            <p>AI recommendations shown below:</p>
        </div>
        </body>
        </html>
        """);

        return html.toString();
    }


    /**
     * Gets the day with the highest spending.
     *
     * @return String representing the day with highest spending
     */
    private String getMostSpendingDay() {
        Map<Integer, Double> dailySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                int day = t.date.getDayOfMonth();
                dailySpending.put(day, dailySpending.getOrDefault(day, 0.0) + t.amount);
            }
        }

        return dailySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> "day " + e.getKey())
                .orElse("unknown day");
    }

    /**
     * Gets the category with the highest spending.
     *
     * @return String representing the top spending category
     */
    private String getTopSpendingCategory() {
        Map<String, Double> categorySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                categorySpending.put(t.counterpart, categorySpending.getOrDefault(t.counterpart, 0.0) + t.amount);
            }
        }

        return categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Gets the primary payment method used in transactions.
     *
     * @return String representing the primary payment method
     */
    private String getPrimaryPaymentMethod() {
        Map<String, Double> methodSpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("Expenditure".equals(t.direction)) {
                methodSpending.put(t.paymentMethod, methodSpending.getOrDefault(t.paymentMethod, 0.0) + t.amount);
            }
        }

        return methodSpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }



    /**
     * Creates a navigation button with an emoji icon.
     *
     * @param labelText The text to display on the button
     * @param emojiSymbol The emoji to display as an icon
     * @return A styled Button with emoji and text
     */
    private Button createNavButtonWithEmoji(String labelText, String emojiSymbol) {
        VBox buttonContent = new VBox();
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setSpacing(2);

        Label emojiLabel = new Label(emojiSymbol);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label textLabel = new Label(labelText);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        buttonContent.getChildren().addAll(emojiLabel, textLabel);

        Button navigationButton = new Button();
        navigationButton.setPrefWidth(456);
        navigationButton.setPrefHeight(80);
        navigationButton.setGraphic(buttonContent);
        navigationButton.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        ScaleTransition scaleNavBtn = new ScaleTransition(Duration.millis(150), navigationButton);
        navigationButton.setOnMouseEntered(e -> {
            scaleNavBtn.setToX(1.03);
            scaleNavBtn.setToY(1.03);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 18px;");
            textLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #855FAF;");
        });

        navigationButton.setOnMouseExited(e -> {
            scaleNavBtn.setToX(1.0);
            scaleNavBtn.setToY(1.0);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 16px;");
            textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        });

        return navigationButton;
    }

    /**
     * The main method that launches the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Represents a financial transaction with its details.
     */
    private static class Transaction {
        LocalDate date;
        String type;
        String counterpart;
        String product;
        String direction;
        double amount;
        String paymentMethod;
        String status;

        /**
         * Constructs a new Transaction with the specified details.
         *
         * @param date The date of the transaction
         * @param type The type of transaction
         * @param counterpart The counterparty involved
         * @param product The product or service involved
         * @param direction The direction of the transaction
         * @param amount The amount of the transaction
         * @param paymentMethod The payment method used
         * @param status The status of the transaction
         */
        public Transaction(LocalDate date, String type, String counterpart, String product,
                           String direction, double amount, String paymentMethod, String status) {
            this.date = date;
            this.type = type;
            this.counterpart = counterpart;
            this.product = product;
            this.direction = direction;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.status = status;
        }
    }
}