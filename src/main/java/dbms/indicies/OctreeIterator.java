package dbms.indicies;

import dbms.util.Range;

import java.util.*;

public class OctreeIterator implements Iterator<String> {
    private final Vector<Range> ranges;
    private final HashSet<String> history;
    private Iterator<String> pagesIterator;
    private Iterator<Map.Entry<Hashtable<String, Object>, Vector<String>>> entriesIterator;
    private Iterator<OctreeNode> childrenIterator;
    private OctreeIterator currentChildIterator;
    private String nextPage;

    public OctreeIterator(OctreeNode octreeNode, Vector<Range> ranges, HashSet<String> history) {
        for (Range range : octreeNode.getRanges()) {
            boolean found = false;
            for (Range r : ranges) {
                if (r.getColumnName().equals(range.getColumnName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                ranges.add(range);
            }
        }
        this.ranges = ranges;
        this.history = history;

        if (octreeNode.isLeaf()) {
            entriesIterator = octreeNode.getEntries().entrySet().iterator();
        } else {
            childrenIterator = octreeNode.getChildren().iterator();
        }

        nextPage = nextPage();
    }

    @Override
    public boolean hasNext() {
        return nextPage != null;
    }

    @Override
    public String next() {
        String tmp = nextPage;
        nextPage = nextPage();
        return tmp;
    }

    private String nextPage() {
        if (pagesIterator != null && pagesIterator.hasNext()) {
            String currentPage = pagesIterator.next();

            if (history.contains(currentPage)) {
                return nextPage();
            }

            history.add(currentPage);
            return currentPage;
        }

        if (entriesIterator != null && entriesIterator.hasNext()) {
            Map.Entry<Hashtable<String, Object>, Vector<String>> entry = entriesIterator.next();

            for (Range range : ranges) {
                if (!range.contains(entry.getKey().get(range.getColumnName()))) {
                    return nextPage();
                }
            }

            pagesIterator = entry.getValue().iterator();
            return nextPage();
        }

        if (currentChildIterator != null && currentChildIterator.hasNext()) {
            return currentChildIterator.next();
        }

        if (childrenIterator != null && childrenIterator.hasNext()) {
            OctreeNode child = childrenIterator.next();

            for (Range range : child.getRanges()) {
                for (Range r : ranges) {
                    if (r.getColumnName().equals(range.getColumnName())) {
                        if (!r.intersects(range)) {
                            return nextPage();
                        }
                    }
                }
            }

            currentChildIterator = new OctreeIterator(child, ranges, history);

            return nextPage();
        }

        return null;
    }
}
