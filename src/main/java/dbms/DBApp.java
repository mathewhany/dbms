package dbms;

import dbms.config.Config;
import dbms.config.ConfigManager;
import dbms.config.PropertiesConfigManager;
import dbms.datatype.*;
import dbms.indicies.IndexFactory;
import dbms.indicies.IndexManager;
import dbms.indicies.OctreeFactory;
import dbms.indicies.SerializedIndexManager;
import dbms.iterators.RowToHashtableIterator;
import dbms.pages.*;
import dbms.parser.SQLLexer;
import dbms.parser.SQLParser;
import dbms.parser.SQLVisitor;
import dbms.tables.CsvTableManager;
import dbms.tables.Table;
import dbms.tables.TableManager;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class DBApp {
    private static final String METADATA_FILE_PATH = "src/main/resources/metadata.csv";
    private static final String CONFIG_FILE_PATH = "src/main/resources/DBApp.config";
    private static final String TABLES_DIR = "src/main/resources/Data/tables/";
    private static final String PAGES_DIR = "src/main/resources/Data/pages/";
    private static final String INDEX_DIR = "src/main/resources/Data/indices/";

    private Config config;
    private TableManager tableManager;
    private PageManager pageManager;
    private PagesIndexManager pagesIndexManager;
    private Hashtable<String, DataType> dataTypes;
    private IndexManager indexManager;

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
            Hashtable<String, IndexFactory> indexFactory = new Hashtable<>();
            indexFactory.put("Octree", new OctreeFactory(config));
            indexManager = new SerializedIndexManager(INDEX_DIR, indexFactory);
        } catch (DBAppException e) {
            System.out.println("Failed to load index manager");
        }

        try {
            tableManager = new CsvTableManager(
                METADATA_FILE_PATH,
                config,
                pageManager,
                dataTypes,
                indexManager
            );

            if (DeveloperFlags.PRELOAD_INDICES) {
                tableManager.preloadIndices();
            }
        } catch (DBAppException e) {
            System.out.println("Failed to load table manager");
            e.printStackTrace();
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
            dataTypes,
            indexManager,
            new Hashtable<>(),
            new Hashtable<>()
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
        Table table = loadTable(tableName);
        String indexName = String.join("", columnNames) + "Index";
        table.createIndex(indexName, "Octree", columnNames);
        tableManager.saveTable(table);

        table = null;
        System.gc();
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
     * @param sqlTerms  An array of SQLTerm objects. Each SQLTerm object is a condition (table, column, value, operator).
     * @param operators An array of strings. Each string is an operator (AND, OR).
     *                  The array will be of size 0 in case there is only one condition.
     * @return An iterator that iterates over the rows that match the conditions.
     * @throws DBAppException
     */
    public Iterator selectFromTable(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        if (sqlTerms.length == 0) {
            throw new DBAppException("No conditions were passed");
        }

        if (operators.length != sqlTerms.length - 1) {
            throw new DBAppException(
                "Number of operators must be one less than the number of conditions");
        }

        String tableName = sqlTerms[0]._strTableName;
        Table table = loadTable(tableName);
        Iterator<Row> iterator = table.select(sqlTerms, operators);

        return new RowToHashtableIterator(iterator);
    }

    public Table loadTable(String tableName) throws DBAppException {
        Table table = tableManager.loadTable(tableName);
        Vector<PageIndexItem> pagesIndex = pagesIndexManager.loadPagesIndex(table);
        table.setPagesIndex(pagesIndex);

        return table;
    }


    // below method returns Iterator with result set if passed
    // strbufSQL is a select, otherwise returns null.
    public Iterator parseSQL(StringBuffer sqlBuffer) throws DBAppException {
        // OK I know this is not the best way to do this but I'm running out of time

        SQLLexer lexer = new SQLLexer(CharStreams.fromString(sqlBuffer.toString()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
//        parser.removeErrorListeners();
        parser.addErrorListener(new AntlrErrorHandler());

        try {
            SQLParser.StartContext context = parser.start();
            if (context.select() != null) {
                String tableName = context.select().table_name().getText();
                SQLTerm[] sqlTerms = new SQLTerm[context.select().conditions().condition().size()];
                String[] operators = new String[sqlTerms.length - 1];

                for (int i = 0; i < sqlTerms.length; i++) {
                    SQLParser.ConditionContext condition =
                        context.select().conditions().condition(i);
                    String columnName = condition.column().getText();
                    String operator = condition.operator().getText();
                    SQLParser.ValueContext value = condition.value();
                    Object valueObject = null;
                    if (value.DATE() != null) {
                        valueObject = new Date(value.getText());
                    } else if (value.INT() != null) {
                        valueObject = Integer.parseInt(value.getText());
                    } else if (value.FLOAT() != null) {
                        valueObject = Double.parseDouble(value.getText());
                    } else if (value.STRING() != null) {
                        valueObject = value.getText();
                    }

                    SQLTerm sqlTerm = new SQLTerm();
                    sqlTerm._strTableName = tableName;
                    sqlTerm._strColumnName = columnName;
                    sqlTerm._strOperator = operator;
                    sqlTerm._objValue = valueObject;

                    sqlTerms[i] = sqlTerm;

                    if (i < operators.length) {
                        operators[i] = context.select().conditions().compound(i).getText();
                    }
                }

                return selectFromTable(sqlTerms, operators);
            } else if (context.insert() != null) {
                String tableName = context.insert().table_name().getText();
                Hashtable<String, Object> values = new Hashtable<>();

                if (context.insert().column().size() != context.insert().value().size()) {
                    throw new DBAppException("Number of columns and values don't match");
                }

                for (int i = 0; i < context.insert().column().size(); i++) {
                    String columnName = context.insert().column(i).getText();
                    SQLParser.ValueContext value = context.insert().value(i);

                    Object valueObject = null;
                    if (value.DATE() != null) {
                        valueObject = new Date(value.getText());
                    } else if (value.INT() != null) {
                        valueObject = Integer.parseInt(value.getText());
                    } else if (value.FLOAT() != null) {
                        valueObject = Double.parseDouble(value.getText());
                    } else if (value.STRING() != null) {
                        valueObject = value.getText();
                    }

                    values.put(columnName, valueObject);
                }

                insertIntoTable(tableName, values);
            } else if (context.update() != null) {
                String tableName = context.update().table_name().getText();
                String clusteringKeyValue = context.update().clustering_key_value().getText();
                String clusteringKeyColumnName = context.update().clustering_key().getText();
                Hashtable<String, Object> values = new Hashtable<>();

                for (int i = 0; i < context.update().assignments().assignment().size(); i++) {
                    String columnName =
                        context.update().assignments().assignment(i).column().getText();
                    SQLParser.ValueContext value =
                        context.update().assignments().assignment(i).value();

                    Object valueObject = null;
                    if (value.DATE() != null) {
                        valueObject = new Date(value.getText());
                    } else if (value.INT() != null) {
                        valueObject = Integer.parseInt(value.getText());
                    } else if (value.FLOAT() != null) {
                        valueObject = Double.parseDouble(value.getText());
                    } else if (value.STRING() != null) {
                        valueObject = value.getText();
                    }

                    values.put(columnName, valueObject);
                }

                Table table = loadTable(tableName);
                if (!table.getClusteringKeyColumn().equals(clusteringKeyColumnName)) {
                    throw new DBAppException("Can only update by clustering key");
                }
                updateTable(tableName, clusteringKeyValue, values);
            } else if (context.delete() != null) {
                String tableName = context.delete().table_name().getText();
                Hashtable<String, Object> searchValues = new Hashtable<>();

                if (context.delete().assignments() != null) {

                    for (SQLParser.AssignmentContext assignmentContext : context.delete()
                                                                                .assignments()
                                                                                .assignment()) {
                        String columnName = assignmentContext.column().getText();
                        SQLParser.ValueContext value = assignmentContext.value();

                        Object valueObject = null;
                        if (value.DATE() != null) {
                            valueObject = new Date(value.getText());
                        } else if (value.INT() != null) {
                            valueObject = Integer.parseInt(value.getText());
                        } else if (value.FLOAT() != null) {
                            valueObject = Double.parseDouble(value.getText());
                        } else if (value.STRING() != null) {
                            valueObject = value.getText();
                        }

                        searchValues.put(columnName, valueObject);
                    }
                }

                deleteFromTable(tableName, searchValues);
            } else if (context.create_table() != null) {
                String tableName = context.create_table().table_name().getText();
                String clusteringKey = null;

                Hashtable<String, String> colNameType = new Hashtable<>();
                Hashtable<String, String> colNameMin = new Hashtable<>();
                Hashtable<String, String> colNameMax = new Hashtable<>();

                for (int i = 0; i < context.create_table().create_table_column().size(); i++) {
                    SQLParser.Create_table_columnContext columnContext =
                        context.create_table().create_table_column(i);
                    String columnName = columnContext.column().getText();
                    if (clusteringKey != null && columnContext.PRIMARY_KEY() != null) {
                        throw new DBAppException("Only one primary key is allowed");
                    } else if (columnContext.PRIMARY_KEY() != null) {
                        clusteringKey = columnName;
                    }

                    String columnType = "";
                    String columnMin = "";
                    String columnMax = "";
                    if (columnContext.type().DATE_TYPE() != null) {
                        columnType = "java.util.Date";
                        columnMin = "0001-01-01";
                        columnMax = "9999-12-31";
                    } else if (columnContext.type().INT_TYPE() != null) {
                        columnType = "java.lang.Integer";
                        columnMin = "-2147483648";
                        columnMax = "2147483647";
                    } else if (columnContext.type().FLOAT_TYPE() != null ||
                               columnContext.type().DOUBLE_TYPE() != null) {
                        columnType = "java.lang.Double";
                        columnMin = "-1.7976931348623157E308";
                        columnMax = "1.7976931348623157E308";
                    } else if (columnContext.type().VARCHAR() != null ||
                               columnContext.type().TEXT_TYPE() != null ||
                               columnContext.type().STRING_TYPE() != null) {
                        columnType = "java.lang.String";
                        columnMin = "a";
                        columnMax = "ZZZZZZZZZZZZZZZ";
                    } else {
                        throw new DBAppException("Invalid column type");
                    }

                    colNameType.put(columnName, columnType);
                    colNameMin.put(columnName, columnMin);
                    colNameMax.put(columnName, columnMax);
                }

                if (context.create_table().clustering_key() != null) {
                    if (clusteringKey == null) {
                        clusteringKey = context.create_table().clustering_key().getText();
                    } else {
                        throw new DBAppException("Only one primary key is allowed");
                    }
                }

                if (clusteringKey == null) {
                    throw new DBAppException("No primary key was specified");
                }

                createTable(tableName, clusteringKey, colNameType, colNameMin, colNameMax);
            } else if (context.create_index() != null) {
                String tableName = context.create_index().table_name().getText();
                String[] columns = new String[context.create_index().column().size()];

                for (int i = 0; i < context.create_index().column().size(); i++) {
                    columns[i] = context.create_index().column(i).getText();
                }

                createIndex(tableName, columns);
            } else {
                throw new DBAppException("Invalid SQL query");
            }
        } catch (
            AntlrException e) {
            throw new DBAppException("Invalid SQL query");
        }

        return null;
    }
}
