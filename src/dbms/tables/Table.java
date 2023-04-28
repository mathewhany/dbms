package dbms.tables;

import dbms.DBAppException;
import dbms.pages.PageIndexItem;
import dbms.pages.Row;
import dbms.util.Util;
import dbms.config.Config;
import dbms.datatype.DataType;
import dbms.pages.Page;
import dbms.pages.PageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class Table {
    private final String tableName;
    private final String clusteringKeyColumn;
    private final Hashtable<String, String> columnTypes = new Hashtable<>();
    private final Hashtable<String, Object> columnMin = new Hashtable<>();
    private final Hashtable<String, Object> columnMax = new Hashtable<>();
    private final Config config;
    private final PageManager pageManager;
    private final Hashtable<String, DataType> dataTypes;

    private Vector<PageIndexItem> pagesIndex = new Vector<>();

    public Table(
        String tableName,
        String clusteringKeyColumn,
        Hashtable<String, String> columnTypes,
        Hashtable<String, String> columnMin,
        Hashtable<String, String> columnMax,
        Config config,
        PageManager pageManager,
        Hashtable<String, DataType> dataTypes
    ) throws DBAppException {
        if (!columnTypes.keySet().equals(columnMin.keySet()) ||
            !columnTypes.keySet().equals(columnMax.keySet())) {
            throw new DBAppException("Column types and column min/max don't match");
        }

        this.tableName = tableName;
        this.clusteringKeyColumn = clusteringKeyColumn;
        this.config = config;
        this.pageManager = pageManager;
        this.dataTypes = dataTypes;

        Set<String> columnNames = columnTypes.keySet();
        for (String columnName : columnNames) {
            if (columnName.isEmpty()) {
                throw new DBAppException("Column name cannot be empty");
            }

            if (!dataTypes.containsKey(columnTypes.get(columnName))) {
                throw new DBAppException("Invalid column type " + columnTypes.get(columnName));
            }

            DataType dataType = dataTypes.get(columnTypes.get(columnName));
            Object min = dataType.parse(columnMin.get(columnName));
            Object max = dataType.parse(columnMax.get(columnName));

            if (dataType.compare(min, max) > 0) {
                throw new DBAppException("Column min is greater than column max");
            }

            this.columnTypes.put(columnName, columnTypes.get(columnName));
            this.columnMin.put(columnName, min);
            this.columnMax.put(columnName, max);
        }

        if (!this.columnTypes.containsKey(clusteringKeyColumn)) {
            throw new DBAppException("Clustering key column not found");
        }

        if (tableName.isEmpty()) {
            throw new DBAppException("Table name cannot be empty");
        }
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

        // Binary search index of dbms.pages
        int searchRes = Util.binarySearch(
            pagesIndex,
            currentRow.getClusteringKeyValue(),
            (pageIndexItem) -> pageIndexItem.pageMin,
            dataTypes.get(columnTypes.get(clusteringKeyColumn))
        );
        int index = Math.max(searchRes, 0);

        while (true) {
            if (index >= pagesIndex.size()) {
                Page newPage =
                    new Page(
                        config.getMaximumRowsCountInTablePage(),
                        columnTypes,
                        dataTypes,
                        clusteringKeyColumn
                    );
                newPage.setFileName(generatePageName(newPage));
                newPage.insert(currentRow);
                pageManager.savePage(newPage);

                PageIndexItem newPageIndexItem =
                    new PageIndexItem(currentRow.getClusteringKeyValue(), newPage.getFileName());
                pagesIndex.add(newPageIndexItem);
                newPage = null;
                System.gc();
                break;
            }

            PageIndexItem pageIndex = pagesIndex.get(index);
            Page page = pageManager.loadPage(pageIndex.fileName);
            Row returnedRow = page.insert(currentRow);
            pageManager.savePage(page);

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
        DataType clusteringKeyDataType = dataTypes.get(clusteringKeyType);
        Object clusterKeyValueObj = clusteringKeyDataType.parse(clusteringKeyValue);

        int searchRes =
            Util.binarySearch(
                pagesIndex,
                clusterKeyValueObj,
                (pageIndex) -> pageIndex.pageMin,
                clusteringKeyDataType
            );

        PageIndexItem pageIndex = pagesIndex.get(searchRes);
        Page page = pageManager.loadPage(pageIndex.fileName);
        page.update(clusterKeyValueObj, newValues);
        pageManager.savePage(page);

    }

    public boolean validate(Hashtable<String, Object> row) {
        for (String columnName : row.keySet()) {
            Object value = row.get(columnName);

            if (!columnTypes.containsKey(columnName)) {
                return false;
            }

            String expectedType = columnTypes.get(columnName);
            DataType dataType = dataTypes.get(expectedType);

            String actualType = value.getClass().getName();

            if (!expectedType.equals(actualType)) {
                return false;
            }

            Object min = columnMin.get(columnName);
            Object max = columnMax.get(columnName);

            if (dataType.compare(value, min) < 0 || dataType.compare(value, max) > 0) {
                return false;
            }
        }

        return true;
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
            Page page = pageManager.loadPage(pageIndexItem.fileName);
            page.deleteLinearSearch(searchValues);

            if (deletePageOrSave(i, page)) {
                i--;
            }
        }
    }

    private void deleteUsingClusteringKey(Hashtable<String, Object> searchValues) throws
        DBAppException {
        int pageIndex = Util.binarySearch(
            pagesIndex,
            searchValues.get(clusteringKeyColumn),
            (pageIndexItem) -> pageIndexItem.pageMin,
            dataTypes.get(columnTypes.get(clusteringKeyColumn))
        );

        if (pageIndex < 0) {
            return;
        }

        PageIndexItem pageIndexItem = pagesIndex.get(pageIndex);
        Page page = pageManager.loadPage(pageIndexItem.fileName);
        page.deleteBinarySearch(searchValues);

        deletePageOrSave(pageIndex, page);
    }

    private boolean deletePageOrSave(int idx, Page page) throws DBAppException {
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
            pageManager.savePage(page);
        }

        return false;
    }

    public String getTableName() {
        return tableName;
    }

    public String getClusteringKeyColumn() {
        return clusteringKeyColumn;
    }

    public Hashtable<String, String> getColumnTypes() {
        return columnTypes;
    }

    public Hashtable<String, Object> getColumnMin() {
        return columnMin;
    }

    public Hashtable<String, Object> getColumnMax() {
        return columnMax;
    }
}
