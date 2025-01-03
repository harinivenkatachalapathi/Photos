package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Album;
import model.User;
import java.util.Map;

public class selectAlbumController {

    @FXML
    private ListView<String> albumListView;

    private Album selectedAlbum;
    
    public User currentUser;

  



    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupAlbums(); // Call setup method here
    }

    private void setupAlbums() {
        if (currentUser != null) {
            // Now you can safely use currentUser to populate the ListView, etc.
            // For example:
            albumListView.setItems(FXCollections.observableArrayList(currentUser.getAlbums().stream().map(Album::getAlbumName).toList()));
        }
    }


    public Album getSelectedAlbum() {
        return selectedAlbum;
    }

    public void handleSelect() {
        String selectedAlbumName = albumListView.getSelectionModel().getSelectedItem();
        if (selectedAlbumName != null) {
            // Find the Album object that matches the selected name
            for (Album album : currentUser.getAlbums()) {
                if (album.getAlbumName().equals(selectedAlbumName)) {
                    selectedAlbum = album;
                    break; // Exit the loop once the matching album is found
                }
            }
        }
        closeStage(); // Close the dialog after selection
    }

    private void closeStage() {
        Stage stage = (Stage) albumListView.getScene().getWindow();
        stage.close();
    }

    // Add your method to get the current user's albums
}
