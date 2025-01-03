package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.User;

public class loginController {
    @FXML
    public Button login;
    @FXML
    public TextField username;

    private static final String storeDir = "dat";
    private static final String storeFile = "users.dat";
    public boolean userFound = false;

    public void login(ActionEvent e) throws Exception {
        readUsersFromFile();
        // Button b = (Button) e.getSource();
        String user = username.getText();
        if (user.equalsIgnoreCase("admin")) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin.fxml"));
            Parent sceneManager = (Parent) fxmlLoader.load();
            adminController adminController = fxmlLoader.getController();
            // You can pass any data to the controller here if needed
            // adminController.setData(...);
            Scene adminScene = new Scene(sceneManager);
            Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            appStage.setScene(adminScene);
            adminController.refresh();
            // If you need to call any initialization methods in the controller, do it here
            // adminController.init();
            appStage.show();
        } else {

            for (User iterate : User.users) {
                if (iterate.username.equalsIgnoreCase(user)) {
                    userFound = true;
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/user.fxml"));
                    Parent sceneManager = (Parent) fxmlLoader.load();
                    userController userController = fxmlLoader.getController();
                    userController.currentUser = iterate;
                    // You can pass any data to the controller here if needed
                    // adminController.setData(...);
                    Scene userScene = new Scene(sceneManager);
                    Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    appStage.setScene(userScene);
                    userController.readAlbumsFromFile();
                    userController.refresh();
                    // If you need to call any initialization methods in the controller, do it here
                    // adminController.init();
                    appStage.show();
                    break;
                }
            }
            if (!userFound) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Admin Error.");
                alert.setContentText("User doesn't exist. Try entering a new username!");
                alert.showAndWait();
                return;
            }

        }

    }
    

    public void readUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(storeDir + File.separator + storeFile))) {
            ArrayList<User> loadedUsers = (ArrayList<User>) ois.readObject();
            for (User loadedUser : loadedUsers) {
                boolean userExists = false;
                for (User existingUser : User.users) {
                    if (existingUser.getUsername().equals(loadedUser.getUsername())) {
                        userExists = true;
                        break;
                    }
                }
                if (!userExists) {
                    User.users.add(loadedUser);
                }
            }
            System.out.println("Users loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}