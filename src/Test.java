import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Test {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
//
//        Hashtable<String, Object> values = new Hashtable<>();
//        values.put("id", 13);
//        values.put("name", "Gomaa 2");
//        values.put("gpa", 3.0);
//
//
//        dbApp.insertIntoTable(
//            "Student",
//            values
//        );
//

        Table table = Table.load("Student");
        Vector<PageIndexItem> pagesIndex = table.getPagesIndex();

        for (PageIndexItem pageIndex : pagesIndex) {
            Page page = Page.load(pageIndex.fileName);
            System.out.println(pageIndex.pageMin + " Min ");
            for (Row row : page.getRows()) {
                System.out.println(row.get("id") + " " + row.get("name") + " " + row.get("gpa"));
            }
            System.out.println();
        }

        System.out.println("=====================================");

        Hashtable<String, Object> values = new Hashtable<>();
        values.put("id", 123);

        dbApp.updateTable("Student", "1", values);

        table = Table.load("Student");
        pagesIndex = table.getPagesIndex();

        for (PageIndexItem pageIndex : pagesIndex) {
            Page page = Page.load(pageIndex.fileName);
            for (Row row : page.getRows()) {
                System.out.println(row.get("id") + " " + row.get("name") + " " + row.get("gpa"));
            }
            System.out.println();
        }

        //        Vector<Integer> integers = new Vector<>();
//        integers.add(1); // 0
//        integers.add(3); // 1
//        integers.add(10); // 2
//        integers.add(140); // 3
//        integers.add(200); // 4
//
//        System.out.println(Util.binarySearch(integers, 0));

    }
}
