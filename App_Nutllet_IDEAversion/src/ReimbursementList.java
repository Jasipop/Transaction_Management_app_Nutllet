//package Merge;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFX application for managing reimbursement items.
 * This application provides a user interface for viewing, searching, and managing reimbursement records
 * with features such as item listing, search functionality, and item deletion.
 *
 * @author Jiachen Hou
 * @version final
 */
public class ReimbursementList extends Application {

    private final List<HBox> allItems = new ArrayList<>();
    private VBox itemsContainer;
    private List<String[]> csvData = new ArrayList<>();

    /**
     * The main entry point for the JavaFX application.
     * Initializes and displays the reimbursement list interface with all necessary components
     * including search functionality, item listing, and navigation elements.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        BorderPane rootplus = new BorderPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        Text title = new Text("Reimbursements Items");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

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

        itemsContainer = new VBox();
        itemsContainer.setSpacing(10);
        itemsContainer.setPadding(new Insets(10));
        itemsContainer.setAlignment(Pos.CENTER);

        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                // Parse CSV line with quotes
                String[] parts = parseCsvLine(line);
                if (parts.length >= 8 && "Reimbursement".equals(parts[1])) {
                    csvData.add(parts);
                    String category = parts[2];
                    String detail = parts[3];  
                    String amount = parts[5].substring(1); 
                    String date = parts[0];    
                    String status = parts[7]; 
                    String person = parts.length > 10 ? parts[9] : "";

                    HBox item = createItem(category, detail, amount, date, status, person, csvData.size() - 1);
                    allItems.add(item);
                    itemsContainer.getChildren().add(item);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(600);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue.toLowerCase().trim();
            itemsContainer.getChildren().clear();
            for (HBox item : allItems) {
                boolean matched = false;
                for (javafx.scene.Node node : item.getChildren()) {
                    if (node instanceof Text t && t.getText().toLowerCase().contains(keyword)) {
                        matched = true;
                        break;
                    }
                    if (node instanceof VBox || node instanceof HBox) {
                        matched = matched || searchTextNodesRecursively(node, keyword);
                    }
                }
                if (matched) {
                    itemsContainer.getChildren().add(item);
                }
            }
        });

        mainLayout.getChildren().addAll(titleBox, searchBox, scrollPane);

        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-color: " + Color.web("#855FAF").toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-font-size: 27px; -fx-font-weight: bold;");
        addButton.setShape(new Circle(30));
        addButton.setMinSize(60, 60);
        addButton.setMaxSize(60, 60);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.3), addButton);
        addButton.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
            scaleTransition.play();
        });
        addButton.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });

        StackPane.setAlignment(addButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 20, 20, 0));

        addButton.setOnAction(e -> {
            try {
                new ReimbursementAddNew().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(mainLayout, addButton);
        rootplus.setCenter(root);

        // Bottom Navigation Bar
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
        rootplus.setBottom(navBar);

        Scene scene = new Scene(rootplus, 1366, 768);
        primaryStage.setTitle("Reimbursements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Parses a CSV line while handling quoted values correctly.
     * This method ensures that commas within quoted values are not treated as delimiters.
     *
     * @param line The CSV line to parse
     * @return An array of strings containing the parsed values
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
     * Deletes a reimbursement item from both the UI and the underlying CSV file.
     * Shows confirmation dialog before deletion and handles any errors that may occur.
     *
     * @param index The index of the item to delete
     */
    private void deleteItem(int index) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Sure you want to delete this reimbursement?");
        alert.setContentText("Deletion will not be recovered");

        ButtonType buttonTypeYes = new ButtonType("Confirm");
        ButtonType buttonTypeNo = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                // Get the transaction data to be deleted
                String[] deletedData = csvData.get(index);
                String deletedLine = findMatchingLineInCSV(deletedData);

                if (deletedLine.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Transaction not found");
                    errorAlert.setContentText("Could not find matching transaction in CSV file");
                    errorAlert.showAndWait();
                    return;
                }

                // Delete data from memory
                csvData.remove(index);
                allItems.remove(index);
                itemsContainer.getChildren().remove(index);

                // Update CSV file
                try {
                    // Read original file
                    List<String> lines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
                        String line;
                        boolean headerSkipped = false;
                        while ((line = reader.readLine()) != null) {
                            if (!headerSkipped) {
                                headerSkipped = true;
                                lines.add(line);
                                continue;
                            }

                            // Skip the line to be deleted
                            if (line.equals(deletedLine)) {
                                continue;
                            }
                            lines.add(line);
                        }
                    }

                    // Write updated file
                    try (FileWriter writer = new FileWriter("deals.csv")) {
                        for (String line : lines) {
                            writer.write(line + "\n");
                        }
                    }

                    // Update indices of remaining items
                    for (int i = 0; i < allItems.size(); i++) {
                        HBox item = allItems.get(i);
                        for (javafx.scene.Node node : item.getChildren()) {
                            if (node instanceof HBox) {
                                for (javafx.scene.Node btnNode : ((HBox) node).getChildren()) {
                                    if (btnNode instanceof Button && ((Button) btnNode).getText().equals("×")) {
                                        Button deleteBtn = (Button) btnNode;
                                        final int newIndex = i;
                                        deleteBtn.setOnAction(e -> deleteItem(newIndex));
                                    }
                                }
                            }
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Successful Operation");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Reimbursement deleted successfully");
                    successAlert.showAndWait();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to delete reimbursement");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    /**
     * Finds the matching line in the CSV file for a given data array.
     * Compares all key fields to ensure the correct line is identified.
     *
     * @param data The data array containing the item's information
     * @return The matching line from the CSV file, or an empty string if not found
     */
    private String findMatchingLineInCSV(String[] data) {
        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length >= 8 && "Reimbursement".equals(parts[1])) {
                    // Check matching fields
                    if (parts[0].equals(data[0]) &&  
                            parts[2].equals(data[2]) &&  
                            parts[3].equals(data[3]) &&  
                            parts[5].equals(data[5]) &&  
                            parts[7].equals(data[7])) {  
                        return line;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Refreshes the UI by reloading all reimbursement items from the CSV file.
     * Clears existing items and rebuilds the display with current data.
     */
    private void refreshUI() {
        allItems.clear();
        itemsContainer.getChildren().clear();
        csvData.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length >= 8 && "Reimbursement".equals(parts[1])) {
                    csvData.add(parts);
                    String category = parts[2];
                    String detail = parts[3];
                    String amount = parts[5].substring(1);
                    String date = parts[0];
                    String status = parts[7];
                    String person = parts.length > 10 ? parts[10] : "";

                    HBox item = createItem(category, detail, amount, date, status, person, csvData.size() - 1);
                    allItems.add(item);
                    itemsContainer.getChildren().add(item);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a navigation button with an emoji icon and label.
     * Used for the bottom navigation bar buttons.
     *
     * @param label The text label for the button
     * @param emoji The emoji icon to display
     * @return A styled Button with the specified label and emoji
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
     * Creates a reimbursement item display box with all its components.
     * Includes amount, category, details, status, and action buttons.
     *
     * @param category The category of the reimbursement
     * @param detail The detailed description
     * @param amount The reimbursement amount
     * @param date The date of the reimbursement
     * @param status The current status
     * @param person The responsible person
     * @param index The index of the item in the list
     * @return An HBox containing the formatted item display
     */
    private HBox createItem(String category, String detail, String amount, String date, String status, String person, int index) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(15);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        itemBox.setMaxWidth(800);

        Text amountLabel = new Text("-" + amount);
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        amountLabel.setFill(Color.RED);

        HBox categoryDetailBox = new HBox();
        categoryDetailBox.setSpacing(10);
        categoryDetailBox.setAlignment(Pos.CENTER_LEFT);

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Text categoryLabel = new Text(category);
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        categoryLabel.setFill(Color.web("#2c3e50"));

        Text detailLabel = new Text(detail);
        detailLabel.setFont(Font.font("Arial", 16));
        detailLabel.setFill(Color.web("#2c3e50"));

        Text statusLabel = new Text("Status: " + status);
        statusLabel.setFont(Font.font("Arial", 14));
        statusLabel.setFill(status.equals("Yes") ? Color.web("#e74c3c") : Color.web("#7f8c8d"));

        Text personLabel = new Text("Person: " + person);
        personLabel.setFont(Font.font("Arial", 14));
        personLabel.setFill(Color.web("#7f8c8d"));

        categoryDetailBox.getChildren().addAll(categoryLabel, detailLabel);

        Text dateLabel = new Text(date);
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setFill(Color.web("#7f8c8d"));

        detailsBox.getChildren().addAll(categoryDetailBox, dateLabel, statusLabel, personLabel);

        ToggleButton starButton = createStarToggleButton();

        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 20px; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteItem(index));

        HBox buttonsBox = new HBox(10, starButton, deleteButton);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        itemBox.getChildren().addAll(amountLabel, detailsBox, buttonsBox);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        return itemBox;
    }

    /**
     * Creates a star toggle button for marking important items.
     * Includes SVG star icon and color transition effects.
     *
     * @return A ToggleButton with star icon and styling
     */
    private ToggleButton createStarToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        SVGPath star = new SVGPath();
        star.setContent("M12,17.27L18.18,21l-1.64-7.03L22,9.24l-7.19-0.61L12,2L9.19,8.63L2,9.24l5.46,4.73L5.82,21L12,17.27z");

        star.setFill(Color.web("#bdc3c7"));
        star.setStroke(Color.web("#bdc3c7"));

        toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                star.setFill(Color.web("#FFD700"));
                star.setStroke(Color.web("#FFD700"));
            } else {
                star.setFill(Color.web("#bdc3c7"));
                star.setStroke(Color.web("#bdc3c7"));
            }
        });

        toggleButton.setGraphic(star);
        return toggleButton;
    }

    /**
     * Recursively searches through text nodes in the UI to find matching keywords.
     * Used for implementing the search functionality.
     *
     * @param node The node to search in
     * @param keyword The keyword to search for
     * @return true if the keyword is found, false otherwise
     */
    private boolean searchTextNodesRecursively(javafx.scene.Node node, String keyword) {
        if (node instanceof Text t && t.getText().toLowerCase().contains(keyword)) {
            return true;
        } else if (node instanceof Pane pane) {
            for (javafx.scene.Node child : pane.getChildren()) {
                if (searchTextNodesRecursively(child, keyword)) return true;
            }
        }
        return false;
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