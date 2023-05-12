package dbms;

import dbms.datatype.DataType;
import dbms.pages.Row;

public class Range implements Expression {
    private final String columnName;

    private final Object start;
    private final Object end;


    public DataType getType() {
        return type;
    }

    private final DataType type;

    private final boolean isStartInclusive;
    private final boolean isEndInclusive;

    public Range(String columnName, Object start, Object end, DataType type) {
        this(columnName, start, end, type, true, true);
    }

    public Range(
        String columnName,
        Object start,
        Object end,
        DataType type,
        boolean isStartInclusive,
        boolean isEndInclusive
    ) {
        this.columnName = columnName;
        this.start = start;
        this.end = end;
        this.type = type;
        this.isStartInclusive = isStartInclusive;
        this.isEndInclusive = isEndInclusive;
    }

    public Object getStart() {
        return start;
    }

    public Object getEnd() {
        return end;
    }

    public boolean isStartInclusive() {
        return isStartInclusive;
    }

    public boolean isEndInclusive() {
        return isEndInclusive;
    }

    public Object getMid() {
        return type.calculateMid(start, end);
    }

    public Range[] split() {
        Object mid = getMid();

        Range[] ranges = new Range[2];
        ranges[0] = new Range(columnName, start, mid, type, isStartInclusive, false);
        ranges[1] = new Range(columnName, mid, end, type, true, isEndInclusive);

        return ranges;
    }

    public boolean intersects(Range otherRange) {
        if (type.compare(start, otherRange.end) > 0 || type.compare(end, otherRange.start) < 0) {
            return false;
        }

        if (type.compare(start, otherRange.end) == 0) {
            return isStartInclusive && otherRange.isEndInclusive;
        }

        if (type.compare(end, otherRange.start) == 0) {
            return isEndInclusive && otherRange.isStartInclusive;
        }

        return true;
    }

    public boolean inFirstHalf(Object value) {
        return this.split()[0].contains(value);
    }

    public boolean contains(Object value) {
        return
            (isStartInclusive ? type.compare(start, value) <= 0 : type.compare(start, value) < 0) &&
            (isEndInclusive ? type.compare(end, value) >= 0 : type.compare(end, value) > 0);
    }

    @Override
    public boolean evaluate(Row row) {
        Object value = row.get(columnName);

        return contains(value);
    }

    public String getColumnName() {
        return columnName;
    }
}
