import java.sql.*;
import java.util.Scanner;

public class BankingCRUD {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String url =
            "jdbc:mysql://localhost:3306/BANKING_SCENARIO";
        String username = "root";
        String password = "Swapnil#123";

        int choice;

        try {

            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create Connection
            Connection con =
                DriverManager.getConnection(
                    url, username, password);

            System.out.println("Database Connected Successfully!");

            do {

                System.out.println("\n===== BANKING MENU =====");
                System.out.println("1. Create Account");
                System.out.println("2. View Accounts");
                System.out.println("3. Update Account");
                System.out.println("4. Delete Account");
                System.out.println("5. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();

                switch (choice) {

                    // =========================
                    // CREATE
                    // =========================
                    case 1:

                        System.out.print("Enter Customer ID: ");
                        int customerId = sc.nextInt();

                        System.out.print("Enter Account Type: ");
                        String accountType = sc.next();

                        System.out.print("Enter Balance: ");
                        double balance = sc.nextDouble();

                        String insertSQL =
                            "INSERT INTO accounts " +
                            "(customer_id, account_type, balance) " +
                            "VALUES (?, ?, ?)";

                        PreparedStatement insertPS =
                            con.prepareStatement(insertSQL);

                        insertPS.setInt(1, customerId);
                        insertPS.setString(2, accountType);
                        insertPS.setDouble(3, balance);

                        int inserted =
                            insertPS.executeUpdate();

                        if (inserted > 0) {
                            System.out.println(
                                "Account created successfully!");
                        }

                        insertPS.close();
                        break;


                    // =========================
                    // READ
                    // =========================
                    case 2:

                        String selectSQL =
                            "SELECT * FROM accounts";

                        Statement stmt =
                            con.createStatement();

                        ResultSet rs =
                            stmt.executeQuery(selectSQL);

                        System.out.println(
                            "\nID | CUSTOMER ID | TYPE | BALANCE");

                        System.out.println(
                            "--------------------------------------");

                        while (rs.next()) {

                            System.out.println(
                                rs.getInt("account_id")
                                + " | " +
                                rs.getInt("customer_id")
                                + " | " +
                                rs.getString("account_type")
                                + " | " +
                                rs.getDouble("balance")
                            );
                        }

                        rs.close();
                        stmt.close();
                        break;


                    // =========================
                    // UPDATE
                    // =========================
                    case 3:

                        System.out.print(
                            "Enter Account ID: ");

                        int accountId =
                            sc.nextInt();

                        System.out.print(
                            "Enter New Balance: ");

                        double newBalance =
                            sc.nextDouble();

                        String updateSQL =
                            "UPDATE accounts " +
                            "SET balance = ? " +
                            "WHERE account_id = ?";

                        PreparedStatement updatePS =
                            con.prepareStatement(updateSQL);

                        updatePS.setDouble(1, newBalance);
                        updatePS.setInt(2, accountId);

                        int updated =
                            updatePS.executeUpdate();

                        if (updated > 0) {

                            System.out.println(
                                "Account updated successfully!");

                        } else {

                            System.out.println(
                                "Account ID not found!");
                        }

                        updatePS.close();
                        break;


                    // =========================
                    // DELETE
                    // =========================
                    case 4:

                        System.out.print(
                            "Enter Account ID: ");

                        int deleteId =
                            sc.nextInt();

                        String deleteSQL =
                            "DELETE FROM accounts " +
                            "WHERE account_id = ?";

                        PreparedStatement deletePS =
                            con.prepareStatement(deleteSQL);

                        deletePS.setInt(1, deleteId);

                        int deleted =
                            deletePS.executeUpdate();

                        if (deleted > 0) {

                            System.out.println(
                                "Account deleted successfully!");

                        } else {

                            System.out.println(
                                "Account ID not found!");
                        }

                        deletePS.close();
                        break;


                    // =========================
                    // EXIT
                    // =========================
                    case 5:

                        System.out.println(
                            "Thank you!");

                        break;


                    default:

                        System.out.println(
                            "Invalid choice!");
                }

            } while (choice != 5);
;
            con.close();
            sc.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

