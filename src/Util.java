import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

public class Util {
    public static <T extends Comparable<T>> int binarySearch(Vector<T> vector, T searchKey) {
        int left = 0;
        int right = vector.size() - 1;
        int result = -1;
        while (left <= right) {
            int mid = (left + right) / 2;
            T midItem = vector.get(mid);

            int comparison = midItem.compareTo(searchKey);
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
