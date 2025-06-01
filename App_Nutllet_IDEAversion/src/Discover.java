//package Merge;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
import javafx.util.Duration;

/**
 * The Discover class represents the main discovery page of the application.
 * It provides a user interface for exploring various features and functionalities of the financial management system.
 *
 * @author Jiachen Hou
 * @version final
 */
public class Discover extends Application {

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the primary stage with the discovery interface.
     * Integrate all functions on this page.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setBackground(new Background(new BackgroundFill(
                Color.web("#FFD4EC", 0.3), new CornerRadii(0), Insets.EMPTY)));

        Text title = new Text("Discover");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(750);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 4; -fx-padding: 10 15; -fx-min-height: 40px; -fx-font-size: 16px;");
        searchField.setAlignment(Pos.CENTER);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d;");

        HBox searchBox = new HBox(10, searchIcon, searchField);
        searchBox.setPrefWidth(800);
        searchBox.setAlignment(Pos.CENTER);

        VBox itemsContainer = new VBox();
        itemsContainer.setSpacing(15);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setPadding(new Insets(10));

        Label noResultsLabel = new Label("Relevant functions are building forward...");
        noResultsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        noResultsLabel.setTextFill(Color.GRAY);
        noResultsLabel.setVisible(false);

        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(600);

        String[] pastelColors = {
                "#FFB6C1B3", "#FFDAB9B3", "#FFFACDB3", "#E0FFFFB3", "#D8BFD8B3", "#C6E2FFB3", "#E6E6FAB3"
        };

        String[] titles = {
                "Transaction Management",
                "Expenditure Classification System",
                "AI Intelligent Classification",
                "Reimbursement Items",
                "Financial Analysis",
                "Bank Data Management",
                "International Currency Exchange",
                "Seasonal Spikes",
                "Reminders",
                "MailBox",
                "Settings",
                "Enterprise Edition",
                "Log out",
        };

        String[] descriptions = {
                "Manage your financial both online and offline",
                "Auto-categorize expenses with custom options",
                "AI transaction categorization with manual edits",
                "Track reimbursable/non-reimbursable funds",
                "Analyze trends and predict cash flow",
                "Import bank CSV files for reconciliation",
                "Real-time multi-currency tracking",
                "Special date budget alerts",
                "Set budget alerts and reminders",
                "Get app updates and admin notifications",
                "Set default and preference",
                "Open the Enterprise Edition",
                "Back to the login page",
        };

        for (int i = 0; i < titles.length; i++) {
            Button btn = createSettingButton(primaryStage, titles[i], descriptions[i], pastelColors[i % pastelColors.length]);
            addHoverAnimation(btn);
            itemsContainer.getChildren().add(btn);
        }

        mainLayout.getChildren().addAll(titleBox, searchBox, scrollPane, noResultsLabel);

        root.setCenter(mainLayout);

        // Bottom Navigation Bar with highlight
        HBox bottomNavigationBar = new HBox();
        bottomNavigationBar.setSpacing(0);
        bottomNavigationBar.setAlignment(Pos.CENTER);
        bottomNavigationBar.setPrefHeight(80);
        bottomNavigationBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        String currentPage = "Discover";

        Button homeButton = createNavButtonWithHighlight("Home", "🏠", currentPage.equals("Home"));
        Button discoverButton = createNavButtonWithHighlight("Discover", "🔍", currentPage.equals("Discover"));
        Button settingsButton = createNavButtonWithHighlight("Settings", "⚙", currentPage.equals("Settings"));

        homeButton.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverButton.setOnAction(e -> {
            // No navigation needed for current page
        });
        settingsButton.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        bottomNavigationBar.getChildren().addAll(homeButton, discoverButton, settingsButton);
        root.setBottom(bottomNavigationBar);

        // Search logic: Dynamically filter buttons based on title
        FilteredList<Button> filteredButtons = new FilteredList<>(
                FXCollections.observableArrayList(
                        itemsContainer.getChildren().filtered(n -> n instanceof Button).toArray(Button[]::new)
                ),
                p -> true
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredButtons.setPredicate(btn -> {
                if (btn.getGraphic() instanceof HBox graphic) {
                    for (Node n : graphic.getChildren()) {
                        if (n instanceof VBox textContent) {
                            for (Node labelNode : textContent.getChildren()) {
                                if (labelNode instanceof Text textNode && textNode.getFont().getStyle().contains("Bold")) {
                                    return textNode.getText().toLowerCase().contains(lower);
                                }
                            }
                        }
                    }
                }
                return false;
            });
            itemsContainer.getChildren().setAll(filteredButtons);
            noResultsLabel.setVisible(filteredButtons.isEmpty());

        });

        // Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1), mainLayout);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), mainLayout);
        slide.setFromY(50);
        slide.setToY(0);

        fade.play();
        slide.play();

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Discover");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates a navigation button with highlight effect for the bottom navigation bar.
     *
     * @param labelText The text to display on the button
     * @param emojiSymbol The emoji symbol to display
     * @param isActive Whether this button represents the active page
     * @return A styled Button with the specified content
     */
    private Button createNavButtonWithHighlight(String labelText, String emojiSymbol, boolean isActive) {
        VBox buttonContent = new VBox();
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setSpacing(2);

        Label emojiLabel = new Label(emojiSymbol);
        emojiLabel.setStyle("-fx-font-size: 16px;" + (isActive ? " -fx-text-fill: #855FAF;" : ""));

        Label textLabel = new Label(labelText);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isActive ? "#855FAF; -fx-font-weight: bold;" : "#7f8c8d;"));

        buttonContent.getChildren().addAll(emojiLabel, textLabel);

        Button navigationButton = new Button();
        navigationButton.setPrefWidth(456);
        navigationButton.setPrefHeight(80);
        navigationButton.setGraphic(buttonContent);
        navigationButton.setStyle("-fx-background-color: " + (isActive ? "#F0F0F0;" : "white;") + " -fx-border-color: transparent;");

        return navigationButton;
    }

    /**
     * Adds hover animation effects to a button.
     * Creates a scale transition when the mouse enters and exits the button.
     *
     * @param button The button to add hover effects to
     */
    private void addHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });
    }

    /**
     * Gets the appropriate emoji symbol for a given title.
     *
     * @param title The title to get the emoji for
     * @return The emoji symbol as a String
     */
    private String getEmojiForTitle(String title) {
        return switch (title) {
            case "Enterprise Edition" -> "👔";
            case "MailBox" -> "📬";
            case "Reminders" -> "⏰";
            case "Reimbursement Items" -> "💸";
            case "Financial Analysis" -> "📊";
            case "Transaction Management" -> "💳";
            case "Bank Data Management" -> "🏦";
            case "Expenditure Classification System" -> "👓";
            case "Seasonal Spikes" -> "📈";
            case "International Currency Exchange" -> "💱";
            case "AI Intelligent Classification" -> "🤖";
            case "Settings" -> "🍥";
            case "Log out" -> "✨";
            default -> "⚙";
        };
    }

    /**
     * Creates a settings button with the specified properties.
     *
     * @param primaryStage The primary stage of the application
     * @param title The title of the button
     * @param description The description text
     * @param bgColor The background color in hex format
     * @return A styled Button with the specified content
     */
    private Button createSettingButton(Stage primaryStage, String title, String description, String bgColor) {
        Button button = new Button();
        button.setMaxWidth(800);
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 15 20; -fx-font-size: 16px; -fx-text-alignment: left;");

        Label emojiLabel = new Label(getEmojiForTitle(title));
        emojiLabel.setFont(Font.font("Arial", 24));
        emojiLabel.setPadding(new Insets(0, 5, 0, 0));

        VBox textContent = new VBox();
        textContent.setSpacing(5);
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));

        Text descriptionText = new Text(description);
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#7f8c8d"));

        textContent.getChildren().addAll(titleText, descriptionText);

        HBox graphicBox = new HBox(10, emojiLabel, textContent);
        graphicBox.setAlignment(Pos.CENTER_LEFT);

        button.setGraphic(graphicBox);
        button.setOnAction(e -> openNewPage(primaryStage, title));

        return button;
    }

    /**
     * Opens a new page based on the selected title.
     * Handles navigation to different sections of the application.
     *
     * @param primaryStage The primary stage of the application
     * @param pageTitle The title of the page to open
     */
    private void openNewPage(Stage primaryStage, String pageTitle) {
        try {
            switch (pageTitle) {
                case "Enterprise Edition" -> new NutlletEnterprise().start(new Stage());
                case "MailBox" -> new Mailbox().start(new Stage());
                case "Reminders" -> new NutlletReminder().start(new Stage());
                case "Reimbursement Items" -> new ReimbursementList().start(new Stage());
                case "Financial Analysis" -> new FinancialAnalysis().start(new Stage());
                case "Transaction Management" -> new Transaction_Management_System().start(new Stage());
                case "Bank Data Management" -> new Bank_Data_Management().start(new Stage());
                case "Expenditure Classification System" -> new Free_Design_Classification().start(new Stage());
                case "International Currency Exchange" -> new InternationalList().start(new Stage());
                case "Seasonal Spikes" ->new UI_1().start(new Stage());
                case "AI Intelligent Classification" -> new Intelligent_Transaction_Classifier().start(new Stage());
                case "Settings" -> new Settings().start(new Stage());
                case "Log out" -> new Login().start(new Stage());
                default -> showDefaultWelcomePage(primaryStage, pageTitle);
            }
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows a default welcome page when a specific page is not implemented.
     *
     * @param primaryStage The primary stage of the application
     * @param pageTitle The title of the page to display
     */
    private void showDefaultWelcomePage(Stage primaryStage, String pageTitle) {
        VBox newPageLayout = new VBox();
        newPageLayout.setAlignment(Pos.CENTER);
        newPageLayout.setSpacing(20);
        newPageLayout.setStyle("-fx-background-color: #FFFBE6;");
        Text label = new Text("Welcome to: " + pageTitle);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        label.setFill(Color.web("#855FAF"));

        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: #855FAF; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 6;");
        backBtn.setOnAction(e -> start(primaryStage));

        newPageLayout.getChildren().addAll(label, backBtn);
        Scene newScene = new Scene(newPageLayout, 1366, 768);
        primaryStage.setScene(newScene);
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

