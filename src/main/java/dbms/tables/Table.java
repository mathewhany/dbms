package dbms.tables;

import dbms.*;
import dbms.indicies.IndexManager;
import dbms.pages.PageIndexItem;
import dbms.pages.Row;
import dbms.util.Util;
import dbms.config.Config;
import dbms.datatype.DataType;
import dbms.pages.Page;
import dbms.pages.PageManager;

import java.util.Hashtable;
import java.util.Set;
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
    private final IndexManager indexManager;
    private final Hashtable<String, String> indexNames;
    private final Hashtable<String, String> indexTypes;

    private Vector<PageIndexItem> pagesIndex = new Vector<>();

    public Table(
        String tableName,
        String clusteringKeyColumn,
        Hashtable<String, String> columnTypes,
        Hashtable<String, String> columnMin,
        Hashtable<String, String> columnMax,
        Config config,
        PageManager pageManager,
        Hashtable<String, DataType> dataTypes,
        IndexManager indexManager,
        Hashtable<String, String> indexNames,
        Hashtable<String, String> indexTypes
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
        this.indexManager = indexManager;

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

        this.indexNames = indexNames;
        this.indexTypes = indexTypes;
    }

    public String generatePageId() {
        if (pagesIndex.size() == 0) {
            return tableName + "_0";
        }

        PageIndexItem lastPageIndexItem = pagesIndex.lastElement();
        int lastPageIndex = Integer.parseInt(lastPageIndexItem.pageId.split("_")[1]);

        return tableName + "_" + (lastPageIndex + 1);
    }

    public void insert(Hashtable<String, Object> row) throws DBAppException {
        validate(row);
        if (!row.containsKey(clusteringKeyColumn)) {
            throw new DBAppException("Cannot insert row without clustering key");
        }

        // https://piazza.com/class/lel8rsvwc4e7j6/post/32
//        if (row.size() != columnTypes.size()) {
//            throw new DBAppException("Row size doesn't match table size");
//        }

        Row currentRow = new Row(row, clusteringKeyColumn);
        Hashtable<String, Index> indicies = new Hashtable<>();
        for (String indexName : indexNames.value()) {
            if (!indicies.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
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
                Page newPage = new Page(
                    config.getMaximumRowsCountInTablePage(),
                    columnTypes,
                    dataTypes,
                    clusteringKeyColumn
                );
                newPage.setPageId(generatePageId());
                newPage.insert(currentRow);
                pageManager.savePage(newPage);

                PageIndexItem newPageIndexItem =
                    new PageIndexItem(currentRow.getClusteringKeyValue(), newPage.getPageId());
                pagesIndex.add(newPageIndexItem);

                newPage = null;
                System.gc();
                break;
            }

            PageIndexItem pageIndex = pagesIndex.get(index);
            Page page = pageManager.loadPage(pageIndex.pageId);
            Row returnedRow = page.insert(currentRow);
            pageManager.savePage(page);
            pageIndex.pageMin = page.getRows().get(0).getClusteringKeyValue();
            page = null;
            System.gc();

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
        validate(newValues);
        if (newValues.containsKey(clusteringKeyColumn)) {
            throw new DBAppException("Cannot update clustering key column");
        }

        String clusteringKeyType = columnTypes.get(clusteringKeyColumn);
        DataType clusteringKeyDataType = dataTypes.get(clusteringKeyType);
        Object clusterKeyValueObj = clusteringKeyDataType.parse(clusteringKeyValue);

        int searchRes = Util.binarySearch(
            pagesIndex,
            clusterKeyValueObj,
            (pageIndex) -> pageIndex.pageMin,
            clusteringKeyDataType
        );

        if (searchRes < 0) {
            throw new DBAppException(
                "Row with clustering key " + clusteringKeyValue + " not found");
        }

        PageIndexItem pageIndex = pagesIndex.get(searchRes);
        Page page = pageManager.loadPage(pageIndex.pageId);
        page.update(clusterKeyValueObj, newValues);
        pageManager.savePage(page);
        page = null;
        System.gc();
    }

    public void validate(Hashtable<String, Object> row) throws DBAppException {
        for (String columnName : row.keySet()) {
            Object value = row.get(columnName);

            if (!columnTypes.containsKey(columnName)) {
                throw new DBAppException("Column " + columnName + " not found");
            }

            String expectedType = columnTypes.get(columnName);
            DataType dataType = dataTypes.get(expectedType);

            String actualType = value.getClass().getName();

            if (!expectedType.equals(actualType)) {
                throw new DBAppException(
                    "Expected type " + expectedType + " for column '" + columnName +
                    "' but found " + actualType);
            }

            Object min = columnMin.get(columnName);
            Object max = columnMax.get(columnName);

            if (dataType.compare(value, min) < 0 || dataType.compare(value, max) > 0) {
                throw new DBAppException(
                    "Value for column '" + columnName + "' must be between " + min + " and " + max);
            }
        }
    }

    public Vector<PageIndexItem> getPagesIndex() {
        return pagesIndex;
    }

    public void setPagesIndex(Vector<PageIndexItem> pagesIndex) {
        this.pagesIndex = pagesIndex;
    }

    public void delete(Hashtable<String, Object> searchValues) throws DBAppException {
        Hashtable<String, Index> indicies = new Hashtable<>();
        for (String indexName : indexNames.value()) {
            if (!indicies.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
        validate(searchValues);

        Hashtable<String, int> matching = new Hashtable<>();
        String maxIndex;
        for (String searchIndex : searchValues.keySet()) {
            if (indices.containsKey(searchIndex)) {
                matching.put(searchIndex, matching.get(searchIndex, 0) + 1);

                if (maxIndex == null || matching.get(searchIndex) > matching.get(maxIndex)) {
                    maxIndex = searchIndex
                }
            }
        }


        if (maxIndex != null) {
            deleteUsingIndex(searchValues, indicies.get(maxIndex));
        } else if (searchValues.containsKey(clusteringKeyColumn)) {
            deleteUsingClusteringKey(searchValues);
        } else {
            linearDelete(searchValues);
        }
    }

    private void deleteUsingIndex(Hashtable<String, Object> searchValues, Index maxIndex) throws DBAppException {
        Hashtable<String, Index> indicies = new Hashtable<>();
        for (String indexName : indexNames.value()) {
            if (!indicies.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
        Vector<row> allRows = new Vector<>();
        Hashtable<String, Object> deleteValues = new Hashtable<>();
        for (String column : indexNames.keySet())
            if (indexNames.get(column).equals(maxIndex.getName()))
                deleteValues.put(column, searchValues.get(column));
        Iterator<String> deletePages = maxIndex.findExact(deleteValues);
        while (deletePages.hasNext()) {
            Page page = pageManager.loadPage(deletePages.Next());
            if (searchValues.containsKey(clusteringKeyColumn))
                Vector<row> reqRows = page.deleteUsingClusteringKey(searchValues);
            else
                Vector<row> reqRows = page.linearDelete(searchValues);
            allRows.addAll(reqRows);
            for (int i = 0;i<pagesIndex.length();i++)
                if (pagesIndex.get(i).pageId.equals(page.get(pageId))) {
                    deletePageOrSave(i, page);
                    page = null;
                }
        }
        for (Index index : indices.values())
                for (row r : allRows)
                    index.delete(r);
        System.gc();
    }

    private void linearDelete(Hashtable<String, Object> searchValues) throws DBAppException {
        Hashtable<String, Index> indicies = new Hashtable<>();
        for (String indexName : indexNames.value()) {
            if (!indicies.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
        for (int i = 0; i < pagesIndex.size(); i++) {
            PageIndexItem pageIndexItem = pagesIndex.get(i);
            Page page = pageManager.loadPage(pageIndexItem.pageId);
            Vector<row> reqRows = page.deleteLinearSearch(searchValues);
            for (Index index : indices.values())
                for (row r : reqRows)
                    index.delete(r);

            if (deletePageOrSave(i, page)) {
                i--;
            }

            page = null;
            System.gc();
        }
    }

    private void deleteUsingClusteringKey(Hashtable<String, Object> searchValues) throws
        DBAppException {
        Hashtable<String, Index> indicies = new Hashtable<>();
        for (String indexName : indexNames.value()) {
            if (!indicies.contains(indexName)) {
                  indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
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
        Page page = pageManager.loadPage(pageIndexItem.pageId);
        Vector<row> reqRows = page.deleteBinarySearch(searchValues);
        for (Index index : indices.values())
            for (row r : reqRows)
                index.delete(r);

        deletePageOrSave(pageIndex, page);

        page = null;
        System.gc();
    }

    private boolean deletePageOrSave(int idx, Page page) throws DBAppException {
        PageIndexItem pageIndexItem = pagesIndex.get(idx);

        if (page.getRows().size() == 0) {
            pagesIndex.remove(idx);
            pageManager.deletePage(pageIndexItem.pageId);
            return true;
        } else {
            pageIndexItem.pageMin = page.getRows().get(0).get(clusteringKeyColumn);
            pageManager.savePage(page);
        }

        return false;
    }

    public Expression sqlTermToExpression(SQLTerm sqlTerm) throws DBAppException {
        String columnName = sqlTerm._strColumnName;
        String columnType = columnTypes.get(columnName);
        DataType dataType = dataTypes.get(columnType);
        Object value = sqlTerm._objValue;

        if (sqlTerm._strOperator.equals("=")) {
            return new Range(sqlTerm._strColumnName, value, value, dataType);
        } else if (sqlTerm._strOperator.equals(">")) {
            return new Range(sqlTerm._strColumnName, value, columnMax.get(columnName), dataType, false, true);
        } else if (sqlTerm._strOperator.equals(">=")) {
            return new Range(sqlTerm._strColumnName, value, columnMax.get(columnName), dataType);
        } else if (sqlTerm._strOperator.equals("<")) {
            return new Range(sqlTerm._strColumnName, columnMin.get(columnName), value, dataType, true, false);
        } else if (sqlTerm._strOperator.equals("<=")) {
            return new Range(sqlTerm._strColumnName, columnMin.get(columnName), value, dataType);
        } else if (sqlTerm._strOperator.equals("!=")) {
            return new BinaryExpression(
                new Range(sqlTerm._strColumnName, columnMin.get(columnName), value, dataType, true, false),
                new Range(sqlTerm._strColumnName, value, columnMax.get(columnName), dataType, false, true),
                "or"
            );
        } else {
            throw new DBAppException("Invalid operator " + sqlTerm._strOperator);
        }
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
