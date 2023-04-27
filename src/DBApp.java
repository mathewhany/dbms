import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class DBApp {
    private static final String METADATA_FILE_PATH = "metadata.csv";
    private static final String CONFIG_FILE_PATH = "DBApp.config";
    private Config config;

    /**
     * Executed once when the application starts.
     */
    public void init() {
        ConfigManager configManager = new ConfigManager(CONFIG_FILE_PATH);

        try {
            config = configManager.load();
        } catch (DBAppException e) {
            System.out.println("Failed to load config file");
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
        // TODO: Handle errors (e.g. table already exists, invalid column type, missing min/max values, clustering key not in column types, etc.)
        // TODO: Use dependency injection and strategy pattern to separate metadata file handling from the rest of the code
        CsvLoader csvLoader = new CsvLoader();
        Vector<Hashtable<String, String>> tableRows = new Vector<>();

        File metadataFile = new File(METADATA_FILE_PATH);
        if (metadataFile.exists()) {
            tableRows = csvLoader.load(METADATA_FILE_PATH);
        }

        Set<String> columnNames = columnTypes.keySet();

        for (String columnName : columnNames) {
            Hashtable<String, String> row = new Hashtable<>();
            row.put("Table Name", tableName);
            row.put("Column Name", columnName);
            row.put("Column Type", columnTypes.get(columnName));
            row.put("ClusteringKey", columnName.equals(clusteringKeyColumn) ? "True" : "False");
            row.put("IndexName", "null");
            row.put("IndexType", "null");
            row.put("min", columnMin.get(columnName));
            row.put("max", columnMax.get(columnName));
            tableRows.add(row);
        }

        csvLoader.save(METADATA_FILE_PATH, tableRows);
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
        table.save();

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
        // Load metadata for table

        // Validate inputs (table exists, column names exist, column types match, etc.)

        // Find the page to update, same as insertIntoTable

        // Find the row to update, same as insertIntoTable

        // Update the row

        Table table = loadTable(tableName);
        table.update(clusteringKeyValue, newValues);
        table.save();
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
        table.save();
    }

    /**
     * Selects rows from the table.
     *
     * @param sqlTerms  An array of SQLTerm objects. Each SQLTerm object is a condition (table, column, value, operator).
     * @param operators An array of strings. Each string is an operator (AND, OR).
     *                  The array will be of size 0 in case there is only one condition.
     * @return An iterator that iterates over the rows that match the conditions.
     * @throws DBAppException
     */
    public Iterator selectFromTable(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        return null;
    }

    public Table loadTable(String tableName) throws DBAppException {
        CsvLoader csvLoader = new CsvLoader();

        Vector<Hashtable<String, String>> metadata = csvLoader.load(METADATA_FILE_PATH);
        Vector<Hashtable<String, String>> tableColumns = new Vector<>();

        for (Hashtable<String, String> row : metadata) {
            if (row.get("Table Name").equals(tableName)) {
                tableColumns.add(row);
            }
        }


        Hashtable<String, String> columnTypes = new Hashtable<>();
        Hashtable<String, String> columnMin = new Hashtable<>();
        Hashtable<String, String> columnMax = new Hashtable<>();
        String clusteringKey = "";

        for (Hashtable<String, String> tableColumn : tableColumns) {
            String columnName = tableColumn.get("Column Name");
            columnTypes.put(columnName, tableColumn.get("Column Type"));
            columnMin.put(columnName, tableColumn.get("min"));
            columnMax.put(columnName, tableColumn.get("max"));
            if (tableColumn.get("ClusteringKey").equals("True")) {
                clusteringKey = columnName;
            }
        }

        Table table = new Table(
            tableName,
            clusteringKey,
            columnTypes,
            columnMin,
            columnMax,
            config
        );

        String indexFilePath = tableName + ".ser";
        File file = new File(indexFilePath);

        if (!file.exists()) {
            return table;
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilePath));
            Vector<PageIndexItem> pageIndex = (Vector<PageIndexItem>) ois.readObject();
            table.setPagesIndex(pageIndex);
            return table;
        } catch (IOException | ClassNotFoundException e) {
            throw new DBAppException("Error loading table: " + tableName, e);
        }

    }
}
