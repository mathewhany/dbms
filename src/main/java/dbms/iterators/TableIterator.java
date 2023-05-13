package dbms.iterators;

import dbms.pages.PageIndexItem;
import dbms.pages.PageManager;
import dbms.pages.Row;
import dbms.tables.Table;

import java.util.Iterator;

public class TableIterator implements Iterator<Row> {
    private final Iterator<Row> rowsIterator;

    public TableIterator(Table table, PageManager pageManager) {
        this.rowsIterator = new RowsIterator(
            table.getPagesIndex().stream().map(PageIndexItem::getPageId).toList().iterator(),
            pageManager
        );
    }

    @Override
    public boolean hasNext() {
        return this.rowsIterator.hasNext();
    }

    @Override
    public Row next() {
        return this.rowsIterator.next();
    }
}
