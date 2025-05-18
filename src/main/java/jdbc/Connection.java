/*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {
    public static void main(String[] args) {
        // Database connection parameters
        String jdbcUrl = "jdbc:mysql://localhost:3306/onlinebookstore";
        String username = "root";
        String password = "password1";

        try {
            // Load the JDBC driver (optional for modern JDBC, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Connection successful!");

            // Close the connection
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }
} */
