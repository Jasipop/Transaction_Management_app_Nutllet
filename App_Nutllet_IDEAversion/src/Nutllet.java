//package Merge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.Cursor;
import javafx.application.HostServices;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.stage.Window;
import javafx.stage.Modality;
import java.time.format.DateTimeParseException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The main application class for NUTLLET Financial Management System.
 * <p>
 * This JavaFX application provides comprehensive personal finance management features including:
 * - Expense tracking and categorization
 * - Interactive data visualization through pie charts
 * - AI-powered spending analysis and recommendations
 * - Transaction record management with CSV integration
 * - Reminder system for financial goals
 * - Multi-view navigation with enterprise edition support
 * </p>
 *
 * <p>The application features a modern UI with real-time data updates, natural language processing
 * for transaction input, and integration with local AI models for financial insights.</p>
 *
 * @author Xinghan Qin
 * @version final
 */
public class Nutllet extends Application {
    /**
     * Primary color constant for UI theme (Purple RGB: 133,95,175)
     */
    public static final Color PRIMARY_PURPLE = Color.rgb(133, 95, 175);
    public static final Color LIGHT_PURPLE_BG = Color.rgb(245, 241, 255);
    public static final Color DARK_NAV_BG = Color.rgb(40, 40, 40);
    public static final Color NAV_HOVER = Color.rgb(70, 70, 70);
    public static final Color NAV_SELECTED = Color.rgb(90, 90, 90);

    public Map<String, Double> categoryTotals = new HashMap<>();
    public ObservableList<String> transactionItems = FXCollections.observableArrayList();
    public double totalExpenditure;
    public HostServices hostServices;
    public List<Expense> expenses = new ArrayList<>();
    public List<Expense> sortedExpenses = new ArrayList<>();
    public PieChart pieChart;
    public Label balanceValue;
    public TextArea aiContent;
    public VBox leftPanel;
    public VBox progressSection;
    /**
     * Initializes and starts the JavaFX application
     * @param primaryStage The main window of the application
     */
    @Override
    public void start(Stage primaryStage) {
        String csvFileName = "deals.csv";
        loadExpensesFromCSV(csvFileName);
        processData(expenses);
        this.hostServices = getHostServices();
        BorderPane root = new BorderPane();
        root.setTop(createHeader());

        ScrollPane mainScroll = new ScrollPane(createMainContent());
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: white;");
        root.setCenter(mainScroll);

        root.setBottom(createBottomNavigation());

        // Create a floating button container
        HBox floatButtons = createFloatButtons();

        // Overlay layout using stackpane
        StackPane rootContainer = new StackPane();
        rootContainer.getChildren().addAll(root, floatButtons);
        StackPane.setAlignment(floatButtons, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(floatButtons, new Insets(620, 20, 70, 0));

        Scene scene = new Scene(rootContainer, 1366, 768);
        primaryStage.setTitle("NUTLLET - Financial Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Loads reminder data from the NutlletReminder application
     * @return List of Reminder objects containing financial goals and progress
     */
    public List<Reminder> loadReminders() {
        NutlletReminder reminderApp = new NutlletReminder();
        reminderApp.loadData(); // Load raw data
        reminderApp.calculateBalance(); // Calculate progress data
        return reminderApp.reminders;
    }
    /**
     * Creates the main content area of the application
     * @return VBox containing the main content layout
     */
    public VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setMaxWidth(Double.MAX_VALUE); // Allow content expansion
        mainContent.setFillWidth(true); // Enable fill width

        SplitPane splitPane = new SplitPane();
        splitPane.setMaxWidth(Double.MAX_VALUE); // Split panel fill width

        // Set width constraints for the left and right panels
        VBox left = createLeftPanel();
        VBox right = createRightPanel();
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);

        splitPane.getItems().addAll(left, right);
        splitPane.setDividerPosition(0, 0.55);

        // Add width constraint to AI section
        VBox aiSection = createAIConsumptionSection();
        aiSection.setMaxWidth(Double.MAX_VALUE);

        mainContent.getChildren().addAll(splitPane, aiSection);
        return mainContent;
    }

    /**
     * Creates the left panel containing balance information and charts
     * @return VBox containing the left panel layout
     */
    public VBox createLeftPanel() {
        leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        leftPanel.setMaxWidth(Double.MAX_VALUE);

        VBox balanceBox = new VBox(5);
        Label balanceTitle = new Label("Total expenditure");
        balanceTitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        this.balanceValue = new Label(String.format("¥ %.2f", totalExpenditure));
        balanceValue.setStyle("-fx-text-fill: #333333; -fx-font-size: 32px; -fx-font-weight: bold;");
        balanceBox.getChildren().addAll(balanceTitle, balanceValue);

        this.pieChart = createPieChart();
        progressSection = createProgressSection(); // Initialization progress bar section

        leftPanel.getChildren().addAll(
                balanceBox,
                this.pieChart,
                progressSection,
                createButtonPanel()
        );
        return leftPanel;
    }

    /**
     * Creates the right panel containing transaction history and search functionality
     * @return VBox containing the right panel layout
     */
    public VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        rightPanel.setMaxWidth(Double.MAX_VALUE); // Allow extensions

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Label searchLabel = new Label("Recent consumption");
        searchLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 16px;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search transaction records...");
        searchField.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 5 10;");
        searchField.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchLabel, spacer, searchField);

        // FilteredList is used to filter lists
        FilteredList<String> filteredData = new FilteredList<>(transactionItems, s -> true);

        ListView<String> transactionList = new ListView<>(filteredData);
        transactionList.setCellFactory(lv -> new TransactionCell());
        transactionList.setStyle("-fx-border-color: #e6e6e6; -fx-background-color: transparent;");

        // Placeholder text when setting 'no relevant records'
        transactionList.setPlaceholder(new Label("No relevant records"));

        // Search box listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return item.toLowerCase().contains(lowerCaseFilter);
            });
        });

        rightPanel.getChildren().addAll(searchBox, transactionList);
        VBox.setVgrow(transactionList, Priority.ALWAYS);
        return rightPanel;
    }

    public int currentAIPage = 0;
    public final List<String> aiContents = Arrays.asList(
            "On the next period, you may need to be extra vigilant to avoid over-spending on food and drink consuming.\n\n" +
                    "This could involve:\n" +
                    "• Reducing the frequency of dining out at expensive restaurants\n" +
                    "• Opting for home-cooked meals to control ingredients quality\n" +
                    "• Planning meals in advance with detailed shopping lists\n" +
                    "• Choosing tap water over bottled beverages\n\n",

            "Financial Advice\n\n" +
                    "According to the analysis of your consumption habits, catering expenses account for 28%. " +
                    "It is recommended to optimize the catering consumption structure.\n\n" +
                    "Currently 35% of the budget remains, and you may consider transferring part of the funds " +
                    "to a financial management account to earn returns."
    );

    public StackPane dot1, dot2;

    /**
     * Creates the AI consumption analysis section
     * @return VBox containing the AI analysis interface
     */
    public VBox createAIConsumptionSection() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30, 50, 30, 50));
        container.setBackground(new Background(new BackgroundFill(
                Color.web("#F5F1FF"), new CornerRadii(10), Insets.EMPTY
        )));
        container.setAlignment(Pos.TOP_CENTER);

        // header
        Label title = new Label("AI consumption analysis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_PURPLE);
        title.setPadding(new Insets(0, 0, 15, 0));

        // Analyze content area
        this.aiContent = new TextArea();
        aiContent.setEditable(false);
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-background-color: white; " +
                "-fx-text-fill: #666666; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");
        aiContent.setPrefHeight(180);
        aiContent.setText("Initializing local AI analysis engine...");

        // Progress indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        // Control button
        Button analyzeBtn = new Button("Start analysis");
        analyzeBtn.setStyle("-fx-background-color: #855FAF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 20;");

        // layout container
        StackPane contentPane = new StackPane(aiContent, progress);
        VBox controls = new VBox(15, analyzeBtn);
        controls.setAlignment(Pos.CENTER);

        container.getChildren().addAll(title, contentPane, controls);

        // Analyze button events
        analyzeBtn.setOnAction(e -> {
            aiContent.setText("Analyzing consumption data...");
            progress.setVisible(true);
            analyzeBtn.setDisable(true);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Process process = new ProcessBuilder(
                            "ollama", "run", "qwen2:1.5b"
                    ).start();

                    // Build input
                    String prompt = "Please analyze the following consumption records and give professional advice in English：\n" +
                            loadCSVForAnalysis() +
                            "\nPlease reply in the following format and translate the answers into English:" +
                            "\n1. summary of consumption trend (no more than 100 words)" +
                            "\n2. three optimization suggestions" +
                            "\n3. Risk warning (if any)";

                    OutputStream stdin = process.getOutputStream();
                    stdin.write(prompt.getBytes());
                    stdin.flush();
                    stdin.close();

                    // Read output
                    InputStream stdout = process.getInputStream();
                    StringBuilder analysis = new StringBuilder();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = stdout.read(buffer)) != -1) {
                        analysis.append(new String(buffer, 0, bytesRead));
                    }

                    // Processing results
                    String formatted = formatAnalysis(analysis.toString());

                    Platform.runLater(() -> {
                        aiContent.setText(formatted);
                        progress.setVisible(false);
                        analyzeBtn.setDisable(false);
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        aiContent.setText("Analysis failed: " + ex.getMessage());
                        progress.setVisible(false);
                        analyzeBtn.setDisable(false);
                    });
                }
            });
        });

        return container;
    }

    /**
     * Loads transaction data from CSV for AI analysis
     * @return Formatted string of transaction data
     */
    public String loadCSVForAnalysis() {
        return expenses.stream()
                .map(e -> String.format("[%s] %s - ¥%.2f (%s)",
                        e.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        e.getCounterpart(),
                        e.getAmount(),
                        e.getProduct()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Formats the raw AI analysis output for display
     * @param raw Raw analysis text from AI
     * @return Formatted analysis text
     */
    public String formatAnalysis(String raw) {
        // Basic format cleaning
        return raw.replaceAll("(?m)^\\s*\\d+\\.?", "\n●")
                .replaceAll("\n+", "\n")
                .replaceAll("(\\p{Lu}):", "\n$1：")
                .trim();
    }

    /**
     * Creates an interactive dot for page navigation
     * @param pageIndex Index of the page this dot represents
     * @return StackPane containing the interactive dot
     */
    public StackPane createInteractiveDot(int pageIndex) {
        Circle dot = new Circle(6);
        dot.setFill(pageIndex == currentAIPage ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        dot.setStroke(Color.web("#999999"));
        dot.setStrokeWidth(0.5);

        StackPane clickableDot = new StackPane(dot);
        clickableDot.setPadding(new Insets(8));  // Expand the click area
        clickableDot.setCursor(Cursor.HAND);
        clickableDot.setOnMouseClicked(e -> {
            if(pageIndex != currentAIPage) {
                Platform.runLater(() -> switchAIPage(pageIndex));
            }
        });
        return clickableDot;
    }

    /**
     * Creates an action button with arrow icon
     * @return Button with arrow icon and styling
     */
    public Button createActionButton() {
        SVGPath arrowIcon = new SVGPath();
        arrowIcon.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        arrowIcon.setFill(Color.WHITE);

        HBox btnContent = new HBox(8, new Label("View Details"), arrowIcon);
        btnContent.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(btnContent);
        button.setStyle("-fx-background-color: #855FAF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 20;");

        // Modify the click event to open a webpage
        button.setOnAction(e -> {
            hostServices.showDocument("https://chat.deepseek.com");
        });

        return button;
    }

    /**
     * Switches between AI analysis pages
     * @param page Index of the page to switch to
     */
    public void switchAIPage(int page) {
        currentAIPage = page;
        aiContent.setText(aiContents.get(currentAIPage));

        // Update pagination point color (by accessing circles in StackPanes)
        updateDotColor(dot1, 0);
        updateDotColor(dot2, 1);

        // Dynamic style adjustment
        String styleBase = "-fx-background-color: transparent; -fx-font-family: 'Segoe UI';";
        aiContent.setStyle(styleBase + (currentAIPage == 1 ?
                "-fx-text-fill: #666666; -fx-font-size: 15px;" :
                "-fx-text-fill: #333333; -fx-font-size: 14px;"));
    }

    /**
     * Updates the color of navigation dots based on current page
     * @param dotPane The dot pane to update
     * @param targetPage The target page index
     */
    public void updateDotColor(StackPane dotPane, int targetPage) {
        Circle dot = (Circle) dotPane.getChildren().get(0);
        dot.setFill(currentAIPage == targetPage ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
    }

    // Retain other original methods (complete implementation)
    /**
     * Creates the application header with title and navigation buttons
     * @return HBox containing the header layout
     */
    public HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(PRIMARY_PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(12, 20, 12, 20));

        HBox leftPanel = new HBox(10);
        Label title = new Label("NUTLLET");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label edition = new Label("Personal Edition");
        edition.setFont(Font.font("Segoe UI", 12));
        edition.setTextFill(Color.rgb(255, 255, 255, 0.6));
        leftPanel.getChildren().addAll(title, edition);

        HBox rightPanel = new HBox(15);
        String[] buttons = {"Syncing", "Enterprise Edition", "Logout"};
        for (String btnText : buttons) {
            Button btn = new Button(btnText);
            if (btnText.equals("Enterprise Edition")) {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #855FAF; -fx-border-radius: 3;");
                btn.setOnAction(e -> {
                    try {
                        new NutlletEnterprise().start(new Stage());
                        ((Stage) btn.getScene().getWindow()).close(); // Close the current page
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
            else if (btnText.equals("Syncing")) {
                // Set the style and click event of the Syncing button
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                btn.setOnAction(e -> {
                    // Refresh data and update UI
                    loadExpensesFromCSV("deals.csv");
                    processData(expenses);
                    updateUI();
                });
            }else if (btnText.equals("Logout")) {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #855FAF; -fx-border-radius: 3;");
                btn.setOnAction(e -> {
                    try {
                        new Login().start(new Stage());
                        ((Stage) btn.getScene().getWindow()).close(); // Close the current page
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            }

            rightPanel.getChildren().add(btn);
        }

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        header.getChildren().addAll(leftPanel, rightPanel);
        return header;
    }

    /**
     * Creates floating action buttons for quick actions
     * @return HBox containing the floating buttons
     */
    public HBox createFloatButtons() {
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        // Voice input button
        Button textInputBtn = createFloatingButton(
                "M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z",
                "Text Input"
        );

        // Manual input button
        Button manualBtn = createFloatingButton(
                "M3 17.46v3.04h3.04L17.46 9.62l-3.04-3.04L3 17.46zm18.72-12.33l-2.68 2.68-3.04-3.04 2.68-2.68c.4-.4 1.04-.4 1.44 0l1.6 1.6c.4.4.4 1.04 0 1.44z",
                "Manual Input"
        );

        buttonContainer.getChildren().addAll(textInputBtn, manualBtn);
        return buttonContainer;
    }

    /**
     * Creates a floating button with icon and text
     * @param svgPath SVG path for the button icon
     * @param text Button text label
     * @return Styled Button with icon and text
     */
    public Button createFloatingButton(String svgPath, String text) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.rgb(64, 64, 64));
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #404040; -fx-font-size: 12px; -fx-font-weight: 500;");

        HBox buttonContent = new HBox(8, icon, label);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setPadding(new Insets(8, 15, 8, 15));

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setStyle("-fx-background-color: #E6E6FA; " +
                "-fx-background-radius: 25; " +
                "-fx-border-radius: 25; " +
                "-fx-border-color: #D0D0D0; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.1, 0, 2);");
        button.setOnAction(e -> handleFloatButtonClick(text));

        return button;
    }

    /**
     * Handles click events for floating action buttons
     * @param buttonType Type of button clicked
     */
    public void handleFloatButtonClick(String buttonType) {
        if ("Manual Input".equals(buttonType)) {
            showManualInputDialog();
        } else if ("Text Input".equals(buttonType)) {
            showTextInputDialog();
        }
    }
    /**
     * Shows dialog for manual expense input
     */
    public void showManualInputDialog() {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Manually enter consumption records");
        dialog.setHeaderText("Please enter detailed consumption information");

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Create form fields
        TextField timeField = new TextField();
        timeField.setPromptText("YYYY-MM-DD HH:mm:ss");
        TextField counterpartField = new TextField();
        TextField productField = new TextField();
        TextField amountField = new TextField();

        grid.add(new Label("Time of transaction:"), 0, 0);
        grid.add(timeField, 1, 0);
        grid.add(new Label("Counterparty:"), 0, 1);
        grid.add(counterpartField, 1, 1);
        grid.add(new Label("Product Description:"), 0, 2);
        grid.add(productField, 1, 2);
        grid.add(new Label("Amount (Yuan):"), 0, 3);
        grid.add(amountField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        // input validation
        ChangeListener<String> inputValidator = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                boolean isValid = !timeField.getText().isEmpty()
                        && !counterpartField.getText().isEmpty()
                        && !productField.getText().isEmpty()
                        && !amountField.getText().matches(".*[^0-9.].*");
                confirmButton.setDisable(!isValid);
            }
        };

        timeField.textProperty().addListener(inputValidator);
        counterpartField.textProperty().addListener(inputValidator);
        productField.textProperty().addListener(inputValidator);
        amountField.textProperty().addListener(inputValidator);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                try {
                    LocalDateTime time = LocalDateTime.parse(timeField.getText(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    double amount = Double.parseDouble(amountField.getText());
                    return new Expense(time, amount, counterpartField.getText(),
                            productField.getText(), "Expenditure", "Payment successful");
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input format").show();
                }
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(expense -> {
            expenses.add(expense);
            processData(expenses);
            updateUI();
            saveExpensesToCSV("deals.csv"); // New saving method
        });
    }

    /**
     * Processes text input for expense recognition
     * @param text Raw text input to process
     */
    public void handleTextInput(String text) {
        Platform.runLater(() -> {
            aiContent.setText("Analyzing text input...\nPlease wait...");
            aiContent.setDisable(true);
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Process process = new ProcessBuilder(
                        "ollama", "run", "qwen2:1.5b"
                ).start();

                // Building AI prompt words
                String prompt = """
                	    STRICT JSON FORMAT REQUIRED! Please extract expense records from the text according to the following rules：
                	    1. time processing:
                	       - Format must beyyyy-MM-dd HH:mm:ss
                	       - If there is no definite time in the text, it is inferred by semantics (such as "yesterday" ->current date -1 day)
                	    2. amount processing:
                	       - Unified conversion to RMB (such as "100 yuan" ->100, "¥ 38.5" ->38.5)
                	    3. merchant identification:
                	       - Standardized name (such as "McDonald's" ->"McDonald's Restaurant")
                	    4. Product Description:
                	       - Retain key information and remove modifiers (such as "delicious hamburger" ->"hamburger")
                	    5. classification logic:
                	    -Catering (including restaurant/food)
                        -Transportation (including travel/refueling)
                        -Entertainment (including movies/games)
                        -Life (supermarket/daily necessities)
                        -Others

                	    Sample input: "it cost 125 yuan to order takeout in meituan on March 15, and didi 38 yuan to take a taxi on March 16"
                        Sample output:
                	    [
                	      {
                	        "transaction_time": "2025-03-15 18:00:00",
                	        "amount": 125.0,
                	        "counterpart": "meituan takeout",
                	        "product": "take out order",
                	        "category": "Catering"
                	      },
                	      {
                	        "transaction_time": "2025-03-16 09:30:00", 
                	        "amount": 38.0,
                	        "counterpart": "Didi trip",
                	        "product": "car Hailing",
                	        "category": "Transportation"
                	      }
                	    ]

                	    Please process the following inputs:
                	    """ + text;

                OutputStream stdin = process.getOutputStream();
                stdin.write(prompt.getBytes("UTF-8"));
                stdin.flush();
                stdin.close();

                // Read AI response
                InputStream stdout = process.getInputStream();
                StringBuilder response = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stdout.read(buffer)) != -1) {
                    response.append(new String(buffer, 0, bytesRead));
                }

                // Parse JSON response
                List<Expense> newExpenses = parseAIResponse(response.toString());

                Platform.runLater(() -> {
                    if (!newExpenses.isEmpty()) {
                        confirmAndSaveExpenses(newExpenses);
                    } else {
                        aiContent.setText("No valid expenses found in the text.");
                    }
                    aiContent.setDisable(false);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    aiContent.setText("Analysis failed: " + ex.getMessage());
                    aiContent.setDisable(false);
                });
            }
        });
    }

    /**
     * Shows confirmation dialog for saving recognized expenses
     * @param newExpenses List of expenses to confirm
     */
    public void confirmAndSaveExpenses(List<Expense> newExpenses) {
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Confirm Expense Records");
        confirmDialog.setHeaderText("Found the following expense entries, confirm to save?\\n(Editable before saving)");

        // Create a table with editable columns
        TableView<Expense> tableView = new TableView<>();

        // The original column definition remains unchanged ..
        TableColumn<Expense, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTransactionTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Expense, String> counterpartCol = new TableColumn<>("Counterparty");
        counterpartCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCounterpart()));

        TableColumn<Expense, Number> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cd ->
                new SimpleDoubleProperty(cd.getValue().getAmount()));

        TableColumn<Expense, String> productCol = new TableColumn<>("Product Description");
        productCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getProduct()));

        // Add editable columns
        TableColumn<Expense, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(param -> new TableCell<>() {
            public final Button editBtn = new Button("✎");
            {
                editBtn.setStyle("-fx-background-color: #E8EAF6; -fx-text-fill: #1A237E;");
                editBtn.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    showEditDialog(expense); // Pop up editing dialog box
                    tableView.refresh(); //Refresh the table display
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        // Add all columns to the table (including existing columns and newly added editCol)
        tableView.getColumns().addAll(timeCol, counterpartCol, amountCol, productCol, editCol);
        tableView.setItems(FXCollections.observableArrayList(newExpenses));
        tableView.setPrefHeight(300);

        // Add feedback button
        Button feedbackBtn = new Button("Report Recognition Issue");
        feedbackBtn.setStyle("-fx-text-fill: #B71C1C; -fx-border-color: #D32F2F;");
        feedbackBtn.setOnAction(e -> collectFeedback(newExpenses));

        // Create bottom button container
        HBox buttonBox = new HBox(15, feedbackBtn);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, tableView, buttonBox);
        content.setPrefWidth(700);

        confirmDialog.getDialogPane().setContent(content);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            expenses.addAll(newExpenses);
            processData(expenses);
            saveExpensesToCSV("deals.csv");
            updateUI();
            aiContent.setText("Successfully saved " + newExpenses.size() + " new records!");
        }
    }

    /**
     * Shows dialog for editing expense details
     * @param expense Expense to edit
     */
    public void showEditDialog(Expense expense) {
        Dialog<Expense> editDialog = new Dialog<>();
        editDialog.setTitle("Edit consumption record");
        editDialog.setHeaderText("Correction of automatic identification results");

        // create form
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TextField timeField = new TextField(expense.getTransactionTime().format(formatter));
        TextField counterpartField = new TextField(expense.getCounterpart());
        TextField productField = new TextField(expense.getProduct());
        TextField amountField = new TextField(String.valueOf(expense.getAmount()));

        grid.addRow(0, new Label("Time:"), timeField);
        grid.addRow(1, new Label("Counterparty:"), counterpartField);
        grid.addRow(2, new Label("Product:"), productField);
        grid.addRow(3, new Label("Amount:"), amountField);

        // input validation
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Node saveButton = editDialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false); // Perform input validation as needed

        editDialog.getDialogPane().setContent(grid);
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    return new Expense(
                            LocalDateTime.parse(timeField.getText(), formatter),
                            Double.parseDouble(amountField.getText()),
                            counterpartField.getText(),
                            productField.getText(),
                            "Expenditure",
                            "Payment successful"
                    );
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input format").show();
                }
            }
            return null;
        });

        Optional<Expense> result = editDialog.showAndWait();
        result.ifPresent(edited -> {
            expense.setTransactionTime(edited.getTransactionTime());
            expense.setAmount(edited.getAmount());
            expense.setCounterpart(edited.getCounterpart());
            expense.setProduct(edited.getProduct());
        });
    }

    /**
     * Shows dialog for collecting feedback on AI recognition
     * @param aiResults List of AI-recognized expenses
     */
    public void collectFeedback(List<Expense> aiResults) {
        Dialog<Void> feedbackDialog = new Dialog<>();
        feedbackDialog.setTitle("Problem feedback");
        feedbackDialog.setHeaderText("Please describe the details of the identification error");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("For example, \"100 yuan\" is recognized as 10 yuan due to the wrong recognition of the amount unit ");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefSize(500, 200);

        Button submitBtn = new Button("Submit feedback");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitBtn.setOnAction(e -> {
            try {
                String logEntry = String.format(
                        "[%s] Original identification result:%s User feedback:%s%n",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        aiResults.toString(),
                        feedbackArea.getText()
                );

                Files.write(Paths.get("ai_feedback.log"),
                        logEntry.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);

                new Alert(Alert.AlertType.INFORMATION, "Feedback submitted successfully!").show();
                feedbackDialog.close();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Unable to save feedback：" + ex.getMessage()).show();
            }
        });

        VBox content = new VBox(15, feedbackArea, submitBtn);
        content.setPadding(new Insets(15));

        feedbackDialog.getDialogPane().setContent(content);
        feedbackDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        feedbackDialog.showAndWait();
    }

    /**
     * Shows dialog for text input analysis
     */
    public void showTextInputDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Text Input Analysis");
        dialog.setHeaderText("Please enter or paste the consumption record text");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Examples：\n"
                + "On March 15, it cost 125.5 yuan to buy daily necessities in the supermarket\n"
                + "At noon on March 16, the consumption of McDonald's was 38 yuan");
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 300);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Set prompts at the beginning of analysis
                aiContent.setText("Starting text analysis...");
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> handleTextInput(text));
    }

    /**
     * Parses AI response into Expense objects
     * @param response Raw AI response text
     * @return List of parsed Expense objects
     */
    public List<Expense> parseAIResponse(String response) {
        try {
            // Clean up response content and extract JSON
            String jsonStr = response.substring(response.indexOf("["), response.lastIndexOf("]") + 1)
                    .replaceAll("\\\\\"", "")
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Configure multiple time formats
            List<DateTimeFormatter> formatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分s秒"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")  // Western style format
            );

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> data = mapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});

            return data.stream()
                    .map(map -> {
                        try {
                            // Analyze time field
                            String rawTime = map.getOrDefault("transaction_time", "").toString();
                            if (rawTime.isEmpty()) {
                                throw new RuntimeException("Missing transaction_time");
                            }

                            // Attempt to parse multiple formats
                            LocalDateTime parsedTime = null;
                            for (DateTimeFormatter formatter : formatters) {
                                try {
                                    parsedTime = LocalDateTime.parse(rawTime, formatter);
                                    break;
                                } catch (DateTimeParseException ignored) {}
                            }

                            if (parsedTime == null) {
                                throw new RuntimeException("Unsupported time format: " + rawTime);
                            }

                            // Analyze the amount
                            double amount = 0.0;
                            Object amountObj = map.get("amount");
                            if (amountObj instanceof Number) {
                                amount = ((Number) amountObj).doubleValue();
                            } else if (amountObj != null) {
                                String amountStr = amountObj.toString()
                                        .replace("¥", "")
                                        .replace("￥", "")
                                        .trim();
                                try {
                                    amount = Double.parseDouble(amountStr);
                                } catch (NumberFormatException e) {
                                    throw new RuntimeException("Invalid amount format: " + amountStr);
                                }
                            }

                            // Building Expense Object
                            return new Expense(
                                    parsedTime,
                                    amount,
                                    map.getOrDefault("counterpart", "").toString(),
                                    map.getOrDefault("product", "").toString(),
                                    "Expenditure",
                                    "Payment successful"
                            );

                        } catch (Exception e) {
                            System.err.println("Error parsing record: " + e.getMessage());
                            return null; // Returning null will be filtered later
                        }
                    })
                    .filter(Objects::nonNull) // Filter out records of parsing failures
                    .collect(Collectors.toList());

        } catch (Exception e) {
            Platform.runLater(() -> showErrorAlert("AI response parsing failed: " + e.getMessage()));
            return Collections.emptyList();
        }
    }
    /**
     * Shows error alert dialog
     * @param message Error message to display
     */
    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input format error");
        alert.setHeaderText("AI cannot recognize the input content");
        alert.setContentText(message + "\n Recommended inspection:\n1. Include complete transaction records\n2. Whether the amount unit is clear\n3. Whether the time format is correct");

        // Add operable buttons
        ButtonType retryButton = new ButtonType("Reenter", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(retryButton, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == retryButton) {
            showTextInputDialog();
        }
    }
    /**
     * Saves expenses to CSV file
     * @param filePath Path to save the CSV file
     */
    public void saveExpensesToCSV(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

            int headerIndex = -1;
            int dataEndIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (headerIndex == -1 && line.startsWith("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status")) {
                    headerIndex = i;
                }
                if (headerIndex != -1 && i > headerIndex && (line.startsWith("----------------------") || line.isEmpty())) {
                    dataEndIndex = i;
                    break;
                }
            }
            if (dataEndIndex == -1) dataEndIndex = lines.size();

            // Keep the original data rows of non merchant consumption
            List<String> existingDataLines = lines.subList(headerIndex + 1, dataEndIndex).stream()
                    .filter(line -> {
                        String[] parts = line.split(",");
                        return parts.length > 1 && !parts[1].contains("Merchant Consumption");
                    })
                    .collect(Collectors.toList());

            // Generate all current valid data rows (including newly added ones)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<String> currentDataLines = expenses.stream()
                    .map(e -> String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\"",
                            e.getTransactionTime().format(formatter),
                            "Merchant Consumption", // Mandatory type for merchant consumption
                            e.getCounterpart(),
                            e.getProduct(),
                            e.getType(),
                            e.getAmount(),
                            "Change",
                            e.getStatus()))
                    .collect(Collectors.toList());

            // Merge data: retain original non merchant consumption data+current consumption data of all merchants
            List<String> mergedData = new ArrayList<>();
            mergedData.addAll(existingDataLines);
            mergedData.addAll(currentDataLines);

            // Build new file content
            List<String> newLines = new ArrayList<>();
            newLines.addAll(lines.subList(0, headerIndex + 1));
            newLines.addAll(mergedData);
            newLines.addAll(lines.subList(dataEndIndex, lines.size()));

            Files.write(Paths.get(filePath), newLines, StandardCharsets.UTF_8);

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage()).show();
        }
    }

    /**
     * Updates the UI with current data
     */
    public void updateUI() {
        // Security testing
        if (pieChart != null) {
            pieChart.getData().clear();
            pieChart.getData().addAll(categoryTotals.entrySet().stream()
                    .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList()));
        }

        if (balanceValue != null) {
            balanceValue.setText(String.format("¥ %.2f", totalExpenditure));
        }
        VBox newProgressSection = createProgressSection();
        int progressIndex = leftPanel.getChildren().indexOf(progressSection);
        if (progressIndex != -1) {
            leftPanel.getChildren().set(progressIndex, newProgressSection);
            progressSection = newProgressSection;
        }
    }

    /**
     * Creates the bottom navigation bar
     * @return HBox containing navigation buttons
     */
    public HBox createBottomNavigation() {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // Create navigation buttons with icons
        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        // Set the initial color of the Home button
        setButtonColor(homeBtn, true);  // Purple
        setButtonColor(discoverBtn, false); // Default gray
        setButtonColor(settingsBtn, false); // Default gray

        // Event handling (maintaining original logic)
        homeBtn.setOnAction(e -> handleNavigation(homeBtn, new Nutllet()));
        discoverBtn.setOnAction(e -> handleNavigation(discoverBtn, new Discover()));
        settingsBtn.setOnAction(e -> handleNavigation(settingsBtn, new Settings()));

        // Arrange buttons from right to left
        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);

        // Set equal width buttons
        HBox.setHgrow(settingsBtn, Priority.ALWAYS);
        HBox.setHgrow(discoverBtn, Priority.ALWAYS);
        HBox.setHgrow(homeBtn, Priority.ALWAYS);

        return navBar;
    }

    /**
     * Handles navigation between different views
     * @param sourceButton Button that triggered navigation
     * @param app Application to navigate to
     */
    public void handleNavigation(Button sourceButton, Application app) {
        try {
            app.start(new Stage());
            ((Stage) sourceButton.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a navigation button with emoji icon
     * @param label Button text label
     * @param emoji Emoji icon to display
     * @return Styled Button with emoji and text
     */
    public Button createNavButtonWithEmoji(String label, String emoji) {
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(2);

        // Emoji tags
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        // Text label
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        // Button Style Settings
        Button button = new Button();
        button.setPrefWidth(456);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        // Hover effect
        button.hoverProperty().addListener((obs, oldVal, isHovering) -> {
            // Get the activation status of the button
            Boolean isActive = (Boolean) button.getUserData();

            // If it is in an active state, do not change the background color
            if (isActive != null && isActive) return;

            // Non activated button handling hover effect
            String bgColor = isHovering ? "#f8f9fa" : "white";
            button.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: transparent;");
        });

        return button;
    }

    /**
     * Sets button color based on active state
     * @param button Button to style
     * @param isActive Whether the button is active
     */
    public void setButtonColor(Button button, boolean isActive) {
        VBox container = (VBox) button.getGraphic();
        String color = isActive ? "#855FAF" : "#7f8c8d";

        // Store activation status to button attributes
        button.setUserData(isActive); // newly added

        // Set text color
        for (Node node : container.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: " + color + ";");
            }
        }

        // Force activation button background color setting
        String bgColor = isActive ? "#f8f9fa" : "white"; // Remove hover judgment
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: transparent;");
    }

    /**
     * Creates a pie chart for expense visualization
     * @return PieChart with expense data
     */
    public PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.getData().addAll(
                categoryTotals.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
        chart.setLegendVisible(false);
        chart.setStyle("-fx-border-color: #f0f0f0;");
        chart.setPrefSize(350, 250);
        return chart;
    }

    /**
     * Custom cell renderer for transaction list items
     */
    class TransactionCell extends ListCell<String> {
        public final HBox container;
        public final Label timeLabel;
        public final Label categoryLabel;
        public final Label amountLabel;
        public final Label dateLabel;
        public final Button deleteButton;

        public TransactionCell() {
            super();

            // Initialize UI components
            timeLabel = new Label();
            timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

            categoryLabel = new Label();
            categoryLabel.setStyle("-fx-font-weight: bold;");

            VBox timeBox = new VBox(2, timeLabel, categoryLabel);

            amountLabel = new Label();
            amountLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

            dateLabel = new Label();
            dateLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

            deleteButton = new Button("×");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-weight: bold;");
            deleteButton.setVisible(false);
            deleteButton.setOnAction(e -> handleDelete());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            container = new HBox(20, timeBox, spacer, amountLabel, dateLabel, deleteButton);
            container.setPadding(new Insets(8, 15, 8, 15));
            container.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250), CornerRadii.EMPTY, Insets.EMPTY)));

            // Hover the mouse to display the delete button
            setOnMouseEntered(e -> deleteButton.setVisible(true));
            setOnMouseExited(e -> deleteButton.setVisible(false));
        }

        public void handleDelete() {
            int index = getIndex();
            if (index < 0 || index >= sortedExpenses.size()) return;

            // Confirm Dialog
            Expense toRemove = sortedExpenses.get(index);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Entry");
            alert.setHeaderText("Are you sure you want to delete this record?");
            alert.setContentText(String.format("%s - ¥%.2f", toRemove.getProduct(), toRemove.getAmount()));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Remove and update from raw data
                expenses.remove(toRemove);
                processData(expenses);
                saveExpensesToCSV("deals.csv");
                updateUI();
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                // Analyze data display
                String[] parts = item.split(" - ");
                String[] timeCat = parts[0].split(" • ");

                timeLabel.setText(timeCat[0]);
                categoryLabel.setText(timeCat[1]);
                categoryLabel.setStyle("-fx-text-fill: " + (getIndex() % 2 == 0 ? "#855FAF" : "#333333"));
                amountLabel.setText(parts[1]);
                dateLabel.setText(parts[2]);

                // Set alternate background colors
                BackgroundFill bgFill = new BackgroundFill(
                        getIndex() % 2 == 0 ? Color.rgb(250, 250, 250) : Color.WHITE,
                        CornerRadii.EMPTY, Insets.EMPTY
                );
                container.setBackground(new Background(bgFill));

                setGraphic(container);
            }
        }
    }

    /**
     * Creates the progress section showing financial goals
     * @return VBox containing progress indicators
     */
    public VBox createProgressSection() {
        VBox progressBox = new VBox(15);
        progressBox.setPadding(new Insets(10, 0, 0, 0));

        List<Reminder> reminders = loadReminders().stream()
                .limit(3) // Only take the first three reminders
                .collect(Collectors.toList());

        for (Reminder reminder : reminders) {
            VBox container = new VBox(8);
            container.setPadding(new Insets(0, 15, 0, 5));

            HBox labelRow = new HBox();
            labelRow.setAlignment(Pos.CENTER_LEFT);

            // Title and Amount Range
            Label titleLabel = new Label(reminder.name);
            titleLabel.setStyle("-fx-text-fill: #333333;-fx-font-family: 'Segoe UI';-fx-font-weight: bold;-fx-font-size: 14px;");

            Label amountLabel = new Label(String.format("¥%.0f-%.0f",
                    reminder.minAmount,
                    reminder.maxAmount));
            amountLabel.setStyle("-fx-text-fill: #666666;-fx-font-family: 'Segoe UI';-fx-font-size: 13px;");
            HBox.setMargin(amountLabel, new Insets(0, 0, 0, 10));

            // Progress bar container
            StackPane progressContainer = new StackPane();
            progressContainer.setStyle("-fx-background-color: #F5F1FF;-fx-pref-height: 20px;-fx-border-radius: 10;");

            // Dynamic progress bar
            ProgressBar progressBar = new ProgressBar(reminder.progress / 100);
            progressBar.setStyle("-fx-accent: #855FAF;-fx-background-color: transparent;-fx-pref-width: 400px;-fx-pref-height: 20px;");

            // Progress description
            Label progressLabel = new Label(reminder.progressText);
            progressLabel.setStyle("-fx-text-fill: #666666;-fx-font-family: 'Segoe UI';-fx-font-size: 13px;");

            // Assemble components
            labelRow.getChildren().addAll(titleLabel, amountLabel);
            progressContainer.getChildren().add(progressBar);
            container.getChildren().addAll(labelRow, progressContainer, progressLabel);
            progressBox.getChildren().add(container);
        }

        return progressBox;
    }

    /**
     * Handles navigation item clicks
     * @param item Navigation item clicked
     */
    public void handleNavClick(String item) {
        System.out.println("Navigation switching: " + item);
    }

    /**
     * Creates the button panel for reminder management
     * @return HBox containing reminder management buttons
     */
    public HBox createButtonPanel() {
        Button modifyBtn = new Button("Modification reminder");
        modifyBtn.setStyle("-fx-background-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-padding: 8 20;");
        modifyBtn.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
                ((Stage) modifyBtn.getScene().getWindow()).close(); // Close the current page
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button detailsBtn = new Button("more details");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-radius: 3; -fx-padding: 8 20;");
        detailsBtn.setOnAction(e -> {
            try {
                new NutlletReminder().start(new Stage());
                ((Stage) detailsBtn.getScene().getWindow()).close(); // Close the current page
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(15, modifyBtn, detailsBtn);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        return buttonBox;
    }

    /**
     * Loads expenses from CSV file
     * @param filePath Path to the CSV file
     */
    public void loadExpensesFromCSV(String filePath) {
        expenses.clear(); // Use initialized member variables
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isDataSection = false;
            List<String> headers = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.contains("WeChat Payment Statement Details List")) {
                    isDataSection = true;
                    headers = Arrays.asList(br.readLine().split(",")); //Read title line
                    continue;
                }

                if (isDataSection && !line.trim().isEmpty()) {
                    Map<String, String> record = parseCSVLine(line, headers);

                    // Ensure the correctness of fields
                    if ("Expenditure".equals(record.get("Income/Expense")) &&
                            "Merchant Consumption".equals(record.get("Transaction Type"))&&
                            "Payment successful".equals(record.get("Current Status"))) {

                        // Clearly analyze all necessary fields
                        LocalDateTime time = LocalDateTime.parse(
                                record.get("Transaction Time"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                        double amount = Double.parseDouble(
                                record.get("Amount (Yuan)").replace("¥", "").trim()
                        );
                        String counterpart = record.get("Counterparty");
                        String product = record.get("Product");
                        String type = record.get("Income/Expense");
                        String status = record.get("Current Status");

                        // Create Expense object correctly and add it to the list
                        Expense expense = new Expense(
                                time, amount, counterpart, product, type, status
                        );
                        expenses.add(expense);
                    }
                }
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load data: " + e.getMessage()).show();
        }
    }

    /**
     * Parses a line from CSV file into a map of values
     * @param line CSV line to parse
     * @param headers List of column headers
     * @return Map of column names to values
     */
    public Map<String, String> parseCSVLine(String line, List<String> headers) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        Map<String, String> record = new HashMap<>();
        for (int i = 0; i < headers.size() && i < values.length; i++) {
            String value = values[i].replaceAll("^\"|\"$", "").trim();
            record.put(headers.get(i), value);
        }
        return record;
    }

    /**
     * Processes expense data and updates statistics
     * @param expenses List of expenses to process
     */
    public void processData(List<Expense> expenses) {
        // 分类统计
        for (Expense expense : expenses) {
            if (expense.getCategory() == null) {
                String initialCategory = categorizeExpense(expense);
                expense.setCategory(initialCategory);
                if ("Other".equals(initialCategory)) {
                    triggerAIClassification(expense);
                }
            }
        }

        // Update statistical logic
        categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory(),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        // Calculate total expenditure
        totalExpenditure = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Format transaction records
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy");

        sortedExpenses = expenses.stream()
                .sorted(Comparator.comparing(Expense::getTransactionTime).reversed())
                .collect(Collectors.toList());

        transactionItems.setAll(sortedExpenses.stream()
                .sorted(Comparator.comparing(Expense::getTransactionTime).reversed())
                .map(e -> String.format("%s • %s - ¥%.2f - %s",
                        e.getTransactionTime().format(timeFormatter),
                        e.getCategory(),
                        e.getAmount(),
                        e.getTransactionTime().format(dateFormatter)))
                .collect(Collectors.toList()));
    }
    /**
     * Triggers AI classification for uncategorized expenses
     * @param expense Expense to classify
     */
    public void triggerAIClassification(Expense expense) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Process process = new ProcessBuilder("ollama", "run", "qwen2:1.5b").start();

                // Build classification prompts
                String prompt = String.format(
                        "STRICT CLASSIFICATION TASK\n" +
                                "Please classify the consumption records according to the following criteria (only return the name of the English category):\n" +
                                "1. Catering - Catering consumption (restaurants, takeout, beverage stores)\n" +
                                "2. Traffic - Transportation expenses (travel tools, cars, high-speed rail, aircraft, refueling)\n" +
                                "3. Entertainment - Entertainment consumption (film and television, games, performances)\n" +
                                "4. Living - Living consumption (supermarkets, daily necessities)\n" +
                                "5. Periodic - Regular expenses (subscription, loan)\n" +
                                "6. Social - Social expenses (transfer, red envelope)\n" +
                                "7. Funds flow - Capital flow (financial management, repayment)\n" +
                                "Examples：\n" +
                                "Input: counterparty「Starbucks」，Product「large American」 → Catering\n" +
                                "Input: counterparty「Airline company」，Product「Air ticket」 → Traffic\n\n" +
                                "Please classify：\n" +
                                "Counterparty：%s\n" +
                                "Product：%s\n" +
                                "ANSWER:",
                        expense.getCounterpart(),
                        expense.getProduct()
                );


                // Send request
                OutputStream stdin = process.getOutputStream();
                stdin.write(prompt.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
                stdin.close();

                // Read response
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                String response = reader.lines().collect(Collectors.joining());

                // Analyze and validate the classification results
                String aiCategory = parseAICategory(response);

                // Update UI
                Platform.runLater(() -> {
                    expense.setCategory(aiCategory);
                    processData(expenses);
                    updateUI();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    expense.setCategory("Other");
                    updateUI();
                });
            }
        });
    }

    /**
     * Parses AI category response
     * @param response Raw AI response text
     * @return Parsed category name
     */
    public String parseAICategory(String response) {
        // Extract the first matching valid classification
        Pattern pattern = Pattern.compile(
                "(Traffic|Entertainment|Catering|Living|Periodic|Social|Funds flow)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1) : "Other";
    }
    /**
     * Categorizes an expense based on its details
     * @param expense Expense to categorize
     * @return Category name for the expense
     */
    public String categorizeExpense(Expense expense) {
        String counterpart = expense.getCounterpart().toLowerCase();
        String product = expense.getProduct().toLowerCase();

        if (counterpart.contains("meituan") || counterpart.contains("restaurant") || counterpart.contains("canteen") || product.contains("meal") || product.contains("tea") || counterpart.contains("nongfu spring")|| counterpart.contains("yuxi technology")) {
            return "Catering";
        } else if (counterpart.contains("didi") || counterpart.contains("petrochemical") || counterpart.contains("gasoline") || counterpart.contains("yikatong") || counterpart.contains("ctrip")|| counterpart.contains("air")) {
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
     * Inner class representing an expense record
     */
    public static class Expense {
        public LocalDateTime transactionTime;
        public double amount;
        public String counterpart;
        public String product;
        public String type;
        public String status;
        public String category;

        /**
         * Gets the expense category
         * @return Category name
         */
        public String getCategory() {
            return category;
        }

        /**
         * Sets the expense category
         * @param category Category name to set
         */
        public void setCategory(String category) {
            this.category = category;
        }
        /**
         * Creates a new Expense instance
         * @param transactionTime Time of the transaction
         * @param amount Transaction amount
         * @param counterpart Transaction counterpart
         * @param product Product description
         * @param type Transaction type
         * @param status Transaction status
         */
        public Expense(LocalDateTime transactionTime, double amount, String counterpart,
                       String product, String type, String status) {
            this.transactionTime = transactionTime;
            this.amount = amount;
            this.counterpart = counterpart;
            this.product = product;
            this.type = type;
            this.status = status;
        }
        public LocalDateTime getTransactionTime() { return transactionTime; }
        public double getAmount() { return amount; }
        public String getCounterpart() { return counterpart; }
        public String getProduct() { return product; }
        // Supplementary Getter Methods
        public String getType() { return type; }
        public String getStatus() { return status; }
        public void setTransactionTime(LocalDateTime transactionTime) {
            this.transactionTime = transactionTime;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setCounterpart(String counterpart) {
            this.counterpart = counterpart;
        }

        public void setProduct(String product) {
            this.product = product;
        }
        @Override
        public String toString() {
            return String.format("%s - %s - ¥%.2f",
                    transactionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    counterpart, amount);
        }
    }
    /**
     * Main method to launch the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}