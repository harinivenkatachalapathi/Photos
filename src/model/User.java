package model;

import java.io.*;
import java.util.ArrayList;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String storeDir = "dat";
    public static final String storeFile = "users.dat";


    public static ArrayList<User> users = new ArrayList<>();

    public String username;
    public ArrayList<Album> albums;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
    }
    
    public String getUsername() {
        return username;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public void deleteAlbum(Album album) {
        albums.remove(album);
    }

    // Write the User object to a file
    public void writeApp() throws IOException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Read the User object from a file
    public static User readApp() throws IOException, ClassNotFoundException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (User) ois.readObject();
        }
    }
}
