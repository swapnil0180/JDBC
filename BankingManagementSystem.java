import java.sql.*;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.UUID;

public class BankingManagementSystem {

    // =====================================================
    // DATABASE CONNECTION
    // =====================================================

    static final String URL =
            "jdbc:mysql://localhost:3306/banking_system";

    static final String USER = "root";
    static final String PASSWORD = "Swapnil#123";

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL, USER, PASSWORD);
    }


    // =====================================================
    // ACCOUNT CREATION
    // =====================================================

    static void createAccount(
            String name,
            String email,
            String phone,
            String address,
            String accountNumber,
            String accountType,
            String pin) {

        String customerSql =
                "INSERT INTO customers " +
                "(full_name,email,phone,address) " +
                "VALUES (?,?,?,?)";

        String accountSql =
                "INSERT INTO accounts " +
                "(customer_id,account_number,account_type,pin) " +
                "VALUES (?,?,?,?)";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {

                int customerId;

                // Insert Customer
                try (PreparedStatement ps =
                             con.prepareStatement(
                                     customerSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, address);

                    ps.executeUpdate();

                    ResultSet rs =
                            ps.getGeneratedKeys();

                    if (!rs.next()) {
                        throw new SQLException(
                                "Customer creation failed");
                    }

                    customerId = rs.getInt(1);
                }


                // Insert Account
                try (PreparedStatement ps =
                             con.prepareStatement(accountSql)) {

                    ps.setInt(1, customerId);
                    ps.setString(2, accountNumber);
                    ps.setString(3, accountType);
                    ps.setString(4, pin);

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println(
                        "\nAccount created successfully!");
                System.out.println(
                        "Account Number: " + accountNumber);

            } catch (Exception e) {

                con.rollback();

                System.out.println(
                        "Account creation failed.");
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: " + e.getMessage());
        }
    }


    // =====================================================
    // CHECK BALANCE
    // =====================================================

    static void checkBalance(
            String accountNumber) {

        String sql =
                "SELECT balance,status " +
                "FROM accounts " +
                "WHERE account_number=?";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "\nAccount Status: "
                        + rs.getString("status"));

                System.out.println(
                        "Current Balance: ₹"
                        + rs.getBigDecimal("balance"));

            } else {

                System.out.println(
                        "Account not found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // DEPOSIT
    // =====================================================

    static void deposit(
            String accountNumber,
            BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println(
                    "Amount must be greater than zero.");
            return;
        }

        String selectSql =
                "SELECT account_id,balance,status " +
                "FROM accounts " +
                "WHERE account_number=?";

        String updateSql =
                "UPDATE accounts " +
                "SET balance=balance+? " +
                "WHERE account_number=?";

        String transactionSql =
                "INSERT INTO transactions " +
                "(transaction_ref,account_id," +
                "transaction_type,amount," +
                "balance_after,description) " +
                "VALUES (?,?,'DEPOSIT',?,?,?)";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {

                int accountId;
                BigDecimal balance;

                // Find Account
                try (PreparedStatement ps =
                             con.prepareStatement(selectSql)) {

                    ps.setString(1, accountNumber);

                    ResultSet rs =
                            ps.executeQuery();

                    if (!rs.next()) {
                        throw new SQLException(
                                "Account not found.");
                    }

                    if (!rs.getString("status")
                            .equals("ACTIVE")) {

                        throw new SQLException(
                                "Account is not active.");
                    }

                    accountId =
                            rs.getInt("account_id");

                    balance =
                            rs.getBigDecimal("balance");
                }

                BigDecimal newBalance =
                        balance.add(amount);


                // Update Balance
                try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setString(2, accountNumber);

                    ps.executeUpdate();
                }


                // Save Transaction
                try (PreparedStatement ps =
                             con.prepareStatement(
                                     transactionSql)) {

                    ps.setString(
                            1,
                            UUID.randomUUID().toString());

                    ps.setInt(2, accountId);
                    ps.setBigDecimal(3, amount);
                    ps.setBigDecimal(4, newBalance);

                    ps.setString(
                            5,
                            "Money deposited");

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println(
                        "Deposit successful.");
                System.out.println(
                        "New Balance: ₹" + newBalance);

            } catch (Exception e) {

                con.rollback();

                System.out.println(
                        "Deposit failed. Rollback done.");
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // WITHDRAW
    // =====================================================

    static void withdraw(
            String accountNumber,
            BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println(
                    "Amount must be greater than zero.");
            return;
        }

        String selectSql =
                "SELECT account_id,balance,status " +
                "FROM accounts " +
                "WHERE account_number=? FOR UPDATE";

        String updateSql =
                "UPDATE accounts " +
                "SET balance=balance-? " +
                "WHERE account_number=?";

        String transactionSql =
                "INSERT INTO transactions " +
                "(transaction_ref,account_id," +
                "transaction_type,amount," +
                "balance_after,description) " +
                "VALUES (?,?,'WITHDRAWAL',?,?,?)";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {

                int accountId;
                BigDecimal balance;

                try (PreparedStatement ps =
                             con.prepareStatement(selectSql)) {

                    ps.setString(1, accountNumber);

                    ResultSet rs =
                            ps.executeQuery();

                    if (!rs.next()) {
                        throw new SQLException(
                                "Account not found.");
                    }

                    if (!rs.getString("status")
                            .equals("ACTIVE")) {

                        throw new SQLException(
                                "Account is not active.");
                    }

                    accountId =
                            rs.getInt("account_id");

                    balance =
                            rs.getBigDecimal("balance");
                }


                // Check Balance
                if (balance.compareTo(amount) < 0) {

                    throw new SQLException(
                            "Insufficient balance.");
                }

                BigDecimal newBalance =
                        balance.subtract(amount);


                // Deduct Money
                try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setString(2, accountNumber);

                    ps.executeUpdate();
                }


                // Save Transaction
                try (PreparedStatement ps =
                             con.prepareStatement(
                                     transactionSql)) {

                    ps.setString(
                            1,
                            UUID.randomUUID().toString());

                    ps.setInt(2, accountId);
                    ps.setBigDecimal(3, amount);
                    ps.setBigDecimal(4, newBalance);

                    ps.setString(
                            5,
                            "Money withdrawn");

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println(
                        "Withdrawal successful.");
                System.out.println(
                        "Remaining Balance: ₹"
                        + newBalance);

            } catch (Exception e) {

                con.rollback();

                System.out.println(
                        "Withdrawal failed.");
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // FUND TRANSFER
    // =====================================================

    static void transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println(
                    "Amount must be greater than zero.");
            return;
        }

        if (fromAccount.equals(toAccount)) {
            System.out.println(
                    "Cannot transfer to same account.");
            return;
        }


        String selectSql =
                "SELECT account_id,balance,status " +
                "FROM accounts " +
                "WHERE account_number=? FOR UPDATE";

        String updateSql =
                "UPDATE accounts " +
                "SET balance=balance+? " +
                "WHERE account_number=?";

        String transactionSql =
                "INSERT INTO transactions " +
                "(transaction_ref,account_id," +
                "transaction_type,amount," +
                "balance_after,related_account_id," +
                "description) " +
                "VALUES (?,?,?,?,?,?,?)";


        try (Connection con = getConnection()) {

            // Start Transaction
            con.setAutoCommit(false);

            try {

                int fromId;
                int toId;

                BigDecimal fromBalance;
                BigDecimal toBalance;


                // -----------------------------------------
                // SOURCE ACCOUNT
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(selectSql)) {

                    ps.setString(1, fromAccount);

                    ResultSet rs =
                            ps.executeQuery();

                    if (!rs.next()) {
                        throw new SQLException(
                                "Source account not found.");
                    }

                    if (!rs.getString("status")
                            .equals("ACTIVE")) {

                        throw new SQLException(
                                "Source account is not active.");
                    }

                    fromId =
                            rs.getInt("account_id");

                    fromBalance =
                            rs.getBigDecimal("balance");
                }


                // -----------------------------------------
                // DESTINATION ACCOUNT
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(selectSql)) {

                    ps.setString(1, toAccount);

                    ResultSet rs =
                            ps.executeQuery();

                    if (!rs.next()) {
                        throw new SQLException(
                                "Destination account not found.");
                    }

                    if (!rs.getString("status")
                            .equals("ACTIVE")) {

                        throw new SQLException(
                                "Destination account is not active.");
                    }

                    toId =
                            rs.getInt("account_id");

                    toBalance =
                            rs.getBigDecimal("balance");
                }


                // -----------------------------------------
                // CHECK BALANCE
                // -----------------------------------------

                if (fromBalance.compareTo(amount) < 0) {

                    throw new SQLException(
                            "Insufficient balance.");
                }


                BigDecimal newFromBalance =
                        fromBalance.subtract(amount);

                BigDecimal newToBalance =
                        toBalance.add(amount);

                String reference =
                        UUID.randomUUID().toString();


                // -----------------------------------------
                // DEDUCT SOURCE ACCOUNT
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                    ps.setBigDecimal(
                            1,
                            amount.negate());

                    ps.setString(
                            2,
                            fromAccount);

                    ps.executeUpdate();
                }


                // -----------------------------------------
                // ADD DESTINATION ACCOUNT
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                    ps.setBigDecimal(
                            1,
                            amount);

                    ps.setString(
                            2,
                            toAccount);

                    ps.executeUpdate();
                }


                // -----------------------------------------
                // SOURCE TRANSACTION
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(
                                     transactionSql)) {

                    ps.setString(1, reference);
                    ps.setInt(2, fromId);

                    ps.setString(
                            3,
                            "TRANSFER_OUT");

                    ps.setBigDecimal(4, amount);
                    ps.setBigDecimal(
                            5,
                            newFromBalance);

                    ps.setInt(6, toId);

                    ps.setString(
                            7,
                            "Transfer to "
                            + toAccount);

                    ps.executeUpdate();
                }


                // -----------------------------------------
                // DESTINATION TRANSACTION
                // -----------------------------------------

                try (PreparedStatement ps =
                             con.prepareStatement(
                                     transactionSql)) {

                    ps.setString(1, reference);
                    ps.setInt(2, toId);

                    ps.setString(
                            3,
                            "TRANSFER_IN");

                    ps.setBigDecimal(4, amount);
                    ps.setBigDecimal(
                            5,
                            newToBalance);

                    ps.setInt(6, fromId);

                    ps.setString(
                            7,
                            "Transfer from "
                            + fromAccount);

                    ps.executeUpdate();
                }


                // -----------------------------------------
                // COMMIT
                // -----------------------------------------

                con.commit();

                System.out.println(
                        "\nTransfer successful!");

                System.out.println(
                        "Amount: ₹" + amount);

                System.out.println(
                        "Transaction ID: "
                        + reference);

            } catch (Exception e) {

                // -----------------------------------------
                // ROLLBACK
                // -----------------------------------------

                con.rollback();

                System.out.println(
                        "\nTransfer failed!");

                System.out.println(
                        "Money deduction rolled back.");

                System.out.println(
                        "Reason: " + e.getMessage());
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // TRANSACTION HISTORY
    // =====================================================

    static void transactionHistory(
            String accountNumber) {

        String sql =
                "SELECT t.transaction_ref," +
                "t.transaction_type,t.amount," +
                "t.balance_after,t.description," +
                "t.transaction_date " +
                "FROM transactions t " +
                "JOIN accounts a " +
                "ON t.account_id=a.account_id " +
                "WHERE a.account_number=? " +
                "ORDER BY t.transaction_date DESC";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            ResultSet rs =
                    ps.executeQuery();

            System.out.println(
                    "\n========== TRANSACTION HISTORY ==========");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "\nReference : "
                        + rs.getString(
                                "transaction_ref"));

                System.out.println(
                        "Type      : "
                        + rs.getString(
                                "transaction_type"));

                System.out.println(
                        "Amount    : ₹"
                        + rs.getBigDecimal(
                                "amount"));

                System.out.println(
                        "Balance   : ₹"
                        + rs.getBigDecimal(
                                "balance_after"));

                System.out.println(
                        "Description: "
                        + rs.getString(
                                "description"));

                System.out.println(
                        "Date      : "
                        + rs.getTimestamp(
                                "transaction_date"));

                System.out.println(
                        "------------------------------------------");
            }

            if (!found) {

                System.out.println(
                        "No transactions found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // BLOCK ACCOUNT
    // =====================================================

    static void blockAccount(
            String accountNumber) {

        String sql =
                "UPDATE accounts " +
                "SET status='BLOCKED' " +
                "WHERE account_number=?";

        try (Connection con = getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Account blocked successfully.");

            } else {

                System.out.println(
                        "Account not found.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database Error: "
                    + e.getMessage());
        }
    }


    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println(
                    "\n======================================");

            System.out.println(
                    "       BANKING MANAGEMENT SYSTEM");

            System.out.println(
                    "======================================");

            System.out.println(
                    "1. Create Account");

            System.out.println(
                    "2. Check Balance");

            System.out.println(
                    "3. Deposit");

            System.out.println(
                    "4. Withdraw");

            System.out.println(
                    "5. Fund Transfer");

            System.out.println(
                    "6. Transaction History");

            System.out.println(
                    "7. Block Account");

            System.out.println(
                    "8. Exit");

            System.out.print(
                    "\nEnter choice: ");

            int choice =
                    sc.nextInt();

            sc.nextLine();


            try {

                switch (choice) {

                    // =====================================
                    // CREATE ACCOUNT
                    // =====================================

                    case 1:

                        System.out.print(
                                "Enter customer name: ");

                        String name =
                                sc.nextLine();

                        System.out.print(
                                "Enter email: ");

                        String email =
                                sc.nextLine();

                        System.out.print(
                                "Enter phone: ");

                        String phone =
                                sc.nextLine();

                        System.out.print(
                                "Enter address: ");

                        String address =
                                sc.nextLine();

                        System.out.print(
                                "Enter account number: ");

                        String accountNumber =
                                sc.nextLine();

                        System.out.print(
                                "Enter account type " +
                                "(SAVINGS/CURRENT): ");

                        String accountType =
                                sc.nextLine();

                        System.out.print(
                                "Enter PIN: ");

                        String pin =
                                sc.nextLine();

                        createAccount(
                                name,
                                email,
                                phone,
                                address,
                                accountNumber,
                                accountType,
                                pin);

                        break;


                    // =====================================
                    // BALANCE
                    // =====================================

                    case 2:

                        System.out.print(
                                "Enter account number: ");

                        String balanceAccount =
                                sc.nextLine();

                        checkBalance(
                                balanceAccount);

                        break;


                    // =====================================
                    // DEPOSIT
                    // =====================================

                    case 3:

                        System.out.print(
                                "Enter account number: ");

                        String depositAccount =
                                sc.nextLine();

                        System.out.print(
                                "Enter amount: ");

                        BigDecimal depositAmount =
                                sc.nextBigDecimal();

                        deposit(
                                depositAccount,
                                depositAmount);

                        break;


                    // =====================================
                    // WITHDRAW
                    // =====================================

                    case 4:

                        System.out.print(
                                "Enter account number: ");

                        String withdrawAccount =
                                sc.nextLine();

                        System.out.print(
                                "Enter amount: ");

                        BigDecimal withdrawAmount =
                                sc.nextBigDecimal();

                        withdraw(
                                withdrawAccount,
                                withdrawAmount);

                        break;


                    // =====================================
                    // TRANSFER
                    // =====================================

                    case 5:

                        System.out.print(
                                "Enter sender account: ");

                        String from =
                                sc.nextLine();

                        System.out.print(
                                "Enter receiver account: ");

                        String to =
                                sc.nextLine();

                        System.out.print(
                                "Enter amount: ");

                        BigDecimal transferAmount =
                                sc.nextBigDecimal();

                        transfer(
                                from,
                                to,
                                transferAmount);

                        break;


                    // =====================================
                    // HISTORY
                    // =====================================

                    case 6:

                        System.out.print(
                                "Enter account number: ");

                        String historyAccount =
                                sc.nextLine();

                        transactionHistory(
                                historyAccount);

                        break;


                    // =====================================
                    // BLOCK ACCOUNT
                    // =====================================

                    case 7:

                        System.out.print(
                                "Enter account number: ");

                        String blockAccount =
                                sc.nextLine();

                        blockAccount(
                                blockAccount);

                        break;


                    // =====================================
                    // EXIT
                    // =====================================

                    case 8:

                        System.out.println(
                                "Thank you!");

                        sc.close();

                        return;


                    default:

                        System.out.println(
                                "Invalid choice.");
                }

            } catch (Exception e) {

                System.out.println(
                        "Error: "
                        + e.getMessage());
            }
        }
    }
}