package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.User;
import javafx.stage.FileChooser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

public class searchController {

    @FXML
    public Button returnUser;

    @FXML
    public TextField searchField;

    @FXML
    public Label title;

    @FXML
    public TilePane tilePane;

    @FXML
    private Text tagText;

    @FXML
    private Text dateText;

    public User currentUser;

    public Album currentAlbum;

    public userController tempController = new userController();

    @FXML
    private TextField startDateField, endDateField;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public boolean tagSearchMode = true;

    private static final String storeDir = "dat";
    private static final String storeFile = "photos.dat";

    public void returnUser(ActionEvent e) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/user.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        userController userController = fxmlLoader.getController();
        userController.currentUser = currentUser;
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene userScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(userScene);
        userController.refresh();
        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();
    }

    public void addPhoto(ActionEvent e) throws Exception {
        FileChooser filechooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
                "*.jpeg", "*.gif");
        filechooser.getExtensionFilters().add(extFilterJPG);
        File imgfile = filechooser.showOpenDialog(null);

        if (imgfile == null) {
            return;
        } else {
            // Load the FXML file for the caption input dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/caption.fxml"));
            Parent root = loader.load();
            captionInputController controller = loader.getController();

            // Open a new stage for entering caption
            Stage captionStage = new Stage();
            captionStage.initModality(Modality.APPLICATION_MODAL);
            captionStage.setTitle("Enter Caption");
            captionStage.setScene(new Scene(root));

            // Pass the current album and image file to the controller
            controller.initData(currentAlbum, imgfile);

            // Show the caption input dialog
            captionStage.showAndWait();
        }
        writeAlbumsToFile();
        refresh();
    }

    public void refresh() {
        title.setText(currentAlbum.albumName);
        readAlbumsFromFile();
        displayAlbumPhotos(currentAlbum);
    }

    public void displayAlbumPhotos(Album album) {
        // Clear existing content
        tilePane.getChildren().clear();

        for (Photo photo : album.getPhotos()) {
            Image image = new Image(photo.file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200); // Adjust width as needed
            imageView.setFitHeight(200); // Adjust height as needed

            // Create a label for the caption
            Label captionLabel = new Label(photo.getCaption());
            captionLabel.setMaxWidth(200); // Limit the width of the label to match the image width
            captionLabel.setWrapText(true); // Enable text wrapping
            captionLabel.setAlignment(Pos.CENTER); // Center align the caption

            // Create a VBox to contain the image and caption
            VBox imageBox = new VBox(10); // Spacing between image and caption
            imageBox.setAlignment(Pos.CENTER); // Center align the VBox
            imageBox.getChildren().addAll(imageView, captionLabel);
            imageBox.setUserData(photo);

            // Add the VBox to the TilePane
            tilePane.getChildren().add(imageBox);
        }
    }

    public void writeAlbumsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(storeDir + File.separator + storeFile))) {
            // Create a map to associate each user with their albums and photos
            Map<String, Map<String, ArrayList<Photo>>> userData = new HashMap<>();
            Map<String, ArrayList<Photo>> albumsMap = new HashMap<>();
            for (Album album : currentUser.albums) {
                albumsMap.put(album.albumName, album.photos);
            }
            userData.put(currentUser.getUsername(), albumsMap);
            oos.writeObject(userData);
            System.out.println("Albums and photos saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save albums and photos to file.");
        }
    }

    public void readAlbumsFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(storeDir + File.separator + storeFile))) {
            Map<String, Map<String, ArrayList<Photo>>> userData = (Map<String, Map<String, ArrayList<Photo>>>) ois
                    .readObject();
            Map<String, ArrayList<Photo>> albumsMap = userData.get(currentUser.getUsername());
            if (albumsMap != null) {
                for (Map.Entry<String, ArrayList<Photo>> entry : albumsMap.entrySet()) {
                    String albumName = entry.getKey();
                    ArrayList<Photo> photos = entry.getValue();

                    // Check if the album already exists
                    Album existingAlbum = findAlbum(currentUser, albumName);
                    if (existingAlbum != null) {
                        // If the album exists, update its photos
                        existingAlbum.getPhotos().clear();
                        existingAlbum.getPhotos().addAll(photos);
                    } else {
                        // If the album doesn't exist, create a new one
                        Album album = new Album(albumName);
                        album.photos = photos;
                        currentUser.albums.add(album);
                    }
                }
            }
            System.out.println("Albums and photos loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Albums and photos from file.");
        }
    }

    private Album findAlbum(User user, String albumName) {
        for (Album album : user.getAlbums()) {
            if (album.albumName.equals(albumName)) {
                return album; // Return existing album
            }
        }
        return null; // Album not found
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /*
     * private Album findOrCreateAlbum(User user, String albumName) {
     * for (Album album : user.getAlbums()) {
     * if (album.albumName.equals(albumName)) {
     * return album; // Return existing album
     * }
     * }
     * // If album doesn't exist, create and return a new one
     * Album newAlbum = new Album(albumName);
     * user.getAlbums().add(newAlbum);
     * return newAlbum;
     * }
     */

    public void displayPhotoDetails(Photo photo) {
        Stage photoDetailsStage = new Stage();
        photoDetailsStage.initModality(Modality.APPLICATION_MODAL);
        photoDetailsStage.setTitle("Photo Details");

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/singlephoto.fxml"));
            Parent root = loader.load();

            // Pass the photo to the controller
            PhotoDetailsController controller = loader.getController();
            controller.setPhoto(photo);
            controller.currentAlbum = currentAlbum;
            controller.currentUser = currentUser;
            controller.initialize();

            Scene scene = new Scene(root);
            photoDetailsStage.setScene(scene);
            photoDetailsStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open photo details.");
        }
    }


public void search(ActionEvent e) {
    Set<Photo> uniquePhotos = new HashSet<>();

    if (tagSearchMode) {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) return;

        String[] tagExpressions = searchQuery.split(",");

        for (String expression : tagExpressions) {
            String[] parts = expression.split("=");
            if (parts.length != 2) continue; // Skip if the format is incorrect

            String tagName = parts[0].trim();
            String tagValue = parts[1].trim();

            // Loop through all photos to find matches
            for (Album album : currentUser.getAlbums()) {
                for (Photo photo : album.getPhotos()) {
                    if (photo.hasTag(tagName, tagValue)) {
                        uniquePhotos.add(photo); // Add to set to ensure uniqueness
                    }
                }
            }
        }
    } else {
        LocalDate startDate = LocalDate.parse(startDateField.getText(), dateFormatter);
        LocalDate endDate = LocalDate.parse(endDateField.getText(), dateFormatter);

        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                LocalDate photoDate = photo.getModificationDate(); // Assuming a getDate() method in Photo
                if ((photoDate.isEqual(startDate) || photoDate.isAfter(startDate)) &&
                    (photoDate.isEqual(endDate) || photoDate.isBefore(endDate))) {
                    uniquePhotos.add(photo); // Add to set to ensure uniqueness
                }
            }
        }
    }

    // Display matching photos
    displayMatchingPhotos(new ArrayList<>(uniquePhotos));
}

    
    /*public void search(ActionEvent e) {
        if (tagSearchMode) {
            String searchQuery = searchField.getText().trim();
            if (searchQuery.isEmpty())
                return;

            ArrayList<Photo> matchingPhotos = new ArrayList<>();
            String[] tagExpressions = searchQuery.split(",");

            for (String expression : tagExpressions) {
                String[] parts = expression.split("=");
                if (parts.length != 2)
                    continue; // Skip if the format is incorrect

                String tagName = parts[0].trim();
                String tagValue = parts[1].trim();

                // Loop through all photos to find matches
                for (Album album : currentUser.getAlbums()) {
                    for (Photo photo : album.getPhotos()) {
                        if (photo.hasTag(tagName, tagValue)) { // Assuming a method in Photo that checks for a tag
                            if (!matchingPhotos.contains(photo)) {
                                matchingPhotos.add(photo);
                            }
                        }
                    }
                }
            }
            // Display matching photos
            displayMatchingPhotos(matchingPhotos);
        } else {
            LocalDate startDate = LocalDate.parse(startDateField.getText(), dateFormatter);
            LocalDate endDate = LocalDate.parse(endDateField.getText(), dateFormatter);

            ArrayList<Photo> matchingPhotos = new ArrayList<>();
            for (Album album : currentUser.getAlbums()) {
                for (Photo photo : album.getPhotos()) {
                    LocalDate photoDate = photo.getModificationDate();
                    // Assuming a getDate() method in Photo
                    if ((photoDate.isEqual(startDate) || photoDate.isAfter(startDate)) &&
                            (photoDate.isEqual(endDate) || photoDate.isBefore(endDate))) {
                        matchingPhotos.add(photo);
                    }
                }
            }

            // Display matching photos
            displayMatchingPhotos(matchingPhotos);
        }

    }*/

    private void displayMatchingPhotos(ArrayList<Photo> photos) {
        tilePane.getChildren().clear(); // Clear previous results

        for (Photo photo : photos) {
            Image image = new Image(photo.file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200); // Adjust width as needed
            imageView.setFitHeight(200); // Adjust height as needed

            // Create a label for the caption
            Label captionLabel = new Label(photo.getCaption());
            captionLabel.setMaxWidth(200); // Limit the width of the label to match the image width
            captionLabel.setWrapText(true); // Enable text wrapping
            captionLabel.setAlignment(Pos.CENTER); // Center align the caption

            // Create a VBox to contain the image and caption
            VBox imageBox = new VBox(10); // Spacing between image and caption
            imageBox.setAlignment(Pos.CENTER); // Center align the VBox
            imageBox.getChildren().addAll(imageView, captionLabel);
            imageBox.setUserData(photo);

            // Add the VBox to the TilePane
            tilePane.getChildren().add(imageBox);
            // Your existing logic to create and display image views
        }
    }

    public void tagSearchSelected(ActionEvent e) {
        tagText.setVisible(true);
        dateText.setVisible(false);
        tagSearchMode = true;
        endDateField.setVisible(false);
        startDateField.setVisible(false);
        searchField.setVisible(true);
        tilePane.getChildren().clear();

    }

    public void dateSearchSelected(ActionEvent e) {
        tagText.setVisible(false);
        dateText.setVisible(true);
        tagSearchMode = false;
        endDateField.setVisible(true);
        startDateField.setVisible(true);
        searchField.setVisible(false);
        tilePane.getChildren().clear();
    }

    public void createAlbum(ActionEvent e) {

        if (!tilePane.getChildren().isEmpty()) {
            // Prompt the user for an album name
            TextInputDialog dialog = new TextInputDialog("New Album");
            dialog.setTitle("Create Album");
            dialog.setHeaderText("Create a new album from search results");
            dialog.setContentText("Please enter a name for the new album:");

            // Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String albumName = result.get();

                // Check if an album with this name already exists for the user
                Album existingAlbum = findAlbum(currentUser, albumName);
                if (existingAlbum != null) {
                    // Inform the user that an album with this name already exists
                    showAlert("Album Creation Error",
                            "An album with this name already exists. Please choose a different name.");
                } else {
                    // Create a new album and add the search result photos
                    Album newAlbum = new Album(albumName);
                    for (Node node : tilePane.getChildren()) {
                        if (node instanceof VBox) {
                            VBox vbox = (VBox) node;
                            Photo photo = (Photo) vbox.getUserData();
                            newAlbum.getPhotos().add(photo);
                        }
                    }

                    // Add the new album to the user's album list
                    currentUser.getAlbums().add(newAlbum);
                    showAlert("Album Created", "A new album with search results has been successfully created.");

                    // Optionally, write the updated album list to the file
                    tempController.currentUser = currentUser;
                    tempController.writeAlbumsToFile(); 

                    writeAlbumsToFile();
                }
            }
        } else {
            // Inform the user that there are no search results to create an album from
            showAlert("No Search Results", "There are no search results to create an album from.");
        }
    }
}
