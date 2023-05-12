package dbms.indicies;

import dbms.DBAppException;
import dbms.pages.Page;
import dbms.tables.Table;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

public class SerializedIndexManager implements IndexManager {
    private final String indexDir;
    private final Hashtable<String, IndexFactory> indexFactories;

    public SerializedIndexManager(
        String indexDir,
        Hashtable<String, IndexFactory> indexFactories
    ) throws DBAppException {
        this.indexDir = indexDir;
        this.indexFactories = indexFactories;

        try {
            Files.createDirectories(Paths.get(indexDir));
        } catch (IOException e) {
            throw new DBAppException("Failed to create index directory: " + indexDir, e);
        }
    }

    public Index loadIndex(String indexName, String tableName) throws DBAppException {
        String filePath = getFilePath(indexName, tableName);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Index) ois.readObject();
        } catch (IOException e) {
            throw new DBAppException("Index file not found: " + filePath, e);
        } catch (ClassNotFoundException e) {
            throw new DBAppException("Index file couldn't be loaded : " + filePath);
        }
    }


    @Override
    public void saveIndex(Index index) throws DBAppException {
        String fileName = getFilePath(index.getName(), index.getTableName());

        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ois.writeObject(index);
        } catch (IOException e) {
            throw new DBAppException("Saving page failed: " + index.getName(), e);
        }
    }

    @Override
    public Index createIndex(
        String indexName,
        String indexType,
        Table table,
        String[] columnNames
    ) throws DBAppException {
        if (!indexFactories.containsKey(indexType)) {
            throw new DBAppException("Index type not found: " + indexType);
        }

        IndexFactory indexFactory = indexFactories.get(indexType);
        return indexFactory.createIndex(indexName, table, columnNames);
    }

    public String getFilePath(String indexName, String tableName) {
        return indexDir + File.separator + tableName + "-" + indexName + ".ser";
    }
}
