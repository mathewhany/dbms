package dbms.datatype;

import dbms.DBAppException;

import java.io.Serializable;

public interface DataType extends Serializable {
    int compare(Object a, Object b);
    Object parse(String str) throws DBAppException;
    String toString(Object obj) throws DBAppException;
    Object calculateMid(Object start, Object end);
    boolean isValidObject(Object obj);
}
