package dbms.tables;

import dbms.DBAppException;

public interface TableManager {
    Table loadTable(String tableName) throws DBAppException;

    void createTable(Table table) throws DBAppException;

    void saveTable(Table table) throws DBAppException;

    void preloadIndices() throws DBAppException;
}
