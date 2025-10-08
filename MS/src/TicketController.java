import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;



public class TicketController {

    private final String DB_URL = "jdbc:mysql://localhost:3306/metrodb";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "J1a2y3a$"; // change to your password

    public TicketController() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("MySQL Driver not found");
            e.printStackTrace();
        }
    }

    // ----------------- Login / Signup -----------------
    public User login(String name, String phone) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT user_id, balance FROM users WHERE phone=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), name, phone, rs.getLong("balance"));
            } else {
                // New user with 0 balance
                String insert = "INSERT INTO users (name, phone, balance) VALUES (?,?,?)";
                ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setLong(3, 0); // default balance = 0
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                return new User(rs.getInt(1), name, phone, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ----------------- Add Money -----------------
    public long addBalance(int userId, long amount) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String update = "UPDATE users SET balance = balance + ? WHERE user_id=?";
            PreparedStatement ps = con.prepareStatement(update);
            ps.setLong(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            // Return new balance
            String query = "SELECT balance FROM users WHERE user_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong("balance");

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    private void generateQRCode(String data, String fileName) {
    try {
        int width = 300;
        int height = 300;
        String fileType = "png";
        Path path = FileSystems.getDefault().getPath(fileName);

        BitMatrix matrix = new MultiFormatWriter()
                .encode(data, BarcodeFormat.QR_CODE, width, height);

        MatrixToImageWriter.writeToPath(matrix, fileType, path);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // ----------------- Book Ticket -----------------
   public Ticket bookTicket(int userId, String source, String destination) {
    try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        // 1. Check balance
        String balanceQuery = "SELECT balance FROM users WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(balanceQuery);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            System.out.println("User not found");
            return null;
        }
        long balance = rs.getLong("balance");

        // 2. Calculate fare
        int fare = calculateFare(source, destination);
        if (balance < fare) {
            System.out.println("Insufficient balance! Please add money first.");
            return null;
        }

        // 3. Deduct fare
        String update = "UPDATE users SET balance = balance - ? WHERE user_id=?";
        ps = con.prepareStatement(update);
        ps.setInt(1, fare);
        ps.setInt(2, userId);
        ps.executeUpdate();

        // 4. Generate ticket
        String ticketCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String insertTicket = "INSERT INTO tickets (user_id, source, destination, fare, ticket_code, validity_hours) VALUES (?,?,?,?,?,?)";
        ps = con.prepareStatement(insertTicket);
        ps.setInt(1, userId);
        ps.setString(2, source);
        ps.setString(3, destination);
        ps.setInt(4, fare);
        ps.setString(5, ticketCode);
        ps.setInt(6, 6); // 6-hour validity
        ps.executeUpdate();

        // 5. Generate QR Code 🔳
        String qrData = "Ticket Code: " + ticketCode +
                        "\nUser ID: " + userId +
                        "\nFrom: " + source +
                        "\nTo: " + destination +
                        "\nFare: " + fare +
                        "\nValid for: 6 hours";

        generateQRCode(qrData, "ticket_" + ticketCode + ".png");

        System.out.println("✅ QR Code generated: ticket_" + ticketCode + ".png");

        return new Ticket(source, destination, fare, ticketCode, 6);

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


    // ----------------- Get Tickets -----------------
    public List<Ticket> getTickets(int userId) {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM tickets WHERE user_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tickets.add(new Ticket(
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getInt("fare"),
                        rs.getString("ticket_code"),
                        rs.getInt("validity_hours")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tickets;
    }

    // ----------------- Fare Calculation -----------------
    private int calculateFare(String source, String destination) {
        String[] stations = {
            "Wimco Nagar Depot","Wimco Nagar","Thiruvotriyur","Thiruvottriyur Theradi",
            "Kaladipet","Tollgate","New Washermenpet","Tondiarpet",
            "Sir Thiyagaraya College","Washermenpet","Mannadi","Highcourt",
            "Puratchi Thalaivar Dr. M.G. Ramachandran Central Metro",
            "Government Estate","LIC","Thousand Lights","AG – DMS",
            "Teynampet","Nandanam","Saidapet Metro","Little Mount","Guindy",
            "Arignar Anna Alandur Metro","OTA-Nanganallur Road","Meenambakkam",
            "Chennai International Airport"
        };
        int srcIndex = 0, destIndex = 0;
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].equalsIgnoreCase(source)) srcIndex = i;
            if (stations[i].equalsIgnoreCase(destination)) destIndex = i;
        }
        return 10 + Math.abs(destIndex - srcIndex) * 5;
    }

    // ----------------- Models -----------------
    public static class User {
        public int userId;
        public String name;
        public String phone;
        public long balance;
        public User(int userId, String name, String phone, long balance) {
            this.userId = userId; this.name = name; this.phone = phone; this.balance = balance;
        }
    }

    public static class Ticket {
        public String source, destination;
        public int fare, validityHours;
        public String ticketCode;
        public Ticket(String source, String destination, int fare, String ticketCode, int validityHours) {
            this.source = source;
            this.destination = destination;
            this.fare = fare;
            this.ticketCode = ticketCode;
            this.validityHours = validityHours;
        }
    }
}
