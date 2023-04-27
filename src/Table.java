import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Table {
    private final String tableName;
    private final String clusteringKeyColumn;
    private final Hashtable<String, String> columnTypes;
    private final Hashtable<String, String> columnMin;
    private final Hashtable<String, String> columnMax;
    private final Config config;

    private Vector<PageIndexItem> pagesIndex = new Vector<>();

    public Table(
        String tableName,
        String clusteringKeyColumn,
        Hashtable<String, String> columnTypes,
        Hashtable<String, String> columnMin,
        Hashtable<String, String> columnMax,
        Config config
    ) {
        this.tableName = tableName;
        this.clusteringKeyColumn = clusteringKeyColumn;
        this.columnTypes = columnTypes;
        this.columnMin = columnMin;
        this.columnMax = columnMax;
        this.config = config;
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
                Page newPage = new Page(config.getMaximumRowsCountInTablePage());
                newPage.setFileName(generatePageName(newPage));
                newPage.setClusteringKeyColumnName(clusteringKeyColumn);
                newPage.insert(currentRow);
                newPage.save();

                PageIndexItem newPageIndexItem =
                    new PageIndexItem(currentRow.getClusteringKeyValue(), newPage.getFileName());
                pagesIndex.add(newPageIndexItem);
                newPage = null;
                System.gc();
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

        System.gc();
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

    public Vector<PageIndexItem> getPagesIndex() {
        return pagesIndex;
    }

    public void setPagesIndex(Vector<PageIndexItem> pagesIndex) {
        this.pagesIndex = pagesIndex;
    }

    public void delete(Hashtable<String, Object> searchValues) throws DBAppException {
        if (!validate(searchValues)) {
            throw new DBAppException("Invalid search values");
        }

        if (searchValues.containsKey(clusteringKeyColumn)) {
            deleteUsingClusteringKey(searchValues);
        } else {
            linearDelete(searchValues);
        }
    }

    private void linearDelete(Hashtable<String, Object> searchValues) throws DBAppException {
        for (int i = 0; i < pagesIndex.size(); i++) {
            PageIndexItem pageIndexItem = pagesIndex.get(i);
            Page page = Page.load(pageIndexItem.fileName);
            page.deleteLinearSearch(searchValues);

            if (deletePageOrSave(i, page)) {
                i--;
            }
        }
    }

    private void deleteUsingClusteringKey(Hashtable<String, Object> searchValues) throws
        DBAppException {
        PageIndexItem searchPageIndex = new PageIndexItem(searchValues.get(clusteringKeyColumn), "");
        int pageIndex = Util.binarySearch(pagesIndex, searchPageIndex);

        if (pageIndex < 0) {
            return;
        }

        PageIndexItem pageIndexItem = pagesIndex.get(pageIndex);
        Page page = Page.load(pageIndexItem.fileName);
        page.deleteBinarySearch(searchValues);

        deletePageOrSave(pageIndex, page);
    }

    private boolean deletePageOrSave(int idx, Page page) throws
        DBAppException {
        PageIndexItem pageIndexItem = pagesIndex.get(idx);

        if (page.getRows().size() == 0) {
            pagesIndex.remove(idx);
            try {
                Files.deleteIfExists(Paths.get(pageIndexItem.fileName));
            } catch (IOException e) {
                throw new DBAppException("Error deleting page file: " + pageIndexItem.fileName);
            }
            return true;
        } else {
            pageIndexItem.pageMin = page.getRows().get(0).get(clusteringKeyColumn);
            page.save();
        }

        return false;
    }
}
