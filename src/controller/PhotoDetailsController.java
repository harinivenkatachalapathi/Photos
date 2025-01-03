package controller;

import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Album;
import model.Photo;

import javafx.scene.layout.VBox;
import model.User;

import java.io.IOException;

public class PhotoDetailsController {

    @FXML
    private VBox photoDetailsBox;

    @FXML
    private ImageView photoImageView;

    @FXML
    private Label captionLabel;

    @FXML
    private Label modificationTimeLabel;

    @FXML
    private Label tagsLabel;

    private Photo photo;

    public User currentUser;

    public Album currentAlbum;

    public albumController tempController = new albumController();

    public void initialize() {
        // Initialize the photo details
        displayPhotoDetails();
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    private void displayPhotoDetails() {
        if (photo != null) {
            Image image = new Image(photo.file.toURI().toString());
            photoImageView.setImage(image);
            captionLabel.setText(photo.getCaption());
            modificationTimeLabel.setText("Modified: " + photo.getModificationDate().toString());
            if (photo.getTags() != null) {
                tagsLabel.setText("Tags: " + printTags());
            } else {
                tagsLabel.setText("Tags: ");
            }

        }

    }

    @FXML
    void recaptionPhoto(ActionEvent event) {
        Stage captionStage = new Stage();
        captionStage.initModality(Modality.APPLICATION_MODAL);
        captionStage.setTitle("Enter Caption");

        TextField captionField = new TextField();
        captionField.setPromptText("Enter caption");

        Button addButton = new Button("Add");
        addButton.setOnAction(newEvent -> {
            try {
                String caption = captionField.getText();
                if (!caption.isEmpty()) {
                    // Create Photo object with the selected file and caption
                    photo.caption = caption;
                    // Update UI with the new photo

                    captionStage.close();
                }
            } catch (IllegalArgumentException ex) {
                // Handle invalid image URL or resource not found exception
                System.err.println("Error loading image: " + ex.getMessage());
                // You can display an error message to the user if needed
            }
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(captionField, addButton);

        Scene scene = new Scene(vbox, 250, 100);
        captionStage.setScene(scene);
        captionStage.showAndWait(); // Wait for the caption stage to be closed

        tempController.currentAlbum = currentAlbum;
        tempController.currentUser = currentUser;
        tempController.writeAlbumsToFile();
        showAlert("Refresh", "Refresh album to view changes");
        closeWindow(event);

        tempController.displayAlbumPhotos(currentAlbum);
        captionLabel.setText(photo.caption);
        displayPhotoDetails();


    }

    @FXML
    void removePhoto(ActionEvent event) {
        currentAlbum.removePhoto(photo);

        tempController.currentAlbum = currentAlbum;
        tempController.currentUser = currentUser;
        tempController.title = new Label();
        tempController.writeAlbumsToFile();
        showAlert("Refresh", "Refresh album to view changes");

        closeWindow(event);

    }

    @FXML
    void closeWindow(ActionEvent event) {
        // Close the photo details window
        Stage stage = (Stage) photoDetailsBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    void addTags(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/tag.fxml"));
            VBox root = loader.load();

            tagController tagDialogController = loader.getController();
            tagDialogController.setPhoto(photo); // Ensure you have a method to pass the photo object to the dialog
                                                 // controller

            Stage tagStage = new Stage();
            tagStage.initModality(Modality.APPLICATION_MODAL);
            tagStage.setTitle("Create or Select Tag");
            tagStage.setScene(new Scene(root));
            tagStage.showAndWait();

            // Write changes to file if necessary
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }

        tempController.currentAlbum = currentAlbum;
        tempController.currentUser = currentUser;

        displayPhotoDetails(); // Refresh photo details to show the newly added tag
        tempController.writeAlbumsToFile();
    }

    public String printTags() {
        String temp = "";
        for (String[] tag : photo.getTags()) {
            temp += tag[0] + ": " + tag[1] + " ";
        }
        return temp;
    }

    @FXML
    void deleteTag(ActionEvent event) {
        // Create the TextInputDialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Tag");
        dialog.setHeaderText("Delete a Tag");
        dialog.setContentText("Please enter tag in 'tagname=tagvalue' format:");

        // Show the dialog and capture the input
        Optional<String> result = dialog.showAndWait();

        // Process the result
        result.ifPresent(tagInput -> {
            // Split the input into tag name and tag value
            String[] parts = tagInput.split("=");
            if (parts.length == 2) {
                String tagName = parts[0].trim();
                String tagValue = parts[1].trim();

                // Find and remove the tag
                boolean tagRemoved = photo.getTags().removeIf(tag -> tag[0].equals(tagName) && tag[1].equals(tagValue));

                if (tagRemoved) {
                    // Tag was found and removed
                    displayPhotoDetails(); // Refresh photo details to reflect the tag deletion
                    showAlert("Tag Deleted", "The tag '" + tagName + ": " + tagValue + "' has been deleted.");
                } else {
                    // Tag not found
                    showAlert("Tag Not Found",
                            "No tag found with the name '" + tagName + "' and value '" + tagValue + "'.");
                }
            } else {
                // Invalid input format
                showAlert("Invalid Format", "Please enter the tag in the correct 'tagname=tagvalue' format.");
            }
        });
        tempController.currentAlbum = currentAlbum;
        tempController.currentUser = currentUser;
        tempController.writeAlbumsToFile();

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void movePhoto(ActionEvent e) { // Moves photo from current album to selected album
        Album targetAlbum = showAlbumSelectionDialog();
        if (targetAlbum != null && !currentAlbum.equals(targetAlbum)) {
            currentAlbum.removePhoto(photo);
            targetAlbum.addPhoto(photo);
            closeWindow(e);
            // Save changes and refresh UI as needed
        }

        tempController.currentAlbum = currentAlbum;
        tempController.currentUser = currentUser;

        albumController saveController = new albumController();
        saveController.currentAlbum = targetAlbum;
        saveController.currentUser = currentUser;
        saveController.writeAlbumsToFile();
    }

    public void copyPhoto(ActionEvent e) { // Adds the photo to selected album without removing it from current album
        Album targetAlbum = showAlbumSelectionDialog();
        if (targetAlbum != null && !currentAlbum.equals(targetAlbum)) {
            targetAlbum.addPhoto(photo);
            closeWindow(e);



            // Save changes and refresh UI as needed
        }
        albumController saveController = new albumController();
        saveController.currentAlbum = targetAlbum;
        saveController.currentUser = currentUser;
        saveController.writeAlbumsToFile();
    }

    private Album showAlbumSelectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/selectalbum.fxml"));
            Parent root = loader.load();
            selectAlbumController tempcontroller = loader.getController();
            tempcontroller.setCurrentUser(currentUser); // Set the current user here
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Album");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            return tempcontroller.getSelectedAlbum();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
