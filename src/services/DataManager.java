package services;

import models.AppData;
import java.io.*;

public class DataManager {
    private static final String DATA_FILE = "data.dat";

    public static AppData loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            return new AppData();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof AppData) {
                return (AppData) obj;
            }
        } catch (Exception e) {
            System.err.println("Error reading " + DATA_FILE + ": " + e.getMessage() + ". Starting with empty data.");
        }
        return new AppData();
    }

    public static boolean saveData(AppData appData) {
        if (appData == null) {
            appData = new AppData();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(appData);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to " + DATA_FILE + ": " + e.getMessage());
            return false;
        }
    }
}
