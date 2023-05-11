package dbms.indicies;

public interface IndexManager {
    Index loadIndex(String indexName, String tableName);

    void saveIndex(Index index);
}
