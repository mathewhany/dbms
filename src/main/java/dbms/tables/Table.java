package dbms.tables;

import dbms.*;
import dbms.indicies.Index;
import dbms.indicies.IndexManager;
import dbms.iterators.FilterIterator;
import dbms.iterators.RowToHashtableIterator;
import dbms.iterators.RowsIterator;
import dbms.iterators.TableIterator;
import dbms.pages.*;
import dbms.util.Util;
import dbms.config.Config;
import dbms.datatype.DataType;

import javax.xml.crypto.Data;
import java.util.*;

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
//        if (!row.containsKey(clusteringKeyColumn)) {
//            throw new DBAppException("Cannot insert row without clustering key");
//        }

        // https://piazza.com/class/lel8rsvwc4e7j6/post/32
        // https://piazza.com/class/lel8rsvwc4e7j6/post/257
        if (row.size() != columnTypes.size()) {
            throw new DBAppException("Cannot insert null values");
        }

        Row currentRow = new Row(row, clusteringKeyColumn);
        Hashtable<String, Index> indices = new Hashtable<>();
        for (String indexName : indexNames.values()) {
            if (!indices.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
        DataType clusteringKeyType = dataTypes.get(columnTypes.get(clusteringKeyColumn));
        // Binary search index of dbms.pages
        int searchRes = Util.binarySearch(
            pagesIndex,
            currentRow.getClusteringKeyValue(),
            (pageIndexItem) -> pageIndexItem.pageMin,
            clusteringKeyType
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
                for (Index tableIndex : indices.values()) {
                    tableIndex.insert(currentRow);
                }
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

            if (returnedRow == null || clusteringKeyType.compare(
                returnedRow.getClusteringKeyValue(),
                currentRow.getClusteringKeyValue()
            ) != 0) {
                for (Index tableIndex : indices.values()) {
                    tableIndex.insert(currentRow);
                }
            }

            if (returnedRow != null) {
                currentRow = returnedRow;
                index++;
            } else {
                break;
            }
        }

        for (Index tableIndex : indices.values()) {
            indexManager.saveIndex(tableIndex);
        }

        System.gc();
    }

    public void update(String clusteringKeyValue, Hashtable<String, Object> newValues) throws
        DBAppException {
        validate(newValues);
        if (newValues.containsKey(clusteringKeyColumn)) {
            throw new DBAppException("Cannot update clustering key column");
        }

        Hashtable<String, Index> indices = new Hashtable<>();
        for (String columnName : newValues.keySet()) {
            String indexName = indexNames.get(columnName);
            if (indexName != null && !indices.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
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
//            throw new DBAppException(
//                "Row with clustering key " + clusteringKeyValue + " not found");
            // https://piazza.com/class/lel8rsvwc4e7j6/post/158
            return;
        }

        PageIndexItem pageIndex = pagesIndex.get(searchRes);
        Page page = pageManager.loadPage(pageIndex.pageId);
        UpdatedRow updatedRow = page.update(clusterKeyValueObj, newValues);
        pageManager.savePage(page);

        page = null;
        System.gc();

        if (updatedRow == null) {
            return;
        }

        for (Index index : indices.values()) {
            index.delete(updatedRow.getOldRow());
            index.insert(updatedRow.getUpdatedRow());
            indexManager.saveIndex(index);
        }
    }

    public void validate(Hashtable<String, Object> row) throws DBAppException {
        for (String columnName : row.keySet()) {
            Object value = row.get(columnName);

            if (!columnTypes.containsKey(columnName)) {
                throw new DBAppException("Column " + columnName + " not found");
            }

            DataType dataType = getDataType(columnName);

            if (!dataType.isValidObject(value)) {
                throw new DBAppException(
                    "Expected type " + columnTypes.get(columnName) + " for column '" + columnName);
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
        validate(searchValues);

        if (searchValues.containsKey(clusteringKeyColumn)) {
            deleteUsingClusteringKey(searchValues);
        } else {
            linearDelete(searchValues);
        }
    }

    private void linearDelete(Hashtable<String, Object> searchValues) throws DBAppException {
        for (int i = 0; i < pagesIndex.size(); i++) {
            PageIndexItem pageIndexItem = pagesIndex.get(i);
            Page page = pageManager.loadPage(pageIndexItem.pageId);
            page.deleteLinearSearch(searchValues);

            if (deletePageOrSave(i, page)) {
                i--;
            }

            page = null;
            System.gc();
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
        Page page = pageManager.loadPage(pageIndexItem.pageId);
        page.deleteBinarySearch(searchValues);

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
            return new Range(
                sqlTerm._strColumnName,
                value,
                columnMax.get(columnName),
                dataType,
                false,
                true
            );
        } else if (sqlTerm._strOperator.equals(">=")) {
            return new Range(sqlTerm._strColumnName, value, columnMax.get(columnName), dataType);
        } else if (sqlTerm._strOperator.equals("<")) {
            return new Range(
                sqlTerm._strColumnName,
                columnMin.get(columnName),
                value,
                dataType,
                true,
                false
            );
        } else if (sqlTerm._strOperator.equals("<=")) {
            return new Range(sqlTerm._strColumnName, columnMin.get(columnName), value, dataType);
        } else if (sqlTerm._strOperator.equals("!=")) {
            return new BinaryExpression(
                new Range(
                    sqlTerm._strColumnName,
                    columnMin.get(columnName),
                    value,
                    dataType,
                    true,
                    false
                ),
                new Range(
                    sqlTerm._strColumnName,
                    value,
                    columnMax.get(columnName),
                    dataType,
                    false,
                    true
                ),
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

    public boolean isIndexed(String columnName) {
        return indexNames.containsKey(columnName);
    }

    public String getIndexName(String columnName) {
        return indexNames.get(columnName);
    }

    public String getIndexType(String columnName) {
        return indexTypes.get(columnName);
    }

    public Object getColumnMin(String columnName) {
        return columnMin.get(columnName);
    }

    public Object getColumnMax(String columnName) {
        return columnMax.get(columnName);
    }

    public void createIndex(String name, String type, String[] columns) throws DBAppException {
        if (indexNames.containsValue(name)) {
            throw new DBAppException("Index name " + name + " already exists");
        }

        for (String column : columns) {
            if (!columnTypes.containsKey(column)) {
                throw new DBAppException("Column " + column + " not found");
            }

            if (indexNames.containsKey(column)) {
                throw new DBAppException("Column " + column + " already indexed");
            }

            this.indexNames.put(column, name);
            this.indexTypes.put(column, type);
        }


        Index index = indexManager.createIndex(name, type, this, columns);
        Iterator<Row> iterator = iterator();

        while (iterator.hasNext()) {
            Row row = iterator.next();
            index.insert(row);
        }

        indexManager.saveIndex(index);
    }

    public Iterator<Row> iterator() {
        return new TableIterator(this, pageManager);
    }

    public DataType getDataType(String columnName) {
        return dataTypes.get(columnTypes.get(columnName));
    }

    public String getIndexForSqlTerms(SQLTerm[] sqlTerms, String[] operators) {
        if (sqlTerms.length == 0) {
            return null;
        }

        for (String operator : operators) {
            if (!operator.equalsIgnoreCase("AND")) {
                return null;
            }
        }

        String bestIndex = null;
        Hashtable<String, Integer> indexColumnCount = new Hashtable<>();

        for (SQLTerm sqlTerm : sqlTerms) {
            String columnName = sqlTerm._strColumnName;
            String indexName = indexNames.get(columnName);

            if (indexName != null && !sqlTerm._strOperator.equals("!=")) {
                indexColumnCount.put(indexName, indexColumnCount.getOrDefault(indexName, 0) + 1);

                if (bestIndex == null ||
                    indexColumnCount.get(indexName) > indexColumnCount.get(bestIndex)) {
                    bestIndex = indexName;
                }
            }
        }

        return bestIndex;
    }

    public Expression getExpression(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        Expression expression = sqlTermToExpression(sqlTerms[0]);
        for (int i = 1; i < sqlTerms.length; i++) {
            expression = new BinaryExpression(
                expression,
                sqlTermToExpression(sqlTerms[i]),
                operators[i - 1]
            );
        }
        return expression;
    }

    public Iterator<Row> selectFromTableLinear(
        SQLTerm[] sqlTerms,
        String[] operators
    ) throws
        DBAppException {
        Iterator<Row> tableIterator = iterator();
        Expression expression = getExpression(sqlTerms, operators);

        return new FilterIterator(tableIterator, expression);
    }

    public Iterator<Row> selectFromTableIndexed(
        SQLTerm[] sqlTerms,
        String[] operators,
        String indexName
    ) throws DBAppException {
        Index index = indexManager.loadIndex(indexName, tableName);
        Vector<Range> ranges = new Vector<>();
        for (SQLTerm sqlTerm : sqlTerms) {
            if (!Objects.equals(sqlTerm._strOperator, "!=") &&
                getIndexName(sqlTerm._strColumnName).equals(indexName)) {
                ranges.add((Range) sqlTermToExpression(sqlTerm));
            }
        }

        return new FilterIterator(
            new RowsIterator(index.find(ranges), pageManager),
            getExpression(sqlTerms, operators)
        );
    }

    public Iterator<Row> select(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        String indexName = getIndexForSqlTerms(sqlTerms, operators);

        return indexName != null
            ? selectFromTableIndexed(sqlTerms, operators, indexName)
            : selectFromTableLinear(sqlTerms, operators);
    }
}
