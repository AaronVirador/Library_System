package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens connections to MySQL.
 * The settings are read from config/db.properties (relative to the folder you run the program from).
 */
public class DBConnection {

    private static final String CONFIG_FILE = "config/db.properties";

    private static final Properties settings = loadSettings();

    static {
        DriverManager.setLoginTimeout(5); // don't freeze the window for long if MySQL is down
    }

    private static Properties loadSettings() {
        Properties p = new Properties();
        // defaults, used if config/db.properties is missing
        p.setProperty("db.url", "jdbc:mysql://localhost:3306/library_db"
                + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000");
        p.setProperty("db.user", "library_user");
        p.setProperty("db.password", "");

        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                p.load(reader);
            } catch (IOException e) {
                System.out.println("Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }
        return p;
    }

    /** A new connection. Always use it in try-with-resources so it gets closed. */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                settings.getProperty("db.url").trim(),
                settings.getProperty("db.user").trim(),
                settings.getProperty("db.password"));
    }

    /** Turns a database error into a short message that says what to fix. */
    public static String explain(Throwable error) {
        // look at the whole chain of causes
        for (Throwable t = error; t != null; t = t.getCause()) {
            String message = String.valueOf(t.getMessage());

            if (message.contains("No suitable driver")) {
                return "The MySQL driver (connector .jar) was not found.\n"
                        + "Put mysql-connector-j-*.jar in the lib folder and run the program with:\n"
                        + "java -cp \"bin:lib/*:resources\" ui.Main";
            }
            if (t instanceof SQLException) {
                SQLException sql = (SQLException) t;
                int code = sql.getErrorCode();
                String state = String.valueOf(sql.getSQLState());

                if (code == 1045) {
                    return "MySQL refused the username or password.\n"
                            + "Check db.user and db.password in " + CONFIG_FILE + ".";
                }
                if (code == 1049) {
                    return "The database 'library_db' does not exist yet.\n"
                            + "Create it with:  sudo mysql < databases/schema.sql";
                }
                if (code == 1146) {
                    return "A table is missing in the database.\n"
                            + "Create the tables with:  sudo mysql < databases/schema.sql";
                }
                if (state.startsWith("08")) {
                    return "Could not reach MySQL. Is it running?\n"
                            + "Start it with:  sudo systemctl start mysql\n"
                            + "Also check the host and port in " + CONFIG_FILE + ".";
                }
            }
        }
        return "A database error happened:\n" + error.getMessage();
    }
}
