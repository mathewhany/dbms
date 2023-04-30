package dbms.pages;

import dbms.DBAppException;
import dbms.tables.Table;

import java.util.Vector;

public interface PagesIndexManager {
    Vector<PageIndexItem> loadPagesIndex(Table table) throws DBAppException;

    void savePagesIndex(Table table) throws DBAppException;
}
