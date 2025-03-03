import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * @author eshaansaini
 */
public class ManagerGUI extends JPanel {
    public ManagerGUI(Connection conn) {
        //JFrame managerDashboard = new JFrame("Manager Dashboard");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
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
     * @param conn Connection for the Database
     * @param orderPanel The panel that contains the inventory table
     * @return nothing
     *
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
                BorderFactory.createEtchedBorder(),"Modify Inventory",
                TitledBorder.LEFT,TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);

        JTextField id = new JTextField(3);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(20);

        JLabel idLabel = new JLabel("ItemId");
        JLabel nameLabel = new JLabel("Item Name");
        JLabel amountLabel = new JLabel("Item Amount");
        JLabel transactionLabel = new JLabel("Transaction ID");

        gbc.gridx =0;
        gbc.gridy =0;
        modifyItemsPanel.add(idLabel, gbc);
        gbc.gridx =1;
        modifyItemsPanel.add(id,gbc);

        gbc.gridx =0;
        gbc.gridy =1;
        modifyItemsPanel.add(nameLabel, gbc);
        gbc.gridx =1;
        modifyItemsPanel.add(name,gbc);

        gbc.gridx =2;
        gbc.gridy =0;
        modifyItemsPanel.add(amountLabel, gbc);
        gbc.gridx =3;
        modifyItemsPanel.add(amount,gbc);

        gbc.gridx =2;
        gbc.gridy =1;
        modifyItemsPanel.add(transactionLabel, gbc);
        gbc.gridx =3;
        modifyItemsPanel.add(transactionId,gbc);

        gbc.gridx =1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JButton addButton = new JButton("ADD");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        modifyItemsPanel.add(addButton);

        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        modifyItemsPanel.add(updateButton);


        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    numAmount = Integer.parseInt(tempAmount);
                }
                if (!tempTransactionId.isEmpty()) {
                    numTransactionId = Integer.parseInt(tempTransactionId);
                }
                deleteValueIntoDatabase(conn, numId);
                buildInventoryTable(conn, inventoryTableModel);
            }
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

    private static JPanel buildTrendsPanel(Connection conn) {
        JPanel trendsPanel = new JPanel();
        trendsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // spacing
        gbc.gridx = 0; // Column index start at 0
        gbc.gridy = 0; // Row index start at 0
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow horizontal stretching where needed

        // stretch
        gbc.weightx = 1;
        gbc.weighty = 1;

        JTextArea weeklySales = fetchWeeklySales(conn);
        JTextArea realisticSales = fetchRealisticSales(conn);
        JTextArea peakSales = fetchPeakSalesDay(conn);
        JTextArea menuInventory = fetchMenuInventory(conn);
        JTextArea popularToppings = fetchMostPopularToppings(conn);
        JTextArea topCustomers = fetchTopCustomers(conn);

        // ================================
        // Sales Report GUI
        // ================================
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton filterButton = new JButton("Filter Sales");

        // Text area for sales data
        JTextArea salesByDateRange = new JTextArea();
        salesByDateRange.setEditable(false);
        salesByDateRange.setRows(5);

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
                JTextArea updatedSales = fetchSalesByDateRange(conn, startDate, endDate);
                salesByDateRange.setText(updatedSales.getText());
            } else {
                String message = "Please enter correct start and end dates.";
                salesByDateRange.setText(message);
                JOptionPane.showMessageDialog(null, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
            } // TODO: never gets called because of the logic error before this, in the function itself.
        });

        addSectionToPanel(trendsPanel, "Weekly Sales History", weeklySales, gbc);
        addSectionToPanel(trendsPanel, "Realistic Sales History", realisticSales, gbc);
        addSectionToPanel(trendsPanel, "Peak Sales Day", peakSales, gbc);
        addSectionToPanel(trendsPanel, "Menu Item Inventory", menuInventory, gbc);
        addSectionToPanel(trendsPanel, "Most Popular Toppings", popularToppings, gbc);
        addSectionToPanel(trendsPanel, "Top Customers", topCustomers, gbc);

        // Add date filter panel
        gbc.gridy++;
        gbc.gridwidth = 2; // allow two columns for date filter panel
        trendsPanel.add(dateFilterPanel, gbc);

        // Add Sales Report section
        gbc.gridy++;
        addSectionToPanel(trendsPanel, "Sales by Date Ranges", salesByDateRange, gbc);

        return trendsPanel;
    }

    /**
     * Adds a section to the trends grid with headers for each section
     */
    private static void addSectionToPanel(JPanel panel, String title, JTextArea textArea, GridBagConstraints gbc) {
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy++; // Move to the next row
        panel.add(headerLabel, gbc);

        // make scrollable but max of 5 rows displayed at once
        textArea.setEditable(false);
        textArea.setRows(5);
        JScrollPane scrollPane = new JScrollPane(textArea);

        gbc.gridy++; // Move to the next row for text area
        panel.add(scrollPane, gbc);
    }

    private static JTextArea fetchWeeklySales(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT date_trunc('week',purchase_date) as week_start, count(order_id) as num_orders FROM customer_transaction GROUP BY week_start ORDER BY week_start";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Week Start: ").append(rs.getString(1)).append(" - Orders: ").append(rs.getInt(2)).append("\n");
            }
            textArea.setText(sb.toString());
        }
        catch (SQLException e) {
            textArea.setText("Error loading weekly sales: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchRealisticSales(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT extract('hour' from ct.purchase_date) as hour, count(order_id) as orders, sum(p.product_cost) as sales FROM customer_transaction ct JOIN product p ON ct.product_id = p.product_id GROUP BY hour ORDER BY hour";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Hour: ").append(rs.getInt(1)).append(" - Orders: ").append(rs.getInt(2)).append(" - Sales: $").append(rs.getDouble(3)).append("\n");
            }
            textArea.setText(sb.toString());
        }
        catch (SQLException e) {
            textArea.setText("Error loading realistic sales: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchPeakSalesDay(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT date(purchase_date) as date, sum(p.product_cost) as sales FROM customer_transaction ct JOIN product p ON ct.product_id = p.product_id GROUP BY date ORDER BY sales DESC LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Date: ").append(rs.getString(1)).append(" - Sales: $").append(rs.getDouble(2)).append("\n");
            }
            textArea.setText(sb.toString());
        }
        catch (SQLException e) {
            textArea.setText("Error loading peak sales day: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchMenuInventory(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT count(*) FROM menu_item_inventory";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                textArea.setText("Total Menu Items in Inventory: " + rs.getInt(1));
            }
            else {
                textArea.setText("No menu inventory data available.");
            }
        }
        catch (SQLException e) {
            textArea.setText("Error loading menu inventory: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchMostPopularToppings(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT topping_type, COUNT(topping_type) FROM customer_transaction GROUP BY topping_type";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Topping: ").append(rs.getString(1)).append(" - Count: ").append(rs.getInt(2)).append("\n");
            }
            textArea.setText(sb.toString());
        }
        catch (SQLException e) {
            textArea.setText("Error loading most popular toppings: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchTopCustomers(Connection conn) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT cr.customer_id, cr.points, cr.email FROM customer_reward cr ORDER BY cr.points DESC LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Customer ID: ").append(rs.getInt(1)).append(" - Points: ").append(rs.getInt(2)).append(" - Email: ").append(rs.getString(3)).append("\n");
            }
            textArea.setText(sb.toString());
        }
        catch (SQLException e) {
            textArea.setText("Error loading top customers: " + e.getMessage());
        }
        return textArea;
    }

    /**
     * Creates the logic for Sales Report, where I list out all drinks sales from a beginning date to the end of the provided date.
     *
     * @param conn - Connection to the database.
     * @param startDate - The first input date to beginning the search from this start date.
     * @param endDate - The second input date to end the search from this end date.
     * @return A text area formatted as a table to dislpay all products and its total cost, and quantity sold.
     */
    private static JTextArea fetchSalesByDateRange(Connection conn, String startDate, String endDate) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        String query =  "SELECT ct.product_id, p.product_name, p.product_type, " +
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

                textArea.setText(sb.length() > 0 ? sb.toString() : "No sales data found for the selected date range.");
            }
        } catch (SQLException e) {
            textArea.setText("Error retrieving sales data: " + e.getMessage());
        } // TODO: never gets called or shown when I throw invalid arguments.

        return textArea;
    }




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
            JOptionPane.showMessageDialog(null, "Error laoding the inventory table");
        }
    }

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

    private static void updateValueIntoDatabase(Connection conn, int id, String name, int amount, int transactionId) {

        String query = "SELECT  item_name,amount,transaction_id FROM inventory where item_id = ?";

        String prevName="";
        int prevAmount=-1;
        int prevTransaction=-1;
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    prevName = rs.getString(1);
                    prevAmount = rs.getInt(2);
                    prevTransaction = rs.getInt(3);
                }
                else{
                    JOptionPane.showMessageDialog(null, "No item found with ID: " + id);
                }
            }
        } catch (SQLException e) {
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
                BorderFactory.createEtchedBorder(),"Modify EmployeeInfo",
                TitledBorder.LEFT,TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);

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



        gbc.gridx =0;
        gbc.gridy =0;
        modifyEmployeePanel.add(idLabel, gbc);
        gbc.gridx =1;
        modifyEmployeePanel.add(employeeIdField,gbc);

        gbc.gridx =0;
        gbc.gridy =1;
        modifyEmployeePanel.add(emailLabel, gbc);
        gbc.gridx =1;
        modifyEmployeePanel.add(emailField,gbc);

        gbc.gridx =2;
        gbc.gridy =0;
        modifyEmployeePanel.add(phoneLabel, gbc);
        gbc.gridx =3;
        modifyEmployeePanel.add(phoneField,gbc);

        gbc.gridx =2;
        gbc.gridy =1;
        modifyEmployeePanel.add(socialLabel, gbc);
        gbc.gridx =3;
        modifyEmployeePanel.add(socialSecurityField,gbc);

        gbc.gridx =4;
        gbc.gridy =0;
        modifyEmployeePanel.add(payLabel, gbc);
        gbc.gridx =5;
        modifyEmployeePanel.add(payField,gbc);

        gbc.gridx =4;
        gbc.gridy =1;
        modifyEmployeePanel.add(bankLabel, gbc);
        gbc.gridx =5;
        modifyEmployeePanel.add(bankAccountField,gbc);

        gbc.gridx =4;
        gbc.gridy =2;
        modifyEmployeePanel.add(managerLabel, gbc);
        gbc.gridx =5;
        modifyEmployeePanel.add(isManagerCheck,gbc);

        JButton addButton = new JButton("ADD");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        modifyEmployeePanel.add(addButton);


        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        modifyEmployeePanel.add(updateButton);


        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempId = employeeIdField.getText().trim();
                if (tempId.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter the Employee ID.");
                    return;
                }

                int employeeId = Integer.parseInt(tempId);
                deleteEmpIntoDatabase(conn, employeeId);
                buildEmployeeTable(conn, employeeTableModel);
            }
        });
        modifyEmployeePanel.add(deleteButton);

        // Adding components to the Employee Panel
        employeePanel.add(employeeTableScrollPane, BorderLayout.CENTER);
        employeePanel.add(modifyEmployeePanel, BorderLayout.SOUTH);
    }

    //modify this part
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

    //Modify this part
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

    //Modify this part
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

    //modify this part
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
    private static void buildProductPanel(Connection conn, JPanel productPanel) {

        DefaultTableModel productTableModel = new DefaultTableModel(
                new String[] {"Product ID", "Product Name", "Cost", "Type"}, 0
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
                BorderFactory.createEtchedBorder(),"Modify Products",
                TitledBorder.LEFT,TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);

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
        gbc.gridx =0;
        gbc.gridy =0;
        modifyProductsPanel.add(prodLabel, gbc);
        gbc.gridx =1;
        modifyProductsPanel.add(idField,gbc);

        gbc.gridx =0;
        gbc.gridy =1;
        modifyProductsPanel.add(prodName, gbc);
        gbc.gridx =1;
        modifyProductsPanel.add(nameField,gbc);

        gbc.gridx =2;
        gbc.gridy =0;
        modifyProductsPanel.add(prodCost, gbc);
        gbc.gridx =3;
        modifyProductsPanel.add(costField,gbc);

        gbc.gridx =2;
        gbc.gridy =1;
        modifyProductsPanel.add(prodType, gbc);
        gbc.gridx =3;
        modifyProductsPanel.add(typeField,gbc);





        JButton insertButton = new JButton("INSERT");
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Product ID must be an integer and Cost must be a valid number."
                    );
                }
            }
        });
        modifyProductsPanel.add(insertButton);

        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Product ID must be an integer.");
                    return;
                }

                // If cost field is not empty, parse it
                if (!temp_cost.isEmpty()) {
                    try {
                        costVal = Float.parseFloat(temp_cost);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Cost must be a valid number.");
                        return;
                    }
                }

                // Retrieve current DB values if fields are left blank
                String prevName = "";
                float prevCost = -1;
                String prevType = "";
                String selectQuery = "SELECT product_name, product_cost, product_type FROM product WHERE product_id = ?";

                try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            prevName = rs.getString("product_name");
                            prevCost = rs.getFloat("product_cost");
                            prevType = rs.getString("product_type");
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "No product found with ID: " + productId
                            );
                            return;
                        }
                    }
                } catch (SQLException ex) {
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
            }
        });
        modifyProductsPanel.add(updateButton);

        JPanel modifyItemsPanel = new JPanel(new GridBagLayout());
        modifyItemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),"Modify Inventory",
                TitledBorder.LEFT,TitledBorder.TOP,
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

        gbc.gridx =0;
        gbc.gridy =0;
        modifyItemsPanel.add(idLabel, gbc);
        gbc.gridx =1;
        modifyItemsPanel.add(id,gbc);

        gbc.gridx =0;
        gbc.gridy =1;
        modifyItemsPanel.add(nameLabel, gbc);
        gbc.gridx =1;
        modifyItemsPanel.add(name,gbc);

        gbc.gridx =2;
        gbc.gridy =0;
        modifyItemsPanel.add(amountLabel, gbc);
        gbc.gridx =3;
        modifyItemsPanel.add(amount,gbc);

        gbc.gridx =2;
        gbc.gridy =1;
        modifyItemsPanel.add(transactionLabel, gbc);
        gbc.gridx =3;
        modifyItemsPanel.add(transactionId,gbc);



        JButton addButton = new JButton("INSERT");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

            }
        });
        modifyItemsPanel.add(addButton);

        JButton updateButton1 = new JButton("UPDATE");
        updateButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        modifyItemsPanel.add(updateButton1);


        // this is the menu inventory part GAHHAHHHA
        JPanel modifyMenuInventoryPanel = new JPanel(new GridBagLayout());
        modifyMenuInventoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),"Modify Menu Inventory Table",
                TitledBorder.LEFT,TitledBorder.TOP,
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

        gbc.gridx =0;
        gbc.gridy =0;
        modifyMenuInventoryPanel.add(headingLabel, gbc);

        gbc.gridx =0;
        gbc.gridy =1;
        modifyMenuInventoryPanel.add(prodLabel1, gbc);
        gbc.gridx =1;
        modifyMenuInventoryPanel.add(prodId1, gbc);

        gbc.gridx =0;
        gbc.gridy =2;
        modifyMenuInventoryPanel.add(itemIdLabel, gbc);
        gbc.gridx =1;
        modifyMenuInventoryPanel.add(items, gbc);

        gbc.gridx =0;
        gbc.gridy =3;
        modifyMenuInventoryPanel.add(quantityLabel, gbc);
        gbc.gridx =1;
        modifyMenuInventoryPanel.add(quantities, gbc);

        //change here
        JButton addButtonMenu = new JButton("INSERT");
        addButtonMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempProdId = prodId1.getText().trim();
                String tempItems = items.getText().trim();
                String tempQuantity = quantities.getText().trim();


                if (tempProdId.isEmpty() || tempItems.isEmpty() || tempQuantity.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields");
                    return;
                }

                String [] tempItemsArray = tempItems.split(",");
                String [] tempQuantArray = tempQuantity.split(",");

                int [] itemsArray = new int[tempItemsArray.length];
                int [] quantityArray = new int[tempQuantArray.length];

                int numId = Integer.parseInt(tempProdId);

                for(int i = 0 ; i < tempItemsArray.length;i++){
                    itemsArray[i] = Integer.parseInt(tempItemsArray[i].trim());
                    quantityArray[i] = Integer.parseInt(tempQuantArray[i].trim());
                    insertValueIntoMenuTable(conn, numId, itemsArray[i], quantityArray[i]);
                }
                JOptionPane.showMessageDialog(null, "Product and its respective menu items inserted successfully!");
            }
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
                productTableModel.addRow(new String[] {id, name, cost, type});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading product table: " + e.getMessage()
            );
        }
    }


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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error inserting product: " + e.getMessage()
            );
        }
    }


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
            } else {
                JOptionPane.showMessageDialog(null, "No product found with ID: " + productId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error updating product: " + e.getMessage()
            );
        }
    }
    private static void buildTrackingPanel(Connection conn, JPanel trackingPanel) {
        // Create a table model for tracking data
        DefaultTableModel trackingTableModel = new DefaultTableModel(
                new String[] {"Transaction ID", "Tracking Number", "Vendor ID", "Estimated Delivery"}, 0
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

    private static void buildTrackingTable(Connection conn, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT transaction_id, tracking_number, vendor_id, estimated_delivery FROM company_transaction";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                String tID = String.valueOf(rs.getInt("transaction_id"));
                String tNumber = String.valueOf(rs.getInt("tracking_number"));
                String vID = String.valueOf(rs.getInt("vendor_id"));
                // If estimated_delivery is a date or timestamp, you can do:
                String estDelivery = String.valueOf(rs.getObject("estimated_delivery"));
                // Or simply rs.getString("estimated_delivery") if it's a text column

                model.addRow(new String[] {tID, tNumber, vID, estDelivery});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading tracking table: " + e.getMessage());
        }
    }
    private static void insertValueIntoMenuTable(Connection conn, int prodId, int itemId, int quantity){
        {
            String insertSQL = "INSERT INTO menu_item_inventory (product_id, item_id, quantity_used) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, prodId);
                ps.setInt(2, itemId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Error inserting product: " + e.getMessage()
                );
            }
        }
    }

    // Z-report pre-req
    private static void closeBusinessWorkflow(Connection conn) {
        String report = generateZReport(conn);

        int confirm = JOptionPane.showConfirmDialog(null, report + "\n\nConfirm close of business?",
                "Close Business Day", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resetXReport();

                JOptionPane.showMessageDialog(null, "Business day over. Shutting down...");
                System.exit(0);
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, "Error closing business: " + e.getMessage());
            }
        }
    }

    private static void resetXReport() {
        clearTransactions();
    }

    public static void clearTransactions() {
        CashierGUI.currentTransactionList.clear();
        CashierGUI.currentTransactionModel.setRowCount(0);
    }

    /* TODO: Add more queries for more results */
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

        } catch (SQLException e) {
            return "Error generating Z-Report: " + e.getMessage();
        }

        return report.toString();
    }

    private static void logBusinessClosure(Connection conn) {
        try {
            String logQuery = "INSERT INTO business_closure_log (closure_date, closure_status) VALUES (CURRENT_TIMESTAMP, 'Closed')";
            PreparedStatement ps = conn.prepareStatement(logQuery);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error logging business closure: " + e.getMessage());
        }
    }
}
