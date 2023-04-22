import java.io.*;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Vector;

public class CsvLoader {
    private static final String DELIMITER = ",";

    /**
     * Saves the data from the database into a CSV file.
     *
     * @param filePath The name of the CSV file to save.
     * @param rows     A vector of hash tables, each hash table represents a row in the CSV file.
     * @throws IOException
     */
    public void save(String filePath, Vector<LinkedHashMap<String, String>> rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            if (rows.size() > 0) {
                StringBuilder headers = new StringBuilder();
                for (String key : rows.get(0).keySet()) {
                    headers.append(key).append(DELIMITER);
                }
                headers.setLength(headers.length() - DELIMITER.length());
                bw.write(headers.toString());
                bw.newLine();
            }
            for (LinkedHashMap<String, String> row : rows) {
                StringBuilder line = new StringBuilder();
                for (String key : row.keySet()) {
                    line.append(row.get(key)).append(DELIMITER);
                }
                line.setLength(line.length() - DELIMITER.length());
                bw.write(line.toString());
                bw.newLine();
            }
        }
    }

    /**
     * Loads the data from the CSV file into the database.
     *
     * @param filePath The name of the CSV file to load.
     * @return A vector of hash tables, each hash table represents a row in the CSV file.
     * @throws IOException
     */
    public Vector<LinkedHashMap<String, String>> load(String filePath) throws IOException {
        Vector<LinkedHashMap<String, String>> rows = new Vector<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(DELIMITER);
                if (headers == null) {
                    headers = fields;
                } else {
                    LinkedHashMap<String, String> row = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length && i < fields.length; i++) {
                        row.put(headers[i], fields[i]);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /**
     * Adds a new row to the CSV file.
     *
     * @param filePath The name of the CSV file to add the row to.
     * @param newRows  A vector of hash tables, each hash table represents a row to add to the CSV file.
     * @throws IOException
     */
    public void addRows(String filePath, Vector<Hashtable<String, String>> newRows) throws IOException {
        for (Hashtable<String, String> newRow : newRows) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
                StringBuilder line = new StringBuilder();
                for (String key : newRow.keySet()) {
                    line.append(newRow.get(key)).append(DELIMITER);
                }
                line.setLength(line.length() - DELIMITER.length());
                bw.write(line.toString());
                bw.newLine();
            }
        }
    }
}
