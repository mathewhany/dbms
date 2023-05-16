package dbms.pages;

import dbms.DBAppException;
import dbms.util.Util;
import dbms.datatype.DataType;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {
    private final Vector<Row> rows = new Vector<>();
    private String pageId;
    private String clusteringKeyColumnName;
    private final int maxRows;
    private final Hashtable<String, String> columnTypes;
    private final Hashtable<String, DataType> dataTypes;

    public Page(
        int maxRows,
        Hashtable<String, String> columnTypes,
        Hashtable<String, DataType> dataTypes,
        String clusteringKeyColumnName
    ) {
        this.maxRows = maxRows;
        this.columnTypes = columnTypes;
        this.dataTypes = dataTypes;
        this.clusteringKeyColumnName = clusteringKeyColumnName;
    }

    public Vector<Row> getRows() {
        return rows;
    }

    public Row insert(Row row) throws DBAppException {
        row.setPageId(pageId);
        
        if (rows.size() == 0) {
            rows.add(row);
            return null;
        }

        int index = Util.binarySearch(
            rows,
            row.getClusteringKeyValue(),
            Row::getClusteringKeyValue,
            dataTypes.get(columnTypes.get(clusteringKeyColumnName))
        );

        DataType clusteringKeyType = dataTypes.get(columnTypes.get(clusteringKeyColumnName));

        if (index >= 0 &&
            clusteringKeyType.compare(
                rows.get(index).getClusteringKeyValue(),
                row.getClusteringKeyValue()
            ) == 0) {
            throw new DBAppException(
                "Row with same clustering key (" + row.getClusteringKeyValue() +
                ") already exists");
        }

        rows.insertElementAt(row, index + 1);

        if (rows.size() > maxRows) {
            return rows.remove(rows.size() - 1);
        }

        return null;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public UpdatedRow update(Object clusteringKeyValue, Hashtable<String, Object> newValues) throws
        DBAppException {

        DataType clusteringKeyType = dataTypes.get(columnTypes.get(clusteringKeyColumnName));

        int index = Util.binarySearch(
            rows,
            clusteringKeyValue,
            Row::getClusteringKeyValue,
            clusteringKeyType
        );
        Row requiredRow = rows.get(index);
        Row oldRow = null;
        try {
            oldRow = (Row) requiredRow.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
        }
//        System.out.println("requiredRow: " + requiredRow.getClusteringKeyValue());
        if (clusteringKeyType.compare(requiredRow.getClusteringKeyValue(), clusteringKeyValue) !=
            0) {
//            throw new DBAppException("Row not found");
            // https://piazza.com/class/lel8rsvwc4e7j6/post/158
            return null;
        }

        for (String key : newValues.keySet()) {
            requiredRow.put(key, newValues.get(key));
        }
        return new UpdatedRow(oldRow, requiredRow);
    }

    public Vector<Row> delete(Hashtable<String, Object> searchValues) {
        if (searchValues.containsKey(clusteringKeyColumnName)) {
            return deleteBinarySearch(searchValues);
        } else {
            return deleteLinearSearch(searchValues);
        }
    }

    private Vector<Row> deleteBinarySearch(Hashtable<String, Object> searchValues) {
        Vector<Row> deletedRows = new Vector<>();
        Object clusteringKeyValue = searchValues.get(clusteringKeyColumnName);
        int index = Util.binarySearch(
            rows,
            clusteringKeyValue,
            Row::getClusteringKeyValue,
            dataTypes.get(columnTypes.get(clusteringKeyColumnName))
        );

        if (index < 0) {
            return deletedRows;
        }

        Row requiredRow = rows.get(index);

        if (!matches(requiredRow, searchValues)) return deletedRows;

        deletedRows.add(requiredRow);

        rows.remove(index);

        return deletedRows;
    }

    private Vector<Row> deleteLinearSearch(Hashtable<String, Object> searchValues) {
        Vector<Row> deletedRows = new Vector<>();
        Vector<Integer> matchingIndices = new Vector<>();

        for (int i = 0; i < rows.size(); i++) {
            Row requiredRow = rows.get(i);

            if (matches(requiredRow, searchValues)) {
                matchingIndices.add(i);
                deletedRows.add(requiredRow);
            }
        }

        for (int i = matchingIndices.size() - 1; i >= 0; i--) {
            rows.remove(matchingIndices.get(i).intValue());
        }
        return deletedRows;
    }

    private boolean matches(Row row, Hashtable<String, Object> searchValues) {
        Hashtable<String, Object> rowValues = row.getValues();

        for (String key : searchValues.keySet()) {
            DataType dataType = dataTypes.get(columnTypes.get(key));
            Object value = rowValues.get(key);

            if (value == null || dataType.compare(value, searchValues.get(key)) != 0) {
                return false;
            }
        }

        return true;
    }
}
