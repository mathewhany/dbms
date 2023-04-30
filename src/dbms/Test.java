package dbms;

import dbms.pages.Page;
import dbms.pages.PageIndexItem;
import dbms.pages.Row;
import dbms.pages.SerializedPageManager;
import dbms.tables.Table;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Test {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
        dbApp.init();

//        Hashtable<String, String> columnTypes = new Hashtable<>();
//        columnTypes.put("id", "java.lang.Integer");
//        columnTypes.put("name", "java.lang.String");
//        columnTypes.put("birthdate", "java.util.Date");
//
//        Hashtable<String, String> columnMin = new Hashtable<>();
//        columnMin.put("id", "1");
//        columnMin.put("name", "A");
//        columnMin.put("birthdate", "1950-01-01");
//
//        Hashtable<String, String> columnMax = new Hashtable<>();
//        columnMax.put("id", "2000");
//        columnMax.put("name", "Z");
//        columnMax.put("birthdate", "2023-01-01");
//
//        dbApp.createTable(
//            "Student2",
//            "name",
//            columnTypes,
//            columnMin,
//            columnMax
//        );


//        for (int i = 1; i < 10; i++) {
            Hashtable<String, Object> values1 = new Hashtable<>();
            values1.put("id", 5);
            values1.put("name", "Ahmed");
            values1.put("birthdate", new Date("2000/10/15"));

            dbApp.insertIntoTable(
                "Student2",
                values1
            );
//        }

////
        Table table = dbApp.loadTable("Student2");
        Vector<PageIndexItem> pagesIndex = table.getPagesIndex();
        SerializedPageManager pageManager = new SerializedPageManager();

        for (PageIndexItem pageIndex : pagesIndex) {
            Page page = pageManager.loadPage(pageIndex.fileName);
            System.out.println(pageIndex.pageMin + " Min ");
            for (Row row : page.getRows()) {
                System.out.println(
                    row.get("id") + " " + row.get("name") + " " + row.get("birthdate"));
            }
            System.out.println();
        }
//
//        System.out.println("=====================================");
//
//        Hashtable<String, Object> values = new Hashtable<>();
//        values.put("name", 1);
//
//        dbApp.updateTable(
//            "Student",
//            "9",
//            values
//        );
//
//        table = dbApp.loadTable("Student");
//        pagesIndex = table.getPagesIndex();
//
//        for (dbms.pages.PageIndexItem pageIndex : pagesIndex) {
//            dbms.pages.Page page = pageManager.loadPage(pageIndex.fileName);
//            for (dbms.pages.Row row : page.getRows()) {
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
//        System.out.println(dbms.util.Util.binarySearch(integers, 0));
//
//        Date i = new Date("2000/12/12");
//        System.out.println(i.toString());
    }
}

