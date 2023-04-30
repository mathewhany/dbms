package dbms;

import dbms.config.Config;
import dbms.config.ConfigManager;
import dbms.config.PropertiesConfigManager;
import dbms.datatype.*;
import dbms.pages.PageIndexItem;
import dbms.pages.SerializedPageManager;
import dbms.pages.SerializedPagesIndexManager;
import dbms.tables.CsvTableManager;
import dbms.tables.Table;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class DBApp {
    private static final String METADATA_FILE_PATH = "src/main/resources/metadata.csv";
    private static final String CONFIG_FILE_PATH = "src/main/resources/DBApp.config";
    private static final String TABLES_DIR = "src/main/resources/Data/tables/";
    private static final String PAGES_DIR = "src/main/resources/Data/pages/";

    private Config config;
    private CsvTableManager tableManager;
    private SerializedPageManager pageManager;
    private SerializedPagesIndexManager pagesIndexManager;
    private Hashtable<String, DataType> dataTypes;

    /**
     * Executed once when the application starts.
     */
    public void init() {
        ConfigManager configManager = new PropertiesConfigManager(CONFIG_FILE_PATH);

        try {
            config = configManager.load();
        } catch (DBAppException e) {
            System.out.println("Failed to load config file");
        }

        dataTypes = new Hashtable<>();
        dataTypes.put("java.lang.Integer", new IntegerDataType());
        dataTypes.put("java.lang.Double", new DoubleDataType());
        dataTypes.put("java.lang.String", new StringDataType());
        dataTypes.put("java.util.Date", new DateDataType());

        try {
            pageManager = new SerializedPageManager(PAGES_DIR);
        } catch (DBAppException e) {
            System.out.println("Failed to load page manager");
        }

        try {
            tableManager = new CsvTableManager(METADATA_FILE_PATH, config, pageManager, dataTypes);
        } catch (DBAppException e) {
            System.out.println("Failed to load table manager");
        }

        try {
            pagesIndexManager = new SerializedPagesIndexManager(TABLES_DIR);
        } catch (DBAppException e) {
            System.out.println("Failed to load pages index manager");
        }
    }

    /**
     * Creates a one table in the database.
     *
     * @param tableName           The name of the table to create.
     * @param clusteringKeyColumn The name of the column that will be the primary key and the clustering column as well.
     * @param columnTypes         A hashtable that holds the column name as key and the data type as value.
     *                            The column type of the clustering key must be passed as well.
     * @param columnMin           A hashtable that holds the column name as key and the minimum value as value.
     * @param columnMax           A hashtable that holds the column name as key and the maximum value as value.
     * @throws DBAppException
     */
    public void createTable(
        String tableName,
        String clusteringKeyColumn,
        Hashtable<String, String> columnTypes,
        Hashtable<String, String> columnMin,
        Hashtable<String, String> columnMax
    ) throws DBAppException {
        Table table = new Table(
            tableName,
            clusteringKeyColumn,
            columnTypes,
            columnMin,
            columnMax,
            config,
            pageManager,
            dataTypes
        );

        tableManager.createTable(table);
    }

    /**
     * Creates an octree index on the specified table and columns.
     *
     * @param tableName   The name of the table to create the index on.
     * @param columnNames The names of the columns to create the index on. Must be three.
     * @throws DBAppException When the number of column names is not three.
     */
    public void createIndex(
        String tableName, String[] columnNames
    ) throws DBAppException {
    }

    /**
     * Inserts a new row into the table.
     *
     * @param tableName The name of the table to insert into.
     * @param values    A hashtable that holds the column name as key and the value as value.
     *                  Must include a value for the primary key
     * @throws DBAppException
     */
    public void insertIntoTable(
        String tableName, Hashtable<String, Object> values
    ) throws DBAppException {
        Table table = loadTable(tableName);
        table.insert(values);
        pagesIndexManager.savePagesIndex(table);

        table = null;
        System.gc();
    }

    /**
     * Updates one row only in the table.
     *
     * @param tableName          The name of the table to update.
     * @param clusteringKeyValue The value of the clustering key to look for to find the row to update.
     * @param newValues          A hashtable that holds the column name as key and the new value as value.
     *                           This should not include the primary key.
     * @throws DBAppException
     */
    public void updateTable(
        String tableName, String clusteringKeyValue, Hashtable<String, Object> newValues
    ) throws DBAppException {
        Table table = loadTable(tableName);
        table.update(clusteringKeyValue, newValues);
        pagesIndexManager.savePagesIndex(table);

        table = null;
        System.gc();
    }

    /**
     * Delete one or more rows from the table.
     *
     * @param tableName    The name of the table to delete from.
     * @param searchValues A hashtable that holds the column name as key and the value as value.
     *                     This will be used in search to identify which rows/tuples to delete.
     *                     The entries are ANDed together.
     * @throws DBAppException
     */
    public void deleteFromTable(
        String tableName, Hashtable<String, Object> searchValues
    ) throws DBAppException {
        Table table = loadTable(tableName);
        table.delete(searchValues);
        pagesIndexManager.savePagesIndex(table);

        table = null;
        System.gc();
    }

    /**
     * Selects rows from the table.
     *
     * @param sqlTerms  An array of dbms.SQLTerm objects. Each dbms.SQLTerm object is a condition (table, column, value, operator).
     * @param operators An array of strings. Each string is an operator (AND, OR).
     *                  The array will be of size 0 in case there is only one condition.
     * @return An iterator that iterates over the rows that match the conditions.
     * @throws DBAppException
     */
    public Iterator selectFromTable(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        return null;
    }

    public Table loadTable(String tableName) throws DBAppException {
        Table table = tableManager.loadTable(tableName);
        Vector<PageIndexItem> pagesIndex = pagesIndexManager.loadPagesIndex(table);
        table.setPagesIndex(pagesIndex);

        return table;
    }
}
