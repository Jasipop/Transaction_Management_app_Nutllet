//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A JavaFX application for creating and managing customizable financial classification systems.
 * <p>
 * This application allows users to:
 * - Define custom expenditure categories and subcategories
 * - Manage transaction groups with hierarchical structures
 * - Edit financial records with real-time calculations
 * - Export classification systems to Markdown format
 *
 * @author Jingyi Liang
 * @version final
 */
public class Free_Design_Classification extends Application {

    private ComboBox<String> groupComboBox;
    private TreeView<String> expenseTreeView;
    private TreeView<String> classificationTreeView;
    private double totalExpenditure = 0.0;
    private Label totalExpenditureLabel;
    private Map<String, TreeItem<String>> groupData;
    private Map<String, TreeItem<String>> classificationSystems;
    private String currentGroup;
    private List<String> addedNames = new ArrayList<>();
    private List<String> addedAmounts = new ArrayList<>();
    private Button exportButton;


    /**
     * JavaFX application entry point
     * @param primaryStage The primary window container
     */
    @Override
    public void start(Stage primaryStage) {
        initializeDataStructures();
        VBox mainLayout = createMainLayout(createMainContent());

        Scene scene = new Scene(mainLayout, 1366, 768);
        primaryStage.setTitle("Free Definition");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeSampleData();
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

        navBar.getChildren().addAll( homeBtn, discoverBtn,settingsBtn); // 从右到左

        mainLayout.getChildren().add(navBar);
    }
    /**
     * Initializes core data structures for storing classification information
     */
    private void initializeDataStructures() {
        groupData = new HashMap<>();
        classificationSystems = new HashMap<>();
    }
    /**
     * Populates sample data for demonstration purposes
     */
    private void initializeSampleData() {
        // Initialize groups
        String[] groups = {"Daily Expenses", "Experiment Group 1", "Project Expenses"};
        for (String group : groups) {
            TreeItem<String> rootItem = new TreeItem<>("Expense Classification");
            rootItem.setExpanded(true);
            groupData.put(group, rootItem);

            TreeItem<String> classRoot = new TreeItem<>("Classification System");
            classRoot.setExpanded(true);
            classificationSystems.put(group, classRoot);
        }

        // Add sample data for "Daily Expenses"
        addSampleDataForDailyExpenses();

        // Add sample data for "Project Expenses"
        addSampleDataForProjectExpenses();

        // Add sample data for "Experiment Group 1"
        addSampleDataForExperiment();

        // Set initial group
        currentGroup = "Daily Expenses";
        groupComboBox.getItems().addAll(groups);
        groupComboBox.setValue(currentGroup);

        // Set initial trees
        updateTrees();
    }
    /**
     * Initializes sample data for daily expenses category
     * <p>
     * Creates default categories for daily expenses in both classification systems
     */
    private void addSampleDataForDailyExpenses() {
        TreeItem<String> root = groupData.get("Daily Expenses");
        TreeItem<String> classRoot = classificationSystems.get("Daily Expenses");

        String[] newCategories = {
                "Food & Beverage", "Shopping", "Transportation",
                "Entertainment", "Education", "Fitness", "Utilities & Transfer"
        };

        for (String category : newCategories) {
            TreeItem<String> categoryNode = new TreeItem<>(category);
            root.getChildren().add(new TreeItem<>(category));      // 左侧支出分类结构
            classRoot.getChildren().add(new TreeItem<>(category)); // 右侧分类系统结构
        }
    }

    /**
     * Initializes sample project expenses with hierarchical structure
     * <p>
     * Creates hardware, software, HR and operations categories with sample transactions
     */
    private void addSampleDataForProjectExpenses() {
        TreeItem<String> root = groupData.get("Project Expenses");
        TreeItem<String> classRoot = classificationSystems.get("Project Expenses");

        // Hardware
        TreeItem<String> hardware = new TreeItem<>("Hardware");
        hardware.getChildren().addAll(
                new TreeItem<>("Server - ¥15000"),
                new TreeItem<>("Laptop - ¥8000"),
                new TreeItem<>("Monitor - ¥2000"),
                new TreeItem<>("Network - ¥1500")
        );

        // Software
        TreeItem<String> software = new TreeItem<>("Software");
        software.getChildren().addAll(
                new TreeItem<>("Dev Tools - ¥5000"),
                new TreeItem<>("Server OS - ¥3000"),
                new TreeItem<>("Design - ¥2400")
        );

        // Human Resources
        TreeItem<String> hr = new TreeItem<>("Human Resources");
        hr.getChildren().addAll(
                new TreeItem<>("Outsourcing - ¥20000"),
                new TreeItem<>("Design - ¥8000"),
                new TreeItem<>("Consulting - ¥5000")
        );

        // Operations
        TreeItem<String> operation = new TreeItem<>("Operations");
        operation.getChildren().addAll(
                new TreeItem<>("Cloud - ¥3000"),
                new TreeItem<>("Domain - ¥500"),
                new TreeItem<>("Office - ¥10000")
        );

        root.getChildren().addAll(hardware, software, hr, operation);

        // Add categories to classification system
        classRoot.getChildren().addAll(
                new TreeItem<>("Hardware"),
                new TreeItem<>("Software"),
                new TreeItem<>("Human Resources"),
                new TreeItem<>("Operations")
        );
    }
    /**
     * Initializes experimental group sample data
     * <p>
     * Creates simplified hardware and software categories for testing purposes
     */
    private void addSampleDataForExperiment() {
        TreeItem<String> root = groupData.get("Experiment Group 1");
        TreeItem<String> classRoot = classificationSystems.get("Experiment Group 1");

        // Hardware
        TreeItem<String> hardware = new TreeItem<>("Hardware");
        hardware.getChildren().addAll(
                new TreeItem<>("Hard Drive - ¥200"),
                new TreeItem<>("Monitor - ¥1200")
        );

        // Software
        TreeItem<String> software = new TreeItem<>("Software");
        software.getChildren().addAll(
                new TreeItem<>("OS - ¥800"),
                new TreeItem<>("Dev Tools - ¥500")
        );

        root.getChildren().addAll(hardware, software);

        // Add categories to classification system
        classRoot.getChildren().addAll(
                new TreeItem<>("Hardware"),
                new TreeItem<>("Software")
        );
    }
    /**
     * Creates the left panel containing transaction groups and expenditure tree
     * @return Configured VBox container with interactive components
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPrefSize(600, 480);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Transaction Group");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("Select Expense Group");
        groupComboBox.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-pref-width: 560;"
        );

        groupComboBox.setOnAction(e -> {
            currentGroup = groupComboBox.getValue();
            updateTrees();
        });

        expenseTreeView = new TreeView<>();
        expenseTreeView.setEditable(true);
        expenseTreeView.setShowRoot(false);
        expenseTreeView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-tree-cell-indent: 20;" +
                        "-fx-padding: 5;"
        );

        expenseTreeView.setCellFactory(tv -> new TreeCell<String>() {
            private HBox cellContent;
            private Label itemLabel;
            private Label actionButton;

            {
                itemLabel = new Label();
                itemLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");

                actionButton = new Label();
                actionButton.setStyle(
                        "-fx-text-fill: #9c36b5;" +
                                "-fx-font-size: 18px;" +
                                "-fx-min-width: 24px;" +
                                "-fx-min-height: 24px;" +
                                "-fx-alignment: center;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );

                cellContent = new HBox(5);
                cellContent.setAlignment(Pos.CENTER_LEFT);

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && getTreeItem() != null && getTreeItem().getValue().contains("¥")) {
                        showEditDialog(getTreeItem());
                    }
                });

                actionButton.setOnMouseClicked(e -> {
                    TreeItem<String> treeItem = getTreeItem();
                    if (treeItem != null) {
                        if (treeItem.getValue().contains("¥")) {
                            if (treeItem.getParent() != null) {
                                treeItem.getParent().getChildren().remove(treeItem);
                                updateTotalExpenditure();
                            }
                        } else {
                            showAddTransactionDialog(treeItem); // 弹出条目选择窗口
                        }
                    }
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                if (item.contains("¥")) {
                    String[] parts = item.split(" - ¥");
                    if (parts.length == 2) {
                        itemLabel.setText(String.format("%s    ¥%s", parts[0], parts[1]));
                        itemLabel.setStyle(
                                "-fx-font-family: 'Microsoft YaHei';" +
                                        "-fx-font-size: 14px;" +
                                        "-fx-text-fill: #495057;" +
                                        "-fx-font-weight: normal;"
                        );
                        actionButton.setText("-");
                    }
                } else {
                    itemLabel.setText(item);
                    itemLabel.setStyle(
                            "-fx-font-family: 'Microsoft YaHei';" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-text-fill: #2b8a3e;" +
                                    "-fx-font-weight: bold;"
                    );
                    actionButton.setText("+");
                }

                cellContent.getChildren().setAll(itemLabel, actionButton);
                setGraphic(cellContent);
            }
        });

        expenseTreeView.setPrefHeight(350);

        totalExpenditureLabel = new Label("Total Expenditure: ¥0");
        totalExpenditureLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        totalExpenditureLabel.setStyle("-fx-text-fill: #4a4a4a;");

        panel.getChildren().addAll(title, groupComboBox, expenseTreeView, totalExpenditureLabel);

        return panel;
    }
    /**
     * Displays dialog for adding transactions from CSV data
     * @param parentItem The tree node where transactions will be added
     */
    private void showAddTransactionDialog(TreeItem<String> parentItem) {
        if (parentItem == null) return;

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Select Transaction from Deals");
        dialog.setHeaderText("Choose a transaction to add");

        ButtonType confirmButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        ListView<String[]> listView = new ListView<>();
        List<String[]> deals = loadDealsFromCSV("src/deals.csv");
        listView.getItems().addAll(deals);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.join(" | ", item));
                }
            }
        });

        listView.setPrefWidth(600);
        listView.setPrefHeight(300);

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selected -> {
            if (selected != null) {

                String rawName = selected.length >= 4 ? selected[3] : "Unnamed";
                String rawAmount = selected.length >= 6 ? selected[5] : "0";

                String cleanName = rawName.replaceAll("^\"|\"$", "").trim();

                String cleanAmount = rawAmount.replaceAll("[^0-9.]", "").trim();

                TreeItem<String> newItem = new TreeItem<>(cleanName + " - ¥" + cleanAmount);
                parentItem.getChildren().add(newItem);
                parentItem.setExpanded(true);
                updateTotalExpenditure();

                addedNames.add(cleanName);
                addedAmounts.add(cleanAmount);

                System.out.println("Added Name: " + cleanName);
                System.out.println("Added Amount: " + cleanAmount);
            }
        });
    }

    /**
     * Loads transaction data from CSV file
     * @param filename Path to CSV file
     * @return List of parsed transaction records
     */
    private List<String[]> loadDealsFromCSV(String filename) {
        List<String[]> deals = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fourth = parts[3].replaceAll("\"", "").trim();
                    String third = parts[2].replaceAll("\"", "").trim();

                    if (fourth.equals("/") || fourth.matches("\\d+")) {
                        parts[3] = parts[2];
                    }
                    deals.add(parts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deals;
    }

    /**
     * Shows editing dialog for transaction details
     * @param item Tree item containing transaction to edit
     */
    private void showEditDialog(TreeItem<String> item) {
        if (item == null || !item.getValue().contains("¥")) return;

        String[] parts = item.getValue().split(" - ¥");
        if (parts.length != 2) return;

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Please enter new transaction details");

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(parts[0]);
        nameField.setPromptText("Transaction Name");
        TextField amountField = new TextField(parts[1]);
        amountField.setPromptText("Amount");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>(nameField.getText(), amountField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String newName = result.getKey().trim();
            String newAmountStr = result.getValue().trim();

            if (newName.isEmpty() || newAmountStr.isEmpty()) {

                return;
            }

            try {
                double newAmount = Double.parseDouble(newAmountStr);
                item.setValue(newName + " - ¥" + String.format("%.2f", newAmount));
                updateTotalExpenditure();
            } catch (NumberFormatException e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid number for the amount.");
                alert.showAndWait();
            }
        });

    }
    /**
     * Updates total expenditure display
     */
    private void updateTotalExpenditure() {
        totalExpenditure = calculateTotalExpenditure(expenseTreeView.getRoot());
        totalExpenditureLabel.setText(String.format("Total Expenditure: ¥%.2f", totalExpenditure));
    }
    /**
     * Recursively calculates total expenditure from tree structure
     * @param item Root node of expenditure tree
     * @return Calculated total expenditure
     */
    private double calculateTotalExpenditure(TreeItem<String> item) {
        if (item == null) return 0.0;

        double sum = 0.0;
        String itemValue = item.getValue();
        if (itemValue != null && itemValue.contains("¥")) {
            try {

                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("¥([0-9]+(\\.[0-9]+)?)").matcher(itemValue);
                while (matcher.find()) {
                    String numberStr = matcher.group(1);
                    sum += Double.parseDouble(numberStr);
                }
            } catch (NumberFormatException e) {
                // jump
            }
        }

        for (TreeItem<String> child : item.getChildren()) {
            sum += calculateTotalExpenditure(child);
        }
        return sum;
    }

    /**
     * Creates the right panel for classification system management
     * @return Configured VBox container with classification tools
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefSize(600, 480);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Classification System Management");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        classificationTreeView = new TreeView<>();
        classificationTreeView.setEditable(true);
        classificationTreeView.setCellFactory(TextFieldTreeCell.forTreeView());
        classificationTreeView.setPrefHeight(350);
        classificationTreeView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-tree-cell-indent: 10;" +
                        "-fx-tree-disclosure-node-size: 0;" +
                        "-fx-padding: 5;"
        );

        Button addCategoryButton = createStyledButton("Add Category", "#9c36b5");
        Button addSubcategoryButton = createStyledButton("Add Subcategory", "#6741d9");
        Button deleteCategoryButton = createStyledButton("Delete", "#c2255c");
        Button saveButton = createStyledButton("Save", "#2f9e44");
        exportButton = createStyledButton("Export MD", "#1971c2");

        addCategoryButton.setOnAction(e -> addCategory());
        addSubcategoryButton.setOnAction(e -> addSubcategory());
        deleteCategoryButton.setOnAction(e -> deleteCategory());
        saveButton.setOnAction(e -> saveClassificationChanges());
        exportButton.setOnAction(e -> exportToMarkdown());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addCategoryButton, addSubcategoryButton,
                deleteCategoryButton, saveButton, exportButton);

        String buttonStyle =
                "-fx-background-color: #9c36b5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100px;";

        addCategoryButton.setStyle(buttonStyle);
        addSubcategoryButton.setStyle(buttonStyle);
        deleteCategoryButton.setStyle(buttonStyle);
        saveButton.setStyle(buttonStyle);

        addCategoryButton.setOnAction(e -> addCategory());
        addSubcategoryButton.setOnAction(e -> addSubcategory());
        deleteCategoryButton.setOnAction(e -> deleteCategory());
        saveButton.setOnAction(e -> saveClassificationChanges());

        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        panel.getChildren().addAll(title, classificationTreeView, buttonBox);

        return panel;
    }
    /**
     * Exports current classification system to Markdown format
     */
    private void exportToMarkdown() {
        TreeItem<String> root = groupData.get(currentGroup);
        if (root == null || root.getChildren().isEmpty()) {
            showAlert("Export Failed", "No data available to export");
            return;
        }

        StringBuilder mdContent = new StringBuilder();
        mdContent.append("# ").append(currentGroup).append("\n\n");
        mdContent.append("**Total Expenditure: ¥").append(String.format("%.2f", totalExpenditure)).append("**\n\n");
        buildDataMarkdownContent(root, mdContent, 0);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Markdown File");
        fileChooser.setInitialFileName(currentGroup.replace(" ", "_") + "_Report.md");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(mdContent.toString());
                showAlert("Export Successful",
                        "Report saved to:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Export Failed", "Error writing file:\n" + ex.getMessage());
            }
        }
    }
    /**
     * Recursively builds Markdown content from classification tree
     * @param node Current tree node being processed
     * @param content StringBuilder accumulating Markdown content
     * @param level Current depth in the tree hierarchy
     */
    private void buildDataMarkdownContent(TreeItem<String> node, StringBuilder content, int level) {
        for (TreeItem<String> item : node.getChildren()) {
            String indent = "  ".repeat(level);
            String[] parts = parseNodeWithAmount(item.getValue());

            String line = indent + "- " + parts[0];
            if (!parts[1].isEmpty()) {
                line += " `¥" + parts[1] + "`";
            }

            content.append(line).append("\n");

            if (!item.getChildren().isEmpty()) {
                buildDataMarkdownContent(item, content, level + 1);
            }
        }
    }
    /**
     * Parses node text containing amount information
     * @param text Node text to parse (format: "Name - ¥Amount")
     * @return String array containing [name, amount] (empty string if no amount)
     */
    private String[] parseNodeWithAmount(String text) {

        String[] parts = text.split(" - ¥");
        if (parts.length == 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return new String[]{text.trim(), ""};
    }

    /**
     * Creates a styled button with hover effects
     * @param text Button display text
     * @param color Background color in hex format
     * @return Configured Button component
     */
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                button.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"));
        button.setOnMouseExited(e -> button.setStyle(
                button.getStyle().replace("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);", "")));

        return button;
    }
    /**
     * Displays styled alert dialog
     * @param title Alert window title
     * @param message Content message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 5px;"
        );
        alert.showAndWait();
    }
    /**
     * Updates both tree views with current group data
     * <p>
     * Synchronizes expense tree and classification tree displays
     */
    private void updateTrees() {
        // Update expense tree
        expenseTreeView.setRoot(groupData.get(currentGroup));
        updateTotalExpenditure();

        // Update classification tree
        classificationTreeView.setRoot(classificationSystems.get(currentGroup));
    }
    /**
     * Saves modified classification structure and updates data models
     * <p>
     * Preserves existing transactions while rebuilding category structure
     */
    private void saveClassificationChanges() {
        TreeItem<String> oldExpenseRoot = groupData.get(currentGroup);
        TreeItem<String> newClassificationRoot = classificationTreeView.getRoot();

        // Create new expense tree based on classification
        TreeItem<String> newExpenseRoot = new TreeItem<>("Expense Classification");
        newExpenseRoot.setExpanded(true);

        // Create map of old expenses
        Map<String, List<TreeItem<String>>> oldExpenses = new HashMap<>();
        collectExpensesRecursively(oldExpenseRoot, oldExpenses, "");

        // Rebuild expense tree
        rebuildExpenseTree(newClassificationRoot, newExpenseRoot, oldExpenses, "");

        // Update data structures
        groupData.put(currentGroup, newExpenseRoot);
        classificationSystems.put(currentGroup, copyTree(newClassificationRoot));

        // Update trees
        updateTrees();
    }
    /**
     * Recursively collects expense items from tree structure
     * @param root Starting node for collection
     * @param expenses Map to store collected expenses by category path
     * @param parentPath Current category path for hierarchical tracking
     */
    private void collectExpensesRecursively(TreeItem<String> root, Map<String, List<TreeItem<String>>> expenses, String parentPath) {
        for (TreeItem<String> category : root.getChildren()) {
            String currentPath = parentPath.isEmpty() ? category.getValue() : parentPath + "/" + category.getValue();
            List<TreeItem<String>> categoryExpenses = new ArrayList<>();

            // Collect direct expenses
            for (TreeItem<String> expense : category.getChildren()) {
                if (expense.getValue().contains("¥")) {
                    categoryExpenses.add(expense);
                }
            }
            expenses.put(currentPath, categoryExpenses);

            // Recursively collect expenses from subcategories
            collectExpensesRecursively(category, expenses, currentPath);
        }
    }
    /**
     * Rebuilds expense tree based on modified classification structure
     * @param sourceRoot Source classification tree root
     * @param targetRoot Target expense tree root
     * @param oldExpenses Map of previously collected expenses
     * @param parentPath Current category path for hierarchy tracking
     */
    private void rebuildExpenseTree(TreeItem<String> sourceRoot, TreeItem<String> targetRoot, Map<String, List<TreeItem<String>>> oldExpenses, String parentPath) {
        for (TreeItem<String> category : sourceRoot.getChildren()) {
            TreeItem<String> newCategory = new TreeItem<>(category.getValue());
            newCategory.setExpanded(true);

            String currentPath = parentPath.isEmpty() ? category.getValue() : parentPath + "/" + category.getValue();

            // Add existing expenses if available
            List<TreeItem<String>> expenses = oldExpenses.get(currentPath);
            if (expenses != null) {
                for (TreeItem<String> expense : expenses) {
                    newCategory.getChildren().add(new TreeItem<>(expense.getValue()));
                }
            }

            targetRoot.getChildren().add(newCategory);

            // Recursively rebuild subcategories
            rebuildExpenseTree(category, newCategory, oldExpenses, currentPath);
        }
    }
    /**
     * Creates deep copy of tree structure
     * @param item Root node of tree to copy
     * @return Complete copy of input tree structure
     */
    private TreeItem<String> copyTree(TreeItem<String> item) {
        TreeItem<String> copy = new TreeItem<>(item.getValue());
        copy.setExpanded(item.isExpanded());
        for (TreeItem<String> child : item.getChildren()) {
            copy.getChildren().add(copyTree(child));
        }
        return copy;
    }
    /**
     * Adds new top-level category to classification system
     */
    private void addCategory() {
        TreeItem<String> root = classificationTreeView.getRoot();
        TreeItem<String> newCategory = new TreeItem<>("New Category");
        root.getChildren().add(newCategory);
        root.setExpanded(true);
    }
    /**
     * Adds subcategory to selected classification node
     */
    private void addSubcategory() {
        TreeItem<String> selectedItem = classificationTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TreeItem<String> newCategory = new TreeItem<>("New Subcategory");
            selectedItem.getChildren().add(newCategory);
            selectedItem.setExpanded(true);
        }
    }
    /**
     * Removes selected category from classification system
     * <p>
     * Cannot remove root nodes or categories with children
     */
    private void deleteCategory() {
        TreeItem<String> selectedItem = classificationTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getParent() != null) {
            selectedItem.getParent().getChildren().remove(selectedItem);
        }
    }
    /**
     * Creates main content area with left and right panels
     * @return Configured HBox containing application's main interface
     */
    private HBox createMainContent() {
        HBox content = new HBox(40);  // Increase spacing between panels to 40px
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 40, 20, 40));  // Add horizontal padding

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        content.getChildren().addAll(leftPanel, rightPanel);
        return content;
    }
    /**
     * Constructs main application layout with title and content
     * @param content Main content HBox from createMainContent()
     * @return Complete VBox layout container
     */
    private VBox createMainLayout(HBox content) {
        Label pageTitle = new Label("Free Classification Designer");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 38));
        pageTitle.setTextFill(Color.WHITE);
        pageTitle.setEffect(new DropShadow(15, Color.web("#4c3092")));

        Label subtitle = new Label("Craft Your Custom Financial Taxonomy");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        subtitle.setTextFill(Color.web("#e6d5ff"));

        VBox titleBox = new VBox(8, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);");
        titleBox.setPadding(new Insets(30, 0, 30, 0));

        VBox contentWrapper = new VBox(content);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(20, 0, 20, 0));
        contentWrapper.setStyle("-fx-background-color: white;");

        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(titleBox, contentWrapper);
        mainLayout.setStyle("-fx-background-color: white;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        return mainLayout;
    }
    /**
     * Creates navigation button with emoji icon
     * @param label Button text label
     * @param emoji Unicode emoji character
     * @return Styled button with vertical layout
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
     * Main method to launch the application
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}