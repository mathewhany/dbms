import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {
    private Vector<Row> rows = new Vector<>();
    private String fileName;
    private String clusteringKeyColumnName;
    private int maxRows = 2;

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

    public Row insert(Row row) {
        if (rows.size() == 0) {
            rows.add(row);
            return null;
        }

        int index = Util.binarySearch(rows, row);

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
}
