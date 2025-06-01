//package Merge;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A JavaFX application that implements a mailbox interface for displaying and managing messages.
 * The application provides features such as message listing, searching, and detailed message viewing
 * with a modern and interactive user interface.
 *
 * @author Jiachen Hou
 * @version final
 */
public class Mailbox extends Application {

    private VBox itemsContainer;
    private String[] titles, descriptions, emojis;

    /** Array of pastel colors for message backgrounds */
    private final String[] pastelColors = {
            "#D8F0FF", "#D0ECFF", "#C8E8FF",
            "#C0E4FF", "#B8E0FF", "#B0DCFF", "#A8D8FF"
    };
    private Stage currentStage;

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the mailbox interface.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {

        this.currentStage = primaryStage;
        this.titles = new String[]{
                "System Update v1.1.3", "New Message Received", "Project Deadline Incoming",
                "Team Meeting Reminder", "Monthly Report Ready", "Account Security Alert",
                "New Feature Available", "Scheduled Maintenance Notice", "Nutllet is Now Live",
                "v1.1.2 Update Released"
        };

        this.descriptions = new String[]{
                "A new system update (v1.1.3) is now available. Please download and install it for the best experience.",
                "You have received a new message from John. Check your inbox to read it.",
                "Your project submission is due tomorrow. Make sure everything is finalized.",
                "Reminder: There will be a team meeting today at 3:00 PM. Please be prepared.",
                "Your monthly activity report is ready. View it now to stay on top of your progress.",
                "A critical security update is required. Please update your account credentials if prompted.",
                "A new productivity tool has been added. Explore it now to enhance your workflow.",
                "Scheduled system maintenance will occur this Friday at 2:00 AM. Please save your work in advance.",
                "Nutllet is now officially available! Thank you for joining us on this journey.",
                "A new version (v1.1.2) has been released. Reimbursement functionality is now available. Please update to enjoy the new features."
        };

        this.emojis = new String[]{
                "🔄", "👋", "🔧", "✨", "🔒", "📊", "👥", "⏰", "✉️", "🖥️"
        };
        BorderPane root = new BorderPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setBackground(new Background(new BackgroundFill(
                Color.web("#E6F0FF", 0.3),
                new CornerRadii(0), Insets.EMPTY)));

        Text title = new Text("MailBox");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#3A5FCD"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        // Search box
        TextField searchField = new TextField();
        searchField.setPromptText("Search messages...");
        searchField.setPrefWidth(750);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 4; -fx-padding: 10 15; -fx-min-height: 40px; -fx-font-size: 16px;");
        searchField.setAlignment(Pos.CENTER);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d;");

        HBox searchBox = new HBox(10, searchIcon, searchField);
        searchBox.setPrefWidth(800);
        searchBox.setAlignment(Pos.CENTER);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterMessages(newValue.trim().toLowerCase());
        });


        ImageView mailboxIcon = new ImageView(new Image("mailbox.png"));
        mailboxIcon.setFitHeight(100);
        mailboxIcon.setPreserveRatio(true);

        // Create container with scrollbar
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; " +
                "-fx-scrollbar-color: #3A5FCD #E6F0FF;");

        itemsContainer = new VBox();
        itemsContainer.setSpacing(15);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setPadding(new Insets(0, 10, 20, 10));

        // Put itemsContainer into scrollPane
        scrollPane.setContent(itemsContainer);

        String[] pastelColors = {
                "#D8F0FF", "#D0ECFF", "#C8E8FF",
                "#C0E4FF", "#B8E0FF", "#B0DCFF", "#A8D8FF"
        };

        // Create notification buttons (add 10 to display scroll effect)
        for (int i = 0; i < titles.length; i++) {
            Button btn = createMessageButton(primaryStage, titles[i], descriptions[i], emojis[i], pastelColors[i % pastelColors.length]);
            addHoverAnimation(btn);
            itemsContainer.getChildren().add(btn);
        }

        // Message count label
        Label messageCountLabel = new Label(titles.length + " new messages");
        messageCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        messageCountLabel.setStyle("-fx-text-fill: #FF3B30;");

        mainLayout.getChildren().addAll(titleBox, mailboxIcon, messageCountLabel, searchBox, scrollPane);

        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙️

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn); // 从右到左

        root.setBottom(navBar);
        root.setCenter(mainLayout);


        // Dynamically calculate scroll panel height
        Scene scene = new Scene(root, 1366, 768);
        scrollPane.prefHeightProperty().bind(scene.heightProperty().subtract(250));

        // Entrance animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1), mainLayout);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), mainLayout);
        slide.setFromY(50);
        slide.setToY(0);

        fade.play();
        slide.play();

        primaryStage.setTitle("MailBox");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Filters messages based on the provided keyword.
     * Updates the display to show only messages containing the keyword in their title or description.
     *
     * @param keyword The search term to filter messages
     */
    private void filterMessages(String keyword) {
        itemsContainer.getChildren().clear();  // Clear old message buttons

        boolean found = false;

        for (int i = 0; i < titles.length; i++) {
            if (titles[i].toLowerCase().contains(keyword) ||
                    descriptions[i].toLowerCase().contains(keyword)) {
                Button btn = createMessageButton(currentStage, titles[i], descriptions[i], emojis[i], pastelColors[i % pastelColors.length]);
                addHoverAnimation(btn);
                itemsContainer.getChildren().add(btn);
                found = true;
            }
        }

        if (!found) {
            Label noResult = new Label("No relevant information");
            noResult.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            noResult.setStyle("-fx-text-fill: #3f81d1;");
            noResult.setPadding(new Insets(20));
            itemsContainer.getChildren().add(noResult);
        }
    }



    /**
     * Creates a navigation button with an emoji icon.
     *
     * @param label The text label for the button
     * @param emoji The emoji symbol to display
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
     * Creates a basic navigation button.
     *
     * @param label The text label for the button
     * @return A styled Button
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
     * Adds hover animation effects to a button.
     * Includes scaling and shadow effects on mouse enter/exit.
     *
     * @param button The button to add hover effects to
     */
    private void addHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();

            button.setStyle(button.getStyle() +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();

            button.setStyle(button.getStyle()
                    .replace("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);", ""));
        });
    }

    /**
     * Creates a message button with title, description, emoji, and background color.
     *
     * @param primaryStage The primary stage of the application
     * @param title The message title
     * @param description The message description
     * @param emoji The emoji symbol for the message
     * @param bgColor The background color for the message button
     * @return A styled Button representing a message
     */
    private Button createMessageButton(Stage primaryStage, String title, String description, String emoji, String bgColor) {
        Button button = new Button();
        button.setMaxWidth(800);
        button.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12; " +
                "-fx-border-radius: 12; " +
                "-fx-padding: 15 20; " +
                "-fx-font-size: 16px; " +
                "-fx-text-alignment: left;");

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("Arial", 24));
        emojiLabel.setPadding(new Insets(0, 15, 0, 0));

        VBox textContent = new VBox();
        textContent.setSpacing(5);

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));

        Text descriptionText = new Text(description);
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#7f8c8d"));
        descriptionText.setWrappingWidth(700);

        Text timeText = new Text(generateTimestampForMessage(title));
        timeText.setFont(Font.font("Arial", 12));
        timeText.setFill(Color.web("#95a5a6"));
        timeText.setStyle("-fx-font-style: italic;");

        textContent.getChildren().addAll(titleText, descriptionText, timeText);

        HBox graphicBox = new HBox(10, emojiLabel, textContent);
        graphicBox.setAlignment(Pos.CENTER_LEFT);

        button.setGraphic(graphicBox);
        button.setOnAction(e -> showMessageDetail(primaryStage, title, description, emoji));

        return button;
    }

    /**
     * Generates a timestamp for a message based on its title.
     *
     * @param title The title of the message
     * @return A formatted timestamp string
     */
    private String generateTimestampForMessage(String title) {
        return switch (title) {
            case "System Update v1.1.3" -> "2025/4/3 12:00:09";
            case "New Message Received" -> "2025/4/2 09:30:45";
            case "Project Deadline Incoming" -> "2025/4/1 15:20:33";
            case "Team Meeting Reminder" -> "2025/3/31 11:15:22";
            case "Monthly Report Ready" -> "2025/3/30 14:05:18";
            case "Account Security Alert" -> "2025/3/29 10:00:00";
            case "New Feature Available" -> "2025/3/28 09:15:00";
            case "Scheduled Maintenance Notice" -> "2025/3/27 17:30:00";
            case "Nutllet is Now Live" -> "2025/3/26 08:45:12";
            case "v1.1.2 Update Released" -> "2025/3/25 13:20:05";
            default -> "2025/4/3 00:00:00";
        };
    }

    /**
     * Displays the detailed view of a message.
     * Shows the full message content with title, timestamp, and a back button.
     *
     * @param primaryStage The primary stage of the application
     * @param title The message title
     * @param content The message content
     * @param emoji The emoji symbol for the message
     */
    private void showMessageDetail(Stage primaryStage, String title, String content, String emoji) {
        VBox detailLayout = new VBox();
        detailLayout.setAlignment(Pos.CENTER);
        detailLayout.setSpacing(30);
        detailLayout.setPadding(new Insets(40));
        detailLayout.setStyle("-fx-background-color: #F5F9FF;");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("Arial", 48));

        VBox titleBox = new VBox(5);
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleText.setFill(Color.web("#3A5FCD"));

        Text timeText = new Text("Received: " + generateTimestampForMessage(title));
        timeText.setFont(Font.font("Arial", 14));
        timeText.setFill(Color.web("#95a5a6"));

        titleBox.getChildren().addAll(titleText, timeText);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        headerBox.getChildren().addAll(emojiLabel, titleBox);

        TextArea contentArea = new TextArea(content);
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setFont(Font.font("Arial", 16));
        contentArea.setStyle("-fx-background-color: transparent; -fx-border-color: #D3D3D3;");
        contentArea.setPrefSize(900, 400);

        Button backBtn = new Button("← Back to MailBox");
        backBtn.setStyle("-fx-background-color: #3A5FCD; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 6;");
        backBtn.setOnAction(e -> start(primaryStage));

        detailLayout.getChildren().addAll(headerBox, contentArea, backBtn);

        Scene detailScene = new Scene(detailLayout, 1366, 768);
        primaryStage.setScene(detailScene);
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