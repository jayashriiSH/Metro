import java.sql.*;
import java.util.Scanner;

public class AdminController {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/metrodb";
    private static final String DB_USER = "root";        // your MySQL root user
    private static final String DB_PASSWORD = "J1a2y3a$"; // your MySQL password

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1. Admin login
        System.out.print("Enter admin username: ");
        String username = sc.nextLine();

        System.out.print("Enter admin password: ");
        String password = sc.nextLine();

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Check admin credentials
            String adminQuery = "SELECT * FROM admins WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(adminQuery);
            ps.setString(1, username);
            ps.setString(2, password);  // In production, store hashed passwords!
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\nAdmin login successful!\n");

                // Fetch all users
                System.out.println("=== All Users ===");
                rs = con.createStatement().executeQuery("SELECT * FROM users");
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %s, Phone: %s, Balance: %d\n",
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getLong("balance"));
                }

                // Fetch all tickets
                System.out.println("\n=== All Tickets ===");
                rs = con.createStatement().executeQuery("SELECT * FROM tickets");
                while (rs.next()) {
                    System.out.printf("Ticket Code: %s, Source: %s, Destination: %s, Fare: %d, User ID: %d\n",
                            rs.getString("ticket_code"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getInt("fare"),
                            rs.getInt("user_id"));
                }

                // Join users and tickets
                System.out.println("\n=== Tickets with User Info ===");
                String joinQuery = "SELECT t.ticket_code, t.source, t.destination, t.fare, u.name, u.phone " +
                        "FROM tickets t JOIN users u ON t.user_id = u.user_id";
                rs = con.createStatement().executeQuery(joinQuery);
                while (rs.next()) {
                    System.out.printf("Ticket: %s, From: %s, To: %s, Fare: %d, User: %s, Phone: %s\n",
                            rs.getString("ticket_code"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getInt("fare"),
                            rs.getString("name"),
                            rs.getString("phone"));
                }

            } else {
                System.out.println("Invalid admin username or password!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sc.close();
    }
}
