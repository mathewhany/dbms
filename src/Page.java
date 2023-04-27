import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {
    private final Vector<Row> rows = new Vector<>();
    private String fileName;
    private String clusteringKeyColumnName;
    private final int maxRows;

    public Page(int maxRows) {
        this.maxRows = maxRows;
    }

    public static Page load(String fileName) throws DBAppException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));

            return (Page) ois.readObject();
        } catch (IOException e) {
            throw new DBAppException("Page file not found: " + fileName);
        } catch (ClassNotFoundException e) {
            throw new DBAppException("Page file couldn't be loaded : " + fileName);
        }
    }

    public void save() throws DBAppException {
        try {
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(fileName));

            ois.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DBAppException("Saving page failed: " + fileName);
        }
    }

    public Vector<Row> getRows() {
        return rows;
    }

    public Row insert(Row row) throws DBAppException {
        if (rows.size() == 0) {
            rows.add(row);
            return null;
        }

        int index = Util.binarySearch(rows, row);

        if (index >= 0 && rows.get(index).getClusteringKeyValue().equals(row.getClusteringKeyValue())) {
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

    public void setClusteringKeyColumnName(String clusteringKeyColumnName) {
        this.clusteringKeyColumnName = clusteringKeyColumnName;
    }

    public void update(Object clusteringKeyValue, Hashtable<String, Object> newValues) throws
        DBAppException {
        Hashtable<String, Object> searchRowValues = new Hashtable<>();
        searchRowValues.put(clusteringKeyColumnName, clusteringKeyValue);

        Row searchRow = new Row(searchRowValues, clusteringKeyColumnName);

        int index = Util.binarySearch(rows, searchRow);
        Row requiredRow = rows.get(index);

        if (!requiredRow.getClusteringKeyValue().equals(clusteringKeyValue)) {
            throw new DBAppException("Row not found");
        }

        for (String key : newValues.keySet()) {
            requiredRow.put(key, newValues.get(key));
        }
    }

    public void deleteBinarySearch(Hashtable<String, Object> searchValues) {
        Row searchRow = new Row(searchValues, clusteringKeyColumnName);

        int index = Util.binarySearch(rows, searchRow);

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
