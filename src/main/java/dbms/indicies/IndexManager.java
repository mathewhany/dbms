package dbms.indicies;

import dbms.DBAppException;

public interface IndexManager {
    Index loadIndex(String indexName, String tableName) throws DBAppException;

    void saveIndex(Index index) throws DBAppException;
}
