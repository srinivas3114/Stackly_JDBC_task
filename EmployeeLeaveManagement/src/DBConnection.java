import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    static Connection getConnection() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_leave_management",
                "root",
                "root"
            );

        } catch (Exception e) {

            System.out.println("Database Connection Error: " + e);
            return null;
        }
    }
}