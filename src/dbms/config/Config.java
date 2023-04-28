package dbms.config;

public class Config {
    private final int maximumRowsCountInTablePage;
    private final int maximumEntriesInOctreeNode;

    public Config(int maximumRowsCountInTablePage, int maximumEntriesInOctreeNode) {
        this.maximumRowsCountInTablePage = maximumRowsCountInTablePage;
        this.maximumEntriesInOctreeNode = maximumEntriesInOctreeNode;
    }

    public int getMaximumRowsCountInTablePage() {
        return maximumRowsCountInTablePage;
    }

    public int getMaximumEntriesInOctreeNode() {
        return maximumEntriesInOctreeNode;
    }
}
