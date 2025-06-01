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

/**
 * The LoginPassword class extends the Login class to provide password change functionality.
 * It implements a user interface for changing passwords with validation and security checks.
 *
 * <p>The password change process includes:
 * <ul>
 *     <li>Username verification</li>
 *     <li>Current password validation</li>
 *     <li>New password requirements (minimum 8 characters)</li>
 *     <li>Password confirmation matching</li>
 *     <li>Security checks to prevent reuse of current password</li>
 * </ul>
 *
 * @author Xinghan Qin
 * @version final
 */
public class LoginPassword extends Login {
    /**
     * Initializes and starts the password change interface.
     * Sets up the primary stage and loads user credentials.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadCredentials();

        primaryStage.setTitle("Change Password");
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);

        showChangePasswordScene();
    }

    /**
     * Creates and displays the password change scene.
     * Sets up all UI components including input fields and buttons.
     */
    private void showChangePasswordScene() {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #FFD4EC54;");

        // Title
        Label titleLabel = new Label("Change Password");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#855FAF"));

        // Instruction
        Label instructionLabel = new Label("Enter your current and new password");
        instructionLabel.setFont(Font.font("Arial", 16));
        instructionLabel.setTextFill(Color.web("#666666"));

        // Form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        formContainer.setPadding(new Insets(30, 40, 30, 40));
        formContainer.setStyle("-fx-background-color: rgba(237,223,248,0.88); -fx-border-radius: 5; -fx-background-radius: 5;");

        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.web("#333333"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Current password field
        Label currentPasswordLabel = new Label("Current Password");
        currentPasswordLabel.setFont(Font.font("Arial", 14));
        currentPasswordLabel.setTextFill(Color.web("#333333"));

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        currentPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // New password field
        Label newPasswordLabel = new Label("New Password");
        newPasswordLabel.setFont(Font.font("Arial", 14));
        newPasswordLabel.setTextFill(Color.web("#333333"));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min 8 characters)");
        newPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Confirm new password field
        Label confirmPasswordLabel = new Label("Confirm New Password");
        confirmPasswordLabel.setFont(Font.font("Arial", 14));
        confirmPasswordLabel.setTextFill(Color.web("#333333"));

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter new password");
        confirmPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Change password button
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 200px; -fx-pref-height: 40px;");
        changePasswordButton.setOnAction(e -> handleChangePassword(
                usernameField.getText(),
                currentPasswordField.getText(),
                newPasswordField.getText(),
                confirmPasswordField.getText()
        ));

        // Back to login link
        Hyperlink backToLoginLink = new Hyperlink("Back to Login");
        backToLoginLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        backToLoginLink.setOnAction(e -> {
            primaryStage.close();
            new Login().start(new Stage());
        });

        // Add components to form
        formContainer.getChildren().addAll(
                usernameLabel, usernameField,
                currentPasswordLabel, currentPasswordField,
                newPasswordLabel, newPasswordField,
                confirmPasswordLabel, confirmPasswordField,
                changePasswordButton, backToLoginLink
        );

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, instructionLabel, formContainer);

        // Create scene
        Scene changePasswordScene = new Scene(mainContainer);
        primaryStage.setScene(changePasswordScene);
        primaryStage.show();
    }

    /**
     * Handles the password change process with validation checks.
     * Verifies all inputs and updates the password if all checks pass.
     *
     * @param username The username of the account
     * @param currentPassword The current password for verification
     * @param newPassword The new password to be set
     * @param confirmPassword The confirmation of the new password
     */
    private void handleChangePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (username.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!userCredentials.containsKey(username)) {
            showAlert("Error", "Username not found.");
            return;
        }

        if (!userCredentials.get(username).equals(currentPassword)) {
            showAlert("Error", "Current password is incorrect.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New passwords do not match.");
            return;
        }

        if (newPassword.length() < 8) {
            showAlert("Error", "New password must be at least 8 characters long.");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showAlert("Error", "New password must be different from current password.");
            return;
        }

        // Update password
        userCredentials.put(username, newPassword);
        saveCredentials();
        showAlert("Success", "Password changed successfully!");
        primaryStage.close();
        new Login().start(new Stage());
    }
}