package dbms.pages;

import dbms.DBAppException;

public interface PageManager {
    Page loadPage(String fileName) throws DBAppException;
    void savePage(Page page) throws DBAppException;

    void deletePage(String pageId) throws DBAppException;
}
