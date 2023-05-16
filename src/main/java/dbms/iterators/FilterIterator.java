package dbms.iterators;

import dbms.util.Expression;
import dbms.pages.Row;

import java.util.Iterator;

public class FilterIterator implements Iterator<Row> {
    private final Iterator<Row> iterator;
    private final Expression expression;
    private Row nextRow;

    public FilterIterator(Iterator<Row> iterator, Expression expression) {
        this.iterator = iterator;
        this.expression = expression;
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
        while (iterator.hasNext()) {
            Row row = iterator.next();

            if (expression.evaluate(row)) {
                return row;
            }
        }

        return null;
    }
}
