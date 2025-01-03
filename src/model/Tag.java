package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Tag {
    
    private static final long serialVersionUID = 1L;

    public static final String storeDir = "dat";
    public static final String storeFile = "users.dat";

    public String tagName;
    public String tagValue;
    public Tag(String type, String value) {
        this.tagName = type;
        this.tagValue = value;
    }

    public String getName() {
        return tagName;
    }

    public String getValue() {
        return tagValue;
    }
    // Write the Tag object to a file
    public void writeApp() throws IOException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Read the Tag object from a file
    public static Tag readApp() throws IOException, ClassNotFoundException {
        String filePath = storeDir + File.separator + storeFile;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Tag) ois.readObject();
        }
    }
}