package dbms.pages;

import java.io.Serializable;

public class PageIndexItem implements Serializable {
    public Object pageMin;
    public String fileName;

    public PageIndexItem(Object pageMin, String fileName) {
        this.pageMin = pageMin;
        this.fileName = fileName;
    }
}
