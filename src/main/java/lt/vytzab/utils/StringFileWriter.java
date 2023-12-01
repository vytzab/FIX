package lt.vytzab.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class StringFileWriter {

    private String filePath;

    public StringFileWriter(String filePath) {
        this.filePath = filePath;
    }

    public void writeStringToFile(String content, boolean append) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(content + "\n");
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }
    }

    public void writeStringToFile(String content) {
        writeStringToFile(content, false); // By default, overwrite the file
    }
}