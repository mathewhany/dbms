package dbms.tables;

import dbms.indicies.IndexManager;
import dbms.config.Config;
import dbms.util.CsvLoader;
import dbms.DBAppException;
import dbms.datatype.DataType;
import dbms.pages.PageManager;

import java.io.File;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class CsvTableManager implements TableManager {
    private static final String HEADER_TABLE_NAME = "Table Name";
    private static final String HEADER_COLUMN_NAME = "Column Name";
    private static final String HEADER_COLUMN_TYPE = "Column Type";
    private static final String HEADER_CLUSTERING_KEY = "ClusteringKey";
    private static final String HEADER_INDEX_NAME = "Index Name";
    private static final String HEADER_INDEX_TYPE = "Index Type";
    private static final String HEADER_COLUMN_MIN = "min";
    private static final String HEADER_COLUMN_MAX = "max";
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String NULL = "null";


    private final String metadataFilePath;
    private final Config config;
    private final PageManager pageManager;
    private final Hashtable<String, DataType> dataTypes;
    private final IndexManager indexManager;

    public CsvTableManager(
        String metadataFilePath,
        Config config,
        PageManager pageManager,
        Hashtable<String, DataType> dataTypes,
        IndexManager indexManager
    ) throws DBAppException {
        this.metadataFilePath = metadataFilePath;
        this.config = config;
        this.pageManager = pageManager;
        this.dataTypes = dataTypes;
        this.indexManager = indexManager;
    }


    @Override
    public Table loadTable(String tableName) throws DBAppException {
        Vector<Hashtable<String, String>> metadata = CsvLoader.load(metadataFilePath);
        Vector<Hashtable<String, String>> tableColumns = new Vector<>();

        for (Hashtable<String, String> row : metadata) {
            if (row.get(HEADER_TABLE_NAME).equals(tableName)) {
                tableColumns.add(row);
            }
        }

        if (tableColumns.isEmpty()) {
            throw new DBAppException("Table " + tableName + " not found");
        }

        Hashtable<String, String> columnTypes = new Hashtable<>();
        Hashtable<String, String> columnMin = new Hashtable<>();
        Hashtable<String, String> columnMax = new Hashtable<>();
        Hashtable<String, String> columnIndexName = new Hashtable<>();
        Hashtable<String, String> columnIndexType = new Hashtable<>();

        String clusteringKey = "";

        for (Hashtable<String, String> tableColumn : tableColumns) {
            if (!dataTypes.containsKey(tableColumn.get(HEADER_COLUMN_TYPE))) {
                throw new DBAppException("Invalid data type");
            }

            String columnName = tableColumn.get(HEADER_COLUMN_NAME);
            columnTypes.put(columnName, tableColumn.get(HEADER_COLUMN_TYPE));
            columnMin.put(columnName, tableColumn.get(HEADER_COLUMN_MIN));
            columnMax.put(columnName, tableColumn.get(HEADER_COLUMN_MAX));
            String indexName = tableColumn.get(HEADER_INDEX_NAME);
            String indexType = tableColumn.get(HEADER_INDEX_TYPE);

            if (!indexName.equals(NULL)) {
                columnIndexName.put(columnName, indexName);
                columnIndexType.put(columnName, indexType);
            }

            if (tableColumn.get(HEADER_CLUSTERING_KEY).equals(TRUE)) {
                clusteringKey = columnName;
            }
        }

        return new Table(
            tableName,
            clusteringKey,
            columnTypes,
            columnMin,
            columnMax,
            config,
            pageManager,
            dataTypes,
            indexManager,
            columnIndexName,
            columnIndexType
        );
    }

    @Override
    public void createTable(Table table) throws DBAppException {
        Vector<Hashtable<String, String>> tableRows = new Vector<>();

        if (new File(metadataFilePath).exists()) {
            tableRows = CsvLoader.load(metadataFilePath);
        }

        for (Hashtable<String, String> row : tableRows) {
            if (row.get(HEADER_TABLE_NAME).equals(table.getTableName())) {
                throw new DBAppException("Table already exists: " + table.getTableName());
            }
        }

        Hashtable<String, String> columnTypes = table.getColumnTypes();
        Hashtable<String, Object> columnMin = table.getColumnMin();
        Hashtable<String, Object> columnMax = table.getColumnMax();

        Set<String> columnNames = table.getColumnTypes().keySet();

        for (String columnName : columnNames) {
            Hashtable<String, String> row = new Hashtable<>();
            row.put(HEADER_TABLE_NAME, table.getTableName());
            row.put(HEADER_COLUMN_NAME, columnName);
            row.put(HEADER_COLUMN_TYPE, columnTypes.get(columnName));
            row.put(
                HEADER_CLUSTERING_KEY,
                columnName.equals(table.getClusteringKeyColumn()) ? TRUE : FALSE
            );
            row.put(HEADER_INDEX_NAME, NULL);
            row.put(HEADER_INDEX_TYPE, NULL);

            DataType dataType = dataTypes.get(columnTypes.get(columnName));

            row.put(HEADER_COLUMN_MIN, dataType.toString(columnMin.get(columnName)));
            row.put(HEADER_COLUMN_MAX, dataType.toString(columnMax.get(columnName)));
            tableRows.add(row);
        }

        CsvLoader.save(metadataFilePath, tableRows);
    }

    @Override
    public void saveTable(Table table) throws DBAppException {
        Vector<Hashtable<String, String>> tableRows = CsvLoader.load(metadataFilePath);
        boolean found = false;

        for (Hashtable<String, String> row : tableRows) {
            if (row.get(HEADER_TABLE_NAME).equals(table.getTableName())) {
                found = true;
                String columnName = row.get(HEADER_COLUMN_NAME);
                String indexName = table.getIndexName(columnName);
                String indexType = table.getIndexType(columnName);

                row.put(HEADER_INDEX_NAME, indexName == null ? NULL : indexName);
                row.put(HEADER_INDEX_TYPE, indexType == null ? NULL : indexType);
            }
        }

        if (!found) {
            throw new DBAppException("Table not found: " + table.getTableName());
        }

        CsvLoader.save(metadataFilePath, tableRows);
    }

    @Override
    public void preloadIndices() throws DBAppException {
        if (!new File(metadataFilePath).exists()) {
            return;
        }

        Vector<Hashtable<String, String>> tableRows = CsvLoader.load(metadataFilePath);

        for (Hashtable<String, String> row : tableRows) {
            String tableName = row.get(HEADER_TABLE_NAME);
            String indexName = row.get(HEADER_INDEX_NAME);
            String indexType = row.get(HEADER_INDEX_TYPE);

            if (!indexName.equals(NULL)) {
                indexManager.loadIndex(indexName, tableName);
            }
        }
    }

}
