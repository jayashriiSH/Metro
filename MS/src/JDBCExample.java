import java.sql.*;

public class JDBCExample {
    public static void main(String[] args) {
        // MySQL credentials
        String url = "jdbc:mysql://localhost:3306/mydb"; // your database name
        String user = "root";                             // your MySQL username
        String password = "J1a2y3a$";               // your MySQL password

        try {
            // 1. Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Connect to Database
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to MySQL database successfully!");

            // 3. Create Statement
            Statement stmt = con.createStatement();

            // 4. Execute Query
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");

            // 5. Process Results
            System.out.println("ID\tName");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getString("name"));
            }

            // 6. Close Connection
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
