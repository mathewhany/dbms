package dbms.config;

import dbms.DBAppException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfigManager implements ConfigManager {
    private final String fileName;

    public PropertiesConfigManager(String fileName) {
        this.fileName = fileName;
    }

    public Config load() throws DBAppException {
        try(FileInputStream fis = new FileInputStream(fileName)) {
            Properties properties = new Properties();
            properties.load(fis);
            int maxPerPage = Integer.parseInt(properties.getProperty("MaximumRowsCountinTablePage"));
            int maxPerOctreeNode = Integer.parseInt(properties.getProperty("MaximumEntriesinOctreeNode"));

            return new Config(
                maxPerPage,
                maxPerOctreeNode
            );
        } catch (IOException e) {
            throw new DBAppException("Couldn't load config file: " + fileName, e);
        }
    }
}
