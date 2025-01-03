package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.User;
import java.io.File;
import javafx.scene.text.Text;

public class slideshowController {

    @FXML
    private ImageView imageView;

    @FXML
    private Button nextPhoto;

    @FXML
    private Button lastPhoto;

    @FXML
    private Text tagText;

    @FXML
    private Text dateText;


    private Album currentAlbum;
    private int currentIndex = 0; // Start at the first photo
    private User currentUser;
    private Photo currentPhoto;

    // Initialize the controller with data
    public void initData(Album album, User user) {
        this.currentAlbum = album;
        this.currentUser = user;

        // Load the first photo, if available
        if (!currentAlbum.getPhotos().isEmpty()) {
            currentPhoto = currentAlbum.getPhotos().get(0);
            displayPhoto(currentPhoto);
        }
    }

    private void displayPhoto(Photo photo) {
        // Assuming Photo class has a method to get the file path
        File file = photo.file;
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        if (currentIndex == 0){
            lastPhoto.setVisible(false);
        } else {
            lastPhoto.setVisible(true);
        }
        if (currentIndex >= currentAlbum.getPhotos().size() -1){
            nextPhoto.setVisible(false);
        } else {
            nextPhoto.setVisible(true);
        }
    }

    @FXML
    private void returnAlbum(ActionEvent e) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/album.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        albumController albumController = fxmlLoader.getController();
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene albumScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(albumScene);
        albumController.currentUser = currentUser;
        albumController.currentAlbum = currentAlbum;
        albumController.refresh();

        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();

    }

    @FXML
    private void nextPhoto(ActionEvent event) {
        if (currentIndex < currentAlbum.getPhotos().size() - 1) {
            currentIndex++; // Move to next photo
            currentPhoto = currentAlbum.getPhotos().get(currentIndex);
            displayPhoto(currentPhoto);
        }
    }

    @FXML
    private void lastPhoto(ActionEvent event) {
        if (currentIndex > 0) {
            currentIndex--; // Move to previous photo
            currentPhoto = currentAlbum.getPhotos().get(currentIndex);
            displayPhoto(currentPhoto);
        }
    }
}
