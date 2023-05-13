package dbms.indicies;

import dbms.util.Range;
import dbms.pages.Row;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public interface Index {
    Iterator<String> find(Vector<Range> ranges);

    void insert(Row row);

    void delete(Row row);

    String getName();
    String getTableName();

    Iterator<String> findExact(Hashtable<String, Object> searchKey);
}
