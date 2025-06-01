//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Login class represents a JavaFX application for user authentication.
 * It provides functionality for user login, signup, and password management.
 *
 * <p>The application features:
 * <ul>
 *     <li>User login with username and password</li>
 *     <li>User registration (signup)</li>
 *     <li>Password change functionality</li>
 *     <li>Password recovery options</li>
 *     <li>Credential persistence using CSV file storage</li>
 * </ul>
 *
 * @author Xinghan Qin
 * @version final
 */
public class Login extends Application {
    /** The primary stage of the application */
    protected Stage primaryStage;
    
    /** File path for storing user credentials */
    protected static final String CREDENTIALS_FILE = "user_credentials.csv";
    
    /** Map storing username-password pairs */
    protected Map<String, String> userCredentials = new HashMap<>();

    /**
     * The main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and starts the JavaFX application.
     * Sets up the primary stage and loads user credentials.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadCredentials();

        primaryStage.setTitle("Login System");
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);

        showLoginScene();
    }

    /**
     * Displays the login scene with username and password fields.
     * Creates and configures all UI components for the login interface.
     */
    protected void showLoginScene() {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #FFD4EC54;");

        // Login title
        Label titleLabel = new Label("Login");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#855FAF"));

        // Instruction text
        Label instructionLabel = new Label("Enter your access details below");
        instructionLabel.setFont(Font.font("Arial", 16));
        instructionLabel.setTextFill(Color.web("#666666"));

        // Form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        formContainer.setPadding(new Insets(30, 40, 30, 40));
        formContainer.setStyle("-fx-background-color: rgba(237,223,248,0.88); -fx-border-radius: 5; -fx-background-radius: 5;");

        // Username field
        Label usernameLabel = new Label("Enter your username");
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.web("#333333"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Password field
        Label passwordLabel = new Label("Enter your password");
        passwordLabel.setFont(Font.font("Arial", 14));
        passwordLabel.setTextFill(Color.web("#333333"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Login button
        Button loginButton = new Button("Log in");
        loginButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 40px;");
        loginButton.setOnAction(e -> {
            if(handleLogin(usernameField.getText(), passwordField.getText())==1){
                new Nutllet().start(new Stage());
                primaryStage.close();
            }
            else if (handleLogin(usernameField.getText(), passwordField.getText())==0){
                new Login().start(new Stage());
                primaryStage.close();
            }
            else if (handleLogin(usernameField.getText(), passwordField.getText())==-1){
                showAlert("Error", "Invalid username or password.");
                new Login().start(new Stage());
                primaryStage.close();

            }
        });


        // Signup button
        Button signupButton = new Button("Click to sign up");
        signupButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 40px;");
        signupButton.setOnAction(e -> {
            new LoginSignUp().start(new Stage());
            primaryStage.close();
        });

        // Change password link
        Hyperlink changePasswordLink = new Hyperlink("Change password");
        changePasswordLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        changePasswordLink.setOnAction(e -> {
            new LoginPassword().start(new Stage());
            primaryStage.close();
        });

        // Forgot password link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot your password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        forgotPasswordLink.setOnAction(e -> showAlert("Password Recovery", "Please contact support to reset your password."));

        // Add components to form
        formContainer.getChildren().addAll(
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                loginButton, signupButton,
                changePasswordLink, forgotPasswordLink
        );

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, instructionLabel, formContainer);

        // Create scene
        Scene loginScene = new Scene(mainContainer);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    /**
     * Handles the login process by validating user credentials.
     *
     * @param username The username entered by the user
     * @param password The password entered by the user
     * @return 1 for successful login, 0 for empty fields, -1 for invalid credentials
     */
    protected int handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return 0;
        }

        if (!userCredentials.containsKey(username)) {
//            showAlert("Error", "Invalid username or password.");
            return -1;
        }else if (userCredentials.get(username).equals(password)) {
            showAlert("Success", "Login successful!");
            primaryStage.close();
            return 1;
        }else {
//            showAlert("Error", "Invalid username or password.");
            return -1;
        }
    }

    /**
     * Loads user credentials from the CSV file.
     * Reads username-password pairs and stores them in the userCredentials map.
     */
    protected void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves user credentials to the CSV file.
     * Writes all username-password pairs from the userCredentials map to the file.
     */
    protected void saveCredentials() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified title and message.
     *
     * @param title The title of the alert
     * @param message The message to display
     */
    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}