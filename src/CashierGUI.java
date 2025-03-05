import java.awt.*;
import java.sql.*;
import java.time.Instant;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
  (Phase 4):
  Check Pinned Google Docs
 */

/**
 * @author Sebastian Chu, Zian Liang
 * @version 1.0
 */
public class CashierGUI extends JPanel {
    static DefaultTableModel currentTransactionModel;
    static final List<TransactionData> currentTransactionList = new ArrayList<>();
    private static int loggedInCustomerId = -1;

    private static JPanel mainPanel;
    private static JPanel selectPanel;
    private static JScrollPane selectScrollPane;
    private static JPanel customizePanel;
    private static CardLayout panelSwitcher;
    private static String currentDrink;
    private static Connection conn = null;
    private static JLabel totalCostLabel;

    /**
     * @param conn the database connection
     */
    public CashierGUI(Connection conn) {
        CashierGUI.conn = conn;
        setLayout(new BorderLayout());

        // =====================================================
        // CASHIER SIDEBAR SECTION
        // =====================================================

        // Sidebar Panel (Left)
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        JLabel sidebarTitle = new JLabel("Current Transaction", SwingConstants.CENTER);
        sidebarTitle.setFont(new Font("Arial", Font.BOLD, 16));

        // Table to Show Current Transaction (Only Visible Fields and Remove Button)
        currentTransactionModel = new DefaultTableModel(
                new String[]{"Product Name", "Product Price", "Ice Amount", "Topping Type", "Remove Item"}, 0);
        JTable transactionTable = new JTable(currentTransactionModel);
        JScrollPane transactionScrollPane = new JScrollPane(transactionTable);
        transactionTable.getColumn("Remove Item").setCellRenderer(new ButtonRenderer());
        transactionTable.getColumn("Remove Item").setCellEditor(new ButtonEditor(new JCheckBox(), transactionTable));

        sidebarPanel.add(sidebarTitle, BorderLayout.NORTH);
        sidebarPanel.add(transactionScrollPane, BorderLayout.CENTER);

        // Panel for Total Cost & Pay Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Total Cost Label
        totalCostLabel = new JLabel("Total: $0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Pay Button
        JButton payButton = new JButton("Pay");
        payButton.setFont(new Font("Arial", Font.BOLD, 24));

        // Add components to bottom panel
        bottomPanel.add(totalCostLabel);
        bottomPanel.add(payButton);

        sidebarPanel.add(bottomPanel, BorderLayout.SOUTH);

        // sidebar to the frame
        add(sidebarPanel, BorderLayout.WEST);

        // =====================================================
        // CASHIER GRID SECTION
        // =====================================================

        // Main Panel for Drink Selection and Customization
        panelSwitcher = new CardLayout();
        mainPanel = new JPanel(panelSwitcher); // mainPanel will now host all the different panels

        JPanel mainInterPanel = new JPanel();
        mainInterPanel.setLayout(new GridLayout(4, 3, 20, 20));

        // Drink selection for general types of drink //
        ArrayList<String> drinkTypes = getDrinkTypesFromDB();
        for (String drinks : drinkTypes) {
            JButton drinkButton = new JButton(drinks);
            drinkButton.setPreferredSize(new Dimension(200, 50));
            drinkButton.setFont(new Font("Arial", Font.BOLD, 24));
            drinkButton.addActionListener(e -> showSpecificDrinks(drinks));
            mainInterPanel.add(drinkButton);
        }

        // Customer ID button
        JButton customerIdButton = new JButton("Customer ID");
        customerIdButton.setPreferredSize(new Dimension(200, 50));
        customerIdButton.setFont(new Font("Arial", Font.BOLD, 24));
        customerIdButton.setBackground(new Color(255, 165, 0)); // Orange color
        customerIdButton.setForeground(Color.WHITE);

        customerIdButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Enter Customer ID:", "Customer Login", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    int customerId = Integer.parseInt(input.trim()); // Convert input to integer
                    setLoggedInCustomerId(customerId); // Set the ID
                    JOptionPane.showMessageDialog(null, "Customer ID set to: " + customerId, "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainInterPanel.add(customerIdButton);

        // mainPanel.add(mainInterPanel, "General Drinks");

        JScrollPane mainInterScrollPane = new JScrollPane(mainInterPanel);
        mainInterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainInterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainInterScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(mainInterScrollPane, "General Drinks");

        /* Drink selection for specific types of drink */
        // selectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        // mainPanel.add(selectPanel, "Specific Drink Selection");

        selectPanel = new JPanel(new GridLayout(4, 3, 20, 20));
        selectScrollPane = new JScrollPane(selectPanel);
        selectScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        selectScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(selectScrollPane, "Specific Drink Selection");

        /* Drink customizer after specific drink selection */
        // customizePanel = new JPanel(new GridBagLayout());
        // mainPanel.add(customizePanel, "Customize Drink");

        customizePanel = new JPanel(new GridBagLayout());
        JScrollPane customizeScrollPane = new JScrollPane(customizePanel);
        customizeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        customizeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        customizeScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(customizeScrollPane, "Customize Drink");

        // main screen to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Pay Button Action
        payButton.addActionListener(e -> finalizeTransaction());
    }

    /**
     * Helper function to reset scroll position as a default setting
     *
     * @param scrollPane - the scroll pane used to reset its position to a set
     *                   default.
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

    /**
     * Executes SQL queries and gets columns from the tables.
     *
     * @param query  - SQL query that will be executed
     * @param params - optional parameter for variables.
     * @return list of strings with first column values from the query.
     * If there was an error, an empty list is returned.
     */
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
     *
     * @return column value from product_type
     */
    private static ArrayList<String> getDrinkTypesFromDB() {
        return getColVal("SELECT DISTINCT product_type FROM product");
    }

    /**
     * Getting drink name from database.
     *
     * @param drinkType - the drink type from product database
     * @return column value from drinkType
     */
    private static ArrayList<String> getDrinksByType(String drinkType) {
        return getColVal("SELECT product_name FROM product WHERE product_type = ?", drinkType);
    }

    /**
     * Getting customization options from database
     *
     * @return all options from the database that is allowed for customization
     */
    private static ArrayList<String[]> getCustomizeOptions() {
        ArrayList<String[]> options = new ArrayList<>();
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT item_name, amount FROM inventory WHERE item_id BETWEEN 22 AND 30;");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                options.add(new String[]{rs.getString("item_name"), rs.getString("amount")});
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error.");
        }
        return options;
    }

    /**
     * Setting up buttons for specific drinks.
     *
     * @param drinkType - the drink type from product database
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
     * Enables customization of the specific drink order by the customer. Generates
     * the GUI and logic in the function
     *
     * @param drink - drink name for customization.
     */
    private static void showCustomizeDrink(String drink) {
        customizePanel.removeAll();
        currentDrink = drink;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // creates 3 columns?
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10); // button customization

        JLabel heading = new JLabel("Customize " + drink);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        customizePanel.add(heading, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1; // resets width to accommodate other components

        ArrayList<String[]> customOptions = getCustomizeOptions();

        /* dynamically store custom options and map it to its text field */
        HashMap<String, Integer> customValues = new HashMap<>();

        for (String[] option : customOptions) {
            String optionName = option[0];
            int initialAmount = 0;

            gbc.gridx = 0;
            gbc.insets = new Insets(5, 15, 5, 15); //topping position
            customizePanel.add(new JLabel(optionName + " (Available: " + option[1] + ")"), gbc);

            gbc.gridx = 1;

            JLabel valueLabel = new JLabel(String.valueOf(initialAmount));
            customizePanel.add(valueLabel, gbc);

            JButton incrementButton = new JButton("+");
            incrementButton.setMargin(new Insets(2, 2, 2, 8)); //button customization
            incrementButton.addActionListener(e -> {
                int currentValue = customValues.get(optionName);
                customValues.put(optionName, currentValue + 1);
                valueLabel.setText(String.valueOf(customValues.get(optionName)));
            });

            JButton decrementButton = new JButton("-");
            incrementButton.setMargin(new Insets(2, 2, 2, 2)); //button customization
            decrementButton.addActionListener(e -> {
                int currentValue = customValues.get(optionName);
                if (currentValue > 0) { // Prevent negative values
                    customValues.put(optionName, currentValue - 1);
                    valueLabel.setText(String.valueOf(customValues.get(optionName)));
                }
            });

            customValues.put(optionName, initialAmount);

            gbc.gridx = 2;
            gbc.insets = new Insets(5, -30, 5, 5); //dec button spacing

            customizePanel.add(decrementButton, gbc);
            gbc.gridx = 3;
            gbc.insets = new Insets(5, 5, 5, 5); //inc button spacing

            customizePanel.add(incrementButton, gbc);
            gbc.gridy++;
        }

        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 30, 5, 10); //toppings spacing
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        JLabel toppingsLabel = new JLabel("Toppings");
        toppingsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        customizePanel.add(toppingsLabel, gbc);

        gbc.gridy++;
        String[] toppings = {"Boba", "Aloe Vera", "Red Bean", "Popping Boba", "None"};
        HashMap<String, JCheckBox> toppingCheckboxes = new HashMap<>();

        for (String topping : toppings) {
            JCheckBox checkBox = new JCheckBox(topping);
            customizePanel.add(checkBox, gbc);
            toppingCheckboxes.put(topping, checkBox);
            gbc.gridy++;
        }

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3; // Makes the buttons span the columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 0, 0, 0);

        gbc.gridy += 2; //This is to help with formatting
        /* Retrieving and process only non-empty string into pop-up */
        JButton confirmButton = new JButton("Confirm Selection");
        customizePanel.add(confirmButton, gbc);

        gbc.gridx = 1;
        addBackButton(customizePanel, "Specific Drink Selection");

        confirmButton.addActionListener(e -> {
            StringBuilder orderSummary = new StringBuilder("<html><h2>Order Placed:</h2>");
            orderSummary.append("<b>").append(currentDrink).append("</b><br>");

            boolean hasCustomizations = false;
            double iceAmount = 0;
            String toppingType = "None";

            for (Map.Entry<String, Integer> entry : customValues.entrySet()) {
                String optionName = entry.getKey();
                int value = entry.getValue();

                if (value > 0) {
                    orderSummary.append(optionName).append(": ").append(value).append("<br>");
                    hasCustomizations = true;

                    if (optionName.toLowerCase().contains("ice")) {
                        iceAmount = value;
                    }
                    if (optionName.toLowerCase().contains("topping")) {
                        toppingType = String.valueOf(value);
                    }
                }
            }

            ArrayList<String> selectedToppings = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> entry : toppingCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedToppings.add(entry.getKey());
                }
            }

            if (!selectedToppings.isEmpty()) {
                toppingType = String.join(", ", selectedToppings);
                orderSummary.append("<b>Toppings:</b> ").append(toppingType).append("<br>");
                hasCustomizations = true;
            }

            if (!hasCustomizations) {
                orderSummary.append("<i>No customizations.</i><br>");
            }

            // Fetch the product price dynamically
            int productId = getProductIdByName(currentDrink);
            double totalCost = getProductPriceById(productId);

            orderSummary.append("<h3>Total Cost: $").append(String.format("%.2f", totalCost)).append("</h3></html>");

            // Show a confirm dialog
            int confirm = JOptionPane.showConfirmDialog(
                    null, orderSummary.toString(), "Confirm Order",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int orderId = generateOrderId();
                int customerId = getCustomerId();
                Timestamp purchaseDate = Timestamp.from(Instant.now());

                addItemToTransaction(productId, orderId, customerId, purchaseDate, iceAmount, toppingType);

                JOptionPane.showMessageDialog(null, "Your order has been placed!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Switch back to main screen
                panelSwitcher.show(mainPanel, "General Drinks");
            }
        });

        customizePanel.revalidate();
        customizePanel.repaint();
        panelSwitcher.show(mainPanel, "Customize Drink");
    }

    /**
     * Creates the back button on the Cashier Grid GUI when ordering drinks.
     *
     * @param panel         - used for the main panel
     * @param previousPanel - uses the previous panel
     */
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
     * Get the product id if the product is confirmed, ensuring validation.
     *
     * @param productName - name of the product from product database
     * @return the product id from product database
     */
    private static int getProductIdByName(String productName) {
        String query = "SELECT product_id FROM product WHERE product_name = ?";
        int productId = -1; // Default if not found

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
     * Get the product price from product database using queries.
     *
     * @param productId - the id of the product from product database
     * @return price of the product
     */
    private static double getProductPriceById(int productId) {
        String query = "SELECT product_cost FROM product WHERE product_id = ?";
        double price = 0.0;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("product_cost");
            }
        }
        catch (SQLException e) {
            System.err.println("Error fetching product price: " + e.getMessage());
        }
        return price;
    }

    /**
     * Updates the total cost display.
     */
    private static void updateTotalCost() {
        double totalCost = 0.0;
        for (TransactionData transaction : currentTransactionList) {
            double productPrice = getProductPriceById(transaction.productId); // Fetch price dynamically
            totalCost += productPrice;
        }
        totalCostLabel.setText(String.format("Total: $%.2f", totalCost));
    }

    /**
     * Add an item to the current transaction but only show selected fields in the
     * GUI.
     *
     * @param productId    - the id of the product
     * @param orderId      - the id of the specific order
     * @param customerId   - the id of the customer, if the customer added
     *                     identification through phone number or email.
     * @param purchaseDate - the date of when each order was purchased
     * @param iceAmount    - the customers choice of the amount of ice
     * @param toppingType  - all the choices of toppings the customer chose to add
     *                     to their drink.
     */
    public static void addItemToTransaction(int productId, int orderId, int customerId, Timestamp purchaseDate,
                                            double iceAmount, String toppingType) {
        String productName = getProductNameById(productId);
        double productPrice = getProductPriceById(productId);

        // Store internally
        currentTransactionList.add(
                new TransactionData(productId, orderId, customerId, productName, purchaseDate, iceAmount, toppingType));

        // Add only visible fields to the GUI
        currentTransactionModel.addRow(new Object[]{productName, productPrice, iceAmount, toppingType, "Remove"});

        updateTotalCost();
    }

    /**
     * Get customer transaction number to increment it (manual auto-increment)
     *
     * @return an increment of the previous transaction number, by one so no
     * transaction number will be the same.
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
                ps.setInt(1, nextTransactionNum++); // Use manually generated transaction number
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
            setLoggedInCustomerId(0);
            currentTransactionList.clear();

            updateTotalCost(); // update total cost after pay button.

        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error processing transaction: " + e.getMessage());
        }
    }

    /**
     * Fetch the product name based on product_id.
     *
     * @param productId - the id of the product from product database.
     * @return the name of the product based off the product_id
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

    // TODO: When setting up customer_rewards, be sure to use setLoggedInCustomerId

    /**
     * Set another variable as customerId
     *
     * @param customerId - the id of the customer
     */
    public static void setLoggedInCustomerId(int customerId) {
        loggedInCustomerId = customerId;

        for (TransactionData transaction : currentTransactionList) {
            transaction.customerId = customerId;
        }
    }

    /**
     * Get customer id if they are logged in into the system before paying.
     *
     * @return the customer's ID if logged in, otherwise a 0.
     */
    private static int getCustomerId() {
        return loggedInCustomerId != -1 ? loggedInCustomerId : 0;
    }

    // ===============================================
    // Transaction Table Remove Button
    // ===============================================

    /**
     * Creates the GUI for the Remove Button on the Transaction sidebar.
     *
     * @author Sebastian Chu
     */
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("X");
            setFont(new Font("Arial", Font.BOLD, 12));
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    /**
     * Creates the logic of the Remove button from the Transaction sidebar.
     *
     * @author Sebastian Chu
     */
    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private boolean isPushed;
        private int row; // Store the row index

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            button = new JButton("X");
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> {
                if (isPushed) {
                    stopCellEditing(); // Ensure editing is stopped before modifying the table

                    // Double-check that the row exists before removing
                    if (row >= 0 && row < table.getRowCount()) {
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                        if (row < currentTransactionList.size()) {
                            currentTransactionList.remove(row); // Remove from internal list
                        }

                        updateTotalCost(); // Update cost after removal from list
                    }
                }
                isPushed = false;
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                     int column) {
            isPushed = true;
            this.row = row; // Store the row index
            return button;
        }

        /**
         * When the button is clicked, change its value.
         *
         * @return string 'X' when button is edited
         */
        @Override
        public Object getCellEditorValue() {
            return "X";
        }

        /**
         * Disables editing of cell from the active table.
         *
         * @return true if function editing was stopped, false otherwise
         */
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

}