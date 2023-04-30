package dbms.pages;

import java.io.Serializable;

public class PageIndexItem implements Serializable {
    public Object pageMin;
    public String pageId;

    public PageIndexItem(Object pageMin, String pageId) {
        this.pageMin = pageMin;
        this.pageId = pageId;
    }
}
