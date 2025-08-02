package game.achievements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of AchievementFile using standard file I/O.
 */
public class FileHandler implements AchievementFile {
    private String fileName = DEFAULT_FILE_LOCATION;

    /**
     * Constructs a FileHandler.
     */
    public FileHandler() {}

    @Override
    public void setFileLocation(String fileLocation) {
        this.fileName = fileLocation;
    }

    @Override
    public String getFileLocation() {
        return fileName;
    }

    @Override
    public void save(String data) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(fileName));
            bufferedWriter.write(data);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("IOException occurred");
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("IOException occurred");
            }
        }
    }

    @Override
    public List<String> read() {
        BufferedReader bufferedReader = null;
        List<String> savedData = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line = bufferedReader.readLine();
            while (line != null) {
                savedData.add(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            System.out.println();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.println();
                }
            }
        }

        return savedData;
    }
}
