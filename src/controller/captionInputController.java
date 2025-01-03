package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.Album;
import model.Photo;
import java.io.File;

public class captionInputController {

    @FXML
    private TextField captionField;

    private Album currentAlbum;
    private File imgFile;

    // Method to initialize data
    public void initData(Album album, File file) {
        this.currentAlbum = album;
        this.imgFile = file;
    }

    // Method to handle the "Add" button action
    @FXML
    private void addCaption(ActionEvent event) {
        String caption = captionField.getText();
        if (!caption.isEmpty()) {
            // Create Photo object with the selected file and caption
            Photo newPhoto = new Photo(imgFile, caption);
            currentAlbum.addPhoto(newPhoto);
            // Update UI with the new photo
            // (You may need to call a method in the parent controller to do this)
            closeDialog(); // Close the dialog after adding the caption
        }
    }

    // Method to close the dialog
    private void closeDialog() {
        captionField.getScene().getWindow().hide();
    }
}