import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Instant;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * (Phase 4) TODO:
 * (1) Rewards Button
 * - be sure to first setLoggedInCustomerId (Line 413) then update their rewards
 * <p>
 * (2) BEAUTIFY THE DAMN THING
 * - add pictures to the drinks and stuff
 * <p>
 * (3) MAKE LAST SECOND CUSTOMIZATION POSSIBLE THROUGH CURRENT ORDER TAB
 * <p>
 * (4) Implement inventory deduction after customization panel
 * <p>
 * (5) Add a interface for the total price of all (custom) drink at the bottom and have each drink
 * show its price instead of purchase date
 * - make sure that price is on the very left
 * <p>
 * (6) check and fix if needed: toppingType null case (NONE or '')?
 * <p>
 * (7) Add like 2 or 3 out of the follow for phase (4) features:
 * - sales
 * - returns
 * - voids
 * - discards
 * - payment methods
 */

public class CashierGUI extends JPanel {
    private static DefaultTableModel currentTransactionModel;
    private static final List<TransactionData> currentTransactionList = new ArrayList<>();
    private static int loggedInCustomerId = -1;

    private static JPanel mainPanel;
    private static JScrollPane mainInterScrollPane;
    private static JPanel selectPanel;
    private static JScrollPane selectScrollPane;
    private static JPanel customizePanel;
    private static JScrollPane customizeScrollPane;
    private static CardLayout panelSwitcher;
    private static String currentDrink;
    private static Connection conn = null;

    public CashierGUI(Connection conn) {
        CashierGUI.conn = conn;
        setLayout(new BorderLayout());

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

        // Pay Button - located at bottom of sidebar
        JButton payButton = new JButton("Pay");
        sidebarPanel.add(payButton, BorderLayout.SOUTH);

        ////////////////////////////////////// MAIN CONTENT SECTION ///////////////////////////////////

        // Main Content Panel (Right)

        // Main Panel for Drink Selection and Customization
        panelSwitcher = new CardLayout();
        mainPanel = new JPanel(panelSwitcher); //mainPanel2 will now host all the different panels

        JPanel mainInterPanel = new JPanel();
        mainInterPanel.setLayout(new GridLayout(4, 3, 20, 20));

        /** Drink selection for general types of drink */
        ArrayList<String> drinkTypes = getDrinkTypesFromDB();
        for (String drinks : drinkTypes) {
            JButton drinkButton = new JButton(drinks);
            drinkButton.setPreferredSize(new Dimension(200, 50));
            drinkButton.setFont(new Font("Arial", Font.BOLD, 24));
            drinkButton.addActionListener(e -> showSpecificDrinks(drinks));
            mainInterPanel.add(drinkButton);
        }

        // mainPanel.add(mainInterPanel, "General Drinks");

        mainInterScrollPane = new JScrollPane(mainInterPanel);
        mainInterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainInterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainInterScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(mainInterScrollPane, "General Drinks");

        /* Drink selection for specific types of drink */
        // selectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        // mainPanel.add(selectPanel, "Specific Drink Selection");

        selectPanel = new JPanel(new GridLayout(4,3, 20, 20));
        selectScrollPane = new JScrollPane(selectPanel);
        selectScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        selectScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(selectScrollPane, "Specific Drink Selection");

        /* Drink customizer after specific drink selection */
        //customizePanel = new JPanel(new GridBagLayout());
        //mainPanel.add(customizePanel, "Customize Drink");

        customizePanel = new JPanel(new GridBagLayout());
        customizeScrollPane = new JScrollPane(customizePanel);
        customizeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        customizeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        customizeScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(customizeScrollPane, "Customize Drink");


        ////////////////////////////////////// MAIN CONTENT SECTION ///////////////////////////////////

        // Add sidebar and main screen to the frame
        add(sidebarPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Pay Button Action
        payButton.addActionListener(e -> finalizeTransaction());
    }

    /**
     * Helper function
     */
    private static void resetScrollPosition(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            // Reset to top position (0, 0)
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
            verticalBar.setValue(verticalBar.getMinimum());
            horizontalBar.setValue(horizontalBar.getMinimum());
        });
    }

    private static ArrayList<String> getColVal(String query, String... params) {
        ArrayList<String> values = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                values.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error.");
        }
        return values;
    }

    /**
     * Getting drink type from database
     */
    private static ArrayList<String> getDrinkTypesFromDB() {
        return getColVal("SELECT DISTINCT product_type FROM product");
    }

    /**
     * Getting drink name from database
     */
    private static ArrayList<String> getDrinksByType(String drinkType) {
        return getColVal("SELECT product_name FROM product WHERE product_type = ?", drinkType);
    }

    /**
     * Getting customization options from database
     */
    private static ArrayList<String[]> getCustomizeOptions() {
        ArrayList<String[]> options = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT item_name, amount FROM inventory WHERE item_id BETWEEN 22 AND 30;");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                options.add(new String[]{rs.getString("item_name"), rs.getString("amount")});
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error.");
        }
        return options;
    }

    /**
     * Setting up buttons for specific drinks
     */
    private static void showSpecificDrinks(String drinkType) {
        selectPanel.removeAll();

        ArrayList<String> drinks = getDrinksByType(drinkType);
        for (String drink : drinks) {
            JButton drinkButton = new JButton(drink);
            drinkButton.setPreferredSize(new Dimension(150, 150));

            String imagePath = "../specificDrinkIMGs/" + drink + ".jpg";
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);

            drinkButton.setVerticalTextPosition(SwingConstants.TOP);
            drinkButton.setHorizontalTextPosition(SwingConstants.CENTER);
            drinkButton.setIcon(icon);
            // drinkButton.setBackground(new Color(162, 224, 229, 213));

            drinkButton.addActionListener(e -> showCustomizeDrink(drink));
            selectPanel.add(drinkButton);
        }
        addBackButton(selectPanel, "General Drinks");
        selectPanel.revalidate();
        selectPanel.repaint();
        panelSwitcher.show(mainPanel, "Specific Drink Selection");

        resetScrollPosition(selectScrollPane);
    }

    /**
     * Once drink has been selected, customization (milk & type, sugar & type, ice)
     */
    private static void showCustomizeDrink(String drink) {
        customizePanel.removeAll();
        currentDrink = drink;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        customizePanel.add(new JLabel("Customize " + drink), gbc);
        gbc.gridy++;

        ArrayList<String[]> customOptions = getCustomizeOptions();

        /* dynamically store custom options and map it to its text field */
        HashMap<String, JTextField> inputFields = new HashMap<>();

        for (String[] option : customOptions) {
            gbc.gridx = 0;
            customizePanel.add(new JLabel(option[0] + " (Available: " + option[1] + ")"), gbc);
            gbc.gridx = 1;

            JTextField inputField = new JTextField(5);
            customizePanel.add(inputField, gbc);
            inputFields.put(option[0], inputField);
            gbc.gridy++;
        }

        /* Retrieving and process only non-empty string into pop-up */
        JButton confirmButton = new JButton("Confirm Selection");
        confirmButton.addActionListener(e -> {
            StringBuilder orderSummary = new StringBuilder("Order placed: " + currentDrink + "\n");

            boolean hasCustomizations = false;
            double iceAmount = 0;
            String toppingType = "";

            for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
                String optionName = entry.getKey();
                String value = entry.getValue().getText().trim();

                if (!value.isEmpty() && !value.equals("0")) {
                    orderSummary.append(optionName).append(": ").append(value).append("\n");
                    hasCustomizations = true;

                    // Assuming "Ice" and "Topping" are valid names in the DB
                    if (optionName.toLowerCase().contains("ice")) {
                        iceAmount = Double.parseDouble(value);
                    }
                    if (optionName.toLowerCase().contains("topping")) {
                        toppingType = value;
                    }
                }
            }
            if (!hasCustomizations) {
                orderSummary.append("No customizations.");
            }
            JOptionPane.showMessageDialog(null, orderSummary.toString());

            int productId = getProductIdByName(currentDrink);  // Implement this method
            int orderId = generateOrderId();  // Get from the current session/order
            int customerId = getCustomerId();  // Get from the session/customer login
            Timestamp purchaseDate = Timestamp.from(Instant.now());

            addItemToTransaction(productId, orderId, customerId, purchaseDate, iceAmount, toppingType);

            // Switch back to main screen
            panelSwitcher.show(mainPanel, "General Drinks");
        });
        gbc.gridx = 0;
        gbc.gridy++;
        customizePanel.add(confirmButton, gbc);

        addBackButton(customizePanel, "Specific Drink Selection");
        customizePanel.revalidate();
        customizePanel.repaint();
        panelSwitcher.show(mainPanel, "Customize Drink");
    }

    private static void addBackButton(JPanel panel, String previousPanel) {
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.setBackground(new Color(227, 134, 116));
        backButton.addActionListener(e -> panelSwitcher.show(mainPanel, previousPanel));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(backButton, gbc);
    }

    /**
     * Add getting the item id if the product is confirmed
     */
    private static int getProductIdByName(String productName) {
        String query = "SELECT product_id FROM product WHERE product_name = ?";
        int productId = -1;  // Default if not found

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                productId = rs.getInt("product_id");
            }
        }
        catch (SQLException e) {
            System.err.println("Error fetching product ID: " + e.getMessage());
        }
        return productId;
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
        }
        catch (SQLException e) {
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

        }
        catch (SQLException e) {
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
        }
        catch (SQLException e) {
            System.err.println("Error fetching product name: " + e.getMessage());
        }
        return productName;
    }

    private static int generateOrderId() {
        return 10000 + new Random().nextInt(90000);
    }

    /**
     * TODO:
     * When setting up customer_rewards, be sure to use setLoggedInCustomerId
     */
    public static void setLoggedInCustomerId(int customerId) {
        loggedInCustomerId = customerId;
    }

    private static int getCustomerId() {
        return loggedInCustomerId != -1 ? loggedInCustomerId : 0;
    }
}