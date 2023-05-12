package dbms.indicies;

import dbms.DBAppException;
import dbms.Range;
import dbms.pages.Row;

import java.io.Serializable;
import java.util.*;

public class OctreeNode implements Index, Serializable {
    private final String name;
    private final String tableName;
    private final int maxCapacity;
    private final Hashtable<Hashtable<String, Object>, Vector<String>> entries = new Hashtable<>();
    private final Vector<OctreeNode> children = new Vector<>();

    private final Vector<Range> ranges;

    public OctreeNode(
        String name, String tableName, int maxCapacity, Vector<Range> ranges
    ) throws DBAppException {
        if (ranges.size() != 3) {
            throw new DBAppException("Octree index must have 3 dimensions");
        }

        this.maxCapacity = maxCapacity;
        this.ranges = ranges;
        this.name = name;
        this.tableName = tableName;
    }

    public void insert(Row row) {
        Hashtable<String, Object> key = new Hashtable<>();

        for (Map.Entry<String, Object> entry : row.getValues().entrySet()) {
            Range range = getRangeForColumn(entry.getKey());

            if (range != null) {
                key.put(entry.getKey(), entry.getValue());
            }
        }

        insert(key, row.getPageId());
    }

    private void insert(Hashtable<String, Object> key, String value) {
        if (isLeaf()) {
            if (entries.containsKey(key)) {
                entries.get(key).add(value);
            } else {
                if (isFull()) {
                    subdivide();
                    insert(key, value);
                } else {
                    Vector<String> values = new Vector<>();
                    values.add(value);
                    entries.put(key, values);
                }
            }
        } else {
            children.get(getIndex(key)).insert(key, value);
        }
    }

    public void delete(Row row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Iterator<String> findExact(Hashtable<String, Object> searchKey) {
        Vector<Range> searchRanges = new Vector<>();

        for (String columnName : searchKey.keySet()) {
            Range range = getRangeForColumn(columnName);
            if (range != null) {
                try {
                    searchRanges.add(new Range(
                        columnName,
                        searchKey.get(columnName),
                        searchKey.get(columnName),
                        range.getType()
                    ));
                } catch (DBAppException e) {
                    // This should never happen
                }
            }
        }

        return find(searchRanges);
    }


    private void subdivide() {
        for (Range x : ranges.get(0).split()) {
            for (Range y : ranges.get(1).split()) {
                for (Range z : ranges.get(2).split()) {
                    try {
                        children.add(new OctreeNode(
                            name,
                            tableName,
                            maxCapacity,
                            new Vector<>(Arrays.asList(x, y, z))
                        ));
                    } catch (DBAppException e) {
                        // This should never happen
                    }
                }
            }
        }

        for (Map.Entry<Hashtable<String, Object>, Vector<String>> entry : entries.entrySet()) {
            for (String value : entry.getValue()) {
                children.get(getIndex(entry.getKey())).insert(entry.getKey(), value);
            }
        }

        entries.clear();
    }

    public Iterator<String> find(Vector<Range> ranges) {
        return new OctreeIterator(this, ranges, new HashSet<>());
    }

    private int getIndex(Hashtable<String, Object> key) {
        return (ranges.get(0).inFirstHalf(key.get(ranges.get(0).getColumnName())) ? 0 : 1) * 4 +
               (ranges.get(1).inFirstHalf(key.get(ranges.get(1).getColumnName())) ? 0 : 1) * 2 +
               (ranges.get(2).inFirstHalf(key.get(ranges.get(2).getColumnName())) ? 0 : 1);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isFull() {
        return entries.size() == maxCapacity;
    }

    public Hashtable<Hashtable<String, Object>, Vector<String>> getEntries() {
        return entries;
    }

    public Vector<OctreeNode> getChildren() {
        return children;
    }

    public Vector<Range> getRanges() {
        return ranges;
    }

    public Range getRangeForColumn(String columnName) {
        for (Range range : ranges) {
            if (range.getColumnName().equals(columnName)) {
                return range;
            }
        }

        return null;
    }
}
