package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
//import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import javafx.scene.control.ListView;
import java.util.Iterator;
public class adminController {
    private static final String storeDir = "dat";
    private static final String storeFile = "users.dat";
    @FXML
    public Button logOut;
    @FXML
    public TextField enteredUser;
    @FXML
    public Button createUser;
    @FXML
    public Button deleteUser;
    @FXML
    ListView<String> listView;

    private ObservableList<String> obsList;

    public void logOff(ActionEvent e) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        // adminController adminController = fxmlLoader.getController();
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene adminScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(adminScene);
        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();
    }

    public void addUser(ActionEvent e) {
        String user = enteredUser.getText();
        if (user.equalsIgnoreCase("admin")){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Admin Error.");
            alert.setContentText("Can't add user \"admin\". Try entering a new username!");
            alert.showAndWait();
            return;
        }
        if (userExists(user)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Admin Error.");
            alert.setContentText("Username already exists. Try entering a new username!");
            alert.showAndWait();
            return;
        } else {
            User.users.add(new User(user));

        }
        writeUsersToFile();
        refresh();
    }

    public boolean userExists(String username) {
        for (User current : User.users) {
            if (current.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public void refresh() {
        readUsersFromFile(); // Load users from file

        obsList = FXCollections.observableArrayList();
        for (User user : User.users) {
            obsList.add(user.getUsername());
        }
        listView.setItems(obsList);

    }

    public void deleteUser(ActionEvent e) {
        String user = enteredUser.getText();
        if (!userExists(user)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Admin Error.");
            alert.setContentText("User doesn't exist. Try entering a new username!");
            alert.showAndWait();
            return;
        } else {
            Iterator<User> iterator = User.users.iterator();
            while (iterator.hasNext()) {
                User current = iterator.next();
                if (current.username.equalsIgnoreCase(user)) {
                    iterator.remove(); // Safely remove the user from the list
                    listView.getItems().remove(user);
                    listView.refresh();
                }
            }
        }
        writeUsersToFile();
        refresh();
    }

    public void writeUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(storeDir + File.separator + storeFile))) {
            oos.writeObject(User.users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save users to file.");
        }
    }

    public void readUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(storeDir + File.separator + storeFile))) {
            User.users = (ArrayList<User>) ois.readObject();
            System.out.println("Users loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users from file.");
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