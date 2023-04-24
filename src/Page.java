import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {
    private Vector<Hashtable<String, Object>> rows = new Vector<>();
    private String fileName;
    private String clusteringKeyColumn;
    private int maxRows = 100;

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
            throw new DBAppException("Saving page failed: " + fileName);
        }
    }

    public Vector<Hashtable<String, Object>> getRows() {
        return rows;
    }

    public Hashtable<String, Object> insert(Hashtable<String, Object> row) {
        if (rows.size() == 0) {
            rows.add(row);
            return null;
        }

        int left = 0;
        int right = rows.size() - 1;
        Comparable rowKey = (Comparable) row.get(clusteringKeyColumn);
        while (left <= right) {
            int mid = (left + right) / 2;
            Hashtable<String, Object> midRow = rows.get(mid);
            Comparable midKey = (Comparable) midRow.get(clusteringKeyColumn);

            int comparison = midKey.compareTo(rowKey);
            if (comparison <= 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        int index = left;

        rows.insertElementAt(row, index);

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

    public boolean isFull() {
        return rows.size() == maxRows;
    }
}
