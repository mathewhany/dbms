package dbms.indicies;

import dbms.Range;

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

//    private final Range x;
//    private final Range y;
//    private final Range z;
//
//    private Iterator<String> pagesIterator;
//    private Iterator<Map.Entry<OctreeKey, Vector<String>>> entriesIterator;
//    private OctreeIterator currentChildIterator;
//    private Iterator<OctreeNode> childrenIterator;
//
//    private String next;
//
//    public OctreeIterator(
//        OctreeNode octreeNode,
//        Range x,
//        Range y,
//        Range z
//    ) {
//        this.x = x == null ? octreeNode.getXRange() : x;
//        this.y = y == null ? octreeNode.getYRange() : y;
//        this.z = z == null ? octreeNode.getZRange() : z;
//
//        if (octreeNode.isLeaf()) {
//            entriesIterator = octreeNode.getEntries().entrySet().iterator();
//        } else {
//            childrenIterator = octreeNode.getChildren().iterator();
//        }
//
//        next = nextMatch();
//    }
//
//    @Override
//    public boolean hasNext() {
//        return next != null;
//    }
//
//    @Override
//    public String next() {
//        String result = next;
//        next = nextMatch();
//        return result;
//    }
//
//    private String nextMatch() {
//        if (pagesIterator != null && pagesIterator.hasNext()) {
//            return pagesIterator.next();
//        }
//
//        if (entriesIterator != null && entriesIterator.hasNext()) {
//            Map.Entry<OctreeKey, Vector<String>> entry = entriesIterator.next();
//            OctreeKey key = entry.getKey();
//
//            if (x.contains(key.getX()) && y.contains(key.getY()) && z.contains(key.getZ())) {
//                pagesIterator = entry.getValue().iterator();
//            }
//
//            return nextMatch();
//        }
//
//        if (currentChildIterator != null && currentChildIterator.hasNext()) {
//            return currentChildIterator.next();
//        }
//
//        if (childrenIterator != null && childrenIterator.hasNext()) {
//            OctreeNode child = childrenIterator.next();
//
//            if (child.getXRange().intersects(x) &&
//                child.getYRange().intersects(y) &&
//                child.getZRange().intersects(z)) {
//                currentChildIterator = new OctreeIterator(child, x, y, z);
//            }
//
//            return nextMatch();
//        }
//
//        return null;
//    }
}
