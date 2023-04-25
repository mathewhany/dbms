import java.io.File;
import java.io.IOException;
import java.util.*;

public class DBApp {
    private static final String METADATA_FILE_PATH = "metadata.csv";

    /**
     * Executed once when the application starts.
     */
    public void init() {
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
        Vector<LinkedHashMap<String, String>> tableRows = new Vector<>();

        File metadataFile = new File(METADATA_FILE_PATH);
        if (metadataFile.exists()) {
            try {
                tableRows = csvLoader.load(METADATA_FILE_PATH);
            } catch (IOException e) {
                throw new DBAppException("Failed to load metadata file");
            }
        }

        Set<String> columnNames = columnTypes.keySet();

        for (String columnName : columnNames) {
            LinkedHashMap<String, String> row = new LinkedHashMap<>();
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

        try {
            csvLoader.save(METADATA_FILE_PATH, tableRows);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        // Load metadata for table


        // Validate inputs (table exists, column names exist, column types match, etc.)


        /*
        Find the page to insert into.
        Data must be sorted by clustering key (primary key).
        Maybe store the clustering key of the first row in each page and perform binary search to find the page.
        Or just do a linear search on the pages.
        */

        /*
        Once you find a page to insert into, perform a binary search to find where to insert in the page.
        If the page is full then move the last row to the next page.
        If the next page is also full repeat until you find a page that is not full,
        or you reach the last page and it is full. Only in this case, create a new page.
        */

        Table table = Table.load(tableName);
        table.insert(values);
        table.save();
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

        Table table = Table.load(tableName);
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
        // Load metadata for table

        // Validate inputs (table exists, column names exist, column types match, etc.)

        /*
        Since the search values might not include the clustering key, we must load every page, and
        filter out the rows that match the search values (remove them from the page), and then save
        the page again.
        If one page became empty, then delete it entirely.
        Also maybe if the clustering key is included in the search values, then we can use it to
        only load the pages that contain the rows that match the search values,
        I don't think this is required though
        */
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
}
