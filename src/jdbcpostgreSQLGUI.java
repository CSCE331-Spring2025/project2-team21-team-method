import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class jdbcpostgreSQLGUI {
    private static Connection conn = null;

    public static void main(String args[]) {
        dbSetup my = new dbSetup();
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_21_db", my.user, my.pswd);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "Opened database successfully");

        JFrame managerDashboard = new JFrame("Manager Dashboard");
        managerDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        managerDashboard.setSize(800, 400);

        JTabbedPane tabsPane = new JTabbedPane();
        JPanel orderPanel = new JPanel();
        JPanel trackingPanel = new JPanel();
        JPanel trendsPanel = new JPanel();
        JPanel employeePanel = new JPanel();
        JPanel productPanel = new JPanel();

        tabsPane.addTab("Order Page", orderPanel);
        tabsPane.addTab("Tracking Page", trackingPanel);
        tabsPane.addTab("Trends Page", trendsPanel);
        tabsPane.addTab("Employee Page", employeePanel);
        tabsPane.addTab("Product Page", productPanel);

        managerDashboard.add(tabsPane, BorderLayout.CENTER);
        managerDashboard.setVisible(true);

        try {
            buildOrderPanel(conn, orderPanel);
            buildEmployeePanel(conn, employeePanel);
            buildProductPanel(conn, productPanel);
            buildTrackingPanel(conn, trackingPanel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }

        managerDashboard.addWindowListener(new WindowAdapter() {
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

    private static void buildInventoryTable(Connection conn, DefaultTableModel inventoryTableModel) {
        inventoryTableModel.setRowCount(0);
        String query = "SELECT item_id, item_name, amount FROM inventory";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String id = String.valueOf(rs.getInt(1));
                String name = rs.getString(2);
                String amount = String.valueOf(rs.getInt(3));
                inventoryTableModel.addRow(new String[]{id, name, amount});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading the inventory table.");
        }
    }

    private static void buildOrderPanel(Connection conn, JPanel orderPanel) {
        DefaultTableModel inventoryTableModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Amount Left"}, 0);
        JTable inventoryTable = new JTable(inventoryTableModel);
        JScrollPane inventoryTableScrollPane = new JScrollPane(inventoryTable);
        buildInventoryTable(conn, inventoryTableModel);

        JPanel modifyItemsPanel = new JPanel(new FlowLayout());
        JTextField id = new JTextField(3);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(20);

        modifyItemsPanel.add(new JLabel("Item ID"));
        modifyItemsPanel.add(id);
        modifyItemsPanel.add(new JLabel("Item Name"));
        modifyItemsPanel.add(name);
        modifyItemsPanel.add(new JLabel("Amount"));
        modifyItemsPanel.add(amount);
        modifyItemsPanel.add(new JLabel("Transaction Number"));
        modifyItemsPanel.add(transactionId);

        JButton addButton = new JButton("ADD");
        addButton.addActionListener(e -> {
            String temp_id = id.getText().trim();
            String temp_name = name.getText().trim();
            String temp_amount = amount.getText().trim();
            String temp_transactionId = transactionId.getText().trim();
            if (temp_id.isEmpty() || temp_name.isEmpty() || temp_amount.isEmpty() || temp_transactionId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields (ID, Name, Amount, Transaction ID). ");
                return;
            }
            int num_id = Integer.parseInt(temp_id);
            int num_amount = Integer.parseInt(temp_amount);
            int num_transactionId = Integer.parseInt(temp_transactionId);
            insertValueIntoDatabase(conn, num_id, temp_name, num_amount, num_transactionId);
            buildInventoryTable(conn, inventoryTableModel);
        });
        modifyItemsPanel.add(addButton);

        orderPanel.setLayout(new BorderLayout());
        orderPanel.add(inventoryTableScrollPane, BorderLayout.CENTER);
        orderPanel.add(modifyItemsPanel, BorderLayout.SOUTH);
    }
}
