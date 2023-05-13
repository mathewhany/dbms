package dbms.indicies;

import dbms.DBAppException;
import dbms.util.Range;
import dbms.config.Config;
import dbms.tables.Table;

import java.util.Vector;

public class OctreeFactory implements IndexFactory {

    private final Config config;

    public OctreeFactory(Config config) {
        this.config = config;
    }

    @Override
    public Index createIndex(String indexName, Table table, String[] columnNames) throws
        DBAppException {
        String tableName = table.getTableName();
        Vector<Range> ranges = new Vector<>();
        for (String columnName : columnNames) {
            ranges.add(new Range(
                columnName,
                table.getColumnMin(columnName),
                table.getColumnMax(columnName),
                table.getDataType(columnName)
            ));
        }

        return new OctreeNode(
            indexName,
            tableName,
            config.getMaximumEntriesInOctreeNode(),
            ranges
        );
    }
}
