package dbms.iterators;

import java.util.Iterator;
import java.util.Vector;

public class SortedPagesIterator implements Iterator<String> {
    private final Iterator<String> iterator;

    public SortedPagesIterator(Iterator<String> iterator) {
        Vector<String> pageIds = new Vector<>();

        while (iterator.hasNext()) {
            pageIds.add(iterator.next());
        }

        pageIds.sort((a, b) -> {
            String[] aParts = a.split("_");
            String[] bParts = b.split("_");

            int aNumber = Integer.parseInt(aParts[1]);
            int bNumber = Integer.parseInt(bParts[1]);

            return aNumber - bNumber;
        });

        this.iterator = pageIds.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String next() {
        return iterator.next();
    }
}
