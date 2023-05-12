package dbms.datatype;

import dbms.DBAppException;

public class DoubleDataType implements DataType {
    @Override
    public int compare(Object a, Object b) {
        return ((Double) a).compareTo((Double) b);
    }

    @Override
    public Object parse(String str) throws DBAppException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new DBAppException("Invalid double: " + str, e);
        }
    }

    @Override
    public String toString(Object obj) {
        return obj.toString();
    }

    @Override
    public Object calculateMid(Object start, Object end) {
        return ((Double) start + (Double) end) / 2;
    }

    @Override
    public boolean isValidObject(Object obj) {
        return obj instanceof Double;
    }
}
