import java.util.Hashtable;

public class Table {
    private String tableName;
    private String clusteringKeyColumn;
    private Hashtable<String, String> columnTypes;
    private Hashtable<String, String> columnMin;
    private Hashtable<String, String> columnMax;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getClusteringKeyColumn() {
        return clusteringKeyColumn;
    }

    public void setClusteringKeyColumn(String clusteringKeyColumn) {
        this.clusteringKeyColumn = clusteringKeyColumn;
    }

    public Hashtable<String, String> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(Hashtable<String, String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Hashtable<String, String> getColumnMin() {
        return columnMin;
    }

    public void setColumnMin(Hashtable<String, String> columnMin) {
        this.columnMin = columnMin;
    }

    public Hashtable<String, String> getColumnMax() {
        return columnMax;
    }

    public void setColumnMax(Hashtable<String, String> columnMax) {
        this.columnMax = columnMax;
    }
}
