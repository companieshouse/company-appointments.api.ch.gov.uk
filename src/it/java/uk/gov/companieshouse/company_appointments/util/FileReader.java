package uk.gov.companieshouse.company_appointments.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import org.springframework.util.FileCopyUtils;

public class FileReader {

    public static String readInputFile(String name) {
        return readFile("input/" + name);
    }

    public static String readOutputFile(String name) {
        return readFile("output/" + name);
    }

    public static String readDataFile(String name) {
        return readFile("data/" + name);
    }

    private static String readFile(String path) {
        String data;
        path = "src/it/resources/" + path + ".json";
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(Files.newInputStream(new File(path).toPath())));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
