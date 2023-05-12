package dbms.indicies;

import dbms.DBAppException;
import dbms.tables.Table;

public interface IndexFactory {
    Index createIndex(String indexName, Table tableName, String[] columnNames) throws
        DBAppException;
}
