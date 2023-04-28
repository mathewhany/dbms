package dbms.datatype;

import dbms.DBAppException;

public class IntegerDataType implements DataType {
    @Override
    public int compare(Object a, Object b) {
        return ((Integer) a).compareTo((Integer) b);
    }

    @Override
    public Object parse(String str) throws DBAppException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new DBAppException("Invalid integer: " + str, e);
        }
    }

    @Override
    public String toString(Object obj) {
        return obj.toString();
    }
}
