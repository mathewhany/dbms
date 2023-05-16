import dbms.DBApp;
import dbms.DBAppException;
import dbms.SQLTerm;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

public class Test {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
        dbApp.init();
//
//        Hashtable<String, String> columnTypes = new Hashtable<>();
//        columnTypes.put("id", "java.lang.Integer");
//        columnTypes.put("name", "java.lang.String");
//        columnTypes.put("birthdate", "java.util.Date");
//        columnTypes.put("salary", "java.lang.Integer");
//        columnTypes.put("gpa", "java.lang.Double");
//        columnTypes.put("rank", "java.lang.Integer");
//
//
//        Hashtable<String, String> columnMin = new Hashtable<>();
//        columnMin.put("id", "1");
//        columnMin.put("name", "A");
//        columnMin.put("birthdate", "1990-01-01");
//        columnMin.put("salary", "0");
//        columnMin.put("gpa", "0.0");
//        columnMin.put("rank", "1");
//
//        Hashtable<String, String> columnMax = new Hashtable<>();
//        columnMax.put("id", "30000");
//        columnMax.put("name", "ZZZZZZZZZZZZZ");
//        columnMax.put("birthdate", "2023-01-01");
//        columnMax.put("salary", "100000");
//        columnMax.put("gpa", "4.0");
//        columnMax.put("rank", "20000");
//
//        dbApp.createTable(
//            "Student3",
//            "id",
//            columnTypes,
//            columnMin,
//            columnMax
//        );
//////
//////
//////        for (int i = 1; i < 20000; i += 1) {
//////            Hashtable<String, Object> values1 = new Hashtable<>();
//////            values1.put("id", i);
//////            values1.put("name", "mathew" + i);
//////            values1.put("birthdate", new Date("2000/10/10"));
//////            values1.put("salary", 1000 + i);
//////            values1.put("gpa", (i / 20000.0) * 4.0);
//////            values1.put("rank", i);
//////
//////            dbApp.insertIntoTable(
//////                "Student3",
//////                values1
//////            );
//////        }
//        for (int i = 1; i < 100; i += 1) {
//            Hashtable<String, Object> values1 = new Hashtable<>();
//            values1.put("id", 20000 + i);
//            values1.put("birthdate", new Date("2000/10/12"));
//            values1.put("salary", 1000);
//            if (i > 20) {
//                values1.put("gpa", 4.0);
//            } else {
//                values1.put("gpa", 3.0);
//            }
//            if (i > 20) {
//                values1.put("rank", 200);
//            } else {
//                values1.put("rank", 100);
//            }
//            if (i > 20) {
//                values1.put("name", "mathew1");
//            } else {
//                values1.put("name", "mathew2");
//            }
//
//
//            dbApp.insertIntoTable(
//                "Student3",
//                values1
//            );
//        }
////        double s = System.currentTimeMillis();
////        Hashtable<String, Object> values1 = new Hashtable<>();
////        values1.put("id", 25005);
////        values1.put("name", "mathew25000");
////        values1.put("birthdate", new Date("2000/10/11"));
////        values1.put("salary", 12345);
////        values1.put("gpa", 4.0);
////        values1.put("rank", 200);
////
////        dbApp.insertIntoTable(
////            "Student3",
////            values1
////        );
////        double e = System.currentTimeMillis();
////        System.out.println((e - s) / 1000.0);
//
////        dbApp.createIndex("Student3", new String[] {"name", "gpa", "birthdate"});
////        dbApp.createIndex("Student3", new String[] {"salary", "id", "rank"});
//        dbApp.createIndex("Student3", new String[] {"gpa", "rank", "name"});
//
//        double s = System.currentTimeMillis();
//        dbApp.deleteFromTable("Student3", new Hashtable<>() {{
//            put("rank", 100);
//            put("gpa", 3.0);
//            put("name", "mathew2");
//        }});
//        double e = System.currentTimeMillis();
//        System.out.println((e - s) / 1000.0);
//
////        double s = System.currentTimeMillis();
////        dbApp.updateTable("Student3", "20001", new Hashtable<>() {{
////            put("salary", 1000);
//////            put("rank", 100);
//////            put("gpa", 3.0);
////        }});
////        double e = System.currentTimeMillis();
////        System.out.println((e - s) / 1000.0);
////
//        double s1 = System.currentTimeMillis();
//        Iterator<Hashtable<String, Object>> tableIterator = dbApp.selectFromTable(
//            new SQLTerm[] {
//                new SQLTerm("Student3", "salary", "=", 1000)
////                new SQLTerm("Student3", "id", "<", 80),
////                new SQLTerm("Student3", "name", ">=", ""),
////                new SQLTerm("Student3", "salary", "=", 1031),
////                new SQLTerm("Student3", "rank", "=", 31),
////                new SQLTerm("Student3", "gpa", "=", 0.00155),
//
//
////                new SQLTerm("Student3", "id", "<=", 80),
////                new SQLTerm("Student3", "birthdate", ">=", new Date("2000/10/10")),
//////                new SQLTerm("Student3", "rank", ">=", 100),
//////                new SQLTerm("Student3", "salary", "=", 1000),
////                new SQLTerm("Student3", "gpa", "<=", 4.0),
//////                new SQLTerm("Student3", "name", ">=", "mathew"),
//            },
//            new String[] {
////                "and",
////                "and",
////                "and",
////                "or",
////                "and",
////                "and"
//            }
//        );
//
//        while (tableIterator.hasNext()) {
//            Hashtable<String, Object> row = tableIterator.next();
//            System.out.println(row.get("salary") + " " + row.get("name") + " " + row.get("id") + " " + row.get("gpa") + " " + row.get("birthdate"));
//        }
////
//        double e1 = System.currentTimeMillis();
//        System.out.println("Time: " + (e1 - s1) / 1000.0);
//        Iterator<Hashtable<String, Object>> tableIterator = dbApp.parseSQL(new StringBuffer("SELECT * FROM Student3 WHERE id <= 51 AND id > 1 AND gpa <= 4.0 AND gpa >= 3.0 AND name >= 'mathew' AND name <= 'mathew1' AND birthdate >= '2000-10-10' AND birthdate <= '2000-10-11' AND salary = 1000 AND rank >= 100 AND rank <= 200"));
//        while (tableIterator.hasNext()) {
//            Hashtable<String, Object> row = tableIterator.next();
//            System.out.println(row.get("salary") + " " + row.get("name") + " " + row.get("id") + " " + row.get("gpa") + " " + row.get("birthdate"));
//        }
//        Iterator<Row> tableIterator = dbApp.selectFromTableLinear(
//            dbApp.loadTable("Student2"),
//            new SQLTerm[] {
//                new SQLTerm("Student2", "id", ">", 50),
//            },
//            new String[] {}
//        );
//
//        while (tableIterator.hasNext()) {
//            Row row = tableIterator.next();
//            System.out.println(row.get("id"));
//        }


//        dbApp.createIndex("Student2", new String[] {"name", "id", "birthdate"});

//
//        Table table = dbApp.loadTable("Student");
//        Vector<PageIndexItem> pagesIndex = table.getPagesIndex();
//        SerializedPageManager pageManager = new SerializedPageManager("src/main/resources/Data/pages");
//
//        for (PageIndexItem pageIndex : pagesIndex) {
//            Page page = pageManager.loadPage(pageIndex.pageId);
//            System.out.println(pageIndex.pageMin + " Min ");
//            for (Row row : page.getRows()) {
//                System.out.println(
//                    row.get("id") + " " + row.get("name") + " " + row.get("birthdate"));
//            }
//            System.out.println();
//        }
//
//        System.out.println("=====================================");
//
//        Hashtable<String, Object> values = new Hashtable<>();
//
//        dbApp.deleteFromTable(
//            "Student",
//            values
//        );
//
//        table = dbApp.loadTable("Student");
//        pagesIndex = table.getPagesIndex();
//
//        for (dbms.pages.PageIndexItem pageIndex : pagesIndex) {
//            dbms.pages.Page page = pageManager.loadPage(pageIndex.pageId);
//            for (dbms.pages.Row row : page.getRows()) {
//                System.out.println(row.get("id") + " " + row.get("name") + " " + row.get("birthdate"));
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

