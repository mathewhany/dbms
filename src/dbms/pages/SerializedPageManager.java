package dbms.pages;

import dbms.DBAppException;

import java.io.*;

public class SerializedPageManager implements PageManager {
    @Override
    public Page loadPage(String fileName) throws DBAppException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Page) ois.readObject();
        } catch (IOException e) {
            throw new DBAppException("dbms.pages.Page file not found: " + fileName);
        } catch (ClassNotFoundException e) {
            throw new DBAppException("dbms.pages.Page file couldn't be loaded : " + fileName);
        }
    }

    @Override
    public void savePage(Page page) throws DBAppException {
        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(page.getFileName()))) {
            ois.writeObject(page);
        } catch (IOException e) {
            throw new DBAppException("Saving page failed: " + page.getFileName(), e);
        }
    }
}
