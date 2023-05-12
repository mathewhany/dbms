package dbms.indicies;

import dbms.DBAppException;
import dbms.tables.Table;

public interface IndexManager {
    Index loadIndex(String indexName, String tableName) throws DBAppException;

    void saveIndex(Index index) throws DBAppException;

    Index createIndex(String indexName, String indexType, Table table, String[] columnNames) throws
        DBAppException;
}
