package dbms.tables;

import dbms.*;
import dbms.indicies.Index;
import dbms.indicies.IndexManager;
import dbms.iterators.FilterIterator;
import dbms.iterators.RowsIterator;
import dbms.iterators.SortedPagesIterator;
import dbms.iterators.TableIterator;
import dbms.pages.*;
import dbms.util.BinaryExpression;
import dbms.util.Expression;
import dbms.util.Range;
import dbms.util.Util;
import dbms.config.Config;
import dbms.datatype.DataType;

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
        int lastPageIndex = Integer.parseInt(lastPageIndexItem.getPageId().split("_")[1]);

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
        Hashtable<String, Index> indices = loadAllIndices();
        DataType clusteringKeyType = dataTypes.get(columnTypes.get(clusteringKeyColumn));

        int searchRes = Util.binarySearch(
            pagesIndex,
            currentRow.getClusteringKeyValue(),
            PageIndexItem::getPageMin,
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
            Page page = pageManager.loadPage(pageIndex.getPageId());
            Row returnedRow = page.insert(currentRow);
            pageManager.savePage(page);
            pageIndex.setPageMin(page.getRows().get(0).getClusteringKeyValue());
            page = null;
            System.gc();

            if (returnedRow == null ||
                clusteringKeyType.compare(
                    returnedRow.getClusteringKeyValue(),
                    currentRow.getClusteringKeyValue()
                ) != 0) {
                for (Index tableIndex : indices.values()) {
                    tableIndex.delete(currentRow);
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

        DataType clusteringKeyDataType = getDataType(clusteringKeyColumn);
        Object clusterKeyValueObj = clusteringKeyDataType.parse(clusteringKeyValue);

        String clusteringKeyIndexName = indexNames.get(clusteringKeyColumn);
        Vector<String> pagesToUpdate = new Vector<>();

        if (clusteringKeyIndexName != null) {
            Index clusteringKeyIndex = indexManager.loadIndex(clusteringKeyIndexName, tableName);
            Hashtable<String, Object> clusteringKeySearch = new Hashtable<>();
            clusteringKeySearch.put(clusteringKeyColumn, clusterKeyValueObj);
            Iterator<String> clusteringKeySearchIterator =
                clusteringKeyIndex.findExact(clusteringKeySearch);
            while (clusteringKeySearchIterator.hasNext()) {
                pagesToUpdate.add(clusteringKeySearchIterator.next());
            }
        } else {
            int searchRes = Util.binarySearch(
                pagesIndex,
                clusterKeyValueObj,
                PageIndexItem::getPageMin,
                clusteringKeyDataType
            );

            if (searchRes < 0) {
//            throw new DBAppException(
//                "Row with clustering key " + clusteringKeyValue + " not found");
                // https://piazza.com/class/lel8rsvwc4e7j6/post/158
                return;
            }
            PageIndexItem pageIndex = pagesIndex.get(searchRes);
            pagesToUpdate.add(pageIndex.getPageId());
        }


        Hashtable<String, Index> indices = new Hashtable<>();
        for (String columnName : newValues.keySet()) {
            String indexName = indexNames.get(columnName);

            if (indexName == null || indices.containsKey(columnName)) {
                continue;
            }

            Index index = indexManager.loadIndex(indexName, tableName);
            indices.put(columnName, index);
        }

        for (String pageId : pagesToUpdate) {
            Page page = pageManager.loadPage(pageId);
            UpdatedRow updatedRow = page.update(clusterKeyValueObj, newValues);
            pageManager.savePage(page);

            page = null;
            System.gc();

            if (updatedRow == null) {
                continue;
            }

            for (Index index : indices.values()) {
                index.delete(updatedRow.getOldRow());
                index.insert(updatedRow.getUpdatedRow());
            }
        }

        for (Index index : indices.values()) {
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


    private String getBestIndexForColumns(
        Collection<String> columns
    ) {
        String bestIndex = null;
        Hashtable<String, Integer> counts = new Hashtable<>();

        for (String column : columns) {
            String indexName = indexNames.get(column);

            if (indexName != null) {
                counts.put(indexName, counts.getOrDefault(indexName, 0) + 1);

                if (bestIndex == null || counts.get(indexName) > counts.get(bestIndex)) {
                    bestIndex = indexName;
                }
            }
        }

        return bestIndex;
    }

    public void delete(Hashtable<String, Object> searchValues) throws DBAppException {
        validate(searchValues);
        Hashtable<String, Index> indices = loadAllIndices();
        String maxIndex = getBestIndexForColumns(searchValues.keySet());

        Vector<Row> deletedRows;

        if (maxIndex != null) {
            deletedRows = deleteUsingIndex(searchValues, indices.get(maxIndex));
        } else if (searchValues.containsKey(clusteringKeyColumn)) {
            deletedRows = deleteUsingClusteringKey(searchValues);
        } else {
            deletedRows = linearDelete(searchValues);
        }

        for (Index index : indices.values()) {
            for (Row row : deletedRows) {
                index.delete(row);
            }
            indexManager.saveIndex(index);
        }
    }

    private Hashtable<String, Index> loadAllIndices() throws DBAppException {
        Hashtable<String, Index> indices = new Hashtable<>();
        for (String indexName : indexNames.values()) {
            if (!indices.contains(indexName)) {
                indices.put(indexName, indexManager.loadIndex(indexName, tableName));
            }
        }
        return indices;
    }

    private Vector<Row> deleteUsingIndex(Hashtable<String, Object> searchValues, Index index) throws
        DBAppException {
        Hashtable<String, Object> indexSearchValues = new Hashtable<>();
        for (String searchColumn : searchValues.keySet()) {
            String indexName = indexNames.get(searchColumn);
            if (indexName != null && indexName.equals(index.getName())) {
                indexSearchValues.put(searchColumn, searchValues.get(searchColumn));
            }
        }

        Vector<Row> allRows = new Vector<>();
        Iterator<String> pages = index.findExact(indexSearchValues);
        while (pages.hasNext()) {
            Page page = pageManager.loadPage(pages.next());
            allRows.addAll(page.delete(searchValues));
            pageManager.savePage(page);
            deletePageOrSave(page);
            page = null;
            System.gc();
        }

        return allRows;
    }

    private Vector<Row> linearDelete(Hashtable<String, Object> searchValues) throws DBAppException {
        Vector<Row> deletedRows = new Vector<>();

        for (int i = 0; i < pagesIndex.size(); i++) {
            PageIndexItem pageIndexItem = pagesIndex.get(i);
            Page page = pageManager.loadPage(pageIndexItem.getPageId());
            deletedRows.addAll(page.delete(searchValues));

            if (deletePageOrSave(page)) {
                i--;
            }

            page = null;
            System.gc();
        }

        return deletedRows;
    }

    private Vector<Row> deleteUsingClusteringKey(Hashtable<String, Object> searchValues) throws
        DBAppException {
        int pageIndex = Util.binarySearch(
            pagesIndex,
            searchValues.get(clusteringKeyColumn),
            PageIndexItem::getPageMin,
            dataTypes.get(columnTypes.get(clusteringKeyColumn))
        );

        if (pageIndex < 0) {
            return new Vector<>();
        }

        PageIndexItem pageIndexItem = pagesIndex.get(pageIndex);
        Page page = pageManager.loadPage(pageIndexItem.getPageId());
        Vector<Row> deletedRows = page.delete(searchValues);

        deletePageOrSave(page);

        page = null;
        System.gc();

        return deletedRows;
    }

    private boolean deletePageOrSave(Page page) throws DBAppException {
        int idx = -1;
        for (int i = 0; i < pagesIndex.size(); i++) {
            if (pagesIndex.get(i).getPageId().equals(page.getPageId())) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            throw new DBAppException("Page not found in pages index");
        }

        PageIndexItem pageIndexItem = pagesIndex.get(idx);

        if (page.getRows().size() == 0) {
            pagesIndex.remove(idx);
            pageManager.deletePage(pageIndexItem.getPageId());
            return true;
        } else {
            pageIndexItem.setPageMin(page.getRows().get(0).get(clusteringKeyColumn));
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
        Vector<String> columns = new Vector<>();

        for (int i = operators.length - 1;
             i >= 0 && operators[i].equalsIgnoreCase("and") || i == -1; i--) {
            if (!sqlTerms[i + 1]._strOperator.equals("!=")) {
                columns.add(sqlTerms[i + 1]._strColumnName);
            }
        }

        return getBestIndexForColumns(columns);
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
        SQLTerm[] sqlTerms, String[] operators
    ) throws DBAppException {
        Iterator<Row> tableIterator = iterator();
        Expression expression = getExpression(sqlTerms, operators);

        return new FilterIterator(tableIterator, expression);
    }

    public Iterator<Row> selectFromTableIndexed(
        SQLTerm[] sqlTerms, String[] operators, String indexName
    ) throws DBAppException {
        Index index = indexManager.loadIndex(indexName, tableName);
        Vector<Range> ranges = new Vector<>();
        for (SQLTerm sqlTerm : sqlTerms) {
            String indexNameForTerm = getIndexName(sqlTerm._strColumnName);
            if (!Objects.equals(sqlTerm._strOperator, "!=") &&
                indexNameForTerm != null && indexNameForTerm.equals(indexName)) {
                ranges.add((Range) sqlTermToExpression(sqlTerm));
            }
        }

        Iterator<String> indexIterator = index.find(ranges);
        Iterator<String> indexResults = DeveloperFlags.SORT_OUTPUT_OF_INDEX
            ? new SortedPagesIterator(indexIterator)
            : indexIterator;

        return new FilterIterator(
            new RowsIterator(indexResults, pageManager),
            getExpression(sqlTerms, operators)
        );
    }

    public Iterator<Row> select(SQLTerm[] sqlTerms, String[] operators) throws DBAppException {
        String indexName = getIndexForSqlTerms(sqlTerms, operators);

        return indexName != null ? selectFromTableIndexed(
            sqlTerms,
            operators,
            indexName
        ) : selectFromTableLinear(sqlTerms, operators);
    }
}
