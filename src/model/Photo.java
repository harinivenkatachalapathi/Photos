package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.time.LocalDate;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String storeDir = "dat";
    public static final String storeFile = "users.dat";

    //private String filePath; // Path to the photo file on the user's machine
    public File file;
    public String caption;
    private LocalDate modificationDate; // Using milliseconds for simplicity
    private List<String> albumNames;


    private ArrayList<String[]> tags;

    public Photo (File file,String caption) {
        //this.filePath = filePath;
        this.modificationDate = LocalDate.now();
        this.tags = new ArrayList<>();
        this.file = file;
        this.caption = caption;
    }

    //public String getFilePath() {
    //    return filePath;
   // }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDate getModificationDate() {
        return modificationDate;
    }

    public ArrayList<String[]> getTags() {
        return tags;
    }

    
    public void addTag(String[] name) {
        tags.add(name);
    }

    public void removeTag(String[] tagToRemove) {
        tags.removeIf(tag -> tag[0].equals(tagToRemove[0]) && tag[1].equals(tagToRemove[1]));
    }
    
    public List<String> getAlbumNames() {
        return albumNames;
    }

    // Write the Photo object to a file
    public void writeApp() throws IOException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Read the Photo object from a file
    public static Photo readApp() throws IOException, ClassNotFoundException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Photo) ois.readObject();
        }
    }
    public boolean hasTag(String tagName, String tagValue) {
        for (String[] tag : tags) {
            if (tag.length >= 2) { // Ensure the array has at least two elements
                String currentTagName = tag[0];
                String currentTagValue = tag[1];
                
                if (currentTagName.equalsIgnoreCase(tagName) && currentTagValue.equalsIgnoreCase(tagValue)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
