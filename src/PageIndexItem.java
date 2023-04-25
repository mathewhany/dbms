import java.io.Serializable;

public class PageIndexItem implements Serializable, Comparable<PageIndexItem> {
    public Object pageMin;
    public String fileName;

    public PageIndexItem(Object pageMin, String fileName) {
        this.pageMin = pageMin;
        this.fileName = fileName;
    }

    @Override
    public int compareTo(PageIndexItem other) {
        Comparable thisMin = (Comparable) pageMin;
        Comparable otherMin = (Comparable) other.pageMin;

        return thisMin.compareTo(otherMin);
    }
}
