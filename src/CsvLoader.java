import java.io.*;
import java.util.Hashtable;
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
    public void save(String filePath, Vector<Hashtable<String, String>> rows) throws
        DBAppException {
        Hashtable<String, Integer> headers = getHeaders(filePath);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            String[] line = new String[headers.size()];
            for (String header : headers.keySet()) {
                line[headers.get(header)] = header;
            }
            bw.write(String.join(DELIMITER, line));
            bw.newLine();

            for (Hashtable<String, String> row : rows) {
                line = new String[headers.size()];
                for (String header : headers.keySet()) {
                    line[headers.get(header)] = row.get(header);
                }
                bw.write(String.join(DELIMITER, line));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new DBAppException("Failed to save metadata file");
        }
    }

    /**
     * Loads the data from the CSV file into the database.
     *
     * @param filePath The name of the CSV file to load.
     * @return A vector of hash tables, each hash table represents a row in the CSV file.
     * @throws IOException
     */
    public Vector<Hashtable<String, String>> load(String filePath) throws DBAppException {
        Vector<Hashtable<String, String>> rows = new Vector<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(DELIMITER);
                if (headers == null) {
                    headers = fields;
                } else {
                    Hashtable<String, String> row = new Hashtable<>();
                    for (int i = 0; i < headers.length && i < fields.length; i++) {
                        row.put(headers[i], fields[i]);
                    }
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new DBAppException("Failed to load metadata file");
        }

        return rows;
    }

    private Hashtable<String, Integer> getHeaders(String filePath) throws DBAppException {
        Hashtable<String, Integer> headers = new Hashtable<>();

        if (!new File(filePath).exists()) {
            headers.put("Table Name", 0);
            headers.put("Column Name", 1);
            headers.put("Column Type", 2);
            headers.put("ClusteringKey", 3);
            headers.put("IndexName", 4);
            headers.put("IndexType", 5);
            headers.put("min", 6);
            headers.put("max", 7);

            return headers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String firstLine = reader.readLine();
            String[] fields = firstLine.split(DELIMITER);

            for (int i = 0; i < fields.length; i++) {
                headers.put(fields[i], i);
            }
        } catch (IOException e) {
            throw new DBAppException("Failed to load metadata file", e);
        }

        return headers;
    }
}
