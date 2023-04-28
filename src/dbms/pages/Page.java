package dbms.pages;

import dbms.DBAppException;
import dbms.util.Util;
import dbms.datatype.DataType;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {
    private final Vector<Row> rows = new Vector<>();
    private String fileName;
    private String clusteringKeyColumnName;
    private final int maxRows;
    private final Hashtable<String, String> columnTypes;
    private final Hashtable<String, DataType> dataTypes;

    public Page(
        int maxRows, Hashtable<String, String> columnTypes, Hashtable<String, DataType> dataTypes, String clusteringKeyColumnName
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

        if (index >= 0 &&
            rows.get(index).getClusteringKeyValue().equals(row.getClusteringKeyValue())) {
            throw new DBAppException("Duplicate key");
        }

        rows.insertElementAt(row, index + 1);

        if (rows.size() > maxRows) {
            return rows.remove(rows.size() - 1);
        }

        return null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void update(Object clusteringKeyValue, Hashtable<String, Object> newValues) throws
        DBAppException {
        int index = Util.binarySearch(
            rows,
            clusteringKeyValue,
            Row::getClusteringKeyValue,
            dataTypes.get(columnTypes.get(clusteringKeyColumnName))
        );
        Row requiredRow = rows.get(index);

        if (!requiredRow.getClusteringKeyValue().equals(clusteringKeyValue)) {
            throw new DBAppException("Row not found");
        }

        for (String key : newValues.keySet()) {
            requiredRow.put(key, newValues.get(key));
        }
    }

    public void deleteBinarySearch(Hashtable<String, Object> searchValues) {
        Object clusteringKeyValue = searchValues.get(clusteringKeyColumnName);
        int index = Util.binarySearch(
            rows,
            clusteringKeyValue,
            Row::getClusteringKeyValue,
            dataTypes.get(columnTypes.get(clusteringKeyColumnName))
        );

        if (index < 0) {
            return;
        }

        Row requiredRow = rows.get(index);

        if (!requiredRow.matches(searchValues)) return;

        rows.remove(index);
    }

    public void deleteLinearSearch(Hashtable<String, Object> searchValues) {
        Vector<Integer> matchingIndices = new Vector<>();

        for (int i = 0; i < rows.size(); i++) {
            Row requiredRow = rows.get(i);

            if (requiredRow.matches(searchValues)) {
                matchingIndices.add(i);
            }
        }

        for (int i = matchingIndices.size() - 1; i >= 0; i--) {
            rows.remove(matchingIndices.get(i).intValue());
        }
    }
}
