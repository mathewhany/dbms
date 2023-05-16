package dbms.datatype;

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

    @Override
    public Object calculateMid(Object start, Object end) {
        // https://www.geeksforgeeks.org/find-the-string-present-at-the-middle-of-a-lexicographically-increasing-sequence-of-strings-from-s-to-t/
        // https://piazza.com/class/lel8rsvwc4e7j6/post/47
        // https://piazza.com/class/lel8rsvwc4e7j6/post/56
        String startStr = ((String) start).toLowerCase();
        String endStr = ((String) end).toLowerCase();

        int N = Math.min(startStr.length(), endStr.length());

        int[] a1 = new int[N + 1];

        for (int i = 0; i < N; i++) {
            a1[i + 1] = (int) startStr.charAt(i) - 97
                        + (int) endStr.charAt(i) - 97;
        }

        // Iterate from right to left
        // and add carry to next position
        for (int i = N; i >= 1; i--) {
            a1[i - 1] += a1[i] / 26;
            a1[i] %= 26;
        }

        // Reduce the number to find the middle
        // string by dividing each position by 2
        for (int i = 0; i <= N; i++) {

            // If current value is odd,
            // carry 26 to the next index value
            if ((a1[i] & 1) != 0) {

                if (i + 1 <= N) {
                    a1[i + 1] += 26;
                }
            }

            a1[i] = a1[i] / 2;
        }

        StringBuilder mid = new StringBuilder();
        for (int i = 1; i <= N; i++) {
            mid.append((char) (a1[i] + 97));
        }

        return mid.toString();
    }

    @Override
    public boolean isValidObject(Object obj) {
        return obj instanceof String;
    }
}
