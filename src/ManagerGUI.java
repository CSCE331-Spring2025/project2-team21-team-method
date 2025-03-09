import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Builds the Manager GUI.
 */
public class ManagerGUI extends JPanel {

    /**
     * Creates and returns the manager GUI as a JPanel.
     * @param conn the database connection
     */
    public ManagerGUI(Connection conn) {
        //JFrame managerDashboard = new JFrame("Manager Dashboard");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch (Exception ex) {
                // Not worth my time
            }
        }
        setLayout(new BorderLayout());

        JTabbedPane tabsPane = new JTabbedPane();

        JPanel orderPanel = new JPanel();
        tabsPane.addTab("Order Page", orderPanel);

        JPanel trackingPanel = new JPanel();
        tabsPane.addTab("Tracking Page", trackingPanel);


        JPanel trendsPanel = buildTrendsPanel(conn);
        tabsPane.addTab("Trends Page", trendsPanel);

        JPanel employeePanel = new JPanel();
        tabsPane.addTab("Employee Page", employeePanel);

        JPanel productPanel = new JPanel();
        tabsPane.addTab("Product Page", productPanel);

        JPanel inventoryUsagePanel = buildInventoryUsagePanel(conn);
        tabsPane.addTab("Inventory Usage", inventoryUsagePanel);
        add(tabsPane, BorderLayout.CENTER);


        add(tabsPane, BorderLayout.CENTER);

        try {
            // building the order panel
            buildOrderPanel(conn, orderPanel);
            buildEmployeePanel(conn, employeePanel);
            buildProductPanel(conn, productPanel);
            buildTrackingPanel(conn, trackingPanel);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Builds the Inventory Panel for the Manager
     *
     * @param conn       Connection for the Database
     * @param orderPanel The panel that contains the inventory table
     */
    private static void buildOrderPanel(Connection conn, JPanel orderPanel) {
        DefaultTableModel inventoryTableModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Amount Left"}, 0);
        JTable inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setRowHeight(30);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        inventoryTable.setForeground(Color.DARK_GRAY);
        inventoryTable.setBackground(Color.WHITE);
        inventoryTable.setGridColor(Color.DARK_GRAY);

        JScrollPane inventoryTableScrollPane = new JScrollPane(inventoryTable);
        inventoryTableScrollPane.setPreferredSize(new Dimension(800, 600));

        buildInventoryTable(conn, inventoryTableModel);

        // creating an area where the manager can add update and delete items
        JPanel modifyItemsPanel = new JPanel(new GridBagLayout());
        modifyItemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Modify Inventory",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        JTextField id = new JTextField(3);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(20);

        JLabel idLabel = new JLabel("ItemId");
        JLabel nameLabel = new JLabel("Item Name");
        JLabel amountLabel = new JLabel("Item Amount");
        JLabel transactionLabel = new JLabel("Transaction ID");

        gbc.gridx = 0;
        gbc.gridy = 0;
        modifyItemsPanel.add(idLabel, gbc);
        gbc.gridx = 1;
        modifyItemsPanel.add(id, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyItemsPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        modifyItemsPanel.add(name, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        modifyItemsPanel.add(amountLabel, gbc);
        gbc.gridx = 3;
        modifyItemsPanel.add(amount, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        modifyItemsPanel.add(transactionLabel, gbc);
        gbc.gridx = 3;
        modifyItemsPanel.add(transactionId, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JButton addButton = new JButton("ADD");
        addButton.addActionListener(e -> {
            String tempId = id.getText().trim();
            String tempName = name.getText().trim();
            String tempAmount = amount.getText().trim();
            String tempTransactionId = transactionId.getText().trim();
            if (tempId.isEmpty() || tempName.isEmpty() || tempAmount.isEmpty() || tempTransactionId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields (Id, Name, Amount, Transaction ID).");
                return;
            }
            int numId = Integer.parseInt(tempId);
            int numAmount = Integer.parseInt(tempAmount);
            int numTransactionId = Integer.parseInt(tempTransactionId);

            // should I add checks for empty and value checking ?

            insertValueIntoDatabase(conn, numId, tempName, numAmount, numTransactionId);
            buildInventoryTable(conn, inventoryTableModel);
        });
        modifyItemsPanel.add(addButton);

        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(e -> {
            String tempId = id.getText().trim();
            String tempName = name.getText().trim();
            String tempAmount = amount.getText().trim();
            String tempTransactionId = transactionId.getText().trim();
            if (tempId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please the ID field.");
                return;
            }
            int numId = Integer.parseInt(tempId);
            int numAmount = -1;
            int numTransactionId = -1;
            if (!tempAmount.isEmpty()) {
                numAmount = Integer.parseInt(tempAmount);
            }
            if (!tempTransactionId.isEmpty()) {
                numTransactionId = Integer.parseInt(tempTransactionId);
            }
            // should I add checks for empty and value checking ?
            updateValueIntoDatabase(conn, numId, tempName, numAmount, numTransactionId);
            buildInventoryTable(conn, inventoryTableModel);
            //buildInventoryTable(conn, inventoryTableModel);
        });
        modifyItemsPanel.add(updateButton);


        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(e -> {
            String tempId = id.getText().trim();
            String tempAmount = amount.getText().trim();
            String tempTransactionId = transactionId.getText().trim();
            if (tempId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please the ID field.");
                return;
            }
            int numId = Integer.parseInt(tempId);
            int numAmount;
            int numTransactionId;
            if (!tempAmount.isEmpty()) {
                // weird bug must keep
                numAmount = Integer.parseInt(tempAmount);
            }
            if (!tempTransactionId.isEmpty()) {
                // weird bug must keep
                numTransactionId = Integer.parseInt(tempTransactionId);
            }
            deleteValueIntoDatabase(conn, numId);
            buildInventoryTable(conn, inventoryTableModel);
        });
        modifyItemsPanel.add(deleteButton);

        // New wrapper panel to permanently show the Modify Inventory section and buttons
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.add(inventoryTableScrollPane);
        wrapperPanel.add(modifyItemsPanel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        /// Button for closing business and Z-report generation ///
        JButton closeBusinessButton = new JButton("Close Business Day");
        closeBusinessButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeBusinessButton.setBackground(new Color(200, 0, 0));
        closeBusinessButton.setForeground(Color.WHITE);
        closeBusinessButton.addActionListener(e -> closeBusinessWorkflow(conn));
        /// Button Done ///

        bottomPanel.add(closeBusinessButton);

        // previous code with previous alignment for Modify Inventory
//        orderPanel.add(inventoryTableScrollPane, BorderLayout.CENTER);
//        orderPanel.add(modifyItemsPanel, BorderLayout.SOUTH);
//        orderPanel.add(closeBusinessButton, BorderLayout.SOUTH); // Last possible button
        orderPanel.setLayout(new BorderLayout());
        orderPanel.add(wrapperPanel, BorderLayout.CENTER);
        orderPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * This method updates the buildTrendsPanel method in ManagerGUI.java to include the sales report functionality.
     * It adds date filter fields and sales report section.
     */
    private static JPanel buildTrendsPanel(Connection conn) {
        JPanel trendsPanel = new JPanel(new GridBagLayout());
        trendsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // header contains title and refresh
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("X-Report: Sales");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh Report");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(refreshButton, BorderLayout.EAST);

        gbc.gridwidth = 2;
        trendsPanel.add(headerPanel, gbc);
        gbc.gridy++;

        // sales panel
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));

        DefaultTableModel salesTableModel = new DefaultTableModel(
                new String[]{"Hour", "Orders", "Total Sales", "Average Sale"}, 0
        );

        JTable salesTable = new JTable(salesTableModel);

        // set spacing with row height
        salesTable.setRowHeight(35);
        salesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        salesTable.setShowGrid(true);
        salesTable.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 14));

        salesTable.getColumnModel().getColumn(0).setPreferredWidth(200);  // Hour column
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Orders column
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Total Sales column
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Average Sale column

        JScrollPane tableScrollPane = new JScrollPane(salesTable);
        reportPanel.add(tableScrollPane, BorderLayout.CENTER);

        gbc.gridwidth = 2;
        trendsPanel.add(reportPanel, gbc);
        gbc.gridy++;

        // Bottom summary panel for totals
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));

        JLabel totalOrdersLabel = new JLabel("Total Orders:");
        JLabel totalSalesLabel = new JLabel("Total Sales:");
        JLabel averageSaleLabel = new JLabel("Average Ticket:");

        totalOrdersLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalOrdersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalSalesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalSalesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        averageSaleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        averageSaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        summaryPanel.add(totalOrdersLabel);
        summaryPanel.add(totalSalesLabel);
        summaryPanel.add(averageSaleLabel);

        gbc.gridwidth = 2;
        trendsPanel.add(summaryPanel, gbc);
        gbc.gridy++;

        // Populates data
        loadXReportData(conn, salesTableModel, totalOrdersLabel, totalSalesLabel, averageSaleLabel);

        // Refresh report button
        refreshButton.addActionListener(e -> {
            // Clear existing data before loading new data
            salesTableModel.setRowCount(0);
            loadXReportData(conn, salesTableModel, totalOrdersLabel, totalSalesLabel, averageSaleLabel);
        });

        // ================================
        // Sales Report Section - Added from sales-report.txt
        // ================================
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton filterButton = new JButton("Filter Sales");

        // Text area for sales data
        JTextArea salesByDateRange = new JTextArea();
        salesByDateRange.setEditable(false);
        salesByDateRange.setRows(10);
        salesByDateRange.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane salesScrollPane = new JScrollPane(salesByDateRange);

        // Panel for date inputs and button
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateFilterPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        dateFilterPanel.add(startDateField);
        dateFilterPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        dateFilterPanel.add(endDateField);
        dateFilterPanel.add(filterButton);

        // Add event listener for button click
        filterButton.addActionListener(e -> {
            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();

            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                try {
                    // Validate date format
                    java.sql.Date.valueOf(startDate);
                    java.sql.Date.valueOf(endDate);

                    JTextArea updatedSales = fetchSalesByDateRange(conn, startDate, endDate);
                    salesByDateRange.setText(updatedSales.getText());
                } catch (IllegalArgumentException ex) {
                    String message = "Please enter valid dates in YYYY-MM-DD format.";
                    salesByDateRange.setText(message);
                    JOptionPane.showMessageDialog(null, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                String message = "Please enter both start and end dates.";
                salesByDateRange.setText(message);
                JOptionPane.showMessageDialog(null, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add date filter panel
        gbc.gridy++;
        gbc.gridwidth = 2; // allow two columns for date filter panel
        trendsPanel.add(dateFilterPanel, gbc);

        // Add Sales Report section title
        gbc.gridy++;
        JLabel salesReportLabel = new JLabel("Sales by Date Range");
        salesReportLabel.setFont(new Font("Arial", Font.BOLD, 16));
        trendsPanel.add(salesReportLabel, gbc);

        // Add the sales report text area with scroll pane
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Allow this component to expand vertically
        trendsPanel.add(salesScrollPane, gbc);

        return trendsPanel;
    }

    /**
     * Loads hourly sales data for the current day into the table model
     *
     * @param conn             Database connection
     * @param tableModel       Table model to populate
     * @param totalOrdersLabel Label to update with total orders
     * @param totalSalesLabel  Label to update with total sales
     * @param averageSaleLabel Label to update with average sale
     */
    private static void loadXReportData(Connection conn, DefaultTableModel tableModel,
                                        JLabel totalOrdersLabel, JLabel totalSalesLabel, JLabel averageSaleLabel) {
        try {
            // getting the most recent generation of z-report/closing timestamp.
            String closureQuery = "SELECT MAX(closure_date) FROM business_closure_log";
            PreparedStatement closureStmt = conn.prepareStatement(closureQuery);
            ResultSet closureRs = closureStmt.executeQuery();

            Timestamp lastClosureTimestamp = null;

            if (closureRs.next()) {
                lastClosureTimestamp = closureRs.getTimestamp(1);
            }

            closureRs.close();
            closureStmt.close();

            if (lastClosureTimestamp == null) {
                LocalDate today = LocalDate.now();
                lastClosureTimestamp = Timestamp.valueOf(today.atStartOfDay());
            }

            // need number of reports, their hour, the total revenue, and also find correct purchase date
            String query =
                    "SELECT CAST(DATE_PART('hour', ct.purchase_date) AS INTEGER) AS hour, " +
                            "COUNT(ct.order_id) AS num_orders, " +
                            "SUM(p.product_cost) AS total_revenue " +
                            "FROM customer_transaction ct " +
                            "JOIN product p ON ct.product_id = p.product_id " +
                            "WHERE ct.purchase_date > ? " +                          //HERE FIX
                            "GROUP BY DATE_PART('hour', ct.purchase_date) " +
                            "ORDER BY hour";
            // order by hour for the report to automatically just get each entry

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setTimestamp(1, lastClosureTimestamp);

            ResultSet rs = ps.executeQuery();

            int totalOrders = 0;
            double totalSales = 0;

            tableModel.setRowCount(0);

            while (rs.next()) {
                int hour = rs.getInt("hour");
                int orderCount = rs.getInt("num_orders");
                double salesTotal = rs.getDouble("total_revenue");
                double avgSale = orderCount > 0 ? salesTotal / orderCount : 0;

                totalOrders += orderCount;
                totalSales += salesTotal;

                String hourDisplay = formatHourDisplay(hour);

                tableModel.addRow(new Object[]{
                        hourDisplay,
                        orderCount,
                        String.format("$%.2f", salesTotal),
                        String.format("$%.2f", avgSale)
                });
            }

            // Update the summary labels
            double avgTicket = totalOrders > 0 ? totalSales / totalOrders : 0;
            totalOrdersLabel.setText("Total Orders: " + totalOrders);
            totalSalesLabel.setText(String.format("Total Sales: $%.2f", totalSales));
            averageSaleLabel.setText(String.format("Average Ticket: $%.2f", avgTicket));

            // Edge case: no sales, add a row that says no sales
            if (tableModel.getRowCount() == 0) {
                tableModel.addRow(new Object[]{"No sales recorded today", "-", "-", "-"});
            }

            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading the X-Report");
        }
    }

    /**
     * Formats an hour number into a readable time display
     *
     * @param hour Hour as 0-23 integer
     * @return Formatted hour string (e.g. "9:00 AM")
     */
    private static String formatHourDisplay(int hour) {
        String amPm1 = hour < 12 ? "AM" : "PM";

        int displayHour1 = hour % 12;
        if (displayHour1 == 0) displayHour1 = 12;

        return String.format("%d:00 %s", displayHour1, amPm1);
    }

    /**
     * Creates the logic for Sales Report, where I list out all drinks sales from a beginning date to the end of the provided date.
     *
     * @param conn      - Connection to the database.
     * @param startDate - The first input date to beginning the search from this start date.
     * @param endDate   - The second input date to end the search from this end date.
     * @return A text area formatted as a table to dislpay all products and its total cost, and quantity sold.
     */
    private static JTextArea fetchSalesByDateRange(Connection conn, String startDate, String endDate) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        String query = "SELECT ct.product_id, p.product_name, p.product_type, " +
                "SUM(p.product_cost) AS total_cost, " +
                "COUNT(*) AS quantity_sold " +
                "FROM customer_transaction ct " +
                "JOIN product p ON ct.product_id = p.product_id " +
                "WHERE ct.purchase_date BETWEEN CAST(? AS DATE) AND CAST(? AS DATE) " +
                "GROUP BY ct.product_id, p.product_name, p.product_type";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            // Converting String date to SQL Date.
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));

            // Builds the text area as a table
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Product ID | Product Name | Product Type | Total Cost | Quantity Sold\n");
                sb.append("--------------------------------------------------------------\n");

                while (rs.next()) {
                    sb.append(rs.getInt("product_id")).append(" | ")
                            .append(rs.getString("product_name")).append(" | ")
                            .append(rs.getString("product_type")).append(" | $")
                            .append(String.format("%.2f", rs.getDouble("total_cost"))).append(" | ")
                            .append(rs.getInt("quantity_sold")).append("\n");
                }

                textArea.setText(!sb.isEmpty() ? sb.toString() : "No sales data found for the selected date range.");
            }
        }
        catch (SQLException e) {
            textArea.setText("Error retrieving sales data: " + e.getMessage());
        } // TODO: never gets called or shown when I throw invalid arguments.

        return textArea;
    }


    /**
     * Populates the inventory table with current inventory data from the database.
     *
     * @param conn the database connection
     * @param inventoryTableModel the table model to populate with inventory data
     */
    private static void buildInventoryTable(Connection conn, DefaultTableModel inventoryTableModel) {
        inventoryTableModel.setRowCount(0);
        String query = "SELECT item_id, item_name, amount FROM inventory order by item_id";
        String id;
        String name;
        String amount;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                id = String.valueOf(rs.getInt(1));
                name = rs.getString(2);
                amount = String.valueOf(rs.getInt(3));

                String[] temp_row = new String[]{id, name, amount};
                inventoryTableModel.addRow(temp_row);
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading the inventory table");
        }
    }

    /**
     * Inserts a new inventory item into the database.
     *
     * @param conn the database connection
     * @param id the unique ID for the new inventory item
     * @param name the name of the inventory item
     * @param amount the quantity of the inventory item
     * @param transactionId the associated transaction ID
     */
    private static void insertValueIntoDatabase(Connection conn, int id, String name, int amount, int transactionId) {
        try (
                PreparedStatement ps = conn.prepareStatement(
                        "insert into inventory(item_id, item_name, amount,transaction_id) values (?,?,?,?)"
                )) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, amount);
            ps.setInt(4, transactionId);
            ps.execute();
            JOptionPane.showMessageDialog(null, " Item adding successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding inventory item: " + e.getMessage());
        }
    }

    /**
     * Updates an existing inventory item's information in the database.
     *
     * @param conn the database connection
     * @param id the ID of the inventory item to update
     * @param name the new name for the inventory item
     * @param amount the new quantity for the inventory item
     * @param transactionId the new associated transaction ID
     */
    private static void updateValueIntoDatabase(Connection conn, int id, String name, int amount, int transactionId) {

        String query = "SELECT  item_name,amount,transaction_id FROM inventory where item_id = ?";

        String prevName = "";
        int prevAmount = -1;
        int prevTransaction = -1;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prevName = rs.getString(1);
                    prevAmount = rs.getInt(2);
                    prevTransaction = rs.getInt(3);
                }
                else {
                    JOptionPane.showMessageDialog(null, "No item found with ID: " + id);
                }
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading the inventory table: " + e.getMessage());
        }
        if (name.isEmpty()) {
            name = prevName;
        }
        if (amount == -1) {
            amount = prevAmount;
        }
        if (transactionId == -1) {
            transactionId = prevTransaction;
        }
        try (
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE inventory SET item_name = ?, amount = ?, transaction_id = ? WHERE item_id = ?"
                )) {

            ps.setString(1, name);
            ps.setInt(2, amount);
            ps.setInt(3, transactionId);
            ps.setInt(4, id);
            ps.execute();
            JOptionPane.showMessageDialog(null, " Item updated successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating inventory item: " + e.getMessage());
        }
    }


    /**
     * Deletes an inventory item from the database.
     *
     * @param conn the database connection
     * @param id the ID of the inventory item to delete
     */
    private static void deleteValueIntoDatabase(Connection conn, int id) {
        // we need ti check if the value exists in menu_items_inventory as well adn delte form there as well
        try (
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM inventory WHERE item_id = ?"
                )) {

            ps.setInt(1, id);
            //ps.setString(2, name);
            //ps.setInt(3, amount);
            //ps.setInt(4, transactionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, " Item deleted successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting inventory item: " + e.getMessage());
        }
    }


    /**
     * Builds the employee management panel.
     *
     * @param conn the database connection
     * @param employeePanel the panel to build the employee management interface on
     */
    private static void buildEmployeePanel(Connection conn, JPanel employeePanel) {
        DefaultTableModel employeeTableModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Email", "Employee Phone", "Is Manager", "Social Security", "Employee Pay", "Bank Account"}, 0
        );

        JTable employeeTable = new JTable(employeeTableModel);
        JScrollPane employeeTableScrollPane = new JScrollPane(employeeTable);
        employeeTableScrollPane.setPreferredSize(new Dimension(800, 350));
        buildEmployeeTable(conn, employeeTableModel);

        employeeTable.setRowHeight(30);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        employeeTable.setForeground(Color.DARK_GRAY);
        employeeTable.setBackground(Color.WHITE);
        employeeTable.setGridColor(Color.DARK_GRAY);


        // creating an area where the manager can add update and delete items
        JPanel modifyEmployeePanel = new JPanel(new GridBagLayout());
        modifyEmployeePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Modify EmployeeInfo",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        JTextField employeeIdField = new JTextField(5);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(10);
        JCheckBox isManagerCheck = new JCheckBox("Yes/No");
        JTextField socialSecurityField = new JTextField(10);
        JTextField payField = new JTextField(10);
        JTextField bankAccountField = new JTextField(10);

        JLabel idLabel = new JLabel("Employee Id");
        JLabel emailLabel = new JLabel("Email");
        JLabel phoneLabel = new JLabel("Phone Number");
        JLabel managerLabel = new JLabel("Manager");
        JLabel socialLabel = new JLabel("Social Security");
        JLabel payLabel = new JLabel("Pay");
        JLabel bankLabel = new JLabel("Bank Account ID");


        gbc.gridx = 0;
        gbc.gridy = 0;
        modifyEmployeePanel.add(idLabel, gbc);
        gbc.gridx = 1;
        modifyEmployeePanel.add(employeeIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyEmployeePanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        modifyEmployeePanel.add(emailField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        modifyEmployeePanel.add(phoneLabel, gbc);
        gbc.gridx = 3;
        modifyEmployeePanel.add(phoneField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        modifyEmployeePanel.add(socialLabel, gbc);
        gbc.gridx = 3;
        modifyEmployeePanel.add(socialSecurityField, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        modifyEmployeePanel.add(payLabel, gbc);
        gbc.gridx = 5;
        modifyEmployeePanel.add(payField, gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        modifyEmployeePanel.add(bankLabel, gbc);
        gbc.gridx = 5;
        modifyEmployeePanel.add(bankAccountField, gbc);

        gbc.gridx = 4;
        gbc.gridy = 2;
        modifyEmployeePanel.add(managerLabel, gbc);
        gbc.gridx = 5;
        modifyEmployeePanel.add(isManagerCheck, gbc);

        JButton addButton = new JButton("ADD");

        addButton.addActionListener(e -> {
            String tempId = employeeIdField.getText().trim();
            String tempEmail = emailField.getText().trim();
            String tempPhone = phoneField.getText().trim();
            String tempSSN = socialSecurityField.getText().trim();
            String tempPay = payField.getText().trim();
            String tempBankAccount = bankAccountField.getText().trim();
            boolean isManager = isManagerCheck.isSelected();

            if (tempId.isEmpty() || tempEmail.isEmpty() || tempPhone.isEmpty() || tempSSN.isEmpty() || tempPay.isEmpty() || tempBankAccount.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields.");
                return;
            }

            int employeeId = Integer.parseInt(tempId);
            int empPhone = Integer.parseInt(tempPhone);
            int socialSecurity = Integer.parseInt(tempSSN);
            double empPay = Double.parseDouble(tempPay);
            int empBankAccount = Integer.parseInt(tempBankAccount);

            insertEmpIntoDatabase(conn, employeeId, tempEmail, empPhone, isManager, socialSecurity, empPay, empBankAccount);
            buildEmployeeTable(conn, employeeTableModel);
        });
        modifyEmployeePanel.add(addButton);


        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(e -> {
            String tempId = employeeIdField.getText().trim();
            String tempEmail = emailField.getText().trim();
            String tempPhone = phoneField.getText().trim();
            String tempSSN = socialSecurityField.getText().trim();
            String tempPay = payField.getText().trim();
            String tempBankAccount = bankAccountField.getText().trim();
            boolean isManager = isManagerCheck.isSelected();

            if (tempId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter the Employee ID.");
                return;
            }

            int employeeId = Integer.parseInt(tempId);
            int empPhone = tempPhone.isEmpty() ? -1 : Integer.parseInt(tempPhone);
            int socialSecurity = tempSSN.isEmpty() ? -1 : Integer.parseInt(tempSSN);
            double empPay = tempPay.isEmpty() ? -1.0 : Double.parseDouble(tempPay);
            int empBankAccount = tempBankAccount.isEmpty() ? -1 : Integer.parseInt(tempBankAccount);

            updateEmpIntoDatabase(conn, employeeId, tempEmail, empPhone, isManager, socialSecurity, empPay, empBankAccount);
            buildEmployeeTable(conn, employeeTableModel);
        });
        modifyEmployeePanel.add(updateButton);


        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(e -> {
            String tempId = employeeIdField.getText().trim();
            if (tempId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter the Employee ID.");
                return;
            }

            int employeeId = Integer.parseInt(tempId);
            deleteEmpIntoDatabase(conn, employeeId);
            buildEmployeeTable(conn, employeeTableModel);
        });
        modifyEmployeePanel.add(deleteButton);

        // Adding components to the Employee Panel
        employeePanel.add(employeeTableScrollPane, BorderLayout.CENTER);
        employeePanel.add(modifyEmployeePanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the employee table with current employee data from the database.
     *
     * @param conn the database connection
     * @param employeeTableModel the table model to populate with employee data
     */
    private static void buildEmployeeTable(Connection conn, DefaultTableModel employeeTableModel) {
        employeeTableModel.setRowCount(0);
        String query = "SELECT employee_id, emp_email, emp_phone, is_manager, social_security, emp_pay, emp_bank_account FROM employee order by employee_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                // Retrieve and convert each column to a String

                String employeeId = String.valueOf(rs.getInt(1));
                String empEmail = rs.getString(2);
                String empPhone = String.valueOf(rs.getLong(3));
                String isManager = String.valueOf(rs.getBoolean(4));
                String socialSecurity = String.valueOf(rs.getLong(5));
                String empPay = String.valueOf(rs.getDouble(6));
                String empBankAccount = String.valueOf(rs.getInt(7));

                // Create an array with all string values
                String[] tempRow = new String[]{
                        employeeId,
                        empEmail,
                        empPhone,
                        isManager,
                        socialSecurity,
                        empPay,
                        empBankAccount
                };

                // Add the row to your table model
                employeeTableModel.addRow(tempRow);
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error laoding the employee table" + e.getMessage());
        }
    }

    /**
     * Inserts a new employee record into the database.
     *
     * @param conn the database connection
     * @param employeeId the unique ID for the new employee
     * @param empEmail email address
     * @param empPhone phone number
     * @param isManager if the employee is a manager
     * @param socialSecurity the employee's social security number
     * @param empPay the employee's pay rate
     * @param empBankAccount the employee's bank account number
     */
    private static void insertEmpIntoDatabase(Connection conn, int employeeId, String empEmail, int empPhone, boolean isManager, int socialSecurity, double empPay, int empBankAccount) {
        try (
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO employee (employee_id, emp_email, emp_phone, is_manager, social_security, emp_pay, emp_bank_account) VALUES (?, ?, ?, ?, ?, ?, ?)"

                )) {

            ps.setInt(1, employeeId);
            ps.setString(2, empEmail);
            ps.setInt(3, empPhone);
            ps.setBoolean(4, isManager);
            ps.setInt(5, socialSecurity);
            ps.setDouble(6, empPay);
            ps.setInt(7, empBankAccount);
            ps.execute();
            JOptionPane.showMessageDialog(null, " Item adding successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding inventory item: " + e.getMessage());
        }
    }

    /**
     * Updates an existing employee's information in the database.
     *
     * @param conn the database connection
     * @param employeeId the ID of the employee to update
     * @param empEmail the new email address for the employee
     * @param empPhone the new phone number for the employee
     * @param isManager if the employee is manager
     * @param socialSecurity the new social security number for the employee
     * @param empPay the new pay rate for the employee
     * @param empBankAccount the new bank account number for the employee
     */
    private static void updateEmpIntoDatabase(Connection conn, int employeeId, String empEmail, long empPhone, boolean isManager, int socialSecurity, double empPay, int empBankAccount) {

        //String query = "SELECT  item_name, amount,transaction_id FROM inventory where item_id = ?";
        String query = "SELECT employee_id,emp_email, emp_phone, is_manager, social_security, emp_pay, emp_bank_account FROM employee WHERE employee_id = ?";


        String prevEmpEmail = "";
        int prevEmployeeId = -1;
        long prevEmpPhone = -1;
        boolean prevIsManager = false;
        int prevSocialSecurity = -1;
        double prevEmpPay = -1.0;
        int prevEmpBackAccount = -1;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prevEmployeeId = rs.getInt(1);
                    prevEmpEmail = rs.getString(2);
                    prevEmpPhone = rs.getLong(3);
                    prevIsManager = rs.getBoolean(4);
                    prevSocialSecurity = rs.getInt(5);
                    prevEmpPay = rs.getDouble(6);
                    prevEmpBackAccount = rs.getInt(7);
                }
                else {
                    JOptionPane.showMessageDialog(null, "No item found with ID: " + employeeId);
                }
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading the employee table: " + e.getMessage());
        }
        if (empEmail.isEmpty()) {
            empEmail = prevEmpEmail;
        }
        if (employeeId == -1) {
            employeeId = prevEmployeeId;
        }
        if (empPhone == -1) {
            empPhone = prevEmpPhone;
        }
        if (!isManager) {
            isManager = prevIsManager;
        }
        if (socialSecurity == -1) {
            socialSecurity = prevSocialSecurity;
        }
        if (empPay == -1.0) {
            empPay = prevEmpPay;
        }
        if (empBankAccount == -1) {
            empBankAccount = prevEmpBackAccount;
        }

        try (
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE employee SET emp_email = ?, emp_phone = ?, is_manager = ?, social_security = ?, emp_pay = ?, emp_bank_account = ? WHERE employee_id = ?"
                )) {

            ps.setString(1, empEmail);
            ps.setLong(2, empPhone);
            ps.setBoolean(3, isManager);
            ps.setInt(4, socialSecurity);
            ps.setDouble(5, empPay);
            ps.setInt(6, empBankAccount);
            ps.setInt(7, employeeId);
            ps.execute();
            JOptionPane.showMessageDialog(null, " Item updated successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating employee item: " + e.getMessage());
        }
    }

    /**
     * Deletes an employee record from the database.
     *
     * @param conn the database connection
     * @param employeeId the ID of the employee to delete
     */
    private static void deleteEmpIntoDatabase(Connection conn, int employeeId) {
        // we need ti check if the value exists in menu_items_inventory as well adn delte form there as well
        try (
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM employee WHERE employee_id = ?"
                )) {

            ps.setInt(1, employeeId);
            //ps.setString(2, name);
            //ps.setInt(3, amount);
            //ps.setInt(4, transactionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, " Item deleted successfully");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting inventory item: " + e.getMessage());
        }
    }

    /**
     * Builds the product management panel.
     *
     * @param conn the database connection
     * @param productPanel the panel to build the product management interface on
     */
    private static void buildProductPanel(Connection conn, JPanel productPanel) {

        DefaultTableModel productTableModel = new DefaultTableModel(
                new String[]{"Product ID", "Product Name", "Cost", "Type"}, 0
        );
        JTable productTable = new JTable(productTableModel);
        JScrollPane productTableScrollPane = new JScrollPane(productTable);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.setForeground(Color.DARK_GRAY);
        productTable.setBackground(Color.WHITE);
        productTable.setGridColor(Color.DARK_GRAY);
        productTableScrollPane.setPreferredSize(new Dimension(800, 500));
        buildProductTable(conn, productTableModel);

        JPanel modifyProductsPanel = new JPanel(new GridBagLayout());
        modifyProductsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Modify Products",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        modifyProductsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Text fields for product ID, name, cost, and type
        JTextField idField = new JTextField(3);
        JTextField nameField = new JTextField(20);
        JTextField costField = new JTextField(5);
        JTextField typeField = new JTextField(10);

        // Add labels and fields to the modifyProductsPanel
        JLabel prodLabel = new JLabel("Product ID:");
        JLabel prodName = new JLabel("Name:");
        JLabel prodCost = new JLabel("Product Cost:");
        JLabel prodType = new JLabel("Product Type:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        modifyProductsPanel.add(prodLabel, gbc);
        gbc.gridx = 1;
        modifyProductsPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyProductsPanel.add(prodName, gbc);
        gbc.gridx = 1;
        modifyProductsPanel.add(nameField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        modifyProductsPanel.add(prodCost, gbc);
        gbc.gridx = 3;
        modifyProductsPanel.add(costField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        modifyProductsPanel.add(prodType, gbc);
        gbc.gridx = 3;
        modifyProductsPanel.add(typeField, gbc);


        JButton insertButton = new JButton("INSERT");
        insertButton.addActionListener(e -> {
            // Gather values from text fields
            String temp_id = idField.getText().trim();
            String temp_name = nameField.getText().trim();
            String temp_cost = costField.getText().trim();
            String temp_type = typeField.getText().trim();
            if (temp_id.isEmpty() || temp_name.isEmpty() || temp_cost.isEmpty() || temp_type.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields (ID, Name, Cost, Type).");
                return;
            }
            try {
                int productId = Integer.parseInt(temp_id);
                float cost = Float.parseFloat(temp_cost);

                insertProductIntoDatabase(conn, productId, temp_name, cost, temp_type);
                buildProductTable(conn, productTableModel);

                idField.setText("");
                nameField.setText("");
                costField.setText("");
                typeField.setText("");
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,
                        "Product ID must be an integer and Cost must be a valid number."
                );
            }
        });
        modifyProductsPanel.add(insertButton);

        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(e -> {
            String temp_id = idField.getText().trim();
            String temp_name = nameField.getText().trim();
            String temp_cost = costField.getText().trim();
            String temp_type = typeField.getText().trim();

            // Product ID is required for update
            if (temp_id.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Please fill the Product ID field for update."
                );
                return;
            }

            int productId;
            float costVal = -1; // sentinel for "no cost entered"
            try {
                productId = Integer.parseInt(temp_id);
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Product ID must be an integer.");
                return;
            }

            // If cost field is not empty, parse it
            if (!temp_cost.isEmpty()) {
                try {
                    costVal = Float.parseFloat(temp_cost);
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Cost must be a valid number.");
                    return;
                }
            }

            // Retrieve current DB values if fields are left blank
            String prevName;
            float prevCost;
            String prevType;
            String selectQuery = "SELECT product_name, product_cost, product_type FROM product WHERE product_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        prevName = rs.getString("product_name");
                        prevCost = rs.getFloat("product_cost");
                        prevType = rs.getString("product_type");
                    }
                    else {
                        JOptionPane.showMessageDialog(null,
                                "No product found with ID: " + productId
                        );
                        return;
                    }
                }
            }
            catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error fetching current product details: " + ex.getMessage()
                );
                return;
            }

            // Use previous values if the new fields are empty
            String newName = temp_name.isEmpty() ? prevName : temp_name;
            float newCost = (costVal == -1) ? prevCost : costVal;
            String newType = temp_type.isEmpty() ? prevType : temp_type;

            // Update the product in the database
            updateProductIntoDatabase(conn, productId, newName, newCost, newType);

            // Refresh the table
            buildProductTable(conn, productTableModel);

            idField.setText("");
            nameField.setText("");
            costField.setText("");
            typeField.setText("");
        });
        modifyProductsPanel.add(updateButton);

        JPanel modifyItemsPanel = new JPanel(new GridBagLayout());
        modifyItemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Modify Inventory",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        modifyItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        //GridBagConstraints gbc = new GridBagConstraints();
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.insets = new Insets(3,3,3,3);

        JTextField id = new JTextField(3);
        JTextField prodId = new JTextField(20);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(10);

        JLabel idLabel = new JLabel("Item Id:");
        JLabel nameLabel = new JLabel("Item Name:");
        JLabel amountLabel = new JLabel("Item Amount");
        JLabel transactionLabel = new JLabel("Transaction ID");
        //JLabel prodLabel1 = new JLabel("Product Id:");

        gbc.gridx = 0;
        gbc.gridy = 0;
        modifyItemsPanel.add(idLabel, gbc);
        gbc.gridx = 1;
        modifyItemsPanel.add(id, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyItemsPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        modifyItemsPanel.add(name, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        modifyItemsPanel.add(amountLabel, gbc);
        gbc.gridx = 3;
        modifyItemsPanel.add(amount, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        modifyItemsPanel.add(transactionLabel, gbc);
        gbc.gridx = 3;
        modifyItemsPanel.add(transactionId, gbc);


        JButton addButton = new JButton("INSERT");
        addButton.addActionListener(e -> {
            String tempId = id.getText().trim();
            String tempName = name.getText().trim();
            String tempAmount = amount.getText().trim();
            String tempTransactionId = transactionId.getText().trim();

            if (tempId.isEmpty() || tempName.isEmpty() || tempAmount.isEmpty() || tempTransactionId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields (Id, Name, Amount, Transaction ID");
                return;
            }
            int numId = Integer.parseInt(tempId);
            int numAmount = Integer.parseInt(tempAmount);
            int numTransactionId = Integer.parseInt(tempTransactionId);


            // should I add checks for empty and value checking ?

            insertValueIntoDatabase(conn, numId, tempName, numAmount, numTransactionId);
            //buildInventoryTable(conn, inventoryTableModel);

        });
        modifyItemsPanel.add(addButton);

        JButton updateButton1 = new JButton("UPDATE");
        updateButton1.addActionListener(e -> {
            String tempId = id.getText().trim();
            String tempName = name.getText().trim();
            String tempAmount = amount.getText().trim();
            String tempTransactionId = transactionId.getText().trim();
            if (tempId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please the ID field.");
                return;
            }
            int numId = Integer.parseInt(tempId);
            int numAmount = -1;
            int numTransactionId = -1;
            if (!tempAmount.isEmpty()) {
                numAmount = Integer.parseInt(tempAmount);
            }
            if (!tempTransactionId.isEmpty()) {
                numTransactionId = Integer.parseInt(tempTransactionId);
            }
            // should I add checks for empty and value checking ?
            updateValueIntoDatabase(conn, numId, tempName, numAmount, numTransactionId);
            //buildInventoryTable(conn, productTableModel);
            //buildInventoryTable(conn, inventoryTableModel);
        });
        modifyItemsPanel.add(updateButton1);


        // this is the menu inventory part GAHHAHHHA
        JPanel modifyMenuInventoryPanel = new JPanel(new GridBagLayout());
        modifyMenuInventoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Modify Menu Inventory Table",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        modifyMenuInventoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel itemIdLabel = new JLabel("Item Id's:");
        JLabel quantityLabel = new JLabel("Amount per Item");
        JLabel headingLabel = new JLabel("Please separate each value for the Items and Amounts field with a comma");
        JLabel prodLabel1 = new JLabel("Product Id:");

        JTextField items = new JTextField(30);
        JTextField quantities = new JTextField(30);
        JTextField prodId1 = new JTextField(5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        modifyMenuInventoryPanel.add(headingLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyMenuInventoryPanel.add(prodLabel1, gbc);
        gbc.gridx = 1;
        modifyMenuInventoryPanel.add(prodId1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        modifyMenuInventoryPanel.add(itemIdLabel, gbc);
        gbc.gridx = 1;
        modifyMenuInventoryPanel.add(items, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        modifyMenuInventoryPanel.add(quantityLabel, gbc);
        gbc.gridx = 1;
        modifyMenuInventoryPanel.add(quantities, gbc);

        //change here
        JButton addButtonMenu = new JButton("INSERT");
        addButtonMenu.addActionListener(e -> {
            String tempProdId = prodId1.getText().trim();
            String tempItems = items.getText().trim();
            String tempQuantity = quantities.getText().trim();


            if (tempProdId.isEmpty() || tempItems.isEmpty() || tempQuantity.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields");
                return;
            }

            String[] tempItemsArray = tempItems.split(",");
            String[] tempQuantArray = tempQuantity.split(",");

            int[] itemsArray = new int[tempItemsArray.length];
            int[] quantityArray = new int[tempQuantArray.length];

            int numId = Integer.parseInt(tempProdId);

            for (int i = 0; i < tempItemsArray.length; i++) {
                itemsArray[i] = Integer.parseInt(tempItemsArray[i].trim());
                quantityArray[i] = Integer.parseInt(tempQuantArray[i].trim());
                insertValueIntoMenuTable(conn, numId, itemsArray[i], quantityArray[i]);
            }
            JOptionPane.showMessageDialog(null, "Product and its respective menu items inserted successfully!");
        });
        modifyMenuInventoryPanel.add(addButtonMenu);


        productPanel.setLayout(new BorderLayout());
        /*productPanel.add(productTableScrollPane, BorderLayout.CENTER);
        productPanel.add(modifyProductsPanel, BorderLayout.SOUTH);
         */
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(modifyProductsPanel);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        containerPanel.add(modifyItemsPanel);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        containerPanel.add(modifyMenuInventoryPanel);


        productPanel.add(productTableScrollPane, BorderLayout.CENTER);
        productPanel.add(containerPanel, BorderLayout.SOUTH);

    }

    /**
     * Populates the product table with current product data from the database.
     *
     * @param conn the database connection
     * @param productTableModel the table model to populate with product data
     */
    private static void buildProductTable(Connection conn, DefaultTableModel productTableModel) {
        // Clear existing rows
        productTableModel.setRowCount(0);

        String query = "SELECT product_id, product_name, product_cost, product_type FROM product order by product_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("product_id"));
                String name = rs.getString("product_name");
                String cost = String.valueOf(rs.getFloat("product_cost"));
                String type = rs.getString("product_type");

                // Add row to the table model
                productTableModel.addRow(new String[]{id, name, cost, type});
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading product table: " + e.getMessage()
            );
        }
    }


    /**
     * Inserts a new product into the database.
     *
     * @param conn the database connection
     * @param productId the unique ID for the new product
     * @param name the name of the product
     * @param cost the cost/price of the product
     * @param type the type/category of the product
     */
    private static void insertProductIntoDatabase(
            Connection conn, int productId, String name, float cost, String type
    ) {
        String insertSQL = "INSERT INTO product (product_id, product_name, product_cost, product_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            ps.setInt(1, productId);
            ps.setString(2, name);
            ps.setFloat(3, cost);
            ps.setString(4, type);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Product inserted successfully!");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error inserting product: " + e.getMessage()
            );
        }
    }


    /**
     * Updates an existing product's information in the database.
     *
     * @param conn the database connection
     * @param productId the ID of the product to update
     * @param name the new name for the product
     * @param cost the new cost/price for the product
     * @param type the new type/category for the product
     */
    private static void updateProductIntoDatabase(
            Connection conn, int productId, String name, float cost, String type
    ) {
        String updateSQL = "UPDATE product SET product_name = ?, product_cost = ?, product_type = ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
            ps.setString(1, name);
            ps.setFloat(2, cost);
            ps.setString(3, type);
            ps.setInt(4, productId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Product updated successfully!");
            }
            else {
                JOptionPane.showMessageDialog(null, "No product found with ID: " + productId);
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error updating product: " + e.getMessage()
            );
        }
    }

    /**
     * Builds and populates the tracking panel with shipment tracking information.
     *
     * @param conn the database connection
     * @param trackingPanel the panel to build the tracking information UI on
     */
    private static void buildTrackingPanel(Connection conn, JPanel trackingPanel) {
        // Create a table model for tracking data
        DefaultTableModel trackingTableModel = new DefaultTableModel(
                new String[]{"Transaction ID", "Tracking Number", "Vendor ID", "Estimated Delivery"}, 0
        );
        JTable trackingTable = new JTable(trackingTableModel);
        JScrollPane trackingScrollPane = new JScrollPane(trackingTable);
        trackingTable.setRowHeight(30);
        trackingTable.setFont(new Font("Arial", Font.PLAIN, 12));
        trackingTable.setForeground(Color.DARK_GRAY);
        trackingTable.setBackground(Color.WHITE);
        trackingTable.setGridColor(Color.DARK_GRAY);
        trackingScrollPane.setPreferredSize(new Dimension(800, 500));
        buildTrackingTable(conn, trackingTableModel);

        trackingPanel.setLayout(new BorderLayout());
        trackingPanel.add(trackingScrollPane, BorderLayout.CENTER);
    }

    /**
     * Populates the tracking table with shipment data from the database.
     *
     * @param conn the database connection
     * @param model the table model to populate with tracking data
     */
    private static void buildTrackingTable(Connection conn, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT transaction_id, tracking_number, vendor_id, estimated_delivery FROM company_transaction";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String tID = String.valueOf(rs.getInt("transaction_id"));
                String tNumber = String.valueOf(rs.getInt("tracking_number"));
                String vID = String.valueOf(rs.getInt("vendor_id"));
                // If estimated_delivery is a date or timestamp, you can do:
                String estDelivery = String.valueOf(rs.getObject("estimated_delivery"));
                // Or simply rs.getString("estimated_delivery") if it's a text column

                model.addRow(new String[]{tID, tNumber, vID, estDelivery});
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading tracking table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new product-inventory item relationship into the menu_item_inventory table.
     *
     * @param conn the database connection
     * @param prodId the product ID
     * @param itemId the inventory item ID
     * @param quantity the quantity of the item used in the product
     */
    private static void insertValueIntoMenuTable(Connection conn, int prodId, int itemId, int quantity) {
        {
            String insertSQL = "INSERT INTO menu_item_inventory (product_id, item_id, quantity_used) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, prodId);
                ps.setInt(2, itemId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Error inserting product: " + e.getMessage()
                );
            }
        }
    }

    /**
     * Generates a Z-Report with end of business day.
     *
     * @param conn the database connection
     */
    private static void closeBusinessWorkflow(Connection conn) {
        String report = generateZReport(conn);

        int confirm = JOptionPane.showConfirmDialog(null, report + "\n\nConfirm close of business?",
                "Close Business Day", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                logBusinessClosure(conn);

                JOptionPane.showMessageDialog(null, "Business day over. Shutting down...");
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error closing business: " + e.getMessage());
            }
        }
    }

    /**
     * Resets the X-Report data by clearing all current transaction information.
     */
    private static void resetXReport() {
        clearTransactions();
    }


    /**
     * Clears all current transactions from the transaction list and table model.
     */
    public static void clearTransactions() {
        CashierGUI.currentTransactionList.clear();
        CashierGUI.currentTransactionModel.setRowCount(0);
    }

    /**
     * Generates a Z-Report.
     *
     * @param conn the database connection
     * @return the Z-Report data
     */
    private static String generateZReport(Connection conn) {
        StringBuilder report = new StringBuilder("Z-Report - Daily Sales Summary\n");

        try {
            // Daily sale total
            String salesQuery = "SELECT SUM(p.product_cost) AS total_sales FROM customer_transaction ct " +
                    "JOIN product p ON ct.product_id = p.product_id WHERE DATE(ct.purchase_date) = CURRENT_DATE";
            PreparedStatement ps = conn.prepareStatement(salesQuery);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                report.append("Total Sales: $").append(rs.getDouble("total_sales")).append("\n");
            }

            // All of today's transaction
            String transactionsQuery = "SELECT COUNT(*) AS total_orders FROM customer_transaction WHERE DATE(purchase_date) = CURRENT_DATE";
            ps = conn.prepareStatement(transactionsQuery);
            rs = ps.executeQuery();
            if (rs.next()) {
                report.append("Total Transactions: ").append(rs.getInt("total_orders")).append("\n");
            }

            // Today's most popular item
            String topItemQuery = "SELECT p.product_name, COUNT(*) AS order_count FROM customer_transaction ct " +
                    "JOIN product p ON ct.product_id = p.product_id WHERE DATE(ct.purchase_date) = CURRENT_DATE " +
                    "GROUP BY p.product_name ORDER BY order_count DESC LIMIT 1";
            ps = conn.prepareStatement(topItemQuery);
            rs = ps.executeQuery();
            if (rs.next()) {
                report.append("Most Sold Item: ").append(rs.getString("product_name"))
                        .append(" (").append(rs.getInt("order_count")).append(" orders)\n");
            }

        }
        catch (SQLException e) {
            return "Error generating Z-Report: " + e.getMessage();
        }

        return report.toString();
    }

    /**
     * Records the business closure in the database with the current timestamp.
     *
     * @param conn the database connection
     * @throws RuntimeException if there is an error logging the business closure
     */
    private static void logBusinessClosure(Connection conn) {
        try {
            String logQuery = "INSERT INTO business_closure_log (closure_date) VALUES (CURRENT_TIMESTAMP)";
            PreparedStatement ps = conn.prepareStatement(logQuery);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException("Error logging business closure: " + e.getMessage());
        }
    }

    /**
     * Builds the inventory usage panel with filtering options.
     *
     * @param conn the database connection
     * @return a JPanel containing the inventory usage report interface
     */
    private static JPanel buildInventoryUsagePanel(Connection conn) {
        JPanel inventoryUsagePanel = new JPanel();
        inventoryUsagePanel.setLayout(new BorderLayout());

        DefaultTableModel inventoryUsageTableModel = new DefaultTableModel(
                new String[]{"Item ID", "Item Name", "Total Inventory Used"}, 0
        );

        JTable inventoryUsageTable = new JTable(inventoryUsageTableModel);
        JScrollPane inventoryUsageScrollPane = new JScrollPane(inventoryUsageTable);
        inventoryUsageTable.setRowHeight(30);
        inventoryUsageTable.setFont(new Font("Arial", Font.PLAIN, 12));
        inventoryUsageTable.setForeground(Color.DARK_GRAY);
        inventoryUsageTable.setBackground(Color.WHITE);
        inventoryUsageTable.setGridColor(Color.DARK_GRAY);

        inventoryUsageScrollPane.setPreferredSize(new Dimension(800, 500));

        // UI for Date Selection
        JPanel filterPanel = new JPanel(new FlowLayout());
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton filterButton = new JButton("Filter Inventory Usage");

        filterPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        filterPanel.add(endDateField);
        filterPanel.add(filterButton);

        filterButton.addActionListener(e -> {
            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();

            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                buildInventoryUsageTable(conn, inventoryUsageTableModel, startDate, endDate);
            } else {
                JOptionPane.showMessageDialog(null, "Please enter both start and end dates.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        inventoryUsagePanel.add(filterPanel, BorderLayout.NORTH);
        inventoryUsagePanel.add(inventoryUsageScrollPane, BorderLayout.CENTER);

        return inventoryUsagePanel;
    }

    /**
     * Populates the inventory usage table with data filtered by date range.
     *
     * @param conn the database connection
     * @param tableModel the table model to populate with inventory usage data
     * @param startDate the start date for the report period (format: YYYY-MM-DD)
     * @param endDate the end date for the report period (format: YYYY-MM-DD)
     */
    private static void buildInventoryUsageTable(Connection conn, DefaultTableModel tableModel, String startDate, String endDate) {
        // Clear the table before updating
        tableModel.setRowCount(0);

        // New Corrected SQL Query
        String query = "SELECT " +
                "    mii.item_id, " +
                "    i.item_name, " +
                "    SUM(mii.quantity_used * order_count) AS total_inventory_used " +
                "FROM " +
                "    menu_item_inventory mii " +
                "JOIN " +
                "    inventory i ON mii.item_id = i.item_id " +
                "JOIN " +
                "    (SELECT product_id, COUNT(order_id) AS order_count " +
                "     FROM customer_transaction " +
                "     WHERE purchase_date BETWEEN ? AND ? " +
                "     GROUP BY product_id) ct " +
                "ON mii.product_id = ct.product_id " +
                "GROUP BY " +
                "    mii.item_id, i.item_name " +
                "ORDER BY " +
                "    total_inventory_used DESC";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String itemId = String.valueOf(rs.getInt("item_id"));
                    String itemName = rs.getString("item_name");
                    String totalUsed = String.valueOf(rs.getInt("total_inventory_used"));

                    tableModel.addRow(new String[]{itemId, itemName, totalUsed});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading inventory usage data: " + e.getMessage());
        }
    }
}
