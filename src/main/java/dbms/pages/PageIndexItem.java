package dbms.pages;

import java.io.Serializable;

public class PageIndexItem implements Serializable {
    private Object pageMin;
    private String pageId;

    public PageIndexItem(Object pageMin, String pageId) {
        this.setPageMin(pageMin);
        this.setPageId(pageId);
    }

    public Object getPageMin() {
        return pageMin;
    }

    public void setPageMin(Object pageMin) {
        this.pageMin = pageMin;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
