package dbms.iterators;

import dbms.pages.Row;

import java.util.Hashtable;
import java.util.Iterator;

public class RowToHashtableIterator implements Iterator<Hashtable<String, Object>> {
    private final Iterator<Row> iterator;

    public RowToHashtableIterator(Iterator<Row> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Hashtable<String, Object> next() {
        return iterator.next().getValues();
    }
}
