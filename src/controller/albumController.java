package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class albumController {

    @FXML
    public Button returnUser;

    @FXML
    public Label title;
    @FXML
    public TilePane tilePane;

    public User currentUser;

    public Album currentAlbum;

    public userController tempController = new userController();

    private static final String storeDir = "dat";
    private static final String storeFile = "photos.dat";


    
    /** 
     * @param e
     * @throws Exception
     */
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

  
    
    /** 
     * @param e
     * @throws Exception
     */
    public void addPhoto(ActionEvent e) throws Exception {
        FileChooser filechooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.bmp",
                "*.jpeg", "*.gif");
        filechooser.getExtensionFilters().add(extFilterJPG);
        File imgfile = filechooser.showOpenDialog(null);
    
        if (imgfile == null) {
            return;
        } else {
            // Check if the album already contains a photo with the same file path
            boolean fileExistsInCurrentAlbum = currentAlbum.getPhotos().stream()
                    .anyMatch(photo -> photo.file.getAbsolutePath().equals(imgfile.getAbsolutePath()));
    
            if (fileExistsInCurrentAlbum) {
                // Show alert if the file already exists in the current album
                showAlert("Duplicate Photo", "This photo already exists in the current album.");
            } else {
                // Search for the photo in all albums
                Photo existingPhoto = findPhotoInAllAlbums(imgfile.getAbsolutePath());
    
                if (existingPhoto != null) {
                    // If the photo exists in another album, add a reference to it in the current album
                    currentAlbum.getPhotos().add(existingPhoto);
                    showAlert("Photo Added", "A reference to the existing photo has been added to this album.");
                } else {
                    // If the photo is new, proceed with adding it as a new photo
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/caption.fxml"));
                    Parent root = loader.load();
                    captionInputController controller = loader.getController();
    
                    Stage captionStage = new Stage();
                    captionStage.initModality(Modality.APPLICATION_MODAL);
                    captionStage.setTitle("Enter Caption");
                    captionStage.setScene(new Scene(root));
    
                    controller.initData(currentAlbum, imgfile);
                    captionStage.showAndWait();
                }
    
                // Refresh the album view and save changes
                writeAlbumsToFile();
                refresh();
            }
        }
    }

    private Photo findPhotoInAllAlbums(String filePath) {
        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                if (photo.file.getAbsolutePath().equals(filePath)) {
                    return photo; // Return the first match found
                }
            }
        }
        return null; // No matching photo found in any album
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

            imageBox.setOnMouseClicked(event -> handlePhotoBoxClick(event));
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

    @FXML
    private void handlePhotoBoxClick(MouseEvent event) {
        VBox clickedBox = (VBox) event.getSource();
        // Now you can access the Photo object associated with the clicked VBox
        Photo photo = (Photo) clickedBox.getUserData();

        // Handle the click event (e.g., display photo details)
        displayPhotoDetails(photo);
    }

    public void showSlideshow(ActionEvent e) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/slideshow.fxml"));
        Parent sceneManager = (Parent) fxmlLoader.load();
        slideshowController slideshowController = fxmlLoader.getController();
        slideshowController.initData(currentAlbum, currentUser);
        // You can pass any data to the controller here if needed
        // adminController.setData(...);
        Scene userScene = new Scene(sceneManager);
        Stage appStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        appStage.setScene(userScene);

        // If you need to call any initialization methods in the controller, do it here
        // adminController.init();
        appStage.show();
    }

    public void renameAlbum(ActionEvent e) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Rename your album");
        dialog.setContentText("Please enter new album name:");
        // Show the dialog and capture the input
        Optional<String> result = dialog.showAndWait();

        // Process the result
        result.ifPresent(nameInput -> {
            // Split the input into tag name and tag value

            nameInput = nameInput.trim();
            currentAlbum.albumName = nameInput;

        });
        writeAlbumsToFile();
        tempController.currentUser = currentUser;
        tempController.writeAlbumsToFile();
        refresh();

    }

    public void initialize() {
        // Initialize the tilePane with zoom functionality
        tilePane.setOnScroll(this::handleZoom);
        // The rest of your initialization code...
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();

            if (deltaY > 0) {
                tilePane.setScaleX(tilePane.getScaleX() * zoomFactor);
                tilePane.setScaleY(tilePane.getScaleY() * zoomFactor);
            } else {
                tilePane.setScaleX(tilePane.getScaleX() / zoomFactor);
                tilePane.setScaleY(tilePane.getScaleY() / zoomFactor);
            }
            event.consume();
        }
    }
    
}
