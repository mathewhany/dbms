package dbms.util;

import dbms.pages.Row;

import java.util.Hashtable;

public interface Expression {
    boolean evaluate(Row row);
    boolean evaluate(Hashtable<String, Range> ranges);
}
