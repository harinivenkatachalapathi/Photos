package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Photo;

public class tagController {

    @FXML
    private ComboBox<String> tagComboBox;
    @FXML
    private TextField newTagField;
    @FXML
    private TextField valueField;

    private Photo photo;

    public void initialize() {
        ObservableList<String> predefinedTags = FXCollections.observableArrayList("location", "person", "New Tag...");
        tagComboBox.setItems(predefinedTags);

        tagComboBox.setOnAction(e -> {
            newTagField.setVisible(tagComboBox.getValue().equals("New Tag..."));
        });
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    @FXML
    private void handleAddTag() {
        String tagName = tagComboBox.getValue().equals("New Tag...") ? newTagField.getText() : tagComboBox.getValue();
        String tagValue = valueField.getText();


        if ("location".equals(tagName)) {
            // Check if a 'location' tag already exists
            boolean locationTagExists = photo.getTags().stream()
                                              .anyMatch(tag -> "location".equals(tag[0]));

            if (locationTagExists) {
                // Show an alert that a 'location' tag already exists and return without adding the tag
                showAlert("Duplicate Location Tag", "This photo already has a location tag. Only one location tag is allowed per photo.");
                return; // Exit the method to prevent adding another 'location' tag
            }
        }
        
        boolean tagExists = photo.getTags().stream()
                .anyMatch(tag -> tag[0].equals(tagName) && tag[1].equals(tagValue));

        if (!tagExists) {
            if (!tagName.isEmpty() && !tagValue.isEmpty()) {
                String[] tag = { tagName, tagValue };
                photo.addTag(tag);
                closeStage();
            }
        } else {
            showAlert("Duplicate Tag", "Can't have duplicate tag");
        }

        // Consider displaying a message if tagName or tagValue is empty
    }

    private void closeStage() {
        Stage stage = (Stage) tagComboBox.getScene().getWindow();
        stage.close();
    }
     private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
