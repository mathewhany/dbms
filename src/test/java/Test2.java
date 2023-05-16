import dbms.DBApp;
import dbms.DBAppException;

import java.util.Iterator;

public class Test2 {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
        dbApp.init();

//        dbApp.parseSQL(new StringBuffer(
//            "CREATE TABLE Student3 (id int PRIMARY KEY , name varchar , birthdate date , salary int , gpa double , rank int)"
//        ));

//        dbApp.parseSQL(new StringBuffer(
//            "INSERT INTO Student3 (id, name, birthdate, salary, gpa, rank) VALUES (1, 'mathew1', '30-10-10', 1001, 0.00005, 1)"
//        ));

//        dbApp.parseSQL(new StringBuffer(
//            "INSERT INTO Student3 (id, name, birthdate, salary, gpa, rank) VALUES (2, 'mathew2', '1000-10-10', 1002, 0.0001, 2)"
//        ));

//        for (int i = 1; i < 20; i += 1) {
//            dbApp.parseSQL(new StringBuffer(
//                "INSERT INTO Student3 (id, name, birthdate, salary, gpa, rank) VALUES (" + i + ", 'mathew', '2000-10-10', 1000, 0.00005, " + i + ")"
//            ));
//        }

//        dbApp.parseSQL(new StringBuffer(
//            "CREATE INDEX idx1 ON Student3 (id, name, salary)"
//        ));

        dbApp.parseSQL(new StringBuffer(
            "DELETE FROM Student3 WHERE id = 2"
        ));

        Iterator iterator = dbApp.parseSQL(new StringBuffer(
            "SELECT * FROM Student3 WHERE id > 0"
        ));

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

    }
}
