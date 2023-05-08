package dbms;

import dbms.pages.Row;

public interface Expression {
    boolean evaluate(Row row);
}
