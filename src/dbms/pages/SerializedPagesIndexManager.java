package dbms.pages;

import dbms.DBAppException;
import dbms.tables.Table;

import java.io.*;
import java.util.Vector;

public class SerializedPagesIndexManager implements PagesIndexManager {
    @Override
    public Vector<PageIndexItem> loadPagesIndex(Table table) throws DBAppException {
        String indexFilePath = table.getTableName() + ".ser";

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
        String indexFilePath = table.getTableName() + ".ser";

        try(ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(indexFilePath))) {
            ois.writeObject(table.getPagesIndex());
        } catch (IOException e) {
            throw new DBAppException("Error while saving table index" + table.getTableName());
        }
    }
}
