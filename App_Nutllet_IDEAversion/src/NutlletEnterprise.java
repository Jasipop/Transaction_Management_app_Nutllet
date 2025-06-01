//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.effect.DropShadow;

/**
 * NutlletEnterprise is a JavaFX application for enterprise financial management.
 * It provides features such as:
 * - Importing and displaying enterprise transaction records
 * - Visualizing revenue and expenditure with charts
 * - Comparing personal and corporate expenditures
 * - Sidebar with financial analysis and AI suggestions
 *
 * @author Simeng Lyu
 * @version final
 */
public class NutlletEnterprise extends Application {
    // Color definitions
    private static final Color PRIMARY_COLOR = Color.web("#1A94BC");// #1A94BC
    private static final Color SUCCESS_COLOR = Color.web("#63B006");// #63B006
    private static final Color TITLE_COLOR = Color.web("#11659A");  // #11659A
    private static final Color TEXT_COLOR = Color.web("#000000");
    private ListView<String> transactionList;

    /**
     * Handles importing transaction data from a CSV file and updates the transaction list.
     */
    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null) {
            try {
                // Read the valid data rows of the imported file
                List<String> validLines = new ArrayList<>();
                boolean isDataSection = false;
                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("---------------------WeChat Payment Statement Details List-------------------")) {
                            isDataSection = true;
                            continue;
                        }
                        if (isDataSection && line.startsWith("Transaction Time,Transaction Type")) {
                            continue;
                        }
                        if (isDataSection) {
                            validLines.add(line);
                        }
                    }
                }

                // Append to the target file
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("EnterpriseDeals.csv", true))) {
                    for (String dataLine : validLines) {
                        bw.write(dataLine);
                        bw.newLine();
                    }
                }

                // Refresh the list display
                transactionList.setItems(getTransactionItems());
                
            } catch (IOException e) {
                e.printStackTrace();
                //Error prompts can be added here
            }
        }
    }

    /**
     * The main entry point for the JavaFX application.
     * Sets up the main layout and displays the primary stage.
     * @param primaryStage The main window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createBottomNav(primaryStage));
        root.setRight(createSidebar(primaryStage));  // Add right sidebar

        // Wrap main layout with scroll container
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Set scene size to 1366x768
        Scene scene = new Scene(scrollPane, 1366, 768);

        primaryStage.setTitle("Financial Edition");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Creates the header section with the application title and navigation button.
     * @return HBox containing the header components
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        // Left title section
        HBox leftSection = new HBox();
        leftSection.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("NUTLLET");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label edition = new Label("Enterprise Edition");
        edition.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        edition.setTextFill(Color.WHITE);
        edition.setPadding(new Insets(0, 0, 0, 20));

        leftSection.getChildren().addAll(title, edition);

        // Right button
        Button personalEditionBtn = new Button("Personal Edition");
        personalEditionBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-border-radius: 3;");
        personalEditionBtn.setOnAction(e -> {
            try {
                new Nutllet().start(new Stage());
                ((Stage) personalEditionBtn.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Use Region as flexible space
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(leftSection, spacer, personalEditionBtn);
        return header;
    }

    /**
     * Creates the main content area with revenue/expenditure cards and transaction list.
     * @return SplitPane containing the left and right panels
     */
    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        // Adjust left and right panel ratio to 4:6
        splitPane.setDividerPositions(0.4);

        // Left panel
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        leftPanel.setMinWidth(350);

        RevenueExpenditureCard revenueExpenditureCard = new RevenueExpenditureCard();
        PersonalCorporateExpenditureCard personalCorporateExpenditureCard = new PersonalCorporateExpenditureCard();

        leftPanel.getChildren().addAll(revenueExpenditureCard, personalCorporateExpenditureCard);

        ScrollPane leftScrollPane = new ScrollPane(leftPanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Right panel
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20, 20, 20, 0));
        rightPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label recentTransactions = new Label("Recent Income or Expenditure");
        recentTransactions.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        recentTransactions.setTextFill(PRIMARY_COLOR);
        recentTransactions.setPadding(new Insets(0, 0, 10, 0));

        // Table header style optimization
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold; -fx-background-radius: 5;");

        Label headerLabel = new Label("The transaction time, product name, receipt/payment type and amount are displayed below");
        headerLabel.setStyle("-fx-text-fill: #000000; ");
        headerLabel.setWrapText(true);
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        Button importButton = new Button("Import CSV");
        importButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-min-width: 100px;");
        importButton.setOnAction(e -> handleImportCSV());

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(importButton);

        headerBox.getChildren().addAll(headerLabel, buttonContainer);

        // Transaction record list
        transactionList = new ListView<>();
        transactionList.setItems(getTransactionItems());
        transactionList.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        transactionList.setPrefHeight(450);
        transactionList.setPadding(new Insets(0));

        // Optimize list item style
        transactionList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-padding: 8px;");
                    setBackground(new Background(new BackgroundFill(
                            Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });

        VBox listContainer = new VBox(10);
        listContainer.getChildren().addAll(headerBox, transactionList);
        VBox.setVgrow(transactionList, Priority.ALWAYS);

        // Right scroll panel
        ScrollPane rightScrollPane = new ScrollPane(listContainer);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-background-insets: 0;");
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setPrefHeight(550);

        rightPanel.getChildren().addAll(recentTransactions, rightScrollPane);
        VBox.setVgrow(rightScrollPane, Priority.ALWAYS);

        splitPane.getItems().addAll(leftScrollPane, rightPanel);
        return splitPane;
    }

    /**
     * Creates the bottom navigation bar with Home, Discover, and Settings buttons.
     * @param primaryStage The main window for navigation
     * @return HBox containing the navigation buttons
     */
    private HBox createBottomNav(Stage primaryStage) {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙

        homeBtn.setOnAction(e -> {
            try {
                new Nutllet().start(new Stage()); // Navigate to Nutllet.java
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        discoverBtn.setOnAction(e -> {
            try {
                new Discover().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        settingsBtn.setOnAction(e -> {
            try {
                new Settings().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);
        return navBar;
    }

    /**
     * Creates the right sidebar with three feature cards: trend analysis, reports, and AI suggestions.
     * @param primaryStage The main window for navigation
     * @return VBox containing the sidebar cards
     */
    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Get screen width 1/3
        double screenWidth = 1366; // Assume screen width is 1366px
        double columnWidth = screenWidth / 3.0;

        // Smart Financial Trend Analysis Module
        Label title1 = new Label("Smart Financial Trend Analysis");
        title1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title1.setTextFill(TITLE_COLOR);

        Label text1 = new Label("Through automated data visualization, the system presents enterprises with monthly income and expenditure trends, major spending categories, and payment method distributions. This helps managers quickly grasp financial dynamics and promptly identify abnormal fluctuations or structural issues.");
        text1.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text1.setTextFill(Color.BLACK);
        text1.setWrapText(true);

        VBox card1 = new VBox(10, title1, text1);
        card1.setPadding(new Insets(20));
        card1.setPrefWidth(columnWidth);
        card1.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card1.setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

        // Auto-Generated Financial Reports Module
        Label title2 = new Label("Auto-Generated Financial Reports");
        title2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title2.setTextFill(TITLE_COLOR);

        Label text2 = new Label("Based on historical transaction data, the system automatically generates comprehensive financial analysis reports with both charts and text. The reports cover key indicators such as total income, total expenditure, net balance, largest single expense, most active spending day, and top spending categories, empowering enterprises to make informed decisions efficiently.");
        text2.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text2.setTextFill(Color.BLACK);
        text2.setWrapText(true);

        VBox card2 = new VBox(10, title2, text2);
        card2.setPadding(new Insets(20));
        card2.setPrefWidth(columnWidth);
        card2.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card2.setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

        // AI-Powered Optimization Suggestions Module
        Label title3 = new Label("AI-Powered Optimization Suggestions");
        title3.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title3.setTextFill(TITLE_COLOR);

        Label text3 = new Label("With an integrated AI analysis engine, the system automatically interprets enterprise spending behavior, generating trend summaries, risk alerts, and three targeted optimization suggestions. Enterprises can use these insights to adjust financial strategies, improve fund utilization efficiency, and avoid potential risks.");
        text3.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text3.setTextFill(Color.BLACK);
        text3.setWrapText(true);

        Button askNowButton = new Button("VIEW MORE DETAILS");
        stylePrimaryButton(askNowButton); // Use the same style as "View More Details" button
        askNowButton.setOnAction(e -> {
            try { new EP_FinancialAnalysis().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        VBox card3 = new VBox(10, title3, text3, askNowButton);
        card3.setPadding(new Insets(20));
        card3.setPrefWidth(columnWidth);
        card3.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card3.setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

        // Adding all cards to the sidebar
        sidebar.getChildren().addAll(card1, card2, card3);
        return sidebar;
    }

    /**
     * Applies the primary button style to the given button.
     * @param button The button to style
     */
    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;");
    }

    /**
     * Converts a Color object to its hexadecimal string representation.
     * @param color The color to convert
     * @return Hexadecimal string of the color
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Reads and returns the transaction items from the CSV file for display.
     * @return ObservableList of formatted transaction strings
     */
    private javafx.collections.ObservableList<String> getTransactionItems() {
        javafx.collections.ObservableList<String> items = javafx.collections.FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("EnterpriseDeals.csv"))) {
            String line;
            boolean isTransactionSection = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("--------------------WeChat Payment Statement Details List-------------------")) {
                    isTransactionSection = true;
                    continue;
                }
                if (isTransactionSection && !line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        String time = parts[0].replace("\"", "");
                        String product = parts[3].replace("\"", "");
                        String type = parts[4].replace("\"", "");
                        String amount = parts[5].replace("\"", "");

                        // Format string, use fixed width to ensure alignment
                        String item = String.format("%-20s %-30s %-10s %-10s",
                                time, product, type, amount);
                        items.add(item);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Creates a navigation button with an emoji icon and label.
     * @param label The text label for the button
     * @param emoji The emoji to display
     * @return Button with emoji and label
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
        button.setPrefWidth(1366 / 3.0);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #f5f5f5; -fx-border-color: transparent;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent;"
        ));

        return button;
    }

    /**
     * The main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Card component for displaying enterprise revenue and expenditure with a pie chart.
     */
    private class RevenueExpenditureCard extends VBox {
        /**
         * Constructs the revenue and expenditure card, calculates totals, and displays a pie chart.
         */
        public RevenueExpenditureCard() {
            setSpacing(10);
            setPadding(new Insets(20));
            setBackground(new Background(new BackgroundFill(
                    Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
            setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

            Label title = new Label("Enterprise Revenue & Expenditure");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            title.setStyle("-fx-text-fill: #1A94BC; ");


            // Calculate total revenue and expenditure
            double totalRevenue = 0;
            double totalExpenditure = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader("EnterpriseDeals.csv"))) {
                String line;
                boolean isTransactionSection = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("----------------------WeChat Payment Statement Details List--------------------")) {
                        isTransactionSection = true;
                        continue;
                    }
                    if (isTransactionSection && !line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
                        String[] parts = line.split(",");
                        if (parts.length >= 6) {
                            String type = parts[4].replace("\"", "");
                            String amount = parts[5].replace("\"", "").replace("¥", "");
                            double value = Double.parseDouble(amount);
                            if (type.equals("Income")) {
                                totalRevenue += value;
                            } else if (type.equals("Expenditure")) {
                                totalExpenditure += value;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Calculate balance
            double balance = totalRevenue - totalExpenditure;

            // Create pie chart
            PieChart chart = new PieChart();
            PieChart.Data revenueData = new PieChart.Data("Revenue", totalRevenue);
            PieChart.Data expenditureData = new PieChart.Data("Expenditure", totalExpenditure);
            chart.getData().addAll(revenueData, expenditureData);

            // Set pie chart size
            chart.setPrefSize(300, 300);
            chart.setMaxSize(300, 300);

            // Set pie chart colors
            revenueData.getNode().setStyle("-fx-pie-color: " + toHexString(SUCCESS_COLOR) + ";");
            expenditureData.getNode().setStyle("-fx-pie-color: " + toHexString(TITLE_COLOR) + ";");

            // Add total label
            Label totalLabel = new Label(String.format("Balance: ¥%.2f", balance));
            totalLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            totalLabel.setStyle("-fx-text-fill: #000000; ");

            HBox balanceBox = new HBox(totalLabel);
            balanceBox.setAlignment(Pos.CENTER_LEFT);
            balanceBox.setPadding(new Insets(0, 0, 0, 10));

            VBox contentBox = new VBox(15, title, chart, balanceBox);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setPadding(new Insets(0));

            getChildren().addAll(contentBox);
        }
    }

    /**
     * Card component for comparing personal and corporate expenditures with a pie chart.
     */
    private class PersonalCorporateExpenditureCard extends VBox {
        /**
         * Constructs the personal vs corporate expenditure card and displays a pie chart.
         */
        public PersonalCorporateExpenditureCard() {
            setSpacing(10);
            setPadding(new Insets(20));
            setBackground(new Background(new BackgroundFill(
                    Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
            setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

            Label title = new Label("Personal VS Corporate Expenditures");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            title.setStyle("-fx-text-fill: #1A94BC; ");

            // Calculate corporate expenditure total
            double corporateExpenditure = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader("EnterpriseDeals.csv"))) {
                String line;
                boolean isTransactionSection = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("----------------------WeChat Payment Statement Details List--------------------")) {
                        isTransactionSection = true;
                        continue;
                    }
                    if (isTransactionSection && !line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
                        String[] parts = line.split(",");
                        if (parts.length >= 6) {
                            String type = parts[4].replace("\"", "");
                            String amount = parts[5].replace("\"", "").replace("¥", "");
                            if (type.equals("Expenditure")) {
                                corporateExpenditure += Double.parseDouble(amount);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Calculate personal expenditure total
            double personalExpenditure = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
                String line;
                boolean isTransactionSection = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("WeChat Payment Statement Details List")) {
                        isTransactionSection = true;
                        continue;
                    }
                    if (isTransactionSection && !line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
                        String[] parts = line.split(",");
                        if (parts.length >= 6) {
                            String type = parts[4].replace("\"", "");
                            String amount = parts[5].replace("\"", "").replace("¥", "");
                            if (type.equals("Expenditure")) {
                                personalExpenditure += Double.parseDouble(amount);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Calculate total expenditure
            double totalExpenditure = personalExpenditure + corporateExpenditure;

            // Create pie chart
            PieChart chart = new PieChart();
            PieChart.Data personalData = new PieChart.Data("Personal", personalExpenditure);
            PieChart.Data corporateData = new PieChart.Data("Corporate", corporateExpenditure);
            chart.getData().addAll(personalData, corporateData);

            // Set pie chart size
            chart.setPrefSize(300, 300);
            chart.setMaxSize(300, 300);

            // Set pie chart colors
            personalData.getNode().setStyle("-fx-pie-color: " + toHexString(SUCCESS_COLOR) + ";");
            corporateData.getNode().setStyle("-fx-pie-color: " + toHexString(TITLE_COLOR) + ";");

            // Add total label
            Label totalLabel = new Label(String.format("Total Expenditures: ¥%.2f", totalExpenditure));
            totalLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            totalLabel.setStyle("-fx-text-fill: #000000; ");

            getChildren().addAll(title, chart, totalLabel);
        }
    }
}