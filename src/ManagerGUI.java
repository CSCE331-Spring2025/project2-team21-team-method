import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

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

    private static void buildOrderPanel(Connection conn, JPanel orderPanel) {
        DefaultTableModel inventoryTableModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Amount Left"}, 0);
        JTable inventoryTable = new JTable(inventoryTableModel);
        JScrollPane inventoryTableScrollPane = new JScrollPane(inventoryTable);

        buildInventoryTable(conn, inventoryTableModel);

        // creating an area where the manager can add update and delete items

        JPanel modifyItemsPanel = new JPanel(new FlowLayout());
        JTextField id = new JTextField(3);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(20);

        modifyItemsPanel.add(new JLabel("Item ID"));
        modifyItemsPanel.add(id);

        modifyItemsPanel.add(new JLabel("Item name"));
        modifyItemsPanel.add(name);

        modifyItemsPanel.add(new JLabel("Item amount"));
        modifyItemsPanel.add(amount);

        modifyItemsPanel.add(new JLabel("Transaction Number"));
        modifyItemsPanel.add(transactionId);


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

        orderPanel.add(inventoryTableScrollPane, BorderLayout.CENTER);
        orderPanel.add(modifyItemsPanel, BorderLayout.SOUTH);
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

        addSectionToPanel(trendsPanel, "Weekly Sales History", weeklySales, gbc);
        addSectionToPanel(trendsPanel, "Realistic Sales History", realisticSales, gbc);
        addSectionToPanel(trendsPanel, "Peak Sales Day", peakSales, gbc);
        addSectionToPanel(trendsPanel, "Menu Item Inventory", menuInventory, gbc);
        addSectionToPanel(trendsPanel, "Most Popular Toppings", popularToppings, gbc);
        addSectionToPanel(trendsPanel, "Top Customers", topCustomers, gbc);
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

    private static void buildInventoryTable(Connection conn, DefaultTableModel inventoryTableModel) {
        inventoryTableModel.setRowCount(0);
        String query = "SELECT item_id, item_name, amount FROM inventory";
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

        String query = "SELECT  employee_id, emp_email,emp_phone FROM employee where employee_id = ?";

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
            ps.executeUpdate();
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


    //
    // AVI CHANGE FROM THE PART BELOW
    // employeeTableScrollPane.setPreferredSize(new Dimension(700, 200));
    //int employee_id, String emp_email, int emp_phone, boolean is_manager, int social_security, double emp_pay, int emp_bank_account

    private static void buildEmployeePanel(Connection conn, JPanel employeePanel) {
        DefaultTableModel employeeTableModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Email", "Employee Phone", "Is Manager", "Social Security", "Employee Pay", "Bank Account"}, 0
        );

        JTable employeeTable = new JTable(employeeTableModel);
        JScrollPane employeeTableScrollPane = new JScrollPane(employeeTable);
        employeeTableScrollPane.setPreferredSize(new Dimension(800, 200));


        // Populate table with employee data
        buildEmployeeTable(conn, employeeTableModel);

        // Creating an area where the manager can add, update, and delete employees
        JPanel modifyEmployeePanel = new JPanel(new FlowLayout());


        // Input fields for employee details
        JTextField employeeIdField = new JTextField(5);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(10);
        JCheckBox isManagerCheck = new JCheckBox("Manager");
        JTextField socialSecurityField = new JTextField(10);
        JTextField payField = new JTextField(10);
        JTextField bankAccountField = new JTextField(10);

        // Adding labels and fields to the panel
        modifyEmployeePanel.add(new JLabel("Employee ID:"));
        modifyEmployeePanel.add(employeeIdField);

        modifyEmployeePanel.add(new JLabel("Email:"));
        modifyEmployeePanel.add(emailField);

        modifyEmployeePanel.add(new JLabel("Phone:"));
        modifyEmployeePanel.add(phoneField);

        modifyEmployeePanel.add(new JLabel("Social Security:"));
        modifyEmployeePanel.add(socialSecurityField);

        modifyEmployeePanel.add(new JLabel("Pay:"));
        modifyEmployeePanel.add(payField);

        modifyEmployeePanel.add(new JLabel("Bank Account:"));
        modifyEmployeePanel.add(bankAccountField);

        modifyEmployeePanel.add(isManagerCheck);

        // ADD Button
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
        String query = "SELECT employee_id, emp_email, emp_phone, is_manager, social_security, emp_pay, emp_bank_account FROM employee";
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
                        "DELETE FROM inventory WHERE item_id = ?"
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
        // Create a DefaultTableModel with columns for product data
        DefaultTableModel productTableModel = new DefaultTableModel(
                new String[] {"Product ID", "Product Name", "Cost", "Type"}, 0
        );
        JTable productTable = new JTable(productTableModel);
        JScrollPane productTableScrollPane = new JScrollPane(productTable);

        // Populate the table with current product data
        refreshProductTable(conn, productTableModel);

        // Create a panel where the manager can insert or update products
        JPanel modifyProductsPanel = new JPanel(new FlowLayout());

        // Text fields for product ID, name, cost, and type
        JTextField idField = new JTextField(3);
        JTextField nameField = new JTextField(20);
        JTextField costField = new JTextField(5);
        JTextField typeField = new JTextField(10);

        // Add labels and fields to the modifyProductsPanel
        modifyProductsPanel.add(new JLabel("Product ID:"));
        modifyProductsPanel.add(idField);

        modifyProductsPanel.add(new JLabel("Name:"));
        modifyProductsPanel.add(nameField);

        modifyProductsPanel.add(new JLabel("Cost:"));
        modifyProductsPanel.add(costField);

        modifyProductsPanel.add(new JLabel("Type:"));
        modifyProductsPanel.add(typeField);

        // -----------------------------------------------------
        // INSERT button
        // -----------------------------------------------------
        JButton insertButton = new JButton("INSERT");
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Gather values from text fields
                String temp_id = idField.getText().trim();
                String temp_name = nameField.getText().trim();
                String temp_cost = costField.getText().trim();
                String temp_type = typeField.getText().trim();

                // Check if all fields are filled
                if (temp_id.isEmpty() || temp_name.isEmpty() || temp_cost.isEmpty() || temp_type.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields (ID, Name, Cost, Type).");
                    return;
                }

                // Convert numeric fields
                try {
                    int productId = Integer.parseInt(temp_id);
                    float cost = Float.parseFloat(temp_cost);

                    // Insert the product into the database
                    insertProductIntoDatabase(conn, productId, temp_name, cost, temp_type);

                    // Refresh the table
                    refreshProductTable(conn, productTableModel);

                    // Clear text fields
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

        // -----------------------------------------------------
        // UPDATE button
        // -----------------------------------------------------
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
                refreshProductTable(conn, productTableModel);

                // Optionally clear text fields
                idField.setText("");
                nameField.setText("");
                costField.setText("");
                typeField.setText("");
            }
        });
        modifyProductsPanel.add(updateButton);

        // -----------------------------------------------------
        // Assemble the productPanel
        // -----------------------------------------------------
        productPanel.setLayout(new BorderLayout());
        productPanel.add(productTableScrollPane, BorderLayout.CENTER);
        productPanel.add(modifyProductsPanel, BorderLayout.SOUTH);
    }

    // =====================================================
// This method refreshes the product table by selecting
// from the 'product' table and updating the table model.
// =====================================================
    private static void refreshProductTable(Connection conn, DefaultTableModel productTableModel) {
        // Clear existing rows
        productTableModel.setRowCount(0);

        String query = "SELECT product_id, product_name, product_cost, product_type FROM product";
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

    // =====================================================
// This method inserts a new product into the 'product'
// table. The user must provide valid ID, name, cost, type.
// =====================================================
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

    // =====================================================
// This method updates an existing product in the
// 'product' table, given its product_id. Any fields
// left blank will use the existing DB values.
// =====================================================
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

        // Load data from company_transaction
        refreshTrackingTable(conn, trackingTableModel);

        trackingPanel.setLayout(new BorderLayout());
        trackingPanel.add(trackingScrollPane, BorderLayout.CENTER);
    }

    private static void refreshTrackingTable(Connection conn, DefaultTableModel model) {
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


}
