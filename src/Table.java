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
        Comparable rowKey = (Comparable) row.get(clusteringKeyColumn);

        if (pagesIndex.size() == 0) {
            // Create new page
            Page page = new Page();
            String fileName = generatePageName(page);
            page.setFileName(fileName);
            page.insert(row);
            page.save();

            PageIndexItem pageIndexItem = new PageIndexItem();
            pageIndexItem.pageMin = rowKey;
            pageIndexItem.fileName = fileName;
            pagesIndex.add(pageIndexItem);
            return;
        }

        // Binary search index of pages
        int left = 0;
        int right = pagesIndex.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            PageIndexItem midItem = pagesIndex.get(mid);
            Comparable midKey = (Comparable) midItem.pageMin;

            int comparison = midKey.compareTo(rowKey);
            if (comparison <= 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        int index = left - 1;

        PageIndexItem pageIndex = pagesIndex.get(index == -1 ? 0 : index);

        Page page = Page.load(pageIndex.fileName);
        Hashtable<String, Object> returnedRow = page.insert(row);
        page.save();

        // If returned row is not null, then recursively insert the returned row into table
        if (returnedRow != null) {
            if (index == pagesIndex.size() - 1) {
                Page newPage = new Page();
                newPage.setFileName(generatePageName(newPage));
                newPage.insert(returnedRow);
                newPage.save();

                PageIndexItem newPageIndexItem = new PageIndexItem();
                newPageIndexItem.pageMin = returnedRow.get(clusteringKeyColumn);
                newPageIndexItem.fileName = page.getFileName();
                pagesIndex.add(newPageIndexItem);
            } else {
                insert(returnedRow);
            }
        }
    }

    public static Table load(String fileName) throws DBAppException {
        CsvLoader csvLoader = new CsvLoader();

        try {
            Vector<LinkedHashMap<String, String>> metadata = csvLoader.load(fileName);

            for (LinkedHashMap<String, String> row : metadata) {
                String tableName = row.get("Table Name");
                String clusteringKeyColumn = row.get("ClusteringKeyColumn");
                Hashtable<String, String> columnTypes = new Hashtable<>();
                Hashtable<String, String> columnMin = new Hashtable<>();
                Hashtable<String, String> columnMax = new Hashtable<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));

            return (Table) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DBAppException("Error loading table: " + fileName);
        }
    }

    public void save() throws DBAppException {
        try {
            String fileName = tableName + ".ser";
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(this);
        } catch (IOException e) {
            throw new DBAppException("Error saving table: " + tableName);
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
}
