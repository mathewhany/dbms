package dbms;

import dbms.pages.Page;
import dbms.pages.PageIndexItem;
import dbms.pages.PageManager;
import dbms.pages.Row;
import dbms.tables.Table;

import java.util.Iterator;

public class TableIterator implements Iterator<Row> {
    private final Iterator<PageIndexItem> pagesIterator;
    private Row nextRow;
    private Iterator<Row> rowsIterators;
    private final PageManager pageManager;


    public TableIterator(Table table, PageManager pageManager) {
        this.pageManager = pageManager;
        this.pagesIterator = table.getPagesIndex().iterator();
        this.nextRow = nextRow();
    }

    @Override
    public boolean hasNext() {
        return this.nextRow != null;
    }

    @Override
    public Row next() {
        Row tmp = this.nextRow;
        this.nextRow = nextRow();
        return tmp;
    }

    private Row nextRow() {
        if (rowsIterators == null || !rowsIterators.hasNext()) {
            if (!pagesIterator.hasNext()) {
                return null;
            }

            try {
                Page currentPage = pageManager.loadPage(pagesIterator.next().pageId);
                rowsIterators = currentPage.getRows().iterator();
                return nextRow();
            } catch (DBAppException e) {
                throw new RuntimeException(e);
            }
        }

        return rowsIterators.next();
    }
}
