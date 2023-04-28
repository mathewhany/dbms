package dbms.util;

import dbms.datatype.DataType;

import java.util.Vector;
import java.util.function.Function;

public class Util {
    public static <T, K> int binarySearch(
        Vector<T> vector,
        K searchKey,
        Function<T, K> keyExtractor,
        DataType dataType
    ) {
        int left = 0;
        int right = vector.size() - 1;
        int result = -1;

        while (left <= right) {
            int mid = (left + right) / 2;
            T midItem = vector.get(mid);
            K midKey = keyExtractor.apply(midItem);

            int comparison = dataType.compare(midKey, searchKey);
            if (comparison <= 0) {
                result = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return result;
    }
}
