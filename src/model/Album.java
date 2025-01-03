package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.*;


public class Album implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final String storeDir = "dat";
    public static final String storeFile = "users.dat";

    public String albumName;
    public ArrayList<Photo> photos;

    public Album(String name) {
        this.albumName = name;
        this.photos = new ArrayList<>();
    }

    public String getName() {
        return albumName;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }
    // Write the Album object to a file
    public void writeApp() throws IOException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Read the Album object from a file
    public static Album readApp() throws IOException, ClassNotFoundException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Album) ois.readObject();
        }
    }
    
    public String getAlbumName(){
        return albumName;
    }
}

