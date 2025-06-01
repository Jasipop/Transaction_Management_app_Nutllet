//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.effect.DropShadow;

/**
 * NutlletReminder is a JavaFX application for managing financial reminders.
 * It provides features such as:
 * - Loading and displaying reminder data from a CSV file
 * - Calculating and displaying progress for savings and expense monitoring
 * - Adding and deleting reminders
 * - Navigation between different sections of the application
 *
 * @author Simeng Lyu
 * @version final
 */
public class NutlletReminder extends Application {
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color BACKGROUND_COLOR = Color.rgb(255, 212, 236, 0.33);
    private static final Color TEXT_COLOR = Color.BLACK;

    public List<Reminder> reminders = new ArrayList<>();
    private double totalIncome = 0;
    private double totalExpense = 0;

    /**
     * The main entry point for the JavaFX application.
     * Sets up the main layout and displays the primary stage.
     * @param primaryStage The main window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        loadData();
        calculateBalance();
        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        
        // Create ScrollPane to wrap main content
        ScrollPane scrollPane = new ScrollPane(createMainContent(primaryStage));
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);
        
        root.setBottom(createBottomNav(primaryStage));

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Nutllet - Reminders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Loads reminder and transaction data from the CSV file and calculates totals.
     */
    public void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean isReminderSection = false;
            boolean isTransactionSection = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("Type,Reminder name,Reminder Category,Amount Range,Note")) {
                    isReminderSection = true;
                    continue;
                }
                if (line.contains("----------------------WeChat Payment Statement Details List--------------------")) {
                    isReminderSection = false;
                    isTransactionSection = true;
                    continue;
                }
                if (line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
                    continue;
                }

                if (isReminderSection && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        Reminder reminder = new Reminder();
                        reminder.name = parts[1].replace("\"", "");
                        reminder.type = parts[2].replace("\"", "");
                        String amountRange = parts[3].replace("\"", "").replace("¥", "");
                        String[] amounts = amountRange.split("-");
                        reminder.minAmount = Double.parseDouble(amounts[0]);
                        reminder.maxAmount = Double.parseDouble(amounts[1]);
                        reminder.remark = parts[4].replace("\"", "");
                        reminders.add(reminder);
                    }
                }

                if (isTransactionSection && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                    	String transactionType = parts[1].replace("\"", "");
                        if ("Merchant Consumption".equals(transactionType) || "Pocket Money".equals(transactionType)) {
                        	String type = parts[4].replace("\"", "");
                            String amount = parts[5].replace("\"", "").replace("¥", "");
                            double value = Double.parseDouble(amount);
                            if (type.equals("Income")) {
                                totalIncome += value;
                            } else if (type.equals("Expenditure")) {
                                totalExpense += value;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the balance and progress for each reminder based on income and expenses.
     */
    public void calculateBalance() {
        double originalTotalIncome = totalIncome; // Save original total income
        double originalTotalExpense = totalExpense; // Save original total expense
        double currentIncome = originalTotalIncome; // Current income after dynamic deduction

        // First process savings type reminder
        for (Reminder reminder : reminders) {
            if (reminder.type.equals("For savings")) {
                double avgAmount = (reminder.minAmount + reminder.maxAmount) / 2;
                // Use currentIncome to calculate balance
                double balance = currentIncome - originalTotalExpense;
                double progress = balance >= 0 ? Math.min(100, (balance / avgAmount) * 100) : 0;

                // If progress is completed, deduct maxAmount
                if (progress >= 100) {
                    currentIncome -= reminder.maxAmount; // Update current available income
                    progress = 100; // Force to display as 100%
                }
                else {
                	currentIncome = 0;
                }

                reminder.progress = progress;
                reminder.progressText = String.format("Savings progress：%.1f%%", progress);
            }
        }

        // Update final deducted total income
        totalIncome = currentIncome;

        // Process expense type reminder (based on original total expense, not affected by savings deduction)
        for (Reminder reminder : reminders) {
            if (reminder.type.equals("For expense monitoring")) {
                double avgAmount = (reminder.minAmount + reminder.maxAmount) / 2;
                reminder.progress = Math.min(100, (originalTotalExpense / avgAmount) * 100);
                reminder.progressText = String.format("Monthly expense has reached %.1f%% of the target", reminder.progress);
            }
        }
    }

    /**
     * Creates the header section with the application title.
     * @return HBox containing the header components
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Reminders");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(title);
        return header;
    }

    /**
     * Creates the main content area with reminder buttons and an add button.
     * @param primaryStage The main window for navigation
     * @return VBox containing the main content
     */
    private VBox createMainContent(Stage primaryStage) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setMinWidth(400); // Set minimum width to ensure content is not compressed

        for (Reminder reminder : reminders) {
            Button reminderButton = createReminderButton(reminder, primaryStage);
            mainContent.getChildren().add(reminderButton);
        }
        Button addReminderButton = new Button("Add New Reminder");

        stylePrimaryButton(addReminderButton);

        addReminderButton.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContent.getChildren().add(addReminderButton);
        return mainContent;
    }

    /**
     * Creates a button for a reminder with progress bar and delete functionality.
     * @param reminder The reminder to display
     * @param primaryStage The main window for navigation
     * @return Button containing the reminder details
     */
    private Button createReminderButton(Reminder reminder, Stage primaryStage) {
        HBox mainContainer = new HBox(10);
        mainContainer.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        content.setEffect(new DropShadow(12, 0, 4, Color.rgb(0, 0, 0, 0.08)));

        Label title = new Label(reminder.name);
        //title.setStyle("-fx-background-color: red;");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: black;");

        // Add amount range
        Label amountRange = new Label(String.format("Quota：¥%.0f-%.0f", reminder.minAmount, reminder.maxAmount));
        amountRange.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        amountRange.setStyle("-fx-text-fill: black;");
        
        // Add remark
        Label remark = new Label("Remark：" + reminder.remark);
        remark.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        remark.setStyle("-fx-text-fill: black;");
        
        HBox progressBarContainer = new HBox();
        progressBarContainer.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY, new CornerRadii(6), Insets.EMPTY)));
        progressBarContainer.setPrefHeight(8);
        progressBarContainer.setMinHeight(8);
        progressBarContainer.setMaxHeight(8);
        progressBarContainer.setPrefWidth(400);

        Region progressBar = new Region();
        progressBar.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, new CornerRadii(6), Insets.EMPTY)));
        progressBar.setPrefHeight(8);
        progressBar.setMinHeight(8);
        progressBar.setMaxHeight(8);
        progressBar.setPrefWidth(reminder.progress * 4); // 400 * percentage
        progressBarContainer.getChildren().add(progressBar);

        Label description = new Label(reminder.progressText);
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setStyle("-fx-text-fill: black;");
        content.getChildren().addAll(title, amountRange, remark, progressBarContainer, description);

        // Create delete button
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #ffebeb; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;"));

        // Set delete button event
        deleteButton.setOnAction(e -> {
            try {
                deleteReminder(reminder.name);
                // Reload page
                new NutlletReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContainer.getChildren().addAll(content, deleteButton);
        return new Button("", mainContainer);
    }

    /**
     * Deletes a reminder from the CSV file and refreshes the display.
     * @param reminderName The name of the reminder to delete
     */
    private void deleteReminder(String reminderName) {
        try {
            File file = new File("deals.csv");
            StringBuilder content = new StringBuilder();
            boolean isReminderSection = false;
            boolean foundReminder = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Type,Reminder name,Reminder Category,Amount Range,Note")) {
                        isReminderSection = true;
                        content.append(line).append("\n");
                        continue;
                    }
                    if (line.contains("WeChat Payment Statement Details List")) {
                        isReminderSection = false;
                        content.append(line).append("\n");
                        continue;
                    }

                    if (isReminderSection) {
                        // Check if it's the reminder to delete
                        if (line.contains("\"" + reminderName + "\"")) {
                            foundReminder = true;
                            continue; // Skip this line, don't add to new content
                        }
                    }
                    content.append(line).append("\n");
                }
            }

            if (foundReminder) {
                // Write updated content
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(content.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Delete failed");
            alert.setContentText("Failed to delete reminder: " + e.getMessage());
            alert.showAndWait();
        }
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

        // Create navigation button
        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        // Set button event
        homeBtn.setOnAction(e -> {
            try {
                new Nutllet().start(new Stage());
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

        // Adjust button order (Home -> Discover -> Settings)
        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);
        return navBar;
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
        btnContainer.setSpacing(4);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 18px;");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        Button button = new Button();
        button.setPrefWidth(1366 / 3.0);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: transparent; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;"));

        return button;
    }

    /**
     * Applies the primary button style to the given button.
     * @param button The button to style
     */
    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR.darker()) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;"));
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
     * The main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}