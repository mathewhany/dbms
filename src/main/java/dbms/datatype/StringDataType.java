package dbms.datatype;

import dbms.DBAppException;

public class StringDataType implements DataType {
    @Override
    public int compare(Object a, Object b) {
        return ((String) a).compareToIgnoreCase((String) b);
    }

    @Override
    public Object parse(String str) {
        return str;
    }

    @Override
    public String toString(Object obj) {
        return obj.toString();
    }
}
