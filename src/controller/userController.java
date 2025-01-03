package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.User;
import model.Album;
import model.Photo;

public class userController {

    @FXML
    public TextField enteredTitle;
    @FXML
    public Button createAlbum;
    @FXML
    public Button deleteAlbum;
    @FXML
    ListView<String> listView;
    @FXML
    public Label title;

    private ObservableList<String> obsList;

    private static final String storeDir = "dat";
    private static final String storeFile = "albums.dat";

    public User currentUser;


    
    private static Map<String, ArrayList<Album>> userAlbumsMap = new HashMap<>();

    public void logOut(ActionEvent e) throws Exception {
        writeAlbumsToFile();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        // adminController adminController = fxmlLoader.getController();
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene loginScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(loginScene);
        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();
    }

    public void createAlbum(ActionEvent e) {
        String title = enteredTitle.getText().trim();
        if (title.equalsIgnoreCase("admin")) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Admin Error.");
            alert.setContentText("Can't add user \"admin\". Try entering a new username!");
            alert.showAndWait();
            return;
        }
        if (albumExists(title)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("User Error.");
            alert.setContentText("Album already exists. Try entering a new title!");
            alert.showAndWait();
            return;
        } else if (title.equalsIgnoreCase("")) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("User Error.");
            alert.setContentText("Album name can't be blank. Try entering a new title!");
            alert.showAndWait();
            return;
        } else {
            currentUser.albums.add(new Album(title));

        }

        writeAlbumsToFile();
        refresh();
    }

    public void deleteAlbum(ActionEvent e) {
        String title = enteredTitle.getText().trim();
        if (!albumExists(title)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("User Error.");
            alert.setContentText("Album doesn't exist. Try entering a new title!");
            alert.showAndWait();
            return;
        } else {
            Iterator<Album> iterator = currentUser.albums.iterator();
            while (iterator.hasNext()) {
                Album current = iterator.next();
                if (current.albumName.equalsIgnoreCase(title)) {
                    iterator.remove(); // Safely remove the user from the list
                    listView.getItems().remove(title);
                    listView.refresh();
                }
            }
        }
        writeAlbumsToFile();
        refresh();
    }

    public boolean albumExists(String title) {
        for (Album current : currentUser.albums) {
            if (current.albumName.equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    public void writeAlbumsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(storeDir + File.separator + storeFile))) {
            // Create a map to associate each user with their albums
            userAlbumsMap.put(currentUser.getUsername(), currentUser.albums);
            oos.writeObject(userAlbumsMap);
            System.out.println("Albums saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save albums to file.");
        }
    }

    public void readAlbumsFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(storeDir + File.separator + storeFile))) {
            userAlbumsMap = (Map<String, ArrayList<Album>>) ois.readObject();
            // Load albums associated with the current user
            ArrayList<Album> albums = userAlbumsMap.get(currentUser.getUsername());
            if (albums != null) {
                currentUser.albums = albums;
            } else {
                currentUser.albums = new ArrayList<>();
            }
            System.out.println("Albums loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Albums from file.");
        }
    }

    public void refresh() {
        title.setText(currentUser.username + "'s albums");

        obsList = FXCollections.observableArrayList();
        for (Album album : currentUser.albums) {
            int photoCount = album.photos.size();
            String printedInfo;
            LocalDate earliestDate = null;
            LocalDate latestDate = null;
            for (Photo photo : album.photos) {
                LocalDate modificationDate = photo.getModificationDate();
                if (earliestDate == null || modificationDate.isBefore(earliestDate)) {
                    earliestDate = modificationDate;
                    
                }
                if (latestDate == null || modificationDate.isAfter(latestDate)) {
                    latestDate = modificationDate;
                }
            }

            // Append earliest and latest modification dates to printedInfo

            if (photoCount != 1) {
                printedInfo = album.albumName + " : " + photoCount + " photos";
            } else {
                printedInfo = album.albumName + " : " + photoCount + " photo";

            }
            if (earliestDate != null && latestDate != null) {
                printedInfo += " (" + earliestDate.toString() + " - " + latestDate.toString() + ")";
            }

            obsList.add(printedInfo);
        }
        listView.getItems().clear();
        listView.setItems(obsList);

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAlbumClick(MouseEvent e) throws Exception {
        Album selectedAlbum = getSelectedAlbum();

        // writeAlbumsToFile();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/album.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        albumController albumController = fxmlLoader.getController();
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene albumScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(albumScene);
        albumController.currentUser = currentUser;
        albumController.currentAlbum = selectedAlbum;
        albumController.refresh();

        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();

    }

    private Album getSelectedAlbum() {
        String selectedAlbumName = listView.getSelectionModel().getSelectedItem();
        selectedAlbumName = selectedAlbumName.substring(0, selectedAlbumName.indexOf(':') - 1);
        selectedAlbumName.trim();
        if (selectedAlbumName != null) {
            for (Album album : currentUser.albums) {
                if (album.albumName.equals(selectedAlbumName)) {
                    return album;
                }
            }
        }
        return null;
    }
    public void goSearch(ActionEvent e) throws Exception {
    
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/search.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        searchController searchController = fxmlLoader.getController();
        searchController.currentUser = currentUser;
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene searchScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(searchScene);
        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();
    }

}