import java.util.Hashtable;

public class Test {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();

        dbApp.createTable(
            "Student",
            "id",
            new Hashtable<String, String>() {{
                put("id", "java.lang.Integer");
                put("name", "java.lang.String");
                put("gpa", "java.lang.Double");
            }},
            new Hashtable<String, String>() {{
                put("id", "0");
                put("name", "A");
                put("gpa", "0.0");
            }},
            new Hashtable<String, String>() {{
                put("id", "100");
                put("name", "Z");
                put("gpa", "4.0");
            }}
        );
    }
}
