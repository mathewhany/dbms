package dbms.pages;

import dbms.DBAppException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SerializedPageManager implements PageManager {
    private final String pagesDir;

    public SerializedPageManager(String pagesDir) throws DBAppException {
        this.pagesDir = pagesDir;

        try {
            Files.createDirectories(Paths.get(pagesDir));
        } catch (IOException e) {
            throw new DBAppException("Failed to create pages directory: " + pagesDir, e);
        }
    }

    @Override
    public Page loadPage(String pageId) throws DBAppException {
        String filePath = getFilePath(pageId);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Page) ois.readObject();
        } catch (IOException e) {
            throw new DBAppException("Page file not found: " + filePath);
        } catch (ClassNotFoundException e) {
            throw new DBAppException("Page file couldn't be loaded : " + filePath);
        }
    }

    @Override
    public void savePage(Page page) throws DBAppException {
        String fileName = getFilePath(page.getPageId());

        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ois.writeObject(page);
        } catch (IOException e) {
            throw new DBAppException("Saving page failed: " + page.getPageId(), e);
        }
    }

    @Override
    public void deletePage(String pageId) throws DBAppException {
        try {
            Files.deleteIfExists(Paths.get(getFilePath(pageId)));
        } catch (IOException e) {
            throw new DBAppException("Failed to delete page: " + pageId, e);
        }
    }

    private String getFilePath(String pageId) {
        return pagesDir + File.separator + pageId + ".ser";
    }
}
