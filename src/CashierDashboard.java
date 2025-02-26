import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Instant;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class CashierDashboard {
    private static Connection conn = null;
    private static DefaultTableModel currentTransactionModel;
    private static List<TransactionData> currentTransactionList = new ArrayList<>();

    public static void main(String args[]) {
        dbSetup my = new dbSetup();
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_21_db",
                    my.user, my.pswd);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "Opened database successfully");

        // Create Main Frame
        JFrame cashierDashboard = new JFrame("Cashier Dashboard");
        cashierDashboard.setSize(800, 600);
        cashierDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cashierDashboard.setLayout(new BorderLayout());

        // Sidebar Panel (Left)
        JPanel sidebarPanel = new JPanel(new BorderLayout());

        JLabel sidebarTitle = new JLabel("Current Transaction", SwingConstants.CENTER);
        sidebarTitle.setFont(new Font("Arial", Font.BOLD, 16));

        // Table to Show Current Transaction (Only Visible Fields)
        currentTransactionModel = new DefaultTableModel(
            new String[]{"Product Name", "Purchase Date", "Ice Amount", "Topping Type"}, 0);
        JTable transactionTable = new JTable(currentTransactionModel);
        JScrollPane transactionScrollPane = new JScrollPane(transactionTable);

        sidebarPanel.add(sidebarTitle, BorderLayout.NORTH);
        sidebarPanel.add(transactionScrollPane, BorderLayout.CENTER);

        // Pay Button - located at bottom of sidebare
        JButton payButton = new JButton("Pay");
        sidebarPanel.add(payButton, BorderLayout.SOUTH);



        ////////////////////////////////////// MAIN CONTENT SECTION ///////////////////////////////////
        // TODO: integrate your main content section here. 
        // TODO: make sure the button on main section links to adding items into sidebar

        // Main Content Panel (Right)
        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Main Screen", SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.BOLD, 18));

        
        // Button to Add a Test Item *** HARDCODED, MANUAL TESTING (TODO - REMOVE THIS TEST BUTTON AND ADD MAIN SECTION)
        JButton testItemButton = new JButton("Add Test Item");
        testItemButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Add action listener to simulate adding an item (HARDCODED, MANUAL TESTING) TODO - REMOVE THIS TEST BUTTON AND ADD MAIN SECTION
        testItemButton.addActionListener(e -> {
            Timestamp purchaseDate = Timestamp.valueOf("2024-02-25 14:30:00");
            addItemToTransaction(16, 12345, 1001, purchaseDate, 0.5, "Red Bean");
        });





        ////////////////////////////////////// MAIN CONTENT SECTION ///////////////////////////////////
        

        mainPanel.add(placeholder, BorderLayout.NORTH);
        mainPanel.add(testItemButton, BorderLayout.CENTER);

        // Add sidebar and main screen to the frame
        cashierDashboard.add(sidebarPanel, BorderLayout.WEST);
        cashierDashboard.add(mainPanel, BorderLayout.CENTER);

        cashierDashboard.setVisible(true);

        // Pay Button Action
        payButton.addActionListener(e -> finalizeTransaction());

        // Close database connection on window close
        cashierDashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (conn != null) {
                    try {
                        conn.close();
                        System.out.println("Database connection closed.");
                    } catch (SQLException ex) {
                        System.out.println("Error closing connection: " + ex.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Add an item to the current transaction but only show selected fields in the GUI.
     */
    public static void addItemToTransaction(int productId, int orderId, int customerId, Timestamp purchaseDate, double iceAmount, String toppingType) {
        String productName = getProductNameById(productId);
    
        // Store internally
        currentTransactionList.add(new TransactionData(productId, orderId, customerId, productName, purchaseDate, iceAmount, toppingType));
    
        // Add only visible fields to the GUI
        currentTransactionModel.addRow(new Object[]{productName, purchaseDate, iceAmount, toppingType});
    }
    

    /**
     * Get customer transaction number to increment it (manual auto-increment)
     */
    private static int getNextTransactionNumber() {
        int nextNum = 1; // default 1 if table is empty.
        String query = "SELECT MAX(customer_transaction_num) FROM customer_transaction";
    
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                nextNum = rs.getInt(1) + 1;  
            }
        } catch (SQLException e) {
            System.err.println("Error fetching next transaction number: " + e.getMessage());
        }
    
        return nextNum;
    }
    


    /**
     * Finalize the transaction and insert it into the customer_transaction table.
     */
    private static void finalizeTransaction() {
        if (currentTransactionList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No items in transaction.");
            return;
        }
    
        try {
            // Get the latest transaction number
            int nextTransactionNum = getNextTransactionNumber();
    
            // Prepare the INSERT query
            String query = "INSERT INTO customer_transaction (customer_transaction_num, order_id, product_id, customer_id, purchase_date, ice_amount, topping_type) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
    
            for (TransactionData transaction : currentTransactionList) {
                ps.setInt(1, nextTransactionNum++);  // Use manually generated transaction number
                ps.setInt(2, transaction.orderId);
                ps.setInt(3, transaction.productId);
                ps.setInt(4, transaction.customerId);
                ps.setTimestamp(5, transaction.purchaseDate);
                ps.setDouble(6, transaction.iceAmount);
                ps.setString(7, transaction.toppingType);
                ps.addBatch();
            }
    
            ps.executeBatch();
            JOptionPane.showMessageDialog(null, "Transaction completed!");
    
            // Clear transaction list & UI
            currentTransactionModel.setRowCount(0);
            currentTransactionList.clear();
    
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error processing transaction: " + e.getMessage());
        }
    }
    
    

    /**
     * Fetch the product name based on product_id.
     */
    private static String getProductNameById(int productId) {
        String productName = "Unknown";
        String query = "SELECT product_name FROM product WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                productName = rs.getString("product_name");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product name: " + e.getMessage());
        }
        return productName;
    }
}
