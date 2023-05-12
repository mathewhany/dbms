package dbms.datatype;

import dbms.DBAppException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDataType implements DataType {
    @Override
    public int compare(Object a, Object b) {
        return ((Date) a).compareTo((Date) b);
    }

    @Override
    public Object parse(String str) throws DBAppException {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (Exception e) {
            throw new DBAppException("Invalid date, must be in the form yyyy-MM-dd: " + str, e);
        }
    }

    @Override
    public String toString(Object obj) {
        return new SimpleDateFormat("yyyy-MM-dd").format((Date) obj);
    }

    @Override
    public Object calculateMid(Object start, Object end) {
        Date startDate = (Date) start;
        Date endDate = (Date) end;

        return new Date((startDate.getTime() + endDate.getTime()) / 2);

    }

    @Override
    public boolean isValidObject(Object obj) {
        return obj instanceof Date;
    }
}
