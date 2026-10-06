import java.io.*;
import java.util.*;

class Room implements Serializable {
    private String roomNumber;
    private String category; // Standard, Deluxe, Suite
    private double pricePerNight;
    private boolean isAvailable;

    public Room(String roomNumber, String category, double pricePerNight, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " [" + category + "] - $" + pricePerNight + "/night | Status: " + (isAvailable ? "Available" : "Booked");
    }

    public String toFileString() {
        return roomNumber + "," + category + "," + pricePerNight + "," + isAvailable;
    }

    public static Room fromFileString(String line) {
        String[] parts = line.split(",");
        return new Room(parts[0], parts[1], Double.parseDouble(parts[2]), Boolean.parseBoolean(parts[3]));
    }
}

class Reservation implements Serializable {
    private String bookingId;
    private String customerName;
    private String roomNumber;
    private int nights;
    private double totalAmount;

    public Reservation(String bookingId, String customerName, String roomNumber, int nights, double totalAmount) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.nights = nights;
        this.totalAmount = totalAmount;
    }

    public String getBookingId() { return bookingId; }
    public String getRoomNumber() { return roomNumber; }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId + " | Guest: " + customerName + " | Room: " + roomNumber + 
               " | Duration: " + nights + " night(s) | Total Paid: $" + totalAmount;
    }

    public String toFileString() {
        return bookingId + "," + customerName + "," + roomNumber + "," + nights + "," + totalAmount;
    }

    public static Reservation fromFileString(String line) {
        String[] parts = line.split(",");
        return new Reservation(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), Double.parseDouble(parts[4]));
    }
}

// Main System Class
public class Main1 {
    private static final String ROOMS_FILE = "rooms.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";

    private List<Room> rooms = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);

    public Main1() {
        loadData();
        if (rooms.isEmpty()) {
            initializeDefaultRooms();
        }
    }

    private void initializeDefaultRooms() {
        rooms.add(new Room("101", "Standard", 80.0, true));
        rooms.add(new Room("102", "Standard", 80.0, true));
        rooms.add(new Room("201", "Deluxe", 150.0, true));
        rooms.add(new Room("202", "Deluxe", 150.0, true));
        rooms.add(new Room("301", "Suite", 300.0, true));
        saveData();
    }

    public void searchRooms() {
        System.out.println("\n--- Available Rooms ---");
        System.out.println("Filter by Category (Standard / Deluxe / Suite / All): ");
        String category = scanner.nextLine().trim();

        boolean found = false;
        for (Room room : rooms) {
            if (room.isAvailable() && (category.equalsIgnoreCase("All") || room.getCategory().equalsIgnoreCase(category))) {
                System.out.println(room);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No available rooms found matching your criteria.");
        }
    }

    public void bookRoom() {
        System.out.println("\n--- Make a Reservation ---");
        searchRooms();

        System.out.print("\nEnter Room Number to book: ");
        String roomNum = scanner.nextLine().trim();

        Room selectedRoom = null;
        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNum) && room.isAvailable()) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("Invalid room selection or room is currently booked.");
            return;
        }

        System.out.print("Enter Guest Name: ");
        String guestName = scanner.nextLine().trim();

        System.out.print("Enter Number of Nights: ");
        int nights = Integer.parseInt(scanner.nextLine().trim());

        double totalAmount = selectedRoom.getPricePerNight() * nights;
        System.out.printf("Total Cost: $%.2f\n", totalAmount);

        if (simulatePayment(totalAmount)) {
            String bookingId = "RES" + (1000 + new Random().nextInt(9000));
            selectedRoom.setAvailable(false);
            Reservation reservation = new Reservation(bookingId, guestName, selectedRoom.getRoomNumber(), nights, totalAmount);
            reservations.add(reservation);

            saveData();
            System.out.println("\nBooking Successful!");
            System.out.println(reservation);
        } else {
            System.out.println("Payment failed. Reservation canceled.");
        }
    }

    private boolean simulatePayment(double amount) {
        System.out.println("\n--- Payment Processing Simulation ---");
        System.out.printf("Processing payment of $%.2f...\n", amount);
        System.out.print("Enter Card Number (16 digits): ");
        String cardNumber = scanner.nextLine().trim();

        if (cardNumber.length() == 16 && cardNumber.matches("\\d+")) {
            System.out.println("Payment Authorized Successfully!");
            return true;
        } else {
            System.out.println("Invalid Card Number format!");
            return false;
        }
    }

    public void cancelReservation() {
        System.out.println("\n--- Cancel Reservation ---");
        System.out.print("Enter Booking ID: ");
        String bookingId = scanner.nextLine().trim();

        Reservation targetReservation = null;
        for (Reservation res : reservations) {
            if (res.getBookingId().equalsIgnoreCase(bookingId)) {
                targetReservation = res;
                break;
            }
        }

        if (targetReservation != null) {
            reservations.remove(targetReservation);
            for (Room room : rooms) {
                if (room.getRoomNumber().equals(targetReservation.getRoomNumber())) {
                    room.setAvailable(true);
                    break;
                }
            }
            saveData();
            System.out.println("Reservation " + bookingId + " has been successfully canceled.");
        } else {
            System.out.println("Booking ID not found.");
        }
    }

    public void viewBookingDetails() {
        System.out.println("\n--- Booking Details ---");
        if (reservations.isEmpty()) {
            System.out.println("No active bookings in the system.");
            return;
        }
        for (Reservation res : reservations) {
            System.out.println(res);
        }
    }


    private void saveData() {
        try (PrintWriter roomWriter = new PrintWriter(new FileWriter(ROOMS_FILE));
             PrintWriter resWriter = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {

            for (Room r : rooms) {
                roomWriter.println(r.toFileString());
            }

            for (Reservation res : reservations) {
                resWriter.println(res.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        File rFile = new File(ROOMS_FILE);
        File resFile = new File(RESERVATIONS_FILE);

        if (rFile.exists()) {
            try (Scanner s = new Scanner(rFile)) {
                while (s.hasNextLine()) {
                    rooms.add(Room.fromFileString(s.nextLine()));
                }
            } catch (Exception e) {
                System.out.println("Error loading rooms: " + e.getMessage());
            }
        }

        if (resFile.exists()) {
            try (Scanner s = new Scanner(resFile)) {
                while (s.hasNextLine()) {
                    reservations.add(Reservation.fromFileString(s.nextLine()));
                }
            } catch (Exception e) {
                System.out.println("Error loading reservations: " + e.getMessage());
            }
        }
    }


    public void displayMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("     HOTEL RESERVATION SYSTEM     ");
            System.out.println("=================================");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Make a Reservation");
            System.out.println("3. Cancel a Reservation");
            System.out.println("4. View All Booking Details");
            System.out.println("5. Exit");
            System.out.print("Select an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchRooms();
                    break;
                case "2":
                    bookRoom();
                    break;
                case "3":
                    cancelReservation();
                    break;
                case "4":
                    viewBookingDetails();
                    break;
                case "5":
                    System.out.println("Thank you for using the Hotel Reservation System. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        Main1 system = new Main1();
        system.displayMenu();
    }
}