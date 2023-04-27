import java.time.LocalDate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Test {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
        dbApp.init();

//        dbApp.createTable(
//            "TA",
//            "id",
//            new Hashtable<>() {{
//                put("id", "java.lang.Integer");
//                put("name", "java.lang.String");
//                put("birthdate", "java.util.Date");
//            }},
//            new Hashtable<>() {{
//                put("id", "0");
//                put("name", "A");
//                put("birthdate", "1950-01-01");
//            }},
//            new Hashtable<>() {{
//                put("id", "2000");
//                put("name", "Z");
//                put("birthdate", "2023-01-01");
//            }}
//        );


        for (int i = 0; i < 20; i++) {
            Hashtable<String, Object> values1 = new Hashtable<>();
            values1.put("id", i);
            values1.put("name", "Boles");
            values1.put("birthdate", new Date("2002/12/12"));

            dbApp.insertIntoTable(
                "TA",
                values1
            );
        }


        Table table = dbApp.loadTable("TA");
        Vector<PageIndexItem> pagesIndex = table.getPagesIndex();

        for (PageIndexItem pageIndex : pagesIndex) {
            Page page = Page.load(pageIndex.fileName);
            System.out.println(pageIndex.pageMin + " Min ");
            for (Row row : page.getRows()) {
                System.out.println(row.get("id") + " " + row.get("name") + " " + row.get("birthdate"));
            }
            System.out.println();
        }

        System.out.println("=====================================");
//
//        Hashtable<String, Object> values = new Hashtable<>();
//
//        dbApp.deleteFromTable(
//            "Student",
//            values
//        );
//
//        table = Table.load("Student");
//        pagesIndex = table.getPagesIndex();
//
//        for (PageIndexItem pageIndex : pagesIndex) {
//            Page page = Page.load(pageIndex.fileName);
//            for (Row row : page.getRows()) {
//                System.out.println(row.get("id") + " " + row.get("name") + " " + row.get("gpa"));
//            }
//            System.out.println();
//        }

//                Vector<Integer> integers = new Vector<>();
//        integers.add(1); // 0
//        integers.add(3); // 1
//        integers.add(10); // 2
//        integers.add(140); // 3
//        integers.add(200); // 4
//
//        System.out.println(Util.binarySearch(integers, 0));
    }
}
