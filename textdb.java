import java.sql.Connection;
import java.sql.DriverManager;

public class textdb {

    public static void main(String[] args) {

        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a connection
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/college",
                "root",
                "Swapnil#123"
            );

            System.out.println("Connection successful!");

            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}