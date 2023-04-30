package dbms.pages;

import dbms.DBAppException;
import dbms.tables.Table;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

public class SerializedPagesIndexManager implements PagesIndexManager {
    private String tablesDir;

    public SerializedPagesIndexManager(String tablesDir) throws DBAppException {
        this.tablesDir = tablesDir;

        try {
            Files.createDirectories(Paths.get(tablesDir));
        } catch (IOException e) {
            throw new DBAppException("Failed to create tables directory: " + tablesDir, e);
        }
    }

    @Override
    public Vector<PageIndexItem> loadPagesIndex(Table table) throws DBAppException {
        String indexFilePath = getPath(table);

        if (!new File(indexFilePath).exists()) {
            return new Vector<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilePath))) {
            return (Vector<PageIndexItem>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DBAppException("Error loading pages index for table: " + table.getTableName(), e);
        }
    }


    @Override
    public void savePagesIndex(Table table) throws DBAppException {
        String indexFilePath = getPath(table);

        try(ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(indexFilePath))) {
            ois.writeObject(table.getPagesIndex());
        } catch (IOException e) {
            throw new DBAppException("Error while saving table index" + table.getTableName());
        }
    }

    private String getPath(Table table) {
        return tablesDir + File.separator + table.getTableName() + ".ser";
    }
}
