import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TrendsGUI {
    private static Connection conn = null; // Ensure connection is initialized properly

    public static void main(String[] args) {
        dbSetup my = new dbSetup();

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_21_db",
                    my.user, my.pswd);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        JFrame frame = new JFrame("Restaurant Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new GridLayout(2, 3));

        frame.add(wrapPanel(fetchWeeklySales(), "Weekly Sales History"));
        frame.add(wrapPanel(fetchRealisticSales(), "Realistic Sales History"));
        frame.add(wrapPanel(fetchPeakSalesDay(), "Peak Sales Day"));
        frame.add(wrapPanel(fetchMenuInventory(), "Menu Item Inventory"));
        frame.add(wrapPanel(fetchMostPopularToppings(), "Most Popular Toppings"));
        frame.add(wrapPanel(fetchTopCustomers(), "Top Customers"));

        frame.setVisible(true);
    }

    private static JTextArea fetchWeeklySales() {
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
        } catch (SQLException e) {
            textArea.setText("Error loading weekly sales: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchRealisticSales() {
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
        } catch (SQLException e) {
            textArea.setText("Error loading realistic sales: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchPeakSalesDay() {
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
        } catch (SQLException e) {
            textArea.setText("Error loading peak sales day: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchMenuInventory() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String query = "SELECT count(*) FROM menu_item_inventory";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                textArea.setText("Total Menu Items in Inventory: " + rs.getInt(1));
            } else {
                textArea.setText("No menu inventory data available.");
            }
        } catch (SQLException e) {
            textArea.setText("Error loading menu inventory: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchMostPopularToppings() {
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
        } catch (SQLException e) {
            textArea.setText("Error loading most popular toppings: " + e.getMessage());
        }
        return textArea;
    }

    private static JTextArea fetchTopCustomers() {
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
        } catch (SQLException e) {
            textArea.setText("Error loading top customers: " + e.getMessage());
        }
        return textArea;
    }

    private static JPanel wrapPanel(JComponent component, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(component), BorderLayout.CENTER);
        return panel;
    }
}
