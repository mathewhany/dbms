import java.io.Serializable;
import java.util.Hashtable;

public class Row implements Comparable<Row>, Serializable {
    private Hashtable<String, Object> values;
    private String clusteringKeyColumnName;

    public Row(Hashtable<String, Object> values, String clusteringKey) {
        this.values = values;
        this.clusteringKeyColumnName = clusteringKey;
    }

    public Hashtable<String, Object> getValues() {
        return values;
    }

    public Object getClusteringKeyValue() {
        return values.get(clusteringKeyColumnName);
    }

    public Object get(String columnName) {
        return values.get(columnName);
    }
    public void put(String columnName, Object columnValue) {
        values.put(columnName, columnValue);
    }

    @Override
    public int compareTo(Row otherRow) {
        Comparable thisKey = (Comparable) getClusteringKeyValue();
        Comparable otherKey = (Comparable) otherRow.getClusteringKeyValue();

        return thisKey.compareTo(otherKey);
    }
}
