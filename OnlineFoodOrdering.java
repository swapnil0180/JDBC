import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class OnlineFoodOrdering {

    // ================= DATABASE CONNECTION =================

    static final String URL =
            "jdbc:mysql://localhost:3306/food_ordering";

    static final String USER = "root";

    static final String PASSWORD = "Swapnil#123"; 


    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }


    // ================= CUSTOMER REGISTRATION =================

    static void registerCustomer(Scanner sc) {

        System.out.println("\n===== CUSTOMER REGISTRATION =====");

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        System.out.print("Enter Phone: ");
        String phone = sc.nextLine();

        System.out.print("Enter Address: ");
        String address = sc.nextLine();

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String sql =
                "INSERT INTO customers " +
                "(name, email, phone, address, password) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, password);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(
                        "Registration successful!"
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Registration failed."
            );

            System.out.println(e.getMessage());
        }
    }


    // ================= CUSTOMER LOGIN =================

    static int login(Scanner sc) {

        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String sql =
                "SELECT customer_id, name " +
                "FROM customers " +
                "WHERE email = ? AND password = ?";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int customerId =
                        rs.getInt("customer_id");

                String name =
                        rs.getString("name");

                System.out.println(
                        "\nWelcome, " + name + "!"
                );

                return customerId;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.println(
                "Invalid email or password."
        );

        return -1;
    }


    // ================= DISPLAY MENU =================

    static void displayMenu() {

        String sql =
                "SELECT item_id, item_name, " +
                "description, category, price " +
                "FROM menu " +
                "WHERE available = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println(
                    "\n================ FOOD MENU ================"
            );

            System.out.printf(
                    "%-5s %-25s %-15s %-10s%n",
                    "ID",
                    "ITEM",
                    "CATEGORY",
                    "PRICE"
            );

            System.out.println(
                    "-------------------------------------------"
            );

            while (rs.next()) {

                int id =
                        rs.getInt("item_id");

                String item =
                        rs.getString("item_name");

                String category =
                        rs.getString("category");

                double price =
                        rs.getDouble("price");

                System.out.printf(
                        "%-5d %-25s %-15s Rs.%-8.2f%n",
                        id,
                        item,
                        category,
                        price
                );
            }

            System.out.println(
                    "==========================================="
            );

        } catch (SQLException e) {

            System.out.println(
                    "Unable to display menu."
            );

            System.out.println(e.getMessage());
        }
    }


    // ================= PLACE ORDER =================

    static int placeOrder(
            int customerId,
            Scanner sc) {

        Connection con = null;

        try {

            con = getConnection();

            // Start transaction
            con.setAutoCommit(false);

            displayMenu();

            System.out.print(
                    "\nEnter number of different items: "
            );

            int numberOfItems = sc.nextInt();

            if (numberOfItems <= 0) {

                System.out.println(
                        "Invalid number of items."
                );

                con.rollback();
                return -1;
            }


            Map<Integer, Integer> cart =
                    new HashMap<>();


            // Get cart details
            for (int i = 0;
                 i < numberOfItems;
                 i++) {

                System.out.print(
                        "Enter Item ID: "
                );

                int itemId = sc.nextInt();

                System.out.print(
                        "Enter Quantity: "
                );

                int quantity = sc.nextInt();

                if (quantity <= 0) {

                    System.out.println(
                            "Invalid quantity."
                    );

                    con.rollback();
                    return -1;
                }

                cart.put(itemId, quantity);
            }


            // ================= INSERT ORDER =================

            String orderSQL =
                    "INSERT INTO orders " +
                    "(customer_id, status, total_amount) " +
                    "VALUES (?, 'PLACED', 0)";

            PreparedStatement orderPS =
                    con.prepareStatement(
                            orderSQL,
                            Statement.RETURN_GENERATED_KEYS
                    );

            orderPS.setInt(
                    1,
                    customerId
            );

            orderPS.executeUpdate();


            // Get generated Order ID
            ResultSet keys =
                    orderPS.getGeneratedKeys();

            if (!keys.next()) {

                throw new SQLException(
                        "Order ID not generated."
                );
            }

            int orderId =
                    keys.getInt(1);


            // ================= INSERT ORDER ITEMS =================

            String itemSQL =
                    "INSERT INTO order_items " +
                    "(order_id, item_id, quantity, price) " +
                    "SELECT ?, ?, ?, price " +
                    "FROM menu " +
                    "WHERE item_id = ? " +
                    "AND available = TRUE";

            PreparedStatement itemPS =
                    con.prepareStatement(itemSQL);


            for (Map.Entry<Integer, Integer> entry
                    : cart.entrySet()) {

                int itemId =
                        entry.getKey();

                int quantity =
                        entry.getValue();


                itemPS.setInt(
                        1,
                        orderId
                );

                itemPS.setInt(
                        2,
                        itemId
                );

                itemPS.setInt(
                        3,
                        quantity
                );

                itemPS.setInt(
                        4,
                        itemId
                );


                int rows =
                        itemPS.executeUpdate();


                if (rows == 0) {

                    throw new SQLException(
                            "Item ID " +
                            itemId +
                            " is unavailable."
                    );
                }
            }


            // ================= CALCULATE TOTAL =================

            String totalSQL =
                    "SELECT SUM(quantity * price) " +
                    "AS total " +
                    "FROM order_items " +
                    "WHERE order_id = ?";

            PreparedStatement totalPS =
                    con.prepareStatement(totalSQL);

            totalPS.setInt(
                    1,
                    orderId
            );

            ResultSet totalRS =
                    totalPS.executeQuery();

            double subtotal = 0;

            if (totalRS.next()) {

                subtotal =
                        totalRS.getDouble("total");
            }


            // ================= TAX =================

            double tax =
                    subtotal * 0.05;

            double deliveryCharge =
                    40;

            double total =
                    subtotal +
                    tax +
                    deliveryCharge;


            // ================= UPDATE ORDER =================

            String updateSQL =
                    "UPDATE orders " +
                    "SET total_amount = ? " +
                    "WHERE order_id = ?";

            PreparedStatement updatePS =
                    con.prepareStatement(updateSQL);

            updatePS.setDouble(
                    1,
                    total
            );

            updatePS.setInt(
                    2,
                    orderId
            );

            updatePS.executeUpdate();


            // ================= COMMIT =================

            con.commit();


            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "       ORDER SUCCESSFUL"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(
                    "Order ID : " + orderId
            );

            System.out.printf(
                    "Subtotal : Rs. %.2f%n",
                    subtotal
            );

            System.out.printf(
                    "Tax      : Rs. %.2f%n",
                    tax
            );

            System.out.printf(
                    "Delivery : Rs. %.2f%n",
                    deliveryCharge
            );

            System.out.printf(
                    "Total    : Rs. %.2f%n",
                    total
            );

            System.out.println(
                    "================================"
            );


            return orderId;


        } catch (Exception e) {

            // ================= ROLLBACK =================

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }


            System.out.println(
                    "\nOrder failed."
            );

            System.out.println(
                    "Transaction rolled back."
            );

            System.out.println(
                    e.getMessage()
            );

            return -1;


        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);

                    con.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // ================= ORDER HISTORY =================

    static void orderHistory(int customerId) {

        String sql =
                "SELECT order_id, order_date, " +
                "status, total_amount " +
                "FROM orders " +
                "WHERE customer_id = ? " +
                "ORDER BY order_date DESC";


        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );

            ResultSet rs =
                    ps.executeQuery();


            System.out.println(
                    "\n========== ORDER HISTORY =========="
            );


            boolean found = false;


            while (rs.next()) {

                found = true;

                System.out.println(
                        "Order ID : " +
                        rs.getInt("order_id")
                );

                System.out.println(
                        "Date     : " +
                        rs.getTimestamp("order_date")
                );

                System.out.println(
                        "Status   : " +
                        rs.getString("status")
                );

                System.out.printf(
                        "Total    : Rs. %.2f%n",
                        rs.getDouble("total_amount")
                );

                System.out.println(
                        "-----------------------------------"
                );
            }


            if (!found) {

                System.out.println(
                        "No orders found."
                );
            }


        } catch (SQLException e) {

            System.out.println(
                    e.getMessage()
            );
        }
    }


    // ================= BILL GENERATION =================

    static void generateBill(
            int orderId,
            int customerId) {


        String sql =
                "SELECT o.order_id, " +
                "o.order_date, " +
                "m.item_name, " +
                "oi.quantity, " +
                "oi.price, " +
                "(oi.quantity * oi.price) " +
                "AS item_total " +

                "FROM orders o " +

                "JOIN order_items oi " +
                "ON o.order_id = oi.order_id " +

                "JOIN menu m " +
                "ON oi.item_id = m.item_id " +

                "WHERE o.order_id = ? " +
                "AND o.customer_id = ?";


        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    orderId
            );

            ps.setInt(
                    2,
                    customerId
            );


            ResultSet rs =
                    ps.executeQuery();


            boolean found = false;

            double subtotal = 0;


            System.out.println(
                    "\n=============================================="
            );

            System.out.println(
                    "              ABC RESTAURANT"
            );

            System.out.println(
                    "                 BILL"
            );

            System.out.println(
                    "=============================================="
            );


            while (rs.next()) {

                if (!found) {

                    found = true;

                    System.out.println(
                            "Order ID : " +
                            rs.getInt("order_id")
                    );

                    System.out.println(
                            "Date     : " +
                            rs.getTimestamp(
                                    "order_date"
                            )
                    );

                    System.out.println(
                            "----------------------------------------------"
                    );
                }


                String item =
                        rs.getString("item_name");

                int quantity =
                        rs.getInt("quantity");

                double price =
                        rs.getDouble("price");

                double itemTotal =
                        rs.getDouble("item_total");


                subtotal += itemTotal;


                System.out.printf(
                        "%-22s %3d x %7.2f = %8.2f%n",
                        item,
                        quantity,
                        price,
                        itemTotal
                );
            }


            if (!found) {

                System.out.println(
                        "Order not found."
                );

                return;
            }


            double tax =
                    subtotal * 0.05;

            double delivery =
                    40;

            double grandTotal =
                    subtotal +
                    tax +
                    delivery;


            System.out.println(
                    "----------------------------------------------"
            );


            System.out.printf(
                    "Subtotal          : Rs. %.2f%n",
                    subtotal
            );

            System.out.printf(
                    "Tax (5%%)           : Rs. %.2f%n",
                    tax
            );

            System.out.printf(
                    "Delivery Charge   : Rs. %.2f%n",
                    delivery
            );


            System.out.println(
                    "----------------------------------------------"
            );


            System.out.printf(
                    "GRAND TOTAL       : Rs. %.2f%n",
                    grandTotal
            );


            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "          Thank you for ordering!"
            );

            System.out.println(
                    "=============================================="
            );


        } catch (SQLException e) {

            System.out.println(
                    e.getMessage()
            );
        }
    }


    // ================= CUSTOMER MENU =================

    static void customerMenu(
            Scanner sc,
            int customerId) {


        while (true) {


            System.out.println(
                    "\n========== CUSTOMER MENU =========="
            );

            System.out.println(
                    "1. View Menu"
            );

            System.out.println(
                    "2. Place Order"
            );

            System.out.println(
                    "3. Order History"
            );

            System.out.println(
                    "4. Generate Bill"
            );

            System.out.println(
                    "5. Logout"
            );


            System.out.print(
                    "Enter choice: "
            );

            int choice =
                    sc.nextInt();

            sc.nextLine();


            switch (choice) {


                case 1:

                    displayMenu();

                    break;


                case 2:

                    placeOrder(
                            customerId,
                            sc
                    );

                    break;


                case 3:

                    orderHistory(
                            customerId
                    );

                    break;


                case 4:

                    System.out.print(
                            "Enter Order ID: "
                    );

                    int orderId =
                            sc.nextInt();

                    sc.nextLine();


                    generateBill(
                            orderId,
                            customerId
                    );

                    break;


                case 5:

                    System.out.println(
                            "Logged out successfully."
                    );

                    return;


                default:

                    System.out.println(
                            "Invalid choice."
                    );
            }
        }
    }


    // ================= MAIN METHOD =================

    public static void main(String[] args) {


        Scanner sc =
                new Scanner(System.in);


        while (true) {


            System.out.println(
                    "\n======================================"
            );

            System.out.println(
                    "       ONLINE FOOD ORDERING SYSTEM"
            );

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "1. Customer Registration"
            );

            System.out.println(
                    "2. Customer Login"
            );

            System.out.println(
                    "3. View Menu"
            );

            System.out.println(
                    "4. Exit"
            );


            System.out.print(
                    "Enter choice: "
            );


            int choice =
                    sc.nextInt();

            sc.nextLine();


            switch (choice) {


                case 1:

                    registerCustomer(sc);

                    break;


                case 2:

                    int customerId =
                            login(sc);


                    if (customerId != -1) {

                        customerMenu(
                                sc,
                                customerId
                        );
                    }

                    break;


                case 3:

                    displayMenu();

                    break;


                case 4:

                    System.out.println(
                            "Thank you for using the system!"
                    );

                    sc.close();

                    return;


                default:

                    System.out.println(
                            "Invalid choice."
                    );
            }
        }
    }
}