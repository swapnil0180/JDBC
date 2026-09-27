import java.sql.*;
import java.util.Scanner;

public class HotelBookingSystem {

    // =====================================================
    // DATABASE DETAILS
    // =====================================================

    static String url = "jdbc:mysql://localhost:3306/";
    static String database = "HOTEL_BOOKING";
    static String username = "root";
    static String password = "Swapnil#123";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {

        try {

            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL server
            con = DriverManager.getConnection(
                    url,
                    username,
                    password
            );

            System.out.println("MySQL Connected Successfully!");

            // Create database
            createDatabase();

            // Connect to HOTEL_BOOKING database
            con.close();

            con = DriverManager.getConnection(
                    url + database,
                    username,
                    password
            );

            // Create tables
            createTables();

            // Insert rooms
            insertRooms();

            // Start menu
            int choice;

            do {

                System.out.println("\n");
                System.out.println("==============================================");
                System.out.println("        HOTEL ROOM BOOKING SYSTEM");
                System.out.println("==============================================");
                System.out.println("1.  Room Availability");
                System.out.println("2.  Add Customer");
                System.out.println("3.  View Customers");
                System.out.println("4.  Search Customer");
                System.out.println("5.  Book Room");
                System.out.println("6.  View All Bookings");
                System.out.println("7.  Search Booking");
                System.out.println("8.  Check-In");
                System.out.println("9.  Check-Out");
                System.out.println("10. Cancel Booking");
                System.out.println("11. Billing");
                System.out.println("12. Revenue Report");
                System.out.println("13. Exit");
                System.out.println("==============================================");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        roomAvailability();
                        break;

                    case 2:
                        addCustomer();
                        break;

                    case 3:
                        viewCustomers();
                        break;

                    case 4:
                        searchCustomer();
                        break;

                    case 5:
                        bookRoom();
                        break;

                    case 6:
                        viewBookings();
                        break;

                    case 7:
                        searchBooking();
                        break;

                    case 8:
                        checkIn();
                        break;

                    case 9:
                        checkOut();
                        break;

                    case 10:
                        cancelBooking();
                        break;

                    case 11:
                        billing();
                        break;

                    case 12:
                        revenueReport();
                        break;

                    case 13:
                        System.out.println(
                                "Thank you for using Hotel Booking System!"
                        );
                        break;

                    default:
                        System.out.println("Invalid choice!");

                }

            } while (choice != 13);

            con.close();
            sc.close();

        } catch (Exception e) {

            System.out.println(
                    "Database Error: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // CREATE DATABASE
    // =====================================================

    static void createDatabase() throws SQLException {

        Statement stmt = con.createStatement();

        String sql =
                "CREATE DATABASE IF NOT EXISTS HOTEL_BOOKING";

        stmt.executeUpdate(sql);

        stmt.close();

        System.out.println("Database ready.");
    }

    // =====================================================
    // CREATE TABLES
    // =====================================================

    static void createTables() throws SQLException {

        Statement stmt = con.createStatement();

        // ROOMS TABLE

        String roomTable =
                "CREATE TABLE IF NOT EXISTS rooms (" +
                "room_id INT PRIMARY KEY AUTO_INCREMENT," +
                "room_number INT UNIQUE," +
                "room_type VARCHAR(30)," +
                "price_per_day DECIMAL(10,2)," +
                "status VARCHAR(20)" +
                ")";

        stmt.executeUpdate(roomTable);


        // CUSTOMERS TABLE

        String customerTable =
                "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id INT PRIMARY KEY AUTO_INCREMENT," +
                "customer_name VARCHAR(50)," +
                "phone VARCHAR(15)," +
                "email VARCHAR(100)" +
                ")";

        stmt.executeUpdate(customerTable);


        // BOOKINGS TABLE

        String bookingTable =
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id INT PRIMARY KEY AUTO_INCREMENT," +
                "customer_id INT," +
                "room_id INT," +
                "booking_date DATE," +
                "check_in DATE," +
                "check_out DATE," +
                "status VARCHAR(20)," +
                "total_amount DECIMAL(10,2)," +
                "discount DECIMAL(10,2)," +
                "gst DECIMAL(10,2)," +
                "final_amount DECIMAL(10,2)," +
                "payment_status VARCHAR(20)," +
                "payment_method VARCHAR(20)," +
                "FOREIGN KEY(customer_id) " +
                "REFERENCES customers(customer_id)," +
                "FOREIGN KEY(room_id) " +
                "REFERENCES rooms(room_id)" +
                ")";

        stmt.executeUpdate(bookingTable);

        stmt.close();

        System.out.println("Tables ready.");
    }

    // =====================================================
    // INSERT DEFAULT ROOMS
    // =====================================================

    static void insertRooms() throws SQLException {

        String check =
                "SELECT COUNT(*) FROM rooms";

        PreparedStatement ps =
                con.prepareStatement(check);

        ResultSet rs = ps.executeQuery();

        rs.next();

        int count = rs.getInt(1);

        rs.close();
        ps.close();

        if (count == 0) {

            String sql =
                    "INSERT INTO rooms " +
                    "(room_number, room_type, price_per_day, status) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement roomPS =
                    con.prepareStatement(sql);

            addRoom(roomPS, 101, "Single", 1500);
            addRoom(roomPS, 102, "Double", 2500);
            addRoom(roomPS, 103, "Deluxe", 3500);
            addRoom(roomPS, 104, "Single", 1500);
            addRoom(roomPS, 105, "Suite", 5000);

            roomPS.close();

            System.out.println("Default rooms added.");
        }
    }

    static void addRoom(
            PreparedStatement ps,
            int roomNumber,
            String type,
            double price
    ) throws SQLException {

        ps.setInt(1, roomNumber);
        ps.setString(2, type);
        ps.setDouble(3, price);
        ps.setString(4, "Available");

        ps.executeUpdate();
    }

    // =====================================================
    // 1. ROOM AVAILABILITY
    // =====================================================

    static void roomAvailability() {

        try {

            String sql =
                    "SELECT * FROM rooms " +
                    "WHERE status = 'Available'";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== AVAILABLE ROOMS ==========");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "Room ID       : " +
                        rs.getInt("room_id")
                );

                System.out.println(
                        "Room Number   : " +
                        rs.getInt("room_number")
                );

                System.out.println(
                        "Room Type     : " +
                        rs.getString("room_type")
                );

                System.out.println(
                        "Price Per Day : Rs. " +
                        rs.getDouble("price_per_day")
                );

                System.out.println("-------------------------------------");
            }

            if (!found) {
                System.out.println("No rooms available.");
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 2. ADD CUSTOMER
    // =====================================================

    static void addCustomer() {

        try {

            sc.nextLine();

            System.out.print("Enter customer name: ");
            String name = sc.nextLine();

            System.out.print("Enter phone: ");
            String phone = sc.nextLine();

            System.out.print("Enter email: ");
            String email = sc.nextLine();

            String sql =
                    "INSERT INTO customers " +
                    "(customer_name, phone, email) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);

            ps.executeUpdate();

            System.out.println(
                    "Customer added successfully!"
            );

            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 3. VIEW CUSTOMERS
    // =====================================================

    static void viewCustomers() {

        try {

            String sql =
                    "SELECT * FROM customers";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== CUSTOMERS ==========");

            while (rs.next()) {

                System.out.println(
                        "Customer ID : " +
                        rs.getInt("customer_id")
                );

                System.out.println(
                        "Name        : " +
                        rs.getString("customer_name")
                );

                System.out.println(
                        "Phone       : " +
                        rs.getString("phone")
                );

                System.out.println(
                        "Email       : " +
                        rs.getString("email")
                );

                System.out.println("-------------------------------");
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 4. SEARCH CUSTOMER
    // =====================================================

    static void searchCustomer() {

        try {

            System.out.print("Enter customer ID: ");
            int id = sc.nextInt();

            String sql =
                    "SELECT * FROM customers " +
                    "WHERE customer_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "\nCustomer ID : " +
                        rs.getInt("customer_id")
                );

                System.out.println(
                        "Name        : " +
                        rs.getString("customer_name")
                );

                System.out.println(
                        "Phone       : " +
                        rs.getString("phone")
                );

                System.out.println(
                        "Email       : " +
                        rs.getString("email")
                );

            } else {

                System.out.println(
                        "Customer not found."
                );
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 5. BOOK ROOM
    // =====================================================
    
    
    static void bookRoom() {

        boolean transactionStarted = false;

        try {

            System.out.print("Enter customer ID: ");
            int customerId = sc.nextInt();

            System.out.print("Enter room ID: ");
            int roomId = sc.nextInt();

            sc.nextLine();

            System.out.print("Enter check-in date (YYYY-MM-DD): ");
            String checkInString = sc.nextLine();

            System.out.print("Enter check-out date (YYYY-MM-DD): ");
            String checkOutString = sc.nextLine();

            Date checkIn = Date.valueOf(checkInString);
            Date checkOut = Date.valueOf(checkOutString);

            // Calculate number of days
            long difference =
                    checkOut.getTime() - checkIn.getTime();

            long days =
                    difference / (1000 * 60 * 60 * 24);

            if (days <= 0) {
                System.out.println(
                        "Check-out must be after check-in."
                );
                return;
            }

            // Start transaction
            con.setAutoCommit(false);
            transactionStarted = true;


            // =================================================
            // CHECK CUSTOMER
            // =================================================

            String customerSQL =
                    "SELECT customer_id " +
                    "FROM customers " +
                    "WHERE customer_id = ?";

            PreparedStatement customerPS =
                    con.prepareStatement(customerSQL);

            customerPS.setInt(1, customerId);

            ResultSet customerRS =
                    customerPS.executeQuery();

            if (!customerRS.next()) {

                System.out.println(
                        "Customer does not exist."
                );

                customerRS.close();
                customerPS.close();

                con.rollback();
                con.setAutoCommit(true);
                transactionStarted = false;

                return;
            }

            customerRS.close();
            customerPS.close();


            // =================================================
            // CHECK ROOM
            // =================================================

            String roomSQL =
                    "SELECT price_per_day, status " +
                    "FROM rooms " +
                    "WHERE room_id = ?";

            PreparedStatement roomPS =
                    con.prepareStatement(roomSQL);

            roomPS.setInt(1, roomId);

            ResultSet roomRS =
                    roomPS.executeQuery();

            if (!roomRS.next()) {

                System.out.println(
                        "Room does not exist."
                );

                roomRS.close();
                roomPS.close();

                con.rollback();
                con.setAutoCommit(true);
                transactionStarted = false;

                return;
            }

            double price =
                    roomRS.getDouble("price_per_day");

            String status =
                    roomRS.getString("status");

            roomRS.close();
            roomPS.close();


            if (!"Available".equals(status)) {

                System.out.println(
                        "Room is not available."
                );

                con.rollback();
                con.setAutoCommit(true);
                transactionStarted = false;

                return;
            }


            // =================================================
            // CALCULATE BILL
            // =================================================

            double roomAmount = days * price;

            double discount = 0;

            if (roomAmount >= 10000) {
                discount = roomAmount * 0.10;
            }

            double afterDiscount =
                    roomAmount - discount;

            double gst =
                    afterDiscount * 0.18;

            double finalAmount =
                    afterDiscount + gst;


            // =================================================
            // PAYMENT
            // =================================================

            System.out.println("\nPayment Options:");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. UPI");

            System.out.print("Enter payment method: ");
            int paymentChoice = sc.nextInt();

            String paymentMethod;
            String paymentStatus;

            if (paymentChoice == 1) {

                paymentMethod = "Cash";
                paymentStatus = "Paid";

            } else if (paymentChoice == 2) {

                paymentMethod = "Card";
                paymentStatus = "Paid";

            } else if (paymentChoice == 3) {

                paymentMethod = "UPI";
                paymentStatus = "Paid";

            } else {

                paymentMethod = "Pending";
                paymentStatus = "Pending";
            }


            // =================================================
            // INSERT BOOKING
            // =================================================

            String bookingSQL =
                    "INSERT INTO bookings " +
                    "(customer_id, room_id, booking_date, " +
                    "check_in, check_out, status, " +
                    "total_amount, discount, gst, " +
                    "final_amount, payment_status, " +
                    "payment_method) " +
                    "VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement bookingPS =
                    con.prepareStatement(
                            bookingSQL,
                            Statement.RETURN_GENERATED_KEYS
                    );

            bookingPS.setInt(1, customerId);
            bookingPS.setInt(2, roomId);
            bookingPS.setDate(3, checkIn);
            bookingPS.setDate(4, checkOut);
            bookingPS.setString(5, "Booked");
            bookingPS.setDouble(6, roomAmount);
            bookingPS.setDouble(7, discount);
            bookingPS.setDouble(8, gst);
            bookingPS.setDouble(9, finalAmount);
            bookingPS.setString(10, paymentStatus);
            bookingPS.setString(11, paymentMethod);

            bookingPS.executeUpdate();


            // =================================================
            // GET BOOKING ID
            // =================================================

            ResultSet generatedKeys =
                    bookingPS.getGeneratedKeys();

            int bookingId = 0;

            if (generatedKeys.next()) {
                bookingId =
                        generatedKeys.getInt(1);
            }

            generatedKeys.close();
            bookingPS.close();


            // =================================================
            // UPDATE ROOM STATUS
            // =================================================

            String updateRoom =
                    "UPDATE rooms " +
                    "SET status = 'Booked' " +
                    "WHERE room_id = ?";

            PreparedStatement updatePS =
                    con.prepareStatement(updateRoom);

            updatePS.setInt(1, roomId);

            updatePS.executeUpdate();

            updatePS.close();


            // =================================================
            // COMMIT
            // =================================================

            con.commit();

            con.setAutoCommit(true);
            transactionStarted = false;


            // =================================================
            // DISPLAY BOOKING
            // =================================================

            System.out.println("\n");
            System.out.println("================================");
            System.out.println("       BOOKING SUCCESSFUL");
            System.out.println("================================");

            System.out.println(
                    "Booking ID       : " + bookingId
            );

            System.out.println(
                    "Number of Days   : " + days
            );

            System.out.println(
                    "Room Amount      : Rs. " + roomAmount
            );

            System.out.println(
                    "Discount         : Rs. " + discount
            );

            System.out.println(
                    "GST              : Rs. " + gst
            );

            System.out.println(
                    "Final Amount     : Rs. " + finalAmount
            );

            System.out.println(
                    "Payment Status   : " + paymentStatus
            );

            System.out.println(
                    "Payment Method   : " + paymentMethod
            );

            System.out.println("================================");


        } catch (Exception e) {

            // Only rollback if transaction was actually started
            if (transactionStarted) {

                try {

                    con.rollback();

                } catch (SQLException rollbackError) {

                    System.out.println(
                            "Rollback failed: " +
                            rollbackError.getMessage()
                    );
                }

                try {

                    con.setAutoCommit(true);

                } catch (SQLException autoCommitError) {

                    System.out.println(
                            "Could not restore AutoCommit: " +
                            autoCommitError.getMessage()
                    );
                }
            }

            System.out.println(
                    "Booking failed: " +
                    e.getClass().getSimpleName() +
                    " - " +
                    e.getMessage()
            );
        }
    }

   

    // =====================================================
    // 6. VIEW ALL BOOKINGS
    // =====================================================

    static void viewBookings() {

        try {

            String sql =
                    "SELECT b.booking_id, " +
                    "c.customer_name, " +
                    "r.room_number, " +
                    "b.check_in, " +
                    "b.check_out, " +
                    "b.status, " +
                    "b.final_amount, " +
                    "b.payment_status " +
                    "FROM bookings b " +
                    "JOIN customers c " +
                    "ON b.customer_id = c.customer_id " +
                    "JOIN rooms r " +
                    "ON b.room_id = r.room_id";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery();

            System.out.println(
                    "\n========== ALL BOOKINGS =========="
            );

            while (rs.next()) {

                System.out.println(
                        "Booking ID     : " +
                        rs.getInt("booking_id")
                );

                System.out.println(
                        "Customer       : " +
                        rs.getString("customer_name")
                );

                System.out.println(
                        "Room Number    : " +
                        rs.getInt("room_number")
                );

                System.out.println(
                        "Check-In       : " +
                        rs.getDate("check_in")
                );

                System.out.println(
                        "Check-Out      : " +
                        rs.getDate("check_out")
                );

                System.out.println(
                        "Status         : " +
                        rs.getString("status")
                );

                System.out.println(
                        "Final Amount   : Rs. " +
                        rs.getDouble("final_amount")
                );

                System.out.println(
                        "Payment        : " +
                        rs.getString("payment_status")
                );

                System.out.println(
                        "--------------------------------"
                );
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 7. SEARCH BOOKING
    // =====================================================

    static void searchBooking() {

        try {

            System.out.print("Enter booking ID: ");
            int id = sc.nextInt();

            String sql =
                    "SELECT b.*, " +
                    "c.customer_name, " +
                    "c.phone, " +
                    "r.room_number, " +
                    "r.room_type, " +
                    "r.price_per_day " +
                    "FROM bookings b " +
                    "JOIN customers c " +
                    "ON b.customer_id = c.customer_id " +
                    "JOIN rooms r " +
                    "ON b.room_id = r.room_id " +
                    "WHERE b.booking_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "\n========== BOOKING =========="
                );

                System.out.println(
                        "Booking ID    : " +
                        rs.getInt("booking_id")
                );

                System.out.println(
                        "Customer      : " +
                        rs.getString("customer_name")
                );

                System.out.println(
                        "Phone         : " +
                        rs.getString("phone")
                );

                System.out.println(
                        "Room Number   : " +
                        rs.getInt("room_number")
                );

                System.out.println(
                        "Room Type     : " +
                        rs.getString("room_type")
                );

                System.out.println(
                        "Check-In      : " +
                        rs.getDate("check_in")
                );

                System.out.println(
                        "Check-Out     : " +
                        rs.getDate("check_out")
                );

                System.out.println(
                        "Status        : " +
                        rs.getString("status")
                );

                System.out.println(
                        "Final Amount  : Rs. " +
                        rs.getDouble("final_amount")
                );

                System.out.println(
                        "Payment       : " +
                        rs.getString("payment_status")
                );

            } else {

                System.out.println(
                        "Booking not found."
                );
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 8. CHECK-IN
    // =====================================================

    static void checkIn() {

        try {

            System.out.print("Enter booking ID: ");
            int bookingId = sc.nextInt();

            String sql =
                    "UPDATE bookings " +
                    "SET status = 'Checked-In' " +
                    "WHERE booking_id = ? " +
                    "AND status = 'Booked'";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, bookingId);

            int result =
                    ps.executeUpdate();

            if (result > 0) {

                System.out.println(
                        "Customer checked-in successfully."
                );

            } else {

                System.out.println(
                        "Invalid booking or customer already checked-in."
                );
            }

            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 9. CHECK-OUT
    // =====================================================

    static void checkOut() {

        try {

            System.out.print("Enter booking ID: ");
            int bookingId = sc.nextInt();

            con.setAutoCommit(false);


            // Find room
            String findRoom =
                    "SELECT room_id " +
                    "FROM bookings " +
                    "WHERE booking_id = ? " +
                    "AND status = 'Checked-In'";

            PreparedStatement findPS =
                    con.prepareStatement(findRoom);

            findPS.setInt(1, bookingId);

            ResultSet rs =
                    findPS.executeQuery();

            if (!rs.next()) {

                System.out.println(
                        "Booking not found or customer is not checked-in."
                );

                rs.close();
                findPS.close();

                con.rollback();
                con.setAutoCommit(true);

                return;
            }

            int roomId =
                    rs.getInt("room_id");

            rs.close();
            findPS.close();


            // Update booking
            String bookingSQL =
                    "UPDATE bookings " +
                    "SET status = 'Checked-Out' " +
                    "WHERE booking_id = ?";

            PreparedStatement bookingPS =
                    con.prepareStatement(bookingSQL);

            bookingPS.setInt(1, bookingId);

            bookingPS.executeUpdate();

            bookingPS.close();


            // Make room available
            String roomSQL =
                    "UPDATE rooms " +
                    "SET status = 'Available' " +
                    "WHERE room_id = ?";

            PreparedStatement roomPS =
                    con.prepareStatement(roomSQL);

            roomPS.setInt(1, roomId);

            roomPS.executeUpdate();

            roomPS.close();


            con.commit();

            con.setAutoCommit(true);

            System.out.println(
                    "Customer checked-out successfully."
            );

            System.out.println(
                    "Room is now available."
            );

        } catch (SQLException e) {

            try {

                con.rollback();
                con.setAutoCommit(true);

            } catch (SQLException ex) {

                System.out.println(ex.getMessage());
            }

            System.out.println(
                    "Check-out failed: " +
                    e.getMessage()
            );
        }
    }

    // =====================================================
    // 10. CANCEL BOOKING
    // =====================================================

    static void cancelBooking() {

        try {

            System.out.print("Enter booking ID: ");
            int bookingId = sc.nextInt();

            con.setAutoCommit(false);


            // Find room
            String findSQL =
                    "SELECT room_id " +
                    "FROM bookings " +
                    "WHERE booking_id = ? " +
                    "AND status = 'Booked'";

            PreparedStatement findPS =
                    con.prepareStatement(findSQL);

            findPS.setInt(1, bookingId);

            ResultSet rs =
                    findPS.executeQuery();

            if (!rs.next()) {

                System.out.println(
                        "Booking cannot be cancelled."
                );

                rs.close();
                findPS.close();

                con.rollback();
                con.setAutoCommit(true);

                return;
            }

            int roomId =
                    rs.getInt("room_id");

            rs.close();
            findPS.close();


            // Cancel booking
            String cancelSQL =
                    "UPDATE bookings " +
                    "SET status = 'Cancelled' " +
                    "WHERE booking_id = ?";

            PreparedStatement cancelPS =
                    con.prepareStatement(cancelSQL);

            cancelPS.setInt(1, bookingId);

            cancelPS.executeUpdate();

            cancelPS.close();


            // Make room available
            String roomSQL =
                    "UPDATE rooms " +
                    "SET status = 'Available' " +
                    "WHERE room_id = ?";

            PreparedStatement roomPS =
                    con.prepareStatement(roomSQL);

            roomPS.setInt(1, roomId);

            roomPS.executeUpdate();

            roomPS.close();


            con.commit();

            con.setAutoCommit(true);

            System.out.println(
                    "Booking cancelled successfully."
            );

            System.out.println(
                    "Room is now available."
            );

        } catch (SQLException e) {

            try {

                con.rollback();
                con.setAutoCommit(true);

            } catch (SQLException ex) {

                System.out.println(ex.getMessage());
            }

            System.out.println(
                    "Cancellation failed: " +
                    e.getMessage()
            );
        }
    }

    // =====================================================
    // 11. BILLING
    // =====================================================

    static void billing() {

        try {

            System.out.print("Enter booking ID: ");
            int bookingId = sc.nextInt();

            String sql =
                    "SELECT b.*, " +
                    "c.customer_name, " +
                    "c.phone, " +
                    "r.room_number, " +
                    "r.room_type, " +
                    "r.price_per_day " +
                    "FROM bookings b " +
                    "JOIN customers c " +
                    "ON b.customer_id = c.customer_id " +
                    "JOIN rooms r " +
                    "ON b.room_id = r.room_id " +
                    "WHERE b.booking_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, bookingId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                Date checkIn =
                        rs.getDate("check_in");

                Date checkOut =
                        rs.getDate("check_out");

                long difference =
                        checkOut.getTime() -
                        checkIn.getTime();

                long days =
                        difference /
                        (1000 * 60 * 60 * 24);

                System.out.println("\n");
                System.out.println(
                        "======================================"
                );
                System.out.println(
                        "             HOTEL BILL"
                );
                System.out.println(
                        "======================================"
                );

                System.out.println(
                        "Booking ID       : " +
                        rs.getInt("booking_id")
                );

                System.out.println(
                        "Customer         : " +
                        rs.getString("customer_name")
                );

                System.out.println(
                        "Phone            : " +
                        rs.getString("phone")
                );

                System.out.println(
                        "Room Number      : " +
                        rs.getInt("room_number")
                );

                System.out.println(
                        "Room Type        : " +
                        rs.getString("room_type")
                );

                System.out.println(
                        "Check-In         : " +
                        checkIn
                );

                System.out.println(
                        "Check-Out        : " +
                        checkOut
                );

                System.out.println(
                        "Number of Days   : " +
                        days
                );

                System.out.println(
                        "Price Per Day    : Rs. " +
                        rs.getDouble("price_per_day")
                );

                System.out.println(
                        "Room Amount      : Rs. " +
                        rs.getDouble("total_amount")
                );

                System.out.println(
                        "Discount         : Rs. " +
                        rs.getDouble("discount")
                );

                System.out.println(
                        "GST              : Rs. " +
                        rs.getDouble("gst")
                );

                System.out.println(
                        "Final Amount     : Rs. " +
                        rs.getDouble("final_amount")
                );

                System.out.println(
                        "Payment Status   : " +
                        rs.getString("payment_status")
                );

                System.out.println(
                        "Payment Method   : " +
                        rs.getString("payment_method")
                );

                System.out.println(
                        "Booking Status   : " +
                        rs.getString("status")
                );

                System.out.println(
                        "======================================"
                );

            } else {

                System.out.println(
                        "Booking not found."
                );
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    // =====================================================
    // 12. REVENUE REPORT
    // =====================================================

    static void revenueReport() {

        try {

            String sql =
                    "SELECT " +
                    "COUNT(*) AS total_bookings, " +
                    "SUM(final_amount) AS revenue " +
                    "FROM bookings " +
                    "WHERE status <> 'Cancelled'";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                int totalBookings =
                        rs.getInt("total_bookings");

                double revenue =
                        rs.getDouble("revenue");

                System.out.println("\n");
                System.out.println(
                        "================================"
                );

                System.out.println(
                        "         REVENUE REPORT"
                );

                System.out.println(
                        "================================"
                );

                System.out.println(
                        "Total Bookings : " +
                        totalBookings
                );

                System.out.println(
                        "Total Revenue  : Rs. " +
                        revenue
                );

                System.out.println(
                        "================================"
                );
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }
}