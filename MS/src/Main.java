import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TicketController controller = new TicketController();
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.print("Enter your phone: ");
        String phone = sc.nextLine();

        TicketController.User user = controller.login(name, phone);
        System.out.println("Logged in! Current Balance: " + user.balance);

        while (true) {
            System.out.println("\n1. Add Money\n2. Book Ticket\n3. Show Tickets\n4. Exit");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> {
                    addMoney(sc, controller, user);
                }

                case 2 -> {
                    bookTicketWithBalanceCheck(sc, controller, user);
                }

                case 3 -> {
                    showTickets(controller, user);
                }

                case 4 -> {
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                }

                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ----------------- Add Money -----------------
    private static void addMoney(Scanner sc, TicketController controller, TicketController.User user) {
        System.out.print("Enter amount to add: ");
        long amount = Long.parseLong(sc.nextLine());
        long newBalance = controller.addBalance(user.userId, amount);
        user.balance = newBalance;
        System.out.println("Balance updated! New Balance: " + newBalance);
    }

    // ----------------- Book Ticket with Balance Check -----------------
    private static void bookTicketWithBalanceCheck(Scanner sc, TicketController controller, TicketController.User user) {
        System.out.print("Source: ");
        String source = sc.nextLine();
        System.out.print("Destination: ");
        String destination = sc.nextLine();

        if (!isValidStation(source) || !isValidStation(destination)) {
            System.out.println("Invalid source or destination! Please select from the metro stations list.");
            return;
        }

        while (true) {
            TicketController.Ticket ticket = controller.bookTicket(user.userId, source, destination);
            if (ticket != null) {
                user.balance -= ticket.fare; // update local balance
                System.out.println("Ticket booked! Code: " + ticket.ticketCode + ", Fare: " + ticket.fare);
                break;
            } else {
                System.out.println("Insufficient balance. Your balance: " + user.balance);
                System.out.print("Enter amount to add: ");
                long amount = Long.parseLong(sc.nextLine());
                long newBalance = controller.addBalance(user.userId, amount);
                user.balance = newBalance;
                System.out.println("Balance updated! New Balance: " + newBalance);
            }
        }
    }

    // ----------------- Show Tickets -----------------
    private static void showTickets(TicketController controller, TicketController.User user) {
        List<TicketController.Ticket> tickets = controller.getTickets(user.userId);
        if (tickets.isEmpty()) {
            System.out.println("No tickets found!");
        } else {
            for (TicketController.Ticket t : tickets) {
                System.out.println("Ticket " + t.ticketCode + ": " + t.source + " -> " + t.destination + ", Fare: " + t.fare);
            }
        }
    }

    // ----------------- Station Validation -----------------
    private static boolean isValidStation(String station) {
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
        for (String s : stations) {
            if (s.equalsIgnoreCase(station)) return true;
        }
        return false;
    }
}
