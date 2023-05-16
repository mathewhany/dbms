package dbms.iterators;

import dbms.DBAppException;
import dbms.pages.Page;
import dbms.pages.PageManager;
import dbms.pages.Row;

import java.util.Iterator;

public class RowsIterator implements Iterator<Row> {

    private final Iterator<String> pagesIterator;
    private final PageManager pageManager;
    private Row nextRow;
    private Iterator<Row> rowsIterators;

    public RowsIterator(Iterator<String> pagesIterator, PageManager pageManager) {
        this.pagesIterator = pagesIterator;
        this.pageManager = pageManager;

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
                String pageId = pagesIterator.next();
                Page currentPage = pageManager.loadPage(pageId);
                rowsIterators = currentPage.getRows().iterator();

                return nextRow();
            } catch (DBAppException e) {
                throw new RuntimeException(e);
            }
        }

        return rowsIterators.next();
    }
}
