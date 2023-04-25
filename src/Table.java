import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Table implements Serializable {
    private String tableName;
    private String clusteringKeyColumn;
    private Hashtable<String, String> columnTypes;
    private Hashtable<String, String> columnMin;
    private Hashtable<String, String> columnMax;

    private Vector<PageIndexItem> pagesIndex = new Vector<>();

    public Table(
        String tableName,
        String clusteringKeyColumn,
        Hashtable<String, String> columnTypes,
        Hashtable<String, String> columnMin,
        Hashtable<String, String> columnMax
    ) {
        this.tableName = tableName;
        this.clusteringKeyColumn = clusteringKeyColumn;
        this.columnTypes = columnTypes;
        this.columnMin = columnMin;
        this.columnMax = columnMax;
    }

    public String generatePageName(Page page) {
        // Generate random hash for page name
        String name = UUID.randomUUID().toString();

        return name + ".ser";
    }

    public void insert(Hashtable<String, Object> row) throws DBAppException {
        if (!validate(row) || row.keySet().size() != columnTypes.keySet().size()) {
            throw new DBAppException("Invalid row");
        }

        Row currentRow = new Row(row, clusteringKeyColumn);

        // Binary search index of pages
        int searchRes = Util.binarySearch(
            pagesIndex,
            new PageIndexItem(currentRow.getClusteringKeyValue(), "")
        );
        int index = Math.max(searchRes, 0);

        while (true) {
            if (index >= pagesIndex.size()) {
                Page newPage = new Page();
                newPage.setFileName(generatePageName(newPage));
                newPage.setClusteringKeyColumnName(clusteringKeyColumn);
                newPage.insert(currentRow);
                newPage.save();

                PageIndexItem newPageIndexItem =
                    new PageIndexItem(currentRow.getClusteringKeyValue(), newPage.getFileName());
                pagesIndex.add(newPageIndexItem);
                break;
            }

            PageIndexItem pageIndex = pagesIndex.get(index);
            Page page = Page.load(pageIndex.fileName);
            Row returnedRow = page.insert(currentRow);
            page.save();

            pageIndex.pageMin = page.getRows().get(0).getClusteringKeyValue();

            if (returnedRow != null) {
                currentRow = returnedRow;
                index++;
            } else {
                break;
            }
        }
    }

    public void update(String clusteringKeyValue, Hashtable<String, Object> newValues) throws
        DBAppException {
        if (!validate(newValues) || newValues.containsKey(clusteringKeyColumn)) {
            throw new DBAppException("Invalid values");
        }

        String clusteringKeyType = columnTypes.get(clusteringKeyColumn);
        Object clusterKeyValueObj;
        switch (clusteringKeyType) {
            case "java.lang.Integer":
                clusterKeyValueObj = Integer.parseInt(clusteringKeyValue);
                break;
            case "java.lang.Double":
                clusterKeyValueObj = Double.parseDouble(clusteringKeyValue);
                break;
            case "java.util.Date":
                try {
                    clusterKeyValueObj = new SimpleDateFormat("yyyy-MM-dd").parse(clusteringKeyValue);
                } catch (ParseException e) {
                    throw new DBAppException("Invalid date format");
                }
                break;
            default:
                clusterKeyValueObj = clusteringKeyValue;
        }

        int searchRes = Util.binarySearch(
            pagesIndex,
            new PageIndexItem(clusterKeyValueObj, "")
        );

        PageIndexItem pageIndex = pagesIndex.get(searchRes);
        Page page = Page.load(pageIndex.fileName);
        page.update(clusterKeyValueObj, newValues);
        page.save();

    }

    public static Table load(String tableName) throws DBAppException {
        CsvLoader csvLoader = new CsvLoader();

        try {
            Vector<LinkedHashMap<String, String>> metadata = csvLoader.load("metadata.csv");

            Vector<LinkedHashMap<String, String>> tableColumns = new Vector<>();

            for (LinkedHashMap<String, String> row : metadata) {
                if (row.get("Table Name").equals(tableName)) {
                    tableColumns.add(row);
                }
            }


            Hashtable<String, String> columnTypes = new Hashtable<>();
            Hashtable<String, String> columnMin = new Hashtable<>();
            Hashtable<String, String> columnMax = new Hashtable<>();
            String clusteringKey = "";

            for (LinkedHashMap<String, String> tableColumn : tableColumns) {
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
                columnMax
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
                throw new DBAppException("Error loading table: " + tableName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() throws DBAppException {
        try {
            String indexFilePath = tableName + ".ser";
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(indexFilePath));

            ois.writeObject(pagesIndex);
        } catch (IOException e) {
            throw new DBAppException("Error while saving table index" + tableName);
        }
    }

    public boolean validate(Hashtable<String, Object> row) {
        for (String column : row.keySet()) {
            Comparable<?> value = (Comparable<?>) row.get(column);

            if (!columnTypes.containsKey(column)) {
                return false;
            }

            String expectedType = columnTypes.get(column);
            String actualType = value.getClass().getName();
            if (!expectedType.equals(actualType)) {
                return false;
            }

            if (!isBetween(expectedType, columnMin.get(column), columnMax.get(column), value)) {
                return false;
            }
        }

        return true;
    }

    public boolean isBetween(String type, String min, String max, Object value) {
        switch (type) {
            case "java.lang.Integer":
                int minValue = Integer.parseInt(min);
                int maxValue = Integer.parseInt(max);
                int intValue = (int) value;
                return minValue <= intValue && intValue <= maxValue;
            case "java.lang.Double":
                double minDoubleValue = Double.parseDouble(min);
                double maxDoubleValue = Double.parseDouble(max);
                double doubleValue = (double) value;
                return minDoubleValue <= doubleValue && doubleValue <= maxDoubleValue;
            case "java.lang.String":
                String stringValue = (String) value;
                return min.compareTo(stringValue) <= 0 && stringValue.compareTo(max) <= 0;
            case "java.util.Date":
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date minDate = dateFormat.parse(min);
                    Date maxDate = dateFormat.parse(max);
                    Date dateValue = (Date) value;
                    return minDate.compareTo(dateValue) <= 0 && dateValue.compareTo(maxDate) <= 0;
                } catch (ParseException e) {
                    return false;
                }
        }

        return false;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getClusteringKeyColumn() {
        return clusteringKeyColumn;
    }

    public void setClusteringKeyColumn(String clusteringKeyColumn) {
        this.clusteringKeyColumn = clusteringKeyColumn;
    }

    public Hashtable<String, String> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(Hashtable<String, String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Hashtable<String, String> getColumnMin() {
        return columnMin;
    }

    public void setColumnMin(Hashtable<String, String> columnMin) {
        this.columnMin = columnMin;
    }

    public Hashtable<String, String> getColumnMax() {
        return columnMax;
    }

    public void setColumnMax(Hashtable<String, String> columnMax) {
        this.columnMax = columnMax;
    }

    public Vector<PageIndexItem> getPagesIndex() {
        return pagesIndex;
    }

    public void setPagesIndex(Vector<PageIndexItem> pagesIndex) {
        this.pagesIndex = pagesIndex;
    }
}
